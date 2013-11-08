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

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.ecp.solver.propagation.PalmIntVarListener;
import choco.ecp.solver.variables.integer.dbt.PalmBitSetIntDomain;
import choco.ecp.solver.variables.integer.dbt.PalmIntVar;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.structure.PartiallyStoredIntVector;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.propagation.VarEvent;
import choco.kernel.solver.variables.AbstractVar;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;

public class PalmIntVarEvent extends IntVarEvent implements PalmVarEvent {

  public int oldEventType;

  /**
   * This boolean was before in the queue
   */
  public boolean isPopping = false;

  /**
   * Constant value associated to the inf bound restoration prop.
   */

  public final static int RESTINF = 5;


  /**
   * Constant value associated to the sup bound restoration prop.
   */

  public final static int RESTSUP = 6;


  /**
   * Constant value associated to the value restoration prop.
   */

  public final static int RESTVAL = 7;


  /**
   * Creates an prop for the specified variable.
   *
   * @param var The variable this prop is reponsible for.
   */

  public PalmIntVarEvent(Var var) {
    super((AbstractVar) var);
  }


  /**
   * Computes the priority of the prop. Actually, it returns 0 for restoration events (that is urgent
   * events) and 1 for the others.
   *
   * @return The priority of this prop.
   */

  public int getPriority() {
    if (BitSet.getHeavierBit(this.getEventType()) >= RESTINF) return 0;
    return 1;
  }


  /**
   * Generic propagation method. Calls relevant methods depending on the kind of event. It handles some
   * new events for restoration purpose and call the Choco method <code>super.propagateEvent()</code>.
   *
   * @throws ContradictionException
   */

  public boolean propagateEvent() throws ContradictionException {
    // isPopping, oldEventType et oldCause permettent de remettre en etat l'evenement si une contradiction
    // a lieu pendant le traitement de cet evenement. Cf public void reset() et
    // PalmVarEventQueue.resetPopping().
    isPopping = true;
    this.oldEventType = this.eventType;
    assert (this.oldEventType != VarEvent.EMPTYEVENT);
    int oldCause = this.cause;

    // Traitement des restarations
    this.cause = VarEvent.NOEVENT;
    this.eventType = VarEvent.EMPTYEVENT;
    if (BitSet.getBit(this.oldEventType, RESTINF))
      propagateRestInfEvent(oldCause);
    if (BitSet.getBit(this.oldEventType, RESTSUP))
      propagateRestSupEvent(oldCause);
    if (BitSet.getBit(this.oldEventType, RESTVAL))
      propagateRestValEvent(oldCause); //,getRestoreIterator());

    // On sauvegarde l'etat apres le traitement des restaurations
    int newEventType = this.eventType;
    int newCause = this.cause;
    if (this.eventType != VarEvent.EMPTYEVENT) {
      this.modifiedVar.getProblem().getPropagationEngine().getVarEventQueue().remove(this);
    }

    // On fait comme si Palm n'etait pas la pour Choco
    this.eventType = this.oldEventType & 31;
    this.cause = oldCause;

    // On laisse Choco propager
    boolean ret = super.propagateEvent();

    // On retablit des valeurs consistantes avec les resultats obtenus avec Palm
    if (this.eventType == VarEvent.EMPTYEVENT && newEventType != VarEvent.EMPTYEVENT) {
      this.modifiedVar.getProblem().getPropagationEngine().getVarEventQueue().pushEvent(this);
    }
    this.eventType |= newEventType;
    if (cause == VarEvent.NOEVENT) {
      this.cause = newCause;
    } else if (newCause != VarEvent.NOEVENT) {
      this.cause = VarEvent.NOCAUSE;
    }

    // On reinitialise l'evenement puisqu'il n'y a pas eu de contradiction !
    isPopping = false;
    if (((PalmIntVar) this.modifiedVar).hasEnumeratedDomain())
      ((PalmBitSetIntDomain) ((PalmIntVar) this.modifiedVar).getDomain()).releaseRepairDomain();

    // Rappel : ^ = xor logique
    assert (this.eventType == VarEvent.EMPTYEVENT ^ ((PalmVarEventQueue) this.modifiedVar.getProblem().getPropagationEngine().getVarEventQueue()).contains(this));

    return ret;
  }


  /**
   * Propagates the lower bound restoration event.
   */

  public void propagateRestInfEvent(int evtCause) throws ContradictionException {
    AbstractVar v = getModifiedVar();
    PartiallyStoredVector constraints = v.getConstraintVector();
    PartiallyStoredIntVector indices = v.getIndexVector();

    for (IntIterator cit = constraints.getIndexIterator(); cit.hasNext();) {
      int idx = cit.next();
      if (idx != evtCause) {
        PalmIntVarListener c = (PalmIntVarListener) constraints.get(idx);
        if (c.isActive()) {
          int i = indices.get(idx);
          c.awakeOnRestoreInf(i);
        }
      }
    }
  }


  /**
   * Propagates the upper bound restoration event.
   */

  public void propagateRestSupEvent(int evtCause) throws ContradictionException {
    AbstractVar v = getModifiedVar();
    PartiallyStoredVector constraints = v.getConstraintVector();
    PartiallyStoredIntVector indices = v.getIndexVector();

    for (IntIterator cit = constraints.getIndexIterator(); cit.hasNext();) {
      int idx = cit.next();
      if (idx != evtCause) {
        PalmIntVarListener c = (PalmIntVarListener) constraints.get(idx);
        if (c.isActive()) {
          int i = indices.get(idx);
          c.awakeOnRestoreSup(i);
        }
      }
    }
  }

  /**
   * Propagates a value restoration event.
   */
  public void propagateRestValEvent(int evtCause) throws ContradictionException {
    AbstractVar v = getModifiedVar();
    PartiallyStoredVector constraints = v.getConstraintVector();
    PartiallyStoredIntVector indices = v.getIndexVector();

    for (IntIterator cit = constraints.getIndexIterator(); cit.hasNext();) {
      int idx = cit.next();
      if (idx != evtCause) {
        PalmIntVarListener c = (PalmIntVarListener) constraints.get(idx);
        if (c.isActive()) {
          int i = indices.get(idx);
          c.awakeOnRestoreVal(i, this.getRestoreIterator());
        }
      }
    }
  }


  /**
   * Updates explanations for the variable: when bounds are completely restored, the unrelevant explanations
   * are removed.
   */

  public void restoreVariableExplanation() { // TODO : should be renamed ? not only explanation...
    if (BitSet.getBit(this.eventType, RESTINF))
      ((PalmIntVar) this.getModifiedVar()).resetExplanationOnInf();
    if (BitSet.getBit(this.eventType, RESTSUP))
      ((PalmIntVar) this.getModifiedVar()).resetExplanationOnSup();
    // removal chain has to be checked to avoid inconsistent state after a value restoration
    // cf scenario 1
    if (((PalmIntVar) this.getModifiedVar()).hasEnumeratedDomain())
      ((PalmBitSetIntDomain) ((PalmIntVar) this.getModifiedVar()).getDomain()).checkRemovalChain();
  }


  /**
   * Returns an iterator on the chain containing all the restored values.
   */

  public DisposableIntIterator getRestoreIterator() {
    return ((PalmBitSetIntDomain) ((IntDomainVar) modifiedVar).getDomain()).getRepairIterator();
  }


  /**
   * If a contradiction occurs when the event is handled, the event is reinitialized.
   */

  public void reset() {
    // Il faut remettre le bon type d'evenement pour eviter de ne remettre cet evenement dans la queue alors
    // qu'il y ait deja. Cela peut entrainer des probleme de chaines cycliques pour la liste des valeurs
    // restaurees.
    this.eventType = this.oldEventType;
    this.cause = VarEvent.NOCAUSE;
    if (((IntDomainVar) this.modifiedVar).hasEnumeratedDomain()) {
      ((PalmBitSetIntDomain) ((IntDomainVar) this.modifiedVar).getDomain()).resetRemovalChain();
    }

    assert (this.eventType != VarEvent.EMPTYEVENT);
  }

  public boolean isPopping() {
    return this.isPopping;
  }

  public void setPopping(boolean b) {
    this.isPopping = b;
  }
}
