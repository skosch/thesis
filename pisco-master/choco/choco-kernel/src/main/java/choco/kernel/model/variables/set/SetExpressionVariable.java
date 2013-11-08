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

import choco.kernel.model.IConstraintList;
import choco.kernel.model.variables.ComponentVariable;
import choco.kernel.model.variables.IntBoundedVariable;
import choco.kernel.model.variables.Operator;
import choco.kernel.model.variables.VariableType;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 18 mars 2008
 * Since : Choco 2.0.0
 *
 */
public class SetExpressionVariable extends ComponentVariable implements IntBoundedVariable {


    protected int lowB;
    protected int uppB;

    protected SetExpressionVariable(VariableType variableType,
			boolean enableOption, Object parameters, IConstraintList constraints) {
		super(variableType, enableOption, parameters, constraints);
    }

	public SetExpressionVariable(Object parameters, Operator operator, SetExpressionVariable... variables) {
        super(VariableType.SET_EXPRESSION, operator, parameters, variables);
    }
   

    public final SetExpressionVariable getExpressionVariable(int i) {
    	return (SetExpressionVariable) getVariable(i);
	}

    public int getLowB() {
        return lowB;
    }

    public void setLowB(int lowB) {
        this.lowB = lowB;
    }

    public int getUppB() {
        return uppB;
    }

    public void setUppB(int uppB) {
        this.uppB = uppB;
    }

    private void computeBounds(){
        lowB = Integer.MAX_VALUE;
        uppB = Integer.MIN_VALUE;
        if (operator.equals(Operator.SUM)||operator.equals(Operator.SCALAR)){
            //TODO: compute value!
        } else {
            if(!operator.equals(Operator.NONE)){
                int[] val = computeByOperator(0, 1);
                lowB=(val[0]<lowB?val[0]:lowB);
                uppB=(val[1]>uppB?val[1]:uppB);
            }
        }
       }


    private int[] computeByOperator(int i, int j){
    	final SetExpressionVariable v1 =  getExpressionVariable(i);
        final int i1 = v1.getLowB();
        final int s1 = v1.getUppB();
        final SetExpressionVariable v2 =  getExpressionVariable(i);
        final int i2 = v2.getLowB();
        final int s2 = v2.getUppB();
        final int[]vals = new int[4];
        switch (operator) {
            case MINUS:
                vals[0] = i1 - i2;
                vals[1] = i1 - s2;
                vals[2] = s1 - i2;
                vals[3] = s1 - s2;
                break;
            case MULT:
                vals[0] = i1 * i2;
                vals[1] = i1 * s2;
                vals[2] = s1 * i2;
                vals[3] = s1 * s2;
                break;
            case NONE:
                break;
            case PLUS:
                vals[0] = i1 + i2;
                vals[1] = i1 + s2;
                vals[2] = s1 + i2;
                vals[3] = s1 + s2;
                break;
            default:
                vals[0] = Integer.MIN_VALUE;
                vals[1] = Integer.MIN_VALUE;
                vals[2] = Integer.MAX_VALUE;
                vals[3] = Integer.MAX_VALUE;
        }
        int[] bounds = new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE};
        for (int val : vals) {
            bounds[0] = Math.min(bounds[0], val);
            bounds[1] = Math.max(bounds[1], val);
        }
        return bounds;
    }

}
