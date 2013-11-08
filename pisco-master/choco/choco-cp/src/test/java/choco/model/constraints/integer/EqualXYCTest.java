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
import static choco.Choco.makeIntVar;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.EqualXYC;
import choco.cp.solver.constraints.integer.EqualXY_C;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;
/**
 * a class implementing tests for backtrackable search
 */
public class EqualXYCTest{
  private Logger logger = ChocoLogging.getTestLogger();
  private CPModel m;
  private CPSolver s;
  private IntegerVariable x;
  private IntegerVariable y;
  private IntegerVariable z;
  private SConstraint c1;
  private SConstraint c2;

    @Before
  public void setUp() {
    logger.fine("EqualXYC Testing...");
    m = new CPModel();
    s = new CPSolver();
    x = makeIntVar("X", 1, 5);
    y = makeIntVar("Y", 1, 5);
    z = makeIntVar("Z", 1, 5);
        m.addVariables(Options.V_BOUND, x, y, z);
    m.addVariables(x, y, z);
    s.read(m);
    c1 = new EqualXYC(s.getVar(x), s.getVar(y), 2);
    c2 = new EqualXYC(s.getVar(y), s.getVar(z), 1);
  }

    @After
  public void tearDown() {
    c1 = null;
    c2 = null;
    x = null;
    y = null;
    z = null;
    m = null;
    s = null;
  }

    @Test
  public void test1() {
    logger.finer("test1");
    s.post(c1);
    s.post(c2);

    try {
      s.propagate();
    } catch (ContradictionException e) {
      assertTrue(false);
    }
    logger.finest("X : " + s.getVar(x).getInf() + " - > " + s.getVar(x).getSup());
    logger.finest("Y : " + s.getVar(y).getInf() + " - > " + s.getVar(y).getSup());
    logger.finest("Z : " + s.getVar(z).getInf() + " - > " + s.getVar(z).getSup());
    assertEquals(4, s.getVar(x).getInf());
    assertEquals(5, s.getVar(x).getSup());
    assertEquals(2, s.getVar(y).getInf());
    assertEquals(3, s.getVar(y).getSup());
    assertEquals(1, s.getVar(z).getInf());
    assertEquals(2, s.getVar(z).getSup());
    logger.finest("domains OK after first propagate");

    try {
      s.getVar(z).setInf(2);
      s.propagate();
    } catch (ContradictionException e) {
      assertTrue(false);
    }

    assertTrue(s.getVar(x).isInstantiated());
    assertTrue(s.getVar(y).isInstantiated());
    assertTrue(s.getVar(z).isInstantiated());
    assertEquals(5, s.getVar(x).getVal());
    assertEquals(3, s.getVar(y).getVal());
    assertEquals(2, s.getVar(z).getVal());
  }

    @Test
    public void test2(){
        IntegerVariable x = Choco.makeIntVar("x", 1,10);
        IntegerVariable y = Choco.makeIntVar("y", 1,10);
        int c = 5;

        IntegerExpressionVariable e1 = Choco.plus(x, y);
        Constraint cs = Choco.eq(e1,c);

        CPModel model = new CPModel();
        model.addConstraint(cs);

        CPSolver solver = new CPSolver();
        solver.read(model);

        Assert.assertTrue("wrong", EqualXY_C.class.isInstance(solver.getCstr(cs)));
    }

    @Test
    public void test3(){
        IntegerVariable x = Choco.makeIntVar("x", 1,10);
        IntegerVariable y = Choco.makeIntVar("y", 1,10);
        int c = 5;

        IntegerExpressionVariable e1 = Choco.plus(x,c);
        Constraint cs = Choco.eq(e1,y);

        CPModel model = new CPModel();
        model.addConstraint(cs);

        CPSolver solver = new CPSolver();
        solver.read(model);

        Assert.assertTrue("wrong", EqualXYC.class.isInstance(solver.getCstr(cs)));
    }
}

