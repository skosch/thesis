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

package choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.algorithms;

import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.PrecsGraphView;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.VarGraphView;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IStateBitSet;

import java.util.BitSet;
import java.util.logging.Logger;

public class Dominators {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    protected int nbVertices;
    protected VarGraphView graph;
    protected PrecsGraphView precs;

    protected int[] color;
    protected int[] postOrder;
    protected int[] revPostOrder;
    protected BitSet visited;
    protected int time;

    protected BitSet tmp;

    boolean affiche = false;

    public Dominators(VarGraphView graph, PrecsGraphView precs) {
        this.graph = graph;
        this.precs = precs;
        this.nbVertices = graph.getNbNodes();
        this.tmp = new BitSet(nbVertices);
    }

    public BitSet[][] computeDominators() {
        BitSet[][] currentDoms = new BitSet[nbVertices][nbVertices];
        for (int i = 0; i < nbVertices; i++) {
            for (int j = 0; j < nbVertices; j++) {
                currentDoms[i][j] = new BitSet(nbVertices);
                for (int k = 0; k < nbVertices; k++) currentDoms[i][j].set(k, true);
            }
        }
        for (int i = 0; i < nbVertices; i++) currentDoms = computeDominatorTree(i, currentDoms);
        for (int i = 0; i < nbVertices; i++) {
            for (int j = 0; j < nbVertices; j++) {
                currentDoms[i][j].set(i, false);
                currentDoms[i][j].set(j, false);
            }
        }
        return currentDoms;
    }

    private void init(int source) {
        this.color = new int[nbVertices];
        this.postOrder = new int[nbVertices];
        this.revPostOrder = new int[nbVertices];
        this.time = -1;
        this.visited = new BitSet(nbVertices);
        for (int i = 0; i < nbVertices; i++) {
            this.color[i] = 0;
            this.postOrder[i] = -1;
            this.revPostOrder[i] = -1;
        }
        if (affiche) LOGGER.info("source = " + source);
        computePostOrder(source);
        if (affiche) {
            LOGGER.info("-----------------------------");
            for (int i = 0; i < revPostOrder.length; i++) {
                LOGGER.info("revPostOrder[" + i + "] = " + revPostOrder[i]);
            }
            LOGGER.info("-----------------------------");
        }
    }

    /*
     * "A simple, Fast Dominance Algorithm"
     * Keith D. Cooper, Timothy J. Harvey and Ken Kennedy
     * Software-Practice and Experience Journal 2001; 4:1-10
     */
    private BitSet[][] computeDominatorTree(int source, BitSet[][] currentDoms) {
        init(source);
        for (int i = 0; i < nbVertices; i++) {
            if (postOrder[i] == -1) {
                // i ne peut pas �tre atteint depuis source
                BitSet unreachable = currentDoms[source][i];
                for (int j = unreachable.nextSetBit(0); j >= 0; j = unreachable.nextSetBit(j + 1))
                    unreachable.set(j, false);
            }
        }
        boolean changed = true;
        BitSet computed = new BitSet(nbVertices);
        while (changed) {
            changed = false;
            if (affiche) LOGGER.info("une boucle: ");
            for (int i = nbVertices - 1; i > -1; i--) {
                int node = revPostOrder[i];
                if (affiche) LOGGER.info("au temps " + i + " on a visite " + node);
                if (node > -1) {
                    if (affiche)
                        LOGGER.info("\t On cherche les dominants de " + node + " par rapport � " + source + ": ");
                    computed.set(node, true);
                    BitSet newSet = new BitSet(nbVertices);
                    // at the begin, node is dominator of node
                    newSet.set(node, true);
                    // the intersection of the dominators of the predecessors of node in graph is computed
                    BitSet previous = (BitSet) currentDoms[source][node].clone();
                    BitSet intersect = (BitSet) currentDoms[source][node].clone();
                    // on prend en compte les pr�d�cesseurs obligatoires de node :: donn�s par precs
                    tmp = precs.getAncestors(node);
                    tmp.and(precs.getDescendants(source));
                    intersect.or(tmp);
                    // on met jour par rapport � chaque pred de node dans le graphe
                    IStateBitSet preds = graph.getGlobal().getPredecessors(node);
                    boolean atLeastOne = false;
                    boolean atLeastOneComp = false;
                    if (affiche) LOGGER.info("\t\t au moins un pred pour " + node + "? ");
                    for (int j = preds.nextSetBit(0); j >= 0; j = preds.nextSetBit(j + 1)) {
                        if (j != node) {
                            if (affiche) LOGGER.info("oui");
                            atLeastOne = true;
                            if (computed.get(j)) {
                                if (affiche)
                                    LOGGER.info("[update with dom[" + j + "]=" + currentDoms[source][j] + "] ");
                                intersect.and(currentDoms[source][j]);
                                atLeastOneComp = true;
                            }
                        }
                    }
                    if (!atLeastOne) {
                        // add the previous dominators of node in newSet
                        if (affiche) LOGGER.info("non");
                        for (int j = previous.nextSetBit(0); j >= 0; j = previous.nextSetBit(j + 1)) {
                            if (j != node) newSet.set(j, false);
                        }
                    } else {
                        if (atLeastOneComp) newSet.or(intersect);
                    }
                    if (affiche) LOGGER.info("");
                    if (affiche) LOGGER.info("\t\t candidats = " + newSet.toString());
                    for (int j = previous.nextSetBit(0); j >= 0; j = previous.nextSetBit(j + 1)) {
                        if (!newSet.get(j)) changed = true;
                    }
                    for (int j = newSet.nextSetBit(0); j >= 0; j = newSet.nextSetBit(j + 1)) {
                        if (!previous.get(j)) changed = true;
                    }
                    // if the initial set of dominators of node is modified according to newSet, then dominator[source][node] is updated
                    if (changed) {
                        if (affiche)
                            LOGGER.info("\t\t dominants de " + node + " par rapport � " + source + " = " + newSet.toString());
                        currentDoms[source][node] = newSet;
                        /*for (int j = 0; j < nbVertices; j++) {
                            if (newSet.get(j)) {
                                currentDoms[source][node].set(j, true);
                            } else {
                                currentDoms[source][node].set(j, false);
                            }
                        }*/
                        if (affiche) LOGGER.info("");
                    }
                }
            }
            if (affiche) LOGGER.info("------------------------------------------------------------");
        }
        return currentDoms;
    }

    public void computePostOrder(int u) {
        BitSet src = new BitSet(nbVertices);
        src.set(u, true);
        for (int i = src.nextSetBit(0); i >= 0; i = src.nextSetBit(i + 1)) {
            if (color[i] == 0) getPostOrder(i);
        }
    }

    public void getPostOrder(int u) {
        color[u] = 1;
        visited.set(u, true);
        IStateBitSet succ = graph.getGlobal().getSuccessors(u);
        for (int k = succ.nextSetBit(0); k >= 0; k = succ.nextSetBit(k + 1)) {
            if (color[k] == 0) getPostOrder(k);
        }
        color[u] = 2;
        time++;
        postOrder[u] = time;
        revPostOrder[time] = u;
    }
}
