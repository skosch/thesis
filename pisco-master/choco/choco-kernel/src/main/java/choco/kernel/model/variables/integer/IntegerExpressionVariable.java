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
public class IntegerExpressionVariable extends ComponentVariable implements IntBoundedVariable {

	protected int lowB = Integer.MAX_VALUE;
	protected int uppB = Integer.MIN_VALUE;

	protected IntegerExpressionVariable(VariableType variableType,
			Object parameters, boolean enableOption, IConstraintList constraints) {
		super(variableType, enableOption, parameters, constraints);
	}

	public IntegerExpressionVariable(Object parameters, Operator operator, IntegerExpressionVariable... variables) {
		super(VariableType.INTEGER_EXPRESSION, operator, parameters, variables);
	}

	public IntegerExpressionVariable(Object parameters, String operator, IntegerExpressionVariable... variables) {
		super(VariableType.INTEGER_EXPRESSION, operator,parameters, variables);
		//        init();
	}

	public IntegerExpressionVariable(Object parameters, Class operator, IntegerExpressionVariable... variables) {
		this(parameters, operator.getName(), variables);
	}

	private void initializeBounds(){
		if(operator!=null && !operator.equals(Operator.NONE)){
			computeBounds();
		}
	}

	public final IntegerExpressionVariable getExpressionVariable(int i) {
		return (IntegerExpressionVariable) getVariable(i);
	}



	public final int getLowB() {
		if(lowB == Integer.MAX_VALUE) initializeBounds();
		return lowB;
	}

	public void setLowB(int lowB) {
		this.lowB=lowB;
	}

	public final int getUppB() {
		if(uppB == Integer.MIN_VALUE) initializeBounds();
		return uppB;
	}

	public void setUppB(int uppB) {
		this.uppB = uppB;
	}

	private void computeBounds(){
		lowB = Integer.MAX_VALUE;
		uppB = Integer.MIN_VALUE;
		int[] val;
		switch(operator.parameters){
		case 0:
			val = computeByNOperator();
			lowB=(val[0]<lowB?val[0]:lowB);
			uppB=(val[1]>uppB?val[1]:uppB);
			break;
		case 1:
			//for ABS and NEG
			val = computeByOperator(0);
			lowB=(val[0]<lowB?val[0]:lowB);
			uppB=(val[1]>uppB?val[1]:uppB);
			break;
		case 2:
			//for others
			val = computeByOperator(0, 1);
			lowB=(val[0]<lowB?val[0]:lowB);
			uppB=(val[1]>uppB?val[1]:uppB);
			break;
		default:
			break;
		}
	}

	private int[] computeByOperator(int i) {
		final IntegerExpressionVariable v = getExpressionVariable(i);
		final int i1 = v.getLowB();
		final int s1 = v.getUppB();
		final int[] vals = new int[2];
		switch (operator) {
		case ABS:
			vals[0] = Math.min(Math.abs(i1), Math.abs(s1));
			if(i1<0 && s1>0)vals[0] = 0;
			vals[1] = Math.max(Math.abs(i1), Math.abs(s1));
			break;
		case NEG:
			vals[0] = -s1;
			vals[1] = -i1;
			break;
		case NONE:
			break;
		default:
			vals[0] = Integer.MIN_VALUE;
			vals[1] = Integer.MAX_VALUE;
		}
		return vals;
	}

	private int[] computeByOperator(int i, int j) {
		final IntegerExpressionVariable v1 = getExpressionVariable(i);
		final int i1 = v1.getLowB();
		final int s1 = v1.getUppB();
		final IntegerExpressionVariable v2 = (IntegerExpressionVariable) getVariable(j);
		int i2 = v2.getLowB();
		int s2 = v2.getUppB();
		final int[] vals = new int[4];
		switch (operator) {
		case MINUS:
			vals[0] = i1 - i2;
			vals[1] = i1 - s2;
			vals[2] = s1 - i2;
			vals[3] = s1 - s2;
			break;
		case MOD:
			if(i2==0 && s2==0){
				vals[0] = Integer.MIN_VALUE;
				vals[1] = Integer.MIN_VALUE;
				vals[2] = Integer.MAX_VALUE;
				vals[3] = Integer.MAX_VALUE;
				break;
			}
			if(s2==0)s2=1;
			if(i2==0)i2=1;
			vals[0] = i1 % i2;
			vals[1] = i1 % s2;
			vals[2] = s1 % i2;
			vals[3] = s1 % s2;
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
		case POWER:
			vals[0] = (int)Math.pow(i1, i2);
			vals[1] = (int)Math.pow(i1, s2);
			vals[2] = (int)Math.pow(s1, i2);
			vals[3] = (int)Math.pow(s1, s2);
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

	private int[] computeByNOperator() {
		final int n = getNbVars();
		int i1 = 0;
		int s1 = 0;
		int i = 0;
		int[] vals = new int[4];
		switch(operator){
		case SCALAR:
			int cste = n/2;
			do{
				final int val2 = ((IntegerConstantVariable) getVariable(i)).getValue();
				final IntegerExpressionVariable v = (IntegerExpressionVariable) getVariable(i + cste);
				final int i2 = v.getLowB()*val2;
				final int s2 = v.getUppB()*val2;
				vals[0] = i1 + i2;
				vals[1] = i1 + s2;
				vals[2] = s1 + i2;
				vals[3] = s1 + s2;
				i1 = Integer.MAX_VALUE;
				s1 = Integer.MIN_VALUE;
				for (int val : vals) {
					i1 = Math.min(i1, val);
					s1 = Math.max(s1, val);
				}
				i++;
			}while(i < cste);
			break;
		case SUM:
			do{
				final IntegerExpressionVariable v = getExpressionVariable(i);
				final int i2 = v.getLowB();
				final int s2 = v.getUppB();
				vals[0] = i1 + i2;
				vals[1] = i1 + s2;
				vals[2] = s1 + i2;
				vals[3] = s1 + s2;
				i1 = Integer.MAX_VALUE;
				s1 = Integer.MIN_VALUE;
				for (int val : vals) {
					i1 = Math.min(i1, val);
					s1 = Math.max(s1, val);
				}
				i++;
			}while(i < n);
			break;
		case MAX:
			i1 = Integer.MIN_VALUE;
			s1 = Integer.MIN_VALUE;
			for (int k=0 ; k < n; k++) {
				final IntegerExpressionVariable v = getExpressionVariable(k);
				final int i2 = v.getLowB();
				final int s2 = v.getUppB();
				i1 = Math.max(i1, i2);
				s1 = Math.max(s1, s2);
			}
			break;
		case MIN:
			i1 = Integer.MAX_VALUE;
			s1 = Integer.MAX_VALUE;
			for (int k=0 ; k < n; k++) {
				final IntegerExpressionVariable v = getExpressionVariable(k);
				final int i2 = v.getLowB();
				final int s2 = v.getUppB();
				i1 = Math.min(i1, i2);
				s1 = Math.min(s1, s2);
			}
			break;
		default:
			i1 = Integer.MIN_VALUE;
			s1 = Integer.MAX_VALUE;
			break;
		}
		return new int[]{i1, s1};
	}

}
