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

import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.algorithms.ConnectedComponents;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.Solver;

import java.text.MessageFormat;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * the core class that allow to represent a graph and a set of properties that can be dynamically maintained.
 */
public class StoredBitSetGraph {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * list of graph properties that can be maintained for a given graph
     */
    public static enum Maintain {
        TRANSITIVE_CLOSURE, TRANSITIVE_REDUCTION, CONNECTED_COMP, NONE
    }

    /**
     * Choco solver embedding the tree constraint
     */
    protected Solver solver;

    /**
     * list of graph properties to maintain for the graph
     */
    protected List<Maintain> params;

    /**
     * total number of nodes involved in the graph
     */
    protected int nbNodes;

    /**
     * reference idx in a depth first search
     */
    protected int idx;

    /**
     * resulting labelling of the nodes involved in the graph according to a depth first search 
     */
    protected int[] dfsTree;

    /**
     * backtrackable bitset matrix representing the graph
     */
    protected IStateBitSet[] graph;

    /**
     * backtrackable bitset matrix representing the reverse graph
     */
    protected IStateBitSet[] revGraph;

    /**
     * backtrackable bitset matrix representing the transitive closure of the graph
     */
    protected IStateBitSet[] tcGraph;

    /**
     * backtrackable bitset matrix representing the reverse transitive closure of the graph
     */
    protected IStateBitSet[] revTcGraph;

    protected boolean needUpdate;

    /**
     * backtrackable bitset matrix representing the transitive reduction of the graph
     */
    protected IStateBitSet[] trGraph;
    
    /**
     * backtrackable bitset matrix representing the reverse transitive reduction of the graph
     */
    protected IStateBitSet[] revTrGraph;

    /**
     * backtrackable bitset that store the source nodes of the graph
     */
    protected IStateBitSet srcNodes;

    /**
     * backtrackable bitset that store the sink nodes of the graph
     */
    protected IStateBitSet sinkNodes;

    /**
     * connected component structure associated with the graph
     */
    protected ConnectedComponents cc;
    protected Vector<IStateBitSet> setCC;
    protected IStateBitSet[] vertFromNumCC;
    protected IStateBitSet[] numFromVertCC;

    /**
     * backtrackable integer recording the current number of connected components
     */
    protected IStateInt nbCC;

    /**
     * boolean for debug and show a trace of the execution
     */
    protected boolean affiche;

    public StoredBitSetGraph(Solver solver, IStateBitSet[] graph, List<Maintain> params, boolean affiche) {
        this.solver = solver;
        this.graph = graph;
        this.params = params;
        this.nbNodes = graph.length;
        this.affiche = affiche;
        this.idx = 0;
        this.dfsTree = new int[nbNodes];
        for (int k = 0; k < nbNodes; k++) dfsTree[k] = -1;
        // initialize the set of source and sink nodes of the graph
        this.srcNodes = solver.getEnvironment().makeBitSet(nbNodes);
        this.sinkNodes = solver.getEnvironment().makeBitSet(nbNodes);
        // initialize the required graph associated with the initial one
        this.revGraph = new IStateBitSet[nbNodes];
        if (params.contains(Maintain.TRANSITIVE_CLOSURE)) {
            this.tcGraph = new IStateBitSet[nbNodes];
            this.revTcGraph = new IStateBitSet[nbNodes];
        } else {
            this.tcGraph = null;
            this.revTcGraph = null;
        }
        if (params.contains(Maintain.TRANSITIVE_REDUCTION)) {
            this.trGraph = new IStateBitSet[nbNodes];
            this.revTrGraph = new IStateBitSet[nbNodes];
        } else {
            this.trGraph = null;
            this.revTrGraph = null;
        }
        // initialize internal data structure of the graphs
        initAllGraphs();
        createRevGraph();
        updateSpecialNodes();
        // compute required properties
        if (params.contains(Maintain.TRANSITIVE_CLOSURE)) computeTCfromScratch();
        if (params.contains(Maintain.TRANSITIVE_REDUCTION)) computeTRfromScratch();
        if (params.contains(Maintain.CONNECTED_COMP)) {
            initCCstruct();
            computeCCfromScratch();
        }
    }

    private void initCCstruct() {
        this.setCC = new Vector<IStateBitSet>(this.nbNodes);
        this.vertFromNumCC = new IStateBitSet[this.nbNodes];
        this.numFromVertCC = new IStateBitSet[this.nbNodes];
        for (int i = 0; i < this.nbNodes; i++) {
            this.setCC.add(this.solver.getEnvironment().makeBitSet(this.nbNodes));
            this.vertFromNumCC[i] = this.solver.getEnvironment().makeBitSet(this.nbNodes);
            this.numFromVertCC[i] = this.solver.getEnvironment().makeBitSet(this.nbNodes);
        }
        this.nbCC = this.solver.getEnvironment().makeInt(0);
        this.cc = new ConnectedComponents(this.solver, this.nbNodes, this.graph, this.setCC);
    }

    private void initAllGraphs() {
        for (int i = 0; i < nbNodes; i++) {
            this.revGraph[i] = solver.getEnvironment().makeBitSet(nbNodes);
            if (params.contains(Maintain.TRANSITIVE_CLOSURE)) {
                this.tcGraph[i] = solver.getEnvironment().makeBitSet(nbNodes);
                this.revTcGraph[i] = solver.getEnvironment().makeBitSet(nbNodes);
            }
            if (params.contains(Maintain.TRANSITIVE_REDUCTION)) {
                this.trGraph[i] = solver.getEnvironment().makeBitSet(nbNodes);
                this.revTrGraph[i] = solver.getEnvironment().makeBitSet(nbNodes);
            }
        }
    }

    private void createRevGraph() {
        for (int i = 0; i < nbNodes; i++) {
            for (int j = graph[i].nextSetBit(0); j >= 0; j = graph[i].nextSetBit(j + 1)) {
                revGraph[j].set(i, true);
            }
        }
    }

    private void computeTCfromScratch() {
        razTC();
        for (int i = 0; i < nbNodes; i++) {
            for (int j = graph[i].nextSetBit(0); j >= 0; j = graph[i].nextSetBit(j + 1)) {
                if (i != j) {
                    tcGraph[i].set(j, true);
                    revTcGraph[j].set(i, true);
                }
            }
        }
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) {
                if (tcGraph[j].get(i) && j != i) {
                    for (int k = tcGraph[i].nextSetBit(0); k >= 0; k = tcGraph[i].nextSetBit(k + 1)) {
                        tcGraph[j].set(k, true);
                        revTcGraph[k].set(j, true);
                    }
                }
            }
        }
    }

    private void addIncreTC(int i, int j) {
        if (i != j) {
            // descendants
            if (!tcGraph[i].get(j)) {
                tcGraph[i].or(tcGraph[j]);
                tcGraph[i].set(j, true);
                for (int k = revTcGraph[i].nextSetBit(0); k >= 0; k = revTcGraph[i].nextSetBit(k + 1)) {
                    if (!tcGraph[k].get(j)) {
                        tcGraph[k].or(tcGraph[j]);
                        tcGraph[k].set(j, true);
                    }
                }
                // ancestors
                revTcGraph[j].or(revTcGraph[i]);
                revTcGraph[j].set(i, true);
                for (int k = tcGraph[j].nextSetBit(0); k >= 0; k = tcGraph[j].nextSetBit(k + 1)) {
                    if (!tcGraph[i].get(k)) {
                        revTcGraph[k].or(revTcGraph[i]);
                        revTcGraph[k].set(i, true);
                    }
                }
            }
        }
    }

    private void remIncreTC(int i, int j) {
        if (i != j) {
            // reachable nodes from node i in the graph
            IStateBitSet tempDesc = getDesc(i, j, graph);
            if (needUpdate) {
                tcGraph[i] = tempDesc;
                // compute all the reachble nodes from each ancestor of i in graph
                IStateBitSet updateAnc = solver.getEnvironment().makeBitSet(nbNodes);
                for (int k = revTcGraph[i].nextSetBit(0); k >= 0; k = revTcGraph[i].nextSetBit(k + 1)) {
                    if (!updateAnc.get(k)) {
                        tempDesc = getDesc(k, j, graph);
                        if (!needUpdate) updateAnc.or(revTcGraph[k]);
                        else tcGraph[k] = tempDesc;
                    }
                }
                // compute the nodes reachable from j in the reverse graph
                revTcGraph[j] = getDesc(j, i, revGraph);
                // compute all the nodes reachable from each descendant of j in the reverse graph
                IStateBitSet updateDesc = solver.getEnvironment().makeBitSet(nbNodes);
                for (int k = tcGraph[j].nextSetBit(0); k >= 0; k = tcGraph[j].nextSetBit(k + 1)) {
                    if (!updateDesc.get(k)) {
                        tempDesc = getDesc(k, i, revGraph);
                        if (!needUpdate) updateDesc.or(tcGraph[k]);
                        else revTcGraph[k] = tempDesc;
                    }
                }
            }
        }
    }

    private IStateBitSet getDesc(int i, int j, IStateBitSet[] graph) {
        // retrieve the set of reachable nodes from i in the graph
        needUpdate = true;
        Stack<Integer> stack = new Stack<Integer>();
        IStateBitSet reached = solver.getEnvironment().makeBitSet(nbNodes);
        stack.push(i);
        while (!stack.isEmpty()) {
            int a = stack.pop();
            for (int b = graph[a].nextSetBit(0); b >= 0; b = graph[a].nextSetBit(b + 1)) {
                if (!stack.contains(b) && !reached.get(b)) {
                    reached.set(b, true);
                    if (b == j) {
                        needUpdate = false;
                        return reached;
                    } else stack.push(b);
                }
            }
        }
        return reached;
    }

    private void razTC() {
        for (int i = 0; i < nbNodes; i++) {
            for (int j = tcGraph[i].nextSetBit(0); j >= 0; j = tcGraph[i].nextSetBit(j + 1)) {
                tcGraph[i].set(j, false);
                revTcGraph[j].set(i, false);
            }
        }
    }

    private void computeTRfromScratch() {
        razTR();
        for (int i = 0; i < nbNodes; i++) {
            for (int j = graph[i].nextSetBit(0); j >= 0; j = graph[i].nextSetBit(j + 1)) {
                trGraph[i].set(j, true);
                revTrGraph[j].set(i, true);
            }
        }
        for (int i = 0; i < nbNodes; i++) {
            int[][] num = new int[nbNodes][2];
            for (int j = 0; j < nbNodes; j++) {
                num[j][0] = -1;
                num[j][1] = -1;
            }
            idx = 0;
            for (int k = 0; k < nbNodes; k++) dfsTree[k] = -1;
            dfs(i, i, num);
        }
    }

    private void razTR() {
        for (int i = 0; i < nbNodes; i++) {
            for (int j = trGraph[i].nextSetBit(0); j >= 0; j = trGraph[i].nextSetBit(j + 1)) {
                trGraph[i].set(j, false);
                revTrGraph[j].set(i, false);
            }
        }
    }

    private int[][] dfs(int root, int u, int[][] num) {
        num[u][0] = idx++;
        for (int v = graph[u].nextSetBit(0); v >= 0; v = graph[u].nextSetBit(v + 1)) {
            if (num[v][0] == -1) {
                dfsTree[v] = u;
                num = dfs(root, v, num);
            } else {
                if (num[u][1] == -1 && num[u][0] > num[v][0]) {
                    int w = dfsTree[v];
                    if (w == root) { // (w,v) is a transitive arc in the dfs tree
                        trGraph[w].set(v, false);
                        revTrGraph[v].set(w, false);
                    }
                }
                // (u,v) is a transitive arc in a specific branch of the dfs tree
                if (num[v][1] != -1 && num[u][0] < num[v][0]) {
                    trGraph[u].set(v, false);
                    revTrGraph[v].set(u, false);
                }
            }
        }
        num[u][1] = idx++;
        return num;
    }

    private void computeCCfromScratch() {
        this.cc.getConnectedComponents(affiche);
        if (affiche) showCC();
        // record the connected components of the graph
        for (int i = 0; i < nbNodes; i++) this.numFromVertCC[i].clear();
        for (int i = 0; i < this.setCC.size(); i++) {
            IStateBitSet contain = this.setCC.elementAt(i);
            this.vertFromNumCC[i].clear();
            for (int j = contain.nextSetBit(0); j >= 0; j = contain.nextSetBit(j + 1)) {
                this.vertFromNumCC[i].set(j, true);
                this.numFromVertCC[j].set(i, true);
            }
        }
        this.nbCC.set(this.cc.getNbCC());
    }

    private void showCC() {
        for (int i = 0; i < setCC.size(); i++) {
            IStateBitSet contain = setCC.elementAt(i);
            LOGGER.info("cc(" + i + ") = " + contain.toString());
        }
        LOGGER.info("*-*-*-*-*-*-*-*-*-*-*-*-*");
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    ////////////////// Algorithmes pour ajouter/retirer un arc dans graph ////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * add the arc (u,v) in the graph view structure (required properties are dynamically updated)
     *
     * @param u     index of a node
     * @param v     index of a node
     */
    public void addArc(int u, int v) {
        // add arc
        graph[u].set(v, true);
        revGraph[v].set(u, true);
        // update properties
        if (params.contains(Maintain.TRANSITIVE_CLOSURE)) addIncreTC(u, v);
        if (params.contains(Maintain.TRANSITIVE_REDUCTION)) computeTRfromScratch();
        // update sink and source informations
        updateSpecialNodes(u, v);
        // update connected components
        if (params.contains(Maintain.CONNECTED_COMP)) computeCCfromScratch();
    }

    /**
     * remove the arc (u,v) from the graph view structure (required properties are dynamically updated)
     *
     * @param u     index of a node
     * @param v     index of a node
     */
    public void remArc(int u, int v) {
        // remove arc
        graph[u].set(v, false);
        revGraph[v].set(u, false);
        // update properties
        if (params.contains(Maintain.TRANSITIVE_CLOSURE)) remIncreTC(u, v);
        if (params.contains(Maintain.TRANSITIVE_REDUCTION)) computeTRfromScratch();
        // update sink and source informations
        updateSpecialNodes(u, v);
        // update connected components
        if (params.contains(Maintain.CONNECTED_COMP)) computeCCfromScratch();
    }

    /**
     * remove all the successors of node u from the graph view structure (required properties are
     * dynamically updated)
     *
     * @param u     index of a node
     */
    public void remAllSucc(int u) {
        for (int v = graph[u].nextSetBit(0); v >= 0; v = graph[u].nextSetBit(v + 1)) {
            graph[u].set(v, false);
            revGraph[v].set(u, false);
        }
        for (int v = graph[u].nextSetBit(0); v >= 0; v = graph[u].nextSetBit(v + 1)) {
            if (params.contains(Maintain.TRANSITIVE_CLOSURE)) remIncreTC(u, v);
        }
        if (params.contains(Maintain.TRANSITIVE_REDUCTION)) computeTRfromScratch();
        updateSpecialNodes();
        if (params.contains(Maintain.CONNECTED_COMP)) computeCCfromScratch();
    }

    /**
     * remove all the successors of node u, excepted node v, from the graph view structure (required properties are
     * dynamically updated)
     *
     * @param u     index of a node
     * @param v     index of a node
     */
    public void remAllExcepted(int u, int v) {
        // remove all the outgoing arcs excepted (u,v)
        for (int w = graph[u].nextSetBit(0); w >= 0; w = graph[u].nextSetBit(w + 1)) {
            if (w != v) {
                graph[u].set(w, false);
                revGraph[w].set(u, false);
            }
        }
        for (int w = graph[u].nextSetBit(0); w >= 0; w = graph[u].nextSetBit(w + 1)) {
            if (w != v && params.contains(Maintain.TRANSITIVE_CLOSURE)) remIncreTC(u, w);
        }
        if (params.contains(Maintain.TRANSITIVE_REDUCTION)) computeTRfromScratch();
        updateSpecialNodes();
        if (params.contains(Maintain.CONNECTED_COMP)) computeCCfromScratch();
    }

    /**
     * remove all the successors of node u that have an index below to idx (required properties are
     * dynamically updated)
     *
     * @param u     index of a node
     * @param idx   integer value
     */
    public void remAllLowerIdx(int u, int idx) {
        for (int v = graph[u].nextSetBit(0); v >= 0; v = graph[u].nextSetBit(v + 1)) {
            if (v < idx) {
                graph[u].set(v, false);
                revGraph[v].set(u, false);
            }
        }
        for (int v = graph[u].nextSetBit(0); v >= 0; v = graph[u].nextSetBit(v + 1)) {
            if (v < idx && params.contains(Maintain.TRANSITIVE_CLOSURE)) remIncreTC(u, v);
        }
        if (params.contains(Maintain.TRANSITIVE_REDUCTION)) computeTRfromScratch();
        updateSpecialNodes();
        if (params.contains(Maintain.CONNECTED_COMP)) computeCCfromScratch();
    }

    /**
     * remove all the successors of node u that have an index higher than idx (required properties are
     * dynamically updated)
     *
     * @param u     index of a node
     * @param idx   integer value
     */
    public void remAllGreaterIdx(int u, int idx) {
        for (int v = graph[u].nextSetBit(0); v >= 0; v = graph[u].nextSetBit(v + 1)) {
            if (v > idx) {
                graph[u].set(v, false);
                revGraph[v].set(u, false);
            }
        }
        for (int v = graph[u].nextSetBit(0); v >= 0; v = graph[u].nextSetBit(v + 1)) {
            if (v > idx && params.contains(Maintain.TRANSITIVE_CLOSURE)) remIncreTC(u, v);
        }
        if (params.contains(Maintain.TRANSITIVE_REDUCTION)) computeTRfromScratch();
        updateSpecialNodes();
        if (params.contains(Maintain.CONNECTED_COMP)) computeCCfromScratch();
    }

    /**
     * remove all the successors of node u that have an index below to inf and higher
     * than sup (required properties are dynamically updated)
     *
     * @param u     index of a node
     * @param inf   integer value
     * @param sup   integer value
     */
    public void remAllIdx(int u, int inf, int sup) {
        for (int v = graph[u].nextSetBit(0); v >= 0; v = graph[u].nextSetBit(v + 1)) {
            if (v < inf || v > sup) {
                graph[u].set(v, false);
                revGraph[v].set(u, false);
            }
        }
        for (int v = graph[u].nextSetBit(0); v >= 0; v = graph[u].nextSetBit(v + 1)) {
            if (v < inf || v > sup) {
                if (params.contains(Maintain.TRANSITIVE_CLOSURE)) remIncreTC(u, v);
            }
        }
        if (params.contains(Maintain.TRANSITIVE_REDUCTION)) computeTRfromScratch();
        updateSpecialNodes();
        if (params.contains(Maintain.CONNECTED_COMP)) computeCCfromScratch();
    }

    /**
     * all the arc (u,v), such that v belongs to the set depicted by the iterator, are removed
     *
     * @param u     index of a node
     * @param deltaDomain   an iterator over the removed indices
     */
    public void remAllNodes(int u, DisposableIntIterator deltaDomain) {
        while (deltaDomain.hasNext()) {
            int v = deltaDomain.next();
            graph[u].set(v, false);
            revGraph[v].set(u, false);
        }
        deltaDomain.dispose();
        while (deltaDomain.hasNext()) {
            int v = deltaDomain.next();
            if (params.contains(Maintain.TRANSITIVE_CLOSURE)) remIncreTC(u, v);
        }
        deltaDomain.dispose();
        if (params.contains(Maintain.TRANSITIVE_REDUCTION)) computeTRfromScratch();
        updateSpecialNodes();
        if (params.contains(Maintain.CONNECTED_COMP)) computeCCfromScratch();
    }

    private void updateSpecialNodes(int u, int v) {
        if (graph[u].cardinality() == 0) sinkNodes.set(u, true);
        else sinkNodes.set(u, false);
        if (revGraph[v].cardinality() == 0) srcNodes.set(v, true);
        else srcNodes.set(v, false);
    }

    private void updateSpecialNodes() {
        for (int i = 0; i < nbNodes; i++) {
            if (graph[i].cardinality() == 0) sinkNodes.set(i, true);
            else sinkNodes.set(i, false);
            if (revGraph[i].cardinality() == 0) srcNodes.set(i, true);
            else srcNodes.set(i, false);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// Accesseurs pour la structure ///////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    public int getGraphSize() {
        return nbNodes;
    }

    public IStateBitSet getSuccessors(int i) {
        return graph[i];
    }

    public IStateBitSet getPredecessors(int i) {
        return revGraph[i];
    }

    public IStateBitSet getDescendants(int i) {
        return tcGraph[i];
    }

    public IStateBitSet getAncestors(int i) {
        return revTcGraph[i];
    }

    public IStateBitSet[] getGraph() {
        return graph;
    }

    public void setGraph(IStateBitSet[] newGraph) {
        razGraph();
        for (int i = 0; i < nbNodes; i++) {
            for (int j = newGraph[i].nextSetBit(0); j >= 0; j = newGraph[i].nextSetBit(j + 1)) {
                graph[i].set(j, true);
            }
        }
    }

    public void razGraph() {
        for (int i = 0; i < nbNodes; i++) {
            for (int j = graph[i].nextSetBit(0); j >= 0; j = graph[i].nextSetBit(j + 1)) {
                graph[i].set(j, false);
            }
        }
    }

    public IStateBitSet[] getRevGraph() {
        return revGraph;
    }

    public IStateBitSet[] getTcGraph() {
        return tcGraph;
    }

    public IStateBitSet[] getRevTcGraph() {
        return revTcGraph;
    }

    public IStateBitSet[] getTrGraph() {
        return trGraph;
    }

    public IStateBitSet[] getRevTrGraph() {
        return revTrGraph;
    }

    public IStateBitSet getSrcNodes() {
        return srcNodes;
    }

    public IStateBitSet getSinkNodes() {
        return sinkNodes;
    }

    public Vector<IStateBitSet> getSetCC() {
        return setCC;
    }

    public IStateBitSet[] getVertFromNumCC() {
        return vertFromNumCC;
    }

    public IStateBitSet[] getNumFromVertCC() {
        return numFromVertCC;
    }

    public IStateInt getNbCC() {
        return nbCC;
    }

    public String showDesc(int i, String type) {
        StringBuilder s = new StringBuilder("D_" + type + "[" + i + "] = ");
        for (int j = tcGraph[i].nextSetBit(0); j >= 0; j = tcGraph[i].nextSetBit(j + 1)) {
            s.append(j).append(" ");
        }
        return s.toString();
    }

    public void showAllDesc(String type) {
        for (int i = 0; i < nbNodes; i++) {
            StringBuffer st = new StringBuffer();
            st.append(type).append("").append(i).append(":=");
            for (int j = tcGraph[i].nextSetBit(0); j >= 0; j = tcGraph[i].nextSetBit(j + 1)) {
                st.append(" ").append(j);
            }
            LOGGER.info(st.toString());
        }
    }

    public void showGraph(String type) {
        for (int i = 0; i < nbNodes; i++) {
            StringBuffer st = new StringBuffer();
            st.append(type).append("").append(i).append(":=");
            for (int j = graph[i].nextSetBit(0); j >= 0; j = graph[i].nextSetBit(j + 1)) {
                st.append(MessageFormat.format(" {0}", j));
            }
            LOGGER.info(st.toString());
        }
    }

    public void affiche() {
        LOGGER.info("************ Graph **************");
        StringBuffer st = new StringBuffer();
        for (int i = 0; i < nbNodes; i++) {
            st.append("graph[").append(i).append("] = ");
            for (int j = graph[i].nextSetBit(0); j >= 0; j = graph[i].nextSetBit(j + 1)) st.append(j).append(" ");
        }
        LOGGER.info(st.toString());
        LOGGER.info("*********************************");
        if (params.contains(Maintain.TRANSITIVE_CLOSURE)) {
            LOGGER.info("************ TC Graph **************");
            st = new StringBuffer();
            for (int i = 0; i < nbNodes; i++) {
                st.append("TCgraph[").append(i).append("] = ");
                for (int j = tcGraph[i].nextSetBit(0); j >= 0; j = tcGraph[i].nextSetBit(j + 1))
                    st.append(j).append(" ");
            }
            LOGGER.info(st.toString());
            LOGGER.info("*********************************");
        }
        if (params.contains(Maintain.TRANSITIVE_REDUCTION)) {
            LOGGER.info("************ TR Graph **************");
            st = new StringBuffer();
            for (int i = 0; i < nbNodes; i++) {
                st.append("TRgraph[").append(i).append("] = ");
                for (int j = trGraph[i].nextSetBit(0); j >= 0; j = trGraph[i].nextSetBit(j + 1))
                    st.append(j).append(" ");
            }
            LOGGER.info(st.toString());
            LOGGER.info("*********************************");
        }
    }
}
