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

package choco.cp.solver.configure;

import static choco.kernel.solver.Configuration.RESTART_LIMIT;
import static choco.kernel.solver.Configuration.RESTART_LIMIT_BOUND;
import static choco.kernel.solver.Configuration.SEARCH_LIMIT;
import static choco.kernel.solver.Configuration.SEARCH_LIMIT_BOUND;
import choco.cp.solver.search.SearchLimitManager;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import choco.kernel.solver.search.limit.AbstractGlobalSearchLimit;
import choco.kernel.solver.search.limit.BackTrackLimit;
import choco.kernel.solver.search.limit.FailLimit;
import choco.kernel.solver.search.limit.Limit;
import choco.kernel.solver.search.limit.NodeLimit;
import choco.kernel.solver.search.limit.RestartLimit;
import choco.kernel.solver.search.limit.SolutionLimit;
import choco.kernel.solver.search.limit.TimeLimit;

/**
 * @author Arnaud Malapert</br> 
 * @since 27 juil. 2009 version 2.1.1</br>
 * @version 2.1.1</br>
 */
public final class LimitFactory {



	private LimitFactory() {
		super();
	}


	public final static void setSearchLimit(Configuration conf, Limit type, int limitBound) {
		conf.putEnum(SEARCH_LIMIT, type);
		conf.putInt(SEARCH_LIMIT_BOUND, limitBound);
	}

	public final static void setSearchLimit(Solver solver, Limit type, int limitBound) {
		setSearchLimit(solver.getConfiguration(), type, limitBound);
	}

	public final static AbstractGlobalSearchLimit makeSearchLimit(AbstractGlobalSearchStrategy strategy) {
		final Configuration conf = strategy.solver.getConfiguration();
		Limit lim = conf.readEnum(SEARCH_LIMIT, Limit.class);
		return makeLimit(strategy, lim, conf.readInt(SEARCH_LIMIT_BOUND));
	}

	public final static void setRestartLimit(Configuration conf, Limit type, int limitBound) {
		conf.putEnum(RESTART_LIMIT, type);
		conf.putInt(RESTART_LIMIT_BOUND, limitBound);
	}
	
	public final static void setRestartLimit(Solver solver, Limit type, int limitBound) {
		setRestartLimit(solver.getConfiguration(), type, limitBound);
	}

	public final static AbstractGlobalSearchLimit makeRestartLimit(AbstractGlobalSearchStrategy strategy) {
		final Configuration conf = strategy.solver.getConfiguration();
		Limit lim = conf.readEnum(RESTART_LIMIT, Limit.class);
		return makeLimit(strategy, lim, conf.readInt(RESTART_LIMIT_BOUND));
	}


	public final static AbstractGlobalSearchLimit createLimit(AbstractGlobalSearchStrategy strategy, Limit type, int theLimit) {
		switch (type) {
		case TIME: return new TimeLimit(strategy, theLimit);
		case NODE: return new NodeLimit(strategy, theLimit);
		case BACKTRACK: return new BackTrackLimit(strategy, theLimit);
		case RESTART: return new RestartLimit(strategy, theLimit);
		case FAIL: {
			strategy.solver.monitorFailLimit(true);
			return new FailLimit(strategy, theLimit);
		}
		case SOLUTION : return new SolutionLimit(strategy, theLimit);
		default: 
			return null;
		}
	}

	private static Limit getPolicyLimit(Solver solver) {
		return solver.getConfiguration().readEnum(Configuration.RESTART_POLICY_LIMIT, Limit.class);
	}

	public final static SearchLimitManager createLimitManager(AbstractGlobalSearchStrategy strategy) {
		final SearchLimitManager limitManager = new SearchLimitManager(strategy);
		limitManager.setSearchLimit(makeSearchLimit(strategy)); //controlling the search
		limitManager.setRestartLimit(makeRestartLimit(strategy)); //controlling the restart
		//controlling the restart strategy
		limitManager.setRestartStrategy(
				RestartFactory.createRestartStrategy(strategy.solver),
				LimitFactory.createLimit(strategy, getPolicyLimit(strategy.solver), Integer.MAX_VALUE)
		);
		return limitManager;
	}
	
	public final static AbstractGlobalSearchLimit makeLimit(AbstractGlobalSearchStrategy strategy, Limit type, int theLimit) {
		if( strategy != null && type !=null && theLimit != Integer.MAX_VALUE) {
			return createLimit(strategy, type, theLimit);
		} else {return null;}
	}


}
