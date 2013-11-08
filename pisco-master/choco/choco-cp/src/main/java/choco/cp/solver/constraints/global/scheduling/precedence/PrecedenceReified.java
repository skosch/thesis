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

package choco.cp.solver.constraints.global.scheduling.precedence;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;


/**
 * <ul>
 *<li> b = 1 <=> x1 + k1 <= x2
 *<li> b = 0 <=> x1 + k1 > x2
 *</ul>
 */
public final class PrecedenceReified extends AbstractPrecedenceSConstraint {

	/**
	 */
	public PrecedenceReified(IntDomainVar x1, int k1, IntDomainVar x2, IntDomainVar b) {
		super( new IntDomainVar[]{b, x1, x2});
		this.k1 = k1;
	}


	@Override
	public void propagateP1() throws ContradictionException {
		propagate(1, k1, 2);
	}

	@Override
	public void propagateP2() throws ContradictionException {
		vars[2].updateSup(vars[1].getSup() + k1 - 1, this, false);
		vars[1].updateInf(vars[2].getInf() - k1 + 1, this, false);
	}

	@Override
	public Boolean isP1Entailed() {
		return isEntailed(1, k1, 2);
	}

	@Override
	public Boolean isP2Entailed() {
		return null;
	}

	@Override
	public void filterOnP1P2TowardsB() throws ContradictionException {
		reuseBool = isP1Entailed();
		if (reuseBool == Boolean.TRUE) {
			vars[BIDX].instantiate(1, this, false);
		} else if(reuseBool == Boolean.FALSE){
			vars[BIDX].instantiate(0, this, false);
		}
	}

	@Override
	public boolean isSatisfied() {
		return vars[0].isInstantiatedTo(1) ? isSatisfied(1, k1, 2) : vars[1].getVal() + k1 > vars[2].getVal();
	}


	@Override
	public boolean isSatisfied(int[] tuple) {
		return tuple[0] == 1 ? tuple[1]  + k1 <= tuple[2] : tuple[1] + k1 > tuple[2];
	}

	@Override
	public String pretty() {
		return pretty("Precedence Reified", pretty(1, k1, 2),  vars[1]+" + "+ k1+ " > " + vars[2]);
	}

}
