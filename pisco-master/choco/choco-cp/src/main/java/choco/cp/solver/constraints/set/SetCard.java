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

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.cp.solver.variables.set.SetVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.set.AbstractBinSetIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;


public final class SetCard extends AbstractBinSetIntSConstraint {

	// operator pour la contrainte de cardinalit� :
	// inf & !sup -> card(set) <= int
	// sup & !inf -> card(set) => int
	// inf && sup -> card(set) = int
	protected boolean inf = false;
	protected boolean sup = false;

	public SetCard(SetVar sv, IntDomainVar iv, boolean inf, boolean sup) {
		super(iv, sv);
		this.inf = inf;
		this.sup = sup;
	}

    @Override
    public int getFilteredEventMask(int idx) {
        if(idx == 0){
            return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK + IntVarEvent.REMVAL_MASK;
        }
        return SetVarEvent.ADDKER_MASK + SetVarEvent.REMENV_MASK + SetVarEvent.INSTSET_MASK;
    }

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void reactOnInfAndEnvEvents(int envSize) throws ContradictionException {
		if (v0.getInf() > envSize)
			this.fail();
		else if (v0.getInf() == envSize) {
			DisposableIntIterator it = v1.getDomain().getOpenDomainIterator();
            try{
                while (it.hasNext())
                    v1.addToKernel(it.next(), this, false);
            }finally {
                it.dispose();
            }
		}
	}

	public void reactOnSupAndKerEvents(int kerSize) throws ContradictionException {
		if (v0.getSup() < kerSize)
			this.fail();
		else if (v0.getSup() == kerSize) {
			DisposableIntIterator it = v1.getDomain().getOpenDomainIterator();
            try{
                while (it.hasNext())
                    v1.remFromEnveloppe(it.next(), this, false);
            }finally {
                it.dispose();
            }
		}
	}

	public void filter() throws ContradictionException {
		int envSize = v1.getEnveloppeDomainSize();
		int kerSize = v1.getKernelDomainSize();
		if (inf && sup) {
			if (v0.getSup() < kerSize || v0.getInf() > envSize)
				this.fail();
			else if (kerSize < envSize) {
				if (v0.getInf() == envSize) {
					DisposableIntIterator it = v1.getDomain().getOpenDomainIterator();
                    try{
                        while (it.hasNext())
                            v1.addToKernel(it.next(), this, false);
                    }finally{
                        it.dispose();
                    }
				} else if (v0.getSup() == kerSize) {
					DisposableIntIterator it = v1.getDomain().getOpenDomainIterator();
                    try{
                        while (it.hasNext())
                            v1.remFromEnveloppe(it.next(), this, false);
                    }finally {
                        it.dispose();
                    }
				}
			}
		} else if (inf) {
			if (v0.getSup() < kerSize)
				this.fail();
			else if (kerSize < envSize) {
				if (v0.getSup() == kerSize) {
					DisposableIntIterator it = v1.getDomain().getOpenDomainIterator();
                    try{
                        while (it.hasNext())
                            v1.remFromEnveloppe(it.next(), this, false);
                    }finally {
                        it.dispose();
                    }
				}
			}
		} else {
			if (v0.getInf() > envSize)
				this.fail();
			else if (kerSize < envSize) {
				if (v0.getInf() == envSize) {
					DisposableIntIterator it = v1.getDomain().getOpenDomainIterator();
                    try{
                        while (it.hasNext())
                            v1.addToKernel(it.next(), this, false);
                    }finally {
                        it.dispose();
                    }
				}
			}
		}
	}

	public void awakeOnInf(int idx) throws ContradictionException {
		if (inf) reactOnInfAndEnvEvents(v1.getEnveloppeDomainSize());
	}

	public void awakeOnSup(int idx) throws ContradictionException {
		if (sup) reactOnSupAndKerEvents(v1.getKernelDomainSize());
	}

	public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
		filter();
	}

	public void awakeOnkerAdditions(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
		if (inf) {
			int kerSize = v1.getKernelDomainSize();
			v0.updateInf(kerSize, this, false);
			reactOnSupAndKerEvents(kerSize);
		}
	}

	public void awakeOnEnvRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
		if (sup) {
			int envSize = v1.getEnveloppeDomainSize();
			v0.updateSup(envSize, this, false);
			reactOnInfAndEnvEvents(envSize);
		}
	}


	public void awakeOnInst(int varIdx) throws ContradictionException {
		if (varIdx == 1) {
			int kerSize = v1.getKernelDomainSize();
			if (inf && sup)
				v0.instantiate(kerSize, this, false);
			else if (inf)
				v0.updateInf(kerSize, this, false);
			else
				v0.updateSup(kerSize, this, false);
		} else {
			filter();
		}
	}

	public boolean isSatisfied() {
		if (inf && sup)
			return v1.getKernelDomainSize() == v0.getVal();
		else if (inf)
			return v1.getKernelDomainSize() <= v0.getVal();
		else
			return v1.getKernelDomainSize() >= v0.getVal();
	}

	public String toString() {
		if (inf && !sup)
			return " |" + v1 + "| <= " + v0;
		else if (!inf && sup)
			return " |" + v1 + "| >= " + v0;
		else return " |" + v1 + "| = " + v0; 	
	}

	public String pretty() {
		if (inf && !sup)
			return " |" + v1.pretty() + "| <= " + v0.pretty();
		else if (!inf && sup)
			return " |" + v1.pretty() + "| >= " + v0.pretty();
		else return " |" + v1.pretty() + "| = " + v0.pretty();
	}


	public void awake() throws ContradictionException {
		if (inf && sup) {
			v0.updateInf(v1.getKernelDomainSize(), this, false);
			v0.updateSup(v1.getEnveloppeDomainSize(), this, false);
		} else if (inf) {
			v0.updateInf(v1.getKernelDomainSize(), this, false);
		} else
			v0.updateSup(v1.getEnveloppeDomainSize(), this, false);
        propagate();
    }

	public void propagate() throws ContradictionException {
		filter();
	}

	public boolean isConsistent() {
		return (v1.isInstantiated() && v0.isInstantiated() && isSatisfied());
	}

	public Boolean isEntailed() {
		if (inf & sup) {
			if (v0.getInf() > v1.getEnveloppeDomainSize())
				return Boolean.FALSE;
			else if (v0.getSup() < v1.getKernelDomainSize())
				return Boolean.FALSE;
			else if (v0.isInstantiated() && v1.isInstantiated())
				return Boolean.TRUE;
			else
				return null;
		} else if (inf) {
			if (v0.getSup() < v1.getKernelDomainSize())
				return Boolean.FALSE;
			else if (v0.isInstantiated() && v1.isInstantiated())
				return Boolean.TRUE;
			else
				return null;
		} else {
			if (v0.getInf() > v1.getEnveloppeDomainSize())
				return Boolean.FALSE;
			else if (v0.isInstantiated() && v1.isInstantiated())
				return Boolean.TRUE;
			else
				return null;
		}

	}
}
