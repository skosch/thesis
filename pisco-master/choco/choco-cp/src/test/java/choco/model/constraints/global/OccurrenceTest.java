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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static choco.Choco.*;
import static org.junit.Assert.*;

public class OccurrenceTest {
    protected static final Logger LOGGER = ChocoLogging.getTestLogger();
    private CPModel pb;
    private CPSolver s;
    private IntegerVariable x1, x2, x3, x4, x5, x6, x7, y1, x, y, n, m, xx;

    @Before
    public void setUp() {
        LOGGER.log(Level.FINE, "Occurrence Testing...");
        pb = new CPModel();
        x = makeIntVar("x", 0, 2);
        xx = makeIntVar("xx", 1, 1);
        y = makeIntVar("y", 0, 2);
        n = makeIntVar("n", 0, 5);
        m = makeIntVar("m", 0, 5);
        pb.addVariables(Options.V_BOUND, x, xx, y, n, m);
        x1 = makeIntVar("X1", 0, 10);
        x2 = makeIntVar("X2", 0, 10);
        x3 = makeIntVar("X3", 0, 10);
        x4 = makeIntVar("X4", 0, 10);
        x5 = makeIntVar("X5", 0, 10);
        x6 = makeIntVar("X6", 0, 10);
        x7 = makeIntVar("X7", 0, 10);
        y1 = makeIntVar("Y1", 0, 10);
        s = new CPSolver();
    }

    @After
    public void tearDown() {
        pb = null;
        s = null;
        x1 = x2 = x3 = x4 = x5 = x6 = x7 = y1 = x = y = n = m = xx = null;
    }

    /**
     * Simple currentElement: 5 equations on 4 variables: 1 single search solution that should be found by propagation
     */
    @Test
    public void test1() {
        LOGGER.finer("test1");
        try {
            pb.addConstraint(occurrence(3, y1, x1, x2, x3, x4, x5, x6, x7)); // OccurenceEq
            // pb.getPropagationEngine().getLogger().setVerbosity(choco.model.ILogger.TALK);
            s.read(pb);
            s.getVar(x1).setVal(3);
            s.getVar(x2).setVal(3);
            s.getVar(x3).setVal(3);
            s.getVar(x4).remVal(3);
            s.getVar(x5).remVal(3);
            s.propagate();
            assertTrue(s.getVar(y1).getInf() >= 3);
            assertTrue(s.getVar(y1).getSup() <= 5);
        } catch (ContradictionException e) {
            assertFalse(true);
        }
    }

    @Test
    public void test2() {
        LOGGER.finer("test2");
        try {
            pb.addConstraint(occurrence(3, y1, x1, x2, x3));
            pb.addConstraint(occurrence(4, y1, x1, x5, x4, x6));
            s.read(pb);
            s.getVar(x1).setVal(3);
            s.getVar(y1).setInf(3);
            s.propagate();
            assertTrue(s.getVar(x2).isInstantiatedTo(3));
            assertTrue(s.getVar(x3).isInstantiatedTo(3));
            assertTrue(s.getVar(x5).isInstantiatedTo(4));
            assertTrue(s.getVar(x4).isInstantiatedTo(4));
            assertTrue(s.getVar(x6).isInstantiatedTo(4));
        } catch (ContradictionException e) {
            assertFalse(true);
        }
    }

    @Test
    public void test3() {
        LOGGER.finer("test3 : first old choco currentElement");
        try {
            pb.addConstraint(occurrence(1, n, x, y));
            pb.addConstraint(occurrence(2, m, x, y));
            // pb.getPropagationEngine().getLogger().setVerbosity(choco.model.ILogger.TALK);
            s.read(pb);
            s.propagate();
            s.getVar(n).setVal(0);
            s.getVar(x).setSup(1);
            s.propagate();
            assertTrue(s.getVar(x).getVal() == 0);
        } catch (ContradictionException e) {
            assertFalse(true);
        }
    }

    @Test
    public void test4() {
        LOGGER.finer("test3 : third old choco currentElement");
        try {
            pb.addConstraint(occurrence(1, n, xx, m));
            // pb.getPropagationEngine().getLogger().setVerbosity(choo.model.ILogger.TALK);
            s.read(pb);
            s.propagate();
            assertTrue(s.getVar(n).getInf() >= 1);
        } catch (ContradictionException e) {
            assertFalse(true);
        }
    }

    @Test
    public void testMagicSeries() {
        int n = 4;
        CPModel pb = new CPModel();
        IntegerVariable[] vs = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            vs[i] = makeIntVar("" + i, 0, n - 1);
        }
        for (int i = 0; i < n; i++) {
            pb.addConstraint(occurrence(vs[i], vs, i));
        }
        pb.addConstraint(eq(sum(vs), n));     // contrainte redondante 1
        int[] coeff2 = new int[n - 1];
        IntegerVariable[] vs2 = new IntegerVariable[n - 1];
        for (int i = 1; i < n; i++) {
            coeff2[i - 1] = i;
            vs2[i - 1] = vs[i];
        }
        pb.addConstraint(eq(scalar(coeff2, vs2), n)); // contrainte redondante 2
        s.read(pb);
        s.solve();
        do {
            StringBuffer st = new StringBuffer();
            for (IntegerVariable v : vs) {
                st.append(s.getVar(v).getVal()).append(" ");
            }
            LOGGER.info(st.toString());
        } while (s.nextSolution() == Boolean.TRUE);
        assertEquals(2, s.getNbSolutions());
    }

    @Test
    public void testRandomProblems() {
        for (int bigseed = 0; bigseed < 5; bigseed++) {
            int nbsol, nbsol2;
            //nb solutions of the gac constraint
            int realNbSol = randomOcc(-1, bigseed,true,1,true);
            //nb solutions of occurrence + enum
            nbsol = randomOcc(realNbSol, bigseed, true, 3, false);
            //b solutions of occurrences + bound
            nbsol2 = randomOcc(realNbSol, bigseed, false, 3, false);
            LOGGER.info(nbsol + " " + nbsol2 + " " + realNbSol);
            assertEquals(nbsol, nbsol2);
            assertEquals(nbsol, realNbSol);
        }
    }


    public int randomOcc(int nbsol, int seed, boolean enumvar, int nbtest, boolean gac) {
        for (int interseed = 0; interseed < nbtest; interseed++) {
            int nbOcc = 2;
            int nbVar = 9;
            int sizeDom = 4;
            int sizeOccurence = 4;

            CPModel mod = new CPModel();
            IntegerVariable[] vars;
            vars = makeIntVarArray("e", nbVar, 0, sizeDom);
            if (enumvar) {
                mod.addVariables(Options.V_ENUM, vars);
            } else {
                mod.addVariables(Options.V_BOUND, vars);
            }

            List<IntegerVariable> lvs = new LinkedList<IntegerVariable>();
            lvs.addAll(Arrays.asList(vars));

            Random rand = new Random(seed);
            for (int i = 0; i < nbOcc; i++) {
                IntegerVariable[] vs = new IntegerVariable[sizeOccurence];
                for (int j = 0; j < sizeOccurence; j++) {
                    IntegerVariable iv = lvs.get(rand.nextInt(lvs.size()));
                    lvs.remove(iv);
                    vs[j] = iv;
                }
                IntegerVariable ivc = lvs.get(rand.nextInt(lvs.size()));
                int val = rand.nextInt(sizeDom);
                if (gac) {
                    mod.addConstraint(getTableForOccurence(vs, ivc, val, sizeDom));
                } else {
                    mod.addConstraint(occurrence(ivc, vs, val));
                }
            }
            mod.addConstraint(eq(plus(vars[0], vars[3]), vars[6]));

            CPSolver s = new CPSolver();
            s.read(mod);

            s.setValIntSelector(new RandomIntValSelector(interseed));
            s.setVarIntSelector(new RandomIntVarSelector(s, interseed + 10));

            s.solveAll();
            if (nbsol == -1) {
                nbsol = s.getNbSolutions();
                LOGGER.info("GAC NBSOL : " + s.getNbSolutions() + " " + s.getNodeCount() + " " + s.getTimeCount());
            } else {
                LOGGER.info(interseed + " NB solutions " + s.getNbSolutions() + " " + s.getNodeCount() + " " + s.getTimeCount());
                assertEquals(nbsol,s.getNbSolutions());
            }

        }
        return nbsol;
    }

    /**
     * generate a table to encode an occurrence constraint.
     * @param vs array of variables
     * @param occ occurence variable
     * @param val value
     * @param ub upper bound
     * @return Constraint
     */
    public Constraint getTableForOccurence(IntegerVariable[] vs, IntegerVariable occ, int val, int ub) {
        CPModel mod = new CPModel();
        IntegerVariable[] vars;
        vars = makeIntVarArray("e", vs.length + 1, 0, ub);
        mod.addVariables(Options.V_ENUM, vars);
        CPSolver s = new CPSolver();
        s.read(mod);

        List<int[]> tuples = new LinkedList<int[]>();
        s.solve();
        do {
            int[] tuple = new int[vars.length];
            for (int i = 0; i < tuple.length; i++) {
                tuple[i] = s.getVar(vars[i]).getVal();
            }
            int checkocc = 0;
            for (int i = 0; i < (tuple.length - 1); i++) {
                if (tuple[i] == val) checkocc++;
            }
            if (checkocc == tuple[tuple.length - 1]) {
                tuples.add(tuple);
            }
        } while (s.nextSolution() == Boolean.TRUE);

        IntegerVariable[] newvs = new IntegerVariable[vs.length + 1];
        System.arraycopy(vs,0,newvs,0,vs.length);
        newvs[vs.length] = occ;
        return feasTupleAC(Options.C_EXT_AC32, tuples, newvs);
    }

    @Test
    public void occCedric1() {
        CPModel m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable x = makeIntVar("x", new int[]{1, 3, 4});
        IntegerVariable y = makeIntVar("y", new int[]{2, 3});
        IntegerVariable z = makeIntVar("z", 0, 3);
        IntegerVariable[] tab = new IntegerVariable[]{x, y};
        m.addConstraint(occurrence(z, tab, 1));
        CPSolver s = new CPSolver();
        s.read(m);
        s.solveAll();
        assertEquals(6,s.getNbSolutions());

    }


    @Test
    public void testMateo() {
        int nbrun = 10;
        int tableVars = 5;
        int cubeVars = 7;
        for (int seed = 0; seed < nbrun; seed ++) {
            int nba = version1(seed, tableVars, cubeVars);
            int nbb = version2(seed, tableVars, cubeVars);
            int nbc = version3(seed, tableVars, cubeVars);
            assertEquals(nba, nbb);
            assertEquals(nbb, nbc);
            LOGGER.info("---------");
        }
    }

    public static int version1(int seed, int tableVars, int cubeVars) {
        Model model = new CPModel();
        model.setDefaultExpressionDecomposition(true);
        IntegerVariable table[] = new IntegerVariable[tableVars];
        IntegerVariable cube[] = new IntegerVariable[cubeVars];
        for (int i = 0; i < tableVars; i++) {
            table[i] = makeIntVar("table" + i, 0, cubeVars - 1);
        }
        for (int i = 0; i < cubeVars; i++) {
            cube[i] = makeIntVar("cube" + i, 0, 1);
        }
        model.addConstraint(allDifferent(Options.C_ALLDIFFERENT_BC, table));
        for (int t = 0; t < tableVars; t++) {
            for (int c = 0; c < cubeVars; c++) {
                model.addConstraint(implies(eq(table[t], c) , eq(cube[c], 1)));
            }
        }
        for (int c = 0; c < cubeVars; c++) {
            Constraint temp = eq(table[0], c);
            for (int i = 1; i < tableVars; i++) {
                temp = or(temp, eq(table[i],c));
            }
            model.addConstraint(implies(eq(cube[c], 1) , temp));
         }

        Solver solver = new CPSolver();
        solver.read(model);
        //System.out.println(solver.pretty());
        solver.setVarIntSelector(new RandomIntVarSelector(solver,seed));
        solver.setValIntSelector(new RandomIntValSelector(seed));
        solver.solveAll();
        LOGGER.info("Version1: " + seed + " number of solution : " + solver.getNbSolutions()+ " node: " + solver.getNodeCount() + " time: " + solver.getTimeCount());
        return solver.getNbSolutions();
        
    }

    public static int version2(int seed, int tableVars, int cubeVars) {
        Model model = new CPModel();
        model.setDefaultExpressionDecomposition(false);
        IntegerVariable table[] = new IntegerVariable[tableVars];
        IntegerVariable cube[] = new IntegerVariable[cubeVars];
        IntegerVariable one = makeIntVar("one", 1, 1);
        for (int i = 0; i < tableVars; i++) {
            table[i] = makeIntVar("table" + i, 0, cubeVars - 1);
        }
        for (int i = 0; i < cubeVars; i++) {
            cube[i] = makeIntVar("cube" + i, 0, 1);
        }
        model.addConstraint(allDifferent(Options.C_ALLDIFFERENT_BC, table));
        for (int t = 0; t < tableVars; t++) {
            for (int c = 0; c < cubeVars; c++) {
                model.addConstraint(implies(eq(table[t], c) , eq(cube[c], 1)));
            }
        }
        for (int c = 0; c < cubeVars; c++) {
            model.addConstraint(implies(eq(cube[c], 1) , occurrenceMin(one, table, c)));
        }
        Solver solver = new CPSolver();
        solver.read(model);
       // System.out.println(solver.pretty());
        solver.setVarIntSelector(new RandomIntVarSelector(solver,seed));
        solver.setValIntSelector(new RandomIntValSelector(seed));
        solver.solveAll();
        LOGGER.info("Version2: "+ seed + " number of solution : " + solver.getNbSolutions() + " node: " + solver.getNodeCount() + " time: " + solver.getTimeCount());
        return solver.getNbSolutions();
        
    }

    public static int version3(int seed, int tableVars, int cubeVars) {
        Model model = new CPModel();
        model.setDefaultExpressionDecomposition(false);
        IntegerVariable table[] = new IntegerVariable[tableVars];
        IntegerVariable cube[] = new IntegerVariable[cubeVars];
        IntegerVariable one = makeIntVar("one", 1, 1);
        IntegerVariable zero = makeIntVar("zero", 0, 0);
        IntegerVariable booleanIntermediate[][] = new IntegerVariable[cubeVars][tableVars];

        for (int i = 0; i < tableVars; i++) {
            table[i] = makeIntVar("table" + i, 0, cubeVars - 1);
        }
        for (int i = 0; i < cubeVars; i++) {
            cube[i] = makeIntVar("cube" + i, 0, 1);
        }
        for (int i = 0; i < cubeVars; i++) {
            for (int j = 0; j < tableVars; j++) {
                booleanIntermediate[i][j] = makeIntVar("slot:" + i + "_TableVar"
                        + j, 0, 1);
            }
        }
        model.addConstraint(allDifferent(Options.C_ALLDIFFERENT_BC, table));
        for (int i = 0; i < tableVars; i++) {
            for (int j = 0; j < cubeVars; j++) {
                model.addConstraint(boolChanneling(booleanIntermediate[j][i],
                        table[i], j));
            }
        }
        for (int i = 0; i < cubeVars; i++) {
            model.addConstraint(eq(cube[i],
                    sum(booleanIntermediate[i])));
        }
        Solver solver = new CPSolver();
        solver.read(model);
        solver.setVarIntSelector(new RandomIntVarSelector(solver,seed));
        solver.setValIntSelector(new RandomIntValSelector(seed));
        solver.solveAll();
        LOGGER.info("Version 3: "+ seed + " number of solution : " + solver.getNbSolutions() + " node: " + solver.getNodeCount() + " time: " + solver.getTimeCount());
        return solver.getNbSolutions();
    }

    public static Model makeModel(int n) {
        IntegerVariable[] serie;
        Model m = new CPModel();
        serie = makeIntVarArray("", n, 0, n - 1);
        for (int i = 0; i < n; i++) {
            m.addConstraint(occurrence(serie[i], serie, i));
        }
        return m;
    }

    @Test
    public void testMagicSquare() {
        for(int i = 4; i < 8; i++){
            Model m = makeModel(i);
            Solver s = new CPSolver();
            s.read(m);
            s.solve();
        }
    }


}
