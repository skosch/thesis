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

package samples.tutorials.to_sort.tsp;

import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.CPSolver;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MinSpanningTree extends AbstractLargeIntSConstraint {

    public static class MinSpanningTreeManager extends IntConstraintManager {
        public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables, Object parameters, List<String> options) {
            if(solver instanceof CPSolver){
                Object[] p = (Object[]) parameters;
                int[][] dist = (int[][])(p[0]);
                IntDomainVar[] vars = solver.getVar(variables);
                return new MinSpanningTree(vars, dist, solver.getEnvironment());
            }

            return null;
        }
    }

    protected int n;
    protected IntDomainVar objective;
    protected IntDomainVar[] s;
    protected IStateInt[][] dist;
    protected IStateInt lowerBound;

    public MinSpanningTree(IntDomainVar[] allVars, int[][] dist, IEnvironment environment) {
        super(allVars);
        this.n = allVars.length - 1;
        this.s = new IntDomainVar[n];
        System.arraycopy(allVars, 0, this.s, 0, allVars.length - 1);
        this.objective = allVars[n];
        this.dist = new IStateInt[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                this.dist[i][j] = environment.makeInt(dist[i][j]);
            }
        }
        this.lowerBound = environment.makeInt(0);
    }

    public int arpm() {
        int cost = 0;
        List<Set<Integer>> classes = new ArrayList<Set<Integer>>();
        for (int i = 0; i < n; i++) {
            Set<Integer> set = new HashSet<Integer>();
            set.add(i);
            classes.add(set);
        }
        for (int i = 0; i < n; i++) {
            if (s[i].isInstantiated() && i != n-1) {
                int j = s[i].getVal();
                cost += dist[i][j].get();
                //LOGGER.info(showSet(classes));
                Set<Integer> itree = new HashSet<Integer>();
                Set<Integer> jtree = new HashSet<Integer>();
                for (Set<Integer> set : classes) {
                    if (set.contains(i)) itree = set;
                    if (set.contains(j)) jtree = set;
                }
                itree.addAll(jtree);
                classes.remove(jtree);
                //LOGGER.info(showSet(classes));
                //LOGGER.info("---------");
            }

        }
        for (int i = 0; i < n; i++) {
            if (!s[i].isInstantiated()) {
                int ci = Integer.MAX_VALUE;
                int j = -1;
                DisposableIntIterator it = s[i].getDomain().getIterator();
                while (it.hasNext()) {
                    int tmp = it.next();
                    if (ci > dist[i][tmp].get()) {
                        boolean cycle = false;
                        for (Set<Integer> set : classes) {
                            if (set.contains(i) && set.contains(tmp)) cycle = true;
                        }
                        if (!cycle) {
                            j = tmp;
                            ci = dist[i][j].get();
                        }
                    }
                }
                it.dispose();
                if (j > -1) {
                    cost += ci;
                    //LOGGER.info(showSet(classes));
                    Set<Integer> itree = new HashSet<Integer>();
                    Set<Integer> jtree = new HashSet<Integer>();
                    for (Set<Integer> set : classes) {
                        if (set.contains(i)) itree = set;
                        if (set.contains(j)) jtree = set;
                    }
                    itree.addAll(jtree);
                    classes.remove(jtree);
                    //LOGGER.info(showSet(classes));
                    //LOGGER.info("---------");
                }
            }
        }
        return cost;
    }

    public String showSet(List<Set<Integer>> classes) {
        String s = "{ ";
        for (Set<Integer> set : classes) {
            s += set.toString() + " ";
        }
        s += "}";
        return s;
    }

    public void awake() throws ContradictionException {
        propagate();
    }

    public void propagate() throws ContradictionException {
        lowerBound.set(arpm() + dist[n - 1][0].get());
        //LOGGER.info("lb = " + lowerBound.get());
        if (lowerBound.get() > objective.getInf()) {
            objective.updateInf(lowerBound.get(), this, false);
        }
        // filtrer de la borne inf vers les variables s[]
        // m�moriser un arpm et pour chaque arc maybe, l'injecter dans l'arpm, mettre � jour l'arpm
        // si le poids du nouvel arpm > � ancien arpm alors supprimer l'arc maybe consid�r�
    }
                                                             
    public void awakeOnInst(int u) throws ContradictionException {
        this.constAwake(false);
    }

    public void awakeOnInf(int u) throws ContradictionException {
        this.constAwake(false);
    }

    public void awakeOnSup(int u) throws ContradictionException {
        this.constAwake(false);
    }

    public void awakeOnBounds(int u) throws ContradictionException {
        this.constAwake(false);
    }

    public void awakeOnRem(int u, int v) throws ContradictionException {
        this.constAwake(false);
    }

    public void awakeOnRemovals(int u, DisposableIntIterator deltaDomain) throws ContradictionException {
        this.constAwake(false);
    }

    public boolean isSatisfied() {
        return false;
    }
}
