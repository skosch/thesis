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

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.search.IntBranchingDecision;

import java.util.logging.Level;

public class BranchingWithLoggingStatements extends AbstractIntBranchingStrategy {

	public final AbstractIntBranchingStrategy internalBranching;

	private int nextInformationNode;

	public BranchingWithLoggingStatements(AbstractIntBranchingStrategy internalBranching) {
		super();
		this.internalBranching = internalBranching;
		this.setSolver(internalBranching.manager);
		nextInformationNode = getEveryXNodes();
	}

	@Override
	public boolean finishedBranching(IntBranchingDecision decision) {
		return internalBranching.finishedBranching(decision);
	}

	@Override
	public String getDecisionLogMessage(IntBranchingDecision decision) {
		throw new SolverException("What are you doing ? It is the logging wrapper !");
	}


	public final static StringBuilder makeLoggingMsgPrefix(int worldStamp) {
		StringBuilder b  =new StringBuilder();
		//		b.append(LOG_PREFIX[worldStamp % (LOG_PREFIX.length)]);
		b.append(StringUtils.pad("", worldStamp, "."));
		b.append('[').append(worldStamp).append(']');
		return b;
	}

	protected String makeLoggingMessage(IntBranchingDecision decision, String dirMsg,int worldStamp) {
		StringBuilder b  = makeLoggingMsgPrefix(worldStamp);
		b.append(' ').append(dirMsg);
		b.append(internalBranching.getDecisionLogMessage(decision));
		b.append(" branch ").append(decision.getBranchIndex());
		return new String(b);
	}

	private int getEveryXNodes() {
		return manager.solver.getConfiguration().readInt(Configuration.EVERY_X_NODES);
	}

	private int getLoggingMaxDepth() {
		return manager.solver.getConfiguration().readInt(Configuration.LOGGING_MAX_DEPTH);
	}
	
	@Override
	public void goDownBranch(IntBranchingDecision decision)
	throws ContradictionException {
		if(LOGGER.isLoggable(Level.INFO)) {
			if(manager.getNodeCount() >= nextInformationNode) {
				LOGGER.log(Level.INFO, "- Partial Search - {0}.", manager.partialRuntimeStatistics(false));
				nextInformationNode = manager.getNodeCount() + getEveryXNodes();
				ChocoLogging.flushLogs();
			}  
			if (LOGGER.isLoggable(Level.CONFIG) &&
					manager.getSearchLoop().getDepthCount()  < getLoggingMaxDepth()) {
				LOGGER.log(Level.CONFIG, makeLoggingMessage(decision, LOG_DOWN_MSG, manager.solver.getWorldIndex()));
				ChocoLogging.flushLogs();
			}
		}
		internalBranching.goDownBranch(decision);
	}


	@Override
	public void goUpBranch(IntBranchingDecision decision)
	throws ContradictionException {
		if ( LOGGER.isLoggable(Level.CONFIG) 
				&& manager.getSearchLoop().getDepthCount() + 1 < getLoggingMaxDepth()) {
			LOGGER.log(Level.CONFIG, makeLoggingMessage(decision, LOG_UP_MSG, manager.solver.getWorldIndex() + 1));
		}
		internalBranching.goUpBranch(decision);

	}

	@Override
	public void setFirstBranch(IntBranchingDecision decision) {
		internalBranching.setFirstBranch(decision);

	}

	@Override
	public void setNextBranch(IntBranchingDecision decision) {
		internalBranching.setNextBranch(decision);

	}

	@Override
	public Object selectBranchingObject() throws ContradictionException {
		return internalBranching.selectBranchingObject();
	}


	@Override
	public void initBranching() throws ContradictionException {
		super.initBranching();
		internalBranching.initBranching();
	}

	@Override
	public void initConstraintForBranching(SConstraint c) {
		super.initConstraintForBranching(c);
		internalBranching.initConstraintForBranching(c);
	}

	public final static AbstractIntBranchingStrategy setLoggingStatement(AbstractIntBranchingStrategy goal) {
		AbstractIntBranchingStrategy res = new BranchingWithLoggingStatements(goal);
		if (goal.getNextBranching() != null) {
			if(goal.getNextBranching() instanceof AbstractIntBranchingStrategy) {
				res.setNextBranching(setLoggingStatement((AbstractIntBranchingStrategy) goal.getNextBranching()));
			}else {
				throw new SolverException("cant set logging statements");
			}
		}
		return res;
	}
}
