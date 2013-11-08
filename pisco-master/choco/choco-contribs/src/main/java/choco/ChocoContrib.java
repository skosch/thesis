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

package choco;

import java.util.logging.Level;
import java.util.logging.Logger;

import choco.cp.model.managers.UseResourcesManager;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.variables.scheduling.TaskVariable;

public final class ChocoContrib {

	protected final static Logger LOGGER = ChocoLogging.getEngineLogger();
	
	private ChocoContrib() {}
	
	public static Constraint useResourceGeq(TaskVariable task, Constraint... resources) {
		return useResourcesGeq(task, 1, resources);
	}
	
	public static Constraint useResourcesGeq(TaskVariable task, int k, Constraint... resources) {
		return useResources(task, k, false, resources);
	}
	
	public static Constraint useResource(TaskVariable task, Constraint... resources) {
		return useResources(task, 1, resources);
	}
	
	public static Constraint useResources(TaskVariable task, int k, Constraint... resources) {
		return useResources(task, k, true, resources);
	}

	/**
	 * Transversal Constraint among resources that defines the usage of a task: U_1 + ... + U_n ( == or >= ) k.
	 * U_i is the usage variable of the task in the i-th resource.
	 * If Hypothetical domains are enbaled, then additional filtering algorithms are used.
	 * @param k minimal number of resources where the task is regular.
	 * @param eqOrGeq if <code>true</code> equality constraint, otherwise greaterThan constraint.
	 * @param resources if null the requirement concerns all resources (not implemented).
	 * @return
	 */
	private static Constraint useResources(TaskVariable task, int k, boolean eqOrGeq,Constraint[] resources) {
		final int nbR = resources == null ? 0 : resources.length;
		if( k > nbR) {
			LOGGER.log(Level.WARNING, "{0} use {1} resources among {2} resources", new Object[]{task,k,nbR});
			return Choco.FALSE;
		}else if(k >= 0){
			if( k == 0) LOGGER.log(Level.WARNING, "{0} use {1} resources among {2} resources", new Object[]{task,k,nbR});
			if( resources == null || resources.length == 0) throw new ModelException("not yet implemented");
			return new ComponentConstraint(UseResourcesManager.class, new Object[] {resources, k, eqOrGeq}, new TaskVariable[]{task});
		} else throw new ModelException( task+ " uses "+k+" resources");
		
	}
	

}
