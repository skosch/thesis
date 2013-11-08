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

import static choco.Choco.isIncluded;
import static choco.Choco.makeSetVar;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.set.RandomSetValSelector;
import choco.cp.solver.search.set.RandomSetVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.Solver;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 23 avr. 2008
 * Time: 14:27:02
 */
public class SetIsIncludedTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    Model m;
    Solver s;

    @After
    public void tearDown() throws Exception {
        s = null;
        m = null;
    }

    @Before
    public void setUp() throws Exception {
        m = new CPModel();
        s = new CPSolver();
    }

    @Test
    public void test2IsContained() {
		for (int seed = 0; seed < 20; seed++) {
            m = new CPModel();
            s = new CPSolver();
            SetVariable v1 = makeSetVar("v1", 3, 4);
			SetVariable v2 = makeSetVar("v2", 3, 8);

			m.addConstraint(isIncluded(v1, v2));
			//pb.post(pb.setInter(v1, v2, v1));
			s.read(m);
			s.setVarSetSelector(new RandomSetVarSelector(s, seed));
            s.setValSetSelector(new RandomSetValSelector(seed+1));
            s.solveAll();
			LOGGER.info(" " + s.getNbSolutions());

			assertTrue(144 == s.getNbSolutions());
		}
	}

    public Solver modelWihtArrays(int seed) {
        Model m = new CPModel();
        Solver s = new CPSolver();
        int[] set1 = new int[]{2};
        int[] set2 = new int[]{3};
        SetVariable v1 = makeSetVar("v1", set1);
        SetVariable v2 = makeSetVar("v2", set2);

        m.addConstraint(isIncluded(v1, v2));
        s.read(m);
        s.setVarSetSelector(new RandomSetVarSelector(s, seed));
        s.setValSetSelector(new RandomSetValSelector(seed+1));
        return s;
	}

    public Solver modelWihtoutArrays(int seed) {
        Model m = new CPModel();
        Solver s = new CPSolver();
        SetVariable v1 = makeSetVar("v1", 2,2);
        SetVariable v2 = makeSetVar("v2", 3,3);

        m.addConstraint(isIncluded(v1, v2));
        s.read(m);
        s.setVarSetSelector(new RandomSetVarSelector(s, seed));
        s.setValSetSelector(new RandomSetValSelector(seed+1));
        return s;
	}

    @Test
    public void test4IsContained() {
		for (int seed = 0; seed < 20; seed++) {
            Solver s1 = modelWihtArrays(seed);
            Solver s2 = modelWihtoutArrays(seed);
            Assert.assertEquals(s1.solveAll(), s2.solveAll());
            Assert.assertEquals("Number of solutions",s1.getNbSolutions(), s2.getNbSolutions());
            Assert.assertEquals("Number of nodes",s1.getSearchStrategy().getNodeCount(), s2.getSearchStrategy().getNodeCount());
		}
	}
}
