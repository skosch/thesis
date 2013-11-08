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
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.set.AbstractBinSetSConstraint;
import choco.kernel.solver.variables.set.SetVar;


/**
 * Specify a constraint to state x included y
 */
public final class IsIncluded extends AbstractBinSetSConstraint {

	/**
	 * create a constraint to state sv1 isIncluded sv2
	 * sv1 isIncluded in sv2
	 * @param sv1
	 * @param sv2
	 */
	public IsIncluded(SetVar sv1, SetVar sv2) {
        super(sv1, sv2);
	}

    @Override
    public int getFilteredEventMask(int idx) {
        if(idx == 0){
            return SetVarEvent.ADDKER_MASK + SetVarEvent.INSTSET_MASK;
        }
        return SetVarEvent.REMENV_MASK + SetVarEvent.INSTSET_MASK;
    }

    public void filter(int idx) throws ContradictionException {
		if (idx == 0) {
			DisposableIntIterator it1 = v0.getDomain().getKernelIterator();
			try{
                while (it1.hasNext()) {
                    v1.addToKernel(it1.next(), this, false);
                }
            }finally {
                it1.dispose();
            }
		} else if (idx == 1) {
			DisposableIntIterator it2 = v0.getDomain().getEnveloppeIterator();
			try{
                while (it2.hasNext()) {
                    int val = it2.next();
                    if (!v1.isInDomainEnveloppe(val)) {
                        v0.remFromEnveloppe(val, this, false);
                    }
                }
            }finally {
                it2.dispose();
            }
		}
	}

	@Override
	public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
		if (varIdx == 1) {
			v0.remFromEnveloppe(x, this, false);
		}
	}

	@Override
	public void awakeOnKer(int varIdx, int x) throws ContradictionException {
		if (varIdx == 0) {
			v1.addToKernel(x, this, false);
		}
	}

	@Override
	public void awakeOnInst(int varIdx) throws ContradictionException {
		filter(varIdx);
	}

	public void propagate() throws ContradictionException {
		filter(0);
		filter(1);
	}

	public boolean isSatisfied() {
		DisposableIntIterator it2 = v0.getDomain().getKernelIterator();
        try{
            while (it2.hasNext()) {
                if (!v1.isInDomainKernel(it2.next())) {
                    return false;
                }
            }
        }finally {
            it2.dispose();
        }
		return true;
	}

	public boolean isConsistent() {
		return isSatisfied();
	}

	@Override
	public String toString() {
		return v0 + " disjoint " + v1;
	}

	@Override
	public String pretty() {
		return v0.pretty() + " disjoint " + v1.pretty();
	}

    @Override
    public AbstractSConstraint<SetVar> opposite(Solver solver) {
        return new IsNotIncluded(v0, v1);
    }
}

