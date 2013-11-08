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


import choco.kernel.memory.IStateBitSet;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;
import java.util.List;

public class Node {

    /**
     * index of the current node
     */
    protected int idx;

    /**
     * an integer variable that depicts the potential sucessor nodes of the current node (by indices)
     */
    protected IntDomainVar successors;

    /**
     * an integer variable that depicts the indegree of the current node
     */
    protected IntDomainVar inDegree;

    /**
     * an integer variable that depicts the starting time from the current node
     */
    protected IntDomainVar timeWindow;

    /**
     * the set of mandatory successors of the current node
     */
    protected IStateBitSet mandatorySuccessors;

    /**
     * the set of potential mandatory successors of the current node
     */
    protected IStateBitSet condSuccessors;

    /**
     * the set of incomparable nodes with the current node
     */
    protected IStateBitSet incomparableNodes;

    /**
     * constructor: build a node and its associated attributes
     *
     * @param solver    the Choco solver who uses the current tree constraint
     * @param nbNodes   the total number of nodes involved in the different graphs
     * @param idx   index of the current node
     * @param successor the potential sucessor nodes of the current node
     * @param inDegree  the indegree of the current node
     * @param timeWindow    the starting time from the current node
     * @param graphs    a list of graphs: [0] the graph to partition, [1] the precedence graph,
     * [2] the conditional precedence graph and [3] the incomparability graph    
     * @throws choco.kernel.solver.ContradictionException
     */
    public Node(Solver solver, int nbNodes, int idx, IntDomainVar successor, IntDomainVar inDegree,
                IntDomainVar timeWindow, List<BitSet[]> graphs) {
        this.idx = idx;
        this.successors = successor;
        this.inDegree = inDegree;
        this.timeWindow = timeWindow;

        this.mandatorySuccessors = solver.getEnvironment().makeBitSet(nbNodes);
        BitSet prec = graphs.get(1)[idx];
        for (int i = prec.nextSetBit(0); i >= 0; i = prec.nextSetBit(i + 1)) {
            this.mandatorySuccessors.set(i,true);
        }
        this.condSuccessors = solver.getEnvironment().makeBitSet(nbNodes);
        BitSet condSucc = graphs.get(2)[idx];
        for (int i = condSucc.nextSetBit(0); i >= 0; i = condSucc.nextSetBit(i + 1)) {
            this.condSuccessors.set(i,true);
        }
        this.incomparableNodes = solver.getEnvironment().makeBitSet(nbNodes);
        BitSet inc = graphs.get(3)[idx];
        for (int i = inc.nextSetBit(0); i >= 0; i = inc.nextSetBit(i + 1)) {
            this.incomparableNodes.set(i,true);
        }
    }

    /**
     * constructor: build a node and its associated attributes
     *
     * @param solver    the Choco solver who uses the current tree constraint
     * @param nbNodes   the total number of nodes involved in the different graphs
     * @param idx   index of the current node
     * @param graphs    a list of graphs: [0] the graph to partition, [1] the precedence graph,
     * [2] the conditional precedence graph and [3] the incomparability graph
     * @param matrix    a list of integer matrix: [0] the indegree of each node and [1] the starting time from each node
     * @throws choco.kernel.solver.ContradictionException
     */
    public Node(Solver solver, int nbNodes, int idx, List<BitSet[]> graphs, List<int[][]> matrix) throws ContradictionException {
        this.idx = idx;
        if(nbNodes==2){
            this.successors = solver.createBooleanVar("next_" + idx);
        }else{
            this.successors = solver.createEnumIntVar("next_" + idx, 0, nbNodes-1);
        }
        for (int i = 0; i < nbNodes; i++) {
            if (!graphs.get(0)[idx].get(i)) this.successors.remVal(i);
        }
        this.mandatorySuccessors = solver.getEnvironment().makeBitSet(nbNodes);
        BitSet prec = graphs.get(1)[idx];
        for (int i = prec.nextSetBit(0); i >= 0; i = prec.nextSetBit(i + 1)) {
            this.mandatorySuccessors.set(i,true);
        }
        this.condSuccessors = solver.getEnvironment().makeBitSet(nbNodes);
        BitSet condSucc = graphs.get(2)[idx];
        for (int i = condSucc.nextSetBit(0); i >= 0; i = condSucc.nextSetBit(i + 1)) {
            this.condSuccessors.set(i,true);
        }
        this.incomparableNodes = solver.getEnvironment().makeBitSet(nbNodes);
        BitSet inc = graphs.get(3)[idx];
        for (int i = inc.nextSetBit(0); i >= 0; i = inc.nextSetBit(i + 1)) {
            this.incomparableNodes.set(i,true);
        }
        this.inDegree = solver.createBoundIntVar("deg_" + idx, matrix.get(0)[idx][0], matrix.get(0)[idx][1]);
        this.timeWindow = solver.createBoundIntVar("tw_" + idx, matrix.get(1)[idx][0], matrix.get(1)[idx][1]);
    }

    public int getIdx() {
        return this.idx;
    }

    public IntDomainVar getSuccessors() {
        return successors;
    }

    public IntDomainVar getInDegree() {
        return inDegree;
    }

    public IntDomainVar getTimeWindow() {
        return timeWindow;
    }

    public IStateBitSet getMandatorySuccessors() {
        return mandatorySuccessors;
    }

    public IStateBitSet getCondSuccessors() {
        return condSuccessors;
    }

    public IStateBitSet getIncomparableNodes() {
        return incomparableNodes;
    }
}
