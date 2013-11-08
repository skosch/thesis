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
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.integer.extension.LargeRelation;
import choco.kernel.solver.constraints.integer.extension.TuplesTest;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static choco.Choco.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NaryRelationTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

	private CPModel m;
	private CPSolver s;

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

	@Test
	public void test1() {
		IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		m.addConstraint(relationTupleFC(new IntegerVariable[]{x, y, z}, new NotAllEqual()));
		s.read(m);
		s.solveAll();
		assertEquals(120, s.getNbSolutions());
	}

	@Test
	public void test1GAC() {

		IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		m.addConstraint(relationTupleAC(new IntegerVariable[]{x, y, z}, new NotAllEqual()));
		s.read(m);
		s.solveAll();
		assertEquals(120, s.getNbSolutions());
	}

	@Test
	public void test1GAC2001() {

		IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		m.addConstraint(relationTupleAC(Options.C_EXT_AC2001,new IntegerVariable[]{x, y, z}, new NotAllEqual()));
		s.read(m);
		s.solveAll();
		assertEquals(120, s.getNbSolutions());
	}


	@Test
	public void test2() {

		IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		m.addConstraint(relationTupleFC(new IntegerVariable[]{x, y, z}, (LargeRelation) (new NotAllEqual()).getOpposite()));
		s.read(m);
		s.solveAll();
		assertEquals(5, s.getNbSolutions());
	}

	@Test
	public void test2GAC() {

		IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		m.addConstraint(relationTupleAC(new IntegerVariable[]{x, y, z}, (LargeRelation) (new NotAllEqual()).getOpposite()));
		s.read(m);
		s.solveAll();
		assertEquals(5, s.getNbSolutions());
	}

	private class NotAllEqual extends TuplesTest {

		public boolean checkTuple(int[] tuple) {
			for (int i = 1; i < tuple.length; i++) {
				if (tuple[i - 1] != tuple[i]) return true;
			}
			return false;
		}

	}

	@Test
	public void test3() {

		IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		ArrayList<int[]> forbiddenTuples = new ArrayList<int[]>();
		forbiddenTuples.add(new int[]{1, 1, 1});
		forbiddenTuples.add(new int[]{2, 2, 2});
		forbiddenTuples.add(new int[]{3, 3, 3});
		forbiddenTuples.add(new int[]{4, 4, 4});
		forbiddenTuples.add(new int[]{5, 5, 5});
		m.addConstraint(infeasTupleFC(forbiddenTuples, x, y, z));
		s.read(m);
		s.solveAll();
		assertEquals(120, s.getNbSolutions());
	}

	@Test
	public void test3GAC() {

		IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		ArrayList<int[]> forbiddenTuples = new ArrayList<int[]>();
		forbiddenTuples.add(new int[]{1, 1, 1});
		forbiddenTuples.add(new int[]{2, 2, 2});
		forbiddenTuples.add(new int[]{3, 3, 3});
		forbiddenTuples.add(new int[]{4, 4, 4});
		forbiddenTuples.add(new int[]{5, 5, 5});
		m.addConstraint(infeasTupleAC(forbiddenTuples, x, y, z));
		s.read(m);
		s.solveAll();
		assertEquals(120, s.getNbSolutions());
	}

	@Test
	public void test3GACRelation() {

		IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		IntegerVariable w = makeIntVar("w", 1, 5);
		ArrayList<int[]> allowedTuples = new ArrayList<int[]>();
		allowedTuples.add(new int[]{1, 1, 1});
		allowedTuples.add(new int[]{2, 2, 2});
		allowedTuples.add(new int[]{3, 3, 3});
		allowedTuples.add(new int[]{4, 4, 4});
		allowedTuples.add(new int[]{5, 5, 5});
		LargeRelation lrela = s.makeLargeRelation(new int[]{0, 1, 0}, new int[]{6, 6, 5}, allowedTuples, true);

		m.addConstraint(relationTupleAC(new IntegerVariable[]{x, y, z}, lrela));
		m.addConstraint(relationTupleAC(new IntegerVariable[]{x, w, y}, lrela));

		s.read(m);
		s.solveAll();
		assertEquals(5, s.getNbSolutions());
	}

	@Test
	public void test3GACRelation2001() {

		IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		IntegerVariable w = makeIntVar("w", 1, 5);
		ArrayList<int[]> allowedTuples = new ArrayList<int[]>();
		allowedTuples.add(new int[]{1, 1, 1});
		allowedTuples.add(new int[]{2, 2, 2});
		allowedTuples.add(new int[]{3, 3, 3});
		allowedTuples.add(new int[]{4, 4, 4});
		allowedTuples.add(new int[]{5, 5, 5});
		LargeRelation lrela = s.makeLargeRelation(new int[]{0, 1, 0}, new int[]{6, 6, 5}, allowedTuples, true);

		m.addConstraint(relationTupleAC(Options.C_EXT_AC2001,new IntegerVariable[]{x, y, z}, lrela));
		m.addConstraint(relationTupleAC(Options.C_EXT_AC2001,new IntegerVariable[]{x, w, y}, lrela));

		s.read(m);
		s.solveAll();
		assertEquals(5, s.getNbSolutions());
	}

    @Test    
    public void test3GACRelation2008() {

        IntegerVariable x = makeIntVar("x", 1, 5);
        IntegerVariable y = makeIntVar("y", 1, 5);
        IntegerVariable z = makeIntVar("z", 1, 5);
        IntegerVariable w = makeIntVar("w", 1, 5);
        ArrayList<int[]> allowedTuples = new ArrayList<int[]>();
        allowedTuples.add(new int[]{1, 1, 1});
        allowedTuples.add(new int[]{2, 2, 2});
        allowedTuples.add(new int[]{3, 3, 3});
        allowedTuples.add(new int[]{4, 4, 4});
        allowedTuples.add(new int[]{5, 5, 5});
        LargeRelation lrela = s.makeLargeRelation(new int[]{0, 1, 0}, new int[]{6, 6, 5}, allowedTuples, true, 2);

        m.addConstraint(relationTupleAC(Options.C_EXT_AC2008,new IntegerVariable[]{x, y, z}, lrela));
        m.addConstraint(relationTupleAC(Options.C_EXT_AC2008,new IntegerVariable[]{x, w, y}, lrela));

        s.read(m);
        s.solveAll();
        assertEquals(5, s.getNbSolutions());
    }



    @Test
	public void test3bis() {

		IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		ArrayList<int[]> forbiddenTuples = new ArrayList<int[]>();
		forbiddenTuples.add(new int[]{1, 1, 1});
		forbiddenTuples.add(new int[]{2, 2, 2});
		forbiddenTuples.add(new int[]{2, 5, 3});
		m.addConstraint(infeasTupleFC(forbiddenTuples, x, y, z));
		s.read(m);
		s.solveAll();
		assertEquals(122, s.getNbSolutions());
	}

	@Test
	public void test3bisGAC() {

		IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		ArrayList<int[]> forbiddenTuples = new ArrayList<int[]>();
		forbiddenTuples.add(new int[]{1, 1, 1});
		forbiddenTuples.add(new int[]{2, 2, 2});
		forbiddenTuples.add(new int[]{2, 5, 3});
		m.addConstraint(infeasTupleAC(forbiddenTuples, x, y, z));
		s.read(m);
		s.solveAll();
		assertEquals(122, s.getNbSolutions());
	}

	@Test
	public void test3bisbis() {

		IntegerVariable v1 = makeIntVar("v1", 0, 2);
		IntegerVariable v2 = makeIntVar("v2", 0, 4);
		ArrayList<int[]> feasTuple = new ArrayList<int[]>();
		feasTuple.add(new int[]{1, 1}); // x*y = 1
		feasTuple.add(new int[]{2, 4}); // x*y = 1
		m.addConstraint(feasTupleFC(feasTuple, v1, v2));
		s.read(m);
		s.solve();
		do {
			LOGGER.info("v1 : " + s.getVar(v1).getVal() + " v2: " + s.getVar(v2).getVal());
		} while (s.nextSolution() == Boolean.TRUE);
		assertEquals(2, s.getNbSolutions());
	}

	@Test
	public void test3bisbisGAC() {

		IntegerVariable v1 = makeIntVar("v1", 0, 2);
		IntegerVariable v2 = makeIntVar("v2", 0, 4);
		ArrayList<int[]> feasTuple = new ArrayList<int[]>();
		feasTuple.add(new int[]{1, 1}); // x*y = 1
		feasTuple.add(new int[]{2, 4}); // x*y = 1
		m.addConstraint(feasTupleAC(feasTuple, v1, v2));
		s.read(m);
		s.solve();
		do {
			LOGGER.info("v1 : " + s.getVar(v1).getVal() + " v2: " + s.getVar(v2).getVal());
		} while (s.nextSolution() == Boolean.TRUE);
		assertEquals(2, s.getNbSolutions());
	}

	@Test
	public void test3bisbisGAC2001() {

		IntegerVariable v1 = makeIntVar("v1", 0, 2);
		IntegerVariable v2 = makeIntVar("v2", 0, 4);
		ArrayList<int[]> feasTuple = new ArrayList<int[]>();
		feasTuple.add(new int[]{1, 1}); // x*y = 1
		feasTuple.add(new int[]{2, 4}); // x*y = 1
		m.addConstraint(feasTupleAC(Options.C_EXT_AC2001,feasTuple, v1, v2));
		s.read(m);
		s.solve();
		do {
			LOGGER.info("v1 : " + s.getVar(v1).getVal() + " v2: " + s.getVar(v2).getVal());
		} while (s.nextSolution() == Boolean.TRUE);
		assertEquals(2, s.getNbSolutions());
	}

    @Test
	public void test3bisbisGAC2008() {

		IntegerVariable v1 = makeIntVar("v1", 0, 2);
		IntegerVariable v2 = makeIntVar("v2", 0, 4);
		ArrayList<int[]> feasTuple = new ArrayList<int[]>();
		feasTuple.add(new int[]{1, 1}); // x*y = 1
		feasTuple.add(new int[]{2, 4}); // x*y = 1
		m.addConstraint(feasTupleAC(Options.C_EXT_AC2008,feasTuple, v1, v2));
		s.read(m);
		s.solve();
		do {
			LOGGER.info("v1 : " + s.getVar(v1).getVal() + " v2: " + s.getVar(v2).getVal());
		} while (s.nextSolution() == Boolean.TRUE);
		assertEquals(2, s.getNbSolutions());
	}

    @Test
	public void test4() {

		IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		ArrayList<int[]> forbiddenTuples = new ArrayList<int[]>();
		int cpt = 0;
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 5; j++) {
				for (int k = 1; k <= 5; k++) {
					if (i != j || i != k || k != j) {
						int[] tuple = new int[3];
						tuple[0] = i;
						tuple[1] = j;
						tuple[2] = k;
						cpt++;
						forbiddenTuples.add(tuple);
					}
				}
			}
		}
		m.addConstraint(infeasTupleFC(forbiddenTuples, x, y, z));
		s.read(m);
		s.solveAll();
		assertEquals(5, s.getNbSolutions());
	}

	@Test
	public void test5() {

		IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		ArrayList<int[]> goodTuples = new ArrayList<int[]>();
		goodTuples.add(new int[]{1, 1, 1});
		goodTuples.add(new int[]{2, 2, 2});
		goodTuples.add(new int[]{3, 3, 3});
		goodTuples.add(new int[]{4, 4, 4});
		goodTuples.add(new int[]{5, 5, 5});
		m.addConstraint(feasTupleFC(goodTuples, x, y, z));
		s.read(m);
		s.solveAll();
		assertEquals(5, s.getNbSolutions());
	}

	//
	// un petit probl�me pos� en sixi�me � la petite soeur de Ludivine :
	// trouvez l'�ge de mes trois enfants :
	//     - le produit de leurs ages est �gal � 36
	//     - la somme de leurs ages est �gal � 13
	//     - mon a�n� est blond (info cruciale)
	// ici il est r�solu de mani�re compl�tement bidon (on a d�j� la solution avant
	// de lancer le solve en ayant �limin� tous les tuples infaisables :))
	@Test
	public void test6() {

		IntegerVariable x = makeIntVar("x", 1, 12);
		IntegerVariable y = makeIntVar("y", 1, 12);
		IntegerVariable z = makeIntVar("z", 1, 12);
		ArrayList<int[]> forbiddenTuplesProduct = new ArrayList<int[]>();
		ArrayList<int[]> forbiddenTuplesAine = new ArrayList<int[]>();
		ArrayList<int[]> symetryTuples = new ArrayList<int[]>();
		for (int i = 1; i <= 12; i++) {
			for (int j = 1; j <= 12; j++) {
				for (int k = 1; k <= 12; k++) {
					int[] tuple = new int[3];
					tuple[0] = i;
					tuple[1] = j;
					tuple[2] = k;
					if (i * j * k != 36)
						forbiddenTuplesProduct.add(tuple);
					if (i > j || j > k || i > k)
						symetryTuples.add(tuple);
					if ((i == j && i > k) || (i == k && j > k) || (j == k && k > i))
						forbiddenTuplesAine.add(tuple);
				}
			}
		}

		m.addConstraint(eq(sum(new IntegerVariable[]{x, y, z}), 13));
		m.addConstraint(infeasTupleFC(forbiddenTuplesProduct, x, y, z));
		m.addConstraint(infeasTupleFC(forbiddenTuplesAine, x, y, z));
		m.addConstraint(infeasTupleFC(symetryTuples, x, y, z));
		s.read(m);
		s.solveAll();
		LOGGER.info("x " + s.getVar(x).getVal() + " y " + s.getVar(y).getVal() + " z " + s.getVar(z).getVal());
		assertEquals(1, s.getNbSolutions());
		assertEquals(2, s.getVar(x).getVal());
		assertEquals(2, s.getVar(y).getVal());
		assertEquals(9, s.getVar(z).getVal());
	}

	private void genereCst(ArrayList<int[]> tuples, int n) {
		int[] tuple = new int[n];
		int k = 0;
		for (int i = 0; i < n; i++)
			tuple[i] = 1;
		tuple[0] = 0;
		while (k < n) {
			tuple[k]++;
			if (tuple[k] > n) {
				tuple[k] = 1;
				k++;
			} else {
				if (testDouble(tuple)) {
					int[] t = new int[n];
					System.arraycopy(tuple, 0, t, 0, tuple.length);
					tuples.add(t);
				}
				k = 0;
			}
		}
	}


	private boolean testDouble(int[] tuple) {
		for (int i = 0; i < tuple.length; i++) {
			for (int j = i + 1; j < tuple.length; j++) {
				if (tuple[i] == tuple[j])
					return true;
			}
		}
		return false;
	}

	@Test
	public void testFeasibleTupleAC() throws Exception {
		int N = 9;
		java.util.ArrayList<int[]> tuples = new java.util.ArrayList<int[]>();
		tuples.add(new int[]{0, 1, 1, 1, 0, 4, 1, 1, 1, 0});
		IntegerVariable[] v = new IntegerVariable[10];
		for (int n = 0; n < v.length; n++) {
			v[n] = makeIntVar("V" + n, 0, N);
		}
        m.addVariables(Options.V_BOUND, v);
        m.addConstraint(regular(v, tuples));
		s.read(m);
		Boolean b = s.solve();
		assertEquals(true, b);
		assertEquals(0, s.getVar(v[0]).getVal());
		assertEquals(1, s.getVar(v[1]).getVal());
		assertEquals(1, s.getVar(v[2]).getVal());
		assertEquals(1, s.getVar(v[3]).getVal());
		assertEquals(0, s.getVar(v[4]).getVal());
		assertEquals(4, s.getVar(v[5]).getVal());
		assertEquals(1, s.getVar(v[6]).getVal());
		assertEquals(1, s.getVar(v[7]).getVal());
		assertEquals(1, s.getVar(v[8]).getVal());
		assertEquals(0, s.getVar(v[9]).getVal());
	}


	@Test
	public void test7() {

		int n = 7;
		IntegerVariable[] reines = new IntegerVariable[n];
		IntegerVariable[] diag1 = new IntegerVariable[n];
		IntegerVariable[] diag2 = new IntegerVariable[n];
		//Definition Variables
		for (int i = 0; i < n; i++) {
			reines[i] = makeIntVar("reine-" + i, 1, n);
		}
		for (int i = 0; i < n; i++) {
			diag1[i] = makeIntVar("diag1-" + i, -n, 2 * n);
			diag2[i] = makeIntVar("diag2-" + i, -n, 2 * n);
		}
		//Tests contraintes N-aires
		ArrayList<int[]> tuples = new ArrayList<int[]>();
		genereCst(tuples, n);
		m.addConstraint(infeasTupleFC(tuples, reines));
//      m.addConstraint(infeasTuple(reines, tuples, 2001)); TODO: que voulait dire le parametre 2001 ?

		//Definition Contraintes restantes
		for (int i = 0; i < n; i++) {
			m.addConstraint(eq(diag1[i], plus(reines[i], i)));
			m.addConstraint(eq(diag2[i], minus(reines[i], i)));

			for (int j = i + 1; j < n; j++) {
				// m.addConstraint( neq(reines[i],reines[j]));
				m.addConstraint(neq(diag1[i], diag1[j]));
				m.addConstraint(neq(diag2[i], diag2[j]));
			}
		}
		// Resolution
		s.read(m);
		s.setValIntSelector(new RandomIntValSelector(110));
		s.setVarIntSelector(new RandomIntVarSelector(s, 110));
		s.solveAll();
		assertEquals(40, s.getNbSolutions());
	}

	@Test
	public void test7GAC() {

		int n = 7;
		IntegerVariable[] reines = new IntegerVariable[n];
		IntegerVariable[] diag1 = new IntegerVariable[n];
		IntegerVariable[] diag2 = new IntegerVariable[n];
		//Definition Variables
		for (int i = 0; i < n; i++) {
			reines[i] = makeIntVar("reine-" + i, 1, n);
		}
		for (int i = 0; i < n; i++) {
			diag1[i] = makeIntVar("diag1-" + i, -n, 2 * n);
			diag2[i] = makeIntVar("diag2-" + i, -n, 2 * n);
		}
		//Tests contraintes N-aires
		ArrayList<int[]> tuples = new ArrayList<int[]>();
		genereCst(tuples, n);
		m.addConstraint(infeasTupleAC(tuples, reines));
//       m.addConstraint(infeasTuple(reines, tuples, 2001)); TODO: que voulait dire le parametre 2001 ?

		//Definition Contraintes restantes
		for (int i = 0; i < n; i++) {
			m.addConstraint(eq(diag1[i], plus(reines[i], i)));
			m.addConstraint(eq(diag2[i], minus(reines[i], i)));

			for (int j = i + 1; j < n; j++) {
				// m.addConstraint( neq(reines[i],reines[j]));
				m.addConstraint(neq(diag1[i], diag1[j]));
				m.addConstraint(neq(diag2[i], diag2[j]));
			}
		}
		// Resolution
		s.read(m);
		s.setRandomSelectors(110);
		s.solveAll();
		assertEquals(40, s.getNbSolutions());
	}

	@Test
	public void testVain() {
		IntegerVariable[] vars = new IntegerVariable[5];
		Constraint[] cons = new Constraint[4];
		//-----Construct all variables
		int id = 0;
		vars[id] = makeIntVar("v" + id++, new int[]{4, 2});
		vars[id] = makeIntVar("v" + id++, new int[]{5, 1, 4});
		vars[id] = makeIntVar("v" + id++, new int[]{3, 4, 6});
		vars[id] = makeIntVar("v" + id++, new int[]{1, 2});
		vars[id] = makeIntVar("v" + id++, new int[]{7, 8, 6, 1});
		id = 0;
		ArrayList<int[]> tuples;

		//-----Now construct all constraints
		//-----Constraint0
		tuples = new ArrayList<int[]>();
		tuples.add(new int[]{1});
		cons[id] = feasTupleFC(tuples, vars[3]);
		m.addConstraint(cons[id++]);

		//-----Constraint1
		tuples = new ArrayList<int[]>();
		tuples.add(new int[]{6, 7});
		cons[id] = feasTupleFC(tuples, vars[2], vars[4]);
		m.addConstraint(cons[id++]);

		//-----Constraint2
		tuples = new ArrayList<int[]>();
		tuples.add(new int[]{3});
		tuples.add(new int[]{4});
		cons[id] = feasTupleFC(tuples, vars[2]);
		m.addConstraint(cons[id++]);

		//-----Constraint3
		tuples = new ArrayList<int[]>();
		tuples.add(new int[]{7, 1});
		tuples.add(new int[]{8, 1});
		tuples.add(new int[]{6, 5});
		tuples.add(new int[]{1, 5});
		cons[id] = feasTupleFC(tuples, vars[4], vars[1]);
		m.addConstraint(cons[id]);

		//-----Now get solutions
		LOGGER.info("Choco Solutions");
		s.read(m);
		s.solveAll();
		assertTrue(s.isFeasible() == Boolean.FALSE);
	}

	private ArrayList<int[]> tables4() {
		int[] tuple = new int[5];
		ArrayList<int[]> tuples = new ArrayList<int[]>();
		for (int i = 1; i <= 5; i++) {
			tuple[0] = i;
			for (int j = 1; j <= 5; j++) {
				tuple[1] = j;
				for (int k = 1; k <= 5; k++) {
					tuple[2] = k;
					for (int l = 1; l <= 5; l++) {
						tuple[3] = l;
						for (int m = 1; m <= 5; m++) {
							tuple[4] = m;
							if ((i != j) && (i != k) && (i != l) && (j != k) && (j != l) && (k != l) && (m != i) && (m != j) && (m != k) && (m != l)) {
								int[] tupleToAdd = new int[5];
								System.arraycopy(tuple, 0, tupleToAdd, 0, 5);
								tuples.add(tupleToAdd);
							}
						}
					}
				}
			}
		}

		return tuples;
	}

    @Test
    public void testGAC2001OnQueen() {
        testGACPositive(2001);
    }

    @Test
    public void testGAC32OnQueen() {
        testGACPositive(32);
    }

    @Test
    public void testGAC2008OnQueen() {
        testGACPositive(2008);
    }

    public void testGACPositive(int ac) {
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			//int n = Integer.parseInt(args[0]);
			int n = 5;
            IntegerVariable[] reines = new IntegerVariable[n];
			IntegerVariable[] diag1 = new IntegerVariable[n];
			IntegerVariable[] diag2 = new IntegerVariable[n];

			for (int i = 0; i < n; i++) {
				reines[i] = makeIntVar("reine-" + i, 1, n);
				diag1[i] = makeIntVar("diag1-" + i, -n, 2 * n);
				diag2[i] = makeIntVar("diag2-" + i, -n, 2 * n);
			}

			for (int i = 0; i < n; i++) {
				m.addConstraint(eq(diag1[i], plus(reines[i], i)));
				m.addConstraint(eq(diag2[i], minus(reines[i], i)));

				for (int j = i + 1; j < n; j++) {
					//m.addConstraint(neq(reines[i], reines[j]));
					m.addConstraint(neq(diag1[i], diag1[j]));
					m.addConstraint(neq(diag2[i], diag2[j]));
				}
			}

			m.addConstraint(feasTupleAC("cp:ac" + ac,tables4(), reines));
			s.read(m);
			s.setValIntSelector(new RandomIntValSelector(seed + 120));
			s.setVarIntSelector(new RandomIntVarSelector(s, seed + 3));
			LOGGER.info("Choco Solutions");
			if (s.solve() == Boolean.TRUE) {
				do {
                    StringBuffer st = new StringBuffer();
					for (int i = 0; i < m.getNbIntVars(); i++) {
						st.append(MessageFormat.format("{0}, ", m.getIntVar(i)));
					}
					LOGGER.info(st.toString());
				} while (s.nextSolution() == Boolean.TRUE);
			}
			assertEquals(s.getNbSolutions(), 10);
		}
	}

    @Test
     public void testBoundGAC() {
        CPModel m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable x = makeIntVar("x", 0, 2, Options.V_BOUND);
        IntegerVariable y = makeIntVar("y", 0, 2, Options.V_BOUND);
        m.addConstraint(ifThenElse(gt(x, 0), eq(y, 0), Choco.TRUE));

        CPSolver s = new CPSolver();
        s.read(m);

        LOGGER.info("" + s.pretty());
        try {

            s.propagate();

            s.worldPush();
            s.post(s.eq(s.getVar(x), 1));
            s.propagate();
            LOGGER.info(s.varsToString());

            s.worldPop();
            s.post(s.eq(s.getVar(x), 1));
            s.propagate();

            assertEquals(s.getVar(y).getVal(),0);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testAcren(){
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable v1 = Choco.makeIntVar("v1", 4, 5);
        IntegerVariable v2 = Choco.makeIntVar("v2", 50000, 100000);

        m.addVariable(v1);
        m.addVariable(v2);

        List<int[]> tuples = new ArrayList<int[]>();

        tuples.add(new int[] { 1, 100000 });
        tuples.add(new int[] { 4, 100000 });

        m.addConstraint(Choco.feasTupleAC(tuples, v1, v2));

        s.read(m);
        
    }

    @Test(expected = SolverException.class)
    public void test_petersmat(){
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] vs = Choco.makeIntVarArray("vs", 5, 99, 201);

        List<int[]> tuples = new ArrayList<int[]>();
        tuples.add(new int[] { 100, 200, 100, 200, 100});
        tuples.add(new int[] { 200, 100, 200, 100, 200});

        m.addConstraint(Choco.feasTupleFC(tuples, vs));

        s.read(m);
    }

    @Test
	public void testSDEM1() throws ContradictionException {

		IntegerVariable x = makeIntVar("x", 1, 5, Options.V_BOUND);
		IntegerVariable y = makeIntVar("y", 1, 5, Options.V_BOUND);
		ArrayList<int[]> goodTuples = new ArrayList<int[]>();
		goodTuples.add(new int[]{1, 1});
		goodTuples.add(new int[]{2, 2});
		goodTuples.add(new int[]{3, 3});
		goodTuples.add(new int[]{4, 4});
		goodTuples.add(new int[]{5, 5});
        goodTuples.add(new int[]{6, 6});
        goodTuples.add(new int[]{7, 7});
        goodTuples.add(new int[]{8, 8});
        goodTuples.add(new int[]{9, 9});
        goodTuples.add(new int[]{10, 10});
		m.addConstraint(feasTupleFC(goodTuples, x, y));
		s.read(m);
        s.propagate();
        IntDomainVar X = s.getVar(x);
        IntDomainVar Y = s.getVar(y);
        X.instantiate(3, null, false);
        s.propagate();
        Assert.assertTrue("Y is not instantiated", Y.isInstantiated());
    }

    @Test
	public void testSDEM2() throws ContradictionException {

		IntegerVariable x = makeIntVar("x", 1, 10, Options.V_BOUND);
		IntegerVariable y = makeIntVar("y", 1, 10, Options.V_BOUND);
		ArrayList<int[]> goodTuples = new ArrayList<int[]>();
		goodTuples.add(new int[]{1, 1});
		goodTuples.add(new int[]{2, 2});
		goodTuples.add(new int[]{3, 3});
		goodTuples.add(new int[]{4, 4});
		goodTuples.add(new int[]{5, 5});
        goodTuples.add(new int[]{6, 6});
        goodTuples.add(new int[]{7, 7});
        goodTuples.add(new int[]{8, 8});
        goodTuples.add(new int[]{9, 9});
        goodTuples.add(new int[]{10, 10});
		m.addConstraint(feasTupleAC(goodTuples, x, y));
		s.read(m);
        s.propagate();
        IntDomainVar X = s.getVar(x);
        IntDomainVar Y = s.getVar(y);
        X.instantiate(3, null, false);
        s.propagate();
        Assert.assertTrue("Y is not instantiated", Y.isInstantiated());
    }


}
