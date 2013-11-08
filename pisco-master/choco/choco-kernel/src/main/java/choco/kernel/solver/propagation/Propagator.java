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

package choco.kernel.solver.propagation;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.propagation.event.PropagationEvent;

import java.util.EventListener;

/**
 * An interface for all implementations of listeners.
 */
public abstract class Propagator implements EventListener {

    protected PropagationEngine propagationEngine;

    	/**
	 * a field for storing whether the constraint is active or not
	 */
	protected IStateBool active;


    /**
     * The constraint <i>awake</i> var attached to the constraint.
     */

    protected final ConstraintEvent constAwakeEvent;

    /**
	 * The priority of the constraint.
	 */

	protected final int priority;

    protected Propagator() {
        this(ConstraintEvent.UNARY);
    }

    protected Propagator(int priority) {
        this.priority = priority;
        this.constAwakeEvent = new ConstraintEvent(this, false, priority);
    }

    /**
     * This function connects a constraint with its variables in several ways.
     * Note that it may only be called once the constraint
     * has been fully created and is being posted to a model.
     * Note that it should be called only once per constraint.
     * This can be a dynamic addition (undone upon backtracking) or not
     *
     * @param dynamicAddition
     */
    public abstract void addListener(boolean dynamicAddition);

    /**
     * <i>Utility:</i>
     * Testing if all the variables involved in the constraint are instantiated.
     *
     * @return whether all the variables have been completely instantiated
     */

    public abstract boolean isCompletelyInstantiated();


    /**
     * Forces a propagation of the constraint.
     *
     * @param isInitialPropagation indicates if it is the initial propagation or not
     */

    public final void constAwake(boolean isInitialPropagation) {
        propagationEngine.postConstAwake(this, isInitialPropagation);
    }


    /**
     * <i>Propagation:</i>
     * Propagating the constraint for the very first time until local
     * consistency is reached.
     *
     * @throws ContradictionException contradiction exception
     */

    public void awake() throws ContradictionException {
        this.propagate();
    }


    /**
     * <i>Propagation:</i>
     * Propagating the constraint until local consistency is reached.
     *
     * @throws choco.kernel.solver.ContradictionException
     *          contradiction exception
     */

    public abstract void propagate() throws ContradictionException;


    /**
     * Activate a constraint.
     * @param environment current environment
     */
    public final void activate(IEnvironment environment) {
        this.active = environment.makeBool(false);
    }

    /**
	 * Un-freezing a constraint (this is useful for mimicking dynamic
	 * constraint posts...).
	 */

	public final void setActive() {
		if (!(isActive())) {
			setActiveSilently();
			constAwake(true);
		}
	}

	public final void setActiveSilently() {
		active.set(true);
	}


	/**
	 * Freezing a constraint (this is useful for backtracking when mimicking
	 * dynamic constraint posts...).
	 */

	public final void setPassive() {
		if (active != null) {
			active.set(false);
			propagationEngine.desactivatePropagator(this);
		}
	}


	/**
	 * Checks if the constraint is active (e.g. plays a role in the propagation phase).
	 *
	 * @return true if the constraint is indeed currently active
	 */

	public final boolean isActive() {
		return active.get();
	}

    /**
	 * records that a constraint is now entailed (therefore it is now useless to propagate it again)
	 */
	public final void setEntailed() {
		setPassive();
	}

    /**
     * <i>Propagation:</i>
     * Accessing the priority level of the queue handling the propagation
     * of the constraint. Results range from 1 (most reactive, for listeners
     * with fast propagation algorithms) to 4 (most delayed, for listeners
     * with lengthy propagation algorithms).
     *
     * @return the priority level of the queue handling the propagation of the constraint
     */
	public final int getPriority() {
		return priority;
	}
    /**
     * Returns the constraint awake var associated with this constraint.
     *
     * @return the constraint awake var associated with this constraint.
     */

    public final PropagationEvent getEvent() {
        return constAwakeEvent;
    }

    /**
     * Checks whether the constraint is definitely satisfied, no matter what further restrictions
     * occur to the domain of its variables.
     *
     * @return wether the constraint is entailed
     */
    public abstract Boolean isEntailed();

    /**
     * tests if the constraint is consistent with respect to the current state of domains
     *
     * @return wether the constraint is consistent
     */
    public abstract boolean isConsistent();


    public int getFilteredEventMask(int idx) {
        return 0x0FFFF;
    }


    /**
     * Define the propagation engine within the constraint.
     * Mandatory to throw {@link ContradictionException}.
     *
     * @param propEng the current propagation engine
     */
    public void setPropagationEngine(PropagationEngine propEng) {
        this.propagationEngine = propEng;
    }

    /**
	 * raise a contradiction during propagation when the constraint can definitely not be satisfied given the current domains
	 * @throws ContradictionException contradiction exception
	 */
	public void fail() throws ContradictionException {
		propagationEngine.raiseContradiction(this);
	}

}