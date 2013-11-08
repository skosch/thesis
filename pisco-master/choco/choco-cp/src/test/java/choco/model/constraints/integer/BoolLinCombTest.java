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
import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.IntLinComb;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.Propagator;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 31 oct. 2006
 * Time: 15:07:46
 */
public class BoolLinCombTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    CPModel m;
    CPSolver s;

    @Before
    public void before(){
        m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        s = new CPSolver();
    }

    @Test
    public void test0() {
        IntegerVariable[] vars = makeIntVarArray("x", 2, 0, 1);
        m.addConstraint(leq(sum(new IntegerVariable[]{vars[0],vars[1]}),1));
        m.addConstraint(eq(vars[1],1));
        s.read(m);
        try {
            s.propagate();
        } catch (ContradictionException e) {
            assertTrue(false);
        }
    }

    @Test
    public void test0bis() {
        IntegerVariable[] vars = makeIntVarArray("x", 2, 0, 1);
        m.addConstraint(eq(sum(new IntegerVariable[]{vars[0],vars[1]}),1));
        m.addConstraint(eq(vars[1],1));
        s.read(m);
        try {
            s.propagate();
        } catch (ContradictionException e) {
            assertTrue(false);
        }
    }

    @Test
    public void test00() {
        IntegerVariable[] vars = makeIntVarArray("x", 2, 0, 1);
		IntegerVariable sum = makeIntVar("s", 0, 5);
        Constraint prop = leq(scalar(new int[]{3,1}, new IntegerVariable[]{vars[0],vars[1]}),sum);
		m.addConstraint(prop);
        s.read(m);
        try {
            s.propagate();
            s.getVar(vars[0]).setVal(1);
	        //((Propagator) s.getCstr(prop)).constAwake(false);
            DisposableIterator<SConstraint> iter = s.getConstraintIterator();
          for(; iter.hasNext(); ) {
            ((Propagator)iter.next()).constAwake(false);
          }
            iter.dispose();
          s.propagate();
        } catch (ContradictionException e) {
            assertTrue(false);
        }
    }

    @Test
    public void test01() {
        IntegerVariable[] vars = makeIntVarArray("x", 2, 0, 1);
		IntegerVariable sum = makeIntVar("s", 0, 5);
        Constraint prop = leq(scalar(new int[]{3,1}, new IntegerVariable[]{vars[0],vars[1]}),sum);
        try {
	        m.addConstraint(prop);
            s.read(m);
            s.getVar(vars[0]).setVal(1);
	        s.propagate();
        } catch (ContradictionException e) {
            assertTrue(false);
        }
    }

    @Test
    public void test1() {
        LOGGER.info("Test EQ  **************************** ");
        for (int i = 1; i < 16; i++) {
            testBothLinCombVer(i, IntLinComb.EQ, 100);
        }
    }

    @Test
    public void test2() {
        LOGGER.info("Test GT  **************************** ");
        for (int i = 1; i < 10; i++) {
            testBothLinCombVer(i, IntLinComb.GEQ, 101);
        }
    }

    @Test
    public void test3() {
        LOGGER.info("Test LEQ  **************************** ");
        for (int i = 1; i < 13; i++) {
            testBothLinCombVer(i, IntLinComb.LEQ, 102);
        }
    }

	@Test
	public void test1S() {
	    LOGGER.info("Test EQ  **************************** ");
	    for (int i = 1; i < 16; i++) {
	        testBothSumCombVer(i, IntLinComb.EQ, 100);
	    }
	}

	@Test
	public void test2S() {
	    LOGGER.info("Test GT  **************************** ");
	    for (int i = 1; i < 10; i++) {
	        testBothSumCombVer(i, IntLinComb.GEQ, 101);
	    }
	}

	@Test
	public void test3S() {
	    LOGGER.info("Test LEQ  **************************** ");
	    for (int i = 1; i < 13; i++) {
	        testBothSumCombVer(i, IntLinComb.LEQ, 102);
	    }
	}


    @Test
    public void testBMC_IBM(){
        int n = 3;
        IntegerVariable[] bools = Choco.makeBooleanVarArray("b", n);
        int[] coeffs = new int[n];
        Arrays.fill(coeffs, -1);
        coeffs[0] = 1;
        Model m = new CPModel();
        Constraint c = Choco.neq(Choco.scalar(coeffs, bools), 1);
        m.addConstraint(c);

        Solver s = new CPSolver();
        s.read(m);
        try{
            s.propagate();
            for(int i = 1; i < n; i++){
                s.getVar(bools[i]).instantiate(0, null, true);
                s.propagate();
            }
        }catch (ContradictionException ex){
            Assert.fail();
        }
        try{
            s.getVar(bools[0]).instantiate(1, null, true);
            s.propagate();
            Assert.fail("There should be an exception!");
        }catch (ContradictionException ignE){}

    }


    private void testBothLinCombVer(int n, int op, int seed) {
        testLinComb(n, op, false, seed);
        int nbSol1 = nbSol;
        int nbNodes1 = nbNodes;
        testLinComb(n, op, true, seed);
        assertEquals("n:"+n+" seed:"+seed,nbSol1, nbSol);
        assertEquals("n:"+n+" seed:"+seed,nbNodes1, nbNodes);
    }

	private void testBothSumCombVer(int n, int op, int seed) {
	    testSumComb(n, op, false, seed);
	    int nbSol1 = nbSol;
	    int nbNodes1 = nbNodes;
	    testSumComb(n, op, true, seed);
	    assertEquals("n:"+n+" seed:"+seed, nbSol1, nbSol);
	    assertEquals("n:"+n+" seed:"+seed, nbNodes1, nbNodes);
	}


    public int nbSol;
    public int nbNodes;

    private void testLinComb(int n, int op, boolean optimized, int seed) {
	    m = new CPModel();
	    s = new CPSolver();
	    IntegerVariable[] vars = new IntegerVariable[n + 1];
        for (int i = 0; i < n; i++) {
            vars[i] = makeIntVar("v" + i, 0, 1);
        }
        vars[n] = makeIntVar("b", -n - 1, n + 1);
        m.addVariables(Options.V_BOUND,vars);
        m.addVariables(vars);

        int[] randCoefs = getRandomPackingPb(n + 1, 100, seed + 1);
        Random rand = new Random(seed + 2);
        int k = rand.nextInt(2 * n + 5) - n - 5;
        if (optimized) {
            if (op == IntLinComb.GEQ) {
				m.addConstraint(Options.E_DECOMP, geq(scalar(vars, randCoefs), k));
			} else if (op == IntLinComb.EQ) {
                m.addConstraint(Options.E_DECOMP, eq(scalar(vars, randCoefs), k));
            } else if (op == IntLinComb.LEQ) {
                m.addConstraint(Options.E_DECOMP, leq(scalar(vars, randCoefs), k));
            }
          s.read(m);
        } else {
          s.read(m);
            if (op == IntLinComb.GEQ) {
				s.post(makeIntLinComb(s.getVar(vars),randCoefs,-k,IntLinComb.GEQ));
			} else if (op == IntLinComb.EQ) {
                s.post(makeIntLinComb(s.getVar(vars),randCoefs,-k,IntLinComb.EQ));
            } else if (op == IntLinComb.LEQ) {
                ArrayUtils.inverseSign(randCoefs);
                s.post(makeIntLinComb(s.getVar(vars),randCoefs,k,IntLinComb.GEQ));
            }
        }
        s.setVarIntSelector(new RandomIntVarSelector(s, s.getVar(vars), seed + 3));
        s.setValIntSelector(new RandomIntValSelector(seed + 4));
        s.solveAll();
        nbNodes = s.getNodeCount();
        nbSol = s.getNbSolutions();
        LOGGER.info("n:" + n + " op:" + op + " ver:" + optimized + " nbSol " + nbSol + " nbNode " + nbNodes + " tps " + s.getTimeCount());
    }

	private void testSumComb(int n, int op, boolean optimized, int seed) {
		  m = new CPModel();
		  s = new CPSolver();
		  IntegerVariable[] vars = new IntegerVariable[n];
	      for (int i = 0; i < n; i++) {
	          vars[i] = makeIntVar("v" + i, 0, 1);
	      }
	      m.addVariables(vars);
	      Random rand = new Random(seed + 2);
	      int k = rand.nextInt(n);
	      if (optimized) {
	          if (op == IntLinComb.GEQ) {
				m.addConstraint(Options.E_DECOMP, geq(sum(vars), k));
			} else if (op == IntLinComb.EQ) {
	              m.addConstraint(Options.E_DECOMP, eq(sum(vars), k));
	          } else if (op == IntLinComb.LEQ) {
	              m.addConstraint(Options.E_DECOMP, leq(sum(vars), k));
	          }
          s.read(m);
        } else {
          s.read(m);
          int[] sumcoef = new int[n];
		      for (int i = 0; i < sumcoef.length; i++) {
			      sumcoef[i] = 1;
		      }
	          if (op == IntLinComb.GEQ) {
				s.post(makeIntLinComb(s.getVar(vars),sumcoef,-k,IntLinComb.GEQ));
			} else if (op == IntLinComb.EQ) {
	              s.post(makeIntLinComb(s.getVar(vars),sumcoef,-k,IntLinComb.EQ));
	          } else if (op == IntLinComb.LEQ) {
	              s.post(makeIntLinComb(s.getVar(vars),sumcoef,-k,IntLinComb.LEQ));
	          }
	      }
	      s.setVarIntSelector(new RandomIntVarSelector(s, s.getVar(vars), seed + 3));
	      s.setValIntSelector(new RandomIntValSelector(seed + 4));
	      s.solveAll();
	      nbNodes = s.getSearchStrategy().getNodeCount();
	      nbSol = s.getNbSolutions();
	      LOGGER.info("n:" + n + " op:" + op + " ver:" + optimized + " nbSol " + nbSol + " nbNode " + nbNodes + " tps " + s.getSearchStrategy().getTimeCount());
	  }



    private static int[] getRandomPackingPb(int nbObj, int capa, int seed) {
        Random rand = new Random(seed);
        int[] instance = new int[nbObj];
        for (int i = 0; i < nbObj; i++) {
            int val = rand.nextInt(2 * capa) - capa;
            instance[i] = val;
        }
        return instance;
    }

    /**
     * Copy of api to state the old non optimized linear constraint for comparison
     */
    private static SConstraint makeIntLinComb(IntVar[] lvars, int[] lcoeffs, int c, int linOperator) {
        int nbNonNullCoeffs = countNonNullCoeffs(lcoeffs);
        int nbPositiveCoeffs = 0;
        int[] sortedCoeffs = new int[nbNonNullCoeffs];
        IntVar[] sortedVars = new IntVar[nbNonNullCoeffs];

        int j = 0;
        // fill it up with the coefficients and variables in the right order
        for (int i = 0; i < lvars.length; i++) {
            if (lcoeffs[i] > 0) {
                sortedVars[j] = lvars[i];
                sortedCoeffs[j] = lcoeffs[i];
                j++;
            }
        }
        nbPositiveCoeffs = j;

        for (int i = 0; i < lvars.length; i++) {
            if (lcoeffs[i] < 0) {
                sortedVars[j] = lvars[i];
                sortedCoeffs[j] = lcoeffs[i];
                j++;
            }
        }
        IntDomainVar[] tmpVars = new IntDomainVar[sortedVars.length];
        System.arraycopy(sortedVars, 0, tmpVars, 0, sortedVars.length);

        return new IntLinComb(tmpVars, sortedCoeffs, nbPositiveCoeffs, c, linOperator);
    }

    private static int countNonNullCoeffs(int[] lcoeffs) {
        int nbNonNull = 0;
        for (int i = 0; i < lcoeffs.length; i++) {
            if (lcoeffs[i] != 0) {
				nbNonNull++;
			}
        }
        return nbNonNull;
    }
}
