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
import choco.cp.solver.constraints.global.automata.fast_multicostregular.SoftMultiCostRegular;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.constraints.automaton.penalty.IPenaltyFunction;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Apr 27, 2010
 * Time: 5:39:49 PM
 */
public class SoftMultiCostRegularManager extends IntConstraintManager
{
@Override
public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables, Object parameters, List<String> options)
{
        IntDomainVar[] allVars = solver.getVar(variables);
        Object[] param = (Object[]) parameters;
        int xl = (Integer)param[0];
        int yl = (Integer) param[1];

        IntDomainVar[] x = new IntDomainVar[xl];
        System.arraycopy(allVars,0,x,0,xl);

        IntDomainVar[] y =  new IntDomainVar[yl];
        System.arraycopy(allVars,xl,y,0,yl);

        IntDomainVar[] z = new IntDomainVar[yl];
        System.arraycopy(allVars,xl+yl,z,0,yl);

        IntDomainVar Z = allVars[xl+2*yl];

        int offset;
        int[] indexes;
        if (param.length <= 5)
        {
                indexes = ArrayUtils.zeroToN(y.length);
                offset = 0;
        }
        else
        {
                indexes = (int[])param[2];
                offset = 1;
        }

        IPenaltyFunction[] penalty  = (IPenaltyFunction[]) param[offset+2];
        IAutomaton pi = (IAutomaton) param[offset+3];


        int[][][][] costs = (int[][][][]) param[offset+4];

        return new SoftMultiCostRegular(x,y,z,Z,indexes,penalty,pi,costs,(CPSolver)solver);



}
}
