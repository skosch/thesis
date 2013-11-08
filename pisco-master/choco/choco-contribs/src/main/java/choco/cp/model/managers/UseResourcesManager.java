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

package choco.cp.model.managers;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.*;
import choco.kernel.common.Constant;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintManager;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.TaskVar;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


public class UseResourcesManager extends ConstraintManager<Variable> {


	@Override
	public SConstraint makeConstraint(Solver solver,
			Variable[] variables, Object parameters, List<String> options) {
		if (solver instanceof CPSolver) {
			if(variables.length == 1 && variables[0] instanceof TaskVariable) {
				//check task
				final TaskVar task = solver.getVar(variables[0]);
				if (parameters instanceof Object[]) {
					final Object[] params = (Object[]) parameters;
					if(params.length > 2) {
						if (params[0] instanceof Constraint[] &&
								params[1] instanceof Integer &&
								params[2] instanceof Boolean ) {
							//check parameters
							final Constraint[] resources = (Constraint[]) params[0];
							//check number of resources
							final List<IRTask> rtaskL = new ArrayList<IRTask>();
							int k = (Integer) params[1];
							final boolean equal = (Boolean) params[2];
							for (int i = 0; i < resources.length; i++) {
								final SConstraint c = solver.getCstr(resources[i]);
								if ( c != null && c instanceof AbstractResourceSConstraint) {
									final AbstractResourceSConstraint rsc = (AbstractResourceSConstraint) c;
									final int idx = rsc.indexOf(task);
									if( idx >= 0) {
										final IRTask rt = rsc.getRTask(idx);
										if(rt.isRegular()) k--;
										else if(rt.isOptional()){
											rtaskL.add( rt);
										}
									}
								}
							}
							if(k <= 0) return Constant.TRUE;
							else if (rtaskL.size() < k) return Constant.FALSE;
							else {
								final IntDomainVar[] uvars = new IntDomainVar[rtaskL.size()];
								final IRTask[] rtasks = new IRTask[rtaskL.size()];
								final ListIterator<IRTask> iter = rtaskL.listIterator();
								while(iter.hasNext()) {
									final int idx = iter.nextIndex();
									final IRTask rt = iter.next();
									rtasks[idx] = rt;
									uvars[idx] = rt.getUsage();
								}
								AbstractUseResourcesSConstraint cstr = equal ? 
										new UseResourcesEq(solver.getEnvironment(), task, k, uvars, rtasks) :
											new UseResourcesGeq(solver.getEnvironment(), task, k, uvars, rtasks);
								solver.post( new TempTaskConstraintWrapper(task,cstr));	
								return cstr;
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public int[] getFavoriteDomains(List<String> options) {
		return getBCFavoriteIntDomains();
	}

	@Override
	public SConstraint[] makeConstraintAndOpposite(Solver solver,
			Variable[] variables, Object parameters, List<String> options) {
		// TODO - should return the opposite sum - created 4 juil. 2011 by Arnaud Malapert
		return null;
	}

	@Override
	public INode makeNode(Solver solver, Constraint[] cstrs, Variable[] vars) {
		return null;
	}





}
