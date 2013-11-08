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

package choco.model.constraints.integer;

import choco.Choco;
import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.Constant;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 12 août 2008
 * Time: 11:31:25
 */
public class EqTest {

    CPModel m;
    CPSolver s;

    @Before
    public void before(){
        m = new CPModel();
        s = new CPSolver();
    }

    @After
    public void after(){
        m = null;
        s = null;
    }

    @Test
    public void test1(){
        int c = 1;
        IntegerVariable v = makeIntVar("v", 0, 2);
        m.addConstraint(eq(v, c));
        s.read(m);
        s.solve();
        Assert.assertTrue("no solution", s.getNbSolutions()!=0);   
    }

    @Test
    public void test2(){
        int c = 1;
        IntegerVariable v = makeIntVar("v", 0, 2);
        IntegerExpressionVariable w = minus(v, 1);
        m.addConstraint(eq(w, c));
        s.read(m);
        s.solve();
        Assert.assertTrue("no solution", s.getNbSolutions()!=0);
    }

    @Test
    public void test3(){
        IntegerVariable v1 = makeIntVar("v1", 0, 2);
        IntegerVariable v2 = makeIntVar("v2", 0, 2);
        IntegerExpressionVariable w1 = plus(v1, 1);
        IntegerExpressionVariable w2 = minus(v2, 1);
        m.addConstraint(eq(w1, w2));
        s.read(m);
        s.solve();
        Assert.assertTrue("no solution", s.getNbSolutions()!=0);
    }

    @Test
    public void test4(){
        IntegerVariable v1 = makeIntVar("v1", 0, 2);
        IntegerVariable v2 = makeIntVar("v2", 0, 2);
        m.addConstraint(eq(v1, v2));
        s.read(m);
        s.solve();
        Assert.assertTrue("no solution", s.getNbSolutions()!=0);
    }

    @Test
    public void test5(){
    	IntegerVariable v = Choco.makeIntVar("v", -2, 0);
        Constraint c = eq(v, plus(v,0));
        m.addConstraint(c);
        s.read(m);
        Assert.assertEquals("wrong type", Constant.TRUE, s.getCstr(c));
        s.solveAll();
        Assert.assertEquals("nb of solution", 3, s.getNbSolutions());

    }


    @Test
    public void test6(){
        IntegerVariable v = Choco.makeIntVar("v", -2, 0);
        Constraint c = eq(v, plus(v,1));
        m.addConstraint(c);
        s.read(m);
        Assert.assertEquals("wrong type", Constant.FALSE, s.getCstr(c));
        s.solveAll();
        Assert.assertEquals("nb of solution", 0, s.getNbSolutions());

    }

    @Test
    public void test7(){
        IntegerVariable A = Choco.makeIntVar("A", -4, -2);
        IntegerVariable B = Choco.makeIntVar("B", 2, 4);
        IntegerVariable C = Choco.makeIntVar("C", 2, 4);
        IntegerVariable D = Choco.makeIntVar("D", 2, 4);
        IntegerVariable E = Choco.makeIntVar("E", 2, 4);
        IntegerVariable F = Choco.makeIntVar("F", 2, 4);

        Constraint c = Choco.eq( Choco.power(E, F), Choco.mult( Choco.power( A, B) ,Choco.power( C, D) ) );

        Model m  = new CPModel();
        m.addConstraint(c);
        m.setDefaultExpressionDecomposition(true);
        //ChocoLogging.setVerbosity(Verbosity.SOLUTION);
        Solver s = new CPSolver();
        s.read(m);
        s.solveAll();
    }
}

