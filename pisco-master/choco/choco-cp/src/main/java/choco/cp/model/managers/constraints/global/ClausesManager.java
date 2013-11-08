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
import choco.cp.model.managers.MixedConstraintManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.bool.sat.ClauseStore;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 16 févr. 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public final class ClausesManager extends MixedConstraintManager {

    public SConstraint makeConstraint(Solver solver, Variable[] vars, Object parameters, List<String> options) {
        if (solver instanceof CPSolver) {
            IntDomainVar[] vs = new IntDomainVar[vars.length];
            solver._to(vars, vs);
            ClauseStore cs =  new ClauseStore(vs, solver.getEnvironment());
            if (options.contains(Options.C_CLAUSES_ENTAIL)) {
                cs.setEfficientEntailmentTest();
            }
            Constraint[] constraints = (Constraint[])((Object[])parameters)[1];
            for(int c = 0; c < constraints.length; c++){
                ComponentConstraint clause = (ComponentConstraint)constraints[c];
                int offset = (Integer)clause.getParameters();
                IntegerVariable[] posLits = new IntegerVariable[offset];
                IntegerVariable[] negLits = new IntegerVariable[clause.getNbVars() - offset];
                for(int v = 0; v < clause.getNbVars(); v++){
                    if(v < offset){
                        posLits[v] = (IntegerVariable)clause.getVariables()[v];
                    }else{
                        negLits[v-offset] = (IntegerVariable) clause.getVariables()[v];
                    }
                }
                cs.addClause(solver.getVar(posLits), solver.getVar(negLits));
            }
            return cs;
        }
        throw new ModelException("Could not found a constraint manager in " + this.getClass() + " !");
    }

}
