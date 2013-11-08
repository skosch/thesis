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
import choco.cp.solver.constraints.global.tree.filtering.costFiltering.Cost;
import choco.cp.solver.constraints.global.tree.filtering.structuralFiltering.Incomparability;
import choco.cp.solver.constraints.global.tree.filtering.structuralFiltering.Nproper;
import choco.cp.solver.constraints.global.tree.filtering.structuralFiltering.globalCardConstraint.GlobalCardinalityNoLoop;
import choco.cp.solver.constraints.global.tree.filtering.structuralFiltering.precedences.Precedences;
import choco.cp.solver.constraints.global.tree.filtering.structuralFiltering.timeWindows.TimeWindow;
import choco.cp.solver.constraints.global.tree.filtering.structuralFiltering.tree.Tree;
import choco.cp.solver.constraints.global.tree.structure.inputStructure.TreeParameters;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.StructuresAdvisor;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.costStrutures.CostStructure;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class FilteringAdvisor {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * boolean for debug and show a trace of the execution
     */
    protected boolean affiche;

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
    protected TreeParameters tree;

    /**
     * structure advisor
     */
    protected StructuresAdvisor struct;

    /**
     * cost structure advisor
     */
    protected CostStructure costStruct;

    /**
     * object that record the infeasible values in the different domains of the variables involved in the constraint
     */
    protected RemovalsAdvisor propagateStruct;

    /**
     * a propagation queue that initialy contains all the propagators composing the tree constraint
     */
    protected Queue<AbstractPropagator> toPropagate;

    /**
     * a propagator for the <b> pure </b> tree constraint
     */
    protected Tree propagateTree;

    /**
     * a propagator for the degree constraints. In fact, the filtering rule embedded corresponds to the filtering
     * of the <code> Global Cardinality Constraint </code>
     */
    protected GlobalCardinalityNoLoop propagateGlobalCard;

    /**
     * a propagator for the constraint that maintain a given number (not necessarily fixed) of proper tree. NB: a proper
     * tree is a tree containing at least two nodes
     */
    protected Nproper propagateNProper;

    /**
     * a propagator for the precedence constraints
     */
    protected Precedences propagatePrec;

    /**
     * a propagator for the incomparability constraints
     */
    protected Incomparability propagateIncomp;

    /**
     * a propagator for the time windows constraints associated with each nodes
     */
    protected TimeWindow propagateTW;

    /**
     * a propagator for the cost constraint associated with the set of arcs involved in the graph
     */
    protected Cost propagateCost;

    /**
     * constructor: build a filtering manager that deals with all the propagators involved in the tree constraint
     *
     * @param solver    the Choco solver who uses the current tree constraint.
     * @param treeConst     the current Choco constraint (because we have to access to constraints primitives)
     * @param tree  the input data structure available in the <code> structure.inputStructure </code> package.
     * @param struct    the advisor of the internal data structures
     * @param affiche   a boolean that allows to display the main actions done by the filtering manager
     */
    public FilteringAdvisor(Solver solver, TreeSConstraint treeConst, TreeParameters tree,
                           StructuresAdvisor struct, boolean affiche) {
        this.solver = solver;
        this.treeConst = treeConst;
        this.tree = tree;
        this.struct = struct;
        this.costStruct = struct.getCostStruct();
        this.affiche = affiche;

        this.propagateStruct = new RemovalsAdvisor(this.solver, this.treeConst, this.tree, this.struct);

        Object[] params = new Object[]{this.solver, this.tree, this.treeConst.cIndices, this.struct,
                this.costStruct, this.propagateStruct, this.affiche};

        this.toPropagate = new LinkedList<AbstractPropagator>();

        this.propagateTree = new Tree(params);
        this.toPropagate.offer(this.propagateTree);

        this.propagatePrec = new Precedences(params);
        this.toPropagate.offer(this.propagatePrec);

        this.propagateIncomp = new Incomparability(params);
        this.toPropagate.offer(this.propagateIncomp);

        this.propagateTW = new TimeWindow(params);
        this.toPropagate.offer(this.propagateTW);

        this.propagateNProper = new Nproper(params);
        this.toPropagate.offer(this.propagateNProper);

        this.propagateCost = new Cost(params);
        this.toPropagate.offer(this.propagateCost);

        // the global card const is not generified in an abstractPropagator: too bad :-(
        Object[] gccParams = new Object[]{this.tree, this.struct, this.affiche};
        this.propagateGlobalCard = new GlobalCardinalityNoLoop(this.solver, gccParams);
    }

    /**
     * the main method that allows to apply all the filtering rules of each propagator
     *
     * @return <code> true </code> iff there is at least one value removed in the domain of a variable
     * @throws choco.kernel.solver.ContradictionException
     */
    public boolean applyFiltering() throws ContradictionException {
        // initialize the removal data structure
        propagateStruct.initialise();
        // specific case of the gcc filtering algorithm
        if (struct.isUpdateDegree()) {
            if (!propagateGlobalCard.applyGCC(propagateStruct)) {
                if (affiche) LOGGER.info("ECHEC => Degree propagation");
                treeConst.fail();
            }
        }
        // queue containing all the propagators
        Queue<AbstractPropagator> currentQueue = new LinkedList<AbstractPropagator>(toPropagate);
        while (!currentQueue.isEmpty()) {
            AbstractPropagator current = currentQueue.poll();
            // apply the filtering rules of the current propagator
            if (current.feasibility()) current.filter();
            else {
                if (affiche) LOGGER.info("ECHEC => " + current.getTypePropag());
                treeConst.fail();
            }
        }
        // remove the infeasible values recorded in the removal structure 
        propagateStruct.startRemovals();
        return propagateStruct.isFilter();
    }
}
