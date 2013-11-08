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

package samples.tutorials.to_sort.scheduling;


import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.configure.StrategyFactory;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import org.kohsuke.args4j.Option;
import samples.tutorials.PatternExample;

import java.util.Arrays;
import java.util.logging.Level;

import static choco.Choco.*;
import static choco.Options.*;


public class CumulativeScheduling extends PatternExample {
    //Data
    protected final static int NF = 3, NT = 11, N = NF + NT;

    private final static int[] DURATIONS = new int[]{1, 1, 1, 2, 1, 3, 1, 1, 3, 4, 2, 3, 1, 1};
    private final static int HORIZON = 6;

    private final static int[] HEIGHTS = new int[]{2, 1, 4, 2, 3, 1, 5, 6, 2, 1, 3, 1, 1, 2};
    private final static int CAPACITY = 7;

    //Variables

    private TaskVariable[] tasks;

    private IntegerVariable[] usages, heights;

    private IntegerVariable objective;

    private Constraint cumulative;

    @Option(name = "-alt", usage = "Use alternative resource", required = false)
    private boolean useAlternativeResource = true;


    @Override
    public void buildModel() {
        model = new CPModel();
        //the fake tasks to establish the profile capacity of the ressource are the NF firsts.
        tasks = makeTaskVarArray("T", 0, HORIZON, DURATIONS, V_BOUND);
        usages = makeBooleanVarArray("U", NT);
        objective = makeIntVar("obj", 0, NT, V_BOUND, V_OBJECTIVE);

        //set fake tasks to establish the profile capacity
        model.addConstraints(
                startsAt(tasks[0], 1),
                startsAt(tasks[1], 2),
                startsAt(tasks[2], 3)
        );
        //state the objective function
        model.addConstraint(eq(sum(usages), objective));
        if (useAlternativeResource) {
            heights = constantArray(HEIGHTS);
            cumulative = cumulativeMax("alt-cumulative", tasks, heights, usages, constant(CAPACITY), NO_OPTION);
        } else {
            heights = new IntegerVariable[N];
            //post the channeling to know if the task uses the resource or not.
            for (int i = 0; i < NF; i++) {
                heights[i] = constant(HEIGHTS[i]);
            }
            for (int i = NF; i < N; i++) {
                heights[i] = makeIntVar("H_" + i, new int[]{0, HEIGHTS[i]});
                model.addConstraint(boolChanneling(usages[i - NF], heights[i], HEIGHTS[i]));
            }
            cumulative = cumulativeMax("cumulative", tasks, heights, constant(CAPACITY), NO_OPTION);
        }
        model.addConstraint(cumulative);
    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        StrategyFactory.setNoStopAtFirstSolution(solver);
        StrategyFactory.setDoOptimize(solver, true); //maximize
        solver.read(model);
//		solver.clearGoals();
//		solver.addGoal(BranchingFactory.lexicographic(solver, solver.getVar(usages), new MaxVal()));
//		IntDomainVar[] starts = VariableUtils.getStartVars(solver.getVar(tasks));
//		solver.addGoal(BranchingFactory.minDomMinVal(solver, starts));	
        solver.generateSearchStrategy();
    }

    @Override
    public void prettyOut() {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info((useAlternativeResource ? "Alternative Resource" : "Channeling Constraints") + " Model: \n");
            if (solver.existsSolution()) {
                LOGGER.info("number of scheduled tasks: " + solver.getObjectiveValue() + "\n" + Arrays.toString(solver.getVar(usages)));
                final String title = "Cumulative Packing Constraint Visualization";
                //createAndShowGUI(title, createCumulativeChart(title, (CPSolver) solver, cumulative, true));
            }
        }
    }

    @Override
    public void solve() {
        solver.launch();
    }

    public static void main(String[] args) {
        new CumulativeScheduling().execute(args);
    }

}
