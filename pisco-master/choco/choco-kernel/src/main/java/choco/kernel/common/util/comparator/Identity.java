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
 * identity permutation.
 * @author Arnaud Malapert</br>
 * @since 4 déc. 2008 <b>version</b> 2.0.1</br>
 * @version 2.0.1</br>
 */
public class Identity implements IPermutation {

	public final static IPermutation SINGLETON = new Identity();

	private Identity() {
		super();
	}

	@Override
	public int[] applyPermutation(int[] source) {
		return source;
	}

	@Override
	public <T> void applyPermutation(T[] source, T[] dest) {
		if(source.length != dest.length) {
			throw new ArrayIndexOutOfBoundsException("the two arguments should have the same length.");
		}else {
			System.arraycopy(source, 0, dest, 0, source.length);
		}
	}
	
	

	@Override
	public <T> void applyPermutation(List<T> source, T[] dest) {
		source.toArray(dest);		
	}

	@Override
	public int getOriginalIndex(int idx) {
		return idx;
	}

	@Override
	public int getPermutationIndex(int idx) {
		return idx;
	}

	@Override
	public boolean isIdentity() {
		return true;
	}

	@Override
	public String toString() {
		return "IDENTITY";
	}





}
