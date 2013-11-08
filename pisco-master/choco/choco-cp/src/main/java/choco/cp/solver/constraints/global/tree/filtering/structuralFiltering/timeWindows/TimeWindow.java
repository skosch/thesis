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

package choco.cp.solver.constraints.global.tree.filtering.structuralFiltering.timeWindows;

import choco.cp.solver.constraints.global.tree.filtering.AbstractPropagator;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;



public class TimeWindow extends AbstractPropagator {

    /**
     * current traveltime matrix associated with the graph to partition
     */
    protected IStateInt[][] travelTime;

    /**
     * current cost of the minimum travel time matrix associated with the graph to partition
     */
    protected IStateInt[][] minTravelTime;

    /**
     * propagator that contains the filetring rules directly derived from the graph to partition
     */
    protected DirectedPropag propagatePossGraph;

    /**
     * propagator that contains the filtering rules derived from the interaction with the precedence constraints
     */
    protected OrderedGraphPropag propagateOrderedGraph;


    /**
     * Constructor: build a framework to propagate filtering rules related to the time windows constraints.
     *
     * @param params a set of parameters describing each part of the global tree constraint
     *
     */
    public TimeWindow(Object[] params) {
        super(params);
        this.travelTime = costStruct.getCost();
        this.minTravelTime = costStruct.getMinCost();
        this.propagatePossGraph = new DirectedPropag(travelTime,inputGraph, nodes, propagateStruct);
        this.propagateOrderedGraph = new OrderedGraphPropag(travelTime, minTravelTime, precs, nodes, propagateStruct);
    }

    public boolean feasibility() throws ContradictionException {
        return true;
    }

    public void filter() throws ContradictionException {
        // propagate the different filtering rules
        this.propagatePossGraph.applyTWfiltering();
        this.propagateOrderedGraph.applyTWfiltering();
        this.propagatePossGraph.applyGraphFiltering();
    }

    public String getTypePropag() {
        return "Time Window propagation";
    }

}
