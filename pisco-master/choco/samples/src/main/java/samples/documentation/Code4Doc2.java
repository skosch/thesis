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
import choco.cp.solver.constraints.global.geost.Constants;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.constraints.geost.externalConstraints.IExternalConstraint;
import choco.kernel.model.constraints.geost.externalConstraints.NonOverlappingModel;
import choco.kernel.model.variables.geost.GeostObject;
import choco.kernel.model.variables.geost.ShiftedBox;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
public class Code4Doc2 {

    public static void main(String[] args) {
        new Code4Doc2().cequation();
    }

    public void cabs() {
        //totex cabs
        Model m = new CPModel();
        IntegerVariable x = makeIntVar("x", 1, 5, Options.V_ENUM);
        IntegerVariable y = makeIntVar("y", -5, 5, Options.V_ENUM);
        m.addConstraint(abs(x, y));
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        //totex
    }

    public void calldifferent() {
        //totex calldifferent
        int n = 8;
        CPModel m = new CPModel();
        IntegerVariable[] queens = new IntegerVariable[n];
        IntegerVariable[] diag1 = new IntegerVariable[n];
        IntegerVariable[] diag2 = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            queens[i] = makeIntVar("Q" + i, 1, n);
            diag1[i] = makeIntVar("D1" + i, 1, 2 * n);
            diag2[i] = makeIntVar("D2" + i, -n + 1, n);
        }
        m.addConstraint(allDifferent(queens));
        for (int i = 0; i < n; i++) {
            m.addConstraint(eq(diag1[i], plus(queens[i], i)));
            m.addConstraint(eq(diag2[i], minus(queens[i], i)));
        }
        m.addConstraint(Options.C_ALLDIFFERENT_CLIQUE, allDifferent(diag1));
        m.addConstraint(Options.C_ALLDIFFERENT_CLIQUE, allDifferent(diag2));
        // diagonal constraints
        CPSolver s = new CPSolver();
        s.read(m);
        long tps = System.currentTimeMillis();
        s.solveAll();
        System.out.println("tps nreines1 " + (System.currentTimeMillis() - tps) + " nbNode " + s.
                getNodeCount());
        //totex
    }

    public void camong1() {
        //totex camong1
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable var = makeIntVar("v1", 0, 100, Options.V_BOUND);
        int[] values = new int[]{0, 25, 50, 75, 100};
        m.addConstraint(member(var, values));
        s.read(m);
        s.solve();
        //totex
    }

    public void camong2() {
        //totex camong2
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable nvar = makeIntVar("v1", 1, 2);
        IntegerVariable[] vars = Choco.makeIntVarArray("var", 10, 0, 10);
        int[] values = new int[]{2, 3, 5};
        m.addConstraint(among(nvar, vars, values));
        s.read(m);
        s.solve();
        //totex
    }

    public void camong3() {
        //totex camong3
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable nvar = makeIntVar("v1", 1, 2);
        IntegerVariable[] vars = Choco.makeIntVarArray("var", 10, 0, 10);
        SetVariable values = Choco.makeSetVar("s", 2, 6);
        m.addConstraint(among(nvar, vars, values));
        s.read(m);
        s.solve();
        //totex
    }

    public void cand1() {
        //totex cand1
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v1 = makeIntVar("v1", 0, 1);
        IntegerVariable v2 = makeIntVar("v2", 0, 1);
        m.addConstraint(and(eq(v1, 1), eq(v2, 1)));
        s.read(m);
        s.solve();
        //totex
    }

    public void cand2() {
        //totex cand2
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable[] vars = makeBooleanVarArray("b", 10);
        m.addConstraint(and(vars));
        s.read(m);
        s.solve();
        //totex
    }

    public void catmostnvalue() {
        //totex catmostnvalue
        Model m = new CPModel();
        CPSolver s = new CPSolver();
        IntegerVariable v1 = makeIntVar("v1", 1, 1);
        IntegerVariable v2 = makeIntVar("v2", 2, 2);
        IntegerVariable v3 = makeIntVar("v3", 3, 3);
        IntegerVariable v4 = makeIntVar("v4", 3, 4);
        IntegerVariable n = makeIntVar("n", 3, 3);
        Constraint c = atMostNValue(n, new IntegerVariable[]{v1, v2, v3, v4});
        m.addConstraint(c);
        s.read(m);
        s.solve();
        //totex
    }

    public void cboolchanneling() {
        //totex cboolchanneling
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable bool = makeIntVar("bool", 0, 1);
        IntegerVariable x = makeIntVar("x", 0, 5);
        m.addConstraint(boolChanneling(bool, x, 4));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cclause() {
        //totex cclause
        CPModel mod = new CPModel();
        CPSolver s = new CPSolver();
        IntegerVariable[] vars = makeBooleanVarArray("b", 8);

        IntegerVariable[] plits1 = new IntegerVariable[]{vars[0], vars[3], vars[4]};
        IntegerVariable[] nlits1 = new IntegerVariable[]{vars[1], vars[2], vars[6]};
        mod.addConstraint(clause(plits1, nlits1));

        IntegerVariable[] plits2 = new IntegerVariable[]{vars[5], vars[3]};
        IntegerVariable[] nlits2 = new IntegerVariable[]{vars[1], vars[4], vars[7]};
        mod.addConstraint(clause(plits2, nlits2));

        s.read(mod);
        s.solveAll();
        //totex
    }

    public void ccostregular() {
        ////totex ccostregular
        // z counts the number of 2 followed by a 0 or a 1 in sequence x
        IntegerVariable[] vars = makeIntVarArray("x", 10, 0, 2, Options.V_ENUM);
        IntegerVariable z = makeIntVar("z", 3, 4, Options.V_BOUND);

        FiniteAutomaton auto = new FiniteAutomaton();
        // states
        int start = auto.addState();
        int end = auto.addState();
        auto.setInitialState(start);
        auto.setFinal(start);
        auto.setFinal(end);
        // transitions
        auto.addTransition(start, start, 0, 1);
        auto.addTransition(start, end, 2);
        auto.addTransition(end, start, 2);
        auto.addTransition(end, start, 0, 1);
        // costs
        int[][][] costs = new int[vars.length][3][auto.getNbStates()];
        for (int i = 0; i < costs.length; i++) {
            costs[i][0][end] = 1;
            costs[i][1][end] = 1;
        }

        CPModel m = new CPModel();
        m.addConstraint(costRegular(z, vars, auto, costs));
        CPSolver s = new CPSolver();
        s.read(m);
        s.solveAll();
        //totex
    }

    public void ccostregular2() {
        ////totex ccostregular2
        IntegerVariable[] vars = makeIntVarArray("x", 28, 0, 2, Options.V_ENUM);
        IntegerVariable z = makeIntVar("z", 0, 100, Options.V_BOUND);

        // different rules are formulated as patterns that must NOT be matched by x
        List<String> forbiddenRegExps = new ArrayList<String>();
        // do not end with '00' if start with '11'
        forbiddenRegExps.add("11(0|1|2)*00");
        // at most three consecutive 0
        forbiddenRegExps.add("(0|1|2)*0000(0|1|2)*");
        // no pattern '112' at position 5
        forbiddenRegExps.add("(0|1|2){4}112(0|1|2)*");
        // pattern '12' after a 0 or a sequence of 0
        forbiddenRegExps.add("(0|1|2)*02(0|1|2)*");
        forbiddenRegExps.add("(0|1|2)*01(0|1)(0|1|2)*");
        // at most three 2 on consecutive even positions
        forbiddenRegExps.add("(0|1|2)((0|1|2)(0|1|2))*2(0|1|2)2(0|1|2)2(0|1|2)*");

        // a unique automaton is built as the complement language
        // composed of all the forbidden patterns
        FiniteAutomaton auto = new FiniteAutomaton();
        for (String reg : forbiddenRegExps) {
            FiniteAutomaton a = new FiniteAutomaton(reg);
            auto = auto.union(a);
            auto.minimize();
        }
        auto = auto.complement();
        auto.minimize();
        auto.toDotty("myForbiddenRules.dot");
        System.out.println(auto.getNbStates() + " states");
        // costs: count the number of 0 and of 1 at odd positions
        int[][] costs = new int[vars.length][3];
        for (int i = 1; i < costs.length; i += 2) {
            costs[i][0] = 1;
            costs[i][1] = 1;
        }

        CPModel m = new CPModel();
        m.addConstraint(costRegular(z, vars, auto, costs));
        CPSolver s = new CPSolver();
        s.read(m);
        s.minimize(s.getVar(z), true);
        System.out.println(s.solutionToString());
        //totex
    }

    public void ccumulative() {
        //totex ccumulative
        CPModel m = new CPModel();
        // data
        int n = 11 + 3; //number of tasks (include the three fake tasks)
        int[] heights_data = new int[]{2, 1, 4, 2, 3, 1, 5, 6, 2, 1, 3, 1, 1, 2};
        int[] durations_data = new int[]{1, 1, 1, 2, 1, 3, 1, 1, 3, 4, 2, 3, 1, 1};
        // variables
        IntegerVariable capa = constant(7);
        IntegerVariable[] starts = makeIntVarArray("start", n, 0, 5, Options.V_BOUND);
        IntegerVariable[] ends = makeIntVarArray("end", n, 0, 6, Options.V_BOUND);
        IntegerVariable[] duration = new IntegerVariable[n];
        IntegerVariable[] height = new IntegerVariable[n];
        for (int i = 0; i < height.length; i++) {
            duration[i] = constant(durations_data[i]);
            height[i] = makeIntVar("height " + i, new int[]{0, heights_data[i]});
        }
        TaskVariable[] tasks = Choco.makeTaskVarArray("Task", starts, ends, duration);

        IntegerVariable[] bool = makeIntVarArray("taskIn?", n, 0, 1);
        IntegerVariable obj = makeIntVar("obj", 0, n, Options.V_BOUND, Options.V_OBJECTIVE);
        //post the cumulative
        m.addConstraint(cumulative("cumulative", tasks, height, constant(0), capa,
                Options.C_CUMUL_TI));
        //post the channeling to know if the task is scheduled or not
        for (int i = 0; i < n; i++) {
            m.addConstraint(boolChanneling(bool[i], height[i], heights_data[i]));
        }
        //state the objective function
        m.addConstraint(eq(sum(bool), obj));
        CPSolver s = new CPSolver();
        s.read(m);
        //set the fake tasks to establish the profile capacity of the ressource
        try {
            s.getVar(starts[0]).setVal(1);
            s.getVar(ends[0]).setVal(2);
            s.getVar(height[0]).setVal(2);
            s.getVar(starts[1]).setVal(2);
            s.getVar(ends[1]).setVal(3);
            s.getVar(height[1]).setVal(1);
            s.getVar(starts[2]).setVal(3);
            s.getVar(ends[2]).setVal(4);
            s.getVar(height[2]).setVal(4);
        } catch (ContradictionException e) {
            System.out.println("error, no contradiction expected at this stage");
        }
        // maximize the number of tasks placed in this profile
        s.maximize(s.getVar(obj), false);
        System.out.println("Objective : " + (s.getVar(obj).getVal() - 3));
        for (int i = 3; i < starts.length; i++) {
            if (s.getVar(height[i]).getVal() != 0)
                System.out.println("[" + s.getVar(starts[i]).getVal() + " - "
                        + (s.getVar(ends[i]).getVal() - 1) + "]:"
                        + s.getVar(height[i]).getVal());
        }
        //totex
    }

    public void cdistanceeq() {
        //totex cdistanceeq
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v0 = makeIntVar("v0", 0, 5);
        IntegerVariable v1 = makeIntVar("v1", 0, 5);
        IntegerVariable v2 = makeIntVar("v2", 0, 5);
        m.addConstraint(distanceEQ(v0, v1, v2, 0));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cdistancegt() {
        //totex cdistancegt
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v0 = makeIntVar("v0", 0, 5);
        IntegerVariable v1 = makeIntVar("v1", 0, 5);
        IntegerVariable v2 = makeIntVar("v2", 0, 5);
        m.addConstraint(distanceGT(v0, v1, v2, 0));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cdistancelt() {
        //totex cdistancelt
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v0 = makeIntVar("v0", 0, 5);
        IntegerVariable v1 = makeIntVar("v1", 0, 5);
        IntegerVariable v2 = makeIntVar("v2", 0, 5);
        m.addConstraint(distanceLT(v0, v1, v2, 0));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cdistanceneq() {
        //totex cdistanceneq
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v0 = makeIntVar("v0", 0, 5);
        IntegerVariable v1 = makeIntVar("v1", 0, 5);
        m.addConstraint(distanceNEQ(v0, v1, 0));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cdisjoint1() {
        //totex cdisjoint1
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable var = makeIntVar("v1", 0, 100, Options.V_BOUND);
        int[] values = new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90};
        m.addConstraint(notMember(var, values));
        s.read(m);
        s.solve();
        //totex
    }

    public void cdisjoint2() {
        //totex cdisjoint2
        Model m = new CPModel();
        Solver s = new CPSolver();
        TaskVariable[] tasks1 = Choco.makeTaskVarArray("Task1", 0, 10, new int[]{2, 5});
        TaskVariable[] tasks2 = Choco.makeTaskVarArray("Task2", 0, 10, new int[]{3, 4});
        m.addConstraints(disjoint(tasks1, tasks2));
        s.read(m);
        s.solve();
        //totex
    }

    public void cdomainchanneling() {
        //totex cdomainchanneling
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable x = makeIntVar("var", 0, 10);
        IntegerVariable[] b = makeBooleanVarArray("valueIndicator", 10);
        m.addConstraint(domainChanneling(x, b));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void ceq1() {
        //totex ceq1
        Model m = new CPModel();
        Solver s = new CPSolver();
        int c = 1;
        IntegerVariable v = makeIntVar("v", 0, 2);
        m.addConstraint(eq(v, c));
        s.read(m);
        s.solve();
        //totex
    }

    public void ceq2() {
        //totex ceq2
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v1 = makeIntVar("v1", 0, 2);
        IntegerVariable v2 = makeIntVar("v2", 0, 2);
        IntegerExpressionVariable w1 = plus(v1, 1);
        IntegerExpressionVariable w2 = minus(v2, 1);
        m.addConstraint(eq(w1, w2));
        s.read(m);
        s.solve();
        //totex
    }

    public void ceqcard() {
        //totex ceqcard
        Model m = new CPModel();
        Solver s = new CPSolver();
        SetVariable set = makeSetVar("s", 1, 5);
        IntegerVariable card = makeIntVar("card", 2, 3);
        m.addConstraint(member(set, 3));
        m.addConstraint(eqCard(set, card));
        s.read(m);
        s.solve();
        //totex
    }

    public void cequation() {
        //totex cequation
        CPModel m = new CPModel();
        CPSolver s = new CPSolver();
        int n = 10;
        IntegerVariable[] bvars = makeIntVarArray("b", n, 0, 10, Options.V_ENUM);
        int[] coefs = new int[n];

        int charge = 10;
        Random rand = new Random();
        for (int i = 0; i < coefs.length; i++) {
            coefs[i] = -5 + rand.nextInt(10);
        }
        Constraint knapsack = equation(charge, bvars, coefs);
        m.addConstraint(knapsack);
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cfeaspairac() {
        //totex cfeaspairac
        Model m = new CPModel();
        Solver s = new CPSolver();
        ArrayList<int[]> couples2 = new ArrayList<int[]>();
        couples2.add(new int[]{1, 2});
        couples2.add(new int[]{1, 3});
        couples2.add(new int[]{2, 1});
        couples2.add(new int[]{3, 1});
        couples2.add(new int[]{4, 1});
        IntegerVariable v1 = makeIntVar("v1", 1, 4);
        IntegerVariable v2 = makeIntVar("v2", 1, 4);
        m.addConstraint(feasPairAC(Options.C_EXT_AC32, v1, v2, couples2));
        s.read(m);
        s.solveAll();
        //totex
    }

    public void cfeastupleac() {
        //totex cfeastupleac
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v1 = makeIntVar("v1", 0, 2);
        IntegerVariable v2 = makeIntVar("v2", 0, 4);
        ArrayList<int[]> feasTuple = new ArrayList<int[]>();
        feasTuple.add(new int[]{1, 1}); // x*y = 1
        feasTuple.add(new int[]{2, 4}); // x*y = 1
        m.addConstraint(feasTupleAC(Options.C_EXT_AC2001, feasTuple, v1, v2));
        s.read(m);
        s.solve();
        //totex
    }

    public void cfeastuplefc() {
        //totex cfeastuplefc
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v1 = makeIntVar("v1", 0, 2);
        IntegerVariable v2 = makeIntVar("v2", 0, 4);
        ArrayList<int[]> feasTuple = new ArrayList<int[]>();
        feasTuple.add(new int[]{1, 1}); // x*y = 1
        feasTuple.add(new int[]{2, 4}); // x*y = 1
        m.addConstraint(feasTupleFC(feasTuple, v1, v2));
        s.read(m);
        s.solve();
        //totex
    }

    public void cgeost() {
        //totex cgeost
        Model m = new CPModel();
        int dim = 3;
        int lengths[] = {5, 3, 2};
        int widths[] = {2, 2, 1};
        int heights[] = {1, 1, 1};
        int nbOfObj = 3;
        long seed = 0;
        //Create the Objects
        List<GeostObject> obj = new ArrayList<GeostObject>();
        for (int i = 0; i < nbOfObj; i++) {
            IntegerVariable shapeId = Choco.makeIntVar("sid", i, i);
            IntegerVariable coords[] = new IntegerVariable[dim];
            for (int j = 0; j < coords.length; j++) {
                coords[j] = Choco.makeIntVar("x" + j, 0, 2);
            }
            IntegerVariable start = Choco.makeIntVar("start", 1, 1);
            IntegerVariable duration = Choco.makeIntVar("duration", 1, 1);
            IntegerVariable end = Choco.makeIntVar("end", 1, 1);
            obj.add(new GeostObject(dim, i, shapeId, coords, start, duration, end));
        }
        //Create the ShiftedBoxes and add them to corresponding shapes
        List<ShiftedBox> sb = new ArrayList<ShiftedBox>();
        int[] t = {0, 0, 0};
        for (int d = 0; d < nbOfObj; d++) {
            int[] l = {lengths[d], heights[d], widths[d]};
            sb.add(new ShiftedBox(d, t, l));
        }
        //Create the external constraints vector
        List<IExternalConstraint> ectr = new ArrayList<IExternalConstraint>();
        //create the list of dimensions for the external constraint
        int[] ectrDim = new int[dim];
        for (int d = 0; d < dim; d++)
            ectrDim[d] = d;
        //create the list of object ids for the external constraint
        int[] objOfEctr = new int[nbOfObj];
        for (int d = 0; d < nbOfObj; d++) {
            objOfEctr[d] = obj.get(d).getObjectId();
        }
        //create and add one external constraint of type non overlapping
        NonOverlappingModel n = new NonOverlappingModel(Constants.NON_OVERLAPPING, ectrDim, objOfEctr);
        ectr.add(n);
        //create and post the geost constraint
        Constraint geost = Choco.geost(dim, obj, sb, ectr);
        m.addConstraint(geost);
        Solver s = new CPSolver();
        s.read(m);
        s.setValIntSelector(new RandomIntValSelector(seed));
        s.setVarIntSelector(new RandomIntVarSelector(s, seed));
        s.solveAll();
        //totex
    }
}
