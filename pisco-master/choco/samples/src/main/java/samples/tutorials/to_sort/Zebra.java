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
package samples.tutorials.to_sort;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;

import java.util.logging.Logger;

import static choco.Choco.*;

public class Zebra {

	protected final static Logger LOGGER = ChocoLogging.getEngineLogger();
    private static CPSolver solver;

    private static void propagateDecision(IntegerVariable v,int value) throws ContradictionException {
		LOGGER.info(v.pretty()+" = "+value);
		solver.getVar(v).setVal(value);
		solver.propagate();
		LOGGER.info(solver.solutionToString());
	}

	public static void main(String args[]) {
		LOGGER.fine("Zebra Testing...");
        Model model = new CPModel();
        IntegerVariable green = makeIntVar("green", 1, 5);
        IntegerVariable blue = makeIntVar("blue", 1, 5);
        IntegerVariable yellow = makeIntVar("yellow", 1, 5);
        IntegerVariable ivory = makeIntVar("ivory", 1, 5);
        IntegerVariable red = makeIntVar("red", 1, 5);
        IntegerVariable diplomat = makeIntVar("diplomat", 1, 5);
        IntegerVariable painter = makeIntVar("painter", 1, 5);
        IntegerVariable sculptor = makeIntVar("sculptor", 1, 5);
        IntegerVariable doctor = makeIntVar("doctor", 1, 5);
        IntegerVariable violinist = makeIntVar("violinist", 1, 5);
        IntegerVariable norwegian = makeIntVar("norwegian", 1, 5);
        IntegerVariable english = makeIntVar("english", 1, 5);
        IntegerVariable japanese = makeIntVar("japanese", 1, 5);
        IntegerVariable spaniard = makeIntVar("spaniard", 1, 5);
        IntegerVariable italian = makeIntVar("italian", 1, 5);
        IntegerVariable wine = makeIntVar("wine", 1, 5);
        IntegerVariable milk = makeIntVar("milk", 1, 5);
        IntegerVariable coffee = makeIntVar("coffee", 1, 5);
        IntegerVariable water = makeIntVar("water", 1, 5);
        IntegerVariable tea = makeIntVar("tea", 1, 5);
        IntegerVariable fox = makeIntVar("fox", 1, 5);
        IntegerVariable snail = makeIntVar("snail", 1, 5);
        IntegerVariable horse = makeIntVar("horse", 1, 5);
        IntegerVariable dog = makeIntVar("dog", 1, 5);
        IntegerVariable zebra = makeIntVar("zebra", 1, 5);
        IntegerVariable[] colors = new IntegerVariable[]{green, blue, yellow, ivory, red};
        IntegerVariable[] trades = new IntegerVariable[]{diplomat, painter, sculptor, doctor, violinist};
        IntegerVariable[] nationalities = new IntegerVariable[]{norwegian, english, japanese, spaniard, italian};
        IntegerVariable[] drinks = new IntegerVariable[]{wine, milk, coffee, water, tea};
        IntegerVariable[] pets = new IntegerVariable[]{fox, snail, horse, dog, zebra};
        IntegerVariable[][] arrays = new IntegerVariable[][]{colors, trades, nationalities, drinks, pets};

		for (int a = 0; a < 5; a++) {
			for (int i = 0; i < 4; i++) {
				for (int j = i + 1; j < 5; j++) {
					model.addConstraint(neq(arrays[a][i], arrays[a][j]));
				}
			}
		}
		// help for incomplete alldiff on colors
		model.addConstraint(eq(yellow, 1));
//		//
//		// help for incomplete alldiff on colors
		model.addConstraint(eq(water, 1));

		model.addConstraint(eq(english, red));
		model.addConstraint(eq(spaniard, dog));
		model.addConstraint(eq(coffee, green));
		model.addConstraint(eq(italian, tea));
		model.addConstraint(eq(sculptor, snail));
		model.addConstraint(eq(diplomat, yellow));
		model.addConstraint(eq(green, plus(ivory, 1)));
		model.addConstraint(eq(milk, 3));
		model.addConstraint(eq(norwegian, 1));
		model.addConstraint(eq(minus(doctor, fox), 1));
		model.addConstraint(eq(violinist, wine));
		model.addConstraint(eq(japanese, painter));
		model.addConstraint(eq(minus(diplomat, horse), -1));
		model.addConstraint(eq(minus(norwegian, blue), -1));

		solver=new CPSolver();
		solver.read(model);
		try {
			propagateDecision(fox, 1);
			propagateDecision(italian, 2);
			propagateDecision(english, 3);
			propagateDecision(tea, 2);
		} catch (ContradictionException e) {
			LOGGER.info("find a contradiction !");
		}
		LOGGER.info(solver.pretty());
	}
}