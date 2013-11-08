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

package samples.documentation;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.Solver;

import java.util.ArrayList;

import static choco.Choco.*;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 16 oct. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*
* See Code4Doc1.java for more informations.
*/
public class Code4Doc3 {

    public void cgeq1(){
        //totex cgeq1
        Model m = new CPModel();
        Solver s = new CPSolver();
        int c = 1;
        IntegerVariable v = makeIntVar("v", 0, 2);
        m.addConstraint(geq(v, c));
        s.read(m);
        s.solve();
        //totex
    }

    public void cgeq2(){
        //totex cgeq2
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v1 = makeIntVar("v1", 0, 2);
        IntegerVariable v2 = makeIntVar("v2", 0, 2);
        IntegerExpressionVariable w1 = plus(v1, 1);
        IntegerExpressionVariable w2 = minus(v2, 1);
        m.addConstraint(geq(w1, w2));
        s.read(m);
        s.solve();
        //totex
    }

    public void cgeqcard(){
        //totex cgeqcard
        Model m = new CPModel();
        Solver s = new CPSolver();
        SetVariable set = makeSetVar("s", 1, 5);
        IntegerVariable i = makeIntVar("card", 2, 3);
        m.addConstraint(member(set, 3));
        m.addConstraint(geqCard(set, i));
        s.read(m);
        s.solve();
        //totex
    }

    public void cglobalcardinality1(){
        //totex cglobalcardinality1
        int n = 5;
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable[] vars = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
           vars[i] = makeIntVar("var " + i, 1, n);
        }
        int[] LB2 = {0, 1, 1, 0, 3};
        int[] UB2 = {0, 1, 1, 0, 3};
        m.addConstraint(Options.C_GCC_BC, globalCardinality(vars, LB2, UB2, 1));
        s.read(m);
        s.solve();
        //totex
    }

    public void cglobalcardinality2(){
        //totex cglobalcardinality2
        int n = 5;
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable[] vars = makeIntVarArray("vars", n, 1, n);
        IntegerVariable[] cards = makeIntVarArray("cards", n, 0, 1);


        m.addConstraint(Options.C_GCC_BC, globalCardinality(vars, cards, 1));
        s.read(m);
        s.solve();
        //totex
    }

    public void cglobalcardinality3(){
        //totex cglobalcardinality3
        int[] values = {1,3};
        int[] occmin = {1, 2};
        int[] occmax = {3, 2};

        Model m = new CPModel();
        IntegerVariable[] vars = makeIntVarArray("var", 7, 1, 4);
        m.addConstraint(globalCardinality(vars, values, occmin, occmax));

        Solver s = new CPSolver();
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cglobalcardinality4(){
        //totex cglobalcardinality4
        int[] values = {1,3};

        Model m = new CPModel();
        IntegerVariable[] vars = makeIntVarArray("var", 7, 1, 4);
        IntegerVariable[] cards = makeIntVarArray("card", 2, 1, 3);

        m.addConstraint(globalCardinality(vars, values, cards));
        m.addConstraint(member(cards[0], new int[]{1, 2, 3}));
        m.addConstraint(eq(cards[1], 2));

        Solver s = new CPSolver();
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cgt(){
        //totex cgt
        Model m = new CPModel();
        Solver s = new CPSolver();
        int c = 1;
        IntegerVariable v = makeIntVar("v", 0, 2);
        m.addConstraint(gt(v, c));
        s.read(m);
        s.solve();
        //totex
    }

    public void cifonlyif(){
        //totex cifonlyif
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable x = makeIntVar("x", 1, 3);
        IntegerVariable y = makeIntVar("y", 1, 3);
        IntegerVariable z = makeIntVar("z", 1, 3);
        m.addVariables(Options.V_BOUND,x ,y, z);
        m.addConstraint(ifOnlyIf(lt(x, y), lt(y, z)));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cifthenelse(){
        //totex cifthenelse
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable x = makeIntVar("x", 1, 3);
        IntegerVariable y = makeIntVar("y", 1, 3);
        IntegerVariable z = makeIntVar("z", 1, 3);
        // use API ifThenElse(Constraint, Constraint, Constraint)
        m.addConstraint(ifThenElse(lt((x), (y)), gt((y), (z)), FALSE));
         // and ifThenElse(Constraint, IntegerExpressionVariable, IntegerExpressionVariable)
        m.addConstraint(leq(z, ifThenElse(lt(x, y), constant(1), plus(x,y))));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cimplies(){
        //totex cimplies
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable x = makeIntVar("x", 1, 2);
        IntegerVariable y = makeIntVar("y", 1, 2);
        IntegerVariable z = makeIntVar("z", 1, 2);
        m.addVariables(Options.V_BOUND,x ,y, z);
        Constraint e1 = implies(leq(x, y), leq(x, z));
        m.addConstraint(e1);
        s.read(m);
        s.solveAll();
        //totex
    }

    public static void main(String[] args) {
//        ChocoLogging.setVerbosity(Verbosity.SEARCH);
        new Code4Doc3().cknapsack();
    }

    public void cincreasingnvalue(){
        //totex cincreasingnvalue
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable nval = makeIntVar("nval", 1, 3);
        IntegerVariable[] variables = makeIntVarArray("vars", 6, 1, 4);
        m.addConstraint(increasing_nvalue(Options.C_INCREASING_NVALUE_BOTH, nval, variables));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cincreasingsum(){
        //totex cincreasingsum
        Model m = new CPModel();
        IntegerVariable[] res = new IntegerVariable[3];
        res[0] = makeIntVar("x0", -2, 3);
        res[1] = makeIntVar("x1", -3, 3);
        res[2] = makeIntVar("x2", -3, 0);
        IntegerVariable sum = makeIntVar("s", -3, 3);
        m.addConstraint(Choco.increasingSum(res, sum));
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        //totex
    }

    public void cinfeaspairac(){
        //totex cinfeaspairac
        Model m = new CPModel();
        Solver s = new CPSolver();
        boolean[][] matrice2 = new boolean[][]{
                      {false, true, true, false},
                      {true, false, false, false},
                      {false, false, true, false},
                      {false, true, false, false}};
        IntegerVariable v1 = makeIntVar("v1", 1, 4);
        IntegerVariable v2 = makeIntVar("v2", 1, 4);
        m.addConstraint(feasPairAC(Options.C_EXT_AC32,v1, v2, matrice2));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cinfeastupleac(){
        //totex cinfeastupleac
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable x = makeIntVar("x", 1, 5);
        IntegerVariable y = makeIntVar("y", 1, 5);
        IntegerVariable z = makeIntVar("z", 1, 5);
        ArrayList<int[]> forbiddenTuples = new ArrayList<int[]>();
        forbiddenTuples.add(new int[]{1, 1, 1});
        forbiddenTuples.add(new int[]{2, 2, 2});
        forbiddenTuples.add(new int[]{2, 5, 3});
        m.addConstraint(infeasTupleAC(forbiddenTuples, x, y, z));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cinfeastuplefc(){
        //totex cinfeastuplefc
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable x = makeIntVar("x", 1, 5);
        IntegerVariable y = makeIntVar("y", 1, 5);
        IntegerVariable z = makeIntVar("z", 1, 5);
        ArrayList<int[]> forbiddenTuples = new ArrayList<int[]>();
        forbiddenTuples.add(new int[]{1, 1, 1});
        forbiddenTuples.add(new int[]{2, 2, 2});
        forbiddenTuples.add(new int[]{2, 5, 3});
        m.addConstraint(infeasTupleFC(forbiddenTuples, x, y, z));
        s.read(m);
        s.solveAll();        
        //totex
    }

    public void cintdiv(){
        //totex cintdiv
        Model m = new CPModel();
        Solver s = new CPSolver();
        long seed = 0;
        IntegerVariable x = makeIntVar("x", 3, 5);
        IntegerVariable y = makeIntVar("y", 1, 2);
        IntegerVariable z = makeIntVar("z", 0, 5);
        m.addConstraint(intDiv(x, y, z));
        s.setVarIntSelector(new RandomIntVarSelector(s, seed));
        s.setValIntSelector(new RandomIntValSelector(seed + 1));
        s.read(m);
        s.solve();
        //totex
    }

    public void cinversechanneling(){
        //totex cinversechanneling
        int n = 8;
        Model m = new CPModel();
        IntegerVariable[] queenInCol = new IntegerVariable[n];
        IntegerVariable[] queenInRow = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            queenInCol[i] = makeIntVar("QC" + i, 1, n);
            queenInRow[i] = makeIntVar("QR" + i, 1, n);
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
               int k = j - i;
                m.addConstraint(neq(queenInCol[i], queenInCol[j]));           // row
                m.addConstraint(neq(queenInCol[i], plus(queenInCol[j], k)));  // diagonal 1
                m.addConstraint(neq(queenInCol[i], minus(queenInCol[j], k))); // diagonal 2
                m.addConstraint(neq(queenInRow[i], queenInRow[j]));           // column
                m.addConstraint(neq(queenInRow[i], plus(queenInRow[j], k)));  // diagonal 2
                m.addConstraint(neq(queenInRow[i], minus(queenInRow[j], k))); // diagonal 1
            }
        }
        m.addConstraint(inverseChanneling(queenInCol, queenInRow));
        Solver s = new CPSolver();
        s.read(m);
        s.solveAll();
        //totex 
    }

    public void cinversechannelingwithinrange(){
        //totex cinversechannelingwithinrange
        Model m = new CPModel();
        IntegerVariable[] X = Choco.makeIntVarArray("X", 3, 0, 9, Options.V_ENUM);
        IntegerVariable[] Y = Choco.makeIntVarArray("Y", 4, 0, 9, Options.V_ENUM);

        m.addConstraint(Choco.eq(X[0], 9));
        m.addConstraint(Choco.eq(Y[0], 9));
        m.addConstraint(Choco.eq(Y[2], 9));
        m.addConstraint(inverseChannelingWithinRange(X, Y));
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        //totex
    }

    public void cinverseset(){
        //totex cinverseset
        int i = 4;
        int j = 2;
        Model m = new CPModel();
        IntegerVariable[] iv = makeIntVarArray("iv", i, 0, j);
        SetVariable[] sv = makeSetVarArray("sv", j, 0, i);

        m.addConstraint(inverseSet(iv, sv));
        Solver s = new CPSolver();
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cisincluded(){
        //totex cisincluded
        Model m = new CPModel();
        Solver s = new CPSolver();
        SetVariable v1 = makeSetVar("v1", 3, 4);
        SetVariable v2 = makeSetVar("v2", 3, 8);
        m.addConstraint(isIncluded(v1, v2));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cisnotincluded(){
        //totex cisnotincluded
        Model m = new CPModel();
        Solver s = new CPSolver();
        SetVariable v1 = makeSetVar("v1", 3, 4);
        SetVariable v2 = makeSetVar("v2", 3, 8);
        m.addConstraint(isNotIncluded(v1, v2));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cknapsack() {
        //totex cknapsack
        CPModel m = new CPModel();
        IntegerVariable[] items = new IntegerVariable[3];
        items[0] = makeIntVar("item_1", 0, 5);
        items[1] = makeIntVar("item_2", 0, 7);
        items[2] = makeIntVar("item_3", 0, 10);

        IntegerVariable sumWeight = makeIntVar("sumWeight", 0, 40, Options.V_BOUND);
        IntegerVariable sumValue = makeIntVar("sumValue", 0, 34, Options.V_OBJECTIVE);

        int[] weights = new int[]{7, 5, 3};
        int[] values = new int[]{6, 4, 2};

        Constraint knapsack = Choco.knapsackProblem(items, sumWeight, sumValue, weights, values);
        m.addConstraint(knapsack);

        Solver s = new CPSolver();
        s.read(m);
        s.maximize(true);
        //totex
    }


    public void cleq(){
        //totex cleq
        Model m = new CPModel();
        Solver s = new CPSolver();
        int c = 1;
        IntegerVariable v = makeIntVar("v", 0, 2);
        m.addConstraint(leq(v, c));
        IntegerVariable v1 = makeIntVar("v1", 0, 2);
        IntegerVariable v2 = makeIntVar("v2", 0, 2);
        IntegerExpressionVariable w1 = plus(v1, 1);
        IntegerExpressionVariable w2 = minus(v2, 1);
        m.addConstraint(leq(w1, w2));
        s.read(m);
        s.solve();
        //totex
    }

    public void cleqcard(){
        //totex cleqcard
        Model m = new CPModel();
        Solver s = new CPSolver();
        SetVariable set = makeSetVar("s", 1, 5);
        IntegerVariable i = makeIntVar("card", 2, 3);
        m.addConstraint(member(set, 3));
        m.addConstraint(leqCard(set, i));
        s.read(m);
        s.solve();
        //totex
    }

    public void clex(){
        //totex clex
        Model m = new CPModel();
        Solver s = new CPSolver();
        int n = 4;
        int k = 2;
        IntegerVariable[] vs1 = new IntegerVariable[n];
        IntegerVariable[] vs2 = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
           vs1[i] = makeIntVar("" + i, 0, k);
           vs2[i] = makeIntVar("" + i, 0, k);
        }
        m.addConstraint(lex(vs1, vs2));
        s.read(m);
        s.solve();        
        //totex
    }

    public void clexchain(){
        //totex clexchain
        Model m = new CPModel();
        Solver s = new CPSolver();
        int n = 4;
        int k = 2;
        IntegerVariable[] vs1 = new IntegerVariable[n];
        IntegerVariable[] vs2 = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
           vs1[i] = makeIntVar("" + i, 0, k);
           vs2[i] = makeIntVar("" + i, 0, k);
        }
        m.addConstraint(lexChain(vs1, vs2));
        s.read(m);
        s.solve();
        //totex
    }

    public void clexchaineq(){
        //totex clexchaineq
        Model m = new CPModel();
        Solver s = new CPSolver();
        int n = 4;
        int k = 2;
        IntegerVariable[] vs1 = new IntegerVariable[n];
        IntegerVariable[] vs2 = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
           vs1[i] = makeIntVar("" + i, 0, k);
           vs2[i] = makeIntVar("" + i, 0, k);
        }
        m.addConstraint(lexChainEq(vs1, vs2));
        s.read(m);
        s.solve();
        //totex
    }
}
