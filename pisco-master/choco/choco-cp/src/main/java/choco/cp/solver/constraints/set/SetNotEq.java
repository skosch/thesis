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
 * A constraint to state that two set vars can not be equal
 */
public final class SetNotEq extends AbstractBinSetSConstraint {

	/**
	 * create a constraint to state sv1 is not equal to sv2
	 *
	 * @param sv1
	 * @param sv2
	 */
	public SetNotEq(SetVar sv1, SetVar sv2) {
		super(sv1, sv2);
	}

	public boolean checkAreEqual() throws ContradictionException {
		if (v0.isInstantiated() && v1.isInstantiated()
				&& v0.getKernelDomainSize() == v1.getKernelDomainSize()) {
			DisposableIntIterator it1 = v0.getDomain().getKernelIterator();
			while (it1.hasNext()) {
				if (!v1.isInDomainKernel(it1.next())) {
                    it1.dispose();
					return false;
				}
			}
			it1.dispose();
			fail();
		}
		return false;
	}

    @Override
    public int getFilteredEventMask(int idx) {
        return SetVarEvent.ADDKER_MASK + SetVarEvent.REMENV_MASK + SetVarEvent.INSTSET_MASK;
    }


	public static boolean checkAreNotEqual(SetVar instVar, SetVar otherVar) {
		DisposableIntIterator it1 = instVar.getDomain().getKernelIterator();
		while (it1.hasNext()) {
			if (!otherVar.isInDomainEnveloppe(it1.next())) {
				it1.dispose();
				return true;
			}
		}
		it1.dispose();
		it1 = otherVar.getDomain().getKernelIterator();
		while (it1.hasNext()) {
			if (!instVar.isInDomainEnveloppe(it1.next())) {
				it1.dispose();
				return true;
			}
		}
		it1.dispose();
		return false;
	}


	public void filterForInst(SetVar instvar, SetVar otherVar, int idx) throws ContradictionException {
		int deltaSize = otherVar.getEnveloppeDomainSize() - otherVar.getKernelDomainSize();
		if (deltaSize == 0) {
			checkAreEqual();
		} else if (deltaSize == 1 && !checkAreNotEqual(instvar,otherVar)) {
			if (otherVar.getEnveloppeDomainSize() > instvar.getKernelDomainSize()) {
				//we need to add the element missing in otherVar, otherwise they will be equal
				DisposableIntIterator it1 = otherVar.getDomain().getEnveloppeIterator();
				while (it1.hasNext()) {
					int val = it1.next();
					if (!otherVar.isInDomainKernel(val)) {
						otherVar.addToKernel(val, this, false);
					}
				}
				it1.dispose();
			} else {
				//we need to remove the element missing in otherVar, otherwise they will be equal
				DisposableIntIterator it1 = otherVar.getDomain().getEnveloppeIterator();
				while (it1.hasNext()) {
					int val = it1.next();
					if (!otherVar.isInDomainKernel(val)) {
						otherVar.remFromEnveloppe(val, this, false);
					}
				}
				it1.dispose();
			}
		}
	}

	@Override
	public void awakeOnKer(int varIdx, int x) throws ContradictionException {
		if (varIdx == 0) {
			if (!v1.isInDomainEnveloppe(x)) {
				setPassive();
			} else if (v1.isInstantiated()) {
				filterForInst(v1, v0, 0);
			}
		} else {
			if (!v0.isInDomainEnveloppe(x)) {
				setPassive();
			} else if (v0.isInstantiated()) {
				filterForInst(v0, v1, 1);
			}
		}
	}

	@Override
	public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
		if (varIdx == 0) {
			if (v1.isInDomainKernel(x)) {
				setPassive();
			} else if (v1.isInstantiated()) {
				filterForInst(v1, v0, 0);
			}
		} else {
			if (v0.isInDomainKernel(x)) {
				setPassive();
			} else if (v0.isInstantiated()) {
				filterForInst(v0, v1, 1);
			}
		}
	}

	@Override
	public void awakeOnInst(int varIdx) throws ContradictionException {
		if (varIdx == 0) {
			filterForInst(v0, v1, 1);
		} else {
			filterForInst(v1, v0, 0);
		}
	}

	public void propagate() throws ContradictionException {
		if (v0.isInstantiated()) {
			filterForInst(v0, v1, 1);
		}
		if (v1.isInstantiated()) {
			filterForInst(v1, v0, 0);
		}
	}

	public boolean isSatisfied() {
		if (v0.isInstantiated() && v1.isInstantiated() ) {
			if(v0.getKernelDomainSize() == v1.getKernelDomainSize()) {
				final DisposableIntIterator it1 = v0.getDomain().getKernelIterator();
				while (it1.hasNext()) {
					if (! v1.isInDomainKernel(it1.next())) {
                        it1.dispose();
						return true;
					}
				}
				it1.dispose();
				return false;
			}else return true;
		} else {
			return false;
		}
	}

	public boolean isConsistent() {
		return isSatisfied();
	}

	@Override
	public String toString() {
		return v0 + " neq " + v1;
	}

	@Override
	public String pretty() {
		return v0.pretty() + " neq " + v1.pretty();
	}

    @Override
    public AbstractSConstraint<SetVar> opposite(Solver solver) {
        return new SetEq(v0, v1);
    }
}

