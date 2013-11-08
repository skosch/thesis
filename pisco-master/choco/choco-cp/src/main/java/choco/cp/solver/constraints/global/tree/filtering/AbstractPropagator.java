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


import choco.cp.solver.constraints.global.tree.structure.inputStructure.Node;
import choco.cp.solver.constraints.global.tree.structure.inputStructure.TreeParameters;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.StructuresAdvisor;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.costStrutures.CostStructure;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.dominatorTrees.DominatorView;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.PrecsGraphView;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.StoredBitSetGraph;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.VarGraphView;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;

import java.io.IOException;
import java.util.logging.Logger;


public abstract class AbstractPropagator {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * Choco solver embedding the tree constraint
     */
    protected Solver solver;

    /**
     * attributes
     */
    protected TreeParameters tree;
    protected Node[] nodes;

    /**
     * a table that manage the indices of the variables involved in the tree constraint
     */
    protected int[] indices;

    /**
     * structure advisor
     */
    protected StructuresAdvisor struct;

    /**
     * cost structure advisor
     */
    protected CostStructure costStruct;

    /**
     * structure that manage removals
     */
    protected RemovalsAdvisor propagateStruct;

    /**
     * boolean for debug and show a trace of the execution
     */
    protected boolean affiche;

    /**
     * total number of nodes involved in the graph
     */
    protected int nbVertices;

    /**
     * data structure related to the graph representation of the input graph
     */
    protected VarGraphView inputGraph;

    /**
     * data structure related to the graph representation of the partial order
     */
    protected PrecsGraphView precs;

    /**
     * data structure related to the graph representation of the dominator nodes of this graph
     */
    protected DominatorView doms;

    /**
     * data structure related to the graph representation of the incomparability constraint
     */
    protected StoredBitSetGraph incomp;

    /**
     * data structure related to the graph representation of the conditional partial order
     */
    protected StoredBitSetGraph condPrecs;

    /**
     * Constructor: abstract propagator structure
     *
     * @param params    a set of parameters describing each part of the global tree constraint
     */
    protected AbstractPropagator(Object[] params) {
        this.solver = (Solver) params[0];
        this.tree = (TreeParameters) params[1];
        this.indices = (int[]) params[2];
        this.struct = (StructuresAdvisor) params[3];
        this.costStruct = (CostStructure) params[4];
        this.propagateStruct = (RemovalsAdvisor) params[5];
        this.affiche = (Boolean) params[6];
        this.nodes = tree.getNodes();
        this.nbVertices = nodes.length;
        this.inputGraph = struct.getInputGraph();
        this.precs = struct.getPrecs();
        this.doms = struct.getDoms();
        this.incomp = struct.getIncomp();
        this.condPrecs = struct.getCondPrecs();
    }

    public abstract String getTypePropag();

    /**
     * a generic method that manage the filtering methods
     *
     * @return  <code> false </code> iff an inconsistency is detected
     * @throws choco.kernel.solver.ContradictionException
     * @throws IOException
     */
    public boolean applyConstraint() throws ContradictionException, IOException {
        if (!feasibility()) {
            if (affiche) LOGGER.info("==> Fail() on feasibility() test");
            return false;
        } else {
            filter();
            return true;
        }
    }

    /**
     * check the consistency of the filtering rules of a given propagator
     *
     * @return <code> false </code> iff the propagator detect an inconsistency
     * @throws ContradictionException
     */
    public abstract boolean feasibility() throws ContradictionException;

    /**
     * record the inconsistant values with the variables of a given propagator
     * 
     * @throws ContradictionException
     */
    public abstract void filter() throws ContradictionException;

}
