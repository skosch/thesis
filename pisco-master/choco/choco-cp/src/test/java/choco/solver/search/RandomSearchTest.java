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

package choco.solver.search;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import org.junit.Test;

import static choco.Choco.*;
import static org.junit.Assert.assertEquals;

/**
 * <p/>
 * An open-source Constraint Programming Kernel
 * for Research and Education
 * <p/>
 * Created by: Guillaume on 3 nov. 2004
 */
public class RandomSearchTest {
	
	public final static int nbQueensSolution[] = {0, 0, 0, 0, 2, 10, 4, 40, 92, 352, 724, 2680, 14200, 73712};

	
	private Model nQueen(int n) {
		final Model m = new CPModel();
		final IntegerVariable[] queens = Choco.makeIntVarArray("Q", n, 0 , n - 1);
		// diagonal constraints
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				int k = j - i;
				m.addConstraint(neq(queens[i], queens[j]));
				m.addConstraint(neq(queens[i], plus(queens[j], k)));
				m.addConstraint(neq(queens[i], minus(queens[j], k)));
			}
		}
		return m;
	}
	
	public void testNQueens(int n, String...options) {
		Solver s = new CPSolver();
		for (String string : options) {
			s.getConfiguration().putTrue(string);
		}
		s.read( nQueen(n));
		s.setVarIntSelector(new RandomIntVarSelector(s));
		s.setValIntSelector(new RandomIntValSelector());
		s.solveAll();
		if (n >= 4) {
			if (n <= 13) {
				assertEquals(Boolean.TRUE, s.isFeasible());
				assertEquals(nbQueensSolution[n], s.getNbSolutions());
			}
		} else {
			assertEquals(Boolean.FALSE, s.isFeasible());
		}
	}
	
	@Test
	public void testNQueens() {
			testNQueens(8);
	}
	
	@Test
	public void testNQueens2() {
		testNQueens(4, Configuration.INIT_SHAVING);
	}
	
	@Test
	public void testNQueens3() {
		testNQueens(8, Configuration.INIT_SHAVING);
	}
	
	@Test
	public void testNQueens4() {
		//ChocoLogging.setVerbosity(Verbosity.SEARCH);
		Solver s = new CPSolver();
		s.getConfiguration().putTrue(Configuration.INIT_SHAVING);
		s.read( nQueen(4));
		s.generateSearchStrategy();
		s.getSearchStrategy().getShavingTools().setDetectLuckySolution(true);
		s.launch();
		assertEquals(Boolean.TRUE, s.isFeasible());
		assertEquals(1, s.getNodeCount());
	}
}
