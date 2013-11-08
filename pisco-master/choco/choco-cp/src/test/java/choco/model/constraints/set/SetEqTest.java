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

package choco.model.constraints.set;

import static choco.Choco.eq;
import static choco.Choco.makeSetVar;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.set.RandomSetValSelector;
import choco.cp.solver.search.set.RandomSetVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.Solver;
import static junit.framework.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 23 avr. 2008
 * Time: 14:28:12
 */
public class SetEqTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    Model m;
    Solver s;

    @After
    public void tearDown() throws Exception {
        LOGGER.info(s.pretty());
        s = null;
        m = null;
    }

    @Before
    public void setUp() throws Exception {
        m = new CPModel();
        s = new CPSolver();
    }

    @Test
    public void test2SetEq() {
		for (int seed = 1; seed < 20; seed++) {
            m = new CPModel();
            s = new CPSolver();
            SetVariable v1 = makeSetVar("v1", 1, 2);
			SetVariable v2 = makeSetVar("v2", 1, 2);

            m.addConstraint(eq(v1, v2));
			s.read(m);
			s.setVarSetSelector(new RandomSetVarSelector(s, seed));
            s.setValSetSelector(new RandomSetValSelector(seed+1));
            s.solveAll();
            assertEquals(4, s.getNbSolutions());
		}
	}
}