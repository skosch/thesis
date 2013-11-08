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

import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.real.RealVar;
import choco.kernel.solver.variables.set.SetVar;

import java.util.logging.Level;

public abstract class AbstractSolutionCheckerEngine implements ISolutionCheckerEngine {

    /**
     * Check satisfaction of every constraints involved within the {@code solver}.
     * @param solver containing solver
     * @throws SolutionCheckerException if one or more constraint is not satisfied.
     */
	@Override
	public final void checkConstraints(Solver solver) throws SolutionCheckerException {
		final DisposableIterator<SConstraint> ctit = solver.getConstraintIterator();
		try{
            while (ctit.hasNext()) {
                checkConstraint(ctit.next());
            }
        }finally {
            ctit.dispose();
        }

	}

     /**
     * Check the current solution of the {@code solver}.
     * It runs over variables (check instantiation) and constraints (call isSatisfied).
     * By defautlt, it checks the consistency and ignore the nogood recording.
     *
     * @param solver involving solver
     * @throws SolutionCheckerException if the current solution is not correct.
     */
	@Override
	public void checkSolution(Solver solver) throws SolutionCheckerException {
		checkVariables(solver);
		checkConstraints(solver);		
	}

    /**
     * Check instantiation of every variables involved within the {@code solver}.
     * @param solver containing solver
     * @throws SolutionCheckerException if one or more variable is not instantiated.
     */
	@Override
	public final void checkVariables(Solver solver) throws SolutionCheckerException {
		final DisposableIterator<IntDomainVar> ivIter = solver.getIntVarIterator();
		while(ivIter.hasNext()) {
			checkVariable(ivIter.next());
		}
        ivIter.dispose();
		final DisposableIterator<SetVar> svIter = solver.getSetVarIterator();
		while(svIter.hasNext()) {
			checkVariable(svIter.next());
		}
        svIter.dispose();
		final DisposableIterator<RealVar> rvIter = solver.getRealVarIterator();
		while(rvIter.hasNext()) {
			checkVariable(rvIter.next());
		}
        rvIter.dispose();
	}

    /**
     * Inspect satisfaction of every constraints declared in {@code solver}.
     * @param solver containing solver
     * @return false if one or more constraint is not satisfied.
     */
	@Override
	public final boolean inspectConstraints(Solver solver) {
		boolean isOk = true;
		DisposableIterator<SConstraint> ctit =  solver.getConstraintIterator();
		while (ctit.hasNext()) {
			isOk &= inspectConstraint(ctit.next());
		}
        ctit.dispose();
		return isOk;
	}

    /**
     * Inspect the current solution of {@code solver}.
     * It runs over variables (check instantiation) and constraints (call isSatisfied).
     * By defautlt, it checks the consistency and ignore the nogood recording.
     * @param solver involving solver
     * @return false if the current solution is not correct
     */
	@Override
	public boolean inspectSolution(Solver solver) {
		LOGGER.log(Level.CONFIG, "- Check solution: {0}", this.getClass().getSimpleName());
		boolean isOk = true;
		if ( inspectVariables(solver) ) LOGGER.config("- Check solution: Every variable is instantiated.");
		else {
			isOk = false;
			LOGGER.severe("- Check solution: Some variables are not instantiated.");
		}
		if(inspectConstraints(solver)) LOGGER.config("- Check solution: Every constraint is satisfied.");
		else {
			isOk= false;
			LOGGER.severe("- Check solution: Some constraints are not satisfied.");
		}
		return isOk;
	}

    /**
     * Inspect instantiation of every variables involved in {@code solver}.
     * @param solver containing solver.
     * @return false if one or more variable is not instantiated.
     */
	@Override
	public final boolean inspectVariables(Solver solver) {
		boolean isOk = true;
		final DisposableIterator<IntDomainVar> ivIter = solver.getIntVarIterator();
		while(ivIter.hasNext()) {
			isOk &= inspectVariable(ivIter.next());
		}
        ivIter.dispose();
		final DisposableIterator<SetVar> svIter = solver.getSetVarIterator();
		while(svIter.hasNext()) {
			isOk &= inspectVariable(svIter.next());
		}
        svIter.dispose();
		final DisposableIterator<RealVar> rvIter = solver.getRealVarIterator();
		while(rvIter.hasNext()) {
			isOk &= inspectVariable(rvIter.next());
		}
        rvIter.dispose();
		return isOk;
	}


}
