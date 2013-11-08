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

import choco.IPretty;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.structure.iterators.SBVSIterator1;
import choco.kernel.memory.structure.iterators.SBVSIterator2;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.Var;

import java.util.Arrays;
import java.util.List;

import static choco.kernel.common.Constant.SET_INITIAL_CAPACITY;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 17 juil. 2009
* Since : Choco 2.1.0
* Update : Choco 2.1.0
*
*
* Provide a structure for Var-like objects.
* It ensures:
* - an iterator over every variables
* - an efficient iterator over not instanciated variables
* - an iterator over instanciated variables
*/
public final class StoredBipartiteVarSet<E extends Var> extends StoredBipartiteSet<E> implements IPretty {


    private E[] varsNotInstanciated;

    private int size;

    private SBVSIterator1 _iterator1;
    private SBVSIterator2 _iterator2;

    public StoredBipartiteVarSet(final IEnvironment env) {
        super(env);
        //noinspection unchecked
        varsNotInstanciated = (E[]) new Var[SET_INITIAL_CAPACITY];
        size = 0;
    }

    /**
     * Clear datastructures for safe reuses
     */
    @Override
    public void clear() {
        Arrays.fill(varsNotInstanciated, null);
        size = 0;
    }

    /**
     * Add a variable to the structure.
     *
     * @param e the new variable
     * @return the index of the variable in the variable
     */
    @Override
    public boolean add(final E e) {
        ensureCapacity(size + 1);
        elementData[size] = e;
        varsNotInstanciated[size++] = e;
        last.add(1);
        return true;
    }

    /**
     * Ensure the capasities of array
     *
     * @param expectedSize expected size
     */
    @SuppressWarnings({"unchecked"})
    public void ensureCapacity(final int expectedSize) {
        if (elementData.length < expectedSize) {
            int newSize = elementData.length;
            do {
                newSize *= 2;
            } while (newSize < expectedSize);

            E[] oldElements = elementData;
            elementData = (E[]) new Var[newSize];
            System.arraycopy(oldElements, 0, elementData, 0, oldElements.length);

            oldElements = varsNotInstanciated;
            varsNotInstanciated = (E[]) new Var[newSize];
            System.arraycopy(oldElements, 0, varsNotInstanciated, 0, oldElements.length);
        }
    }

    /**
     * removal performs a swap on a pair of elements. Do not remove while iterating if you want to preserve the current order.
     *
     * @param index index of the object to remove
     * @return the removed object
     */
    public E swap(final int index) {
        RangeCheck(index);
        final int idx = last.get() - 1;
        //should swap the element to remove with the last element
        final E tmp = varsNotInstanciated[index];
        varsNotInstanciated[index] = varsNotInstanciated[idx];
        varsNotInstanciated[idx] = tmp;
        last.set(idx);
        return tmp;
    }

    public List<E> toList() {
        @SuppressWarnings({"unchecked"}) final
        E[] t = (E[]) new Var[size];
        System.arraycopy(elementData, 0, t, 0, size);
        return Arrays.asList(t);
    }

    @Override
    public E[] toArray() {
        @SuppressWarnings({"unchecked"}) final
        E[] t = (E[]) new Var[size];
        System.arraycopy(elementData, 0, t, 0, size);
        return t;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */
    public boolean contains(final E o) {
        return indexOf(o) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o search object
     * @return index of o
     */
    public int indexOf(final E o) {
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (elementData[i] == null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String pretty() {
        return StringUtils.pretty(elementData, 0, size);
    }

    @Override
    public String toString() {
        return Arrays.toString(elementData);
    }

    /**
     * removal performs a swap on a pair of elements. Do not remove while iterating if you want to preserve the current order.
     *
     * @see java.util.AbstractList#remove(int)
     */
    @Override
    public E remove(final int index) {
        throw new SolverException("Not yet implemented");
    }

    /**
     * Iterator over non instanciated variables
     * BEWARE: initial order is not preserved
     *
     * @return iterator
     */
    @SuppressWarnings({"unchecked"})
    public final DisposableIterator<E> getNotInstanciatedVariableIterator() {
//        return SBVSIterator1.getIterator(this, varsNotInstanciated, last);
        if (_iterator1 == null || !_iterator1.reusable()) {
            _iterator1 = new SBVSIterator1();
        }
        _iterator1.init(this, varsNotInstanciated, last);
        return _iterator1;

    }

    /**
     * Iterator over instanciated variables
     * BEWARE: initial order is not preserved
     *
     * @return iterator
     */
    @SuppressWarnings({"unchecked"})
    public final DisposableIterator<E> getInstanciatedVariableIterator() {
//        return SBVSIterator2.getIterator(varsNotInstanciated, size);
        if (_iterator2 == null || !_iterator2.reusable()) {
            _iterator2 = new SBVSIterator2();
        }
        _iterator2.init(varsNotInstanciated, size);
        return _iterator2;

    }
}
