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

package choco.cp.model.managers.variables;

import choco.Options;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.set.SetCard;
import choco.cp.solver.variables.set.SetVarImpl;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.VariableManager;
import choco.kernel.model.variables.VariableType;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 8 août 2008
 * Time: 13:26:41
 */
public final class SetVariableManager implements VariableManager<SetVariable> {

    /**
     * Build a set variable for the given solver
     * @param solver
     * @param var
     * @return a set variable
     */
    public Var makeVariable(Solver solver, SetVariable var) {
        if(solver instanceof CPSolver){
            IntDomainVar card = (var.getCard()!=null?solver.getVar(var.getCard()):null);
            SetVar s;
            if(var.getVariableType()== VariableType.CONSTANT_SET){
                    s = new SetVarImpl(solver, var.getName(), var.getValues(), card, SetVar.BOUNDSET_CONSTANT);
            }else
            if (var.getValues() == null) {
                if(var.getOptions().contains(Options.V_BOUND)){
                    s = new SetVarImpl(solver, var.getName(), var.getLowB(), var.getUppB(), card, SetVar.BOUNDSET_BOUNDCARD);
                }else
                {
                    s = new SetVarImpl(solver, var.getName(), var.getLowB(), var.getUppB(), card, SetVar.BOUNDSET_ENUMCARD);
                }
            }else{
                int[] values = var.getValues();
                if(var.getOptions().contains(Options.V_BOUND)){
                    s = new SetVarImpl(solver, var.getName(), values, card, SetVar.BOUNDSET_BOUNDCARD);
                }else
                {
                    s = new SetVarImpl(solver, var.getName(), values, card, SetVar.BOUNDSET_ENUMCARD);
                }
            }
            ((CPSolver)solver).addSetVar(s);
            solver.post(new SetCard(s, s.getCard(), true, true)); //post |v| = v.getCard() 
            return s;
        }
        throw new ModelException("Could not found a variable manager in " + this.getClass() + " !");
    }

    /**
     * Build a expression node
     *
     * @param solver
     * @param cstrs  constraints
     * @param vars   variables
     * @return
     */
    public INode makeNode(Solver solver, Constraint[] cstrs, Variable[] vars) {
        return null;
    }
}
