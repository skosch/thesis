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

import choco.kernel.solver.Configuration;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solution;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.Var;




public abstract class AbstractOptimize extends AbstractGlobalSearchStrategy {
	/**
	 * a boolean indicating whether we want to maximize (true) or minimize (false) the objective variable
	 */
	public final boolean doMaximize;

	/**
	 * the objective variable
	 */
	public final Var objective;

	/**
	 * the bounding object, record objective value and compute target bound.
	 */
	protected final IObjectiveManager objManager;

	/**
	 * constructor
	 * @param solver the solver
     * @param maximize maximization or minimization ?
     * @param configuration
     */
	protected AbstractOptimize(Solver solver, IObjectiveManager bounds, boolean maximize) {
		super(solver);
		this.objManager = bounds;
		objective = bounds.getObjective();
		doMaximize = maximize;
	}


	@Override
	public final IObjectiveManager getObjectiveManager() {
		return objManager;
	}

	@Override
	public void newFeasibleRootState() {
		super.newFeasibleRootState();
		objManager.initBounds();
	}


	@Override
	public void writeSolution(Solution sol) {
		super.writeSolution(sol);
		objManager.writeObjective(sol);
	}

	@Override
	public void recordSolution() {
		super.recordSolution();
		objManager.setBound();
		objManager.setTargetBound();
	}


	/**
	 * we use  targetBound data structures for the optimization cuts
	 */
	@Override
	public void postDynamicCut() throws ContradictionException {
		objManager.postTargetBound();
	}



	@Override
	protected void advancedInitialPropagation() throws ContradictionException {
		if(solver.getConfiguration().readBoolean(Configuration.INIT_DESTRUCTIVE_LOWER_BOUND) 
				|| solver.getConfiguration().readBoolean(Configuration.BOTTOM_UP) ) {
			shavingTools.destructiveLowerBound(objManager);
		}
		super.advancedInitialPropagation();
	}

	@Override
	public Boolean nextSolution() {
		if( objManager.isTargetInfeasible()) {
			//the search is finished as the optimum has been proven by the bounding mechanism.
			return Boolean.FALSE;
		}else {
			//otherwise, continue the search.
			return super.nextSolution();
		}
	}


	protected final void bottomUpSearch() {
		// FIXME - Not compatible with restarts - created 5 juil. 2011 by Arnaud Malapert
		final int oldBaseWorld = baseWorld;
		baseWorld = solver.getWorldIndex();
		solver.worldPush();
		while( shavingTools.nextBottomUp(objManager) == Boolean.FALSE) {
			//The current upper bound is infeasible, try next
			objManager.incrementFloorBound();
			if(objManager.isTargetInfeasible() ) return; //problem is infeasible
			else {
				//partially initialize a new search tree
				clearTrace();
				solver.worldPopUntil(baseWorld);
				solver.worldPush();
				nextMove = INIT_SEARCH;
			} 
		}
		baseWorld = oldBaseWorld;
	}

	@Override
	public void incrementalRun() {
		initialPropagation();
		if(isFeasibleRootState()) {
			assert(solver.getWorldIndex() > baseWorld);
			if( solver.getConfiguration().readBoolean(Configuration.BOTTOM_UP) ) bottomUpSearch();
			else topDownSearch();
		}
		endTreeSearch();
	}


	@Override
	public String partialRuntimeStatistics(boolean logOnSolution) {
		if( logOnSolution) {
			return "Objective: "+objManager.getObjectiveValue()+", "+super.partialRuntimeStatistics(logOnSolution);
		}else {
			return "Upper-bound: "+objManager.getBestObjectiveValue()+", "+super.partialRuntimeStatistics(logOnSolution);
		}

	}


	@Override
	public String runtimeStatistics() {
		return "  "+ (doMaximize ? "Maximize: " : "Minimize: ") + objective + '\n' +super.runtimeStatistics();
	}


	@Override
	public void restoreBestSolution() {
		super.restoreBestSolution();
		if( ! objManager.getBestObjectiveValue().equals(objManager.getObjectiveValue())) {
			throw new SolverException("Illegal state: the best objective "+objManager.getBestObjectiveValue()+" is not equal to the best solution objective "+objManager.getObjectiveValue());
		}
	}




}
