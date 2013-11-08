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
package choco.kernel.model.constraints;

import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.scheduling.TaskVariable;

import java.util.Properties;

/**
 * An wrapper for constraint involving some Taskvariable.
 * It contains additional variables (tasks) added to the model.
 * For example, if you have the constraint (T1 precedes T2) then T1 and T2 should be added to the model with the constraint end(T1) <= start(T2). 
 * @author Arnaud Malapert</br> 
 * @since 28 janv. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public class MetaTaskConstraint extends ComponentConstraint {
	// TODO - Suppress class ? - created 5 juil. 2011 by Arnaud Malapert
	
	private static final long serialVersionUID = 5309541509214967423L;

	protected Constraint constraint;

	public MetaTaskConstraint(TaskVariable[] taskvariables,
			Constraint constraint) {
		super(ConstraintType.METATASKCONSTRAINT,constraint,taskvariables);
		this.constraint = constraint;
	}

    /**
     * Extract variables of a constraint
     * and return an array of variables.
     * @return an array of every variables contained in the Constraint.
     */
	@Override
	public Variable[] doExtractVariables() {
		Variable[] listVars = super.doExtractVariables();
		listVars = ArrayUtils.append(listVars, constraint.extractVariables());
		return ArrayUtils.getNonRedundantObjects(Variable.class, listVars);
	}

	@Override
	public void findManager(Properties propertiesFile) {
		super.findManager(propertiesFile);
		constraint.findManager(propertiesFile);
	}


	@Override
	public int[] getFavoriteDomains() {
		return constraint.getFavoriteDomains();
	}
	
	
}
