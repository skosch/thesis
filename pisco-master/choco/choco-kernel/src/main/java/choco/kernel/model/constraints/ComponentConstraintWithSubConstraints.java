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

package choco.kernel.model.constraints;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.variables.Variable;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 20 janv. 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class ComponentConstraintWithSubConstraints extends ComponentConstraint {

    private final List<Constraint> constraints;
   
    public ComponentConstraintWithSubConstraints(ConstraintType constraintType, Variable[] variables,
                                                 Object params, Constraint... constraints) {
        super(constraintType, params, variables);
        this.constraints = new LinkedList<Constraint>(ArrayUtils.toList(constraints));
    }

    public ComponentConstraintWithSubConstraints(String componentClassName, Variable[] variables,
                                                 Object params, Constraint... constraints) {
        super(componentClassName, appendParameters(params, constraints), variables);
        this.constraints = new LinkedList<Constraint>(ArrayUtils.toList(constraints));
    }

    public ComponentConstraintWithSubConstraints(Class componentClass, Variable[] variables,
                                                 Object params, Constraint... constraints) {
        super(componentClass, appendParameters(params, constraints), variables);
        this.constraints = new LinkedList<Constraint>(ArrayUtils.toList(constraints));
    }


    public void addElements(Variable[] vars, Constraint... cstrs){
    	Variable[] currentV = getVariables();
    	List<Variable> newV = new LinkedList<Variable>();
    	for (Variable var : vars) {
            	if( ! ArrayUtils.contains(currentV, var))  newV.add(var);
    	}
    	if( ! newV.isEmpty() ) setVariables(ArrayUtils.append(currentV, newV.toArray(new Variable[newV.size()])));
    	constraints.addAll(ArrayUtils.toList(cstrs));
    }

  
    @Override
    public Object getParameters() {
        return appendParameters(parameters, ArrayUtils.toArray(Constraint.class, constraints));
    }

    private static Object appendParameters(Object parameters, Constraint... constraints){
        return new Object[]{parameters, constraints};
    }


    @Override
	public final void findManager(Properties propertiesFile) {
        super.findManager(propertiesFile);
        for (Constraint constraint : constraints) {
            constraint.findManager(propertiesFile);
        }
    }


    /**
     * Extract variables of a constraint
     * and return an array of variables.
     * @return an array of every variables contained in the Constraint.
     */
    @Override
	public Variable[] doExtractVariables() {
        Variable[] listVars = this.getVariables();
        for (Constraint c : constraints) {
            listVars = ArrayUtils.append(listVars, c.extractVariables());
        }
        return ArrayUtils.getNonRedundantObjects(Variable.class, listVars);
    }
}
