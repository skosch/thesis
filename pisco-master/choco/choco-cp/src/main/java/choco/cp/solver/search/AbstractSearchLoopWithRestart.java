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

package choco.cp.solver.search;

import choco.cp.solver.search.restart.IKickRestart;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.branch.AbstractBranchingStrategy;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import static choco.Choco.FALSE;
import static choco.kernel.solver.search.AbstractGlobalSearchStrategy.*;
import choco.kernel.solver.search.AbstractSearchLoop;
import choco.kernel.solver.search.IntBranchingTrace;

import java.util.logging.Level;


public abstract class AbstractSearchLoopWithRestart extends AbstractSearchLoop {

	protected int moveAfterSolution = UP_BRANCH;

	protected int previousNbSolutions;

	protected IKickRestart kickRestart;

	/**
	 * current trace object. 
	 */
	protected IntBranchingTrace ctx = null;


	public AbstractSearchLoopWithRestart(AbstractGlobalSearchStrategy searchStrategy, IKickRestart kickRestart) {
		super(searchStrategy);
		this.kickRestart = kickRestart;
	}



	public final IntBranchingTrace getCurrentTrace() {
		return ctx;
	}



	public final IKickRestart getKickRestart() {
		return kickRestart;
	}


	public final void setRestartAfterEachSolution(boolean restart) {
		moveAfterSolution = restart ? RESTART : UP_BRANCH; 
	}



	public final void setKickRestart(IKickRestart kickRestart) {
		this.kickRestart = kickRestart;
	}



	//*****************************************************************//
	//*******************  INITIALIZATIONS ***************************//
	//***************************************************************//





	@Override
	public void initialize() {
		previousNbSolutions = 0;
		super.initialize();
	}

	protected AbstractBranchingStrategy br;

	@Override
	public void initSearch() {
		br = searchStrategy.mainGoal;
		try {
			while (br != null) {
				br.initBranching();
				br = br.getNextBranching();
			}
			searchStrategy.nextMove = OPEN_NODE;
		} catch (ContradictionException e) {
			searchStrategy.nextMove = STOP;
		}
	}



	@Override
	public void initLoop() {
		previousNbSolutions = searchStrategy.getSolutionCount();
		searchStrategy.setEncounteredLimit(null);
		ctx = searchStrategy.initialTrace();
	}


	/**
	 * reset the limit and compute the exit status. 
	 */
	@Override
	public Boolean endLoop() {
		searchStrategy.limitManager.reset();
		if (searchStrategy.getSolutionCount() > previousNbSolutions) {
			return Boolean.TRUE;
		} else if (searchStrategy.isEncounteredLimit()) {
			return null;
		} else {
			return Boolean.FALSE;
		}
	}


	//*****************************************************************//
	//*******************  OPEN_NODE  ********************************//
	//***************************************************************//

	protected Object branchingObj;

	protected AbstractIntBranchingStrategy currentBranching;

	@Override
	public void openNode() {
		try {
			searchStrategy.limitManager.newNode();
			//looking for the next branching object
			currentBranching =  ctx.getBranching();
			while(currentBranching != null) {
				branchingObj = currentBranching.selectBranchingObject();
				if( branchingObj == null) {
					//the branching is achieved, check next branching
					currentBranching = (AbstractIntBranchingStrategy) currentBranching.getNextBranching();
				}else {
					//create the node associated with the branching object
					ctx = searchStrategy.pushTrace();
					ctx.setBranching(currentBranching);
					ctx.setBranchingObject(branchingObj);
					currentBranching.setFirstBranch(ctx);
					searchStrategy.nextMove = AbstractGlobalSearchStrategy.DOWN_BRANCH;
					return; //the new node is opened.
				}
			}
			//The original version makes more comparisons and affectations 

			//			Object branchingObj = null;
			//			AbstractIntBranching currentBranching = (AbstractIntBranching) ctx.getBranching();
			//			AbstractIntBranching nextBranching = currentBranching;
			//			while ((branchingObj == null) && (nextBranching != null)) {
			//				currentBranching = nextBranching;
			//				branchingObj = currentBranching.selectBranchingObject();
			//				nextBranching = (AbstractIntBranching) currentBranching.getNextBranching();
			//			}
			//			if (branchingObj != null) {
			//				ctx = searchStrategy.pushTrace();
			//				ctx.setBranching(currentBranching);
			//				ctx.setBranchingObject(branchingObj);
			//				ctx.setBranchIndex(currentBranching.getFirstBranch(ctx.getBranchingObject()));
			//				searchStrategy.nextMove = AbstractGlobalSearchStrategy.DOWN_BRANCH;
			//			} else {
			//				searchStrategy.recordSolution();
			//				searchStrategy.nextMove = moveAfterSolution;
			//				stop = true;
			//			}

			//we found a valid solution because there is no more branching object
			//the solution must instantiate at least the decision variables
			//Other variables should be fixed by propagation or remained not instantiated 
			searchStrategy.nextMove = moveAfterSolution; //set the next move (backtrack, restart or stop)
			stop = true; //a solution has been found, we should run the loop again to find another solution
			searchStrategy.recordSolution(); //record the solution (could change the nextMove)
		} catch (ContradictionException e) {
			searchStrategy.nextMove = e.getContradictionMove();
		}
	}


	//*****************************************************************//
	//*******************  UP_BRANCH  ********************************//
	//***************************************************************//

	protected abstract void worldPop();

	/**
	 * post the dynamic cut, backtrack and propagate.
	 * @throws choco.kernel.solver.ContradictionException can be thrown 
	 */
	protected void goUpBranch() throws ContradictionException {
		searchStrategy.postDynamicCut();
		ctx.getBranching().goUpBranch(ctx);
		searchStrategy.solver.propagate();
	}




	@Override
	public void upBranch() {
		try {
			searchStrategy.limitManager.endNode(); //check limit
			worldPop();
			goUpBranch(); //backtrack
			//compute the next move
			if (!ctx.getBranching().finishedBranching(ctx)) {
				ctx.getBranching().setNextBranch(ctx);
				ctx.incrementBranchIndex();
				searchStrategy.nextMove = AbstractGlobalSearchStrategy.DOWN_BRANCH;
			} else {
				ctx = searchStrategy.popTrace();
				searchStrategy.nextMove = AbstractGlobalSearchStrategy.UP_BRANCH;
			}
		} catch (ContradictionException e) {
			ctx = searchStrategy.popTrace();
			searchStrategy.nextMove = e.getContradictionMove();
		}
	}


	//*****************************************************************//
	//*******************  DOWN_BRANCH  ********************************//
	//***************************************************************//

	protected abstract void worldPush();

	@Override
	public void downBranch() {
		try {
			worldPush();
			ctx.getBranching().goDownBranch(ctx);
			searchStrategy.solver.propagate();
			searchStrategy.nextMove = AbstractGlobalSearchStrategy.OPEN_NODE;
		} catch (ContradictionException e) {
			searchStrategy.nextMove = e.getContradictionMove();
		}
	}

	//*****************************************************************//
	//*******************  RESTART  ********************************//
	//***************************************************************//


	/**
	 * perform the restart.
	 */
	@Override
	public void restart() {
		if(LOGGER.isLoggable(Level.CONFIG)) LOGGER.log(Level.CONFIG, "- Restarting search - {0} Restarts", getRestartCount());
		kickRestart.restoreRootNode(ctx);
		try {
			searchStrategy.postDynamicCut();
			searchStrategy.solver.propagate();
			ctx = searchStrategy.getReusableInitialTrace();
			searchStrategy.nextMove = INIT_SEARCH;
			if( searchStrategy.limitManager.newRestart()) {
				LOGGER.config("- Limit reached: stop restarting");
				setRestartAfterEachSolution(false);
				searchStrategy.limitManager.cancelRestartStrategy();
			}
		} catch (ContradictionException e) {
			stop = true;
		}
	}

}
