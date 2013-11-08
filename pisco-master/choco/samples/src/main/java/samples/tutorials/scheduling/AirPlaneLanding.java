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

package samples.tutorials.scheduling;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.preprocessor.PreProcessConfiguration;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import samples.tutorials.PatternExample;

import static choco.Choco.*;
import static choco.Options.*;

/**
 * n planes must land on a landing strip.
 * Each plane has an arrival time, a landing duration time and a number of passengers.
 * We want to prioritize planes according to the number of passengers.
 * the objective is to minimize the weighted sum of tardiness.
 *
 * @author Arnaud Malapert
 *         <p/>
 *         TODO: refaire cet exemple pour qu'il colle au specifications de la OR lib
 */
public class AirPlaneLanding extends PatternExample {

    //20 Tasks ; Horizon 12 hours : 12 x 4 = 48 (15mn)
    //DATA

    /**
     * Each plane has an arrival time.
     */
    private int[] arrivalTimes = {
            0, 0, 2, 4, 8,
            8, 12, 12, 12, 12,
            16, 18, 22, 24, 28,
            32, 32, 35, 35, 40
    };

    /**
     * Each plane has a landing deadline.
     */
    private int[] deadlines = {
            16, 18, 14, 18, 21,
            25, 26, 27, 29, 32,
            34, 35, 35, 37, 38,
            40, 40, 43, 46,
            48, 46, 46, 48, 48
    };

    /**
     * Each plane has a landing duration
     */
    private int[] landingDurations = {
            2, 3, 2, 1, 3,
            3, 2, 1, 3, 2,
            1, 3, 2, 4, 2,
            3, 2, 2, 1, 2
    };

    /**
     * Each plane has a number of passengers.
     */
    private int[] numberOfPassengers = {
            200, 400, 250, 125, 450,
            500, 250, 150, 500, 220,
            125, 400, 200, 800, 175,
            400, 250, 175, 80, 250
    };

    private int maxTardiness;
    //vARIABLES

    /**
     * Each plane is represented as a task. its starting time is its landing time and its duration its landing time.
     */
    protected TaskVariable[] planes;

    /**
     * Each plane has a tardiness (starting time - arrival time)
     */
    protected IntegerVariable[] tardiness;

    /**
     * the objective to minimize
     */
    protected IntegerVariable weightedSumOfCompletionTimes;

    private boolean useDisjMod = true;


    @Override
    public void printDescription() {
        LOGGER.info("n planes must land on a landing strip.");
        LOGGER.info("Each plane has an arrival time, a landing duration time and a number of passengers.");
        LOGGER.info("We want to prioritize planes according to the number of passengers.");
        LOGGER.info("the objective is to minimize the weighted sum of tardiness.\n");
    }

    @Override
    public void buildModel() {
        model = new CPModel();

        this.maxTardiness = 0;
        for (int i = 0; i < arrivalTimes.length; i++) {
            maxTardiness = Math.max(maxTardiness, deadlines[i] - landingDurations[i] - arrivalTimes[i]);
        }

        //create Tasks
        planes = makeTaskVarArray("plane", arrivalTimes, deadlines, constantArray(landingDurations));
        /* the landing strip is represented as an unary resource (capacity 1) */
        model.addConstraint(disjunctive("LandS", planes));
        // tardiness = start - arrivalTime;
        tardiness = makeIntVarArray("tardiness", planes.length, 0, maxTardiness, V_BOUND, V_NO_DECISION);
        for (int i = 0; i < planes.length; i++) {
            model.addConstraint(eq(tardiness[i], minus(planes[i].start(), arrivalTimes[i])));
        }
        //create objective
        weightedSumOfCompletionTimes = makeIntVar("objective", 0, planes.length * maxTardiness * MathUtils.max(numberOfPassengers), V_BOUND, V_OBJECTIVE, V_NO_DECISION);
        model.addConstraint(eq(weightedSumOfCompletionTimes, scalar(numberOfPassengers, tardiness)));
    }


    @Override
    public void buildSolver() {
        if (useDisjMod) {
            solver = new PreProcessCPSolver();
            PreProcessConfiguration.keepSchedulingPreProcess(solver);

        } else {
            solver = new CPSolver();
        }
        solver.read(model);
        solver.setTimeLimit(20000);

        //solver.clearGoals();
    }


    @Override
    public void solve() {
        solver.minimize(false);
    }

    @Override
    public void prettyOut() {
        LOGGER.info("\nSolution");
        for (int i = 0; i < planes.length; i++) {
            int diff = solver.getVar(planes[i].start()).getVal() - arrivalTimes[i];
            LOGGER.info("landing time for plane " + i + " :" + arrivalTimes[i] + " " + (diff == 0 ? "" :"+"+ Math.abs(diff)));
        }
        LOGGER.info("Penalty :" + solver.getVar(weightedSumOfCompletionTimes).getVal());
    }


    public static void main(String[] args) {
        (new AirPlaneLanding()).execute(args);
    }
}

