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

package choco.cp.model.managers.constraints.expressions;

import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.constraints.reified.leaves.bool.AndNode;
import choco.cp.solver.constraints.reified.leaves.bool.NotNode;
import choco.cp.solver.constraints.reified.leaves.bool.OrNode;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.MetaConstraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.reified.INode;

import java.util.List;

/*
 * User:    charles
 * Date:    22 août 2008
 */
public final class IfOnlyIfManager extends IntConstraintManager{

    /**
     * Build a constraint for the given solver and "model variables"
     *
     * @param solver
     * @param variables
     * @param parameters : a "hook" to attach any kind of parameters to constraints
     * @param options
     * @return
     */
    public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables, Object parameters, List<String> options) {
        return null;
    }

    /**
     * Build arithm node from a IntegerExpressionVariable
     *
     * @param solver
     * @param cstrs  constraints (can be null)
     * @param vars   variables
     * @return
     */
    public INode makeNode(Solver solver, Constraint[] cstrs, Variable[] vars) {
        MetaConstraint mc = (MetaConstraint)cstrs[0];
        INode[] nt = new INode[2];
        for (int i = 0; i < mc.getConstraints().length; i++) {
            Constraint c = mc.getConstraints()[i];
            Variable[] ev = new Variable[c.getNbVars()];
            for(int j = 0; j < c.getNbVars(); j++){
                ev[j]  = c.getVariables()[j];
            }
            nt[i] = c.getExpressionManager().makeNode(solver, new Constraint[]{c}, ev);
        }
        INode[] nt2 = new INode[2];
        nt2[0] = new OrNode(nt[0], new NotNode(new INode[]{nt[1]}));
        nt2[1] = new OrNode(new NotNode(new INode[]{nt[0]}), nt[1]);
        return new AndNode(nt2);
    }
}
