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

import java.util.BitSet;
import java.util.List;

import choco.kernel.model.variables.MultipleVariables;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;

/*
 * User:    charles
 * Date:    26 août 2008
 */
public class TreeParametersObject extends MultipleVariables {

    IntegerVariable nTree;
    int nbNodes;
    TreeNodeObject[] nodes;
    IntegerVariable nproper;
    IntegerVariable objective;
    List<BitSet[]> graphs;
    List<int[][]> matrix;
    int[][] travel;


    public TreeParametersObject(int nbNodes, IntegerVariable nTree, IntegerVariable nproper,
                                IntegerVariable objective, List<BitSet[]> graphs, List<int[][]> matrix, int[][] travel) {
        super();
    	this.nTree = nTree;
    	this.nbNodes = nbNodes;
        this.nproper = nproper;
        this.objective = objective;
        this.graphs = graphs;
        this.matrix = matrix;
        this.travel = travel;
        this.nodes = new TreeNodeObject[this.nbNodes];
        final Variable[] vars = new Variable[this.nbNodes+3];
        vars[0]=nTree;
        vars[1]=nproper;
        vars[2]=objective;
        for (int i = 0; i < this.nbNodes; i++){
            this.nodes[i] = new TreeNodeObject(i, nbNodes, graphs, matrix);
            vars[3+i] = this.nodes[i];
        }
        setVariables(vars);
    }


    public IntegerVariable[] getSuccVars(){
        IntegerVariable[] succVars = new IntegerVariable[nbNodes];
        for (int i = 0; i < succVars.length; i++) {
            succVars[i] = nodes[i].getSuccessors();
        }
        return succVars;
    }

    public IntegerVariable getNTree() {
        return nTree;
    }

    public int getNbNodes() {
        return nbNodes;
    }

    public TreeNodeObject[] getNodes() {
        return nodes;
    }

    public IntegerVariable getNproper() {
        return nproper;
    }

    public IntegerVariable getObjective() {
        return objective;
    }

    public List<BitSet[]> getGraphs() {
        return graphs;
    }

    public List<int[][]> getMatrix() {
        return matrix;
    }

    public int[][] getTravel() {
        return travel;
    }
}
