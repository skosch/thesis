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

package choco.cp.solver.variables.delta;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.IntArrayIterator;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.variables.delta.IDeltaDomain;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 11 déc. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*/
public final class BipartiteDeltaDomain implements IDeltaDomain {

    /**
     * A pointer to the first removed value to be propagated.
     * its position in the list
     */
    private int beginningOfDeltaDomain;

    /**
     * A pointer on the last removed value propagated.
     * its position in the list
     */
    private int endOfDeltaDomain;

    /**
     * The number of values currently in the domain.
     */
    private final IStateInt valuesInDomainNumber;

    /**
     * The values (not ordered) contained in the domain.
     */
    private final int[] values;

    public BipartiteDeltaDomain(final int size, final int[] theValues, final IStateInt theValuesInDomainNumber) {
        this.endOfDeltaDomain = size;
        this.beginningOfDeltaDomain = size;
        this.values = theValues;
        this.valuesInDomainNumber = theValuesInDomainNumber;
    }

    /**
     * The delta domain container is "frozen" (it can no longer accept new value removals)
     * so that this set of values can be iterated as such²
     */
    @Override
    public void freeze() {
        // freeze all data associated to bounds for the the event
        beginningOfDeltaDomain = valuesInDomainNumber.get() + 1;
    }

    /**
     * Update the delta domain
     *
     * @param value removed
     */
    @Override
    public void remove(final int value) {
        if (endOfDeltaDomain <= value) {
            endOfDeltaDomain = value + 1;
        }
    }

    /**
     * cleans the data structure implementing the delta domain
     */
    @Override
    public void clear() {
        beginningOfDeltaDomain = valuesInDomainNumber.get() + 1;
        endOfDeltaDomain = beginningOfDeltaDomain;
    }

    /**
     * Check if the delta domain is released or frozen.
     *
     * @return true if release
     */
    @Override
    public boolean isReleased() {
        return beginningOfDeltaDomain == endOfDeltaDomain;
    }

    /**
     * after an iteration over the delta domain, the delta domain is reopened again.
     *
     * @return true iff the delta domain is reopened empty (no updates have been made to the domain
     *         while it was frozen, false iff the delta domain is reopened with pending value removals (updates
     *         were made to the domain, while the delta domain was frozen).
     */
    @Override
    public boolean release() {
        // release all data associated to bounds for the the event
        endOfDeltaDomain = beginningOfDeltaDomain;
        beginningOfDeltaDomain = valuesInDomainNumber.get() + 1;
        return beginningOfDeltaDomain == endOfDeltaDomain;
    }

    /**
     * Iterator over delta domain
     *
     * @return delta iterator
     */
    @Override
    public DisposableIntIterator iterator() {
        return IntArrayIterator.getIterator(values, beginningOfDeltaDomain, endOfDeltaDomain);
    }

    @Override
    public IDeltaDomain copy() {
        return new BipartiteDeltaDomain(this.endOfDeltaDomain, this.values, null);
    }

    /**
     * pretty printing of the object. This String is not constant and may depend on the context.
     *
     * @return a readable string representation of the object
     */
    @Override
    public String pretty() {
        return beginningOfDeltaDomain +" -> "+ endOfDeltaDomain;
    }
}
