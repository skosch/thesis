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

package choco.model.constraints.global;

import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.SortingSConstraint;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 4 juin 2007
 * Time: 16:45:46
 */
public class SortingTest {

	protected final static Logger LOGGER = ChocoLogging.getTestLogger();

	@Test
	public void testSorting() {
		CPModel m = new CPModel();
		IntegerVariable[] x = {
				makeIntVar("x0", 1, 16),
				makeIntVar("x1", 5, 10),
				makeIntVar("x2", 7, 9),
				makeIntVar("x3", 12, 15),
				makeIntVar("x4", 1, 13)
		};
		IntegerVariable[] y = {
				makeIntVar("y0", 2, 3),
				makeIntVar("y1", 6, 7),
				makeIntVar("y2", 8, 11),
				makeIntVar("y3", 13, 16),
				makeIntVar("y4", 14, 18)
		};
		Constraint c = sorting(x, y);
		m.addConstraint(c);
		CPSolver s = new CPSolver();
		s.read(m);
		try {
			((SortingSConstraint)s.getCstr(c)).boundConsistency();
		}
		catch (ContradictionException e) {
			assertTrue(false);
			e.printStackTrace();
		}
	}

	@Test
	public void testSorting2() {
		for (int seed = 0; seed < 1; seed++) {
			CPModel m = new CPModel();
			int n = 3;
			IntegerVariable[] x = makeIntVarArray("x", n, 0, n);
			IntegerVariable[] y = makeIntVarArray("y", n, 0, n);
			Constraint c = sorting(x, y);
			m.addConstraint(c);
			m.addConstraint(allDifferent(x));
			CPSolver s = new CPSolver();
			s.read(m);
			//            s.setValIntSelector(new RandomIntValSelector(seed));
			//            s.setVarIntSelector(new RandomIntVarSelector(s, seed + 2));
			s.solve();
			HashSet<String> sols = new HashSet<String>();
			if(s.isFeasible()){
				do{
					StringBuffer st = new StringBuffer();
					st.append(s.getVar(x[0]).getVal());
					for(int i = 1; i < n; i++){
						st.append(",").append(s.getVar(x[i]).getVal());
					}
					//                    st.append(" - ").append(s.getVar(y[0]).getVal());
					//                    for(int i = 1; i < n; i++){
					//                        st.append(",").append(s.getVar(y[i]).getVal());
					//                    }
					sols.add(st.toString());
					LOGGER.info(st.toString());
				}while(s.nextSolution());
			}

			LOGGER.info("---------------");
			CPSolver s1 = new CPSolver();
			s1.read(m);
			//            s.setValIntSelector(new RandomIntValSelector(seed));
			//            s.setVarIntSelector(new RandomIntVarSelector(s, seed + 2));
			s1.setVarIntSelector(new StaticVarOrder(s1, s1.getVar((IntegerVariable[]) ArrayUtils.append(x,y))));
			s1.setValIntIterator(new IncreasingDomain());
			s1.solve();
			if(s1.isFeasible()){
				do{
					StringBuffer st = new StringBuffer();
					st.append(s1.getVar(x[0]).getVal());
					for(int i = 1; i < n; i++){
						st.append(",").append(s1.getVar(x[i]).getVal());
					}
					//                    st.append(" - ").append(s1.getVar(y[0]).getVal());
					//                    for(int i = 1; i < n; i++){
					//                        st.append(",").append(s1.getVar(y[i]).getVal());
					//                    }
					sols.remove(st.toString());
					LOGGER.info(st.toString());
				}while(s1.nextSolution());
			}
			if(LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("########");
				LOGGER.info(Arrays.toString(sols.toArray()));
				LOGGER.log(Level.INFO,"{0} - {1}:{2}", new Object[]{n, s.getNbSolutions(), s1.getNbSolutions()});
			}
			assertEquals(s.getNbSolutions(), s1.getNbSolutions());
			//            assertEquals(840, s1.getNbSolutions());
		}

	}

	@Test
	public void testName() {
		CPModel m = new CPModel();
		int n = 3;
		IntegerVariable[] x = makeIntVarArray("x", n, 0, n);
		IntegerVariable[] y = makeIntVarArray("y", n, 0, n);
		m.addConstraint(sorting(x, y));
		m.addConstraint(allDifferent(x));
		CPSolver s = new CPSolver();
		s.read(m);
		s.setVarIntSelector(new StaticVarOrder(s, s.getVar(x)));
		s.setValIntIterator(new IncreasingDomain());
		s.solve();
		if(s.isFeasible()){
			do{
				StringBuffer st = new StringBuffer();
				st.append(s.getVar(x[0]).getVal());
				for(int i = 1; i < n; i++){
					st.append(",").append(s.getVar(x[i]).getVal());
				}
				LOGGER.info(st.toString());
			}while(s.nextSolution());
		}
		LOGGER.log(Level.INFO, "{0}", s.getNbSolutions());
	}
}
