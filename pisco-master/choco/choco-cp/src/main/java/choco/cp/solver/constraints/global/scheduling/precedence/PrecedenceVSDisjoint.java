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
import choco.kernel.solver.variables.scheduling.TaskVar;
/**
 * The precedence disjoint constraint with setup times and variable duration.
 * b = 1 <=> e1 + k1 <= s2
 * b = 0 <=> e2 + k2 <= s1
 */ 
public final class PrecedenceVSDisjoint extends AbstractPrecedenceSConstraint {

	public PrecedenceVSDisjoint(IntDomainVar b, 
			IntDomainVar s1, IntDomainVar e1, int k1,
			IntDomainVar s2, IntDomainVar e2, int k2
	) {
		super(new IntDomainVar[]{b,s1,s2,e1,e2});
		this.k1 = k1;
		this.k2 = k2;
	}

	public PrecedenceVSDisjoint(IntDomainVar b, TaskVar t1, int k1, TaskVar t2, int k2) {
		this(b, t1.start(), t1.end(), k1, t2.start(), t2.end(), k2);
		setTasks(t1, t2);
	}


	@Override
	public Boolean isP1Entailed() {
		return isEntailed(3, k1, 2);
	}

	@Override
	public Boolean isP2Entailed() {
		return isEntailed(4, k2, 1);
	}

	@Override
	public void propagateP1() throws ContradictionException {
		propagate(3, k1, 2);
	}

	@Override
	public void propagateP2() throws ContradictionException {
		propagate(4, k2, 1);
	}

	@Override
	public boolean isSatisfied() {
		return vars[BIDX].isInstantiatedTo(1) ? isSatisfied(3, k1, 2) : isSatisfied(4, k2, 1);
	}

	@Override
	public boolean isSatisfied(int[] tuple) {
		return tuple[BIDX] == 1 ? tuple[3] + k1 <= tuple[2] : tuple[4] + k2 <= tuple[1];
	}

	@Override
	public String pretty() {
		return pretty( "Precedence Disjoint", pretty(3, k1, 2), pretty(4, k2, 1) );
	}

}
