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

package choco.cp.solver.variables.integer;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.variables.delta.IDeltaDomain;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * History:
 * 2007-12-07 : FR_1873619 CPRU: DomOverDeg+DomOverWDeg
 */
public abstract class AbstractIntDomain implements IntDomain {

	final PropagationEngine propagationEngine;


	/**
	 * The involved variable.
	 */
	final IntDomainVar variable;

	/**
	 * for the delta domain: current value of the inf (domain lower bound) when the bound started beeing propagated
	 * (just to check that it does not change during the propagation phase)
	 */
	protected int currentInfPropagated;

	/**
	 * for the delta domain: current value of the sup (domain upper bound) when the bound started beeing propagated
	 * (just to check that it does not change during the propagation phase)
	 */
	protected int currentSupPropagated;


	IDeltaDomain deltaDom;

	protected AbstractIntDomain(final IntDomainVar aVariable,final PropagationEngine propagationEngine) {
		this.propagationEngine = propagationEngine;
		this.variable = aVariable;

	}
	
	@Override
	public boolean isBoolean() {
		return getInf() >=0 && getSup() <= 1;
	}


	@Override
	public int fastNextValue(int x) {
		return this.getNextValue(x);
	}

	@Override
	public int fastPrevValue(int x) {
		return this.getPrevValue(x);
	}

	/**
	 * Internal var: update on the variable upper bound caused by its i-th
	 * constraint.
	 * Returns a boolean indicating whether the call indeed added new information.
	 *
	 * @param x   The new upper bound
	 * @param cause constraint causing the modification
	 * @param forceAwake
	 * @return a boolean indicating whether the call indeed added new information.
	 * @throws ContradictionException contradiction exception
	 */
	public boolean updateSup(final int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
		if (_updateSup(x, cause)) {
			final int val = getInf();
			if ( getSup() == val) {
				restrict(val);
				propagationEngine.postInstInt(variable, cause, true);
			} else {
				propagationEngine.postUpdateSup(variable, cause, forceAwake);
			}
			return true;
		} else
			return false;
	}

	/**
	 * Internal var: update on the variable lower bound caused by its i-th
	 * constraint.
	 * Returns a boolean indicating whether the call indeed added new information
	 *
	 * @param x   The new lower bound.
	 * @param cause constraint causing the modification
	 * @param forceAwake
	 * @return a boolean indicating whether the call indeed added new information
	 * @throws ContradictionException contradiction exception
	 */

	public boolean updateInf(final int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
		if (_updateInf(x, cause)) {
			final int val = getSup();
			if (getInf() == val) {
				restrict(val);
				propagationEngine.postInstInt(variable, cause, true);
			} else {
				propagationEngine.postUpdateInf(variable, cause, forceAwake);
			}
			return true;
		} else
			return false;
	}

	/**
	 * Internal var: update (value removal) on the domain of a variable caused by
	 * its i-th constraint.
	 * <i>Note:</i> Whenever the hole results in a stronger var (such as a bound update or
	 * an instantiation, then we forget about the index of the var generating constraint.
	 * Indeed the propagated var is stronger than the initial one that
	 * was generated; thus the generating constraint should be informed
	 * about such a new var.
	 * Returns a boolean indicating whether the call indeed added new information.
	 *
	 * @param x   The removed value
	 * @param cause constraint causing the modification
	 * @param forceAwake
	 * @return a boolean indicating whether the call indeed added new information.
	 * @throws ContradictionException contradiction exception
	 */

	public boolean removeVal(final int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
		if (_removeVal(x, cause)) {
			// we must forget the cause when the event is promoted !
			if (getInf() == getSup())
				propagationEngine.postInstInt(variable, cause, true);
			else if (x < getInf())
				propagationEngine.postUpdateInf(variable, cause, true);
			else if (x > getSup())
				propagationEngine.postUpdateSup(variable, cause, true);
			else
				propagationEngine.postRemoveVal(variable, x, cause, forceAwake);
			return true;
		} else
			return false;
	}


	/**
	 * Internal var: remove an interval (a sequence of consecutive values) from
	 * the domain of a variable caused by its i-th constraint.
	 * Returns a boolean indicating whether the call indeed added new information.
	 *
	 * @param a   the first removed value
	 * @param b   the last removed value
	 * @param cause constraint causing the modification
	 * @param forceAwake
	 * @return a boolean indicating whether the call indeed added new information.
	 * @throws ContradictionException contradiction exception
	 */

	public boolean removeInterval(final int a, final int b, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
		if (a <= getInf())
			return updateInf(b + 1, cause, forceAwake);
		else if (getSup() <= b)
			return updateSup(a - 1, cause, forceAwake);
		else if (variable.hasEnumeratedDomain()) {     // TODO: really ugly .........
			boolean anyChange = false;
			for (int v = getNextValue(a - 1); v <= b; v = getNextValue(v)) {
				//for (int v = a; v <= b; v++) {
				anyChange |= removeVal(v, cause, forceAwake);
			}
			return anyChange;
		} else
			return false;
	}

	/**
	 * Internal var: instantiation of the variable caused by its i-th constraint
	 * Returns a boolean indicating whether the call indeed added new information.
	 *
	 * @param x   the new upper bound
	 * @param cause constraint causing the modification
	 * @param forceAwake
	 * @return a boolean indicating whether the call indeed added new information.
	 * @throws ContradictionException contradiction exception
	 */

	public boolean instantiate(final int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
		if (_instantiate(x, cause)) {
			propagationEngine.postInstInt(variable, cause, forceAwake);
			return true;
		} else
			return false;
	}

	// ============================================
	// Private methods for maintaining the
	// domain.
	// ============================================

	/**
	 * Instantiating a variable to an search value. Returns true if this was
	 * a real modification or not
	 *
	 * @param x the new instantiate value
	 * @param cause constraint causing the modification
	 * @return wether it is a real modification or not
	 * @throws ContradictionException contradiction exception
	 */

	boolean _instantiate(final int x, final SConstraint cause) throws ContradictionException {
		if (variable.isInstantiated()) {
			if (variable.getVal() != x) {
				propagationEngine.raiseContradiction(cause);
				return true; // Just for compilation !
			} else return false;
		} else {
			if (x < getInf() || x > getSup() || !contains(x)) { // GRT : we need to check bounds
				// since contains suppose trivial bounds
				// are containing tested value !!
				propagationEngine.raiseContradiction(cause);
				return true; // Just for compilation !
			} else {
				restrict(x);
			}
			return true;
		}
	}


	/**
	 * Improving the lower bound.
	 *
	 * @param x the new lower bound
	 * @param cause constraint causing the modification
	 * @return a boolean indicating wether the update has been done
	 * @throws ContradictionException contradiction exception
	 */

	// note: one could have thrown an OutOfDomainException in case (x > IStateInt.MAXINT)
	boolean _updateInf(final int x, final SConstraint cause) throws ContradictionException {
		if (x > getInf()) {
			if (x > getSup()) {
				propagationEngine.raiseContradiction(cause);
				return true; // Just for compilation !
			} else {
				updateInf(x);
				return true;
			}
		} else {
			return false;
		}
	}


	/**
	 * Improving the upper bound.
	 *
	 * @param x the new upper bound
	 * @param cause constraint causing the modification
	 * @return wether the update has been done
	 * @throws ContradictionException contradiction exception
	 */
	boolean _updateSup(final int x, final SConstraint cause) throws ContradictionException {
		if (x < getSup()) {
			if (x < getInf()) {
				propagationEngine.raiseContradiction(cause);
			} else {
				updateSup(x);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Removing a value from the domain of a variable. Returns true if this
	 * was a real modification on the domain.
	 *
	 * @param x the value to remove
	 * @param cause constraint causing the modification
	 * @return wether the removal has been done
	 * @throws ContradictionException contradiction excpetion
	 */
	boolean _removeVal(final int x, final SConstraint cause) throws ContradictionException {
		final int infv = getInf();
		final int supv = getSup();
		if (infv <= x && x <= supv) {
			if (x == infv) {
				_updateInf(x + 1, cause);
				if (getInf() == supv) {
					restrict(supv);
					//_instantiate(supv, idx);
				}
				return true;
			} else if (x == supv) {
				_updateSup(x - 1, cause);
				if (getSup() == infv) {
					restrict(infv);
					//_instantiate(infv, idx);
				}
				return true;
			} else {
				return remove(x);
			}
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return pretty();
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////// DELTA DOMAIN /////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public final DisposableIntIterator getDeltaIterator() {
		return deltaDom.iterator();
	}

	@Override
	public void freezeDeltaDomain() {
		deltaDom.freeze();
	}

	/**
	 * release the delta domain
	 *
	 * @return wether it was a new update
	 */
	@Override
	public boolean releaseDeltaDomain() {
		return deltaDom.release();
	}

	@Override
	public final void clearDeltaDomain() {
		deltaDom.clear();
	}

	/**
	 * checks whether the delta domain has indeed been released (ie: chechks that no domain updates are pending)
	 */
	@Override
	public boolean getReleasedDeltaDomain() {
		return deltaDom.isReleased();
	}

	@Override
	public final IDeltaDomain copyDelta() {
		return deltaDom.copy();
	}
}
