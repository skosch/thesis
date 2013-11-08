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

import java.util.List;
import java.util.logging.Logger;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.Solution;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.measure.ISolutionMeasures;


/**
 * An abstract class handling the control for solving a model
 */
public abstract class AbstractSearchStrategy implements ISolutionMeasures {

	/**
	 * an object for logging trace statements
	 */
	protected final static Logger LOGGER = ChocoLogging.getSearchLogger();

	/**
	 * The (optimization or decision) model to which the entity belongs.
	 */

	public final Solver solver;


	protected ISolutionPool solutionPool;
	
	protected ISolutionMonitor solutionMonitor;
	
	/**
	 * count of the solutions found during search
	 */
	protected int nbSolutions = 0;


	public AbstractSearchStrategy(Solver solver) {
		super();
		this.solver = solver;
	}

	/**
	 * Retrieves the solver of the entity
	 */

	public Solver getSolver() {
		return solver;
	}

	@Override
	public final boolean existsSolution() {
		return nbSolutions > 0;
	}

	@Override
	public final int getSolutionCount() {
		return nbSolutions;
	}


	public final ISolutionPool getSolutionPool() {
		return solutionPool;
	}

	/**
	 * a null argument cancel the solution recording.
	 */
	public final void setSolutionPool(ISolutionPool solutionPool) {
		if(solutionPool == null) {
			this.solutionPool = NoSolutionPool.SINGLETON;
		} else this.solutionPool = solutionPool;
	}


	/**
	 * a null argument cancel the solution monitoring.
	 */
	public final void setSolutionMonitor(ISolutionMonitor solutionMonitor) {
		if(solutionMonitor == null) {
			this.solutionMonitor= ISolutionMonitor.NO_MONITORING;
		} else this.solutionMonitor = solutionMonitor;
	}

	public final void resetSolutions() {
		solutionPool.clear();
		nbSolutions = 0;
		solver.setFeasible(null);
	}


	/**
	 * recording the current state as a solution
	 * stores information from the current state in the next solution of the model
	 * note: only instantiated variables are recorded in the Solution object
	 * either all variables or a user-defined subset of them are recorded
	 * this may erase a soolution that was previously stored in the ith position
	 * this may also increase the size of the pb.solutions vector.
	 */
	public void recordSolution() {
		solver.setFeasible(Boolean.TRUE);
		nbSolutions++;
		solutionPool.recordSolution(solver);
		solutionMonitor.recordSolution(solver);
	}


	public void writeSolution(Solution sol) {
		sol.setSolver(solver);
		sol.recordSolutionCount(nbSolutions);
		//record values
		sol.recordIntValues();
		sol.recordSetValues();
		sol.recordRealValues();
	}


	public void restoreBestSolution() {
		solver.restoreSolution(solutionPool.getBestSolution());
	}

	public final List<Solution> getStoredSolutions(){
		return solutionPool.asList();
	}

	
}
