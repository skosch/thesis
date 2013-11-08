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

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.listener.PropagationEngineListener;
import choco.kernel.solver.search.measure.FailMeasure;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.real.RealVar;
import choco.kernel.solver.variables.set.SetVar;

import java.util.logging.Logger;

/**
 * An interface for all implementations of propagation engines.
 */
public interface PropagationEngine {

	public final static Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * Returns the fails counter, FailMeasure
     * @return fails counter
     */ 
	FailMeasure getFailMeasure();

    void clear();

    void loadSettings(Configuration configuration);

    //****************************************************************************************************************//
    //*************************************** CONTRADICTION **********************************************************//
    //****************************************************************************************************************//

    /**
	 * Raising a contradiction with a cause.
     * @param cause contradiction cause
     * @throws choco.kernel.solver.ContradictionException 
     */
	public void raiseContradiction(Object cause) throws ContradictionException;

    /**
	 * Raising a contradiction with a cause and a movement
     * @param cause contradiction cause
     * @param move next move after throwing the contradiction
     * @throws choco.kernel.solver.ContradictionException
     */
	public void raiseContradiction(Object cause, int move) throws ContradictionException;

    /**
	 * Raising a contradiction with a variable.
     * @param cidx index of the constraint in the constraints network
     * @param variable variable causing the contradiction
     * @param cause constraint causing the contradiction
     * @throws choco.kernel.solver.ContradictionException
     */
    @Deprecated
    public void raiseContradiction(int cidx, Var variable, final SConstraint cause) throws ContradictionException;


    //****************************************************************************************************************//
    //************************************ EVENTS ********************************************************************//
    //****************************************************************************************************************//

	/**
	 * Removes all pending events (used when interrupting a propagation because
	 * a contradiction has been raised)
	 */
	public void flushEvents();

	/**
	 * checking that the propagation engine remains in a proper state
     * @return return <code>true</code> if the state is proper, <code>false</code> otherwise
     */
	public boolean checkCleanState();
    
	/**
	 * Generic method to post events. The caller is reponsible of basic event
	 * type field: it should be meaningful for the the associate kind of event.
	 * @param v The modified variable.
     * @param basicEvt integer specifying mdofication kind for the attached
     * @param constraint constraint at the origin of the modification 
     * @param forceAwake should the constraint be informed of the current event
     */
	void postEvent(Var v, int basicEvt, final SConstraint constraint, final boolean forceAwake);

    /**
	 * Specific method to post modification of lower bound event 
	 * @param v The modified integer variable.
     * @param constraint constraint at the origin of the modification 
     * @param forceAwake should the constraint be informed of the current event
     */
    void postUpdateInf(IntDomainVar v, final SConstraint constraint, final boolean forceAwake);

    /**
	 * Specific method to post modification of upper bound event 
	 * @param v The modified integer variable.
     * @param constraint constraint at the origin of the modification 
     * @param forceAwake should the constraint be informed of the current event
     */
	void postUpdateSup(IntDomainVar v, final SConstraint constraint, final boolean forceAwake);

    /**
	 * Specific method to post instantiation event
	 * @param v The modified integer variable.
     * @param constraint constraint at the origin of the modification 
     * @param forceAwake should the constraint be informed of the current event
     */
	void postInstInt(IntDomainVar v, final SConstraint constraint, final boolean forceAwake);

    /**
	 * Specific method to post removal event
	 * @param v The modified integer variable.
     * @param x the value removed
     * @param constraint constraint at the origin of the modification 
     * @param forceAwake should the constraint be informed of the current event
     */
	void postRemoveVal(IntDomainVar v, int x, final SConstraint constraint, final boolean forceAwake);

    /**
	 * Specific method to post modification of lower bound event 
	 * @param v The modified real variable.
     * @param constraint constraint at the origin of the modification 
     * @param forceAwake should the constraint be informed of the current event
     */
	void postUpdateInf(RealVar v, final SConstraint constraint, final boolean forceAwake);

    /**
	 * Specific method to post modification of upper bound event 
	 * @param v The modified real variable.
     * @param constraint constraint at the origin of the modification 
     * @param forceAwake should the constraint be informed of the current event
     */
	void postUpdateSup(RealVar v, final SConstraint constraint, final boolean forceAwake);

    /**
	 * Specific method to post modification of envelope removal event 
	 * @param v The modified set variable.
     * @param constraint constraint at the origin of the modification 
     * @param forceAwake should the constraint be informed of the current event
     */
	void postRemEnv(SetVar v, final SConstraint constraint, final boolean forceAwake);

    /**
	 * Specific method to post modification of kernel addition event 
	 * @param v The modified set variable.
     * @param constraint constraint at the origin of the modification 
     * @param forceAwake should the constraint be informed of the current event
     */
	void postAddKer(SetVar v, final SConstraint constraint, final boolean forceAwake);

    /**
	 * Specific method to post instantiation event
	 * @param v The modified set variable.
     * @param constraint constraint at the origin of the modification 
     * @param forceAwake should the constraint be informed of the current event
     */
	void postInstSet(SetVar v, final SConstraint constraint, final boolean forceAwake);

    /**
     *  Post a constraint event.
     * @param constraint constraint to call
     * @param init indicates wether this call should be a call to {@link choco.kernel.solver.propagation.Propagator#awake()} (<code>true</code>)
     * or to {@link choco.kernel.solver.propagation.Propagator#Propagator()} (<code>false</code>).
     * @return <code>true</code> if the event has been added, <code>false</code> if it was already present
     */
	boolean postConstAwake(Propagator constraint, boolean init);

    /**
     * Register a constraint. Any constraint declared in the {@link choco.kernel.solver.Solver} should be present in <code>this</code>.
     * @param propagator element to declare to <code>this</code>
     */
	void registerPropagator(Propagator propagator);

    /**
     * Desactivate a constraint. This constraint won't be informed of any new events occuring on its variable.
     * @param propagator  element to desactivate
     */
    void desactivatePropagator(Propagator propagator);

    /**
     * Propagate one by one events registered
     *
     * @throws choco.kernel.solver.ContradictionException if an event propagation creates a contradiction
     */
    void propagateEvents() throws ContradictionException;

    /**
     * Decrements the number of init constraint awake events.
     */

    void decPendingInitConstAwakeEvent();


    /**
     * Increments the number of init constraint awake events.
     */

    void incPendingInitConstAwakeEvent();

    /**
     * Freeze the current events contained in <code>this</code>.
     * The behaviour of the engine is still the same, but those events won't be treated in next calls to {@link choco.kernel.solver.propagation.PropagationEngine#propagateEvents()}
     */
    void freeze();

    /**
     * Unfreeze the previously frozen events contained in <code>this</code>. See {@link choco.kernel.solver.propagation.PropagationEngine#freeze()}.
     */
    void unfreeze();

    //****************************************************************************************************************//
    //********************************** LISTENERS *******************************************************************//
    //****************************************************************************************************************//

	/**
	 * Adds a new listener to some events occuring in the propagation engine.
	 * @param listener a new listener
	 */
	void addPropagationEngineListener(PropagationEngineListener listener);

    /**
     * Removes a old listener from the propagation engine
     * @param listener removal listener
     */
    void removePropagationEngineListener(PropagationEngineListener listener);

    /**
     * Check wether <code>this</code> contains <code>listener</code> in its list of listeners
     * @param listener
     * @return
     */
    boolean containsPropagationListener(PropagationEngineListener listener);

}
