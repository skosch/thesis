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
import choco.kernel.common.util.iterators.EmptyIntIterator;
import choco.kernel.common.util.iterators.OneValueIterator;
import choco.kernel.solver.variables.delta.IDeltaDomain;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 14 d�c. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*/
public final class BooleanDeltaDomain implements IDeltaDomain {

    private int valueToPropagate;

    protected OneValueIterator _iterator;

    public BooleanDeltaDomain() {
        valueToPropagate = Integer.MIN_VALUE;
    }

    /**
     * The delta domain container is "frozen" (it can no longer accept new value removals)
     * so that this set of values can be iterated as such�
     */
    @Override
    public void freeze() {
        //useless
    }

    /**
     * Update the delta domain
     *
     * @param value removed
     */
    @Override
    public void remove(final int value) {
        valueToPropagate = value;
    }

    /**
     * cleans the data structure implementing the delta domain
     */
    @Override
    public void clear() {
        valueToPropagate = Integer.MIN_VALUE;
    }

    /**
     * Check if the delta domain is released or frozen.
     *
     * @return true if release
     */
    @Override
    public boolean isReleased() {
        // useless
        return true;
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
        valueToPropagate = Integer.MIN_VALUE;
        return true;
    }

    /**
     * pretty printing of the object. This String is not constant and may depend on the context.
     *
     * @return a readable string representation of the object
     */
    @Override
    public String pretty() {
        return String.valueOf(valueToPropagate);
    }


    public DisposableIntIterator iterator() {
        if(valueToPropagate == Integer.MIN_VALUE){
            return EmptyIntIterator.getIterator();
        }
        return OneValueIterator.getIterator(valueToPropagate);
    }


    @Override
    public IDeltaDomain copy() {
        final BooleanDeltaDomain delta = new BooleanDeltaDomain();
        delta.valueToPropagate = valueToPropagate;
        return delta;

    }

}
