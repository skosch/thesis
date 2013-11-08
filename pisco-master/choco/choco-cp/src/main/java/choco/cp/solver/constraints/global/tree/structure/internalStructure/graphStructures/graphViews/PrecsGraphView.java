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

package choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews;

import choco.cp.solver.constraints.global.tree.structure.inputStructure.Node;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.solver.Solver;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Logger;

public class PrecsGraphView {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * boolean for debug and show a trace of the execution
     */
    protected boolean affiche = false;

    /**
     * Choco solver embedding the tree constraint
     */
    protected Solver solver;

    /**
     * list of graph properties to maintain for the precedence graph
     */
    protected List<StoredBitSetGraph.Maintain> precsParams;

    /**
     * total number of nodes involved in the graph
     */
    protected int nbNodes;

    /**
     * data structure of the precedence graph
     */
    protected StoredBitSetGraph precs;

    /**
     * backtrackable bitset matrix representing the precedence graph
     */
    protected IStateBitSet[] precsGraph;

    /**
     * 
     * @param solver    the Choco solver who uses the current tree constraint
     * @param nodes     total number of nodes involved in the graph
     */
    public PrecsGraphView(Solver solver, Node[] nodes) {
        this.solver = solver;
        this.nbNodes = nodes.length;
        // announce the properties maintained
        this.precsParams = new ArrayList<StoredBitSetGraph.Maintain>();
        this.precsParams.add(StoredBitSetGraph.Maintain.TRANSITIVE_CLOSURE);
        this.precsParams.add(StoredBitSetGraph.Maintain.TRANSITIVE_REDUCTION);
        this.precsParams.add(StoredBitSetGraph.Maintain.CONNECTED_COMP);
        this.precsGraph = new IStateBitSet[nbNodes];
        for (int i = 0; i < nbNodes; i++) this.precsGraph[i] = nodes[i].getMandatorySuccessors();
        // create the precedence graph associated with the precedence constraints
        this.precs = new StoredBitSetGraph(solver, this.precsGraph, this.precsParams, false);
    }

    /**
     * incrementaly add the arc (u,v) in the precedence graph
     *
     * @param u     index of a node
     * @param v     index of a node
     * @return  <code> true </code> iff the arc (u,v) is effectively added in the precedence graph
     */
    public boolean addPrec(int u, int v) {
        if (affiche) {
        	ChocoLogging.flushLogs();
            LOGGER.info("============= Add Incr�mental : (" + u + "," + v + ") ================");
            precs.showGraph("precs");
            LOGGER.info("---------------");
            precs.showAllDesc("tcPrecs");
            LOGGER.info("**********************");
        }
        boolean res = false;
        if (u != v) {
            if (!precs.getDescendants(u).get(v)) {
                if (affiche)
                    LOGGER.info("\t\t(" + u + "," + v + ") est ajoute dans Gp!");
                precs.addArc(u, v);
                // transitive reduction become the current view of the precedence graph
                this.precsGraph = this.precs.getTrGraph();
                res = true;
            } else {
                if (affiche) LOGGER.info("\t\t(" + u + "," + v + ") a deja ete ajoute dans Gp");
                res = false;
            }
        }
        if (affiche) {
            precs.showGraph("precs");
            LOGGER.info("---------------");
            precs.showAllDesc("tcPrecs");
            LOGGER.info("============= END Add Incr�mental ================");
        }
        return res;
    }

    public StoredBitSetGraph getPrecs() {
        return precs;
    }

    public IStateBitSet getSuccessors(int i) {
        return precs.getSuccessors(i);
    }

    public IStateBitSet getPredecessors(int i) {
        return precs.getPredecessors(i);
    }

    public BitSet getDescendants(int i) {
        return hardCopy(precs.getDescendants(i));
    }

    public BitSet getAncestors(int i) {
        return hardCopy(precs.getAncestors(i));
    }

    public IStateBitSet getSinkNodes() {
        return precs.getSinkNodes();
    }

    public IStateBitSet getSrcNodes() {
        return precs.getSrcNodes();
    }

    public IStateBitSet[] getVertFromNumCC() {
        return precs.getVertFromNumCC();
    }

    public IStateBitSet[] getNumFromVertCC() {
        return precs.getNumFromVertCC();
    }

    public String showDesc(int i) {
        return precs.showDesc(i, "descPrecs");
    }

    public void showPrecGraph() {
        precs.showGraph("precs");
    }

    public void showAllDesc() {
        precs.showAllDesc("descPrecs");
    }

    private BitSet hardCopy(IStateBitSet b) {
        BitSet bs = new BitSet(nbNodes);
        for (int i = b.nextSetBit(0); i >= 0; i = b.nextSetBit(i + 1)) {
            bs.set(i, true);
        }
        return bs;
    }

}
