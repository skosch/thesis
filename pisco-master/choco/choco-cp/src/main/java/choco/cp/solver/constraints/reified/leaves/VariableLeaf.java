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

package choco.cp.solver.constraints.reified.leaves;

import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.reified.ArithmNode;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.constraints.reified.NodeType;
import choco.kernel.solver.variables.integer.IntDomainVar;

/*
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 23 avr. 2008
 * Since : Choco 2.0.0
 *
 */
public final class VariableLeaf extends INode implements ArithmNode {

	public int idx;

	public IntDomainVar var;

	public IntegerVariable ivar;

	public VariableLeaf(IntDomainVar var) {
		super(NodeType.VARIABLELEAF);
		this.var = var;
	}

	public VariableLeaf(IntegerVariable ivar) {
		super(NodeType.VARIABLELEAF);
		this.ivar = ivar;
	}


    public int getIdx() {
        return idx;    
    }

    public void setSolverVar(IntDomainVar v) {
		var = v;
	}

	public int eval(int[] tuple) {
		return tuple[idx];
	}

	public void setIndexes(IntDomainVar[] vs) {
		for (int i = 0; i < vs.length; i++) {
			if (vs[i] == var) {
				idx = i;break;
			}
		}
	}

	public final IntDomainVar[] getScope(Solver s) {
		if (ivar != null) var = s.getVar(ivar);
		return new IntDomainVar[]{var};
	}

	public IntegerVariable[] getModelScope() {
		return new IntegerVariable[]{ivar};	
	}

	public boolean isDecompositionPossible() {
		return true;
	}

	public boolean isReified() {
		return false;
	}
	
	public IntDomainVar extractResult(Solver s) {
		return var;
	}

    public int getNbSubTrees() {
        return 0;
    }

    public boolean isAVariable() {
        return true;
    }

    public boolean isBoolean(){
        return var.hasBooleanDomain();
    }

    public boolean isAConstant() {
        return false;
    }

    public String pretty() {
        return var.pretty();
    }

    public int countNbVar() {
        return 1;
    }

    public boolean isALinearTerm() {
        return true;
    }
    
    public int[] computeLinearExpr(int scope) {
        int[] coeffs = new int[scope + 1];
        coeffs[idx] = 1;
        return coeffs;
    }

}
