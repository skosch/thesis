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
import choco.kernel.memory.IStateIntVector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A subclass of AbtractBipartiteGraph restricted only to matchings
 * (and not flows).
 */
public abstract class AbstractBipartiteMatching
        extends AbstractBipartiteGraph {
    /**
     * Vector with the reverse matching.
     */
    protected IStateIntVector refInverseMatch;
    // the reverse assignment is stored

    /**
     * Builds a new instance for the specified vars.
     *
     * @param environment
     * @param vars        the variables
     * @param nbLeft      number of nodes in the first part of the bipartite matching
     * @param nbRight     number of nodes in the second part
     */
    public AbstractBipartiteMatching(IEnvironment environment, final IntDomainVar[] vars,
                                     final int nbLeft, final int nbRight) {
        super(environment, vars, nbLeft, nbRight);
        this.refInverseMatch = environment.makeIntVector(this.nbRightVertices, -1);
    }
//
//  /**
//   * Builds a copy of this contraint.
//   * @return a clone of this constraint
//   * @throws CloneNotSupportedException if an error occurs during cloning
//   */
//  public Object clone() throws CloneNotSupportedException {
//    AbstractBipartiteMatching newc = (AbstractBipartiteMatching) super.clone();
//    newc.initAbstractBipartiteMatching();
//    return newc;
//  }

//  /**
//   * Initializes the matching by building the backtrackable vector for the
//   * right vertices values.
//   */
//  public void initAbstractBipartiteMatching() {
//    this.refInverseMatch =
//        this.getSolver().getEnvironment()
//        .makeIntVector(this.nbRightVertices, -1);
//  }


    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < nbRightVertices; i++) {
            this.refInverseMatch.set(i, -1);
        }
    }

    /**
     * Accessing the left vertex matched to j.
     *
     * @param j the vertex
     * @return the left vertex matched to j
     */
    public int inverseMatch(final int j) {
        return this.refInverseMatch.get(j);
    }

    /**
     * Matching size has been increase by 1.
     *
     * @param j useless here
     */
    public void increaseMatchingSize(final int j) {
        this.matchingSize.set(this.matchingSize.get() + 1);
    }

    /**
     * Matching size has been decrease by 1.
     *
     * @param j useless here
     */
    public void decreaseMatchingSize(final int j) {
        this.matchingSize.set(this.matchingSize.get() - 1);
    }

    /**
     * Removing the arc i-j from the reference matching.
     *
     * @param i the left vertex
     * @param j the right vertex
     */
    public void deleteMatch(final int i, final int j) {
        if (j == this.refMatch.get(i)) {
            this.refMatch.set(i, -1);
            this.refInverseMatch.set(j, -1);
            this.decreaseMatchingSize(j);
        }
    }

    /**
     * Adding the arc i-j in the reference matching without any updates.
     *
     * @param i the left vertex
     * @param j the right vertex
     */
    public void putRefMatch(final int i, final int j) {
        this.refMatch.set(i, j);
        this.refInverseMatch.set(j, i);
    }

    /**
     * Adding the arc i-j in the reference matching.
     *
     * @param i the left vertex
     * @param j the right vertex
     */
    public void setMatch(final int i, final int j) {
        int j0 = this.refMatch.get(i);
        int i0 = this.refInverseMatch.get(j);
        if (j0 != j) {
            // assert (i0 != i);
            if (j0 >= 0) {
                this.refInverseMatch.set(j0, -1);
                this.decreaseMatchingSize(j0);
            }
            if (i0 >= 0) {
                this.refMatch.set(i0, -1);
                this.decreaseMatchingSize(j);
            }
            this.refMatch.set(i, j);
            this.refInverseMatch.set(j, i);
            this.increaseMatchingSize(j);
        }
    }

    /**
     * Checks if the flow can be decreased between source and a vertex.
     *
     * @param j the vertex
     * @return whether the flow from the source to j (a right vertex)
     *         may be decreased
     */
    public boolean mayDiminishFlowFromSource(final int j) {
        return this.refInverseMatch.get(j) != -1;
    }

    /**
     * Checks if the flow can be increased between source and a vertex.
     *
     * @param j the vertex
     * @return whether the flow from the source to j (a right vertex)
     *         may be increased
     */
    public boolean mayGrowFlowFromSource(final int j) {
        return this.refInverseMatch.get(j) == -1;
    }

    /**
     * Checks if the flow must be increased between the source and a vertex.
     *
     * @param j the vertex
     * @return whether the flow from the source to j (a right vertex)
     *         must be increased in order to get a maximal
     *         (sink/left vertex set saturating) flow
     */
    public boolean mustGrowFlowFromSource(final int j) {
        return false; // TODO in not in claire ice, cf code below
        //    assert(1 <= j & j <= c.nbRightVertices),
        //    inverseMatch(c,j) = 0
    }
}
