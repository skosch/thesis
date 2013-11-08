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

package samples.tutorials.pack;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.integer.IntegerVariable;
import org.kohsuke.args4j.Option;
import samples.tutorials.PatternExample;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 28 mai 2008
 * Since : Choco 2.0.0
 *
 */
public class KnapSack extends PatternExample {

    @Option(name = "-f", usage = "Data file", required = false)
    String file = null;

    @Option(name = "-g", usage = "Use global constraint", required = false)
    boolean global = false;

    int[] capacities = {0, 34};
    int[] energies = {6, 4, 3};
    int[] volumes = {7, 5, 3};
    int n = 3;

    IntegerVariable[] quantities;
    IntegerVariable power, capacity;

    @Override
    public void printDescription() {
        if (file != null) {
            parse(file);
        }
        LOGGER.info("Given a set of items, each with a weight and a value, ");
        LOGGER.info("determine the number of each item to include in a collection so that the total weight is less than or equal to a given limit ");
        LOGGER.info("and the total value is as large as possible.");
        LOGGER.info("(http://en.wikipedia.org/wiki/Knapsack_problem)\n");
        LOGGER.info("Data:");
        LOGGER.info(String.format("Capacity: [%d, %d]", capacities[0], capacities[1]));
        LOGGER.info("Item\tWeight\tValue");
        for (int i = 0; i < n; i++) {
            LOGGER.info(String.format("%d\t%d\t%d", i, volumes[i], energies[i]));
        }
        LOGGER.info("\n");
    }

    @Override
    public void buildModel() {
        model = new CPModel();

        quantities = new IntegerVariable[n];
        int i = 0;
        for (; i < n; i++) {
            quantities[i] = Choco.makeIntVar("q_" + i, 0, capacities[1] / volumes[i] + 1, Options.V_ENUM);
        }
        capacity = Choco.makeIntVar("capacity", capacities[0] - 1, capacities[1] + 1, Options.V_ENUM);
        power = Choco.makeIntVar("power", 0, 999999, Options.V_BOUND);

        if (global) {
            model.addConstraint(Choco.knapsackProblem(power, capacity, quantities, energies, volumes));
        } else {
            model.addConstraint(Choco.leq(Choco.scalar(quantities, volumes), capacity));

            model.addConstraint(Choco.eq(Choco.scalar(quantities, energies), power));
        }
    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
    }

    @Override
    public void solve() {
        solver.maximize(solver.getVar(power), true);
    }

    @Override
    public void prettyOut() {
        LOGGER.info("The best solution is:");
        for (int i = 0; i < quantities.length; i++) {
            LOGGER.info(String.format("Number of item %d : %d", i, solver.getVar(quantities[i]).getVal()));
        }
        LOGGER.info(String.format("Weight : %d", solver.getVar(capacity).getVal()));
        LOGGER.info(String.format("Total value : %d\n", solver.getVar(power).getVal()));
    }


    public static void main(String[] args) {
        new KnapSack().execute(args);
    }

    ////////////////////////////////////////////////////

    protected void parse(String fileName) {
        try {
            Scanner sc = new Scanner(new File(fileName));
            n = sc.nextInt();
            capacities = new int[2];
            energies = new int[n];
            volumes = new int[n];

            capacities[0] = sc.nextInt();
            capacities[1] = sc.nextInt();

            for (int i = 0; i < n; i++) {
                energies[i] = sc.nextInt();
                volumes[i] = sc.nextInt();
            }

            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
