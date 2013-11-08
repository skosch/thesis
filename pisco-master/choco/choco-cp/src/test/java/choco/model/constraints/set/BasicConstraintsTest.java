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

package choco.model.constraints.set;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.set.*;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static choco.Choco.*;
import static java.text.MessageFormat.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasicConstraintsTest {
    protected final static Logger LOGGER = ChocoLogging.getTestLogger();
    private Model m;
    private Solver s;
    private SetVariable x;
    private SetVariable y;
    private SetVariable z;
    private IntegerVariable iv;
    private Constraint c1;
    private Constraint c2;
    private Constraint c3;
    private Constraint c4;

    @Before
    public void setUp() {
        LOGGER.fine("EqualXC Testing...");
        m = new CPModel();
        s = new CPSolver();
        x = makeSetVar("X", 1, 5);
        y = makeSetVar("Y", 1, 5);
        z = makeSetVar("Z", 2, 3);
    }

    @After
    public void tearDown() {
        c1 = null;
        c2 = null;
        c3 = null;
        c4 = null;
        x = null;
        y = null;
        z = null;
        iv = null;
        m = null;
        s = null;
    }

    /**
     * Test MemberX - NotMemberX
     */
    @Test
    public void test1() {
        LOGGER.finer("test1");
        c1 = member(x, 3);
        c2 = member(x, 5);
        c3 = notMember(x, 2);
        try {
            m.addConstraint(c1);
            m.addConstraint(c2);
            m.addConstraint(c3);
            s.read(m);
            s.propagate();
        } catch (ContradictionException e) {
            assertTrue(false);
        }
        assertTrue(s.getVar(x).isInDomainKernel(3));
        assertTrue(s.getVar(x).isInDomainKernel(5));
        assertTrue(!s.getVar(x).isInDomainKernel(2));
        LOGGER.info("[BasicConstraintTests,test1] x : " + x.pretty());
        LOGGER.finest("domains OK after first propagate");
    }

    /**
     * Test MemberXY
     */
    @Test
    public void test2() {
        LOGGER.finer("test2");
        iv = makeIntVar("iv", 1, 5);
        c1 = member(x, 3);
        c2 = member(x, 5);
        c3 = notMember(x, 2);
        c4 = member(x, iv);
        try {
            m.addConstraint(c4);
            m.addConstraint(c2);
            m.addConstraint(c3);
            m.addConstraint(c1);
            s.read(m);
            s.propagate();
        } catch (ContradictionException e) {
            assertTrue(false);
        }
        assertTrue(!s.getVar(iv).canBeInstantiatedTo(2));
        assertTrue(s.getVar(x).isInDomainKernel(3));
        assertTrue(s.getVar(x).isInDomainKernel(5));
        assertTrue(!s.getVar(x).isInDomainKernel(2));
        LOGGER.info("[BasicConstraintTests,test2] x : " + x.pretty());
        LOGGER.info("[BasicConstraintTests,test2] iv : " + iv.pretty());
        LOGGER.finest("domains OK after first propagate");
        s.setFirstSolution(false);
        s.generateSearchStrategy();
        s.addGoal(new AssignSetVar(new MinDomSet(s), new MinEnv()));
        s.launch();

        assertEquals(12, s.getNbSolutions());
    }

    /**
     * Test NotMemberXY
     */
    @Test
    public void test3() {
        LOGGER.finer("test3");
        iv = makeIntVar("iv", 1, 5);
        c1 = member(x, 3);
        c2 = member(x, 5);
        c3 = notMember(x, 2);
        c4 = notMember(x, iv);
        try {
            m.addConstraint(c2);
            m.addConstraint(c1);
            m.addConstraint(c3);
            m.addConstraint(c4);
            s.read(m);
            s.propagate();
        } catch (ContradictionException e) {
            assertTrue(false);
        }
        LOGGER.info("[BasicConstraintTests,test1] x : " + s.getVar(x).pretty());
        LOGGER.info("[BasicConstraintTests,test1] iv : " + s.getVar(iv).pretty());
        assertTrue(!s.getVar(iv).canBeInstantiatedTo(3));
        assertTrue(!s.getVar(iv).canBeInstantiatedTo(5));
        assertTrue(s.getVar(x).isInDomainKernel(3));
        assertTrue(s.getVar(x).isInDomainKernel(5));
        assertTrue(!s.getVar(x).isInDomainKernel(2));
        LOGGER.info("[BasicConstraintTests,test3] x : " + s.getVar(x).pretty());
        LOGGER.info("[BasicConstraintTests,test3] iv : " + s.getVar(iv).pretty());
        LOGGER.finest("domains OK after first propagate");
        s.setFirstSolution(false);
        s.generateSearchStrategy();
        s.addGoal(new AssignSetVar(new MinDomSet(s), new MinEnv()));
        s.launch();

        assertEquals(8, s.getNbSolutions());
    }

    /**
     * Test TestCardinality ==
     */
    @Test
    public void test4() {
        for (int i = 0; i < 20; i++) {
            m = new CPModel();
            s = new CPSolver();
            LOGGER.finer("test4");
            x = makeSetVar("X", 1, 5);
            iv = makeIntVar("iv", 2, 3);
            c1 = member(x, 3);
            c2 = eqCard(x, iv);   // on teste l'�galit�
            try {
                m.addConstraint(c1);
                m.addConstraint(c2);
                s.read(m);
                s.propagate();
            } catch (ContradictionException e) {
                assertTrue(false);
            }
//        s.setFirstSolution(false);
//        s.generateSearchStrategy();
            s.setVarSetSelector(new RandomSetVarSelector(s, i));
            s.setValSetSelector(new RandomSetValSelector(i + 1));
//        s.launch();
            s.solve();
            do {
                StringBuffer st = new StringBuffer();
                st.append(format("x = {0}", s.getVar(x).pretty()));
                st.append(format(", iv = {0}", s.getVar(iv).pretty()));
                LOGGER.info(st.toString());
            } while (s.nextSolution());
            LOGGER.info("Nb solution: " + s.getNbSolutions());
            assertEquals(10, s.getNbSolutions());
        }
    }

    @Test
    public void simpleTests(){
        x = makeSetVar("x", 1, 2);
        iv = makeIntVar("iv", 1, 1);
        m.addVariable(Options.V_BOUND, iv);
        m.addConstraint(eqCard(x, iv));
        s.read(m);
        s.solve();
            do{
                LOGGER.info("x = " + s.getVar(x).pretty());
                LOGGER.info("iv = " + s.getVar(iv).pretty());
            }while(s.nextSolution());
        assertEquals(s.getNbSolutions(),2);

        m = new CPModel();
        s = new CPSolver();
        m.addConstraint(leqCard(x, iv));
        s.read(m);
        s.solve();
            do{
                LOGGER.info("x = " + s.getVar(x).pretty());
                LOGGER.info("iv = " + s.getVar(iv).pretty());
            }while(s.nextSolution());
        assertEquals(s.getNbSolutions(),3);

        m = new CPModel();
        s = new CPSolver();
        m.addConstraint(geqCard(x, iv));
        s.read(m);
        s.solve();
            do{
                LOGGER.info("x = " + s.getVar(x).pretty());
                LOGGER.info("iv = " + s.getVar(iv).pretty());
            }while(s.nextSolution());
        assertEquals(s.getNbSolutions(),3);


    }

    /**
     * Test TestCardinality <=
     */
    @Test
    public void test5() {
        for (int i = 0; i < 20; i++) {
            LOGGER.finer("test5");
            m = new CPModel();
            s = new CPSolver();
            x = makeSetVar("X", 1, 3);
            iv = makeIntVar("iv", 2, 2);
            c1 = member(x, 3);
            c2 = leqCard(x, iv);   // on teste <=
            try {
                m.addConstraint(c1);
                m.addConstraint(c2);
                s.read(m);
                s.propagate();
            } catch (ContradictionException e) {
                assertTrue(false);
            }
            s.setVarSetSelector(new RandomSetVarSelector(s, i));
            s.setValSetSelector(new RandomSetValSelector(i + 1));
            s.solve();
            do{
                LOGGER.info("x = " + s.getVar(x).pretty());
                LOGGER.info("iv = " + s.getVar(iv).pretty());
            }while(s.nextSolution());

            assertEquals(3, s.getNbSolutions());
        }
    }

    /**
     * Test TestCardinality >=
     */
    @Test
    public void test6() {
        for (int i = 0; i < 20; i++) {
            LOGGER.finer("test6");
            m = new CPModel();
            s = new CPSolver();
            x = makeSetVar("X", 1, 3);
            iv = makeIntVar("iv", 1, 2);
            c1 = member(x, 3);
            c2 = geqCard(x, iv);   // on teste =>
            try {
                m.addConstraint(c1);
                m.addConstraint(c2);
                s.read(m);
                s.propagate();
            } catch (ContradictionException e) {
                assertTrue(false);
            }
            s.setVarSetSelector(new RandomSetVarSelector(s, i));
            s.setValSetSelector(new RandomSetValSelector(i + 1));
            s.solve();
            do{

                LOGGER.info("x = " + s.getVar(x).pretty());
                LOGGER.info("iv = " + s.getVar(iv).pretty());
            }while(s.nextSolution());
            LOGGER.info("Nb solution: " + s.getNbSolutions());
            assertEquals(7, s.getNbSolutions());
        }
    }

    /**
     * Test TestDisjoint
     * The number of disjoint pair of set taken in a set of initial size n is :
     * sigma_{k = 0 -> k = n} (C_n^k * 2^(n - k))
     */
    @Test
    public void test7() {
        for (int i = 0; i < 20; i++) {
            LOGGER.finer("test7");
            m = new CPModel();
            s = new CPSolver();
            x = makeSetVar("X", 1, 3);
            y = makeSetVar("Y", 1, 3);
            c1 = setDisjoint(x, y);
            try {
                m.addConstraint(c1);
                s.read(m);
                s.propagate();
            } catch (ContradictionException e) {
                assertTrue(false);
            }
            s.setVarSetSelector(new RandomSetVarSelector(s, i));
            s.setValSetSelector(new RandomSetValSelector(i + 1));
            s.solveAll();
            LOGGER.info("nbSol " + s.getNbSolutions());
            assertEquals(27, s.getNbSolutions());
        }
    }

    /**
     * Test Intersection
     */
    @Test
    public void test8() {
        for (int i = 0; i < 20; i++) {
        LOGGER.finer("test8");
            m = new CPModel();
            s = new CPSolver();
        x = makeSetVar("X", 1, 3);
        y = makeSetVar("Y", 1, 3);
        z = makeSetVar("Z", 2, 3);
        c1 = setInter(x, y, z);
        //c2 = notmember(z,2);
        try {
            //m.addConstraint(c2);
            m.addConstraint(c1);
            s.read(m);
            s.propagate();
        } catch (ContradictionException e) {
            assertTrue(false);
        }
       s.setVarSetSelector(new RandomSetVarSelector(s, i));
            s.setValSetSelector(new RandomSetValSelector(i + 1));
            s.solveAll();
        LOGGER.info("nbSol " + s.getNbSolutions());
        assertEquals(48, s.getNbSolutions());
        }
    }

    /**
	 * Test cardinality reasonnings
	 */
    @Test
    public void test9() {
		LOGGER.finer("test9");
		m = new CPModel();
		x = makeSetVar("X", 1, 3);
		m.addConstraint(geqCard(x,2));
		m.addConstraint(eqCard(x,1));
		boolean contr = false;
        Solver s = new CPSolver();
        s.read(m);
        try {
			s.propagate();
		} catch (ContradictionException e) {
			contr = true;
		}
		assertTrue(contr);
	}

    @Test
    public void test10() {
		cardtest10(true);
	}

    @Test
    public void test10_2() {
		cardtest10(false);
	}

	public void cardtest10(boolean cardr) {
		LOGGER.finer("test10");
		m = new CPModel();
		x = makeSetVar("X", 0, 5);
		y = makeSetVar("Y", 0, 5);
		z = makeSetVar("Z", 0, 5);
		m.addConstraint(setUnion(x,y,z));
		m.addConstraint(leqCard(x,2));
		m.addConstraint(leqCard(y,2));
		m.addConstraint(geqCard(z,5));
        s = new CPSolver();
        s.setCardReasoning(cardr);
        s.read(m);
        boolean contr = false;
		try {
			s.propagate();
		} catch (ContradictionException e) {
            LOGGER.info("The contradiction is seen only if cardr is set to true");
            contr = true;
		}
		assertTrue(cardr == contr);
	}

	  @Test
	    public void test11() {
	    	Model m = new CPModel();
			IntegerVariable iv = makeIntVar("iv", 1, 9);
			SetVariable sv = makeSetVar("sv", 1, 9);
			m.addConstraint(eq(iv, 8));
			m.addConstraint(eq(sv, constant(new int[] { 8 })));
			m.addConstraint(member(sv, iv));
			Solver s = new CPSolver();
			s.read(m);
			s.solveAll();
			assertTrue(s.isFeasible());
			assertEquals(1, s.getSolutionCount());
	    }
}
