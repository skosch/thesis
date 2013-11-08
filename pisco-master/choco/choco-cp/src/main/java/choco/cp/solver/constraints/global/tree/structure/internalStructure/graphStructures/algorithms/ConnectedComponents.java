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

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.solver.Solver;

import java.util.BitSet;
import java.util.Vector;
import java.util.logging.Logger;

public class ConnectedComponents {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    protected boolean affiche;

    protected Solver solver;

    protected IStateBitSet[] graph;

    protected int nbNodes;
    protected BitSet[] undirected;
    protected Vector<IStateBitSet> cc;
    protected int nbCC;

    protected int[] color;
    protected BitSet reached;

    public ConnectedComponents(Solver solver, int nbNodes, IStateBitSet[] graph,
                               Vector<IStateBitSet> cc) {
        this.solver = solver;
        this.nbNodes = nbNodes;
        this.graph = graph;
        this.cc = cc;
        this.color = new int[nbNodes];
        undirected = new BitSet[nbNodes];
    }

    private void update() {
        this.nbCC = 0;
        this.color = new int[nbNodes];
        this.reached = new BitSet(nbNodes);
        getUndirectedGraph();
        if (affiche) showGraph();
        for (int i = 0; i < nbNodes; i++) cc.elementAt(i).clear();
    }

    private void showGraph() {
        for (int i = 0; i < nbNodes; i++) {
            IStateBitSet contain = graph[i];
            LOGGER.info("sure[" + i + "] = "+contain.toString());
        }
        for (int i = 0; i < nbNodes; i++) {
            BitSet contain = undirected[i];
            LOGGER.info("undirected[" + i + "] = "+contain.toString());
        }
        LOGGER.info("**************************");
    }

    private void getUndirectedGraph() {
        for (int i = 0; i < nbNodes; i++) undirected[i] = new BitSet(nbNodes);
        for (int v = 0; v < nbNodes; v++) {
            for (int j = graph[v].nextSetBit(0); j >= 0; j = graph[v].nextSetBit(j + 1)) {
                if (v != j) {
                    undirected[v].set(j, true);
                    undirected[j].set(v, true);
                }
            }
        }
    }

    public void getConnectedComponents(boolean b) {
        affiche = b;
        update();
        /*if (affiche) {
            showGraph();
            LOGGER.info("----------------");
        }*/
        for (int i = 0; i < nbNodes; i++) color[i] = 0;
        int u = existsUnVisited();
        while (u != -1) {
            reached.set(u,true);
            if (affiche) LOGGER.info("cc[" + u + "] = ");
            dfsVisit(u);
            IStateBitSet toModif = cc.remove(u);
            toModif.clear();
            for (int j = reached.nextSetBit(0); j >= 0; j = reached.nextSetBit(j + 1)) toModif.set(j,true);
            cc.insertElementAt(toModif, u);
            reached.clear();
            //for (int j = reached.nextSetBit(0); j >= 0; j = reached.nextSetBit(j + 1)) reached.set(j, false);
            u = existsUnVisited();
            nbCC++;
        }
        if (affiche) LOGGER.info("----------------");
    }

    private void convertToStored(Vector<BitSet> vb) {
        for (int i = 0; i < nbNodes; i++) {
            BitSet b = vb.elementAt(i);
            IStateBitSet c = cc.elementAt(i);
            c.clear();
            for (int j = b.nextSetBit(0); j >= 0; j = b.nextSetBit(j + 1)) c.set(j,true);
        }
    }

    private void dfsVisit(int u) {
        if (affiche) LOGGER.info(u + " ");
        color[u] = 1;
        reached.set(u, true);
        BitSet adj = undirected[u];
        for (int v = adj.nextSetBit(0); v >= 0; v = adj.nextSetBit(v + 1)) {
            if (color[v] == 0) dfsVisit(v);
        }
    }

    // choix d'un sommet parmi les possibles
    protected int existsUnVisited() {
        for (int i = 0; i < nbNodes; i++) {
            if (color[i] == 0) return i;
        }
        return -1;
    }

    public int getNbCC() {
        return nbCC;
    }
}
