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

package choco.cp.model.managers.constraints.global;

import choco.cp.model.managers.MixedConstraintManager;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

import java.util.List;

public abstract class AbstractPrecedenceManager extends MixedConstraintManager {

	
	protected abstract SConstraint makeIntConstraintB0(CPSolver s, IntDomainVar x1, int k1, IntDomainVar x2, int k2);
	
	protected SConstraint makeIntConstraintB1(CPSolver s, IntDomainVar x1, int k1, IntDomainVar x2, int k2) {
		return s.leq( s.plus( x1, k1), x2);
	}
	
	protected abstract SConstraint makeIntConstraint(CPSolver s, IntDomainVar x1, int k1, IntDomainVar x2, int k2, IntDomainVar dir);
	
	
	protected abstract SConstraint makeTaskConstraintB0(CPSolver s, TaskVar t1, int k1, TaskVar t2, int k2);
	
	protected SConstraint makeTaskConstraintB1(CPSolver s, TaskVar t1, int k1, TaskVar t2, int k2) {
		return s.preceding(t1, k1, t2);
	}
	
	protected abstract SConstraint makeTaskConstraint(CPSolver s, TaskVar t1, int k1, TaskVar t2, int k2, IntDomainVar dir);
	
	protected final int getConstantValue( Solver s, Variable var) {
		final IntDomainVar v = s.getVar( (IntegerVariable) var);
		if( v.isInstantiated()) {
			return v.getVal();
		}else {
			throw new SolverException(var+" should be constant");
		}
	}
	@Override
	public final SConstraint makeConstraint(Solver solver, Variable[] variables,
			Object parameters, List<String> options) {
		if(solver instanceof CPSolver){
			final CPSolver s = (CPSolver) solver;
			final int k1 = getConstantValue(s, variables[1]);
			final int k2 = getConstantValue(s, variables[3]);
			final IntDomainVar dir = solver.getVar((IntegerVariable)variables[4]);
			if(parameters == Boolean.TRUE) {
				//precedence between task variables
				final TaskVar t1 = solver.getVar((TaskVariable) variables[0]);
				final TaskVar t2 = solver.getVar((TaskVariable) variables[2]);
				if (dir.isInstantiatedTo(0)) return makeTaskConstraintB0(s, t1, k1, t2, k2);
				else if(dir.isInstantiatedTo(1)) return makeTaskConstraintB1(s, t1, k1, t2, k2);
				else return makeTaskConstraint(s, t1, k1, t2, k2, dir);
			} else if(parameters == Boolean.FALSE) {
				//precedence between integer variables
				final IntDomainVar x1 = solver.getVar((IntegerVariable) variables[0]);
				final IntDomainVar x2 = solver.getVar((IntegerVariable) variables[2]);
				
				if (dir.isInstantiatedTo(0)) return makeIntConstraintB0(s, x1, k1, x2, k2);
				else if(dir.isInstantiatedTo(1)) return makeIntConstraintB1(s, x1, k1, x2, k2);
				else return makeIntConstraint(s, x1, k1, x2, k2, dir);
			} else {
				LOGGER.severe("unknown constraint parameters: "+parameters);
			}
		}
		return fail("Precedence (Implied|Reified|Disjoint)");
	}

	@Override
	public int[] getFavoriteDomains(List<String> options) {
		return getBCFavoriteIntDomains();
	}

	
	
}
