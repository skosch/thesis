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

package choco.cp.solver.search.integer.branching;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.search.integer.IntVarValPair;
import choco.kernel.solver.search.integer.VarValPairSelector;

public class AssignOrForbidIntVarValPair extends
AbstractAssignOrForbidBranching {


	public final VarValPairSelector pairSelector;

	public AssignOrForbidIntVarValPair(VarValPairSelector pairSelector) {
		super(null);
		this.pairSelector = pairSelector;
	}

	@Override
	public void setFirstBranch(final IntBranchingDecision decision) {}

	@Override
	public void goDownBranch(IntBranchingDecision decision)
	throws ContradictionException {
		IntVarValPair pair = (IntVarValPair) decision.getBranchingObject();
		if( decision.getBranchIndex() == 0) {
			pair.var.setVal(pair.val);
		}else {
			assert(decision.getBranchIndex() == 1);
			pair.var.remVal(pair.val);
		}
	}

	@Override
	public Object selectBranchingObject() throws ContradictionException {
		return pairSelector.selectVarValPair();
	}

	@Override
	public String getDecisionLogMessage(IntBranchingDecision decision) {
		final IntVarValPair pair = (IntVarValPair) decision.getBranchingObject();
		return pair.var + 
		(decision.getBranchIndex() == 0 ? LOG_DECISION_MSG_ASSIGN : LOG_DECISION_MSG_REMOVE) + 
		pair.val;
	}



}
