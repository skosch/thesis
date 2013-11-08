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

package common;

import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ResolutionPolicy;
import org.junit.Assert;
import org.junit.Test;
import samples.tutorials.to_sort.MinimumEdgeDeletion;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 26 avr. 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public class SearchTest {

    private int capa = 0;

    class PoolSwitcher extends MinimumEdgeDeletion {

        @Override
        public void buildSolver() {
            solver = new CPSolver();
            solver.read(model);
            Configuration configuration = solver.getConfiguration();
            configuration.putBoolean(Configuration.STOP_AT_FIRST_SOLUTION, false);
            configuration.putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MINIMIZE);
            configuration.putInt(Configuration.SOLUTION_POOL_CAPACITY, capa);
            solver.setValIntSelector(new MinVal());
            //solver.generateSearchStrategy();

        }

        @Override
        public void solve() {
            super.solve();
            Assert.assertEquals(Math.min(capa, solver.getNbSolutions()), solver.getSearchStrategy().getSolutionPool().size());
        }
    }

    @Test
    public void testSolutionPool() {
        //ChocoLogging.setVerbosity(Verbosity.SEARCH);
        PoolSwitcher pl = new PoolSwitcher();
        for (capa = 0; capa < 7; capa++) {
            pl.execute();
        }
        capa = Integer.MAX_VALUE;
        pl.execute();
    }

}
