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

package choco.kernel.model.variables.set;

import java.util.Arrays;

import choco.kernel.model.ModelException;
import choco.kernel.model.variables.VariableType;
import choco.kernel.model.variables.integer.IntegerConstantVariable;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 17 mars 2008
 * Since : Choco 2.0.0
 *
 */
public class SetConstantVariable extends SetVariable {


	public SetConstantVariable(IntegerConstantVariable card, int... values) {
        super(VariableType.CONSTANT_SET, false, values, NO_CONSTRAINTS_DS, card);
        this.setName(Arrays.toString(values));
        this.values = values;
    }

    public int[] getValues() {
        return values;
    }

    public int getLowB() {
        if(values.length>0)return values[0];
        throw new ModelException("Cannot access lower bound of an empty set");
    }

    public int getUppB() {
        if(values.length>0)return values[values.length-1];
        throw new ModelException("Cannot access lower bound of an empty set");
    }
    
	@Override
	public void setLowB(int lowB) {
		throwConstantException();
	}

	@Override
	public void setUppB(int uppB) {
		throwConstantException();
	}

    /**
     * pretty printing of the object. This String is not constant and may depend on the context.
     *
     * @return a readable string representation of the object
     */
    @Override
    public String pretty() {
        return name;
    }


    @Override
	public boolean equals(Object o) {
        if(o instanceof SetConstantVariable){
            return values == ((SetConstantVariable) o).getValues();
        }else{
            return false;
        }
    }
}
