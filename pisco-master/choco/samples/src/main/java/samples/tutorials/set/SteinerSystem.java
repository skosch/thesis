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
package samples.tutorials.set;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.set.AssignSetVar;
import choco.cp.solver.search.set.MinDomSet;
import choco.cp.solver.search.set.MinEnv;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.Solution;
import org.kohsuke.args4j.Option;
import samples.tutorials.PatternExample;

import static choco.Choco.*;
import static java.text.MessageFormat.format;

public class SteinerSystem extends PatternExample {

    /**
     * A ternary Steiner system of order n is a set of triplets of distinct elements
     * taking their values between 1 and n, such that all the pairs included in two different triplets are different.
     * a solution for n = 7 :
     * [{1, 2, 3}, {2, 4, 5}, {3, 4, 6}, {1, 4, 7}, {1, 5, 6}, {2,6, 7}, {3, 5, 7}]
     * we must have n % 6 = 1 or n % 6 = 3 to get a valid n for the problem
     */

    @Option(name = "-p", usage = "Max value (default: 7)", required = false)
    protected int p = 7;
    protected int n;

    SetVariable[] vars;

    @Override
    public void printDescription() {
        LOGGER.info("A ternary Steiner system of order n is a set of triplets of n*(n - 1) / 6 ");
        LOGGER.info("distinct elements taking their values between 1 and n,");
        LOGGER.info("such that all the pairs included in two different triplets are different. ");
        LOGGER.info("see http://mathworld.wolfram.com/SteinerTripleSystem.html \n \n");
    }

    @Override
    public void buildModel() {
        model = new CPModel();
        n = p * (p - 1) / 6;

        vars = new SetVariable[n];
        SetVariable[] intersect = new SetVariable[n * n];

        // Create Variables
        for (int i = 0; i < n; i++)
            vars[i] = makeSetVar("set " + i, 1, n);
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
                intersect[i * n + j] = makeSetVar("interSet " + i + " " + j, 1, n);

        // Post constraints
        for (int i = 0; i < n; i++) {
            model.addConstraint(eqCard(vars[i], 3));
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                model.addConstraint(setInter(vars[i], vars[j], intersect[i * n + j]));
                model.addConstraint(leqCard(intersect[i * n + j], 1));
            }
        }
    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
    }

    @Override
    public void solve() {
        solver.setFirstSolution(true);
        solver.generateSearchStrategy();
        solver.addGoal(new AssignSetVar(new MinDomSet(solver, solver.getVar(vars)), new MinEnv()));
        solver.launch();
    }

    @Override
    public void prettyOut() {
        StringBuffer s = new StringBuffer();
        Solution sol = solver.getSearchStrategy().getSolutionPool().getBestSolution();
        solver.restoreSolution(sol);

        s.append(format("A solution for n = {0}\n\n", p));
        for (int i = 0; i < n; i++) {
            s.append(format("set[{0}]:{1}\n", i, solver.getVar(vars[i]).pretty()));
        }
        LOGGER.info(s.toString());

    }

    public static void main(String[] args) {
        new SteinerSystem().execute(args);
    }


}
