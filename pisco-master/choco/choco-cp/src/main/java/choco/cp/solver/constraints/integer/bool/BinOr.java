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

package choco.cp.solver.constraints.integer.bool;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * maintaint v1 Or v2 where v1 and v2 are boolean variables
 * i.e variables of domain {0,1}
 */
public final class BinOr extends AbstractBinIntSConstraint {

	BinOr(IntDomainVar v1, IntDomainVar v2) {
		super(v1, v2);
	}


    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK;
    }    

    public void propagate() throws ContradictionException {
		if (v0.isInstantiatedTo(0)) v1.instantiate(1, this, false);
		if (v1.isInstantiatedTo(0)) v0.instantiate(1, this, false);
	}

	public void awakeOnInst(int idx) throws ContradictionException {
		int val;
		if (idx == 0) {
			val = v0.getVal();
			if (val == 0) v1.instantiate(1, this, false);
		} else {
			val = v1.getVal();
			if (val == 0) v0.instantiate(1, this, false);
		}
	}

  public void awakeOnInf(int varIdx) throws ContradictionException {
  }

  public void awakeOnSup(int varIdx) throws ContradictionException {
  }

  public void awakeOnRem(int varIdx, int val) throws ContradictionException {
  }

  public boolean isSatisfied(int[] tuple) {
		return tuple[0] == 1 || tuple[1] == 1;
	}

	public Boolean isEntailed() {
		if (v0.isInstantiatedTo(1) ||
				v1.isInstantiatedTo(1))
			return Boolean.TRUE;
		else if (v0.isInstantiatedTo(0) &&
				v1.isInstantiatedTo(0))
			return Boolean.FALSE;
		else return null;
	}

    /**
     * Get the opposite constraint
     *
     * @return the opposite constraint  @param solver
     */
    @Override
    public AbstractSConstraint<IntDomainVar> opposite(final Solver solver) {
        return BooleanFactory.nor(vars);
    }
}
