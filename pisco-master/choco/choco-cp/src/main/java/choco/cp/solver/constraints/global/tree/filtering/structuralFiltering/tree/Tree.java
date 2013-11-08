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

package choco.cp.solver.constraints.global.tree.filtering.structuralFiltering.tree;


import choco.cp.solver.constraints.global.tree.filtering.AbstractPropagator;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;
import java.util.Vector;


public class Tree extends AbstractPropagator {

    public Tree(Object[] params) {
        super(params);
    }

    public String getTypePropag() {
        return "Tree propagation";
    }

    /**
     * Necessary and sufficient condition for the <b> pure tree </b> constraint. The checkout is made with
     * the reduced graph associated with the graph to partition:
     * <blockquote> 1- intersection{dom(ntree),[MINTREE,MAXTREE]} != emptySet; </blockquote>
     * <blockquote> 2- the sinks of the ReducedGraph involve at least one node the graph which is a
     * potential root. </blockquote>
     */
    public boolean feasibility() {
        // find sink strongly connected components (scc)
        Vector<IStateBitSet> cfc = inputGraph.getReducedGraph().getCFC();
        IStateBitSet[] reducedGraph = inputGraph.getReducedGraph().getCFCgraph();
        BitSet sink = new BitSet(nbVertices);
        for (int i = 0; i < reducedGraph.length; i++) {
            if (reducedGraph[i].cardinality() == 0) sink.set(i, true);
        }
        // the number of potential roots is synchronized with ntree
        int realLoop = 0;
        int mintree;
        IStateBitSet potentialRoots = inputGraph.getPotentialRoots();
        for (int i = potentialRoots.nextSetBit(0); i >= 0; i = potentialRoots.nextSetBit(i + 1)) {
            if (inputGraph.getSure().getGraph()[i].get(i)) realLoop++;
        }
        if (realLoop < sink.cardinality())
            mintree = sink.cardinality();
        else
            mintree = realLoop;
        // the maximum number of potential roots is synchronized with ntree
        int maxtree = potentialRoots.cardinality();
        boolean C1 = false;
        for (int i = mintree; i <= maxtree; i++) {
            if (tree.getNtree().canBeInstantiatedTo(i)) C1 = true;
        }
        if (C1) {
            // each sink scc contains at least one potential root
            int nb = 0;
            if (affiche) LOGGER.info("|SinkSCC| = " + sink.cardinality() + " -VS- |cfc| = " + cfc.size());
            for (int i = sink.nextSetBit(0); i >= 0; i = sink.nextSetBit(i + 1)) {
                boolean loop = false;
                IStateBitSet cont = cfc.elementAt(i);
                for (int j = cont.nextSetBit(0); j >= 0; j = cont.nextSetBit(j + 1)) {
                    if (potentialRoots.get(j)) loop = true;
                }
                if (loop) nb++;
            }
            if (nb == sink.cardinality()) {
                return true;
            } else {
                if (affiche)
                    LOGGER.info("1- tree: violation de la contrainte tree");
                return false;
            }
        } else {
            if (affiche)
                LOGGER.info("2- tree: violation de la contrainte tree");
            return false;
        }
    }

    /**
     * Filtering method for the <b> pure tree </b> constraint. Algorithmics details are provided in the CPAIOR'05 paper. 
     */
    public void filter() throws ContradictionException {
        Vector<IStateBitSet> cfc = inputGraph.getReducedGraph().getCFC();
        IStateBitSet[] reducedGraph = inputGraph.getReducedGraph().getCFCgraph();
        BitSet sink = new BitSet(reducedGraph.length);
        for (int i = 0; i < reducedGraph.length; i++) {
            if (reducedGraph[i].cardinality() == 0) sink.set(i, true);
        }
        int mintree = sink.cardinality();
        int maxtree = 0;
        for (int m = 0; m < nbVertices; m++) if (nodes[m].getSuccessors().canBeInstantiatedTo(m)) maxtree++;
        // update lower and upper bounds of ntree
        if (tree.getNtree().getSup() > maxtree) {
            if (affiche)
                LOGGER.info("treeConstraint.filtering.structuralFiltering.tree.Tree: updateSup ntree = " + tree.getNtree().getSup() + " ==> " + maxtree);
            propagateStruct.setMaxNtree(maxtree);
        }
        if (tree.getNtree().getInf() < mintree) {
            if (affiche)
                LOGGER.info("treeConstraint.filtering.structuralFiltering.tree.Tree: updateInf ntree = " + tree.getNtree().getInf() + " ==> " + mintree);
            propagateStruct.setMinNtree(mintree);
        }
        // detect a sink scc with one single potential root
        for (int m = sink.nextSetBit(0); m >= 0; m = sink.nextSetBit(m + 1)) {
            IStateBitSet b = cfc.elementAt(m);
            int occurs = 0;
            int name = -1;
            for (int j = b.nextSetBit(0); j >= 0; j = b.nextSetBit(j + 1)) {
                if (nodes[j].getSuccessors().canBeInstantiatedTo(j)) {
                    name = j;
                    occurs++;
                }
            }
            // a sink scc with a single potential root
            if (occurs == 1) {
                DisposableIntIterator it = nodes[name].getSuccessors().getDomain().getIterator();
                int[] toRem = new int[nodes[name].getSuccessors().getDomainSize()];
                for (int j = 0; j < toRem.length; j++) toRem[j] = -1;
                int j = 0;
                while (it.hasNext()) {
                    int k = it.next();
                    if (k != name) {
                        toRem[j] = k;
                        j++;
                    }
                }
                it.dispose();
                j = 0;
                while (toRem[j] != -1) {
                    if (nodes[name].getSuccessors().canBeInstantiatedTo(toRem[j])) {
                        if (affiche)
                            LOGGER.info("1- treeConstraint.filtering.structuralFiltering.tree.Tree: suppression arc (" + name + "," + toRem[j] + ")");
                        int[] arc = {name, toRem[j]};
                        propagateStruct.addRemoval(arc);
                    }
                    j++;
                }
            }
        }
        // dom(ntree) inter [mintree,maxtree] = maxtree => all potential roots fixed
        int cpt = 0;
        int val = -1;
        for (int m = mintree; m <= maxtree; m++) {
            if (tree.getNtree().canBeInstantiatedTo(m)) {
                cpt++;
                val = m;
            }
        }
        if (cpt == 1 && val == maxtree) {
            for (int m = 0; m < nbVertices; m++) {
                IntDomainVar var = nodes[m].getSuccessors();
                if (var.canBeInstantiatedTo(m)) {
                    DisposableIntIterator it = var.getDomain().getIterator();
                    int[] toRem = new int[var.getDomainSize()];
                    for (int j = 0; j < toRem.length; j++) toRem[j] = -1;
                    int j = 0;
                    while (it.hasNext()) {
                        int k = it.next();
                        if (k != m) {
                            toRem[j] = k;
                            j++;
                        }
                    }
                    it.dispose();
                    j = 0;
                    while (toRem[j] != -1) {
                        if (var.canBeInstantiatedTo(toRem[j])) {
                            if (affiche)
                                LOGGER.info("2- treeConstraint.filtering.structuralFiltering.tree.Tree: suppression arc (" + m + "," + toRem[j] + ")");
                            int[] arc = {m, toRem[j]};
                            propagateStruct.addRemoval(arc);
                        }
                        j++;
                    }
                }
            }
        }
        // dom(ntree) inter [mintree,maxtree] = mintree => no potential root fixed
        boolean affected = false;
        int name = -1;
        for (int m = 0; m < nbVertices; m++) {
            if (nodes[m].getSuccessors().isInstantiatedTo(m)) {
                affected = true;
                name = m;
            }
        }
        if (affected) {
            BitSet nonSink = new BitSet(reducedGraph.length);
            for (int i = 0; i < reducedGraph.length; i++) {
                if (reducedGraph[i].cardinality() > 0) nonSink.set(i, true);
            }
            val = tree.getNtree().getSup();
            if (val == mintree) {
                for (int m = nonSink.nextSetBit(0); m >= 0; m = nonSink.nextSetBit(m + 1)) {
                    IStateBitSet cont = cfc.elementAt(m);
                    for (int j = cont.nextSetBit(0); j >= 0; j = cont.nextSetBit(j + 1)) {
                        if (nodes[j].getSuccessors().canBeInstantiatedTo(j) && !nodes[j].getSuccessors().isInstantiatedTo(j)) {
                            if (affiche)
                                LOGGER.info("3- treeConstraint.filtering.structuralFiltering.tree.Tree: suppression boucle sur " + j);
                            int[] arc = {j, j};
                            propagateStruct.addRemoval(arc);
                        }
                    }
                }
            }
        } else {
            for (int m = 0; m < nbVertices; m++) {
                if (nodes[m].getSuccessors().isInstantiatedTo(m) && m != name) {
                    if (affiche)
                        LOGGER.info("4- treeConstraint.filtering.structuralFiltering.tree.Tree: suppression boucle sur " + m);
                    int[] arc = {m, m};
                    propagateStruct.addRemoval(arc);
                }
            }
        }
        // filetring for the doors involved in each scc
        BitSet nonSink = new BitSet(reducedGraph.length);
        for (int i = 0; i < reducedGraph.length; i++) {
            if (reducedGraph[i].cardinality() > 0) nonSink.set(i, true);
        }
        for (int m = nonSink.nextSetBit(0); m >= 0; m = nonSink.nextSetBit(m + 1)) {
            IStateBitSet cont = cfc.elementAt(m);
            BitSet door = new BitSet();
            for (int j = cont.nextSetBit(0); j >= 0; j = cont.nextSetBit(j + 1)) {
                IntDomainVar var = nodes[j].getSuccessors();
                DisposableIntIterator it = var.getDomain().getIterator();
                while (it.hasNext()) {
                    int r = it.next();
                    if (r == j)
                        door.set(j, true);
                    else if (!cont.get(r)) door.set(j, true);
                }
                it.dispose();
            }
            if (door.cardinality() == 1) {
                int j = door.nextSetBit(0);
                IntDomainVar var = nodes[j].getSuccessors();
                DisposableIntIterator it = var.getDomain().getIterator();
                while (it.hasNext()) {
                    int r = it.next();
                    if (cont.get(r) && r != j && var.canBeInstantiatedTo(r)) {
                        if (affiche)
                            LOGGER.info("5- treeConstraint.filtering.structuralFiltering.tree.Tree: suppression (" + j + "," + r + ")");
                        int[] arc = {j, r};
                        propagateStruct.addRemoval(arc);
                    }
                }
                it.dispose();
            }
        }
        // filtering rule on dominator nodes (originaly called strong articulation points in CPAIOR'05 paper)
        BitSet[][] dominators = doms.getDominators();
        BitSet doms = new BitSet(nbVertices);
        for (int i = 0; i < nbVertices; i++) {
            for (int j = 0; j < nbVertices; j++) {
                doms.or(dominators[i][j]);
            }
        }
        for (int p = doms.nextSetBit(0); p >= 0; p = doms.nextSetBit(p + 1)) {
            BitSet reached = new BitSet(nbVertices);
            BitSet unreached = new BitSet(nbVertices);
            for (int i = 0; i < nbVertices; i++) {
                unreached.set(i, true);
            }
            for (int i = 0; i < nbVertices; i++) {
                if (inputGraph.getGlobal().getSuccessors(i).get(i)) {
                    SearchInfeasible search = new SearchInfeasible(p, inputGraph.getGlobal().getRevGraph());
                    search.dfsVisit(i);
                    BitSet from_i = search.getReached();
                    reached.or(from_i);
                }
            }
            unreached.xor(reached);
            for (int i = unreached.nextSetBit(0); i >= 0; i = unreached.nextSetBit(i + 1)) {
                if (nodes[p].getSuccessors().canBeInstantiatedTo(i)) {
                    if (affiche) LOGGER.info("6- Tree: suppression arc (" + p + "," + i + ")");
                    int[] arc = {p, i};
                    propagateStruct.addRemoval(arc);
                }
            }
        }
    }
}
