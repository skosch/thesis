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

package choco.cp.solver.constraints.set;

import choco.cp.solver.variables.set.SetVarEvent;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.set.AbstractUnSetSConstraint;
import choco.kernel.solver.variables.set.SetVar;

/**
 * Ensure that a value will not belong to a setVar
 */
public final class NotMemberX extends AbstractUnSetSConstraint {

	protected int cste;

	public NotMemberX(SetVar v, int val) {
		super(v);
		cste = val;
	}

    @Override
    public int getFilteredEventMask(int idx) {
        return SetVarEvent.ADDKER_MASK + SetVarEvent.REMENV_MASK + SetVarEvent.INSTSET_MASK;
    }

    public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void propagate() throws ContradictionException {
		v0.remFromEnveloppe(cste, this, false);
	}

	public void awakeOnInst(int varIdx) throws ContradictionException {
		if (v0.isInDomainKernel(cste))
			this.fail();
	}

	public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
		if (!v0.isInDomainEnveloppe(cste))
			setEntailed();
	}

	public void awakeOnKer(int varIdx, int x) throws ContradictionException {
		if (v0.isInDomainKernel(cste))
			this.fail();
	}

	public boolean isSatisfied() {
		return !v0.isInDomainEnveloppe(cste);
	}

	public boolean isConsistent() {
		return !v0.isInDomainEnveloppe(cste);
	}

	public String toString() {
		return cste + " is not in " + v0;
	}

	public String pretty() {
		return cste + " is not in " + v0.pretty();
	}


	/**
	 * Checks if the listeners must be checked or must fail.
	 */
	public Boolean isEntailed() {
		if (!v0.isInDomainEnveloppe(cste))
			return Boolean.TRUE;
		else if (v0.isInDomainKernel(cste))
			return Boolean.FALSE;
		else
			return null;
	}

    @Override
    public AbstractSConstraint<SetVar> opposite(Solver solver) {
        return new MemberX(v0, cste);
    }

}
