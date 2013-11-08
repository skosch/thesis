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
import choco.cp.solver.constraints.global.pack.PackSConstraint;
import choco.kernel.common.util.bitmask.BitMask;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;

import java.util.List;

import static choco.kernel.common.util.tools.VariableUtils.getIntVar;
import static choco.kernel.common.util.tools.VariableUtils.getSetVar;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 8 août 2008
 * Time: 20:08:04
 */
public final class PackManager extends MixedConstraintManager {

	/**
	 * Build a constraint for the given solver and "model variables"
	 *
	 * @param solver
	 * @param variables
	 * @param parameters : a "hook" to attach any kind of parameters to constraints
	 * @param options
	 * @return
	 */
	@Override
	public SConstraint makeConstraint(Solver solver, Variable[] variables, Object parameters, List<String> options) {
		if(solver instanceof CPSolver){
			CPSolver s = (CPSolver) solver;
			if(parameters instanceof Object[]){
				Object[] params = (Object[])parameters;
				final int n = (Integer) params[0]; //nb items
				final int m = (Integer) params[1]; //nb bins
				final int v1 = 2 * m;
				final int v2 = v1 + n;
				final int v3 = v2 + n;
				final SetVar[] itemSets = getSetVar(s, variables, 0, m);
				final IntDomainVar[] loads = getIntVar(s, variables, m, v1);
				final IntDomainVar[] bins = getIntVar(s, variables, v1, v2);
				final IntDomainVar[] sizes = getIntVar(s, variables, v2, v3);
				final IntDomainVar  nbNonEmpty = solver.getVar((IntegerVariable)variables[v3]);
				final PackSConstraint ct = new PackSConstraint(s.getEnvironment(), itemSets, loads, sizes, bins, nbNonEmpty);
				ct.readOptions(options);	
				return ct;
			}
		}
		return fail("pack");
	}

	@Override
	public int[] getFavoriteDomains(final List<String> options) {
		return getBCFavoriteIntDomains();
	}

}
