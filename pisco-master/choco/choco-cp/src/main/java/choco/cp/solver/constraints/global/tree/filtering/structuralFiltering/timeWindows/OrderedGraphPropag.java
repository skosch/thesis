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

package choco.cp.solver.constraints.global.tree.filtering.structuralFiltering.timeWindows;


import choco.cp.solver.constraints.global.tree.filtering.RemovalsAdvisor;
import choco.cp.solver.constraints.global.tree.structure.inputStructure.Node;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.PrecsGraphView;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class OrderedGraphPropag {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * boolean that allow to display debug mode for this propagator
     */
    protected boolean debugRem = false;

    /**
     * total number of nodes involved in the tree constraint
     */
    protected int nbNodes;

    /**
     * table of the nodes involved in the tree constraint
     */
    protected Node[] nodes;

    /**
     * graph of the precedence constraints
     */
    protected PrecsGraphView precs;

    /**
     * the set of source nodes in the precedence graph
     */
    protected IStateBitSet src;

    /**
     * the set of sink nodes in the precedence graph
     */
    protected IStateBitSet sink;

    /**
     * travel time matrix
     */
    protected IStateInt[][] travelTime;

    /**
     * minimum travel time matrix (computed from travelTime)
     */
    protected IStateInt[][] minTravelTime;

    /**
     * record the infeasible values in the different domains of the variables involved in the constraint
     */
    protected RemovalsAdvisor propagateStruct;

    /**
     * Constructor: build a propagator for the time windows constraints according to the precedence constraints
     *
     * @param travelTime    travel time matrix
     * @param minTravelTime     minimum travel time matrix
     * @param precs     graph of the precedence constraints
     * @param nodes     table of nodes involved in the tree constraints
     * @param propagateStruct   data structure managing the removals
     */
    public OrderedGraphPropag(IStateInt[][] travelTime, IStateInt[][] minTravelTime,
                              PrecsGraphView precs, Node[] nodes, RemovalsAdvisor propagateStruct) {
        this.precs = precs;
        this.travelTime = travelTime;
        this.minTravelTime = minTravelTime;
        this.nodes = nodes;
        this.propagateStruct = propagateStruct;
        this.nbNodes = nodes.length;
        this.sink = precs.getSinkNodes();
        this.src = precs.getSrcNodes();

    }

    /**
     * filtering methods that update the time windows of each node according to the precedence constraints and
     * the minimum travel time matrix
     *
     * @throws choco.kernel.solver.ContradictionException
     */
    public void applyTWfiltering() throws ContradictionException {
        updateInf();
        updateSup();
        updateSupByDesc();
    }

    private void updateInf() {
        Queue<Integer> queue = new LinkedList<Integer>();
        BitSet reached = new BitSet(nbNodes);
        for (int i = src.nextSetBit(0); i >= 0; i = src.nextSetBit(i + 1)) {
            if (!reached.get(i)) queue.offer(i);
        }
        while (!queue.isEmpty()) {
            int i = queue.poll();
            reached.set(i, true);
            for (int j = precs.getSuccessors(i).nextSetBit(0); j >= 0; j = precs.getSuccessors(i).nextSetBit(j + 1)) {
                if (!reached.get(j)) {
                    queue.offer(j);
                    int newVal = nodes[i].getTimeWindow().getInf() + minTravelTime[i][j].get();
                    if (nodes[j].getTimeWindow().getInf() < newVal) {
                        propagateStruct.setMinStart(j,newVal);
                    }
                }
            }
        }
    }

    private void updateSup() throws ContradictionException {
        Queue<Integer> queue = new LinkedList<Integer>();
        BitSet reached = new BitSet(nbNodes);
        for (int i = sink.nextSetBit(0); i >= 0; i = sink.nextSetBit(i + 1)) {
            if (!reached.get(i)) queue.offer(i);
        }
        while (!queue.isEmpty()) {
            int j = queue.poll();
            reached.set(j, true);
            for (int i = precs.getPredecessors(j).nextSetBit(0); i >= 0; i = precs.getPredecessors(j).nextSetBit(i + 1)) {
                if (!reached.get(i)) {
                    queue.offer(i);
                    int newVal = nodes[j].getTimeWindow().getSup() - minTravelTime[i][j].get();
                    if (nodes[i].getTimeWindow().getSup() > newVal) {
                        propagateStruct.setMaxStart(i,newVal);
                    }
                }
            }
        }
    }

    private void updateSupByDesc() throws ContradictionException {
        Queue<Integer> queue = new LinkedList<Integer>();
        for (int i = sink.nextSetBit(0); i >= 0; i = sink.nextSetBit(i + 1)) queue.offer(i);
        if (debugRem) {
            LOGGER.info("======= sink ==============");
            affichePrec();
            LOGGER.info("" + queue.toString());
            LOGGER.info("==========================");
        }
        while (!queue.isEmpty()) {
            int j = queue.poll();
            if (precs.getSuccessors(j).cardinality() > 0) propagateBestDescSet(j);
            for (int i = precs.getPredecessors(j).nextSetBit(0); i >= 0; i = precs.getPredecessors(j).nextSetBit(i + 1)) {
                if (!queue.contains(i)) queue.offer(i);
            }
        }
    }

    private void propagateBestDescSet(int i) throws ContradictionException {
        int newSum = 0;
        int maxStartSucc = Integer.MIN_VALUE;
        for (int j = precs.getSuccessors(i).nextSetBit(0); j >= 0; j = precs.getSuccessors(i).nextSetBit(j + 1)) {
            if (nodes[j].getTimeWindow().getSup() > maxStartSucc) maxStartSucc = nodes[j].getTimeWindow().getSup();//o[j].getSup();
            int minPred_j = minTravelTime[i][j].get();
            for (int k = precs.getSuccessors(i).nextSetBit(0); k >= 0; k = precs.getSuccessors(i).nextSetBit(k + 1)) {
                if (k != j && minPred_j > minTravelTime[k][j].get()) {
                    minPred_j = minTravelTime[k][j].get();
                }
            }
            newSum += minPred_j;
        }
        int newMax = maxStartSucc - newSum;
        if (nodes[i].getTimeWindow().getSup() > newMax) {
            if (debugRem) {
                LOGGER.info("-------------------------------------------------------------------");
                LOGGER.info("Update[propagateBestDescSet] : twPropagation.TWConstraint for nodes " + i);
                LOGGER.info("\t max start " + i + " = " + nodes[i].getTimeWindow().getSup() + " devient " + newMax);
                LOGGER.info("-------------------------------------------------------------------");
            }
            propagateStruct.setMaxStart(i,newMax);
        }
    }

    public void afficheMinTravelTime() {
        StringBuffer st = new StringBuffer();
        for (int i = 0; i < nbNodes; i++) {
            st.append("prec[" + i + "] = ");
            for (int j = precs.getSuccessors(i).nextSetBit(0); j >= 0; j = precs.getSuccessors(i).nextSetBit(j + 1)) {
                st.append(j + " (" + minTravelTime[i][j] + "),");
            }
            LOGGER.info(st.toString());
        }
    }

    private void affichePrec() {
        StringBuffer st = new StringBuffer();
        for (int i = 0; i < nbNodes; i++) {
            st.append("prec[" + i + "] = ");
            for (int j = precs.getSuccessors(i).nextSetBit(0); j >= 0; j = precs.getSuccessors(i).nextSetBit(j + 1)) {
                st.append(j + " ");
            }
            LOGGER.info(st.toString());
        }
    }

}
