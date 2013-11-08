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

package choco.solver.search.set;

import static choco.Choco.makeSetVar;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.set.RandomSetValSelector;
import choco.cp.solver.search.set.RandomSetVarSelector;
import choco.kernel.model.Model;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.set.SetVar;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 23 avr. 2008
 * Time: 15:44:33
 */
public class RandomSetValSelectorTest {

    @Test
    public void test1() {
        for (int j = 0; j < 20; j++) {
            Model m = new CPModel();
            Solver solver = new CPSolver();

            SetVariable s1 = makeSetVar("s1", 1, 7);
            SetVariable s2 = makeSetVar("s2", 1, 7);
            m.addVariables(s1, s2);
            solver.read(m);

            RandomSetVarSelector t = new RandomSetVarSelector(solver, j);
            RandomSetValSelector p = new RandomSetValSelector(j + 1);
            int n=0;
            do {
                SetVar s = t.selectVar();
                int i = p.getBestVal(s);
                try {
                    s.remFromEnveloppe(i, null, true);
                    n++;
                } catch (ContradictionException e) {
                    Assert.fail();
                }
            } while (solver.getVar(s1).getEnveloppeDomainSize() > 0 || solver.getVar(s2).getEnveloppeDomainSize() > 0);
            assertEquals(n, 14);
        }
    }
}
