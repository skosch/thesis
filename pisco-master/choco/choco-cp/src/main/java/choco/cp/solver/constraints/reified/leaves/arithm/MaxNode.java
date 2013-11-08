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

import choco.cp.solver.constraints.integer.MaxOfAList;
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
public final class MaxNode extends INode implements ArithmNode {

	public MaxNode(INode[] subt) {
		super(subt, NodeType.MAX);
	}

	public int eval(int[] tuple) {
		int maxeval = Integer.MIN_VALUE;
        for (INode subtree : subtrees) {
            maxeval = Math.max(((ArithmNode) subtree).eval(tuple), maxeval);
        }
		return maxeval;
	}

	public IntDomainVar extractResult(Solver s) {
		IntDomainVar[] vs = new IntDomainVar[subtrees.length];
        IntDomainVar vmax;
        boolean allenum = true;
        int lb, ub;
        for (int i = 0; i < subtrees.length; i++) {
            vs[i] = subtrees[i].extractResult(s);
            allenum &= vs[i].hasEnumeratedDomain();
        }
        if (vs.length == 1) return vs[0];
        lb = vs[0].getInf();
		ub = vs[0].getSup();
		for (int i = 1; i < vs.length; i++) {
			lb = Math.min(lb,vs[i].getInf());
			ub = Math.max (ub,vs[i].getSup());
		}
        if(lb == 0 && ub == 1){
            vmax = s.createBooleanVar(StringUtils.randomName());
        }else
		if (allenum) {
			vmax = s.createEnumIntVar(StringUtils.randomName(), lb, ub);
		} else {
			vmax = s.createBoundIntVar(StringUtils.randomName(), lb, ub);
		}
		IntDomainVar[] tmpVars = new IntDomainVar[vs.length + 1];
        tmpVars[0] = vmax;
        System.arraycopy(vs, 0, tmpVars, 1, vs.length);
        s.post(new MaxOfAList(s.getEnvironment(), tmpVars));
		return vmax;
	}

    public String pretty() {
        StringBuilder s = new StringBuilder("max(");
	    for (int i = 0; i < subtrees.length - 1; i++) {
            s.append(subtrees[i].pretty()).append(",");
	    }
	    return s.append(subtrees[subtrees.length - 1].pretty()).append(")").toString();
    }
}
