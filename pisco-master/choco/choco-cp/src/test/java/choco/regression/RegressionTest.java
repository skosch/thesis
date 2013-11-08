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

package choco.regression;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.logging.Logger;

import static choco.Choco.*;
import static org.junit.Assert.*;

/**
 * @author grochart
 */
public class RegressionTest {
    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

  // 03/01/2006: number of constraints was not correctly maintained !
  // bug reported by R�mi Coletta
    @Test
  public void testNbConstraints() {
    LOGGER.info("Regression currentElement: 03/01/2006 - Remi Coletta");

    CPModel m = new CPModel();
    Solver s = new CPSolver();
    IntegerVariable[] vart = new IntegerVariable[2];
    vart[0] = makeIntVar("x1", 0, 3);
    vart[1] = makeIntVar("x2", 0, 3);
    m.addVariables(vart);
    s.read(m);

    IntDomainVar[] vars = s.getVar(vart) ;
    // On place la contrainte (x1 == x2)
    assertEquals(0, s.getNbIntConstraints());
    s.worldPush();
    SConstraint c1 = s.eq(vars[0], vars[1]);
    s.post(c1);
    assertEquals(1, s.getNbIntConstraints());
    s.solve(true);
    int nbSol1 = s.getNbSolutions();
    assertEquals(4, nbSol1);
    // On supprime maintenant la contrainte...
    s.worldPopUntil(0);
    assertEquals(0, s.getNbIntConstraints());

    // On place la seconde contrainte : (x1 != x2)
    s.worldPush();
    SConstraint c2 = s.neq(vars[0], vars[1]);
    s.post(c2);
    assertEquals(1, s.getNbIntConstraints());
    s.solve(true);
    int nbSol2 = s.getNbSolutions();
    assertEquals(12, nbSol2);
  }

    @Test
  public void testIsFeasible() {
    LOGGER.info("Regression currentElement: 27/01/2006");

    Model m = new CPModel();
    Solver s = new CPSolver();
    IntegerVariable[] vars = new IntegerVariable[4];
    vars[0] = makeIntVar("x1", 0, 2);
    vars[1] = makeIntVar("x2", 0, 2);
    vars[2] = makeIntVar("x3", 0, 2);
    vars[3] = makeIntVar("x4", 0, 2);
    for (int i = 0; i < 4; i++) {
      for (int j = i + 1; j < 4; j++)
        m.addConstraint(neq(vars[i], vars[j]));
    }
        s.read(m);
    s.solve();
    // On place la contrainte (x1 == x2)
    assertEquals(true, s.isFeasible() != null);
    assertEquals(false, s.isFeasible());

  }


    @Test
    public void run(){
        for(int i = 0; i < 10; i++){
            LOGGER.info("i:"+i);
            this.constraintsIterator();
        }
    }

  public void constraintsIterator() {
        LOGGER.info("Regression currentElement: 27/01/2006 - iterator");
    Model m = new CPModel();
    Solver s = new CPSolver();
    IntegerVariable x = makeIntVar("X", 1, 5);
    IntegerVariable y = makeIntVar("Y", 1, 5);
        m.addVariables(Options.V_BOUND, x, y);
    Constraint c1 = eq(x, 1);
    Constraint c2 = eq(x, y);
    m.addConstraints(c1,c2);
        s.read(m);
    assertEquals(s.getVar(x).getNbConstraints(), 2);
    assertEquals(s.getVar(y).getNbConstraints(), 1);

    Iterator constraints = s.getVar(x).getConstraintsIterator();
      assertTrue(constraints.hasNext());
      assertEquals(constraints.next(), s.getCstr(c1));
      assertTrue(constraints.hasNext());
      assertEquals(constraints.next(), s.getCstr(c2));
      assertFalse(constraints.hasNext());

    constraints = s.getVar(y).getConstraintsIterator();
    assertTrue(constraints.hasNext());
    assertEquals(constraints.next(), s.getCstr(c2));
    assertFalse(constraints.hasNext());
  }

    /*public void testMult() {
      LOGGER.info("Regression currentElement: 27/01/2006");

      Model pb = new Model();
      IntegerVariable v = pb.makeIntVar("x1", 0, 2);
      Constraint c = pb.eq(pb.mult(3,v),1);
      pb.post(c);
      pb.solve();
      this.assertEquals(true, pb.isFeasible() != null);
      this.assertEquals(false, pb.isFeasible().booleanValue());
    } */


    @Test
    public void testCleanState() {
        String[] type = new String[]{Options.V_ENUM, Options.V_BOUND, Options.V_BTREE, Options.V_LINK};

        for (String aType : type) {

            Model m = new CPModel();
            IntegerVariable v = makeIntVar("v", 1, 2, aType);
            m.addConstraint(geq(v, 1));
            Solver s = new CPSolver();
            s.read(m);
            try {
                s.propagate();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            Assert.assertTrue(s.getPropagationEngine().checkCleanState());
        }
    }

}
