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

package choco.cp.solver.constraints.global.tree.structure.internalStructure.costStrutures;

import choco.cp.solver.constraints.global.tree.structure.inputStructure.TreeParameters;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.VarGraphView;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.Solver;

import java.util.logging.Logger;

public class CostStructure {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * boolean for debug and show a trace of the execution
     */
    protected boolean affichecosts = false;

    /**
     * total number of nodes involved in the graph
     */
    protected int nbNodes;

    /**
     * Choco solver embedding the tree constraint
     */
    protected Solver solver;

    /**
     * data structure related to the graph representation of the input graph
     */
    protected VarGraphView inputGraph;

    /**
     * a shortest path structure of the graph to partition
     */
    protected ShortestPaths path;

    /**
     * cost matrix associated with the graph to partition. Actually, this matrix corresponds to the travel time one.
     */
    protected IStateInt[][] cost;

    /**
     * backtrackable shortest path matrix
     */
    protected IStateInt[][] minCost;

    /**
     * backtrackable integer that record the cost of a forest cover of the graph to partition
     */
    protected IStateInt forestCost;

    /**
     * minimum over cost induced by the outgoing arcs for each sink node of the required graph
     */
    protected IStateInt[] deltaCost;

    /**
     * Constructor of the costStructure
     *
     * @param solver    the Choco solver who uses the current tree constraint
     * @param tree  the input data structure available in the <code> structure.inputStructure </code> package
     * @param inputGraph    data structure related to the graph representation of the input graph
     */
    public CostStructure(Solver solver, TreeParameters tree, VarGraphView inputGraph) {
        this.solver = solver;
        this.nbNodes = inputGraph.getNbNodes();
        this.inputGraph = inputGraph;
        this.cost = tree.getTravelTime();
        this.minCost = new IStateInt[nbNodes][nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) this.minCost[i][j] = solver.getEnvironment().makeInt(0);
        }
        this.path = new ShortestPaths(nbNodes, this.cost, inputGraph, this.minCost);
        this.forestCost = solver.getEnvironment().makeInt(Integer.MAX_VALUE);
        this.deltaCost = new IStateInt[nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            this.deltaCost[i] = solver.getEnvironment().makeInt(0);
        }
    }

    public IStateInt[][] getCost() {
        return cost;
    }

    public IStateInt[][] getMinCost() {
        return minCost;
    }

    public IStateInt getForestCost() {
        return forestCost;
    }

    public IStateInt[] getDeltaCost() {
        return deltaCost;
    }

    /**
     * update the cost structure according to the current state of the graph to partition
     */
    public void updateCostStruct() {
        // update the shortest path structure
        this.path.computeMinPaths();
        // compute the cost of a forest covering of the required arcs
        IStateBitSet[] vertFromNumGt = inputGraph.getSure().getVertFromNumCC();
        if (affichecosts) {
            for (int i = 0; i < nbNodes; i++) {
                LOGGER.info("cc_" + i + " = " + vertFromNumGt[i].toString());
            }
            LOGGER.info("- - - - - - - - - - - -");
            for (int i = 0; i < nbNodes; i++) {
                LOGGER.info("gt[" + i + "] = " + inputGraph.getSure().getSuccessors(i));
            }
            LOGGER.info("-------------------------------");
        }
        int[] connexComponentCosts = new int[nbNodes];
        int sinkOf = -1;
        int total = 0;
        for (int i = 0; i < nbNodes; i++) {
            connexComponentCosts[i] = 0;
            if (vertFromNumGt[i].nextSetBit(0) > -1) {
                // the cost of a connected component (cc) i in the graph of required arcs
                for (int j = vertFromNumGt[i].nextSetBit(0); j >= 0; j = vertFromNumGt[i].nextSetBit(j + 1)) {
                    if (inputGraph.isFixedSucc(j)) {
                        int val = inputGraph.getSure().getSuccessors(j).nextSetBit(0);
                        if (vertFromNumGt[i].get(val)) {
                            if (inputGraph.getPotentialRoots().get(j)) sinkOf = j;
                            connexComponentCosts[i] += cost[j][val].get();
                        } else sinkOf = j;
                    } else sinkOf = j;
                }
                if (sinkOf == -1) sinkOf = vertFromNumGt[i].nextSetBit(0);
                deltaCost[i].set(getMinOutGoingArc(sinkOf));
                if (affichecosts) {
                    LOGGER.info("\t\tconnexCompCosts[" + i + "] = " + connexComponentCosts[i]);
                    LOGGER.info("\t\tdeltaCosts[" + i + "] = " + deltaCost[i].get());
                }
                total = total + connexComponentCosts[i] + deltaCost[i].get();
            }
        }
        forestCost.set(total);
        if (affichecosts) {
            LOGGER.info("total cost = " + forestCost.get());
            LOGGER.info("-------------------------------");
        }
    }

    /**
     * compute the minimum outgoing arc from node sink in the graph of potential arcs
     *
     * @param sink  a node that corresponds to a successor variable not yet fixed
     * @return  the index of the successor of minimum cost from sink
     */
    private int getMinOutGoingArc(int sink) {
        int minSucc = Integer.MAX_VALUE;
        if (inputGraph.isFixedSucc(sink) && inputGraph.getPotentialRoots().get(sink)) {
            minSucc = cost[sink][sink].get();
        } else {
            IStateBitSet maybe = inputGraph.getMaybe().getSuccessors(sink);
            for (int i = maybe.nextSetBit(0); i >= 0; i = maybe.nextSetBit(i + 1)) {
                if (cost[sink][i].get() < minSucc) minSucc = cost[sink][i].get();
            }
        }
        return minSucc;
    }
}
