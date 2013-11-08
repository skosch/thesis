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

package choco.model.constraints.global;

import choco.Choco;
import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import static java.text.MessageFormat.format;
import java.util.logging.Logger;

/**
 * Tests for the AllDifferent constraint.
 */
public class AllDifferentTest{
    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    @Test
    public void testDummy() {
        LOGGER.info("Dummy AllDifferent currentElement...");
        CPModel m = new CPModel();
        IntegerVariable a = makeIntVar("a", 1, 2);
        IntegerVariable b = makeIntVar("b", 1, 2);
        IntegerVariable c = makeIntVar("c", 1, 4);
        IntegerVariable d = makeIntVar("d", 1, 4);
        IntegerVariable[] vars = new IntegerVariable[]{a, b, c, d};
        Constraint alldiff = allDifferent(vars);

        m.addConstraint(alldiff);

        CPSolver s = new CPSolver();

        s.read(m);
        try {
            s.propagate();
        } catch (ContradictionException e) {
            assertTrue(false);
        }
        assertEquals(s.getVar(c).getInf(), 3);
    }
    @Test
    public void testNQueen() {
        int n = 8;
        CPModel m = new CPModel();
        IntegerVariable[] queens = new IntegerVariable[n];
        IntegerVariable[] diag1 = new IntegerVariable[n];
        IntegerVariable[] diag2 = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            queens[i] = makeIntVar("Q" + i, 1, n);
            diag1[i] = makeIntVar("D1" + i, 1, 2 * n);
            diag2[i] = makeIntVar("D2" + i, -n + 1, n);
        }

        m.addConstraint(allDifferent(queens));
        for (int i = 0; i < n; i++) {
            m.addConstraint(eq(diag1[i], plus(queens[i], i)));
            m.addConstraint(eq(diag2[i], minus(queens[i], i)));
        }
        m.addConstraint(Options.C_ALLDIFFERENT_CLIQUE, allDifferent(diag1));
        m.addConstraint(Options.C_ALLDIFFERENT_CLIQUE, allDifferent(diag2));

        // diagonal constraints
        CPSolver s = new CPSolver();
        s.read(m);
        //s.setTimeLimit(30000);
        long tps = System.currentTimeMillis();
        s.solveAll();
        LOGGER.info(format("tps nreines1 {0} nbNode {1}", System.currentTimeMillis() - tps, s.getNodeCount()));
        assertEquals(92,s.getNbSolutions());
    }
     @Test
     public void testNQueen2() {
        int n = 8;
	    CPModel m = new CPModel();
        IntegerVariable[] queens = new IntegerVariable[n];
        IntegerVariable[] diag1 = new IntegerVariable[n];
        IntegerVariable[] diag2 = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            queens[i] = makeIntVar("Q" + i, 1, n);
            diag1[i] = makeIntVar("D1" + i, 1, 2 * n);
            diag2[i] = makeIntVar("D2" + i, -n + 1, n);
        }

        m.addConstraint(allDifferent(queens));
        for (int i = 0; i < n; i++) {
            m.addConstraint(eq(diag1[i], plus(queens[i], i)));
            m.addConstraint(eq(diag2[i], minus(queens[i], i)));
        }
        m.addConstraint(allDifferent(diag1));
        m.addConstraint(allDifferent(diag2));

        // diagonal constraints
        CPSolver s = new CPSolver();
        s.read(m);
        //s.setTimeLimit(30000);
        long tps = System.currentTimeMillis();
        s.solveAll();
        LOGGER.info(format("tps nreines2 {0} nbNode {1}", System.currentTimeMillis() - tps, s.getNodeCount()));
        assertEquals(92,s.getNbSolutions());
    }
    @Test
    public void testNQueen3() {
        int n = 8;
        CPModel m = new CPModel();
        IntegerVariable[] queens = new IntegerVariable[n];
        IntegerVariable[] diag1 = new IntegerVariable[n];
        IntegerVariable[] diag2 = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            queens[i] = makeIntVar("Q" + i, 1, n);
            diag1[i] = makeIntVar("D1" + i, 1, 2 * n);
            diag2[i] = makeIntVar("D2" + i, -n + 1, n);
        }
        m.addVariables(Options.V_BOUND, queens);
        m.addVariables(Options.V_BOUND, diag1);
        m.addVariables(Options.V_BOUND, diag2);

        m.addConstraint(allDifferent(queens));
        for (int i = 0; i < n; i++) {
            m.addConstraint(eq(diag1[i], plus(queens[i], i)));
            m.addConstraint(eq(diag2[i], minus(queens[i], i)));
        }
        m.addConstraint(allDifferent(diag1));
        m.addConstraint(allDifferent(diag2));

        // diagonal constraints
        CPSolver s = new CPSolver();
        s.read(m);
        //s.setTimeLimit(30000);
        long tps = System.currentTimeMillis();
        s.solveAll();
        LOGGER.info(format("tps nreines3 {0} nbNode {1}", System.currentTimeMillis() - tps, s.getNodeCount()));
        assertEquals(92,s.getNbSolutions());
    }
   @Test
    public void testLatinSquare() {
        LOGGER.info("Latin Square Test...");
        // Toutes les solutions de n=5 en 90 sec  (161280 solutions)
        final int n = 4;
        final int[] soluces = new int[]{1, 2, 12, 576, 161280};

        // Model
        CPModel m = new CPModel();

        // Variables
        IntegerVariable[] vars = new IntegerVariable[n * n];
        for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
                vars[i * n + j] = makeIntVar("C" + i + "_" + j, 1, n);
            }
		}

        // Constraints
        for (int i = 0; i < n; i++) {
            IntegerVariable[] row = new IntegerVariable[n];
            IntegerVariable[] col = new IntegerVariable[n];
            for (int x = 0; x < n; x++) {
                row[x] = vars[i * n + x];
                col[x] = vars[x * n + i];
            }
            m.addConstraint(allDifferent(row));
            m.addConstraint(allDifferent(col));
        }

       CPSolver s = new CPSolver();
       s.read(m);
        s.solve(true);

        assertEquals(soluces[n - 1], s.getNbSolutions());
        LOGGER.info(format("LatinSquare Solutions : {0}", s.getNbSolutions()));
    }
    @Test
    public void testLatinSquare2() {
        LOGGER.info("Latin Square Test...");
        // Toutes les solutions de n=5 en 90 sec  (161280 solutions)
        final int n = 4;
        final int[] soluces = new int[]{1, 2, 12, 576, 161280};

        // Model
        CPModel m = new CPModel();

        // Variables
        IntegerVariable[] vars = new IntegerVariable[n * n];
        for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
                vars[i * n + j] = makeIntVar("C" + i + "_" + j, 1, n, Options.V_BOUND);
            }
		}

        // Constraints
        for (int i = 0; i < n; i++) {
            IntegerVariable[] row = new IntegerVariable[n];
            IntegerVariable[] col = new IntegerVariable[n];
            for (int x = 0; x < n; x++) {
                row[x] = vars[i * n + x];
                col[x] = vars[x * n + i];
            }
            m.addConstraint(allDifferent(row));
            m.addConstraint(allDifferent(col));
        }

        CPSolver s = new CPSolver();
        s.read(m);
        s.solve(true);

        assertEquals(soluces[n - 1], s.getNbSolutions());
        LOGGER.info(format("LatinSquare Solutions : {0}", s.getNbSolutions()));
    }


    @Test
    public void testOneVariable(){
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v = makeIntVar("v", 1, 10);
        m.addConstraint(allDifferent(v));
        try{
            s.read(m);
            s.solve();
        }catch (Exception e){
            Assert.fail();
        }
    }

    @Test
    public void dummyTest1(){

        Model m  = new CPModel();
        IntegerVariable[] vars = new IntegerVariable[5];
        vars[0] = Choco.makeIntVar("v2", 0, 3);
        vars[1] = Choco.makeIntVar("v3", -1, 2);
        vars[2] = Choco.makeIntVar("v4", -1, -1);
        vars[3] = Choco.makeIntVar("v5", 2, 3);
        vars[4] = Choco.makeIntVar("v6", 0, 1);

        Constraint c = Choco.allDifferent(vars);
        m.addConstraint(
                c
        );
        Solver s1 = new CPSolver();
        s1.read(m);
        s1.post(s1.eq(s1.getVar(vars[4]), 0));
        s1.solve();

        Assert.assertTrue(s1.isFeasible());

        Solver s2 = new CPSolver();
        s2.read(m);

        try {
//            ((AbstractIntSConstraint)s2.getCstr(c)).awake(); <= AWAKE doesn't ACTIVE!!
            s2.propagate();
            s2.getVar(vars[4]).removeVal(1, null, true);
        } catch (ContradictionException e) {
            Assert.fail();
        }
        try {
            s2.propagate();
        } catch (ContradictionException e) {
            Assert.fail("Removing 1 from v6 should not lead to a fail!!");
        }

    }
}
