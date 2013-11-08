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

package choco.cp.solver.constraints.reified.leaves.arithm;

import choco.kernel.common.util.tools.StringUtils;
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
public final class NegNode extends INode implements ArithmNode {

	public NegNode(INode[] subt) {
		super(subt, NodeType.NEG);
	}

	public int eval(int[] tuple) {
		return -((ArithmNode) subtrees[0]).eval(tuple);
	}

	public IntDomainVar extractResult(Solver s) {
		IntDomainVar v1 = subtrees[0].extractResult(s);
		IntDomainVar v2;
		int lb = Math.min(
                    Math.min(Math.min(v1.getInf(), -v1.getInf()),v1.getSup()),-v1.getSup());

        int ub = Math.max(
                    Math.max(Math.max(v1.getInf(), -v1.getInf()),v1.getSup()),-v1.getSup());

        if(lb==0 && ub == 1){
            v2 = s.createBooleanVar(StringUtils.randomName());
        }else
		if (v1.hasEnumeratedDomain()) {
			v2 = s.createEnumIntVar(StringUtils.randomName(), lb, ub);
		} else {
			v2 = s.createBoundIntVar(StringUtils.randomName(), lb, ub);
		}
		//s.post(new SignOp(v1,v2, false));
        s.post(s.eq(s.plus(v1, v2),0));//s.eq(0, s.scalar(new int[]{1,1},new IntDomainVar[]{v1,v2})));
        return v2;
	}

    public String pretty() {
        return "-("+subtrees[0].pretty()+")";
    }

    public boolean isALinearTerm() {
        for (INode subtree : subtrees) {
            if (!subtree.isALinearTerm()) return false;
        }
        return true;
    }

    public int[] computeLinearExpr(int scope) {
        int[] coeffs = subtrees[0].computeLinearExpr(scope);
        for (int i = 0; i < coeffs.length; i++) {
            coeffs[i] = -coeffs[i];
        }
        return coeffs;
    }

}
