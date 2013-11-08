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

import java.util.List;

/**
 * Modelize a sorting permutation of a set.
 * @author Arnaud Malapert</br>
 * @since 4 déc. 2008 <b>version</b> 2.0.1</br>
 * @version 2.0.1</br>
 */
public interface IPermutation {


	/**
	 * apply the permutation on the source array
	 * @param source the array to be permuted
	 * @param dest the permuted array
	 */
	public <T> void applyPermutation(T[] source,T[] dest);
	
	public <T> void applyPermutation(List<T> source,T[] dest);

	//public int[] getPermutation();
	/**
	 * apply the permutation to the integer array
	 * @return the permuted array
	 */
	public int[] applyPermutation(int[] source);

	/**
	 * retu
	 * @param idx the permutation index
	 * @return the index in the original order
	 */
	public int getOriginalIndex(int idx);

	/**
	 * return
	 * @param idx the index in the original order
	 * @return the index in the permutation
	 */
	public int getPermutationIndex(int idx);

	public boolean isIdentity();


}
