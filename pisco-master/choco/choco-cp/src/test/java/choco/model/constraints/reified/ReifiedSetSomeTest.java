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

package choco.model.constraints.reified;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import org.junit.Assert;
import org.junit.Test;

import static choco.Choco.*;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 17 mai 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public class ReifiedSetSomeTest {

    @Test
    public void test_jussien1() {
        int NV = 2;

        Model m = new CPModel();
        Solver s = new CPSolver();

        SetVariable[] vars = new SetVariable[NV];
        for (int i = 0; i < NV; i++) {
            vars[i] = Choco.makeSetVar("v", 0, 2);
        }
        IntegerVariable bool = Choco.makeBooleanVar("b");

        m.addConstraint(Choco.reifiedConstraint(bool, eq(vars[0], vars[1]), neq(vars[0], vars[1])));

        s.read(m);
//        ChocoLogging.toSolution();
        s.solveAll();
    }

    @Test
    public void test_jussien2() {
        int NV = 2;

        Model m = new CPModel();
        Solver s = new CPSolver();

        SetVariable[] vars = new SetVariable[NV];
        for (int i = 0; i < NV; i++) {
            vars[i] = Choco.makeSetVar("v", 0, 2);
        }
        IntegerVariable bool = Choco.makeBooleanVar("b");

        m.addConstraint(Choco.reifiedConstraint(bool, isIncluded(vars[0], vars[1]), isNotIncluded(vars[0], vars[1])));

        s.read(m);
//        ChocoLogging.toSolution();
        s.solveAll();
    }

    @Test
    public void test_jussien3() {
        int NV = 3;

        Model m = new CPModel();
        Solver s = new CPSolver();

        SetVariable[] vars = new SetVariable[NV];
        for (int i = 0; i < NV; i++) {
            vars[i] = Choco.makeSetVar("v", 0, 2);
        }

        m.addConstraint(Choco.implies(eq(vars[0], vars[1]), neq(vars[1], vars[2])));

        s.read(m);
//        ChocoLogging.toSolution();
        s.solveAll();
    }

    @Test
    public void test_jussien3ref() {
        int NV = 3;

        Model m = new CPModel();
        Solver s = new CPSolver();

        SetVariable[] vars = new SetVariable[NV];
        for (int i = 0; i < NV; i++) {
            vars[i] = Choco.makeSetVar("v", 0, 2);
        }
        IntegerVariable[] bool = Choco.makeBooleanVarArray("b", NV);

//        m.addConstraint(Choco.implies(eq(vars[0], vars[1]), neq(vars[1], vars[2])));
        m.addConstraint(reifiedConstraint(bool[0], eq(vars[0], vars[1]), neq(vars[0], vars[1])));
        m.addConstraint(reifiedConstraint(bool[1], neq(vars[1], vars[2]), eq(vars[1], vars[2])));
        m.addConstraint(implies(eq(bool[0], 1), eq(bool[1], 1)));


        s.read(m);
//        ChocoLogging.toSolution();
        s.solveAll();
    }

    @Test
    public void test4() {
        CPModel model = new CPModel();
        SetVariable set = Choco.makeSetVar("set", 0, 1);
        model.addConstraint(
                Choco.implies(Choco.member(0, set), Choco.member(1, set))
        );
        CPSolver solver = new CPSolver();
        solver.read(model);
    }

    @Test
    public void test5() {
        Model m = new CPModel();
        Solver s = new CPSolver();
        SetVariable a = Choco.makeSetVar("0_R0", 0, 255, "cp:enum");
        m.addVariable(a);
        m.addConstraint(Choco.or(Choco.member(1, a), Choco.member(0, a)));
        s.read(m);
        s.solve();
    }

    @Test
    public void test6() {
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable b = Choco.makeBooleanVar("b");
        IntegerVariable v = Choco.makeIntVar("v", 0, 3);
        IntegerVariable w = Choco.makeIntVar("w", 0, 3);

        m.addConstraint(reifiedConstraint(b, and(eq(v, 1), eq(w, 2))));
        s.read(m);
        s.solve();
    }

    @Test
    public void test7() {
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable b = Choco.makeBooleanVar("b");
        IntegerVariable v = Choco.makeIntVar("v", 0, 3);
        IntegerVariable w = Choco.makeIntVar("w", 0, 3);

        m.addConstraint(reifiedConstraint(b, and(eq(v, 1), eq(w, 2)), nand(eq(v, 1), eq(w, 2))));
        s.read(m);
        s.solve();
    }

    @Test
    public void test8() {
        Model model = new CPModel();
        model.setDefaultExpressionDecomposition(true);
        SetVariable Z = Choco.makeSetVar("Z", 1, 6);
        Z.setCard(Choco.makeIntVar("cardZ", 0, 6));
        SetVariable X = Choco.makeSetVar("X", 1, 3);
        X.setCard(Choco.makeIntVar("cardX", 0, 3));

        IntegerVariable v1 = Choco.makeIntVar("v1", 0, 1);
        model.addConstraint(Choco.or(Choco.eq(v1, 1), Choco.isIncluded(X, Z)));
        Solver s = new CPSolver();
        s.read(model);
        s.solve();
    }

    @Test
    public void test9() {
        Model model = new CPModel();
        Solver solver = new CPSolver();

        SetVariable mySetVar = Choco.makeSetVar("mySetVar", 0, 2);
        model.addVariable(mySetVar);
        IntegerVariable[] vars = new IntegerVariable[5];
        for (int i = 0; i < 5; i++) {
            vars[i] = Choco.makeIntVar("v_" + i, 0, 2);
        }
        model.addVariables(vars);
        Constraint c1 = Choco.leqCard(mySetVar, 2);
        Constraint c3 = Choco.eq(0, vars[1]);
        Constraint c4 = Choco.eq(0, vars[0]);
        Constraint c5 = Choco.and(c3, c4);
        model.addConstraint(Choco.implies(c1, c5));

        solver.read(model);
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
    }

    @Test
    public void test10() {
        Model model = new CPModel();
        Solver solver = new CPSolver();

        SetVariable mySetVar = Choco.makeSetVar("mySetVar", 0, 2);
        model.addVariable(mySetVar);
        IntegerVariable[] vars = new IntegerVariable[5];
        for (int i = 0; i < 5; i++) {
            vars[i] = Choco.makeIntVar("v_" + i, 0, 2);
        }
        model.addVariables(vars);
        Constraint c1 = Choco.leqCard(mySetVar, 2);
        Constraint c3 = Choco.eq(0, vars[1]);
        Constraint c4 = Choco.eq(0, vars[0]);
        Constraint c7 = Choco.implies(c1, c3);
        Constraint c8 = Choco.implies(c1, c4);

        model.addConstraint(Choco.and(c7, c8));

        solver.read(model);
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
    }

    @Test
    public void test11() {
        Model model = new CPModel();
        Solver solver = new CPSolver();

        SetVariable mySetVar = Choco.makeSetVar("mySetVar", 0, 2);
        model.addVariable(mySetVar);
        IntegerVariable[] vars = new IntegerVariable[5];
        for (int i = 0; i < 5; i++) {
            vars[i] = Choco.makeIntVar("v_" + i, 0, 2);
        }
        model.addVariables(vars);
        Constraint c1 = Choco.leqCard(mySetVar, 2);
        Constraint c3 = Choco.eq(0, vars[1]);
        Constraint c4 = Choco.eq(0, vars[0]);
        Constraint c7 = Choco.implies(c1, c3);
        Constraint c8 = Choco.implies(c1, c4);

        model.addConstraint(c7);
        model.addConstraint(c8);

        solver.read(model);
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
    }

    @Test
    public void test12() {
        Model model = new CPModel();
        Solver solver = new CPSolver();

        SetVariable mySetVar = Choco.makeSetVar("mySetVar", 0, 2);
        model.addVariable(mySetVar);
        IntegerVariable[] vars = new IntegerVariable[5];
        for (int i = 0; i < 5; i++) {
            vars[i] = Choco.makeIntVar("v_" + i, 0, 2);
        }
        model.addVariables(vars);
        Constraint c1 = Choco.leqCard(mySetVar, 2);
//        Constraint notC1 = Choco.not(c1);
//        Constraint c2 = Choco.notMember(0, mySetVar);
        Constraint c3 = Choco.eq(0, vars[1]);
        Constraint c4 = Choco.eq(0, vars[0]);
        Constraint c6 = Choco.or(c3, c4);

        /**
         * Constraint: c1 => c6
         *
         * Modelization 1
         */
        model.addConstraint(Choco.implies(c1, c6));

        solver.read(model);
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
    }

    @Test
    public void test13() {
        Model model = new CPModel();
        Solver solver = new CPSolver();

        SetVariable mySetVar = Choco.makeSetVar("mySetVar", 0, 2);
        model.addVariable(mySetVar);
        IntegerVariable[] vars = new IntegerVariable[5];
        for (int i = 0; i < 5; i++) {
            vars[i] = Choco.makeIntVar("v_" + i, 0, 2);
        }
        model.addVariables(vars);
        Constraint c1 = Choco.leqCard(mySetVar, 2);
        Constraint notC1 = Choco.not(c1);
        Constraint c3 = Choco.eq(0, vars[1]);
        Constraint c4 = Choco.eq(0, vars[0]);
        Constraint c6 = Choco.or(c3, c4);

        model.addConstraint(Choco.or(notC1, c6));

        solver.read(model);
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
    }

    @Test
    public void test14() {
        Model model = new CPModel();
        Solver solver = new CPSolver();

        SetVariable mySetVar = Choco.makeSetVar("mySetVar", 0, 2);
        model.addVariable(mySetVar);
        IntegerVariable[] vars = new IntegerVariable[5];
        for (int i = 0; i < 5; i++) {
            vars[i] = Choco.makeIntVar("v_" + i, 0, 2);
        }
        model.addVariables(vars);
        Constraint c1 = Choco.leqCard(mySetVar, 2);
        Constraint notC1 = Choco.not(c1);
        Constraint c3 = Choco.eq(0, vars[1]);
        Constraint c4 = Choco.eq(0, vars[0]);

        /**
         * Constraint: c1 => c6
         *
         * Moselization 3
         */
        model.addConstraint(Choco.or(notC1, c3, c4));

        solver.read(model);
        try {
            solver.propagate();
        } catch (ContradictionException e) {

            Assert.fail();
        }
    }
}
