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
import choco.cp.solver.constraints.set.MemberXiY;
import choco.cp.solver.search.BranchingFactory;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import gnu.trove.TIntHashSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static choco.Choco.*;

/**
 * User : cprudhom
 * Mail : cprudhom(a)emn.fr
 * Date : 22 fevr. 2010
 * Since : Choco 2.1.1
 */
public class MemberTest {


    private static int[] buildValues(Random r, int low, int up) {
        int nb = 1 + r.nextInt(up - low + 1);
        TIntHashSet set = new TIntHashSet(nb);
        for (int i = 0; i < nb; i++) {
            set.add(low + r.nextInt(up - low + 1));
        }
        int[] values = set.toArray();
        Arrays.sort(values);
        return values;
    }

    @Test
    public void test1() {
        Random r;
        for (int i = 0; i < 200; i++) {
            r = new Random(i);
            int low = r.nextInt(20);
            int up = low + r.nextInt(10) + 1;
            int[] values = buildValues(r, low, up);

            IntegerVariable v = makeIntVar("v", low, up);

            Model m1 = new CPModel();
            Constraint among = member(v, values);

            m1.addConstraint(among);

            Solver s = new CPSolver();
            s.read(m1);

            s.setVarIntSelector(new RandomIntVarSelector(s, i));
            s.setValIntSelector(new RandomIntValSelector(i));

            s.solveAll();
            Assert.assertEquals("seed:" + i, values.length, s.getSolutionCount());
            if (values.length == 0) {
                Assert.assertEquals("seed:" + i, 0, s.getNodeCount());
            } else if (values.length == 1) {
                Assert.assertEquals("seed:" + i, 1, s.getNodeCount());
            } else {
                Assert.assertEquals("seed:" + i, values.length + 1, s.getNodeCount());
            }
        }
    }

    @Test
    public void test2() {
        Random r;
        for (int i = 0; i < 200; i++) {
            r = new Random(i);
            int low = r.nextInt(20);
            int up = low + r.nextInt(10) + 1;
            int[] values = buildValues(r, low, up);

            IntegerVariable v = makeIntVar("v", low, up);

            Model m1 = new CPModel();
            Constraint among = member(v, values);

            m1.addConstraint(among);

            Solver s = new CPSolver();
            s.read(m1);

            try {
                s.propagate();
                Assert.assertEquals("seed:" + i, values.length, s.getVar(v).getDomainSize());
            } catch (ContradictionException e) {
                Assert.assertEquals("seed:" + i, 0, values.length);
            }
        }
    }

    @Test
    public void test3() {
        Random r;
        for (int i = 0; i < 200; i++) {
            r = new Random(i);
            int low = r.nextInt(20);
            int up = low + r.nextInt(10) + 1;
            int[] values = buildValues(r, low, up);

            IntegerVariable v = makeIntVar("v", low, up);

            Model m1 = new CPModel();
            Constraint among = member(v, values);
            m1.addConstraint(among);

            Model m2 = new CPModel();
            IntegerVariable[] bools = new IntegerVariable[values.length];
            for (int j = 0; j < values.length; j++) {
                bools[j] = makeBooleanVar("b" + j);
                m2.addConstraint(reifiedConstraint(bools[j], eq(v, values[j])));
            }
            m2.addConstraint(eq(sum(bools), 1));

            Solver s1 = new CPSolver();
            s1.read(m1);
            s1.setVarIntSelector(new RandomIntVarSelector(s1, i));
            s1.setValIntSelector(new RandomIntValSelector(i));

            Solver s2 = new CPSolver();
            s2.read(m2);
            s2.setVarIntSelector(new RandomIntVarSelector(s2, i));
            s2.setValIntSelector(new RandomIntValSelector(i));

            s1.solveAll();
            s2.solveAll();

            Assert.assertEquals("seed:" + i, s2.getSolutionCount(), s1.getSolutionCount());
        }
    }

    @Test
    public void test4() {
        Random r;
        for (int i = 0; i < 200; i++) {
            r = new Random(i);
            int low = r.nextInt(20);
            int up = low + r.nextInt(10) + 1;

            IntegerVariable v = makeIntVar("v", low, up);

            int lower, upper;
            lower = low + r.nextInt(up - low + 1);
            upper = lower + r.nextInt(up - low + 1);


            Model m1 = new CPModel();
            Constraint among = member(v, lower, upper);

            m1.addConstraint(among);

            Solver s = new CPSolver();
            s.read(m1);

            s.setVarIntSelector(new RandomIntVarSelector(s, i));
            s.setValIntSelector(new RandomIntValSelector(i));

            s.solveAll();

            int min = Math.max(low, lower);
            int max = Math.min(up, upper);

            Assert.assertEquals("seed:" + i, (max - min) + 1, s.getSolutionCount());
            if (max - min == 0) {
                Assert.assertEquals("seed:" + i, 1, s.getNodeCount());
            } else {
                Assert.assertEquals("seed:" + i, s.getSolutionCount() + 1, s.getNodeCount());
            }
        }
    }

    @Test
    public void test5() {
        Random r;
        for (int i = 0; i < 1000; i++) {
            r = new Random(i);
            int low = r.nextInt(20);
            int up = low + r.nextInt(10) + 1;

            IntegerVariable v = makeIntVar("v", low, up);

            int lower, upper;
            lower = low + r.nextInt(up - low + 1);
            upper = lower + r.nextInt(up - low + 1);


            Model m1 = new CPModel();
            Constraint among = member(v, lower, upper);

            m1.addConstraint(among);

            Solver s = new CPSolver();
            s.read(m1);


            int min = Math.max(low, lower);
            int max = Math.min(up, upper);

            try {
                s.propagate();
                Assert.assertEquals("seed:" + i, (max- min)+1, s.getVar(v).getDomainSize());
            } catch (ContradictionException e) {
                Assert.assertEquals("seed:" + i, 0, (max- min)+1);
            }
        }
    }

    @Test
    public void test6() {
        Random r;
        for (int i = 0; i < 200; i++) {
            r = new Random(i);
            int low = r.nextInt(20);
            int up = low + r.nextInt(10) + 1;

            IntegerVariable v = makeIntVar("v", low, up);

            int lower, upper;
            lower = low + r.nextInt(up - low + 1);
            upper = lower + r.nextInt(up - low + 1);


            Model m1 = new CPModel();
            Constraint among = member(v, lower, upper);

            m1.addConstraint(among);

            Model m2 = new CPModel();
            IntegerVariable[] bools = new IntegerVariable[upper - lower +1];
            for (int j = 0; j < bools.length; j++) {
                bools[j] = makeBooleanVar("b" + j);
                m2.addConstraint(reifiedConstraint(bools[j], eq(v, lower + j)));
            }
            m2.addConstraint(eq(sum(bools), 1));


            Solver s1 = new CPSolver();
            s1.read(m1);
            s1.setVarIntSelector(new RandomIntVarSelector(s1, i));
            s1.setValIntSelector(new RandomIntValSelector(i));

            Solver s2 = new CPSolver();
            s2.read(m2);
            s2.setVarIntSelector(new RandomIntVarSelector(s2, i));
            s2.setValIntSelector(new RandomIntValSelector(i));

            s1.solveAll();
            s2.solveAll();

            Assert.assertEquals("seed:" + i, s2.getSolutionCount(), s1.getSolutionCount());
        }
    }

    @Test
    public void test7(){
        Random r = new Random();
        int nb = 3;
        int base = 4;
        for (int i = 0; i < 50; i++) {
            r.setSeed(i);
            int lb = -base + r.nextInt(2*base);
            int ub = lb + 1 + r.nextInt(2*base);
            IntegerVariable[] ivars = Choco.makeIntVarArray("x", nb, lb, ub);
            SetVariable svar = Choco.makeSetVar("s", lb, ub);

            Model model1 = new CPModel();
            Model model2 = new CPModel();

            for(int j = 0; j < ivars.length; j++){
                model1.addConstraint(Choco.member(ivars[j], svar));
            }

            model2.addConstraint(MemberXiY.build(ivars, svar));

//            ChocoLogging.toSolution();
            Solver solver1 = new CPSolver();
            Solver solver2 = new CPSolver();

            solver1.read(model1);
            solver1.addGoal(BranchingFactory.lexicographic(solver1, solver1.getVar(ivars)));
            solver1.addGoal(BranchingFactory.lexicographic(solver1, solver1.getVar(new SetVariable[]{svar})));
            solver1.solveAll();

            solver2.read(model2);
            solver2.addGoal(BranchingFactory.lexicographic(solver2, solver2.getVar(ivars)));
            solver2.addGoal(BranchingFactory.lexicographic(solver2, solver2.getVar(new SetVariable[]{svar})));
            solver2.solveAll();
            Assert.assertEquals(solver1.getSolutionCount(), solver2.getSolutionCount());
           // System.out.printf("%d vs. %d\n", solver1.getTimeCount(), solver2.getTimeCount());

        }
    }
    
  

}
