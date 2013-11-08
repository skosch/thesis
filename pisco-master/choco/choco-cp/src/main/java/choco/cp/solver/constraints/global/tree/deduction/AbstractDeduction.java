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

package choco.cp.solver.constraints.global.tree.deduction;

import choco.cp.solver.constraints.global.tree.structure.inputStructure.Node;
import choco.cp.solver.constraints.global.tree.structure.inputStructure.TreeParameters;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.StructuresAdvisor;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.dominatorTrees.DominatorView;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.PrecsGraphView;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.StoredBitSetGraph;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.VarGraphView;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.Solver;

import java.util.logging.Logger;

public abstract class AbstractDeduction {

    protected final static Logger LOGGER  = ChocoLogging.getEngineLogger();

    protected boolean affiche = true;

    // the number nodes in the graph
    protected int nbVertices;

    protected Solver solver;
    protected TreeParameters tree;
    protected StructuresAdvisor struct;
    protected boolean update;
    protected boolean compatible;

    protected Node[] nodes;
    protected VarGraphView inputGraph;
    protected PrecsGraphView precs;
    protected DominatorView doms;
    protected StoredBitSetGraph incomp;
    protected StoredBitSetGraph condPrecs;

    
    /**
     *
     * @param params    a set of parameters describing each part of the global tree constraint
     * 
     */
    protected AbstractDeduction(Object[] params) {
        this.solver = (Solver) params[0];
        this.tree = (TreeParameters) params[1];
        this.struct = (StructuresAdvisor) params[2];
        this.update = (Boolean) params[3];
        this.compatible = (Boolean) params[4];
        this.affiche = (Boolean) params[5];

        this.nbVertices = tree.getNbNodes();
        this.nodes = tree.getNodes();
        this.inputGraph = struct.getInputGraph();
        this.precs = struct.getPrecs();
        this.doms = struct.getDoms();
        this.incomp = struct.getIncomp();
        this.condPrecs = struct.getCondPrecs();
    }

    public boolean isCompatible() {
        return compatible;
    }

    public boolean isUpdate() {
        return update;
    }
}
