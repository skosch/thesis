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

package choco.model.constraints.global;

import choco.Choco;
import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: GROCHART
 * Date: 8 janv. 2008
 * Time: 18:22:15
 */
public class BoundGccTest {
    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    @Test
  public void testIsSatisfied() {
    Model m = new CPModel();
    IntegerVariable v1 = makeIntVar("v1", 1, 1);
    IntegerVariable v2 = makeIntVar("v2", 1, 1);
    IntegerVariable v3 = makeIntVar("v3", 2, 2);
    IntegerVariable v4 = makeIntVar("v4", 2, 2);
    Constraint c1 = globalCardinality(new IntegerVariable[]{v1, v2, v3, v4}, new int[]{1, 1}, new int[]{2, 2}, 1);
    Constraint c2 = globalCardinality(new IntegerVariable[]{v1, v2, v3, v4}, new int[]{1, 1}, new int[]{1, 3}, 1);
    LOGGER.info(c1.pretty());
    LOGGER.info(c2.pretty());
    m.addConstraints(Options.C_GCC_BC, c1, c2);
    CPSolver s = new CPSolver();
    s.read(m);
    SConstraint c = s.getCstr(c1);
    assertTrue(c.isSatisfied());
    assertFalse(s.getCstr(c2).isSatisfied());
  }

    @Test
  public void testIsSatisfiedVar() {
    Model m = new CPModel();
    IntegerVariable v1 = makeIntVar("v1", 1, 1);
    IntegerVariable v2 = makeIntVar("v2", 1, 1);
    IntegerVariable v3 = makeIntVar("v3", 2, 2);
    IntegerVariable v4 = makeIntVar("v4", 2, 2);
    IntegerVariable x = makeIntVar("x", 2, 2);
    IntegerVariable y = makeIntVar("y", 1, 1);
    Constraint c1 = globalCardinality(new IntegerVariable[]{v1, v2, v3}, new IntegerVariable[]{x, y}, 1);
    Constraint c2 = globalCardinality(new IntegerVariable[]{v1, v3, v4}, new IntegerVariable[]{x, y}, 1);
    LOGGER.info(c1.pretty());
    LOGGER.info(c2.pretty());
        m.addConstraints(c1, c2);
        CPSolver s = new CPSolver();
        s.read(m);
    assertTrue(s.getCstr(c1).isSatisfied());
    assertFalse(s.getCstr(c2).isSatisfied());
  }


    @Test
    public void test2(){
        int MAX = 6;
        for(int seed = 0; seed < 30; seed++){
            Random r = new Random();
            gcc(r.nextInt(MAX)+1, r.nextInt(MAX)+1);
        }
    }

    @Test
    public void test2bis(){
        gcc(3, 4);
    }

    private static void gcc(int nbVariable, int nbValue){
        LOGGER.info("dim:"+nbVariable+" nbVal:"+nbValue);
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("vars", nbVariable, 1, nbValue);
        IntegerVariable[] card = makeIntVarArray("card", nbValue, 0, nbVariable);

        m.addConstraint(Choco.globalCardinality(vars, card, 1));
        s.read(m);
        s.solveAll();
    }



    @Test
    public void test3(){
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("vars", 6, 0, 3);
        IntegerVariable[] card = makeIntVarArray("card", 4, 0, 6);

        m.addConstraint(Choco.globalCardinality(vars, card, 0));

        m.addConstraint(eq(vars[0], 0));
        m.addConstraint(eq(vars[1], 1));
        m.addConstraint(eq(vars[2], 3));
        m.addConstraint(eq(vars[3], 2));
        m.addConstraint(eq(vars[4], 0));
        m.addConstraint(eq(vars[5], 0));

        s.read(m);
        s.solve();
        Assert.assertTrue(s.getNbSolutions()>0);
    }

    @Test
    public void test4(){
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("vars", 2, 0, 1);
        IntegerVariable[] card = makeIntVarArray("card", 2, 0, 2);

        m.addConstraint(Choco.globalCardinality(vars, card, 0));

        m.addConstraint(eq(vars[0], 0));
        m.addConstraint(eq(vars[1], 1));

        s.read(m);
        s.solve();
        Assert.assertTrue(s.getNbSolutions()>0);
    }


    @Test
    public void test5(){
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("vars", 3, 1, 3);
        IntegerVariable[] card = makeIntVarArray("card", 3, 0, 3);
        card[0] = constant(0);
        card[1] = constant(1);

        m.addConstraint(Choco.globalCardinality(vars, card, 1));

        s.read(m);
        s.solveAll();
        Assert.assertEquals(3, s.getNbSolutions());
    }

    @Test
    public void test6(){
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("vars", 1, 3, 3);
        IntegerVariable[] card = makeIntVarArray("card", 4, 0, 1);


        m.addConstraint(Choco.globalCardinality(vars, card, 0));

        s.read(m);
        s.solveAll();
        Assert.assertEquals(1, s.getNbSolutions());
    }

    @Test
    public void test6ter(){
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("vars", 1, 0, 3);
        IntegerVariable[] card = makeIntVarArray("card", 5, 0, 1);


        m.addConstraint(Choco.globalCardinality(vars, card, 0));

        s.read(m);
        s.solveAll();
        Assert.assertEquals(4, s.getNbSolutions());
    }

    @Test
    public void test6quater(){
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("vars", 1, 1, 2);
        IntegerVariable[] card = makeIntVarArray("card", 5, 0, 1);


        m.addConstraint(Choco.globalCardinality(vars, card, 0));

        s.read(m);
        s.solveAll();
        Assert.assertEquals(2, s.getNbSolutions());
    }

    @Test
    public void test7(){
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("vars", 1, 0, 3);
        IntegerVariable[] card = makeIntVarArray("card", 4, 0, 1);


        m.addConstraint(Choco.globalCardinality(vars, card, 0));
        m.addConstraint(Choco.eq(vars[0], 3));

        s.read(m);
        s.solveAll();
        Assert.assertEquals(1, s.getNbSolutions());
    }

    @Test
    public void test8(){
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("vars", 1, 3, 3);
        int low[] = new int[]{0,0,0,0};
        int upp[] = new int[]{0,0,1,1};

        m.addConstraint(Choco.globalCardinality(vars, low, upp, 0));

        s.read(m);
        s.solveAll();
        Assert.assertEquals(1, s.getNbSolutions());
    }

    @Test
    public void test82(){
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("vars", 1, 3, 3);
        int low[] = new int[]{0,0,0,0};
        int upp[] = new int[]{0,0,1,1};

        m.addConstraint(Choco.globalCardinality(vars, low, upp, 0));

        s.read(m);
        s.solveAll();
        Assert.assertEquals(1, s.getNbSolutions());
    }

}
