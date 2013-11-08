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


/* File choco.currentElement.search.BitSetIntDomainTest.java, last modified by Francois 21 sept. 2003 10:59:44 */

package choco.model.constraints.integer;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.integer.AbstractIntDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.structure.OneWordSBitSet32;
import choco.kernel.memory.structure.OneWordSBitSet64;
import choco.kernel.memory.structure.SBitSet;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import static choco.Choco.makeIntVar;
import static org.junit.Assert.*;

/**
 * a class implementing tests for backtrackable search
 */
public class BitSetIntDomainTest {
    private Logger logger = ChocoLogging.getTestLogger();
    private CPModel m;
    private IntegerVariable x, y;
    private CPSolver s;
    AbstractIntDomain yDom;

    @Before
    public void setUp() {
        logger.fine("BitSetIntDomain Testing...");
        m = new CPModel();
        x = makeIntVar("X", 1, 100);
        m.addVariable(Options.V_BOUND, x);
        y = makeIntVar("Y", 1, 15);
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
        logger.finer("test1");
        try {
            assertEquals(1, yDom.getInf());
            assertEquals(15, yDom.getSup());
            assertEquals(15, yDom.getSize());
            logger.finest("First step passed");

            s.getEnvironment().worldPush();
            yDom.removeVal(2, null, true);
            assertEquals(1, yDom.getInf());
            assertEquals(15, yDom.getSup());
            assertEquals(14, yDom.getSize());
            logger.finest("Second step passed");

            yDom.removeVal(1, null, true);
            assertEquals(3, yDom.getInf());
            assertEquals(15, yDom.getSup());
            assertEquals(13, yDom.getSize());
            logger.finest("Third step passed");


            s.worldPop();
            assertEquals(1, yDom.getInf());
            assertEquals(15, yDom.getSup());
            assertEquals(15, yDom.getSize());
            logger.finest("Fourth step passed");

        } catch (ContradictionException e) {
            assertTrue(false);
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {
        logger.finer("test2");
        try {

            yDom.removeVal(10, null, true);
            yDom.removeVal(12, null, true);
            yDom.removeVal(14, null, true);
            yDom.removeVal(13, null, true);
            yDom.updateSup(14);
            assertEquals(1, yDom.getInf());
            assertEquals(11, yDom.getSup());
            assertEquals(10, yDom.getSize());
            logger.finest("First step passed");

            yDom.updateInf(8);
            assertEquals(8, yDom.getInf());
            assertEquals(11, yDom.getSup());
            assertEquals(3, yDom.getSize());
            logger.finest("Second step passed");

            yDom.removeVal(11, null, true);
            assertEquals(8, yDom.getInf());
            assertEquals(9, yDom.getSup());
            assertEquals(2, yDom.getSize());
            logger.finest("Third step passed");
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
        logger.finer("test3");
        Set expectedSet357 = new TreeSet();
        expectedSet357.add(new Integer(3));
        expectedSet357.add(new Integer(5));
        expectedSet357.add(new Integer(7));
        Set expectedSet9 = new TreeSet();
        expectedSet9.add(new Integer(9));

        {
            yDom.freezeDeltaDomain();
            DisposableIntIterator it = yDom.getDeltaIterator();
            assertFalse(it.hasNext());
            assertTrue(yDom.releaseDeltaDomain());
            it.dispose();
        }
        yDom.remove(3);
        yDom.remove(5);
        yDom.remove(7);
        Set tmp357 = new TreeSet();
        yDom.freezeDeltaDomain();
        yDom.remove(9);
        for (DisposableIntIterator it = yDom.getDeltaIterator(); it.hasNext();) {
            int val = it.next();
            tmp357.add(new Integer(val));
            it.dispose();
        }
        assertEquals(expectedSet357, tmp357);
        assertFalse(yDom.releaseDeltaDomain());
        yDom.freezeDeltaDomain();
        Set tmp9 = new TreeSet();
        for (DisposableIntIterator it = yDom.getDeltaIterator(); it.hasNext();) {
            int val = it.next();
            tmp9.add(new Integer(val));
            it.dispose();
        }
        assertEquals(expectedSet9, tmp9);
        assertTrue(yDom.releaseDeltaDomain());
    }

    /**
     * currentElement the restrict method
     */
    @Test
    public void test4() {
        logger.finer("test2");
        try {
            yDom.removeVal(10, null, true);
            yDom.removeVal(12, null, true);
            yDom.removeVal(14, null, true);
            yDom.removeVal(13, null, true);
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
    public void testSBitSetTest() {
        IEnvironment env = new EnvironmentTrailing();
        SBitSet bit = new SBitSet(env, 15);
        testSBitSet(bit);
    }

    @Test
    public void testOneWordSBitSet64Test() {
        IEnvironment env = new EnvironmentTrailing();
        OneWordSBitSet64 bit = new OneWordSBitSet64(env, 15);
        testSBitSet(bit);
    }

    @Test
    public void testOneWordSBitSet32Test() {
        IEnvironment env = new EnvironmentTrailing();
        OneWordSBitSet32 bit = new OneWordSBitSet32(env, 15);
        testSBitSet(bit);
    }

    protected void testSBitSet(IStateBitSet bit) {
        bit.set(2, 13);
        StringBuffer st = new StringBuffer();
        for (int i = bit.nextSetBit(0); i >= 0; i = bit.nextSetBit(i + 1)) {
            st.append(i);
        }
        Assert.assertEquals("23456789101112", st.toString());
        st.setLength(0);
        for (int i = bit.nextClearBit(0); i >= 0 && i < 15; i = bit.nextClearBit(i + 1)) {
            st.append(i);
        }
        Assert.assertEquals("011314", st.toString());
        st.setLength(0);
        for (int i = bit.prevSetBit(15); i >= 0; i = bit.prevSetBit(i - 1)) {
            st.append(i);
        }
        Assert.assertEquals("12111098765432", st.toString());
        st.setLength(0);
        for (int i = bit.prevClearBit(14); i >= 0; i = bit.prevClearBit(i - 1)) {
            st.append(i);
        }
        Assert.assertEquals("141310", st.toString());

        bit.clear(4, 10);
        st.setLength(0);
        for (int i = bit.nextSetBit(0); i >= 0; i = bit.nextSetBit(i + 1)) {
            st.append(i);
        }
        Assert.assertEquals("23101112", st.toString());
        st.setLength(0);
        for (int i = bit.nextClearBit(0); i >= 0 && i < 15; i = bit.nextClearBit(i + 1)) {
            st.append(i);
        }
        Assert.assertEquals("014567891314", st.toString());
        st.setLength(0);
        for (int i = bit.prevSetBit(15); i >= 0; i = bit.prevSetBit(i - 1)) {
            st.append(i);
        }
        Assert.assertEquals("12111032", st.toString());
        st.setLength(0);
        for (int i = bit.prevClearBit(14); i >= 0; i = bit.prevClearBit(i - 1)) {
            st.append(i);
        }
        Assert.assertEquals("141398765410", st.toString());
    }

    @Test
    public void testLimitOneWordSBitSet64Test() {
        IEnvironment env = new EnvironmentTrailing();
        OneWordSBitSet64 bit;
        for(int b = 0; b < 64; b++){
            bit = new OneWordSBitSet64(env, b);
            bit.set(0, b);
            int nbbits = 0;
            for (int i = bit.nextSetBit(0); i >= 0 && nbbits < 70; i = bit.nextSetBit(i + 1)) {
                nbbits++;
            }
            Assert.assertEquals(b, nbbits);
        }
    }

    @Test
    public void testLimitOneWordSBitSet32Test() {
        IEnvironment env = new EnvironmentTrailing();
        OneWordSBitSet32 bit;
        for(int b = 0; b < 32; b++){
            bit = new OneWordSBitSet32(env, b);
            bit.set(0, b);
            int nbbits = 0;
            for (int i = bit.nextSetBit(0); i >= 0 && nbbits < 35; i = bit.nextSetBit(i + 1)) {
                nbbits++;
            }
            Assert.assertEquals(b, nbbits);
        }
    }

}