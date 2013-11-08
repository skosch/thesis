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

package choco.kernel.solver.search;

import static choco.kernel.solver.search.AbstractGlobalSearchStrategy.*;


public abstract class AbstractSearchLoop implements ISearchLoop {

	public final AbstractGlobalSearchStrategy searchStrategy;

	private int nodeCount;

	private int backtrackCount;

	private int restartCount;

	private int depthCount;

	protected boolean stop;


	public AbstractSearchLoop(AbstractGlobalSearchStrategy searchStrategy) {
		this.searchStrategy = searchStrategy;
	}



	public final AbstractGlobalSearchStrategy getSearchStrategy() {
		return searchStrategy;
	}



	public final boolean isStopped() {
		return stop;
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public final int getBacktrackCount() {
		return backtrackCount;
	}


	public final int getRestartCount() {
		return restartCount;
	}


	public final int getDepthCount() {
		return depthCount;
	}


	@Override
	public void initialize() {
		nodeCount = 0;
		backtrackCount = 0;
		restartCount = 0;
		depthCount = 0;

	}


	public final Boolean run() {
		stop = false;
		initLoop();
		while (!stop) {
			switch (searchStrategy.nextMove) {
			//The order of the condition is important. 
			//SEARCH TREE MOVES
			case OPEN_NODE: {
				nodeCount++;
				openNode();
				break;
			}
			case DOWN_BRANCH: {
				depthCount++;
				downBranch();
				break;
			}
			case UP_BRANCH: {
				if (searchStrategy.isTraceEmpty()) {
					//cant backtrack from the root node
					stop = true;
				} else {
					depthCount--;
					backtrackCount++;
					upBranch();
				}
				break;
			}
			//RESTART MOVES
			case RESTART: {
				restartCount++;
				restart();
				depthCount = 0;
				break;
			}
			case INIT_SEARCH: {
				initSearch();
				break;
			}
			//FINAL MOVES
			case STOP: {
				stop = true;
				break;
			}
			}
		}
		return endLoop();
	}

	public abstract void initLoop();

	public abstract void openNode();

	public abstract void upBranch();

	public abstract void downBranch();

	public abstract void restart();

	public abstract void initSearch();

	public abstract Boolean endLoop();


}
