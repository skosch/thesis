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

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solution;
import choco.kernel.solver.variables.Var;

public interface IObjectiveManager {

	Var getObjective();
	
	/**
	 * v1.0 accessing the objective value of an optimization model
	 * (note that the objective value may not be instantiated, while all other variables are)
	 *
	 * @return the current objective value
	 */
	Number getObjectiveValue();

	/**
	 * v1.0 accessing the best found objective value of an optimization model
	 * (note that the objective value may not be instantiated, while all other variables are)
	 *
	 * @return the best found objective value
	 */
	Number getBestObjectiveValue();

	/**
	 * the target for the objective function: we are searching for a solution at least as good as this (tentative bound)
	 */
	Number getObjectiveTarget();
	
	/**
	 * Currently best known bound on the optimal solution value of the problem.
	 */
	Number getObjectiveFloor();
	

	void writeObjective(Solution sol);
	
	/**
	 * initialization of the optimization bound data structure
	 */
	void initBounds();

	/**
	 * resetting the optimization bounds
	 */
	void setBound();
	/**
	 * resetting the values of the target bounds (bounds for the remaining search).
	 * @return <code>true</code> if the target bound is indeasible regarding to the objective domain.
	 */
	void setTargetBound();
	
	/**
	 * propagating the optimization cuts from the new target bounds
	 */
	void postTargetBound() throws ContradictionException;
	
	/**
	 * propagating the optimization cuts from the new floor bounds
	 */
	void postFloorBound() throws ContradictionException;
	
	void incrementFloorBound();
	
	void postIncFloorBound() throws ContradictionException;
	
	/**
	 * indicates if the target bound is infeasible, i.e. does not belong to the current objective domain.
	 * @return <code>true</code> if the target bound does not belong to the objective domain, <code>false</code> otherwise.
	 */
	boolean isTargetInfeasible();
}