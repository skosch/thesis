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

package choco.model.constraints.integer;

import static choco.Choco.eq;
import static choco.Choco.makeIntVar;
import static choco.Choco.makeIntVarArray;
import static choco.Choco.makeSetVar;
import static choco.Choco.member;
import static choco.Choco.min;
import static choco.model.constraints.integer.MaxTest.testAll;
import static java.text.MessageFormat.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;

/**
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 30 janv. 2007
 * Time: 10:49:34
 * To change this template use File | Settings | File Templates.
 */
public class MinTest{

	protected final static Logger LOGGER = ChocoLogging.getTestLogger();

	private CPModel m;
	private CPSolver s;

	@Before
	public void before(){
		m = new CPModel();
		s = new CPSolver();
	}

	@After
	public void after(){
		m = null;
		s = null;
	}

	@Test
	public void test1() {
		for (int i = 0; i <= 10; i++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 5);
			IntegerVariable y = makeIntVar("y", 1, 5);
			IntegerVariable z = makeIntVar("z", 1, 5);
			IntegerVariable w = makeIntVar("w", 1, 5);
			m.addConstraint(min(new IntegerVariable[]{x, y, z},w));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, i));
			s.setValIntSelector(new RandomIntValSelector(i + 1));
			s.solve();
			do {
				/*LOGGER.info("" + x.getVal() + "=max(" + y.getVal() + "," +
                z.getVal()+")");*/
			} while (s.nextSolution() == Boolean.TRUE);
			LOGGER.info("" + s.getSearchStrategy().getNodeCount());
			assertEquals(125, s.getNbSolutions());
			//LOGGER.info("Nb solution : " + s.getNbSolutions());
		}
	}

	@Test
	public void test2() {
		for (int i = 0; i <= 10; i++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 5);
			IntegerVariable y = makeIntVar("y", 1, 5);
			IntegerVariable z = makeIntVar("z", 1, 5);
			m.addVariables(Options.V_BOUND, x, y, z);
			IntegerVariable w = makeIntVar("w", 1, 5);
			m.addConstraint(min(new IntegerVariable[]{x, y, z},w));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, i));
			s.setValIntSelector(new RandomIntValSelector(i + 1));
			s.solve();
			do {
				//LOGGER.info("" + x.getVal() + "=max(" + y.getVal() + "," +
				//�    z.getVal()+")");
			} while (s.nextSolution() == Boolean.TRUE);
			LOGGER.info("" + s.getSearchStrategy().getNodeCount());
			assertEquals(125, s.getNbSolutions());
			//LOGGER.info("Nb solution : " + s.getNbSolutions());
		}
	}

	@Test
	public void testPropagMinTern1() {

		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 4, 5);
		IntegerVariable min = makeIntVar("min", 1, 5);
		m.addConstraint(min(z, y, min));
		s.read(m);
		try {
			s.getVar(min).remVal(3);
			s.propagate();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		LOGGER.info(format("min {0}", s.getVar(min).getDomain().pretty()));
		LOGGER.info(format("y {0}", s.getVar(y).getDomain().pretty()));
		LOGGER.info(format("z {0}", s.getVar(z).getDomain().pretty()));
		LOGGER.info(format("{0}", !s.getVar(y).canBeInstantiatedTo(3)));
		assertTrue(!s.getVar(y).canBeInstantiatedTo(3));
	}

	@Test
	public void testPropagMinTern2() {

		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		IntegerVariable min = makeIntVar("min", 1, 5);
		m.addConstraint(min(z, y, min));
		s.read(m);
		try {
			s.getVar(y).remVal(3);
			s.propagate();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		LOGGER.info(format("min {0}", s.getVar(min).getDomain().pretty()));
		LOGGER.info(format("y {0}", s.getVar(y).getDomain().pretty()));
		LOGGER.info(format("z {0}", s.getVar(z).getDomain().pretty()));
		LOGGER.info(format("{0}", s.getVar(z).canBeInstantiatedTo(3) && s.getVar(min).canBeInstantiatedTo(3)));
		assertTrue(s.getVar(z).canBeInstantiatedTo(3) && s.getVar(min).canBeInstantiatedTo(3));
	}

	@Test
	public void testPropagMinTern3() {
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		IntegerVariable min = makeIntVar("min", 1, 5);
		m.addConstraint(min(z, y, min));
		s.read(m);
		try {
			s.getVar(min).remVal(3);
			s.propagate();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		LOGGER.info(format("min {0}", s.getVar(min).getDomain().pretty()));
		LOGGER.info(format("y {0}", s.getVar(y).getDomain().pretty()));
		LOGGER.info(format("z {0}", s.getVar(z).getDomain().pretty()));
		LOGGER.info(format("{0}", s.getVar(y).canBeInstantiatedTo(3) && s.getVar(z).canBeInstantiatedTo(3)));
		assertTrue(s.getVar(y).canBeInstantiatedTo(3) && s.getVar(z).canBeInstantiatedTo(3));
	}

	@Test
	public void testPropagMinTern4() {
		IntegerVariable y = makeIntVar("y", 1, 3);
		IntegerVariable z = makeIntVar("z", 4, 6);
		IntegerVariable min = makeIntVar("min", 1, 6);
		m.addConstraint(min(z, y, min));
		s.read(m);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		LOGGER.info(format("min {0}", s.getVar(min).getDomain().pretty()));
		LOGGER.info(format("y {0}", s.getVar(y).getDomain().pretty()));
		LOGGER.info(format("z {0}", s.getVar(z).getDomain().pretty()));
		LOGGER.info(format("{0}", s.getVar(min).getDomain().getSize() == 3));
		assertTrue(s.getVar(min).getDomain().getSize() == 3);
	}


	@Test
	public void testRandom() {

		for (int i = 0; i < 10; i++) {

			m = new CPModel();
			s = new CPSolver();
			IntegerVariable varA = makeIntVar("varA", 0, 3);
			IntegerVariable varB = makeIntVar("varB", 0, 3);
			IntegerVariable varC = makeIntVar("varC", 0, 3);
			m.addConstraint(min(varA, varB, varC));
			s.read(m);

			//-----Now get solutions
			s.setFirstSolution(true);
			s.generateSearchStrategy();
			s.setValIntSelector(new RandomIntValSelector(100 + i));
			s.setVarIntSelector(new RandomIntVarSelector(s, 101 + i));

			//LOGGER.info("Choco Solutions");
			int nbSolution = 0;
			if (s.solve() == Boolean.TRUE) {
				do {
					//LOGGER.info("Min(" + ((IntegerVariable) chocoCSP.getIntVar(0)).getVal() + ", " + ((IntegerVariable) chocoCSP.getIntVar(1)).getVal() + ") = " + ((IntegerVariable) chocoCSP.getIntVar(2)).getVal());
					nbSolution++;
				} while (s.nextSolution() == Boolean.TRUE);
			}
			assertEquals(nbSolution, 16);
		}
	}

	@Test
	public void testConstant(){
		for (int i = 0; i < 10; i++) {

			m = new CPModel();
			s= new CPSolver();


			IntegerVariable x = makeIntVar("x", 0, 3);
			IntegerVariable y = makeIntVar("y", 0, 3);
			m.addConstraint(eq(y, min(x, 1)));

			//-----Now get solutions

			s.read(m);
			s.setFirstSolution(true);
			s.generateSearchStrategy();
			s.setValIntSelector(new RandomIntValSelector(100 + i));
			s.setVarIntSelector(new RandomIntVarSelector(s, i));


			//LOGGER.info("Choco Solutions");
			int nbSolution = 0;
			if (s.solve() == Boolean.TRUE) {
				do {
					//LOGGER.info("Max(" + ((IntegerVariable) chocoCSP.getIntVar(0)).getVal() + ", " + ((IntegerVariable) chocoCSP.getIntVar(1)).getVal() + ") = " + ((IntegerVariable) chocoCSP.getIntVar(2)).getVal());
					nbSolution++;
				} while (s.nextSolution() == Boolean.TRUE);
			}

			assertEquals(nbSolution, 4);
		}
	}
	@Test
	public void testSet1() {
		testAll(true,true);
	}

	@Test
	public void testSet2() {
		testAll(true,false);
	}

	@Test
	public void testEmptySetDefPolicy() {
		IntegerVariable[] vars = Choco.constantArray(new int[]{1,2,3});
		IntegerVariable min = makeIntVar("min", 0, 3);
		SetVariable svar = makeSetVar("sv", 0, 2);
		m.addConstraint(min(svar, vars, min, Options.C_MINMAX_INF));
		s.read(m);
		s.solveAll();
		assertEquals("nb-sols", 8, s.getNbSolutions());        
	}

	@Test
	public void testOneVarMin() {
		IntegerVariable[] vars = makeIntVarArray("vars", 1, 3, 5);
		IntegerVariable min = makeIntVar("min", 1, 6);
		m.addConstraint(eq(min, min(vars)));
		s.read(m);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		LOGGER.info("min " + s.getVar(min).getDomain().pretty());
		assertTrue(s.getVar(min).getDomain().getSize() == 3);
	}

	private void checkVar(IntDomainVar mv, int inf, int sup) {
		//LOGGER.info(mv.pretty()); 
		assertEquals(inf, mv.getInf());
		assertEquals(sup, mv.getSup());
		assertEquals(sup-inf+1, mv.getDomainSize());

	}

	@Test
	public void testSetEvents() throws ContradictionException {
		final IntegerVariable[] vars = new IntegerVariable[]{
				makeIntVar("v1", 0, 20),
				makeIntVar("v2", 5, 10),
				makeIntVar("v3", 3, 8),
				makeIntVar("v4", 15, 18)        	
		};
		final IntegerVariable min = makeIntVar("min", -10, 25);
		final SetVariable svar = makeSetVar("sv", 0, 3);
		final Constraint c = min(svar, vars, min);
		m.addConstraint(c);
		s.read(m);
		s.propagate();

		final IntDomainVar mv = s.getVar(min);
		final SetVar sv = s.getVar(svar);

		//ChocoLogging.getTestLogger().setLevel(Level.INFO);
		//checkMin(mv, -1, 20);

		sv.addToKernel(3, null, false);
		s.propagate();
		checkVar(mv, 0, 18);
		sv.remFromEnveloppe(0, null, false);
		s.propagate();
		checkVar(mv, 3, 18);
		mv.updateInf(9, null, true);
		s.propagate();
		assertFalse(sv.isInDomainEnveloppe(2));
		s.getVar(vars[1]).updateInf(10, null, true);
		s.propagate();
		checkVar(mv, 10, 18);
		mv.updateInf(13, null, true);
		s.propagate();
		checkVar(mv, 15, 18);
		assertFalse(sv.isInDomainEnveloppe(1));
		mv.updateSup(17, null, true);
		s.propagate();
		checkVar(s.getVar(vars[3]), 15, 17);
		s.getVar(vars[3]).updateSup(16, null, true);
		s.propagate();
		checkVar(mv, 15, 16);
		s.getVar(vars[3]).instantiate(16, null, true);
		s.propagate();
		checkVar(mv, 16, 16);
	}


	private void checkSwap(IntDomainVar v1, IntDomainVar v2) {
		assertEquals(v1.getInf(), - v2.getSup());
		assertEquals(v1.getSup(), - v2.getInf());
	}

	@Test
	public void setMinMaxShaker() throws ContradictionException {
		for (int p = 0; p < 20; p++) {
			int n = 30;
			int l = 50;
			int nb = 100;
			Random rnd = new Random(0);
			final SetVariable minsv = makeSetVar("S", 0, n-1);
			final SetVariable maxsv = makeSetVar("S", 0, n-1);
			final IntegerVariable[] minv = new IntegerVariable[n];
			final IntegerVariable[] maxv = new IntegerVariable[n];
			for (int i = 0; i < n; i++) {
				final int inf = rnd.nextInt(l);
				minv[i] = makeIntVar("minv"+(i+1), inf, inf + rnd.nextInt(l));
				maxv[i] = makeIntVar("maxv"+(i+1),  - minv[i].getUppB(), - minv[i].getLowB());
			}
			final IntegerVariable min = makeIntVar("m", 0, 2*l);
			final IntegerVariable max = makeIntVar("m", - 2*l, 0);

			m = new CPModel();
			m.addConstraints(
					Choco.max(maxsv, maxv, max),
					Choco.min(minsv, minv, min),
					member(0, minsv),
					member(0, maxsv)
					);
			s = new CPSolver();
			s.read(m);
			final SetVar minsvar = s.getVar(minsv);
			final SetVar maxsvar = s.getVar(maxsv);
			final IntDomainVar[] minvars = s.getVar(minv);
			final IntDomainVar[] maxvars = s.getVar(maxv);
			final IntDomainVar minvar = s.getVar(min);
			final IntDomainVar maxvar = s.getVar(max);
			for (int k = 0; k < nb; k++) {
				s.propagate();
				for (int i = 0; i < n; i++) {
					checkSwap(minvars[i], maxvars[i]);
				}
				checkSwap(minvar, maxvar);
				assertEquals(minsvar.getKernelDomainSize(), maxsvar.getKernelDomainSize());
				assertEquals(minsvar.getEnveloppeDomainSize(), maxsvar.getEnveloppeDomainSize());

				switch(rnd.nextInt(6)) {
				case 0 : {
					if(minsvar.getKernelDomainSize() > 0) {
						int idx = rnd.nextInt(minsvar.getKernelDomainSize());
						DisposableIntIterator iter = minsvar.getDomain().getKernelIterator();
						for (int i = 0; i < idx; i++) {
							iter.next();
						}
						idx = iter.next();
						minsvar.addToKernel(idx, null, true);
						maxsvar.addToKernel(idx, null, true);
						iter.dispose();
					}
					break;
				}
				case 1 : {
					if(minsvar.getEnveloppeDomainSize() - minsvar.getKernelDomainSize() > 0) {
						int idx = rnd.nextInt(minsvar.getEnveloppeDomainSize() - minsvar.getKernelDomainSize());
						DisposableIntIterator iter = minsvar.getDomain().getOpenDomainIterator();
						for (int i = 0; i < idx; i++) {
							iter.next();
						}
						idx = iter.next();
						minsvar.remFromEnveloppe(idx, null, true);
						maxsvar.remFromEnveloppe(idx, null, true);
						iter.dispose();
					}
					break;
				}
				case 2 : {
					final int offset = rnd.nextInt(minvar.getDomainSize());
					minvar.updateInf(minvar.getInf() + offset, null, true);
					maxvar.updateSup(maxvar.getSup() - offset, null, true);
					break;
				}
				case 3 : {
					final int offset = rnd.nextInt(minvar.getDomainSize());
					minvar.updateSup(minvar.getSup() - offset, null, true);
					maxvar.updateInf(maxvar.getInf() + offset, null, true);
					break;
				}
				case 4 : {
					final int idx = rnd.nextInt(n);
					final int offset = rnd.nextInt(minvars[idx].getDomainSize());
					minvars[idx].updateInf(minvars[idx].getInf() + offset, null, true);
					maxvars[idx].updateSup(maxvars[idx].getSup() - offset, null, true);
					break;
				}
				case 5 : {
					final int idx = rnd.nextInt(n);
					final int offset = rnd.nextInt(minvars[idx].getDomainSize());
					minvars[idx].updateSup(minvars[idx].getSup() - offset, null, true);
					maxvars[idx].updateInf(maxvars[idx].getInf() + offset, null, true);
					break;
				}

				}
				//System.out.println(s.pretty());
			}

		}
	}

}
