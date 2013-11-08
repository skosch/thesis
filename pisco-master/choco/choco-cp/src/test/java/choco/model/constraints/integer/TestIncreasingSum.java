/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.IncreasingSum;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.Test;

import java.util.Random;
import java.util.logging.Logger;

import static choco.Choco.*;
import static org.junit.Assert.*;

/**
 * A sum constraint with increasing variables
 * junit tests
 * BC algorithm
 * User: tpetit
 * thierry.petit(a)mines-nantes.fr
 */

public class TestIncreasingSum {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();
    protected final static boolean DEBUG = false;

    public CPModel model(OneInstanceLinear inst) {
        CPModel m = new CPModel();
        m.addConstraint(Choco.increasingSum(inst.getVars(), inst.getSum()));
        return m;
    }

    public CPModel reformulation(IntegerVariable[] vvars, IntegerVariable sum) {
        CPModel m = new CPModel();
        for (int i = 0; i < vvars.length - 1; i++) {
            m.addConstraint(leq(vvars[i], vvars[i + 1]));
        }
        m.addConstraint(eq(sum, sum(vvars)));
        return m;
    }


    @Test
    public void checkIsSatisfied() {
        CPModel m = new CPModel();
        IntegerVariable[] vars = new IntegerVariable[4];
        vars[0] = makeIntVar("x1", -1, -1);
        vars[1] = makeIntVar("x2", 3, 3);
        vars[2] = makeIntVar("x3", 3, 3);
        vars[3] = makeIntVar("x4", 5, 5);
        IntegerVariable sum = makeIntVar("s", 10, 10);
        Constraint c = Choco.increasingSum(vars, sum);
        m.addConstraints(c);
        CPSolver s = new CPSolver();
        s.read(m);
        if (DEBUG) LOGGER.info(c.pretty());
        assertTrue(s.getCstr(c).isSatisfied());
    }

    @Test
    public void checkIsViolated() {
        CPModel m = new CPModel();
        IntegerVariable[] vars = new IntegerVariable[4];
        vars[0] = makeIntVar("x1", -1, -1);
        vars[1] = makeIntVar("x2", 3, 3);
        vars[2] = makeIntVar("x3", 3, 3);
        vars[3] = makeIntVar("x4", 5, 5);
        IntegerVariable sum = makeIntVar("s", 11, 11);
        Constraint c = Choco.increasingSum(vars, sum);
        m.addConstraints(c);
        CPSolver s = new CPSolver();
        s.read(m);
        if (DEBUG) LOGGER.info(c.pretty());
        assertFalse(s.getCstr(c).isSatisfied());
    }

    @Test
    public void checkIsViolated2() {
        CPModel m = new CPModel();
        IntegerVariable[] vars = new IntegerVariable[4];
        vars[0] = makeIntVar("x1", 3, 3);
        vars[1] = makeIntVar("x2", -1, -1);
        vars[2] = makeIntVar("x3", 3, 3);
        vars[3] = makeIntVar("x4", 5, 5);
        IntegerVariable sum = makeIntVar("s", 10, 10);
        Constraint c = Choco.increasingSum(vars, sum);
        m.addConstraints(c);
        CPSolver s = new CPSolver();
        s.read(m);
        if (DEBUG) LOGGER.info(c.pretty());
        assertFalse(s.getCstr(c).isSatisfied());
    }

    @Test
    public void unit_updateMax() {
        OneInstanceLinear inst = new OneInstanceLinear(4, 3, 1);
        IntegerVariable[] vars = inst.getVars();
        IntegerVariable sum = inst.getSum();

        CPModel m = new CPModel();
        m.addConstraint(Choco.increasingSum(vars, sum));
        CPSolver s = new CPSolver();
        s.read(m);
        IntDomainVar[] _vars = new IntDomainVar[vars.length];
        String res1 = "";
        for (int i = 0; i < vars.length; i++) {
            _vars[i] = s.getVar(vars[i]);
            res1 += "[" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]";
        }
        IntDomainVar _sum = s.getVar(sum);
        res1 += " sum = " + "[" + _sum.getInf() + ", " + _sum.getSup() + "]";
        IncreasingSum sc = new IncreasingSum(_vars, _sum);
        try {
            sc.updateMax();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        String res2 = "";
        int maxSum = 0;
        for (int i = 0; i < _vars.length; i++) {
            res2 += "[" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]";
            if (i > 0) {
                assertTrue(_vars[i - 1].getSup() <= _vars[i].getSup());
            }
            maxSum += _vars[i].getSup();
        }
        res2 += " sum = " + "[" + _sum.getInf() + ", " + _sum.getSup() + "]";
        assertTrue(_sum.getSup() <= maxSum);
        if (DEBUG) {
            LOGGER.info("avant \n" + res1);
            LOGGER.info("apres updateMax \n" + res2);
        }
    }

    @Test
    public void unit_updateMin() {
        OneInstanceLinear inst = new OneInstanceLinear(4, 3, 1);
        IntegerVariable[] vars = inst.getVars();
        IntegerVariable sum = inst.getSum();

        CPModel m = new CPModel();
        m.addConstraint(Choco.increasingSum(vars, sum));
        CPSolver s = new CPSolver();
        s.read(m);
        IntDomainVar[] _vars = new IntDomainVar[vars.length - 1];
        String res1 = "";
        for (int i = 0; i < vars.length - 1; i++) {
            _vars[i] = s.getVar(vars[i]);
            res1 += "[" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]";
        }
        IntDomainVar _sum = s.getVar(vars[vars.length - 1]);
        res1 += " sum = " + "[" + _sum.getInf() + ", " + _sum.getSup() + "]";
        IncreasingSum sc = new IncreasingSum(_vars, _sum);
        try {
            sc.updateMin();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        String res2 = "";
        int minSum = 0;
        for (int i = 0; i < vars.length - 1; i++) {
            res2 += "[" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]";
            if (i > 0) {
                assertTrue(_vars[i - 1].getInf() <= _vars[i].getInf());
            }
            minSum += _vars[i].getInf();
        }
        res2 += " sum = " + "[" + _sum.getInf() + ", " + _sum.getSup() + "]";
        assertTrue(_sum.getInf() >= minSum);
        if (DEBUG) {
            LOGGER.info("avant \n" + res1);
            LOGGER.info("apres updateMin \n" + res2);
        }
    }

    @Test
    public void unit_filterMax1() {
        if (DEBUG) LOGGER.info("Test filterMax() 1");
        IntegerVariable[] res = new IntegerVariable[6];
        res[0] = makeIntVar("x1", 2, 6);
        res[1] = makeIntVar("x2", 4, 7);
        res[2] = makeIntVar("x3", 4, 7);
        res[3] = makeIntVar("x4", 5, 7);
        res[4] = makeIntVar("x5", 6, 9);
        res[5] = makeIntVar("x6", 7, 9);
        IntegerVariable sum = makeIntVar("s", 28, 28);
        CPModel m = new CPModel();
        m.addConstraint(Choco.increasingSum(res, sum));
        CPSolver s = new CPSolver();
        s.read(m);
        IntDomainVar[] _vars = new IntDomainVar[res.length];
        String res1 = "";
        for (int i = 0; i < res.length; i++) {
            _vars[i] = s.getVar(res[i]);
            res1 += "[" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]";
        }
        IntDomainVar _sum = s.getVar(sum);
        res1 += " sum = " + "[" + _sum.getInf() + ", " + _sum.getSup() + "]";
        IncreasingSum sc = new IncreasingSum(_vars, _sum);
        try {
            sc.filterMax();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < _vars.length; i++) {
            assertTrue(_vars[i].getInf() == _vars[i].getSup());
            if (DEBUG) LOGGER.info("x" + i + " = [" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]");
        }
        assertTrue(_vars[0].getInf() == 2);
        assertTrue(_vars[1].getInf() == 4);
        assertTrue(_vars[2].getInf() == 4);
        assertTrue(_vars[3].getInf() == 5);
        assertTrue(_vars[4].getInf() == 6);
        assertTrue(_vars[5].getInf() == 7);
    }

    @Test
    public void unit_filterMin1() {
        if (DEBUG) LOGGER.info("Test filterMin() 1");
        IntegerVariable[] res = new IntegerVariable[6];
        res[0] = makeIntVar("x1", -3, 2);
        res[1] = makeIntVar("x2", -4, 4);
        res[2] = makeIntVar("x3", -3, 4);
        res[3] = makeIntVar("x4", -1, 5);
        res[4] = makeIntVar("x5", 2, 6);
        res[5] = makeIntVar("x6", 2, 7);
        IntegerVariable sum = makeIntVar("s", 28, 28);
        CPModel m = new CPModel();
        m.addConstraint(Choco.increasingSum(res, sum));
        CPSolver s = new CPSolver();
        s.read(m);
        IntDomainVar[] _vars = new IntDomainVar[res.length];
        String res1 = "";
        for (int i = 0; i < res.length; i++) {
            _vars[i] = s.getVar(res[i]);
            res1 += "[" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]";
        }
        IntDomainVar _sum = s.getVar(sum);
        res1 += " sum = " + "[" + _sum.getInf() + ", " + _sum.getSup() + "]";
        IncreasingSum sc = new IncreasingSum(_vars, _sum);
        try {
            sc.filterMin();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < _vars.length; i++) {
            assertTrue(_vars[i].getInf() == _vars[i].getSup());
            if (DEBUG) LOGGER.info("x" + i + " = [" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]");
        }
        assertTrue(_vars[0].getInf() == 2);
        assertTrue(_vars[1].getInf() == 4);
        assertTrue(_vars[2].getInf() == 4);
        assertTrue(_vars[3].getInf() == 5);
        assertTrue(_vars[4].getInf() == 6);
        assertTrue(_vars[5].getInf() == 7);
    }

    @Test
    public void unit_filterMin2() {
        if (DEBUG) LOGGER.info("Test filterMin() 2");
        IntegerVariable[] res = new IntegerVariable[6];
        res[0] = makeIntVar("x1", -3, 2);
        res[1] = makeIntVar("x2", -4, 4);
        res[2] = makeIntVar("x3", -3, 4);
        res[3] = makeIntVar("x4", -1, 5);
        res[4] = makeIntVar("x5", 2, 6);
        res[5] = makeIntVar("x6", 2, 7);
        IntegerVariable sum = makeIntVar("s", 27, 28);
        CPModel m = new CPModel();
        m.addConstraint(Choco.increasingSum(res, sum));
        CPSolver s = new CPSolver();
        s.read(m);
        IntDomainVar[] _vars = new IntDomainVar[res.length];
        String res1 = "";
        for (int i = 0; i < res.length; i++) {
            _vars[i] = s.getVar(res[i]);
            res1 += "[" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]";
        }
        IntDomainVar _sum = s.getVar(sum);
        res1 += " sum = " + "[" + _sum.getInf() + ", " + _sum.getSup() + "]";
        IncreasingSum sc = new IncreasingSum(_vars, _sum);
        try {
            sc.filterMin();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < _vars.length; i++) {
            if (DEBUG) LOGGER.info("x" + i + " = [" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]");
        }
        assertTrue(_vars[0].getInf() == 1 && _vars[0].getSup() == 2);
        assertTrue(_vars[1].getInf() == 3 && _vars[1].getSup() == 4);
        assertTrue(_vars[2].getInf() == 4 && _vars[2].getSup() == 4);
        assertTrue(_vars[3].getInf() == 4 && _vars[3].getSup() == 5);
        assertTrue(_vars[4].getInf() == 5 && _vars[4].getSup() == 6);
        assertTrue(_vars[5].getInf() == 6 && _vars[5].getSup() == 7);
    }


    @Test
    public void unit_filterMax2() {
        if (DEBUG) LOGGER.info("Test filterMax() 2");
        IntegerVariable[] res = new IntegerVariable[6];
        res[0] = makeIntVar("x1", 2, 6);
        res[1] = makeIntVar("x2", 4, 7);
        res[2] = makeIntVar("x3", 4, 7);
        res[3] = makeIntVar("x4", 5, 7);
        res[4] = makeIntVar("x5", 6, 9);
        res[5] = makeIntVar("x6", 7, 9);
        IntegerVariable sum = makeIntVar("s", 28, 29);
        CPModel m = new CPModel();
        m.addConstraint(Choco.increasingSum(res, sum));
        CPSolver s = new CPSolver();
        s.read(m);
        IntDomainVar[] _vars = new IntDomainVar[res.length];
        String res1 = "";
        for (int i = 0; i < res.length; i++) {
            _vars[i] = s.getVar(res[i]);
            res1 += "[" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]";
        }
        IntDomainVar _sum = s.getVar(sum);
        res1 += " sum = " + "[" + _sum.getInf() + ", " + _sum.getSup() + "]";
        IncreasingSum sc = new IncreasingSum(_vars, _sum);
        try {
            sc.filterMax();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < _vars.length; i++) {
            if (DEBUG) LOGGER.info("x" + i + " = [" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]");
        }
        assertTrue(_vars[0].getInf() == 2);
        assertTrue(_vars[0].getSup() == 3);
        assertTrue(_vars[1].getInf() == 4);
        assertTrue(_vars[1].getSup() == 4);
        assertTrue(_vars[2].getInf() == 4);
        assertTrue(_vars[2].getSup() == 5);
        assertTrue(_vars[3].getInf() == 5);
        assertTrue(_vars[3].getSup() == 6);
        assertTrue(_vars[4].getInf() == 6);
        assertTrue(_vars[4].getSup() == 7);
        assertTrue(_vars[5].getInf() == 7);
        assertTrue(_vars[5].getSup() == 8);
    }

    @Test
    public void unit_filterMax3() {
        if (DEBUG) LOGGER.info("Test filterMax() 3");
        IntegerVariable[] res = new IntegerVariable[6];
        res[0] = makeIntVar("x0", 2, 6);
        res[1] = makeIntVar("x1", 4, 7);
        res[2] = makeIntVar("x2", 4, 7);
        res[3] = makeIntVar("x3", 5, 7);
        res[4] = makeIntVar("x4", 6, 9);
        res[5] = makeIntVar("x5", 7, 9);
        IntegerVariable sum = makeIntVar("s", 28, 30);
        CPModel m = new CPModel();
        m.addConstraint(Choco.increasingSum(res, sum));
        CPSolver s = new CPSolver();
        s.read(m);
        IntDomainVar[] _dec = new IntDomainVar[res.length];
        String res1 = "";
        for (int i = 0; i < res.length; i++) {
            _dec[i] = s.getVar(res[i]);
            res1 += "[" + _dec[i].getInf() + ", " + _dec[i].getSup() + "]";
        }
        IntDomainVar _sum = s.getVar(sum);
        res1 += " sum = " + "[" + _sum.getInf() + ", " + _sum.getSup() + "]";
        IncreasingSum sc = new IncreasingSum(_dec, _sum);
        try {
            sc.filterMax();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < _dec.length; i++) {
            if (DEBUG) LOGGER.info("x" + i + " = [" + _dec[i].getInf() + ", " + _dec[i].getSup() + "]");
        }
        assertTrue(_dec[0].getInf() == 2);
        assertTrue(_dec[0].getSup() == 4);
        assertTrue(_dec[1].getInf() == 4);
        assertTrue(_dec[1].getSup() == 5);
        assertTrue(_dec[2].getInf() == 4);
        assertTrue(_dec[2].getSup() == 5);
        assertTrue(_dec[3].getInf() == 5);
        assertTrue(_dec[3].getSup() == 6);
        assertTrue(_dec[4].getInf() == 6);
        assertTrue(_dec[4].getSup() == 7);
        assertTrue(_dec[5].getInf() == 7);
        assertTrue(_dec[5].getSup() == 9);
    }

    @Test
    public void unit_filterMax4() {
        if (DEBUG) LOGGER.info("Test filterMax() 4");
        IntegerVariable[] res = new IntegerVariable[3];
        res[0] = makeIntVar("x0", -2, 3);
        res[1] = makeIntVar("x1", -3, 3);
        res[2] = makeIntVar("x2", -3, 0);
        IntegerVariable sum = makeIntVar("s", -3, 3);
        CPModel m = new CPModel();
        m.addConstraint(Choco.increasingSum(res, sum));
        CPSolver s = new CPSolver();
        s.read(m);
        IntDomainVar[] _vars = new IntDomainVar[res.length];
        String res1 = "";
        for (int i = 0; i < res.length; i++) {
            _vars[i] = s.getVar(res[i]);
            res1 += "[" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]";
        }
        IntDomainVar _sum = s.getVar(sum);
        res1 += " sum = " + "[" + _sum.getInf() + ", " + _sum.getSup() + "]";
        IncreasingSum sc = new IncreasingSum(_vars, _sum);
        try {
            sc.updateMax(); // Or the test does not makes sense
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        try {
            sc.filterMax();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < _vars.length; i++) {
            if (DEBUG) LOGGER.info("x" + i + " = [" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]");
        }
        assertTrue(_vars[0].getInf() == -2);
        assertTrue(_vars[0].getSup() == 0);
        assertTrue(_vars[1].getInf() == -3);
        assertTrue(_vars[1].getSup() == 0);
        assertTrue(_vars[2].getInf() == -3);
        assertTrue(_vars[2].getSup() == 0);
    }

    @Test
    public void unit_filterMax5() {
        if (DEBUG) LOGGER.info("Test filterMax() 5");
        IntegerVariable[] res = new IntegerVariable[3];
        res[0] = makeIntVar("x0", -2, 2);
        res[1] = makeIntVar("x1", 0, 3);
        res[2] = makeIntVar("x2", 0, 2);
        IntegerVariable sum = makeIntVar("s", -2, 3);
        CPModel m = new CPModel();
        m.addConstraint(Choco.increasingSum(res, sum));
        CPSolver s = new CPSolver();
        s.read(m);
        IntDomainVar[] _vars = new IntDomainVar[res.length];
        String res1 = "";
        for (int i = 0; i < res.length; i++) {
            _vars[i] = s.getVar(res[i]);
            res1 += "[" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]";
        }
        IntDomainVar _sum = s.getVar(sum);
        res1 += " sum = " + "[" + _sum.getInf() + ", " + _sum.getSup() + "]";
        IncreasingSum sc = new IncreasingSum(_vars, _sum);
        try {
            sc.updateMax(); // Or the test does not makes sense
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        try {
            sc.filterMax();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < _vars.length; i++) {
            if (DEBUG) LOGGER.info("x" + i + " = [" + _vars[i].getInf() + ", " + _vars[i].getSup() + "]");
        }
    }

    public void checkNbSolutions(int seed, int nbVars) {
        OneInstanceLinear inst = new OneInstanceLinear(nbVars, nbVars + 1, seed);
        // model with IncreasingSum
        Model m1 = model(inst);
        CPSolver s1 = new CPSolver();
        s1.read(m1);
        s1.solveAll();
        if (DEBUG) LOGGER.info("nodes increasing: " + s1.getNodeCount());
        if (DEBUG) LOGGER.info("time increasing : " + s1.getTimeCount());
        // model with reformulation
        Model m2 = reformulation(inst.getVars(), inst.getSum());
        CPSolver s2 = new CPSolver();
        s2.read(m2);
        s2.solveAll();
        if (DEBUG) LOGGER.info("nodes reformulation : " + s2.getNodeCount());
        if (DEBUG) LOGGER.info("time reformulation  : " + s2.getTimeCount());
        if (DEBUG) LOGGER.info("nb sol : " + s1.getNbSolutions() + "=" + s2.getNbSolutions());
        assertEquals(s1.getNbSolutions(), s2.getNbSolutions());
    }

    @Test
    public void checkNbSolutions() {
        for (int seed = 0; seed < 25; seed++) {
            if (DEBUG) LOGGER.info("seed = " + seed);
            checkNbSolutions(seed, 1);
            checkNbSolutions(seed, 2);
            checkNbSolutions(seed, 3);
            //checkNbSolutions(seed,4);
            checkNbSolutions(seed, 5);
            //checkNbSolutions(seed,7);
            checkNbSolutions(seed, 8);
        }
    }

    public void simpleSolve(int seed, int nbVars) {
        OneInstanceLinear inst = new OneInstanceLinear(nbVars, nbVars + 1, seed);
        // model with IncreasingSum
        Model m1 = model(inst);
        CPSolver s1 = new CPSolver();
        s1.read(m1);
        s1.solve();
        if (DEBUG) LOGGER.info("nbVars = " + nbVars + ", time = " + s1.getTimeCount());
    }

    @Test
    public void checkScaling() {
        for (int size = 10; size <= 5000; size = size * 10) {
            simpleSolve(1, size);
        }
    }

    private static class OneInstanceLinear {
        public int[][] vars; // for each the minimum and maximum value in its domain
        public int[] s; // minimum and maximum sum value

        public OneInstanceLinear(int[][] vars, int[] s) {
            this.vars = vars;
            this.s = s;
        }

        public OneInstanceLinear(int n, int max, int seed) {
            vars = new int[n][2];
            Random r = new Random(seed);
            int maxSum = Integer.MIN_VALUE;
            int minSum = Integer.MAX_VALUE;
            for (int i = 0; i < vars.length; i++) {
                int a = -r.nextInt(max); // <=0
                int b = r.nextInt(max); // >=0
                vars[i][0] = a;
                vars[i][1] = b;
                if (a < minSum) {
                    minSum = a;
                }
                if (b > maxSum) {
                    maxSum = b;
                }
            }
            s = new int[2];
            s[0] = minSum;
            s[1] = maxSum;
        }

        public IntegerVariable[] getVars() {
            IntegerVariable[] res = new IntegerVariable[vars.length];
            for (int i = 0; i < res.length; i++) {
                res[i] = makeIntVar("x" + i, vars[i][0], vars[i][1]);
            }
            return res;
        }

        public IntegerVariable getSum() {
            return makeIntVar("s", s[0], s[1]);
        }
    }
}