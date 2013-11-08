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
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.set.AbstractLargeSetIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;



/**
 * 
 * A constraint stating that
 * value j belongs to the s[i] set variable
 * iff integer variable x[j] equals to i.
 * This constraint models the inverse s: I -> P(J)
 * of a function x: J -> I (I and J sets of integers)
 * adapted from InverseChanneling
 *
 * @author Sophie Demassey
 */

public final class InverseSetInt extends AbstractLargeSetIntSConstraint {

	boolean init; 

	public InverseSetInt(IntDomainVar[] x, SetVar[] s) {
		super(x, s);
		init = true;
	}

    @Override
    public int getFilteredEventMask(int idx) {
        if(isSetVarIndex(idx)){
            return SetVarEvent.ADDKER_MASK + SetVarEvent.REMENV_MASK + SetVarEvent.INSTSET_MASK;
        }
        return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
    }

    /**
	 * Filtering Rule 0 : i>s.length => x[j]!=i \forall j
	 * j>x.length => j \not\in s[i] \forall i 
	 */
	public void filterFromIndices() throws ContradictionException {
		int n = getNbSetVars() - 1;
		for (int j = 0; j < ivars.length; j++) {
			ivars[j].updateInf(0, this, false);
			ivars[j].updateSup(n, this, false);
		}
		n= getNbIntVars()-1;
		for (int i = 0; i < svars.length; i++) {
			while (svars[i].getEnveloppeInf()< 0) {
				svars[i].remFromEnveloppe( svars[i].getEnveloppeInf(), this, false);
			}
			while (svars[i].getEnveloppeSup()> n) {
				svars[i].remFromEnveloppe( svars[i].getEnveloppeSup(), this, false);
			}
		}
	}

	@Override
	public void awake() throws ContradictionException {
		filterFromIndices(); 
		super.awake();
	}

	/**
	 * Filtering Rule 1 : j \in s[i] => x[j]=i (then propagate Rule 3)
	 */
	@Override
	public void awakeOnKer (int i, int j) throws ContradictionException {
		assert( isSetVarIndex(i));
		ivars[j].instantiate( i, this, true);
	}

	/**
	 * Filtering Rule 2 : j \not\in s[i] => x[j]!=i
	 */
	@Override
	public void awakeOnEnv (int i, int j) throws ContradictionException {
		assert( isSetVarIndex(i));
		ivars[j].removeVal( i, this, false);
	}

	/**
	 * propagation on domain revision of set variable s[i] : rules 1 & 2
	 */
	private void filterSetVar (int i) throws ContradictionException {
		for (int j = 0; j < getNbIntVars(); j++) {
			if (svars[i].isInDomainKernel(j)) {
				ivars[j].instantiate(i, this, true);
			} else if (!svars[i].isInDomainEnveloppe(j)) {
				ivars[j].removeVal(i, this, false);
			}
		}
	}

	/**
	 * Filtering Rule 3 : x[j]=i => j \in s[i] and j \not\in s[i'] \forall i'!=i
	 * Rules 1 & 2 : s[i]=V => x[j]=i \iff j \in V
	 */
	@Override
	public void awakeOnInst (int x) throws ContradictionException {
		if( isSetVarIndex(x)) { filterSetVar(x);}
		else {
			final int iv = getIntVarIndex(x);
			final int s = ivars[iv].getVal();
			svars[s].addToKernel(iv, this, false);
			for (int i = 0; i < s; i++) {
				svars[i].remFromEnveloppe(iv, this, false);
			}	
			for (int i = s + 1; i < svars.length; i++) {
				svars[i].remFromEnveloppe(iv, this, false);
			}
		}
	}


	/**
	 * Filtering Rule 4 : x[j]!=i => j \not\in s[i]
	 */
	@Override
	public void awakeOnRem (int x, int v) throws ContradictionException {
		if (isSetVarIndex(x)) awakeOnEnv(x, v);
		else svars[v].remFromEnveloppe( getIntVarIndex(x), this, false);
	}

	/**
	 * propagation on a var: Rules 1 & 2 (integer) or 3 & 4 (set)
	 */
	public void awakeOnVar (int x) throws ContradictionException {
		if ( getVar(x).isInstantiated()) awakeOnInst(x);
		else if (isSetVarIndex(x)) filterSetVar(x);
		else {
			final int iv = getIntVarIndex(x);
			for (int i = 0; i < svars.length; i++) {
				if (!ivars[iv].canBeInstantiatedTo(i)) {
					svars[i].remFromEnveloppe(iv, this, false);
				}
			}
		} 
	}

	public void propagate () throws ContradictionException {
		for (int x = 0; x < getNbVars(); x++) {
			awakeOnVar(x);
		}        
	}

	/** @return true if the set vars are all consistent with the j-th integer (instantiated) */
	public boolean isSatisfied (int j) {
		int s = ivars[j].getVal();
		if (!svars[s].isInDomainKernel(j)) return false;
		for (int i = 0; i < svars.length; i++)
			if (svars[i].isInDomainEnveloppe(j) && i!=s) return false;
		return true;
	}

	public boolean isSatisfied () {
		for (int j = 0; j < getNbIntVars(); j++) {
			if (!isSatisfied(j)) return false;
		}
		return true;
	}

	public boolean isConsistent () {
		for (int j = 0; j < ivars.length; j++) {
			if (ivars[j].isInstantiated()) {
				if (!isSatisfied(j)) return false;
			} else {
				for (int i = 0; i < svars.length; i++) {
					if (!ivars[j].canBeInstantiatedTo(i) ) {
						if (svars[i].isInDomainEnveloppe(j)) return false;
					} else if (svars[i].isInDomainKernel(j) ||
							!svars[i].isInDomainEnveloppe(j)) return false;
				}
			}
		}
		return true;
	}
}
