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

package choco.cp.solver.constraints.reified.leaves.bool;

import choco.cp.solver.constraints.global.Occurrence;
import choco.cp.solver.constraints.integer.channeling.ReifiedLargeOr;
import choco.cp.solver.variables.integer.IntDomainVarImpl;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.reified.BoolNode;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.constraints.reified.NodeType;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 18 mai 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public class NorNode extends AbstractBoolNode{

    public NorNode(INode... subt) {
		super(subt, NodeType.NOR);
	}

	public boolean checkTuple(int[] tuple) {
        for (INode subtree : subtrees) {
            if (((BoolNode) subtree).checkTuple(tuple)) {
                return false;
            }
        }
		return true;
	}

	@Override
	public IntDomainVar extractResult(Solver s) {
        IntDomainVar[] vs = new IntDomainVar[subtrees.length];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = subtrees[i].extractResult(s);
        }
        if (vs.length > 1) {
            IntDomainVar v = s.createBooleanVar(StringUtils.randomName());
            IntDomainVar notv = s.createBooleanVar(StringUtils.randomName());
            IntDomainVar[] vars = ArrayUtils.append(new IntDomainVar[]{v}, vs);
            s.post(new ReifiedLargeOr(vars, s.getEnvironment()));
            s.post(s.neq(v,notv));
            return notv;
        } else {
            IntDomainVar notv = s.createBooleanVar(StringUtils.randomName());
            s.post(s.neq(vs[0],notv));
            return notv;
        }
	}

    /**
     * Extracts the sub constraint without reifying it !
     *
     * @param s solver
     * @return the equivalent constraint
     */
    public SConstraint extractConstraint(Solver s) {
        IntDomainVar[] vs = new IntDomainVar[subtrees.length+1];
		for (int i = 0; i < vs.length-1; i++) {
			vs[i] = subtrees[i].extractResult(s);
		}
        vs[vs.length - 1] = new IntDomainVarImpl(s, StringUtils.randomName(), IntDomainVar.ONE_VALUE,
                subtrees.length, subtrees.length);
        return new Occurrence(vs, 0, true, true, s.getEnvironment());
    }

    @Override
	public boolean isReified() {
		return true;
	}

	@Override
	public String pretty() {
        StringBuilder st = new StringBuilder("(");
        int i = 0;
        while (i < subtrees.length-1) {
            st.append(subtrees[i].pretty()).append(" and ");
            i++;
        }
        st.append(subtrees[i].pretty()).append(')');
        return st.toString();
    }
}
