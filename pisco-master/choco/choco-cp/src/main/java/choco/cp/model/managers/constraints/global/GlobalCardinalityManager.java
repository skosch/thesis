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
import choco.cp.solver.constraints.global.BoundGcc;
import choco.cp.solver.constraints.global.BoundGccVar;
import choco.cp.solver.constraints.global.matching.GlobalCardinality;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Arrays;
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
 * A manager to build new all different constraints (and more... soon)
 */
public final class GlobalCardinalityManager extends IntConstraintManager {


    public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables, Object parameters, List<String> options) {
        if (solver instanceof CPSolver) {
            if (parameters instanceof Object[]) {
                Object[] params = (Object[]) parameters;
                ConstraintType type = (ConstraintType) params[0];
                int[] low, up;
                int min, max, n, k;
                IntDomainVar[] vars, varT, card;
                switch (type) {

                    case GLOBALCARDINALITYMAX:
                        min = (Integer) params[1];
                        max = (Integer) params[2];
                        low = (int[]) params[3];
                        up = (int[]) params[4];
                        vars = solver.getVar(variables);
                        if (options.contains(Options.C_GCC_AC)) {
                            return new GlobalCardinality(vars, min, max, low, up, solver.getEnvironment());
                        }
                        if (options.contains(Options.C_GCC_BC)) {
                            return new BoundGcc(vars, min, max, low, up, solver.getEnvironment());
                        }
                        if (vars[0].hasEnumeratedDomain()) {
                            return new GlobalCardinality(vars, min, max, low, up, solver.getEnvironment());
                        } else {
                            return new BoundGcc(vars, min, max, low, up, solver.getEnvironment());
                        }
                    case GLOBALCARDINALITYVALUES:
                        int[] values = (int[]) params[1];
                        low = (int[]) params[2];
                        up = (int[]) params[3];
                        vars = solver.getVar(variables);

                        min = values[0];
                        max = values[values.length - 1];

                        for (int v = 0; v < vars.length; v++) {
                            IntDomainVar var = vars[v];
                            if (min > var.getInf()) {
                                min = var.getInf();
                            }
                            if (max < var.getSup()) {
                                max = var.getSup();
                            }
                        }
                        int[] _low = new int[max - min + 1];
                        int[] _up = new int[max - min + 1];
                        Arrays.fill(_up, vars.length);
                        k = 0;
                        for (int i = min; i <= max; i++) {
                            if (k < low.length && i == values[k]) {
                                _low[i - min] = low[k];
                                _up[i - min] = up[k];
                                k++;
                            }
                        }

                        if (options.contains(Options.C_GCC_AC)) {
                            return new GlobalCardinality(vars, min, max, _low, _up, solver.getEnvironment());
                        }
                        if (options.contains(Options.C_GCC_BC)) {
                            return new BoundGcc(vars, min, max, _low, _up, solver.getEnvironment());
                        }
                        if ((vars[0]).hasEnumeratedDomain()) {
                            return new GlobalCardinality(vars, min, max, _low, _up, solver.getEnvironment());
                        } else {
                            return new BoundGcc(vars, min, max, _low, _up, solver.getEnvironment());
                        }
                    case GLOBALCARDINALITYVAR:
                        min = (Integer) params[1];
                        max = (Integer) params[2];
                        n = (Integer) params[3];
                        vars = solver.getVar(variables);
                        varT = new IntDomainVar[n];
                        card = new IntDomainVar[vars.length - n];
                        System.arraycopy(vars, 0, varT, 0, n);
                        System.arraycopy(vars, n, card, 0, card.length);


                        return new BoundGccVar(varT, card, min, max, solver.getEnvironment());
                    case GLOBALCARDINALITYVARVALUES:
                        values = (int[]) params[1];
                        n = (Integer) params[2];
                        vars = solver.getVar(variables);
                        varT = new IntDomainVar[n];
                        card = new IntDomainVar[vars.length - n];
                        System.arraycopy(vars, 0, varT, 0, n);
                        System.arraycopy(vars, n, card, 0, card.length);

                        min = values[0];
                        max = values[values.length - 1];

                        for (int v = 0; v < varT.length; v++) {
                            IntDomainVar var = varT[v];
                            if (min > var.getInf()) {
                                min = var.getInf();
                            }
                            if (max < var.getSup()) {
                                max = var.getSup();
                            }
                        }
                        IntDomainVar[] _card = new IntDomainVar[max - min + 1];
                        k = 0;
                        for (int i = min; i <= max; i++) {
                            if (k < values.length && i == values[k]) {
                                _card[i - min] = card[k];
                                k++;
                            } else {
                                _card[i - min] = solver.createBoundIntVar(StringUtils.randomName(), 0, varT.length);
                            }
                        }

                        return new BoundGccVar(varT, _card, min, max, solver.getEnvironment());

                    /*if (GLOBALCARDINALITY.equals(type)) {
                        int[] low = (int[]) params[1];
                        int[] up = (int[]) params[2];
                        IntDomainVar[] vars = solver.getVar(variables);
                        if (options.contains(Options.C_GCC_AC)) {
                            return new GlobalCardinality(vars, 1, low.length, low, up, solver.getEnvironment());
                        }
                        if (options.contains(Options.C_GCC_BC)) {
                            return new BoundGcc(vars, 1, low.length, low, up, solver.getEnvironment());
                        }
                        if ((vars[0]).hasEnumeratedDomain()) {
                            return new GlobalCardinality(vars, 1, low.length, low, up, solver.getEnvironment());
                        } else {
                            return new BoundGcc(vars, 1, low.length, low, up, solver.getEnvironment());
                        }
                    }*/
                }
            }
        }
        throw new ModelException("Could not found a constraint manager in " + this.getClass() + " !");
    }

    public int[] getFavoriteDomains
            (List<String> options) {
        if (options.contains(Options.C_GCC_BC)) {
            return getBCFavoriteIntDomains();
        } else {
            return getACFavoriteIntDomains();
        }
    }
}
