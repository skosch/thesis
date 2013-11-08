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

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import samples.tutorials.PatternExample;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/01/11
 */
public class ProjectSchedulingLoad extends PatternExample {

    TaskVariable[] tasks;
    IntegerVariable[] durations;
    IntegerVariable[] resources;
    IntegerVariable[] loads;

    @Override
    public void printDescription() {
        super.printDescription();
        LOGGER.info("We have 7 tasks. Each task is defined by a duration, a set a precedence rules and also requires a given load (duration x needed resource).");
        LOGGER.info("Knowing that we have at most 7 persons, find the earliest end");
        LOGGER.info("Task_1 (duration = 2), starts before Task_2, Task_3 and Task_4");
        LOGGER.info("Task_2 (duration = 1), starts before Task_5");
        LOGGER.info("Task_3 (duration = 4), starts before Task_6");
        LOGGER.info("Task_4 (duration = 2), starts before Task_5");
        LOGGER.info("Task_5 (duration = 3), starts before Task_7");
        LOGGER.info("Task_6 (duration = 1), starts before Task_7");
        LOGGER.info("Task_7 (duration = 0)");
    }

    @Override
    public void buildModel() {
        model = new CPModel();

        loads = Choco.constantArray(new int[]{1, 3, 12, 4, 12, 6, 0});
        durations = new IntegerVariable[7];
        resources = new IntegerVariable[7];
        for (int i = 0; i < 6; i++) {
            durations[i] = Choco.makeIntVar("duration_" + i, 0, loads[i].getLowB());
            resources[i] = Choco.makeIntVar("resource_" + i, 0, loads[i].getLowB());
            model.addConstraint(Choco.times(durations[i], resources[i], loads[i]));
        }
        durations[6]  = Choco.constant(0);
        resources[6]  = Choco.constant(0);

        tasks = Choco.makeTaskVarArray("Task", 0, 999, durations);

        model.addConstraint(Choco.endsAfterBegin(tasks[0], tasks[1]));
        model.addConstraint(Choco.endsAfterBegin(tasks[0], tasks[2]));
        model.addConstraint(Choco.endsAfterBegin(tasks[0], tasks[3]));
        model.addConstraint(Choco.endsAfterBegin(tasks[1], tasks[4]));
        model.addConstraint(Choco.endsAfterBegin(tasks[2], tasks[5]));
        model.addConstraint(Choco.endsAfterBegin(tasks[3], tasks[4]));
        model.addConstraint(Choco.endsAfterBegin(tasks[4], tasks[6]));
        model.addConstraint(Choco.endsAfterBegin(tasks[5], tasks[6]));

        model.addConstraint(Choco.cumulative("cumu", tasks, resources, null, Choco.constant(0), Choco.constant(7), (IntegerVariable) null, ""));
    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
    }

    @Override
    public void solve() {
        solver.minimize(solver.getVar(tasks[6].end()), true);
        //LOGGER.info(solver.pretty());
    }

    @Override
    public void prettyOut() {
        LOGGER.info("\n");

        for (int i = 0; i < tasks.length - 1; i++) {
            StringBuilder st = new StringBuilder(tasks[i].getName()).append(" [");
            int k = solver.getVar(tasks[i].start()).getVal();
            int d = solver.getVar(durations[i]).getVal();
            int r = solver.getVar(resources[i]).getVal();
            for (int j = 0; j < 13; j++) {
                if (j >= k && j < k + d) {
                    st.append(r);
                } else {
                    st.append("-");
                }
            }
            st.append("]");
            LOGGER.info(st.toString());
        }
        StringBuilder st = new StringBuilder(tasks[6].getName()).append(" [");
        int k = solver.getVar(tasks[6].start()).getVal();
        for (int j = 0; j < 13; j++) {
            if (j == k) {
                st.append("|");
            } else {
                st.append("-");
            }
        }
        st.append("]");
        LOGGER.info(st.toString());
    }

    public static void main(String[] args) {
        new ProjectSchedulingLoad().execute();
    }
}
