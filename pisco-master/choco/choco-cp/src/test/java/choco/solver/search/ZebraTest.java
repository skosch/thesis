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

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static choco.Choco.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ZebraTest {
	private final static Logger LOGGER = ChocoLogging.getTestLogger();
  private Model m;
    private Solver s;
  private IntegerVariable green, blue, yellow, ivory, red;
  private IntegerVariable diplomat, painter, sculptor, doctor, violinist;
  private IntegerVariable norwegian, english, japanese, spaniard, italian;
  private IntegerVariable wine, milk, coffee, water, tea;
  private IntegerVariable fox, snail, horse, dog, zebra;
  private IntegerVariable[] colors, trades, nationalities, drinks, pets;
  private IntegerVariable[][] arrays;

    @Before
  public void setUp() {
    LOGGER.fine("Zebra Testing...");
    m = new CPModel();
        s = new CPSolver();
    green = makeIntVar("green", 1, 5);
    blue = makeIntVar("blue", 1, 5);
    yellow = makeIntVar("yellow", 1, 5);
    ivory = makeIntVar("ivory", 1, 5);
    red = makeIntVar("red", 1, 5);
    diplomat = makeIntVar("diplomat", 1, 5);
    painter = makeIntVar("painter", 1, 5);
    sculptor = makeIntVar("sculptor", 1, 5);
    doctor = makeIntVar("doctor", 1, 5);
    violinist = makeIntVar("violinist", 1, 5);
    norwegian = makeIntVar("norwegian", 1, 5);
    english = makeIntVar("english", 1, 5);
    japanese = makeIntVar("japanese", 1, 5);
    spaniard = makeIntVar("spaniard", 1, 5);
    italian = makeIntVar("italian", 1, 5);
    wine = makeIntVar("wine", 1, 5);
    milk = makeIntVar("milk", 1, 5);
    coffee = makeIntVar("coffee", 1, 5);
    water = makeIntVar("water", 1, 5);
    tea = makeIntVar("tea", 1, 5);
    fox = makeIntVar("fox", 1, 5);
    snail = makeIntVar("snail", 1, 5);
    horse = makeIntVar("horse", 1, 5);
    dog = makeIntVar("dog", 1, 5);
    zebra = makeIntVar("zebra", 1, 5);
    colors = new IntegerVariable[]{green, blue, yellow, ivory, red};
    trades = new IntegerVariable[]{diplomat, painter, sculptor, doctor, violinist};
    nationalities = new IntegerVariable[]{norwegian, english, japanese, spaniard, italian};
    drinks = new IntegerVariable[]{wine, milk, coffee, water, tea};
    pets = new IntegerVariable[]{fox, snail, horse, dog, zebra};
    arrays = new IntegerVariable[][]{colors, trades, nationalities, drinks, pets};
  }

    @After
  public void tearDown() {
    m = null;
    green = null;
    blue = null;
    yellow = null;
    ivory = null;
    red = null;
    diplomat = null;
    painter = null;
    sculptor = null;
    doctor = null;
    violinist = null;
    norwegian = null;
    english = null;
    japanese = null;
    spaniard = null;
    italian = null;
    wine = null;
    milk = null;
    coffee = null;
    water = null;
    tea = null;
    fox = null;
    snail = null;
    horse = null;
    dog = null;
    zebra = null;
    colors = null;
    trades = null;
    nationalities = null;
    drinks = null;
    pets = null;
    arrays = null;
  }

  private void checkSolution() {
    for (int a = 0; a < 5; a++) {
      for (int i = 0; i < 5; i++) {
        assertTrue(s.getVar(arrays[a][i]).isInstantiated());
      }
    }
    assertEquals(1, s.getVar(norwegian).getVal());
    assertEquals(1, s.getVar(diplomat).getVal());
    assertEquals(1, s.getVar(fox).getVal());
    assertEquals(1, s.getVar(water).getVal());
    assertEquals(1, s.getVar(yellow).getVal());
    assertEquals(2, s.getVar(italian).getVal());
    assertEquals(2, s.getVar(doctor).getVal());
    assertEquals(2, s.getVar(horse).getVal());
    assertEquals(2, s.getVar(tea).getVal());
    assertEquals(2, s.getVar(blue).getVal());
    assertEquals(3, s.getVar(english).getVal());
    assertEquals(3, s.getVar(sculptor).getVal());
    assertEquals(3, s.getVar(snail).getVal());
    assertEquals(3, s.getVar(milk).getVal());
    assertEquals(3, s.getVar(red).getVal());
    assertEquals(4, s.getVar(spaniard).getVal());
    assertEquals(4, s.getVar(violinist).getVal());
    assertEquals(4, s.getVar(dog).getVal());
    assertEquals(4, s.getVar(wine).getVal());
    assertEquals(4, s.getVar(ivory).getVal());
    assertEquals(5, s.getVar(japanese).getVal());
    assertEquals(5, s.getVar(painter).getVal());
    assertEquals(5, s.getVar(zebra).getVal());
    assertEquals(5, s.getVar(coffee).getVal());
    assertEquals(5, s.getVar(green).getVal());
  }

    @Test
  public void test0() {
    for (int a = 0; a < 5; a++) {
      for (int i = 0; i < 4; i++) {
        for (int j = i + 1; j < 5; j++) {
          m.addConstraint(neq(arrays[a][i], arrays[a][j]));
        }
      }
    }
    m.addConstraint(eq(english, red));
    m.addConstraint(eq(spaniard, dog));
    m.addConstraint(eq(coffee, green));
    m.addConstraint(eq(italian, tea));
    m.addConstraint(eq(sculptor, snail));
    m.addConstraint(eq(diplomat, yellow));
    m.addConstraint(eq(green, plus(ivory, 1)));
    m.addConstraint(eq(milk, 3));
    m.addConstraint(eq(norwegian, 1));
    // m.addConstraint(((doctor - fox == 1) or (doctor - fox == -1)));
    m.addConstraint(eq(minus(doctor, fox), 1));
    m.addConstraint(eq(violinist, wine));
    m.addConstraint(eq(japanese, painter));
    //m.addConstraint(((diplomat - horse == 1) or (diplomat - horse == -1)));
    m.addConstraint(eq(minus(diplomat, horse), -1));
    //m.addConstraint(((norwegian - blue == 1) or (norwegian - blue == -1)));
    m.addConstraint(eq(minus(norwegian, blue), -1));
    s.read(m);
    s.solve(true);
    assertEquals(1, s.getNbSolutions());
    checkSolution();
  }

    @Test
  public void test1() {
    for (int a = 0; a < 5; a++) {
      for (int i = 0; i < 4; i++) {
        for (int j = i + 1; j < 5; j++) {
          m.addConstraint(neq(arrays[a][i], arrays[a][j]));
        }
      }
    }
    m.addConstraint(eq(english, red));
    m.addConstraint(eq(spaniard, dog));
    m.addConstraint(eq(coffee, green));
    m.addConstraint(eq(italian, tea));
    m.addConstraint(eq(sculptor, snail));
    m.addConstraint(eq(diplomat, yellow));
    m.addConstraint(eq(green, plus(ivory, 1)));
    m.addConstraint(eq(milk, 3));
    m.addConstraint(eq(norwegian, 1));
    m.addConstraint(or(eq(minus((doctor), (fox)), (1)), eq(minus((doctor), (fox)), (-1))));
    m.addConstraint(eq(violinist, wine));
    m.addConstraint(eq(japanese, painter));
    m.addConstraint(or(eq(minus((diplomat), (horse)), (1)), eq(minus((diplomat), (horse)), (-1))));
    m.addConstraint(or(eq(minus((norwegian), (blue)), (1)), eq(minus((norwegian), (blue)), (-1))));
    s.read(m);
    s.solve(true);
    assertEquals(1, s.getNbSolutions());
    checkSolution();
  }

}