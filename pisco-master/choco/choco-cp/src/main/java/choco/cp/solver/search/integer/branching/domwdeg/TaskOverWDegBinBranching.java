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

import choco.cp.solver.constraints.global.scheduling.precedence.ITemporalSRelation;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.IntRatio;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.task.ITemporalRatio;
import choco.cp.solver.search.task.OrderingValSelector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class TaskOverWDegBinBranching extends AbstractDomOverWDegBinBranching {

	private final OrderingValSelector precValSelector;

	public TaskOverWDegBinBranching(Solver solver, ITemporalRatio[] varRatios, OrderingValSelector valHeuri, Number seed) {
		super(solver, varRatios, seed);
		this.precValSelector = valHeuri;
	}

	public void setFirstBranch(final IntBranchingDecision decision) {
		final ITemporalSRelation brObj =  (ITemporalSRelation) decision.getBranchingObject();
		decision.setBranchingValue(precValSelector.getBestVal( brObj));
		decreaseVarWeights( brObj.getDirection());
	}

	@Override
	public void setNextBranch(IntBranchingDecision decision) {
		if( updateWeightsCount == getExpectedUpdateWeightsCount() ) {
			increaseVarWeights( ((ITemporalSRelation) decision.getBranchingObject()).getDirection());
		} else updateWeightsCount = Integer.MIN_VALUE;
		super.setNextBranch(decision);
	}



	@Override
	public void goDownBranch(final IntBranchingDecision decision) throws ContradictionException {
		final IntDomainVar v = ( (ITemporalSRelation) decision.getBranchingObject()).getDirection();
		if (decision.getBranchIndex() == 0) {
			v.setVal(decision.getBranchingValue());
		} else {
			assert decision.getBranchIndex() == 1;
			v.remVal(decision.getBranchingValue());
		}
	}

	@Override
	public Object selectBranchingObject() throws ContradictionException {
		reinitBranching();
		IntRatio best = getRatioSelector().selectIntRatio();
		return best == null ? null :  ( (ITemporalRatio) best).getTemporalRelation();
	}


}
