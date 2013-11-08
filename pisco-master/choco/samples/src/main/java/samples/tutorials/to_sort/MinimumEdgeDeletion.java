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

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.integer.IntegerVariable;
import org.kohsuke.args4j.Option;
import samples.tutorials.PatternExample;

import java.util.Arrays;
import java.util.Random;

import static choco.Choco.*;

/**
 * Let consider a set of N boolean variables and a binary constraint network (eq or neq).
 * The goal is to find an assignment minimizing the number of required edge, or constraint, deletion.
 * The problem is inspired from the Minimum Equivalence Deletion Problem
 *
 * @author Arnaud Malapert</br>
 * @version 2.0.3</br>
 * @since 22 mars 2009 version 2.0.3</br>
 */
public class MinimumEdgeDeletion extends PatternExample {

    protected IntegerVariable[] pairVars;

    protected IntegerVariable[] boolVars;

    protected IntegerVariable deletion;

    @Option(name = "-n", usage = "Number of variables (default : 7)", required = false)
    protected int nbBools = 7;

    @Option(name = "-p", usage = "Probability of an edge constraint (default : 0.5)", required = false)
    protected double pairProba = 0.5;

    @Option(name = "-seed", usage = "Seed for random", required = false)
    protected long seed = System.currentTimeMillis();

    protected int nbPairs;

    protected int[] instantiated;


    protected Boolean[][] pairs;


    @Override
    public void buildModel() {
        model = new CPModel();
        nbPairs = 0;
        pairs = new Boolean[nbBools][nbBools];
        instantiated = new int[nbBools];
        final Random rnd = new Random(seed);
        for (int i = 0; i < nbBools; i++) {
            for (int j = 0; j < i; j++) {
                //equivalence existence
                if (rnd.nextDouble() < pairProba) {
                    pairs[i][j] = rnd.nextBoolean();
                    nbPairs++;
                }
            }
        }


        boolVars = makeBooleanVarArray("b", nbBools);
        model.addVariables(boolVars);
        pairVars = new IntegerVariable[nbPairs];
        deletion = makeIntVar("deletion", 0, nbPairs, Options.V_OBJECTIVE);
        int cpt = 0;
        for (int i = 0; i < nbBools; i++) {
            for (int j = 0; j < nbBools; j++) {
                if (pairs[i][j] == Boolean.TRUE) {
                    pairVars[cpt] = makeBooleanVar("eq_" + i + "_" + j);
                    model.addConstraint(Choco.reifiedConstraint(pairVars[cpt], eq(boolVars[i], boolVars[j])));
                    cpt++;
                } else if (pairs[i][j] == Boolean.FALSE) {
                    pairVars[cpt] = makeBooleanVar("neq_" + i + "_" + j);
                    model.addConstraint(Choco.reifiedConstraint(pairVars[cpt], neq(boolVars[i], boolVars[j])));
                    cpt++;
                }
            }
        }
        model.addConstraint(eq(minus(cpt, sum(pairVars)), deletion));
        //LOGGER.info(model.pretty());
    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
        solver.setFirstSolution(false);
        solver.setDoMaximize(false);
    }

    @Override
    public void prettyOut() {
        //LOGGER.info("pairs: "+Arrays.toString(solver.getVar(pairVars)));
        LOGGER.info("nbDeletions= " + solver.getOptimumValue());
        LOGGER.info("bool vars: " + Arrays.toString(solver.getVar(boolVars)));
    }

    @Override
    public void solve() {
        //solver.setSolutionPoolCapacity(5);
        solver.generateSearchStrategy();
        solver.launch();
    }

    public static void main(String[] args) {
        new MinimumEdgeDeletion().execute(args);
    }
}
