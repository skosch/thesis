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


import choco.kernel.memory.IStateBitSet;

import java.util.BitSet;
import java.util.logging.Level;
public class OrderedGraphDeduction extends AbstractDeduction {

    public OrderedGraphDeduction(Object[] params) {
        super(params);
    }

    /**
     * the main method that update the precedence constraints according to the different parts of the tree constraint
     */
    public void updateOrderedGraphWithDeductions() {
        update = false;
        compatible = true;
        addFromSureGraph();
        updatePrecs();
        addPrecsFromDoms();
        addPrecsFromGraph();
        updatePrecsWithCondPrecs();
        updatePrecsWithIncs();
    }

    /**
     * add each required arc of the graph in the precedence constraint structure
     */
    private void addFromSureGraph() {
        for (int i = 0; i < nbVertices; i++) {
            if (inputGraph.getSure().getSuccessors(i).cardinality() == 1) {
                int j = inputGraph.getSure().getSuccessors(i).nextSetBit(0);
                if (i != j && !precs.getSuccessors(i).get(j)) {
                    int[] arc = {i, j};
                    if (isCompatible(arc)) {
                        if (affiche)
                            LOGGER.log(Level.INFO, "0- ajout de l'arc: ({0}, {1}) dans Gprec", new Object[]{i,j});
                        addInStruct(arc);
                    }
                }
            }
        }
    }

    /**
     * add new precedence constraint according to the required paths in the graph
     */
    private void addPrecsFromGraph() {
        for (int i = 0; i < nbVertices; i++) {
            IStateBitSet prec = precs.getSuccessors(i);
            // node i is not yet fixed and cannot be instantiated to a loop on itself
            if (!inputGraph.isFixedSucc(i) && !inputGraph.getPotentialRoots().get(i)) {
                BitSet common = new BitSet(nbVertices);
                common.set(0, nbVertices, true);
                IStateBitSet maybeSucc = inputGraph.getMaybe().getSuccessors(i);
                for (int j = maybeSucc.nextSetBit(0); j >= 0; j = maybeSucc.nextSetBit(j + 1)) {
                    if (j != i) common.and(precs.getDescendants(j));
                }
                if (common.cardinality() == 1 && !prec.get(common.nextSetBit(0))) {
                    int dest = common.nextSetBit(0);
                    int[] arc = {i, dest};
                    if (isCompatible(arc)) {
                        if (i != dest) {
                            if (affiche)
                                LOGGER.log(Level.INFO, "1- ajout de l'arc: ({0},{1}) dans Gp", new Object[]{i, dest});
                            addInStruct(arc);
                        }
                    }
                }
                if (common.cardinality() > 1) {
                    for (int dest = common.nextSetBit(0); dest >= 0; dest = common.nextSetBit(dest + 1)) {
                        common.xor(precs.getDescendants(dest));
                        if (dest != i && common.cardinality() == 0 && !prec.get(dest)) {
                            int[] arc = {i, dest};
                            if (isCompatible(arc)) {
                                if (affiche)
                                    LOGGER.log(Level.INFO, "2- ajout de l'arc: ({0},{1}) dans Gp", new Object[]{i, dest});
                                addInStruct(arc);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * move the existing precedence constraints starting from a given node i to the last node involved in a path
     * of required arcs.
     *
     */
    private void updatePrecs() {
        for (int i = 0; i < nbVertices; i++) {
            IStateBitSet mandSucc = precs.getSuccessors(i);
            if (mandSucc.cardinality() > 1) {
                // the descendants of node i restricted to the required arcs: the last node reached is recorded
                BitSet iToLfi = new BitSet(nbVertices);
                iToLfi.set(i, true);
                int lfi = i;
                Boolean testLoop = true;
                while (inputGraph.isFixedSucc(lfi) && testLoop) {
                    int slfi = inputGraph.getSure().getSuccessors(lfi).nextSetBit(0);
                    if (!iToLfi.get(slfi) && slfi != lfi) {
                        lfi = slfi;
                        iToLfi.set(lfi, true);
                    } else testLoop = false;
                }
                if (lfi != i) {
                    for (int j = mandSucc.nextSetBit(0); j >= 0; j = mandSucc.nextSetBit(j + 1)) {
                        int[] arc = {lfi, j};
                        if (lfi != j && !iToLfi.get(j)) {
                            if (isCompatible(arc)) {
                                if (affiche)
                                    LOGGER.log(Level.INFO, "Struct[updatePred()]: ({0},{1}) ==> ({2},{3})", new Object[]{i, j, lfi, j});
                                addInStruct(arc);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * update the precedence constraints according to the dominator nodes involved in the graph
     */
    private void addPrecsFromDoms() {
        BitSet[][] dominators = doms.getDominators();
        for (int i = 0; i < nbVertices; i++) {
            BitSet idesc = precs.getDescendants(i);
            for (int j = idesc.nextSetBit(0); j >= 0; j = idesc.nextSetBit(j + 1)) {
                if (j != i) {
                    for (int k = dominators[i][j].nextSetBit(0); k >= 0; k = dominators[i][j].nextSetBit(k + 1)) {
                        if (i != k && !precs.getDescendants(i).get(k)) {
                            if (affiche) {
                                LOGGER.log(Level.INFO, "({0} -> {1}) -> {2}", new Object[]{i, k, j});
                            }
                            int[] arc1 = {i, k};
                            if (affiche){
                                LOGGER.log(Level.INFO, "\ttry ({0}, {1}) in Gp", new Object[]{i, k});
                            }
                            if (isCompatible(arc1)) {
                                addInStruct(arc1);
                                if (affiche){
                                    LOGGER.log(Level.INFO, "\t\t1- ajoute : ({0},{1}) dans Gp", new Object[]{i, k});
                                }
                            }
                        }
                        if (k != j && !precs.getDescendants(k).get(j)) {
                            if (affiche) {
                                LOGGER.log(Level.INFO, "{0} -> ({1},{2})", new Object[]{i,k,j});
                            }
                            int[] arc2 = {k, j};
                            if (affiche){
                                LOGGER.log(Level.INFO, "\ttry ({0},{1}) in Gp", new Object[]{k,j});
                            }
                            if (isCompatible(arc2)) {
                                addInStruct(arc2);
                                if (affiche){
                                    LOGGER.log(Level.INFO, "\t\t2- ajoute: ({0},{1}) dans Gp", new Object[]{k,j});
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * update precedence constraints according to the interaction between the current precedence constraints and
     * the incomparability constraints
     */
    private void updatePrecsWithIncs() {
        for (int u = 0; u < nbVertices; u++) {
            BitSet A_u = precs.getAncestors(u);
            A_u.set(u, true);
            for (int v = precs.getSuccessors(u).nextSetBit(0); v >= 0; v = precs.getSuccessors(u).nextSetBit(v + 1)) {
                if (u != v) {
                    BitSet A_v = precs.getAncestors(v);
                    A_v.set(v, false);
                    for (int u_a = A_u.nextSetBit(0); u_a >= 0; u_a = A_u.nextSetBit(u_a + 1)) {
                        IStateBitSet inc_ua = incomp.getSuccessors(u_a);
                        for (int w = inc_ua.nextSetBit(0); w >= 0; w = inc_ua.nextSetBit(w + 1)) {
                            if (!precs.getSuccessors(w).get(v)) {
                                BitSet A_w = precs.getAncestors(w);
                                A_w.set(w, false);
                                A_w.and(A_v);
                                int[] arc = {w, v};
                                if (isCompatible(arc) && w != v) {
                                    if (A_w.cardinality() > 0) {
                                        if (affiche)
                                            LOGGER.log(Level.INFO, "Structure[updatePrecsWithIncs()]: ({0},{1}) dans Gp", new Object[]{w,v});
                                        addInStruct(arc);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if an arc, among the potentials, is compatible with the precedence constraints
     *
     * @param arc
     * @return  <code> true </code> iff the arc is compatible with the precedence constraints
     */
    private boolean isCompatible(int[] arc) {
        boolean cycle = false;
        boolean transitive = false;
        int src = arc[0];
        int dest = arc[1];
        if (src == dest) {
            compatible = false;
            if (affiche)
                LOGGER.log(Level.INFO, "\t\t1- ({0},{1}) incompatible dans Gp", new Object[]{src, dest});
            return false;
        }
        // A first dfs that checks the arc to add does not create a circuit in the precedence graph
        BitSet desc_1 = precs.getDescendants(dest);
        if (desc_1.get(src)) cycle = true;
        if (!cycle) {
            // A second dfs that checks the arc to add is not a transitive arc in the precedence graph
            BitSet desc_2 = precs.getDescendants(src);
            if (desc_2.get(dest) && !precs.getSuccessors(src).get(dest)) {
                if (affiche)
                    LOGGER.log(Level.INFO, "\t\t({0},{1}) incompatible dans Gp => transitif", new Object[]{src, dest});
                transitive = true;
            }
            return !transitive;
        } else {
            if (affiche) {
                LOGGER.info("\t\t----------------------------");
                LOGGER.info("\t\tprec(" + src + "," + dest + ") => cycle");
                precs.showAllDesc();
                LOGGER.info("\t\tD_" + dest + "(Gp) = " + desc_1.toString());
                LOGGER.info("\t\t(" + src + "," + dest + ") incompatible dans Gp => cycle");
                LOGGER.info("\t\t----------------------------");
            }
            compatible = false;
            return false;
        }
    }

    /**
     *
     * @param arc   An arc which is added in the structures related to the precedence constraints
     */
    private void addInStruct(int[] arc) {
        int src = arc[0];
        int dest = arc[1];
        update = precs.addPrec(src,dest);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// Partie relative au maintient du pickUp ////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * update the precedence graph according to the current graph and the conditionnal precedence constraints
     */
    private void updatePrecsWithCondPrecs() {
        for (int src = 0; src < nbVertices; src++) {
            IStateBitSet prec = precs.getSuccessors(src);
            IStateBitSet pickup = condPrecs.getSuccessors(src);
            // if (src,k) belongs to the precedences then, (src,dest) is added in the precedence constraints
            if (prec.cardinality() > 0) {
                for (int dest = pickup.nextSetBit(0); dest >= 0; dest = pickup.nextSetBit(dest + 1)) {
                    int[] arc = {src, dest};
                    if (isCompatible(arc)) {
                        addInStruct(arc);
                        if (affiche)
                            LOGGER.log(Level.INFO, "2- Pickup: ajout de l'arc ({0},{1}) dans Gp", new Object[]{src, dest});
                        pickup.set(dest, false);
                    }
                }
            }
            // if src node is fixed to node dest and src != dest then, for any (src,k) in the conditional precedences
            // we add this arc in the precedence constraints
            if (inputGraph.isFixedSucc(src)) {
                int dest = inputGraph.getFixedSucc(src);
                if (src != dest && pickup.cardinality() > 0) {
                    for (int succ = pickup.nextSetBit(0); succ >= 0; succ = pickup.nextSetBit(succ + 1)) {
                        if (!prec.get(succ)) {
                            int[] arc = {src, succ};
                            if (isCompatible(arc)) {
                                addInStruct(arc);
                                if (affiche)
                                    LOGGER.log(Level.INFO, "4- Pickup: ajout de l'arc ({0},{1}) dans Gp", new Object[]{src, succ});
                                pickup.set(succ, false);
                            }
                        }
                    }
                }
            }
            // if node src is not yet fixed, is not a potential root and is the origin of a conditional precedence
            // constraint then (src,dest) is added in the precedence constraints 
            if (!inputGraph.isFixedSucc(src) && !inputGraph.getPotentialRoots().get(src)) {
                for (int dest = pickup.nextSetBit(0); dest >= 0; dest = pickup.nextSetBit(dest + 1)) {
                    int[] arc = {src, dest};
                    if (isCompatible(arc)) {
                        addInStruct(arc);
                        if (affiche)
                            LOGGER.log(Level.INFO, "6- Pickup: ajout de l'arc ({0},{1}) dans Gp", new Object[]{src, dest});
                        pickup.set(dest, false);
                    }
                }
            }
        }
    }
}
