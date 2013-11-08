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
import choco.kernel.solver.propagation.Propagator;

/**
 * A class for constraint revisions in the propagation process.
 */
public class ConstraintEvent implements PropagationEvent {

    private static int _value = 1;
    public final static int UNARY = _value++;
    public final static int BINARY = _value++;
    public final static int TERNARY = _value++;
    public final static int LINEAR = _value++;
    public final static int QUADRATIC = _value++;
    public final static int CUBIC = _value++;
    public final static int VERY_SLOW = _value++;
    public final static int NB_PRIORITY = _value;


  /**
   * The touched constraint.
   */

  private Propagator touchedConstraint;


  /**
   * Specifies if the constraint should be initialized.
   */

  private boolean initialized = false;


  /**
   * Returns the priority of the var.
   */

  private int priority = (-1);

  /**
   * Constructs a new var with the specified values for the fileds.
   */

  public ConstraintEvent(Propagator constraint, boolean init, int prio) {
    this.touchedConstraint = constraint;
    this.initialized = init;
    this.priority = prio;
  }

  public Object getModifiedObject() {
    return touchedConstraint;
  }

  /**
   * Returns the priority of the var.
   */

  @Override
  public int getPriority() {
    return priority;
  }


  /**
   * Propagates the var: awake or propagate depending on the init status.
   *
   * @throws choco.kernel.solver.ContradictionException
   *
   */

  public boolean propagateEvent() throws ContradictionException {
      if (this.initialized) {
          assert (this.touchedConstraint.isActive());
          this.touchedConstraint.propagate();
      } else {
          this.touchedConstraint.setActiveSilently();
          this.touchedConstraint.awake();
      }
      return true;
  }


  /**
   * Returns if the constraint is initialized.
   */

  public boolean isInitialized() {
    return this.initialized;
  }


  /**
   * Sets if the constraint is initialized.
   */

  public void setInitialized(boolean init) {
    this.initialized = init;
  }


  /**
   * Testing whether an event is active in the propagation network
   */

  public boolean isActive(int idx) {
    return true;
  }


  /**
   * Clears the var. This should not be called with this kind of var.
   */

  public void clear() {
	  LOGGER.warning("Const Awake Event does not need to be cleared !");
  }
}

