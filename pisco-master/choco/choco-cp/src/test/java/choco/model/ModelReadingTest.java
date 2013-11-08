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

package choco.model;

import choco.Choco;
import static choco.Choco.leq;
import static choco.Choco.scalar;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 15 janv. 2010
 * Time: 17:46:30
 * <p/>
 * Test suite on model reading performance
 */
public class ModelReadingTest {

    @Test
    public void test1() {
        Model m = new CPModel();
        IntegerVariable[] vvs = Choco.makeBooleanVarArray("b", 20801);
        m.addVariables(vvs);

        int n = 500;
        IntegerVariable[] vars = Choco.makeBooleanVarArray("b", n);
        int[] coeffs = new int[n];
        Arrays.fill(coeffs, 12);
        IntegerVariable[] result = Choco.makeIntVarArray("result", n, 0, 2048);

        for (int i = 0; i < 100; i++) {
            m.addConstraint(leq(scalar(vars, coeffs), result[i]));
        }
        Solver s = new CPSolver();
        long ts = -System.currentTimeMillis();
        s.read(m);
        ts += System.currentTimeMillis();
        Assert.assertTrue(ts < 5000);

    }
}
