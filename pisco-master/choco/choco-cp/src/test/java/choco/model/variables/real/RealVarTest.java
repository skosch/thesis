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

package choco.model.variables.real;

import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.real.RealExpressionVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.solver.Solver;
import org.junit.Test;

/**
 * User : cprudhom
 * Mail : cprudhom(a)emn.fr
 * Date : 24 f�vr. 2010
 * Since : Choco 2.1.1
 */
public class RealVarTest {

    @Test
    public void testWayne99() {
        //ChocoLogging.setVerbosity(Verbosity.SEARCH);
    	final Model m = new CPModel();
        final RealVariable r = makeRealVar("test_r", 0, 1.0);

        m.addConstraint(leq(r, 0.4));

        final Solver s = new CPSolver();
        s.read(m);
        s.maximize(s.getVar(r), false);
    }

    @Test
    public void testDemeter() {
        // Build the model
        final Model m = new CPModel();
        // Creation of an array of variables
        final double[] prices = new double[2];
        prices[0] = 2.0;
        prices[1] = 5.0;

        // For each variable, we define its name and the boundaries of its domain.
        final RealVariable pizza = makeRealVar("pizza", 0, 1000);
        final RealVariable sandwich = makeRealVar("sandwich", 0, 1000);
        final RealVariable obj = makeRealVar("obj", 0, 1000);

        final RealExpressionVariable profitPizza = mult(prices[0], pizza);
        final RealExpressionVariable profitSandwich = mult(prices[1], sandwich);
        final RealExpressionVariable sumProfit = plus(profitPizza, profitSandwich);

        // Define constraints
        final Constraint c1 = leq(pizza, 4);
        m.addConstraint(c1);
        final Constraint c2 = leq(sandwich, 3);
        m.addConstraint(c2);
        final RealExpressionVariable sum = plus(pizza, sandwich);
        final Constraint c3 = leq(sum, 6);
        m.addConstraint(c3);
        final Constraint c4 = geq(pizza, 0);
        m.addConstraint(c4);
        final Constraint c5 = geq(sandwich, 0);
        m.addConstraint(c5);
        final Constraint c6 = eq(obj, sumProfit);
        m.addConstraint(c6);


        final Solver s = new CPSolver();
        s.setPrecision(0.1);

        //ChocoLogging.setVerbosity(Verbosity.SEARCH);
        s.read(m);

        s.maximize(s.getVar(obj), true);
    }

    @Test
    public void testSciss() {
        final Model m = new CPModel();
        final RealVariable s1 = makeRealVar( "start1", 0.0, 4.0 );
        final RealVariable s2 = makeRealVar( "stop1", 0.0, 60.0 );

        m.addConstraint(geq( s2, plus( 2.0, s1 )));

        final Solver s = new CPSolver();
        s.setPrecision(0.1);
        s.read(m);
        s.solve();
    }
}
