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

package choco.cp.solver.constraints.global.tree.filtering.structuralFiltering;


import choco.cp.solver.constraints.global.tree.filtering.AbstractPropagator;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomain;

import java.util.BitSet;


public class Incomparability extends AbstractPropagator {

    public Incomparability(Object[] params) {
        super(params);
    }

    public String getTypePropag() {
        return "Incomp propagation";
    }

    public boolean feasibility() {
        IStateBitSet sources = precs.getSrcNodes();
        for (int w = sources.nextSetBit(0); w >= 0; w = sources.nextSetBit(w + 1)) {
            BitSet D_w = precs.getDescendants(w);
            for (int u = D_w.nextSetBit(0); u >= 0; u = D_w.nextSetBit(u + 1)) {
                IStateBitSet I_u = nodes[u].getIncomparableNodes();
                for (int v = I_u.nextSetBit(0); v >= 0; v = I_u.nextSetBit(v + 1)) {
                    if (D_w.get(v)) {
                        if (affiche)
                            LOGGER.info("Violation incomp : inc(" + u + "," + v + ") VS desc_" + w + " = " + D_w.toString());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * <p> two filtering rules are applied for the incomparability constraints: </p>
     *
     * <blockquote> 1- remove infeasible potential roots when the number of trees is fixed to 1. </blockquote>
     * <blockquote> 2- for each potential arc (u,v) in the graph, if there exists a node u_d in the mandatory
     * descendants of u and a node a_v in the mandatory ancestors of v such that u_d and a_v are incomparable then,
     * the arc (u,v) is infeasible according to the interaction between precedence and incomparability constraints.
     * </blockquote> 
     * @throws choco.kernel.solver.ContradictionException
     */
    public void filter() throws ContradictionException {
        filterAccordingToNtree();
        for (int u = 0; u < nbVertices; u++) {
            if (!nodes[u].getSuccessors().isInstantiated()) {
                IntDomain dom = nodes[u].getSuccessors().getDomain();
                DisposableIntIterator iter = dom.getIterator();
                try{
                while (iter.hasNext()) {
                    int v = iter.next();
                    if (u != v) filteringAccordingToPrecsAndIncs(u, v);
                }
                }finally {
                    iter.dispose();
                }
            }
        }
    }

    private void filterAccordingToNtree() {
        if (tree.getNtree().isInstantiatedTo(1)) {
            for (int i = 0; i < nbVertices; i++) {
                if (nodes[i].getSuccessors().canBeInstantiatedTo(i)) {
                    if (nodes[i].getIncomparableNodes().cardinality() != 0) {
                        if (affiche)
                            LOGGER.info("1- filterAccordingToNtree(): l'arc (" + i + "," + i + ") est impossible");
                        int[] arc = {i, i};
                        propagateStruct.addRemoval(arc);
                    } else {
                        for (int j = 0; j < nbVertices; j++) {
                            if (nodes[j].getIncomparableNodes().get(i)) {
                                if (affiche)
                                    LOGGER.info("2- filterAccordingToNtree(): l'arc (" + i + "," + i + ") est impossible");
                                int[] arc = {i, i};
                                propagateStruct.addRemoval(arc);
                            }
                        }
                    }
                }
            }
        }
    }

    private void filteringAccordingToPrecsAndIncs(int u, int v) {
        int[] arc = {u, v};
        BitSet A_u = precs.getAncestors(u);
        A_u.set(u,true);
        BitSet D_v = precs.getDescendants(v);
        D_v.set(v,true);
        BitSet A_v = precs.getAncestors(v);
        A_v.set(v,true);
        BitSet Dr_u = precs.getDescendants(u);
        Dr_u.set(u,false);
        for (int v_a = A_v.nextSetBit(0); v_a >= 0; v_a = A_v.nextSetBit(v_a + 1)) {
            for (int u_d = Dr_u.nextSetBit(0); u_d >= 0; u_d = Dr_u.nextSetBit(u_d + 1)) {
                if (v_a < u_d && nodes[v_a].getIncomparableNodes().get(u_d)) {
                    if (affiche)
                        LOGGER.info("1- filteringAccordingToPrecsAndIncs(): l'arc (" + u + "," + v + ") est impossible");
                    propagateStruct.addRemoval(arc);
                    return;
                }
                if (u_d < v_a && nodes[u_d].getIncomparableNodes().get(v_a)) {
                    if (affiche)
                        LOGGER.info("2- filteringAccordingToPrecsAndIncs(): l'arc (" + u + "," + v + ") est impossible");
                    propagateStruct.addRemoval(arc);
                    return;
                }
            }
        }
        for (int u_a = A_u.nextSetBit(0); u_a >= 0; u_a = A_u.nextSetBit(u_a + 1)) {
            for (int v_d = D_v.nextSetBit(0); v_d >= 0; v_d = D_v.nextSetBit(v_d + 1)) {
                if (u_a < v_d && nodes[u_a].getIncomparableNodes().get(v_d)) {
                    if (affiche)
                        LOGGER.info("3- filteringAccordingToPrecsAndIncs(): l'arc (" + u + "," + v + ") est impossible");
                    propagateStruct.addRemoval(arc);
                    return;
                }
                if (v_d < u_a && nodes[v_d].getIncomparableNodes().get(u_a)) {
                    if (affiche)
                        LOGGER.info("4- filteringAccordingToPrecsAndIncs(): l'arc (" + u + "," + v + ") est impossible");
                    propagateStruct.addRemoval(arc);
                    return;
                }
            }
        }
    }

}
