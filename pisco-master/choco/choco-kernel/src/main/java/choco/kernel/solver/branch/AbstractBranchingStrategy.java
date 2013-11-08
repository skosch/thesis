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

package choco.kernel.solver.branch;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;

public abstract class AbstractBranchingStrategy implements BranchingStrategy {

	/**
	 * the main control object (responsible for the whole exploration, while Eqthe branching object
	 * is responsible only at the choice point level
	 */
	protected AbstractGlobalSearchStrategy manager;
	/**
	 * a link towards the next branching object (once this one is exhausted)
	 */
	private AbstractBranchingStrategy nextBranching;


	public final static String LOG_DOWN_MSG = "down branch ";
	public final static String LOG_UP_MSG = "up branch ";
	public final static String LOG_DECISION_MSG_ASSIGN = "==";
	public static final String LOG_DECISION_MSG_REMOVE = " != ";



	public void setSolver(AbstractGlobalSearchStrategy s) {
		manager = s;
	}

	public AbstractGlobalSearchStrategy getManager(){
		return manager;
	}

	/**
	 * Gets the next branching.
	 * @return the next branching
	 */
	public final AbstractBranchingStrategy getNextBranching() {
		return nextBranching;
	}

	/**
	 * Sets the next branching.
	 * @param nextBranching the next branching
	 */
	public final void setNextBranching(AbstractBranchingStrategy nextBranching) {
		this.nextBranching = nextBranching;
	}

	

	/**
	 * This method is called before launching the search. it may be used to intialiaze data structures or counters for
	 * instance.
	 */
	public void initBranching() throws ContradictionException {
		// Nothing to do by default
	}

	/**
	 * this method is used to build the data structure in the branching for
	 * the given constraint. This is used when the constraint was not present
	 * at the initialization of the branching, for example a cut
	 * @param c constraint
	 */
	public void initConstraintForBranching(SConstraint c) {
		//nothing to do by default
	}

}
