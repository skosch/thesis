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
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Implements a constraint X + Y <= C , with X and Y two variables and C a constant.
 */
public final class LessOrEqualXY_C extends AbstractBinIntSConstraint {

	/**
	 * The search constant of the constraint
	 */
	protected final int cste;

	/**
	 * Constructs the constraint with the specified variables and constant.
	 *
	 * @param x0 Should be greater than <code>x0+c</code>.
	 * @param x1 Should be less than <code>x0-c</code>.
	 * @param c  The search constant used in the inequality.
	 */

	public LessOrEqualXY_C(IntDomainVar x0, IntDomainVar x1, int c) {
		super(x0, x1);
		this.cste = c;
		
	}


	@Override
	public int getFilteredEventMask(int idx) {
		return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
		// return 0x0B;
	}


	private final void updateSupV0() throws ContradictionException {
		v0.updateSup(cste - v1.getInf(), this, false);
	}

	private final void updateSupV1() throws ContradictionException {
		v1.updateSup( cste - v0.getInf(), this, false);
	}
	/**
	 * The propagation on constraint awake events.
	 *
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */

	public void propagate() throws ContradictionException {
		updateSupV0();
		updateSupV1();
	}


	/**
	 * Propagation when a minimal bound of a variable was modified.
	 *
	 * @param idx The index of the variable.
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */

	@Override
	public void awakeOnInf(int idx) throws ContradictionException {
		if (idx == 0) updateSupV1();
		else assert(idx == 1); updateSupV0();
	}


	/**
	 * Propagation when a maximal bound of a variable was modified.
	 *
	 * @param idx The index of the variable.
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */

	@Override
	public void awakeOnSup(int idx) throws ContradictionException {
		if( v0.getSup() + v1.getSup() <= cste) setEntailed();
	}


	/**
	 * Propagation when a variable is instantiated.
	 *
	 * @param idx The index of the variable.
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */

	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		if (idx == 0) updateSupV1();
		else updateSupV0();
		assert(v0.getSup() + v1.getSup() <= this.cste);
		this.setEntailed();
	}

	/**
	 * Checks if the listeners must be checked or must fail.
	 */

	@Override
	public Boolean isEntailed() {
		if (v0.getSup() + v1.getSup() <= this.cste)
			return Boolean.TRUE;
		else if (v0.getInf() + v1.getInf() > this.cste)
			return Boolean.FALSE;
		return null;
	}


	/**
	 * Checks if the constraint is satisfied when the variables are instantiated.
	 *
	 * @return true if the constraint is satisfied
	 */

	@Override
	public boolean isSatisfied(int[] tuple) {
		return tuple[0] + tuple[1] <= this.cste;
	}

	/**
	 * tests if the constraint is consistent with respect to the current state of domains
	 *
	 * @return true iff the constraint is bound consistent (weaker than arc consistent)
	 */
	@Override
	public boolean isConsistent() {
		return ((v0.getInf() + v1.getSup() <=  this.cste) && (v0.getSup() + v1.getInf() <= this.cste));
	}

	@Override
	public AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
		return (AbstractSConstraint) solver.gt(solver.plus(v0, v1),cste);
	}

	@Override
	public String pretty() {
		StringBuffer sb = new StringBuffer();
		sb.append(v0).append(" + ").append(v1);
		sb.append(" <= ").append(this.cste);
		return sb.toString();
	}

}
