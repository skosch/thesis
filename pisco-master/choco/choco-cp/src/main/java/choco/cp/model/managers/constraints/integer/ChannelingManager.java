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

package choco.cp.model.managers.constraints.integer;

import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.InverseChanneling;
import choco.cp.solver.constraints.integer.InverseChannelingWithinRange;
import choco.cp.solver.constraints.integer.channeling.BooleanChanneling;
import choco.cp.solver.constraints.integer.channeling.DomainChanneling;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
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
 * A manager to build new all channeling constraints
 */
public final class ChannelingManager extends IntConstraintManager {


    public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables, Object parameters, List<String> options) {
        if (solver instanceof CPSolver) {
            if (parameters instanceof Object[]) {
                Object[] params = (Object[]) parameters;
                ConstraintType type = (ConstraintType) params[0];
                IntDomainVar[] var;
                switch (type) {
                    case CHANNELING:
                        IntDomainVar yij = solver.getVar(variables[0]);
                        IntDomainVar xi = solver.getVar(variables[1]);
                        int j = ((IntegerConstantVariable) variables[2]).getValue();
                        IntDomainVar boolv, intv;
                        if ((yij.getInf() >= 0) && (yij.getSup() <= 1)) {
                            boolv = yij;
                            intv = xi;
                        } else {
                            boolv = xi;
                            intv = yij;
                        }
                        if ((boolv.getInf() >= 0) && (boolv.getSup() <= 1)/* && (intv.canBeInstantiatedTo(j))*/) {
                            return new BooleanChanneling(boolv, intv, j);
                        } else {
                            throw new SolverException(yij + " should be a boolean variable and " + j + " should belongs to the domain of " + xi);
                        }
                    case INVERSECHANNELING:
                        var = solver.getVar(variables);
                        return new InverseChanneling(var, var.length / 2);
                    case INVERSECHANNELINGWITHINRANGE:
                        int nbx = (Integer) params[1];
                        var = solver.getVar(variables);
                        return new InverseChannelingWithinRange(var, nbx);
                    case DOMAIN_CHANNELING:
                        return new DomainChanneling(
                                VariableUtils.getIntVar(solver, variables, 0, variables.length - 1),
                                solver.getVar(variables[variables.length - 1]),
                                solver.getEnvironment());
                }
            }
        }
        throw new ModelException("Could not found a constraint manager in " + this.getClass() + " !");
    }
}
