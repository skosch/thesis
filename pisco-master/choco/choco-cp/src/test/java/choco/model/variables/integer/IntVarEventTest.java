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

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.EqualXYC;
import choco.cp.solver.propagation.ChocoEngine;
import choco.cp.solver.variables.integer.IntDomainVarImpl;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.Constant;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.propagation.event.PropagationEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class IntVarEventTest  {
	private final static Logger LOGGER = ChocoLogging.getTestLogger();
  private Solver s;
  private ChocoEngine pe;
  private IntDomainVarImpl x;
  private IntDomainVarImpl y;
  private IntDomainVarImpl z;
  private SConstraint c1;
  private SConstraint c2;
  PropagationEvent evt;

  class LocalSConstraintClass extends AbstractBinIntSConstraint {
    public boolean isSatisfied() {
      return false;
    }

    public void propagate() {
    }

    public void awakeOnInf(int idx) {
    }

    public void awakeOnSup(int idx) {
    }
      
    public Object clone() throws CloneNotSupportedException {
      return super.clone();
    }

    public LocalSConstraintClass(IntDomainVar x, IntDomainVar y) {
      super(x, y);
    }
  }

    @Before
  public void setUp() {
    LOGGER.fine("IntVarEvent Testing...");
    s = new CPSolver();
    pe = (ChocoEngine) s.getPropagationEngine();
    x = (IntDomainVarImpl) ((CPSolver)s).createIntVar("X",1, 0, 100);
    y = (IntDomainVarImpl) ((CPSolver)s).createIntVar("Y", 1, 0, 100);
    z = (IntDomainVarImpl) ((CPSolver)s).createIntVar("Z",1,  0, 100);
    c1 = new LocalSConstraintClass(x, y);
    c2 = new LocalSConstraintClass(y, z);
  }

    @After
  public void tearDown() {
    c1 = null;
    c2 = null;
    x = null;
    y = null;
    z = null;
    s = null;
    pe = null;
    evt = null;
  }

    @Test
  public void test1() {
    assertEquals(0, pe.getNbPendingEvents());
    s.post(c1);
    s.post(c2);
    assertEquals(2, pe.getNbPendingEvents());
    HashSet<SConstraint> expectedSet = new HashSet<SConstraint>();
    expectedSet.add(c1);
    expectedSet.add(c2);
    HashSet<SConstraint> tmp = new HashSet<SConstraint>();
    tmp.add((SConstraint)pe.getPendingEvent(0).getModifiedObject());
    tmp.add((SConstraint)pe.getPendingEvent(1).getModifiedObject());
    assertEquals(expectedSet, tmp);
    try {
      s.propagate();
    } catch (ContradictionException e) {
      assertFalse(true);
    }
    assertEquals(0, pe.getNbPendingEvents());
    x.getDomain().updateInf(1);
    pe.postUpdateInf(x, c2, false);
    y.getDomain().updateSup(95);
    pe.postUpdateSup(y, c2, false);
    y.getDomain().updateInf(3);
    pe.postUpdateInf(y, c1, false); // and not a value above 1, such as 2 !!

    assertEquals(2, pe.getNbPendingEvents());
    evt = pe.getPendingEvent(0);
    assertEquals(evt.getModifiedObject(), x);
    assertEquals(IntVarEvent.INCINF_MASK + IntVarEvent.REMVAL_MASK, ((IntVarEvent) evt).getEventType());
    assertEquals(c2, ((IntVarEvent) evt).getCause());

    evt = pe.getPendingEvent(1);
    assertEquals(evt.getModifiedObject(), y);
    assertEquals(IntVarEvent.BOUNDS_MASK + IntVarEvent.REMVAL_MASK, ((IntVarEvent) evt).getEventType());
    assertEquals(null, ((IntVarEvent) evt).getCause());
  }

  /**
   * tests that a bound event on a variable with two constraints and no cause, yields two propagation steps
   */
  @Test
  public void test2() {
    c1 = new EqualXYC(x, y, 2);
    c2 = new EqualXYC(y, z, 1);
    assertEquals(0, pe.getNbPendingEvents());
    s.post(c1);
    s.post(c2);
    assertEquals(2, pe.getNbPendingEvents());
    HashSet<SConstraint> expectedSet = new HashSet<SConstraint>();
    expectedSet.add(c1);
    expectedSet.add(c2);
    HashSet<SConstraint> tmp = new HashSet<SConstraint>();
    tmp.add((SConstraint)pe.getPendingEvent(0).getModifiedObject());
    tmp.add((SConstraint)pe.getPendingEvent(1).getModifiedObject());
    assertEquals(expectedSet, tmp);
    try {
      s.propagate();
    } catch (ContradictionException e) {
      assertFalse(true);
    }
    assertEquals(0, pe.getNbPendingEvents());

    y.getDomain().updateSup(90);
    pe.postUpdateSup(y, c1, false);
    y.getDomain().updateInf(10);
    pe.postUpdateInf(y, c2, false);
    assertEquals(1, pe.getNbPendingEvents());
    evt = pe.getPendingEvent(0);
    assertEquals(evt.getModifiedObject(), y);
    assertEquals(IntVarEvent.BOUNDS_MASK + IntVarEvent.REMVAL_MASK, ((IntVarEvent) evt).getEventType());

    PartiallyStoredVector constraints = y.getConstraintVector();
    DisposableIntIterator cit = constraints.getIndexIterator();
    assertTrue(cit.hasNext());
    assertEquals(Constant.STORED_OFFSET, cit.next());
    assertTrue(cit.hasNext());
    assertEquals(Constant.STORED_OFFSET + 1, cit.next());
    assertFalse(cit.hasNext());
  }

}
