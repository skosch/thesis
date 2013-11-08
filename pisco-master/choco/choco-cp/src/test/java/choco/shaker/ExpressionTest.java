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
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntVar;
import choco.shaker.tools.factory.CPModelFactory;
import choco.shaker.tools.factory.VariableFactory;
import org.junit.*;

import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;

import static choco.Choco.*;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 11 mars 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*
* Shaker of integer expressions
*/
public class ExpressionTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    Model m;
    static Random random;
    int seed;

    @Before
    public void before(){
        m = new CPModel();
        random = new Random();
    }

    @After
    public void after(){
        m = null;
    }

    @Test
    @Ignore
    public void test1() {
        int i =0;
        while(i < 10000){
            seed = i++;
            LOGGER.info("seed:"+seed);
            try{
                mainTest(seed, 5, 10, 3, true, true);
            }catch (UnsupportedOperationException e){
                LOGGER.severe(seed+" - "+e);
            }catch (Exception e){
                Assert.fail("seed:"+seed+" => "+e);
            }
        }
    }

    @Test
    @Ignore
    public void test2() {
        int i =0;
        while(i < 10000){
            seed = i++;
            LOGGER.info("seed:"+seed);
            try{
                mainTest(seed, 5, 10, 3, true, false);
            }catch (UnsupportedOperationException e){
                LOGGER.severe(seed+" - "+e);
            }
        }
    }

     @Test
    @Ignore
    public void test3() {
        int i =0;
        while(i < 10000){
            seed = i++;
            LOGGER.info("seed:"+seed);
            try{
                mainTest(seed, 5, 10, 5, false, true);
            }catch (UnsupportedOperationException e){
                LOGGER.severe(seed+" - "+e);
            }
        }
    }

    @Test
    public void testExpression1() {
        m  = new CPModel();
        final IntegerVariable[] vars = new IntegerVariable[4];
        vars[0] = Choco.makeIntVar("v1", -2,3, Options.V_ENUM);
        vars[1] = Choco.makeIntVar("v2", 3,13, Options.V_ENUM);
        vars[2] = Choco.makeIntVar("v3", -5,-1, Options.V_ENUM);
        vars[3] = Choco.makeIntVar("v4", 4,8, Options.V_ENUM);

        m.addConstraint(
                leq(
                    neg(scalar(new int[]{3,8}, new IntegerVariable[]{vars[0], vars[1]})),
                    minus(vars[3],abs(vars[2]))
                )
        );

        checker();
    }

    @Test
    public void testExpression2() {
        m  = new CPModel();
        final IntegerVariable[] vars = new IntegerVariable[4];
        vars[0] = Choco.makeIntVar("v1", 6,7);
        vars[1] = Choco.makeIntVar("v2", 7,8);

        m.addConstraint(
                geq(
                    scalar(new int[]{2}, new IntegerVariable[]{vars[0]}),
                    max(new IntegerVariable[]{vars[1]})
                )
        );


        checker();
    }

    @Test
    public void testExpression3() {
        m  = new CPModel();
        final IntegerVariable[] vars = new IntegerVariable[3];
        vars[0] = Choco.makeBooleanVar("v1");
        vars[1] = Choco.makeIntVar("v2", -6,2);
        vars[2] = Choco.makeIntVar("v3", 0,4);

        m.addConstraint(implies(
                    sameSign(vars[2],vars[0]),
                    oppositeSign(vars[0],vars[1])
                )
        );


        checker();
    }

    @Test
    public void testExpression4() {
        m  = new CPModel();
        final IntegerVariable[] vars = new IntegerVariable[2];
        vars[0] = Choco.makeBooleanVar("v1");
        vars[1] = Choco.makeIntVar("v2", 3,7);

        m.addConstraint(implies(
                    sameSign(vars[1],vars[0]),
                    FALSE
                )
        );


        checker();
    }


    @Test
    public void testExpression5() {
        m  = new CPModel();
        final IntegerVariable[] vars = new IntegerVariable[3];
        vars[0] = Choco.makeIntVar("v1", 1,3);
        vars[1] = Choco.makeIntVar("v2", 2,2);
        vars[2] = Choco.makeIntVar("v3", 1,2);
        m.addConstraint(gt(
                    plus(ifThenElse(leq(vars[2],vars[0]),constant(2),vars[0]),
                            scalar(new int[]{7,7}, new IntegerVariable[]{vars[1], vars[2]})),
                    plus(vars[1],
                            min(max(vars[0], vars[1]),vars[1])
                    )
                )
        );


        checker();
    }

    @Test
    public void testExpression6() {
        m  = new CPModel();
        final IntegerVariable[] vars = new IntegerVariable[3];
        vars[0] = Choco.makeIntVar("v1", 0,2);
        vars[1] = Choco.makeBooleanVar("v2");

        m.addConstraint(geq(
                    vars[0],
                    max(new IntegerVariable[]{vars[1]})
                )
        );


        checker();
    }

    @Test
    public void testExpression7() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[4];
        v[0] = Choco.makeIntVar("v1", 6,9);
        v[1] = Choco.makeIntVar("v2", 3,8);
        v[2] = Choco.makeIntVar("v3", 8,9);
        v[3] = Choco.makeIntVar("v4", 5,9);


        m.addConstraint(
                not(
                        oppositeSign(
                                mult(
                                        plus(v[0],v[1]),
                                        mult(neg(v[2]), minus(4, v[3]))
                                ),
                                max(
                                        sum(v[1], v[2], constant(4)),
                                        max(
                                                sum(v[3],v[3],v[3],v[0]),
                                                v[2]
                                        )
                                )
                        )
                )
        );


        checker();
    }

    @Test
    public void testExpression8() {
        m  = new CPModel();
        final IntegerVariable[] vars = new IntegerVariable[5];
        vars[0] = Choco.makeIntVar("v1", -4,2);
        vars[1] = Choco.makeBooleanVar("v2");
        vars[2] = Choco.makeIntVar("v3", -2,0);
        vars[3] = Choco.makeIntVar("v4", -2,3);
        vars[4] = Choco.makeIntVar("v5", -1,0);

        m.addConstraint(oppositeSign(
                scalar(new int[]{7,1,4,2}, new IntegerVariable[]{vars[0], vars[1], vars[2], vars[3]}),
                scalar(new int[]{9,7,10}, new IntegerVariable[]{vars[4], vars[0], vars[1]})
                )
        );


        checker(false);
    }

    @Test
    public void testExpression9() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[2];
        v[0] = Choco.makeBooleanVar("v1");
        v[1] = Choco.makeIntVar("v2", -8,0);

        m.addConstraint(ifThenElse(
                geq(v[0], 3),
                TRUE,
                gt(v[1], v[1])
                )
        );


        checker();
    }

    @Test
    public void testExpression10() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[5];
        v[0] = Choco.makeBooleanVar("v1");
        v[1] = Choco.makeIntVar("v2", 2,4);
        v[2] = Choco.makeIntVar("v2", 1,1);
        v[3] = Choco.makeIntVar("v2", -9,0);
        v[4] = Choco.makeIntVar("v2", -1,2);

        m.addConstraint(neq(
                v[0],
                neg(
                        mod(
                                mod(v[1], v[2]),
                                mod(v[3], v[4])
                        )
                )
                )
        );
        checker();
    }

    @Test
    public void testExpression11() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[5];
        v[0] = Choco.makeIntVar("v1", -4,3);
        v[1] = Choco.makeIntVar("v2", 2,5);
        v[2] = Choco.makeIntVar("v3", 4,7);
        v[3] = Choco.makeIntVar("v4", 0,4);
        v[4] = Choco.makeIntVar("v5", 1,4);

        m.addConstraint(geq(
                v[4],
                mult(
                        scalar(new int[]{9,6,5}, new IntegerVariable[]{v[0], v[1], v[2]}),
                        max(new IntegerExpressionVariable[]{v[3], v[1], plus(v[3], v[1]),
                                ifThenElse(FALSE, v[1], v[0])})
                        )
                )

        );


        checker();
    }

    @Test
    public void testExpression12() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[4];
        v[0] = Choco.makeIntVar("v1", 0, 6);
        v[1] = Choco.makeIntVar("v2", 7, 7);
        v[2] = Choco.makeIntVar("v3", 2, 6);
        v[3] = Choco.makeIntVar("v4", -4, 3);

        m.addConstraint(ifThenElse(
                geq(v[0],v[1]),
                neq(v[2],v[1]),
                gt(v[3],v[1])
        )
        );
        checker();
    }



    @Test
    public void testExpression13() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[4];
        v[0] = Choco.makeIntVar("v1", 0, 1);
        v[1] = Choco.makeIntVar("v2", 0, 1);
        v[2] = Choco.makeIntVar("v3", 0, 1);
        v[3] = Choco.makeIntVar("v4", -1, 1);

        m.addConstraint(
                lt(
                        minus(
                                sum(v[0],v[1]),
                                max(new IntegerExpressionVariable[]{
                                        ifThenElse(
                                                eq(
                                                        v[2],
                                                        max(new IntegerExpressionVariable[]{sum(v[1],v[3], v[2])})
                                                ),
                                                v[1],
                                                v[3]
                                        )}
                                )
                        ),
                        scalar(new int[]{9,7}, new IntegerVariable[]{v[0], v[2]})
                )
        );
        checker();
    }

    @Test
    public void testExpression14() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[3];
        v[0] = Choco.makeIntVar("v1", -2,3);
        v[1] = Choco.makeIntVar("v2", 0,1);
        v[2] = Choco.makeIntVar("v2", 0,10);

        m.addConstraint(eq(
                v[2],
                mod(v[0],v[1])
        )
        );

        checker();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testExpression15() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[3];
        v[0] = Choco.makeIntVar("v1", -2,3);

        m.addConstraint(
                implies(allDifferent(v[0],v[0]),
                        TRUE
                )
        );

        checker();
    }

    @Test
    public void testExpression16() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[3];
        v[0] = Choco.makeIntVar("v1", -2,3);
        v[1] = Choco.makeIntVar("v2", -2,3);
        v[2] = Choco.makeIntVar("v3", -2,3);

        m.addConstraint(reifiedConstraint(v[0], and(eq(v[1],0), eq(v[2],0)), or(neq(v[1],0), neq(v[2],0))));

        checker();
    }

    @Test
    public void testExpression17() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[5];
        v[0] = Choco.makeIntVar("v1", 1,1, Options.V_BTREE);
        v[1] = Choco.makeIntVar("v2", 0,2, Options.V_BTREE);
        v[2] = Choco.makeIntVar("v3", 0,1);
        v[3] = Choco.makeIntVar("v4", -5,4, Options.V_BTREE);
        v[4] = Choco.makeIntVar("v5", -3,5, Options.V_BTREE);

        m.addConstraint(
                        neq(
                                minus(
                                        plus(
                                                min(new IntegerVariable[]{v[0], v[1], v[4]}),
                                                abs(v[3])
                                        ),
                                        neg(scalar(new int[]{10}, new IntegerVariable[]{v[4]}))
                                    ),
                                abs(scalar(new int[]{3,6,5},new IntegerVariable[]{v[4], v[3], v[2]}))
                        )
        );

        checker(true);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testExpression18() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[4];
        v[0] = Choco.makeIntVar("v_1", 1,6, Options.V_LINK);
        v[1] = Choco.makeIntVar("v_2", -5,3, Options.V_BLIST);
        v[2] = Choco.makeIntVar("v_3", -3,6, Options.V_BTREE);
        v[3] = Choco.makeIntVar("v_4", 3,9, Options.V_LINK);
        final IntegerVariable zero = constant(0);

        m.addConstraint(
                not(
                        gt(
                                plus(
                                        minus(
                                                mult(v[0], v[1]),
                                                max(new IntegerVariable[]{v[2], v[3], zero})
                                        ),
                                        mult(zero, mult(zero,v[1]))
                                ),
                                max(new IntegerVariable[]{v[2]})
                        )
                )
        );
        LOGGER.info(m.pretty());
        for(int i = 0; i < m.getNbIntVars(); i++){
            LOGGER.info(String.format("%s", m.getIntVar(i).getOptions()));
        }
        checker();

        final Random r = new Random(5140);
        final CPModelFactory mf = new CPModelFactory();

        mf.limits(5);
        mf.depth(3);
        mf.uses(VariableFactory.V.ENUMVAR, VariableFactory.V.BOUNDVAR,
                VariableFactory.V.BLISTVAR, VariableFactory.V.BTREEVAR,
                VariableFactory.V.LINKVAR, VariableFactory.V.BOOLVAR,
                VariableFactory.V.CST);

        mf.includesOperators();
        mf.includesMetaconstraints();
        m = mf.model(r);
        LOGGER.info(m.pretty());
        for(int i = 0; i < m.getNbIntVars(); i++){
            LOGGER.info(String.format("%s", m.getIntVar(i).getOptions()));
        }
        checker();
    }

    @Test
    public void testExpression19() {
        /**
         * test 2 seed 1104
         */
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[5];
        v[0] = Choco.makeIntVar("v1", 1,7, Options.V_BTREE);
        v[1] = Choco.makeIntVar("v2", 0,3);
        v[2] = Choco.makeIntVar("v3", 0,1);
        v[3] = Choco.makeIntVar("v4", -3,6);
        v[4] = Choco.makeIntVar("v5", -5,4, Options.V_BTREE);

        m.addConstraint(
                neq(
                        minus(
                                plus(
                                        min(new IntegerVariable[]{v[0], v[1], v[2], v[3]}),
                                        abs(v[4])
                                ),
                                neg(scalar(new int[]{10}, new IntegerVariable[]{v[3]}))
                        ),
                        abs(scalar(new int[]{3,6,5}, new IntegerVariable[]{v[3], v[4], v[2]}))
                )
        );
        checker();
    }

    @Test
    public void testExpression20() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[5];
        v[0] = Choco.makeIntVar("v1", -1,3);
        v[1] = Choco.makeIntVar("v2", 2,3);
        v[2] = Choco.makeIntVar("v3", 0,1);
        v[3] = Choco.makeIntVar("v4", 1,1);
        v[4] = Choco.makeIntVar("v5", 0,1);

        m.addConstraint(
//                ifThenElse(
//                        eq(v[0], v[1]),
                        reifiedConstraint(v[2], eq(v[3], v[4]), geq(v[0], v[2]))//,
//                        leq(v[0], v[4])
//                )
        );
        checker(true);
    }

    @Test
    public void testExpression20bis() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[5];
        v[0] = Choco.makeIntVar("v1", -1,3);
        v[1] = Choco.makeIntVar("v2", 2,3);
        v[2] = Choco.makeIntVar("v3", 0,1);
        v[3] = Choco.makeIntVar("v4", 1,1);
        v[4] = Choco.makeIntVar("v5", 0,1);

        m.addConstraint(
                reifiedConstraint(v[2], eq(v[3], v[4]), eq(v[3], v[4]))//,
        );
        checker(true);
    }

    @Test
    public void testExpression21() {
        /**
         * test 3 seed 275
         */
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[4];
        v[0] = Choco.makeIntVar("v1", 7,8, Options.V_ENUM);
        v[1] = Choco.makeIntVar("v2", -3,2, Options.V_ENUM);
        v[2] = Choco.makeIntVar("v3", 0,1);
        v[3] = Choco.makeIntVar("v4", 2,2, Options.V_ENUM);

        m.addConstraint(
                ifThenElse(
                        lt(v[0], v[1]),
                        TRUE,
                        reifiedConstraint(v[2], leq(1, v[3]), oppositeSign(v[3], v[3]))
                )
        );
        checker();
    }

    @Test
    public void testExpression22() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[5];
        v[0] = Choco.makeBooleanVar("v1");
        v[1] = Choco.makeIntVar("v2", -2,1, Options.V_BLIST);
        v[2] = Choco.makeIntVar("v3", 3,9, Options.V_BLIST);
        v[3] = Choco.makeIntVar("v4", -2,6, Options.V_BLIST);
        v[4] = Choco.makeBooleanVar("v5");

        m.addConstraint(
                ifThenElse(
                        neq(v[0], v[1]),
                        oppositeSign(v[2], v[3]),
                        reifiedConstraint(v[4], neq(v[0], v[1]), geq(v[2], v[0]))
                )
        );
        checker();
    }

    @Test
    public void testExpression23() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[5];
        v[0] = Choco.makeIntVar("v1", 0, 7);
        v[1] = Choco.makeBooleanVar("b2");

        m.addConstraint(
                oppositeSign(
                        abs(
                                scalar(
                                        new int[]{2},
                                        new IntegerVariable[]{v[0]}
                                )
                        ),
                        max(
                                new IntegerExpressionVariable[]{
                                        abs(
                                            neg(
                                                    v[1]
                                            )
                                    )
                                }
                        )
                )
        );
        checker();
    }

    @Test
    public void testExpression24() {
        m  = new CPModel();
        seed = 1104;
        final IntegerVariable[] v = new IntegerVariable[5];
        v[0] = Choco.makeIntVar("v1", 1, 7, Options.V_BTREE);
        v[1] = Choco.makeIntVar("v2", 0, 3, Options.V_ENUM);
        v[2] = Choco.makeBooleanVar("b3");
        v[3] = Choco.makeIntVar("v4", -3, 6, Options.V_BLIST);
        v[4] = Choco.makeIntVar("v1", -5, 4, Options.V_BTREE);

        m.addConstraint(
                neq(
                        minus(
                                plus(
                                        min(new IntegerVariable[]{v[0], v[1], v[2], v[3]}),
                                        abs(v[4])
                                ),
                                neg(
                                        scalar(
                                                new int[]{10}, new IntegerVariable[]{v[3]}
                                        )
                                )
                        ),
                        abs(
                                scalar(new int[]{3,6,5}, new IntegerVariable[]{v[3], v[4], v[2]})
                        )
                )
                
        );
        checker();
    }

    @Test
    public void testExpression25() {
        m  = new CPModel();
        final IntegerVariable[] v = new IntegerVariable[5];
        v[0] = Choco.makeIntVar("v1", -9, 0);
        v[1] = Choco.makeIntVar("v2", 0, 3);
        v[2] = Choco.makeIntVar("v3", -1, 5);
        v[3] = Choco.makeIntVar("v4", -4, 2);
        v[4] = Choco.makeBooleanVar("b5");

        m.addConstraint(
                or(
                        gt(
                                scalar(
                                        new int[]{3,9},
                                        new IntegerVariable[]{v[0], v[2]}
                                ),
                                sum(v[2], v[3])
                        ),
                        FALSE,
                        eq(
                                neg(
                                        abs(
                                                sum(v[4], v[4], v[4])
                                        )
                                ),
                                max(
                                        new IntegerExpressionVariable[]{sum(v[3], v[2])}
                                )
                        )
                )

        );
        checker();
    }

    private void buildModel(final int seed, final int bounds, final int dsize, final int depth, final boolean includesOp, final boolean includesMC){
        final Random r = new Random(seed);
        final CPModelFactory mf = new CPModelFactory();

        mf.limits(bounds);
        mf.depth(depth);
        mf.domain(dsize);
        mf.uses(VariableFactory.V.ENUMVAR, VariableFactory.V.BOUNDVAR,
                VariableFactory.V.BLISTVAR, VariableFactory.V.BTREEVAR,
                VariableFactory.V.LINKVAR, VariableFactory.V.BOOLVAR,
                VariableFactory.V.CST);

        if(includesOp)mf.includesOperators();
        if(includesMC)mf.includesMetaconstraints();
        m = mf.model(r);
    }


    private void mainTest(final int seed, final int bounds, final int dsize, final int depth, final boolean includesOp, final boolean includesMC){
        buildModel(seed, bounds, dsize, depth, includesOp, includesMC);
        checker();
    }

    private void checker() {
        checker(false);
    }

    private void checker(final boolean print) {
        final Solver decomposedSolver = new CPSolver();
        final Solver undecomposedSolver = new CPSolver();
        StringBuffer st;
        m.setDefaultExpressionDecomposition(true);
        decomposedSolver.read(m);
        decomposedSolver.setVarIntSelector(new RandomIntVarSelector(decomposedSolver, seed));
        decomposedSolver.setValIntSelector(new RandomIntValSelector(seed));


        m.setDefaultExpressionDecomposition(false);
        undecomposedSolver.read(m);
        undecomposedSolver.setVarIntSelector(new RandomIntVarSelector(undecomposedSolver, seed));
        undecomposedSolver.setValIntSelector(new RandomIntValSelector(seed));

        decomposedSolver.solve();
        if (decomposedSolver.isFeasible()) {

            do {
                Assert.assertEquals("decomposedSolver.isSatisfied()",Boolean.TRUE, decomposedSolver.checkSolution());
                if(print){
                    st = new StringBuffer();
                    final Iterator<IntegerVariable> it = m.getIntVarIterator();
                    while(it.hasNext()){
                        final IntVar v = decomposedSolver.getVar(it.next());
                        st.append(v.getName()).append(':').append(v.getVal()).append(' ');
                    }
                    LOGGER.info(st.toString());
                }
            } while (decomposedSolver.nextSolution());
        }

        if(print){
            LOGGER.info("=========");
        }

        undecomposedSolver.solve();
        if (undecomposedSolver.isFeasible()) {
            do {
                if(print){
                    Assert.assertEquals("undecomposedSolver.isSatisfied()",Boolean.TRUE, undecomposedSolver.checkSolution());
                    st = new StringBuffer();
                    final Iterator<IntegerVariable> it = m.getIntVarIterator();
                    while(it.hasNext()){
                        final IntVar v = undecomposedSolver.getVar(it.next());
                        st.append(v.getName()).append(':').append(v.getVal()).append(' ');
                    }
                    LOGGER.info(st.toString());
                    }
            } while (undecomposedSolver.nextSolution());
        }
        Assert.assertEquals("Not same number of solutions", decomposedSolver.getNbSolutions(), undecomposedSolver.getNbSolutions());
    }


}
