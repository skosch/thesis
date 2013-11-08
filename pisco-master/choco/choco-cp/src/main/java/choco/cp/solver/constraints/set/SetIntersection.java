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
import choco.kernel.solver.constraints.set.AbstractTernSetSConstraint;
import choco.kernel.solver.variables.set.SetVar;

/**
 * A constraint stating that a set is the intersection of two others
 * There are seven propagation rules for the constraint sv3 = intersection(sv1, sv2)
 * Ker(sv1) contains Ker(sv3)
 * Ker(sv2) contains Ker(sv3)
 * Ker(sv3) contains (Ker(sv1) inter Ker(sv2))
 * Env(v3)  disjoint Complement(Env(v1))
 * Env(v3)  disjoint Complement(Env(v2))
 * Env(v2)  disjoint Ker(v1) inter Complement(Env(v3))
 * Env(v1)  disjoint Ker(v2) inter Complement(Env(v3))
 */
public final class SetIntersection extends AbstractTernSetSConstraint {

	/**
	 * @param sv3 the intersection set
	 */

	public SetIntersection(SetVar sv1, SetVar sv2, SetVar sv3) {
        super(sv1, sv2, sv3);
	}

    @Override
    public int getFilteredEventMask(int idx) {
        return SetVarEvent.ADDKER_MASK + SetVarEvent.REMENV_MASK + SetVarEvent.INSTSET_MASK;
    }

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void awakeOnKer(int varIdx, int x) throws ContradictionException {
		if (varIdx == 0) {
			if (v1.isInDomainKernel(x)) v2.addToKernel(x, this, false);
			if (!v2.isInDomainEnveloppe(x)) v1.remFromEnveloppe(x, this, false);
		} else if (varIdx == 1) {
			if (v0.isInDomainKernel(x)) v2.addToKernel(x, this, false);
			if (!v2.isInDomainEnveloppe(x)) v0.remFromEnveloppe(x, this, false);
		} else {
			if (!v0.isInDomainKernel(x)) v0.addToKernel(x, this, false);
			if (!v1.isInDomainKernel(x)) v1.addToKernel(x, this, false);
		}
	}

	public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
		if (varIdx == 0) {
			v2.remFromEnveloppe(x, this, false);
		} else if (varIdx == 1) {
			v2.remFromEnveloppe(x, this, false);
		} else {
			if (v0.isInDomainKernel(x)) v1.remFromEnveloppe(x, this, false);
			if (v1.isInDomainKernel(x)) v0.remFromEnveloppe(x, this, false);
		}
	}

	public void awakeOnInst(int varIdx) throws ContradictionException {
		int x;
		if (varIdx == 0) {
			DisposableIntIterator it = v0.getDomain().getKernelIterator();
            try{
                while (it.hasNext()) {
                    x = it.next();
                    if (v1.isInDomainKernel(x)) v2.addToKernel(x, this, false);
                    if (!v2.isInDomainEnveloppe(x)) v1.remFromEnveloppe(x, this, false);
                }
            }finally {
                it.dispose();
            }
			it = v2.getDomain().getEnveloppeIterator();
            try{
                while (it.hasNext()) {
                    x = it.next();
                    if (!v0.isInDomainKernel(x)) v2.remFromEnveloppe(x, this, false);
                }
            }finally {
                it.dispose();
            }
		} else if (varIdx == 1) {
			DisposableIntIterator it = v1.getDomain().getKernelIterator();
            try{
			while (it.hasNext()) {
				x = it.next();
				if (v0.isInDomainKernel(x)) v2.addToKernel(x, this, false);
				if (!v2.isInDomainEnveloppe(x)) v0.remFromEnveloppe(x, this, false);
			}
            }finally {
                it.dispose();
            }
			it = v2.getDomain().getEnveloppeIterator();
            try{
			while (it.hasNext()) {
				x = it.next();
				if (!v1.isInDomainKernel(x)) v2.remFromEnveloppe(x, this, false);
			}
            }finally {
                it.dispose();
            }
		} else {
			DisposableIntIterator it = v2.getDomain().getKernelIterator();
            try{
                while (it.hasNext()) {
                    x = it.next();
                    if (!v0.isInDomainKernel(x)) v0.addToKernel(x, this, false);
                    if (!v1.isInDomainKernel(x)) v1.addToKernel(x, this, false);
                }
            }finally {
                it.dispose();
            }
		}
	}

	public void propagate() throws ContradictionException {
		DisposableIntIterator it = v0.getDomain().getKernelIterator();
		try{
        while (it.hasNext()) {
			int val = it.next();
			if (v1.isInDomainKernel(val)) v2.addToKernel(val, this, false);
			if (!v2.isInDomainEnveloppe(val)) v1.remFromEnveloppe(val, this, false);
		}
        }finally {
            it.dispose();
        }
		it = v1.getDomain().getKernelIterator();
        try{
            while (it.hasNext()) {
                int val = it.next();
                if (v0.isInDomainKernel(val)) v2.addToKernel(val, this, false);
                if (!v2.isInDomainEnveloppe(val)) v0.remFromEnveloppe(val, this, false);
            }
        }finally {
            it.dispose();
        }
		it = v2.getDomain().getKernelIterator();
        try{
            while (it.hasNext()) {
                int val = it.next();
                if (!v0.isInDomainKernel(val)) v0.addToKernel(val, this, false);
                if (!v1.isInDomainKernel(val)) v1.addToKernel(val, this, false);
            }
        }finally {
            it.dispose();
        }
		it = v2.getDomain().getEnveloppeIterator();
        try{
            while (it.hasNext()) {
                int val = it.next();
                if (!v0.isInDomainEnveloppe(val) ||
                        !v1.isInDomainEnveloppe(val))
                    v2.remFromEnveloppe(val, this, false);

            }           
        }finally {
            it.dispose();
        }
	}

	public String toString() {
		return v0 + " intersect " + v1 + " = " + v2;
	}

	public String pretty() {
		return v0.pretty() + " intersect " + v1.pretty() + " = " + v2.pretty();
	}

	public boolean isSatisfied() {
		boolean nonout = true, allIn = true;
		DisposableIntIterator it = v2.getDomain().getKernelIterator();
		while (it.hasNext()) {
			int val = it.next();
			if (!(v0.isInDomainKernel(val)) || !(v1.isInDomainKernel(val))) {
				allIn = false;
				break;
			}
		}
        it.dispose();
		if (!allIn) return false;
		it = v1.getDomain().getKernelIterator();
		while (it.hasNext()) {
			int val = it.next();
			if (!v2.isInDomainKernel(val) && v0.isInDomainKernel(val)) {
				nonout = false;
				break;
			}
		}
        it.dispose();
		return nonout;
	}

	public boolean isConsistent() {
		// TODO
		return false;
	}
}
