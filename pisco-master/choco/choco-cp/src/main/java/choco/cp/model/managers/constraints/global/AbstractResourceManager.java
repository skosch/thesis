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

import static choco.kernel.common.util.tools.VariableUtils.getIntVar;
import static choco.kernel.common.util.tools.VariableUtils.getTaskVar;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import choco.Options;
import choco.cp.model.managers.MixedConstraintManager;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.global.MetaSConstraint;
import choco.kernel.solver.constraints.global.scheduling.ResourceParameters;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;


/**
 * @author Arnaud Malapert</br> 
 * @since 27 janv. 2009 version 2.0.0</br>
 * @version 2.0.3</br>
 */
public abstract class AbstractResourceManager extends MixedConstraintManager {


	@Override
	public SConstraint makeConstraint(Solver solver, Variable[] variables,
			Object parameters, List<String> options) {
		if(solver instanceof CPSolver){
			CPSolver s = (CPSolver) solver;
			if (parameters instanceof ResourceParameters) {
				ResourceParameters rdata = (ResourceParameters) parameters;
				return makeConstraint(s, variables, rdata, options);
			}else {
				LOGGER.log(Level.WARNING, "unknown parameter for resource constraint: {0}", parameters);
			}
		}
		return null;
	}

	protected abstract SConstraint makeConstraint(CPSolver solver, Variable[] variables, ResourceParameters rdata, List<String> options);

	protected final IntDomainVar getHorizon(CPSolver s, Variable[] variables, ResourceParameters p) {
		return  p.isHorizonDefined() ? s.getVar((IntegerVariable) variables[variables.length-1]) : s.createMakespan();
	}

	/**
	 * Bounded.
	 * @see choco.kernel.model.constraints.ConstraintManager#getFavoriteDomains(java.util.List
	 */
	@Override
	public int[] getFavoriteDomains(final List<String> options) {
		return getBCFavoriteIntDomains();
	}


}
