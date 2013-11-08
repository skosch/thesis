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

import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 23 f�vr. 2007
 * Time: 16:00:01
 */
public class DistanceTest {

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

	@Test
	public void test1Solve() {
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			int k = 9, k1 = 7, k2 = 6;
			IntegerVariable v0 = makeIntVar("v0", 0, 10);
			IntegerVariable v1 = makeIntVar("v1", 0, 10);
			IntegerVariable v2 = makeIntVar("v2", 0, 10);
			IntegerVariable v3 = makeIntVar("v3", 0, 10);
			m.addConstraint(distanceEQ(v0, v1, k));
			m.addConstraint(distanceEQ(v1, v2, k1));
			m.addConstraint(distanceEQ(v2, v3, k2));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed + 32));
			s.setValIntSelector(new RandomIntValSelector(seed));
			try {
				s.propagate();
			} catch (ContradictionException e) {
				LOGGER.info(e.getMessage());
			}
			s.solveAll();
			int nbNode = s.getNodeCount();
			LOGGER.info("solutions : " + s.getNbSolutions() + " nbNode : " + nbNode);
			assertEquals(s.getNbSolutions(), 4);
		}
	}

	@Test
	public void test1NeqSolve() {
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			int k = 9, k1 = 7, k2 = 6;
			IntegerVariable v0 = makeIntVar("v0", 0, 10);
			IntegerVariable v1 = makeIntVar("v1", 0, 10);
            IntegerVariable v2 = makeIntVar("v2", 0, 10);
            IntegerVariable v3 = makeIntVar("v3", 0, 10);

	        m.addConstraint(distanceNEQ(v0, v1, k));
            m.addConstraint(distanceNEQ(v1, v2, k1));
            m.addConstraint(distanceNEQ(v2, v3, k2));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed + 32));
			s.setValIntSelector(new RandomIntValSelector(seed));
			try {
				s.propagate();
			} catch (ContradictionException e) {
				LOGGER.info(e.getMessage());
			}
			s.solveAll();
			int nbNode = s.getNodeCount();
			LOGGER.info("solutions : " + s.getNbSolutions() + " nbNode : " + nbNode);
			assertEquals(s.getNbSolutions(), 12147);
		}
	}

	@Test
	public void test2SolveNegDoms() {
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			int k = 9, k1 = 7, k2 = 6;
			IntegerVariable v0 = makeIntVar("v0", -5, 5);
			IntegerVariable v1 = makeIntVar("v1", -5, 5);
			IntegerVariable v2 = makeIntVar("v2", -5, 5);
			IntegerVariable v3 = makeIntVar("v3", -5, 5);
			m.addConstraint(distanceEQ(v0, v1, k));
			m.addConstraint(distanceEQ(v1, v2, k1));
			m.addConstraint(distanceEQ(v2, v3, k2));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed + 32));
			s.setValIntSelector(new RandomIntValSelector(seed));
			try {
				s.propagate();
			} catch (ContradictionException e) {
				LOGGER.info(e.getMessage());
			}
			s.solveAll();
			int nbNode = s.getNodeCount();
			LOGGER.info("solutions : " + s.getNbSolutions() + " nbNode : " + nbNode);
			assertEquals(s.getNbSolutions(), 4);
		}
	}

	@Test
	public void test3BoundsSolve() {
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			int k = 9, k1 = 7, k2 = 6;
			IntegerVariable v0 = makeIntVar("v0", 0, 10);
			IntegerVariable v1 = makeIntVar("v1", 0, 10);
			IntegerVariable v2 = makeIntVar("v2", 0, 10);
			IntegerVariable v3 = makeIntVar("v3", 0, 10);
            m.addVariables(Options.V_BOUND, v0, v1, v2, v3);
            m.addConstraint(distanceEQ(v0, v1, k));
			m.addConstraint(distanceEQ(v1, v2, k1));
			m.addConstraint(distanceEQ(v2, v3, k2));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed + 32));
			s.setValIntSelector(new RandomIntValSelector(seed));
			try {
				s.propagate();
			} catch (ContradictionException e) {
				LOGGER.info(e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
			}
			s.solveAll();
			int nbNode = s.getNodeCount();
			LOGGER.info("solutions : " + s.getNbSolutions() + " nbNode : " + nbNode);
			assertEquals(s.getNbSolutions(), 4);
		}
	}

	@Test
	public void test3GTEnumSolve() {
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			int k = 8;
			IntegerVariable v0 = makeIntVar("v0", 0, 10);
			IntegerVariable v1 = makeIntVar("v1", 0, 10);
			m.addConstraint(distanceGT(v0, v1, k));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed + 32));
			s.setValIntSelector(new RandomIntValSelector(seed));
			try {
				s.propagate();
			} catch (ContradictionException e) {
				LOGGER.info(e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
			}
			s.solveAll();
			int nbNode = s.getNodeCount();
			LOGGER.info("solutions : " + s.getNbSolutions() + " nbNode : " + nbNode);
			assertEquals(s.getNbSolutions(), 6);
		}
	}

	@Test
	public void test3GTBoundSolve() {
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			int k = 8;
			IntegerVariable v0 = makeIntVar("v0", 0, 10);
			IntegerVariable v1 = makeIntVar("v1", 0, 10);
            m.addVariables(Options.V_BOUND, v0, v1);
            m.addConstraint(distanceGT(v0, v1, k));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed + 32));
			s.setValIntSelector(new RandomIntValSelector(seed));
			try {
				s.propagate();
			} catch (ContradictionException e) {
				LOGGER.info(e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
			}
			s.solveAll();
			int nbNode = s.getNodeCount();
			LOGGER.info("solutions : " + s.getNbSolutions() + " nbNode : " + nbNode);
			assertEquals(s.getNbSolutions(), 6);
		}
	}

	@Test
	public void test3LTSolve() {
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			int k = 2;
			IntegerVariable v0 = makeIntVar("v0", 0, 10);
			IntegerVariable v1 = makeIntVar("v1", 0, 10);
			m.addConstraint(distanceLT(v0, v1, k));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed + 32));
			s.setValIntSelector(new RandomIntValSelector(seed));
			try {
				s.propagate();
			} catch (ContradictionException e) {
				LOGGER.info(e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
			}
			s.solveAll();
			int nbNode = s.getNodeCount();
			LOGGER.info("solutions : " + s.getNbSolutions() + " nbNode : " + nbNode);
			assertEquals(s.getNbSolutions(), 31);
			assertEquals(nbNodeFromRegulatModel(seed), nbNode);
		}
	}

	private int nbNodeFromRegulatModel(int seed) {
		m = new CPModel();
		s = new CPSolver();		
		int k = 2;
		IntegerVariable v0 = makeIntVar("v0", 0, 10);
		IntegerVariable v1 = makeIntVar("v1", 0, 10);
		List<int[]> ltuple = new LinkedList<int[]>();
		for (int i = 0; i <= 10; i++) {
			for (int j = 0; j <= 10; j++) {
				if (Math.abs(i - j) < k)
					ltuple.add(new int[]{i, j});
			}
		}
		m.addConstraint(regular(new IntegerVariable[]{v0, v1}, ltuple));
		s.read(m);
		s.setVarIntSelector(new RandomIntVarSelector(s, seed + 32));
		s.setValIntSelector(new RandomIntValSelector(seed));
		try {
			s.propagate();
		} catch (ContradictionException e) {
			LOGGER.info(e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
		}
		s.solveAll();
		//LOGGER.info("solutions regular : " + s.getNbSolutions());
		return s.getNodeCount();
	}

	//********************************************************************//
	//****************************** Test on DistanceXYZ *****************//
	//********************************************************************//

	@Test
	public void testDXYZProp1() {
		IntegerVariable v0 = makeIntVar("v0", 1, 4);
		IntegerVariable v1 = makeIntVar("v1", 5, 7);
		IntegerVariable v2 = makeIntVar("v2", -100, 100);
        m.addVariables(Options.V_BOUND, v0, v1, v2);
        m.addConstraint(distanceEQ(v0, v1, v2, 0));
		s.read(m);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			assertTrue(false);
		}
		assertEquals(1, s.getVar(v2).getInf());
		assertEquals(6, s.getVar(v2).getSup());
	}

	@Test
	public void testDXYZProp1bis() {

		IntegerVariable v0 = makeIntVar("v0", 1, 4);
		IntegerVariable v1 = makeIntVar("v1", 5, 7);
		IntegerVariable v2 = makeIntVar("v2", -100, 100);
        m.addVariables(Options.V_BOUND, v0, v1, v2);
        m.addConstraint(distanceEQ(v0, v1, v2, 2));
		s.read(m);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			LOGGER.info(e.getMessage());
			assertTrue(false);
		}
		assertEquals(-1, s.getVar(v2).getInf());
		assertEquals(4, s.getVar(v2).getSup());
	}


	@Test
	public void testDXYZProp2() {

		IntegerVariable v0 = makeIntVar("v0", 1, 5);
		IntegerVariable v1 = makeIntVar("v1", 5, 10);
		IntegerVariable v2 = makeIntVar("v2", 1, 2);
        m.addVariables(Options.V_BOUND, v0, v1, v2);
        m.addConstraint(distanceEQ(v0, v1, v2, 0));
		s.read(m);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			assertTrue(false);
		}
		assertEquals(3, s.getVar(v0).getInf());
		assertEquals(7, s.getVar(v1).getSup());
	}

	@Test
	public void testDXYZProp2bis() {

		IntegerVariable v0 = makeIntVar("v0", 1, 5);
		IntegerVariable v1 = makeIntVar("v1", 5, 10);
		IntegerVariable v2 = makeIntVar("v2", 1, 2);
        m.addVariables(Options.V_BOUND, v0, v1, v2);
        m.addConstraint(distanceEQ(v0, v1, v2, -1));
		s.read(m);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			assertTrue(false);
		}
		assertEquals(4, s.getVar(v0).getInf());
		assertEquals(6, s.getVar(v1).getSup());
		s.solve();
		LOGGER.info("" + s.pretty());
	}


	@Test
	public void testDXYZProp3() {

		IntegerVariable v0 = makeIntVar("v0", 1, 5);
		IntegerVariable v1 = makeIntVar("v1", 5, 6);
		IntegerVariable v2 = makeIntVar("v2", 3, 10);
        m.addVariables(Options.V_BOUND, v0, v1, v2);
        m.addConstraint(distanceEQ(v0, v1, v2, 0));
		s.read(m);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			assertTrue(false);
		}
		assertEquals(3, s.getVar(v0).getSup());
		assertEquals(5, s.getVar(v2).getSup());
	}

	@Test
	public void testDXYZProp4() {

		IntegerVariable v0 = makeIntVar("v0", -1, 5);
		IntegerVariable v1 = makeIntVar("v1", -5, 6);
		IntegerVariable v2 = makeIntVar("v2", -2, 2);
		m.addConstraint(distanceEQ(v0, v1, v2, 0));
		s.read(m);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			assertTrue(false);
		}
		assertEquals(-3, s.getVar(v1).getInf());
		assertEquals(0, s.getVar(v2).getInf());
	}

	@Test
	public void testDXYZProp5() {

		IntegerVariable v0 = makeIntVar("v0", -1, 1);
		IntegerVariable v1 = makeIntVar("v1", -5, 6);
		IntegerVariable v2 = makeIntVar("v2", 3, 10);
		m.addConstraint(distanceEQ(v0, v1, v2, 0));
		s.read(m);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			assertTrue(false);
		}
		LOGGER.info("" + v1.pretty());
		assertTrue(!s.getVar(v1).canBeInstantiatedTo(0));
		assertTrue(!s.getVar(v1).canBeInstantiatedTo(1));
		assertTrue(!s.getVar(v1).canBeInstantiatedTo(-1));
		assertEquals(7, s.getVar(v2).getSup());
	}

	@Test
	public void testDXYZSolve1() {
		for (int seed = 0; seed < 10; seed++) {

			m = new CPModel();
			s = new CPSolver();
			IntegerVariable v0 = makeIntVar("v0", 0, 5);
			IntegerVariable v1 = makeIntVar("v1", 0, 5);
			IntegerVariable v2 = makeIntVar("v2", 0, 5);
			m.addConstraint(distanceEQ(v0, v1, v2, 0));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed + 32));
			s.setValIntSelector(new RandomIntValSelector(seed));

			s.solveAll();
			assertEquals(36, s.getNbSolutions());
		}
	}


	@Test
	public void testDXYZSolve2() {
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable v0 = makeIntVar("v0", 3, 6);
			IntegerVariable v1 = makeIntVar("v1", -3, 4);
			IntegerVariable v2 = makeIntVar("v2", 0, 5);
			IntegerVariable v3 = makeIntVar("v3", 2, 5);

			m.addConstraint(distanceEQ(v0, v1, v2, 0));
			m.addConstraint(distanceEQ(v0, v2, v3, 0));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed + 32));
			s.setValIntSelector(new RandomIntValSelector(seed));

			s.solveAll();
			LOGGER.info("nbsol " + s.getNbSolutions());
			assertEquals(getNbSolByDecomp(0), s.getNbSolutions());
		}
	}

	@Test
	public void testDXYZSolve3() {
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable v0 = makeIntVar("v0", 3, 6);
			IntegerVariable v1 = makeIntVar("v1", -3, 4);
			IntegerVariable v2 = makeIntVar("v2", 0, 5);
			IntegerVariable v3 = makeIntVar("v3", 2, 5);

			m.addConstraint(distanceLT(v0, v1, v2, 0));
			m.addConstraint(distanceLT(v0, v2, v3, 0));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed + 32));
			s.setValIntSelector(new RandomIntValSelector(seed));

			s.solveAll();
			LOGGER.info("nbsol " + s.getNbSolutions());
			assertEquals(getNbSolByDecomp(1), s.getNbSolutions());
		}
	}

	@Test
	public void testDXYZSolve4() {
		for (int seed = 0; seed < 10; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable v0 = makeIntVar("v0", 3, 6);
			IntegerVariable v1 = makeIntVar("v1", -3, 4);
			IntegerVariable v2 = makeIntVar("v2", 0, 5);
			IntegerVariable v3 = makeIntVar("v3", 2, 5);

			m.addConstraint(distanceGT(v0, v1, v2, 0));
			m.addConstraint(distanceGT(v0, v2, v3, 0));
			s.read(m);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed + 32));
			s.setValIntSelector(new RandomIntValSelector(seed));

			s.solveAll();
			LOGGER.info("nbsol " + s.getNbSolutions());
			assertEquals(getNbSolByDecomp(2), s.getNbSolutions());
		}
	}

	public int getNbSolByDecomp(int op) {
		Model m = new CPModel();
		Solver s = new CPSolver();
		IntegerVariable v0 = makeIntVar("v0", 3, 6);
		IntegerVariable v1 = makeIntVar("v1", -3, 4);
		IntegerVariable interV0V1 = makeIntVar("v01", -100, 100);
		IntegerVariable v2 = makeIntVar("v2", 0, 5);
		IntegerVariable interV0V2 = makeIntVar("v02", -100, 100);
		IntegerVariable v3 = makeIntVar("v3", 2, 5);

		m.addConstraint(eq(minus(v0, v1), interV0V1));
		m.addConstraint(eq(minus(v0, v2), interV0V2));
		if (op == 0) {
			m.addConstraint(abs(v2, interV0V1));
			m.addConstraint(abs(v3, interV0V2));
		} else if (op == 1) {
			IntegerVariable interV0V1bis = makeIntVar("v01b", -100, 100);
			IntegerVariable interV0V2bis = makeIntVar("v02b", -100, 100);
			m.addConstraint(abs(interV0V1bis, interV0V1));
			m.addConstraint(abs(interV0V2bis, interV0V2));
			m.addConstraint(lt(interV0V1bis, v2));
			m.addConstraint(lt(interV0V2bis, v3));
		} else {
			IntegerVariable interV0V1bis = makeIntVar("v01b", -100, 100);
			IntegerVariable interV0V2bis = makeIntVar("v02b", -100, 100);
			m.addConstraint(abs(interV0V1bis, interV0V1));
			m.addConstraint(abs(interV0V2bis, interV0V2));
			m.addConstraint(gt(interV0V1bis, v2));
			m.addConstraint(gt(interV0V2bis, v3));
		}
		s.read(m);
		s.solveAll();
		return s.getNbSolutions();
	}

    @Test
    public void testEQ(){
        Model m1 = new CPModel();
        IntegerVariable x = makeIntVar("x", 2, 10);
        IntegerVariable y = makeIntVar("y", 2, 10);

        m1.addConstraint(distanceEQ(x,y,-2));

        Model m2 = new CPModel();
        m2.addConstraint(eq(abs(minus(x,y)), -2));

        Solver s1 = new CPSolver();
        s1.read(m1);

        Solver s2 = new CPSolver();
        s2.read(m2);

        s1.solveAll();
        s2.solveAll();

        Assert.assertEquals("nb sol", s1.getNbSolutions(), s2.getNbSolutions());
    }

    @Test
    public void testNEQ(){
        Model m1 = new CPModel();
        IntegerVariable x = makeIntVar("x", 2, 4);
        IntegerVariable y = makeIntVar("y", 2, 4);

        m1.addConstraint(distanceNEQ(x,y,-2));

        Model m2 = new CPModel();
        m2.addConstraint(neq(abs(minus(x,y)), -2));

        Solver s1 = new CPSolver();
        s1.read(m1);

        Solver s2 = new CPSolver();
        s2.read(m2);

//        s1.solveAll();
//        s2.solveAll();
        s1.solve();
        do{
            LOGGER.info("x:"+s1.getVar(x).getVal()+" y:"+s1.getVar(y).getVal());
        }while(s1.nextSolution());
        LOGGER.info("=======");

        s2.solve();
        do{
            LOGGER.info("x:"+s2.getVar(x).getVal()+" y:"+s2.getVar(y).getVal());
        }while(s2.nextSolution());

        Assert.assertEquals("nb sol", s1.getNbSolutions(), s2.getNbSolutions());
    }

    @Test
    public void testGT(){
        Model m1 = new CPModel();
        IntegerVariable x = makeIntVar("x", 2, 10);
        IntegerVariable y = makeIntVar("y", 2, 10);

        m1.addConstraint(distanceGT(x,y,-2));

        Model m2 = new CPModel();
        m2.addConstraint(gt(abs(minus(x,y)),-2));

        Solver s1 = new CPSolver();
        s1.read(m1);

        Solver s2 = new CPSolver();
        s2.read(m2);

        s1.solveAll();
        s2.solveAll();

        Assert.assertEquals("nb sol", s1.getNbSolutions(), s2.getNbSolutions());
    }

    @Test
    public void testLT(){
        Model m1 = new CPModel();
        IntegerVariable x = makeIntVar("x", 2, 10);
        IntegerVariable y = makeIntVar("y", 2, 10);

        m1.addConstraint(distanceLT(x,y,-2));

        Model m2 = new CPModel();
        m2.addConstraint(lt(abs(minus(x,y)), -2));

        Solver s1 = new CPSolver();
        s1.read(m1);

        Solver s2 = new CPSolver();
        s2.read(m2);

        s1.solveAll();
        s2.solveAll();

        Assert.assertEquals("nb sol", s1.getNbSolutions(), s2.getNbSolutions());
    }

}

