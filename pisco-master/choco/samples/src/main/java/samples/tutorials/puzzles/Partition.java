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
package samples.tutorials.puzzles;

import static choco.Choco.allDifferent;
import static choco.Choco.constant;
import static choco.Choco.eq;
import static choco.Choco.increasingSum;
import static choco.Choco.lt;
import static choco.Choco.makeIntVarArray;
import static choco.Choco.times;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;

import org.kohsuke.args4j.Option;

import samples.tutorials.PatternExample;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.MaxVal;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateInt;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.integer.IntVarValPair;
import choco.kernel.solver.search.integer.VarValPairSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * Modified A. Malapert (Oct. 2012)
 * @since 13/04/11
 */
public class Partition extends PatternExample {
	static final NumberFormat formatter = new DecimalFormat("#0.00");

	@Option(name = "-n", usage = "Numbers to partition", required = false)
	int size = 24;

	IntegerVariable[] x,y;

	@Override
	public void printDescription() {
		LOGGER.info("This problem consists in finding a partition of numbers 1..2*N into two sets A and B such that:");
		LOGGER.info("a) A and B have the same cardinality");
		LOGGER.info("b) sum of numbers in A = sum of numbers in B");
		LOGGER.info("c) sum of squares of numbers in A = sum of squares of numbers in B");
		LOGGER.info("See problem 49 at http://www.csplib.org/");
		LOGGER.info(MessageFormat.format("Here n = {0}\n\n", size));
	}


	@Override
	public void buildModel() {
		model = new CPModel();
		final int ub =2*size;
		////////////////////////
		// Define Numbers 
		x = makeIntVarArray("x", size,1, ub);
		y = makeIntVarArray("y", size,1, ub);

		/////////////////////////////////
		// symmetry breaking constraints
		model.addConstraint(eq(x[0],1));
		for (int i = 0; i < size - 1; i++) {
			model.addConstraint(lt(x[i], x[i + 1]));
			model.addConstraint(lt(y[i], y[i + 1]));
		}

		//////////////////////////
		// Define squared numbers 
		final int sub = ub*ub;
		IntegerVariable[] sx = makeIntVarArray("sx", size,1, sub);
		IntegerVariable[] sy = makeIntVarArray("sy", size,1, sub);
		for (int i = 0; i < size; i++) {
			model.addConstraints(
					times(x[i], x[i], sx[i]),
					times(y[i], y[i], sy[i])
					);
		}

		///////////////////
		//Sum of numbers 
		final IntegerVariable sumb = constant( (ub * (ub+1)) / 4);
		model.addConstraints(
				increasingSum(x, sumb),
				increasingSum(y, sumb)
				);
		//////////////////////////
		// Sum of squared numbers 
		//http://www.les-suites.fr/somme-des-n-premiers-carres.htm
		final IntegerVariable sumc = constant((ub * (ub+1) * (2 * ub +1))/12);
		model.addConstraints(
				increasingSum(sx, sumc),
				increasingSum(sy, sumc)
				);

		/////////////////////////
		// Redondant constraints
		//model.addConstraint(eq(sum(x), sum(y)));
		//model.addConstraint(eq(sum(sx), sum(sy)));
		// Partitioning 
		final IntegerVariable[] xy = ArrayUtils.append(x,y);
		model.addConstraint(allDifferent(xy));
	}

	@Override
	public void buildSolver() {
		solver = new CPSolver();
		solver.read(model);
		solver.clearGoals();
		solver.setVarIntSelector(new MinDomain(solver));
		solver.setValIntSelector(new MaxVal());
		//It would be far better to use a dedicated search strategy
		// Try to assign the highest remaining value to the last free variable of x then y
		solver.generateSearchStrategy();
	}

	@Override
	public void solve() {
		//ChocoLogging.toSearch();
		//solver.launch();
		solver.solve();
	}

	@Override
	public void prettyOut() {
		StringBuilder st = new StringBuilder("A: ");
		for(int i = 0 ; i < size; i++){
			st.append(solver.getVar(x[i]).getVal()).append(" ");
		}
		st.append("\nB: ");
		for(int i = 0 ; i < size; i++){
			st.append(solver.getVar(y[i]).getVal()).append(" ");
		}
		LOGGER.info(st.toString());
	}

	public static void main(String[] args) {
		new Partition().execute(args);
	}
}

