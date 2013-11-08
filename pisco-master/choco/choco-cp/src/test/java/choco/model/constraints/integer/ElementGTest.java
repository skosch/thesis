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
import static choco.Choco.constant;
import static choco.Choco.makeIntVar;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.model.managers.constraints.global.ElementManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import org.junit.*;
import static org.junit.Assert.*;

import java.util.Random;
import java.util.logging.Logger;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 14 oct. 2009
* Since : Choco 2.1.0
* Update : Choco 2.1.0
*/
@Ignore
public class ElementGTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    CPModel m;
    CPSolver s;


    @Before
    public void before() {
        m = new CPModel();
        s = new CPSolver();
    }

    @After
    public void after() {
        m = null;
        s = null;
    }

    private static Constraint element(IntegerVariable index, int[] values, IntegerVariable val, int offset) {
		IntegerVariable[] vars = new IntegerVariable[values.length+2];
		for (int i = 0; i < values.length; i++) {
			vars[i] = constant(values[i]);
		}
		vars[vars.length-2] = index;
		vars[vars.length-1] = val;
		Constraint c =new ComponentConstraint(ElementManager.class, offset, vars);
        c.addOption(Options.C_NTH_G);
        return c;
	}

    private static Constraint element(IntegerVariable index, int[] values, IntegerVariable val) {
		return element(index, values, val,0);
	}

    private static Constraint element(IntegerVariable index, IntegerVariable[] varArray, IntegerVariable val, int offset) {
		IntegerVariable[] vars = ArrayUtils.append(varArray, new IntegerVariable[]{index, val});
		Constraint c = new ComponentConstraint(ElementManager.class, offset, vars);
        c.addOption(Options.C_NTH_G);
        return c;
	}

    private static Constraint element(IntegerVariable index, IntegerVariable[] varArray, IntegerVariable val) {
		return element(index, varArray, val, 0);
	}



    @Test
    public void test0(){
        int[] values = new int[]{1, 2, 0, 4, 3};
		IntegerVariable index = makeIntVar("index", -3, 10);
		IntegerVariable value = makeIntVar("value", -20, 20);

        // actions normalement effectuees dans Choco.java
        // debut
        IntegerVariable[] vars = new IntegerVariable[values.length];
		for (int i = 0; i < values.length; i++) {
			vars[i] = Choco.constant(values[i]);
		}

        Constraint element = element(index, vars, value, 0);
        // fin
        m.addConstraint(element);
		s.read(m);
		s.solveAll();

		assertEquals(5, s.getNbSolutions());
    }


    @Test
	public void test1() {
		int[] values = new int[]{1, 2, 0, 4, 3};
		IntegerVariable index = makeIntVar("index", -3, 10);
		IntegerVariable var = makeIntVar("value", -20, 20);
		m.addConstraint(element(index, values, var));
		s.read(m);
		s.solve();
		do {
			LOGGER.info("index = " + s.getVar(index).getVal());
			LOGGER.info("value = " + s.getVar(var).getVal());
			assertEquals(s.getVar(var).getVal(), values[s.getVar(index).getVal()]);
		} while (s.nextSolution());

		assertEquals(5, s.getNbSolutions());
	}

	@Test
	public void test2() {

		int[] values = new int[]{1, 2, 0, 4, 3};
		IntegerVariable index = makeIntVar("index", 2, 10);
		IntegerVariable var = makeIntVar("value", -20, 20);
		m.addConstraint(element(index, values, var));
		s.read(m);
		s.solve();
		do {
			LOGGER.info("index = " + s.getVar(index).getVal());
			LOGGER.info("value = " + s.getVar(var).getVal());
			assertEquals(s.getVar(var).getVal(), values[s.getVar(index).getVal()]);
		} while (s.nextSolution());

		assertEquals(3, s.getNbSolutions());
	}

	@Test
	public void test3() {

		IntegerVariable X = makeIntVar("X", 0, 5);
		IntegerVariable Y = makeIntVar("Y", 3, 7);
		IntegerVariable Z = makeIntVar("Z", 5, 8);
		IntegerVariable I = makeIntVar("index", -5, 12);
		IntegerVariable V = makeIntVar("V", -3, 20);
		m.addConstraint(element(I, new IntegerVariable[]{X, Y, Z}, V));
		s.read(m);
		try {
			s.propagate();
			assertEquals(s.getVar(I).getInf(), 0);
			assertEquals(s.getVar(I).getSup(), 2);
			assertEquals(s.getVar(V).getInf(), 0);
			assertEquals(s.getVar(V).getSup(), 8);
			s.getVar(V).setSup(5);
			s.getVar(Z).setInf(6);
			s.propagate();
			assertEquals(s.getVar(I).getSup(), 1);
			s.getVar(Y).remVal(4);
			s.getVar(Y).remVal(5);
			s.getVar(V).remVal(3);
			s.propagate();
			assertTrue(s.getVar(I).isInstantiatedTo(0));
			s.getVar(V).setSup(2);
			s.getVar(V).remVal(1);
			s.propagate();
			assertEquals(s.getVar(X).getSup(), 2);
			assertFalse(s.getVar(X).canBeInstantiatedTo(1));
		} catch (ContradictionException e) {
			assertFalse(true);
		}
	}

	@Test
	public void test4() {

		IntegerVariable V = makeIntVar("V", 0, 20);
		IntegerVariable X = makeIntVar("X", 10, 50);
		IntegerVariable Y = makeIntVar("Y", 0, 1000);
		IntegerVariable I = makeIntVar("I", 0, 1);
		m.addConstraint(element(I, new IntegerVariable[]{X, Y}, V));
		s.read(m);
		try {
			s.propagate();
			assertFalse(s.getVar(I).isInstantiated());
			s.getVar(Y).setInf(30);
			s.propagate();
			assertTrue(s.getVar(I).isInstantiatedTo(0));
			assertEquals(s.getVar(V).getInf(), s.getVar(X).getInf());
			assertEquals(s.getVar(V).getSup(), s.getVar(X).getSup());
		} catch (ContradictionException e) {
			assertFalse(true);
		}
	}

	/**
	 * testing the initial propagation and the behavior when the index variable is instantiated
	 */
	@Test
	public void test5() {

		int n = 2;
		IntegerVariable[] vars = new IntegerVariable[n];
		for (int idx = 0; idx < n; idx++) {
			vars[idx] = makeIntVar("t" + idx, 3 * idx, 2 + 3 * idx);
		}
		IntegerVariable index = makeIntVar("index", -3, 15);
		IntegerVariable var = makeIntVar("value", -25, 20);

		m.addConstraint(element(index, vars, var));
		s.read(m);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			assertFalse(true);
		}
		assertEquals(0, s.getVar(var).getInf());
		assertEquals(3 * n - 1, s.getVar(var).getSup());
		assertEquals(s.getVar(var).getDomainSize(), 3 * n);
		assertEquals(0, s.getVar(index).getInf());
		assertEquals(n - 1, s.getVar(index).getSup());
		assertEquals(s.getVar(index).getDomainSize(), n);

		assertEquals(0, s.getVar(vars[0]).getInf());
		assertEquals(2, s.getVar(vars[0]).getSup());
		assertEquals(s.getVar(vars[0]).getDomainSize(), 3);
		assertEquals(3, s.getVar(vars[1]).getInf());
		assertEquals(5, s.getVar(vars[1]).getSup());
		assertEquals(s.getVar(vars[1]).getDomainSize(), 3);

		try {
			s.getVar(index).setVal(1);
			s.propagate();
			assertEquals(3, s.getVar(var).getInf());
			assertEquals(5, s.getVar(var).getSup());
			assertEquals(0, s.getVar(vars[0]).getInf());
			assertEquals(2, s.getVar(vars[0]).getSup());
			assertEquals(3, s.getVar(vars[1]).getInf());
			assertEquals(5, s.getVar(vars[1]).getSup());
			s.getVar(vars[0]).setVal(0);
			s.propagate();
		} catch (ContradictionException e) {
			assertFalse(true);
		}
	}

	/**
	 * same as test5, but counting the number of solutions
	 */
	@Test
	public void test6() {
		subtest6(2);
		subtest6(3);
		subtest6(4);
	}

	private void subtest6(int n) {
		m = new CPModel();
		s = new CPSolver();
		IntegerVariable[] vars = new IntegerVariable[n];
		for (int idx = 0; idx < n; idx++) {
			vars[idx] = makeIntVar("t" + idx, 3 * idx, 2 + 3 * idx);
		}
		IntegerVariable index = makeIntVar("index", -3, n + 15);
		IntegerVariable var = makeIntVar("value", -25, 4 * n + 20);

		m.addConstraint(element(index, vars, var));
		s.read(m);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			assertFalse(true);
		}
		assertEquals(0, s.getVar(var).getInf());
		assertEquals(3 * n - 1, s.getVar(var).getSup());
		assertEquals(s.getVar(var).getDomainSize(), 3 * n);
		assertEquals(0, s.getVar(index).getInf());
		assertEquals(n - 1, s.getVar(index).getSup());
		assertEquals(s.getVar(index).getDomainSize(), n);
		for (int i = 0; i < n; i++) {
			assertEquals(3 * i, s.getVar(vars[i]).getInf());
			assertEquals(2 + 3 * i, s.getVar(vars[i]).getSup());
			assertEquals(s.getVar(vars[i]).getDomainSize(), 3);
		}
		s.solveAll();
		assertEquals(Math.round(n * Math.pow(3, n)), s.getNbSolutions());

	}

	@Test
	public void testNthG() {
		for (int i = 0; i < 100; i++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable X = makeIntVar("X", 0, 5);
			IntegerVariable Y = makeIntVar("Y", 3, 7);
			IntegerVariable Z = makeIntVar("Z", 5, 8);
			IntegerVariable I = makeIntVar("index", -5, 12);
			IntegerVariable V = makeIntVar("V", -3, 20);
			m.addConstraint(element(I, new IntegerVariable[]{X, Y, Z}, V));
			s.setVarIntSelector(new RandomIntVarSelector(s, i));
			s.setValIntSelector(new RandomIntValSelector(i + 1));
			s.read(m);
			s.solveAll();
			int nbSol = s.getNbSolutions();
			//LOGGER.info("nbsol " + nbSol);
			assertEquals(nbSol, 360);
		}

	}

	@Test
	public void testNthG2() {
		for (int i = 0; i < 30; i++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable X = makeIntVar("X", 0, 5);
			IntegerVariable Z = makeIntVar("Z", 5, 8);
			IntegerVariable I = makeIntVar("index", -5, 12);
			IntegerVariable V = makeIntVar("V", -3, 20);
			m.addConstraint(element(I, new IntegerVariable[]{X, V, Z}, V));
			s.setVarIntSelector(new RandomIntVarSelector(s, i));
			s.setValIntSelector(new RandomIntValSelector(i + 1));
			s.read(m);
			s.solveAll();
			int nbSol = s.getNbSolutions();
			//LOGGER.info("nbsol " + nbSol);
			assertEquals(nbSol,624);
		}
	}

	@Test
	public void testNthG3() {
		for (int i = 0; i < 30; i++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable X = makeIntVar("X", 0, 5);
			IntegerVariable Z = makeIntVar("Z", 5, 8);
			IntegerVariable I = makeIntVar("index", -5, 12);
			IntegerVariable V = makeIntVar("V", -3, 20);
			m.addConstraint(element(I, new IntegerVariable[]{X, I, Z}, V));
			s.setVarIntSelector(new RandomIntVarSelector(s, i));
			s.setValIntSelector(new RandomIntValSelector(i + 1));
			s.read(m);
			s.solveAll();
			int nbSol = s.getNbSolutions();
			//LOGGER.info("nbsol " + nbSol);
			assertEquals(nbSol,72);
		}

	}

	@Test
    //BUG ID: 2860512
	public void testNthManager() {
		IntegerVariable I = makeIntVar("index", 0, 2);
		IntegerVariable V = makeIntVar("V", 0, 5);
		m.addConstraint(element(I, new IntegerVariable[]{constant(0),constant(1), makeIntVar("VV",2,3)}, V));
		s.read(m);
		//ChocoLogging.setVerbosity(Verbosity.SOLUTION);
		s.solveAll();
		assertEquals(6, s.getSolutionCount());
	}

    @Test
    //BUG ID: 2860512
    public void testNthManager2() {
        IntegerVariable I = makeIntVar("index", 0, 2);
        IntegerVariable V = makeIntVar("V", 0, 5);
        m.addConstraint(element(I, new IntegerVariable[]{constant(0),constant(1), constant(2)}, V));
        s.read(m);
        //ChocoLogging.setVerbosity(Verbosity.SOLUTION);
        s.solveAll();
        assertEquals(3, s.getSolutionCount());
    }

    @Test
    //BUG ID: 2860512
	public void testNthManager3() {
		IntegerVariable I = makeIntVar("index", 0, 2);
		IntegerVariable V = makeIntVar("V", 0, 5);
		m.addConstraint(element(I, new IntegerVariable[]{makeIntVar("VV",0,1), makeIntVar("VV",1,2), makeIntVar("VV",2,3)}, V));
		s.read(m);
		//ChocoLogging.setVerbosity(Verbosity.SOLUTION);
		s.solveAll();
		assertEquals(24, s.getSolutionCount());
	}

    @Test
    public void testFznElement(){
        for(int j = 0; j< 100; j++){
            Random r = new Random(j);
            IntegerVariable index = Choco.makeIntVar("idx", 24, 25);
            IntegerVariable val = Choco.makeBooleanVar("val");
            int[] values = new int[24];
            for(int i = 0; i < values.length; i++){
                values[i] = r.nextInt(2);
            }
            m.addConstraint(element(index, values, val));
            s.read(m);
            s.solveAll();
            Assert.assertEquals(20, s.getSolutionCount());
        }

    }
}
