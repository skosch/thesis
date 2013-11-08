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
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.VarGraphView;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class DirectedPropag {

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
     * graph of the required arcs
     */
    protected IStateBitSet[] sure;

    /**
     * reverse graph of the required arcs
     */
    protected IStateBitSet[] revSure;

    /**
     * the set of source nodes in the graph
     */
    protected IStateBitSet src;

    /**
     * the set of sink nodes in the graph
     */
    protected IStateBitSet sink;

    /**
     * graph of the potential arcs
     */
    protected IStateBitSet[] maybe;

    /**
     * reverse graph of the potential arcs
     */
    protected IStateBitSet[] revMaybe;

    /**
     * travel time matrix
     */
    IStateInt[][] travelTime;

    /**
     * record the infeasible values in the different domains of the variables involved in the constraint
     */
    protected RemovalsAdvisor propagateStruct;

    /**
     * Constructor: build a propagator for the time windows constraints according to the graph
     *
     * @param travelTime      travel time matrix
     * @param graph           view of the graph to partition
     * @param nodes           table of nodes involved in the graph
     * @param propagateStruct data structure managing the removals
     */
    public DirectedPropag(IStateInt[][] travelTime, VarGraphView graph, Node[] nodes, RemovalsAdvisor propagateStruct) {
        this.travelTime = travelTime;
        this.sure = graph.getSure().getGraph();
        this.revSure = graph.getSure().getRevGraph();
        this.maybe = graph.getGlobal().getGraph();
        this.revMaybe = graph.getGlobal().getGraph();
        this.nbNodes = graph.getNbNodes();
        this.sink = graph.getGlobal().getSinkNodes();
        this.src = graph.getGlobal().getSrcNodes();
        this.propagateStruct = propagateStruct;
        this.nodes = nodes;
    }

    /**
     * filtering methods that update the time windows of each node according to the travel time matrix and the graph
     * <p/>
     * * @throws ContradictionException
     * @throws choco.kernel.solver.ContradictionException
     */
    public void applyTWfiltering() throws ContradictionException {
        updateInf();
        updateSup();
        updateByPotentialSucc();
        updateByPotentialPred();
    }

    /**
     * filtering method that removes the arcs which become infeasible because of the time windows constraints
     *
     * @throws ContradictionException
     */
    public void applyGraphFiltering() throws ContradictionException {
        for (int i = 0; i < nbNodes - 1; i++) {
            for (int j = maybe[i].nextSetBit(0); j >= 0; j = maybe[i].nextSetBit(j + 1)) {
                if (nodes[i].getTimeWindow().getInf() + travelTime[i][j].get() > nodes[j].getTimeWindow().getSup()) {//o[i].getInf() + travelTime[i][j].get() > o[j].getSup()) {
                    if (debugRem) {
                        LOGGER.info("-------------------------------------------------------------------");
                        LOGGER.info("Update[applyGraphFiltering] : twPropagation.TWConstraint for nodes " + i);
                        LOGGER.info("\tremove arc (" + i + "," + j + ") from Gmaybe : "+nodes[i].getTimeWindow().getInf() + "+" + travelTime[i][j] + " > " + nodes[j].getTimeWindow().getSup());
                        LOGGER.info("-------------------------------------------------------------------");
                    }
                    int[] arc = {i, j};
                    propagateStruct.addRemoval(arc);
                }
            }
        }
    }

    private void updateInf() throws ContradictionException {
        Queue<Integer> queue = new LinkedList<Integer>();
        BitSet reached = new BitSet(nbNodes);
        for (int i = src.nextSetBit(0); i >= 0; i = src.nextSetBit(i + 1)) {
            if (!reached.get(i)) queue.offer(i);
        }
        while (!queue.isEmpty()) {
            int i = queue.poll();
            reached.set(i, true);
            for (int j = sure[i].nextSetBit(0); j >= 0; j = sure[i].nextSetBit(j + 1)) {
                if (!reached.get(j)) queue.offer(j);
                int newVal = nodes[i].getTimeWindow().getInf() + travelTime[i][j].get();
                if (nodes[j].getTimeWindow().getInf() < newVal) propagateStruct.setMinStart(j, newVal);
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
            for (int i = revSure[j].nextSetBit(0); i >= 0; i = revSure[j].nextSetBit(i + 1)) {
                if (!reached.get(i)) {
                    queue.offer(i);
                    int newVal = nodes[j].getTimeWindow().getSup() - travelTime[i][j].get();
                    if (nodes[i].getTimeWindow().getSup() > newVal) propagateStruct.setMaxStart(i, newVal);
                }
            }
        }
    }

    public void updateByPotentialSucc() throws ContradictionException {
        for (int i = 0; i < nbNodes; i++) {
            if (!maybe[i].get(i)) {
                int newMax = 0;
                boolean update = false;
                for (int j = maybe[i].nextSetBit(0); j >= 0; j = maybe[i].nextSetBit(j + 1)) {
                    int val = nodes[j].getTimeWindow().getSup() - travelTime[i][j].get();
                    if (newMax < val) {
                        update = true;
                        newMax = val;
                    }
                }
                if ((newMax < nodes[i].getTimeWindow().getSup()) && update) {
                    if (debugRem) {
                        LOGGER.info("-------------------------------------------------------------------");
                        LOGGER.info("Update[updateByPotentialSucc] : twPropagation.TWConstraint for nodes " + i);
                        LOGGER.info("\t max start " + i + " = " + nodes[i].getTimeWindow().getSup() + " devient " + newMax);
                        LOGGER.info("-------------------------------------------------------------------");
                    }
                    propagateStruct.setMaxStart(i, newMax);
                }
            }
        }
    }

    public void updateByPotentialPred() throws ContradictionException {
        for (int j = 0; j < nbNodes; j++) {
            if (nodes[j].getInDegree().getInf() > 0) {
                int newMin = 2000000000;
                boolean update = false;
                for (int i = revMaybe[j].nextSetBit(0); i >= 0; i = revMaybe[j].nextSetBit(i + 1)) {
                    int val = nodes[i].getTimeWindow().getInf() + travelTime[i][j].get();
                    if (newMin > val) {
                        update = true;
                        newMin = val;
                    }
                }
                if ((newMin > nodes[j].getTimeWindow().getInf()) && update) {
                    if (debugRem) {
                        LOGGER.info("-------------------------------------------------------------------");
                        LOGGER.info("Update[updateByPotentialPred] : twPropagation.TWConstraint for nodes " + j);
                        LOGGER.info("\t min start " + j + " = " + nodes[j].getTimeWindow().getInf() + " devient " + newMin);
                        LOGGER.info("-------------------------------------------------------------------");
                    }
                    propagateStruct.setMaxStart(j, newMin);
                }
            }
        }
    }
}
