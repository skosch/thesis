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

import java.util.NoSuchElementException;

import static choco.kernel.common.Constant.STORED_OFFSET;

public final class PSVIterator<E> extends DisposableIterator<E> {

    private int nStaticObjects;

    private int nStoredObjects;

    private E[] staticObjects;

    private E[] storedObjects;

    private int idx;

    public PSVIterator() {
    }

    /**
     * Freeze the iterator, cannot be reused.
     */
    public void init(final int theNStaticObjects, final E[] theStaticObjects,
                     final IStateInt theNStoredObjects, final E[] theStoredObjects) {
        super.init();
        idx = -1;
        this.nStaticObjects = theNStaticObjects;
        this.staticObjects = theStaticObjects;
        this.nStoredObjects = theNStoredObjects.get();
        this.storedObjects = theStoredObjects;
    }

    public boolean hasNext() {
        if (idx < STORED_OFFSET) {
            return idx + 1 < nStaticObjects || nStoredObjects > 0;
        } else {
            return idx + 1 < STORED_OFFSET + nStoredObjects;
        }
    }

    public E next() {
        if (idx < STORED_OFFSET) {
            if (idx + 1 < nStaticObjects) {
                idx++;
                while (staticObjects[idx] == null && idx < nStaticObjects) {
                    idx++;
                }
                return staticObjects[idx];
            } else if (nStoredObjects > 0) {
                idx = STORED_OFFSET;
                return storedObjects[0];
            } else {
                throw new NoSuchElementException();
            }
        } else if (idx + 1 < STORED_OFFSET + nStoredObjects) {
            return storedObjects[++idx - STORED_OFFSET];
        } else {
            throw new NoSuchElementException();
        }
    }
}