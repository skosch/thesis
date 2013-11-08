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

package samples.multicostregular.nsp;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.branch.AbstractLargeIntBranchingStrategy;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Dec 9, 2008
 * Time: 1:03:46 PM
 */
public class NSPBranching extends AbstractLargeIntBranchingStrategy {

	NSPVarSelector varselec;
	NSPValSelector valselec;
	IntDomainVar nextVar;

	public NSPBranching(NSPVarSelector varselec, NSPValSelector valselec)
	{
		this.varselec = varselec;
		this.valselec = valselec;
	}



	public void setFirstBranch(final IntBranchingDecision decision) {
		decision.setBranchingValue( valselec.getBestVal( decision.getBranchingIntVar()));
	}


	private IntDomainVar reuseVar;

	public void setNextBranch(final IntBranchingDecision decision) {
		decision.setBranchingValue( valselec.getBestVal( reuseVar));
	}

	public boolean finishedBranching(final IntBranchingDecision decision) {
		if(decision.getBranchingIntVar().getDomainSize() == 0) {
			return true;
		}else {
			reuseVar = varselec.selectVar();
			return reuseVar == null;
		}

	}

	public Object selectBranchingObject() throws ContradictionException {
		return varselec.selectVar();
	}

	public void goDownBranch(final IntBranchingDecision decision) throws ContradictionException {
		if ( decision.getBranchIndex() == 0) {
			decision.setIntVal();
		} else {
			reuseVar.setVal( decision.getBranchingValue());
		}
	}

	public void goUpBranch(final IntBranchingDecision decision) throws ContradictionException {
		if ( decision.getBranchIndex() == 0) {
			decision.remIntVal();
		} else {
			reuseVar.remVal( decision.getBranchingValue());
		}    
	}



	@Override
	public String getDecisionLogMessage(IntBranchingDecision decision) {
		return (decision.getBranchIndex() == 0 ? decision.getBranchingObject() : reuseVar) + LOG_DECISION_MSG_ASSIGN + decision.getBranchingValue();
	}
	
	
}
