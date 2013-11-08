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

package choco.model.constraints.integer;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import junit.framework.Assert;
import org.junit.Test;

public class BoolSumTest {

    private final IntegerVariable[] bvars = Choco.makeBooleanVarArray("b", 10);

    private final IntegerExpressionVariable sum = Choco.sum(bvars);

    private final int nbTotSols = 1 << bvars.length;


    private int solveWith(Constraint c) {
        final Model m = new CPModel();
        m.addConstraint(c);
        final CPSolver s = new CPSolver();
        s.read(m);
        s.setRandomSelectors(0);
        s.solveAll();
        return s.getNbSolutions();
    }

    private final void testNbSols(int[] sols1, int[] sols2) {
        //		LOGGER.info(nbTotSols);
        //		LOGGER.info(Arrays.toString(sols1));
        //		LOGGER.info(Arrays.toString(sols2));
        for (int i = 0; i < sols1.length; i++) {
            Assert.assertEquals(nbTotSols, sols1[i] + sols2[i]);
        }
    }

    @Test
    public void testEqNeq() {
        //ChocoLogging.setVerbosity(Verbosity.VERBOSE);
        final int[] sols1 = new int[bvars.length + 3];
        final int[] sols2 = new int[bvars.length + 3];
        for (int i = -1; i < bvars.length + 2; i++) {
            sols1[i + 1] = solveWith(Choco.eq(sum, i));
            sols2[i + 1] = solveWith(Choco.neq(sum, i));
        }
        testNbSols(sols1, sols2);
        final int n2 = sols1.length / 2;
        for (int i = 0; i < n2; i++) {
            Assert.assertEquals(sols1[i], sols1[sols1.length - i - 1]);
            Assert.assertEquals(sols2[i], sols2[sols2.length - i - 1]);
        }
    }

    @Test
    public void testLeqGeq() {
        final int[] sols1 = new int[bvars.length + 2];
        final int[] sols2 = new int[bvars.length + 2];
        for (int i = 0; i < bvars.length + 2; i++) {
            sols1[i] = solveWith(Choco.geq(sum, i));
            sols2[i] = solveWith(Choco.lt(sum, i));
        }
        testNbSols(sols1, sols2);
        for (int i = 0; i < sols1.length; i++) {
            Assert.assertEquals(sols1[i], sols2[sols1.length - i - 1]);
        }

    }

    @Test(expected = ContradictionException.class)
    public void testEq() throws ContradictionException {
        final Model m = new CPModel();
        m.addConstraint(Choco.eq(sum, 1));
        final CPSolver s = new CPSolver();
        s.read(m);
        try {
            for (int i = 0; i < bvars.length - 2; i++) {
                s.getVar(bvars[i]).setVal(0);
            }
            s.propagate();
        } catch (ContradictionException ignored) {
            Assert.fail();
        }
        for (int i = bvars.length - 2; i < bvars.length; i++) {
            s.getVar(bvars[i]).setVal(0);
        }
        s.propagate();
    }

    @Test
    public void testEq2() throws ContradictionException {
        final Model m = new CPModel();
        m.addConstraint(Choco.eq(sum, 1));
        final CPSolver s = new CPSolver();
        s.read(m);
        try {
            for (int i = 0; i < bvars.length - 2; i++) {
                s.getVar(bvars[i]).setVal(0);
            }
            s.propagate();
        } catch (ContradictionException ignored) {
            Assert.fail();
        }
        s.getVar(bvars[bvars.length - 2]).setVal(0);
        s.getVar(bvars[bvars.length - 1]).setVal(1);
        s.propagate();
        //Assert.assertTrue(s.isFeasible());
    }

    @Test
    public void testEq3() throws ContradictionException {
        Model m = new CPModel();
        IntegerVariable b1 = Choco.makeBooleanVar("b1");
        IntegerVariable b2 = Choco.constant(0);
        IntegerVariable b3 = Choco.makeBooleanVar("b3");
        IntegerVariable b4 = Choco.makeBooleanVar("b4");
        m.addConstraint(Choco.eq(Choco.sum(b1,b2,b3,b4), 4));

        CPSolver s = new CPSolver();
        s.read(m);
        try {
            s.propagate();
            Assert.fail("unexpected behaviour");
        } catch (ContradictionException ignored) {}
    }

    @Test
    public void testEq4() throws ContradictionException {
        Model m = new CPModel();
        IntegerVariable b1 = Choco.makeBooleanVar("b1");
        IntegerVariable b2 = Choco.constant(1);
        IntegerVariable b3 = Choco.makeBooleanVar("b3");
        IntegerVariable b4 = Choco.makeBooleanVar("b4");
        m.addConstraint(Choco.eq(Choco.sum(b1,b2,b3,b4), 5));

        CPSolver s = new CPSolver();
        s.read(m);
        try {
            s.propagate();
            Assert.fail("unexpected behaviour");
        } catch (ContradictionException ignored) {}
    }

    @Test
    public void testEq5() throws ContradictionException {
        Model m = new CPModel();
        IntegerVariable b1 = Choco.makeBooleanVar("b1");
        IntegerVariable b2 = Choco.constant(0);
        IntegerVariable b3 = Choco.makeBooleanVar("b3");
        IntegerVariable b4 = Choco.makeBooleanVar("b4");
        m.addConstraint(Choco.eq(Choco.sum(b1,b2,b3,b4), 4));

        CPSolver s = new CPSolver();
        s.read(m);
        s.solveAll();
        Assert.assertEquals(0, s.getSolutionCount());
    }

    @Test
    public void testGeq3() throws ContradictionException {
        Model m = new CPModel();
        IntegerVariable b1 = Choco.makeBooleanVar("b1");
        IntegerVariable b2 = Choco.makeBooleanVar("b2");//Choco.constant(0);
        IntegerVariable b3 = Choco.makeBooleanVar("b3");
        IntegerVariable b4 = Choco.makeBooleanVar("b4");
        m.addConstraint(Choco.geq(Choco.sum(b1,b2,b3,b4), 10));

        CPSolver s = new CPSolver();
        s.read(m);
        try {
            s.propagate();
            Assert.fail("unexpected behaviour");
        } catch (ContradictionException ignored) {}
    }
}
