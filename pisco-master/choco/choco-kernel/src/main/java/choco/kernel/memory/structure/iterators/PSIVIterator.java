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

package choco.kernel.memory.structure.iterators;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IStateInt;

import static choco.kernel.common.Constant.STORED_OFFSET;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 29 mars 2010br/>
 * Since : Choco 2.1.1<br/>
 */
public final class PSIVIterator extends DisposableIntIterator {

    private int nStaticInts;

    private int nStoredInts;

    private int idx;

    private boolean stats;

    private boolean storeds;

    /**
     * Freeze the iterator, cannot be reused.
     */
    public void init(final int theNStaticInts, final IStateInt theNStoredInts) {
        super.init();
        this.nStaticInts = theNStaticInts;
        this.nStoredInts = theNStoredInts.get();
        stats = (nStaticInts > 0);
        storeds = (nStoredInts > 0);
        idx = -1;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    @Override
    public boolean hasNext() {
        if (idx == -1) {
            return stats || storeds;
        } else {
            return ((stats && idx < nStaticInts - 1)
                    || (idx == nStaticInts - 1 && storeds)
                    || (storeds && STORED_OFFSET <= idx && idx < STORED_OFFSET + nStoredInts - 1));
        }
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws java.util.NoSuchElementException
     *          iteration has no more elements.
     */
    @Override
    public int next() {
        idx++;
        if (idx == nStaticInts) {
            idx = STORED_OFFSET;
        }
        return idx;
    }
}