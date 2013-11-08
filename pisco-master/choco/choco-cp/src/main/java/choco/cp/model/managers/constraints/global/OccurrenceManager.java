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

import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.Occurrence;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.ModelException;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;

/*
*  ______
* (__  __)
*    ||
*   /__\                  Choco manager
*    \                    =============
*    \                      Aug. 2008
*    \            All alldifferent constraints
*    \
*    |
*/
/**
 * A manager to build new all occurence constraints
 */
public final class OccurrenceManager extends IntConstraintManager {

    private static final int NOR = 0;
    private static final int MIN = -1;
    private static final int MAX = 1;

    public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables, Object parameters, List<String> options) {
        if(solver instanceof CPSolver){
            if(parameters instanceof Integer){
                int type = (Integer)parameters;
                IntegerVariable[] vars = new IntegerVariable[variables.length-1];
                vars[vars.length-1] = variables[1];
                //noinspection SuspiciousSystemArraycopy
                System.arraycopy(variables, 2, vars, 0, vars.length-1);
                if(type == NOR){
                    return new Occurrence(solver.getVar(vars), ((IntegerConstantVariable)variables[0]).getValue(), true, true, solver.getEnvironment());
                }else
                if(type == MIN){
                    return new Occurrence(solver.getVar(vars), ((IntegerConstantVariable)variables[0]).getValue(), true, false, solver.getEnvironment());
                }else
                if(type == MAX){
                    return new Occurrence(solver.getVar(vars), ((IntegerConstantVariable)variables[0]).getValue(), false, true, solver.getEnvironment());
                }
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
        if (solver instanceof CPSolver) {
			if(parameters instanceof Integer){
                int type = (Integer)parameters;
                IntDomainVar Y;
                final IntDomainVar X = solver.getVar(variables[1]);
                // Introduces a intermediary variable
                if(X.hasBooleanDomain()){
                    Y = solver.createBooleanVar("Y_opp");
                }else if(X.hasEnumeratedDomain()){
                    Y = solver.createEnumIntVar("Y_opp", X.getInf(), X.getSup());
                }else{
                    Y = solver.createBoundIntVar("Y_opp", X.getInf(), X.getSup());
                }

                IntegerVariable[] tvars = new IntegerVariable[variables.length-2];
                //noinspection SuspiciousSystemArraycopy
                System.arraycopy(variables, 2, tvars, 0, tvars.length);
                IntDomainVar[] vars = ArrayUtils.append(solver.getVar(tvars), new IntDomainVar[]{Y});

                if(type == NOR){
                    solver.post(new Occurrence(vars, ((IntegerConstantVariable)variables[0]).getValue(), true, true, solver.getEnvironment()));
                }else
                if(type == MIN){
                    solver.post(new Occurrence(vars, ((IntegerConstantVariable)variables[0]).getValue(), true, false, solver.getEnvironment()));
                }else
                if(type == MAX){
                    solver.post(new Occurrence(vars, ((IntegerConstantVariable)variables[0]).getValue(), false, true, solver.getEnvironment()));
                }
                cs[0] = solver.eq(Y, X);
                cs[1] = solver.neq(Y, X);
                return cs;
			}
		}
		throw new ModelException("Could not found a constraint and opposite manager in " + this.getClass() + " !");
    }
}
