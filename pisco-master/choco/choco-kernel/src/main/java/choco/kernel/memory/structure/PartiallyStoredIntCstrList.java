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
import choco.kernel.memory.structure.iterators.PSCLEIterator;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 21 juil. 2009
* Since : Choco 2.1.0
* Update : Choco 2.1.0
*/
public final class PartiallyStoredIntCstrList<C extends AbstractSConstraint> extends APartiallyStoredCstrList<C> {

    private final PartiallyStoredIntVector[] events;
    private final int[] eventTypes;
    private final int[] idxEventTypes;
    private PSCLEIterator<C> _iterator;

    public PartiallyStoredIntCstrList(IEnvironment env, int... eventTypes) {
        super(env);
        int size = eventTypes.length;
        events = new PartiallyStoredIntVector[size];
        this.eventTypes = eventTypes;
        this.idxEventTypes = new int[eventTypes[size - 1] + 1];
        for (int i = 0; i < size; i++) {
            events[i] = env.makePartiallyStoredIntVector();
            idxEventTypes[eventTypes[i]] = i;
        }
    }


    /**
     * Adds a new constraints on the stack of constraints
     * the addition can be dynamic (undone upon backtracking) or not.
     *
     * @param c               the constraint to add
     * @param varIdx          the variable index accrding to the added constraint
     * @param dynamicAddition states if the addition is definitic (cut) or
     *                        subject to backtracking (standard constraint)
     * @return the index affected to the constraint according to this variable
     */
    public int addConstraint(SConstraint c, int varIdx, boolean dynamicAddition) {
        int constraintIdx = super.addConstraint(c, varIdx, dynamicAddition);
        AbstractSConstraint ic = ((AbstractSConstraint) c);
        int mask = ic.getFilteredEventMask(varIdx);
        for (int evt : eventTypes) {
            if ((mask & evt) != 0) {
                addEvent(dynamicAddition, idxEventTypes[evt], constraintIdx);
            }
        }
        return constraintIdx;
    }

    /**
     * Add event to the correct partially stored int vector
     *
     * @param dynamicAddition static or dynamic constraint
     * @param indice          indice of teh event type
     * @param constraintIdx   index of the constraint
     */
    private void addEvent(boolean dynamicAddition, int indice, int constraintIdx) {
        if (dynamicAddition) {
            events[indice].add(constraintIdx);
        } else {
            events[indice].staticAdd(constraintIdx);
        }
    }

    /**
     * Removes (permanently) a constraint from the list of constraints
     * connected to the variable.
     *
     * @param c the constraint that should be removed from the list this variable
     *          maintains.
     */
    public int eraseConstraint(SConstraint c) {
        int idx = super.eraseConstraint(c);
        int mask = ((AbstractIntSConstraint) c).getFilteredEventMask(indices.get(idx));
        for (int evt : eventTypes) {
            if ((mask & evt) != 0) {
                events[idxEventTypes[evt]].remove(idx);
            }
        }
        return idx;
    }

    public PartiallyStoredIntVector[] getEventsVector() {
        return events;
    }

    @SuppressWarnings({"unchecked"})
    public DisposableIterator<Couple<C>> getActiveConstraint(int event, C cstrCause) {
//        return PSCLEIterator.getIterator(events[idxEventTypes[event]],cstrCause, this.elements, this.indices);
        if (_iterator == null) {
            _iterator = new PSCLEIterator<C>();
        } else if (!_iterator.reusable()) {
            assert false;
            _iterator = new PSCLEIterator<C>();
        }
        _iterator.init(cstrCause, events[idxEventTypes[event]], elements, indices);
        return _iterator;
    }
}
