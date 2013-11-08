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

package choco.kernel.solver.propagation.event;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.Var;

/**
 * Implements an
 * {@link PropagationEvent} for the variable events.
 */
public abstract class VarEvent <E extends Var> implements PropagationEvent{

    /**
   * empty bitvector for the event type.
   */
  public static final int NOEVENT = -2;

  /**
   * Semantic of the cause of an event: -1 means that the event is active without
   * any precise cause. (Equivalent of 0 in Claire version)
   */
  public static final int NOCAUSE = -1;

  /**
   * Semantic of the cause of an event: -1 means that the event is active without
   * any precise cause. (Equivalent of 0 in Claire version)
   */
  public static final int DOWDCAUSE = Integer.MIN_VALUE;

  /**
   * Cause of this basic var.
   */

  protected SConstraint cause = null;


  /**
   * empty bitvector for the event type.
   */
  public static final int EMPTYEVENT = 0;

  /**
   * The touched variable.
   */

  protected E modifiedVar;


  /**
   * active constraints to be propagated
   */
  // protected IStateBitSet activeConstraints;

  /**
   * stores the type of update performed on the variable
   */
  protected int eventType = EMPTYEVENT;

  /**
   * The events that should be fired for the constraints
   */
  protected int propagatedEvents = 0;

    /**
   * Constructs a variable event for the specified variable and with the given
   * basic events.
   */

  public VarEvent(E var) {
    this.modifiedVar = var;
    // activeConstraints = new IStateBitSet(getProblem().getEnvironment(),0);
  }

  /*
   public int getNbListeners() {
     return activeConstraints.cardinality();
     // return activeCycle.size();
   }
   */

  public final void addPropagatedEvents(int bitsmask) {
    propagatedEvents |= bitsmask;
  }

  public final int getPropagatedEvents() {
    return propagatedEvents;
  }

  /**
   * Returns the touched variable.
   */

  public final E getModifiedVar() {
    return modifiedVar;
  }

  /**
   * Returns the touched variable.
   */

  public final Object getModifiedObject() {
    return modifiedVar;
  }

  /**
   * freezes the state of the "delta domain": the set of values that are considered for removal
   * from the domain. Further removals will be treated as a further event.
   */
  protected void freeze() {
      cause = null;
  }

  protected boolean release() {
      return (cause != null);
  }

  /**
   * Propagates the event through calls to the propagation engine.
   *
   * @return true if the event has been fully propagated (and can thus be discarded), false otherwise
   * @throws choco.kernel.solver.ContradictionException
   */
  public abstract boolean propagateEvent() throws ContradictionException;

  /**
   * Clears the var: delegates to the basic events.
   */
  public abstract void clear();

  /**
   * Checks if a given listener is active or not
   *
   * @param idx the index of the listener among all listeners connected to the variable
   */


  public final boolean isActive(int idx) {
//    return activeCycle.isInCycle(idx);
//      return activeConstraints.get(idx);
    return true; // TODO FIXME
  }

  /**
   * Returns the cause of this basic var.
   */
  public final SConstraint getCause(){
      return cause;
  }

  public int getPriority() {
    return modifiedVar.getPriority();
  }

    public final int getEventType() {
        return eventType;
    }

    /**
     * Compute the constraint idx to inform only DomOverWDegBranching of contradiction.
     * @param idx constraint idx
     * @return constraint idx for DomOverWDeg
     */
    @Deprecated
    public static int domOverWDegIdx(int idx){
        return DOWDCAUSE + idx;
    }

    /**
     * Recompute the inital constraint idx for DomOverWDegBranching.
     * @param idx modified constraint idx
     * @return initial constraint idx 
     */
    @Deprecated
    public static int domOverWDegInitialIdx(int idx){
        return idx - DOWDCAUSE;
    }


    /**
   * tests whether the event is currently active (present in some queue) or not
   *
   * @return true if and only if the event is present in some queue, waiting to be handled
   *         (returns false if the event is either absent from the queue or is the current event,
   *         just popped from the queue and being propagated)
   */
  public final boolean isEnqueued() {
    return (eventType != EMPTYEVENT);
  }

  public void recordEventTypeAndCause(int basicEvt, final SConstraint constraint, final boolean forceAwake) {
    // if no such event was active on the same variable
//    if ((oldCause == NOEVENT) || (eventType == EMPTYEVENT)) {  // note: these two tests should be equivalent
    if (eventType == EMPTYEVENT) {
        assert((cause == null) || (eventType == EMPTYEVENT));
      // the varevent is reduced to basicEvt, and the cause is recorded
      eventType = (1 << basicEvt);
      if(!forceAwake){
          cause = constraint;
      }
    } else {
      // otherwise, this basic event is added to all previous updates that are possibly mending on the same variable
      eventType = (eventType | (1 << basicEvt));
      // in case the cause of this update is different from the previous cause, all causes are forgotten
      // (so that the constraints that caused the event will be reawaken)
      if (cause != constraint) {
        cause = null;
      }
    }
  }

}
