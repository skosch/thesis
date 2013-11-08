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

/**
 * 
 */
package choco.cp.model.managers.constraints.integer;

import choco.Options;
import choco.cp.model.managers.MixedConstraintManager;
import choco.cp.solver.CPSolver;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;

import java.util.List;

/**
 * @author Arnaud Malapert</br> 
 * @since 28 janv. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public final class MetaTaskConstraintManager extends MixedConstraintManager {

	/**
	 * @see choco.kernel.model.constraints.ConstraintManager#makeConstraint(choco.kernel.solver.Solver,V[], Object,java.util.List
	 */
	@Override
	public SConstraint makeConstraint(Solver solver, Variable[] variables,
			Object parameters, List<String> options) {
		  if (solver instanceof CPSolver) {
			  if (parameters instanceof Constraint) {
				final Constraint ic = (Constraint) parameters;
                boolean decomp = false;
                if (ic.getOptions().contains(Options.E_DECOMP)) {
                    decomp = true;
                }
                return ( (CPSolver) solver).makeSConstraint(ic, decomp);
			}
		  }
		  throw new ModelException("Could not found a constraint manager in " + this.getClass() + " !");
	}

	@Override
	public int[] getFavoriteDomains(List<String> options) {
		//because we are dealing with tasks
		return getBCFavoriteIntDomains();
	}

	
	

}
