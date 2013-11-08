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

package choco.kernel.memory.structure;

import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.Propagator;
import choco.kernel.solver.propagation.event.ConstraintEvent;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 21 juil. 2009
* Since : Choco 2.1.0
* Update : Choco 2.1.0
*/
public abstract class APartiallyStoredCstrList<C extends SConstraint> {

    protected final PartiallyStoredVector<C> elements;

    protected final PartiallyStoredIntVector indices;

    protected final IStateInt priority;

    protected APartiallyStoredCstrList(IEnvironment env) {
        elements = env.makePartiallyStoredVector();
        indices = env.makePartiallyStoredIntVector();
        priority = env.makeInt(ConstraintEvent.UNARY);
    }

    /**
     * Retrieve the C element i
     * @param i index of the constraint
     * @return the ith constraint
     */
    public final C getConstraint(final int i) {
		return elements.get(i);
	}

    /**
	 * Returns the index of the constraint.
	 * @param constraintIndex the index of the constraint
	 * @return the index
	 */
	public final int getConstraintIndex(final int constraintIndex) {
		return indices.get(constraintIndex);
	}

    /**
	 * Returns the number of constraints
	 * @return the number of constraints
	 */
	public final int getNbConstraints() {
		return elements.size();
	}

    /**
	 * Access the data structure storing constraints
	 * @return the backtrackable structure containing the constraints
	 */
	public final PartiallyStoredVector<C> getConstraintVector() {
		return elements;
	}

    /**
	 * Access the data structure storing indices associated to constraints .
	 * @return the indices associated to this variable in each constraint
	 */
	public final PartiallyStoredIntVector getIndexVector() {
		return indices;
	}

    /**
	 * Removes (permanently) a constraint from the list of constraints
	 * connected to the variable.
	 * @param c the constraint that should be removed from the list this variable
	 * maintains.
     * @return index of the deleted constraint
	 */
	public int eraseConstraint(final SConstraint c) {
		int idx = elements.remove(c);
		indices.remove(idx);
        return idx;
	}

    /**
	 * Adds a new constraints on the stack of constraints
	 * the addition can be dynamic (undone upon backtracking) or not.
	 * @param c the constraint to add
	 * @param varIdx the variable index accrding to the added constraint
	 * @param dynamicAddition states if the addition is definitic (cut) or
	 * subject to backtracking (standard constraint)
	 * @return the index affected to the constraint according to this variable
	 */
	@SuppressWarnings({"unchecked"})
    public int addConstraint(final SConstraint c, final int varIdx, final boolean dynamicAddition) {
		int constraintIdx;
		if (dynamicAddition) {
			constraintIdx = elements.add((C)c);
			indices.add(varIdx);
		} else {
			constraintIdx = elements.staticAdd((C)c);
			indices.staticAdd(varIdx);
		}
        computePriority(c);
		return constraintIdx;
	}

    /**
     * Compute the priotity of the variable
     *
     * @param c the new constraint
     */
    protected void computePriority(SConstraint c) {
        priority.set(Math.max(priority.get(), ((Propagator) c).getPriority()));
    }

    /**
	 * This methods should be used if one want to access the different constraints stored.
	 *
	 * Indeed, since indices are not always
	 * consecutive, it is the only simple way to achieve this.
	 *
	 * Warning ! this iterator should not be used to remove elements.
	 * The <code>remove</code> method throws an
	 * <code>UnsupportedOperationException</code>.
	 *
	 * @return an iterator over all constraints involving this variable
	 */
	public final DisposableIterator<SConstraint> getConstraintsIterator() {
		return elements.getIterator();
	}

    /**
     * Return the minimum priority of the constraints in <code>this</code>
     * @return priority
     */
    public final int getPriority() {
		return priority.get();
	}
}
