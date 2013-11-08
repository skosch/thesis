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

package choco.cp.solver.search.integer.branching.domwdeg;

import choco.cp.solver.search.integer.varselector.ratioselector.ratios.IntRatio;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.IntBranchingDecision;

public abstract class AbstractDomOverWDegBinBranching extends
		AbstractDomOverWDegBranching {

	protected final IStateInt nbRightBranches;

	public AbstractDomOverWDegBinBranching(Solver solver, IntRatio[] varRatios, Number seed) {
		super(solver, varRatios, seed);
		nbRightBranches = solver.getEnvironment().makeInt();
	}
	
	
	@Override
	protected int getExpectedUpdateWeightsCount() {
		return solver.getSearchStrategy().getSearchLoop().getDepthCount() - nbRightBranches.get();
	}

	public final void getNextBranch(final IntBranchingDecision decision) {
		assert decision.getBranchIndex() == 0;
	}

	public final boolean finishedBranching(final IntBranchingDecision decision) {
		return decision.getBranchIndex() > 0;
	}

	@Override
	public String getDecisionLogMessage(IntBranchingDecision decision) {
		return getDefaultAssignOrForbidMsg(decision);
	}

	/**
	 * nothing to do
	 */
	@Override
	public void goUpBranch(IntBranchingDecision decision) throws ContradictionException {
		//nothing to do	
	}

	/**
	 * nothing to do
	 */
	@Override
	public void setNextBranch(IntBranchingDecision decision) {
		assert decision.getBranchIndex() == 0;
		nbRightBranches.add(1);
	}
	
}