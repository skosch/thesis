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

import choco.cp.solver.constraints.integer.TimesXYZ;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.reified.ArithmNode;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.constraints.reified.NodeType;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 23 avr. 2008
 * Time: 15:30:18
 * <p/>
 * <p/>
 * [BUG 3297805]: fix
 */
public final class MultNode extends INode implements ArithmNode {

    public MultNode(INode[] subt) {
        super(subt, NodeType.MULT);
    }

    public int eval(int[] tuple) {
        return ((ArithmNode) subtrees[0]).eval(tuple) * ((ArithmNode) subtrees[1]).eval(tuple);
    }

    public IntDomainVar extractResult(Solver s) {
        IntDomainVar v1 = subtrees[0].extractResult(s);
        IntDomainVar v2 = subtrees[1].extractResult(s);
        IntDomainVar v3;

        long i1 = (long) v1.getInf();
        long s1 = (long) v1.getSup();
        long i2 = (long) v2.getInf();
        long s2 = (long) v2.getSup();


        long a = i1 * i2;
        long b = i1 * s2;
        long c = s1 * i2;
        long d = s1 * s2;
        long _lb = Math.min(Math.min(Math.min(a, b), c), d);
        long _ub = Math.max(Math.max(Math.max(a, b), c), d);
        int lb = (int) Math.max(_lb, Integer.MIN_VALUE);
        int ub = (int) Math.min(_ub, Integer.MAX_VALUE);
        if (lb == 0 && ub == 1) {
            v3 = s.createBooleanVar(StringUtils.randomName());
        } else { // Times only performs BC, so bounded variable is enough
            v3 = s.createBoundIntVar(StringUtils.randomName(), lb, ub);
        }
        s.post(new TimesXYZ(v1, v2, v3));
        return v3;
    }


    public String pretty() {
        return "(" + subtrees[0].pretty() + " * " + subtrees[1].pretty() + ")";
    }


    public boolean isALinearTerm() {
        int a = subtrees[0].countNbVar();
        int b = subtrees[1].countNbVar();
        return (a == 0 || b == 0) && (subtrees[0].isALinearTerm() && subtrees[1].isALinearTerm());
    }

    public int[] computeLinearExpr(int scope) {
        int[] coeffs = subtrees[0].computeLinearExpr(scope);
        int[] coeffs2 = subtrees[1].computeLinearExpr(scope);
        if (subtrees[0].isAConstant()) {
            for (int i = 0; i < scope + 1; i++) {
                coeffs[i] = coeffs2[i] * coeffs[scope];
            }
        } else if (subtrees[1].isAConstant()) {
            for (int i = 0; i < scope + 1; i++) {
                coeffs[i] = coeffs[i] * coeffs2[scope];
            }
        } else {
            for (int i = 0; i < scope + 1; i++) {
                if (coeffs[i] == 0) {
                    coeffs[i] = coeffs2[i];
                } else if (coeffs2[i] != 0) {
                    coeffs[i] = coeffs2[i] * coeffs[i];
                }
            }
        }
//        if (subtrees[0].isAVariable()) {
//            int idx = ((VariableLeaf) subtrees[0]).getIdx();
//            coeffs[idx] = coeffs2[scope];
//        } else if (subtrees[1].isAConstant()) { //two constant
//            coeffs[scope + 1] *= coeffs2[scope + 1];
//        }
        return coeffs;
    }

}
