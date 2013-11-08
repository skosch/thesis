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

import choco.cp.solver.constraints.integer.bool.BooleanFactory;
import choco.cp.solver.constraints.integer.channeling.ReifiedLargeNand;
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
public class NandNode extends AbstractBoolNode{

    public NandNode(INode... subt) {
        super(subt, NodeType.NAND);
    }

    public boolean checkTuple(int[] tuple) {
        for (INode subtree : subtrees) {
            if (!((BoolNode) subtree).checkTuple(tuple)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IntDomainVar extractResult(Solver s) {
        IntDomainVar[] vs = new IntDomainVar[subtrees.length+1];
//		IntDomainVar sand = s.createBoundIntVar(StringUtils.randomName(),0,subtrees.length);
		for (int i = 0; i < vs.length; i++) {
            vs[i] = subtrees[i].extractResult(s);
        }
        IntDomainVar v = s.createBooleanVar(StringUtils.randomName());
		s.post(new ReifiedLargeNand(ArrayUtils.append(new IntDomainVar[]{v}, vs), s.getEnvironment()));
		return v;
    }

    public SConstraint extractConstraint(Solver s) {
        IntDomainVar[] vs = new IntDomainVar[subtrees.length];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = subtrees[i].extractResult(s);
        }
        return BooleanFactory.nand(s.getEnvironment(), vs);
    }

    @Override
    public boolean isReified() {
        return true;
    }


    @Override
    public String pretty() {
        StringBuilder st = new StringBuilder("(");
        int i = 0;
        while (i < subtrees.length - 1) {
            st.append(subtrees[i].pretty()).append(" nand ");
            i++;
        }
        st.append(subtrees[i].pretty()).append(')');
        return st.toString();
    }
}
