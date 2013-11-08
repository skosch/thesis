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

package choco.solver.blackboxsolver;

import choco.Choco;
import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.BoundAllDiff;
import choco.cp.solver.constraints.global.matching.AllDifferent;
import choco.cp.solver.constraints.global.scheduling.disjunctive.Disjunctive;
import choco.cp.solver.constraints.integer.*;
import choco.cp.solver.constraints.integer.bool.BoolIntLinComb;
import choco.cp.solver.constraints.integer.bool.sat.ClauseStore;
import choco.cp.solver.constraints.integer.extension.CspLargeSConstraint;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.variables.integer.*;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.shaker.tools.factory.CPModelFactory;
import static choco.shaker.tools.factory.ConstraintFactory.C;
import choco.shaker.tools.factory.MetaConstraintFactory;
import choco.shaker.tools.factory.OperatorFactory;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.Random;
import java.util.logging.Logger;

/**
 *
 */
public class ConstraintsDetectionTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    @Test
    public void testAutoDetectionScalar0() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable v = makeIntVar("v", 0, 2);
        Constraint c = eq(1,minus(v,1));//plus(minus(mult(v[0], 2), v[2]), mult(v[1], 3)), 12), 5);
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
        }
        it.dispose();
        s.solveAll();
        assertEquals(1, s.getNbSolutions());

    }

    @Test
    public void testAutoDetectionScalar1() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 10, 0, 2);
        Constraint c = geq(plus(plus(minus(mult(v[0], 2), v[2]), mult(v[1], 3)), 12), 5);
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            assertTrue(p instanceof IntLinComb);
            LOGGER.info(p.pretty());
        }
        it.dispose();
        s.solveAll();
        assertEquals(27, s.getNbSolutions());

    }

    @Test
    public void testAutoDetectionScalar2() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 10, 0, 2);
        Constraint c = lt(plus(plus(plus(minus(mult(v[0], 2), v[2]), mult(v[4], -10)), mult(v[1], 3)), 12), 5);
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            assertTrue(p instanceof IntLinComb);
            LOGGER.info(p.pretty());
        }
        it.dispose();
        s.solveAll();
        assertEquals(36, s.getNbSolutions());

    }

    @Test
    public void testAutoDetectionScalar3() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 10, 0, 2);
        Constraint c = lt(plus(mult(v[0], v[1]), 12), v[3]);
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());

        assertTrue(s.getNbIntConstraints() > 1);

    }


    @Test
    public void testAutoDetectionScalar4() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 10, 0, 1);
        Constraint c = lt(plus(v[0], plus(v[1], plus(v[3], plus(v[4], 3)))), 12);
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            assertTrue(p instanceof BoolIntLinComb);
            LOGGER.info(p.pretty());
        }
        it.dispose();
        s.solveAll();
        assertEquals(16, s.getNbSolutions());

    }

    @Test
    public void testAutoDetectionScalar5() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 10, 0, 2);
        Constraint c = gt(plus(sum(v), mult(v[0], 10)), 32);
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            assertTrue(p instanceof IntLinComb);
            LOGGER.info(p.pretty());
        }
        it.dispose();
        s.solveAll();
        assertEquals(5365, s.getNbSolutions());

    }

    @Test
    public void testAutoDetectionScalar5bis() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 3, 0, 2);
        Constraint c = gt(plus(scalar(v, new int[]{2, -1, -3}), mult(v[0], 10)), 12);
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            assertTrue(p instanceof IntLinComb);
            LOGGER.info(p.pretty());
        }
        it.dispose();
        s.solveAll();
        assertEquals(9, s.getNbSolutions());

    }

    @Test
    public void testAutoDetectionScalar6() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 10, 0, 2);
        Constraint c = gt(plus(plus(mult(v[0], 5), plus(mult(v[1], 2), mult(4, v[0]))), 9), 12);
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            //assertTrue(p instanceof IntLinComb);
        }
        it.dispose();
        s.solveAll();
        assertEquals(7, s.getNbSolutions());

    }

    @Test
    public void testAutoDetectionScalar7() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 10, 0, 2);
        Constraint c = gt(plus(plus(neg(v[0]), plus(mult(neg(v[1]), 2), mult(4, v[0]))), 9), 12);
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof IntLinComb);
        }
        it.dispose();
        s.solveAll();
        assertEquals(2, s.getNbSolutions());

    }

    @Test
    public void testAutoDetectionScalar8() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 10, -3, 3);
        Constraint c = gt(mult(3, plus(neg(v[0]), plus(mult(neg(v[1]), 2), 3))), 12);
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof IntLinComb);
        }
        it.dispose();
        s.solveAll();
        assertEquals(19, s.getNbSolutions());

    }

    @Test
    public void testAutoDetectionScalar9() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 10, -3, 3);
        Constraint c = gt(neg(plus(v[4], mult(3, plus(neg(v[0]), plus(mult(neg(v[1]), 2), 3))))), 12);
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof IntLinComb);
        }
        it.dispose();
        s.solveAll();
        assertEquals(19, s.getNbSolutions());

    }

    @Test
    public void testAutoDetectionScalar10() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 10, -3, 3);
        Constraint c = eq(neg(plus(v[4], mult(3, plus(neg(v[0]), plus(mult(neg(v[1]), 2), 3))))), 12);
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof IntLinComb);
        }
        it.dispose();
        s.solveAll();
        assertEquals(5, s.getNbSolutions());

    }

    @Test
    public void testAutoDetectionScalar11() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 10, -3, 3);
        Constraint c = neq(neg(plus(v[4], mult(3, plus(neg(v[0]), plus(mult(neg(v[1]), 2), 3))))), 12);
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            //assertTrue(p instanceof IntLinComb);
        }
        it.dispose();
        s.solveAll();
        assertEquals(338, s.getNbSolutions());
    }

    @Test
    public void testAutoDetectionScalar12() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = new IntegerVariable[4];
        v[0] = makeIntVar("v0", 0, 100000);
        v[1] = makeIntVar("v1", 0, 4);
        v[2] = makeIntVar("v2", 0, 5);
        v[3] = makeIntVar("v3", 0, 6);
        Constraint c = eq(v[0], scalar(new int[]{1000, 100, 10, 1}, new IntegerVariable[]{v[1], v[2], v[3], v[1]}));
        m.addConstraint(c);
        CPSolver s = new CPSolver();

        s.read(m);

        LOGGER.info(s.getCstr(c).pretty());
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            //assertTrue(p instanceof IntLinComb);
        }
        it.dispose();
        s.solveAll();
        assertEquals(210, s.getNbSolutions());
    }

    @Test
    public void testAutoDetectionSameSign() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 10, new int[]{-3,-2,-1,1,2,3});
        Constraint c = geq(mult(v[0], v[1]), 0);
        m.addConstraint(c);
        CPSolver s = new PreProcessCPSolver();
        LOGGER.info(""+c);

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof SignOp);
        }
        it.dispose();
        s.solveAll();
        assertEquals(18, s.getNbSolutions());

    }

    @Test
    public void testAutoDetectionOppSign() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 10, new int[]{-3,-2,-1,1,2,3});
        Constraint c = lt(mult(v[0], v[1]), 0);
        m.addConstraint(c);
        CPSolver s = new PreProcessCPSolver();
        LOGGER.info(""+c);

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof SignOp);
        }
        it.dispose();
        s.solveAll();
        assertEquals(18, s.getNbSolutions());

    }

    @Test
    public void testFullSignDetection(){
        Model m = new CPModel();
        CPSolver s = new PreProcessCPSolver();

        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 2, new int[]{-3,-2,-1,1,2,3});
        Constraint[] c = new Constraint[16];
        // x.y > 0 same
        c[0] = gt(mult(v[0], v[1]), 0);
        // 0 < x.y same
        c[1] = lt(0, mult(v[0], v[1]));
        // x.y >= 0 same
        c[2] = geq(mult(v[0], v[1]), 0);
        // 0 <= x.y same
        c[3] = leq(0, mult(v[0], v[1]));
        // x.y > -1 same
        c[4] = gt(mult(v[0], v[1]), -1);
        // -1 < x.y same
        c[5] = lt(-1, mult(v[0], v[1]));
        // x.y >= 1 same
        c[6] = geq(mult(v[0], v[1]), 1);
        // 1 <= x.y same
        c[7] = leq(1, mult(v[0], v[1]));

        // x.y <= 0 opp
        c[8] = leq(mult(v[0], v[1]), 0);
        // 0 >= x.y opp
        c[9] = geq(0, mult(v[0], v[1]));
        // x.y < 1 opp
        c[10] = lt(mult(v[0], v[1]), 1);
        // 1 > x.y opp
        c[11] = gt(1, mult(v[0], v[1]));
        // x.y < 0 opp
        c[12] = lt(mult(v[0], v[1]), 0);
        // 0 > x.y opp
        c[13] = gt(0, mult(v[0], v[1]));
        // x.y <= -1 opp
        c[14] = leq(mult(v[0], v[1]), -1);
        // -1 >= x.y opp
        c[15] = geq(-1, mult(v[0], v[1]));

        m.addConstraints(c);

        s.read(m);

        //Same sign
        for(int i = 0; i < 8; i++){
            SConstraint sc = s.getCstr(c[i]);
            assertTrue("type incorrect :" +i , sc instanceof SignOp);
            assertTrue("pretty incorrect :" +i ,sc.pretty().contains("same sign"));
        }
        //Opp sign
        for(int i = 8; i < 16; i++){
            SConstraint sc = s.getCstr(c[i]);
            assertTrue("type incorrect :" +i , sc instanceof SignOp);
            assertTrue("pretty incorrect :" +i , sc.pretty().contains("different sign"));
        }

    }


    @Test
    public void testAutoDetectionMin1() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 3, -3, 3);
        IntegerVariable minv = makeIntVar("min", -3, 3);

        Constraint c = eq(min(v), minv);
        m.addConstraint(c);
        CPSolver s = new PreProcessCPSolver();
        LOGGER.info(""+c);

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof MinOfAList);
        }
        it.dispose();
        s.solveAll();
        assertEquals(343, s.getNbSolutions());
    }

    @Test
    public void testAutoDetectionMin2() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 3, -3, 3);
        IntegerVariable minv = makeIntVar("min", -3, 3);

        Constraint c = eq(minv, min(v));
        m.addConstraint(c);
        CPSolver s = new PreProcessCPSolver();
        LOGGER.info(""+c);

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof MinOfAList);
        }
        it.dispose();
        s.solveAll();
        assertEquals(343, s.getNbSolutions());
    }

    @Test
    public void testAutoDetectionMax3() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 3, -3, 3);
        IntegerVariable minv = makeIntVar("min", -3, 3);

        Constraint c = eq(minv, max(v));
        m.addConstraint(c);
        CPSolver s = new PreProcessCPSolver();
        LOGGER.info(""+c);

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof MaxOfAList);
        }
        it.dispose();
        s.solveAll();
        assertEquals(343, s.getNbSolutions());

    }


    @Test
    public void testAutoDetectionMax4() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 3, -3, 3);
        IntegerVariable minv = makeIntVar("min", -3, 3);

        Constraint c = eq(minv, max(v));
        m.addConstraint(c);
        CPSolver s = new PreProcessCPSolver();
        LOGGER.info(""+c);

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof MaxOfAList);
        }
        it.dispose();
        s.solveAll();
        assertEquals(343, s.getNbSolutions());

    }


     @Test
    public void testAutoDetectionDistance1() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(false);
        IntegerVariable[] v = makeIntVarArray("v", 2, -3, 3);
        IntegerVariable d = makeIntVar("min", -3, 3);

        Constraint c = eq(d, abs(minus(v[0],v[1])));
        //Constraint c = distanceEQ(v[0],v[1],d);
         m.addConstraint(c);
        CPSolver s = new PreProcessCPSolver();
        LOGGER.info(""+c);

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof DistanceXYZ);
        }
         it.dispose();
        s.solveAll();
        assertEquals(37, s.getNbSolutions());

    }

     @Test
    public void testAutoDetectionDistance2() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(false);
        IntegerVariable[] v = makeIntVarArray("v", 2, -3, 3);
        IntegerVariable d = makeIntVar("min", -3, 3);

        Constraint c = eq(abs(minus(v[0],v[1])),d);
        //Constraint c = distanceEQ(v[0],v[1],d);
         m.addConstraint(c);
        CPSolver s = new PreProcessCPSolver();
        LOGGER.info(""+c);

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof DistanceXYZ);
        }
         it.dispose();
        s.solveAll();
        assertEquals(37, s.getNbSolutions());

    }

      @Test
    public void testAutoDetectionDistance3() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(false);
        IntegerVariable[] v = makeIntVarArray("v", 2, -3, 3);
          
        Constraint c = eq(abs(minus(v[0],v[1])),3);
        //Constraint c = distanceEQ(v[0],v[1],d);
         m.addConstraint(c);
        CPSolver s = new PreProcessCPSolver();
        LOGGER.info(""+c);

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof DistanceXYC);
        }
          it.dispose();
        s.solveAll();
        assertEquals(8, s.getNbSolutions());

    }

      @Test
    public void testAutoDetectionDistance4() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(false);
        IntegerVariable[] v = makeIntVarArray("v", 2, -3, 3);
        IntegerVariable d = makeIntVar("min", -3, 3);

        Constraint c = leq(abs(minus(v[0],v[1])),d);
        //Constraint c = distanceEQ(v[0],v[1],d);
         m.addConstraint(c);
        CPSolver s = new PreProcessCPSolver();
        LOGGER.info(""+c);

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof DistanceXYZ);
        }
          it.dispose();
        s.solveAll();
        assertEquals(92, s.getNbSolutions());

    }

     @Test
    public void testAutoDetectionDistance5() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(false);
        IntegerVariable[] v = makeIntVarArray("v", 2, -3, 3);
        IntegerVariable d = makeIntVar("min", -3, 3);
        m.addVariables(Options.V_ENUM,v);
        m.addVariable(Options.V_ENUM,d);
        Constraint c = geq(abs(minus(v[0],v[1])),d);
        //Constraint c =  distanceGT(v[0],v[1],d,-1);

        m.addConstraint(c);

        CPSolver s = new PreProcessCPSolver();
        LOGGER.info(""+c);

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            assertTrue(p instanceof DistanceXYZ);
        }
         it.dispose();
        s.solveAll();
        assertEquals(288, s.getNbSolutions());

    }

     @Test
    public void testDetectionCliques() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(false);
        IntegerVariable[] v = makeIntVarArray("v", 13, 0, 11);

        for (int i = 0; i < v.length; i++) {
            for (int j = i + 1; j < v.length; j++) {
               m.addConstraint(neq(v[i],v[j]));
            }
        }

        CPSolver s = new PreProcessCPSolver();

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
         boolean b=false;
         while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            b |= (p instanceof BoundAllDiff || p instanceof AllDifferent);
        }
         it.dispose();
        s.solveAll();
        assertEquals(0, s.getNbSolutions());
    }

    //or(le(add(X0,X1),X2),le(add(X2,X3),X0))
    @Test
    public void testDetectionDisjunctives() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 5, 0, 13);

        m.addConstraint(or(leq(plus(v[0],2),v[1]),leq(plus(v[1],3),v[0])));
        m.addConstraint(or(leq(plus(v[0],2),v[2]),leq(plus(v[2],4),v[0])));
        m.addConstraint(or(leq(plus(v[0],2),v[3]),leq(plus(v[3],5),v[0])));
        m.addConstraint(or(leq(plus(v[0],2),v[4]),leq(plus(v[4],3),v[0])));

        m.addConstraint(or(leq(plus(v[1],3),v[2]),leq(plus(v[2],4),v[1])));
        m.addConstraint(or(leq(plus(v[1],3),v[3]),leq(plus(v[3],5),v[1])));
        m.addConstraint(or(leq(plus(v[1],3),v[4]),leq(plus(v[4],3),v[1])));

        m.addConstraint(or(leq(plus(v[2],4),v[3]),leq(plus(v[3],5),v[2])));
        m.addConstraint(or(leq(plus(v[2],4),v[4]),leq(plus(v[4],3),v[2])));

        m.addConstraint(or(leq(plus(v[4],3),v[3]),leq(plus(v[3],5),v[4])));

        CPSolver s = new PreProcessCPSolver();

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        boolean disjunctivedetected = false;
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            disjunctivedetected |= p instanceof Disjunctive;
        }
        it.dispose();
        assertTrue(disjunctivedetected);
        s.solveAll();
        LOGGER.info("" + s.getNbSolutions());
        assertEquals(168, s.getNbSolutions());
    }

    @Test
    public void testDetectionDisjunctives2() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 5, 0, 13);

        m.addConstraint(or(leq(plus(2,v[0]),v[1]),leq(plus(v[1],3),v[0])));
        m.addConstraint(or(leq(plus(v[0],2),v[2]),leq(plus(v[2],4),v[0])));
        m.addConstraint(or(leq(plus(v[0],2),v[3]),leq(plus(v[3],5),v[0])));
        m.addConstraint(or(leq(plus(v[0],2),v[4]),leq(plus(v[4],3),v[0])));

        m.addConstraint(or(leq(plus(v[1],3),v[2]),leq(plus(v[2],4),v[1])));
        m.addConstraint(or(leq(plus(3,v[1]),v[3]),leq(plus(v[3],5),v[1])));
        m.addConstraint(or(leq(plus(v[4],3),v[1]),leq(plus(v[1],3),v[4])));

        m.addConstraint(or(leq(plus(4,v[2]),v[3]),leq(plus(v[3],5),v[2])));
        m.addConstraint(or(leq(plus(v[2],4),v[4]),leq(plus(3,v[4]),v[2])));

        m.addConstraint(or(leq(plus(v[4],3),v[3]),leq(plus(v[3],5),v[4])));

        CPSolver s = new PreProcessCPSolver();

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        boolean disjunctivedetected = false;
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            disjunctivedetected |= p instanceof Disjunctive;
        }
        it.dispose();
        assertTrue(disjunctivedetected);
        s.solveAll();
        LOGGER.info("" + s.getNbSolutions());
        assertEquals(168, s.getNbSolutions());
    }


    @Test
    public void testIncludedDiff() {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 5, 0, 5);
        IntegerVariable[] v2 = makeIntVarArray("v", 5, 0, 5);

        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                m.addConstraint(gt(0,mult(minus(v[i],v[j]),minus(v[j],v[i]))));
                m.addConstraint(neq(v2[i],v2[j]));
            }
        }

        CPSolver s = new PreProcessCPSolver();

        s.read(m);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        boolean alldiffd = false;
        while (it.hasNext()) {
            SConstraint p = it.next();
            LOGGER.info(p.pretty());
            LOGGER.info("" + p);
            alldiffd |= (p instanceof AllDifferent || p instanceof BoundAllDiff);
        }
        it.dispose();
        assertTrue(alldiffd);
    }

    @Test
    public void testEqualitiesDetection() {
        for (int k = 0; k < 10; k++) {
            Model m = new CPModel();
            Solver s = new PreProcessCPSolver();
            Solver s2 = new CPSolver();
            int n = 2;
            IntegerVariable[] vars = Choco.makeIntVarArray("v", n, 0, n - 1);

            for (int i = 0; i < n - 1; i++) {
                m.addConstraint(Choco.eq(vars[i], vars[i + 1]));
            }
            m.addConstraint(Choco.eq(vars[n - 1], n-1));
            long t1 = System.currentTimeMillis();
            s.read(m);
            long t2 = System.currentTimeMillis();
            s2.read(m);
            long t3 = System.currentTimeMillis();

            Assert.assertEquals("nb var BB", s.getNbIntVars(), 1);
            Assert.assertEquals("nb const var BB", s.getNbConstants(),1);
            Assert.assertEquals("nb var S", s2.getNbIntVars(), n); // n + 1 = n + cste
            Assert.assertEquals("nb var S", s2.getNbConstants(),1);
            Assert.assertTrue("One solution BB",s.solve());
            Assert.assertTrue("One solution S",s2.solve());
            Assert.assertEquals("Nb node BB",s.getSearchStrategy().getNodeCount(), 1);
            Assert.assertEquals("Nb node S",s2.getSearchStrategy().getNodeCount(), 1);
            LOGGER.info("BlackBox:" + (t2 - t1)+" / "+"Solver:" + (t3 - t2));
        }
    }


    @Test
    public void testMixedEqualitiesDetection() {
        for (int k = 0; k < 10; k++) {
            Model m = new CPModel();
            Solver s = new PreProcessCPSolver();
            Solver s2 = new CPSolver();
            int n = 1000;
            IntegerVariable[] vars = Choco.makeIntVarArray("v", n, 0, n - 1);

            for (int i = 0; i < n/2; i++) {
                m.addConstraint(Choco.eq(vars[i], vars[i + 1]));
            }
            m.addConstraint(Choco.leq(vars[n/2], vars[(n/2)+1]));
            for (int i = (n/2)+1; i < n-1; i++) {
                m.addConstraint(Choco.eq(vars[i], vars[i + 1]));
            }
            long t1 = System.currentTimeMillis();
            s.read(m);
            long t2 = System.currentTimeMillis();
            s2.read(m);
            long t3 = System.currentTimeMillis();

            Assert.assertEquals("nb var BB", s.getNbIntVars(), 2);
            Assert.assertEquals("nb var S", s2.getNbIntVars(), n);
            Assert.assertTrue("One solution BB",s.solve());
            Assert.assertTrue("One solution S",s2.solve());
            Assert.assertEquals("Nb node BB",s.getSearchStrategy().getNodeCount(), 3);
            Assert.assertEquals("Nb node S",s2.getSearchStrategy().getNodeCount(), 3);
            LOGGER.info("BlackBox:" + (t2 - t1)+" / "+"Solver:" + (t3 - t2));
        }
    }

    @Test
    public void testEqualitiesWithConstante() {
        for (int k = 0; k < 4; k++) {
            Model m = new CPModel();
            Solver s = new PreProcessCPSolver();
            IntegerVariable v1 = makeIntVar("v1", 0, 2);
            IntegerVariable v2 = null;
            Boolean doable = null;
            switch (k){
                case 0:
                    v2 = makeIntVar("v2", 0, 2);
                    doable = true;
                break;
                case 1:
                    v2 = makeIntVar("v2", -1, 3);
                    doable = true;
                    break;
                case 2:
                    v2 = makeIntVar("v2", 1, 1);
                    doable = true;
                    break;
                case 3:
                    v2 = makeIntVar("v2", 4, 6);
                    doable = false;
                    break;
            }
            m.addConstraint(Choco.eq(v1, v2));
            s.read(m);
            s.solve();
            Assert.assertEquals("Not expected results", doable, s.isFeasible());

        }
    }

    @Test
    public void testNoVariableAnymore(){
        try{
            Model m = new CPModel();
            Solver s = new CPSolver();
            IntegerVariable v = makeIntVar("c", 1, 1);
            m.addVariable(v);
            s.read(m);
            s.solve();
        }catch (Exception e){
            Assert.fail();
        }

    }

    @Test
      public void bugOnConstantTimes() {
          Model m = new CPModel();
          Solver s = new CPSolver();
          IntegerVariable x = makeIntVar("x", 0, 10);
          IntegerVariable y = makeIntVar("var2", 0, 10);
          IntegerVariable result = makeIntVar("result", 0, 10);
          Constraint c = eq(result,
                          mult(2,
                              max(minus(x, y),
                                  constant(0))
                              )
          );

          m.addConstraint(c);
          s.read(m);
          s.solve();
      }


    @Test
    public void detectEqualities(){
        String[] options  = new String[]{Options.V_BTREE, Options.V_ENUM, Options.V_BLIST, Options.V_LINK, Options.V_BOUND};
        Class[] classes = new Class[]{IntervalBTreeDomain.class, BitSetIntDomain.class,
                BipartiteIntDomain.class, LinkedIntDomain.class, IntervalIntDomain.class};

        Model m;
        PreProcessCPSolver s;
        Random r;
        for(int seed = 0; seed < 50; seed++){
            r = new Random(seed);
            m = new CPModel();
            int dx = r.nextInt(options.length);
            int dy = r.nextInt(options.length);
            IntegerVariable x = Choco.makeIntVar("x", 0, 2, options[dx]);
            IntegerVariable y = Choco.makeIntVar("y", -10, 10, options[dy]);
            m.addConstraint(eq(x, y));
            s = new PreProcessCPSolver();
            s.read(m);
            Class c = classes[Math.min(dx,dy)];
            Assert.assertEquals("bad object for X ("+seed+")", s.getVar(x).getDomain().getClass(),  c);
            Assert.assertEquals("bad object for Y ("+seed+")", s.getVar(y).getDomain().getClass(),  c);
            Assert.assertEquals("wrong lower bound for X ("+seed+")", 0, s.getVar(x).getInf());
            Assert.assertEquals("wrong upper bound for X ("+seed+")", 2, s.getVar(x).getSup());
            Assert.assertEquals("wrong lower bound for X ("+seed+")", 0, s.getVar(y).getInf());
            Assert.assertEquals("wrong upper bound for Y ("+seed+")", 2, s.getVar(y).getSup());
        }
    }

    @Test
    public void detectEqualities2(){
        String[] options  = new String[]{Options.V_BTREE, Options.V_ENUM, Options.V_BLIST, Options.V_LINK, Options.V_BOUND};

        Model m;
        PreProcessCPSolver s;
        Random r;
        for(int seed = 0; seed < 50; seed++){
            r = new Random(seed);
            m = new CPModel();
            int dx = r.nextInt(options.length);
            int dy = r.nextInt(options.length);
            IntegerVariable x = Choco.makeIntVar("x", 0, 1, options[dx]);
            IntegerVariable y = Choco.makeIntVar("y", -10, 10, options[dy]);
            m.addConstraint(eq(x, y));
            s = new PreProcessCPSolver();
            s.read(m);
            Assert.assertEquals("bad object for X ("+seed+")", s.getVar(x).getDomain().getClass(),  BooleanDomain.class);
            Assert.assertEquals("bad object for Y ("+seed+")", s.getVar(y).getDomain().getClass(),  BooleanDomain.class);
            Assert.assertEquals("wrong lower bound for X ("+seed+")", 0, s.getVar(x).getInf());
            Assert.assertEquals("wrong upper bound for X ("+seed+")", 1, s.getVar(x).getSup());
            Assert.assertEquals("wrong lower bound for X ("+seed+")", 0, s.getVar(y).getInf());
            Assert.assertEquals("wrong upper bound for Y ("+seed+")", 1, s.getVar(y).getSup());
        }
    }


    @Test
    public void detectEqualities3(){
        Model m;
        PreProcessCPSolver s;
        for(int size = 1000; size <= 2000; size +=100){
            m = new CPModel();
            IntegerVariable[] vars = Choco.makeIntVarArray("v", size, 0, 10, Options.V_BOUND);
            for(int i = 0; i < size-1; i++){
                m.addConstraint(eq(vars[i], vars[i+1]));
            }
            s = new PreProcessCPSolver();
            long t = -System.currentTimeMillis();
            s.read(m);
            Assert.assertEquals(1, s.getNbIntVars());
        }
    }

    @Test
    public void detectTasks1(){
        Model m;
        PreProcessCPSolver s;

        m = new CPModel();
        IntegerVariable A = Choco.makeIntVar("A", 0, 10, Options.V_BOUND);
        IntegerVariable B = Choco.makeIntVar("B", 0, 10, Options.V_BOUND);
        IntegerVariable C = Choco.makeIntVar("C", 0, 10, Options.V_BOUND);

        TaskVariable t1 = Choco.makeTaskVar("t1", A, B, C);
        TaskVariable t2 = Choco.makeTaskVar("t2", A, B, C);

        m.addVariables(t1, t2);

        s = new PreProcessCPSolver();
        s.read(m);
        
        Assert.assertEquals(1, s.getNbTaskVars());

    }

    @Test
    public void detectTasks2(){
        Model m;
        PreProcessCPSolver s;

        m = new CPModel();
        IntegerVariable A = Choco.makeIntVar("A", 0, 10, Options.V_BOUND);
        IntegerVariable B = Choco.makeIntVar("B", 0, 10, Options.V_BOUND);
        IntegerVariable C = Choco.makeIntVar("C", 0, 10, Options.V_BOUND);

        TaskVariable t1 = Choco.makeTaskVar("t2", A, C);
        TaskVariable t2 = Choco.makeTaskVar("t1", A, B, C);

        m.addVariables(t1, t2);

        s = new PreProcessCPSolver();
        s.read(m);

        Assert.assertEquals(1, s.getNbTaskVars());
        IntDomainVar a = s.getVar(A);
        IntDomainVar b = s.getVar(B);
        IntDomainVar c = s.getVar(C);

        TaskVar t = s.getVar(t2);
        Assert.assertEquals(a, t.start());
        Assert.assertEquals(b, t.end());
        Assert.assertEquals(c, t.duration());


    }

    @Test
    public void detectTasks3(){
        Model m;
        PreProcessCPSolver s;

        m = new CPModel();
        IntegerVariable A = Choco.makeIntVar("A", 0, 10, Options.V_BOUND);
        IntegerVariable B = Choco.makeIntVar("B", 0, 10, Options.V_BOUND);
        IntegerVariable C = Choco.makeIntVar("C", 0, 10, Options.V_BOUND);

        for(int i = 0; i < 100; i++){
            TaskVariable t = Choco.makeTaskVar("t", A, B, C);
            m.addVariables(t);
        }


        s = new PreProcessCPSolver();
        s.read(m);

        Assert.assertEquals(1, s.getNbTaskVars());

    }

    @Test
    public void detectNotAndEqual(){
        Model m;
        CPSolver s;
        Random r;
        IntegerVariable A = Choco.makeIntVar("A", 0, 10, Options.V_BOUND);
        IntegerVariable B = Choco.makeIntVar("B", 0, 10, Options.V_BOUND);
        for(int seed = 0; seed < 100; seed++){
            r = new Random(seed);
            CPModelFactory mf = new CPModelFactory();
            mf.defines(A, B);
            mf.uses(MetaConstraintFactory.MC.NOT);
            mf.uses(C.EQ, C.NEQ, C.GEQ, C.LEQ,
                    C.GT, C.LT);
            mf.uses(OperatorFactory.O.NONE);

            m = mf.model(r);

            s = new CPSolver();
            s.read(m);

            DisposableIterator<SConstraint> it = s.getConstraintIterator();
            while(it.hasNext()){
                SConstraint c = it.next();
                boolean t = c instanceof CspLargeSConstraint;
                Assert.assertFalse("unexpected type of constraint: "+seed, t);
            }
            it.dispose();

        }
    }

    @Test
    public void detectNotAndSign(){
        Model m;
        CPSolver s;
        Random r;
        IntegerVariable A = Choco.makeIntVar("A", 0, 10, Options.V_BOUND);
        IntegerVariable B = Choco.makeIntVar("B", 0, 10, Options.V_BOUND);
        for(int seed = 0; seed < 20; seed++){
            r = new Random(seed);
            CPModelFactory mf = new CPModelFactory();
            mf.defines(A, B);
            mf.uses(MetaConstraintFactory.MC.NOT);
            mf.uses(C.SAMESIGN, C.SIGNOPP);
            mf.uses(OperatorFactory.O.NONE);

            m = mf.model(r);
            m.setDefaultExpressionDecomposition(true);

            s = new CPSolver();
            s.read(m);

            DisposableIterator<SConstraint> it = s.getConstraintIterator();
            while(it.hasNext()){
                SConstraint c = it.next();
                boolean t = c instanceof SignOp;
                Assert.assertTrue("unexpected type of constraint", t);
            }
            it.dispose();

        }
    }

    @Test
       public void detectClauses(){
           Model m;
           CPSolver s;
           Random r;

           for(int seed = 0; seed < 20; seed++){
               r = new Random(seed);
               m = new CPModel();
               m.setDefaultExpressionDecomposition(true);
               IntegerVariable[] vars = Choco.makeIntVarArray("v", 10, 0,1);
               Constraint[] cs = new Constraint[10];
               for(int c = 0; c < 10; c++){
                   cs[c] = eq(vars[c], r.nextInt(2));
               }
               m.addConstraint(or(cs));

               s = new CPSolver();
               s.read(m);

               DisposableIterator<SConstraint> it = s.getConstraintIterator();
               while(it.hasNext()){
                   SConstraint c = it.next();
                   boolean t = c instanceof ClauseStore;
                   Assert.assertTrue("unexpected type of constraint", t);
               }
               it.dispose();

           }
       }


}
