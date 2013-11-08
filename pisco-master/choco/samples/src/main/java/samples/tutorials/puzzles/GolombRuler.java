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

package samples.tutorials.puzzles;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.model.variables.integer.IntegerVariable;
import org.kohsuke.args4j.Option;
import samples.tutorials.PatternExample;

import java.text.MessageFormat;
import java.util.Arrays;

import static choco.Choco.*;

/**
 * @author Arnaud Malapert
 */
public class GolombRuler extends PatternExample {


    IntegerVariable[] ticks, diff;

    @Option(name = "-s", usage = "Number of ticks on a ruler (default : 8)", required = false)
    public int m = 8;

    @Option(name = "-allDiff", usage = "Use allDifferent constraint", required = false)
    private boolean useAllDiff = false;


    @Override
    public void printDescription() {
        super.printDescription();
        LOGGER.info("A Golomb ruler may be defined as a set of m integers");
        LOGGER.info("0 = a_1 < a_2 < ... < a_m ");
        LOGGER.info("such that the m(m-1)/2 differences a_j - a_i, 1 <= i < j <= m are distinct.");
        LOGGER.info("Such a ruler is said to contain m marks and is of length a_m.");
        LOGGER.info("The objective is to find optimal (minimum length) or near optimal rulers.");
        LOGGER.info("(http://www.csplib.org/)");
        LOGGER.info(MessageFormat.format("Here n = {0}\n\n", m));
    }

    @Override
    public void buildModel() {
        model = new CPModel();
        ticks = makeIntVarArray("a", m, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), Options.V_BOUND);
        diff = makeIntVarArray("d", ((m) * (m - 1)) / 2, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), Options.V_BOUND);

        model.addConstraint(Choco.eq(ticks[0], 0));
        for (int i = 0; i < ticks.length - 1; i++) {
            model.addConstraint(Choco.lt(ticks[i], ticks[i + 1]));
        }

        diff = new IntegerVariable[(m * m - m) / 2];
        for (int k = 0, i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                diff[k] = Choco.makeIntVar("d_" + i, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), Options.V_BOUND);
            }
        }

        for (int k = 0, i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                // d[k] is m[j]-m[i] and must be at least sum of first j-i integers
                model.addConstraint(Choco.eq(0,
                        Choco.scalar(new int[]{1, -1, -1}, new IntegerVariable[]{ticks[j], ticks[i], diff[k]})));
                model.addConstraint(Choco.leq((j - i) * (j - i + 1) / 2,
                        Choco.scalar(new int[]{1}, new IntegerVariable[]{diff[k]})));
                model.addConstraint(Choco.geq(-((m - 1 - j + i) * (m - j + i)) / 2,
                        Choco.scalar(new int[]{1, -1}, new IntegerVariable[]{diff[k], ticks[m - 1]})));
            }
        }
        // break symetries
        if (m > 2) {
            model.addConstraint(Choco.lt(diff[0], diff[diff.length - 1]));
        }

        if (useAllDiff) {
            model.addConstraint(allDifferent(diff));
        } else {
            // d_ij != d_kl
            for (int i = 0; i < diff.length; i++) {
                for (int j = i + 1; j < diff.length; j++) {
                    model.addConstraint(neq(diff[i], diff[j]));
                }
            }
        }
    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
       // solver.addGoal(new AssignVar(new StaticVarOrder(solver, solver.getVar(ticks)), new IncreasingDomain()));
        solver.addGoal(new AssignVar(new StaticVarOrder(solver, solver.getVar(ticks)), new IncreasingDomain()));
    }

    @Override
    public void solve() {
        solver.minimize(solver.getVar(ticks[m - 1]), true);
    }

    @Override
    public void prettyOut() {
        LOGGER.info(Arrays.toString(solver.getVar(ticks)));
    }


    public static void main(String[] args) {
        new GolombRuler().execute(args);
    }

}
