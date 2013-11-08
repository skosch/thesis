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
package choco.model.constraints.global;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.tree.TreeParametersObject;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static choco.Choco.*;


public class Bug4 {

    public static int computeMaxCost(int[][] costs) {
        int maxCost = 0;
        for (int i = 0; i < costs.length; i++)
            for (int j = 0; j < costs.length; j++)
                maxCost += costs[i][j];
        return maxCost;

    }


    public static void postTreeConstraint(Model m, IntegerVariable[] sucs, IntegerVariable objective, int nbNodes, int[][] costs) {
        BitSet[] succ = new BitSet[nbNodes];
        BitSet[] prec = new BitSet[nbNodes];
        BitSet[] condPrecs = new BitSet[nbNodes];
        BitSet[] inc = new BitSet[nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            succ[i] = new BitSet(nbNodes);
            prec[i] = new BitSet(nbNodes);
            condPrecs[i] = new BitSet(nbNodes);
            inc[i] = new BitSet(nbNodes);
        }
// initial graph (encoded as successors variables)
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++)
                succ[i].set(j, true);
            ;
        }
// restriction on bounds on the indegree of each node
        int[][] degree = new int[nbNodes][2];
        for (int i = 0; i < nbNodes; i++) {
            degree[i][0] = 0;
            degree[i][1] = nbNodes; // 0 <= indegree[i] <= nbNodes
        }
// restriction on bounds on the starting time at each node
        int[][] tw = new int[nbNodes][2];
        for (int i = 0; i < nbNodes; i++) {
            tw[i][0] = 0;
            tw[i][1] = computeMaxCost(costs); // 0 <= start[i] <= maxCost
        }

        int[][] travel = new int[nbNodes][nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) {
                travel[i][j] = costs[i][j];
            }
        }

//1- create the variables involved in the partitioning problem
        IntegerVariable ntree = makeIntVar("ntree", 1, 1);
        IntegerVariable nproper = makeIntVar("nproper", 1, 1);

//2- create the different graphs modeling restrictions
        List<BitSet[]> graphs = new ArrayList<BitSet[]>();
        graphs.add(succ);
        graphs.add(prec);
        graphs.add(condPrecs);
        graphs.add(inc);


//3- create the different matrix modeling restrictions
        List<int[][]> matrix = new ArrayList<int[][]>();
        matrix.add(degree);
        matrix.add(tw);

//4- matrix for the travel time between each pair of nodes

//5- create the input structure and the tree constraint
        TreeParametersObject parameters = new TreeParametersObject(nbNodes, ntree, nproper,
                objective
                , graphs, matrix, travel);
        Constraint c = tree(parameters);
        m.addConstraint(c);

//5- constraining succesor variables
        for (int i = 0; i < parameters.getSuccVars().length; i++) {
            m.addConstraint(eq(parameters.getSuccVars()[i], sucs[i]));
        }

    }

    public static void runTest(int[][] costs) throws ContradictionException {
//int maxIntVal=100000;
        int n = costs.length;

        Model m = new CPModel();
        IntegerVariable[] sucs = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            sucs[i] = makeIntVar("suc_" + i, 0, n - 1);

            m.addVariable(sucs[i]);

            if (i == 0)
                m.addConstraint(eq(sucs[i], 0));
            else
                m.addConstraint(neq(sucs[i], i));

        }


// Setting up the objective
        IntegerVariable obj = makeIntVar("objective");
        m.addVariable(obj);

// Posting Tree constraint
        postTreeConstraint(m, sucs, obj, n, costs);

        Solver s = new CPSolver();
        s.read(m);

//7- heuristic: choose successor variables as the only decision variables
        IntegerVariable[] decVars = new IntegerVariable[n + 1];
        for (int i = 0; i < n; i++)
            decVars[i] = sucs[i];
        decVars[n] = obj;
        s.setVarIntSelector(new StaticVarOrder(s, s.getVar(decVars)));

        s.monitorNodeLimit(true);
        s.monitorFailLimit(true);
        s.monitorTimeLimit(true);


// define the objective variable
        s.setObjective(s.getVar(obj));
// define the optimization
        s.setDoMaximize(false);
// restart from the root nod after each solution
        s.setRestart(false);
// stop at first solution
        s.setFirstSolution(true);
// generate and launch the search
        s.generateSearchStrategy();
        s.launch();
// if there is a first solution
        int sol_id = 0;
        System.out.println("OBJ\tTIME\tFAILURES");
        int last_obj = 0;
        if (s.isFeasible()) {
            do {
                sol_id += 1;
                last_obj = s.getVar(obj).getVal();
                System.out.println(last_obj + "\t" + s.getTimeCount() + "\t" + s.getFailCount());
            } while (s.nextSolution());
            System.out.println(last_obj + "\t" + s.getTimeCount() + "\t" + s.getFailCount());
        } else
            System.out.println("failure");

    }

    public static int computeCost(int[][] costs, int[] sucs) {
        int totalCost = 0;
        for (int i = 0; i < sucs.length; i++)
            totalCost += costs[i][sucs[i]];
        return totalCost;

    }

    public static void main(String[] args) throws ContradictionException, IOException {
//String out_dir="";
        String out_dir = "";
        int[][] costs = {{0, 325, 998, 981, 953, 531, 972, 892, 557, 574},
                {325, 0, 777, 674, 636, 208, 696, 735, 232, 263},
                {998, 777, 0, 415, 832, 643, 233, 216, 678, 564},
                {981, 674, 415, 0, 440, 471, 182, 581, 473, 411},
                {953, 636, 832, 440, 0, 462, 610, 951, 416, 473},
                {531, 208, 643, 471, 462, 0, 518, 656, 55, 79},
                {972, 696, 233, 182, 610, 518, 0, 411, 539, 443},
                {892, 735, 216, 581, 951, 656, 411, 0, 703, 584},
                {557, 232, 678, 473, 416, 55, 539, 703, 0, 119},
                {574, 263, 564, 411, 473, 79, 443, 584, 119, 0}};

        int[] mysucs = {0, 0, 6, 9, 8, 1, 3, 2, 5, 5};
        System.out.println(computeCost(costs, mysucs));
        runTest(costs);
    }
}