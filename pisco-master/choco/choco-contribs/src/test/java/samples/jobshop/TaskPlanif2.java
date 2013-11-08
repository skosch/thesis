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
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import org.kohsuke.args4j.Option;
import samples.tutorials.PatternExample;

import java.util.Random;
import java.util.logging.Level;

public class TaskPlanif2 extends PatternExample {

    private final static int MIN_DURATION = 10;
    private final static int MAX_DURATION = 50;


    @Option(name = "-n", usage = "Number of tasks (default: 10)", required = false)
    /**
     * number of tasks
     */
    public int nbTasks = 10;

    @Option(name = "-h", usage = "Horizon (default: 2000)", required = false)
    /**
     * horizon (number of periods
     */
    public int horizon = 2000;

    @Option(name = "-seed", usage = "Seed for random", required = false)
    /** number of destinations */
    public long seed = System.currentTimeMillis();

    protected int[] durationIfStartAt;

    protected IntegerVariable[] durations;

    protected TaskVariable[] tasks;

    protected IntegerVariable sumDurations;

    @Override
    public void buildModel() {
        Random rnd = new Random(seed);
        durationIfStartAt = new int[horizon];
        for (int i = 0; i < horizon; i++) {
            durationIfStartAt[i] = MIN_DURATION + rnd.nextInt(MAX_DURATION - MIN_DURATION);
        }

        model = new CPModel();
        //variables
        durations = Choco.makeIntVarArray("d", nbTasks, 5, 55, Options.V_ENUM);
        tasks = Choco.makeTaskVarArray("t", 0, horizon, durations);
        for (TaskVariable t : tasks) t.start().addOption(Options.V_ENUM);
        sumDurations = Choco.makeIntVar("sumDur", nbTasks * MIN_DURATION, nbTasks * MAX_DURATION);
        model.addVariables(tasks);
        //constraints
        model.addConstraint(Choco.eq(sumDurations, Choco.sum(durations)));
        for (int i = 0; i < nbTasks; i++) {
            model.addConstraint(Choco.nth(tasks[i].start(), durationIfStartAt, tasks[i].duration()));
        }
        for (int i = 1; i < nbTasks; i++) {
            model.addConstraint(Choco.startsAfterEnd(tasks[i], tasks[i - 1]));
        }

    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
        final IntDomainVar[] v = {solver.getVar(sumDurations)};
        solver.attachGoal(new AssignOrForbidIntVarVal(new StaticVarOrder(solver, v), new MinVal()));
        solver.addGoal(new AssignVar(new MinDomain(solver, solver.getVar(durations)), new MinVal()));
        solver.addGoal(new IncompleteAssignvar(solver.getVar(tasks)));
    }

    @Override
    public void prettyOut() {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(StringUtils.pretty(solver.getVar(tasks)));
        }

    }

    @Override
    public void solve() {
        //ChocoLogging.setVerbosity(Verbosity.VERBOSE);
        solver.generateSearchStrategy();
        solver.setTimeLimit(120 * 1000);
        solver.minimize(solver.getVar(sumDurations), false);
    }


    class IncompleteAssignvar extends AssignVar {

        public IncompleteAssignvar(TaskVar[] tasks) {
            super(
                    new StaticVarOrder(solver, VariableUtils.getStartVars(tasks)),
                    new MinVal()
            );
        }

        @Override
        public boolean finishedBranching(IntBranchingDecision decision) {
            return true; //explore only one branch.
        }


    }


    public static void main(String[] args) {
        new TaskPlanif2().execute(args);
    }

}
