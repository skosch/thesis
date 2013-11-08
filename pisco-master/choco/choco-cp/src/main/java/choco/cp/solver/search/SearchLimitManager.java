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


import choco.kernel.common.TimeCacheThread;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import static choco.kernel.solver.search.AbstractGlobalSearchStrategy.RESTART;
import static choco.kernel.solver.search.AbstractGlobalSearchStrategy.STOP;
import choco.kernel.solver.search.GlobalSearchLimitManager;
import choco.kernel.solver.search.limit.AbstractGlobalSearchLimit;
import choco.kernel.solver.search.limit.NoLimit;
import choco.kernel.solver.search.restart.NoRestartStrategy;
import choco.kernel.solver.search.restart.UniversalRestartStrategy;

import java.util.logging.Logger;

public class SearchLimitManager implements GlobalSearchLimitManager {

	public final static Logger LOGGER = ChocoLogging.getSearchLogger();

	protected final AbstractGlobalSearchStrategy searchStrategy;

	protected AbstractGlobalSearchLimit restartLimit;

	protected AbstractGlobalSearchLimit searchLimit;

	//RESTART LIMIT
	protected UniversalRestartStrategy restartStrategy;

	protected AbstractGlobalSearchLimit restartStrategyLimit;

	private int restartCutoff = 0;

	//COUNTERS
	private int restartFromStrategyCount = 0;

	private int timeCount;

	private long starth;

	public SearchLimitManager(AbstractGlobalSearchStrategy searchStrategy) {
		super();
		this.searchStrategy = searchStrategy;
	}

	//*****************************************************************//
	//*******************  GETTERS/SETTERS ****************************//
	//***************************************************************//
	public final AbstractGlobalSearchLimit getRestartLimit() {
		return restartLimit;
	}

	public final UniversalRestartStrategy getRestartStrategy() {
		return restartStrategy;
	}

	public final AbstractGlobalSearchLimit getRestartStrategyLimit() {
		return restartStrategyLimit;
	}

	public final void setRestartLimit(AbstractGlobalSearchLimit restartLimit) {
		this.restartLimit = restartLimit == null ? NoLimit.SINGLOTON : restartLimit;
	}

	public final AbstractGlobalSearchLimit getSearchLimit() {
		return searchLimit;
	}



	public final void setSearchLimit(AbstractGlobalSearchLimit searchLimit) {
		this.searchLimit = searchLimit == null ? NoLimit.SINGLOTON : searchLimit;
	}


	public final void setRestartStrategy(UniversalRestartStrategy restartStrategy, AbstractGlobalSearchLimit restartStrategyLimit) {
		if( restartStrategyLimit == null || restartStrategy == null) {
			this.restartStrategyLimit = NoLimit.SINGLOTON;
			this.restartStrategy = NoRestartStrategy.SINGLOTON;
		}else {
			this.restartStrategy = restartStrategy;
			this.restartStrategyLimit = restartStrategyLimit;
		}
	}


	public final int getRestartFromStrategyCount() {
		return restartFromStrategyCount;
	}


	public final int getRestartCutoff() {
		return restartCutoff;
	}

	/**
	 * Get the time in milliseconds elapsed since the beginning of the search.
	 */
	public final int getTimeCount() {
		return timeCount;
	}

	@Override
	public final AbstractGlobalSearchStrategy getSearchStrategy() {
		return searchStrategy;
	}



	//*****************************************************************//
	//*******************  LIMIT MANAGEMENT **************************//
	//***************************************************************//
	protected final void updateTimeCount() {
		timeCount = (int) (TimeCacheThread.currentTimeMillis - starth);
	}

	@Override
	public final void initialize() {
		starth = System.currentTimeMillis();
		TimeCacheThread.currentTimeMillis = starth;
		restartFromStrategyCount = 0;
		restartCutoff = restartStrategy.getScaleFactor();
		restartStrategyLimit.setNbMax(restartCutoff);
	}

	@Override
	public final void reset() {
		updateTimeCount();
		//reset the restart limit to allow diversification
		//I notice that solutions appear sometimes in cluster, at least for shop-scheduling.
		//should it be optional ?
		restartStrategyLimit.setNbMax( restartStrategyLimit.getNb() + restartCutoff);
	}



	@Override
	public void endTreeSearch() {
		TimeCacheThread.currentTimeMillis=System.currentTimeMillis();
		updateTimeCount();
	}

	@Override
	public final void newNode() throws ContradictionException {
		updateTimeCount();
		if( searchLimit.getNb() >= searchLimit.getNbMax()) {
			//end search
			searchStrategy.setEncounteredLimit(searchLimit);
			searchStrategy.solver.getPropagationEngine().raiseContradiction(searchLimit, STOP);
		}
		if( restartStrategyLimit.getNb() >= restartStrategyLimit.getNbMax()) {
			//update cutoff
			restartFromStrategyCount++;
			restartCutoff = restartStrategy.getNextCutoff(restartFromStrategyCount);
			restartStrategyLimit.setNbMax( restartStrategyLimit.getNb() + restartCutoff);
			//perform restart
			searchStrategy.solver.getPropagationEngine().raiseContradiction(searchLimit, RESTART);
		}
	}

	@Override
	public final void endNode() throws ContradictionException {
		updateTimeCount();
		if( searchLimit.getNb() >= searchLimit.getNbMax()) {
			//end search
			searchStrategy.setEncounteredLimit(searchLimit);
			searchStrategy.solver.getPropagationEngine().raiseContradiction(searchLimit, STOP);
		}
		//do not restart while backtraking.
		//side effects with nogood recording
		//can also miss the end of the search
	}

	@Override
	public final boolean newRestart() {
		return restartLimit.getNb() >= restartLimit.getNbMax();
	}


	public final void cancelRestartStrategy() {
		//restartLimit = NoLimit.SINGLOTON;
		restartStrategyLimit = NoLimit.SINGLOTON;
		//restartStrategy = NoRestartStrategy.SINGLOTON;
	}


	@Override
	public String toString() {
		if( searchLimit != null && searchLimit.getUnit() != NoLimit.NO_LIMIT_UNIT) {
			return searchLimit.pretty();
		}
		return "";
	}

	@Override
	public String pretty() {
		if( restartLimit!= null && restartLimit.getUnit() != NoLimit.NO_LIMIT_UNIT) {
			return toString()+ ",  restart: "+restartLimit.pretty();
		}
		return toString();
	}

}






