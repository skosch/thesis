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
import choco.kernel.solver.search.ValIterator;
import choco.kernel.solver.variables.integer.IntDomainVar;


/**
 * History:
 * 2008-04-23 : Creation : dom / wdeg needs to be a branching not just an heuristic to allow to deal with backtracking events !
 * WARNING ! This implementation suppose that the variables will not change. It copies all variables in an array
 * at the beginning !!
 */
public class DomOverWDegBranchingNew extends AbstractDomOverWDegBranching {

	private final ValIterator valIterator;


	// Le constructeur avec :
	// * le solver pour fournir les variables
	// * l'heuristique de valeurs pour instantier une valeur
	public DomOverWDegBranchingNew(Solver s, IntDomainVar[] vars, ValIterator valHeuri, Number seed) {
		super(s, RatioFactory.createDomWDegRatio(vars, true), seed);
		valIterator = valHeuri;
	}

	@Override
	public String getDecisionLogMessage(IntBranchingDecision decision) {
		return getDefaultAssignMsg(decision);
	}
	
	@Override
	protected int getExpectedUpdateWeightsCount() {
		return solver.getSearchStrategy().getSearchLoop().getDepthCount();
	}

	@Override
	public void goDownBranch(IntBranchingDecision decision)
	throws ContradictionException {
		decision.setIntVal();
	}

	@Override
	public void goUpBranch(IntBranchingDecision decision)
	throws ContradictionException {
		//The weights are updated for the current branching object in setFirstBranch and finishedBranching.
		//We cant use a selector yet because the condition in finishedBranching is never activated and the weights become inconsistent.
		//// FIXME - No back-propagation ! - created 16 août 2011 by Arnaud Malapert

	}

	@Override
	public void setFirstBranch(IntBranchingDecision decision) {
		final IntDomainVar var = decision.getBranchingIntVar();
		decreaseVarWeights(var);
		decision.setBranchingValue( valIterator.getFirstVal(var));
	}

	@Override
	public void setNextBranch(IntBranchingDecision decision) {
		decision.setBranchingValue( 
				valIterator.getNextVal(decision.getBranchingIntVar(), decision.getBranchingValue()) 
		);
	}
	
	@Override
	public boolean finishedBranching(IntBranchingDecision decision) {
		final IntDomainVar var = decision.getBranchingIntVar();
		if (valIterator.hasNextVal(var, decision.getBranchingValue())) {
			return false;
		} else {
			increaseVarWeights(var);
			return true;
		}
	}

}


