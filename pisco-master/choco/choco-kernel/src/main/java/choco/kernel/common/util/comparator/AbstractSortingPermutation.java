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

package choco.kernel.common.util.comparator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 2 juil. 2009
* Since : Choco 2.1.0
* Update : Choco 2.1.0
*/
public abstract class AbstractSortingPermutation implements IPermutation, Comparator<Integer> {

	public final static IPermutation IDENTITY = Identity.SINGLETON;

	protected boolean identity;

	protected final Integer[] orderingPermutation;

	protected final Integer[] reversePermutation;

	public AbstractSortingPermutation(int size) {
		super();
		orderingPermutation =new Integer[size];
		reversePermutation =new Integer[size];
		for (int i = 0; i < orderingPermutation.length; i++) {
			orderingPermutation[i] = Integer.valueOf(i);
		}
	}

	public final void sort(boolean reverse) {
		Arrays.sort(orderingPermutation,  reverse ? Collections.reverseOrder(this) : this);
		identity=true;
		for (int i = 0; i < orderingPermutation.length; i++) {
			reversePermutation[orderingPermutation[i]]=i;
			if(identity && i!=orderingPermutation[i]) {identity=false;}
		}
	}


	@Override
	public final <T> void applyPermutation(T[] source,T[] dest) {
		if(source.length != orderingPermutation.length || source.length != dest.length) {
			throw new ArrayIndexOutOfBoundsException("the two arguments should have the same length than the permutation array");
		}else {
			for (int i = 0; i < source.length; i++) {
				dest[i]=source[orderingPermutation[i]];
			}
		}

	}
	
	@Override
	public <T> void applyPermutation(List<T> source, T[] dest) {
		if(source.size() != orderingPermutation.length || source.size() != dest.length) {
			throw new ArrayIndexOutOfBoundsException("the two arguments should have the same length than the permutation array");
		}else {
			int k = 0;
			for (T s : source) {
				dest[k++]= s;
			}
		}
		
	}

	@Override
	public int[] applyPermutation(int[] source) {
		int[] dest=new int[orderingPermutation.length];
		for (int i = 0; i < orderingPermutation.length; i++) {
			dest[i]=source[orderingPermutation[i]];
		}
		return dest;
	}



	/**
	 * return the original index of the idx-th element of the permuted array
	 */
	@Override
	public final int getOriginalIndex(int idx) {
		return this.orderingPermutation[idx];
	}
	/**
	 * return the index in the permutation of the idx-th element
	 */
	@Override
	public final int getPermutationIndex(int idx) {
		return reversePermutation[idx];
	}



	@Override
	public final boolean isIdentity() {
		return identity;
	}




	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Ordering permutation: ");
		b.append(Arrays.toString(orderingPermutation));
		return new String(b);
	}


}
