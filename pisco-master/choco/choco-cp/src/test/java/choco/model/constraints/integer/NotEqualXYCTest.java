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


/* File choco.currentElement.search.NotEqualXYCTest.java, last modified by Francois 21 sept. 2003 10:59:44 */

package choco.model.constraints.integer;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.NotEqualXYC;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static choco.Choco.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NotEqualXYCTest {
	private final static Logger LOGGER = ChocoLogging.getTestLogger();
  private CPModel m;
  private CPSolver s;
  private IntegerVariable x;
  private IntegerVariable y;
  private IntegerVariable z;
  private SConstraint c1;
  private SConstraint c2;

    @Before
  public void setUp() {
    LOGGER.fine("NotEqualXYC Testing...");
    m = new CPModel();
        s = new CPSolver();
    x = makeIntVar("X", 1, 5);
    y = makeIntVar("Y", 1, 5);
    z = makeIntVar("Z", 1, 5);
        m.addVariables(x, y, z);
        s.read(m);
    c1 = new NotEqualXYC(s.getVar(x), s.getVar(y), -2);
    c2 = new NotEqualXYC(s.getVar(y), s.getVar(z), 1);
  }

    @After
  public void tearDown() {
    c1 = null;
    c2 = null;
    x = null;
    y = null;
    z = null;
    s = null;
        m=null;
  }

    @Test
  public void test1() {
    LOGGER.finer("test1");
    try {
      s.post(c1);
      s.getVar(x).setSup(2);
      s.getVar(y).setVal(3);
      s.propagate();
      assertFalse(s.getVar(x).canBeInstantiatedTo(1));
      assertTrue(s.getVar(x).isInstantiatedTo(2));
    } catch (ContradictionException e) {
      assertFalse(true);
    }
  }

    @Test
  public void test2() {
    LOGGER.finer("test2");
    try {
      s.post(c1);
      s.post(c2);
      s.getVar(y).setVal(3);
      s.propagate();
      assertFalse(s.getVar(x).canBeInstantiatedTo(1));
      assertFalse(s.getVar(z).canBeInstantiatedTo(2));
    } catch (ContradictionException e) {
      assertFalse(true);
    }
  }

	public static void main(String[] args) {
		CPModel mod = new CPModel();
		int n = 11;
		IntegerVariable[] vs = makeIntVarArray("yo", n, 0, n-2);
		for (int i = 0; i < vs.length; i++) {
			for (int j = i + 1; j < vs.length; j++) {
				mod.addConstraint(neq(vs[i], vs[j]));
			}
		}
		CPSolver s = new CPSolver();
		s.read(mod);
		s.setBackTrackLimit(Integer.MAX_VALUE);
		s.solve();
		s.printRuntimeStatistics();
	}

}
