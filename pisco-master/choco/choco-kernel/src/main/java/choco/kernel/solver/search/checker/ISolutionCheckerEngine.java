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

package choco.kernel.solver.search.checker;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.Var;

import java.util.logging.Logger;

public interface ISolutionCheckerEngine {

	public final static Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * Check the current solution of the {@code solver}.
     * It runs over variables (check instantiation) and constraints (call isSatisfied).
     * By defautlt, it checks the consistency and ignore the nogood recording.
     *
     * @param solver involving solver
     * @throws SolutionCheckerException if the current solution is not correct.
     */
	void checkSolution(Solver solver) throws SolutionCheckerException;

    /**
     * Check instantiation of every variables involved within the {@code solver}.
     * @param solver containing solver
     * @throws SolutionCheckerException if one or more variable is not instantiated.
     */
	void checkVariables(Solver solver) throws SolutionCheckerException;

    /**
     * Check satisfaction of every constraints involved within the {@code solver}.
     * @param solver containing solver
     * @throws SolutionCheckerException if one or more constraint is not satisfied.
     */
	void checkConstraints(Solver solver) throws SolutionCheckerException;

    /**
     * Check the instantiation of {@code var}.
     * @param var variable to check
     * @throws SolutionCheckerException if {@code var} is not instantiated.
     */
	void checkVariable(Var var) throws SolutionCheckerException;

    /**
     * Check the satisfaction of {@code c}.
     * @param c constraint to check
     * @throws SolutionCheckerException if {@code c} is not satisfied
     */
	void checkConstraint(SConstraint<?> c) throws SolutionCheckerException;

    /**
     * Inspect the current solution of {@code solver}.
     * It runs over variables (check instantiation) and constraints (call isSatisfied).
     * By defautlt, it checks the consistency and ignore the nogood recording.
     * @param solver involving solver
     * @return false if the current solution is not correct
     */
	boolean inspectSolution(Solver solver);

    /**
     * Inspect instantiation of every variables involved in {@code solver}.
     * @param solver containing solver.
     * @return false if one or more variable is not instantiated.
     */
	boolean inspectVariables(Solver solver);

    /**
     * Inspect satisfaction of every constraints declared in {@code solver}.
     * @param solver containing solver
     * @return false if one or more constraint is not satisfied.
     */
	boolean inspectConstraints(Solver solver);

    /**
     * Inspect the instantiation of {@code var}.
     * @param var variable to check
     * @return false if the variable is not instantiated.
     */
	boolean inspectVariable(Var var);

    /**
     * Inspect the satisfaction of {@code c}.
     * @param c contraint to check
     * @return false if the constraint is not satisfied.
     */
	boolean inspectConstraint(SConstraint<?> c);


}