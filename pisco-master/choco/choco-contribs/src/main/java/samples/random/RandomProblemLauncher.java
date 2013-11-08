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

package samples.random;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.strong.DomOverDDegRPC;
import choco.cp.solver.constraints.strong.DomOverWDegRPC;
import choco.cp.solver.constraints.strong.StrongConsistencyManager;
import choco.cp.solver.constraints.strong.maxrpcrm.MaxRPCrm;
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraintWithSubConstraints;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;

import static java.lang.System.out;
import static java.text.MessageFormat.format;
import java.util.*;

public class RandomProblemLauncher {

    // protected final static Logger LOGGER = ChocoLogging.getSamplesLogger();

    private static int NBINSTANCES = -1;

    private static int TIMEOUT = -1;

    private enum Heuristic {
        DDEG, WDEG, PROPAGATE
    }

    private enum Filter {
        AC, MaxRPCLight, MaxRPC;

        public String toString() {
            switch (this) {
            case AC:
                return "a";
            case MaxRPCLight:
                return "l";
            case MaxRPC:
                return "m";

            default:
                throw new IllegalStateException();
            }
        }
    }

    private static final List<Filter> TEST = Arrays.asList(Filter.AC,
            Filter.MaxRPC, Filter.MaxRPCLight);

    private RandomProblemLauncher() {

    }

    public static void main(String args[]) {
        final int nbVar;
        final int nbVal;
        final double density;
        final int e;
        final long seed;
        final boolean force;

        final double tightness1;
        final double tightness2;
        final double increment;

        final Heuristic heuristic;

        // final Filter filter;
        try {
            int i = 0;
            nbVar = Integer.valueOf(args[i++]);
            nbVal = Integer.valueOf(args[i++]);

            density = Double.valueOf(args[i++]);

            tightness1 = Double.valueOf(args[i++]);
            tightness2 = Double.valueOf(args[i++]);
            increment = Double.valueOf(args[i++]);

            force = Boolean.valueOf(args[i++]);

            seed = Long.valueOf(args[i++]);

            heuristic = Heuristic.valueOf(args[i++]);

            // filter = Filter.valueOf(args[i++]);

            TIMEOUT = 1000 * Integer.valueOf(args[i++]);

            NBINSTANCES = Integer.valueOf(args[i++]);

        } catch (Exception exception) {
            out.println(exception.getMessage());
            out
                    .println("Usage : RandomProblem nbVar nbVal density tightness1 tightness2 increment force seed heuristic timeout nbinstances");
            return;
        }

        e = RandomProblem.nbArcs(nbVar, density);

        for (double tightness = tightness1; tightness <= tightness2; tightness += increment) {
            out.println("-------------------");
            out.println(nbVar + " var, " + nbVal + " val, " + density
                    + " density (" + e + " cstr), " + tightness + " tightness"
                    + (force ? ", forced" : "") + " (pcr = "
                    + RandomProblem.criticTightness(nbVar, nbVal, e) + "), "
                    + heuristic);

            test(nbVar, nbVal, e, tightness, seed, force, heuristic);
        }
    }

    private static void test(int nbVar, int nbVal, int nbCons,
            double tightness, long seed, boolean force, Heuristic heuristic) {
        final Map<Filter, List<Integer>> nodes = new HashMap<Filter, List<Integer>>(
                Filter.values().length);
        final Map<Filter, List<Double>> cpu = new HashMap<Filter, List<Double>>(
                Filter.values().length);
        final List<Integer> nbCliques = new ArrayList<Integer>(NBINSTANCES);
        final Map<Filter, List<Integer>> nbAwakes = new HashMap<Filter, List<Integer>>(
                Filter.values().length);
        final Map<Filter, List<Long>> mem = new HashMap<Filter, List<Long>>(
                Filter.values().length);
        // final double[] nps = new double[NBINSTANCES - 1];

        for (Filter f : Filter.values()) {
            nodes.put(f, new ArrayList<Integer>(NBINSTANCES));
            cpu.put(f, new ArrayList<Double>(NBINSTANCES));
            nbAwakes.put(f, new ArrayList<Integer>(NBINSTANCES));
            mem.put(f, new ArrayList<Long>(NBINSTANCES));
        }

        for (int i = NBINSTANCES; --i >= 0;) {

            out.print("g");
            final RandomProblem problem = new RandomProblem(nbVar, nbVal,
                    nbCons, tightness, seed + i, force);

            final List<Constraint> constraints = new ArrayList<Constraint>(
                    problem.getConstraints());

            nbCliques.add(cliques(constraints).size());

            for (Filter filter : TEST) {
                out.print(filter);
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                System.gc();
                // Build a model
                final Model m = new CPModel();

                m.addVariables(problem.getVariables());

                switch (filter) {
                case MaxRPC:
                case MaxRPCLight:
                    for (Constraint c : constraints) {
                        c.getOptions().clear();
                        c.addOption(Options.C_EXT_AC3);
                    }
                    final Constraint cc = new ComponentConstraintWithSubConstraints(
                            StrongConsistencyManager.class, problem
                                    .getVariables(), MaxRPCrm.class,
                            constraints.toArray(new Constraint[constraints
                                    .size()]));
                    if (filter == Filter.MaxRPCLight) {
                        cc.addOption("light");
                    }
                    m.addConstraint(cc);
                    break;
                default:
                    for (Constraint c : constraints) {
                        c.getOptions().clear();
                        c.addOption(Options.C_EXT_AC32);
                        m.addConstraint(c);
                    }
                }

                // out.println("Building solver...");

                // Build a solver
                final Solver s = new CPSolver();
                // CPSolver.setVerbosity(CPSolver.SEARCH);
                // Read the model
                s.read(m);
                if (TIMEOUT > 0) {
                    s.setTimeLimit(TIMEOUT);
                }
                // out.println("Solving...");
                Boolean result;

                long time;

                if (heuristic.equals(Heuristic.PROPAGATE)) {
                    time = -System.nanoTime();
                    try {
                        s.propagate();
                        result = Boolean.TRUE;
                    } catch (ContradictionException e) {
                        result = Boolean.FALSE;
                    }
                    time += System.nanoTime();
                } else {
                    switch (heuristic) {
                    case WDEG:
                        // s.attachGoal(new DomOverWDegBranching(s, new
                        // MinVal()));
                        s.attachGoal(new AssignOrForbidIntVarVal(
                                new DomOverWDegRPC(s), new MinVal()));

                        // s.setVarIntSelector(new DomOverWDegRPC(s));
                        // s
                        // .attachGoal(new
                        // choco.cp.solver.search.integer
                        // .branching.DomOverWDegBranching(
                        // s, new MinVal()));
                        // s
                        // .attachGoal(new AssignVar(new DomOverWDeg(s),
                        // new MinVal()));
                        // s.attachGoal(new DomOverWDegBranching(s,
                        // new IncreasingDomain()));
                    default:
                        s.attachGoal(new AssignOrForbidIntVarVal(
                                new DomOverDDegRPC(s), new MinVal()));
                    }

                    result = s.solve();
                    time = (long) s.getTimeCount() * 1000000l;
                }

                // final Boolean result = s.solve();

                mem.get(filter).add(
                        Runtime.getRuntime().totalMemory()
                                - Runtime.getRuntime().freeMemory());
                // CPSolver.flushLogs();

                if (result == null) {
                    out.print("*");
                    // nodes[i] = Integer.MAX_VALUE;
                    cpu.get(filter).add(Double.POSITIVE_INFINITY);
                    nbAwakes.get(filter).add(Integer.MAX_VALUE);
                    nodes.get(filter).add(Integer.MAX_VALUE);
                } else {
                    out.print(result ? 1 : 0);
                    try {
                        nodes.get(filter).add(s.getNodeCount());
                    } catch (Exception e) {
                        nodes.get(filter).add(0);
                    }
                    cpu.get(filter).add(time / 1e9d);
                    nbAwakes.get(filter).add(MaxRPCrm.nbPropag);
                    MaxRPCrm.nbPropag = 0;
                }
                // nps[i] = 1000 * s.getNodeCount() / s.getTimeCount();
            }

        }
        out.println();
        out.println(format("\n{0} cliques avg", avg(nbCliques)));
        for (Filter f : TEST) {
            out.println(f + " :");
            out.println(avg(cpu.get(f)) + " seconds avg");
            out.println(avg(nodes.get(f)) + " nodes avg");
            out.println(avg(nbAwakes.get(f)) + " awakes avg");
            out.println(avg(mem.get(f)) + " mem avg");
        }
    }

    private static class Clique {
        final Constraint[] constraints;

        public Clique(Constraint c1, Constraint c2, Constraint c3) {
            constraints = new Constraint[] { c1, c2, c3 };
        }
    }

    private static <T extends Comparable<T>> T median(List<T> array) {
        final List<T> sorted = new ArrayList<T>(array);
        Collections.sort(sorted);
        return sorted.get((sorted.size() - 1) / 2);
    }

    private static <T extends Number> double avg(List<T> array) {
        double sum = 0;
        for (T i : array) {
            sum += i.doubleValue();
        }
        return sum / array.size();
    }

    private static Collection<Clique> cliques(List<Constraint> constraints) {
        final Collection<Clique> cliques = new ArrayList<Clique>();

        for (int ci1 = constraints.size(); --ci1 >= 0;) {
            final Constraint c1 = constraints.get(ci1);
            final Set<Variable> c1Scope = new HashSet<Variable>(Arrays
                    .asList(c1.getVariables()));
            for (int ci2 = ci1; --ci2 >= 0;) {
                final Constraint c2 = constraints.get(ci2);

                final Variable v3;

                if (c1Scope.contains(c2.getVariables()[0])) {
                    v3 = c2.getVariables()[1];
                } else if (c1Scope.contains(c2.getVariables()[1])) {
                    v3 = c2.getVariables()[0];
                } else {
                    continue;
                }

                for (int ci3 = ci2; --ci3 >= 0;) {
                    final Constraint c3 = constraints.get(ci3);
                    if ((v3 == c3.getVariables()[0] && c1Scope.contains(c3
                            .getVariables()[1]))
                            || (v3 == c3.getVariables()[1] && c1Scope
                                    .contains(c3.getVariables()[0]))) {
                        cliques.add(new Clique(c1, c2, c3));
                        break;
                    }
                }
            }
        }
        return cliques;
    }
}
