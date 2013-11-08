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

package choco.scheduling;

import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.Arrays;
import java.util.logging.Logger;


/**
 * @author Arnaud Malapert</br>
 * @since 2 mars 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public class TestAlternativeResources {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

	protected void testAltDisjunctive(int nbTests, int[] durations, int makespan, int type) {
		int nbSols = solveDisjSubProblems(durations, makespan, type);
		AltDisjProblem pb = new AltDisjProblem(durations, type);
		pb.setHorizon(makespan);
		TestDisjunctive.launchAllRules(pb,nbTests, nbSols);
	}

	protected void testAltCumulative(int nbTests, int[] durations, int[] heights, int capa, int makespan, int type) {
		int nbSols = solveCumulSubProblems(durations, heights, capa, makespan, type);
		AltCumulProblem pb = new AltCumulProblem(durations, heights,type);
		pb.setCapacity(capa);
		pb.setHorizon(makespan);
		TestCumulative.launchOnlysweep(pb, nbTests, nbSols, SchedUtilities.NO_CHECK_NODES);
	}

	protected int[] getNbSolutionFactors(int[] durations, int makespan, int type) {
		final int n = durations.length;
		switch (type) {
		case 1: {
			int[] factors = new int[n];
			for (int i = 0; i < n; i++) {
				factors[i] = makespan - durations[i] + 1;
			}
			return factors;
		}
		case 2: {
			int[] factors = new int[(n * (n - 1)) / 2];
			int cpt = 0;
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
					factors[cpt++] = (makespan - durations[i] + 1)
					* (makespan - durations[j] + 1);
				}
			}
			return factors;
		}
		default:
			return null;
		}

	}

	private static int[][] createHeightSubsets(int[] tasks, int type) {
		final int n = tasks.length;
		switch (type) {
		case 1: {
			int[][] subsets = new int[n][n - type];
			for (int i = 0; i < n; i++) {
                System.arraycopy(tasks, 0, subsets[i], 0, i);
                System.arraycopy(tasks, i + 1, subsets[i], i + 1 - 1, n - (i + 1));
			}
			return subsets;
		}
		case 2: {
			int[][] subsets = new int[(n * (n - 1)) / 2][n
			                                             - type];
			int line = 0;
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
					int cpt = 0;
					for (int k = 0; k < n; k++) {
						if (k != i && k != j) {
							subsets[line][cpt++] = tasks[k];
						}
					}
					++line;
				}
			}
			return subsets;
		}
		default:
			return null;
		}
	}

	private static TaskVariable[][] createTaskSubsets(TaskVariable[] tasks, int type) {
		final int n = tasks.length;
		switch (type) {
		case 1: {
			TaskVariable[][] subsets = new TaskVariable[n][n - type];
			for (int i = 0; i < n; i++) {
                System.arraycopy(tasks, 0, subsets[i], 0, i);
                System.arraycopy(tasks, i + 1, subsets[i], i + 1 - 1, n - (i + 1));
			}
			return subsets;
		}
		case 2: {
			TaskVariable[][] subsets = new TaskVariable[(n * (n - 1)) / 2][n
			                                                               - type];
			int line = 0;
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
					int cpt = 0;
					for (int k = 0; k < n; k++) {
						if (k != i && k != j) {
							subsets[line][cpt++] = tasks[k];
						}
					}
					++line;
				}
			}
			return subsets;
		}
		default:
			return null;
		}
	}

	protected int solveDisjSubProblems(int[] durations, int makespan, int type) {
		final TaskVariable[] tasks = makeTaskVarArray("T", 0, makespan,
				durations);
		TaskVariable[][] subsets = createTaskSubsets(tasks, type);
		int[] factors = getNbSolutionFactors(durations, makespan, type);
		int nbSols = 0;
		for (int i = 0; i < factors.length; i++) {
			Model m = new CPModel();
			m.addConstraint(disjunctive(subsets[i]));
			CPSolver solver = new CPSolver();
			solver.setHorizon(makespan);
			solver.read(m);
			solver.solveAll();
			int subNbSols = solver.getNbSolutions() * factors[i];
			LOGGER.info("Nb sols reduced subproblem " + i + ": "
					+ factors[i] + " x " + solver.getNbSolutions() + " = "
					+ subNbSols);
			// The eliminated task could be anywhere in the schedule
			nbSols += subNbSols;
		}
		return nbSols;
	}

	protected int solveCumulSubProblems(int[] durations, int[] heights, int capa, int makespan, int type) {
		final TaskVariable[] tasks = makeTaskVarArray("T", 0, makespan,
				durations);
		TaskVariable[][] subsets = createTaskSubsets(tasks, type);
		int[][] hsubsets = createHeightSubsets(heights, type);
		int[] factors = getNbSolutionFactors(durations, makespan, type);
		int nbSols = 0;
		for (int i = 0; i < factors.length; i++) {
			Model m = new CPModel();
			m.addConstraint(cumulativeMax(subsets[i], hsubsets[i], capa));
			CPSolver solver = new CPSolver();
			solver.setHorizon(makespan);
			solver.read(m);
			solver.solveAll();
			int subNbSols = solver.getNbSolutions() * factors[i];
			LOGGER.info("Nb sols reduced subproblem " + i + ": "
					+ factors[i] + " x " + solver.getNbSolutions() + " = "
					+ subNbSols);
			// The eliminated task could be anywhere in the schedule
			nbSols += subNbSols;
		}
		return nbSols;
	}

	@Test
	public void testAltDisj1() {
		//ChocoLogging.setVerbosity(Verbosity.VERBOSE);
		int n = 4;
		int[] durations = ArrayUtils.oneToN(n);
		testAltDisjunctive(TestDisjunctive.NB_TESTS, durations, (n * (n - 1)) / 2, 2);
	}

	@Test
	public void testAltDisj2() {
		int n = 5;	
		int[] durations = ArrayUtils.oneToN(n);
		testAltDisjunctive(1,durations, (n * (n - 1)) / 2, 1);
	}
	
	@Test
	public void testAltDisj3() {
		int n = 5;
		int[] durations = ArrayUtils.oneToN(n);
		testAltDisjunctive(1, durations, ((n - 1) * (n - 2)) / 2 + n / 2, 2);
	}

	@Test
	public void testEmptyResource() {
		int n = 4;
		int[] durations = ArrayUtils.oneToN(n);
		AltDisjProblem pb = new AltDisjProblem(durations, 4);
		pb.setHorizon(4);
		TestDisjunctive.launchAllRules(pb, 2 * 3 * 4);
	}

	@Test
	public void testAltCumul1() {
		int n = 3;
		int[] durations = ArrayUtils.oneToN(n);
		int[] heights = new int[n];
		Arrays.fill(heights, n/2);
		testAltCumulative(TestCumulative.NB_TEST, durations, heights, n, n*(n+1)/4+3, 1);
		testAltCumulative(TestCumulative.NB_TEST, durations, heights, n, n*(n+1)/4+3, 2);
	}

	@Test
	public void testAltCumul2() {
		int n = 4;
		int[] durations = ArrayUtils.oneToN(n);
		int[] heights = new int[n];
		Arrays.fill(heights, n/2);
		testAltCumulative(2, durations, heights, n, n*(n+1)/4-1, 1);
		testAltCumulative(2, durations, heights, n, n*(n+1)/4+3, 2);	
	}
	
	@Test
	public void testAltCumul3() {
		int n = 5;
		int[] durations = ArrayUtils.oneToN(n);
		int[] heights = new int[n];
		Arrays.fill(heights, n/2);
		testAltCumulative(1, durations, heights, n, n*(n+1)/4-2, 1);
		testAltCumulative(1, durations, heights, n, n*(n+1)/4+2, 1);
		testAltCumulative(1, durations, heights, n, n*(n+1)/4, 2);
	}
	
	private int horizon = 22;
	private TaskVariable JobA = makeTaskVar("JobA", 0, horizon, 4);
	private TaskVariable JobB = makeTaskVar("JobB", 0, horizon, 6);
	private TaskVariable JobC = makeTaskVar("JobC", 0, horizon, 8);
	
	private IntegerVariable JobA_Res1 = makeBooleanVar("JobA_Res1");
	private IntegerVariable JobA_Res2 = makeBooleanVar("JobA_Res2");
	private IntegerVariable JobB_Res1 = makeBooleanVar("JobB_Res1");
	private IntegerVariable JobB_Res2 = makeBooleanVar("JobB_Res2");
	private IntegerVariable JobC_Res1 = makeBooleanVar("JobC_Res1");
	private IntegerVariable JobC_Res2 = makeBooleanVar("JobC_Res2");
	
	protected final CPModel createModelJobARes1Res2() {
		CPModel model = new CPModel();
		model.addConstraint(eq(plus(JobA_Res1, JobA_Res2), 1));
		model.addConstraint(eq(plus(JobB_Res1, JobB_Res2), 1));
		model.addConstraint(eq(plus(JobC_Res1, JobC_Res2), 1));

		model.addConstraint(disjunctive( new TaskVariable[]{JobA, JobB, JobC}, 
				new IntegerVariable[] {JobA_Res1, JobB_Res1, JobC_Res1})) ; 
		model.addConstraint(disjunctive( new TaskVariable[]{JobA, JobB, JobC}, 
				new IntegerVariable[] {JobA_Res2, JobB_Res2, JobC_Res2})) ;
		return model;
	}
	
	protected final CPModel createModelJobARes1() {
		CPModel model = new CPModel();
		model.addConstraint(eq(plus(JobB_Res1, JobB_Res2), 1));
		model.addConstraint(eq(plus(JobC_Res1, JobC_Res2), 1));

		model.addConstraint(disjunctive( new TaskVariable[]{JobA, JobB, JobC}, 
				new IntegerVariable[] {JobB_Res1, JobC_Res1})) ; 
		model.addConstraint(disjunctive( new TaskVariable[]{JobB, JobC}, 
				new IntegerVariable[] {JobB_Res2, JobC_Res2})) ;
		return model;
	}
	
	protected int minimizeMakespan(CPModel model) {
		CPSolver solver = new CPSolver();
		solver.setHorizon(horizon);
		solver.read(model);
		solver.setRandomSelectors();
		solver.setObjective(solver.getMakespan());
		solver.minimize(false);
		assertTrue("did not prove optimum ", solver.isObjectiveOptimal());
		return solver.getObjectiveValue().intValue();
	}
	
	protected int solveAll(CPModel model) {		CPSolver solver = new CPSolver();
		solver.setHorizon(horizon);
		solver.read(model);
		solver.setRandomSelectors();
		solver.solveAll();
		assertTrue("is infeasible", solver.isFeasible());
		return solver.getSolutionCount();
	}

	@Test
	public void cosmicTest() {
		//ChocoLogging.setVerbosity(Verbosity.DEFAULT);
		CPModel model = createModelJobARes1();
		int obj = minimizeMakespan(model);
		int nbsols = solveAll(model);
		model = createModelJobARes1Res2();
		assertEquals("SymBreak vs Simple", obj, minimizeMakespan(model));
		assertEquals("SymBreak vs Simple nbsols", 2 * nbsols, solveAll(model));
	}
}
