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


/* File choco.currentElement.search.IntLinCombTest.java, last modified by Francois 27 sept. 2003 12:08:59 */
package choco.model.constraints.integer;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.IntLinComb;
import choco.cp.solver.constraints.integer.NotEqualXYC;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.logging.Logger;

import static choco.Choco.*;
import static org.junit.Assert.*;

public class IntLinCombTest {
    protected final static Logger LOGGER = ChocoLogging.getTestLogger();
    private CPModel m;
    private CPSolver s;
    private IntegerVariable x1, x2, x3, x4, x5, x6, x7, y1, y2;

    @Before
    public void setUp() {
        LOGGER.fine("IntLinComb Testing...");
        m = new CPModel();
        s = new CPSolver();
        x1 = makeIntVar("X1", 0, 10);
        x2 = makeIntVar("X2", 0, 10);
        x3 = makeIntVar("X3", 0, 10);
        x4 = makeIntVar("X4", 0, 10);
        x5 = makeIntVar("X5", 0, 10);
        x6 = makeIntVar("X6", 0, 10);
        x7 = makeIntVar("X7", 0, 10);
        y1 = makeIntVar("Y1", 0, 10);
        y2 = makeIntVar("Y2", 0, 50);
        m.addVariables(Options.V_BOUND, x1, x2, x3, x4, x5, x6, x7, y1, y2);
        //m.addVariable(x1, x2, x3, x4, x5, x6, x7, y1, y2);
        s.read(m);
    }

    @After
    public void tearDown() {
        x1 = null;
        x2 = null;
        x3 = null;
        x4 = null;
        x5 = null;
        x6 = null;
        x7 = null;
        y1 = null;
        y2 = null;
        m = null;
        s = null;
    }

    /**
     * Simple currentElement: 5 equations on 4 variables: 1 single search solution that should be found by propagation
     */
    @Test
    public void test1() {
        LOGGER.finer("test1");
        try {
            m.addConstraint(eq(scalar(new int[]{3, 7, 9, -1}, new IntegerVariable[]{x1, x2, x3, y1}), 68));
            m.addConstraint(eq(scalar(new int[]{5, 2, 8, -1}, new IntegerVariable[]{x1, x2, x3, y1}), 44));
            m.addConstraint(eq(scalar(new int[]{3, 12, 2, -1}, new IntegerVariable[]{x1, x2, x3, y1}), 72));
            m.addConstraint(eq(scalar(new int[]{15, 4, 1, -1}, new IntegerVariable[]{x1, x2, x3, y1}), 53));
            m.addConstraint(eq(scalar(new int[]{12, 7, 9, -1}, new IntegerVariable[]{x1, x2, x3, y1}), 86));
            // pb.getPropagationEngine().getLogger().setVerbosity(choco.model.ILogger.TALK);
            s.read(m);
            s.propagate();
            assertTrue(s.getVar(x1).isInstantiated());
            assertTrue(s.getVar(x2).isInstantiated());
            assertTrue(s.getVar(x3).isInstantiated());
            assertTrue(s.getVar(y1).isInstantiated());
            assertEquals(2, s.getVar(x1).getVal());
            assertEquals(5, s.getVar(x2).getVal());
            assertEquals(3, s.getVar(x3).getVal());
            assertEquals(0, s.getVar(y1).getVal());
        } catch (ContradictionException e) {
            assertFalse(true);
        }
    }

    /**
     * Exact same currentElement as test1, but expressed with binary +/x operators instead of "scalar" operator
     */
    @Test
    public void test2() {
        LOGGER.finer("test2");
        try {
            m.addConstraint(eq(plus(plus(plus(mult(3, x1), mult(7, x2)), mult(9, x3)), mult(-1, y1)), 68));
            m.addConstraint(eq(plus(plus(plus(mult(5, x1), mult(2, x2)), mult(8, x3)), mult(-1, y1)), 44));
            m.addConstraint(eq(plus(plus(plus(mult(3, x1), mult(12, x2)), mult(2, x3)), mult(-1, y1)), 72));
            m.addConstraint(eq(plus(plus(plus(mult(15, x1), mult(4, x2)), mult(1, x3)), mult(-1, y1)), 53));
            m.addConstraint(eq(plus(plus(plus(mult(12, x1), mult(7, x2)), mult(9, x3)), mult(-1, y1)), 86));
            // pb.getPropagationEngine().getLogger().setVerbosity(choco.model.ILogger.TALK);
            s.read(m);
            s.propagate();
            assertTrue(s.getVar(x1).isInstantiated());
            assertTrue(s.getVar(x2).isInstantiated());
            assertTrue(s.getVar(x3).isInstantiated());
            assertTrue(s.getVar(y1).isInstantiated());
            assertEquals(2, s.getVar(x1).getVal());
            assertEquals(5, s.getVar(x2).getVal());
            assertEquals(3, s.getVar(x3).getVal());
            assertEquals(0, s.getVar(y1).getVal());
        } catch (ContradictionException e) {
            assertFalse(true);
        }
    }

    /**
     * Another currentElement: 5 equations on 4 variables: the solution is found step by step
     */
    @Test
    public void test3() {
        LOGGER.finer("test3");
        try {
        	m.addConstraint(Options.E_DECOMP, eq(scalar(new int[]{1, 3, 5, -1}, new IntegerVariable[]{x1, x2, x3, y1}), 23));
            m.addConstraint(Options.E_DECOMP, eq(scalar(new int[]{2, 10, 1, -1}, new IntegerVariable[]{x1, x2, x3, y2}), 14));
            m.addConstraint(Options.E_DECOMP, eq(scalar(new int[]{7, -1}, new IntegerVariable[]{y1, y2}), 0));
            s.read(m);
            s.getVar(x1).setInf(1);
            s.getVar(x2).setInf(1);
            s.getVar(x3).setInf(1);
            s.getVar(y1).setInf(1);
            s.propagate();
            assertEquals(1, s.getVar(x1).getInf());
            assertEquals(10, s.getVar(x1).getSup());
            assertEquals(1, s.getVar(x2).getInf());
            assertEquals(6, s.getVar(x2).getSup());
            assertEquals(1, s.getVar(x3).getInf());
            assertEquals(5, s.getVar(x3).getSup());
            assertEquals(1, s.getVar(y1).getInf());
            assertEquals(7, s.getVar(y1).getSup());
            assertEquals(7, s.getVar(y2).getInf());
            assertEquals(49, s.getVar(y2).getSup());
            s.getVar(x1).setInf(7);
            s.propagate();
            assertEquals(7, s.getVar(x1).getInf());
            assertEquals(10, s.getVar(x1).getSup());
            assertEquals(1, s.getVar(x2).getInf());
            assertEquals(4, s.getVar(x2).getSup());
            assertEquals(1, s.getVar(x3).getInf());
            assertEquals(4, s.getVar(x3).getSup());
            assertEquals(2, s.getVar(y1).getInf());
            assertEquals(7, s.getVar(y1).getSup());
            assertEquals(14, s.getVar(y2).getInf());
            assertEquals(49, s.getVar(y2).getSup());
            s.getVar(x3).setSup(2);
            s.propagate();
            assertEquals(7, s.getVar(x1).getInf());
            assertEquals(10, s.getVar(x1).getSup());
            assertEquals(2, s.getVar(x2).getInf());
            assertEquals(4, s.getVar(x2).getSup());
            assertEquals(1, s.getVar(x3).getInf());
            assertEquals(2, s.getVar(x3).getSup());
            assertEquals(3, s.getVar(y1).getInf());
            assertEquals(6, s.getVar(y1).getSup());
            assertEquals(21, s.getVar(y2).getInf());
            assertEquals(42, s.getVar(y2).getSup());
            s.getVar(y2).setInf(30);
            s.propagate();
            assertEquals(7, s.getVar(x1).getInf());
            assertEquals(10, s.getVar(x1).getSup());
            assertEquals(3, s.getVar(x2).getInf());
            assertEquals(4, s.getVar(x2).getSup());
            assertTrue(s.getVar(x3).isInstantiated());
            assertEquals(2, s.getVar(x3).getVal());
            assertEquals(5, s.getVar(y1).getInf());
            assertEquals(6, s.getVar(y1).getSup());
            assertEquals(35, s.getVar(y2).getInf());
            assertEquals(42, s.getVar(y2).getSup());
            s.getVar(x2).setInf(4);
            s.propagate();

            assertTrue(s.getVar(x1).isInstantiated());
            assertTrue(s.getVar(x2).isInstantiated());
            assertTrue(s.getVar(x3).isInstantiated());
            assertTrue(s.getVar(y1).isInstantiated());
            assertTrue(s.getVar(y2).isInstantiated());
            assertEquals(7, s.getVar(x1).getVal());
            assertEquals(4, s.getVar(x2).getVal());
            assertEquals(2, s.getVar(x3).getVal());
            assertEquals(6, s.getVar(y1).getVal());
            assertEquals(42, s.getVar(y2).getVal());
        } catch (ContradictionException e) {
            assertFalse(true);
        }
    }

    /**
     * Slightly larger currentElement: 10 equations on 7 variables: 1 single search solution that should be found by propagation
     */
    @Test
    public void test4() {
        try {
            m.addConstraint(Options.E_DECOMP, eq(scalar(new int[]{98527, 34588, 5872, 59422, 65159, -30704, -29649}, new IntegerVariable[]{x1, x2, x3, x5, x7, x4, x6}), 1547604));
            m.addConstraint(Options.E_DECOMP, eq(scalar(new int[]{98957, 83634, 69966, 62038, 37164, 85413, -93989}, new IntegerVariable[]{x2, x3, x4, x5, x6, x7, x1}), 1823553));
            m.addConstraint(Options.E_DECOMP, eq(scalar(new int[]{10949, 77761, 67052, -80197, -61944, -92964, -44550}, new IntegerVariable[]{x1, x2, x5, x3, x4, x6, x7}), -900032));
            m.addConstraint(Options.E_DECOMP, eq(scalar(new int[]{73947, 84391, 81310, -96253, -44247, -70582, -33054}, new IntegerVariable[]{x1, x3, x5, x2, x4, x6, x7}), 1164380));
            m.addConstraint(Options.E_DECOMP, eq(scalar(new int[]{13057, 42253, 77527, 96552, -60152, -21103, -97932}, new IntegerVariable[]{x3, x4, x5, x7, x1, x2, x6}), 1185471));
            m.addConstraint(Options.E_DECOMP, eq(scalar(new int[]{66920, 55679, -64234, -65337, -45581, -67707, -98038}, new IntegerVariable[]{x1, x4, x2, x3, x5, x6, x7}), -1394152));
            m.addConstraint(Options.E_DECOMP, eq(scalar(new int[]{68550, 27886, 31716, 73597, 38835, -88963, -76391}, new IntegerVariable[]{x1, x2, x3, x4, x7, x5, x6}), 279091));
            m.addConstraint(Options.E_DECOMP, eq(scalar(new int[]{76132, 71860, 22770, 68211, 78587, -48224, -82817}, new IntegerVariable[]{x2, x3, x4, x5, x6, x1, x7}), 480923));
            m.addConstraint(Options.E_DECOMP, eq(scalar(new int[]{94198, 87234, 37498, -71583, -25728, -25495, -70023}, new IntegerVariable[]{x2, x3, x4, x1, x5, x6, x7}), -519878));
            m.addConstraint(Options.E_DECOMP, eq(scalar(new int[]{78693, 38592, 38478, -94129, -43188, -82528, -69025}, new IntegerVariable[]{x1, x5, x6, x2, x3, x4, x7}), -361921));
            // pb.getPropagationEngine().getLogger().setVerbosity(choco.model.ILogger.DEBUG);
            s.read(m);
            s.propagate();
            assertEquals(0, s.getVar(x1).getInf());
            assertEquals(10, s.getVar(x1).getSup());
            assertEquals(0, s.getVar(x2).getInf());
            assertEquals(10, s.getVar(x2).getSup());
            assertEquals(0, s.getVar(x3).getInf());
            assertEquals(10, s.getVar(x3).getSup());
            assertEquals(0, s.getVar(x4).getInf());
            assertEquals(10, s.getVar(x4).getSup());
            assertEquals(0, s.getVar(x5).getInf());
            assertEquals(10, s.getVar(x5).getSup());
            assertEquals(0, s.getVar(x6).getInf());
            assertEquals(10, s.getVar(x6).getSup());
            assertEquals(0, s.getVar(x7).getInf());
            assertEquals(10, s.getVar(x7).getSup());
            s.getVar(x1).setInf(6);
            s.propagate();
            assertEquals(6, s.getVar(x1).getInf());
            assertEquals(1, s.getVar(x5).getInf());
            assertEquals(7, s.getVar(x6).getSup());
            assertEquals(3, s.getVar(x7).getInf());
            s.getVar(x3).setInf(8);
            s.propagate();
            assertEquals(6, s.getVar(x1).getInf());
            assertEquals(6, s.getVar(x2).getSup());
            assertEquals(8, s.getVar(x3).getInf());
            s.getVar(x4).setInf(4);
            s.propagate();
            assertEquals(5, s.getVar(x2).getSup());
            assertEquals(4, s.getVar(x4).getInf());
            assertEquals(4, s.getVar(x5).getInf());
            assertEquals(4, s.getVar(x7).getInf());
            s.getVar(x5).setInf(9);
            s.propagate();
            assertEquals(4, s.getVar(x2).getSup());
            assertEquals(9, s.getVar(x5).getInf());
            assertEquals(6, s.getVar(x6).getSup());
            s.getVar(x6).setInf(3);
            s.propagate();
            assertEquals(8, s.getVar(x1).getSup());
            assertEquals(2, s.getVar(x2).getSup());
            assertEquals(7, s.getVar(x4).getSup());
            assertEquals(3, s.getVar(x6).getInf());
            assertEquals(5, s.getVar(x6).getSup());
            assertEquals(8, s.getVar(x7).getInf());
            s.getVar(x7).setInf(9);
            s.propagate();
            assertTrue(s.getVar(x1).isInstantiated());
            assertTrue(s.getVar(x2).isInstantiated());
            assertTrue(s.getVar(x3).isInstantiated());
            assertTrue(s.getVar(x4).isInstantiated());
            assertTrue(s.getVar(x5).isInstantiated());
            assertTrue(s.getVar(x6).isInstantiated());
            assertTrue(s.getVar(x7).isInstantiated());
            assertEquals(6, s.getVar(x1).getVal());
            assertEquals(0, s.getVar(x2).getVal());
            assertEquals(8, s.getVar(x3).getVal());
            assertEquals(4, s.getVar(x4).getVal());
            assertEquals(9, s.getVar(x5).getVal());
            assertEquals(3, s.getVar(x6).getVal());
            assertEquals(9, s.getVar(x7).getVal());
        } catch (ContradictionException e) {
            assertFalse(true);
        }
    }

    @Test
    public void test5() {
        IntegerVariable a = makeIntVar("a", 0, 4);
        m.addConstraint(eq(plus(a, 1), 2));
        s.read(m);
        s.solve();
        assertEquals(1, s.getVar(a).getVal());
        LOGGER.info("Variable " + a + " = " + s.getVar(a).getVal());
    }


    @Test
    public void test6() {
        IntegerVariable[] a = makeIntVarArray("a", 4, 0, 4);
        int[] zeroCoef = new int[4];
        m.addConstraint(leq(scalar(a, zeroCoef), 2));
        s.read(m);
        s.solve();
        assertTrue(s.isFeasible());
    }

    @Test
    public void test7() {
        IntegerVariable[] a = makeIntVarArray("a", 4, 0, 4);
        int[] zeroCoef = new int[4];
        m.addConstraint(geq(scalar(a, zeroCoef), 2));
        m.addConstraint(geq(scalar(a, zeroCoef), 10));
        m.addConstraint(geq(scalar(a, zeroCoef), 1));
        s.read(m);
        s.solve();
        assertTrue(!s.isFeasible());
    }

    @Test
    public void test8() {
        IntegerVariable[] a = makeIntVarArray("a", 4, 0, 4);
        int[] zeroCoef = new int[4];
        m.addConstraint(geq(scalar(a, zeroCoef), -2));
        m.addConstraint(geq(scalar(a, zeroCoef), -3));
        s.read(m);
        s.solve();
        assertTrue(s.isFeasible());
    }

    @Test
    public void test9() {
        IntegerVariable[] a = makeIntVarArray("a", 4, 0, 4);
        int[] zeroCoef = new int[4];
        m.addConstraint(eq(scalar(a, zeroCoef), 0));
        s.read(m);
        s.solve();
        LOGGER.info(s.pretty());
        assertTrue(s.isFeasible());
    }
    
    @Test
    public void test10() {
        IntegerVariable[] a = {x1, x2};
        int[] zeroCoef = new int[2];
        m.addConstraint(eq(scalar(a, zeroCoef), 0));
        s.read(m);
        s.solve();
        LOGGER.info(s.pretty());
        assertTrue(s.isFeasible());
    }
    
    @Test
    public void test11() {
    	IntegerVariable[] a = {x1, x2, x2, x1};
        int[] zeroCoef = {1,2,-2,-1};
        m.addConstraint(eq(scalar(a, zeroCoef), 0));
        s.read(m);
        s.solve();
        assertTrue(s.isFeasible());
    }
    
    @Test
    public void test12() {
    	IntegerVariable[] a = {x1, x2, x2, x3, x3, x1};
        int[] zeroCoef = {1,2,-2,3, -3, -1};
        m.addConstraint(eq(scalar(a, zeroCoef), 0));
        s.read(m);
        s.solve();
        assertTrue(s.isFeasible());
    }

    @Test
    public void testXavier1(){
        int n = 2 ;
        final CPSolver s1 = new CPSolver();
        final CPSolver s2 = new CPSolver();

        final IntDomainVar[] lvars1 = new IntDomainVar[n];
        final IntDomainVar[] lvars2 = new IntDomainVar[n];

        for(int i = 0; i < n; i++){
            lvars1[i] = s1.createEnumIntVar("v_"+i, 1, 2*n);
            lvars2[i] = s2.createEnumIntVar("v_"+i, 1, 2*n);
        }
//        lvars2[n] = s2.createEnumIntVar("v_"+n, 1, 1);
        final SConstraint c11 = new IntLinComb(lvars1, new int[]{1,-1}, 1, 0, IntLinComb.GEQ);
        final SConstraint c12 = new NotEqualXYC(lvars1[0], lvars1[1], 0);
        s1.post(c11);
        s1.post(c12);

        final SConstraint c2 = new IntLinComb(lvars2,new int[]{1,-1}, 1, -1, IntLinComb.GEQ);
        s2.post(c2);

        s1.solveAll();
        s2.solveAll();

        Assert.assertEquals("not same number of solution", s1.getSolutionCount(), s2.getSolutionCount());

    }

    @Test
    public void testXavier2(){
        int n = 2 ;
        final CPSolver s1 = new CPSolver();
        final CPSolver s2 = new CPSolver();

        final IntDomainVar[] lvars1 = new IntDomainVar[n];
        final IntDomainVar[] lvars2 = new IntDomainVar[n];

        for(int i = 0; i < n; i++){
            lvars1[i] = s1.createEnumIntVar("v_"+i, 1, 2*n);
            lvars2[i] = s2.createEnumIntVar("v_"+i, 1, 2*n);
        }
//        lvars2[n] = s2.createEnumIntVar("v_"+n, 1, 1);
        final SConstraint c11 = new IntLinComb(lvars1, new int[]{1,-1}, 1, 0, IntLinComb.LEQ);
        final SConstraint c12 = new NotEqualXYC(lvars1[0], lvars1[1], 0);
        s1.post(c11);
        s1.post(c12);

        final SConstraint c2 = new IntLinComb(lvars2,new int[]{1,-1}, 1, 1, IntLinComb.LEQ);
        s2.post(c2);

        s1.solveAll();
        s2.solveAll();

        Assert.assertEquals("not same number of solution", s1.getSolutionCount(), s2.getSolutionCount());

    }

    @Test
    public void testXavier3(){
        int n = 10;
        final CPSolver s1 = new CPSolver();
        final CPSolver s2 = new CPSolver();

        final IntDomainVar[] lvars1 = new IntDomainVar[n];
        final IntDomainVar[] lvars2 = new IntDomainVar[n];

        int[] coeffs = new int[n];
        Arrays.fill(coeffs, 1);
        for(int i = 0; i < n; i++){
            lvars1[i] = s1.createEnumIntVar("v_"+i, 0, 1);
            lvars2[i] = s2.createEnumIntVar("v_"+i, 0, 1);
        }
//        lvars2[n] = s2.createEnumIntVar("v_"+n, 1, 1);
        final SConstraint c1 = new IntLinComb(lvars1, coeffs, n, -n/2, IntLinComb.NEQ);
        s1.post(c1);

        final SConstraint c2 = s2.neq(s2.scalar(lvars2, coeffs), n/2);
        s2.post(c2);

        s1.solveAll();
        s2.solveAll();

        Assert.assertEquals("not same number of solution", s1.getSolutionCount(), s2.getSolutionCount());

    }

    @Test
    public void testXavier4(){
        int n = 10;
        final CPSolver s1 = new CPSolver();
        final CPSolver s2 = new CPSolver();

        final IntDomainVar[] lvars1 = new IntDomainVar[n];
        final IntDomainVar[] lvars2 = new IntDomainVar[n];

        int[] coeffs = new int[n];
        Arrays.fill(coeffs, 1);
        for(int i = 0; i < n; i++){
            lvars1[i] = s1.createEnumIntVar("v_"+i, 0, 1);
            lvars2[i] = s2.createEnumIntVar("v_"+i, 0, 1);
        }
//        lvars2[n] = s2.createEnumIntVar("v_"+n, 1, 1);
        final SConstraint c1 = new IntLinComb(lvars1, coeffs, n, -n/2, IntLinComb.EQ);
        s1.post(c1);

        final SConstraint c2 = s2.eq(s2.scalar(lvars2, coeffs), n/2);
        s2.post(c2);

        s1.solveAll();
        s2.solveAll();

        Assert.assertEquals("not same number of solution", s1.getSolutionCount(), s2.getSolutionCount());

    }

}

/*
(     choco/post(p, list(98527, 34588, 5872, 59422, 65159, -30704, -29649) scalar list(x1,x2,x3,x5,x7,x4,x6) + 1547604),
      choco/post(p, list(98957, 83634, 69966, 62038, 37164, 85413, -93989) scalar list(x2,x3,x4,x5,x6,x7,x1) ==  1823553),
      choco/post(p, list(10949, 77761, 67052, -80197, -61944, -92964, -44550) scalar list(x1,x2,x5,x3,x4,x6,x7) == -900032),
      choco/post(p, list(73947, 84391, 81310, -96253, -44247, -70582, -33054) scalar list(x1,x3,x5,x2,x4,x6,x7) == 1164380),
      choco/post(p, list(13057, 42253, 77527, 96552, -60152, -21103, -97932) scalar list(x3,x4,x5,x7,x1,x2,x6) == 1185471),
      choco/post(p, list(66920, 55679, -64234, -65337, -45581, -67707, -98038) scalar list(x1,x4,x2,x3,x5,x6,x7) == -1394152),
      choco/post(p, list(68550, 27886, 31716, 73597, 38835, -88963, -76391) scalar list(x1,x2,x3,x4,x7,x5,x6) == 279091),
      choco/post(p, list(76132, 71860, 22770, 68211, 78587, -48224, -82817) scalar list(x2,x3,x4,x5,x6,x1,x7) == 480923),
      choco/post(p, list(94198, 87234, 37498, -71583, -25728, -25495, -70023) scalar list(x2,x3,x4,x1,x5,x6,x7) == -519878),
      choco/post(p, list(78693, 38592, 38478, -94129, -43188, -82528, -69025) scalar list(x1,x5,x6,x2,x3,x4,x7) == -361921),
   */