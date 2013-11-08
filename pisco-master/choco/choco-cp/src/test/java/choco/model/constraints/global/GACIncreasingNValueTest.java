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

package choco.model.constraints.global;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import static choco.Choco.makeIntVar;

/**
 * User : xlorca
 * Mail : xlorca(a)emn.fr
 * Date : 29 janv. 2010
 * Since : Choco 2.1.1
 */
public class GACIncreasingNValueTest {

    private static Logger LOGGER = ChocoLogging.getTestLogger();

    public class GACIncreasingNValue {

        long seed;
        int nbVars;
        int maxVal;
        double hole;

        Model m;
        IntegerVariable[] vars;
        IntegerVariable[] mvars;

        public GACIncreasingNValue(long seed, int nbVars, int maxVal, double hole) {
            this.nbVars = nbVars;
            this.maxVal = maxVal;
            this.hole = hole;
            this.seed = seed;
        }

        public void generateBaseModel() {
            Random rand = new Random(seed);
            this.m = new CPModel();
            int minOcc = 1 + rand.nextInt(Math.min(nbVars, maxVal) / 2);
            int maxOcc = minOcc + rand.nextInt(nbVars - minOcc);
            InstanceGenerator gen = new InstanceGenerator(seed, nbVars, 0, maxVal, minOcc, maxOcc, hole);
            vars = new IntegerVariable[nbVars + 1];
            System.arraycopy(gen.getVars(), 0, vars, 1, gen.getVars().length);
            vars[0] = gen.getOcc();
            m.addConstraint(Options.C_INCREASING_NVALUE_BOTH, Choco.increasingNValue(gen.getOcc(), gen.getVars()));
        }

        public Model generateSpecificModel(int idx, int val) {
            Random rand = new Random(seed);
            Model mm = new CPModel();
            int minOcc = 1 + rand.nextInt(Math.min(nbVars, maxVal) / 2);
            int maxOcc = minOcc + rand.nextInt(nbVars - minOcc);
            InstanceGenerator gen = new InstanceGenerator(seed, nbVars, 0, maxVal, minOcc, maxOcc, hole);
            mvars = new IntegerVariable[nbVars + 1];
            System.arraycopy(gen.getVars(), 0, mvars, 1, gen.getVars().length);
            mvars[0] = gen.getOcc();
            // generation du cas specifique
            mvars[idx] = Choco.makeIntVar(mvars[idx].getName(), val, val, Options.V_ENUM);
            IntegerVariable[] vvars = new IntegerVariable[nbVars];
            System.arraycopy(mvars, 1, vvars, 0, vvars.length);
            mm.addConstraint(Options.C_INCREASING_NVALUE_BOTH, Choco.increasingNValue(mvars[0], vvars));
            return mm;
        }


        public void generatePropag() {
            Solver s = new CPSolver();
            s.read(m);
            boolean test = true;
            try {
                s.propagate();
            } catch (ContradictionException e) {
                test = false;
            }
            if (test) {
                for (int i = 0; i < nbVars + 1; i++) {
                    IntDomainVar dv = s.getVar(vars[i]);
                    DisposableIntIterator it = dv.getDomain().getIterator();
                    while (it.hasNext()) {
                        int val = it.next();
                        Model mm = generateSpecificModel(i, val);
                        Solver ss = new CPSolver();
                        ss.read(mm);
                        ss.solve();
                        if (ss.getNbSolutions() == 0) {
                            LOGGER.severe("Probleme GAC");
                            System.exit(0);
                        }
                    }
                }
            } else {
                LOGGER.info("No solution");
            }
        }
    }

    @Test
    public void test() {
        //ChocoLogging.setVerbosity(Verbosity.SILENT);
        int nbVars = 8;
        int maxVal = 10;
        double hole = 0.5;
        int iter = 500;
        while (iter > 0) {
            long seed = System.currentTimeMillis();
            LOGGER.info("\nseed = " + seed);
            GACIncreasingNValue test = new GACIncreasingNValue(seed, nbVars, maxVal, hole);
            test.generateBaseModel();
            test.generatePropag();
            iter--;
        }
    }

    private static class InstanceGenerator {
        public static boolean debug = false;

        long seed;

        int n; // nb variables
        int min; // min domain
        int max; // max domain
        double hole; // hole in domains
        int minOcc; // min occurrence
        int maxOcc; // max occurrence with maxOcc > minOccc

        int[] val; // values satisfying constraint
        int valOcc; // occurrence satisfying constraint

        IntegerVariable occ;
        IntegerVariable[] vars;
        int rem; // nb values to remove

        public InstanceGenerator(long seed, int n, int min, int max, int minOcc, int maxOcc, double hole) {
            this.seed = seed;
            this.n = n;
            this.min = min;
            this.max = max;
            this.minOcc = minOcc;
            this.maxOcc = maxOcc;
            this.hole = hole;
            this.generateSolution();
            this.generateDomains();
            if (debug) LOGGER.info("" + getOcc());
            for (IntegerVariable v : getVars()) {
                if (debug) LOGGER.info("" + v);
            }
        }

        private void generateSolution() {
            Random rand = new Random(seed);
            valOcc = minOcc + rand.nextInt((maxOcc - minOcc < 1) ? 1 : (maxOcc - minOcc));
            if (debug) LOGGER.info("" + valOcc);
            if (debug) LOGGER.info("-----");
            int[] genVal = new int[valOcc];
            genVal[0] = min + rand.nextInt((max - min - valOcc < 1) ? 1 : (max - min - valOcc));
            if (debug) LOGGER.info("" + genVal[0]);
            for (int i = 1; i < valOcc; i++) {
                genVal[i] = (1 + genVal[i - 1]) + rand.nextInt((max - genVal[i - 1] - (valOcc - i) < 1) ? 1 : (max - genVal[i - 1] - (valOcc - i))); // todo pb ! je veux generer 3 valeurs distinctes entre 0 et 2 en commen�ant par 1 ? impossible
                if (debug) LOGGER.info("" + genVal[i]);
            }
            if (debug) LOGGER.info("-----");
            int[] cut = new int[valOcc + 1];
            cut[0] = 0;
            if (debug) LOGGER.info("" + cut[0]);
            for (int i = 1; i < valOcc; i++) {
                cut[i] = 1 + cut[i - 1] + rand.nextInt((n - cut[i - 1] - valOcc < 1) ? 1 : (n - cut[i - 1] - valOcc));
                if (debug) LOGGER.info("" + cut[i]);
            }
            cut[valOcc] = n - 1;
            if (debug) LOGGER.info("" + cut[valOcc]);
            if (debug) LOGGER.info("-----");
            val = new int[n];
            int j = 0;
            for (int i = 0; i < n; i++) {
                if (i >= cut[j] && i <= cut[j + 1]) {
                    val[i] = genVal[j];
                    if (debug) LOGGER.info("" + val[i]);
                } else {
                    j++;
                    val[i] = genVal[j];
                    if (debug) LOGGER.info("" + val[i]);
                }
            }
        }

        private void generateDomains() {
            Random rand = new Random(seed);
            vars = new IntegerVariable[n];
            for (int i = 0; i < n; i++) {
                vars[i] = buildVarDomain(rand, i, "vm" + i);
            }
            int minOcc = 1 + rand.nextInt(valOcc);
            int maxOcc = valOcc + rand.nextInt((n - valOcc < 1) ? 1 : (n - valOcc));
            occ = buildOccDomain(rand, "occ", minOcc, maxOcc);//makeIntVar("occ", minOcc, maxOcc, CPOptions.V_ENUM);
        }

        private IntegerVariable buildOccDomain(Random rand, String s, int minVal, int maxVal) {
            if (debug) LOGGER.info("minOcc = " + minVal);
            if (debug) LOGGER.info("maxOcc = " + maxVal);
            rem = (int) ((maxVal - minVal) * hole);
            if (debug) LOGGER.info("rem = " + rem);
            int size = (1 + maxVal - minVal) - rem;
            if (debug) LOGGER.info("size = " + size);
            int[] res = new int[size];
            List<Integer> vals = new ArrayList<Integer>();
            for (int i = minVal; i <= maxVal; i++) {
                if (i != valOcc) vals.add(i);
            }
            res[0] = valOcc;
            int k = 1;
            while (k < size) {
                int idx = rand.nextInt(vals.size());
                res[k] = vals.remove(idx);
                k++;
            }
            for (int i : res) {
                if (debug) System.out.print(i + ", ");
            }
            if (debug) LOGGER.info("");
            return makeIntVar(s, res, Options.V_ENUM);
        }

        private IntegerVariable buildVarDomain(Random rand, int idV, String s) {
            if (debug) LOGGER.info("*******");
            if (debug) LOGGER.info("val[" + idV + "] = " + val[idV]);
            int minidV = val[idV] - rand.nextInt(val[idV] < 1 ? 1 : val[idV]);
            int maxidV = 1 + val[idV] + rand.nextInt((max - val[idV]) < 1 ? 1 : (max - val[idV]));
            if (debug) LOGGER.info("minidV = " + minidV);
            if (debug) LOGGER.info("maxidV = " + maxidV);
            rem = (int) ((maxidV - minidV) * hole);
            if (debug) LOGGER.info("rem = " + rem);
            int size = (1 + maxidV - minidV) - rem;
            if (debug) LOGGER.info("size = " + size);
            int[] res = new int[size];
            List<Integer> vals = new ArrayList<Integer>();
            for (int i = minidV; i <= maxidV; i++) {
                if (i != val[idV]) vals.add(i);
            }
            res[0] = val[idV];
            int k = 1;
            while (k < size) {
                int idx = rand.nextInt(vals.size());
                res[k] = vals.remove(idx);
                k++;
            }
            for (int i : res) {
                if (debug) System.out.print(i + ", ");
            }
            if (debug) LOGGER.info("");
            return makeIntVar(s, res, Options.V_ENUM);
        }

        public IntegerVariable getOcc() {
            return occ;
        }

        public IntegerVariable[] getVars() {
            return vars;
        }

        public boolean test() {
            for (int i = 0; i < n; i++) {
                if (val[i] < min || max < val[i]) {
                    LOGGER.info("val[" + i + "] = " + val[i] + " -VS- {" + min + "," + max + "}");
                    return false;
                }
            }
            if (!occ.canBeEqualTo(valOcc)) {
                LOGGER.info("fail occ");
                return false;
            } else {
                for (int i = 0; i < n; i++) {
                    if (!vars[i].canBeEqualTo(val[i])) {
                        LOGGER.info("fail " + vars[i] + " VS " + val[i]);
                        return false;
                    }
                }
                return true;
            }
        }

        public static void buildProblem(int n, int vars, int val, long seed) {
            Random rand = new Random(seed);
            int minOcc = 1 + rand.nextInt(Math.min(vars, val) / 2);
            int maxOcc = minOcc + rand.nextInt(Math.min(val, vars) - minOcc);
            //LOGGER.info("[" + minOcc + "," + maxOcc + "]");
            InstanceGenerator gen = new InstanceGenerator(seed, vars, 0, val, minOcc, maxOcc, 0.3);
            boolean test = gen.test();
            LOGGER.info((n++) + " -- " + seed + " -- " + test);
            if (!test) System.exit(0);
        }

        public String toString() {
            String s = "";
            s += prettyPrint(occ);
            for (IntegerVariable v : vars) {
                s += prettyPrint(v);
            }
            s = s.substring(0, s.length() - 1);
            return s;
        }

        public String prettyPrint(IntegerVariable v) {
            String s = v.getName() + ":= [";
            DisposableIntIterator it = v.getDomainIterator();
            while (it.hasNext()) {
                s += it.next() + ",";
            }
            s = s.substring(0, s.length() - 1);
            s += "]\n";
            return s;
        }

        @Test
        public void test0() {
            int vars = 10;
            int val = 5;
            long seed;

            int n = 0;
            while (!debug) {
                seed = System.currentTimeMillis();
                buildProblem(n, vars, val, seed);
                n++;
            }

            seed = 1264064191854L;
            buildProblem(0, vars, val, seed);
        }
    }

}