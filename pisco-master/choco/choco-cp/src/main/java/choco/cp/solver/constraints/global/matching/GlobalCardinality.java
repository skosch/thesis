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


package choco.cp.solver.constraints.global.matching;

import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.global.matching.AbstractBipartiteFlow;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * very simple version of the cardinality constraint where the values
 * the set of values whose occurrences are counted in the interval (minValue .. maxValue)
 */
public class GlobalCardinality extends AbstractBipartiteFlow {

    /**
     * Constructor, Global cardinality constraint API
     * note : maxVal - minVal + 1 = valueMinOccurence.length = valueMaxOccurence.length
     *
     * @param vars        the variable list
     * @param minValue    smallest value that could be assigned to variable
     * @param maxValue    greatest value that could be assigned to variable
     * @param low         minimum for each value
     * @param up          maximum occurences for each value
     * @param environment
     */
    public GlobalCardinality(IntDomainVar[] vars, int minValue, int maxValue, int[] low, int[] up, IEnvironment environment) {
        super(environment, vars, vars.length, maxValue - minValue + 1);
        globalCardinalityTest(vars, minValue, maxValue, low, up);
        this.minValue = minValue;
        this.maxValue = maxValue;
        for (int i = 0; i < minFlow.length; i++) {
            minFlow[i] = low[i];
            maxFlow[i] = up[i];
        }
    }

    /**
     * Constructor, Global cardinality constraint API, short cut when smallest value equals 0
     * note : maxVal - minVal + 1 = low.length = up.length
     *
     * @param vars        the variable list
     * @param low         minimum for each value
     * @param up          maximum occurences for each value
     * @param environment
     */
    public GlobalCardinality(IntDomainVar[] vars, int[] low, int[] up, IEnvironment environment) {
        super(environment, vars, vars.length, low.length);
        globalCardinalityTest(vars, 1, low.length, low, up);
        this.minValue = 1;
        this.maxValue = low.length;
        for (int i = 0; i < minFlow.length; i++) {
            minFlow[i] = low[i];
            maxFlow[i] = up[i];
        }
    }

    private static void globalCardinalityTest(IntDomainVar[] vars, int min, int max, int[] low, int[] up) {
        if (low.length != up.length) {
            throw new SolverException("globalCardinality : low and up do not have same size");
        }
        int sumL = 0;
        for (int i = 0; i < low.length; i++) {
            sumL += low[i];
            if (low[i] > up[i]) throw new SolverException("globalCardinality : incorrect low and up (" + i + ")");
        }

        if (vars.length < sumL) {
            throw new SolverException("globalCardinality : not enough minimum values");
        }
    }

    public Object clone() throws CloneNotSupportedException {
        GlobalCardinality newc = (GlobalCardinality) super.clone();
        System.arraycopy(this.minFlow, 0, newc.minFlow, 0, this.minFlow.length);
        System.arraycopy(this.maxFlow, 0, newc.maxFlow, 0, this.maxFlow.length);
        return newc;
    }

    /**
     * implement one of the two main events:
     * when an edge is definitely removed from the bipartite assignment graph
     *
     * @param i the variable to unmatch
     * @param j the value to remove
     * @throws choco.kernel.solver.ContradictionException
     *          if the removal generates a contradiction
     */
    public void deleteEdgeAndPublish(int i, int j) throws ContradictionException {
        assert (0 <= i && i < nbLeftVertices && 0 <= j && j < nbRightVertices);
        deleteMatch(i, j);
        vars[i].removeVal(j + minValue, this, false);
    }

    /**
     * implement the other main event:
     * when an edge is definitely set in the bipartite assignment graph
     *
     * @param i the variable to assign
     * @param j the assignement value
     * @throws ContradictionException
     */
    public void setEdgeAndPublish(int i, int j) throws ContradictionException {
        assert (1 <= i && i <= nbLeftVertices && 1 <= j && j <= nbRightVertices);
        setMatch(i, j);
        vars[i].instantiate(j + minValue, this, false);
    }

    // propagation functions: reacting to events

    /**
     * Implement reaction to edge removal
     *
     * @param idx variable index
     * @param x   value to remove
     * @throws ContradictionException
     */
    public void awakeOnRem(int idx, int x) throws ContradictionException {
        deleteEdgeAndPublish(idx, x - minValue);
        constAwake(false);
    }


    /**
     * update the reference matching before redoing the strongly connected components analysis
     * when removing value in the domain of variable idx
     *
     * @param idx variable index
     */
    public void awakeOnInf(int idx) throws ContradictionException {
        for (int j = this.minValue; j < this.vars[idx].getInf(); j++) {      // TODO : verifier modif par rapport Claire..
            //for (int j = 1; j < vars[idx].getInf() ; j++) {
            deleteMatch(idx, j - minValue);
        }
        constAwake(false);
    }

    /**
     * update the reference matching before redoing the strongly connected components analysis
     * when removing value in the domain of variable idx
     *
     * @param idx variable index
     * @throws ContradictionException
     */
    public void awakeOnSup(int idx) throws ContradictionException {
        for (int j = vars[idx].getSup() + 1; j <= maxValue; j++) {
            deleteMatch(idx, j - minValue);
        }
        constAwake(false);
    }

    /**
     * update the reference matching before redoing the strongly connected components analysis
     * when idx is instantiated
     *
     * @param idx variable index
     */
    public void awakeOnInst(int idx) throws ContradictionException {
        setMatch(idx, vars[idx].getVal() - minValue);
        constAwake(false);
    }

    /**
     * performing the initial propagation, reduce variables domain to the candidate assign values
     *
     * @throws ContradictionException
     */
    public void awake() throws ContradictionException {
        init();
        for (int i = 0; i < nbLeftVertices; i++) {
            vars[i].updateInf(minValue, this, false);
            vars[i].updateSup(maxValue, this, false);
        }
        propagate();
    }

    public void propagate() throws ContradictionException {
        super.propagate();
        //Bounds are checked on every edges
        for (int j = 0; j < nbRightVertices; j++) {
            if (this.flow.get(j) > this.getMaxFlow(j)
                    || this.flow.get(j) < this.getMinFlow(j)) {
                this.fail();
            }
        }


    }

    public boolean isSatisfied(int[] tuple) {
        int[] occurrences = new int[this.maxValue - this.minValue + 1];
        for (int i = 0; i < vars.length; i++) {
            occurrences[tuple[i] - this.minValue]++;
        }
        for (int i = 0; i < occurrences.length; i++) {
            int occurrence = occurrences[i];
            if ((this.minFlow[i] > occurrence) || (occurrence > this.maxFlow[i]))
                return false;
        }
        return true;
    }

    public String pretty() {
        StringBuffer buf = new StringBuffer("GCC[" + vars.length + "," + (maxValue - minValue + 1) + "]\n");
        for (int i = 0; i < vars.length; i++) {
            buf.append(vars[i].pretty());
            buf.append("\n");
        }
        return new String(buf);

    }
}
