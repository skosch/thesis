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

package choco.cp.solver.constraints.global.tree.filtering.costFiltering;

import choco.cp.solver.constraints.global.tree.filtering.AbstractPropagator;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;


public class Cost extends AbstractPropagator {

    /**
     * current cost matrix associated with the graph to partition
     */
    protected IStateInt[][] cost;

    /**
     * current cost of the shortest path matrix associated with the graph to partition
     */
    protected IStateInt[][] minCost;

    /**
     * current cost of a forest covering the graph
     */
    protected IStateInt forestCost;

    public Cost(Object[] params) {
        super(params);
        this.cost = costStruct.getCost();
        this.minCost = costStruct.getMinCost();
        this.forestCost = costStruct.getForestCost();
    }

    public String getTypePropag() {
        return "Cost propagation";
    }

    public boolean feasibility() throws ContradictionException {
        forestCost = costStruct.getForestCost();
        return !(forestCost.get() > tree.getObjective().getSup() || forestCost.get() < tree.getObjective().getInf());
    }

    /**
     * remove each arc (i,j) of the graph such that the total cost a forest containing this arc exceed the objective
     * variable
     *
     * @throws ContradictionException
     */
    public void filter() throws ContradictionException {
        IStateBitSet[] numFromVertGt = struct.getInputGraph().getSure().getNumFromVertCC();
        IStateInt[] deltaCosts = costStruct.getDeltaCost();
        /*
        * (i,j) be a maybe arc, cc_i the component of i, if cost(i,j) + total - delta(cc_i) > objective,
        * (i,j) should be removed!
        */
        propagateStruct.setMinObjective(forestCost.get());
        for (int i = 0; i < nbVertices; i++) {
            IntDomainVar var = nodes[i].getSuccessors();
            if (!var.isInstantiated()) {
                int cc_i = numFromVertGt[i].nextSetBit(0);
                DisposableIntIterator values = var.getDomain().getIterator();
                try{
                while (values.hasNext()) {
                    int j = values.next();
                    if (forestCost.get() - deltaCosts[cc_i].get() + cost[i][j].get() > tree.getObjective().getSup() ||
                            forestCost.get() - deltaCosts[cc_i].get() + cost[i][j].get() < tree.getObjective().getInf()) {
                        int[] arc = {i, j};
                        propagateStruct.addRemoval(arc);
                    }
                }
                }finally {
                    values.dispose();
                }
            }
        }
    }

    public boolean allInstantiated() {
        for (int i = 0; i < nbVertices; i++) {
            if (!nodes[i].getSuccessors().isInstantiated()) return false;
        }
        return true;
    }
}
