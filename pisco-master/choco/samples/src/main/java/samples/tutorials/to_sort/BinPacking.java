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

package samples.tutorials.to_sort;


import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valiterator.DecreasingDomain;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.Solver;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import static choco.Choco.*;
import static java.text.MessageFormat.format;

/**
 * Created by IntelliJ IDEA.
 * User: Xavier Lorca
 * Date: 2 oct. 2007
 * Time: 07:42:16
 */
public class BinPacking {

    protected final static Logger LOGGER = ChocoLogging.getMainLogger();

    public int[] getRandomPackingPb(int nbObj, int capa, int seed) {
        Random rand = new Random(seed);
        int[] instance = new int[nbObj];
        for (int i = 0; i < nbObj; i++) {
            instance[i] = rand.nextInt(capa) + 1;
        }
        return instance;
    }

    public void afficheInst(int[] instance) {
        for (int j = 0; j < instance.length; j++) {
            LOGGER.info("Bin " + j + ": " + instance[j]);
        }
        LOGGER.info("");
    }

    public int computeLB(int[] instance, int capa) {
        int load = 0;
        for (int i = 0; i < instance.length; i++) {
            load += instance[i];
        }
        return (int) Math.ceil((double) load / (double) capa);
    }

    /**
     * First model
     *
     * @param n       nb bin
     * @param capaBin capacity
     * @param seed    random root
     */
    public void binPacking1(int n, int capaBin, int seed) {
        boolean keepSolving = true;
        int[] instance = getRandomPackingPb(n, capaBin, seed);
        Arrays.sort(instance);
        afficheInst(instance);
        int nbBin = computeLB(instance, capaBin);

        while (keepSolving) {
            LOGGER.info("------------------------" + nbBin + " bins");
            Model m = new CPModel();
            IntegerVariable[][] vs = new IntegerVariable[n][nbBin];
            IntegerVariable[] vars = new IntegerVariable[n * nbBin];
            IntegerVariable[] sumBin = new IntegerVariable[nbBin];
            int cpt = 0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < nbBin; j++) {
                    vs[i][j] = makeIntVar("obj " + i + "_" + j, 0, 1);
                    vars[cpt] = vs[i][j];
                    cpt++;
                }
            }
            for (int j = 0; j < nbBin; j++) {
                sumBin[j] = makeIntVar("sumBin " + j + "_" + j, 0, capaBin);
            }
            m.addVariables(Options.V_BOUND, sumBin);
            for (int j = 0; j < nbBin; j++) {
                IntegerVariable[] col = new IntegerVariable[n];
                for (int i = 0; i < n; i++) {
                    col[i] = vs[i][j];
                }
                m.addConstraint(eq(scalar(col, instance), sumBin[j]));
                m.addConstraint(leq(sumBin[j], capaBin));
            }
            for (int i = 0; i < n; i++) { // Each object has to be placed in one bin
                m.addConstraint(eq(sum(vs[i]), 1));
            }

            Solver s = new CPSolver();
            s.read(m);
            s.setVarIntSelector(new StaticVarOrder(s, s.getVar(vars)));
            s.setValIntIterator(new DecreasingDomain());
            s.solve();
            // Print of solution
            if (s.isFeasible() == Boolean.TRUE) {
                for (int j = 0; j < nbBin; j++) {
                    StringBuffer st = new StringBuffer();
                    st.append("Bin " + j + ": ");
                    int load = 0;
                    for (int i = 0; i < n; i++) {
                        if (s.getVar(vs[i][j]).isInstantiatedTo(1)) {
                            st.append(i + " ");
                            load += instance[i];
                        }
                    }
                    st.append(" - load " + load + " = " + s.getVar(sumBin[j]).getVal());
                    LOGGER.info(st.toString());
                }
                keepSolving = false;
            }
            nbBin++;
        }
    }

    /**
     * Optimize model
     *
     * @param n       nb of objects
     * @param capaBin capacity
     * @param seed
     */
    public void binPacking2(int n, int capaBin, int seed) {
        int[] instance = getRandomPackingPb(n, capaBin, seed);
        Arrays.sort(instance);
        Model m = new CPModel();
        IntegerVariable[] debut = new IntegerVariable[n];
        IntegerVariable[] duree = new IntegerVariable[n];
        IntegerVariable[] fin = new IntegerVariable[n];
        int nbBinMin = computeLB(instance, capaBin);
        for (int i = 0; i < n; i++) {
            debut[i] = makeIntVar("debut " + i, 0, n);
            duree[i] = makeIntVar("duree " + i, 1, 1);
            fin[i] = makeIntVar("fin " + i, 0, n);
        }
        IntegerVariable obj = makeIntVar("nbBin ", nbBinMin, n);
        TaskVariable[] tasks = makeTaskVarArray("t", debut, fin, duree);
        m.addConstraint(cumulativeMax(tasks, instance, capaBin));
        for (int i = 0; i < n; i++) {
            m.addConstraint(geq(obj, debut[i]));
        }

        IntegerVariable[] branchvars = new IntegerVariable[n + 1];
        System.arraycopy(debut, 0, branchvars, 0, n);
        branchvars[n] = obj;

        Solver s = new CPSolver();
        s.read(m);
        s.setVarIntSelector(new StaticVarOrder(s, s.getVar(branchvars)));
        s.minimize(s.getVar(obj), false);
        LOGGER.info("------------------------ " + (s.getVar(obj).getVal() + 1) + " bins");
        if (s.isFeasible() == Boolean.TRUE) {
            for (int j = 0; j <= s.getVar(obj).getVal(); j++) {
                StringBuffer st = new StringBuffer();
                st.append(format("Bin {0}: ", j));
                int load = 0;
                for (int i = 0; i < n; i++) {
                    if (s.getVar(debut[i]).isInstantiatedTo(j)) {
                        st.append(format("{0} ", i));
                        load += instance[i];
                    }
                }
                st.append(format(" - load {0}", load));
                LOGGER.info(st.toString());
            }
        }
    }

    public void binPacking3(int n, int capaBin, int seed) {
        LOGGER.severe("not implemented");
//        int[] instance = getRandomPackingPb(n, capaBin, seed);
//        CPpack pack = new CPpack();
//        pack.setUp(new Object[]{instance, capaBin, -1});
//        pack.cpPack();

    }

    public static void main(String[] args) {
        BinPacking tp2 = new BinPacking();
        LOGGER.info("************** Boolean model **************");
        tp2.binPacking1(10, 13, 1);
        LOGGER.info("");
        LOGGER.info("************** Cumulative model ***************");
        tp2.binPacking2(10, 13, 1);
        LOGGER.info("************** Pack model ***************");
        tp2.binPacking3(10, 13, 1);
    }
}