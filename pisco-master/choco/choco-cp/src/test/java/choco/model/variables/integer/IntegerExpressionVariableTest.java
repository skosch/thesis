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

package choco.model.variables.integer;

import choco.Choco;
import static choco.Choco.makeIntVar;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.*;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 23 oct. 2008
 * Time: 10:29:09
 */
public class IntegerExpressionVariableTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    private int[] computeBound(){
        Random r = new Random();
        int signI = 2*r.nextInt(2) -1;
        int signS;
        do{
            signS = 2*r.nextInt(2) -1;
        }while(signS < signI);

        int inf = signI * r.nextInt(21);
        int sup;
        do{
            sup = signS * r.nextInt(21);
        }while(inf > sup);
        return new int[]{inf, sup};

    }

    @Test
    public void abs(){
        for(int seed =0; seed < 20; seed++){
            int[] val = computeBound();
            IntegerVariable x0= makeIntVar("x", val[0], val[1]);

            IntegerExpressionVariable abs = Choco.abs(x0);
            CPSolver solver = new CPSolver();
            IntDomainVar x1 = solver.createBoundIntVar("x", val[0], val[1]);
            IntDomainVar y1 = solver.createBoundIntVar("y", -30, 30);
            SConstraint abs1 = new Absolute(y1, x1);
            solver.post(abs1);
            try {
                solver.propagate();
            } catch (ContradictionException e) {
                LOGGER.severe(e.getMessage());
            }
            Assert.assertEquals("lower bound", y1.getInf(), abs.getLowB());
            Assert.assertEquals("upper bound", y1.getSup(), abs.getUppB());
        }
    }


    @Test
    public void neg(){
        for(int seed =0; seed < 20; seed++){
            int[] val = computeBound();

            IntegerVariable x0= makeIntVar("x", val[0], val[1]);

            IntegerExpressionVariable neg0 = Choco.neg(x0);
            CPSolver solver = new CPSolver();
            IntDomainVar x1 = solver.createBoundIntVar("x", val[0], val[1]);
            IntDomainVar y1 = solver.createBoundIntVar("y", -30, 30);
            SConstraint neg1 = solver.eq(y1, solver.minus(0, x1));
            solver.post(neg1);
            try {
                solver.propagate();
            } catch (ContradictionException e) {
                LOGGER.severe(e.getMessage());
            }
            Assert.assertEquals("lower bound", y1.getInf(), neg0.getLowB());
            Assert.assertEquals("upper bound", y1.getSup(), neg0.getUppB());
        }
    }

    @Test
    public void max(){
        for(int seed =0; seed < 20; seed++){
            int[] valx = computeBound();
            int[] valy = computeBound();


            IntegerVariable x0= makeIntVar("x", valx[0], valx[1]);
            IntegerVariable y0= makeIntVar("y", valy[0], valy[1]);

            IntegerExpressionVariable max = Choco.max(x0, y0);
            CPSolver p = new CPSolver();
            IntDomainVar x1 = p.createBoundIntVar("x", valx[0], valx[1]);
            IntDomainVar y1 = p.createBoundIntVar("y", valy[0], valy[1]);
            IntDomainVar z = p.createBoundIntVar("y", -100, 100);
            SConstraint max1 = new MaxXYZ(x1, y1, z);
            p.post(max1);
            try {
                p.propagate();
            } catch (ContradictionException e) {
                LOGGER.severe(e.getMessage());
            }
            Assert.assertEquals("lower bound", z.getInf(), max.getLowB());
            Assert.assertEquals("upper bound", z.getSup(), max.getUppB());
        }
    }

    @Test
    public void min(){
        for(int seed =0; seed < 20; seed++){
            int[] valx = computeBound();
            int[] valy = computeBound();


            IntegerVariable x0= makeIntVar("x", valx[0], valx[1]);
            IntegerVariable y0= makeIntVar("y", valy[0], valy[1]);

            IntegerExpressionVariable min = Choco.min(x0, y0);
            CPSolver p = new CPSolver();
            IntDomainVar x1 = p.createBoundIntVar("x", valx[0], valx[1]);
            IntDomainVar y1 = p.createBoundIntVar("y", valy[0], valy[1]);
            IntDomainVar z = p.createBoundIntVar("y", -100, 100);
            SConstraint neg1 = new MinXYZ(x1, y1, z);
            p.post(neg1);
            try {
                p.propagate();
            } catch (ContradictionException e) {
                LOGGER.severe(e.getMessage());
            }
            Assert.assertEquals("lower bound", z.getInf(), min.getLowB());
            Assert.assertEquals("upper bound", z.getSup(), min.getUppB());
        }
    }

    @Test
    public void minus(){
        for(int seed =0; seed < 20; seed++){
            int[] valx = computeBound();
            int[] valy = computeBound();


            IntegerVariable x0= makeIntVar("x", valx[0], valx[1]);
            IntegerVariable y0= makeIntVar("y", valy[0], valy[1]);

            IntegerExpressionVariable minus0 = Choco.minus(x0, y0);
            CPSolver p = new CPSolver();
            IntDomainVar x1 = p.createBoundIntVar("x", valx[0], valx[1]);
            IntDomainVar y1 = p.createBoundIntVar("y", valy[0], valy[1]);
            IntDomainVar z = p.createBoundIntVar("y", -100, 100);
            SConstraint minus1 = p.eq(z, p.minus(x1, y1));
            p.post(minus1);
            try {
                p.propagate();
            } catch (ContradictionException e) {
                LOGGER.severe(e.getMessage());
            }
            Assert.assertEquals("lower bound", z.getInf(), minus0.getLowB());
            Assert.assertEquals("upper bound", z.getSup(), minus0.getUppB());
        }
    }

    @Test
    public void mult(){
        for(int seed =0; seed < 20; seed++){
            int[] valx = computeBound();
            int[] valy = computeBound();


            IntegerVariable x0= makeIntVar("x", valx[0], valx[1]);
            IntegerVariable y0= makeIntVar("y", valy[0], valy[1]);

            IntegerExpressionVariable mult0 = Choco.mult(x0, y0);
            CPSolver p = new CPSolver();
            IntDomainVar x1 = p.createBoundIntVar("x", valx[0], valx[1]);
            IntDomainVar y1 = p.createBoundIntVar("y", valy[0], valy[1]);
            IntDomainVar z = p.createBoundIntVar("y", -10000, 10000);
            SConstraint mult1 = new TimesXYZ(x1, y1, z);
            p.post(mult1);
            try {
                p.propagate();
            } catch (ContradictionException e) {
                LOGGER.severe(e.getMessage());
            }
            Assert.assertEquals("lower bound", z.getInf(), mult0.getLowB());
            Assert.assertEquals("upper bound", z.getSup(), mult0.getUppB());
        }
    }

    @Test
    public void plus(){
        for(int seed =0; seed < 20; seed++){
            int[] valx = computeBound();
            int[] valy = computeBound();


            IntegerVariable x0= makeIntVar("x", valx[0], valx[1]);
            IntegerVariable y0= makeIntVar("y", valy[0], valy[1]);

            IntegerExpressionVariable plus0 = Choco.plus(x0, y0);
            CPSolver p = new CPSolver();
            IntDomainVar x1 = p.createBoundIntVar("x", valx[0], valx[1]);
            IntDomainVar y1 = p.createBoundIntVar("y", valy[0], valy[1]);
            IntDomainVar z = p.createBoundIntVar("y", -100, 100);
            SConstraint plus1 = p.eq(z, p.plus(x1, y1));
            p.post(plus1);
            try {
                p.propagate();
            } catch (ContradictionException e) {
                LOGGER.severe(e.getMessage());
            }
            Assert.assertEquals("lower bound", z.getInf(), plus0.getLowB());
            Assert.assertEquals("upper bound", z.getSup(), plus0.getUppB());
        }
    }

    @Test
    @Ignore
    public void power(){
        for(int seed =0; seed < 20; seed++){
            int[] valx = computeBound();
            int[] valy = computeBound();


            IntegerVariable x0= makeIntVar("x", valx[0], valx[1]);
            IntegerVariable y0= makeIntVar("y", valy[0], valy[1]);

            IntegerExpressionVariable power = Choco.power(x0, y0);
            CPSolver p = new CPSolver();
            IntDomainVar x1 = p.createBoundIntVar("x", valx[0], valx[1]);
            IntDomainVar y1 = p.createBoundIntVar("y", valy[0], valy[1]);
            IntDomainVar z = p.createBoundIntVar("y", -100, 100);

            /////////////////CHECK///////////////////////
            SConstraint plus1 = p.eq(z, p.plus(x1, y1));
            /////////////////CHECK///////////////////////

            p.post(plus1);
            try {
                p.propagate();
            } catch (ContradictionException e) {
                LOGGER.severe(e.getMessage());
            }
            Assert.assertEquals("lower bound", z.getInf(), power.getLowB());
            Assert.assertEquals("upper bound", z.getSup(), power.getUppB());
        }
    }

    @Test
    @Ignore
    public void modulo(){
        for(int seed =0; seed < 20; seed++){
            int[] valx = computeBound();
            int[] valy = computeBound();


            IntegerVariable x0= makeIntVar("x", valx[0], valx[1]);
            IntegerVariable y0= makeIntVar("y", valy[0], valy[1]);

            IntegerExpressionVariable mod0 = Choco.mod(x0, y0);
            CPSolver p = new CPSolver();
            IntDomainVar x1 = p.createBoundIntVar("x", valx[0], valx[1]);
            IntDomainVar y1 = p.createBoundIntVar("y", valy[0], valy[1]);
            IntDomainVar z = p.createBoundIntVar("y", -100, 100);
            SConstraint mod1 = new ModuloXYC2(x1, y1, 2);
            p.post(mod1);
            try {
                p.propagate();
            } catch (ContradictionException e) {
                LOGGER.severe(e.getMessage());
            }
            Assert.assertEquals("lower bound", z.getInf(), mod0.getLowB());
            Assert.assertEquals("upper bound", z.getSup(), mod0.getUppB());
        }
    }

    @Test
    public void testBigDomain(){

        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable x = Choco.makeIntVar("x", -10, 10);
        IntegerVariable y = Choco.makeIntVar("y", -10, 10);
        IntegerVariable a = Choco.makeIntVar("a", -10, 10);
        IntegerVariable z = Choco.makeIntVar("z", -100, 100);
        m.addConstraint(Choco.eq(z, Choco.plus(x, Choco.max(y, a))));
//m.setDefaultExpressionDecomposition(true);
        s.read(m);
        LOGGER.info(s.pretty());

        s.solve();
        Assert.assertEquals(s.isFeasible(),true);
        s.printRuntimeStatistics();
    }

    @Test
    public void scalar(){
        for(int seed =0; seed < 20; seed++){
            int[] valx = computeBound();
            int[] valy = computeBound();
            int[] valz = computeBound();
            int[] c1 = computeBound();
            int[] c2 = computeBound();


            IntegerVariable x0= makeIntVar("x", valx[0], valx[1]);
            IntegerVariable y0= makeIntVar("y", valy[0], valy[1]);
            IntegerVariable z0= makeIntVar("z", valz[0], valz[1]);
            IntegerVariable[] v0 = new IntegerVariable[]{x0, y0, z0};
            int[] c = new int[]{c1[0], c2[1], c1[1]};

            IntegerExpressionVariable plus0 = Choco.scalar(v0, c);

            CPSolver p = new CPSolver();
            IntDomainVar x1 = p.createBoundIntVar("x", valx[0], valx[1]);
            IntDomainVar y1 = p.createBoundIntVar("y", valy[0], valy[1]);
            IntDomainVar z1 = p.createBoundIntVar("z", valz[0], valz[1]);
            IntDomainVar[] v1 = new IntDomainVar[]{x1, y1, z1};
            IntDomainVar w = p.createBoundIntVar("y", -10000, 10000);
            SConstraint plus1 = p.eq(w, p.scalar(v1, c));
            p.post(plus1);
            try {
                p.propagate();
            } catch (ContradictionException e) {
                LOGGER.severe(e.getMessage());
            }
            Assert.assertEquals("lower bound", w.getInf(), plus0.getLowB());
            Assert.assertEquals("upper bound", w.getSup(), plus0.getUppB());
        }
    }

    @Test
    public void sum(){
        for(int seed =0; seed < 20; seed++){
            int[] valx = computeBound();
            int[] valy = computeBound();
            int[] valz = computeBound();


            IntegerVariable x0= makeIntVar("x", valx[0], valx[1]);
            IntegerVariable y0= makeIntVar("y", valy[0], valy[1]);
            IntegerVariable z0= makeIntVar("z", valz[0], valz[1]);

            IntegerExpressionVariable plus0 = Choco.sum(x0, y0, z0);

            CPSolver p = new CPSolver();
            IntDomainVar x1 = p.createBoundIntVar("x", valx[0], valx[1]);
            IntDomainVar y1 = p.createBoundIntVar("y", valy[0], valy[1]);
            IntDomainVar z1 = p.createBoundIntVar("z", valz[0], valz[1]);
            IntDomainVar w = p.createBoundIntVar("y", -100, 100);
            SConstraint plus1 = p.eq(w, p.sum(x1, y1, z1));
            p.post(plus1);
            try {
                p.propagate();
            } catch (ContradictionException e) {
                LOGGER.severe(e.getMessage());
            }
            Assert.assertEquals("lower bound", w.getInf(), plus0.getLowB());
            Assert.assertEquals("upper bound", w.getSup(), plus0.getUppB());
        }
    }

}
