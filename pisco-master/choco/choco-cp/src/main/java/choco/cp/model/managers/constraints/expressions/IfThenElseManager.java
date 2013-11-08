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

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.reified.leaves.arithm.IfThenElseNode;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.constraints.ExpressionManager;
import choco.kernel.model.constraints.MetaConstraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.reified.INode;

/*
 * User:    charles
 * Date:    21 août 2008
 */
public final class IfThenElseManager implements ExpressionManager {

    /**
     * Build arithm node from a IntegerExpressionVariable
     *
     * @param solver
     * @param cstrs  constraints (can be null)
     * @param vars   variables
     * @return
     */
    public INode makeNode(Solver solver, Constraint[] cstrs, Variable[] vars) {
        if(solver instanceof CPSolver){
            CPSolver s = (CPSolver)solver;
            INode[] nt = new INode[3];
            if(cstrs[0].getConstraintType().equals(ConstraintType.IFTHENELSE)){
                MetaConstraint mc = (MetaConstraint)cstrs[0];
                for (int i = 0; i < mc.getConstraints().length; i++) {
                    Constraint c = mc.getConstraints()[i];
                    Variable[] ev = new Variable[c.getNbVars()];
                    for(int j = 0; j < c.getNbVars(); j++){
                        ev[j]  = c.getVariables()[j];
                    }
                    nt[i] = c.getExpressionManager().makeNode(solver, new Constraint[]{c}, ev);
                }
                return new IfThenElseNode(nt);
            }else /*if(cstrs[0] instanceof ComponentConstraint)*/{
                Variable[] ev = new Variable[cstrs[0].getNbVars()];
                for(int j = 0; j < cstrs[0].getNbVars(); j++){
                    ev[j]  = cstrs[0].getVariables()[j];
                }
                nt[0] = cstrs[0].getExpressionManager().makeNode(solver, new Constraint[]{cstrs[0]}, ev);
                nt[1] = vars[0].getExpressionManager().makeNode(s, vars[0].getConstraints(), vars[0].getVariables());
                nt[2] = vars[1].getExpressionManager().makeNode(s, vars[1].getConstraints(), vars[1].getVariables());
                return new IfThenElseNode(nt);
            }
        }
        throw new ModelException("Could not found a node manager in " + this.getClass() + " !");
    }
}
