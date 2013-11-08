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

package choco.cp.solver.constraints.global.tree.structure.internalStructure.costStrutures;

import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.VarGraphView;
import choco.kernel.memory.IStateInt;



public class ShortestPaths {

    int size;

    protected int maxInt = 100000;
    protected VarGraphView inputGraph;
    protected IStateInt[][] travelTime;
    protected IStateInt[][] minTravelTime;

    public ShortestPaths(int nbNodes, IStateInt[][] travelTime, VarGraphView inputGraph, IStateInt[][] minTravelTime) {
        this.size = nbNodes;
        this.travelTime = travelTime;
        this.minTravelTime = minTravelTime;
        this.inputGraph = inputGraph;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                minTravelTime[i][j].set(travelTime[i][j].get());
            }
        }
    }

    /* Floyd algorithms : O(n^3)
    // Initializations
        Initialize the diagonal of the matrix V to 0, and the others to +infty
        for i:=1 � N
	        for each successor j of i
		        V[i,j]:=W[i,j];
    // compute iteratively the matrix V(k)
        for k := 1 � N
            for i := 1 � N
                for j := 1 � N
                    if (V[i,k]+V[k,j] < V[i,j]) then
                        V[i,j] := V[i,k] + V[k,j];
                    FS
                FP
            FP
        FP
    */
    public void computeMinPaths() {
        for (int i = 0; i < size; i++) {
            if (inputGraph.isFixedSucc(i)) {
                for (int j = 0; j < size; j++) {
                    if (j != inputGraph.getSure().getSuccessors(i).nextSetBit(0)) {
                        minTravelTime[i][j].set(maxInt);
                    } else  minTravelTime[i][j].set(travelTime[i][j].get());
                }
            } else {
                for (int j = 0; j < size; j++) minTravelTime[i][j].set(travelTime[i][j].get());
            }
        }
        for (int k = 0; k < size; k++) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (minTravelTime[i][k].get() + minTravelTime[k][j].get() < minTravelTime[i][j].get()) {
                        minTravelTime[i][j].set(minTravelTime[i][k].get() + minTravelTime[k][j].get());
                    }
                }
            }
        }
    }
}
