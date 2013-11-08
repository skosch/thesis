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

import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.structure.StoredBipartiteVarSet;
import choco.kernel.solver.variables.Var;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 26 mars 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public final class SBVSIterator1<E extends Var> extends DisposableIterator<E> {

    private int i = -1;
    private StoredBipartiteVarSet storedBipartiteVarSet;
    private E[] elements;
    private IStateInt last;
    private int nlast;

    public SBVSIterator1() {
    }

    /**
     * Freeze the iterator, cannot be reused.
     */
    public void init(final StoredBipartiteVarSet aStoredBipartiteVarSet, final E[] someElements, final IStateInt aLast) {
        super.init();
        this.storedBipartiteVarSet = aStoredBipartiteVarSet;
        this.elements = someElements;
        this.last = aLast;
        this.nlast = aLast.get();
        i = -1;
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
        i++;
        while (i < nlast && elements[i].isInstantiated()) {
            storedBipartiteVarSet.swap(i);
            nlast = last.get();
        }
        return i < nlast;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws java.util.NoSuchElementException
     *          iteration has no more elements.
     */
    @Override
    public E next() {
        return elements[i];
    }
}
