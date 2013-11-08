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

package choco.kernel.solver.constraints.global.matching;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A general assignment constraint with constraints on the flow bounds
 */

public abstract class AbstractBipartiteFlow extends AbstractBipartiteGraph {
    protected int[] minFlow;         // flow bounds
    protected int[] maxFlow;
    protected IStateIntVector flow;  // flow on the edges from v2 to the sink
    protected boolean compatibleFlow;
    protected IStateBool compatibleSupport;

    /**
     * Constructor for AbstractBipartiteFlow
     *
     * @param environment
     * @param vars        nbLeft domain variables
     * @param nbLeft      number of domain variables to assign
     * @param nbRight     number of values for assignment
     */
    public AbstractBipartiteFlow(IEnvironment environment, IntDomainVar[] vars, int nbLeft, int nbRight) {
        super(environment, vars, nbLeft, nbRight);
        this.flow = environment.makeIntVector(this.nbRightVertices, 0);
        this.minFlow = new int[this.nbRightVertices];
        this.maxFlow = new int[this.nbRightVertices];
        this.left2rightArc = new int[this.nbLeftVertices + 1];
        this.queue = new IntQueue(this.nbVertices);
        this.compatibleSupport = environment.makeBool(true);
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < this.nbRightVertices; i++) {
            this.flow.set(i, 0);
        }
        this.compatibleSupport.set(true);
    }

    //	/**
//	 * Builds a clone of this object.
//	 *
//	 * @return
//	 * @throws CloneNotSupportedException
//	 */
//	public Object clone() throws CloneNotSupportedException {
//		AbstractBipartiteFlow newc = (AbstractBipartiteFlow) super.clone();
//		newc.initAbstractBipartiteFlow();
//		return newc;
//	}
//
//	protected void initAbstractBipartiteFlow() {
//		this.flow = this.getSolver().getEnvironment().makeIntVector(this.nbRightVertices, 0);
//		this.minFlow = new int[this.nbRightVertices];
//		this.maxFlow = new int[this.nbRightVertices];
//		this.left2rightArc = new int[this.nbLeftVertices + 1];
//		this.queue = new IntQueue(this.nbVertices);
//		this.compatibleSupport = this.getSolver().getEnvironment().makeBool(true);
//	}

    protected int getMinFlow(int j) {
        return minFlow[j];
    }

    protected int getMaxFlow(int j) {
        return maxFlow[j];
    }

    /**
     * match the ith variable to value j
     *
     * @param i the variable to match
     * @param j the value to assign
     */
    public void setMatch(int i, int j) {
        assert (0 <= i && i < nbLeftVertices && 0 <= j && j < nbRightVertices);
        int j0 = this.refMatch.get(i);
        if (j0 != j) {
            if (j0 >= 0) {
                // i was already assign to j0, remove it!
                this.refMatch.set(i, -1);
                this.decreaseMatchingSize(j0);
            }
            // check if new assignment is compatible with capacity of value j
            if ((this.flow.get(j) < this.getMaxFlow(j))) {
                this.refMatch.set(i, j);
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
        assert (0 <= i && i < nbLeftVertices && 0 <= j && j < nbRightVertices);
        if (j == this.refMatch.get(i)) {
            this.refMatch.set(i, -1);
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
        this.refMatch.set(i, j);
    }

    /**
     * check unassignement
     *
     * @param j the jth value
     * @return true if a variable assigned to j could be unassigned
     */
    public boolean mayDiminishFlowFromSource(int j) {
        return this.flow.get(j) > this.getMinFlow(j);
    }

    /**
     * check assignement
     *
     * @param j the jth value
     * @return true if a variable could be assigned to j
     */
    public boolean mayGrowFlowFromSource(int j) {
        return this.flow.get(j) < this.getMaxFlow(j);
    }

    /**
     * check if j should be assigned to other variables
     *
     * @param j the jth value
     * @return true if j has not been assigned to enough variables
     */
    public boolean mustGrowFlowFromSource(int j) {
        return this.flow.get(j) < this.getMinFlow(j);
    }

    /**
     * updates the matching size when one more left vertex is matched with j
     *
     * @param j indice of the assigned value
     */
    public void increaseMatchingSize(int j) {
        this.matchingSize.add(1);
        this.flow.set(j, this.flow.get(j) + 1);
        // We must check if this is still possible ...
        int delta = flow.get(j) - this.getMaxFlow(j);
        if (delta > 0) {
            this.compatibleSupport.set(false);
        }
    }

    /**
     * updates the matching size when the matching is rebuilt
     *
     * @param j indice of the removed assignement
     */
    public void decreaseMatchingSize(int j) {
        this.matchingSize.add(-1);
        this.flow.set(j, this.flow.get(j) - 1);
        // We must check if this is still possible ...
        int delta = this.getMinFlow(j) - flow.get(j);
        if (delta > 0) {
            this.compatibleSupport.set(false);
        }
    }

    /**
     * Search for an augmenting path
     *
     * @return
     */
    public int findAlternatingPath() {
        // /!\  Logging statements really decrease performance
        //LOGGER.log(Level.INFO, "Search for an augmenting path to grow matching above {0} nodes", this.matchingSize);
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
            //LOGGER.log(Level.FINE, "FIFO: pop {0}", x);
            if (x >= n && x < m + n) {
                x -= n;
                boolean shouldBreak = false;
                int[] yy = this.mayInverseMatch(x);
                for (int i = 0; i < yy.length; i++) { // For each value y in mayInverseMatch(x)
                    int y = yy[i];
                    if (this.mayGrowFlowBetween(x, y) && !this.queue.onceInQueue(y)) {
                        //if (LOGGER.isLoggable(Level.FINE)) LOGGER.log(Level.FINE, "{0}.{1} [vs. {2}]", new Object[]{y, x, this.match(y)});
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
                // assert (! this.mayGrowFlowToSink(x))
                int y = this.match(x);
                // assert (y >= 0)
                // assert (this.mayDiminishFlowBetween(y,x))
                if (!this.queue.onceInQueue(y + n)) {
                    //if (LOGGER.isLoggable(Level.FINE)) LOGGER.log(Level.FINE, "{0} # {1}", new Object[]{x, y});
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
        //LOGGER.log(Level.INFO, "Found an alternating path ending in {0} (-1 if none).", eopath);
        return eopath;
    }

    /**
     * Augment flow on the current matching
     *
     * @param x left extremity of one of the matching arc
     */
    public void augment(int x) {
        // /!\  Logging statements really decrease performance
        int y = this.left2rightArc[x];
        // TODO not in ice claire
        if (this.compatibleFlow) {
            while (!this.mayGrowFlowFromSource(y)) {
                //if (LOGGER.isLoggable(Level.FINE)) LOGGER.log(Level.FINE, "Add {0}.{1}", new Object[]{x,y});
                this.putRefMatch(x, y);
                x = this.right2leftArc[y];
                //if (LOGGER.isLoggable(Level.FINE)) LOGGER.log(Level.FINE, "Rem {0}.{1}", new Object[]{x,y});
                //assert (this.match(x) == y);
                y = this.left2rightArc[x];
                //assert (y >= 0);
            }
        } else {
            int n = this.nbLeftVertices;
            int m = this.nbRightVertices;
            while (!this.mustGrowFlowFromSource(y)) {
                //if (LOGGER.isLoggable(Level.FINE)) LOGGER.log(Level.FINE, "Add {0}.{1}", new Object[]{x,y});
                this.putRefMatch(x, y);
                x = this.right2leftArc[y];
                if (x == n) {
                    // The path go through the source vertex...
                    this.increaseMatchingSize(y);
                    y = this.left2rightArc[x];
                    this.decreaseMatchingSize(y);
                    x = this.right2leftArc[y];
                }
                //if (LOGGER.isLoggable(Level.FINE)) LOGGER.log(Level.FINE, "Rem {0}.{1}", new Object[]{x,y});
                //assert (this.match(x) == y);
                y = this.left2rightArc[x];
                //assert (y >= 0);
            }
        }
        //if (LOGGER.isLoggable(Level.FINE)) LOGGER.log(Level.FINE, "[Matching]Add {0}.{1}", new Object[]{x,y});
        this.putRefMatch(x, y);
        this.increaseMatchingSize(y);

        //		if (LOGGER.isLoggable(Level.FINE)) {
        //			for (int i = 0; i < this.nbRightVertices; i++) {
        //				LOGGER.log(Level.FINE, "Flow between {0} and source: {1}", new Object[]{i, flow.get(i)});
        //			}
        //		}
    }

    protected void removeUselessEdges() throws ContradictionException {
        if (!compatibleSupport.get()) {
            this.matchingSize.set(0);
            for (int i = 0; i < this.nbLeftVertices; i++) {
                this.refMatch.set(i, -1);
            }
            for (int j = 0; j < this.nbRightVertices; j++) {
                this.flow.set(j, 0);
            }
            this.augmentFlow();
        }
        super.removeUselessEdges();
    }
}
