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

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.Absolute;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.reified.ArithmNode;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.constraints.reified.NodeType;
import choco.kernel.solver.variables.integer.IntDomainVar;

import static java.lang.Math.*;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 5 août 2008
 * Since : Choco 2.0.0
 *
 */
public final class DistNode extends INode implements ArithmNode {

	public DistNode(INode[] subt) {
		super(subt, NodeType.DIST);
	}

	public int eval(int[] tuple) {
		return abs(((ArithmNode) subtrees[0]).eval(tuple)-((ArithmNode) subtrees[1]).eval(tuple));
	}

	public IntDomainVar extractResult(Solver s) {
		IntDomainVar v1 = subtrees[0].extractResult(s);
		IntDomainVar v2 = subtrees[1].extractResult(s);
		IntDomainVar v3;
        IntDomainVar v4;
        int a = v1.getInf() - v2.getInf();
		int b = v1.getInf() - v2.getSup();
		int c = v1.getSup() - v2.getInf();
		int d = v1.getSup() - v2.getSup();
		int lb = min(min(min(a,b),c),d);
		int ub = max(max(max(a,b),c),d);
        if(lb==0 && ub ==1){
            v3 = s.createBooleanVar(StringUtils.randomName());
            v4 = s.createBooleanVar(StringUtils.randomName());
        }else
		if (v1.hasEnumeratedDomain() && v2.hasEnumeratedDomain()) {
			v3 = s.createEnumIntVar(StringUtils.randomName(), lb, ub);
            v4 = s.createEnumIntVar(StringUtils.randomName(), lb, ub);
        } else {
			v3 = s.createBoundIntVar(StringUtils.randomName(), lb, ub);
            v4 = s.createBoundIntVar(StringUtils.randomName(), lb, ub);
        }
		s.post(s.eq(v3, ((CPSolver)s).minus(v1,v2)));
        s.post(new Absolute(v4, v3));
        return v4;
	}

    public String pretty() {
        return "(|"+subtrees[0].pretty()+"-"+subtrees[1].pretty()+"|)";
    }
}
