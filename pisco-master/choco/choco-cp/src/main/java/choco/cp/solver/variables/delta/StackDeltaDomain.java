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
import choco.kernel.solver.variables.delta.IDeltaDomain;
import gnu.trove.TIntArrayList;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 11 déc. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*/
public class StackDeltaDomain implements IDeltaDomain {

    /**
     * A chained list implementing two subsets of values:
     * - the removed values waiting to be propagated
     * - the removed values being propagated
     * (each element points to the index of the enxt element)
     * -1 for the last element
     */
    private final TIntArrayList list;

    boolean freeze;
    private int from;
    private int to;

    public StackDeltaDomain() {
        list = new TIntArrayList();
        from = -1;
        to = 0;
        freeze = false;
    }

    /**
     * The delta domain container is "frozen" (it can no longer accept new value removals)
     * so that this set of values can be iterated as such²
     */
    @Override
    public void freeze() {
        // freeze all data associated to bounds for the the event
        // if the delta domain is already being iterated, it cannot be frozen
        if (!freeze) {
//        }//throw new IllegalStateException();
//        else {
            // the set of values waiting to be propagated is now "frozen" as such,
            // so that those value removals can be iterated and propagated
            // the container (link list) for values waiting to be propagated is reinitialized to an empty set
            from = to;
            to = list.size();
            freeze = true;
        }
    }

    /**
     * Update the delta domain
     *
     * @param value removed
     */
    @Override
    public void remove(final int value) {
        list.add(value);
    }

    /**
     * cleans the data structure implementing the delta domain
     */
    @Override
    public void clear() {
        from = -1;
        to = 0;
        list.clear();
        freeze = false;
    }

    /**
     * Check if the delta domain is released or frozen.
     *
     * @return true if release
     */
    @Override
    public boolean isReleased() {
        return !freeze;
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
        try{
            return (to == list.size());
        }finally {
            from = -1;
            freeze = false;
        }
    }

    /**
     * Iterator over delta domain
     *
     * @return delta iterator
     */
    @Override
    public DisposableIntIterator iterator() {
        return IntArrayIterator.getIterator(list.toNativeArray(), from, to);
    }

    /**
     * pretty printing of the object. This String is not constant and may depend on the context.
     *
     * @return a readable string representation of the object
     */
    @Override
    public String pretty() {
        return from +" -> "+ to;
    }

    @Override
    public IDeltaDomain copy() {
        throw new UnsupportedOperationException();
    }

}
