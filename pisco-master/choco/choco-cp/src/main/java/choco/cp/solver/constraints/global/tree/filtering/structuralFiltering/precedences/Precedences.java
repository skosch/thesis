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

package choco.cp.solver.constraints.global.tree.filtering.structuralFiltering.precedences;


import choco.cp.solver.constraints.global.tree.filtering.AbstractPropagator;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.solver.ContradictionException;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Vector;


public class Precedences extends AbstractPropagator {

    public Precedences(Object[] params) {
        super(params);
    }

    public String getTypePropag() {
        return "Precedences propagation";
    }

    /**
     * check the compatibility between the graph and the precedence constraints
     *
     * @return <code> true </code> iff the precedence constraints are consistent with the graph 
     */
    public boolean feasibility() {
        // For any node u in the graph, the descendants of u in the precedence graph belong to the descendants
        // of u in the graph.
        int m = 0;
        boolean res = true;
        while (m < nbVertices && res) {
            BitSet desc = precs.getDescendants(m);
            IStateBitSet desc_m = inputGraph.getGlobal().getDescendants(m);
            for (int i = desc.nextSetBit(0); i >= 0; i = desc.nextSetBit(i + 1)) {
                if (!desc_m.get(i)) res = false;
            }
            m++;
        }
        if (!res) {
            int v = m - 1;
            if (affiche) {
                LOGGER.info("2- Violation hasse: pour " + v + " dans V, les " +
                        precs.showDesc(v) + " ne sont pas dans les " + inputGraph.getGlobal().showDesc(v, "G"));
            }
            return false;
        }
        for (int i = 0; i < nbVertices; i++) {
            // node i is a fixed potential root then we have to check its compatibility
            // with the precedence constraints
            if (inputGraph.getSure().getSuccessors(i).get(i)) {
                // any fixed potential root is a sink in the precedence graph
                if (!precs.getSinkNodes().get(i)) {
                    if (affiche) LOGGER.info("3- Violation hasse");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * filtering rules related to the interaction between the graph to partition and the precedence constraints.
     * For details, see the Constraint'08 paper.
     *
     * @throws choco.kernel.solver.ContradictionException
     */
    public void filter() throws ContradictionException {
        // Analyze each potential arc (u,v) according to the precedence constraints
        for (int i = 0; i < nbVertices; i++) {
            IStateBitSet possible = inputGraph.getMaybe().getSuccessors(i);
            for (int j = possible.nextSetBit(0); j >= 0; j = possible.nextSetBit(j + 1)) {
                int[] arc = {i, j};
                if (i != j) {
                    if (affiche)
                        LOGGER.info("test arc (" + i + "," + j + ") ");
                    if (!isCompatible(arc)) {
                        if (affiche)
                            LOGGER.info("Precedences-0: suppression de l'arc (" + i + "," + j + ")");
                        propagateStruct.addRemoval(arc);
                    }
                } else {
                    if (precs.getSuccessors(i).cardinality() > 0) {
                        if (affiche)
                            LOGGER.info("Precedences-1: suppression de l'arc (" + i + "," + j + ")");
                        propagateStruct.addRemoval(arc);
                    }
                }
            }
        }
        // all the mandatory descendants desc_i of a node i belong to the potential descendants
        // desc_j of a succesor j of i in the graph 
        for (int i = 0; i < nbVertices; i++) {
            IStateBitSet succ_i = inputGraph.getGlobal().getSuccessors(i);
            BitSet desc_i = precs.getDescendants(i);
            for (int j = succ_i.nextSetBit(0); j >= 0; j = succ_i.nextSetBit(j + 1)) {
                if (i != j) {
                    BitSet desc_j = inputGraph.getGlobal().getDescendants(j).copyToBitSet();
                    if (affiche) {
                        LOGGER.info("Pdesc_" + i + " = " + desc_i.toString());
                        LOGGER.info("Mdesc_" + j + " = " + desc_j.toString());
                    }
                    desc_j.and(desc_i);
                    desc_j.set(i, false);
                    if (affiche)
                        LOGGER.info("inter = " + desc_i.toString());
                    if (desc_j.cardinality() < desc_i.cardinality() - 1) {
                        if (affiche) {
                            LOGGER.info("inter[" + i + "," + j + "] = " + desc_j.toString());
                            LOGGER.info("Precedences-2: suppression de l'arc (" + i + "," + j + ")");
                        }
                        int[] arc = {i, j};
                        propagateStruct.addRemoval(arc);
                    }
                    if (affiche) LOGGER.info("------------------------------");
                }
            }
        }
        // remove all the potential roots which are not a sink node in the precedence graph
        IStateBitSet roots = inputGraph.getPotentialRoots();
        for (int i = roots.nextSetBit(0); i >= 0; i = roots.nextSetBit(i + 1)) {
            if (!precs.getSinkNodes().get(i)) {
                if (affiche) LOGGER.info("Precedences-3: suppression boucle sur " + i);
                int[] arc = {i, i};
                propagateStruct.addRemoval(arc);
            }
        }
        // for each source node s in the precedence graph, there exist a path, in the reduced graph,
        // containing all the descendants of s in the precedence graph.
        BitSet sources = precs.getSrcNodes().copyToBitSet();
        BitSet[] markedH = new BitSet[nbVertices];
        for (int i = 0; i < nbVertices; i++) {
            markedH[i] = new BitSet(nbVertices);
        }
        for (int i = sources.nextSetBit(0); i >= 0; i = sources.nextSetBit(i + 1)) {
            for (int j = precs.getDescendants(i).nextSetBit(0); j >= 0; j = precs.getDescendants(i).nextSetBit(j + 1)) {
                markedH[j].set(i, true);
            }
        }
        // a topological sort of the precedence graph
        Vector<IStateBitSet> CFC = inputGraph.getReducedGraph().getCFC();
        TopologicSort ts = new TopologicSort(inputGraph.getReducedGraph().getCFCgraph());
        IStateBitSet[] reducedGraph = inputGraph.getReducedGraph().getCFCgraph();
        int[] numTable = ts.sort();
        // record the nodes of the graph that belong to a node of the reduced graph. NB: indeed, a reduced node is
        // a scc of the graph.
        BitSet[] containG_r = new BitSet[reducedGraph.length];
        for (int i = 0; i < containG_r.length; i++) {
            containG_r[i] = new BitSet(nbVertices);
            containG_r[i] = CFC.elementAt(i).copyToBitSet();
        }
        BitSet[] markedG_r = new BitSet[reducedGraph.length];
        for (int i = 0; i < reducedGraph.length; i++) {
            markedG_r[i] = new BitSet(nbVertices);
            for (int j = containG_r[i].nextSetBit(0); j >= 0; j = containG_r[i].nextSetBit(j + 1)) {
                markedG_r[i].or(markedH[j]);
            }
        }
        // DFS from each scc containing a source node of the precedence graph
        BitSet[] falseReducedGraph = new BitSet[reducedGraph.length];
        for (int i = 0; i < falseReducedGraph.length; i++) falseReducedGraph[i] = new BitSet(falseReducedGraph.length);
        while (sources.cardinality() > 0) {
            int currentSRC = -1;
            int start = -1;
            for (int i = 0; i < reducedGraph.length; i++) {
                for (int j = containG_r[i].nextSetBit(0); j >= 0; j = containG_r[i].nextSetBit(j + 1)) {
                    if (sources.get(j)) {
                        currentSRC = j;
                        sources.set(currentSRC, false);
                    }
                }
                start = i;
            }
            if (start == -1 || currentSRC == -1) {
                LOGGER.info("PROBLEME: treeConstraint.filtering.structuralFiltering.precedences.Precedences (line 209)");
                return;
            }
            // start a first DFS
            int[] numPre = DFS(start, reducedGraph, numTable, markedG_r, currentSRC);
            // remove any arc (u,v) of the reduced graph such that there exists a node w with
            // numPre[u] < numPre[w] < numPre[v] and u,v,w are tagged by currentSRC.
            for (int u = 0; u < reducedGraph.length; u++) {
                for (int v = reducedGraph[u].nextSetBit(0); v >= 0; v = reducedGraph[u].nextSetBit(v + 1)) {
                    // (u,v) is a transitive arc of the precedence graph
                    if (numPre[u] < numPre[v] + 1) {
                        for (int w = reducedGraph[u].nextSetBit(0); w >= 0; w = reducedGraph[u].nextSetBit(w + 1)) {
                            // there exists w such that w is between u and v, w is tagged
                            if (numPre[w] < numPre[v] && markedG_r[w].get(currentSRC)) {
                                falseReducedGraph[u].set(v, false);
                            }
                        }
                    }
                }
            }
        }
        // restore the set of arcs in the graph corresponding to an arc in the reduced graph
        for (int u = 0; u < falseReducedGraph.length; u++) {
            for (int v = falseReducedGraph[u].nextSetBit(0); v >= 0; v = falseReducedGraph[u].nextSetBit(v + 1)) {
                IStateBitSet contain_u = CFC.elementAt(u);
                IStateBitSet contain_v = CFC.elementAt(v);
                for (int i = contain_u.nextSetBit(0); i >= 0; i = contain_u.nextSetBit(i + 1)) {
                    IStateBitSet succ_i = inputGraph.getGlobal().getSuccessors(i);
                    for (int j = succ_i.nextSetBit(0); j >= 0; j = succ_i.nextSetBit(j + 1)) {
                        if (contain_v.get(j) && !propagateStruct.getGraphRem()[i].get(j)) {
                            if (affiche)
                                LOGGER.info("Precedences-5: suppression de l'arc (" + i + "," + j + ")");
                            int[] arc = {i, j};
                            propagateStruct.addRemoval(arc);
                        }
                    }
                }
            }
        }
        // record the connected components of the precedence graph
        BitSet[] undirectedHasse = new BitSet[nbVertices];
        for (int i = 0; i < nbVertices; i++) undirectedHasse[i] = new BitSet(nbVertices);
        for (int i = 0; i < nbVertices; i++) {
            IStateBitSet u = precs.getSuccessors(i);
            for (int j = u.nextSetBit(0); j >= 0; j = u.nextSetBit(j + 1)) {
                undirectedHasse[i].set(j, true);
                undirectedHasse[j].set(i, true);
            }
        }
        IStateBitSet[] vertFromNum = precs.getVertFromNumCC();
        BitSet out = new BitSet(vertFromNum.length);
        for (int i = 0; i < vertFromNum.length; i++) {
            IStateBitSet vertices = vertFromNum[i];
            boolean exists = false;
            for (int j = vertices.nextSetBit(0); j >= 0; j = vertices.nextSetBit(j + 1)) {
                IStateBitSet succ = precs.getSuccessors(j);
                boolean b = true;
                for (int k = succ.nextSetBit(0); k >= 0; k = succ.nextSetBit(k + 1)) {
                    if (vertices.get(k)) b = false;
                }
                if (b) exists = true;
            }
            if (!exists) out.set(i, true);
        }
        int sum_out = out.cardinality();
        int maxtree = vertFromNum.length - sum_out;
        int oldmax = tree.getNtree().getSup();
        if (oldmax > maxtree && tree.getNtree().getSup() > maxtree) {
            if (affiche)
                LOGGER.info("Precedences: updateSup ntree = " + tree.getNtree().getSup() + " ==> " + maxtree);
            propagateStruct.setMaxNtree(maxtree);
        }
    }

    /**
     * check the compatible of a given arc with the precedence constraints
     *
     * @param arc
     * @return <code> true </code> iff arc is compatible with the precedence constraints
     */
    private boolean isCompatible(int[] arc) {
        boolean cycle = false;
        boolean transitive = false;
        int src = arc[0];
        int dest = arc[1];
        if (src == dest) {
            if (affiche) LOGGER.info("\t(" + src + "," + dest + ") incompatible dans Gp");
            return false;
        }
        // first DFS checks the arc to add does not create a circuit in the precedence graph
        BitSet desc_1 = precs.getDescendants(dest);
        if (affiche)
            LOGGER.info("desc_" + dest + " = " + desc_1.toString());
        if (desc_1.get(src)) cycle = true;
        if (!cycle) {
            // second dfs checks the arc to add is not transitive in the precedence graph
            BitSet desc_2 = precs.getDescendants(src);
            if (desc_2.get(dest) && !precs.getSuccessors(src).get(dest)) {
                if (affiche)
                    LOGGER.info("\ttransitif");
                transitive = true;
            }
            return !transitive;
        } else {
            if (affiche) 
                LOGGER.info("\tcycle");
            return false;
        }
    }

    private int[] DFS(int start, IStateBitSet[] graph, int[] sortLVL, BitSet[] marked, int origin) {
        int cpt = 0;
        int[] numPre = new int[graph.length];
        for (int i = 0; i < numPre.length; i++) numPre[i] = -1;
        LinkedList<Integer> toVisit = new LinkedList<Integer>();
        LinkedList<Integer> visited = new LinkedList<Integer>();
        visited.offer(start);
        toVisit.addFirst(start);
        while (toVisit.size() != 0) {
            int current = toVisit.poll();
            numPre[current] = cpt;
            cpt++;
            IStateBitSet succ = graph[current];
            // record the nodes of low level in the topological sort
            BitSet minlvl = new BitSet(graph.length);
            int lvl = graph.length;
            for (int i = succ.nextSetBit(0); i >= 0; i = succ.nextSetBit(i + 1)) {
                if (!visited.contains(i) && sortLVL[i] < lvl) {
                    minlvl.clear();
                    minlvl.set(i, true);
                    lvl = sortLVL[i];
                } else {
                    if (!visited.contains(i) && sortLVL[i] == lvl) minlvl.set(i, true);
                }
            }
            // nodes of low level, tagged by origin, are processed first...
            for (int i = minlvl.nextSetBit(0); i >= 0; i = minlvl.nextSetBit(i + 1)) {
                if (marked[i].get(origin)) {
                    visited.offer(i);
                    toVisit.addFirst(i);
                    minlvl.set(i, false);
                }
            }
            // next, the other.
            for (int i = minlvl.nextSetBit(0); i >= 0; i = minlvl.nextSetBit(i + 1)) {
                visited.offer(i);
                toVisit.addFirst(i);
            }
        }
        return numPre;
    }
}
