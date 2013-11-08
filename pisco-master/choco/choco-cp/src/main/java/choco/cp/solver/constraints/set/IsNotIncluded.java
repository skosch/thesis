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


/* 
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 27 avr. 2008
 * Since : Choco 2.0.0
 *
 */
public final class IsNotIncluded extends AbstractBinSetSConstraint {

	/**
	 * create a constraint to state sv1 isIncluded sv2
	 * sv1 isIncluded in sv2
	 *
	 * @param sv1
	 * @param sv2
	 */
	public IsNotIncluded(SetVar sv1, SetVar sv2) {
        super(sv1, sv2);
	}

    @Override
    public int getFilteredEventMask(int idx) {
        return SetVarEvent.ADDKER_MASK + SetVarEvent.REMENV_MASK + SetVarEvent.INSTSET_MASK;
    }

    public static boolean isKer1IncludedInKer2(SetVar x0, SetVar x1) {
		if (x0.getKernelDomainSize() <= x1.getKernelDomainSize()) {
			DisposableIntIterator it1 = x0.getDomain().getKernelIterator();
			while (it1.hasNext()) {
				if (!x1.isInDomainKernel(it1.next())) {
                    it1.dispose();
					return false;
				}
			}
            it1.dispose();
			return true;
		} else {
			return false;
		}
	}

	public static boolean isKer1IncludedInEnv2(SetVar x0, SetVar x1) {
		if (x0.getKernelDomainSize() <= x1.getEnveloppeDomainSize()) {
			DisposableIntIterator it1 = x0.getDomain().getKernelIterator();
			while (it1.hasNext()) {
				if (!x1.isInDomainEnveloppe(it1.next())) {
                    it1.dispose();
					return false;
				}
			}
            it1.dispose();
			return true;
		} else {
			return false;
		}
	}

	public boolean prune;

	/**
	 * test if all values in env(v0) are in ker(v1) and returns
	 * - +Infini if it is false and there are at least two such values
	 * - -Infini if it is true
	 * - the single value that is in env(v0) and not in ker(v1) if there is a single one
	 */
	public int findUniqueOutsider() throws ContradictionException {
		prune = false;
		DisposableIntIterator it1 = v0.getDomain().getEnveloppeIterator();
		int uniqueOutsider = Integer.MAX_VALUE;
		while (it1.hasNext()) {
			int val = it1.next();
			if (!v1.isInDomainKernel(val)) {
				if (!prune) {
					uniqueOutsider = val;
					prune = true;
				} else {
					prune = false;
                    it1.dispose();
					return Integer.MAX_VALUE;
				}
			}
		}
        it1.dispose();
		if (!prune) {
			this.fail();
		}
		return uniqueOutsider;
	}


	public void filter() throws ContradictionException {
		int uniqueOutsider = findUniqueOutsider();
		if (prune) {
			if (v0.isInDomainKernel(uniqueOutsider)) {
				v1.remFromEnveloppe(uniqueOutsider, this, true);
			}
			if (!v1.isInDomainEnveloppe(uniqueOutsider)) {
				v0.addToKernel(uniqueOutsider, this, true);
			}
		}
	}

	@Override
	public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
		if (varIdx == 1) {
			if (v0.isInDomainKernel(x)) {
				setPassive();
			}
		}
		filter();
	}

	@Override
	public void awakeOnKer(int varIdx, int x) throws ContradictionException {
		if (varIdx == 0) {
			if (!v1.isInDomainEnveloppe(x)) {
				setPassive();
			}
		}
		filter();
	}

	@Override
	public void awakeOnInst(int varIdx) throws ContradictionException {
		filter();
	}

	public void propagate() throws ContradictionException {
		filter();
	}

	public boolean isSatisfied() {
		DisposableIntIterator it2 = v0.getDomain().getKernelIterator();
		while (it2.hasNext()) {
			if (!v1.isInDomainKernel(it2.next())) {
                it2.dispose();
				return true;
			}
		}
        it2.dispose();
		return false;
	}

	public boolean isConsistent() {
		return isSatisfied();
	}

	@Override
	public String toString() {
		return v0 + " is Not Included in " + v1;
	}

	@Override
	public String pretty() {
		return v0.pretty() + " is Not Included in " + v1.pretty();
	}

    @Override
    public AbstractSConstraint<SetVar> opposite(Solver solver) {
        return new IsIncluded(v0, v1);
    }
}
