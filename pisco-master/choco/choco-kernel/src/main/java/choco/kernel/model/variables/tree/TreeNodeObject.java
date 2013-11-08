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

package choco.kernel.model.variables.tree;

import choco.Options;
import choco.kernel.model.variables.MultipleVariables;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;

import java.util.BitSet;
import java.util.List;

import static choco.Choco.makeIntVar;

/*
 * User:    charles
 * Date:    26 août 2008
 */
public class TreeNodeObject extends MultipleVariables{

    /**
     * index of the current node
     */
    protected int idx;

    /**
     * an integer variable that depicts the potential sucessor nodes of the current node (by indices)
     */
    protected IntegerVariable successors;

    /**
     * an integer variable that depicts the indegree of the current node
     */
    protected IntegerVariable inDegree;

    /**
     * an integer variable that depicts the starting time from the current node
     */
    protected IntegerVariable timeWindow;

    public TreeNodeObject(int idx, int nbNodes, List<BitSet[]> graphs, List<int[][]> matrix) {
        super();
    	this.idx = idx;
        this.successors = makeIntVar("next_" + idx, 0, nbNodes-1, Options.V_ENUM);
        for (int i = 0; i < nbNodes; i++) {
            if (!graphs.get(0)[idx].get(i)) this.successors.removeVal(i);
        }
        this.inDegree = makeIntVar("deg_" + idx, matrix.get(0)[idx][0], matrix.get(0)[idx][1],
                Options.V_BOUND, Options.V_NO_DECISION);
        this.timeWindow = makeIntVar("tw_" + idx, matrix.get(1)[idx][0], matrix.get(1)[idx][1],
                Options.V_BOUND, Options.V_NO_DECISION);
        setVariables(new Variable[]{ this.successors, this.inDegree, this.timeWindow});
    }


    public IntegerVariable getSuccessors() {
        return successors;
    }

    public int getIdx() {
        return idx;
    }

    public IntegerVariable getInDegree() {
        return inDegree;
    }

    public IntegerVariable getTimeWindow() {
        return timeWindow;
    }
}
