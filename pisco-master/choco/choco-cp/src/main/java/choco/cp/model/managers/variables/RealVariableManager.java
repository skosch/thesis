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

import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.real.RealVarImpl;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.VariableManager;
import choco.kernel.model.variables.real.RealConstantVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.real.RealIntervalConstant;
import choco.kernel.solver.variables.real.RealVar;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 8 août 2008
 * Time: 13:36:45
 */
public final class RealVariableManager implements VariableManager<RealVariable> {

    /**
     * Build a real variable for the given solver
     *
     * @param solver
     * @param var
     * @return a real variable
     */
    public Var makeVariable(Solver solver, RealVariable var) {
        if (solver instanceof CPSolver) {
            if (var instanceof RealConstantVariable) {
                RealConstantVariable rcv = (RealConstantVariable) var;
                // Constant treatment
                double value = rcv.getValue();
//                RealVar s = new RealVarImpl(solver, rcv.getName(), value, value, RealVar.BOUNDS);
                RealIntervalConstant s = new RealIntervalConstant(value, value);
                ((CPSolver) solver).addrealConstant(value, s);
                return s;
            }
            RealVar s = new RealVarImpl(solver, var.getName(), var.getLowB(), var.getUppB(), RealVar.BOUNDS);
            ((CPSolver) solver).addRealVar(s);
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
