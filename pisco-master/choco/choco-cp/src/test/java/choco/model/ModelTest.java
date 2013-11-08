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

package choco.model;

import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.BranchingFactory;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static java.lang.System.currentTimeMillis;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 22 juil. 2008
 * Time: 10:27:41
 * Test suite concerning Decision variables
 */
public class ModelTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

	@Test
	public void testOnNonDecisionVariables() {
		final int n = 8;
		final Model m = new CPModel();

		final IntegerVariable[] queens = new IntegerVariable[n];
		final IntegerVariable[] queensdual = new IntegerVariable[n];

		for (int i = 0; i < n; i++) {
			queens[i] = makeIntVar(String.format("Q_%d", i), 1, n);
			queensdual[i] = makeIntVar(String.format("QD_%d", i), 1, n);
		}

		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				int k = j - i;
				m.addConstraint(neq(queens[i], queens[j]));
				m.addConstraint(neq(queens[i], plus(queens[j], k)));  // diagonal constraints
				m.addConstraint(neq(queens[i], minus(queens[j], k))); // diagonal constraints
			}
		}
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				int k = j - i;
				m.addConstraint(neq(queensdual[i], queensdual[j]));
				m.addConstraint(neq(queensdual[i], plus(queensdual[j], k)));  // diagonal constraints
				m.addConstraint(neq(queensdual[i], minus(queensdual[j], k))); // diagonal constraints
			}
		}
		m.addConstraint(inverseChanneling(queens, queensdual));

		CPSolver s1 = new CPSolver();
		s1.read(m);
		//        s1.setVarIntSelector(new DomOverWDeg(s1, s1.getVar(queens)));
		s1.attachGoal(BranchingFactory.incDomWDeg(s1, s1.getVar(queens), new IncreasingDomain()));

		m.addOptions(Options.V_NO_DECISION, queensdual);
		Solver s2 = new CPSolver();
		s2.read(m);

		s1.solveAll();
		s2.solveAll();
		Assert.assertEquals("No same number of solutions", s1.getNbSolutions(), s2.getNbSolutions());
		//Assert.assertEquals("No same number of nodes", s1.getSearchStrategy().getNodeCount(), s2.getSearchStrategy().getNodeCount());
	}


	@Test
	public void testNullModel() {
		Solver s = new CPSolver();
		Model m = new CPModel();
		IntegerVariable v = makeIntVar("v", 0, 1);
		IntegerVariable w = makeIntVar("w", 0, 1);
		Constraint c = eq(v, 1);
		Constraint d = eq(v, w);

		for (int i = 0; i < 11; i++) {
			m.addVariables(v, w);
			m.addConstraints(c, d);
			StringBuilder message = new StringBuilder(i + ": ");
			try {
				switch (i) {
				case 0:
					message.append("No variable, no constraint");
					m.removeVariable(w);
					m.removeVariable(v);
					m.removeConstraint(c);
					m.removeConstraint(d);
					break;
				case 1:
					message.append("One variable, no constraint");
					m.removeVariable(w);
					m.removeConstraint(c);
					m.removeConstraint(d);
					break;
				case 2:
					message.append("One variable, One constraint");
					m.removeVariable(w);
					m.removeConstraint(d);
					break;
				case 3:
					message.append("Two variables, no constraint");
					m.removeConstraint(c);
					m.removeConstraint(d);
					break;
				case 4:
					message.append("Two variables, one constraint");
					m.removeConstraint(c);
					break;
				case 5:
					message.append("Two variables, one constraint (2)");
					m.removeConstraint(d);
					break;
				case 6:
					message.append("No variable, one constraint");
					m.removeVariables(v, w);
					m.removeConstraint(c);
					break;
				case 7:
					message.append("No variable, one constraint(2)");
					m.removeVariables(v, w);
					m.removeConstraint(d);
					break;
				case 8:
					message.append("No variable, two constraints");
					m.removeVariables(v, w);
					break;
				case 9:
					message.append("One variable, two constraints");
					m.removeVariable(v);
					break;
				case 10:
					message.append("One variable, two constraints(2)");
					m.removeVariable(w);
					break;
				}
				s.read(m);
			} catch (Exception e) {
				Assert.fail(message.toString());
			}
			Iterator<Constraint> itc;
			try {
				itc = v.getConstraintIterator(m);
				while (itc.hasNext()) {
					itc.next();
				}
			} catch (Exception e) {
				Assert.fail("v iterator");
			}
			try {
				itc = w.getConstraintIterator(m);
				while (itc.hasNext()) {
					itc.next();
				}
			} catch (Exception e) {
				Assert.fail("w iterator");
			}
			Iterator<Variable> itv;
			try {
				itv = c.getVariableIterator();
				while (itv.hasNext()) {
					itv.next();
				}
			} catch (Exception e) {
				Assert.fail("c iterator");
			}
			try {
				itv = d.getVariableIterator();
				while (itv.hasNext()) {
					itv.next();
				}
			} catch (Exception e) {
				Assert.fail("d iterator");
			}
		}

	}



	@Test
	@Ignore
	public void testCharge1(){
		int cpt;
		int newcpt;
		int[] nbVar = new int[]{10, 100, 1000, 100000, 1000000};
		for(int i = 1; i < nbVar.length; i++) {
			Runtime.getRuntime().gc();
			long t = currentTimeMillis();
			int n = nbVar[i];
			int b = 100;
			Model m = new CPModel();
			IntegerVariable[] v = makeIntVarArray("v", n, 1, b);
			m.addVariables(v);
			newcpt = (int) Runtime.getRuntime().totalMemory() - (int) Runtime.getRuntime().freeMemory();
			//                if (cpt != 0) {
			//                    assertTrue(newcpt <= cpt + 10000);
			//                }
			cpt = newcpt;
            StringBuffer st = new StringBuffer();
			st.append(MessageFormat.format("|{0} |", StringUtils.pad("" + n, -9, " ")));
			st.append(MessageFormat.format("|{0} |", StringUtils.pad("" + (currentTimeMillis() - t), -5, " ")));
			st.append(MessageFormat.format("|{0} |", StringUtils.pad("" + cpt, -10, " ")));
            LOGGER.info(st.toString());
		}
	}


	@Test
	@Ignore
	public void testCharge2(){
		int cpt;
		int newcpt;
		int[] nbCstr = new int[]{10, 100, 1000, 100000, 1000000};
		for(int i = 1; i < nbCstr.length; i++) {
			Runtime.getRuntime().gc();
			long t = currentTimeMillis();
			int n = nbCstr[i];
			int b = 10;
			Model m = new CPModel();
			IntegerVariable v = makeIntVar("v", 1, b);
			for(int j=0; j < nbCstr[i]; j++){
				m.addConstraint(eq(v, 5));
			}
			newcpt = (int) Runtime.getRuntime().totalMemory() - (int) Runtime.getRuntime().freeMemory();
			//                if (cpt != 0) {
			//                    assertTrue(newcpt <= cpt + 10000);
			//                }
			cpt = newcpt;
            StringBuffer st = new StringBuffer();
			st.append(MessageFormat.format("|{0} |", StringUtils.pad("" + n, -9, " ")));
			st.append(MessageFormat.format("|{0} |", StringUtils.pad("" + (currentTimeMillis() - t), -5, " ")));
			st.append(MessageFormat.format("|{0} |", StringUtils.pad("" + cpt, -10, " ")));
            LOGGER.info(st.toString());
		}
	}


	@Test
	@Ignore
	public void testCharge3(){
		int cpt;
		int newcpt;
		int[] domSize = new int[]{10, 100, 1000, 100000};
		for(int i = 1; i < domSize.length; i++) {
			Runtime.getRuntime().gc();
			long t = currentTimeMillis();
			int n = domSize[i];
			IntegerVariable v = makeIntVar("v", 1, n);
			for(int j=0; j < n; j+=2){
				v.removeVal(j);
			}
			newcpt = (int) Runtime.getRuntime().totalMemory() - (int) Runtime.getRuntime().freeMemory();
			//                if (cpt != 0) {
			//                    assertTrue(newcpt <= cpt + 10000);
			//                }
			cpt = newcpt;
			StringBuffer st = new StringBuffer();
            st.append(MessageFormat.format("|{0} |", StringUtils.pad("" + n, -9, " ")));
			st.append(MessageFormat.format("|{0} |", StringUtils.pad("" + (currentTimeMillis() - t), -5, " ")));
			st.append(MessageFormat.format("|{0} |", StringUtils.pad("" + cpt, -10, " ")));
            LOGGER.info(st.toString());
		}
	}

	@Test
	public void testSeveralModels() {

		IntegerVariable v1 = makeIntVar("v1", 0, 10);
		IntegerVariable v2 = makeIntVar("v2", 0, 10);
		IntegerVariable v3 = makeIntVar("v3", 0, 10);

		CPModel m1 = new CPModel();
		CPModel m2 = new CPModel();

		m1.addConstraint(eq(v1, v2));
		m1.addConstraint(neq(v2, v3));

		Constraint ct = m1.getConstraint(0);
		m2.addConstraint(ct);

		CPSolver s1 = new CPSolver();
		CPSolver s2 = new CPSolver();

		s1.read(m1);
		s2.read(m2);

		LOGGER.info(MessageFormat.format("{0}", s1.pretty()));
		LOGGER.info(MessageFormat.format("{0}", s2.pretty()));

		s1.solveAll();
		s2.solveAll();
		org.junit.Assert.assertEquals(s2.getNbSolutions(), 11);
		org.junit.Assert.assertEquals(s1.getNbSolutions(), 110);


	}

}
