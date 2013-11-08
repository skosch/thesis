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

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import org.kohsuke.args4j.Option;
import samples.tutorials.PatternExample;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static choco.Choco.*;

/**
 * Created by IntelliJ IDEA.
 * User: Xavier Lorca
 * Date: 4 sept. 2007
 * Time: 09:31:10
 */
public class Sudoku extends PatternExample {


    @Option(name = "-f", usage = "Instance file", required = false)
    String filename;


    @Option(name = "-prop", usage = "Propagation only", required = false)
    boolean onlyProp = false;

    @Option(name = "-bin", usage = "Binary model", required = false)
    boolean binary = false;

    int n = 9;
    int[][] instance = {
            {0, 0, 7, 5, 0, 0, 3, 0, 0},
            {0, 4, 0, 0, 2, 0, 1, 0, 0},
            {1, 0, 0, 0, 7, 0, 0, 5, 0},
            {0, 0, 3, 1, 4, 0, 2, 0, 6},
            {4, 0, 0, 0, 6, 2, 7, 0, 0},
            {0, 6, 5, 0, 3, 0, 0, 0, 8},
            {0, 7, 1, 0, 0, 0, 6, 0, 0},
            {8, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 5, 0, 7, 0, 0, 0, 4, 1}
    };
    IntegerVariable[][] rows;

    @Override
    public void printDescription() {
        if (filename != null) {
            parse(filename);
        }

        LOGGER.info("Sudoku is a logic-based combinatorial number-placement puzzle.");
        LOGGER.info("The objective is to fill a 9?9 grid with digits ");
        LOGGER.info("so that each column, each row, and each of the nine 3?3 sub-grids that compose the grid ");
        LOGGER.info("contains all of the digits from 1 to 9. ");
        LOGGER.info("The puzzle setter provides a partially completed grid, which typically has a unique solution.");
        LOGGER.info("(http://en.wikipedia.org/wiki/Sudoku)\n");

        LOGGER.info("Data:");
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                st.append(instance[i][j] > 0 ? instance[i][j] : ".").append(" ");
            }
            LOGGER.info(st.toString());
            st.setLength(0);
        }
        LOGGER.info("\n");
    }

    @Override
    public void buildModel() {
        // Build Model
        model = new CPModel();

        // Build an array of integer variables
        rows = makeIntVarArray("rows", n, n, 1, n);

        if (binary) {
            simple(model, rows, instance);
        } else {
            advanced(model, rows, instance);
        }

    }

    private void simple(Model model, IntegerVariable[][] rows, int[][] instance) {
        // Not equal constraint between each case of a row
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = j; k < n; k++) {
                    if (k != j) {
                        model.addConstraint(neq(rows[i][j], rows[i][k]));
                    }
                }
            }
        }
        // Not equal constraint between each case of a column
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                for (int k = 0; k < n; k++) {
                    if (k != i) {
                        model.addConstraint(neq(rows[i][j], rows[k][j]));
                    }
                }
            }
        }
        // Not equal constraint between each case of a sub region
        for (int ci = 0; ci < n; ci += 3) {
            for (int cj = 0; cj < n; cj += 3) {
                // Extraction of disequality of a sub region
                for (int i = ci; i < ci + 3; i++) {
                    for (int j = cj; j < cj + 3; j++) {
                        for (int k = ci; k < ci + 3; k++) {
                            for (int l = cj; l < cj + 3; l++) {
                                if (k != i || l != j) model.addConstraint(neq(rows[i][j], rows[k][l]));
                            }
                        }
                    }
                }
            }
        }
        // Read the instance given.
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (instance[i][j] != 0) {
                    Constraint c = eq(rows[i][j], instance[i][j]);
                    model.addConstraint(c);
                }
            }
        }
    }

    public void advanced(Model model, IntegerVariable[][] rows, int[][] instance) {
        int n = instance.length;
        // Declare variables
        IntegerVariable[][] cols = new IntegerVariable[n][n];

        // Channeling between rows and columns
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                cols[i][j] = rows[j][i];
            }
        }

        // Add alldifferent constraint
        for (int i = 0; i < n; i++) {
            model.addConstraint(allDifferent(cols[i]));
            model.addConstraint(allDifferent(rows[i]));
        }
        // Define sub regions
        IntegerVariable[][] carres = new IntegerVariable[n][n];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    carres[j + k * 3][i] = rows[0 + k * 3][i + j * 3];
                    carres[j + k * 3][i + 3] = rows[1 + k * 3][i + j * 3];
                    carres[j + k * 3][i + 6] = rows[2 + k * 3][i + j * 3];
                }
            }
        }

        // Add alldifferent on sub regions
        for (int i = 0; i < n; i++) {
            Constraint c = allDifferent(carres[i]);
            model.addConstraint(c);
        }

        // Read the instance
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (instance[i][j] != 0) {
                    Constraint c = eq(rows[i][j], instance[i][j]);
                    model.addConstraint(c);
                }
            }
        }
    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        // Read model
        solver.read(model);
    }

    @Override
    public void solve() {

        // First choice : only propagation
        if (onlyProp) {
            try {
                solver.propagate();
            } catch (ContradictionException e) {
                LOGGER.info("pas de solutions");
            }
        }
        // Second choice : find a solution
        else {
            solver.solve();
        }
    }

    @Override
    public void prettyOut() {
        printGrid(rows, solver);
    }

    public static void printGrid(IntegerVariable[][] rows, Solver s) {
        for (int i = 0; i < 9; i++) {
            StringBuffer st = new StringBuffer();
            for (int j = 0; j < 9; j++) st.append(s.getVar(rows[i][j]).getVal() + " ");
            LOGGER.info(st.toString());
        }
        LOGGER.info("\n");
    }

    public static void main(String[] args) {
        new Sudoku().execute(args);
    }

////////////////////////////////////////////////////

    protected void parse(String fileName) {
        try {
            Scanner sc = new Scanner(new File(fileName));
            n = sc.nextInt();
            instance = new int[n][n];

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    instance[i][j] = sc.nextInt();
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
