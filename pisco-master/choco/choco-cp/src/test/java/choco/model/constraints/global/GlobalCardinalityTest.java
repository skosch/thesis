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
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.matching.GlobalCardinality;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.Model;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;

import static choco.Choco.*;
import static org.junit.Assert.*;

/**
 * Tests for the GlobalCardinality constraint.
 */
public class GlobalCardinalityTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    @Test
    public void testGCC() {
        LOGGER.info("Dummy GlobalCardinality currentElement...");
        CPModel m = new CPModel();

        IntegerVariable peter = makeIntVar("Peter", 0, 1);
        IntegerVariable paul = makeIntVar("Paul", 0, 1);
        IntegerVariable mary = makeIntVar("Mary", 0, 1);
        IntegerVariable john = makeIntVar("John", 0, 1);
        IntegerVariable bob = makeIntVar("Bob", 0, 2);
        IntegerVariable mike = makeIntVar("Mike", 1, 4);
        IntegerVariable julia = makeIntVar("Julia", 2, 4);
        IntegerVariable[] vars = new IntegerVariable[]{peter, paul, mary, john, bob, mike, julia};

        Constraint gcc = globalCardinality(vars, new int[]{1, 1, 1, 0, 0}, new int[]{2, 2, 1, 2, 2}, 0);

        m.addConstraint(gcc);

        CPSolver s = new CPSolver();
        s.read(m);

        try {
            s.propagate();
            assertEquals(2, s.getVar(bob).getInf());
            assertEquals(2, s.getVar(bob).getSup());
            s.getVar(julia).remVal(3);
            s.propagate();
        } catch (ContradictionException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testBoundGcc() {
        CPModel pb = new CPModel();
        IntegerVariable v1 = makeIntVar("v1", 2, 2);
        IntegerVariable v2 = makeIntVar("v2", 1, 2);
        IntegerVariable v3 = makeIntVar("v3", 2, 3);
        IntegerVariable v4 = makeIntVar("v4", 2, 3);
        IntegerVariable v5 = makeIntVar("v5", 1, 4);
        IntegerVariable v6 = makeIntVar("v6", 3, 4);
        pb.addVariables(Options.V_BOUND, v1, v2, v3, v4, v5, v6);

        pb.addConstraint(Options.C_GCC_BC, globalCardinality(new IntegerVariable[]{v1, v2, v3, v4, v5, v6},
                new int[]{1, 1, 1, 2},
                new int[]{3, 3, 3, 3}, 1));
        CPSolver s = new CPSolver();
        s.read(pb);

        try {
            s.propagate();
            assertTrue(s.getVar(v2).isInstantiatedTo(1));
            assertTrue(s.getVar(v5).isInstantiatedTo(4));
            assertTrue(s.getVar(v6).isInstantiatedTo(4));
        } catch (ContradictionException e) {
            assertTrue(false);
        }

        LOGGER.info(pb.varsToString());
    }

    @Test
    public void testBoundGcc4() {
        CPModel pb = new CPModel();
        IntegerVariable v1 = makeIntVar("v1", 1, 2);
        IntegerVariable v2 = makeIntVar("v2", 1, 2);
        IntegerVariable v3 = makeIntVar("v3", 1, 3);
        IntegerVariable v4 = makeIntVar("v4", 2, 3);
        IntegerVariable v5 = makeIntVar("v5", 2, 4);
        IntegerVariable v6 = makeIntVar("v6", 3, 4);
        pb.addVariables(Options.V_BOUND, v1, v2, v3, v4, v5, v6);

        pb.addConstraint(Options.C_GCC_BC, globalCardinality(new IntegerVariable[]{v1, v2, v3, v4, v5, v6},
                new int[]{3, 1, 1, 1},
                new int[]{3, 5, 5, 5}, 1));
        CPSolver s = new CPSolver();
        s.read(pb);
        try {
            s.propagate();
            assertTrue(s.getVar(v1).isInstantiatedTo(1));
            assertTrue(s.getVar(v2).isInstantiatedTo(1));
            assertTrue(s.getVar(v3).isInstantiatedTo(1));
        } catch (ContradictionException e) {
            assertTrue(false);
        }

        LOGGER.info(pb.varsToString());
    }

    @Test
    public void testBoundGcc5() {
        CPModel pb = new CPModel();
        IntegerVariable v1 = makeIntVar("v1", 1, 4);
        IntegerVariable v2 = makeIntVar("v2", 1, 4);
        IntegerVariable v3 = makeIntVar("v3", 1, 4);
        IntegerVariable v4 = makeIntVar("v4", 1, 3);
        IntegerVariable v5 = makeIntVar("v5", 1, 3);
        IntegerVariable v6 = makeIntVar("v6", 1, 3);
        pb.addVariables(Options.V_BOUND, v1, v2, v3, v4, v5, v6);

        pb.addConstraint(Options.C_GCC_BC, globalCardinality(new IntegerVariable[]{v1, v2, v3, v4, v5, v6},
                new int[]{1, 1, 1, 3},
                new int[]{5, 5, 5, 5}, 1));
        CPSolver s = new CPSolver();
        s.read(pb);
        try {
            s.propagate();
            assertTrue(s.getVar(v1).isInstantiatedTo(4));
            assertTrue(s.getVar(v2).isInstantiatedTo(4));
            assertTrue(s.getVar(v3).isInstantiatedTo(4));
        } catch (ContradictionException e) {
            assertTrue(false);
        }
        LOGGER.info(pb.varsToString());
    }

    @Test
    public void testBoundGcc6() {
        CPModel pb = new CPModel();
        IntegerVariable v1 = makeIntVar("v1", 1, 4);
        IntegerVariable v2 = makeIntVar("v2", 1, 4);
        IntegerVariable v3 = makeIntVar("v3", 1, 4);
        IntegerVariable v4 = makeIntVar("v4", 1, 4);
        IntegerVariable v5 = makeIntVar("v5", 1, 4);
        IntegerVariable v6 = makeIntVar("v6", 1, 4);
        pb.addVariables(Options.V_ENUM, v1, v2, v3, v4, v5, v6);

        pb.addConstraint(Options.C_GCC_BC, globalCardinality(new IntegerVariable[]{v1, v2, v3, v4, v5, v6},
                new int[]{1, 3, 1, 1},
                new int[]{5, 3, 5, 5}, 1));
        CPSolver s = new CPSolver();
        s.read(pb);
        try {
            s.getVar(v4).removeVal(2, null, true);
            s.getVar(v5).removeVal(2, null, true);
            s.getVar(v6).removeVal(2, null, true);
            s.propagate();
            assertTrue(s.getVar(v1).isInstantiatedTo(2));
            assertTrue(s.getVar(v2).isInstantiatedTo(2));
            assertTrue(s.getVar(v3).isInstantiatedTo(2));
        } catch (ContradictionException e) {
            assertTrue(false);
        }
        LOGGER.info(pb.varsToString());
    }

    @Test
    public void testBoundGcc6_IDEMPOTENCE() {
        CPModel pb = new CPModel();
        IntegerVariable v1 = makeIntVar("v1", 1, 4);
        IntegerVariable v2 = makeIntVar("v2", 1, 4);
        IntegerVariable v3 = makeIntVar("v3", 1, 4);
        pb.addVariables(Options.V_BOUND, v1, v2, v3);
        IntegerVariable v4 = makeIntVar("v4", 1, 3);
        IntegerVariable v5 = makeIntVar("v5", 1, 3);
        IntegerVariable v6 = makeIntVar("v6", 1, 3);

        Constraint c = globalCardinality(new IntegerVariable[]{v1, v2, v3, v4, v5, v6},
                new int[]{1, 3, 1, 1},
                new int[]{5, 3, 5, 5}, 1);
        pb.addConstraint(Options.C_GCC_BC, c);
        CPSolver s = new CPSolver();
        s.read(pb);
        try {
            s.getVar(v4).removeVal(2, null, true);
            s.getVar(v5).removeVal(2, null, true);
            s.getVar(v6).removeVal(2, null, true);
            s.propagate();
        } catch (ContradictionException e) {
            assertTrue(true);
        }
        LOGGER.info(pb.varsToString());
    }

    @Test
    public void testBoundGcc3() {
        int n = 5;
        CPModel pb = new CPModel();
        IntegerVariable[] vars = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            vars[i] = makeIntVar("var " + i, 1, n);
        }
        int[] LB2 = {0, 1, 1, 0, 3};
        int[] UB2 = {0, 1, 1, 0, 3};
        pb.addConstraint(Options.C_GCC_BC, globalCardinality(vars, LB2, UB2, 1));
        //pb.addConstraint(pb.globalCardinality(vars,1,n,LB2,UB2));
        CPSolver s = new CPSolver();
        s.read(pb);
        int cpt = 1;
        s.solve();
        StringBuffer st = new StringBuffer();
        for (int i = 0; i < n; i++) {
            st.append(MessageFormat.format("{0}", s.getVar(vars[i]).getVal()));
        }
        LOGGER.info(st.toString());
        while (s.nextSolution() == Boolean.TRUE) {
            cpt++;
            st = new StringBuffer();
            for (int i = 0; i < n; i++) {
                st.append(MessageFormat.format("{0}", s.getVar(vars[i]).getVal()));
            }
            LOGGER.info(st.toString());
        }
        LOGGER.info("nb Sol " + cpt + " time " + s.getTimeCount());
        assertEquals(20, cpt);
    }

    @Test
    public void testBoundGcc2() {
        int n = 3;
        CPModel pb = new CPModel();
        IntegerVariable[] vars = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            vars[i] = makeIntVar("var " + i, 1, n);
        }
        int[] LB2 = {0, 0, 2};
        int[] UB2 = {2, 2, 3};
        pb.addConstraint(Options.C_GCC_BC, globalCardinality(vars, LB2, UB2, 1));
        //pb.addConstraint(pb.globalCardinality(vars,1,3,LB2,UB2));
        CPSolver s = new CPSolver();
        s.read(pb);
        int cpt = 1;
        s.solve();
        StringBuffer st = new StringBuffer();
        for (int i = 0; i < n; i++) {
            st.append(MessageFormat.format("{0}", s.getVar(vars[i]).getVal()));
        }
        LOGGER.info(st.toString());
        while (s.nextSolution() == Boolean.TRUE) {
            cpt++;
            st = new StringBuffer();
            for (int i = 0; i < n; i++) {
                st.append(MessageFormat.format("{0}", s.getVar(vars[i]).getVal()));
            }
            LOGGER.info(st.toString());
        }
        LOGGER.info("nb Sol " + cpt + " time " + s.getTimeCount());
        assertEquals(7, cpt);
    }

    private void tooLongTestBugTPetit1(boolean bound) {
        int n = 10;
        CPModel pb = new CPModel();
        IntegerVariable[] vars = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            vars[i] = makeIntVar("var " + i, 1, n);
        }
        int[] LB = {0, 1, 2, 0, 0, 0, 3, 0, 0, 0};
        int[] UB = {5, 2, 2, 9, 10, 9, 5, 1, 5, 5};
        LOGGER.info("premiere gcc :");
        if (bound) {
            pb.addConstraint(Options.C_GCC_BC, globalCardinality(vars, LB, UB, 1));
        } else {
            pb.addConstraint(globalCardinality(vars, LB, UB, 0));
        }

        int[] LB2 = {0, 0, 0, 0, 0, 4, 0, 0, 0, 0};
        int[] UB2 = {10, 10, 10, 10, 10, 10, 10, 10, 10, 10};
        LOGGER.info("deuxieme gcc :");
        if (bound) {
            pb.addConstraint(Options.C_GCC_BC, globalCardinality(vars, LB2, UB2, 1));
        } else {
            pb.addConstraint(globalCardinality(vars, LB2, UB2, 1));
        }
        CPSolver s = new CPSolver();
        s.read(pb);
        int cpt = 1;
        s.solve();
        StringBuffer st = new StringBuffer();
        for (int i = 0; i < n; i++) {
            st.append(MessageFormat.format("{0}", s.getVar(vars[i]).getVal()));
        }
        LOGGER.info(st.toString());
        while (s.nextSolution() == Boolean.TRUE) {
            cpt++;
        }
        LOGGER.info("nb Sol " + cpt + " time " + s.getTimeCount() + " nbNode " + s.getNodeCount());
        assertEquals(12600, cpt);
    }

    @Test
    public void toolongtestBoundBugTPetit1() {
        tooLongTestBugTPetit1(true);
    }

    @Test
    @Ignore
    public void toolongtestBugTPetit1() {
        tooLongTestBugTPetit1(false);
    }

    @Test
    public void testBugTPetit2() {
        CPModel pb = new CPModel();
        IntegerVariable[] vars = new IntegerVariable[2];
        vars[0] = makeIntVar("x " + 0, 1, 3);
        vars[1] = makeIntVar("x " + 1, 2, 7);
        IntegerVariable[] absVars = new IntegerVariable[6];
        for (int i = 0; i < absVars.length; i++) {
            absVars[i] = makeIntVar("V" + i, 0, 6);
        }

        pb.addConstraint(or(eq(minus((vars[0]), (vars[1])), (absVars[0])),
                eq(minus((vars[1]), (vars[0])), (absVars[0]))));

        int[] LB = {0, 0, 0, 0, 2, 0, 0};
        int[] UB = {6, 6, 6, 6, 6, 6, 6};
        Constraint gcc = globalCardinality(absVars,
                LB,
                UB, 1);
        pb.addConstraint(gcc);
        CPSolver s = new CPSolver();
        s.read(pb);
        s.solve();
    }

    @Test
    public void testlatinSquareGCC() {
        latinSquareGCC(false);
    }

    @Test
    public void testlatinSquareBoundGCC() {
        latinSquareGCC(true);
    }

    private void latinSquareGCC(boolean bound) {
        LOGGER.info("Latin Square Test...");
        // Toutes les solutions de n=5 en 90 sec  (161280 solutions)
        final int n = 4;
        final int[] soluces = new int[]{1, 2, 12, 576, 161280};

        // Model
        CPModel myPb = new CPModel();

        // Variables
        IntegerVariable[] vars = new IntegerVariable[n * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                vars[i * n + j] = makeIntVar("C" + i + "_" + j, 1, n);
            }
        }

        // Constraints
        for (int i = 0; i < n; i++) {
            int[] low = new int[n];
            int[] up = new int[n];
            IntegerVariable[] row = new IntegerVariable[n];
            IntegerVariable[] col = new IntegerVariable[n];
            for (int x = 0; x < n; x++) {
                row[x] = vars[i * n + x];
                col[x] = vars[x * n + i];
                low[x] = 0;
                up[x] = 1;
            }
            if (bound) {
                myPb.addConstraint(Options.C_GCC_BC, globalCardinality(row, low, up, 1));
                myPb.addConstraint(Options.C_GCC_BC, globalCardinality(col, low, up, 1));
            } else {
                myPb.addConstraint(globalCardinality(row, low, up, 1));
                myPb.addConstraint(globalCardinality(col, low, up, 1));
            }
        }
        CPSolver s = new CPSolver();
        s.read(myPb);
        s.solve(true);

        assertEquals(soluces[n - 1], s.getNbSolutions());
        LOGGER.info("LatinSquare Solutions : " + s.getNbSolutions() + " " + s.getTimeCount());
    }

    @Test
    public void testGccEmi() {
        int n = 8;
        LOGGER.info("Le probleme des " + n + " reines");
        CPModel nreine = new CPModel();
        // mod�lisation par Ligne
        IntegerVariable[] ligne = new IntegerVariable[n];
        int i, j;
        for (i = 0; i < n; i++) {
            ligne[i] = makeIntVar("Ligne " + i, 0, n - 1);
        }
        // allez hop les constrainnnnnts
        // constraint sur la colonne (ligne deja faite)
        for (i = 0; i < n; i++) {
            for (j = i + 1; j < n; j++) {
                // contrainte ligne pas a gerer grace a la modelisation
                // contrainte colonne
                nreine.addConstraint(neq(ligne[i], ligne[j]));
                nreine.addConstraint(neq(ligne[i], plus(ligne[j], Math.abs(i - j))));
                nreine.addConstraint(neq(ligne[i], minus(ligne[j], Math.abs(i - j))));
            }
        }
        long tps = System.currentTimeMillis();
        //nreine.getSolver().setVarIntSelector(new MinDomain(nreine, ligne));
        int[] lb = new int[n];
        int[] ub = new int[n];
        for (int ii = 0; ii < 8; ii++) {
            lb[ii] = 0;
            ub[ii] = n - 1;
        }
        lb[0] = 2; // force 2 reine sur la premiere colonne, interdit donc une solution


        Constraint Gcc = globalCardinality(ligne, lb, ub, 0);
        CPSolver s = new CPSolver();
        nreine.addConstraint(Gcc);
        s.read(nreine);
        s.solve(); // en solveAll, ca sort une erreur avec le choco 1_02_3 au passage
        assertTrue(s.getNbSolutions() == 0);
        if (s.getNbSolutions() > 0) {
            StringBuffer st = new StringBuffer();
            st.append("Solution : ");
            for (IntegerVariable l : ligne) {
                st.append(MessageFormat.format("{0}/", s.getVar(l).getVal())); // les solutions
            }
            LOGGER.info(st.toString());
        }


        tps = System.currentTimeMillis() - tps;
        int nbNode = s.getNodeCount();
        LOGGER.info("temps (en ms) : " + tps + " Noeud : " + nbNode + " Nombre de solutions : " + s.getNbSolutions());

    }


    @Test
    public void testRandomBoundGcc() {
        randomGCCTest(true);
    }

    @Test
    public void testRandomGcc() {
        randomGCCTest(false);
    }

    private void randomGCCTest(boolean bound) {
        LOGGER.info("Random GlobalCardinality currentElement...");
        for (int seed = 0; seed < 20; seed++) {
            int n = 6;
            int[] min = new int[]{1, 1, 0, 0, 0, 1};
            int[] max = new int[]{1, 2, 0, 2, 3, 1};
            CPModel pb = new CPModel();
            IntegerVariable[] vars = new IntegerVariable[n];
            Random rand = new Random(seed + 102);
            for (int i = 0; i < n; i++) {
                vars[i] = makeIntVar("var " + i, 0, n - 1);
            }
            pb.addVariables(vars);
            CPSolver s = new CPSolver();
            s.read(pb);
            for (IntegerVariable var : vars) {
                int val1 = rand.nextInt(n);
                int val2 = rand.nextInt(n);
                int val3 = rand.nextInt(n);
                IntDomainVar v = s.getVar(var);
                try {
                    v.remVal(val1);
                    v.remVal(val2);
                    v.remVal(val3);
                } catch (ContradictionException e) {
                    e.printStackTrace();
                }
            }
            LOGGER.info(StringUtils.pretty(vars));
            int nbsol = getNBSolByBruteForce(vars, n, seed, min, max, s);
            Constraint gcc;
            gcc = globalCardinality(vars, min, max, 0);
            if (!bound) {
                pb.addConstraint(gcc);
            } else {
                pb.addConstraint(Options.C_GCC_BC, gcc);
            }
            s.read(pb);
            LOGGER.info(StringUtils.pretty(s.getVar(vars)));
            LOGGER.info(s.pretty());
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.setVarIntSelector(new RandomIntVarSelector(s, seed + 10));
            s.solveAll();
            LOGGER.info("" + s.getNbSolutions() + "?=" + nbsol);
            assertEquals(s.getNbSolutions(), nbsol);
        }
    }

    private int getNBSolByBruteForce(IntegerVariable[] initdom, int n, int seed, int[] min, int[] max, CPSolver s) {
        int nbsol = 0;
        int[] stuple = nextLexicoTuple(null, n - 1, n);
        while (stuple != null) {
            int[] tup = new int[stuple.length];
            System.arraycopy(stuple, 0, tup, 0, n);
            int[] occ = new int[n];
            boolean isCorrect = true;
            for (int i = 0; i < n; i++) {
                if (!s.getVar(initdom[i]).canBeInstantiatedTo(tup[i])) {
                    isCorrect = false;
                    break;
                }
            }
            if (isCorrect) {
                for (int aTup : tup) {
                    occ[aTup]++;
                }
                boolean isValid = true;
                for (int i = 0; i < occ.length; i++) {
                    if (!(occ[i] >= min[i] && occ[i] <= max[i])) {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    nbsol++;
                }
            }
            stuple = nextLexicoTuple(stuple, n - 1, n);
        }
        return nbsol;
    }

    private int[] nextLexicoTuple(int[] t, int maxval, int s) {
        if (t == null) {
            return new int[s];
        }
        for (int i = (s - 1); i >= 0; i--) {
            if (t[i] < maxval) {
                t[i]++;
                for (int j = i + 1; j < s; j++) {
                    t[j] = 0;
                }
                return t;
            }
        }
        return null;
    }

    @Test
    public void testGCCEmilien() {
        int n = 8;
        LOGGER.info("Le probleme des " + n + " reines");

        CPModel nreine = new CPModel();

        // mod�lisation par Ligne
        IntegerVariable[] ligne = new IntegerVariable[n];

        int i, j;

        for (i = 0; i < n; i++) {
            ligne[i] = makeIntVar("Ligne " + i, 0, n - 1);
        }

        // allez hop les constrainnnnnts

        // constraint sur la colonne (ligne d�ja faite)
        for (i = 0; i < n; i++) {
            for (j = i + 1; j < n; j++) {
                // contrainte ligne pas � g�rer grace � la mod�lisation
                // contrainte colonne
                nreine.addConstraint(neq(ligne[i], ligne[j]));
                nreine.addConstraint(neq(ligne[i], plus(ligne[j], Math.abs(i - j))));
                nreine.addConstraint(neq(ligne[i], minus(ligne[j], Math.abs(i - j))));
            }
        }
        long tps = System.currentTimeMillis();
        //nreine.getSolver().setVarIntSelector(new MinDomain(nreine, ligne));

        int[] lb = new int[n];
        int[] ub = new int[n];
        for (int ii = 0; ii < 8; ii++) {
            lb[ii] = 0;
            ub[ii] = n - 1;
        }

        /*
              lb[0] = 2;
              ub[0] = 2;
                 // pas de solutions => n'entraine pas de bugs
           */

        lb[7] = 2;
        ub[7] = 2;
        //  1 solution => bug

        Constraint Gcc = globalCardinality(ligne, lb, ub, 0);

        nreine.addConstraint(Gcc);
        CPSolver s = new CPSolver();
        s.read(nreine);
        s.solve(); // en solveAll, ca sort une erreur bizarre au passage
        // a tester le solve All !!!

        if (s.getNbSolutions() > 0) {
            assertTrue(false);
            StringBuffer st = new StringBuffer();
            st.append("Solution : ");
            for (IntegerVariable l : ligne) //foreach
            {
                st.append(MessageFormat.format("{0}/", s.getVar(l).getVal())); // la solution
            }
            LOGGER.info(st.toString());
        }


        tps = System.currentTimeMillis() - tps;
        int nbNode = s.getNodeCount();
        LOGGER.info("temps (en ms) : " + tps + " Noeud : " + nbNode + " Nombre de solutions : " + s.getNbSolutions());

    }

    @Test
    public void robustBoundGccTest() {
        int n = 5;
        CPModel pb = new CPModel();
        IntegerVariable[] card = makeIntVarArray("c", n, 0, n);
        pb.addConstraint(eq(sum(card), n));
        //CPSolver.setVerbosity(CPSolver.SEARCH);
        //pb.getSolver().setLoggingMaxDepth(100);
        int totnbsol = 0;
        CPSolver s = new CPSolver();
        s.read(pb);
        s.solve();
        do {
            CPModel pb2 = new CPModel();
            IntegerVariable[] vs = makeIntVarArray("c", n, 1, n);
            pb2.addVariables(Options.V_BOUND, vs);
            int[] min = new int[n];
            int[] max = new int[n];
            for (int i = 0; i < max.length; i++) {
                min[i] = s.getVar(card[i]).getVal();
                max[i] = s.getVar(card[i]).getVal();
            }
            pb2.addConstraint(Options.C_GCC_BC, globalCardinality(vs, min, max, 1));
            CPSolver s2 = new CPSolver();
            s2.read(pb2);
            s2.solveAll();
            totnbsol += s2.getNbSolutions();
            int aseertnbsol = assertNbSol(card, n, s);
            if (s2.getNbSolutions() != aseertnbsol) {
                LOGGER.info(s2.getNbSolutions() + " " + aseertnbsol);
                StringBuffer st = new StringBuffer();
                for (int i = 0; i < n; i++) {
                    st.append(MessageFormat.format(" {0}", s.getVar(card[i]).getVal()));
                }
                LOGGER.info(st.toString());
            }
            assertEquals(s2.getNbSolutions(), aseertnbsol);
        } while (s.nextSolution() == Boolean.TRUE);

        assertEquals(totnbsol, 3125);
        //if (pb.getSolver().getNbSolutions() != 3125)
        // throw new Error("stop " + seed);
    }

    private int assertNbSol(IntegerVariable[] card, int n, CPSolver s) {
        CPModel pb2 = new CPModel();
        IntegerVariable[] vs = makeIntVarArray("c", n, 1, n);
        int[] min = new int[n];
        int[] max = new int[n];
        for (int i = 0; i < max.length; i++) {
            min[i] = s.getVar(card[i]).getVal();
            max[i] = s.getVar(card[i]).getVal();
        }
        pb2.addConstraint(globalCardinality(vs, min, max, 1));
        CPSolver s2 = new CPSolver();
        s2.read(pb2);
        s2.solveAll();
        return s2.getNbSolutions();
    }

    @Test
    public void testBoundGccVar() {
        for (int seed = 0; seed < 20; seed++) {
            int n = 5;
            CPModel pb = new CPModel();
            IntegerVariable[] vs = makeIntVarArray("v", n, 1, n);
            IntegerVariable[] card = makeIntVarArray("c", n, 0, n);
            pb.addVariables(Options.V_BOUND, vs);
            pb.addVariables(Options.V_BOUND, card);
            //pb.addConstraint(pb.eq(pb.sum(card), n));
            pb.addConstraint(globalCardinality(vs, card, 1));
            CPSolver s = new CPSolver();
            s.read(pb);
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.setVarIntSelector(new RandomIntVarSelector(s, seed + 10));

            s.solveAll();
            LOGGER.info("" + s.getNbSolutions() + " nbnode " + s.getSearchStrategy().getNodeCount() + " time " + s.getSearchStrategy().getTimeCount());
            assertEquals(s.getNbSolutions(), 3125);
        }
    }

    @Test
    public void testBoundGccVarWithEnum() {
        for (int seed = 0; seed < 20; seed++) {
            int n = 5;
            CPModel pb = new CPModel();
            IntegerVariable[] vs = makeIntVarArray("v", n, 1, n);
            IntegerVariable[] card = makeIntVarArray("c", n, 0, n);
            pb.addVariables(Options.V_BOUND, card);
            //pb.addConstraint(pb.eq(pb.sum(card), n));
            pb.addConstraint(globalCardinality(vs, card, 1));
            CPSolver s = new CPSolver();
            s.read(pb);
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.setVarIntSelector(new RandomIntVarSelector(s, seed + 10));

            s.solveAll();
            LOGGER.info("" + s.getNbSolutions() + " nbnode " + s.getSearchStrategy().getNodeCount() + " time " + s.getSearchStrategy().getTimeCount());
            assertEquals(s.getNbSolutions(), 3125);
        }
    }

    @Test
    public void testBoundGccVarWithEnum2() {
        for (int seed = 0; seed < 10; seed++) {
            int n = 7;
            CPModel pb = new CPModel();
            IntegerVariable[] vs = makeIntVarArray("v", n, 4, n);
            IntegerVariable[] card = makeIntVarArray("c", n - 4 + 1, 1, 2);
            pb.addVariables(Options.V_BOUND, card);
            //pb.addConstraint(pb.eq(pb.sum(card), n));
            pb.addConstraint(globalCardinality(vs, card, 4));
            //pb.addConstraint(new GlobalCardinalityVar(vs, 4, n, card));
            CPSolver s = new CPSolver();
            s.read(pb);
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.setVarIntSelector(new RandomIntVarSelector(s, seed + 10));

            s.solveAll();
            LOGGER.info("" + s.getNbSolutions() + " nbnode " + s.getSearchStrategy().getNodeCount() + " time " + s.getSearchStrategy().getTimeCount());
            assertEquals(s.getNbSolutions(), 2520);
        }
    }


    private int f(int i, int j, int n) {
        if (j >= i) {
            return (i * n - i * (i + 1) / 2 + j - i);
        } else {
            return f(j, i - 1, n);
        }
    }

    @Test
    public void testIlogGColoring() {
        CPModel pb = new CPModel();

        long executionStart = System.currentTimeMillis();

        String arg = "31";
        int clique_size = Integer.parseInt(arg);
        LOGGER.info("Graph Coloring for " + clique_size
                + " cliques. " + new Date());
        int n = (clique_size % 2 > 0) ? clique_size + 1 : clique_size;
        boolean redundant_constraint = true;

        int size = n * (n - 1) / 2;

        int i, j;
        int nbColors = n - 1;
        IntegerVariable[] vars = makeIntVarArray("vars", size, 0, nbColors - 1);
        pb.addVariables(Options.V_ENUM, vars);
        IntegerVariable[][] cliques = new IntegerVariable[n][n - 1];
        for (i = 0; i < n; i++) {
            for (j = 0; j < n - 1; j++) {
                int node = f(i, j, n);
                IntegerVariable v = vars[node];
                cliques[i][j] = v;
            }
            //Constraint constraintAllDiff = boundAllDifferent(true, cliques[i]);
            Constraint constraintAllDiff = allDifferent(cliques[i]);
            pb.addConstraint(Options.C_GCC_BC, constraintAllDiff);
        }

        // Redundant Constraint: every color is used at least n/2 times
        int[] colAr = new int[nbColors];
        for (int k = 0; k < nbColors; k++) {
            colAr[k] = k;
        }

        if (redundant_constraint) {
            //create two int arrays to pass to chocoDistribute()
            int[] low = new int[colAr.length];
            int[] up = new int[colAr.length];
            for (int l = 0; l < colAr.length; l++) {
                low[l] = (n / 2); // low[l] = 16
                //up[l] = nbColors - 1;
                up[l] = 16;
            }
            pb.addConstraint(Options.C_GCC_BC, globalCardinality(vars, low, up, 0));
        }

        CPSolver s = new CPSolver();
        s.read(pb);
        s.setVarIntSelector(new MinDomain(s, s.getVar(vars)));
        s.setValIntSelector(new MinVal());

        s.setFirstSolution(true);
        s.generateSearchStrategy();
        s.launch();
        LOGGER.info("nb choice points " + s.getSearchStrategy().getNodeCount());
        assertTrue(s.getSearchStrategy().getNodeCount() <= 500);
        if (true) {
            //stop time
            long executionTime = System.currentTimeMillis() - executionStart;
            LOGGER.info("Execution time: " + executionTime + " msec");

            // print Solution
            LOGGER.info("Solution:");

            for (i = 0; i < n; i++) {
                LOGGER.info("\nClique " + i + ":");
                String str = "";
                for (j = 0; j < n - 1; j++) {
                    int node = f(i, j, n);
                    int color = s.getVar(vars[node]).getVal();
                    str = str + " " + node + "=" + color;
                }
                LOGGER.info(str);
            }
        } else {
            LOGGER.info("no solution found");
        }
    }

    @Test
    public void testSatisfied() {
        Model pb = new CPModel();
        IntegerVariable v1 = makeIntVar("v1", 1, 1);
        IntegerVariable v2 = makeIntVar("v2", 1, 1);
        IntegerVariable v3 = makeIntVar("v3", 2, 2);
        IntegerVariable v4 = makeIntVar("v4", 2, 2);
        Constraint c1 = globalCardinality(new IntegerVariable[]{v1, v2, v3, v4}, new int[]{1, 1}, new int[]{2, 2}, 1);
        Constraint c2 = globalCardinality(new IntegerVariable[]{v1, v2, v3, v4}, new int[]{1, 1}, new int[]{1, 3}, 1);
        pb.addConstraints(c1, c2);
        CPSolver s = new CPSolver();
        s.read(pb);
        LOGGER.info(c1.pretty());
        LOGGER.info(c2.pretty());
        assertTrue(s.getCstr(c1).isSatisfied());
        assertFalse(s.getCstr(c2).isSatisfied());
    }


    @Test
    public void testRegin() {
        Model m = new CPModel();

        //Possible values are:
        // - 0 : morning
        // - 1 : day
        // - 2 : night
        // - 3 : backup
        // - 4 : day-off
        String[] activities = new String[]{"M", "D", "N", "B", "O"};


        int[] low = new int[]{1, 1, 1, 0, 0};
        int[] upp = new int[]{2, 2, 1, 2, 2};


        IntegerVariable[] guys = new IntegerVariable[7];

        guys[0] = makeIntVar("peter", 0, 4);
        guys[1] = makeIntVar("paul", 0, 4);
        guys[2] = makeIntVar("mary", 0, 4);
        guys[3] = makeIntVar("john", 0, 4);
        guys[4] = makeIntVar("bob", 0, 4);
        guys[5] = makeIntVar("mike", 0, 4);
        guys[6] = makeIntVar("julia", 0, 4);

        //Peter cannot work during the night, cannot be backup nor in day-off
        m.addConstraint(Choco.neq(guys[0], 2));
        m.addConstraint(Choco.neq(guys[0], 3));
        m.addConstraint(Choco.neq(guys[0], 4));

        //Paul cannot work during the night, cannot be backup nor in day-off
        m.addConstraint(Choco.neq(guys[1], 2));
        m.addConstraint(Choco.neq(guys[1], 3));
        m.addConstraint(Choco.neq(guys[1], 4));

        //Mary cannot work during the night, cannot be backup nor in day-off
        m.addConstraint(Choco.neq(guys[2], 2));
        m.addConstraint(Choco.neq(guys[2], 3));
        m.addConstraint(Choco.neq(guys[2], 4));

        //John cannot work during the night, cannot be backup nor in day-off
        m.addConstraint(Choco.neq(guys[3], 2));
        m.addConstraint(Choco.neq(guys[3], 3));
        m.addConstraint(Choco.neq(guys[3], 4));

        //Bob cannot be backup nor in day-off
        m.addConstraint(Choco.neq(guys[4], 3));
        m.addConstraint(Choco.neq(guys[4], 4));

        //Mike cannot work during the morning
        m.addConstraint(Choco.neq(guys[5], 0));

        //Julia cannot work in the morning, during the day, be backup.
        m.addConstraint(Choco.neq(guys[6], 0));
        m.addConstraint(Choco.neq(guys[6], 1));
        m.addConstraint(Choco.neq(guys[6], 3));


        // Each activity has a min and max request manager
        m.addConstraint(globalCardinality(Options.C_GCC_AC, guys, low, upp, 0));


        Solver s = new CPSolver();
        s.read(m);
        if (s.solve()) {
            do {
                for (int i = 0; i < 7; i++) {
                    LOGGER.info(s.getVar(guys[i]).getName() + " : " + activities[s.getVar(guys[i]).getVal()]);
                }
                LOGGER.info("==============================");
            } while (s.nextSolution());
            Assert.assertEquals("nb solution", 12, s.getNbSolutions());
        } else {
            LOGGER.info("No solution");
            Assert.fail("no solution");
        }
    }

    @Test
    public void testNoSolution() {
        Model m = new CPModel();
        int[] low = new int[]{0, 2};
        int[] upp = new int[]{0, 2};
        IntegerVariable[] var = makeIntVarArray("v", 1, 0, 1);
        try {
            m.addConstraint(globalCardinality(Options.C_GCC_AC, var, low, upp, 0));
            Assert.fail("Not enough minimum values");
        } catch (ModelException e) {
            //nothing to do
        }
    }

    @Test
    public void testNoSolution2() {
        Model m = new CPModel();
        int[] low = new int[]{1, 0};
        int[] upp = new int[]{0, 2};
        IntegerVariable[] var = makeIntVarArray("v", 1, 0, 1);
        try {
            m.addConstraint(globalCardinality(Options.C_GCC_AC, var, low, upp, 0));
            Assert.fail("Not enough minimum values");
        } catch (ModelException e) {
            //nothing to do
        }
    }


    @Test
    public void testBugTP1() {
        Solver solver = new CPSolver();
        IntDomainVar[] vars = new IntDomainVar[3];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = solver.createBoundIntVar("var " + i, 1, 5);
        }
        int[] LB = {0, 1, 0, 3, 0};
        int[] UB = {3, 3, 3, 3, 3};
        try {
            GlobalCardinality gcc = new GlobalCardinality(vars, LB, UB, solver.getEnvironment());
            fail();
        } catch (SolverException se) {
            //ok
        } catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testBugTP2() {
        Solver solver = new CPSolver();
        IntDomainVar[] vars = new IntDomainVar[3];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = solver.createBoundIntVar("var " + i, 1, 5);
        }
        int[] LB = {0, 1, 0, 3, 0};
        int[] UB = {0, 3, 3, 2, 3};
        try {
            GlobalCardinality gcc = new GlobalCardinality(vars, LB, UB, solver.getEnvironment());
            fail();
        } catch (SolverException se) {
            //ok
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testBugTP3() {
        Solver solver = new CPSolver();
        IntDomainVar[] vars = new IntDomainVar[3];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = solver.createBoundIntVar("var " + i, 1, 5);
        }
        int[] LB = {1, 1, 1, 1, 0};
        int[] UB = {3, 3, 3, 3, 3};
        try {
            GlobalCardinality gcc = new GlobalCardinality(vars, LB, UB, solver.getEnvironment());
            fail();
        } catch (SolverException se) {
            //ok
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testBugTP4() {
        Solver solver = new CPSolver();
        IntDomainVar[] vars = new IntDomainVar[3];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = solver.createBoundIntVar("var " + i, 1, 5);
        }
        int[] LB = {0, 3, 1, 1, 0};
        int[] UB = {3, 3, 3, 3, 3};
        try {
            GlobalCardinality gcc = new GlobalCardinality(vars, LB, UB, solver.getEnvironment());
            fail();
        } catch (SolverException se) {
            //ok
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testBugTP5() {
        Solver solver = new CPSolver();
        IntDomainVar[] vars = new IntDomainVar[3];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = solver.createBoundIntVar("var " + i, 1, 5);
        }
        int[] LB = {0, 1, 0, 2, 0};
        int[] UB = {3, 3, 3, 3, 3};
        GlobalCardinality gcc = new GlobalCardinality(vars, LB, UB, solver.getEnvironment());
        solver.post(gcc);
        for (int i = 0; i < vars.length - 1; i++)
            solver.post(solver.eq(vars[i], 2));
        solver.solve();
        Assert.assertEquals("solution found", 0, solver.getNbSolutions());
    }

    /**
     * Bug 2871359
     */
    @Test
    public void testMenana1() {
        int[] occmin = {1, 1, 0, 0};
        int[] occmax = {2, 2, 0, 0};

        IntegerVariable[] v = makeIntVarArray("x", 4, 0, 2);

        Constraint c2 = globalCardinality(v, occmin, occmax, 0);    // nouvelle api, ne passe pas le test low.length != max - min + 1

        Model m = new CPModel();

        m.addConstraint(c2);


        Solver s = new CPSolver();

        s.read(m);

        s.solve();
        Assert.assertTrue(s.getNbSolutions() > 0);
    }

    /**
     * JSR331
     */
    @Test
    public void testJSR331_base() {
        int[] occmin = {1, 0, 2, 0};
        int[] occmax = {3, 7, 2, 7};

        IntegerVariable[] vars = makeIntVarArray("var", 7, 1, 4);

        Constraint c2 = globalCardinality(vars, occmin, occmax, 1);

        Model m = new CPModel();

        m.addConstraint(c2);


        Solver s = new CPSolver();

        s.read(m);

        s.solveAll();
        Assert.assertEquals(4200, s.getSolutionCount());
        Assert.assertEquals(6156, s.getNodeCount());

    }

    /**
     * JSR331
     */
    @Test
    public void testJSR331_1() {
        for (int i = 1; i < 4; i++) {
            for (int j = i + 1; j < 5; j++) {
                int[] values = {i, j};

                int[] occmin = {1, 2};
                int[] occmax = {3, 2};

                IntegerVariable[] vars = makeIntVarArray("var", 7, 1, 4);

                Constraint c2 = globalCardinality(vars, values, occmin, occmax);

                Model m = new CPModel();

                m.addConstraint(c2);


                Solver s = new CPSolver();

                s.read(m);

                s.solveAll();
                Assert.assertEquals(4200, s.getSolutionCount());
                Assert.assertEquals(6156, s.getNodeCount());

            }
        }

    }

    /**
     * JSR331
     */
    @Test
    public void testJSR331_21() {
        for (int i = 1; i < 4; i++) {
            for (int j = i + 1; j < 5; j++) {
                int[] values = {i, j};

                IntegerVariable[] vars = makeIntVarArray("var", 7, 1, 4);
                IntegerVariable[] cards = makeIntVarArray("card", 2, 1, 3);


                Constraint c2 = globalCardinality(vars, values, cards);

                Model m = new CPModel();

                m.addConstraint(c2);
                m.addConstraint(member(cards[0], new int[]{1, 2, 3}));
                m.addConstraint(member(cards[1], new int[]{2}));

                Solver s = new CPSolver();

                s.read(m);

                s.solveAll();
                Assert.assertEquals(4200, s.getSolutionCount());
                Assert.assertEquals(7632, s.getNodeCount());
            }
        }
    }

    @Test
    public void testJSR331_31() {
        IntegerVariable[] X = Choco.makeIntVarArray("X", 3, 0, 4);
        int[] values = new int[]{1, 2, 3};
        int[] low = new int[]{0, 1, 0};
        int[] up = new int[]{1, 2, 1};

        Model m = new CPModel();
        m.addConstraint(Choco.globalCardinality(X, values, low, up));

        Solver solver = new CPSolver();
        solver.read(m);

        solver.solveAll();
        Assert.assertEquals(54, solver.getSolutionCount());
        Assert.assertEquals(69, solver.getNodeCount());
    }

}
