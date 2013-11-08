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
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.AbstractUnIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Implements a constraint X >= C, with X a variable and C a constant.
 */
public final class GreaterOrEqualXC extends AbstractUnIntSConstraint {

	/**
	 * The search constant of the constraint
	 */
	protected final int cste;

	/**
	 * Constructs the constraint with the specified variables and constant.
	 *
	 * @param x0 the search valued domain variable
	 * @param c  the search constant used in the inequality.
	 */

	public GreaterOrEqualXC(IntDomainVar x0, int c) {
		super(x0);
		this.cste = c;
	}

	@Override
	public int getFilteredEventMask(int idx) {
		return IntVarEvent.INSTINT_MASK;
	}


	/**
	 * Pretty print of the constraint.
	 */

	@Override
	public String pretty() {
		return this.v0 + " >= " + cste;
	}


	/**
	 * The one and only propagation method. <br>
	 * Note that after the first propagation, the constraint is set passive
	 * (to prevent from further calls to propagation methods)
	 */

	public void propagate() throws ContradictionException {
		v0.updateInf(this.cste, this, false);
		this.setEntailed();
	}


	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		assert(idx == 0);
		if (v0.getVal() < this.cste) this.fail();
	}

	/**
	 * When the whole domain of <code>v0</code> is below or above <code>cste</code>,
	 * we know for sure whether the constraint will be satisfied or not
	 */

	@Override
	public Boolean isEntailed() {
		if (v0.getInf() >= this.cste)
			return Boolean.TRUE;
		else if (v0.getSup() < this.cste)
			return Boolean.FALSE;
		else
			return null;
	}

	/**
	 * tests if the constraint is satisfied when the variables are instantiated.
	 */

	@Override
	public boolean isSatisfied(int[] tuple) {
		return (tuple[0] >= this.cste);
	}

	/**
	 * tests if the constraint is consistent with respect to the current state of domains
	 *
	 * @return true iff the constraint is bound consistent (same as arc consistent)
	 */
	@Override
	public boolean isConsistent() {
		return (v0.getInf() >= this.cste);
	}

	@Override
	public AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
		return (AbstractSConstraint) solver.lt(v0, cste);
	}


}
