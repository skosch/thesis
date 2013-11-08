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
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.variables.integer.IntegerVariable;
import org.kohsuke.args4j.Option;
import samples.tutorials.PatternExample;

import java.text.MessageFormat;
import java.util.logging.Level;

import static choco.Choco.*;


public class MagicSquare extends PatternExample {

    @Option(name = "-n", usage = "Order of the magic square (default : 5)", required = false)
    public int n = 5;

    public int magicSum;

    protected IntegerVariable[][] vars;


    public static int getMagicSum(int n) {
        return n * (n * n + 1) / 2;
    }


    @Override
    public void printDescription() {
        LOGGER.info("An order n magic square is a n by n matrix containing the numbers 1 to n^2, ");
        LOGGER.info("with each row, column and main diagonal equal the same sum.");
        LOGGER.info("(http://www.csplib.org/)");
        LOGGER.info(MessageFormat.format("Here n = {0}\n\n", n));
    }

    @Override
    public void buildModel() {
        model = new CPModel();
        magicSum = getMagicSum(n);
        final int ub = n * n;
        vars = makeIntVarArray("v", n, n, 1, ub);
        // All cells of the matrix must be different
        model.addConstraint(allDifferent(ArrayUtils.flatten(vars)));
        final IntegerVariable[] varDiag1 = new IntegerVariable[n];
        final IntegerVariable[] varDiag2 = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            // All rows must be equal to the magic sum
            model.addConstraint(eq(sum(vars[i]), magicSum));
            // All columns must be equal to the magic sum
            model.addConstraint(eq(sum(ArrayUtils.getColumn(vars, i)), magicSum));
            //record diagonals variable
            varDiag1[i] = vars[i][i];
            varDiag2[i] = vars[(n - 1) - i][i];
        }
        // Every diagonal have to be equal to the magic sum
        model.addConstraint(eq(sum(varDiag1), magicSum));
        model.addConstraint(eq(sum(varDiag2), magicSum));
        //symmetry breaking constraint: enforce that the upper left corner contains the minimum corner value.
        model.addConstraint(and(
                lt(vars[0][0], vars[0][n - 1]),
                lt(vars[0][0], vars[n - 1][n - 1]),
                lt(vars[0][0], vars[n - 1][0])
        ));

    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.monitorFailLimit(true);
        solver.read(model);
        solver.setTimeLimit(500 * 1000);
    }

    @Override
    public void prettyOut() {
        if (LOGGER.isLoggable(Level.INFO)) {
            StringBuilder st = new StringBuilder();
            // Print of the solution
            if (solver.existsSolution()) {
                for (int i = 0; i < n; i++) {
                    st.append('\n');
                    for (int j = 0; j < n; j++) {
                        st.append(MessageFormat.format("{0} ", solver.getVar(vars[i][j]).getVal()));
                    }
                }
            } else st.append("\nno solution to display!");
            LOGGER.info(st.toString());
        }
    }

    @Override
    public void solve() {
        solver.solve();
    }


    public static void main(String[] args) {
        new MagicSquare().execute(args);
    }

}