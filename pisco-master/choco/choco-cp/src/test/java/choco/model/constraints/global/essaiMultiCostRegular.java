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
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

import static choco.Choco.*;

public class essaiMultiCostRegular {

	public static void main(String[] args) {
		// Define input Data
		int nbNodes = 5;
		int nbEdges = 9;
		int pathLength = nbNodes - 1;
		int nbLabels = nbEdges;
		FiniteAutomaton dfa = new FiniteAutomaton();
		int a = dfa.addState();		int b = dfa.addState();		int c = dfa.addState();
		int d = dfa.addState();		int e = dfa.addState();
		dfa.setInitialState(a);
		dfa.setFinal(e);
		dfa.addTransition(a,b,0);	dfa.addTransition(a,c,1);	dfa.addTransition(c,a,2);
		dfa.addTransition(b,c,3);	dfa.addTransition(d,b,4);	dfa.addTransition(b,e,5);
		dfa.addTransition(c,d,6);	dfa.addTransition(d,e,7);	dfa.addTransition(e,e,8);
		int [][][] costs = new int[pathLength][nbLabels][2];
		for (int i=0; i<pathLength; i++){
			costs[i][0][0] = 6;		costs[i][0][1] = 4;
			costs[i][1][0] = 4;		costs[i][1][1] = 7;
			costs[i][2][0] = 5;		costs[i][2][1] = 3;
			costs[i][3][0] = 3;		costs[i][3][1] = 2;
			costs[i][4][0] = 6;		costs[i][4][1] = 4;
			costs[i][5][0] = 4;		costs[i][5][1] = 6;
			costs[i][6][0] = 10;	costs[i][6][1] = 1;
			costs[i][7][0] = 2;		costs[i][7][1] = 1;
			costs[i][8][0] = 0;		costs[i][8][1] = 0;
		}
		int [] maxCost = new int[]{30,10};

		// Declare variables
		IntegerVariable[] x = makeIntVarArray("Edge n¡",pathLength,0,nbEdges-1);	// x[i] = k if the ith edge of the path is k
		IntegerVariable[] totalCost = makeIntVarArray("Total cost",2,0,100);

		// Add constraints to the model
		Model m = new CPModel();
		m.addConstraint(multiCostRegular(totalCost,x,dfa,costs));	// x is a word recognized by dfa and totalCost = cost of x
		m.addConstraint(leq(totalCost[0], maxCost[0]));
		m.addConstraint(leq(totalCost[1], maxCost[1]));

		// Solve !
		Solver s = new CPSolver();
		s.read(m);
		s.solve();
		System.out.println(s.pretty());
		for (int i=0; i<nbNodes-1; i++)
			System.out.println(x[i].getName() + " = " + s.getVar(x[i]).getVal());
		System.out.println(totalCost[0].getName() + " = " + s.getVar(totalCost[0]).getVal());
		System.out.println(totalCost[1].getName() + " = " + s.getVar(totalCost[1]).getVal());
	}

}

