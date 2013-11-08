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

import java.util.Arrays;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 5 août 2008
 * Since : Choco 2.0.0
 *
 */
public final class ScalarNode extends INode implements ArithmNode {
    protected int[] coeffs;

    public ScalarNode(INode[] subt) {
        super(subt, NodeType.SCALAR);
        coeffs = new int[subt.length];
        Arrays.fill(coeffs, 1);
    }

    public ScalarNode(INode[] subt, int[] coeffs) {
        super(subt, NodeType.SCALAR);
        this.coeffs = coeffs;
        assert(subt.length == coeffs.length);
    }

    public int eval(int[] tuple) {
        int sum = 0;
        int i = 0;
        for (INode t : subtrees) {
            sum += coeffs[i++] * ((ArithmNode) t).eval(tuple);
        }
        return sum;
    }

    public IntDomainVar extractResult(Solver s) {
        IntDomainVar[] vars = new IntDomainVar[subtrees.length];
        int lb = 0, ub = 0;
        for (int i = 0; i < vars.length; i++) {
            vars[i] = subtrees[i].extractResult(s);
            lb += coeffs[i] >= 0 ? vars[i].getInf() * coeffs[i] : vars[i].getSup() * coeffs[i];
            ub += coeffs[i] >= 0 ? vars[i].getSup() * coeffs[i] : vars[i].getInf() * coeffs[i];
        }
        IntDomainVar sum = s.createBoundIntVar(StringUtils.randomName(), lb, ub);
        s.post(s.eq(sum, s.scalar(coeffs, vars)));
        return sum;
    }

    public String pretty() {
        StringBuffer st = new StringBuffer("(");
        int i = 0;
        for (INode t : subtrees) {
            st.append(t.pretty());
            i++;
            if (i < subtrees.length) {
                st.append("+");
            }
        }
        st.append(")");
        return st.toString();
    }

    public boolean isALinearTerm() {
        for (INode subtree : subtrees) {
            if (!subtree.isALinearTerm()) return false;
        }
        return true;
    }

    public int[] computeLinearExpr(int scope) {
        int[] cToRet = new int[scope + 1];
        for (int i = 0; i < coeffs.length; i++) {
            int[] c = subtrees[i].computeLinearExpr(scope);
            for (int j = 0; j < c.length; j++) {
                //cToRet[j] = (c[j] != 0) ? coeffs[i] * c[j] : cToRet[j];
                cToRet[j] += coeffs[i] * c[j];
            }
        }
        return cToRet;
    }

}

