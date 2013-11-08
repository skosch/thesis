/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
import choco.kernel.model.variables.integer.IntegerVariable;
import samples.tutorials.PatternExample;

import static choco.Choco.*;


/**
 * <b>The famous SEND + MORE = MONEY problem.</b></br>
 * The Send More Money Problem consists in finding distinct digits for the letters D, E, M, N, O, R, S, Y
 * such that S and M are different from zero (no leading zeros) and the equation SEND + MORE = MONEY is satisfied.
 *
 * @author Arnaud Malapert</br>
 * @version 2.0.1</br>
 * @since 3 déc. 2008 version 2.0.1</br>
 */
public class SendMoreMoney extends PatternExample {

    IntegerVariable S, E, N, D, M, O, R, Y;

    @Override
    public void printDescription() {
        LOGGER.info("Find the value of letters such that the following equation is correct: ");
        LOGGER.info("  SEND");
        LOGGER.info("+ MORE");
        LOGGER.info("-------");
        LOGGER.info(" MONEY\n");
    }

    @Override
    public void buildModel() {
        model = new CPModel();

        S = makeIntVar("S", 0, 9);
        E = makeIntVar("E", 0, 9);
        N = makeIntVar("N", 0, 9);
        D = makeIntVar("D", 0, 9);
        M = makeIntVar("M", 0, 9);
        O = makeIntVar("0", 0, 9);
        R = makeIntVar("R", 0, 9);
        Y = makeIntVar("Y", 0, 9);
        model.addConstraints(neq(S, 0), neq(M, 0));
        model.addConstraint(allDifferent(S, E, N, D, M, O, R, Y));


        IntegerVariable[] SEND, MORE, MONEY;
        SEND = new IntegerVariable[]{S, E, N, D};
        MORE = new IntegerVariable[]{M, O, R, E};
        MONEY = new IntegerVariable[]{M, O, N, E, Y};
        model.addConstraints(
                eq(plus(scalar(new int[]{1000, 100, 10, 1}, SEND),
                        scalar(new int[]{1000, 100, 10, 1}, MORE)),
                        scalar(new int[]{10000, 1000, 100, 10, 1}, MONEY))
        );
    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
    }

    @Override
    public void solve() {
        solver.solve();
    }

    @Override
    public void prettyOut() {
        LOGGER.info("The solution is:");
        LOGGER.info(String.format("  %d%d%d%d", solver.getVar(S).getVal(), solver.getVar(E).getVal(),
                solver.getVar(N).getVal(), solver.getVar(D).getVal()));
        LOGGER.info(String.format("+ %d%d%d%d", solver.getVar(M).getVal(), solver.getVar(O).getVal(),
                solver.getVar(R).getVal(), solver.getVar(E).getVal()));
        LOGGER.info("-------");
        LOGGER.info(String.format(" %d%d%d%d%d\n", solver.getVar(M).getVal(), solver.getVar(O).getVal(),
                solver.getVar(N).getVal(), solver.getVar(E).getVal(), solver.getVar(Y).getVal()));

    }

    public static void main(String[] args) {
        new SendMoreMoney().execute(args);
    }

}