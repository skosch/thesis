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
package choco.model.constraints.global;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import org.junit.Test;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/06/12
 */
public class EquationTest {


    private Constraint sum(IntegerVariable[] vars, int[] coeffs, int r) {
        return Choco.eq(Choco.scalar(vars, coeffs), r);
    }

    private Constraint sumR(IntegerVariable[] vars, int[] coeffs, int r) {
        return Choco.equation(vars, coeffs, r);
    }

    @Test
    public void test1() {
        IntegerVariable[] vars = Choco.makeIntVarArray("v", 3, 0, 4, choco.Options.V_ENUM);
        int[] coeffs = new int[]{1,2,3};

        Model m1 = new CPModel();
        m1.addConstraint(sum(vars, coeffs, 6));
        CPSolver s1 = new CPSolver();
        s1.read(m1);
        s1.solveAll();
        Model m2 = new CPModel();
        m2.addConstraint(sumR(vars, coeffs, 6));
        CPSolver s2 = new CPSolver();
        s2.read(m2);
        s2.solveAll();

        org.junit.Assert.assertEquals(s1.getSolutionCount(), s2.getSolutionCount());

    }


    @Test
    public void test2() {

        Random r = new Random();
        int i = 4;
        for (int seed = 1; seed < 100; seed++) {
            r.setSeed(seed);
            IntegerVariable[] vars = Choco.makeIntVarArray("v", i, -15 + r.nextInt(15), r.nextInt(15), Options.V_BOUND);
            int[] coeffs = new int[i];
            for (int j = 0; j < i; j++) {
                coeffs[j] = r.nextInt(10) - 5;
            }
            Model m1 = new CPModel();
            m1.addConstraint(sum(vars, coeffs, 0));
            CPSolver s1 = new CPSolver();
            s1.read(m1);
            s1.solveAll();
            Model m2 = new CPModel();
            m2.addConstraint(sumR(vars, coeffs, 0));
            CPSolver s2 = new CPSolver();
            s2.read(m2);
            s2.solveAll();

            org.junit.Assert.assertEquals(s1.getSolutionCount(), s2.getSolutionCount());
            System.out.printf("%d vs %d\n", s1.getNodeCount(), s2.getNodeCount());
        }
    }
}
