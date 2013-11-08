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

package choco.solver.goals;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.goals.choice.Generate;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.Test;

import java.util.logging.Logger;

import static choco.Choco.*;
import static org.junit.Assert.assertEquals;


/**
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 20 mars 2008
 * Time: 08:35:12
 */
public class SearchTest {
	
	protected final static Logger LOGGER = ChocoLogging.getTestLogger();

	@Test
	public void testnode() {
		//ChocoLogging.setVerbosity(Verbosity.SEARCH);
        int n1 = testNQueens(true);
        int n2 = testNQueens(false);
        assertEquals("Nb Nodes", n1 ,n2);
	}

	//return the number of nodes needed to solve the problem
	private int testNQueens(boolean withgoal) {
		int NB_REINES = 8;

		Model m = new CPModel();


		IntegerVariable[] vars = new IntegerVariable[NB_REINES];
		for (int i = 0; i < NB_REINES; i++) {
			vars[i] = makeIntVar("x" + i, 0, NB_REINES - 1);
		}

		for (int i = 0; i < NB_REINES; i++) {
			for (int j = i + 1; j < NB_REINES; j++) {
				m.addConstraint(neq(vars[i], vars[j]));
			}                                                                                             
		}

		for (int i = 0; i < NB_REINES; i++) {
			for (int j = i + 1; j < NB_REINES; j++) {
				int k = j - i;
				m.addConstraint(neq(vars[i], plus(vars[j], k)));
				m.addConstraint(neq(vars[i], minus(vars[j], k)));
			}
		}

		Solver s = new CPSolver();
		s.read(m);
		if (withgoal) {
            s.setIlogGoal(new Generate(s.getVar(vars)));
        }else{
            s.addGoal(new AssignVar(new MinDomain(s), new IncreasingDomain()));
        }

		s.solveAll();
		LOGGER.info("Nb solutions = " + s.getNbSolutions());
		return s.getNodeCount();
	}

}