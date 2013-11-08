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

package choco.cp.model.managers.constraints;

import choco.cp.solver.constraints.reified.leaves.bool.FalseNode;
import choco.cp.solver.constraints.reified.leaves.bool.TrueNode;
import choco.kernel.common.Constant;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintManager;
import choco.kernel.model.variables.Variable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.reified.INode;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 11 août 2008
 * Time: 13:00:17
 */
public final class BooleanManager extends ConstraintManager<Variable> {

    /**
     * Build a constraint for the given solver and "model variables"
     *
     * @param solver
     * @param variables
     * @param parameters : a "hook" to attach any kind of parameters to constraints
     * @param options
     * @return
     */
    @Override
    public SConstraint makeConstraint(Solver solver, Variable[] variables, Object parameters, List<String> options) {
        if(parameters instanceof Boolean){
            boolean bool = (Boolean)parameters;
            if(bool){
                return Constant.TRUE;
            }else{
                return Constant.FALSE;
            }
        }
        return null;
    }

    /**
     * Build a constraint and its opposite for the given solver and "model variables"
     *
     * @param solver
     * @param variables
     * @param parameters
     * @param options
     * @return array of 2 SConstraint object, the constraint and its opposite
     */
    @Override
    public SConstraint[] makeConstraintAndOpposite(Solver solver, Variable[] variables, Object parameters, List<String> options) {
         if(parameters instanceof Boolean){
            boolean bool = (Boolean)parameters;
            if(bool){
                return new SConstraint[]{Constant.TRUE, Constant.FALSE};
            }else{
                return new SConstraint[]{Constant.FALSE, Constant.TRUE};
            }
        }
        return null;
    }

    /**
     * @param options : the set of options on the constraint (Typically the level of consistency)
     * @return a list of domains accepted by the constraint and sorted
     *         by order of preference
     */
    @Override
    public int[] getFavoriteDomains(List<String> options) {
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
        ComponentConstraint cc = (ComponentConstraint)cstrs[0];
        if(cc.getParameters() instanceof Boolean){
            boolean bool = (Boolean)cc.getParameters();
            if(bool){
                return new TrueNode();
            }else{
                return new FalseNode();
            }
        }
        return null;
    }
}