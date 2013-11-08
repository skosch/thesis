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

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.memory.structure.iterators.BipartiteListIterator;
import choco.kernel.memory.structure.iterators.BipartiteListRemIterator;
import choco.kernel.solver.SolverException;

/**
 * A stored list dedicated to two operations :
 * - iteration
 * - removal of an element during iteration
 * It only requires a StoredInt to denote the first element of the list
 * and proceeds by swapping element with the first one to remove them and incrementing
 * the index of the first element.
 * IT DOES NOT PRESERVE THE ORDER OF THE LIST
 */
public final class StoredIntBipartiteList implements IStateIntVector {

    /**
     * The list of values
     */
    private final int[] list;

    /**
     * The first element of the list
     */
    private final IStateInt last;

    private BipartiteListIterator _iterator;
    
    private BipartiteListRemIterator _remIterator;


    public StoredIntBipartiteList(final IEnvironment environment, final int[] values) {
        this.list = values;
        this.last = environment.makeInt(values.length - 1);
    }


    public int size() {
        return last.get() + 1;
    }

    public boolean isEmpty() {
        return last.get() == -1;
    }

    public void add(final int i) {
        throw new UnsupportedOperationException("adding element is not permitted in this structure (the list is only meant to decrease during search)");
    }

    public void remove(final int i) {
        throw new UnsupportedOperationException("removing element is not permitted in this structure (the list is only meant to decrease during search)");
    }

    public void removeLast() {
        last.add(-1);
    }

    public int get(final int index) {
        return list[index];
    }

    @Override
    public int quickGet(final int index) {
        return list[index];
    }

    @Override
    public boolean contain(final int val) {
        final int llast = last.get();
        for (int i = 0; i < llast; i++) {
            if (val == list[i]) {
                return true;
            }
        }
        return false;
    }

    public int set(final int index, final int val) {
        throw new SolverException("setting an element is not permitted on this structure");
    }

    @Override
    public int quickSet(final int index, final int val) {
        return set(index, val);
    }

    public DisposableIntIterator getIterator() {
        if (_iterator == null) {
            _iterator = new BipartiteListIterator();
        }else if (!_iterator.reusable()) {
            assert false;
            _iterator = new BipartiteListIterator();
        }
        _iterator.init(list, last);
        return _iterator;

    }
    

    public DisposableIntIterator getRemIterator() {
        if (_remIterator == null) {
            _remIterator = new BipartiteListRemIterator();
        }else if (!_remIterator.reusable()) {
            assert false;
            _remIterator = new BipartiteListRemIterator();
        }
        _remIterator.init(list, last);
        return _remIterator;

    }

    public String pretty() {
        final StringBuilder s = new StringBuilder("[");
        for (int i = 0; i <= last.get(); i++) {
            s.append(list[i]).append(i == last.get() ? "" : ",");
        }
        return s.append(']').toString();
    }
}
