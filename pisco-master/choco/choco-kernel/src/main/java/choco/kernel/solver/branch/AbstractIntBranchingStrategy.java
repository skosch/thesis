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


import choco.kernel.solver.search.IntBranchingDecision;



/**
 * An abstract class for all implementations of branching objets (objects controlling the tree search)
 *
 */
public abstract class AbstractIntBranchingStrategy extends AbstractBranchingStrategy implements IntBranching {


	//	private static final String LOG_MSG_FORMAT = "{0} {1} {2} {3} {4}";
	//	
	//	private static final String LOG_MSG_FORMAT_WITH_BRANCH = "{0} {1} {2} {3} {4} branch {5}";
	//	
	//	public void goDownBranch(Object x, int i) throws ContradictionException {
	//		logDownBranch(x, i);
	//	}
	//
	//	public void goUpBranch(Object x, int i) throws ContradictionException {
	//		logUpBranch(x, i);
	//	}
	//
	//	protected Object getVariableLogParameter(final Object x) {
	//		return x;
	//	}
	//
	//	protected Object getValueLogParameter(final Object x, final int branch) {
	//		return Integer.valueOf(branch);
	//	}
	//
	//
	//	@Override
	//	public String getDecisionLogMsg(int branchIndex) {
	//		return LOG_DECISION_MSG_ASSIGN;
	//	}
	//
	//	protected final String getDefaultLogMessage() {
	//		return LOG_MSG_FORMAT;
	//	}
	//
	//	protected final String getLogMessageWithBranch() {
	//		return LOG_MSG_FORMAT_WITH_BRANCH;
	//	}
	//
	//	/**
	//	 * a log message using java.util logging {} arguments </br>
	//	 * {0}: world index (formatter arguments)</br>
	//	 * {1}: Up or Down message </br>
	//	 * {2}: Branching var </br>
	//	 * {3}: decision msg </br>
	//	 * {4}: Branching val </br>
	//	 * {5}: Branch index </br>
	//	 * 
	//	 * @return
	//	 */
	//	protected String getLogMessage() {
	//		return getDefaultLogMessage();
	//	}
	//
	//	protected final void logDownBranch(final Object x, final int i) {
	//		if (LOGGER.isLoggable(Level.INFO)) {
	//			final WorldFormatter wl = new WorldFormatter(manager);
	//			if ( wl.isLoggable(manager)) {
	//				LOGGER.log(Level.INFO, getLogMessage(), new Object[]{wl, LOG_DOWN_MSG, getVariableLogParameter(x), getDecisionLogMsg(i), getValueLogParameter(x,i), Integer.valueOf(i)});
	//			}
	//		}
	//	}
	//
	//	protected final void logUpBranch(final Object x, final int i) {
	//		if (LOGGER.isLoggable(Level.INFO)) {
	//			final WorldFormatter wl = new WorldFormatter(manager, 1);
	//			if ( wl.isLoggable(manager)) {
	//				LOGGER.log(Level.INFO, getLogMessage(), new Object[]{wl, LOG_UP_MSG, getVariableLogParameter(x), getDecisionLogMsg(i), getValueLogParameter(x,i), Integer.valueOf(i)});
	//			}
	//		}
	//	}

	public static String getDefaultAssignMsg(IntBranchingDecision decision) {
		return decision.getBranchingObject() + 
		LOG_DECISION_MSG_ASSIGN + 
		decision.getBranchingValue();
	}

	public static String getDefaultAssignOrForbidMsg(IntBranchingDecision decision) {
		return decision.getBranchingObject() + 
		(decision.getBranchIndex() == 0 ? LOG_DECISION_MSG_ASSIGN : LOG_DECISION_MSG_REMOVE) + 
		decision.getBranchingValue();
	}

}
