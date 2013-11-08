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
package samples.tutorials.continuous;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.real.CyclicRealVarSelector;
import choco.cp.solver.search.real.RealIncreasingDomain;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealConstantVariable;
import choco.kernel.model.variables.real.RealExpressionVariable;
import choco.kernel.model.variables.real.RealVariable;
import samples.tutorials.PatternExample;

import static choco.Choco.*;

/**
 * Samples in Elisa package.
 */
public class Arm extends PatternExample {

    RealVariable a, b, alpha, beta, x, y, jr, ir;


    @Override
    public void printDescription() {
        LOGGER.info("ARM problem is:");
        LOGGER.info(" y - ( a * sin(alpha) + b * sin(alpha - beta) ) = 0.0");
        LOGGER.info(" x - ( a * cos(alpha) + b * cos(alpha - beta) ) = 0.0");
        LOGGER.info(" a * cos(alpha) <= 10.0");
        LOGGER.info(" b * sin(alpha) <= 8.0");
        LOGGER.info(" (x - 8)^2 + (y - 4)^2 <= 4.0");
        LOGGER.info(" alpha = PI/6");
        LOGGER.info(" a - (ir * 2) = 0");
        LOGGER.info(" b - (jr * 2) = 0");
    }

    @Override
    public void buildModel() {
        model = new CPModel();

        a = makeRealVar("a", 2.0, 8.0);
        b = makeRealVar("b", 2.0, 8.0);
        alpha = makeRealVar("alpha", 0.0, Math.PI);
        beta = makeRealVar("beta", 0.0, Math.PI);
        x = makeRealVar("x", 0.0, 10.0);
        y = makeRealVar("y", 0.0, 8.0);

        IntegerVariable i = makeIntVar("i", 1, 4);
        IntegerVariable j = makeIntVar("j", 1, 4);
        ir = makeRealVar("i'", 1.0, 4.0);
        jr = makeRealVar("j'", 1.0, 4.0);
        model.addConstraint(eq(ir, i));
        model.addConstraint(eq(jr, j));

        RealExpressionVariable exp1 = minus(y, plus(mult(a, sin(alpha)), mult(b, sin(minus(alpha, beta)))));
        RealExpressionVariable exp2 = minus(x, plus(mult(a, cos(alpha)), mult(b, cos(minus(alpha, beta)))));
        model.addConstraint(eq(exp1, 0.0));
        model.addConstraint(eq(exp2, 0.0));

        model.addConstraint(leq(mult(a, cos(alpha)), 10.0));
        model.addConstraint(leq(mult(a, sin(alpha)), 8.0));

        RealExpressionVariable circle = plus(power(minus(x, 8), 2), power(minus(y, 4), 2));
        model.addConstraint(leq(circle, 4.0));

        model.addConstraint(eq(alpha, Math.PI / 6));
        RealConstantVariable v = new RealConstantVariable(1.99, 2.01);
        model.addConstraint(eq(minus(a, mult(ir, v)), 0.0));
        model.addConstraint(eq(minus(b, mult(jr, v)), 0.0));
    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
        solver.setVarRealSelector(new CyclicRealVarSelector(solver));
        solver.setValRealIterator(new RealIncreasingDomain());
    }

    @Override
    public void solve() {
        solver.solve();
    }

    @Override
    public void prettyOut() {
        LOGGER.info("\nWhere:");
        LOGGER.info("a = "+ solver.getVar(a).getValue());
        LOGGER.info("b = "+ solver.getVar(b).getValue());
        LOGGER.info("alpha = "+ solver.getVar(alpha).getValue());
        LOGGER.info("beta = "+ solver.getVar(beta).getValue());
        LOGGER.info("x = "+ solver.getVar(x).getValue());
        LOGGER.info("y = "+ solver.getVar(y).getValue());
        LOGGER.info("ir = "+ solver.getVar(ir).getValue());
        LOGGER.info("jr = "+ solver.getVar(jr).getValue());
    }
    
	public static void main(String[] args) {
        new Arm().execute();
    }
}