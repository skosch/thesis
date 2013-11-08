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
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.tree.TreeParametersObject;
import choco.kernel.solver.Solver;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static choco.Choco.makeIntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 21 sept. 2010
 */
public class TreeConstraintTest {

    @Test
     public void testTree(){
        Model m = new CPModel();
        int nbNodes = 7;
        //1- create the  variables involved in the partitioning problem
        IntegerVariable ntree = makeIntVar("ntree",1,5);
        IntegerVariable  nproper = makeIntVar("nproper",1,1);
        IntegerVariable  objective = makeIntVar("objective",1,100, Options.V_BOUND, Options.V_NO_DECISION);
        //2- create the different graphs modeling restrictions
        List<BitSet[]> graphs = new ArrayList<BitSet[]>();
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
        succ[0].set(0,true); succ[0].set(2,true); succ[0].set(4,true);
        succ[1].set(0,true); succ[1].set(1,true); succ[1].set(3,true);
        succ[2].set(0,true); succ[2].set(1,true); succ[2].set(3,true); succ[2].set(4,true);
        succ[3].set(2,true); succ[3].set(4,true); // successor of 3 is either 2 or 4
        succ[4].set(2,true); succ[4].set(3,true);
        succ[5].set(4,true); succ[5].set(5,true); succ[5].set(6,true);
        succ[6].set(3,true); succ[6].set(4,true); succ[6].set(5,true);
         // restriction on precedences
        prec[0].set(4,true); // 0 has to precede 4
        prec[4].set(3,true); prec[4].set(2,true);
        prec[6].set(4,true);
//         restriction on conditional precedences
        condPrecs[5].set(1,true); // 5 has to precede 1 if they belong to the same tree
//         restriction on incomparability:
        inc[0].set(6,true); inc[6].set(0,true); // 0 and 6 have to belong to distinct subtrees
        graphs.add(succ);
        graphs.add(prec);
        graphs.add(condPrecs);
        graphs.add(inc);
        //3- create the different matrix modeling restrictions
        List<int[][]> matrix = new ArrayList<int[][]>();
         // restriction on bounds on the indegree of each node
        int[][] degree = new int[nbNodes][2];
        for (int i = 0; i < nbNodes; i++) {
            degree[i][0] = 0; degree[i][1] = 2; // 0 <= indegree[i] <= 2
        }
        matrix.add(degree);
         // restriction on bounds on the starting time at each node
        int[][] tw = new int[nbNodes][2];
        for (int i = 0; i < nbNodes; i++) {
            tw[i][0] = 0; tw[i][1] = 100; // 0 <= start[i] <= 100
        }
        tw[0][1] = 15;      // 0 <= start[0] <= 15
        tw[2][0] = 35; tw[2][1] = 40; // 35 <= start[2] <= 45
        tw[6][1] = 5;      // 0 <= start[6] <= 5
        matrix.add(tw);
        //4- matrix for the travel time between each pair of nodes
        int[][] travel = new int[nbNodes][nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) travel[i][j] = 100000;
        }
        travel[0][0] = 0; travel[0][2] = 10; travel[0][4] = 20;
        travel[1][0] = 20; travel[1][1] = 0; travel[1][3] = 20;
        travel[2][0] = 10; travel[2][1] = 10; travel[2][3] = 5; travel[2][4] = 5;
        travel[3][2] = 5; travel[3][4] = 2;
        travel[4][2] = 5; travel[4][3] = 2;
        travel[5][4] = 15; travel[5][5] = 0; travel[5][6] = 10;
        travel[6][3] = 5; travel[6][4] = 20; travel[6][5] = 10;
        //5- create the input structure and the tree constraint
        TreeParametersObject parameters = new TreeParametersObject(nbNodes, ntree, nproper, objective
             , graphs, matrix, travel);
        Constraint c = Choco.tree(parameters);
        m.addConstraint(c);
        Solver s = new CPSolver();
        s.read(m);
        //6- heuristic: choose successor variables as the only decision variables
        s.setVarIntSelector(new StaticVarOrder(s, s.getVar(parameters.getSuccVars())));
//        ChocoLogging.toSolution();
        s.solveAll();
        Assert.assertEquals(s.getSolutionCount(), 2);
    }
}
