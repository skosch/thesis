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

package choco.model.variables.integer;

import choco.Choco;
import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.cp.solver.variables.integer.AbstractIntDomain;
import choco.cp.solver.variables.integer.BipartiteIntDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Tests on the domain implemented with a backtrackable list
 * of integers
 **/
public class BipartiteIntDomainTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();
    private CPModel m;
    private IntegerVariable x, y;
    private CPSolver s;
    AbstractIntDomain yDom;

    @Before
    public void setUp() {
        LOGGER.fine("BitSetIntDomain Testing...");
        m = new CPModel();
        x = makeIntVar("X", 1, 100, Options.V_BLIST);
        y = makeIntVar("Y", 1, 15, Options.V_BLIST);
        m.addVariables(x, y);
        s = new CPSolver();
        s.read(m);
        yDom = (AbstractIntDomain) s.getVar(y).getDomain();
    }

    @After
    public void tearDown() {
        yDom = null;
        y = null;
        x = null;
        m = null;
        s = null;
    }

    @Test
    public void test1() {
        LOGGER.finer("test1");
        try {
            assertEquals(1, yDom.getInf());
            assertEquals(15, yDom.getSup());
            assertEquals(15, yDom.getSize());
            LOGGER.finest("First step passed");

            s.getEnvironment().worldPush();
            yDom.removeVal(2, null, true);
            assertEquals(1, yDom.getInf());
            assertEquals(15, yDom.getSup());
            assertEquals(14, yDom.getSize());
            LOGGER.finest("Second step passed");

            yDom.removeVal(1, null, true);
            assertEquals(3, yDom.getInf());
            assertEquals(15, yDom.getSup());
            assertEquals(13, yDom.getSize());
            LOGGER.finest("Third step passed");


            s.worldPop();
            assertEquals(1, yDom.getInf());
            assertEquals(15, yDom.getSup());
            assertEquals(15, yDom.getSize());
            LOGGER.finest("Fourth step passed");

        } catch (ContradictionException e) {
            assertTrue(false);
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {
        LOGGER.finer("test2");
        try {
            yDom.removeVal(10, null, false);
            yDom.removeVal(12, null, false);
            yDom.removeVal(14, null, false);
            yDom.removeVal(13, null, false);
            yDom.updateSup(14);
            LOGGER.info("" + yDom.pretty());
            assertEquals(1, yDom.getInf());
            assertEquals(11, yDom.getSup());
            assertEquals(10, yDom.getSize());
            LOGGER.finest("First step passed");

            yDom.updateInf(8);
            assertEquals(8, yDom.getInf());
            assertEquals(11, yDom.getSup());
            assertEquals(3, yDom.getSize());
            LOGGER.finest("Second step passed");

            yDom.removeVal(11, null, false);
            assertEquals(8, yDom.getInf());
            assertEquals(9, yDom.getSup());
            assertEquals(2, yDom.getSize());
            LOGGER.finest("Third step passed");
        } catch (ContradictionException e) {
            assertTrue(false);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    /**
     * testing delta domain management
     */
    @Test
    public void test3() {
        LOGGER.finer("test3");
        Set<Integer> expectedSet357 = new TreeSet<Integer>();
        expectedSet357.add(3);
        expectedSet357.add(5);
        expectedSet357.add(7);
        Set<Integer> expectedSet9 = new TreeSet<Integer>();
        expectedSet9.add(9);

        {
            yDom.freezeDeltaDomain();
            DisposableIntIterator it = yDom.getDeltaIterator();
            assertFalse(it.hasNext());
            assertTrue(yDom.releaseDeltaDomain());
        }
        yDom.remove(3);
        yDom.remove(5);
        yDom.remove(7);
        Set<Integer> tmp357 = new TreeSet<Integer>();
        yDom.freezeDeltaDomain();
        yDom.remove(9);
        for (DisposableIntIterator it = yDom.getDeltaIterator(); it.hasNext();) {
            int val = it.next();
            tmp357.add(val);
        }
        assertEquals(expectedSet357, tmp357);
        assertFalse(yDom.releaseDeltaDomain());
        yDom.freezeDeltaDomain();
        Set<Integer> tmp9 = new TreeSet<Integer>();
        for (DisposableIntIterator it = yDom.getDeltaIterator(); it.hasNext();) {
            int val = it.next();
            tmp9.add(val);
        }
        assertEquals(expectedSet9, tmp9);
        assertTrue(yDom.releaseDeltaDomain());
    }

    /**
     * currentElement the restrict method
     */
    @Test
    public void test4() {
        LOGGER.finer("test2");
        try {
        yDom.removeVal(10, null, false);
        yDom.removeVal(12, null, false);
        yDom.removeVal(14, null, false);
        yDom.removeVal(13, null, false);
        yDom.updateSup(14);
        yDom.instantiate(7, null, true);
        assertEquals(7, yDom.getInf());
        assertEquals(7, yDom.getSup());
        assertEquals(1, yDom.getSize());
        DisposableIntIterator it = yDom.getIterator();
        assertTrue(it.hasNext());
        assertEquals(7, it.next());
        assertFalse(it.hasNext());
            it.dispose();
        } catch (ContradictionException e) {
            assertTrue(false);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Test
    public void testRandomValue() {
        yDom.remove(5);
        yDom.remove(10);
        yDom.remove(12);
        for (int i = 0; i < 100; i++) {
            int val = yDom.getRandomValue();
            assertTrue(yDom.contains(val));
        }

        IntDomain xDom = s.getVar(x).getDomain();
        xDom.updateInf(5);
        xDom.updateSup(10);
        for (int i = 0; i < 100; i++) {
            int val = xDom.getRandomValue();
            assertTrue(xDom.contains(val));
        }
    }

    @Test
    public void test5(){
        y = Choco.makeIntVar("Y", 0, 12);
        m.addVariable(Options.V_BLIST, y);
        m.addVariable(y);
        s = new CPSolver();
        s.read(m);
        yDom = (BipartiteIntDomain) s.getVar(y).getDomain();

        s.worldPush();
        try{
            s.getVar(y).remVal(7);
            s.getVar(y).remVal(5);

            s.getVar(y).remVal(3);
            s.getVar(y).remVal(1);

            s.getVar(y).remVal(11);
            s.getVar(y).remVal(9);

        }catch(ContradictionException e){
            fail();
        }
        assertTrue("yDom not contains 4",yDom.contains(4));
        assertFalse("yDom contains 7",yDom.contains(7));
        assertTrue("yDom not contains 8",yDom.contains(8));
        assertTrue("yDom not contains 0",yDom.contains(0));
        assertTrue("yDom not contains 12",yDom.contains(12));
        assertTrue("yDom not contains 10",yDom.contains(2));
        assertTrue("yDom not contains 6",yDom.contains(6));
    }

   /**
   * testing freeze and release for the delta domain
   */
  @Test
  public void test6() {
    LOGGER.finer("test2");

    yDom.freezeDeltaDomain();
    assertTrue(yDom.releaseDeltaDomain());

    yDom.updateInf(2);
    yDom.updateSup(12);
    yDom.freezeDeltaDomain();
    yDom.updateInf(3);
    assertFalse(yDom.releaseDeltaDomain());

    yDom.freezeDeltaDomain();
    assertTrue(yDom.releaseDeltaDomain());
  }

    @Test
    public void test7(){
        IntDomainVar v = s.getVar(y);
        BipartiteIntDomain dom = (BipartiteIntDomain) v.getDomain();
        for (int i = 1; i < 15; i++) {
            s.worldPush();
            try {
                v.remVal(i);
                LOGGER.info("" + dom.pretty());
            } catch (ContradictionException e) {
                LOGGER.severe(e.getMessage());
            }

            Assert.assertFalse("y remove "+i, yDom.contains(i));
            Assert.assertEquals("y size inf "+i, yDom.getSize(), 15-i);

        }
        s.worldPush();
        try {
            v.remVal(15);
            Assert.fail();
        } catch (ContradictionException e) {
            s.worldPop();
        }
        for (int i = 15; i > 1; i--) {
            Assert.assertTrue("y add "+i, yDom.contains(i));
            Assert.assertTrue("y size inf "+i, yDom.getSize()==16-i);
            s.worldPop();
        }
        for (int i = 15; i > 0; i--) {
            s.worldPush();
            try {
                v.remVal(15);
            } catch (ContradictionException e) {
                Assert.fail();
            }
        }
        for (int i = 15; i > 0; i--) {
            s.worldPop();
        }

    }

    @Test
    public void test8(){
        y = Choco.makeIntVar("Y", 1, 8);
        m.addVariable(Options.V_BLIST, y);
        m.addVariables(x, y);
        s = new CPSolver();
        s.read(m);
        yDom = (BipartiteIntDomain) s.getVar(y).getDomain();

        s.worldPush();
        try {
            s.getVar(y).remVal(1);
        } catch (ContradictionException e) {
            fail();
        }
        try {
            s.getVar(y).remVal(7);
        } catch (ContradictionException e) {
            fail();
        }
        try {
            s.getVar(y).remVal(2);
        } catch (ContradictionException e) {
            fail();
        }

        s.worldPush();
        try {
            s.getVar(y).remVal(3);
            s.getVar(y).instantiate(8, null, false);
        } catch (ContradictionException e) {
            fail();
        }

        s.worldPop();
        assertTrue("yDom not contains 4",yDom.contains(4));
        assertTrue("yDom not contains 5",yDom.contains(5));
        assertTrue("yDom not contains 6",yDom.contains(6));

        s.worldPop();
        assertTrue("yDom not contains 3",yDom.contains(1));
        assertTrue("yDom not contains 4",yDom.contains(7));
        assertTrue("yDom not contains 5",yDom.contains(2));
        assertEquals("yDom bad size", yDom.getSize(), 8);
    }

    public static int nbQueensSolution[] = {0, 0, 0, 0, 2, 10, 4, 40, 92, 352, 724, 2680, 14200, 73712};

    @Test
    public void queenBiplist() {
        for (int n = 4; n < 10; n++) {
            LOGGER.finer("n queens, binary model, n=" + n);
            m = new CPModel();
            s = new CPSolver();
            // create variables
            IntegerVariable[] queens = new IntegerVariable[n];
            for (int i = 0; i < n; i++) {
                queens[i] = makeIntVar("Q" + i, 1, n, Options.V_BLIST);//blist");
            }
            // diagonal constraints
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    int k = j - i;
                    m.addConstraint(neq(queens[i], queens[j]));
                    m.addConstraint(neq(queens[i], plus(queens[j], k)));
                    m.addConstraint(neq(queens[i], minus(queens[j], k)));
                }
            }
            s.read(m);
            s.solveAll();
            LOGGER.info(n+": "+ s.getNbSolutions()+ " " + s.getTimeCount());
            if (n >= 4) {
                if (n <= 13) {
                    assertEquals(Boolean.TRUE, s.isFeasible());
                    assertEquals(nbQueensSolution[n], s.getNbSolutions());

                }
            } else {
                assertEquals(Boolean.FALSE, s.isFeasible());
            }
        }
    }

     @Test
    public void testAutoExample0() {
        for (int seed = 0; seed < 10; seed++) {
            CPModel m = new CPModel();
            CPSolver s = new CPSolver();
            IntegerVariable v1 = makeIntVar("v1", 1, 2, Options.V_BLIST);
            IntegerVariable v2 = makeIntVar("v2", new int[]{0, 3}, Options.V_BLIST);
            IntegerVariable v3 = makeIntVar("v3", new int[]{0, 3}, Options.V_BLIST);

            //remove some forbidden tuples (here, the tuples define a not_all_equal constraint)
            List<int[]> tuples = new LinkedList<int[]>();
            tuples.add(new int[]{1, 3, 0});
            tuples.add(new int[]{2, 3, 3});

            // post the constraint
            m.addConstraint(regular(new IntegerVariable[]{v1, v2, v3}, tuples));

            s.read(m);
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.solveAll();

            LOGGER.info("ExpectedSolutions 2 - nbSol " + s.getNbSolutions());
            assertEquals(2, s.getNbSolutions());
        }
     }

        @Test
    public void testAbs() {
        for (int i = 0; i <= 10; i++) {
            LOGGER.info("seed " + i);
            CPModel m = new CPModel();
            IntegerVariable x = makeIntVar("x", 1, 5, Options.V_BLIST);
            IntegerVariable y = makeIntVar("y", -5, 5, Options.V_BLIST);
            m.addConstraint(abs(x,y));
            CPSolver s = new CPSolver();
            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, i));
            s.setValIntSelector(new RandomIntValSelector(i + 1));
            s.solve();
            do {
                LOGGER.info("" + s.getVar(x).getVal() + "=abs(" + s.getVar(y).getVal() + ")");
            } while (s.nextSolution() == Boolean.TRUE);
            LOGGER.info("" + s.getSearchStrategy().getNodeCount());
            assertEquals(10, s.getNbSolutions());
            //LOGGER.info("Nb solution : " + pb.getSolver().getNbSolutions());
        }
    }

     @Test
    public void testBugNormalized() {
        for (int seed = 10; seed < 30; seed++) {
            LOGGER.info("seed:" + seed);
            CPModel m = new CPModel();
            m.setDefaultExpressionDecomposition(true);
            CPSolver s = new CPSolver();
            IntegerVariable[] vars = new IntegerVariable[26];
            vars[0] = makeIntVar("v0", 31, 31);//,CPOptions.V_BLIST);
            vars[1] = makeIntVar("v1", 60, 60);//,CPOptions.V_BLIST);

            for (int i = 2; i < 26; i = i + 3) {
                vars[i] = makeIntVar("vA" + i, 0, 60, Options.V_BLIST);
                vars[i + 1] = makeIntVar("vB" + i, 0, 31, Options.V_BLIST);
                vars[i + 2] = makeIntVar("vC" + i, 0, 1, Options.V_BLIST);
            }

            // Predicat 1
            m.addConstraint(predicat1(vars[3], vars[4], 7, 5, vars[0]));
            m.addConstraint(predicat1(vars[6], vars[7], 14, 5, vars[0]));
            m.addConstraint(predicat1(vars[9], vars[10], 14, 8, vars[0]));
            m.addConstraint(predicat1(vars[12], vars[13], 4, 8, vars[0]));
            m.addConstraint(predicat1(vars[15], vars[16], 21, 13, vars[0]));
            m.addConstraint(predicat1(vars[18], vars[19], 7, 11, vars[0]));
            m.addConstraint(predicat1(vars[21], vars[22], 14, 11, vars[0]));
            m.addConstraint(predicat1(vars[24], vars[25], 14, 5, vars[0]));

            //Predicat 2
            m.addConstraint(predicat2(vars[11], vars[12], vars[13], 8, 4, vars[14], vars[15], vars[16], 13, 21));
            m.addConstraint(predicat2(vars[11], vars[12], vars[13], 8, 4, vars[17], vars[18], vars[19], 11, 7));
            m.addConstraint(predicat2(vars[11], vars[12], vars[13], 8, 4, vars[20], vars[21], vars[22], 11, 14));
            m.addConstraint(predicat2(vars[11], vars[12], vars[13], 8, 4, vars[23], vars[24], vars[25], 5, 14));
            m.addConstraint(predicat2(vars[14], vars[15], vars[16], 13, 21, vars[17], vars[18], vars[19], 11, 7));
            m.addConstraint(predicat2(vars[14], vars[15], vars[16], 13, 21, vars[20], vars[21], vars[22], 11, 14));
            m.addConstraint(predicat2(vars[14], vars[15], vars[16], 13, 21, vars[23], vars[24], vars[25], 5, 14));
            m.addConstraint(predicat2(vars[17], vars[18], vars[19], 11, 7, vars[20], vars[21], vars[22], 11, 14));
            m.addConstraint(predicat2(vars[17], vars[18], vars[19], 11, 7, vars[23], vars[24], vars[25], 5, 14));
            m.addConstraint(predicat2(vars[2], vars[3], vars[4], 5, 7, vars[11], vars[12], vars[13], 8, 4));
            m.addConstraint(predicat2(vars[2], vars[3], vars[4], 5, 7, vars[14], vars[15], vars[16], 13, 21));
            m.addConstraint(predicat2(vars[2], vars[3], vars[4], 5, 7, vars[17], vars[18], vars[19], 11, 7));
            m.addConstraint(predicat2(vars[2], vars[3], vars[4], 5, 7, vars[20], vars[21], vars[22], 11, 14));
            m.addConstraint(predicat2(vars[2], vars[3], vars[4], 5, 7, vars[23], vars[24], vars[25], 5, 14));


            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.setGeometricRestart(1, 1.2);
            s.solve();
            LOGGER.info("" + s.getNodeCount());
            Assert.assertTrue("Solution incorrecte", s.checkSolution(false));
        }
    }


    private Constraint predicat1(IntegerVariable v1, IntegerVariable v2, int v3, int v4, IntegerVariable v5) {
        //e.setDecomposeExp(true);
        return leq(plus(v1, ifThenElse(eq((v2), (0)), constant(v3), constant(v4))), v5);
    }

    private Constraint predicat2(IntegerVariable v0, IntegerVariable v1, IntegerVariable v2, int v3, int v4, IntegerVariable v5, IntegerVariable v6, IntegerVariable v7, int v8, int v9) {
        //e.setDecomposeExp(true);
        return or(or(leq(plus((v0),
                ifThenElse(eq((v2), (0)), constant(v3), constant(v4))), (v5)), leq(plus((v5), ifThenElse(eq((v7), (0)), constant(v8), constant(v9))), (v0))), or(leq(plus((v1), ifThenElse(eq((v2), constant(0)), constant(v4), constant(v3))), (v6)), leq(plus((v6), ifThenElse(eq((v7), (0)), constant(v9), constant(v8))), (v1))));
    }


    @Test
    public void testPretty(){
        Model m = new CPModel();
        IntegerVariable v = Choco.makeIntVar("v", 1, 20, Options.V_BLIST);
        IntegerVariable w = Choco.makeIntVar("w", 1, 10, Options.V_BLIST);
        m.addVariables(v, w);
        Solver s = new CPSolver();
        s.read(m);
        String stv = s.getVar(v).pretty();
        Assert.assertEquals("v", "v:?[20]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, ..., 20}", stv);
        String stw = s.getVar(w).pretty();
        Assert.assertEquals("w", "w:?[10]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}", stw);
    }
}
