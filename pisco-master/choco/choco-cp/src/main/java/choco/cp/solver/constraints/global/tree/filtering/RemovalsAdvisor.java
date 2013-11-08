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

package choco.cp.solver.constraints.global.tree.filtering;


import choco.cp.solver.constraints.global.tree.TreeSConstraint;
import choco.cp.solver.constraints.global.tree.structure.inputStructure.Node;
import choco.cp.solver.constraints.global.tree.structure.inputStructure.TreeParameters;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.StructuresAdvisor;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;
import java.util.logging.Logger;

public class RemovalsAdvisor {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * boolean for debug and show a trace of the execution
     */
    protected boolean affiche = false;

    /**
     * boolean that allow to display the removals trace
     */
    protected boolean afficheRemovals = false;

     /**
     * check the compatibility of the udpate according to the constraint itself
     */
    protected boolean compatible;

    /**
     * total number of nodes involved in the graph
     */
    protected int nbNodes;

    /**
     * Choco solver embedding the tree constraint
     */
    protected Solver solver;

    /**
     * the tree constraint object that allow to access to the Choco solver functions like <code> fail() </code>
     */
    protected TreeSConstraint treeConst;

    /**
     * attributes
     */
    protected TreeParameters treeParams;
    protected Node[] nodes;

    /**
     * structure advisor
     */
    protected StructuresAdvisor struct;

    /**
     * lower bound of the number of trees
     */
    protected int mintree;

    /**
     * upper bound of the number of trees
     */
    protected int maxtree;

    /**
     * lower bound of the number of proper trees
     */
    protected int minprop;

    /**
     * upper bound of the number of proper trees
     */
    protected int maxprop;

    /**
     * table of the minimum starting time from each node
     */
    protected int[] minStart;

    /**
     * table of the maximum starting time from each node
     */
    protected int[] maxStart;

    /**
     * lower bound of the objective cost
     */
    protected int minObjective;

    /**
     * upper bound of the objective cost
     */
    protected int maxObjective;

    /**
     * a bitset matrix that record the set of arcs to remove
     */
    protected BitSet[] graphRem;

    /**
     * true iff the ntree variable is updated
     */
    protected boolean updateNtree;

    /**
     * true iff the nproper variable is updated
     */
    protected boolean updateNprop;

    /**
     * true iff the objective variable is updated
     */
    protected boolean updateObjective;

    /**
     * true iff a starting time from a node is updated
     */
    protected boolean updateStart;

    /**
     * true iff at least one valur is removed from the domain of a variable
     */
    protected boolean filter;

    /**
     *
     * @param solver    the Choco problem who uses the current tree constraint.
     * @param treeConst     the current Choco constraint (because we have to access to constraints primitives)
     * @param treeParams    the input data structure available in the <code> structure.inputStructure </code> package.
     * @param struct    the advisor of the internal data structures
     */
    public RemovalsAdvisor(Solver solver, TreeSConstraint treeConst,
                           TreeParameters treeParams, StructuresAdvisor struct) {
        this.solver = solver;
        this.treeConst = treeConst;
        this.struct = struct;
        this.treeParams = treeParams;
        this.nodes = treeParams.getNodes();
        this.nbNodes = treeParams.getNbNodes();
        this.initialise();
    }

    /**
     * initialize the attributes of this class according to the current state of the variables
     */
    public void initialise() {
        this.mintree = treeParams.getNtree().getInf();
        this.maxtree = treeParams.getNtree().getSup();
        this.minprop = treeParams.getNproper().getInf();
        this.maxprop = treeParams.getNproper().getSup();
        this.minObjective = treeParams.getObjective().getInf();
        this.maxObjective = treeParams.getObjective().getSup();
        this.graphRem = new BitSet[nbNodes];
        this.minStart = new int[nbNodes];
        this.maxStart = new int[nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            this.graphRem[i] = new BitSet(nbNodes);
            this.minStart[i] = nodes[i].getTimeWindow().getInf();
            this.maxStart[i] = nodes[i].getTimeWindow().getSup();
        }
        this.updateNtree = false;
        this.updateNprop = false;
        this.updateStart = false;
        this.updateObjective = false;
        this.filter = false;
    }

    /**
     * main method that synchronize the recorded value removals with the corresponding variables  
     *
     * @throws choco.kernel.solver.ContradictionException
     */
    public void startRemovals() throws ContradictionException {
        IStateBitSet[] trueGraph = struct.getInputGraph().getSure().getGraph();
        if (updateNtree && maxtree < treeParams.getNtree().getSup()) {
            filter = true;
            treeParams.getNtree().updateSup(maxtree, this.treeConst, false);
        }
        if (updateNtree && mintree > treeParams.getNtree().getInf()) {
            filter = true;
            treeParams.getNtree().updateInf(mintree, this.treeConst, false);
        }
        if (updateNprop && maxprop < treeParams.getNproper().getSup()) {
            filter = true;
            treeParams.getNproper().updateSup(maxprop, this.treeConst, false);
        }
        if (updateNprop && minprop > treeParams.getNproper().getInf()) {
            filter = true;
            treeParams.getNproper().updateInf(minprop, this.treeConst, false);
        }
        if (updateObjective && maxObjective < treeParams.getObjective().getSup()) {
            filter = true;
            treeParams.getObjective().updateSup(maxObjective, this.treeConst, false);
        }
        if (updateObjective && minObjective > treeParams.getObjective().getInf()) {
            filter = true;
            treeParams.getObjective().updateInf(minObjective, this.treeConst, false);
        }
        int left, right;
        for (int i = 0; i < nbNodes; i++) {
            IntDomainVar var_i = nodes[i].getSuccessors();
            if (updateStart) {
                if (maxStart[i] < nodes[i].getTimeWindow().getSup())
                    nodes[i].getTimeWindow().updateSup(maxStart[i], this.treeConst, false);
                if (minStart[i] > nodes[i].getTimeWindow().getInf())
                    nodes[i].getTimeWindow().updateInf(minStart[i], this.treeConst, false);
            }
            left = right = Integer.MIN_VALUE;
            for (int j = graphRem[i].nextSetBit(0); j >= 0; j = graphRem[i].nextSetBit(j + 1)) {
                if (var_i.canBeInstantiatedTo(j)) {
                    filter = true;
                    if (afficheRemovals)
                        LOGGER.info("1-Removals: suppression effective de l'arc (" + i + "," + j + ")");
//                    var_i.removeVal(j, this.treeConst, false);
                    if (j == right + 1) {
                        right = j;
                    } else {
                        var_i.removeInterval(left, right, this.treeConst, false);
                        left = right = j;
                    }
                }
                if (var_i.isInstantiatedTo(j) && i != j) {
                    if (afficheRemovals)
                        LOGGER.info("1-Removals: suppression de l'arc (" + i + "," + j + ") qui est instancie => FAIL");
                    this.treeConst.fail();
                    compatible = false;
                }
            }
            var_i.removeInterval(left, right, this.treeConst, false);
            if (var_i.isInstantiated() && !trueGraph[i].get(var_i.getVal())) {
                int j = var_i.getVal();
                IntDomainVar var_j = nodes[j].getSuccessors();
                if (var_j.canBeInstantiatedTo(i) && j != i) {
                    if (afficheRemovals)
                        LOGGER.info("2-Removals: suppression de l'arc (" + j + "," + i + ")");
                    var_j.removeVal(i, this.treeConst, false);
                    filter = true;
                }
                if (var_j.isInstantiated()) {
                    if (var_j.isInstantiatedTo(i) && i != j) {
                        if (afficheRemovals)
                            LOGGER.info("2-Removals: suppression de l'arc (" + j + "," + i + ") qui est instancie => FAIL");
                        this.treeConst.fail();
                        compatible = false;
                    }
                }
            }
        }
    }

    /**
     *
     * @return  the bitset matrix of the arc to remove
     */
    public BitSet[] getGraphRem() {
        return graphRem;
    }

    /**
     *
     * @param arc   add the arc in the removal structure
     */
    public void addRemoval(int[] arc) {
        graphRem[arc[0]].set(arc[1], true);
    }

    /**
     * update the lower bound of the node idx with the value min
     *
     * @param idx   idx of the node
     * @param min   new lower bound of the starting time
     */
    public void setMinStart(int idx, int min) {
        if (minStart[idx] < min) {
            minStart[idx] = min;
            updateStart = true;
        }
    }

    /**
     * update the upper bound of the node idx with the value max
     *
     * @param idx   idx of the node
     * @param max   new upper bound of the starting time
     */
    public void setMaxStart(int idx, int max) {
        if (maxStart[idx] > max) {
            maxStart[idx] = max;
            updateStart = true;
        }
    }

    /**
     * update the upper bound of the ntree variable with the value val
     *
     * @param val   new upper bound of the ntree variable
     * @throws ContradictionException
     */
    public void setMaxNtree(int val) throws ContradictionException {
        if (val < maxtree) {
            this.maxtree = val;
            updateNtree = true;
        }
    }

    /**
     * update the lower bound of the ntree variable with the value val
     *
     * @param val   new lower bound of the ntree variable
     * @throws ContradictionException
     */
    public void setMinNtree(int val) throws ContradictionException {
        if (val > mintree) {
            this.mintree = val;
            updateNtree = true;
        }
    }

    /**
     * update the upper bound of the nproper variable with the value val
     *
     * @param val   new upper bound of the nproper variable
     * @throws ContradictionException
     */
    public void setMaxNProper(int val) throws ContradictionException {
        if (val < maxprop) {
            this.maxprop = val;
            updateNprop = true;
        }
    }

    /**
     * update the lower bound of the nproper variable with the value val
     *
     * @param val   new lower bound of the nproper variable
     * @throws ContradictionException
     */
    public void setMinNProper(int val) throws ContradictionException {
        if (val > minprop) {
            this.minprop = val;
            updateNprop = true;
        }
    }

    /**
     * update the upper bound of the objective variable with the value val
     *
     * @param val   new upper bound of the objective variable
     * @throws ContradictionException
     */
    public void setMaxObjective(int val) throws ContradictionException {
        if (val < maxObjective) {
            this.maxObjective = val;
            updateObjective = true;
        }
    }

    /**
     * update the lower bound of the objective variable with the value val
     *
     * @param val   new lower bound of the objective variable
     * @throws ContradictionException
     */
    public void setMinObjective(int val) throws ContradictionException {
        if (val > minObjective) {
            this.minObjective = val;
            updateObjective = true;
        }
    }

    /**
     *
     * @return <code> true </code> iff a value has been removed from the domain of a variable
     */
    public boolean isFilter() {
        return filter;
    }
}
