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

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.search.BranchingFactory;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static choco.Choco.*;
import static org.junit.Assert.*;

public class TimesXYZTest {
	private final static Logger LOGGER = ChocoLogging.getTestLogger();

	private CPModel m;
	private CPSolver s;
	private IntegerVariable x, y, z;

	@Before
	public void setUp() {
		LOGGER.fine("choco.currentElement.bool.TimesXYZTest Testing...");
		m = new CPModel();
		s = new CPSolver();
	}

	@After
	public void tearDown() {
		y = null;
		x = null;
		z = null;
		m = null;
		s = new CPSolver();
	}

	@Test
	public void test1() {
		LOGGER.finer("test1");
		x = makeIntVar("x", -7, 12);
		y = makeIntVar("y", 3, 5);
		z = makeIntVar("z", 22, 59);
		m.addConstraint(times(x, y, z));
		s.read(m);
		try {
			s.propagate();
			assertEquals(s.getVar(x).getInf(), 5);
			s.getVar(y).setVal(3);
			s.propagate();
			assertEquals(s.getVar(x).getInf(), 8);
			assertEquals(s.getVar(z).getSup(), 36);
			assertEquals(s.getVar(z).getInf(), 24);
		} catch (ContradictionException e) {
			assertFalse(true);
		}
	}

	@Test
	public void test2() {
		for (int i = 0; i < 10; i++) {
			m = new CPModel();
			s = new CPSolver();
			LOGGER.finer("test2");
			x = makeIntVar("x", 1, 2);
			y = makeIntVar("y", 3, 5);
			z = makeIntVar("z", 3, 10);
			m.addConstraint(times(x, y, z));
			s.read(m);
			s.setRandomSelectors(110);
			s.solve();
			do {
				//        /LOGGER.info("" + s.getVar(x).getVal() + "*" + s.getVar(y).getVal() + "=" + s.getVar(z).getVal());
			} while (s.nextSolution() == Boolean.TRUE);
			LOGGER.info(String.format("Nb solution : %d", s.getNbSolutions()));
			assertEquals( s.getNbSolutions(), 6);
		}
	}

	@Test
	public void test2b() throws ContradictionException {
		for (int i = 0; i < 10; i++) {
			m = new CPModel();
			s = new CPSolver();
			LOGGER.finer("test2");
			x = makeIntVar("x", 1, 2);
			y = makeIntVar("y", 3, 5);
			z = makeIntVar("z", 3, 10);
			m.addVariables(Options.V_BOUND, z, y, x);
			s.read(m);
			//      m.addConstraint(times(x, y, z));
			//      s.setVarIntSelector(new RandomIntVarSelector(s, i));
			s.setVarIntSelector(new StaticVarOrder(s, s.getVar(x,y,z)));
			s.setValIntSelector(new RandomIntValSelector(i + 10));
			s.solveAll();
			//      do {
			//        LOGGER.info("" + x.getVal() + "*" + y.getVal() + "=" +
			//            z.getVal());
			//        assertEquals(s.getVar(x).getVal() * s.getVar(y).getVal(), s.getVar(z).getVal());
			//      } while (s.nextSolution() == Boolean.TRUE);
			ChocoLogging.flushLogs();
			//LOGGER.info("Nb solution : " + s.getNbSolutions());
			//      assertEquals(s.getNbSolutions(), 6);
		}
	}

	@Test
	public void test3() {
		for (int i = 0; i < 10; i++) {
			LOGGER.info("test3-" + i);
			try {
				m=new CPModel();
				s= new CPSolver();

				IntegerVariable x = makeIntVar("x", -10, 10);
				IntegerVariable y = makeIntVar("y", -10, 10);
				IntegerVariable z = makeIntVar("z", -20, 20);
				//IntVar oneFourFour = pb.makeConstantIntVar(144);

				m.addConstraint(times(x, y, z));
				//m.addConstraint(pb.neq(z, oneFourFour));
				s.read(m);
				s.setRandomSelectors(0);
				s.solve();
				do {
					//LOGGER.info("" + x.getVal() + "*" + y.getVal() + "=" + z.getVal());
					assertEquals(s.getVar(x).getVal() * s.getVar(y).getVal(), s.getVar(z).getVal());
				} while (s.nextSolution() == Boolean.TRUE);
				assertEquals(225, s.getNbSolutions()); // with 10,10,20
				//assertEquals(3993, s.getNbSolutions()); // with 100,100,200
				//LOGGER.info("Nb solution : " + s.getNbSolutions());
			} catch (Exception e) {
				e.printStackTrace();
				assertTrue(false);
			}
		}
	}

	@Test
	public void test3b() {
		for (int i = 0; i < 10; i++) {
			LOGGER.info("test3-" + i);
			try {
				m=new CPModel();
				s= new CPSolver();
				IntegerVariable x = makeIntVar("x", -10, 10);
				IntegerVariable y = makeIntVar("y", -10, 10);
				IntegerVariable z = makeIntVar("z", -20, 20);
				IntegerConstantVariable oneFourFour = constant(14);
				//IntVar oneFourFour = pb.makeConstantIntVar(144);

				m.addConstraint(times(x, y, z));
				m.addConstraint(neq(z, oneFourFour));
				s.read(m);
				s.setVarIntSelector(new RandomIntVarSelector(s, i));
				s.setValIntSelector(new RandomIntValSelector(i + 1));
				s.solve();
				do {
					//LOGGER.info("" + x.getVal() + "*" + y.getVal() + "=" + z.getVal());
					assertEquals(s.getVar(x).getVal() * s.getVar(y).getVal(), s.getVar(z).getVal());
				} while (s.nextSolution() == Boolean.TRUE);
				assertEquals(221, s.getNbSolutions()); // with 10,10,20,14
				//assertEquals(3967, s.getNbSolutions()); // with 100,100,200,144
				//LOGGER.info("Nb solution : " + s.getNbSolutions());
			} catch (Exception e) {
				e.printStackTrace();
				assertTrue(false);
			}
		}
	}

	@Test
	public void test3c() {
		for (int i = 0; i < 10; i++) {
			LOGGER.info("test3-" + i);
			try {
				m=new CPModel();
				s= new CPSolver();

				IntegerVariable x = makeIntVar("x", -10, 10);
				IntegerVariable y = makeIntVar("y", -10, 10);
				IntegerVariable z = makeIntVar("z", -20, 20);
				m.addVariables(Options.V_BOUND, x, y, z);

				m.addConstraint(times(x, y, z));
				s.read(m);
				s.setVarIntSelector(new RandomIntVarSelector(s, i));
				s.setValIntSelector(new RandomIntValSelector(i + 1));
				s.solve();
				do {
					//LOGGER.info("" + x.getVal() + "*" + y.getVal() + "=" + z.getVal());
					assertEquals(s.getVar(x).getVal() * s.getVar(y).getVal(), s.getVar(z).getVal());
				} while (s.nextSolution() == Boolean.TRUE);
				assertEquals(225, s.getNbSolutions()); // with 10,10,20
				//assertEquals(3993, s.getNbSolutions()); // with 100,100,200
				//LOGGER.info("Nb solution : " + s.getNbSolutions());
			} catch (Exception e) {
				e.printStackTrace();
				assertTrue(false);
			}
		}
	}

    @Test
	public void testPDAV() {
        m = new CPModel();
        s = new PreProcessCPSolver();

        final IntegerVariable x = makeIntVar("x", -10, 10);
        final IntegerVariable z = makeIntVar("z", -20, 20);
        m.addVariables(Options.V_BOUND, x, z);

        m.addConstraint(eq(z, mult(x, x)));
        try{
            s.read(m);
        }catch (IndexOutOfBoundsException e){
            Assert.fail();
        }
    }

    @Test
    public void testJoost() {
    	CPModel model = new CPModel();
        //model.setDefaultExpressionDecomposition(true);
		Solver solver = new CPSolver();

		//int max = 100 // finds a solution in 37ms
		int max = 1000; // 30871ms

		IntegerVariable v1 = Choco.makeIntVar("v1", 0, max);
		IntegerVariable v2 = Choco.makeIntVar("v2", 0, max);
		IntegerVariable v3 = Choco.makeIntVar("v3", 0, max);
		model.addVariable(v1);
		model.addVariable(v2);
		model.addVariable(v2);

		//model.addConstraint(Choco.eq(v1, Choco.plus(v2,v3)));
		model.addConstraint(Choco.eq(v1, Choco.mult(v2,v3)));
		solver.read(model);

		solver.solve();
		Assert.assertEquals(Boolean.TRUE, solver.checkSolution());
	}

    @Test
    public void testJoost2() {
		CPModel model = new CPModel();
        //model.setDefaultExpressionDecomposition(true);
		Solver solver = new CPSolver();

		//int max = 100 // finds a solution in 37ms
		int max = 569448; // 30871ms

		IntegerVariable v1 = Choco.makeIntVar("v1", 1,1);
		IntegerVariable v2 = Choco.makeIntVar("v2", 0, max, Options.V_BOUND);
		IntegerVariable v3 = Choco.makeIntVar("v3", 0, max, Options.V_BOUND);
		model.addVariable(v1);
		model.addVariable(v2);
		model.addVariable(v2);

		//model.addConstraint(Choco.eq(v1, Choco.plus(v2,v3)));
		model.addConstraint(Choco.eq(v1, Choco.mult(v2,v3)));
		solver.read(model);

		solver.solve();
		Assert.assertEquals(Boolean.TRUE, solver.checkSolution());
	}

    @Test
    public void testJoost3() {
		CPModel model = new CPModel();
        //model.setDefaultExpressionDecomposition(true);
		Solver solver = new CPSolver();

		//int max = 100 // finds a solution in 37ms
		int max = Integer.MAX_VALUE; // 30871ms

		IntegerVariable v1 = Choco.makeIntVar("v1", 0,max, Options.V_BOUND);
		IntegerVariable v2 = Choco.makeIntVar("v2", 0, max, Options.V_BOUND);
		IntegerVariable v3 = Choco.makeIntVar("v3", 0, max, Options.V_BOUND);

		//model.addConstraint(Choco.eq(v1, Choco.plus(v2,v3)));
		model.addConstraint(times(v2, v3, v1));
		solver.read(model);
        solver.addGoal(BranchingFactory.minDomIncDom(solver));
		solver.solve();
		Assert.assertEquals(Boolean.TRUE, solver.checkSolution());
	}

    @Test
    public void testJoost4() {
		CPModel model = new CPModel();
        //model.setDefaultExpressionDecomposition(true);
		Solver solver = new CPSolver();

		//int max = 100 // finds a solution in 37ms
		int min = Integer.MIN_VALUE; // 30871ms
        int max = Integer.MAX_VALUE; // 30871ms

		IntegerVariable v1 = Choco.makeIntVar("v1", min,max, Options.V_BOUND);
		IntegerVariable v2 = Choco.makeIntVar("v2", min, max, Options.V_BOUND);
		IntegerVariable v3 = Choco.makeIntVar("v3", min, max, Options.V_BOUND);

		//model.addConstraint(Choco.eq(v1, Choco.plus(v2,v3)));
		model.addConstraint(times(v2, v3, v1));
		solver.read(model);
        solver.addGoal(BranchingFactory.minDomIncDom(solver));
		solver.solve();
		Assert.assertEquals(Boolean.TRUE, solver.checkSolution());
	}

}
