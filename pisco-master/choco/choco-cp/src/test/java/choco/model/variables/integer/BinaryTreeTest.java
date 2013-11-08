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
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.integer.IntervalBTreeDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.logging.Logger;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 20 nov. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*/
public class BinaryTreeTest {
	private final static Logger LOGGER = ChocoLogging.getTestLogger();
  private Model m;
  private Solver s;
  private IntegerVariable x, y;
  IntervalBTreeDomain yDom;

    @Before
  public void setUp() {
    LOGGER.fine("BitSetIntDomain Testing...");
    m = new CPModel();
    x = Choco.makeIntVar("X", 1, 100);
    y = Choco.makeIntVar("Y", 1, 15);
        m.addVariables(Options.V_BTREE, x, y);
    m.addVariables(x, y);
    s = new CPSolver();
    s.read(m);
    yDom = (IntervalBTreeDomain) s.getVar(y).getDomain();
  }

    @After
  public void tearDown() {
    yDom = null;
    x = null;
    y = null;
    m = null;
    s = null;
  }

  /**
   * testing read and write on bounds with backtracking
   */
  @Test
  public void test1() {
    LOGGER.finer("test1");

    assertEquals(1, yDom.getInf());
    assertEquals(15, yDom.getSup());
    assertEquals(15, yDom.getSize());
    LOGGER.finest("First step passed");

    s.worldPush();
    yDom.updateInf(2);
    yDom.updateInf(3);
    assertEquals(3, yDom.getInf());
    assertEquals(15, yDom.getSup());
    assertEquals(13, yDom.getSize());
    LOGGER.finest("Second step passed");

    s.worldPush();
    yDom.updateSup(13);
    yDom.updateInf(4);
    assertEquals(4, yDom.getInf());
    assertEquals(13, yDom.getSup());
    assertEquals(10, yDom.getSize());
    LOGGER.finest("Third step passed");

    s.worldPop();
    assertEquals(3, yDom.getInf());
    assertEquals(15, yDom.getSup());
    assertEquals(13, yDom.getSize());

    s.worldPop();
    assertEquals(1, yDom.getInf());
    assertEquals(15, yDom.getSup());
    assertEquals(15, yDom.getSize());
  }



  /**
   * testing freeze and release for the delta domain
   */
  @Test
  public void test2() {
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
    public void test3(){
        for (int i = 1; i < 15; i++) {
            s.worldPush();
            try {
                s.getVar(y).remVal(i);
            } catch (ContradictionException e) {
                Assert.fail();
            }

            Assert.assertFalse("y remove "+i, yDom.contains(i));
            Assert.assertEquals("y size inf "+i, yDom.getSize(), 15-i);

        }
        s.worldPush();
        try {
            s.getVar(y).remVal(15);
            Assert.fail();
        } catch (ContradictionException e) {
            s.worldPop();
        }
        for (int i = 15; i > 0; i--) {
            Assert.assertTrue("y add "+i, yDom.contains(i));
            Assert.assertTrue("y size inf "+i, yDom.getSize()==16-i);
            s.worldPop();
        }
        for (int i = 15; i > 0; i--) {
            s.worldPush();
            try {
                s.getVar(y).remVal(15);
            } catch (ContradictionException e) {
                Assert.fail();
            }
        }
        for (int i = 15; i > 0; i--) {
            s.worldPop();
        }

    }

    @Test
    public void test4(){
        y = Choco.makeIntVar("Y", 1, 8);
        m.addVariable(Options.V_BTREE, y);
        m.addVariables(x, y);
        s = new CPSolver();
        s.read(m);
        yDom = (IntervalBTreeDomain) s.getVar(y).getDomain();

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

    @Test
    public void test5(){
        y = Choco.makeIntVar("Y", 0, 12);
        m.addVariable(Options.V_BTREE, y);
        m.addVariable(y);
        s = new CPSolver();
        s.read(m);
        yDom = (IntervalBTreeDomain) s.getVar(y).getDomain();

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

    @Test
     public void test_patakm() {
        String option = Options.V_BTREE;
        for(int i = 0; i < 1000; i++){
            Random r = new Random(i);
            Model m = new CPModel();
            int lb = r.nextInt(100) * (r.nextBoolean()?1:-1);
            int ub = lb+ r.nextInt(100);
            boolean isArray = r.nextBoolean();
            IntegerVariable link = (isArray?Choco.makeIntVar("v", new int[]{lb, ub}, option):Choco.makeIntVar("v", lb, ub, option));
            m.addVariable(link);
            Solver s = new CPSolver();
            s.read(m);
            s.solveAll();
            Assert.assertEquals("["+lb+","+ (isArray?"...":"")+ub+"]", isArray?(ub-lb==0?1:2):(ub-lb+1), s.getNbSolutions());
        }
    }


}
