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
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.variables.integer.IntegerVariable;
import samples.tutorials.PatternExample;

import java.util.logging.Logger;

import static choco.Choco.*;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 27 mai 2008
 * Since : Choco 2.0.0
 *
 */
public class DonaldGeraldRobert extends PatternExample {

    protected final static Logger LOGGER = ChocoLogging.getMainLogger();

    public static IntegerVariable D;
    public static IntegerVariable O;
    public static IntegerVariable N;
    public static IntegerVariable A;
    public static IntegerVariable L;
    public static IntegerVariable G;
    public static IntegerVariable E;
    public static IntegerVariable R;
    public static IntegerVariable B;
    public static IntegerVariable T;

    @Override
    public void printDescription() {
        LOGGER.info("Find the value of letters such that the following equation is correct: ");
        LOGGER.info("  DONALD");
        LOGGER.info("+ GERALD");
        LOGGER.info("---------");
        LOGGER.info("  ROBERT\n");
    }

    @Override
    public void buildModel() {
        model = new CPModel();

        // Declare every letter as a variable
        D = makeIntVar("d", 0, 9);
        O = makeIntVar("o", 0, 9);
        N = makeIntVar("n", 0, 9);
        A = makeIntVar("a", 0, 9);
        L = makeIntVar("l", 0, 9);
        G = makeIntVar("g", 0, 9);
        E = makeIntVar("e", 0, 9);
        R = makeIntVar("r", 0, 9);
        B = makeIntVar("b", 0, 9);
        T = makeIntVar("t", 0, 9);
        IntegerVariable r1 = makeIntVar("r1", 0, 1);
        IntegerVariable r2 = makeIntVar("r2", 0, 1);
        IntegerVariable r3 = makeIntVar("r3", 0, 1);
        IntegerVariable r4 = makeIntVar("r4", 0, 1);
        IntegerVariable r5 = makeIntVar("r5", 0, 1);

        // Add equality between letters
        model.addConstraint(eq(plus(D, D), plus(T, mult(10, r1))));
        model.addConstraint(eq(plus(r1, plus(L, L)), plus(R, mult(10, r2))));
        model.addConstraint(eq(plus(r2, plus(A, A)), plus(E, mult(10, r3))));
        model.addConstraint(eq(plus(r3, plus(N, R)), plus(B, mult(10, r4))));
        model.addConstraint(eq(plus(r4, plus(O, E)), plus(O, mult(10, r5)))); // rewrite in scalar with a null coefficient => Bug !
//        model.addConstraint(eq(plus(r4,       e ),        mult(10,r5)));  // OK
        model.addConstraint(eq(plus(r5, plus(D, G)), R));

//      model.addConstraint(eq(d, 5));   // if you add a clue ... propagation is enougth

        // Add constraint of all different letters.
        model.addConstraint(allDifferent(D, O, N, A, L, G, E, R, B, T));
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
        LOGGER.info(String.format("  %d%d%d%d%d%d", solver.getVar(D).getVal(), solver.getVar(O).getVal(),
                solver.getVar(N).getVal(), solver.getVar(A).getVal(), solver.getVar(L).getVal(),
                solver.getVar(D).getVal()));
        LOGGER.info(String.format("+ %d%d%d%d%d%d", solver.getVar(G).getVal(), solver.getVar(E).getVal(),
                solver.getVar(R).getVal(), solver.getVar(A).getVal(), solver.getVar(L).getVal(),
                solver.getVar(D).getVal()));
        LOGGER.info("-------");
        LOGGER.info(String.format("  %d%d%d%d%d%d", solver.getVar(R).getVal(), solver.getVar(O).getVal(),
                solver.getVar(B).getVal(), solver.getVar(E).getVal(), solver.getVar(R).getVal(),
                solver.getVar(T).getVal()));
    }


    public static void main(String[] args) {
        new DonaldGeraldRobert().execute();
    }
}
