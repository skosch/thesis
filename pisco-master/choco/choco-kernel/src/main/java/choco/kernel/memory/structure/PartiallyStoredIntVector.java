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
import choco.kernel.memory.structure.iterators.PSIVIterator;

import static choco.kernel.common.Constant.*;


/**
 * A class implementing a vector with two kind of storage:
 * standard static storage in an array, and backtrackable storage.
 * By convention, integers with small indices (0 .. 999999) are statically managed
 * as if they were in a standard array.
 * And integers with large indices (1000000 ... ) are "stored" in a backtrackable
 * manner, as if they were in a StoredIntVector
 */
public final class PartiallyStoredIntVector {

    private int[] staticInts;
    private int[] storedInts;

    private int nStaticInts;
    private final IStateInt nStoredInts;
    private PSIVIterator _iterator;

    public PartiallyStoredIntVector(final IEnvironment env) {
        staticInts = new int[INITIAL_STATIC_CAPACITY];
        storedInts = new int[INITIAL_STORED_CAPACITY];
        nStaticInts = 0;
        nStoredInts = env.makeInt(0);
    }

    public int staticAdd(final int o) {
        ensureStaticCapacity(nStaticInts + 1);
        staticInts[nStaticInts++] = o;
        return nStaticInts - 1;
    }

    public void ensureStaticCapacity(final int n) {
        if (n > staticInts.length) {
            int newSize = staticInts.length;
            while (n >= newSize) {
                newSize = (3 * newSize) / 2;
            }
            final int[] newStaticObjects = new int[newSize];
            System.arraycopy(staticInts, 0, newStaticObjects, 0, staticInts.length);
            this.staticInts = newStaticObjects;
        }
    }

    public int add(final int o) {
        ensureStoredCapacity(nStoredInts.get() + 1);
        storedInts[nStoredInts.get()] = o;
        nStoredInts.add(1);
        return STORED_OFFSET + nStoredInts.get() - 1;
    }

    public void remove(final int o) {
        staticInts[o] = staticInts[nStaticInts];
        staticInts[nStaticInts] = 0;
        nStaticInts--;
    }

    public void ensureStoredCapacity(final int n) {
        if (n > storedInts.length) {
            int newSize = storedInts.length;
            while (n >= newSize) {
                newSize = (newSize*3) / 2 + 1;
            }
            final int[] newStoredObjects = new int[newSize];
            System.arraycopy(storedInts, 0, newStoredObjects, 0, storedInts.length);
            this.storedInts = newStoredObjects;
        }
    }

    public int get(final int index) {
        if (index < STORED_OFFSET) {
            return staticInts[index];
        } else {
            return storedInts[index - STORED_OFFSET];
        }
    }

    public boolean isEmpty() {
        return ((nStaticInts == 0) && (nStoredInts.get() == 0));
    }

    public int size() {
        return (nStaticInts + nStoredInts.get());
    }

    public DisposableIntIterator getIndexIterator() {
        if (_iterator == null) {
            _iterator = new PSIVIterator();
        }else if (!_iterator.reusable()) {
            assert false;
            _iterator = new PSIVIterator();
        }
        _iterator.init(nStaticInts, nStoredInts);
        return _iterator;

    }

    public static boolean isStaticIndex(final int idx) {
        return idx < STORED_OFFSET;
    }

    public static int getSmallIndex(final int idx) {
        if (idx < STORED_OFFSET){
            return idx;
        }else{
            return idx - STORED_OFFSET;
        }
    }

    public static int getGlobalIndex(final int idx, final boolean isStatic) {
        if (isStatic){
            return idx;
        }else{
            return idx + STORED_OFFSET;
        }
    }

}
