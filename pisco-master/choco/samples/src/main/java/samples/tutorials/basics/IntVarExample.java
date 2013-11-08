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

package samples.tutorials.basics;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.integer.IntegerVariable;
import samples.tutorials.PatternExample;

import static choco.Choco.*;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 27 avr. 2010
 * Time: 14:45:58
 * TODO: rename to fill with IntegerVariable...
 */




public class IntVarExample extends PatternExample {

    IntegerVariable me, him;
    /*
         * Easy simple problem defined by:
         * "Six years ago, my brother was two time my age.
         * In five years, we will have 40 years together.
         * How old am I?"
         * (sorry for the translation :) )
         */

    @Override
    public void printDescription() {
        super.printDescription();
        LOGGER.info("Six years ago, my brother was twice my age.");
        LOGGER.info("In five years, our ages will add up to 40");
        LOGGER.info("How old am I ?");
    }

    @Override
    public void buildModel() {
        model = new CPModel();
        me = makeIntVar("me", 0, 40);
        him = makeIntVar("him", 0, 40);

        model.addConstraint(eq(mult(2, minus(me, 6)), minus(him, 6)));
        model.addConstraint(eq(40, plus(plus(me, 5), plus(him,5))));
    }
    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
    }

    @Override
    public void solve() {
        solver.solveAll();
    }

    @Override
    public void prettyOut() {
        LOGGER.info("\nMe :"+ solver.getVar(me).getVal()+" years old");
        LOGGER.info("Him :"+ solver.getVar(him).getVal()+" years old\n");
    }

    public static void main(String[] args) {
        new IntVarExample().execute();
    }
}
