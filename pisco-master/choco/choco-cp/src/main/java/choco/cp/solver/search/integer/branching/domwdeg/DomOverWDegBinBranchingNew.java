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

import choco.cp.solver.search.integer.varselector.ratioselector.ratios.RatioFactory;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class DomOverWDegBinBranchingNew extends AbstractDomOverWDegBinBranching {

	// L'heuristique pour le valeurs
	protected final ValSelector valSelector;

	public DomOverWDegBinBranchingNew(Solver solver, IntDomainVar[] vars, ValSelector valHeuri, Number seed) {
		super(solver, RatioFactory.createDomWDegRatio(vars, true), seed);
		this.valSelector = valHeuri;
	}


	public void setFirstBranch(final IntBranchingDecision decision) {
		decision.setBranchingValue(valSelector.getBestVal(decision.getBranchingIntVar()));
		decreaseVarWeights(decision.getBranchingIntVar());
	}

	@Override
	public void setNextBranch(IntBranchingDecision decision) {
		if( updateWeightsCount == getExpectedUpdateWeightsCount() + 1 ) increaseVarWeights(decision.getBranchingIntVar());
		else updateWeightsCount = Integer.MIN_VALUE;
		super.setNextBranch(decision);
	}


	@Override
	public void goDownBranch(final IntBranchingDecision decision) throws ContradictionException {
		if (decision.getBranchIndex() == 0) {
			decision.setIntVal();
		} else {
			assert decision.getBranchIndex() == 1;
			decision.remIntVal();
		}
	}


}
