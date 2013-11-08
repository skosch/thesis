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

//     ___  ___         PaLM library for Choco system
//    /__ \/ __\        (c) 2001 - 2004 -- Narendra Jussien
//   ____\ |/_____
//  /_____\/_____ \     PalmExplanation based constraint programming tool
// |/   (_)(_)   \|
//       \ /            Version 0.1
//       \ /            January 2004
//       \ /
// ______\_/_______     Contibutors: Fran�ois Laburthe, Hadrien Cambazard, Guillaume Rochart...

package choco.ecp.solver.propagation.dbt;

import choco.cp.solver.propagation.ChocEngine;
import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.explanations.dbt.PalmConstraintPlugin;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.propagation.PalmRealVarEvent;
import choco.ecp.solver.search.dbt.PalmContradiction;
import choco.ecp.solver.variables.integer.dbt.PalmIntVar;
import choco.ecp.solver.variables.real.PalmRealVar;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.Propagator;
import choco.kernel.solver.propagation.VarEvent;
import choco.kernel.solver.variables.Var;

public class PalmEngine extends ChocEngine {
  /**
   * Indicates is a contradiction has be found.
   */

  private boolean contradictory;


  /**
   * A dummy variable. Useful for raising PalmFakeContradictions.
   */

  private PalmIntVar dummyVariable;


  /**
   * Constructs an engine with the specified problem.
   *
   * @param pb The problem to associate with this engine.
   */

  public PalmEngine(Solver pb) {
    super(pb);
    this.varEventQueue = null; // force le GC pour la queue cr??e
    this.varEventQueue = new PalmVarEventQueue();
  }


  /**
   * Resets all the events in the queue (no cause, and in the queue (no popping events anymore)).
   */

  public void resetEvents() {
    ((PalmVarEventQueue) this.varEventQueue).reset();
  }


  /**
   * Posts an inf bound restoration prop.
   *
   * @param v The variable on which the inf bound is restored.
   */

  public void postRestoreInf(PalmIntVar v) {
    postEvent(v, PalmIntVarEvent.NOCAUSE, PalmIntVarEvent.RESTINF);
  }


  /**
   * Posts a sup bound restoration prop.
   *
   * @param v The variable on which the sup bound is restored.
   */

  public void postRestoreSup(PalmIntVar v) {
    postEvent(v, PalmIntVarEvent.NOCAUSE, PalmIntVarEvent.RESTSUP);
  }


  /**
   * Posts an inf bound restoration prop.
   *
   * @param v The variable on which the inf bound is restored.
   */

  public void postRestoreInf(PalmRealVar v) {
    postEvent(v, PalmIntVarEvent.NOCAUSE, PalmRealVarEvent.RESTINF);
  }


  /**
   * Posts a sup bound restoration prop.
   *
   * @param v The variable on which the sup bound is restored.
   */

  public void postRestoreSup(PalmRealVar v) {
    postEvent(v, PalmIntVarEvent.NOCAUSE, PalmRealVarEvent.RESTSUP);
  }


  /**
   * Posts value restoration.
   *
   * @param v     The variable that should be modified.
   * @param value The value restored.
   */

  public void postRestoreVal(PalmIntVar v, int value) {
    postEvent(v, PalmIntVarEvent.NOCAUSE, PalmIntVarEvent.RESTVAL);
  }


  /**
   * Posts value removal. Needs to be overriden by Palm ?
   * @param v The modified variable.
   * @param x The removed value.
   * @param idx The index of the responsible constraint.
   */

  /*public void postRemoveVal(PalmIntVar v, int x, int idx) {
      postEvent(v,idx,PalmIntVarEvent.REMVAL);
      // TODO : ask naren
  }   */


  /**
   * Deletes all the events. <b>Sould be in Choco, not here !!</b>
   */

/*    public void flushEvents() {
        // c'est dans Choco !
        this.varEventQueue.flushEventQueue();
    }  */


  /**
   * Raises a Palm Contradiction caused by the specified variable.
   *
   * @param var The variable which is responsible of the contradiction.
   */

  public void raisePalmContradiction(Var var) throws ContradictionException {
    this.contradictionCause = var;
    this.contradictory = true;
    this.resetEvents();
    throw new PalmContradiction(var);
  }


  /**
   * Raises a fake Contradiction with the specified explain. Useful when the contradiction is not
   * due to only one domain.
   *
   * @param expl The explain of the contradiction.
   */

  public void raisePalmFakeContradiction(PalmExplanation expl) throws ContradictionException {
    if (dummyVariable == null) {
      dummyVariable = (PalmIntVar) ((PalmSolver) this.getSolver()).makeBoundIntVar("*dummy*", 0, 1, false);
    }
    dummyVariable.updateInf(2, VarEvent.NOCAUSE, expl);
  }


  /**
   * Raise a System Contradiction, that is a Choco Contradiction, that means that no solution can be found
   * anymore without removing constraint with a level upper that <code>PalmProblem.MAX_RELAX_LEVEL</code>.
   *
   * @throws ContradictionException
   */

  public void raiseSystemContradiction() throws ContradictionException {
    this.contradictory = false;
    this.flushEvents();
    throw new ContradictionException(this.getSolver());
  }


  /**
   * Removes properly a constraint: the constraint is deactivated, and all depending filtering decisions are
   * undone.
   *
   * @param constraint The constraint to be removed.
   */

  public void remove(Propagator constraint) {
    PalmConstraintPlugin pi = (PalmConstraintPlugin) constraint.getPlugIn();
    pi.removeDependance();
    constraint.setPassive();
    pi.undo();
    this.restoreVariableExplanations();
  }


  /**
   * Removes several constraints.
   *
   * @param constraints An array with all the constraints to remove.
   */

  public void remove(SConstraint[] constraints) {
    for (int i = 0; i < constraints.length; i++) {
      AbstractSConstraint constraint = (AbstractSConstraint) constraints[i];
      constraint.setPassive();
      ((PalmConstraintPlugin) constraint.getPlugIn()).undo();
    }
    this.restoreVariableExplanations();
  }


  /**
   * Updates explanations when constraints are removed.
   */

  private void restoreVariableExplanations() {
    ((PalmVarEventQueue) this.varEventQueue).restoreVariableExplanations();
  }


  /**
   * Checks if a contradiction has been reached.
   *
   * @return True if a contradiciton happened.
   */

  public boolean isContradictory() {
    return contradictory;
  }


  /**
   * Sets if a contradiciton has been reached.
   *
   * @param c The value to set.
   */

  public void setContradictory(boolean c) {
    this.contradictory = c;
  }


  /**
   * Resets the dummy variable.
   */

  public void resetDummyVariable() {
    this.dummyVariable = null;
  }
}
