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


import choco.cp.solver.search.integer.varselector.DomOverWDeg;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.branch.AbstractLargeIntBranchingStrategy;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.search.ValIterator;
import choco.kernel.solver.search.ValSelector;

public class AssignVar extends AbstractLargeIntBranchingStrategy {
	private final VarSelector varHeuristic;
	private final ValueChooserWrapper wrapper;

	public AssignVar(VarSelector varSel, ValIterator valIterator) {
		varHeuristic = varSel;
		wrapper = new ValIteratorWrapper(valIterator);
	}

	public AssignVar(VarSelector varSel, ValSelector valSelector) {
		varHeuristic = varSel;
		wrapper = new ValSelectorWrapper(valSelector);
	}

	/**
	 * selecting the object under scrutiny (that object on which an alternative will be set)
	 *
	 * @return the object on which an alternative will be set (often  a variable)
	 */
	public Object selectBranchingObject() throws ContradictionException {
		return varHeuristic.selectVar();
	}


	public boolean finishedBranching(final IntBranchingDecision decision) {
		return wrapper.finishedBranching(decision);
	}

	

	public void setFirstBranch(final IntBranchingDecision decision) {
		decision.setBranchingValue(wrapper.getFirstBranch(decision));
	}

	public void setNextBranch(final IntBranchingDecision decision) {
		decision.setBranchingValue(wrapper.getNextBranch(decision));
	}

	@Override
	public void goDownBranch(final IntBranchingDecision decision) throws ContradictionException {
		decision.setIntVal();
	}

	@Override
	public void goUpBranch(final IntBranchingDecision decision) throws ContradictionException {
		decision.remIntVal();
	}

	@Override
	public String getDecisionLogMessage(IntBranchingDecision decision) {
		return getDefaultAssignMsg(decision);
	}

	protected interface ValueChooserWrapper  {
		public boolean finishedBranching(IntBranchingDecision decision);

		public int getFirstBranch(IntBranchingDecision decision);


		public int getNextBranch(IntBranchingDecision decision);
	}

    @SuppressWarnings({"unchecked"})
	protected static final class ValIteratorWrapper  implements ValueChooserWrapper {
        private final ValIterator valHeuristic;

        public ValIteratorWrapper(final ValIterator valHeuristic) {
            this.valHeuristic = valHeuristic;
        }

        public boolean finishedBranching(final IntBranchingDecision decision) {
			return ( ! valHeuristic.hasNextVal(decision.getBranchingIntVar(), decision.getBranchingValue()));
		}

		public int getFirstBranch(final IntBranchingDecision decision) {
			return valHeuristic.getFirstVal(decision.getBranchingIntVar());
		}

		public int getNextBranch(final IntBranchingDecision decision) {
			return valHeuristic.getNextVal(decision.getBranchingIntVar(), decision.getBranchingValue());
		}
	}

    @SuppressWarnings({"unchecked"})
	protected static final class ValSelectorWrapper implements ValueChooserWrapper {
		private final ValSelector valSelector;

        public ValSelectorWrapper(final ValSelector valSelector) {
            this.valSelector = valSelector;
        }

        public boolean finishedBranching(final IntBranchingDecision decision) {
			return decision.getBranchingIntVar().getDomainSize() == 0;
		}

		public int getFirstBranch(final IntBranchingDecision decision) {
			return valSelector.getBestVal(decision.getBranchingIntVar());
		}

		public int getNextBranch(final IntBranchingDecision decision) {
			return valSelector.getBestVal(decision.getBranchingIntVar());
		}
	}

	@Override
	public void initConstraintForBranching(SConstraint c) {
		if (varHeuristic instanceof DomOverWDeg) {
			((DomOverWDeg) varHeuristic).initConstraintForBranching(c);
		}
	}
}
