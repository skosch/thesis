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
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.bool.sat.ClauseStore;
import choco.cp.solver.search.BranchingFactory;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.cnf.ALogicTree;
import choco.kernel.model.constraints.cnf.Literal;
import choco.kernel.model.constraints.cnf.Node;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.ISolutionMonitor;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.logging.Logger;

import static choco.Choco.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 16 févr. 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class ClausesTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();


    @Test
    public void test0() {
        int nbsol = computeNbSol();
        for (int seed = 0; seed < 20; seed++) {
            CPModel mod = new CPModel();
            CPSolver s = new CPSolver();
            IntegerVariable[] vars = makeBooleanVarArray("b", 10);
            mod.addVariables(vars);
            s.read(mod);
            IntDomainVar[] bvs = s.getVar(vars);
            ClauseStore store = new ClauseStore(s.getVar(vars), s.getEnvironment());
            store.addClause(new IntDomainVar[]{bvs[0], bvs[3], bvs[4]}, new IntDomainVar[]{bvs[1], bvs[2], bvs[7]});
            store.addClause(new IntDomainVar[]{bvs[5], bvs[3]}, new IntDomainVar[]{bvs[1], bvs[5], bvs[4]});
            store.addClause(new IntDomainVar[]{bvs[8]}, new IntDomainVar[]{bvs[4]});
            store.addClause(new IntDomainVar[]{bvs[9], bvs[4]}, new IntDomainVar[]{bvs[6], bvs[8]});
            s.post(store);
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.solveAll();
            assertEquals(nbsol, s.getNbSolutions());
        }
    }

    @Test
    public void test1() {
        int nbsol = computeNbSol();
        for (int seed = 0; seed < 20; seed++) {
            CPModel mod = new CPModel();
            CPSolver s = new CPSolver();
            IntegerVariable[] vars = makeBooleanVarArray("b", 10);
            mod.addConstraint(clause(new IntegerVariable[]{vars[0], vars[3], vars[4]}, new IntegerVariable[]{vars[1], vars[2], vars[7]}));
            mod.addConstraint(clause(new IntegerVariable[]{vars[5], vars[3]}, new IntegerVariable[]{vars[1], vars[4], vars[5]}));
            mod.addConstraint(clause(new IntegerVariable[]{vars[8]}, new IntegerVariable[]{vars[4]}));
            mod.addConstraint(clause(new IntegerVariable[]{vars[9], vars[4]}, new IntegerVariable[]{vars[6], vars[8]}));
            s.read(mod);

            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.solveAll();
            assertEquals(nbsol, s.getNbSolutions());
        }
    }

    @Test
    public void testRestartClauses() {
        //for (int seed = 0; seed < 20; seed++) {
        CPModel mod = new CPModel();
        CPSolver s = new CPSolver();
        IntegerVariable[] vars = makeBooleanVarArray("b", 3);
        mod.addConstraint(clause(new IntegerVariable[]{vars[0], vars[1], vars[2]}, new IntegerVariable[]{}));
        s.read(mod);
        s.setGeometricRestart(1, 1.1);
        s.setVarIntSelector(new StaticVarOrder(s, s.getVar(vars)));
        s.solve();
        LOGGER.info(s.getVar(vars[0]) + " " + s.getVar(vars[1]) + " " + s.getVar(vars[2]));
        s.addNogood(new IntDomainVar[]{s.getVar(vars[0]), s.getVar(vars[1])}, new IntDomainVar[]{});
        s.nextSolution();
        LOGGER.info(s.getVar(vars[0]) + " " + s.getVar(vars[1]) + " " + s.getVar(vars[2]));

        assertTrue(s.getVar(vars[0]).getVal() == 1 || s.getVar(vars[1]).getVal() == 1);
        //}
    }


    @Test
    public void test2() {
        int nbsol = computeNbSol2();
        for (int seed = 0; seed < 20; seed++) {
            CPModel mod = new CPModel();
            CPSolver s = new CPSolver();
            IntegerVariable[] vars = makeBooleanVarArray("b", 10);
            mod.addConstraint(clause(new IntegerVariable[]{vars[0], vars[0], vars[4]}, new IntegerVariable[]{vars[1], vars[2], vars[7]}));
            mod.addConstraint(clause(new IntegerVariable[]{vars[5], vars[3]}, new IntegerVariable[0]));
            mod.addConstraint(clause(new IntegerVariable[0], new IntegerVariable[]{vars[8]}));
            mod.addConstraint(clause(new IntegerVariable[]{vars[9], vars[6]}, new IntegerVariable[]{vars[4], vars[6], vars[4]}));
            mod.addConstraint(clause(new IntegerVariable[]{vars[4]}, new IntegerVariable[0]));

            s.read(mod);

            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.solveAll();
            LOGGER.info("" + s.getNbSolutions());
            assertEquals(nbsol, s.getNbSolutions());
        }
    }

    private int computeNbSol2() {
        CPModel mod = new CPModel();
        CPSolver s = new CPSolver();
        IntegerVariable[] v = makeBooleanVarArray("b", 10);
        mod.addVariables(v);
        mod.addConstraint(or(eq(v[0], 1), eq(v[0], 1), eq(v[4], 1), eq(v[1], 0), eq(v[2], 0), eq(v[7], 0)));
        mod.addConstraint(or(eq(v[5], 1), eq(v[3], 1)));
        mod.addConstraint(or(eq(v[8], 0)));
        mod.addConstraint(or(eq(v[4], 1)));
        mod.addConstraint(or(eq(v[9], 1), eq(v[6], 1), eq(v[4], 0), eq(v[6], 0), eq(v[4], 0)));
        s.read(mod);
        s.solveAll();
        return s.getNbSolutions();
    }

    private int computeNbSol() {
        CPModel mod = new CPModel();
        CPSolver s = new CPSolver();
        IntegerVariable[] v = makeBooleanVarArray("b", 10);
        mod.addVariables(v);
        mod.addConstraint(or(eq(v[0], 1), eq(v[3], 1), eq(v[4], 1), eq(v[1], 0), eq(v[2], 0), eq(v[7], 0)));
        mod.addConstraint(or(eq(v[5], 1), eq(v[3], 1), eq(v[1], 0), eq(v[5], 0), eq(v[4], 0)));
        mod.addConstraint(or(eq(v[8], 1), eq(v[4], 0)));
        mod.addConstraint(or(eq(v[9], 1), eq(v[4], 1), eq(v[6], 0), eq(v[8], 0)));
        s.read(mod);
        s.solveAll();
        return s.getNbSolutions();
    }

    @Test
    public void testRandomSatFormula() {
        for (int seed1 = 0; seed1 < 20; seed1++) {
            Random rand = new Random(seed1);
            int nbvar = rand.nextInt(200) + 30;
            int nbct = rand.nextInt(300) + 10;
            //int nbvar = 2000;//rand.nextInt(200) + 250;
            //int nbct = 15000;//rand.nextInt(2000) + 500;

            for (int seed2 = 0; seed2 < 5; seed2++) {
                LOGGER.info("seed " + seed1);
                int nbnode = solveNBSOL(seed2, nbvar, nbct, false, false);
                int nbnode2 = solveNBSOL(seed2, nbvar, nbct, false, true);
                int nbnode3 = solveNBSOL(seed2, nbvar, nbct, true, false);
                assertEquals(nbnode, nbnode2);
                assertEquals(nbnode, nbnode3);
            }
        }
    }

    public int solveNBSOL(int seed, int nbvar, int nbc, boolean clause, boolean decomp) {
        CPModel mod = new CPModel();
        mod.setDefaultExpressionDecomposition(decomp);
        CPSolver s = new CPSolver();

        Random rand = new Random(seed);
        IntegerVariable[] vars = makeBooleanVarArray("b", nbvar);
        mod.addVariables(vars);

        for (int i = 0; i < nbc; i++) {
            int poss1 = rand.nextInt(5) + 1;
            int neg1 = rand.nextInt(5);
            IntegerVariable[] poslit = new IntegerVariable[poss1];
            IntegerVariable[] neglit = new IntegerVariable[neg1];
            for (int j = 0; j < poslit.length; j++) {
                poslit[j] = vars[rand.nextInt(nbvar)];
            }
            for (int j = 0; j < neglit.length; j++) {
                neglit[j] = vars[rand.nextInt(nbvar)];
            }
            if (clause) {
                mod.addConstraint(clause(poslit, neglit));
            } else {
                int cpt = 0;
                Constraint[] largeor = new Constraint[poss1 + neg1];
                for (IntegerVariable aPoslit : poslit) {
                    largeor[cpt] = eq(aPoslit, 1);
                    cpt++;
                }
                for (IntegerVariable aNeglit : neglit) {
                    largeor[cpt] = eq(aNeglit, 0);
                    cpt++;
                }
                mod.addConstraint(or(largeor));
            }
        }
        s.read(mod);

        s.setVarIntSelector(new RandomIntVarSelector(s, seed));
        s.setValIntSelector(new RandomIntValSelector(seed));
        s.solve();
        LOGGER.info(seed + " : T= " + s.getTimeCount() + " N= " + s.getNodeCount());
        return s.getNodeCount();

    }

    @Test
    public void testBooleanIssue() {
        CPModel mod = new CPModel();
        CPSolver s = new CPSolver();
        IntegerVariable[] v = makeBooleanVarArray("b", 3);
        mod.addVariables(v); //bug without it !
        mod.addConstraint(or(eq(v[2], 1), eq(v[1], 1), eq(v[1], 1)));
        mod.addConstraint(or(eq(v[2], 1), eq(v[1], 0), eq(v[2], 0), eq(v[1], 0)));
        mod.addConstraint(or(eq(v[1], 1), eq(v[2], 1)));
        s.read(mod);
        s.solveAll();
        assertEquals(s.getNbSolutions(), 6);
    }


    @Test
    public void test3() {
        int nbsol = computeNbSol3();
        for (int seed = 0; seed < 1; seed++) {
            CPModel mod = new CPModel();
            CPSolver s = new CPSolver(/*new EnvironmentRecomputation()*/);
            s.setRecomputationGap(10);
            IntegerVariable[] vars = makeBooleanVarArray("b", 3);
            mod.addVariables(vars);
            s.read(mod);
            IntDomainVar[] bvs = s.getVar(vars);
            ClauseStore store = new ClauseStore(s.getVar(vars), s.getEnvironment());
            store.addClause(new IntDomainVar[]{bvs[0]}, new IntDomainVar[]{bvs[1], bvs[2]});
            s.post(store);
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.solveAll();
            assertEquals(nbsol, s.getNbSolutions());
        }
    }

    private int computeNbSol3() {
        CPModel mod = new CPModel();
        CPSolver s = new CPSolver(new EnvironmentTrailing());
        IntegerVariable[] v = makeBooleanVarArray("b", 3);
        mod.addVariables(v);
        mod.addConstraint(or(eq(v[0], 1), eq(v[1], 0), eq(v[2], 0)));
        s.read(mod);
        s.solveAll();
        return s.getNbSolutions();
    }

    @Test
    public void test4() {
        CPModel mod = new CPModel();
        CPSolver s = new CPSolver();
        IntegerVariable[] v = makeBooleanVarArray("b", 4);

        Literal a = Literal.pos(v[0]);
        Literal na = Literal.neg(a.flattenBoolVar()[0]);
        Literal b = Literal.pos(v[1]);
        Literal c = Literal.pos(v[2]);
        Literal d = Literal.pos(v[3]);

        ALogicTree root = Node.and(a, b, na, c, d);
        mod.addConstraints(clauses(root));

        s.read(mod);
        s.solveAll();
        assertEquals(s.getNbSolutions(), 0);
    }

    @Test
    public void test5() {
        int nSol = 1;
        for (int n = 1; n < 12; n++) {
            for (int i = 0; i <= n; i++) {

                CPModel mod = new CPModel();
                CPSolver s = new CPSolver();
                IntegerVariable[] bs = new IntegerVariable[n];
                Literal[] lits = new Literal[n];
                for (int j = 0; j < n; j++) {
                    bs[j] = Choco.makeBooleanVar("b" + j);
                    if (j < i) {
                        lits[j] = Literal.pos(bs[j]);
                    } else {
                        lits[j] = Literal.neg(bs[j]);
                    }
                }
                ALogicTree or = Node.or(lits);
                mod.addConstraints(clauses(or));
                s.read(mod);
                s.solveAll();

                long sol = s.getSolutionCount();
                Assert.assertEquals(sol, nSol);
            }
            nSol = nSol * 2 + 1;
        }
    }

    @Test
    public void testBothAnd() {
        CPModel mod = new CPModel();
        CPSolver s = new CPSolver();
        IntegerVariable[] bs = new IntegerVariable[1];
        bs[0] = Choco.makeBooleanVar("to be");

        ALogicTree and = Node.and(Literal.pos(bs[0]), Literal.neg(bs[0]));

        mod.addConstraints(clauses(and));
        s.read(mod);
        s.solveAll();

        long sol = s.getSolutionCount();
        Assert.assertEquals(sol, 0);
    }

    @Test
    public void testBothOr() {
        CPModel mod = new CPModel();
        CPSolver s = new CPSolver();
        IntegerVariable b = Choco.makeBooleanVar("to be");
        mod.addVariable(b);

        ALogicTree or = Node.or(Literal.pos(b), Literal.neg(b));

        mod.addConstraints(clauses(or));
        s.read(mod);
        s.solveAll();

        long sol = s.getSolutionCount();
        Assert.assertEquals(2, sol);
    }


    @Test
    public void testNogood1() {
        final CPModel mod = new CPModel();
        final CPSolver s = new CPSolver();
        final IntegerVariable[] vars = makeBooleanVarArray("b", 3);
        mod.addVariables(vars);
        mod.addConstraint(clause(new IntegerVariable[]{vars[0]}, new IntegerVariable[]{vars[1], vars[2]}));
        s.read(mod);
        final IntDomainVar[] bvs = s.getVar(vars);
        s.setSolutionMonitor(new ISolutionMonitor() {

            @Override
            public void recordSolution(Solver solver) {
                if (solver.getSolutionCount() == 1) {
                    //Suppress second solution
                    s.addNogood(new IntDomainVar[]{}, new IntDomainVar[]{bvs[0], bvs[1]});
                }
            }
        });
        s.clearGoals();
        s.addGoal(BranchingFactory.lexicographic(s, s.getVar(vars)));
        s.solveAll();
        assertEquals(5, s.getSolutionCount());
    }

    @Test
    public void testNogood2() {
        ChocoLogging.toSearch();
        final CPModel mod = new CPModel();
        final CPSolver s = new CPSolver();
        final IntegerVariable[] vars = makeBooleanVarArray("b", 3);
        mod.addVariables(vars);
        s.read(mod);
        final IntDomainVar[] bvs = s.getVar(vars);
//		s.addNogood(new IntDomainVar[]{},new IntDomainVar[]{bvs[1], bvs[2]});
        s.setSolutionMonitor(new ISolutionMonitor() {

            @Override
            public void recordSolution(Solver solver) {
                if (solver.getSolutionCount() == 1) {
                    //Suppress second solution
                    s.addNogood(new IntDomainVar[]{}, new IntDomainVar[]{bvs[1], bvs[2]});
                }
            }
        });
        s.clearGoals();
        s.addGoal(BranchingFactory.lexicographic(s, s.getVar(vars)));
        s.solveAll();
        assertEquals(6, s.getSolutionCount());
        assertEquals(11, s.getNodeCount());
        assertEquals(10, s.getBackTrackCount());
    }
}
