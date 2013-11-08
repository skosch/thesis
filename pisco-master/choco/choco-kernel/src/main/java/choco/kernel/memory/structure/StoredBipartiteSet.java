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

import static choco.kernel.common.Constant.SET_INITIAL_CAPACITY;
import choco.kernel.common.util.iterators.ArrayIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.variables.Var;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;



/**
 * @author Arnaud Malapert</br> 
 * @since 10 févr. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 * @param <E>
 */
public class StoredBipartiteSet<E> extends AbstractList<E> {

	/**
	 * The list of values
	 */
	protected E[] elementData;

	/**
	 * The index of last element of the list
	 */
	protected IStateInt last;

    StoredBipartiteSet(IEnvironment env) {
        super();
        //noinspection unchecked
        elementData = (E[])new Var[SET_INITIAL_CAPACITY];
		this.last = env.makeInt(0);
    }

    @SuppressWarnings("unchecked")
    public StoredBipartiteSet(IEnvironment env, Collection<E>  coll) {
		super();
		this.elementData = (E[]) coll.toArray();
		this.last = env.makeInt(elementData.length);
	}

	public StoredBipartiteSet(IEnvironment env, E[] elementData) {
		super();
		this.elementData = Arrays.copyOf(elementData, elementData.length);
		this.last = env.makeInt(elementData.length);
	}

    /**
	 * Checks if the given index is in range.  If not, throws an appropriate
	 * runtime exception.  This method does *not* check if the index is
	 * negative: It is always used immediately prior to an array access,
	 * which throws an ArrayIndexOutOfBoundsException if index is negative.
     * @param index index to check
     */
	void RangeCheck(int index) {
		if (index >= size())
			throw new IndexOutOfBoundsException(
					"Index: "+index+", Size: "+size());
	}

	
	
	@Override
	public E get(int index) {
		RangeCheck(index);
		return elementData[index];
	}
	
	public E getQuick(int index) {
		return elementData[index];
	}

	@SuppressWarnings({"unchecked"})
    public DisposableIterator<E> quickIterator() {
		return ArrayIterator.getIterator(elementData, size());
		
	}

	@Override
	public int size() {
		return last.get();
	}

	/**
	 * removal performs a swap on a pair of elements. Do not remove while iterating if you want to preserve the current order.
	 * @see java.util.AbstractList#remove(int)
	 */
	@Override
	public E remove(int index) {
		RangeCheck(index);
		final int idx = size()-1;
		//should swap the element to remove with the last element
		final E tmp = elementData[index];
		elementData[index] = elementData[idx];
		elementData[idx] = tmp;
		last.set(idx);
		return tmp;
	}

	public void sort(Comparator<E> cmp) {
		Arrays.sort(elementData, 0, size(), cmp);
	}

}
