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

package choco.cp.model.managers.constraints.global;

import choco.Options;
import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.*;
import choco.cp.solver.constraints.reified.ExpressionSConstraint;
import choco.cp.solver.constraints.reified.leaves.VariableLeaf;
import choco.cp.solver.constraints.reified.leaves.bool.AndNode;
import choco.cp.solver.constraints.reified.leaves.bool.EqNode;
import choco.cp.solver.constraints.reified.leaves.bool.NeqNode;
import choco.cp.solver.constraints.reified.leaves.bool.OrNode;
import choco.kernel.model.ModelException;
import choco.kernel.model.variables.VariableType;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 9 août 2008
 * Time: 16:25:12
 */
public final class ElementManager extends IntConstraintManager{

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
        if(solver instanceof CPSolver){
            if(parameters instanceof Integer){
                int offset = (Integer)parameters;
                IntDomainVar index = solver.getVar(variables[variables.length-2]);
                IntDomainVar val = solver.getVar(variables[variables.length-1]);
                // BUG 2860512 : check every type is mandatory
                int[] values = new int[variables.length-2];
                boolean areConstants = true;
                for(int i = 0; i < variables.length-2; i++){
                    if(variables[i].getVariableType().equals(VariableType.CONSTANT_INTEGER)){
                        values[i] = ((IntegerConstantVariable)variables[i]).getValue();
                    }else{
                        areConstants = false;
                        break;
                    }
                }
                if(areConstants){
                    if(options.contains(Options.C_NTH_G)){
                        return new ElementG(index, values, val, solver.getEnvironment());
                    }
                    return new Element(index, values, val, offset);
                }else{
                    if (index.hasEnumeratedDomain()) {
                        if(options.contains(Options.C_NTH_G)){
                            return new ElementVG(solver.getVar((IntegerVariable[])variables), offset, solver.getEnvironment());
                        }
                        return new ElementV(solver.getVar((IntegerVariable[])variables), offset, solver.getEnvironment());
                    }else{
                        throw new SolverException(index.getName()+" has not an enumerated domain");
                    }
                }
            }else if(parameters instanceof int[][]){
                int[][] varArray = (int[][])parameters;
                IntDomainVar index = solver.getVar(variables[0]);
                IntDomainVar index2 = solver.getVar(variables[1]);
                IntDomainVar val = solver.getVar(variables[2]);
                return new Element2D(index, index2, val, varArray);
            }
        }

        throw new ModelException("Could not found a constraint manager in " + this.getClass() + " !");
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
    public SConstraint[] makeConstraintAndOpposite(Solver solver, IntegerVariable[] variables, Object parameters, List<String> options) {

        SConstraint[] cs = new SConstraint[2];
        if(solver instanceof CPSolver){
            if(parameters instanceof Integer){
                int offset = (Integer)parameters;
                IntDomainVar y;
                final IntDomainVar x = solver.getVar(variables[variables.length-2]);
                // Introduces a intermediary variable
                if(x.hasBooleanDomain()){
                    y = solver.createBooleanVar("Y_opp");
                }else if(x.hasEnumeratedDomain()){
                    y = solver.createEnumIntVar("Y_opp", x.getInf(), x.getSup());
                }else{
                    y = solver.createBoundIntVar("Y_opp", x.getInf(), x.getSup());
                }

                IntDomainVar val = solver.getVar(variables[variables.length-1]);
                if(variables[0] instanceof IntegerConstantVariable){
                    int[] values = new int[variables.length-2];
                    for(int i = 0; i < variables.length-2; i++){
                        values[i] = ((IntegerConstantVariable)variables[i]).getValue();
                    }
                    if(options.contains(Options.C_NTH_G)){
                        solver.post(new ElementG(y, values, val, solver.getEnvironment()));
                    }else{
                        solver.post(new Element(y, values, val, offset));
                    }
                }else{
                    if (y.hasEnumeratedDomain()) {
                        IntDomainVar[] tvars = solver.getVar((IntegerVariable[])variables);
                        tvars[variables.length-2] = y;
                        if(options.contains(Options.C_NTH_G)){
                            solver.post(new ElementVG(tvars, offset, solver.getEnvironment()));
                        }else{
                            solver.post(new ElementV(tvars, offset, solver.getEnvironment()));
                        }
                    }else{
                        throw new SolverException(x.getName()+" has not an enumerated domain");
                    }
                }
                cs[0] = solver.eq(y, x);
                cs[1] = solver.neq(y, x);
            }else if(parameters instanceof int[][]){
                IntDomainVar y1, y2;
                int[][] varArray = (int[][])parameters;
                final IntDomainVar x1 = solver.getVar(variables[0]);
                final IntDomainVar x2 = solver.getVar(variables[1]);
                if(x1.hasBooleanDomain()){
                    y1 = solver.createBooleanVar("Y1_opp");
                }else if(x1.hasEnumeratedDomain()){
                    y1 = solver.createEnumIntVar("Y1_opp", x1.getInf(), x1.getSup());
                }else{
                    y1 = solver.createBoundIntVar("Y1_opp", x1.getInf(), x1.getSup());
                }
                if(x2.hasBooleanDomain()){
                    y2 = solver.createBooleanVar("Y2_opp");
                }else if(x2.hasEnumeratedDomain()){
                    y2 = solver.createEnumIntVar("Y2_opp", x2.getInf(), x2.getSup());
                }else{
                    y2 = solver.createBoundIntVar("Y2_opp", x2.getInf(), x2.getSup());
                }

                IntDomainVar val = solver.getVar(variables[2]);
                solver.post(new Element2D(y1, y2, val, varArray));
                cs[0] = new ExpressionSConstraint(
                        new AndNode(new EqNode(new INode[]{new VariableLeaf(y1), new VariableLeaf(x1)}),
                                new EqNode(new INode[]{new VariableLeaf(y2), new VariableLeaf(x2)})));
                cs[1] = new ExpressionSConstraint(
                        new OrNode(new NeqNode(new INode[]{new VariableLeaf(y1), new VariableLeaf(x1)}),
                                new NeqNode(new INode[]{new VariableLeaf(y2), new VariableLeaf(x2)})));
            }
            return cs;
        }

        throw new ModelException("Could not found a node manager in " + this.getClass() + " !");
    }
}
