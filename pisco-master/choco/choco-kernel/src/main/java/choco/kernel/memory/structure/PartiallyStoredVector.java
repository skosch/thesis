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
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.MemoryException;
import choco.kernel.memory.structure.iterators.PSVIndexIterator;
import choco.kernel.memory.structure.iterators.PSVIterator;

import java.util.Arrays;

import static choco.kernel.common.Constant.*;

/**
 * A class implementing a vector with two kind of storage:
 * standard static storage in an array, and backtrackable storage.
 * By convention, objects with small indices (0 .. 999999) are statically managed
 * as if they were in a standard array.
 * And objects with large indices (1000000 ... ) are "stored" in a backtrackable
 * manner, as if they were in a StoredIntVector
 */
public final class PartiallyStoredVector<E> {
    /**
     * objects stored statically
     */
    private E[] staticObjects;
    /**
     * objects stored dynamically
     */
    private E[] storedObjects;

    /**
     * Number of static objects
     */
    private int nStaticObjects;

    /**
     * number of stored objects
     */
    private final IStateInt nStoredObjects;

    private PSVIndexIterator<E> _iterator1;

    private PSVIterator<E> _iterator2;

    /**
     * Constructor
     *
     * @param env environment where the data structure should be created
     */
    @SuppressWarnings({"unchecked"})
    public PartiallyStoredVector(final IEnvironment env) {
        staticObjects = (E[]) new Object[INITIAL_STATIC_CAPACITY];
        storedObjects = (E[]) new Object[INITIAL_STORED_CAPACITY];
        nStaticObjects = 0;
        nStoredObjects = env.makeInt(0);
    }

    /**
     * Clear datastructures for safe reuses
     */
    public void clear() {
        Arrays.fill(staticObjects, null);
        Arrays.fill(storedObjects, null);
        nStaticObjects = 0;
        nStoredObjects.set(0);
    }

    /**
     * Check wether an object is stored.
     * First, try in the array of static objects, then in the array of stored objects.
     * Return true if the PartiallyStoredVector contains the object.
     *
     * @param o the object to look for
     * @return true if it is contained
     */
    public boolean contains(final Object o) {
        for (int i = 0; i < nStaticObjects; i++) {
            if (staticObjects[i].equals(o)) {
                return true;
            }
        }
        for (int i = 0; i < nStoredObjects.get(); i++) {
            if (storedObjects[i].equals(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a object in the array of static object
     *
     * @param o the object to add
     * @return the indice of this object in the structure
     */
    public int staticAdd(final E o) {
        ensureStaticCapacity(nStaticObjects + 1);
        staticObjects[nStaticObjects++] = o;
        return nStaticObjects - 1;
    }

    /**
     * Insert an object in the array of static object.
     *
     * @param ind indice to insert in
     * @param o   object to insert
     * @return the indice of the last object in the array of static objects
     */
    public int staticInsert(final int ind, final E o) {
        ensureStaticCapacity(nStaticObjects++);
        System.arraycopy(staticObjects, ind, staticObjects, ind + 1, nStaticObjects - ind);
        staticObjects[ind] = o;
        return nStaticObjects - 1;
    }

    /**
     * Remove the object placed at indice idx
     *
     * @param idx
     */
    void staticRemove(int idx) {
        staticObjects[idx] = null;
        if (idx == nStaticObjects - 1) {
            while (staticObjects[nStaticObjects] == null && nStaticObjects > 0) {
                nStaticObjects--;
            }
            if (staticObjects[nStaticObjects] != null) {
                nStaticObjects++;
            }
        }
    }

    void storedRemove(int idx) {
        storedObjects[idx] = null;
        if (idx == nStoredObjects.get() - 1) {
            while (storedObjects[nStoredObjects.get()] == null && nStoredObjects.get() > 0) {
                nStoredObjects.add(-1);
            }
            if (storedObjects[nStoredObjects.get()] != null) {
                nStoredObjects.add(1);
            }
        }
    }


    /**
     * Remove an object
     *
     * @param o object to remove
     * @return indice of the object
     * @throws choco.kernel.memory.MemoryException
     *          when trying to remove unknown object or stored object
     */
    public int remove(final Object o) {
        for (int i = 0; i < nStaticObjects; i++) {
            final Object staticObject = staticObjects[i];
            if (staticObject == o) {
                staticRemove(i);
                return i;
            }
        }
        if (nStoredObjects.getEnvironment().getWorldIndex() == 0) {
            for (int i = 0; i < nStoredObjects.get(); i++) {
                final Object storedObject = storedObjects[i];
                if (storedObject == o) {
                    storedRemove(i);
                    return i;
                }
            }
        } else {
            throw new MemoryException("impossible to remove the object (a constraint ?) from the dynamic part of the collection (root node ?)");
        }


        throw new MemoryException("impossible to remove the object (a constraint ?) from the static part of the collection (cut manager ?)");
    }

    /**
     * Ensure that the lenght of the array of static objects is always sufficient.
     *
     * @param n the expected size
     */
    @SuppressWarnings({"unchecked"})
    void ensureStaticCapacity(final int n) {
        if (n >= staticObjects.length) {
            int newSize = staticObjects.length;
            while (n >= newSize) {
                newSize = (3 * newSize) / 2;
            }
            final Object[] newStaticObjects = new Object[newSize];
            System.arraycopy(staticObjects, 0, newStaticObjects, 0, staticObjects.length);
            this.staticObjects = (E[]) newStaticObjects;
        }
    }

    /**
     * Add an stored object
     *
     * @param o the object to add
     * @return indice of the object in the structure
     */
    public int add(final E o) {
        ensureStoredCapacity(nStoredObjects.get() + 1);
        storedObjects[nStoredObjects.get()] = o;
        nStoredObjects.add(1);
        return STORED_OFFSET + nStoredObjects.get() - 1;
    }

    /**
     * Insert an stored object
     *
     * @param ind indice where to add object
     * @param o   object to insert
     * @return the size of stored structure
     */
    public int insert(final int ind, final E o) {
        ensureStoredCapacity(nStoredObjects.get() + 1);
        System.arraycopy(storedObjects, ind, storedObjects, ind + 1, nStoredObjects.get() - ind);
//
//        for (int i = nStoredObjects.get() + 1; i > ind; i--) {
//            storedObjects[i] = storedObjects[i - 1];
//        }
        storedObjects[ind] = o;
        nStoredObjects.add(1);
        return STORED_OFFSET + nStoredObjects.get() - 1;
    }

    /**
     * Ensure that the stored structure is long enough to add a new element
     *
     * @param n the expected size
     */
    @SuppressWarnings({"unchecked"})
    void ensureStoredCapacity(final int n) {
        if (n >= storedObjects.length) {
            int newSize = storedObjects.length;
            while (n >= newSize) {
                newSize = (3 * newSize) / 2;
            }
            final Object[] newStoredObjects = new Object[newSize];
            System.arraycopy(storedObjects, 0, newStoredObjects, 0, storedObjects.length);
            this.storedObjects = (E[]) newStoredObjects;
        }
    }

    /**
     * Get the index th stored object
     *
     * @param index the indice of the required object
     * @return the 'index'th object
     */
    public E get(final int index) {
        if (index < STORED_OFFSET) {
            return staticObjects[index];
        } else {
            return storedObjects[index - STORED_OFFSET];
        }
    }

    /**
     * Check wether the structure is empty
     *
     * @return true if the structure is empty
     */
    public boolean isEmpty() {
        return ((nStaticObjects == 0) && (nStoredObjects.get() == 0));
    }

    /**
     * Return the number of static and stored objects contained in the structure
     *
     * @return int
     */
    public int size() {
        return (nStaticObjects + nStoredObjects.get());
    }

    public DisposableIntIterator getIndexIterator() {
        if (_iterator1 == null) {
            _iterator1 = new PSVIndexIterator<E>();
        }else if (!_iterator1.reusable()) {
            assert false;
            _iterator1 = new PSVIndexIterator<E>();
        }
        _iterator1.init(nStaticObjects, staticObjects, nStoredObjects);
        return _iterator1;

    }

    public DisposableIterator getIterator() {
        if (_iterator2 == null) {
            _iterator2 = new PSVIterator<E>();
        }else if (!_iterator2.reusable()) {
            assert false;
            _iterator2 = new PSVIterator<E>();
        }
        _iterator2.init(nStaticObjects, staticObjects, nStoredObjects, storedObjects);
        return _iterator2;

    }

    /**
     * Check wether the indice idx define a static object
     *
     * @param idx
     * @return
     */
    public static boolean isStaticIndex(final int idx) {
        return idx < STORED_OFFSET;
    }

    /**
     * Return the indexe of an object minus the stored offset
     *
     * @param idx
     * @return
     */
    public static int getSmallIndex(final int idx) {
        if (idx < STORED_OFFSET) {
            return idx;
        } else {
            return idx - STORED_OFFSET;
        }
    }

    public static int getGlobalIndex(final int idx, final boolean isStatic) {
        if (isStatic) {
            return idx;
        } else {
            return idx + STORED_OFFSET;
        }
    }

    /**
     * Return the indice of the last static object
     *
     * @return
     */
    public int getLastStaticIndex() {
        return nStaticObjects - 1;
    }

    /**
     * Return the indice of the first static object
     *
     * @return
     */
    public static int getFirstStaticIndex() {
        return 0;
    }


    /**
     * Return the indice of the last stored object
     *
     * @return
     */
    public int getLastStoredIndex() {
        return nStoredObjects.get() - 1;
    }
}
