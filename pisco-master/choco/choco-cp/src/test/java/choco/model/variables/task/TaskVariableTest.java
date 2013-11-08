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

package choco.model.variables.task;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.propagation.event.TaskVarEvent;
import choco.kernel.solver.propagation.listener.TaskPropagator;
import choco.kernel.solver.variables.scheduling.TaskVar;
import org.junit.Assert;
import org.junit.Test;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 26 janv. 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class TaskVariableTest {

    @Test
    public void test0(){
        Model m = new CPModel();
        TaskVariable t = Choco.makeTaskVar("task1", 10, 5);
        m.addVariable(t);

        Solver solver = new CPSolver();
        solver.read(m);
        Assert.assertNotNull("t is null", solver.getVar(t));
        Assert.assertTrue("t not well created", TaskVar.class.isInstance(solver.getVar(t)));
    }


    @Test
    public void testTaskVarEvent() throws ContradictionException {
        Model m = new CPModel();
        int[] duration = new int[]{5,5};
        TaskVariable[] tasks = Choco.makeTaskVarArray("task", 1, 10, duration);
        m.addVariables(tasks);

        Solver solver = new CPSolver();
        solver.read(m);

        FakeTaskConstraint ftc1 = new FakeTaskConstraint(solver.getVar(tasks), 0);
        FakeTaskConstraint ftc2 = new FakeTaskConstraint(solver.getVar(tasks), 0);

        solver.post(ftc1);
        solver.post(ftc2);

        solver.propagate();
        Assert.assertEquals(1,ftc1.getVal());
        Assert.assertEquals(1,ftc2.getVal());
        ftc1.forceAwake(0);
        solver.propagate();
        Assert.assertEquals(1,ftc1.getVal());
        Assert.assertEquals(2,ftc2.getVal());
    }

    private class FakeTaskConstraint extends AbstractSConstraint<TaskVar> implements TaskPropagator{

        int value;

        /**
         * Constraucts a constraint with the priority 0.
         *
         * @param vars variables involved in the constraint
         * @param init
         */
        protected FakeTaskConstraint(TaskVar[] vars, int init) {
            super(vars);
            this.value = init;
        }

        @Override
        public int getFilteredEventMask(int idx) {
            return TaskVarEvent.HYPDOMMODbitvector;
        }

        @Override
        public SConstraintType getConstraintType() {
            return null;
        }

        /**
         * <i>Propagation:</i>
         * Propagating the constraint until local consistency is reached.
         *
         * @throws choco.kernel.solver.ContradictionException
         *          contradiction exception
         */
        @Override
        public void propagate() throws ContradictionException {
            value++;
        }

        /**
         * tests if the constraint is consistent with respect to the current state of domains
         *
         * @return wether the constraint is consistent
         */
        @Override
        public boolean isConsistent() {
            return true;
        }

        /**
         * <i>Semantic:</i>
         * Testing if the constraint is satisfied.
         * Note that all variables involved in the constraint must be
         * instantiated when this method is called.
         *
         * @return true if the constraint is satisfied
         */
        @Override
        public boolean isSatisfied() {
            return true;
        }

        public void forceAwake(int idx){
            vars[idx].updateHypotheticalDomain(cIndices[idx], this, false);
        }

        public final int getVal(){
            return value;
        }

        /**
         * Default propagation on improved hypothetical domain: propagation on domain revision.
         */
        @Override
        public void awakeOnHypDomMod(int varIdx) throws ContradictionException {
            value++;
        }

        
    }

}
