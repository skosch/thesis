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

package choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.dominatorTrees;

import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.algorithms.Dominators;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.PrecsGraphView;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.VarGraphView;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.Solver;

import java.util.BitSet;
import java.util.logging.Logger;

public class DominatorView {

     protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    // le solveur choco
    protected Solver solver;

    // le nombre de sommets dans le graphe
    protected int nbNodes;

    // le graphe sur lequel on cherche les dominants
    protected VarGraphView graph;

    // un ordre partiel entre les sommets du graphe
    protected PrecsGraphView precs;

    // les sommets dominants
    protected Dominators dom;
    //protected StoredJavaBitSet[][] dominators;
    protected BitSet[][] dominators;

    protected boolean update;

    public DominatorView(Solver solver, VarGraphView graph, PrecsGraphView precs) {
        this.solver = solver;
        this.graph = graph;
        this.precs = precs;
        this.nbNodes = graph.getNbNodes();
        //this.dominators = new StoredJavaBitSet[nbNodes][nbNodes];
        this.dominators = new BitSet[nbNodes][nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) {
                //dominators[i][j] = new StoredJavaBitSet(pb.getEnvironment(), nbNodes);
                dominators[i][j] = new BitSet(nbNodes);
                dominators[i][j].set(0, nbNodes, true);
            }
        }
        this.dom = new Dominators(graph, precs);
        updateDominators();
    }

    // mise � jour des sommets dominants
    public void updateDominators() {
        //BitSet[][] newDoms = dom.computeDominators();
        dominators = dom.computeDominators();
        //updateDoms(newDoms);
    }

    private void updateDoms(BitSet[][] newDoms) {
        update = false;
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) {
                for (int k = newDoms[i][j].nextSetBit(0); k >= 0; k = newDoms[i][j].nextSetBit(k + 1)) {
                    if (!dominators[i][j].get(k)) {
                        dominators[i][j].set(k, true);
                        update = true;
                    }
                }
                for (int k = dominators[i][j].nextSetBit(0); k >= 0; k = dominators[i][j].nextSetBit(k + 1)) {
                    if (!newDoms[i][j].get(k)) {
                        dominators[i][j].set(k, false);
                        update = true;
                    }
                }//*/
            }
            //showDoms(i,nbNodes-1);
        }
    }

    /*public StoredJavaBitSet[][] getDominators() {
        return dominators;
    }*/
    public BitSet[][] getDominators() {
        return dominators;
    }

    public boolean isUpdate() {
        return update;
    }

    public void showDoms(int i) {
        for (int j = 0; j < nbNodes; j++) {
            if (j != i) showDoms(i, j);
        }
    }

    public void showDoms(int i, int j) {
        LOGGER.info("dom[" + i + "][" + j + "] = "+dominators[i][j].toString());
    }
}
