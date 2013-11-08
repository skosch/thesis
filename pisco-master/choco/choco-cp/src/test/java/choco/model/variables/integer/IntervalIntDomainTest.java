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

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.cp.solver.variables.integer.IntervalIntDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static choco.Choco.makeIntVar;
import static org.junit.Assert.*;

// CVS Information
// File:               $RCSfile: IntervalIntDomainTest.java,v $
// Version:            $Revision: 1.4 $
// Last Modification:  $Date: 2007/07/16 15:17:33 $
// Last Contributor:   $Author: menana $

public class IntervalIntDomainTest{
	private final static Logger LOGGER = ChocoLogging.getTestLogger();
  private Model m;
  private Solver s;
  private IntegerVariable x, y;
  IntervalIntDomain yDom;

    @Before
  public void setUp() {
    LOGGER.fine("BitSetIntDomain Testing...");
    m = new CPModel();
    x = makeIntVar("X", 1, 100);
    y = makeIntVar("Y", 1, 15);
        m.addVariables(Options.V_BOUND, x, y);
    s = new CPSolver();
    s.read(m);
    yDom = (IntervalIntDomain) s.getVar(y).getDomain();
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

      s.getVar(y).getEvent().addPropagatedEvents(IntVarEvent.BOUNDS_MASK);
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

      /**
   * testing freeze and release for the delta domain
   */
  @Test
  public void test3() {
    LOGGER.finer("test2");

    yDom.freezeDeltaDomain();
    assertTrue(yDom.releaseDeltaDomain());

    yDom.updateInf(2);
    yDom.updateSup(12);
    yDom.freezeDeltaDomain();
    yDom.updateInf(3);
    assertTrue(yDom.releaseDeltaDomain());

    yDom.freezeDeltaDomain();
    assertTrue(yDom.releaseDeltaDomain());
  }
}
