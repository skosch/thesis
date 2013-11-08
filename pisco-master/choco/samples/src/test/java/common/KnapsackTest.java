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
package common;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.StatisticUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import choco.kernel.model.Model;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import gnu.trove.TLongArrayList;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static choco.Choco.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27 juil. 2010
 */
public class KnapsackTest {

    static TLongArrayList times = new TLongArrayList();

    int[] capacites = {0, 34};
    int[] energies = {6, 4, 3};
    int[] volumes = {7, 5, 2};
    int[] nbOmax = {4, 6, 17};
    int n = 3;


    private void generate(int n) {
        Random r = new Random();

        capacites = new int[2];
        capacites[1] = 60 + r.nextInt(15);

        energies = new int[n];
        volumes = new int[n];
        nbOmax = new int[n];
        for (int i = 0; i < n; i++) {
            energies[i] = 1 + r.nextInt(10);
            volumes[i] = 1 + r.nextInt(10);
            nbOmax[i] = capacites[1] / volumes[i];
        }
//        System.out.println(n);
//        System.out.println(Arrays.toString(capacites));
//        System.out.println(Arrays.toString(energies));
//        System.out.println(Arrays.toString(volumes));
//        System.out.println(Arrays.toString(nbOmax));
    }


    private void parse(String fileName, int n) throws IOException {
        URL url = this.getClass().getResource(fileName);
        ParseurKS.parseFile(url.getFile(), n);

//        Arrays.sort(ParseurKS.bounds);
        capacites = ParseurKS.bounds;
        energies = ParseurKS.instances[0];
        volumes = ParseurKS.instances[1];
        this.n = volumes.length;
        nbOmax = new int[this.n];
        for (int i = 0; i < this.n; i++) {
            nbOmax[i] = capacites[1] / volumes[i];
        }

//        System.out.println(this.n);
//        System.out.println(Arrays.toString(capacites));
//        System.out.println(Arrays.toString(energies));
//        System.out.println(Arrays.toString(volumes));
//        System.out.println(Arrays.toString(nbOmax));

    }


    public void modelIt(boolean opt) throws IOException {
        int nos = energies.length;

        IEnvironment env = new EnvironmentTrailing();

        IntegerVariable[] objects = new IntegerVariable[nos];
        int i = 0;
        for (; i < nos; i++) {
            objects[i] = Choco.makeIntVar("v" + i, 0, nbOmax[i], Options.V_ENUM);
        }

        IntegerVariable power = Choco.makeIntVar("v" + (i++), 0, 999999, Options.V_BOUND);

        IntegerVariable scalar = Choco.makeIntVar("v" + (i++), capacites[0] - 1, capacites[1] + 1, Options.V_ENUM);

        List<Constraint> lcstrs = new ArrayList<Constraint>(3);

        lcstrs.add(Choco.eq(Choco.scalar(objects, volumes), scalar));
//        lcstrs.add(Choco.geq(scalar, capacites[0]));
//        lcstrs.add(Choco.leq(scalar, capacites[1]));
        lcstrs.add(Choco.eq(Choco.scalar(objects, energies), power));

        Model model = new CPModel();
        model.addConstraints(lcstrs.toArray(new Constraint[lcstrs.size()]));

        IntegerVariable[] vars = new IntegerVariable[nos + 2];
        System.arraycopy(objects, 0, vars, 0, nos);
        vars[nos] = power;
        vars[nos + 1] = scalar;
        Solver s = new CPSolver();
        s.read(model);
//        s.attachGoal(BranchingFactory.incDomWDeg(s, s.getVar(objects), new IncreasingDomain(), 0));
        s.attachGoal(new AssignOrForbidIntVarVal(new StaticVarOrder(s, s.getVar(objects)), new MinVal()));
//        s.setTimeLimit(20000);

//        System.out.printf("%s\n", s.pretty());
     //     ChocoLogging.toVerbose();
        if (opt) {
            s.maximize(s.getVar(power), false);
        } else {
            s.solveAll();
        }
        times.add(s.getTimeCount());
//        System.out.println("down:" + AbstractSearchLoop.d);
//        System.out.printf("a:%d, b:%d, c:%d\n", IntLinComb.a, IntLinComb.b, IntLinComb.c);
    }

    public static void main(String[] args) throws IOException {
        KnapsackTest ks = new KnapsackTest();
        ChocoLogging.toSilent();
//        ks.generate(3);
//        for (int i = 0; i < 5; i++) {
//            ks.parse("../files/knapsack.20-1.txt");
        for (int j = 6; j < 15; j++) {
            for (int i = 1; i < 11; i++) {
                ks.parse("../files/knapsack.20-1.txt", j);
                ks.modelIt(false);
//                ChocoLogging.flushLogs();
            }
            long[] values = StatisticUtils.prepare(times.toNativeArray());
            times.clear();
            System.out.printf("j = %d\n", j);
            System.out.printf("Moyenne: %f\n", StatisticUtils.mean(values));
            System.out.printf("Ecart-type: %f\n", StatisticUtils.standarddeviation(values));
//        System.out.printf("inst: %f (%d)\n", StatisticUtils.mean(IntLinComb.a.toNativeArray()), IntLinComb.a.size());
//        System.out.printf("low: %f (%d)\n", StatisticUtils.mean(IntLinComb.b.toNativeArray()), IntLinComb.b.size());
//        System.out.printf("upp: %f (%d)\n", StatisticUtils.mean(IntLinComb.c.toNativeArray()), IntLinComb.c.size());
        }
//        }


    }

    @Test
    public void testALL10() throws IOException {
        KnapsackTest ks = new KnapsackTest();
        for (int i = 0; i < 10; i++) {
            ks.parse("../files/knapsack.13-1.txt", 13);
            ks.modelIt(true);
            ChocoLogging.flushLogs();
        }

        long[] t = StatisticUtils.prepare(times.toNativeArray());

        System.out.printf("std : %f", StatisticUtils.mean(t));
        System.out.printf("std : %f", StatisticUtils.standarddeviation(t));

    }

    @Test
    public void testOPT13() throws IOException {
        KnapsackTest ks = new KnapsackTest();
        ks.parse("../files/knapsack.13-1.txt", 13);
        ks.modelIt(true);
//        System.out.printf("i:%d, r:%d, l:%d, u:%d\n", IntDomainVarImpl.inst, IntDomainVarImpl.rem,
//                IntDomainVarImpl.low, IntDomainVarImpl.upp);
//        System.out.printf("i:%d, r:%d, l:%d, u:%d\n", AbstractIntDomain.instE, AbstractIntDomain.remE,
//                AbstractIntDomain.lowE, AbstractIntDomain.uppE);
//        Assert.assertTrue(s.getSearchLoop().getTimeCount() < 15000, "time spent > 15000 ms" );
    }

    @Test
    public void test100() {
        Model model = new CPModel();
        Random rand = new Random(0);
        int n = 300;
        int m = 300;

        int[] weights = new int[n];
        int[] costs = new int[n];

        for (int i = 0; i < n; i++) {
            int value = rand.nextInt(100) + 1;
            costs[i] = weights[i] = value;
        }

        IntegerVariable[] items = Choco.makeIntVarArray("items", weights.length, 0, 1);
        IntegerVariable sommePoids = Choco.makeIntVar("sommePoids", 0, m, Options.V_BOUND);
        int maxSommeValue = 0;
        for (int value : costs) {
            maxSommeValue += value;
        }
        IntegerVariable sommeProfit = Choco.makeIntVar("sommeValue", 0, maxSommeValue, Options.V_OBJECTIVE);
        model.addConstraint(Choco.knapsackProblem(sommePoids, sommeProfit, items, weights, costs));

        CPSolver solver = new CPSolver();
        solver.read(model);
        solver.setTimeLimit(240000);
//        ChocoLogging.toSolution();
        solver.maximize(true);


    }

    @Test
    public void testEquation() {
        int n = 3;
        IntegerVariable[] bvars = makeIntVarArray("b", n, 0, 2, Options.V_ENUM);
        int[] coefs = new int[]{1, 2, 3};
        int charge = 5;
        Constraint knapsack = equation(charge, bvars, coefs);

        CPModel m = new CPModel();
        m.addConstraint(knapsack);
        CPSolver s = new CPSolver();
        s.read(m);
        s.solveAll();

        CPModel m2 = new CPModel();
        m2.addConstraint(Choco.eq(charge, scalar(coefs, bvars)));
        CPSolver s2 = new CPSolver();
        s2.read(m2);
        s2.solveAll();
        org.junit.Assert.assertEquals(s.getSolutionCount(), s2.getSolutionCount());
    }

    @Test(expected = ModelException.class)
    public void testEquation2() {
        int n = 3;
        IntegerVariable[] bvars = makeIntVarArray("b", n, 0, 1, Options.V_ENUM);
        IntegerVariable var = makeIntVar("v", 0, n - 1, Options.V_ENUM);
        int[] coefs = new int[n];
        Arrays.fill(coefs,1);
        Constraint knapsack = equation(var, bvars, coefs);

        CPModel m = new CPModel();
        m.addConstraint(knapsack);
        CPSolver s = new CPSolver();
        s.read(m);
        s.solveAll();

        CPModel m2 = new CPModel();
        m2.addConstraint(Choco.eq(var, scalar(coefs, bvars)));
        CPSolver s2 = new CPSolver();
        s2.read(m2);
        s2.solveAll();
        org.junit.Assert.assertEquals(s.getSolutionCount(), s2.getSolutionCount());
    }

    @Test(expected = ModelException.class)
    public void testEquation3() {
        int n = 3;
        IntegerVariable[] bvars = makeIntVarArray("b", n, 0, 1, Options.V_ENUM);
        int[] coefs = new int[n];
        Arrays.fill(coefs,-1);
        Constraint knapsack = equation(-n, bvars, coefs);

        CPModel m = new CPModel();
        m.addConstraint(knapsack);
        CPSolver s = new CPSolver();
        s.read(m);
        s.solveAll();

        CPModel m2 = new CPModel();
        m2.addConstraint(Choco.eq(-n+1, scalar(coefs, bvars)));
        CPSolver s2 = new CPSolver();
        s2.read(m2);
        s2.solveAll();
        org.junit.Assert.assertEquals(s.getSolutionCount(), s2.getSolutionCount());
    }

}
