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

import choco.kernel.solver.Solver;


/**
 * A general assignment constraint with constraints on the flow bounds
 */

public abstract class AbstractBipartFlow extends AbstractBipartGraph {
    protected int[] minFlow;         // flow bounds
    protected int[] maxFlow;
    protected int[] flow;
    protected boolean compatibleFlow;

    /**
     * Constructor for AbstractBipartiteFlow
     *
     * @param solver   the solver
     * @param pack  a set of parameters such as the original set of variables fvars, the contracted graph structure associated
     * and the table of indices
     */
    protected AbstractBipartFlow(Solver solver, Object[] pack) {
        super(solver, pack);
        initAbstractBipartFlow();
    }

    protected void initAbstractBipartFlow() {
        //this.flow = this.pb.getEnvironment().makeIntVector(this.nbRightVertices, 0);
        this.flow = new int[this.nbRightVertices];
        this.minFlow = new int[this.nbRightVertices];
        this.maxFlow = new int[this.nbRightVertices];
        this.left2rightArc = new int[this.nbLeftVertices + 1];
        this.queue = new IntQueue(this.nbVertices);
    }

    /**
     * match the ith variable to value j
     *
     * @param i the variable to match
     * @param j the value to assign
     */
    public void setMatch(int i, int j) {
        assert(1 <= i && i <= nbLeftVertices && 1 <= j && j <= nbRightVertices);
        //int j0 = this.refMatch.get(i);
        int j0 = this.refMatch[i];
        if (j0 != j) {
            if (j0 >= 0) {
                // i was already assign to j0, remove it!
                this.refMatch[i] = -1;
                //this.refMatch.set(i, -1);
                this.decreaseMatchingSize(j0);
            }
            // check if new assignment is compatible with capacity of value j
            //if ((this.flow.get(j) < this.maxFlow[j]) && ((j0 == -1 || (this.flow.get(j0) >= this.minFlow[j0])))) {
            if ((this.flow[j] < this.maxFlow[j]) && ((j0 == -1 || (this.flow[j0] >= this.minFlow[j0])))) {
                this.refMatch[i] = j;
                //this.refMatch.set(i, j);
                this.increaseMatchingSize(j);
            }
        }

    }

    /**
     * remove the assignment of j to the ith variable
     *
     * @param i the variable to unmatch
     * @param j the value to remove
     */
    public void deleteMatch(int i, int j) {
        //if (j == this.refMatch.get(i)) {
        if (j == this.refMatch[i]) {
            //this.refMatch.set(i, -1);
            this.refMatch[i] = -1;
            this.decreaseMatchingSize(j);
        }
    }


    /**
     * Assignment of j to the ith variable
     *
     * @param i the variable to assign
     * @param j the value
     */
    public void putRefMatch(int i, int j) {
        //this.refMatch.set(i, j);
        this.refMatch[i] = j;
    }

    /**
     * check unassignement
     *
     * @param j the jth value
     * @return true if a variable assigned to j could be unassigned
     */
    public boolean mayDiminishFlowFromSource(int j) {
        //return this.flow.get(j) > this.minFlow[j];
        return this.flow[j] > this.minFlow[j];
    }

    /**
     * check assignement
     *
     * @param j the jth value
     * @return true if a variable could be assigned to j
     */
    public boolean mayGrowFlowFromSource(int j) {
        //return this.flow.get(j) < this.maxFlow[j];
        return this.flow[j] < this.maxFlow[j];
    }

    /**
     * check if j should be assigned to other variables
     *
     * @param j the jth value
     * @return true if j has not been assigned to enough variables
     */
    public boolean mustGrowFlowFromSource(int j) {
        //return this.flow.get(j) < this.minFlow[j];
        return this.flow[j] < this.minFlow[j];
    }

    /**
     * updates the matching size when one more left vertex is matched with j
     *
     * @param j indice of the assigned value
     */
    public void increaseMatchingSize(int j) {
        //this.matchingSize.add(1);
        this.matchingSize++;
        //this.flow.set(j, this.flow.get(j) + 1);
        this.flow[j]++;
    }

    /**
     * updates the matching size when the matching is rebuilt
     *
     * @param j indice of the removed assignement
     */
    public void decreaseMatchingSize(int j) {
        //this.matchingSize.add(-1);
        this.matchingSize--;
        //this.flow.set(j, this.flow.get(j) - 1);
        this.flow[j]--;
    }

    /**
     * Search for an augmenting path
     *
     * @return eopath
     */
    public int findAlternatingPath() {
        int eopath = -1;
        int n = this.nbLeftVertices;
        int m = this.nbRightVertices;
        this.queue.init();
        for (int j = 0; j < this.nbRightVertices; j++) {
            if (this.mustGrowFlowFromSource(j)) this.queue.push(j + n);
        }
        if (this.queue.getSize() == 0) {
            this.compatibleFlow = true;
            for (int j = 0; j < this.nbRightVertices; j++) {
                if (this.mayGrowFlowFromSource(j)) this.queue.push(j + n);
            }
        } else
            this.compatibleFlow = false;
        while (this.queue.getSize() > 0) {
            int x = this.queue.pop();
            if (x >= n && x < m + n) {
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

                // <grt> Added for exception when making a flow compatible ....
                if (!compatibleFlow && this.mayDiminishFlowFromSource(x) && !this.queue.onceInQueue(n + m)) {
                    this.left2rightArc[n] = x;
                    this.queue.push(n + m);
                }
                if (shouldBreak) break;
            } else if (x < n) {
                int y = this.match(x);
                if (!this.queue.onceInQueue(y + n)) {
                    this.right2leftArc[y] = x;
                    this.queue.push(y + n);
                }
            } else if (!compatibleFlow) {
                for (int j = 0; j < this.nbRightVertices; j++) {
                    if (this.mayGrowFlowFromSource(j) && !this.queue.onceInQueue(j + n)) {
                        this.right2leftArc[j] = n;
                        this.queue.push(j + n);
                    }
                }
            }
        }
        return eopath;
    }

    /**
     * Augment flow on the current matching
     *
     * @param x left extremity of one of the matching arc
     */
    public void augment(int x) {
        int y = this.left2rightArc[x];
        if (this.compatibleFlow) {
            while (!this.mayGrowFlowFromSource(y)) {
                this.putRefMatch(x, y);
                x = this.right2leftArc[y];
                y = this.left2rightArc[x];
            }
        } else {
            int n = this.nbLeftVertices;
            while (!this.mustGrowFlowFromSource(y)) {
                this.putRefMatch(x, y);
                x = this.right2leftArc[y];
                if (x == n) {
                    // The path go through the source vertex...
                    this.increaseMatchingSize(y);
                    y = this.left2rightArc[x];
                    this.decreaseMatchingSize(y);
                    x = this.right2leftArc[y];
                }
                y = this.left2rightArc[x];
            }
        }
        this.putRefMatch(x, y);
        this.increaseMatchingSize(y);
    }
}
