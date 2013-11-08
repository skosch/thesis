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

package choco.model.constraints.reified;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.reified.ExpressionSConstraint;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.integer.extension.LargeRelation;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import static choco.Choco.*;
import static java.text.MessageFormat.format;
import static org.junit.Assert.assertEquals;

/**
 * <p/>
 * An open-source Constraint Programming Kernel
 * for Research and Education
 * <p/>
 * Created by: Guillaume on 30 juin 2004
 */
public class ReifiedSearchTest {

	protected final static Logger LOGGER = ChocoLogging.getTestLogger();

	Model m;
	Solver s;

	@Before
	public void b() {
		m = new CPModel();
		s = new CPSolver();
	}

	@After
	public void a() {
		m = null;
		s = null;
	}

	@Test
	public void testMultBoundVar() {
		int nbexpectedsol = 44;
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			m.setDefaultExpressionDecomposition(true);
			IntegerVariable x = makeIntVar("x", 1, 10);
			IntegerVariable y = makeIntVar("y", 1, 10);
			IntegerVariable z = makeIntVar("z", 1, 10);
			m.addVariables(Options.V_BOUND, x, y, z);

			m.addConstraint(or(eq(mult((x), (y)), (z)),
					eq(mult((z), (y)), (x))));
			s.read(m);
			LOGGER.info(s.pretty());

			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));

			s.solveAll();

			LOGGER.info("" + s.getNbSolutions());

			assertEquals(nbexpectedsol, s.getNbSolutions());

		}
	}

	@Test
	@Ignore
	public void testAbsBoundVar() {
		int nbexpectedsol = 289;
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			m.setDefaultExpressionDecomposition(true);
			IntegerVariable x = makeIntVar("x", -10, 10);
			IntegerVariable y = makeIntVar("y", -10, 10);
			IntegerVariable z = makeIntVar("z", -10, 10);
			m.addVariables(Options.V_BOUND,x ,y, z);

			m.addConstraint(or(eq(mult(abs(minus((x),(3))), (y)), (z)),
					eq(mult((z), (y)), abs((x)))));
			s.read(m);
			LOGGER.info(s.pretty());

			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));

			s.solveAll();
			if (nbexpectedsol == -1)
				nbexpectedsol = s.getNbSolutions();

			LOGGER.info("" + s.getNbSolutions());

			assertEquals(nbexpectedsol, s.getNbSolutions());

		}
	}



	@Test
	public void testOrBoundVar() {
		IntegerVariable x = makeIntVar("x", 1, 3);
		IntegerVariable y = makeIntVar("y", 1, 3);
		IntegerVariable z = makeIntVar("z", 1, 3);
		m.addVariables(Options.V_BOUND,x ,y, z);

		m.addConstraint(or(lt(x, y), lt(y, x)));
		m.addConstraint(or(lt(y, z), lt(z, y)));
		s.read(m);
		LOGGER.info(s.pretty());
		s.solveAll();

		assertEquals(12, s.getNbSolutions());
	}


	@Test
	public void testOrBoundVarDecomp() {
		m.setDefaultExpressionDecomposition(false);
		IntegerVariable x = makeIntVar("x", 1, 3);
		IntegerVariable y = makeIntVar("y", 1, 3);
		IntegerVariable z = makeIntVar("z", 1, 3);
		m.addVariables(Options.V_BOUND,x ,y, z);
		Constraint e1 = or(lt((x), (y)), lt((y), (x)));
		//e1.setDecomposeExp(true);
		Constraint e2 = or(lt((y), (z)), lt((z), (y)));
		//e2.setDecomposeExp(true);

		m.addConstraint(e1);
		m.addConstraint(e2);

		s.read(m);
		LOGGER.info(s.pretty());
		s.solveAll();
		LOGGER.info("" + s.getNbSolutions());
		assertEquals(12, s.getNbSolutions());
	}


	@Test
	public void testOrEnumVar() {

		IntegerVariable x = makeIntVar("x", 1, 3);
		IntegerVariable y = makeIntVar("y", 1, 3);
		IntegerVariable z = makeIntVar("z", 1, 3);

		m.addConstraint(or(lt((x), (y)), lt((y), (x))));
		m.addConstraint(or(lt((y), (z)), lt((z), (y))));
		s.read(m);
		LOGGER.info(s.pretty());
		s.solveAll();

		assertEquals(12, s.getNbSolutions());
	}

	@Test
	public void testLargeOrForGecode() {
		//public static void main(String[] args) {
		int nbexpectedsol = -1;
		for (int seed = 0; seed < 10; seed++) {

			CPModel m = new CPModel();
			CPSolver s = new CPSolver();

			IntegerVariable x1 = makeIntVar("x", 1, 10);
			IntegerVariable x2 = makeIntVar("y", 1, 10);
			IntegerVariable y1 = makeIntVar("x", 1, 10);
			IntegerVariable y2 = makeIntVar("y", 1, 10);

			int s1 = 3, s2 = 4;

			m.addConstraint(or(gt(minus((x1), (x2)), (s1)),
					gt(minus((x2), (x1)), (s2)),
					gt(minus((y1), (y2)), (s1)),
					gt(minus((y2), (y1)), (s2))));

			s.read(m);

			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));

			s.solveAll();
			if (nbexpectedsol == -1)
				nbexpectedsol = s.getNbSolutions();

			LOGGER.info("" + s.getNbSolutions());

			assertEquals(nbexpectedsol, s.getNbSolutions());
		}
	}


	@Test
	public void testIfThenElse() {
		//public static void main(String[] args) {
		int nbexpectedsol = -1;
		for (int seed = 0; seed < 10; seed++) {
			CPModel m = new CPModel();
			CPSolver s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 3);
			IntegerVariable y = makeIntVar("y", 1, 3);
			IntegerVariable z = makeIntVar("z", 1, 3);

			m.addConstraint(ifThenElse(lt((x), (y)), gt((y), (z)), FALSE));
			s.read(m);

			LOGGER.info(s.pretty());

			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));

			s.solveAll();

			if (nbexpectedsol == -1)
				nbexpectedsol = s.getNbSolutions();

			LOGGER.info("" + s.getNbSolutions());

			assertEquals(nbexpectedsol, s.getNbSolutions());
		}
	}

	@Test
	public void testIfThenElse2() {
		//public static void main(String[] args) {
		int nbexpectedsol = -1;
		for (int seed = 0; seed < 10; seed++) {
			CPModel m1 = new CPModel();
			m1.setDefaultExpressionDecomposition(false);
			m1.setDefaultExpressionDecomposition(true);
			CPModel m2 = new CPModel();
			CPSolver s1 = new CPSolver();
			CPSolver s2 = new CPSolver();
			IntegerVariable x = makeIntVar("x", 0, 1);
			IntegerVariable y = makeIntVar("y", 1, 3);
			IntegerVariable z = makeIntVar("z", 1, 3);

			Constraint c = ifThenElse(eq(x, 1), and(eq(y, 3), eq(z, 3)), and(eq(y, 1), eq(z, 2)));
			m1.addConstraint(c);
			m2.addConstraint(c);

			s1.read(m1);
			s2.read(m2);

			//			LOGGER.info(s1.pretty());
			//            LOGGER.info(s2.pretty());

			s1.setVarIntSelector(new RandomIntVarSelector(s1, seed));
			s1.setValIntSelector(new RandomIntValSelector(seed + 1));

			s2.setVarIntSelector(new RandomIntVarSelector(s2, seed));
			s2.setValIntSelector(new RandomIntValSelector(seed + 1));

			s1.solveAll();
			s2.solveAll();

			if (nbexpectedsol == -1){
				nbexpectedsol = s1.getNbSolutions();
				nbexpectedsol = s2.getNbSolutions();
			}

			assertEquals(nbexpectedsol, s1.getNbSolutions());
			assertEquals(nbexpectedsol, s2.getNbSolutions());
			assertEquals(s1.getNbSolutions(), s2.getNbSolutions());
		}
	}

	@Test
	public void testOrEnumVarDecomp() {

		IntegerVariable x = makeIntVar("x", 1, 3);
		IntegerVariable y = makeIntVar("y", 1, 3);
		IntegerVariable z = makeIntVar("z", 1, 3);

		Constraint e1 = or(lt((x), (y)), lt((y), (x)));
		//e1.setDecomposeExp(true);
		Constraint e2 = or(lt((y), (z)), lt((z), (y)));
		//e2.setDecomposeExp(true);
		m.addConstraints(e1, e2);
		s.read(m);
		LOGGER.info(s.pretty());

		s.solveAll();

		assertEquals(12, s.getNbSolutions());
	}

	@Test
	public void testEquiv() {
		int nbexpectedsol = 11;
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 3);
			IntegerVariable y = makeIntVar("y", 1, 3);
			IntegerVariable z = makeIntVar("z", 1, 3);
			m.addVariables(Options.V_BOUND,x ,y, z);

			m.addConstraint(ifOnlyIf(lt((x), (y)), lt((y), (z))));
			s.read(m);
			LOGGER.info(s.pretty());
			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));
			s.solveAll();
			assertEquals(nbexpectedsol, s.getNbSolutions());
		}
	}

	@Test
	public void testEquivDecomp() {

		int nbexpectedsol = 11;
		for (int seed = 0; seed < 10; seed++) {
			CPModel m = new CPModel();
			CPSolver s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 3);
			IntegerVariable y = makeIntVar("y", 1, 3);
			IntegerVariable z = makeIntVar("z", 1, 3);
			m.addVariables(Options.V_BOUND,x ,y, z);

			Constraint e1 = ifOnlyIf(lt((x), (y)), lt((y), (z)));
			//1.setDecomposeExp(true);

			m.addConstraint(e1);
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));
			LOGGER.info(s.pretty());

			s.solveAll();
			LOGGER.info("NBSOLUTION: " + s.getNbSolutions());
			assertEquals(nbexpectedsol, s.getNbSolutions());
		}
	}

	@Test
	public void testImplies() {

		IntegerVariable x = makeIntVar("x", 1, 2);
		IntegerVariable y = makeIntVar("y", 1, 2);
		IntegerVariable z = makeIntVar("z", 1, 2);
		m.addVariables(Options.V_BOUND,x ,y, z);

		m.addConstraint(implies(leq((x), (y)), leq((x), (z))));
		s.read(m);
		LOGGER.info(s.pretty());
		s.solveAll();

		assertEquals(7, s.getNbSolutions());
	}

	@Test
	public void testImpliesDecomp() {

		IntegerVariable x = makeIntVar("x", 1, 2);
		IntegerVariable y = makeIntVar("y", 1, 2);
		IntegerVariable z = makeIntVar("z", 1, 2);
		m.addVariables(Options.V_BOUND,x ,y, z);

		Constraint e1 = implies(leq((x), (y)), leq((x), (z)));
		//e1.setDecomposeExp(true);

		m.addConstraint(e1);
		s.read(m);
		LOGGER.info(s.pretty());

		s.solveAll();

		assertEquals(7, s.getNbSolutions());
	}

	@Test
	public void testAnd() {

		IntegerVariable x = makeIntVar("x", 1, 3);
		IntegerVariable y = makeIntVar("y", 1, 3);
		IntegerVariable z = makeIntVar("z", 1, 3);
		m.addVariables(Options.V_BOUND,x ,y, z);

		m.addConstraint(implies(lt((x), (2)), and(lt((x), (y)), lt((y), (z)))));
		s.read(m);
		LOGGER.info(s.pretty());
		s.solveAll();
	}

	@Test
	public void testAndDecomp() {

		IntegerVariable x = makeIntVar("x", 1, 3);
		IntegerVariable y = makeIntVar("y", 1, 3);
		IntegerVariable z = makeIntVar("z", 1, 3);
		m.addVariables(Options.V_BOUND,x ,y, z);

		Constraint e1 = implies(lt((x), (2)), and(lt((x), (y)), lt((y), (z))));
		//e1.setDecomposeExp(true);

		m.addConstraint(e1);
		s.read(m);
		LOGGER.info(s.pretty());

		s.solveAll();
	}

	@Test
	public void testLargeOr() {

		IntegerVariable x = makeIntVar("x", 1, 2);
		IntegerVariable y = makeIntVar("y", 1, 2);
		IntegerVariable z = makeIntVar("z", 1, 2);
		m.addVariables(Options.V_BOUND,x ,y, z);

		m.addConstraint(or(lt((x), (y)), lt((x), (z)), lt((y), (z))));
		s.read(m);
		LOGGER.info(s.pretty());
		s.solve();
		do {
			LOGGER.info("x = " + s.getVar(x).getVal());
			LOGGER.info("y = " + s.getVar(y).getVal());
			LOGGER.info("z = " + s.getVar(z).getVal());

		} while (s.nextSolution());

		assertEquals(4, s.getNbSolutions());
	}

	@Test
	public void testLargeOrDecomp() {

		IntegerVariable x = makeIntVar("x", 1, 2);
		IntegerVariable y = makeIntVar("y", 1, 2);
		IntegerVariable z = makeIntVar("z", 1, 2);
		m.addVariables(Options.V_BOUND,x ,y, z);

		Constraint e1 = or(lt((x), (y)), lt((x), (z)), lt((y), (z)));
		//e1.setDecomposeExp(true);
		m.addConstraint(e1);
		s.read(m);
		LOGGER.info(s.pretty());

		s.solve();
		do {
			LOGGER.info("x = " + s.getVar(x).getVal());
			LOGGER.info("y = " + s.getVar(y).getVal());
			LOGGER.info("z = " + s.getVar(z).getVal());

		} while (s.nextSolution());

		assertEquals(4, s.getNbSolutions());
	}


	@Test
	public void testSylvainBug1() {
		CPModel m = new CPModel();
		CPSolver s = new CPSolver();
		IntegerVariable[][] v = makeIntVarArray("v", 3, 2, 0, 1);
		Constraint[] tab = {(eq(sum(v[0]), 2)),
				(eq(sum(v[1]), 2)),
				(eq(sum(v[2]), 2))};
		Constraint c = or(tab);
		m.addConstraint(c);
		s.read(m);
		LOGGER.info(s.pretty());
		if (s.solve()) {
			StringBuffer st = new StringBuffer();
			st.append(format("v[0] = [{0}, {1}] / ", s.getVar(v[0][0]).getVal(), s.getVar(v[0][1]).getVal()));
			st.append(format("v[1] = [{0}, {1}] / ", s.getVar(v[1][0]).getVal(), s.getVar(v[1][1]).getVal()));
			st.append(format("v[2] = [{0}, {1}] - > c", s.getVar(v[2][0]).getVal(), s.getVar(v[2][1]).getVal()));
			LOGGER.info(st.toString());
			//LOGGER.info(s.getCstr(c).isSatisfied() ? " satisfied." : " not satisfied!!!");

		}

		while (s.nextSolution()) {
			StringBuffer st = new StringBuffer();
			st.append(format("v[0] = [{0}, {1}] / ", s.getVar(v[0][0]).getVal(), s.getVar(v[0][1]).getVal()));
			st.append(format("v[1] = [{0}, {1}] / ", s.getVar(v[1][0]).getVal(), s.getVar(v[1][1]).getVal()));
			st.append(format("v[2] = [{0}, {1}] - > c", s.getVar(v[2][0]).getVal(), s.getVar(v[2][1]).getVal()));
			LOGGER.info(st.toString());
			//LOGGER.info(s.getCstr(c).isSatisfied() ? " satisfied." : "not satisfied!!!");
		}
		assertEquals(37, s.getNbSolutions());

	}

	@Test
	public void testSylvainBug2() {
		CPModel m = new CPModel();
		CPSolver s = new CPSolver();
		IntegerVariable[][] v = makeIntVarArray("v", 2, 2, 0, 1);
		Constraint[] tab = {(eq(sum(v[0]), 2)),
				(eq(sum(v[1]), 2))};
		Constraint c = or(tab[0], tab[1]);
		m.addConstraint(c);
		s.read(m);
		LOGGER.info(s.pretty());
		//LOGGER.info(" " + s.getCstr(c).isSatisfied());
		if (s.solve()) {
			StringBuffer st = new StringBuffer();
			st.append(format("v[0] = [{0}, {1}] / ", s.getVar(v[0][0]).getVal(), s.getVar(v[0][1]).getVal()));
			st.append(format("v[1] = [{0}, {1}] / ", s.getVar(v[1][0]).getVal(), s.getVar(v[1][1]).getVal()));
			LOGGER.info(st.toString());
			//LOGGER.info(s.getCstr(c).isSatisfied() ? " satisfied." : " not satisfied!!!");

		}

		while (s.nextSolution()) {
			StringBuffer st = new StringBuffer();
			st.append(format("v[0] = [{0}, {1}] / ", s.getVar(v[0][0]).getVal(), s.getVar(v[0][1]).getVal()));
			st.append(format("v[1] = [{0}, {1}] / ", s.getVar(v[1][0]).getVal(), s.getVar(v[1][1]).getVal()));
			LOGGER.info(st.toString());
			//LOGGER.info(s.getCstr(c).isSatisfied() ? " satisfied." : "not satisfied!!!");
		}
		assertEquals(7, s.getNbSolutions());

	}

	@Test
	public void testSylvainBug2Decomp() {
		CPModel m = new CPModel();
		CPSolver s = new CPSolver();
		IntegerVariable[][] v = makeIntVarArray("v", 2, 2, 0, 1);
		Constraint[] tab = {(eq(sum(v[0]), 2)),
				(eq(sum(v[1]), 2))};
		Constraint c = or(tab[0], tab[1]);
		m.addConstraint(c);
		m.setDefaultExpressionDecomposition(true);
		s.read(m);
		LOGGER.info(s.pretty());
		//LOGGER.info(" " + s.getCstr(c).isSatisfied());
		if (s.solve()) {
			StringBuffer st = new StringBuffer();
			st.append(format("v[0] = [{0}, {1}] / ", s.getVar(v[0][0]).getVal(), s.getVar(v[0][1]).getVal()));
			st.append(format("v[1] = [{0}, {1}] / ", s.getVar(v[1][0]).getVal(), s.getVar(v[1][1]).getVal()));
			LOGGER.info(st.toString());
			//LOGGER.info(s.getCstr(c).isSatisfied() ? " satisfied." : " not satisfied!!!");

		}

		while (s.nextSolution()) {
			StringBuffer st = new StringBuffer();
			st.append(format("v[0] = [{0}, {1}] / ", s.getVar(v[0][0]).getVal(), s.getVar(v[0][1]).getVal()));
			st.append(format("v[1] = [{0}, {1}] / ", s.getVar(v[1][0]).getVal(), s.getVar(v[1][1]).getVal()));
			LOGGER.info(st.toString());
			//LOGGER.info(s.getCstr(c).isSatisfied() ? " satisfied." : "not satisfied!!!");
		}
		assertEquals(7, s.getNbSolutions());

	}


	@Test
	public void testEoin1() {
		int nbexpectedsol = 1; //initialize to -1 if number of solutions to hard to compute by hand

		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 2);
			IntegerVariable y = makeIntVar("y", 5, 5);
			IntegerVariable z = makeIntVar("z", 1, 2);
			m.addVariables(Options.V_BOUND,x ,y, z);

			m.addConstraint(and(
					and(leq((x), (y)), leq((x), (z))),
					not(eq((x), (z)))));

			s.read(m);
			LOGGER.info(s.pretty());
			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));

			s.solveAll();

			assertEquals(nbexpectedsol, s.getNbSolutions());
		}
	}

	@Test
	public void testEoin1Decomp() {
		int nbexpectedsol = 1; //initialize to -1 if number of solutions to hard to compute by hand

		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 2);
			IntegerVariable y = makeIntVar("y", 5, 5);
			IntegerVariable z = makeIntVar("z", 1, 2);
			m.addVariables(Options.V_BOUND,x ,y, z);

			Constraint e1 = and(
					and(leq((x), (y)), leq((x), (z))),
					not(eq((x), (z))));

			//e1.setDecomposeExp(true);

			m.addConstraint(e1);

			s.read(m);
			LOGGER.info(s.pretty());

			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));

			s.solveAll();

			assertEquals(nbexpectedsol, s.getNbSolutions());
		}
	}

	@Test
	public void testEoin2() {
		int nbexpectedsol = 2; //initialize to -1 if number of solutions to hard to compute by hand

		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 1);
			IntegerVariable y = makeIntVar("y", 1, 1);
			IntegerVariable z = makeIntVar("z", 1, 2);
			m.addVariables(Options.V_BOUND,x ,y, z);

			m.addConstraint(and(
					leq((y), (z)),
					implies(leq((x), (y)), leq((x), (z)))));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));
			LOGGER.info(s.pretty());
			s.solveAll();

			assertEquals(nbexpectedsol, s.getNbSolutions());
		}
	}

	@Test
	public void testEoin2Decomp() {
		int nbexpectedsol = 2; //initialize to -1 if number of solutions to hard to compute by hand

		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 1);
			IntegerVariable y = makeIntVar("y", 1, 1);
			IntegerVariable z = makeIntVar("z", 1, 2);
			m.addVariables(Options.V_BOUND,x ,y, z);

			Constraint e1 = and(
					leq((y), (z)),
					implies(leq((x), (y)), leq((x), (z))));
			//e1.setDecomposeExp(true);

			m.addConstraint(e1);
			s.read(m);
			LOGGER.info(s.pretty());
			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));

			s.solveAll();

			assertEquals(nbexpectedsol, s.getNbSolutions());
		}
	}

	//================================================
	@Test
	public void testEoin3() {
		int nbexpectedsol = 2; //initialize to -1 if number of solutions to hard to compute by hand

		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 3);
			IntegerVariable y = makeIntVar("y", 1, 3);
			IntegerVariable z = makeIntVar("z", 1, 3);
			m.addVariables(Options.V_BOUND,x ,y, z);

			IntegerVariable[] vars = new IntegerVariable[]{x, y, z};

			// negating an iff turns it into an XOR gate

			m.addConstraint(and(
					not(
							ifOnlyIf(
									and(
											leq((x), (y)),
											leq((y), (z))
									),
									and(
											leq((z), (y)),
											leq((y), (x))
									)
							)),
							(allDifferent(vars))
			));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));
			LOGGER.info(s.pretty());
			s.solveAll();

			assertEquals(nbexpectedsol, s.getNbSolutions());
		}
	}

	@Test
	public void testEoin4() {
		int nbexpectedsol = 0; //initialize to -1 if number of solutions to hard to compute by hand

		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 3);
			IntegerVariable y = makeIntVar("y", 1, 3);
			IntegerVariable z = makeIntVar("z", 1, 3);
			m.addVariables(Options.V_BOUND,x ,y, z);

			m.addConstraint(and(
					and(
							not(eq((x), (y))),
							not(eq((x), (z)))),
							and(
									not(eq((y), (z))),
									not(not(eq((x), (z)))))
			));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));
			LOGGER.info(s.pretty());
			s.solveAll();

			assertEquals(nbexpectedsol, s.getNbSolutions());
		}
	}

	@Test
	public void testEoin4Decomp() {
		int nbexpectedsol = 0; //initialize to -1 if number of solutions to hard to compute by hand

		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 3);
			IntegerVariable y = makeIntVar("y", 1, 3);
			IntegerVariable z = makeIntVar("z", 1, 3);
			m.addVariables(Options.V_BOUND,x ,y, z);

			Constraint e1 = and(
					and(
							not(eq((x), (y))),
							not(eq((x), (z)))),
							and(
									not(eq((y), (z))),
									not(not(eq((x), (z))))
							));

			//e1.setDecomposeExp(true);
			m.addConstraint(e1);
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));
			LOGGER.info(s.pretty());
			s.solveAll();

			assertEquals(nbexpectedsol, s.getNbSolutions());
		}
	}

	@Test
	public void testEoin5() {
		int nbexpectedsol = 2; //initialize to -1 if number of solutions to hard to compute by hand

		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 3);
			IntegerVariable y = makeIntVar("y", 1, 3);
			IntegerVariable z = makeIntVar("z", 1, 3);
			m.addVariables(Options.V_BOUND,x ,y, z);

			IntegerVariable[] vars = new IntegerVariable[]{x, y, z};

			// negating an iff turns it into an XOR gate

			m.addConstraint(and(
					not(
							ifOnlyIf(
									and(
											leq((x), (y)),
											leq((y), (z))
									),
									and(
											leq((z), (y)),
											leq((y), (x))
									)
							)),
							(allDifferent(vars))
			));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));
			LOGGER.info(s.pretty());
			s.solveAll();

			assertEquals(nbexpectedsol, s.getNbSolutions());
		}
	}

	@Test
	public void testDeepak() {
		int nbexpectedsol = -1; //initialize to -1 if number of solutions to hard to compute by hand

		for (int seed = 0; seed < 10; seed++) {
			LOGGER.info("seed " + seed);
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 3);
			IntegerVariable y = makeIntVar("y", 1, 3);
			IntegerVariable z = makeIntVar("z", 0, 1);
			IntegerVariable w = makeIntVar("w", 0, 1);
			m.addVariables(Options.V_BOUND,x ,y, z);

			Constraint c = implies(and(eq((z), (1)), eq((w), (1))), leq((x), (y)));
			m.addConstraint(c);
			s.read(m);
			ExpressionSConstraint p = (ExpressionSConstraint) s.getCstr(c);
			p.setScope(s);
			IntDomainVar[] vs = p.getVars();
			int[] max = new int[4];
			for (int i = 0; i < vs.length; i++) {
				max[i] = vs[i].getSup() + i;
			}
			LargeRelation lrela = s.makeLargeRelation(new int[4], max, p.getTuples(s), true);
			//relationTupleAC(new IntegerVariable[]{x, y}, lrela);
			s.post(s.relationTupleAC(p.getVars(), lrela));
			LOGGER.info(s.pretty());
			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));

			//s.solve();
			//LOGGER.info(z + " " + w + " " + y + " " + x);
			//LOGGER.info(isFeasible() + " " + c.pretty());
			s.solveAll();
			LOGGER.info("" + s.getNbSolutions());
			if (nbexpectedsol == -1)
				nbexpectedsol = s.getNbSolutions();
			assertEquals(nbexpectedsol, s.getNbSolutions());
		}

	}

	@Test
	public void testDeepakDecomp() {
		int nbexpectedsol = -1; //initialize to -1 if number of solutions to hard to compute by hand

		for (int seed = 0; seed < 10; seed++) {
			LOGGER.info("seed " + seed);
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable x = makeIntVar("x", 1, 3);
			IntegerVariable y = makeIntVar("y", 1, 3);
			IntegerVariable z = makeIntVar("z", 0, 1);
			IntegerVariable w = makeIntVar("w", 0, 1);
			m.addVariables(Options.V_BOUND, w, x ,y, z);

			Constraint e1 = implies(and(eq((z), (1)), eq((w), (1))), leq((x), (y)));
			//e1.setDecomposeExp(true);
			m.addConstraint(e1);
			s.read(m);
			LOGGER.info(s.pretty());
			ExpressionSConstraint p = (ExpressionSConstraint) s.getCstr(e1);
			p.setScope(s);
			IntDomainVar[] vs = p.getVars();
			int[] max = new int[4];
			for (int i = 0; i < vs.length; i++) {
				max[i] = vs[i].getSup() + i;
			}
			LargeRelation lrela = s.makeLargeRelation(new int[4], max, p.getTuples(s), true);
			relationTupleAC(new IntegerVariable[]{x, y}, lrela);
			s.post(s.relationTupleAC(p.getVars(), lrela));

			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));

			//s.solve();
			//LOGGER.info(z + " " + w + " " + y + " " + x);
			//LOGGER.info(isFeasible() + " " + c.pretty());
			s.solveAll();
			LOGGER.info("" + s.getNbSolutions());
			if (nbexpectedsol == -1)
				nbexpectedsol = s.getNbSolutions();
			assertEquals(nbexpectedsol, s.getNbSolutions());
		}

	}

	@Test
	public void testExp1() {
		CPModel m = new CPModel();
		IntegerVariable z = makeIntVar("z", -10, 10);
		IntegerVariable w = makeIntVar("w", -10, 10);
		m.addVariables(z, w);

		CPSolver s = new CPSolver();
		// z = 10 * |w| OU z >= 9
		Constraint exp =
			or(
					eq((z), mult((10), abs((w)))),
					geq((z), (9))
			);

		m.addConstraint(exp);

		s.read(m);
		LOGGER.info(s.pretty());
		try {
			s.propagate();
			LOGGER.info(s.getVar(z).pretty() + " " + s.getVar(w).pretty());
		} catch (
				ContradictionException e) {
			LOGGER.severe(e.getMessage());
		}

		s.solve();

		LOGGER.info("" + s.isFeasible());
		LOGGER.info(s.getVar(z).getVal() + " " + s.getVar(w).getVal());
	}

	@Test
	public void testExp1Decomp() {
		CPModel m = new CPModel();
		IntegerVariable z = makeIntVar("z", -10, 10);
		IntegerVariable w = makeIntVar("w", -10, 10);
		m.addVariables(z, w);

		CPSolver s = new CPSolver();
		// z = 10 * |w| OU z >= 9
		Constraint exp =
			or(
					eq((z), mult((10), abs((w)))),
					geq((z), (9))
			);

		m.setDefaultExpressionDecomposition(true);
		m.addConstraint(exp);

		s.read(m);
		LOGGER.info(s.pretty());
		try {
			s.propagate();
			LOGGER.info(s.getVar(z).pretty() + " " + s.getVar(w).pretty());
		} catch (
				ContradictionException e) {
			LOGGER.severe(e.getMessage());
		}

		s.solve();

		LOGGER.info("" + s.isFeasible());
		LOGGER.info(s.getVar(z).getVal() + " " + s.getVar(w).getVal());
	}

	@Test
	public void testNotReifiedExpr() {
		CPModel m = new CPModel();
		IntegerVariable x = makeIntVar("x", -10, 10);
		IntegerVariable z = makeIntVar("z", -10, 10);
		IntegerVariable w = makeIntVar("w", -10, 10);
		m.addVariables(x,z, w);

		CPSolver s = new CPSolver();
		// z = 10 * |w| OU z >= 9
		Constraint exp = geq(x, mult(z,w));

		m.setDefaultExpressionDecomposition(true);
		m.addConstraint(exp);

		s.read(m);
		s.solveAll();

		assertEquals(s.getNbSolutions(),4705);

	}

	@Test
	public void testIfThenElse3() {
		for (int seed = 0; seed < 100; seed++) {
			CPModel m = new CPModel();
			m.setDefaultExpressionDecomposition(true);
			IntegerVariable x = makeIntVar("x", 0, 2, Options.V_BOUND);
			IntegerVariable y = makeIntVar("y", 0, 2, Options.V_BOUND);
			m.addConstraint(ifThenElse(gt(x, 0), eq(y, 0), Choco.TRUE));
			CPSolver s = new CPSolver();
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));
			s.solveAll();
			LOGGER.info("" + s.getNbSolutions());
			assertEquals(s.getNbSolutions(), 5);

		}
	}


	@Test
	public void testReifiedWithOppositeMin() {
		int n = 3;
		int m = 2;
		IntegerVariable bo = Choco.makeBooleanVar("bo");
		IntegerVariable X = Choco.makeIntVar("cible", 0, n);
		IntegerVariable[] vars = Choco.makeIntVarArray("vars", m, 0, n);
		Constraint c = Choco.min(vars, X);

		Model m1 = new CPModel();
		m1.addConstraint(reifiedConstraint(bo, c));
		m1.setDefaultExpressionDecomposition(true);
		Solver s1 = new CPSolver();
		s1.read(m1);
		s1.setVarIntSelector(new MinDomain(s1));
		s1.setValIntIterator(new IncreasingDomain());
		s1.solve();
		if(s1.isFeasible()){
			do{
				StringBuffer st = new StringBuffer();
				for(int i =0; i < s1.getNbIntVars(); i++){
					st.append(s1.getIntVar(i).getName()).append(":")
					.append(s1.getIntVar(i).getVal())
					.append(" ");
				}
				//                    st.append(s1.getVar(bo)).append(" - ");
				//                    st.append(s1.getVar(X).getVal()).append(" de : {");
				//                    st.append(s1.getVar(vars[0]).getVal());
				//                    for(int i = 1; i< m; i ++){
				//                        st.append(",").append(s1.getVar(vars[i]).getVal());
				//                    }
				//                    st.append("}");
				LOGGER.log(Level.INFO, st.toString());
			}while(s1.nextSolution());
		}
		LOGGER.info(s1.runtimeStatistics());
		LOGGER.log(Level.INFO, "------------");

		Model m2  = new CPModel();
		IntegerVariable Y = Choco.makeIntVar("oppcible", 0, n);
		Constraint c2 = min(vars, Y);
		m2.addConstraint(c2);
		m2.addConstraints(reifiedConstraint(bo, eq(X,Y), neq(X,Y)));
		Solver s2 = new CPSolver();
		s2.read(m2);
		s2.setVarIntSelector(new MinDomain(s2));
		s2.setValIntIterator(new IncreasingDomain());
		s2.solve();
		if(s2.isFeasible()){
			do{
				StringBuffer st = new StringBuffer();
				for(int i =0; i < s2.getNbIntVars(); i++){
					st.append(s2.getIntVar(i).getName()).append(":")
					.append(s2.getIntVar(i).getVal())
					.append(" ");
				}

				//                    st.append(s2.getVar(bo)).append(" - ");
				//                    st.append(s2.getVar(X).getVal()).append(" (");
				//                    st.append(s2.getVar(Y).getVal()).append(") de : {");
				//                    st.append(s2.getVar(vars[0]).getVal());
				//                    for(int i = 1; i< m; i ++){
				//                        st.append(",").append(s2.getVar(vars[i]).getVal());
				//                    }
				//                    st.append("}");
				LOGGER.log(Level.INFO, st.toString());
				//                    Assert.assertEquals(s2.getVar(Y).getVal(), Math.min(s2.getVar(vars[0]).getVal(), s2.getVar(vars[1]).getVal()));
			}while(s2.nextSolution());
		}

		LOGGER.info(s2.runtimeStatistics());
		Assert.assertEquals("number of solution", s1.getNbSolutions(), s2.getNbSolutions());

	}

	@Test
	public void testNot() {
		int n = 3;
		int m = 2;
		IntegerVariable X = Choco.makeIntVar("cible", 0, n);
		IntegerVariable[] vars = Choco.makeIntVarArray("vars", m, 0, n);
		Constraint c = Choco.min(vars, X);

		Model m1 = new CPModel();
		m1.addConstraint(not(c));
		m1.setDefaultExpressionDecomposition(false);
		Solver s1 = new CPSolver();
		s1.read(m1);
		s1.setVarIntSelector(new MinDomain(s1));
		s1.setValIntIterator(new IncreasingDomain());
		s1.solve();
		if(s1.isFeasible()){
			do{
				StringBuffer st = new StringBuffer();
				for(int i =0; i < s1.getNbIntVars(); i++){
					st.append(s1.getIntVar(i).getName()).append(":")
					.append(s1.getIntVar(i).getVal())
					.append(" ");
				}
				//                    st.append(s1.getVar(X).getVal()).append(" de : {");
				//                    st.append(s1.getVar(vars[0]).getVal());
				//                    for(int i = 1; i< m; i ++){
				//                        st.append(",").append(s1.getVar(vars[i]).getVal());
				//                    }
				//                    st.append("}");
				LOGGER.log(Level.INFO, st.toString());
			}while(s1.nextSolution());
		}
		LOGGER.info(s1.runtimeStatistics());
		LOGGER.log(Level.INFO, "------------");
	}

	@Test
	public void testReifiedWithOppositeOccurrence() {
		int n = 3;
		int m = 2;
		IntegerVariable bo = Choco.makeBooleanVar("bo");
		IntegerVariable X = Choco.makeIntVar("cible", 0, n);
		IntegerVariable[] vars = Choco.makeIntVarArray("vars", m, 0, n);
		Constraint c = Choco.occurrence(X, vars, 2);

		Model m1 = new CPModel();
		m1.addConstraint(reifiedConstraint(bo, c));
		m1.setDefaultExpressionDecomposition(true);
		Solver s1 = new CPSolver();
		s1.read(m1);
		s1.setVarIntSelector(new MinDomain(s1));
		s1.setValIntIterator(new IncreasingDomain());
		s1.solve();
		if(s1.isFeasible()){
			do{
				StringBuffer st = new StringBuffer();
				for(int i =0; i < s1.getNbIntVars(); i++){
					st.append(s1.getIntVar(i).getName()).append(":")
					.append(s1.getIntVar(i).getVal())
					.append(" ");
				}
				//                    st.append(s1.getVar(bo)).append(" - ");
				//                    st.append(s1.getVar(X).getVal()).append(" de : {");
				//                    st.append(s1.getVar(vars[0]).getVal());
				//                    for(int i = 1; i< m; i ++){
				//                        st.append(",").append(s1.getVar(vars[i]).getVal());
				//                    }
				//                    st.append("}");
				LOGGER.log(Level.INFO, st.toString());
			}while(s1.nextSolution());
		}
		LOGGER.info(s1.runtimeStatistics());
		LOGGER.log(Level.INFO, "------------");

		Model m2  = new CPModel();
		IntegerVariable Y = Choco.makeIntVar("oppcible", 0, n);
		Constraint c2 = occurrence(Y, vars, 2);
		m2.addConstraint(c2);
		m2.addConstraints(reifiedConstraint(bo, eq(X,Y), neq(X,Y)));
		Solver s2 = new CPSolver();
		s2.read(m2);
		s2.setVarIntSelector(new MinDomain(s2));
		s2.setValIntIterator(new IncreasingDomain());
		s2.solve();
		if(s2.isFeasible()){
			do{
				StringBuffer st = new StringBuffer();
				for(int i =0; i < s2.getNbIntVars(); i++){
					st.append(s2.getIntVar(i).getName()).append(":")
					.append(s2.getIntVar(i).getVal())
					.append(" ");
				}

				//                    st.append(s2.getVar(bo)).append(" - ");
				//                    st.append(s2.getVar(X).getVal()).append(" (");
				//                    st.append(s2.getVar(Y).getVal()).append(") de : {");
				//                    st.append(s2.getVar(vars[0]).getVal());
				//                    for(int i = 1; i< m; i ++){
				//                        st.append(",").append(s2.getVar(vars[i]).getVal());
				//                    }
				//                    st.append("}");
				LOGGER.log(Level.INFO, st.toString());
				//                    Assert.assertEquals(s2.getVar(Y).getVal(), Math.min(s2.getVar(vars[0]).getVal(), s2.getVar(vars[1]).getVal()));
			}while(s2.nextSolution());
		}

		LOGGER.info(s2.runtimeStatistics());
		Assert.assertEquals("number of solution", s1.getNbSolutions(), s2.getNbSolutions());

	}

	@Test
	public void testReifiedWithOppositeDistance() {
		int n = 3;
		int m = 2;
		IntegerVariable bo = Choco.makeBooleanVar("bo");
		IntegerVariable X = Choco.makeIntVar("cible", 0, n);
		IntegerVariable[] vars = Choco.makeIntVarArray("vars", m, 0, n);
		Constraint c = Choco.distanceEQ(vars[0], vars[1], X);

		Model m1 = new CPModel();
		m1.addConstraint(reifiedConstraint(bo, c));
		m1.setDefaultExpressionDecomposition(true);
		Solver s1 = new CPSolver();
		s1.read(m1);
		s1.setVarIntSelector(new MinDomain(s1));
		s1.setValIntIterator(new IncreasingDomain());
		s1.solve();
		if(s1.isFeasible()){
			do{
				StringBuffer st = new StringBuffer();
				for(int i =0; i < s1.getNbIntVars(); i++){
					st.append(s1.getIntVar(i).getName()).append(":")
					.append(s1.getIntVar(i).getVal())
					.append(" ");
				}
				//                    st.append(s1.getVar(bo)).append(" - ");
				//                    st.append(s1.getVar(X).getVal()).append(" de : {");
				//                    st.append(s1.getVar(vars[0]).getVal());
				//                    for(int i = 1; i< m; i ++){
				//                        st.append(",").append(s1.getVar(vars[i]).getVal());
				//                    }
				//                    st.append("}");
				LOGGER.log(Level.INFO, st.toString());
			}while(s1.nextSolution());
		}
		LOGGER.info(s1.runtimeStatistics());
		LOGGER.log(Level.INFO, "------------");

		Model m2  = new CPModel();
		IntegerVariable Y = Choco.makeIntVar("oppcible", 0, n);
		Constraint c2 = Choco.distanceEQ(vars[0], vars[1], Y);
		m2.addConstraint(c2);
		m2.addConstraints(reifiedConstraint(bo, eq(X,Y), neq(X,Y)));
		Solver s2 = new CPSolver();
		s2.read(m2);
		s2.setVarIntSelector(new MinDomain(s2));
		s2.setValIntIterator(new IncreasingDomain());
		s2.solve();
		if(s2.isFeasible()){
			do{
				StringBuffer st = new StringBuffer();
				for(int i =0; i < s2.getNbIntVars(); i++){
					st.append(s2.getIntVar(i).getName()).append(":")
					.append(s2.getIntVar(i).getVal())
					.append(" ");
				}

				//                    st.append(s2.getVar(bo)).append(" - ");
				//                    st.append(s2.getVar(X).getVal()).append(" (");
				//                    st.append(s2.getVar(Y).getVal()).append(") de : {");
				//                    st.append(s2.getVar(vars[0]).getVal());
				//                    for(int i = 1; i< m; i ++){
				//                        st.append(",").append(s2.getVar(vars[i]).getVal());
				//                    }
				//                    st.append("}");
				LOGGER.log(Level.INFO, st.toString());
				//                    Assert.assertEquals(s2.getVar(Y).getVal(), Math.min(s2.getVar(vars[0]).getVal(), s2.getVar(vars[1]).getVal()));
			}while(s2.nextSolution());
		}

		LOGGER.info(s2.runtimeStatistics());
		Assert.assertEquals("number of solution", s1.getNbSolutions(), s2.getNbSolutions());
	}
}