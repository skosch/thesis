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

package choco.kernel.common.util.iterators;

import choco.kernel.common.util.disposable.PoolManager;

public final class ArrayIterator<E> extends DisposableIterator<E>  implements IStored{

    private static final ThreadLocal<PoolManager<ArrayIterator>> manager = new ThreadLocal<PoolManager<ArrayIterator>>();

    private E[] elements;

    private int size;

    private int cursor;

    private boolean isStored;

    private ArrayIterator() {
    }

    @SuppressWarnings({"unchecked"})
    public static <E> ArrayIterator<E> getIterator(final E[] elements, final int size) {
        PoolManager<ArrayIterator> tmanager = manager.get();
        if (tmanager == null) {
            tmanager = new PoolManager<ArrayIterator>();
            manager.set(tmanager);
        }
        ArrayIterator it = tmanager.getE();
        if (it == null) {
            it = new ArrayIterator();
        }
        it.init(elements, size);
        return it;
    }

    @SuppressWarnings({"unchecked"})
    public static <E> ArrayIterator<E> getIterator(final E[] elements) {
        PoolManager<ArrayIterator> tmanager = manager.get();
        if (tmanager == null) {
            tmanager = new PoolManager<ArrayIterator>();
            manager.set(tmanager);
        }
        ArrayIterator it = tmanager.getE();
        if (it == null) {
            it = new ArrayIterator();
        }
        it.init(elements, elements.length);
        return it;
    }

    /**
     * Freeze the iterator, cannot be reused.
     */
    private void init(final E[] someElements, final int aSize) {
        super.init();
        this.elements = someElements;
        this.size = aSize;
        cursor = 0;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the getIterator has more elements.
     */
    @Override
    public boolean hasNext() {
        return cursor < size;
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
        return elements[cursor++];
    }

    @Override
    public void dispose() {
        super.dispose();
        manager.get().returnE(this);
    }

    @Override
    public void push() {
        isStored = true;
    }

    @Override
    public void pop() {
        isStored = false;
    }

    @Override
    public boolean isStored() {
        return isStored;
    }
}
