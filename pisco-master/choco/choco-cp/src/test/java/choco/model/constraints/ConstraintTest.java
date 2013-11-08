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

package choco.model.constraints;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;
import junit.framework.Assert;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 23 sept. 2008
 * Time: 14:56:33
 */
public class ConstraintTest {
    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    @Test
    /**
     * John Horan bug on PartiallyStoredVector#staticRemove(int idx)
     */
    public void eraseConstraintTest() {
        Solver s = new CPSolver();
        IntDomainVar v = s.createEnumIntVar("v", 0, 3);
        SConstraint c1 = s.lt(v, 1);
        SConstraint c2 = s.lt(v, 2);
        SConstraint c3 = s.lt(v, 3);
        s.postCut(c1);
        s.postCut(c2);
        s.postCut(c3);
        s.eraseConstraint(c2);
        s.eraseConstraint(c3);
        try {
            LOGGER.info(s.pretty());
        } catch (Exception e) {
            Assert.fail();
        }
    }


    private static final class MockConstraint extends AbstractSConstraint {

        StringBuilder st;
        String name;

        public MockConstraint(String name, StringBuilder st, int priority) {
            super(priority, new IntDomainVar[0]);
            this.st = st;
            this.name = name;
        }

        @Override
        public void propagate() throws ContradictionException {
            st.append(name);
        }

        @Override
        public boolean isConsistent() {
            return false;
        }

        @Override
        public boolean isSatisfied() {
            return true;
        }

        @Override
        public SConstraintType getConstraintType() {
            return null;
        }
    }


    private static void orderTest(int order, String expected) {
        Configuration conf = new Configuration();
        conf.putInt(Configuration.CEQ_ORDER, order);

        Solver s = new CPSolver(conf);
        StringBuilder st = new StringBuilder();
        s.post(new MockConstraint("U", st, ConstraintEvent.UNARY));
        s.post(new MockConstraint("B", st, ConstraintEvent.BINARY));
        s.post(new MockConstraint("T", st, ConstraintEvent.TERNARY));
        s.post(new MockConstraint("L", st, ConstraintEvent.LINEAR));
        s.post(new MockConstraint("Q", st, ConstraintEvent.QUADRATIC));
        s.post(new MockConstraint("C", st, ConstraintEvent.CUBIC));
        s.post(new MockConstraint("S", st, ConstraintEvent.VERY_SLOW));

        try {
            s.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(expected, st.toString());


    }

    @Test
    public void test1() {
        orderTest(1234567, "UBTLQCS");
        orderTest(7654321, "SCQLTBU");
        orderTest(3214765, "TBULSCQ");
        orderTest(1111777, "LTBUSCQ");
        orderTest(7777777, "SCQLTBU");
    }

    @Test
    public void stynesTest1() {
        Model m = new CPModel();
        IntegerVariable x = Choco.makeIntVar("X", 0, 5);
        IntegerVariable y = Choco.makeIntVar("Y", 0, 5);

        Constraint[] constraints = new Constraint[2];

        constraints[0] = Choco.geq( x, 0);
        constraints[1] = Choco.eq(x, 3);
        m.addConstraint( Choco.ifOnlyIf( Choco.eq(y, 1), Choco.and(constraints) )  );
        // this modification must not be taken into account
        constraints[1] = Choco.eq(x, 4);

        m.addConstraint(Choco.eq(y,1));
        Solver s = new CPSolver();
        s.read(m);
        s.solveAll();
        Assert.assertEquals(s.getSolutionCount(), 1);
        Assert.assertEquals(s.getVar(x).getVal(), 3);

    }

    @Test
    public void stynesTest2() {
        Model m = new CPModel();
        IntegerVariable x = Choco.makeIntVar("X", 0, 5);
        IntegerVariable y = Choco.makeIntVar("Y", 0, 5);

        Constraint[] constraints = new Constraint[2];
        postRecursiveConstraint(constraints, m, x, y, 1, 0);

        m.addConstraint(Choco.eq(y,1));

        Solver s = new CPSolver();
        s.read(m);
        s.solveAll();
        Assert.assertEquals(s.getSolutionCount(), 1);
        Assert.assertEquals(s.getVar(x).getVal(), 3);
        Assert.assertEquals(s.getVar(y).getVal(), 1);
    }

    private static void postRecursiveConstraint(Constraint[] constraints, Model m, IntegerVariable x, IntegerVariable y, int yValue, int position){

        if (position == 1){

            Constraint andConstraint = Choco.and(constraints);
            m.addConstraint(
                    Choco.ifOnlyIf(Choco.eq(y, yValue), andConstraint));
        } else{
            constraints[position] = Choco.geq(x, 0);
            constraints[position + 1] = Choco.eq(x, 3);
            postRecursiveConstraint(constraints, m, x, y, 1, 1);
            constraints[position + 1] = Choco.eq(x, 4);
            postRecursiveConstraint(constraints, m, x, y, 2, 1);
        }
    }

}
