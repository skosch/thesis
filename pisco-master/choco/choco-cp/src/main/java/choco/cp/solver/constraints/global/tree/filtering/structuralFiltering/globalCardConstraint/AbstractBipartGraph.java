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

package choco.cp.solver.constraints.global.tree.filtering.structuralFiltering.globalCardConstraint;

import choco.cp.solver.constraints.global.tree.filtering.RemovalsAdvisor;
import choco.cp.solver.constraints.global.tree.structure.inputStructure.TreeParameters;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.StructuresAdvisor;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;
import java.util.logging.Logger;

public abstract class AbstractBipartGraph {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    // slots storing the graph as a matching
    protected int nbLeftVertices, nbRightVertices, nbVertices;
    protected int minValue = Integer.MIN_VALUE;
    protected int maxValue = Integer.MAX_VALUE;
    protected int source;
    protected int[] refMatch;
    protected int matchingSize;
    protected int[] left2rightArc, right2leftArc; // storing the alternating forest (in the search for augmenting paths)
    protected IntQueue queue;

    protected Solver solver;
    protected IntDomainVar nTree;
    protected BitSet[] graph;
    protected boolean isFeasible; // true iff there exists a fail case
    protected TreeParameters tree;
    protected int[] index;
    protected StructuresAdvisor struct;

    // slots for algorithm computing the strongly connected components
    // temporary data structure: markers, iterators, ....
    protected int time = 0;               // a time counter
    protected int[] finishDate;           // finishDate[i] : value of time when the expansion of i was completed in DFS
    protected boolean[] seen;             // seen[i]=true <=> node i has been expanded in DFS
    protected int currentNode = -1;       // the current node in the second DFS exploration
    protected int currentComponent = -1;  // a counter used when building the solution
    // the solution
    protected int[] component;            // storing the solution: component[i] is the index of strong con. comp. of i
    protected boolean[][] componentOrder; // componentOrder[i,j]=true <=> there exists an edge in the SCC graph from
    // component i to component j

    /**
     * Constructor
     *
     * @param solver the choco solver involving the constraint
     * @param pack    a set of parameters such as the original set of variables fvars, the contracted graph
     *                structure associated and the table of indices
     */
    protected AbstractBipartGraph(Solver solver, Object[] pack) {
        this.solver = solver;
        init(pack);
    }

    protected void init(Object[] pack) {
        this.tree = (TreeParameters) pack[0];
        this.nTree = tree.getNtree();
        this.struct = (StructuresAdvisor) pack[1];
        this.graph = struct.getDegree().getGccVars();
        this.nbLeftVertices = struct.getDegree().getNbLeftVertices();
        this.nbRightVertices = struct.getDegree().getLow().length;
        this.index = struct.getDegree().getIndexVars();
        this.nbVertices = this.nbLeftVertices + this.nbRightVertices + 1;
        this.refMatch = new int[this.nbLeftVertices];
        for (int i = 0; i < this.refMatch.length; i++) this.refMatch[i] = -1;
        this.matchingSize = 0;
        this.queue = new IntQueue(this.nbVertices - 1);
        this.left2rightArc = new int[this.nbLeftVertices];
        this.right2leftArc = new int[this.nbRightVertices];
        this.source = this.nbVertices - 1;
        this.finishDate = new int[this.nbVertices]; // Default value : 0
        this.seen = new boolean[this.nbVertices]; // Default value : false
        this.component = new int[this.nbVertices];
        for (int i = 0; i < component.length; i++) {
            component[i] = -1;
        }
        this.componentOrder = new boolean[this.nbVertices][this.nbVertices];
        for (int i = 0; i < this.nbVertices; i++) {
            this.componentOrder[i][i] = true;
        }
        isFeasible = true;
    }

    // ==============================================================
    //           GENERIC IMPLEMENTATION OF A BIPARTITE GRAPH
    // ==============================================================

    /**
     * Accessing the edges of the bipartite graph access from the left vertex set
     *
     * @param i
     * @return ret
     */
    public int[] mayMatch(int i) {
        int[] ret = new int[graph[i].cardinality()];
        int offset = 0;
        for (int j = graph[i].nextSetBit(0); j >= 0; j = graph[i].nextSetBit(j + 1)) {
            ret[offset++] = j - this.minValue;
        }
        return ret;

    }

    /**
     * reverse, access from the right vertex set:
     * iterating over the variables (left vertex set) and reading their domains
     *
     * @param j
     * @return ret2
     */
    public int[] mayInverseMatch(int j) {
        int[] ret = new int[this.nbLeftVertices];
        int nb = 0;
        for (int i = 0; i < this.nbLeftVertices; i++) {
            if (graph[i].get(j + this.minValue)) {
                ret[nb++] = i;
            }
        }
        int[] ret2 = new int[nb];
        System.arraycopy(ret, 0, ret2, 0, nb);
        return ret2;
    }

    /**
     * @param i left vextex index
     * @return accessing the right vertex matched to i
     */
    public int match(int i) {
        //return this.refMatch.get(i);
        return this.refMatch[i];
    }

    /**
     * whether the flow from i (a left vertex) to the sink may be increased
     *
     * @param i
     * @return boolean
     */
    public boolean mayGrowFlowToSink(int i) {
        return this.match(i) == -1;
    }

    /**
     * whether the flow from j (a right vertex) to i (a left vertex) may be increased
     * (the additional flow is able to arrive to j, we don't care yet whether it will be able to leave i)
     *
     * @param j
     * @param i
     * @return boolean
     */
    public boolean mayGrowFlowBetween(int j, int i) {
        return this.match(i) != j;
    }

    /**
     * whether the flow from j (a right vertex) to i (a left vertex) may be increased
     *
     * @param j
     * @param i
     * @return boolean
     */
    public boolean mayDiminishFlowBetween(int j, int i) {
        return this.match(i) == j;
    }

    /**
     * updates the matching size when one more left vertex is matched with j
     *
     * @param j
     */
    public abstract void increaseMatchingSize(int j);

    /**
     * updates the matching size when one more left vertex is de-matched with j
     *
     * @param j
     */
    public abstract void decreaseMatchingSize(int j);

    /**
     * removing the arc i-j from the reference matching & update matchingSize
     *
     * @param i
     * @param j
     */
    public abstract void deleteMatch(int i, int j);

    /**
     * adding the arc i-j in the reference matching without any updates
     *
     * @param i left node index
     * @param j right node index
     */
    public abstract void putRefMatch(int i, int j);

    /**
     * adding the arc i-j in the reference matching & update matchingSize
     *
     * @param i
     * @param j
     */
    public abstract void setMatch(int i, int j);

// a flow is built in the bipartite graph from the reference matching
//          match(i) = j <=> flow(j to i) = 1
//    from a source linked to all right vertices and a sink linked to all left vertices
//    Yes, this IS counter-intuitive, the flow goes from left to right
//    but this makes the job much easier for gcc in order to compute compatible flows
//    (with lower bounds on the edges from the source to the right vertices)


    /**
     * whether the flow from the source to j (a right vertex) may be decreased
     *
     * @param j
     * @return boolean
     */
    public abstract boolean mayDiminishFlowFromSource(int j);

    /**
     * whether the flow from the source to j (a right vertex) may be increased
     *
     * @param j
     * @return boolean
     */
    public abstract boolean mayGrowFlowFromSource(int j);

    /**
     * whether the flow from the source to j (a right vertex) must be increased in order to get a maximal
     * (sink/left vertex set saturating) flow
     *
     * @param j
     * @return boolean
     */
    public abstract boolean mustGrowFlowFromSource(int j);

    // ==============================================================
    //            FINDING AN AUGMENTING PATH IN THE GRAPH
    // ==============================================================

    /**
     * First pass: use Ford & Fulkerson algorithm to compute a reference flow (assignment)
     * finds an augmenting path using a fifo queue
     *
     * @return 0 if none found, otherwise the end of the path
     */
    public int findAlternatingPath() {
        int eopath = -1;
        int n = this.nbLeftVertices;
        this.queue.init();
        for (int j = 0; j < this.nbRightVertices; j++) {
            // enqueue vertives of V2 whose lower bounds haven't been reached
            if (this.mustGrowFlowFromSource(j)) this.queue.push(j + n);
        }
        if (this.queue.getSize() == 0) {
            for (int j = 0; j < this.nbRightVertices; j++) {
                // otherwise enqueue vertives of V2 whose upper bounds haven't been reached
                if (this.mayGrowFlowFromSource(j)) this.queue.push(j + n);
            }
        }
        while (this.queue.getSize() > 0) {
            int x = this.queue.pop();
            if (x >= n) { // if the dequeued vertex is in V1
                x -= n;
                boolean shouldBreak = false;
                int[] yy = this.mayInverseMatch(x);
                for (int y : yy) { // For each value y in mayInverseMatch(x)
                    if (this.mayGrowFlowBetween(x, y) && !this.queue.onceInQueue(y)) {
                        this.left2rightArc[y] = x;
                        if (this.mayGrowFlowToSink(y)) {
                            eopath = y;
                            shouldBreak = true;
                            break;
                        } else {
                            this.queue.push(y);
                        }
                    }
                }
                if (shouldBreak) break;
            } else {
                int y = this.match(x);
                if (!this.queue.onceInQueue(y + n)) {
                    this.right2leftArc[y] = x;
                    this.queue.push(y + n);
                }
            }
        }
        return eopath;
    }

    /**
     * augment the matching along one alternating path
     * note: throughout the following code, we assume (1 <= x <= c.nbLeftVertices), (1 <= y <= c.nbRightVertices)
     *
     * @param x
     */
    public void augment(int x) {
        int y = this.left2rightArc[x];
        while (!this.mayGrowFlowFromSource(y)) {
            this.putRefMatch(x, y);
            x = this.right2leftArc[y];
            assert (this.match(x) == y);
            y = this.left2rightArc[x];
            assert (y >= 0);
        }
        this.putRefMatch(x, y);
        this.increaseMatchingSize(y);
    }

    /**
     * keeps augmenting the flow until a maximal flow is reached
     */
    public void augmentFlow() {
        int eopath = this.findAlternatingPath();
        int n1 = this.nbLeftVertices;
        //if (this.matchingSize.get() < n1) {
        if (this.matchingSize < n1) {
            while (eopath >= 0) {
                this.augment(eopath);
                eopath = this.findAlternatingPath();
            }
            // if (this.matchingSize.get() < n1) {
            if (this.matchingSize < n1) {
                this.isFeasible = false;
            }
        }
    }

    // ==============================================================
    //       FINDING STRONGLY CONNECTED COMPONENTS IN THE GRAPH
    // ==============================================================

    /**
     * initialize the graph data structure storing the SCC decomposition
     */
    public void initSCCGraph() {
        // erase the component partial order graph
        for (int i = 0; i < this.currentComponent; i++) {
            for (int j = 0; j < this.currentComponent; j++)
                if (i != j) {
                    this.componentOrder[i][j] = false;
                }
        }
        // erase the component graph
        for (int i = 0; i < this.nbVertices; i++) {
            this.component[i] = -1;
        }
        this.currentComponent = -1;
    }

    /**
     * adds a new vertex to the component graph (= a component = a set of s. connected vertices in the original graph)
     */
    public void addComponentVertex() {
        this.currentComponent++;
    }

    /**
     * add an edge in the component graph between compi and compj:
     * componentOrder stores the transitive closure of that graph
     *
     * @param compi
     * @param compj
     */
    public void addComponentEdge(int compi, int compj) {
        if (!this.componentOrder[compi][compj]) {
            this.componentOrder[compi][compj] = true;
            for (int compj2 = 0; compj2 < compj; compj2++) {
                if (this.componentOrder[compj][compj2]) {
                    this.componentOrder[compi][compj2] = true;
                }
            }
        }
    }

    public void firstPassDFS() {
        for (int i = 0; i < this.nbVertices; i++) {
            this.finishDate[i] = 0;
            this.seen[i] = false;
        }
        this.time = 0;
        for (int i = 0; i < this.nbVertices; i++) {
            this.firstDFSearch(i);
        }
    }

    /**
     * the first search explores (DFS) the reduced graph
     *
     * @param i
     */
    public void firstDFSearch(int i) {
        if (!this.seen[i]) {
            this.time++;
            this.seen[i] = true;
            if (i < this.nbLeftVertices) {    // (i % c.leftVertices)
                this.firstDFSearch(this.match(i) + this.nbLeftVertices);
            } else if (i < this.source) {     // (i % c.rightVertices)
                int[] jj = this.mayInverseMatch(i - this.nbLeftVertices);
                for (int j : jj) { // for each j in mayInverseMatch...
                    if (this.match(j) != i - this.nbLeftVertices) {
                        this.firstDFSearch(j);
                    }
                }
                if (this.mayDiminishFlowFromSource(i - this.nbLeftVertices)) {
                    this.firstDFSearch(this.source);
                }
            } else {                          // (i = sc.source)
                for (int j = 0; j < this.nbRightVertices; j++) {
                    if (this.mayGrowFlowFromSource(j)) {
                        this.firstDFSearch(j + this.nbLeftVertices);
                    }
                }
            }
            this.time++;
            this.finishDate[i] = this.time;
        }
    }

    public void secondPassDFS() {
        this.initSCCGraph();
        while (true) {
            int maxf = 0;
            int rootOfComp = -1;
            for (int i = 0; i < this.nbVertices; i++) {
                if ((this.component[i] == -1) && (this.finishDate[i] > maxf)) {
                    maxf = this.finishDate[i];
                    rootOfComp = i;
                }
            }
            if (maxf > 0) {
                this.addComponentVertex();
                this.secondDFSearch(rootOfComp);
            } else
                return;
        }
    }

    /**
     * the second search explores (DFS) the inverse of the reduced graph
     *
     * @param i
     */
    public void secondDFSearch(int i) {
        int compi = this.component[i];
        int curComp = this.currentComponent;
        if (compi == -1) {
            this.component[i] = curComp;
            this.currentNode = i;
            if (i < this.nbLeftVertices) {    // (i % c.leftVertices)
                int[] jj = this.mayMatch(i);
                for (int j : jj) {
                    if (this.match(i) != j) {
                        this.secondDFSearch(j + this.nbLeftVertices);
                    }
                }
            } else if (i < this.source) {     // (i % c.rightVertices)
                int[] jj = this.mayInverseMatch(i - this.nbLeftVertices);
                for (int j : jj) {
                    if (this.match(j) == i - this.nbLeftVertices) {
                        this.secondDFSearch(j);
                    }
                }
                if (this.mayGrowFlowFromSource(i - this.nbLeftVertices))
                    this.secondDFSearch(this.source);
            } else {                          // (i = sc.source)
                for (int j = 0; j < this.nbRightVertices; j++) {
                    if (this.mayDiminishFlowFromSource(j)) {
                        this.secondDFSearch(j + this.nbLeftVertices);
                    }
                }
            }
        } else if (compi < curComp) {
            // ajouter � la composante du sommet "p�re" une ar�te vers la composante du sommet i
            this.addComponentEdge(curComp, compi);
        }
    }

    /**
     * implement one of the two main events:
     * when an edge is definitely removed from the bipartite assignment graph
     *
     * @param i the variable to unmatch
     * @param j the value to remove
     */
    public abstract void deleteEdgeAndPublish(int i, int j, RemovalsAdvisor rem);

    /**
     * remove arcs connecting two different strongly connected components
     * the event generated by the flow algorithm:
     * discovering that an edge is no longer valid, and posting this event
     * to the constraint solver: since we are already achieving GAC consistency
     * in one single loop, there is no need to post a constAwake
     */
    public void removeUselessEdges(RemovalsAdvisor rem) throws ContradictionException {
        // assert
        if (this.matchingSize < this.nbLeftVertices) {
            this.augmentFlow();
        }
        this.firstPassDFS();
        this.secondPassDFS();
        for (int i = 0; i < this.nbLeftVertices; i++) {
            int[] jj = this.mayMatch(i);
            for (int j : jj) {
                if (this.match(i) != j) {
                    if (this.component[i] != this.component[j + this.nbLeftVertices]) {
                        this.deleteEdgeAndPublish(i, j, rem);
                    }
                }
            }
        }
    }

    public void propagate(RemovalsAdvisor rem) throws ContradictionException {
        this.removeUselessEdges(rem);
    }

    public int getPriority() {
        return 2;
    }

    // ==============================================================
    //   QUEUE OF REACHED VERTICES WHEN FINDING AN AUGMENTING PATH
    // ==============================================================

    protected static class IntQueue {
        /**
         * Maximum size of the queue.
         */
        private int maxSize;

        /**
         * Number of elements actually in the queue.
         */
        private int nbElts;

        /**
         * Last element pushed in the queue.
         */
        private int last;

        /**
         * Linked list of the values.
         */
        private int[] contents;

        /**
         * States if the value is in the queue or not.
         */
        private boolean[] onceInQueue;

        /**
         * Constructs a new queue with the specified maximal number of values.
         *
         * @param n Maximal size of the queue.
         */
        IntQueue(int n) {
            maxSize = n;
            contents = new int[n];
            onceInQueue = new boolean[n];
            this.init();
        }


        /**
         * @return the size of the queue.
         */
        public int getSize() {
            return this.nbElts;
        }

        /**
         * Initializes the queue.
         */
        public void init() {
            this.nbElts = 0;
            for (int i = 0; i < this.maxSize; i++) {
                this.contents[i] = -1;
                this.onceInQueue[i] = false;
            }
        }

        /**
         * Adds a value in the queue
         *
         * @param val
         */
        public void push(int val) {
            // assert (val <= this.maxSize);
            this.onceInQueue[val] = true;
            if (this.contents[val] == -1) {
                if (this.nbElts == 0) {
                    this.contents[val] = val;
                } else {
                    this.contents[val] = this.contents[last];
                    this.contents[last] = val;
                }
                this.last = val;
                this.nbElts++;
            }
        }

        /**
         * @return the older value in the queue
         */
        public int pop() {
            int val = this.contents[this.last];
            this.nbElts--;
            this.contents[this.last] = this.contents[val];
            this.contents[val] = -1;
            return val;
        }

        /**
         * @param i
         * @return true if i is in the queue
         */
        public boolean onceInQueue(int i) {
            return this.onceInQueue[i];
        }
    }
}
