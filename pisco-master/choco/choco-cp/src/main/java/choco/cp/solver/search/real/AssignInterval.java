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

package choco.cp.solver.search.real;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.search.ValIterator;
import choco.kernel.solver.variables.real.RealMath;
import choco.kernel.solver.variables.real.RealVar;

/**
 * A binary branching assigning interval to subinterval.
 */

public class AssignInterval extends AbstractIntBranchingStrategy {

	protected VarSelector<RealVar> varSelector;
	protected ValIterator<RealVar> valIterator;

	protected static final String[] LOG_DECISION_MSG = new String[]{"in first half of ", "in second half of "};

	public AssignInterval(VarSelector<RealVar> varSelector, ValIterator<RealVar> valIterator) {
		this.varSelector = varSelector;
		this.valIterator = valIterator;
	}

	public Object selectBranchingObject() throws ContradictionException {
		return varSelector.selectVar();
	}

	@Override
	public void goDownBranch(final IntBranchingDecision decision) throws ContradictionException {
		final RealVar x = decision.getBranchingRealVar();
		if ( decision.getBranchingValue() == 1) {
			x.intersect(RealMath.firstHalf(x));
			//manager.solver.propagate(); //FIXME is propagate useful ?
		} else if( decision.getBranchingValue() == 2) {
			x.intersect(RealMath.secondHalf(x));
			//manager.solver.propagate(); //FIXME is propagate useful ?
		} else {
			throw new SolverException("invalid real branching value");
		}
	}

	/**
	 * do nothing
	 */
	@Override
	public void goUpBranch(final IntBranchingDecision decision) throws ContradictionException {
		//do nothing
	}


    public void setFirstBranch(final IntBranchingDecision decision) {
		decision.setBranchingValue( valIterator.getFirstVal(decision.getBranchingRealVar()));
	}

	public void setNextBranch(final IntBranchingDecision decision) {
		decision.setBranchingValue( valIterator.getNextVal(decision.getBranchingRealVar(), decision.getBranchingValue()));
	}

	public boolean finishedBranching(final IntBranchingDecision decision) {
		return  ! valIterator.hasNextVal(decision.getBranchingRealVar(), decision.getBranchingValue());
	}

	@Override
	public String getDecisionLogMessage(IntBranchingDecision decision) {
		return  LOG_DECISION_MSG[decision.getBranchIndex()] + decision.getBranchingObject();
	}


}
