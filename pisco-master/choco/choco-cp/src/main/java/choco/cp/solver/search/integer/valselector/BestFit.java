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

package choco.cp.solver.search.integer.valselector;

import choco.cp.solver.constraints.global.pack.PackSConstraint;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * @author Arnaud Malapert</br>
 * @since 7 dec. 2008 version 2.0.1</br>
 * @version 2.0.1</br>
 */
public class BestFit implements ValSelector<IntDomainVar> {

	public final PackSConstraint pack;

	public BestFit(PackSConstraint cstr) {
		super();
		this.pack = cstr;
	}

	@Override
	public int getBestVal(IntDomainVar x) {
		final DisposableIntIterator iter=x.getDomain().getIterator();
		int bin= iter.next();
		int max=pack.getRemainingSpace(bin);
		while(iter.hasNext()) {
			final int  b =iter.next();
			// DONE 21 sept. 2011- getRemainingSpace is not valid (for instance when packing the last items) - created 7 juil. 2011 by Arnaud Malapert
			//We should compute capacity - getRequiredSpace 
			final int space=pack.getRemainingSpace(b);
			if(space<max) {
				max =space;
				bin = b;
			}
		}
        iter.dispose();
		return bin;
	}



}
