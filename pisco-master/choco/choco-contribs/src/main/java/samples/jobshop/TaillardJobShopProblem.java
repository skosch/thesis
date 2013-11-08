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

package samples.jobshop;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.strong.DomOverDDegRPC;
import choco.cp.solver.constraints.strong.DomOverWDegRPC;
import choco.cp.solver.constraints.strong.StrongConsistencyManager;
import choco.cp.solver.constraints.strong.maxrpcrm.MaxRPCrm;
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.ComponentConstraintWithSubConstraints;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import samples.jobshop.SimpleDTConstraint.SimpleDTConstraintManager;

import java.util.*;
import java.util.logging.Logger;

public class TaillardJobShopProblem {

    protected final static Logger LOGGER = ChocoLogging.getMainLogger();

    private IntegerVariable[][] variables;
    private Collection<Collection<Constraint>> disjunctive;
    private Collection<Collection<Constraint>> precedence;
    private Map<Variable, Integer> durationsMap;
    private final int jobs;
    private final int machines;
    private IntegerVariable makespan;
    private Collection<Constraint> optConstraints;

    public TaillardJobShopProblem(int jobs, int machines, int[][] times,
            int[][] shuffle, int bound) {
        this.jobs = jobs;
        this.machines = machines;
        // LOGGER.info("Generating problem with " + nbVar +
        // " variables, "
        // + nbVal + " values, " + nbConstraints + " constraints, "
        // + tightness + " Tightness");

        makespan = Choco.makeIntVar("makespan", 0, bound, Options.V_OBJECTIVE);

        // Build enumerated domain variables
        variables = new IntegerVariable[jobs][machines];
        durationsMap = new HashMap<Variable, Integer>();
        optConstraints = new ArrayList<Constraint>();
        for (int i = jobs; --i >= 0;) {
            for (int j = machines; --j >= 0;) {

                variables[i][j] = Choco.makeIntVar("V" + i + "," + j, 0, bound
                        - times[i][j] + 1);

                durationsMap.put(variables[i][j], times[i][j]);

                optConstraints.add(Choco.leq(variables[i][j], Choco.minus(
                        makespan, times[i][j])));
            }
        }

        disjunctive = new ArrayList<Collection<Constraint>>();

        // L'op�ration j du job i
        // se fait sur la machine machines[i][j]
        // et dure durations[i][j]
        // Les machines ne font qu'une chose � la fois

        for (int l = machines; --l >= 0;) {
            final Collection<Constraint> disj = new ArrayList<Constraint>();
            disjunctive.add(disj);
            for (int c1 = jobs; --c1 >= 0;) {
                for (int c2 = jobs; --c2 >= c1 + 1;) {
                    disj
                            .add(new ComponentConstraint(
                                    SimpleDTConstraintManager.class, new int[] {
                                            times[l][c1], times[l][c2] },
                                    new Variable[] { variables[l][c1],
                                            variables[l][c2] }));
                }
            }
        }

        // cpt = 0 ;

        precedence = new ArrayList<Collection<Constraint>>();
        precedence.add(optConstraints);
        for (int c = jobs; --c >= 0;) {
            final Collection<Constraint> prec = new ArrayList<Constraint>();
            precedence.add(prec);
            for (int l1 = machines; --l1 >= 1;) {
                prec.add(Choco
                        .leq(Choco.plus(variables[l1 - 1][find(c,
                                shuffle[l1 - 1])], times[l1 - 1][find(c,
                                shuffle[l1 - 1])]), variables[l1][find(c,
                                shuffle[l1])]));

            }

        }

    }

    public static int find(int value, int[] array) {
        for (int i = array.length; --i >= 0;) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public IntegerVariable[] getVariables() {
        final IntegerVariable[] vars = new IntegerVariable[jobs * machines];
        for (int i = jobs; --i >= 0;) {
            System.arraycopy(variables[i], 0, vars, jobs * i, machines);
        }
        return vars;
    }

    public Collection<Collection<Constraint>> getDisjConstraints() {
        return disjunctive;
    }

    public Collection<Collection<Constraint>> getPrecConstraints() {
        return precedence;
    }

    private static int NBINSTANCES = 1;

    private static int TIMEOUT = -1;

    private enum Heuristic {
        DDEG, WDEG
    }

    public static void main(String args[]) {
        final int jobs;
        final int machines;
        final int timesSeed;
        final int machinesSeed;
        final int bound;

        final Heuristic heuristic;

        final boolean maxrpc;
        final boolean light;
        try {
            int i = 0;
            jobs = Integer.valueOf(args[i++]);
            machines = Integer.valueOf(args[i++]);

            timesSeed = Integer.valueOf(args[i++]);

            machinesSeed = Integer.valueOf(args[i++]);

            bound = Integer.valueOf(args[i++]);

            heuristic = Heuristic.valueOf(args[i++]);

            maxrpc = Boolean.valueOf(args[i++]);

            light = Boolean.valueOf(args[i++]);

            TIMEOUT = 1000 * Integer.valueOf(args[i]);

            // NBINSTANCES = Integer.valueOf(args[i++]);

        } catch (Exception exception) {
            LOGGER.info("Usage : "
                            + TaillardJobShopProblem.class.getSimpleName()
                            + " nbJobs nbMachines timesSeed machinesSeed bound heuristic maxrpc light timeout");
            return;
        }

        final int[][] timesMatrix = RandomGenerator.randMatrix(timesSeed, jobs,
                machines);

        final int[][] machinesMatrix = RandomGenerator.randShuffle(
                machinesSeed, jobs, machines);

        LOGGER.info("-------------------");
        LOGGER.info(jobs + " jobs, " + machines + " machines, "
                + timesSeed + " time seed, " + machinesSeed
                + " machines seed, " + bound + " bound, "
                + (maxrpc ? " maxrpc, " : "ac, ") + (light ? "light" : "full")
                + ", " + heuristic);
        LOGGER.info(Arrays.deepToString(timesMatrix));
        LOGGER.info(Arrays.deepToString(machinesMatrix));

        test(jobs, machines, timesMatrix, machinesMatrix, bound, heuristic,
                maxrpc, light);

    }

    private static void test(int jobs, int machines, int[][] timesMatrix,
            int[][] machinesMatrix, int bound, Heuristic heuristic,
            boolean maxrpc, boolean light) {
        final List<Integer> nodes = new ArrayList<Integer>(NBINSTANCES);
        final List<Double> cpu = new ArrayList<Double>(NBINSTANCES);
        final List<Integer> nbAwakes = new ArrayList<Integer>(NBINSTANCES);
        final List<Long> mem = new ArrayList<Long>(NBINSTANCES);
        // final double[] nps = new double[NBINSTANCES - 1];

        for (int i = NBINSTANCES; --i >= 0;) {
            StringBuffer st = new StringBuffer();
            st.append("g");
            final TaillardJobShopProblem problem = new TaillardJobShopProblem(
                    jobs, machines, timesMatrix, machinesMatrix, bound);

            // Build a model
            final Model m = new CPModel();

            m.addVariables(problem.getVariables());

            // if (!maxRPCConstraints.isEmpty()) {
            // final Set<Variable> maxRPCVariables = new HashSet<Variable>();
            // for (Constraint c : maxRPCConstraints) {
            // maxRPCVariables.addAll(Arrays.asList(c.getVariables()));
            // }
            //
            // final Constraint cc = new ComponentConstraintWithSubConstraints(
            // StrongConsistencyManager.class, maxRPCVariables
            // .toArray(new Variable[maxRPCVariables.size()]),
            // MaxRPCrm.class, maxRPCConstraints
            // .toArray(new Constraint[maxRPCConstraints
            // .size()]));
            // if (light) {
            // cc.addOption("light");
            // }
            // m.addConstraint(cc);
            // acConstraints.removeAll(maxRPCConstraints);
            // }

            // nbMaxRPCCons.add(maxRPCConstraints.size());

            if (maxrpc) {
                for (Collection<Constraint> disj : problem.getDisjConstraints()) {
                    final Set<Variable> maxRPCVariables = new HashSet<Variable>();
                    for (Constraint c : disj) {
                        maxRPCVariables.addAll(Arrays.asList(c.getVariables()));
                    }
                    final Constraint cc = new ComponentConstraintWithSubConstraints(
                            StrongConsistencyManager.class, maxRPCVariables
                                    .toArray(new Variable[maxRPCVariables
                                            .size()]), MaxRPCrm.class, disj
                                    .toArray(new Constraint[disj.size()]));
                    if (light) {
                        cc.addOption("light");
                    }
                    m.addConstraint(cc);
                }

            } else {
                for (Collection<Constraint> disj : problem.getDisjConstraints()) {
                    for (Constraint c : disj) {
                        m.addConstraint(c);
                    }
                }
            }

            for (Collection<Constraint> prec : problem.getPrecConstraints()) {
                for (Constraint c : prec) {
                    m.addConstraint(c);
                }
            }

            // LOGGER.info("Building solver...");

            // Build a solver
            final CPSolver s = new CPSolver();

            // Read the model
            s.read(m);

            if (TIMEOUT > 0) {
                s.setTimeLimit(TIMEOUT);
            }
            st.append("s");
            Boolean result;
            System.gc();

            long time;

            switch (heuristic) {
            case WDEG:
                s.attachGoal(new AssignOrForbidIntVarVal(new DomOverWDegRPC(s,
                        s.getVar(problem.getVariables())), new MinVal()));
                break;
            default:
                s.attachGoal(new AssignOrForbidIntVarVal(new DomOverDDegRPC(s),
                        new MinVal()));
            }
            s.setGeometricRestart(20, 1.2);
            // s.setLoggingMaxDepth(20);
            // CPSolver.setVerbosity(CPSolver.SEARCH);

            result = s.minimize(true);
            time = (long) s.getTimeCount() * 1000000l;

            // final Boolean result = s.solve();
            mem.add(Runtime.getRuntime().totalMemory()
                    - Runtime.getRuntime().freeMemory());
            // CPSolver.flushLogs();

            if (result == null) {
                st.append("*");
                // nodes[i] = Integer.MAX_VALUE;
                cpu.add(Double.POSITIVE_INFINITY);
                nbAwakes.add(Integer.MAX_VALUE);
                nodes.add(Integer.MAX_VALUE);
            } else {
                st.append(s.getObjectiveValue());
                try {
                    nodes.add(s.getNodeCount());
                } catch (Exception e) {
                    nodes.add(0);
                }
                cpu.add(time / 1e9d);
                nbAwakes.add(MaxRPCrm.nbPropag);
                MaxRPCrm.nbPropag = 0;
            }
            // nps[i] = 1000 * s.getNodeCount() / s.getTimeCount();
           LOGGER.info(st.toString());
        }

        LOGGER.info(median(cpu) + " seconds med");
        LOGGER.info(median(nodes) + " nodes med");
        LOGGER.info(median(nbAwakes) + " awakes med");
        LOGGER.info(median(mem) + " mem med");
    }

    private static <T extends Comparable<T>> T median(List<T> array) {
        final List<T> sorted = new ArrayList<T>(array);
        Collections.sort(sorted);
        return sorted.get((sorted.size() - 1) / 2);
    }
}