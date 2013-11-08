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


package choco.cp.solver.constraints.integer;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Implements a constraint X == Y + C, with X and Y two variables and C a constant.
 */
public final class EqualXYC extends AbstractBinIntSConstraint {

	/**
	 * The search constant of the constraint
	 */
	protected final int cste;
	
	private DisposableIntIterator reuseIter;

	/**
	 * Constructs the constraint with the specified variables and constant.
	 *
	 * @param x0 first IntDomainVar
	 * @param x1 second IntDomainVar
	 * @param c  The search constant used in the disequality.
	 */

	public EqualXYC(IntDomainVar x0, IntDomainVar x1, int c) {
		super(x0, x1);
		this.cste = c;
	}

	@Override
	public int getFilteredEventMask(int idx) {
		if(idx == 0){
			if(v0.hasEnumeratedDomain()){
				return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
			}else{
				return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
			}
		}else{
			if(v1.hasEnumeratedDomain()){
				return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
			}else{
				return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
			}
		}
	}

	private final void updateInfV0() throws ContradictionException {
		v0.updateInf(v1.getInf() + cste, this, false);
	}

	private final void updateInfV1() throws ContradictionException {
		v1.updateInf(v0.getInf() - cste, this, false);
	}

	private final void updateSupV0() throws ContradictionException {
		v0.updateSup(v1.getSup() + cste, this, false);
	}

	private final void updateSupV1() throws ContradictionException {
		v1.updateSup(v0.getSup() - cste, this, false);
	}


	/**
	 * The one and only propagation method, using foward checking
	 */
	public void propagate() throws ContradictionException {
		updateInfV0();
		updateSupV0();
		updateInfV1();
		updateSupV1();
		// ensure that, in case of enumerated domains,  holes are also propagated
		int val;
		if (v1.hasEnumeratedDomain() && v0.hasEnumeratedDomain()) {
			reuseIter = v0.getDomain().getIterator();
			try {
				while (reuseIter.hasNext()) {
					val = reuseIter.next();
					if (!(v1.canBeInstantiatedTo(val - cste))) {
						v0.removeVal(val, this, false);
					}
				}
			} finally {
				reuseIter.dispose();	
			}
			reuseIter = v1.getDomain().getIterator();
			try{
				while (reuseIter.hasNext()) {
					val = reuseIter.next();
					if (!(v0.canBeInstantiatedTo(val + cste))) {
						v1.removeVal(val, this, false);
					}
				}
			}finally{
				reuseIter.dispose();
			}
		}
	}


	@Override
	public final void awakeOnInf(int idx) throws ContradictionException {
		if (idx == 0) updateInfV1();
		else updateInfV0();
	}

	@Override
	public final void awakeOnSup(int idx) throws ContradictionException {
		if (idx == 0) updateSupV1();
		else updateSupV0();
	}

	@Override
	public final void awakeOnInst(int idx) throws ContradictionException {
		if (idx == 0) v1.instantiate(v0.getVal() - cste, this, false);
		else v0.instantiate(v1.getVal() + cste, this, false);
	}


	@Override
	public void awakeOnRem(int idx, int x) throws ContradictionException {
		if (idx == 0) v1.removeVal(x - cste, this, false);
		else {
			assert(idx == 1);
			v0.removeVal(x + cste, this, false);
		}
	}

	/**
	 * Checks if the listeners must be checked or must fail.
	 */

	@Override
	public final Boolean isEntailed() {
		if ((v0.getSup() < v1.getInf() + cste) ||
				(v0.getInf() > v1.getSup() + cste))
			return Boolean.FALSE;
		else if (v0.isInstantiated() &&
				v1.isInstantiated() &&
				(v0.getVal() == v1.getVal() + cste))
			return Boolean.TRUE;
		else
			return null;
	}

	/**
	 * Checks if the constraint is satisfied when the variables are instantiated.
	 */

	@Override
	public final boolean isSatisfied(int[] tuple) {
		return (tuple[0] == tuple[1] + this.cste);
	}

	/**
	 * tests if the constraint is consistent with respect to the current state of domains
	 *
	 * @return true iff the constraint is bound consistent (weaker than arc consistent)
	 */
	@Override
	public final boolean isConsistent() {
		return ((v0.getInf() == v1.getInf() + cste) && (v0.getSup() == v1.getSup() + cste));
	}

	@Override
	public final AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
		return (AbstractSConstraint) solver.neq(v0, solver.plus(v1, cste));
	}


	@Override
	public final String pretty() {
		StringBuffer sb = new StringBuffer();
		sb.append(v0);
		sb.append(" = ");
		sb.append(v1);
		sb.append(StringUtils.pretty(this.cste));
		return sb.toString();
	}

}
