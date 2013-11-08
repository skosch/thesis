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

package choco.model.constraints.real;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.real.MixedEqXY;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.real.RealIntervalConstant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static choco.Choco.makeIntVar;
import static choco.Choco.makeRealVar;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * <p/>
 * An open-source Constraint Programming Kernel
 * for Research and Education
 * <p/>
 * Created by: Guillaume on 18 juin 2004
 */
public class MixedEqualityTest  {
  CPModel m;
    CPSolver s;
  RealVariable v1;
  IntegerVariable v2;

  @Before
  public void setUp() {
    m = new CPModel();
    s = new CPSolver();
    v1 = makeRealVar("v1", 0.0, 8.0);
    v2 = makeIntVar("v2", 2, 10);
      m.addVariables(v1, v2);
      s.read(m);
    s.post(new MixedEqXY(s.getVar(v1), s.getVar(v2)));
  }

  @After
  public void tearDown() {
    m = null;
      s = null;
    v1 = null;
    v2 = null;
  }

  @Test
  public void testInt2Real() {
    try {
      s.propagate();
      assertEquals(2.0, s.getVar(v1).getInf(), 1e-10);
      s.getVar(v2).setSup(6);
      s.propagate();
      assertEquals(6.0, s.getVar(v1).getSup(), 1e-10);
    } catch (ContradictionException e) {
      assertTrue("The model is consistent !", false);
    }
  }

  @Test
  public void testReal2Int() {
    try {
      s.propagate();
      assertEquals(8, s.getVar(v2).getSup());
      s.getVar(v1).intersect(new RealIntervalConstant(4.0, Double.POSITIVE_INFINITY));
      s.propagate();
      assertEquals(4, s.getVar(v2).getInf());
    } catch (ContradictionException e) {
      assertTrue("The model is consistent !", false);
    }
  }
}
