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

package choco.kernel.model.variables.integer;

import choco.kernel.model.variables.VariableType;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 17 mars 2008
 * Since : Choco 2.0.0
 *
 */
public final class IntegerConstantVariable extends IntegerVariable{

	public IntegerConstantVariable(final int value) {
        super(VariableType.CONSTANT_INTEGER, new int[]{value},false, NO_CONSTRAINTS_DS);
        setName(Integer.toString(value));
        this.values = new int[]{value};
    }

    public int getValue() {
        return values[0];
    }

	@Override
	public void setLowB(final int lowB) {
		throwConstantException();
	}

	@Override
	public void setUppB(final int uppB) {
		throwConstantException();
	}


	@Override
	public boolean equals(final Object o) {
        return o instanceof IntegerConstantVariable && getValue() == ((IntegerConstantVariable) o).getValue();
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
	public int compareTo(final Object o) {
        if(o instanceof IntegerConstantVariable){
		    final IntegerConstantVariable c = (IntegerConstantVariable)o;
            return getValue() - c.getValue();
        }else return super.compareTo(o);
	}
}
