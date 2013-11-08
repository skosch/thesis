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

package choco.model.constraints;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.matching.AllDifferent;
import choco.cp.solver.constraints.integer.IntLinComb;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.Test;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 10 mars 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public class LangfordNumberTest {


    IntDomainVar[] decVars;

    // n the size of the number of dist
    // dist[i] distance between two occurences of the same color, |dist| = n
    // k the number of occurences of each number, here k = 2
    public void langfordNumber(CPSolver ls, int n, int[] dist) {
        IEnvironment env = ls.getEnvironment();
        // allvars[i] index of the first occurence of color i
        // allvars[i+n] index of the second occurence of color i
        IntDomainVar[] vars = new IntDomainVar[2*n];
        decVars = new IntDomainVar[n];
        for (int i = 0; i < 2*n; i++) {
            vars[i] = ls.createEnumIntVar("oc"+i,0,2*n-1);
            if (i < n) {
                decVars[i] = vars[i];
            }
        }

        for (int i = 0; i < n; i++) {
            // this.vars[i+n] - this.vars[i] = dist[i];
            ls.post(new IntLinComb(new IntDomainVar[]{vars[i+n],vars[i]},
                                    new int[]{1,-1}, 1, dist[i]+1, IntLinComb.EQ));
        }

        ls.post(new AllDifferent(vars, env));
    }


    @Test
    public void langfordNumber() {
        CPSolver ls = new CPSolver();
//        ls.unsafeSetPropagationEngine(new StaticEngine(ls));
        int[] dist = {1,2,3,4,5,6,7};
        langfordNumber(ls, 7, dist);
        ls.attachGoal(new AssignVar(new StaticVarOrder(ls, decVars), new IncreasingDomain()));
        //SettChocoLogging.setVerbosity(Verbosity.SOLUTION);
        ls.solveAll();
    }

}
