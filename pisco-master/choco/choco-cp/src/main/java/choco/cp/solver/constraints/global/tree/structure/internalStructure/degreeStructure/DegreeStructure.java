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

package choco.cp.solver.constraints.global.tree.structure.internalStructure.degreeStructure;

import choco.cp.solver.constraints.global.tree.structure.inputStructure.TreeParameters;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.VarGraphView;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;
import java.util.logging.Logger;


public class DegreeStructure {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * boolean for debug and show a trace of the execution
     */
    protected boolean affiche = false;

    /**
     * Choco solver embedding the tree constraint
     */
    protected Solver solver;

    /**
     * an integer variable that depicts the number of tree allowed to partition the graph
     */
    protected TreeParameters tree;

    /**
     * data structure related to the graph representation of the input graph
     */
    protected VarGraphView graph;

    /**
     * total number of nodes involved in the graph
     */
    protected int nbVertices;

    /**
     * the number of nodes not yet fixed: the left nodes in the network flow
     */
    protected int nbLeftVertices;

    /**
     * network flow associated with the gcc
     */
    protected BitSet[] gccVars;

    /**
     * index who help to find a graph node from a network node
     */
    protected int[] indexVars;

    protected int[] OriginalMinFlow;

    protected int[] OriginalMaxFlow;

    /**
     * minimum current flow
     */
    protected int[] low;

    /**
     * maximum current flow
     */
    protected int[] up;

    protected boolean degree;

    /**
     * constructor
     *
     * @param solver    the Choco solver who uses the current tree constraint
     * @param tree  the input data structure available in the <code> structure.inputStructure </code> package
     */
    public DegreeStructure(Solver solver, TreeParameters tree, VarGraphView graph) {
        this.solver = solver;
        this.tree = tree;
        this.graph = graph;
        this.nbVertices = tree.getNbNodes();
        this.initFlow();
    }

    private void initFlow() {
        // build the network associated with a gcc constraint
        this.indexVars = new int[nbVertices];
        this.degree = true;
        for (int i = 0; i < nbVertices; i++) {
			indexVars[i] = i;
		}
        this.gccVars = new BitSet[nbVertices];
        nbLeftVertices = nbVertices;
        for (int i = 0; i < nbVertices; i++) {
            // +1 comes from the modeling of state "potential loop" in the network
            BitSet succ = new BitSet(nbVertices + 1);
            IStateBitSet next_i = graph.getGlobal().getSuccessors(i);
            for (int j = next_i.nextSetBit(0); j >= 0; j = next_i.nextSetBit(j + 1)) {
                if (j != i) {
					succ.set(j, true);
				}
                if (j == i) {
					succ.set(nbVertices, true);
				}
            }
            this.gccVars[i] = succ;
        }
        this.OriginalMinFlow = new int[nbVertices + 1];
        for (int i = 0; i < OriginalMinFlow.length; i++) {
			OriginalMinFlow[i] = 0;
		}
        this.low = new int[nbVertices + 1];
        for (int i = 0; i < low.length; i++) {
            if (i < nbVertices) {
                IntDomainVar deg_i = tree.getNodes()[i].getInDegree();
                low[i] = deg_i.getInf();
                OriginalMinFlow[i] = deg_i.getInf();
            }
            if (i == nbVertices) {
                low[i] = tree.getNtree().getInf();
                OriginalMinFlow[i] = tree.getNtree().getInf();
            }
        }
        this.OriginalMaxFlow = new int[nbVertices + 1];
        for (int i = 0; i < OriginalMaxFlow.length; i++) {
			OriginalMaxFlow[i] = 0;
		}
        this.up = new int[nbVertices + 1];
        for (int i = 0; i < up.length; i++) {
            if (i < nbVertices) {
                IntDomainVar deg_i = tree.getNodes()[i].getInDegree();
                up[i] = deg_i.getSup();
                OriginalMaxFlow[i] = deg_i.getSup();
            }
            if (i == nbVertices) {
                up[i] = tree.getNtree().getSup();
                OriginalMaxFlow[i] = tree.getNtree().getSup();
            }
        }
    }

    /**
     *
     * @return <code> true </code> iff there is any change in the structures that leads to an udpate of the degree
     * constraints
     */
    public boolean needUpdate() {
        initFlow();
        for (int i = 0; i <= nbVertices; i++) {
            int deg_i = 0;
            boolean instNull_i = (OriginalMaxFlow[i] == OriginalMaxFlow[i] && OriginalMaxFlow[i] == 0);
            for (BitSet gccVar : gccVars) {
                if (gccVar.get(i)) {
					deg_i++;
				}
            }
            if (!instNull_i && deg_i <= OriginalMaxFlow[i]) {
                return true;
            }
            if (instNull_i && deg_i > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * update the capacities of the arcs involved in the network associated with the gcc
     */
    public void updateDegree() {
        int[] mindeg = new int[low.length];
        int[] maxdeg = new int[up.length];
        updateCurrentDeg(mindeg,maxdeg);
        updateGccVars(mindeg,maxdeg);
        // update the network flow
        for (int i = 0; i <= nbVertices; i++) {
            up[i] = maxdeg[i];
            low[i] = mindeg[i];
        }
        // synchronize the degrees
        for (int i = 0; i < nbVertices; i++) {
            int nbused_i;
            int nbpotused_i;
            IStateBitSet maybePred_i = graph.getMaybe().getPredecessors(i);
            IStateBitSet surePred_i = graph.getSure().getPredecessors(i);
            if (surePred_i.get(i)) {
				nbused_i = surePred_i.cardinality() - 1;
			} else {
				nbused_i = surePred_i.cardinality();
			}
            if (maybePred_i.get(i)) {
				nbpotused_i = maybePred_i.cardinality() - 1;
			} else {
				nbpotused_i = maybePred_i.cardinality();
			}
            if (nbpotused_i < nbused_i) {
				nbpotused_i = nbused_i;
			}
            if (nbused_i > OriginalMaxFlow[i]) {
				degree = false;
			}
            if (nbpotused_i < OriginalMinFlow[i]) {
				degree = false;
			}
        }
        if (affiche) {
            LOGGER.info("*********************************");
            for (int i = 0; i < nbVertices + 1; i++) {
                LOGGER.info("deg[" + i + "] = [" + low[i] + "," + up[i] + "]");
            }
            LOGGER.info("------------------------------------------");
            for (int i = 0; i < nbVertices; i++) {
                LOGGER.info("gcc[" + i + "] = " + gccVars[i].toString());
            }
            LOGGER.info("*********************************");
        }
    }

    private void updateCurrentDeg(int[] mindeg, int[] maxdeg) {
        // udpate minFlow[] and maxFlow[] according to ntree and the graph
        int[] canBeUsed = new int[nbVertices + 1];
        int[] usedByInst = new int[nbVertices + 1];
        for (int i = 0; i < canBeUsed.length; i++) {
            canBeUsed[i] = 0;
            usedByInst[i] = 0;
        }
        for (int i = 0; i < nbVertices; i++) {
            IStateBitSet pred_i = graph.getGlobal().getPredecessors(i);
            IStateBitSet surePred_i = graph.getSure().getPredecessors(i);
            if (pred_i.get(i)) {
                canBeUsed[i] += pred_i.cardinality() - 1;
                canBeUsed[nbVertices]++;
                if (surePred_i.get(i)) {
                    usedByInst[i] += surePred_i.cardinality() - 1;
                    usedByInst[nbVertices]++;
                }
            } else {
                canBeUsed[i] += pred_i.cardinality();
                usedByInst[i] += surePred_i.cardinality();
            }
        }
        // update degree according to ntree, the graph and the precedence constraints
        for (int i = 0; i < mindeg.length; i++) {
            if (i < nbVertices) {
                if (OriginalMinFlow[i] <= usedByInst[i] && usedByInst[i] <= OriginalMaxFlow[i]) {
                    mindeg[i] = usedByInst[i];
                } else {
                    mindeg[i] = OriginalMinFlow[i];
                }
            }
            if (i == nbVertices) {
                mindeg[i] = Math.max(tree.getNtree().getInf(), Math.max(usedByInst[i], OriginalMinFlow[i]));
            }
        }
        for (int i = 0; i < maxdeg.length; i++) {
            if (i < nbVertices) {
                if (OriginalMaxFlow[i] >= canBeUsed[i] && canBeUsed[i] >= OriginalMinFlow[i]) {
                    maxdeg[i] = canBeUsed[i];
                } else {
                    maxdeg[i] = OriginalMaxFlow[i];
                }
            }
            if (i == nbVertices) {
                maxdeg[i] = Math.min(tree.getNtree().getSup(), Math.min(canBeUsed[i], OriginalMaxFlow[i]));
            }
        }
    }

    private void updateGccVars(int[] mindeg, int[] maxdeg) {
        // update the network by removing the fixed variables and the related capacities
        int nbInstVars = 0;
        for (int i = 0; i < nbVertices; i++) {
            if (graph.getSure().getSuccessors(i).cardinality() > 0) {
				nbInstVars++;
			}
        }
        nbLeftVertices = nbVertices - nbInstVars;
        this.gccVars = new BitSet[nbVertices - nbInstVars];
        this.indexVars = new int[nbVertices - nbInstVars];
        for (int i = 0; i < gccVars.length; i++) {
			gccVars[i] = new BitSet(nbVertices + 1);
		}
        for (int i = 0; i < indexVars.length; i++) {
			indexVars[i] = i;
		}
        int decal = 0;
        for (int i = 0; i < nbVertices; i++) {
            IStateBitSet maybeSucc_i = graph.getMaybe().getSuccessors(i);
            IStateBitSet sureSucc_i = graph.getSure().getSuccessors(i);
            for (int j = maybeSucc_i.nextSetBit(0); j >= 0; j = maybeSucc_i.nextSetBit(j + 1)) {
                if (i != j) {
					gccVars[i - decal].set(j, true);
				}
                if (i == j) {
					gccVars[i - decal].set(nbVertices, true);
				}
                indexVars[i - decal] = i;
            }
            for (int j = sureSucc_i.nextSetBit(0); j >= 0; j = sureSucc_i.nextSetBit(j + 1)) {
                if (j == i) {
					j = nbVertices;
				}
                decal++;
                maxdeg[j] = maxdeg[j] - 1;
                mindeg[j] = mindeg[j] - 1;
                if (maxdeg[j] < 0) {
                    maxdeg[j] = 0;
                    mindeg[j] = 0;
                }
                if (mindeg[j] < 0) {
                    mindeg[j] = 0;
                }
            }
        }
    }

    public boolean isCompatibleDegree() {
        return degree;
    }

    public int[] getLow() {
        return low;
    }

    public int[] getUp() {
        return up;
    }

    public BitSet[] getGccVars() {
        return gccVars;
    }

    public int getNbLeftVertices() {
        return nbLeftVertices;
    }

    public int[] getIndexVars() {
        return indexVars;
    }
}
