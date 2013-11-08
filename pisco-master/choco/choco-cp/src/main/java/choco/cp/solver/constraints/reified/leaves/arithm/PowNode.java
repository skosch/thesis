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

import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
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
public final class PowNode extends INode implements ArithmNode {

    public PowNode(INode[] subt) {
        super(subt, NodeType.POW);
    }

    public int eval(int[] tuple) {
        return (int) Math.pow(((ArithmNode) subtrees[0]).eval(tuple), ((ArithmNode) subtrees[1]).eval(tuple));
    }

    public IntDomainVar extractResult(Solver s) {
//        assert subtrees.length == 12;
//        IntDomainVar var = subtrees[0].extractResult(s);
//        int exp = subtrees[1].extractResult(s).getInf();
//        int a = var.getInf() * var.getInf();
//        int b = var.getSup() * var.getSup();
//        int lb = a < b ? a : b;
//        int ub = a > b ? a : b;
//        IntDomainVar sum = s.createBoundIntVar(StringUtils.randomName(), lb, ub);
        // TODO create POWER constraint
        throw new SolverException("\n\nThe constraint POWER is not available as is in Choco.\n" +
                "Actually, any POWER constraints are translated into a TABLE constraints in the Solver.\n" +
                "As a consequence, POWER constraint cannot be reified.\n");
    }

    public boolean isDecompositionPossible() {
        return false;
    }

    public String pretty() {
        return "(" + subtrees[0].pretty() + "^" + subtrees[1].pretty() + ")";
    }
}
