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
 * Implements a constraint X !== Y + C, with X and Y two variables and C a constant.
 */
public final class NotEqualXYC extends AbstractBinIntSConstraint {

	/**
	 * The search constant of the constraint
	 */
	protected final int cste;

	/**
	 * Constructs the constraint with the specified variables and constant.
	 *
	 * @param x0 first IntDomainVar
	 * @param x1 second IntDomainVar
	 * @param c  The search constant used in the disequality.
	 */

	public NotEqualXYC(IntDomainVar x0, IntDomainVar x1, int c) {
		super(x0, x1);
		this.cste = c;
	}

	@Override
	public int getFilteredEventMask(int idx) {
		//Principle : if v0 is instantiated and v1 is enumerated, then awakeOnInst(0) performs all needed pruning
		//Otherwise, we must check if we can remove the value from v1 when the bounds has changed.
		final IntDomainVar v = idx == 0 ? v1 : v0;
		return v.hasEnumeratedDomain() ? 
				IntVarEvent.INSTINT_MASK : IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
	}

	private void removeValV0() throws ContradictionException {
        if(v0.removeVal(v1.getVal() + this.cste, this, false)){
            this.setEntailed();
        }else if(!v0.canBeInstantiatedTo(v1.getVal() + this.cste)){
            this.setEntailed();
        }
	}

	private void removeValV1() throws ContradictionException {
        if(v1.removeVal(v0.getVal() - this.cste, this, false)){
            this.setEntailed();
        }else if(!v1.canBeInstantiatedTo(v0.getVal() - this.cste)){
            this.setEntailed();
        }
	}

	/**
	 * The one and only propagation method, using foward checking
	 */

	public final void propagate() throws ContradictionException {
		if (v0.isInstantiated()) {
            removeValV1();
        }
		else if (v1.isInstantiated()){
            removeValV0();
        }
	}

	@Override
	public void awakeOnInf(int idx) throws ContradictionException {
		propagate();
	}

	@Override
	public void awakeOnSup(int idx) throws ContradictionException {
		propagate();
	}

	@Override
	public final void awakeOnInst(int idx) throws ContradictionException {
		if (idx == 0) {
            removeValV1();
        }
		else{
            assert (idx == 1);
            removeValV0();
        }
	}



	@Override
	public void awakeOnRem(int varIdx, int val) throws ContradictionException {}



	@Override
	public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain)
	throws ContradictionException {}



	/**
	 * Checks if the listeners must be checked or must fail.
	 */

	@Override
	public Boolean isEntailed() {
		if ((v0.getSup() < v1.getInf() + this.cste) ||
				(v1.getSup() < v0.getInf() - this.cste))
			return Boolean.TRUE;
		else if ( v0.isInstantiated() 
				&& v1.isInstantiated() 
				&& v0.getInf() == v1.getInf() + this.cste)
			return Boolean.FALSE;
		else
			return null;
	}

	/**
	 * Checks if the constraint is satisfied when the variables are instantiated.
	 */

	@Override
	public boolean isSatisfied(int[] tuple) {
		return (tuple[0] != tuple[1] + this.cste);
	}

	/**
	 * tests if the constraint is consistent with respect to the current state of domains
	 *
	 * @return true iff the constraint is bound consistent (weaker than arc consistent)
	 */
	@Override
	public boolean isConsistent() {
		return ((v0.isInstantiated()) ?
				((v1.hasEnumeratedDomain()) ?
						(!v1.canBeInstantiatedTo(v0.getVal())) :
							((v1.getInf() != v0.getVal()) && (v1.getSup() != v0.getVal()))) :
								((!v1.isInstantiated()) || ((v0.hasEnumeratedDomain()) ?
										(!v0.canBeInstantiatedTo(v1.getVal())) :
											((v0.getInf() != v1.getVal()) && (v0.getSup() != v1.getVal())))));
	}

	@Override
	public AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
		return (AbstractSConstraint) solver.eq(v0, solver.plus(v1, cste));
	}


	@Override
	public String pretty() {
		StringBuffer sb = new StringBuffer();
		sb.append(v0).append(" != ");
		sb.append(v1).append(StringUtils.pretty(this.cste));
		return sb.toString();
	}

}
