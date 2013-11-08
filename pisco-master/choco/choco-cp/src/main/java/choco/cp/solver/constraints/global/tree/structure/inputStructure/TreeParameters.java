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

package choco.cp.solver.constraints.global.tree.structure.inputStructure;


import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;
import java.util.List;


public class TreeParameters {

    /**
     * Choco solver embedding the tree constraint
     */
    protected Solver solver;

    /**
     * total number of nodes involved in the graph
     */
    protected int nbNodes;

    /**
     * table of nodes involved in the graphs composing a tree constraint
     */
    protected Node[] nodes;

    /**
     * an integer variable that depicts the number of tree allowed to partition the graph
     */
    protected IntDomainVar ntree;

    /**
     * an integer variable that depicts the number of proper tree allowed to partition the graph
     */
    protected IntDomainVar nproper;

    /**
     * a bounded variable that depicts the total cost of the partition
     */
    protected IntDomainVar objective;

    /**
     * the travel time matrix
     */
    protected IStateInt[][] travelTime;

    /**
     * constructor: build the input data of a tree constraint.
     *
     * @param solver        the Choco problem who uses the current tree constraint
     * @param nbNodes   the total number of nodes involved in the different graphs
     * @param ntree     an integer variable that depicts the number of tree allowed to partition the graph
     * @param nproper   an integer variable that depicts the number of proper tree allowed to partition the graph
     * @param objective a bounded variable that depicts the total cost of the partition
     * @param graphs    a list of graphs: [0] the graph to partition, [1] the precedence graph,
     *                  [2] the conditional precedence graph and [3] the incomparability graph
     * @param matrix    a list of integer matrix: [0] the indegree of each node
     *                  and [1] the starting time from each node
     * @param travel    the travel time matrix 
     * @throws choco.kernel.solver.ContradictionException
     */
    public TreeParameters(Solver solver, int nbNodes, IntDomainVar ntree, IntDomainVar nproper,
                          IntDomainVar objective, List<BitSet[]> graphs,
                          List<int[][]> matrix, int[][] travel) throws ContradictionException {
        this.solver = solver;
        this.nbNodes = nbNodes;
        this.ntree = ntree;
        this.nproper = nproper;
        this.objective = objective;

        this.nodes = new Node[this.nbNodes];
        this.travelTime = new IStateInt[this.nbNodes][this.nbNodes];
        for (int i = 0; i < this.nbNodes; i++) {
            for (int j = 0; j < this.nbNodes; j++)
                this.travelTime[i][j] = solver.getEnvironment().makeInt(travel[i][j]);
        }
        for (int i = 0; i < this.nbNodes; i++) this.nodes[i] = new Node(solver, nbNodes, i, graphs, matrix);
    }

    /**
     * constructor: build the input data of a tree constraint.
     *
     * @param solver    the Choco solver who uses the current tree constraint
     * @param nbNodes   the total number of nodes involved in the different graphs
     * @param ntree     an integer variable that depicts the number of tree allowed to partition the graph
     * @param nproper   an integer variable that depicts the number of proper tree allowed to partition the graph
     * @param objective a bounded variable that depicts the total cost of the partition
     * @param nodes     the nodes and its attributes
     * @param travel    the travel time matrix
     * @throws choco.kernel.solver.ContradictionException
     */
    public TreeParameters(Solver solver, int nbNodes, IntDomainVar ntree, IntDomainVar nproper,
                          IntDomainVar objective, Node[] nodes, int[][] travel){
        this.solver = solver;
        this.nbNodes = nbNodes;
        this.ntree = ntree;
        this.nproper = nproper;
        this.objective = objective;

        this.nodes = nodes;
        this.travelTime = new IStateInt[this.nbNodes][this.nbNodes];
        for (int i = 0; i < this.nbNodes; i++) {
            for (int j = 0; j < this.nbNodes; j++)
                this.travelTime[i][j] = solver.getEnvironment().makeInt(travel[i][j]);
        }
    }

    /**
     * build a table of all the integer variables involved in a tree constraint
     *
     * @return  a table of all the integer variables involved in a tree constraint 
     */
    public IntDomainVar[] getAllVars() {
        IntDomainVar[] tab = new IntDomainVar[3 * nbNodes + 3];
        tab[0] = ntree;
        tab[1] = nproper;
        tab[2] = objective;
        int idx = 3;
        for (int i = 0; i < nbNodes; i++) {
            tab[idx++] = nodes[i].getSuccessors();
        }
        for (int i = 0; i < nbNodes; i++) {
            tab[idx++] = nodes[i].getTimeWindow();
        }
        for (int i = 0; i < nbNodes; i++) {
            tab[idx++] = nodes[i].getInDegree();
        }
        return tab;
    }

    /**
     * build a table of all the successor variables that depict the graph to partition
     *
     * @return  a table of all the successor variables that depict the graph to partition
     */
    public IntDomainVar[] getSuccVars() {
        IntDomainVar[] tab = new IntDomainVar[nbNodes];
        int idx = 0;
        for (int i = 0; i < nbNodes; i++) {
            tab[idx++] = nodes[i].getSuccessors();
        }
        return tab;
    }

    public Solver getSolver() {
        return solver;
    }

    public int getNbNodes() {
        return nbNodes;
    }

    /**
     * the table of all the nodes object that compose the input structure <code> TreeParameters </code>
     * of a tree constraint
     *
     * @return  a table of all the nodes object associated with the input structure of a tree constraint
     */
    public Node[] getNodes() {
        return nodes;
    }

    public IntDomainVar getNtree() {
        return ntree;
    }

    public IntDomainVar getNproper() {
        return nproper;
    }

    public IntDomainVar getObjective() {
        return objective;
    }

    public IStateInt[][] getTravelTime() {
        return travelTime;
    }
}
