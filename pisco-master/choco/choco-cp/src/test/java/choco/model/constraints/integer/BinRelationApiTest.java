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

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.extension.BinRelation;
import choco.kernel.solver.constraints.integer.extension.ConsistencyRelation;
import choco.kernel.solver.constraints.integer.extension.CouplesTest;
import choco.kernel.solver.constraints.integer.extension.CspBinSConstraint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.logging.Logger;

import static choco.Choco.*;
import static org.junit.Assert.assertEquals;


public class BinRelationApiTest {

	protected static final Logger LOGGER = ChocoLogging.getTestLogger();
	private CPModel m;
	private CPSolver s;
	private IntegerVariable v1, v2, v3;
	boolean[][] matrice1, matrice2;
	ArrayList<int[]> couples1;
    ArrayList<int[]> couples2;

	@Before
	public void setUp() {
		LOGGER.fine("BitSetIntDomain Testing...");
		m = new CPModel();
		s = new CPSolver();
		//v4 = makeIntVar("v4", 3, 8);
		matrice1 = new boolean[][]{
				{false, true, true, true},
				{true, false, true, false},
				{false, false, false, false},
				{true, true, true, false}};
		couples1 = new ArrayList<int[]>();
		couples1 = new ArrayList<int[]>();
		couples1.add(new int[]{1, 2});
		couples1.add(new int[]{1, 3});
		couples1.add(new int[]{1, 4});
		couples1.add(new int[]{2, 1});
		couples1.add(new int[]{2, 3});
		couples1.add(new int[]{3, 1});
		couples1.add(new int[]{3, 2});
		couples1.add(new int[]{3, 3});
		matrice2 = new boolean[][]{
				{false, true, true, false},
				{true, false, false, false},
				{false, false, true, false},
				{false, true, false, false}};
		couples2 = new ArrayList<int[]>();
		couples2.add(new int[]{1, 2});
		couples2.add(new int[]{1, 3});
		couples2.add(new int[]{2, 1});
		couples2.add(new int[]{3, 1});
		couples2.add(new int[]{4, 1});

	}

	@After
	public void tearDown() {
		v1 = null;
		v2 = null;
		v3 = null;
		m = null;
		matrice1 = null;
		matrice2 = null;
		couples1 = null;
		couples2 = null;
	}

	@Test
	public void test1FeasAc4() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addConstraint(feasPairAC(Options.C_EXT_AC3, v1, v2, matrice2));
		s.read(m);
		s.solveAll();
		assertEquals(5, s.getNbSolutions());
	}

	@Test
	public void test23FeasAc32() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addConstraint(feasPairAC(Options.C_EXT_AC32,v1, v2, couples2));
		s.read(m);
		s.solveAll();
		assertEquals(5, s.getNbSolutions());
	}


	@Test
	public void test23FeasAc322() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addConstraint(feasPairAC(Options.C_EXT_AC322,v1, v2, couples2));
		s.read(m);
		s.solveAll();
		assertEquals(5, s.getNbSolutions());
	}

    @Test
	public void test23FeasFC() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addConstraint(feasPairAC(Options.C_EXT_FC,v1, v2, couples2));
		s.read(m);
		s.solveAll();
		assertEquals(5, s.getNbSolutions());
	}

    @Test
	public void test2FeasAc4() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addConstraint(feasPairAC(Options.C_EXT_AC32,v1, v2, couples2));
		s.read(m);
		s.solveAll();
		assertEquals(5, s.getNbSolutions());
	}

	@Test
	public void test2FeasAc4bis() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addVariables(v1, v2);
		s.read(m);
		SConstraint c = s.feasPairAC(s.getVar(v1), s.getVar(v2), couples2, 2001);
		ConsistencyRelation r = (ConsistencyRelation) ((CspBinSConstraint) c).getRelation();
		s = new CPSolver();
		m.addConstraint(relationPairAC(v1, v2, (BinRelation) (r).getOpposite()));
		s.read(m);
		s.solveAll();
		assertEquals((16 - 5), s.getNbSolutions());
	}

	@Test
	public void test1InFeasAc4() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addConstraint(infeasPairAC(Options.C_EXT_AC32,v1, v2, matrice2));
		s.read(m);
		s.solveAll();
		assertEquals((16 - 5), s.getNbSolutions());
	}

	@Test
	public void test2InFeasAc4() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addConstraint(infeasPairAC(Options.C_EXT_AC32,v1, v2, couples2));
		s.read(m);
		s.solveAll();
		assertEquals((16 - 5), s.getNbSolutions());
	}

    @Test
	public void test2InFeasFC() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addConstraint(infeasPairAC(Options.C_EXT_FC,v1, v2, couples2));
		s.read(m);
		s.solveAll();
		assertEquals((16 - 5), s.getNbSolutions());
	}

    @Test
	public void test1FeasAc3() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addConstraint(feasPairAC(Options.C_EXT_AC3,v1, v2, matrice2));
		s.read(m);
		s.solveAll();
		assertEquals(5, s.getNbSolutions());
	}

	@Test
	public void test1FeasAc2001() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addConstraint(feasPairAC(Options.C_EXT_AC2001,v1, v2, matrice2));
		s.read(m);
		s.solve();
		LOGGER.info("v1 : " + s.getVar(v1).getVal() + " v2: " + s.getVar(v2).getVal());
		while (s.nextSolution() == Boolean.TRUE) {
			LOGGER.info("v1 : " + s.getVar(v1).getVal() + " v2: " + s.getVar(v2).getVal());
		}
		assertEquals(5, s.getNbSolutions());
	}

	@Test
	public void test3FeasAc2001() {
		v1 = makeIntVar("v1", 0, 4);
		v2 = makeIntVar("v2", 0, 2);
		ArrayList<int[]> feasTuple = new ArrayList<int[]>();
		feasTuple.add(new int[]{1, 1}); // x*y = 1
		feasTuple.add(new int[]{4, 2}); // x*y = 1
		Constraint c = feasPairAC(Options.C_EXT_AC2001,v1, v2, feasTuple);
		LOGGER.info("c = " + c.pretty());
		m.addConstraint(c);
		s.read(m);
		try {
			s.propagate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		s.solve();
		do {
			LOGGER.info("v1 : " + s.getVar(v1).getVal() + " v2: " + s.getVar(v2).getVal());
		} while (s.nextSolution() == Boolean.TRUE);
		assertEquals(2, s.getNbSolutions());
	}

	@Test
	public void test1FeasAc32() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addConstraint(feasPairAC(Options.C_EXT_AC32,v1, v2, matrice2));
		s.read(m);
		s.solve();
		LOGGER.info("v1 : " + s.getVar(v1).getVal() + " v2: " + s.getVar(v2).getVal());
		while (s.nextSolution() == Boolean.TRUE) {
			LOGGER.info("v1 : " + s.getVar(v1).getVal() + " v2: " + s.getVar(v2).getVal());
		}
		assertEquals(5, s.getNbSolutions());
	}

	@Test
	public void test1FeasAc322() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addConstraint(feasPairAC(Options.C_EXT_AC322,v1, v2, matrice2));
		s.read(m);
		s.solve();
		LOGGER.info("v1 : " + s.getVar(v1).getVal() + " v2: " + s.getVar(v2).getVal());
		while (s.nextSolution() == Boolean.TRUE) {
			LOGGER.info("v1 : " + s.getVar(v1).getVal() + " v2: " + s.getVar(v2).getVal());
		}
		assertEquals(5, s.getNbSolutions());
	}

	@Test
	public void test1InFeasAc3() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		m.addConstraint(infeasPairAC(Options.C_EXT_AC3,v1, v2, matrice2));
		s.read(m);
		s.solveAll();
		assertEquals((16 - 5), s.getNbSolutions());
	}

	@Test
	public void test3FeasAc4() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		v3 = makeIntVar("v3", 3, 6);
		m.addConstraint(feasPairAC(Options.C_EXT_AC322,v1, v2, matrice1));
		m.addConstraint(feasPairAC(Options.C_EXT_AC322,v2, v3, matrice2));
		s.read(m);
		s.solveAll();
		assertEquals(10, s.getNbSolutions());
	}

    @Test
	public void test3FeasFC() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		v3 = makeIntVar("v3", 3, 6);
		m.addConstraint(feasPairAC(Options.C_EXT_FC,v1, v2, matrice1));
		m.addConstraint(feasPairAC(Options.C_EXT_FC,v2, v3, matrice2));
		s.read(m);
		s.solveAll();
		assertEquals(10, s.getNbSolutions());
	}

    @Test
	public void test2FeasAc2001() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		v3 = makeIntVar("v3", 3, 6);
		m.addConstraint(feasPairAC(Options.C_EXT_AC2001,v1, v2, matrice1));
		m.addConstraint(feasPairAC(Options.C_EXT_AC2001,v2, v3, matrice2));
		s.read(m);
		s.solveAll();
		assertEquals(10, s.getNbSolutions());
	}

	@Test
	public void test2FeasAc322() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		v3 = makeIntVar("v3", 3, 6);
		m.addConstraint(feasPairAC(Options.C_EXT_AC32,v1, v2, matrice1));
		m.addConstraint(feasPairAC(Options.C_EXT_AC32,v2, v3, matrice2));
		s.read(m);
		s.solveAll();
		assertEquals(10, s.getNbSolutions());
	}

	@Test
	public void test4FeasAc4() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		v3 = makeIntVar("v3", 3, 6);
		m.addConstraint(relationPairAC(Options.C_EXT_AC32,v1, v2, new MyEquality()));
		m.addConstraint(relationPairAC(Options.C_EXT_AC32,v2, v3, new MyEquality()));
		s.read(m);
		s.solveAll();
		assertEquals(2, s.getNbSolutions());
	}

	@Test
	public void test5FeasAc4() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		v3 = makeIntVar("v3", 5, 8);
		m.addConstraint(relationPairAC(Options.C_EXT_AC32,v1, v2, new MyInequality()));
		m.addConstraint(relationPairAC(Options.C_EXT_AC32,v2, v3, new MyInequality()));
		s.read(m);
		s.solveAll();
		assertEquals(48, s.getNbSolutions());
	}

	@Test
	public void test6FeasAc4() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		v3 = makeIntVar("v3", 5, 8);
		m.addConstraint(relationPairAC(Options.C_EXT_AC32,v1, v2, (BinRelation) (new MyEquality()).getOpposite()));
		m.addConstraint(relationPairAC(Options.C_EXT_AC32,v2, v3, (BinRelation) (new MyEquality()).getOpposite()));
		s.read(m);
		s.solveAll();
		assertEquals(48, s.getNbSolutions());
	}

	@Test
	public void test7Relation() {
		v1 = makeIntVar("v1", 1, 4);
		v2 = makeIntVar("v2", 1, 4);
		v3 = makeIntVar("v3", 1, 4);
		BinRelation brel = s.makeBinRelation(new int[]{0,0},new int[]{5,5},couples2,true);
		m.addConstraint(relationPairAC(v1,v2,brel));
		m.addConstraint(relationPairAC(v2,v3,brel));
		s.read(m);
		s.solveAll();
		assertEquals(8, s.getNbSolutions());
	}

	private class MyEquality extends CouplesTest {

		public boolean checkCouple(int x, int y) {
			return x == y;
		}

	}

	private class MyInequality extends CouplesTest {

		public boolean checkCouple(int x, int y) {
			return x != y;
		}
	}

}
