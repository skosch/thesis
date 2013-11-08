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

package choco.shaker;

import choco.Choco;
import static choco.Choco.max;
import static choco.Choco.min;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.propagation.Propagator;
import choco.shaker.tools.search.SearchLoopWithEntailment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 11 mars 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class EntailmentTest {

    private CPModel m;
    private Constraint c;
    private IntegerVariable[] variables;

    private static final int MIN = 0;
    private static final int MAX = 1;

    @Before
    public void before(){
        m = new CPModel();
        c = null;
    }

    @After
    public void after(){
        m = null;
        c = null;
        variables = null;
    }


    @Test
    public void minTest(){
        test(MIN);
        test(MAX);
    }

    /**
     * Call the model creation and solver resolution.
     * @param cst
     */
    private void test(int cst){
        Random random = new Random();
        int[] sizes = new int[]{3, 7/*, 20*/};
        int[] domSizes = new int[]{4, 8/*, 100*/};
        StringBuffer message;
        for(int k = 0; k <  sizes.length; k++){
            int n = sizes[k];
            int domSize = domSizes[k];
            for(int rr = 0; rr < 10;rr++ ){
                int seed = random.nextInt();
                message = new StringBuffer();
                message.append("[n:").append(n);
                message.append("seed:").append(seed).append("]");
                model(cst, seed, domSize, n, message);
                solve(seed, message);
            }
        }
    }

    /**
     * Build the correct model
     * @param cst
     * @param seed
     * @param domSize
     * @param n
     * @param message
     */
    private void model(int cst, int seed, int domSize, int n, StringBuffer message) {
        switch (cst){
            case MIN :
                modelMin(seed, domSize, n, message);
                break;
            case MAX :
                modelMax(seed, domSize, n, message);
                break;
            default:
                Assert.fail("unknow constraint");
        }
    }

    /**
     * Build model for min constraint
     * @param seed
     * @param domSize
     * @param n
     * @param message
     */
    private void modelMin(int seed, int domSize, int n, StringBuffer message){
        m = new CPModel();
        Random r = new Random(seed);
        IntegerVariable[] vars = new IntegerVariable[n];
        int upp = r.nextInt(domSize);
        int low = upp - r.nextInt(domSize);
        IntegerVariable mmm = Choco.makeIntVar("mmm", low, upp);
        message.append(mmm.pretty()).append(" ");
        for(int i = 0; i < n; i++){
            upp = r.nextInt(domSize);
            low = upp - r.nextInt(domSize);
            vars[i] = Choco.makeIntVar("v_"+i, low, upp);
            message.append(vars[i].pretty()).append(" ");
        }
        variables = ArrayUtils.append(vars, new IntegerVariable[]{mmm});
        c = min(vars, mmm);
        m.addConstraint(c);
    }

    /**
     * Build model for max constraint
     * @param seed
     * @param domSize
     * @param n
     * @param message
     */
    private void modelMax(int seed, int domSize, int n, StringBuffer message){
        m = new CPModel();
        Random r = new Random(seed);
        IntegerVariable[] vars = new IntegerVariable[n];
        int upp = r.nextInt(domSize);
        int low = upp - r.nextInt(domSize);
        IntegerVariable mmm = Choco.makeIntVar("mmm", low, upp);
        message.append(mmm.pretty()).append(" ");
        for(int i = 0; i < n; i++){
            upp = r.nextInt(domSize);
            low = upp - r.nextInt(domSize);
            vars[i] = Choco.makeIntVar("v_"+i, low, upp);
            message.append(vars[i].pretty()).append(" ");
        }
        variables = ArrayUtils.append(vars, new IntegerVariable[]{mmm});
        c = max(vars, mmm);
        m.addConstraint(c);
    }

    /**
     * Solve the model in 2 ways:
     * a checker solver to search all solution
     * a solver that look for an entailment and search for all solution without propagation
     * @param seed
     * @param message
     */
    private void solve(int seed, StringBuffer message) {
        Solver checker = new CPSolver();
        checker.read(m);
        checker.solveAll();

        Solver s = new CPSolver();
        s.read(m);

        Propagator p = (Propagator) s.getCstr(c);
        try {
            s.propagate();
        } catch (ContradictionException e) {

        }
        Boolean entail = p.isEntailed();
        if (entail != Boolean.FALSE) {
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.setFirstSolution(true);
            s.generateSearchStrategy();
            SearchLoopWithEntailment slft = new SearchLoopWithEntailment(s.getSearchStrategy(), p);
            s.getSearchStrategy().setSearchLoop(slft);
            s.launch();
            if (s.isFeasible()) {
                do {
                    Assert.assertEquals(message.toString(), slft.entail, s.getCstr(c).isSatisfied());
                } while (s.nextSolution());
            }
            Assert.assertEquals("number of solution", checker.getNbSolutions(), s.getNbSolutions());
        } else {
            try {
                for (int i = 0; i < variables.length; i++) {
                    s.getVar(variables[i]).setVal(s.getVar(variables[i]).getRandomDomainValue());
                }
            } catch (ContradictionException e) {
                Assert.fail(message.toString() + "unexpected contradiction!!");
            }
            Assert.assertEquals(message.toString(), entail, s.getCstr(c).isSatisfied());
            Assert.assertEquals("number of solution", checker.getNbSolutions(), 0);
        }
    }


}
