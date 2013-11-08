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

/* File choco.currentElement.search.SolveTest.java, last modified by Francois 2 d�c. 2003 23:49:19 */
package choco.solver.search;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valiterator.DecreasingDomain;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.Propagator;
import choco.kernel.solver.search.restart.AbstractRestartStrategy;
import choco.kernel.solver.search.restart.GeometricalRestartStrategy;
import choco.kernel.solver.search.restart.LubyRestartStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import static choco.Choco.*;
import static java.text.MessageFormat.format;
import static org.junit.Assert.*;

public class SolveTest {
	protected final static Logger LOGGER = ChocoLogging.getTestLogger();
	private Model m;
	private Solver s;
	private IntegerVariable x;
	private IntegerVariable y;

	@Before
	public void setUp() {
		LOGGER.fine("StoredInt Testing...");
		m = new CPModel();
		s = new CPSolver();
		x = makeIntVar("X", 0, 5);
		y = makeIntVar("Y", 0, 1);
		m.addVariables(Options.V_BOUND,x, y);
	}

	@After
	public void tearDown() {
		x = null;
		y = null;
		m = null;
	}

	/**
	 * Memory leak test that was catastrophic...
	 */
	@Test
    @Ignore
	public void test0() {
		int[] weights = new int[]{1, 2, 3, 4, 5};
		int cpt = 0;
		int newcpt;
		int nb = 0;
		while (nb < 20) {
			Runtime.getRuntime().gc();
			LOGGER.info(format("In Use:{0}\n", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
			newcpt = (int) Runtime.getRuntime().totalMemory() - (int) Runtime.getRuntime().freeMemory();
			if (cpt != 0) {
				assertTrue(newcpt <= cpt + 10000);
			}
			cpt = newcpt;
			Model a = new CPModel();
			IntegerVariable[] bools =
				makeIntVarArray("bools", weights.length, 0, 1);
			Constraint con = eq(scalar(weights, bools), 12);
			a.addConstraint(con);
			nb++;
		}
	}

	/**
	 * currentElement the solution count for an infeasible model
	 */
	@Test
	public void test1() {
		LOGGER.finer("test1");
		m.addConstraint(eq(x, 2));
		m.addConstraint(eq(x, 3));
		s.read(m);
		s.solve(false);
		assertEquals(s.getNbSolutions(), 0);
		assertEquals(s.isFeasible(), Boolean.FALSE);
	}

	/**
	 * currentElement the solution count for instantiated model
	 */
	@Test
	public void test2() {
		LOGGER.finer("test2");
		m.addConstraint(eq(x, 2));
		m.addConstraint(eq(y, 1));
		s.read(m);
		s.solve(true);
		assertEquals(s.isFeasible(), Boolean.TRUE);
		assertEquals(s.getNbSolutions(), 1);
	}

	/**
	 * currentElement the solution count for a simple one-variable model
	 */
	@Test
	public void test3() {
		LOGGER.finer("test3");
		m.addVariable(x);
		m.addConstraint(eq(y, 1));
		s.read(m);
		s.solve(true);
		assertEquals(s.isFeasible(), Boolean.TRUE);
		assertEquals(s.getNbSolutions(), 6);
	}

	/**
	 * currentElement the solution count for a simple two-variable model
	 */
	@Test
	public void test4() {
		LOGGER.finer("test4");
		m.addVariables(x, y);
		s.read(m);
		s.solve(true);
		assertEquals(s.isFeasible(), Boolean.TRUE);
		assertEquals(s.getNbSolutions(), 12);
	}

	/**
	 * currentElement the incremental solve.
	 */
	@Test
	public void test5() {
		LOGGER.finer("test5");
		m.addVariables(x, y);
		s.read(m);
		s.solve();
		while (s.nextSolution() == Boolean.TRUE) {
		}
		s.printRuntimeStatistics();
		assertEquals(s.isFeasible(), Boolean.TRUE);
		assertEquals(s.getNbSolutions(), 12);
	}

	/**
	 * currentElement redondqnt post
	 */
	@Test
	public void test6() {
		LOGGER.finer("tesT6");
		Constraint c = eq(y, 1);
		s.worldPush();
		m.addConstraint(c);
		s.read(m);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		s.worldPop();
		m.addConstraint(c);
	}

	@Test
	public void testBugTPetit150205() {
		m.removeVariables(x, y);
		int n = 3;
		IntegerVariable[] vars = new IntegerVariable[n];
		for (int i = 0; i < n; i++) {
			vars[i] = makeIntVar("debut " + i + " :" + i, 1, n);
		}
		ArrayList<Constraint> list = new ArrayList<Constraint>();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i != j) {
					Constraint c = neq(vars[i], vars[j]);
					list.add(c);
					m.addConstraint(c);
				}
			}
		}

		s.read(m);
		int FAILPB = 0; // try any value but 0 => no m
		for (int j = 0; j < 5; j++) {
			for (Constraint constraint : list) {
				((Propagator) s.getCstr(constraint)).constAwake(true);
			}

			s.worldPush();

			Constraint ct = eq(vars[0], 1);
			Constraint ct2 = eq(vars[1], 1);
			if (j == FAILPB) { // no solution
				m.addConstraints(ct, ct2);
			}
			s.read(m);
			s.solveAll();
			if (s.getNbSolutions() > 0) {
				StringBuffer st = new StringBuffer();
				for (int i = 0; i < n; i++) {
					st.append(format("{0} ", s.getVar(vars[i]).getVal()));
				}
				LOGGER.info(st.toString());
			}
			if (FAILPB == j) {
				assertEquals(s.getNbSolutions(), 0);
			}
			if (FAILPB != j) {
				assertEquals(s.getNbSolutions(), 6);
			}
			s.worldPopUntil(0);

			/*   if (j == FAILPB) {
                            ct.delete();
                            ct2.delete();
                          } */
		}

	}

	@Test
	public void testNbNodes() {
		m.removeVariables(x, y);
		IntegerVariable v1 = makeIntVar("v1", 1, 4);
		IntegerVariable v2 = makeIntVar("v2", 1, 3);
		IntegerVariable v3 = makeIntVar("v3", 1, 2);

		m.addConstraint(gt(v1, v2));
		m.addConstraint(gt(v2, v3));
		s.read(m);

		s.attachGoal(new AssignVar(new MinDomain(s), new IncreasingDomain()));
		s.solve(false);
		LOGGER.info(m.pretty());

		int time = s.getSearchStrategy().getTimeCount();
		int nds = s.getSearchStrategy().getNodeCount();
		assertEquals(2, nds);
		LOGGER.info(" time: " + time + " nodes: " + nds);
	}

	@Test
	public void test7() {
		int n = 10;
		IntegerVariable[] bvars = makeIntVarArray("b", n, 0, 1);
		IntegerVariable charge = makeIntVar("charge", 20000, 100000);
		m.addVariable(Options.V_BOUND, charge);
		int[] coefs = new int[n];
		Random rand = new Random(100);
		int[] coef = new int[]{2000, 4000};
		for (int i = 0; i < coefs.length; i++) {
			coefs[i] = coef[rand.nextInt(2)];
		}
		//m.addConstraint(m.eq(m.scalar(,)));
		Constraint knapsack = geq(scalar(coefs, bvars), charge);
		m.addConstraint(Options.E_DECOMP, knapsack);
		s.read(m);
		s.worldPush();
		int initWorld = s.getWorldIndex();
		int cpt = 0;
		int k = 5;

		while (cpt < 100) {
			s.worldPush();

			//redemande le reveil initial des contraintes
			DisposableIterator<SConstraint> it = s.getConstraintIterator();
			for (; it.hasNext();) {
				Propagator o = (Propagator)it.next();
				o.constAwake(true);
			}
            it.dispose();

			//instancie au hasard au plus k variable a 0
			try {
				for (int i = 0; i < k; i++) {
					s.getVar(bvars[rand.nextInt(10)]).setVal(0);
				}
				/*for (int i = 0; i < n; i++) {
                                        bvars[i].setVal(0);
                                    }*/
				if (rand.nextBoolean()) {
					s.getVar(charge).setVal(22000);
				}
			} catch (ContradictionException e) {
				LOGGER.severe(e.getMessage());
			}

			if (s.getVar(charge).isInstantiated()) {
                s.solve();
            } else {
                s.maximize(s.getVar(charge), true);
            }

            if (s.isFeasible()) {
                do {
                    StringBuffer st = new StringBuffer();
                    st.append("Charge FIXED : ");
                    for (int i = 0; i < n; i++) {
						st.append(format("{0}*{1} ", coefs[i], s.getVar(bvars[i]).getVal()));
					}
					st.append(format(" = {0}", s.getVar(charge).getVal()));
                    LOGGER.info(st.toString());
				} while (s.nextSolution() == Boolean.TRUE);
			} else {
				LOGGER.info("no solution");
			}

			//retour a un etat propre
			s.worldPopUntil(initWorld);

			//verifie que tout est propre
			for (int i = 0; i < n; i++) {
				if (s.getVar(bvars[i]).isInstantiated()) {
					throw new Error("Error");
				}
			}
			if (s.getVar(charge).isInstantiated()) {
				throw new Error("Error");
			}
			cpt++;
		}
	}

	@Test
	public void bugRestoringSolution() {
		Model m = new CPModel();

		IntegerVariable obj1;
		IntegerVariable obj2;
		IntegerVariable obj3;
		IntegerVariable c;
		obj1 = makeIntVar("obj1", 0, 5);
		obj2 = makeIntVar("obj2", 0, 7);
		obj3 = makeIntVar("obj3", 0, 10);
		c = makeIntVar("cost", 1, 100);
		m.addVariable(Options.V_BOUND, c);

		int capacity = 34;

		int[] volumes = new int[]{7, 5, 3};
		int[] energy = new int[]{6, 4, 2};

		m.addConstraint(leq(scalar(volumes, new IntegerVariable[]{obj1, obj2, obj3}), capacity));
		m.addConstraint(eq(scalar(energy, new IntegerVariable[]{obj1, obj2, obj3}), c));
		Solver s = new CPSolver();
		s.read(m);
		s.setValIntIterator(new DecreasingDomain());
		s.maximize(s.getVar(c), true);
		LOGGER.info("obj1: " + s.getVar(obj1).getVal());
		LOGGER.info("obj2: " + s.getVar(obj2).getVal());
		LOGGER.info("obj3: " + s.getVar(obj3).getVal());
		LOGGER.info("cost: " + s.getVar(c).getVal());

	}



	public final static int[] LUBY_2 = {1,1,2,1,1,2,4,1,1,2,1,1,2,4,8,1,1,2,1,1,2,4,1,1,2,1,1,2,4,8,16};

	public final static int[] LUBY_3 = {1,1,1,3,1,1,1,3,1,1,1,3,9,
		1,1,1,3,1,1,1,3,1,1,1,3,9,
		1,1,1,3,1,1,1,3,1,1,1,3,9,27};

	public final static int[] LUBY_4 = {1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,16,
		1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,16,
		1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,16,
		1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,16,64
	};

	public final static int[] GEOMETRIC_1_3 = {1,2,2,3,3,4,5,7,9,11,14,18,24,31,40};
	
	private void checkRestart(AbstractRestartStrategy r,double factor,int[] expected) {
		r.setGeometricalFactor(factor);
		int[] computed = r.getSequenceExample(expected.length);
		LOGGER.info("check Restart sequence: "+Arrays.toString(computed));
		assertArrayEquals(expected, computed);
	}

	@Test
	public void testRestartStrategy() {
		AbstractRestartStrategy r = new LubyRestartStrategy(1,2);
		checkRestart(r, 2, LUBY_2);
		checkRestart(r, 3, LUBY_3);
		checkRestart(r, 4, LUBY_4);
		r = new GeometricalRestartStrategy(1,1.3);
		checkRestart(r, 1.3, GEOMETRIC_1_3);
	}


	@Test
	public void bugRestart() {
		Model m = new CPModel();
		IntegerVariable[] v = makeIntVarArray("v", 2, 0, 2);
		IntegerVariable x = makeIntVar("v", 0, 1);
		m.addConstraint(neq(v[0],v[1]));
		m.addConstraint(neq(x,v[1]));
		m.addConstraint(neq(x,v[0]));
		CPSolver s = new CPSolver();
		s.read(m);
		s.setGeometricRestart(2, 1);
		s.setFirstSolution(false);
		s.setRandomSelectors();
		s.generateSearchStrategy();
		s.launch();
		assertEquals(s.getNbSolutions(), 4);
	}

	@Test
	public void pigeonHole() {
		int n = 10;
		CPModel mod = new CPModel();
		IntegerVariable[] vars = makeIntVarArray("pigeon", n, 0, n - 2);

		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				mod.addConstraint(neq(vars[i], vars[j]));
			}
		}
		CPSolver s = new CPSolver();
		s.read(mod);
		s.solve();
		s.printRuntimeStatistics();
        assertEquals(s.getNbSolutions(), 0);
	}

       @Test
    public void examplePapierWorkshop() {
        //1- Create the model
        Model m = new CPModel();
        int n = 6;
        //2- declaration of variables
        IntegerVariable[] vars = makeIntVarArray("v", n, 0, 5, Options.V_ENUM);
        IntegerVariable obj = makeIntVar("obj",0,100, Options.V_BOUND);

        //3- add the constraint
        String regexp = "(1|2)(3*)(1|4|5)";
        m.addConstraint(regular(vars, regexp));
        m.addConstraint(neq(vars[0], vars[5]));
        m.addConstraint(eq(scalar(new int[]{2,3,1,-2,8,10}, vars), obj));

        //4- Create the solver
        Solver s = new CPSolver();

        //5- read the model and solve it
        s.read(m);
        s.solve();
        if (s.isFeasible()) {
            do {
                StringBuffer st = new StringBuffer();
                for (int i = 0; i < n; i++) {
                    st.append(s.getVar(vars[i]).getVal());
                }
                st.append(format(" {0}", s.getVar(obj).getVal()));
                LOGGER.info(st.toString());
            } while (s.nextSolution());
        }

        //6- Print the number of solution found
        LOGGER.info("Nb_sol : " + s.getNbSolutions());
        assertEquals(s.getNbSolutions(), 5);       
    }

    @Test
    public void testNQueen() {
        CPModel m = new CPModel();
        IntegerVariable V0 = makeIntVar("V0", 1, 20);
        IntegerVariable V1 = makeIntVar("V1", 1, 20);
        IntegerVariable V2 = makeIntVar("V2", 1, 20);
        IntegerVariable V3 = makeIntVar("V3", 1, 20);
        IntegerVariable V4 = makeIntVar("V4", 1, 20);
        IntegerVariable V5 = makeIntVar("V5", 1, 20);
        IntegerVariable V6 = makeIntVar("V6", 1, 20);
        IntegerVariable V7 = makeIntVar("V7", 1, 20);
        IntegerVariable V8 = makeIntVar("V8", 1, 20);
        IntegerVariable V9 = makeIntVar("V9", 1, 20);
        IntegerVariable V10 = makeIntVar("V10", 1, 20);
        IntegerVariable V11 = makeIntVar("V11", 1, 20);
        IntegerVariable V12 = makeIntVar("V12", 1, 20);
        IntegerVariable V13 = makeIntVar("V13", 1, 20);
        IntegerVariable V14 = makeIntVar("V14", 1, 20);
        IntegerVariable V15 = makeIntVar("V15", 1, 20);
        IntegerVariable V16 = makeIntVar("V16", 1, 20);
        IntegerVariable V17 = makeIntVar("V17", 1, 20);
        IntegerVariable V18 = makeIntVar("V18", 1, 20);
        IntegerVariable V19 = makeIntVar("V19", 1, 20);

        m.addConstraint(and(neq(V14,V19),neq(abs(minus(V14,V19)),5)));
        m.addConstraint(and(neq(V3,V11), neq(abs(minus(V3,V11)),8)));
        m.addConstraint(and(neq(V3,V12),neq(abs(minus(V3,V12)),9)));
        m.addConstraint(and(neq(V3,V10),neq(abs(minus(V3,V10)),7)));
        m.addConstraint(and(neq(V3,V9),neq(abs(minus(V3,V9)),6)));
        m.addConstraint(and(neq(V3,V8),neq(abs(minus(V3,V8)),5)));
        m.addConstraint(and(neq(V3,V7),neq(abs(minus(V3,V7)),4)));
        m.addConstraint(and(neq(V3,V6),neq(abs(minus(V3,V6)),3)));
        m.addConstraint(and(neq(V13,V15),neq(abs(minus(V13,V15)),2)));
        m.addConstraint(and(neq(V3,V5),neq(abs(minus(V3,V5)),2)));
        m.addConstraint(and(neq(V3,V4),neq(abs(minus(V3,V4)),1)));
        m.addConstraint(and(neq(V2,V19),neq(abs(minus(V2,V19)),17)));
        m.addConstraint(and(neq(V2,V18),neq(abs(minus(V2,V18)),16)));
        m.addConstraint(and(neq(V13,V19),neq(abs(minus(V13,V19)),6)));
        m.addConstraint(and(neq(V13,V18),neq(abs(minus(V13,V18)),5)));
        m.addConstraint(and(neq(V13,V17),neq(abs(minus(V13,V17)),4)));
        m.addConstraint(and(neq(V13,V16),neq(abs(minus(V13,V16)),3)));
        m.addConstraint(and(neq(V14,V18),neq(abs(minus(V14,V18)),4)));
        m.addConstraint(and(neq(V14,V17),neq(abs(minus(V14,V17)),3)));
        m.addConstraint(and(neq(V14,V16),neq(abs(minus(V14,V16)),2)));
        m.addConstraint(and(neq(V14,V15),neq(abs(minus(V14,V15)),1)));
        m.addConstraint(and(neq(V12,V19),neq(abs(minus(V12,V19)),7)));
        m.addConstraint(and(neq(V13,V14),neq(abs(minus(V13,V14)),1)));
        m.addConstraint(and(neq(V2,V16),neq(abs(minus(V2,V16)),14)));
        m.addConstraint(and(neq(V2,V17),neq(abs(minus(V2,V17)),15)));
        m.addConstraint(and(neq(V2,V12),neq(abs(minus(V2,V12)),10)));
        m.addConstraint(and(neq(V2,V11),neq(abs(minus(V2,V11)),9)));
        m.addConstraint(and(neq(V2,V14),neq(abs(minus(V2,V14)),12)));
        m.addConstraint(and(neq(V2,V13),neq(abs(minus(V2,V13)),11)));
        m.addConstraint(and(neq(V2,V8),neq(abs(minus(V2,V8)),6)));
        m.addConstraint(and(neq(V2,V7),neq(abs(minus(V2,V7)),5)));
        m.addConstraint(and(neq(V2,V10),neq(abs(minus(V2,V10)),8)));
        m.addConstraint(and(neq(V2,V9),neq(abs(minus(V2,V9)),7)));
        m.addConstraint(and(neq(V11,V19),neq(abs(minus(V11,V19)),8)));
        m.addConstraint(and(neq(V11,V18),neq(abs(minus(V11,V18)),7)));
        m.addConstraint(and(neq(V12,V14),neq(abs(minus(V12,V14)),2)));
        m.addConstraint(and(neq(V12,V13),neq(abs(minus(V12,V13)),1)));
        m.addConstraint(and(neq(V12,V16),neq(abs(minus(V12,V16)),4)));
        m.addConstraint(and(neq(V12,V15),neq(abs(minus(V12,V15)),3)));
        m.addConstraint(and(neq(V2,V15),neq(abs(minus(V2,V15)),13)));
        m.addConstraint(and(neq(V12,V18),neq(abs(minus(V12,V18)),6)));
        m.addConstraint(and(neq(V12,V17),neq(abs(minus(V12,V17)),5)));
        m.addConstraint(and(neq(V6,V18),neq(abs(minus(V6,V18)),12)));
        m.addConstraint(and(neq(V4,V15),neq(abs(minus(V4,V15)),11)));
        m.addConstraint(and(neq(V7,V8),neq(abs(minus(V7,V8)),1)));
        m.addConstraint(and(neq(V6,V19),neq(abs(minus(V6,V19)),13)));
        m.addConstraint(and(neq(V4,V18),neq(abs(minus(V4,V18)),14)));
        m.addConstraint(and(neq(V4,V19),neq(abs(minus(V4,V19)),15)));
        m.addConstraint(and(neq(V4,V16),neq(abs(minus(V4,V16)),12)));
        m.addConstraint(and(neq(V4,V17),neq(abs(minus(V4,V17)),13)));
        m.addConstraint(and(neq(V7,V13),neq(abs(minus(V7,V13)),6)));
        m.addConstraint(and(neq(V4,V12),neq(abs(minus(V4,V12)),8)));
        m.addConstraint(and(neq(V7,V14),neq(abs(minus(V7,V14)),7)));
        m.addConstraint(and(neq(V4,V11),neq(abs(minus(V4,V11)),7)));
        m.addConstraint(and(neq(V7,V15),neq(abs(minus(V7,V15)),8)));
        m.addConstraint(and(neq(V4,V10),neq(abs(minus(V4,V10)),6)));
        m.addConstraint(and(neq(V4,V9),neq(abs(minus(V4,V9)),5)));
        m.addConstraint(and(neq(V7,V9),neq(abs(minus(V7,V9)),2)));
        m.addConstraint(and(neq(V7,V10),neq(abs(minus(V7,V10)),3)));
        m.addConstraint(and(neq(V7,V11),neq(abs(minus(V7,V11)),4)));
        m.addConstraint(and(neq(V4,V14),neq(abs(minus(V4,V14)),10)));
        m.addConstraint(and(neq(V7,V12),neq(abs(minus(V7,V12)),5)));
        m.addConstraint(and(neq(V4,V13),neq(abs(minus(V4,V13)),9)));
        m.addConstraint(and(neq(V6,V9),neq(abs(minus(V6,V9)),3)));
        m.addConstraint(and(neq(V6,V8),neq(abs(minus(V6,V8)),2)));
        m.addConstraint(and(neq(V4,V5),neq(abs(minus(V4,V5)),1)));
        m.addConstraint(and(neq(V4,V6),neq(abs(minus(V4,V6)),2)));
        m.addConstraint(and(neq(V4,V7),neq(abs(minus(V4,V7)),3)));
        m.addConstraint(and(neq(V4,V8),neq(abs(minus(V4,V8)),4)));
        m.addConstraint(and(neq(V6,V16),neq(abs(minus(V6,V16)),10)));
        m.addConstraint(and(neq(V3,V14),neq(abs(minus(V3,V14)),11)));
        m.addConstraint(and(neq(V6,V17),neq(abs(minus(V6,V17)),11)));
        m.addConstraint(and(neq(V3,V13),neq(abs(minus(V3,V13)),10)));
        m.addConstraint(and(neq(V15,V17),neq(abs(minus(V15,V17)),2)));
        m.addConstraint(and(neq(V6,V14),neq(abs(minus(V6,V14)),8)));
        m.addConstraint(and(neq(V3,V16),neq(abs(minus(V3,V16)),13)));
        m.addConstraint(and(neq(V15,V16),neq(abs(minus(V15,V16)),1)));
        m.addConstraint(and(neq(V6,V15),neq(abs(minus(V6,V15)),9)));
        m.addConstraint(and(neq(V3,V15),neq(abs(minus(V3,V15)),12)));
        m.addConstraint(and(neq(V6,V12),neq(abs(minus(V6,V12)),6)));
        m.addConstraint(and(neq(V3,V18),neq(abs(minus(V3,V18)),15)));
        m.addConstraint(and(neq(V6,V13),neq(abs(minus(V6,V13)),7)));
        m.addConstraint(and(neq(V3,V17),neq(abs(minus(V3,V17)),14)));
        m.addConstraint(and(neq(V6,V10),neq(abs(minus(V6,V10)),4)));
        m.addConstraint(and(neq(V6,V11),neq(abs(minus(V6,V11)),5)));
        m.addConstraint(and(neq(V3,V19),neq(abs(minus(V3,V19)),16)));
        m.addConstraint(and(neq(V17,V18),neq(abs(minus(V17,V18)),1)));
        m.addConstraint(and(neq(V16,V19),neq(abs(minus(V16,V19)),3)));
        m.addConstraint(and(neq(V18,V19),neq(abs(minus(V18,V19)),1)));
        m.addConstraint(and(neq(V17,V19),neq(abs(minus(V17,V19)),2)));
        m.addConstraint(and(neq(V15,V19),neq(abs(minus(V15,V19)),4)));
        m.addConstraint(and(neq(V15,V18),neq(abs(minus(V15,V18)),3)));
        m.addConstraint(and(neq(V16,V18),neq(abs(minus(V16,V18)),2)));
        m.addConstraint(and(neq(V16,V17),neq(abs(minus(V16,V17)),1)));
        m.addConstraint(and(neq(V9,V13),neq(abs(minus(V9,V13)),4)));
        m.addConstraint(and(neq(V9,V12),neq(abs(minus(V9,V12)),3)));
        m.addConstraint(and(neq(V9,V11),neq(abs(minus(V9,V11)),2)));
        m.addConstraint(and(neq(V9,V10),neq(abs(minus(V9,V10)),1)));
        m.addConstraint(and(neq(V9,V14),neq(abs(minus(V9,V14)),5)));
        m.addConstraint(and(neq(V8,V15),neq(abs(minus(V8,V15)),7)));
        m.addConstraint(and(neq(V8,V18),neq(abs(minus(V8,V18)),10)));
        m.addConstraint(and(neq(V8,V19),neq(abs(minus(V8,V19)),11)));
        m.addConstraint(and(neq(V8,V16),neq(abs(minus(V8,V16)),8)));
        m.addConstraint(and(neq(V8,V17),neq(abs(minus(V8,V17)),9)));
        m.addConstraint(and(neq(V5,V19),neq(abs(minus(V5,V19)),14)));
        m.addConstraint(and(neq(V6,V7),neq(abs(minus(V6,V7)),1)));
        m.addConstraint(and(neq(V5,V17),neq(abs(minus(V5,V17)),12)));
        m.addConstraint(and(neq(V5,V18),neq(abs(minus(V5,V18)),13)));
        m.addConstraint(and(neq(V0,V17),neq(abs(minus(V0,V17)),17)));
        m.addConstraint(and(neq(V5,V14),neq(abs(minus(V5,V14)),9)));
        m.addConstraint(and(neq(V0,V18),neq(abs(minus(V0,V18)),18)));
        m.addConstraint(and(neq(V5,V13),neq(abs(minus(V5,V13)),8)));
        m.addConstraint(and(neq(V0,V19),neq(abs(minus(V0,V19)),19)));
        m.addConstraint(and(neq(V5,V16),neq(abs(minus(V5,V16)),11)));
        m.addConstraint(and(neq(V1,V2),neq(abs(minus(V1,V2)),1)));
        m.addConstraint(and(neq(V5,V15),neq(abs(minus(V5,V15)),10)));
        m.addConstraint(and(neq(V5,V12),neq(abs(minus(V5,V12)),7)));
        m.addConstraint(and(neq(V5,V11),neq(abs(minus(V5,V11)),6)));
        m.addConstraint(and(neq(V8,V10),neq(abs(minus(V8,V10)),2)));
        m.addConstraint(and(neq(V8,V9),neq(abs(minus(V8,V9)),1)));
        m.addConstraint(and(neq(V8,V12),neq(abs(minus(V8,V12)),4)));
        m.addConstraint(and(neq(V0,V11),neq(abs(minus(V0,V11)),11)));
        m.addConstraint(and(neq(V8,V11),neq(abs(minus(V8,V11)),3)));
        m.addConstraint(and(neq(V0,V12),neq(abs(minus(V0,V12)),12)));
        m.addConstraint(and(neq(V8,V14),neq(abs(minus(V8,V14)),6)));
        m.addConstraint(and(neq(V0,V13),neq(abs(minus(V0,V13)),13)));
        m.addConstraint(and(neq(V8,V13),neq(abs(minus(V8,V13)),5)));
        m.addConstraint(and(neq(V0,V14),neq(abs(minus(V0,V14)),14)));
        m.addConstraint(and(neq(V0,V15),neq(abs(minus(V0,V15)),15)));
        m.addConstraint(and(neq(V0,V16),neq(abs(minus(V0,V16)),16)));
        m.addConstraint(and(neq(V7,V16),neq(abs(minus(V7,V16)),9)));
        m.addConstraint(and(neq(V7,V17),neq(abs(minus(V7,V17)),10)));
        m.addConstraint(and(neq(V7,V18),neq(abs(minus(V7,V18)),11)));
        m.addConstraint(and(neq(V7,V19),neq(abs(minus(V7,V19)),12)));
        m.addConstraint(and(neq(V5,V10),neq(abs(minus(V5,V10)),5)));
        m.addConstraint(and(neq(V5,V6),neq(abs(minus(V5,V6)),1)));
        m.addConstraint(and(neq(V5,V7),neq(abs(minus(V5,V7)),2)));
        m.addConstraint(and(neq(V5,V8),neq(abs(minus(V5,V8)),3)));
        m.addConstraint(and(neq(V5,V9),neq(abs(minus(V5,V9)),4)));
        m.addConstraint(and(neq(V0,V9),neq(abs(minus(V0,V9)),9)));
        m.addConstraint(and(neq(V0,V8),neq(abs(minus(V0,V8)),8)));
        m.addConstraint(and(neq(V0,V10),neq(abs(minus(V0,V10)),10)));
        m.addConstraint(and(neq(V1,V12),neq(abs(minus(V1,V12)),11)));
        m.addConstraint(and(neq(V0,V5),neq(abs(minus(V0,V5)),5)));
        m.addConstraint(and(neq(V0,V4),neq(abs(minus(V0,V4)),4)));
        m.addConstraint(and(neq(V1,V10),neq(abs(minus(V1,V10)),9)));
        m.addConstraint(and(neq(V0,V7),neq(abs(minus(V0,V7)),7)));
        m.addConstraint(and(neq(V1,V11),neq(abs(minus(V1,V11)),10)));
        m.addConstraint(and(neq(V0,V6),neq(abs(minus(V0,V6)),6)));
        m.addConstraint(and(neq(V1,V8),neq(abs(minus(V1,V8)),7)));
        m.addConstraint(and(neq(V1,V9),neq(abs(minus(V1,V9)),8)));
        m.addConstraint(and(neq(V1,V6),neq(abs(minus(V1,V6)),5)));
        m.addConstraint(and(neq(V1,V7),neq(abs(minus(V1,V7)),6)));
        m.addConstraint(and(neq(V1,V4),neq(abs(minus(V1,V4)),3)));
        m.addConstraint(and(neq(V11,V17),neq(abs(minus(V11,V17)),6)));
        m.addConstraint(and(neq(V1,V5),neq(abs(minus(V1,V5)),4)));
        m.addConstraint(and(neq(V11,V16),neq(abs(minus(V11,V16)),5)));
        m.addConstraint(and(neq(V11,V15),neq(abs(minus(V11,V15)),4)));
        m.addConstraint(and(neq(V1,V3),neq(abs(minus(V1,V3)),2)));
        m.addConstraint(and(neq(V11,V13),neq(abs(minus(V11,V13)),2)));
        m.addConstraint(and(neq(V11,V14),neq(abs(minus(V11,V14)),3)));
        m.addConstraint(and(neq(V10,V19),neq(abs(minus(V10,V19)),9)));
        m.addConstraint(and(neq(V11,V12),neq(abs(minus(V11,V12)),1)));
        m.addConstraint(and(neq(V10,V17),neq(abs(minus(V10,V17)),7)));
        m.addConstraint(and(neq(V10,V18),neq(abs(minus(V10,V18)),8)));
        m.addConstraint(and(neq(V10,V16),neq(abs(minus(V10,V16)),6)));
        m.addConstraint(and(neq(V0,V1),neq(abs(minus(V0,V1)),1)));
        m.addConstraint(and(neq(V0,V2),neq(abs(minus(V0,V2)),2)));
        m.addConstraint(and(neq(V0,V3),neq(abs(minus(V0,V3)),3)));
        m.addConstraint(and(neq(V2,V4),neq(abs(minus(V2,V4)),2)));
        m.addConstraint(and(neq(V2,V5),neq(abs(minus(V2,V5)),3)));
        m.addConstraint(and(neq(V1,V17),neq(abs(minus(V1,V17)),16)));
        m.addConstraint(and(neq(V1,V18),neq(abs(minus(V1,V18)),17)));
        m.addConstraint(and(neq(V1,V19),neq(abs(minus(V1,V19)),18)));
        m.addConstraint(and(neq(V2,V3),neq(abs(minus(V2,V3)),1)));


        m.setDefaultExpressionDecomposition(false);
        CPSolver s = new CPSolver();

        s.read(m);
        LOGGER.info("" + s.pretty());

        s.solve();
        //LOGGER.info("" + s.isFeasible());
        s.printRuntimeStatistics();
        assertTrue(s.isFeasible());
    }
    
    @Test
    public void testShavingBug() {
    	 Model m = new CPModel();
         IntegerVariable [] pos = new IntegerVariable[4];
         for (int i = 0; i < pos.length; i++) {
             pos[i] = Choco.makeIntVar("VM" + i + "on-?", 0, 4);
             IntegerVariable [] bools = Choco.makeBooleanVarArray("VM" + i + "on", 6);
             m.addConstraint(Choco.domainChanneling(pos[i], bools));
             m.addConstraint(Choco.neq(pos[i], 3));
             m.addConstraint(Choco.neq(pos[i], 4));
         }
         IntegerVariable nbNodes = Choco.makeIntVar("nbNodes", 3, 3, Options.V_OBJECTIVE);
         m.addConstraint(Choco.atMostNValue(nbNodes, pos));
         Solver s = new CPSolver();
         s.getConfiguration().putTrue(Configuration.INIT_SHAVING);
         s.read(m);
        //ChocoLogging.setVerbosity(Verbosity.SOLUTION);
         s.minimize(s.getVar(nbNodes), false);
    	
    }
}




