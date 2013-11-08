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

package choco.kernel.model.variables.real;

import choco.kernel.model.IConstraintList;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.VariableType;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 17 mars 2008
 * Since : Choco 2.0.0
 *
 */
public class RealVariable extends RealExpressionVariable {

	
	protected RealVariable(VariableType variableType, boolean enableOption,
			IConstraintList constraints,double lowB, double uppB) {
		super(variableType, enableOption, new double[]{lowB, uppB}, constraints);
		this.lowB = lowB;
		this.uppB = uppB;
		setVariables(this);
	}


	public RealVariable(String name, double lowB, double uppB) {
        //noinspection NullArgumentToVariableArgMethod
        this(VariableType.REAL, true, new ConstraintsDataStructure(), lowB, uppB);
		this.setName(name);
	}



    /**
     * pretty printing of the object. This String is not constant and may depend on the context.
     *
     * @return a readable string representation of the object
     */
    @Override
    public String pretty() {
        return name+" ["+getLowB()+", "+getUppB()+"]";
    }

   
    
    /**
     * Extract first level sub-variables of a variable
     * and return an array of non redundant sub-variable.
     * In simple variable case, return a an array
     * with just one element.
     * Really usefull when expression variables.
     * @return a hashset of every sub variables contained in the Variable.
     */
    @Override
     public Variable[] doExtractVariables() {
        return getVariables();
    }
}
