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
package choco.model.constraints.reified;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.BranchingFactory;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import static choco.Choco.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/02/12
 */
public class ReifiedGlobalConstraint {

    IntegerVariable b = constant(1);
    Constraint c_true = TRUE;

    private void make(Constraint constraint, int loop, long seed) {
        Model m1 = new CPModel();
        Model m2 = new CPModel();
        m1.addConstraint(constraint);
        m2.addConstraint(ifThenElse(eq(b, 1), constraint, c_true));

        for (int i = 0; i < loop; i++) {

            Solver s1 = new CPSolver();
            Solver s2 = new CPSolver();

            s1.read(m1);
            s2.read(m2);

            s1.addGoal(BranchingFactory.randomIntSearch(s1, seed + i));
            s2.addGoal(BranchingFactory.randomIntSearch(s2, seed + i));

//            ChocoLogging.toSearch();
            s1.solveAll();
            s2.solveAll();

            System.out.printf("%d - %d\n", s1.getSolutionCount(), s2.getSolutionCount());
            Assert.assertEquals("wrong number of solutions", s1.getSolutionCount(), s2.getSolutionCount());
        }
    }

    @Test
    public void alldiffactest() {
        IntegerVariable[] vars = makeIntVarArray("v", 5, -2, 3);
        Constraint c = allDifferent(Options.C_ALLDIFFERENT_AC, vars);
        make(c, 20, 10);
    }

    @Test
    public void alldiffbctest() {
        IntegerVariable[] vars = makeIntVarArray("v", 5, -2, 3);
        Constraint c = allDifferent(Options.C_ALLDIFFERENT_BC, vars);
        make(c, 20, 10);
    }

    @Test
    public void sortingtest() {
        IntegerVariable[] vars = makeIntVarArray("v", 5, -2, 3);
        IntegerVariable[] wars = makeIntVarArray("w", 5, -2, 3);
        Constraint c = sorting(vars, wars);
        make(c, 20, 10);
    }

    @Test
    public void nth1test() {
        IntegerVariable var = makeIntVar("var", -2, 3);
        IntegerVariable val = makeIntVar("val", -2, 3);
        Constraint c = nth(var, new int[]{2, 0, -1, 4, 1, 3}, val, -2);
        make(c, 20, 10);
    }

    @Test
    public void nth2test() {
        IntegerVariable var = makeIntVar("var", -2, 3);
        IntegerVariable[] vars = makeIntVarArray("vars", 5, -2, 3);
        IntegerVariable val = makeIntVar("val", -2, 3);
        Constraint c = nth(var, vars, val, -2);
        make(c, 20, 10);
    }

    @Test
    public void nth3test() {
        IntegerVariable var1 = makeIntVar("var1", 0, 3);
        IntegerVariable var2 = makeIntVar("var2", 0, 3);
        IntegerVariable val = makeIntVar("val", -3, 3);
        Constraint c = nth(var1, var2, new int[][]{{0, 1, 2, 3}, {1, 2, 3, 0}, {2, 3, 0, 1}, {3, 0, 1, 2}}, val);
        make(c, 20, 10);
    }

    @Test
    public void amongtest() {
        IntegerVariable nvar = makeIntVar("v1", 1, 2);
        IntegerVariable[] vars = makeIntVarArray("var", 4, 0, 5);
        int[] values = new int[]{2, 3, 5};
        Constraint c = among(nvar, vars, values);
        make(c, 20, 10);
    }

    @Test
    public void atmostnvaluetest() {
        IntegerVariable v1 = makeIntVar("v1", 1, 2);
        IntegerVariable v2 = makeIntVar("v2", 2, 3);
        IntegerVariable v3 = makeIntVar("v3", 3, 4);
        IntegerVariable v4 = makeIntVar("v4", 3, 4);
        IntegerVariable n = makeIntVar("n", 2, 4);
        Constraint c = atMostNValue(n, new IntegerVariable[]{v1, v2, v3, v4});
        make(c, 20, 10);
    }

    @Test
    public void boolChanntest() {
        IntegerVariable bool = makeIntVar("bool", 0, 1);
        IntegerVariable x = makeIntVar("x", 0, 5);
        Constraint c = boolChanneling(bool, x, 4);
        make(c, 20, 10);
    }


    @Test
    public void distEQtest() {
        IntegerVariable v0 = makeIntVar("v0", 0, 5);
        IntegerVariable v1 = makeIntVar("v1", 0, 5);
        IntegerVariable v2 = makeIntVar("v2", 0, 5);
        Constraint c = distanceEQ(v0, v1, v2, 0);
        make(c, 20, 10);
    }

    @Test
    public void distGTtest() {
        IntegerVariable v0 = makeIntVar("v0", 0, 5);
        IntegerVariable v1 = makeIntVar("v1", 0, 5);
        IntegerVariable v2 = makeIntVar("v2", 0, 5);
        Constraint c = distanceGT(v0, v1, v2, 0);
        make(c, 20, 10);
    }

    @Test
    public void distLTtest() {
        IntegerVariable v0 = makeIntVar("v0", 0, 5);
        IntegerVariable v1 = makeIntVar("v1", 0, 5);
        IntegerVariable v2 = makeIntVar("v2", 0, 5);
        Constraint c = distanceLT(v0, v1, v2, 0);
        make(c, 20, 10);
    }

    @Test
    public void distNEQtest() {
        IntegerVariable v0 = makeIntVar("v0", 0, 5);
        IntegerVariable v1 = makeIntVar("v1", 0, 5);
        Constraint c = distanceNEQ(v0, v1, 0);
        make(c, 20, 10);
    }

    @Test
    public void domChanntest() {
        IntegerVariable x = makeIntVar("var", 0, 10);
        IntegerVariable[] b = makeBooleanVarArray("valueIndicator", 10);
        Constraint c = domainChanneling(x, b);
        make(c, 20, 10);
    }

    @Test
    public void gcc1atest() {
        int n = 5;
        IntegerVariable[] vars = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            vars[i] = makeIntVar("var_" + i, 1, n);
        }
        int[] LB2 = {0, 1, 1, 0, 3};
        int[] UB2 = {0, 1, 1, 0, 3};
        Constraint c = globalCardinality(Options.C_GCC_BC, vars, LB2, UB2, 1);
        make(c, 20, 10);
    }

    @Test
    public void gcc1btest() {
        int n = 5;
        IntegerVariable[] vars = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            vars[i] = makeIntVar("var_" + i, 1, n);
        }
        int[] LB2 = {0, 1, 1, 0, 3};
        int[] UB2 = {0, 1, 1, 0, 3};
        Constraint c = globalCardinality(Options.C_GCC_AC, vars, LB2, UB2, 1);
        make(c, 20, 10);
    }

    @Test
    public void gcc2test() {
        int n = 5;
        IntegerVariable[] vars = makeIntVarArray("vars", n, 1, n);
        IntegerVariable[] cards = makeIntVarArray("cards", n, 0, 1);
        Constraint c = globalCardinality(vars, cards, 1);
        make(c, 20, 10);
    }

    @Test
    public void gcc3test() {
        int[] values = {1, 3};
        int[] occmin = {1, 2};
        int[] occmax = {3, 2};
        IntegerVariable[] vars = makeIntVarArray("var", 4, 1, 4);
        Constraint c = globalCardinality(vars, values, occmin, occmax);
        make(c, 20, 10);
    }

    @Test
    public void gcc4test() {
        int[] values = {1, 3};
        IntegerVariable[] vars = makeIntVarArray("var", 4, 1, 4);
        IntegerVariable[] cards = makeIntVarArray("card", 2, 1, 3);
        Constraint c = globalCardinality(vars, values, cards);
        make(c, 20, 10);
    }

    @Test
    public void incNval1test() {
        IntegerVariable nval = makeIntVar("nval", 1, 3);
        IntegerVariable[] variables = makeIntVarArray("vars", 6, 1, 4);
        Constraint c = increasingNValue(Options.C_INCREASING_NVALUE_BOTH, nval, variables);
        make(c, 20, 10);
    }

    @Test
    public void incNval2test() {
        IntegerVariable nval = makeIntVar("nval", 1, 3);
        IntegerVariable[] variables = makeIntVarArray("vars", 6, 1, 4);
        Constraint c = increasingNValue(Options.C_INCREASING_NVALUE_ATLEAST, nval, variables);
        make(c, 20, 10);
    }

    @Test
    public void incNval3test() {
        IntegerVariable nval = makeIntVar("nval", 1, 3);
        IntegerVariable[] variables = makeIntVarArray("vars", 6, 1, 4);
        Constraint c = increasingNValue(Options.C_INCREASING_NVALUE_ATMOST, nval, variables);
        make(c, 20, 10);
    }

    @Test
    public void incSumtest() {
        IntegerVariable[] res = new IntegerVariable[3];
        res[0] = makeIntVar("x0", -2, 3);
        res[1] = makeIntVar("x1", -3, 3);
        res[2] = makeIntVar("x2", -3, 0);
        IntegerVariable sum = makeIntVar("s", -3, 3);
        Constraint c = increasingSum(res, sum);
        make(c, 20, 10);
    }

    @Test
    public void invChantest() {
        IntegerVariable[] X = makeIntVarArray("X", 5, 0, 6);
        IntegerVariable[] Y = makeIntVarArray("Y", 5, 0, 6);
        Constraint c = inverseChanneling(X, Y);
        make(c, 20, 10);
    }

    @Test
    public void invChanRangetest() {
        IntegerVariable[] X = makeIntVarArray("X", 3, 0, 5);
        IntegerVariable[] Y = makeIntVarArray("Y", 3, 0, 5);
        Constraint c = inverseChannelingWithinRange(X, Y);
        make(c, 20, 10);
    }

    @Test
    public void lextest() {
        IntegerVariable[] X = makeIntVarArray("X", 3, 0, 5);
        IntegerVariable[] Y = makeIntVarArray("Y", 4, 0, 5);
        Constraint c = lex(X, Y);
        make(c, 20, 10);
    }

    @Test
    public void lexChaintest() {
        IntegerVariable[] X = makeIntVarArray("X", 3, 0, 3);
        IntegerVariable[] Y = makeIntVarArray("Y", 3, 0, 3);
        IntegerVariable[] Z = makeIntVarArray("Y", 3, 0, 3);
        Constraint c = lexChain(X, Y, Z);
        make(c, 20, 10);
    }

    @Test
    public void lexChainEqtest() {
        IntegerVariable[] X = makeIntVarArray("X", 3, 0, 3);
        IntegerVariable[] Y = makeIntVarArray("Y", 3, 0, 3);
        IntegerVariable[] Z = makeIntVarArray("Y", 3, 0, 3);
        Constraint c = lexChainEq(X, Y, Z);
        make(c, 20, 10);
    }

    @Test
    public void lexEqtest() {
        IntegerVariable[] X = makeIntVarArray("X", 3, 0, 5);
        IntegerVariable[] Y = makeIntVarArray("Y", 3, 0, 5);
        Constraint c = lexEq(X, Y);
        make(c, 20, 10);
    }

    @Test
    public void leximin1test() {
        IntegerVariable[] X = makeIntVarArray("X", 3, 0, 5);
        IntegerVariable[] Y = makeIntVarArray("Y", 3, 0, 5);
        Constraint c = leximin(X, Y);
        make(c, 20, 10);
    }

    @Test
    public void leximin2test() {
        IntegerVariable[] X = makeIntVarArray("X", 3, 0, 5);
        Constraint c = leximin(new int[]{2, 1, 3}, X);
        make(c, 20, 10);
    }

    @Test
    public void max1test() {
        IntegerVariable[] vars = makeIntVarArray("X", 3, 1, 5);
        Constraint c = max(vars[0], vars[1], vars[2]);
        make(c, 20, 10);
    }

    @Test
    public void max2test() {
        IntegerVariable[] vars = makeIntVarArray("X", 3, 1, 5);
        IntegerVariable z = makeIntVar("z", 1, 5);
        Constraint c = max(vars, z);
        make(c, 20, 10);
    }

    @Test
    public void min1test() {
        IntegerVariable[] vars = makeIntVarArray("X", 3, 1, 5);
        Constraint c = min(vars[0], vars[1], vars[2]);
        make(c, 20, 10);
    }

    @Test
    public void min2test() {
        IntegerVariable[] vars = makeIntVarArray("X", 3, 1, 5);
        IntegerVariable z = makeIntVar("z", 1, 5);
        Constraint c = min(vars, z);
        make(c, 20, 10);
    }

    @Test
    public void modtest() {
        IntegerVariable[] vars = makeIntVarArray("X", 2, 1, 5);
        Constraint c = mod(vars[0], vars[1], 2);
        make(c, 20, 10);
    }

    @Test
    public void occtest() {
        int n = 4;
        IntegerVariable[] x = makeIntVarArray("X", n, 0, 10);
        IntegerVariable z = makeIntVar("Z", 0, 10);
        Constraint c = occurrence(z, x, 3);
        make(c, 20, 10);
    }

    @Test
    public void occmaxtest() {
        int n = 4;
        IntegerVariable[] x = makeIntVarArray("X", n, 0, 10);
        IntegerVariable z = makeIntVar("Z", 0, 10);
        Constraint c = occurrenceMax(z, x, 3);
        make(c, 20, 10);
    }

    @Test
    public void occmintest() {
        int n = 4;
        IntegerVariable[] x = makeIntVarArray("X", n, 0, 10);
        IntegerVariable z = makeIntVar("Z", 0, 10);
        Constraint c = occurrenceMin(z, x, 3);
        make(c, 20, 10);
    }

    @Test
    public void stretchPathtest() {
        int n = 7;
        IntegerVariable[] vars = makeIntVarArray("v", n, 0, 2);
        ArrayList<int[]> lgt = new ArrayList<int[]>();
        lgt.add(new int[]{0, 2, 2}); // stretches of value 0 are of length 2
        lgt.add(new int[]{1, 2, 3}); // stretches of value 1 are of length 2 or 3
        lgt.add(new int[]{2, 2, 2}); // stretches of value 2 are of length 2
        Constraint c = stretchPath(lgt, vars);
        make(c, 20, 10);
    }

    @Test
    public void timestest() {
        IntegerVariable[] vars = makeIntVarArray("X", 3, 0, 8);
        Constraint c = times(vars[0], vars[1], vars[2]);
        make(c, 20, 10);
    }

    // clause
    // costRegular
    // cumulative
    // disjunctive
    // extensions
    // geost
    // reified...
    // multicostregular
    // regular
    // tree


}
