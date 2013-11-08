/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco.cp.model.managers.constraints.global;

import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.regular.Regular;
import choco.kernel.common.Constant;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.automaton.DFA;
import choco.kernel.model.constraints.automaton.Transition;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/*
 *  ______
 * (__  __)
 *    ||
 *   /__\                  Choco manager
 *    \                    =============
 *    \                      Aug. 2008
 *    \                  Regular constraint
 *    \
 *    |
 */

/**
 * A manager to build new regular constraint
 */
public final class RegularManager extends IntConstraintManager {
    public SConstraint makeConstraint(Solver solver, IntegerVariable[] vars, Object parameters, List<String> options) {
        if (solver instanceof CPSolver) {
            IntDomainVar[] variables = solver.getVar((IntegerVariable[]) vars);
            if (parameters instanceof int[][]) {
                int[] coefs = ((int[][]) parameters)[0];
                int value = ((int[][]) parameters)[1][0];
                return knapsack(solver, vars, value, coefs);
            } else if (parameters instanceof Object[]) { // List of tuples, min and max lists
                Object[] params = (Object[]) parameters;
                return new Regular(new DFA((List<int[]>) params[0], (int[]) params[1], (int[]) params[2]), variables, solver.getEnvironment());
            } else if (parameters instanceof List) { // List of tuples
                return new Regular(new DFA((List<int[]>) parameters), variables, solver.getEnvironment());
            } else if (parameters instanceof DFA) { // Direct DFA
                return new Regular((DFA) parameters, variables, solver.getEnvironment());
            } else if (parameters instanceof String) { // Regexp
                return new Regular(new DFA((String) parameters, vars.length), variables, solver.getEnvironment());
            }
        }
        throw new ModelException("Could not found a constraint manager in " + this.getClass() + " !");
    }

    public int[] getFavoriteDomains(List<String> options) {
        return new int[]{
                IntDomainVar.BITSET,
                IntDomainVar.LINKEDLIST,
                IntDomainVar.BINARYTREE};
    }


    private int nID;

    /**
     * @param vars   Boolean variables
     * @param goal   Sum
     * @param coeffs An array Containing the coefficients
     * @return
     */
    public SConstraint knapsack(Solver s, IntegerVariable[] vars, int goal, int[] coeffs) {
        boolean tuples = false;
        int n = coeffs.length;
        // first check bounds, coefficients and goal
        if (goal < 0) {
            tuples = true;
        }
        for (int i = 0; !tuples && i < n; i++) {
            if (coeffs[i] < 0 || vars[i].getLowB() * coeffs[i] < 0 || vars[i].getUppB() * coeffs[i] < 0) {
                tuples = true;
            }
        }

        if (tuples) {

            List<int[]> ts = new ArrayList<int[]>();
            explore(0, 0, new int[vars.length], vars, coeffs, goal, ts);
            if (ts.size() == 0) {
                return Constant.FALSE;// not satisfiable
            }
            DFA dfa = new DFA(ts);
            return new Regular(dfa, s.getVar(vars), s.getEnvironment());
        } else {

            int[] temp = new int[coeffs.length + 1];
            System.arraycopy(coeffs, 0, temp, 1, coeffs.length);


            temp[0] = 0;
            coeffs = temp;
            int[][] P = new int[coeffs.length][goal + 1];
            int[][] G = new int[coeffs.length][goal + 1]; // table containing minimum solution paths

            for (int i = 0; i < P.length; ++i)
                for (int j = 0; j < P[0].length; ++j)
                    P[i][j] = G[i][j] = 0;

            P[0][0] = 1;

            for (int i = 1; i < P.length; ++i)
                for (int b = 0; b < goal + 1; ++b)
                    if (P[i - 1][b] == 1) {
                        DisposableIntIterator it = vars[i - 1].getDomainIterator();
                        while (it.hasNext()) {
                            int value = it.next();
                            //for (int x = bools[i - 1].getLowB(); x <= bools[i - 1].getUppB(); ++x)
                            if (b + coeffs[i] * value <= goal)
                                P[i][b + coeffs[i] * value] = 1;
                        }
                        it.dispose();
                    }

            boolean sat = false;
            for (int i = goal; i >= goal; --i)
                sat |= (P[P.length - 1][goal] == 1);

            G[G.length - 1][goal] = 1;
            if (sat) {
                for (int i = G.length - 2; i >= 0; --i)
                    for (int b = 0; b < G[0].length; b++)
                        if (G[i + 1][b] == 1) {
                            DisposableIntIterator it = vars[i].getDomainIterator();
                            while (it.hasNext()) {
                                int x = it.next();
                                //for (int x = bools[i].getLowB(); x <= bools[i].getUppB(); ++x)
                                if (b - coeffs[i + 1] * x >= 0 && P[i][b - coeffs[i + 1] * x] == 1)
                                    G[i][b - coeffs[i + 1] * x] = 1;
                            }
                            it.dispose();
                        }

                List<Transition> t = new LinkedList<Transition>();
                List<Integer> ints = new LinkedList<Integer>();
                nID = 0;
                int[][] labels = new int[coeffs.length][goal + 1];
                for (int i = 0; i < labels.length; ++i) {
                    Arrays.fill(labels[i], -1);
                }
                generateTransitionList(0, 0, t, labels, coeffs, G, vars);
                for (int i = 0; i <= 0; ++i) {
                    ints.add(G.length + i - 1);
                }
                DFA dfa = new DFA(t, ints, ints.get(0));
                return new Regular(dfa, s.getVar(vars), s.getEnvironment());
            } else {
                return Constant.FALSE;// not satisfiable
            }
        }
    }

    private void explore(int vidx, int sum, int[] path, IntegerVariable[] vars, int[] coeffs, int goal, List<int[]> ts) {
        if (vidx == vars.length) {
            if (sum == goal) {
                ts.add(path.clone());
            }
            return;
        }
        DisposableIntIterator it = vars[vidx].getDomainIterator();
        while (it.hasNext()) {
            path[vidx] = it.next();
            explore(vidx + 1, sum + coeffs[vidx] * path[vidx], path, vars, coeffs, goal, ts);
        }
        it.dispose();
    }

    /**
     * Generates the list of transitions
     *
     * @param x      X-ord in grid G
     * @param y      Y-ord in grid G
     * @param t      List of transitions
     * @param labels Table containing all nodes previously labeled
     * @param A      Array of coeffieients
     * @param G      Table containg minimum solution paths
     * @param bools  list of variables (not always bools)
     */
    private void generateTransitionList(int x, int y, List<Transition> t,
                                        int[][] labels, int[] A, int[][] G, IntegerVariable[] bools) {
        if (x >= G.length - 1)
            return;
        int[] vars = bools[x].getValues();
        if (vars == null) {
            for (int var = bools[x].getLowB(); var <= bools[x].getUppB(); ++var)
                if (y + A[x + 1] * var < G[0].length && G[x + 1][y + A[x + 1] * var] == 1) {
                    if (labels[x][y] == -1)
                        labels[x][y] = nID++;
                    if (labels[x + 1][y + A[x + 1] * var] == -1)
                        labels[x + 1][y + A[x + 1] * var] = nID++;
                    t.add(new Transition(labels[x][y], var, labels[x + 1][y + A[x + 1] * var]));
                    generateTransitionList(x + 1, y + A[x + 1] * var, t, labels, A, G, bools);
                }
        } else {
            for (int var = 0; var < vars.length; ++var) {
                if (y + A[x + 1] * vars[var] < G[0].length && G[x + 1][y + A[x + 1] * vars[var]] == 1) {
                    if (labels[x][y] == -1)
                        labels[x][y] = nID++;
                    if (labels[x + 1][y + A[x + 1] * vars[var]] == -1)
                        labels[x + 1][y + A[x + 1] * vars[var]] = nID++;
                    t.add(new Transition(labels[x][y], vars[var],
                            labels[x + 1][y + A[x + 1] * vars[var]]));
                    generateTransitionList(x + 1, y + A[x + 1] * vars[var], t, labels, A, G, bools);
                }
            }
        }
    }

}

