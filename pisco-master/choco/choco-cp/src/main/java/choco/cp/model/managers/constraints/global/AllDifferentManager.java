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
import choco.cp.solver.constraints.global.BoundAllDiff;
import choco.cp.solver.constraints.global.matching.AllDifferent;
import choco.kernel.memory.IEnvironment;
import choco.kernel.model.ModelException;
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
 * A manager to build new all different constraints (and more... soon)
 */
public final class AllDifferentManager extends IntConstraintManager {

    public SConstraint makeConstraint(Solver solver, IntegerVariable[] vars, Object parameters, List<String> options) {
        if (solver instanceof CPSolver) {
            IntDomainVar[] variables = solver.getVar((IntegerVariable[]) vars);
            if (options.contains(Options.C_ALLDIFFERENT_AC))
                return new AllDifferent(variables, solver.getEnvironment());
            if (options.contains(Options.C_ALLDIFFERENT_BC))
//                return new PropAllDiffBC(variables, solver);
                return new BoundAllDiff(variables, true);
            if (options.contains(Options.C_ALLDIFFERENT_CLIQUE))
                return new BoundAllDiff(variables, false);

            return defaultDetection(variables, solver.getEnvironment());
        }
        throw new ModelException("Could not found a constraint manager in " + this.getClass() + " !");
    }

    public int[] getFavoriteDomains(List<String> options) {
        if (options.contains(Options.C_ALLDIFFERENT_BC)) {
            return getBCFavoriteIntDomains();
        } else {
            return getACFavoriteIntDomains();
        }
    }

    /**
     * make a choice if the user didn't specify the type of consistency desired
     * @param vars
     * @param environment
     * @return
     */
    public SConstraint defaultDetection(IntDomainVar[] vars, IEnvironment environment) {
            int maxdszise = 0;
            int nbnoninstvar = 0;
            boolean holes = false;
            boolean boundOnly = true;
            for (int i = 0; i < vars.length; i++) {
                boundOnly &= !vars[i].hasEnumeratedDomain();
                int span = vars[i].getSup() - vars[i].getInf() + 1;
                if (vars[i].getDomainSize() > maxdszise) {
                    maxdszise = vars[i].getDomainSize();
                }
                if (vars[i].getDomainSize() > 1) nbnoninstvar++;
                holes |= 0.7 * span > vars[i].getDomainSize();
            }

            if (vars.length <= 3) {//very small cliques
                return new BoundAllDiff(vars, false);
            } else if (boundOnly) {
                return new BoundAllDiff(vars,true);
            } else if (holes || (maxdszise <= 30 &&
                      (vars.length <= 10 ||
                      (nbnoninstvar < vars.length && nbnoninstvar < 20)))) {
                //clique containing relatively small domains (less than 30) and
                //instantiated variables (so less than 20 real variables)
                return new AllDifferent(vars, environment);
            }
            //return new AllDifferent(vars);
            return new BoundAllDiff(vars,true);
    }
}
