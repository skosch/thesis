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

package choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.algorithms;

import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.StoredBitSetGraph;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Vector;
import java.util.logging.Logger;

public class RestrictedSCC {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    protected boolean affiche = false;
    protected boolean debug = false;

    protected StoredBitSetGraph graph;

    protected int sap;
    protected int time;
    protected int[] composante;
    protected int[] prefix;
    protected BitSet reachedFirst;
    protected BitSet reachedSecond;
    protected LinkedList<Integer> listSuffix;
    protected Vector<BitSet> CFC;
    protected int numComp;
    protected int nbVertices;


    protected boolean firstLeaf;
    protected boolean canBeSAP;
    protected int[] revPrefix;
    protected int nbToReach;

    protected IStateBitSet contain;
    protected int[] minReached;
    protected BitSet reached;

    public RestrictedSCC(int sap, StoredBitSetGraph graph, IStateBitSet contain) {
        this.sap = sap;
        this.graph = graph;
        this.contain = contain;
        this.nbVertices = graph.getGraphSize();
        // l'horloge
        time = 0;
        // chaque sommet appartient a une composante
        composante = new int[nbVertices];
        for (int i = 0; i < nbVertices; i++) composante[i] = -1;
        // ordre prefixe et suffixes lors des parcours
        prefix = new int[nbVertices];
        for (int i = 0; i < nbVertices; i++) prefix[i] = 0;
        reachedFirst = new BitSet(nbVertices);
        reachedSecond = new BitSet(nbVertices);
        // liste des sommets tries par suffixes
        listSuffix = new LinkedList<Integer>();
        CFC = new Vector<BitSet>();
        // numero de la composante courrante
        numComp = -1;


        // le plus petit sommet atteignable dans la cfc
        this.minReached = new int[nbVertices];
        for (int i = 0; i < minReached.length; i++) minReached[i] = IStateInt.MAXINT;
        minReached[sap] = -2;

        this.reached = new BitSet(contain.cardinality());

        this.firstLeaf = false;
        this.canBeSAP = false;
        this.revPrefix = new int[contain.cardinality()];
        for (int i = 0; i < revPrefix.length; i++) revPrefix[i] = -1;

        this.nbToReach = 0;
        for (int i = contain.nextSetBit(0); i >= 0; i = contain.nextSetBit(i + 1)) {
            if (i != sap) nbToReach++;
        }
    }

    /**
     * methode principale: calculer le nombre de cfc crees par le retrait d'un sommet
     */
    public Vector getRestrictedSCC() {
        // debut du traitement
        if (debug) LOGGER.info("on verifie que " + sap + " peut etre un paf...");
        if (affiche || debug) LOGGER.info("====> Premier DFS:");
        for (int v = 0; v < contain.cardinality(); v++) {
            if (contain.get(v)) {
                if (prefix[v] == 0 && v != sap) {
                    if (debug) LOGGER.info(" On entre par " + v);
                    listSuffix = restrictDFS_suffix(v, v);
                }
            }
        }
        if (affiche || debug) LOGGER.info("Fin du premier DFS <====");
        if (canBeSAP) {
            if (debug) LOGGER.info(" oui!!!");
            // mise a jour des structures
            razStruct();
            // inversion de la matrice CGA
            Vector invCGA = inverse();
            if (affiche) {
                LOGGER.info("==== Calcul des cfc : le graphe invCGA ====");
                for (int i = 0; i < invCGA.size(); i++) {
                    BitSet s = (BitSet) invCGA.elementAt(i);
                    StringBuffer st = new StringBuffer();
                    st.append("cga[").append(i).append("] = ");
                    for (int j = s.nextSetBit(0); j >= 0; j = s.nextSetBit(j + 1)) {
                        st.append(j).append(" ");
                    }
                    LOGGER.info(st.toString());
                }
            }
            if (affiche || debug) LOGGER.info("====> Second DFS (invers�):");
            while (listSuffix.size() != 0) {
                int v = listSuffix.removeLast();
                if (contain.get(v) && prefix[v] == 0 && v != sap) {
                    numComp++;
                    if (affiche) LOGGER.info("    On entre par " + v);
                    restrictDFS_mark(v, invCGA);
                }
            }
            if (affiche || debug) LOGGER.info("Fin du second DFS <====");
            // le tableau composante contient la liste des cfc, on effectue un traitement pour le mettre sous forme d'un vector
            for (int i = 0; i < numComp + 1; i++) {
                BitSet cont = new BitSet(nbVertices);
                boolean add = false;
                for (int j = 0; j < contain.cardinality(); j++) {
                    if (contain.get(j) && composante[j] == i) {
                        if (affiche) LOGGER.info(j + " est dans la composante " + i);
                        add = true;
                        cont.set(j, true);
                    }
                }
                if (add) CFC.addElement(cont);
            }
            if (affiche) LOGGER.info("nbre de cfc = " + CFC.size());
            return CFC;
        } else {
            if (debug) LOGGER.info(" non!!!");
            // recalcul des cfc's
            CFC.removeAllElements();
            CFC.addElement(null);
            return CFC;
        }
    }

    // DFS restreint sur graph pour partitionner les composantes
    public LinkedList<Integer> restrictDFS_suffix(int v, int origin) {
        reached.set(v, true);
        time++;
        prefix[v] = time;
        if (debug) LOGGER.info("\t On visite " + v + " au temps " + time + " (i.e., prefix[" + v + "] = " + prefix[v] + ")");
        minReached[v] = prefix[v];
        revPrefix[prefix[v]] = v;
        IStateBitSet dom = graph.getSuccessors(v);
        for (int j = dom.nextSetBit(0); j >= 0; j = dom.nextSetBit(j + 1)) {
            if (contain.get(j) && j != sap && j != v) {
                if (prefix[j] > 0 && minReached[j] < minReached[v]) {
                    minReached[v] = minReached[j];
                    if (debug) LOGGER.info("\t\t le plus petit sommet, d�ja parcouru, atteignable depuis " + v + " ==> " + revPrefix[minReached[v]]);
                }
                if (prefix[j] == 0) {
                    if (debug) LOGGER.info("\t\t prefix[" + j + "] = " + prefix[j] + " => appel sur " + j);
                    restrictDFS_suffix(j, origin);
                }
            }
        }
        if (!firstLeaf) {
            if (debug) LOGGER.info("\t on est sur la premi�re feuille (" + v + ") on teste...");
            firstLeaf = true;
            // on sait que tout sommet de la cfc\{sap} peut-�tre atteint
            if (reached.cardinality() == nbToReach) {
                // on v�rifie que cfc\{sap} reste une cfc
                if (v != sap && minReached[v] != prefix[origin]) {
                    if (debug) LOGGER.info("\t\t la feuille " + v + " ne peut pas atteindre l'origine " + origin + " => " + sap + " peut etre un paf");
                    canBeSAP = true; // v peut �tre un sap
                }
                if (!canBeSAP) { // v ne peut pas �tre un sap
                    if (debug) LOGGER.info("\t\t le sommet " + sap + " ne peut pas �tre un paf");
                    return listSuffix;
                }
            } else {
                if (debug) LOGGER.info("\t\t tous les sommets de contain vivants n'ont pas �t� atteint => " + sap + " peut etre un paf");
                canBeSAP = true; // v peut �tre un sap
            }
        }
        listSuffix.offer(v);
        return listSuffix;
    }

    // DFS restreint sur invCGA
    public BitSet restrictDFS_mark(int v, Vector invCGA) {
        prefix[v] = time++;
        if (affiche) LOGGER.info("       On visite " + v);
        if (composante[v] == -1) composante[v] = numComp;
        BitSet listPossSucc = (BitSet) invCGA.elementAt(v);
        for (int j = listPossSucc.nextSetBit(0); j >= 0; j = listPossSucc.nextSetBit(j + 1)) {
            if (contain.get(j) && prefix[j] == 0 && j != sap) restrictDFS_mark(j, invCGA);
        }
        reachedSecond.set(v, true);
        return reachedSecond;
    }

    // remise a zero de la struct
    public void razStruct() {
        // l'horloge
        time = 0;
        // ordre prefixe
        prefix = new int[nbVertices];
        for (int i = 0; i < nbVertices; i++) prefix[i] = 0;

        for (int i = 0; i < minReached.length; i++) minReached[i] = IStateInt.MAXINT;
        minReached[sap] = -2;

        this.reached = new BitSet(contain.cardinality());

        this.nbToReach = 0;
        for (int i = contain.nextSetBit(0); i >= 0; i = contain.nextSetBit(i + 1)) {
            if (i != sap) nbToReach++;
        }
    }

    // inversion de la matrice
    public Vector inverse() {
        Vector<BitSet> invCGA = new Vector<BitSet>(nbVertices);
        for (int i = 0; i < nbVertices; i++) {
            BitSet contain = new BitSet(nbVertices);
            invCGA.addElement(contain);
        }
        for (int i = 0; i < nbVertices; i++) {
            IStateBitSet dom = graph.getSuccessors(i);
            for (int j = dom.nextSetBit(0); j >= 0; j = dom.nextSetBit(j + 1)) {
                invCGA.elementAt(j).set(i, true);
            }
        }
        return invCGA;
    }
}
