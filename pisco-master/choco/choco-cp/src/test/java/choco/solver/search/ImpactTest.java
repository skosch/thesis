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

package choco.solver.search;

import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.ImpactBasedBranching;
import choco.cp.solver.search.integer.branching.domwdeg.DomOverWDegBranchingNew;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;

/**
 * Created by IntelliJ IDEA.
 * User: GROCHART
 * Date: 17 mai 2008
 * Time: 12:36:46
 */
public class ImpactTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    @Test
    public void test1() {
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable[] vs1 = Choco.makeIntVarArray("v1", 10, 2, 10, Options.V_ENUM);

        for (int i = 0; i < vs1.length; i++) {
            for (int j = i + 1; j < vs1.length; j++) {
                m.addConstraint(Choco.neq(vs1[i], vs1[j]));
            }
        }

        s.read(m);
        s.getConfiguration().putInt(Configuration.INIT_IMPACT_TIME_LIMIT, 100);
        ImpactBasedBranching ibb = new ImpactBasedBranching(s);
        s.addGoal(ibb);
       // ChocoLogging.toVerbose();
        s.solve();
        Assert.assertEquals(0, s.getSolutionCount());
        Assert.assertEquals(260650, s.getNodeCount());

    }

    @Test
    public void test2() {
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable[] vs1 = Choco.makeIntVarArray("v1", 10, 2, 10, Options.V_BOUND);

        for (int i = 0; i < vs1.length; i++) {
            for (int j = i + 1; j < vs1.length; j++) {
                m.addConstraint(Choco.neq(vs1[i], vs1[j]));
            }
        }
        s.read(m);

        ImpactBasedBranching ibb = new ImpactBasedBranching(s);
        s.getConfiguration().putInt(Configuration.INIT_IMPACT_TIME_LIMIT, 100);
        s.addGoal(ibb);

        s.solve();
        Assert.assertEquals(0, s.getSolutionCount());
        Assert.assertEquals(260650, s.getNodeCount());
    }

    @Test
    public void test3() {
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable[] vs1 = Choco.makeIntVarArray("v", 2, 0, 10, Options.V_ENUM);
        m.addConstraint(Choco.eq(Choco.plus(vs1[0], vs1[1]),-1));
        s.read(m);
        s.getConfiguration().putInt(Configuration.INIT_IMPACT_TIME_LIMIT, 100);
        ImpactBasedBranching ibb = new ImpactBasedBranching(s);
        s.addGoal(ibb);
        ChocoLogging.toVerbose();
        s.solve();
        Assert.assertEquals(0, s.getSolutionCount());
        Assert.assertEquals(0, s.getNodeCount());
    }
    
    @Test
    public void test4() {
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable[] vs1 = Choco.makeIntVarArray("v1", 3, 0, 1, Options.V_ENUM);
        for (int i = 0; i < vs1.length; i++) {
            for (int j = i + 1; j < vs1.length; j++) {
                m.addConstraint(Choco.neq(vs1[i], vs1[j]));
            }
        }
        s.read(m);

        ImpactBasedBranching ibb = new ImpactBasedBranching(s);
        s.getConfiguration().putInt(Configuration.INIT_IMPACT_TIME_LIMIT, 100);
        s.addGoal(ibb);

        s.solve();
        Assert.assertEquals(0, s.getSolutionCount());
        Assert.assertEquals(0, s.getNodeCount());
    }



    @Test
    public void testMagicSquare() {
        testMagicSquare(4, 9);
        testMagicSquare(7, 289);
        testMagicSquare(9, 1005);

        testMagicSquareRestartDwdeg(4, 9);
        testMagicSquareRestartDwdeg(7, 9487);
    }

    public void testMagicSquare(int n, int nnodes) {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] vars = Choco.makeIntVarArray("C", n * n, 1, n * n);
        m.addVariables(Options.V_ENUM, vars);

        IntegerVariable sum = Choco.makeIntVar("sum", 1, n * n * (n * n + 1) / 2);
        m.addVariable(sum);

        m.addConstraint(Options.C_ALLDIFFERENT_BC, Choco.allDifferent(vars));
        for (int i = 0; i < n; i++) {
            IntegerVariable[] col = new IntegerVariable[n];
            IntegerVariable[] row = new IntegerVariable[n];

            for (int j = 0; j < n; j++) {
                col[j] = vars[i * n + j];
                row[j] = vars[j * n + i];
            }

            m.addConstraint(Choco.eq(Choco.sum(col), sum));
            m.addConstraint(Choco.eq(Choco.sum(row), sum));
        }

        m.addConstraint(Choco.eq(sum, n * (n * n + 1) / 2));

        CPSolver s = new CPSolver();
        s.read(m);
        ImpactBasedBranching ibb = new ImpactBasedBranching(s);
        s.getConfiguration().putInt(Configuration.INIT_IMPACT_TIME_LIMIT, 1000000);

        s.setTimeLimit(65000);
        s.setGeometricRestart(14, 1.5d);
        s.addGoal(ibb);
        s.solve();
        Assert.assertEquals(nnodes, s.getNodeCount());
    }

    public void testMagicSquareRestartDwdeg(int n, int nnodes) {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] vars = Choco.makeIntVarArray("C", n * n, 1, n * n);
        m.addVariables(Options.V_ENUM, vars);

        IntegerVariable sum = Choco.makeIntVar("sum", 1, n * n * (n * n + 1) / 2);
        m.addVariable(sum);

        m.addConstraint(Options.C_ALLDIFFERENT_BC, Choco.allDifferent(vars));
        for (int i = 0; i < n; i++) {
            IntegerVariable[] col = new IntegerVariable[n];
            IntegerVariable[] row = new IntegerVariable[n];

            for (int j = 0; j < n; j++) {
                col[j] = vars[i * n + j];
                row[j] = vars[j * n + i];
            }

            m.addConstraint(Choco.eq(Choco.sum(col), sum));
            m.addConstraint(Choco.eq(Choco.sum(row), sum));
        }

        m.addConstraint(Choco.eq(sum, n * (n * n + 1) / 2));

        CPSolver s = new CPSolver();
        s.read(m);

        s.setGeometricRestart(14, 1.5d);
        s.addGoal(new DomOverWDegBranchingNew(s, s.getVar(vars), new IncreasingDomain(), 0));
        s.solve();

        Assert.assertEquals(nnodes, s.getNodeCount());
    }

}
