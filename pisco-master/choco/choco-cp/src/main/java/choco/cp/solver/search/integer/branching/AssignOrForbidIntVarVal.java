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
import choco.kernel.solver.SolverException;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.search.integer.VarValPairSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A class for branching schemes that consider two branches: - one assigning a
 * value to an IntVar (X == v) - and the other forbidding this assignment (X != v)
 */

public class AssignOrForbidIntVarVal extends AbstractAssignOrForbidBranching {
	
	private VarSelector varHeuristic;

	public AssignOrForbidIntVarVal(VarSelector<IntDomainVar> varHeuristic,
			ValSelector<IntDomainVar> valSHeuristic) {
		super(valSHeuristic);
		this.varHeuristic = varHeuristic;
	}

	/** replaced by {@link AssignOrForbidIntVarValPair} */
	@Deprecated
	public AssignOrForbidIntVarVal(VarValPairSelector pairh) {
		super(null);
		throw new SolverException("replaced by AssignOrForbidIntVarValPair");
	}


	@Override
	public void goDownBranch(final IntBranchingDecision ctx) throws ContradictionException {
		if (ctx.getBranchIndex() == 0) {
			ctx.setIntVal();
		} else {
			ctx.remIntVal();
		}
		// Calls to propagate() are useless since it is done in the SearchLoop
	}

	
    /**
     * selecting the object under scrutiny (that object on which an alternative will be set)
     *
     * @return the object on which an alternative will be set (often  a variable)
     */
    public Object selectBranchingObject() throws ContradictionException {
		return varHeuristic.selectVar();
	}
}
