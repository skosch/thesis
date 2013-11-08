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

import choco.Choco;
import choco.DeprecatedChoco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.bitmask.BitMask;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import static choco.Choco.*;
import static choco.cp.solver.constraints.global.scheduling.cumulative.AbstractCumulativeSConstraint.*;
import static org.junit.Assert.*;


/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 27 mars 2006
 * Time: 10:03:39
 * 28/02/08 modification for choco scheduling extension
 */
public class TestCumulative {

	protected final static Logger LOGGER = ChocoLogging.getTestLogger();

	public final static int NB_TEST = 15;

	public final static BitMask SETTINGS = new BitMask();

    private static final Random RANDOM =  new Random();

	private static void compareTI(CumulProblem cp, final int nbSol, final int nbNodes) {
		SETTINGS.clear();
		final int seed = RANDOM.nextInt();
		SETTINGS.set(TASK_INTERVAL_SLOW);
		cp.generateSolver();
		cp.solver.setRandomSelectors(seed);
		CPSolver s1 = cp.solver;
		SETTINGS.clear();
		SETTINGS.set(TASK_INTERVAL);
		cp.generateSolver();
		cp.solver.setRandomSelectors(seed);
		LOGGER.info("compare Task intervals algorithms");
		SchedUtilities.compare(nbSol, nbNodes, "TI", s1, cp.solver);
	}

	public void launchAllRules(CumulProblem cp, final int nbSol) {
		launchAllRules(cp, NB_TEST, nbSol, -1, true);
	}

	public static void launchAllRules(CumulProblem cp, final int nbSol, final int nbNodes) {
		launchAllRules(cp, NB_TEST, nbSol, nbNodes, true);
	}
	
	private static  void solve(CumulProblem cp, final int nbSol, int nbNodes) {
		cp.generateSolver();
		//LOGGER.info(cp.solver.pretty());
		SchedUtilities.solveRandom(cp.solver, nbSol, nbNodes, cp.rsc.getOptions()+": "+SETTINGS);
	}



	public static void launchAllRules(CumulProblem cp, final int nbTests, final int nbSol, int nbNodes, boolean useTI) {
		cp.initializeModel();
		for (int i = 0; i < nbTests; i++) {
			SETTINGS.clear();
			solve(cp, nbSol, nbNodes);
			if( useTI) {
				SETTINGS.set(VHM_CEF_ALGO_N2K);
				solve(cp, nbSol, nbNodes);
				compareTI(cp, nbSol, nbNodes >= 0 ? nbNodes : SchedUtilities.NO_CHECK_NODES);
			}
			SETTINGS.clear();
			cp.generateSolver(AbstractTestProblem.getConfig(false));
			SchedUtilities.solveRandom(cp.solver, nbSol, nbNodes, cp.rsc.getOptions().toString());
			
			cp.generateSolver(AbstractTestProblem.getConfig(true));
			SchedUtilities.solveRandom(cp.solver, nbSol, nbNodes, cp.rsc.getOptions().toString());
		}
	}

	protected void launch(int nbSols, int nbNodes, int horizon, int capa, CumulProblem... pbs) {
		for (CumulProblem pb : pbs) {
			pb.setCapacity(capa);
			pb.setHorizon(horizon);
			launchAllRules(pb, nbSols, nbNodes);
		}
	}

	protected void launch(int nbSols, int horizon, int capa, CumulProblem... pbs) {
		this.launch(nbSols, -1, horizon, capa, pbs);
	}

	@Test
	public void testUnsat1() {
		int[] pmin = {7, 3, 1, 8, 2};
		int[] pmax = {7, 3, 1, 9, 2};
		IntegerVariable[] p = SchedUtilities.makeIntvarArray("duration", pmin, pmax);
		IntegerVariable[] h = constantArray(new int[]{2, 2, 3, 1, 2});
		launch(0, 9, 3, new CumulProblem(p, h));
	}

	@Test
	public void testUnsat2() {
		int[] pmin = {4, 3, 2, 8, 2};
		int[] pmax = {10, 3, 2, 12, 2};
		IntegerVariable[] p = SchedUtilities.makeIntvarArray("duration", pmin, pmax);
		IntegerVariable[] h = constantArray(new int[]{2, 2, 4, 1, 4});
		launch(0, 11, 4, new CumulProblem(p, h));
	}


	/**
	 * Trivial exemple with 2 solutions
	 */
	@Test
	public void test1() {
		launch(2, 3, 3, new CumulProblem(new int[]{2, 2, 1}, new int[]{2, 1, 3}));
	}
	
	

	/**
	 * Trivial exemple with 2 solutions
	 */
	@Test
	public void test1Bis() {
		IntegerVariable[] d = constantArray(new int[]{2, 2, 1});
		IntegerVariable[] h = constantArray(new int[]{2, 1, 3});
		IntegerVariable[] s = SchedUtilities.makeIntvarArray("start", new int[]{0, 0, 0}, new int[]{0, 3, 3});
		launch(1, 3, 3, new CumulProblem(s, d, h));
	}

	/**
	 * Trivial exemple with 6 solutions and one nil height
	 */
	@Test
	public void test1Ter() {
		launch(6, 3, 3, new CumulProblem(new int[]{2, 2, 1, 1}, new int[]{2, 1, 3, 0}));
	}
	
	/**
	 * Test nil heights
	 */
	@Test
	public void testNilHeights() {
		launch(36, 3, 3, new CumulProblem(new int[]{2, 2, 1, 1}, new int[]{0, 0, 0, 0}));
	}
	/**
	 * Trivial exemple with 2 solutions
	 *
	 * @throws ContradictionException
	 */
	@Test
	public void testEnd1() {
		IntegerVariable[] d = constantArray(new int[]{2, 2, 1});
		IntegerVariable[] h = constantArray(new int[]{2, 1, 3});
		int hor = 3;
		int[] min = {1, 0, 0};
		int[] max = {1, hor, hor};
		IntegerVariable[] s = SchedUtilities.makeIntvarArray("start", min, max);
		launch(1, 1, 3, 3, new CumulProblem(s, d, h));
	}

	/**
	 * nb solutions 208
	 */
	@Test
	public void test2() {
		int[] pmin = {4, 3, 1, 8, 2};
		int[] pmax = {10, 3, 1, 12, 2};
		IntegerVariable[] p = SchedUtilities.makeIntvarArray("duration", pmin, pmax);
		IntegerVariable[] h = constantArray(new int[]{2, 2, 3, 1, 4});
		launch(208, 11, 4, new CumulProblem(p, h));
	}


	//
	/**
	 * Trivial exemple for edge finding
	 * nb solutions : 2
	 */
	@Test
	public void test3() {
		IntegerVariable[] d = constantArray(new int[]{5, 3, 2});
		IntegerVariable[] h = constantArray(new int[]{2, 2, 3});
		IntegerVariable[] s = Choco.makeIntVarArray("start", d.length, 0, 3);
		launch(2, 7, 4, new CumulProblem(s, d, h));
	}


	/**
	 * Trivial example where edgefinding shouldn't fail !
	 * example send to Luc to highlhight the problem in algorithm CalcR !
	 */
	@Test
	public void test4() {
		IntegerVariable[] d = constantArray(new int[]{3, 2});
		IntegerVariable[] h = constantArray(new int[]{2, 3});
		int[] st = {3, 0};
		IntegerVariable[] s = SchedUtilities.makeIntvarArray("start", st, st);
		launch(1, 1, 12, 4, new CumulProblem(s, d, h));
	}


	/**
	 * nb solutions = 510
	 */
	@Test
	public void test5() {
		int[] t1 = new int[5], t2 = new int[5];
		Arrays.fill(t1, 2);
		Arrays.fill(t2, 4);
		IntegerVariable[] d = constantArray(t1);
		IntegerVariable[] h = SchedUtilities.makeIntvarArray("h", t1, t2);
		launch(510, 6, 4, new CumulProblem(d, h));
	}


	/**
	 * nb solutions = 48
	 */
	@Test
	public void test6() {
		int[] t1 = new int[3], t2 = new int[3], t3 = new int[3];
		Arrays.fill(t1, 2);
		Arrays.fill(t2, 3);
		Arrays.fill(t3, 4);
		IntegerVariable[] d = constantArray(t1);
		IntegerVariable[] h = SchedUtilities.makeIntvarArray("h", t2, t3);
		launch(48, 6, 4, new CumulProblem(d, h));
	}


	@Test
	public void testEnd6() {
		int[] t1 = new int[3], t2 = new int[3], t3 = new int[3];
		Arrays.fill(t1, 2);
		Arrays.fill(t2, 3);
		Arrays.fill(t3, 4);
		t2[2] = 4;
		int[] min = {0, 4, 2};
		IntegerVariable[] d = constantArray(t1);
		IntegerVariable[] h = SchedUtilities.makeIntvarArray("h", t2, t3);
		IntegerVariable[] s = SchedUtilities.makeIntvarArray("s", min, min);
		launch(4, 6, 4, new CumulProblem(s, d, h));
	}


	@Test
	public void testExtraction1() {
		int[] p = {1, 2, 3, 4};
		int[] ih = {6, 6, 6, 6};
		launch(24, 10, 10, new CumulProblem(p, ih));
	}

	@Test
	public void testExtraction2() {
		int[] p = {2, 2, 3, 3, 1};
		int[] ih = {8, 8, 4, 3, 3};
		launch(18, 7, 10, new CumulProblem(p, ih));
	}

	/**
	 * 0 nodes with taskIntervals, 836 otherwise
	 *
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */
	@Test(expected = ContradictionException.class)
	public void test7TaskInterval() throws ContradictionException {
		int n = 10;
		int[] p = new int[n];
		int[] d = new int[n];
		int[] r = new int[n];
		int[] h = new int[n];
		Arrays.fill(p, 2);
		Arrays.fill(d, 4);
		Arrays.fill(h, 1);
		CumulProblem cp = new CumulProblem(p, h);
		cp.starts = SchedUtilities.makeIntvarArray("start", r, d);
		cp.setCapacity(2);
		cp.setHorizon(6);
		cp.initializeModel();

		SETTINGS.set(VHM_CEF_ALGO_N2K);
		cp.generateSolver();
		cp.solver.propagate();

	}


	/**
	 * Example found page 59 of the book : Constraint Based Scheduling
	 *
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */
	@Test
	public void testPropagEdgeFindingStarting() throws ContradictionException {
		int[] p = new int[]{11, 6, 5, 5};
		int[] d = new int[]{8, 4, 5, 5};
		int[] r = new int[]{0, 0, 0, 0};
		int[] h = new int[]{1, 1, 1, 1};
		CumulProblem cp = new CumulProblem(p, h);
		cp.starts = SchedUtilities.makeIntvarArray("start", r, d);
		cp.setCapacity(2);
		cp.setHorizon(30);
		cp.initializeModel();
		SETTINGS.set(VHM_CEF_ALGO_N2K);
		cp.generateSolver();
		cp.solver.propagate();
		IntDomainVar v = cp.solver.getVar(cp.starts[0]);
		assertEquals(6, v.getInf());
	}

	/**
	 * Ending date version of Example found page 59 of the book : Constraint Based Scheduling
	 *
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */
	@Test
	public void testPropagEdgeFindingEnding() throws ContradictionException {
		int[] p = new int[]{11, 6, 5, 5};
		int[] d = new int[]{19, 19, 19, 19};
		int[] r = new int[]{0, 9, 9, 9};
		int[] h = new int[]{1, 1, 1, 1};
		CumulProblem cp = new CumulProblem(p, h);
		cp.starts = SchedUtilities.makeIntvarArray("start", r, d);
		cp.setCapacity(2);
		cp.setHorizon(19);
		cp.initializeModel();
		cp.generateSolver();
		SETTINGS.set(VHM_CEF_ALGO_N2K);
		cp.solver.propagate();
		IntDomainVar v = cp.solver.getVar(cp.starts[0]);
		assertEquals(2, v.getSup());
	}

	/**
	 * Another small example
	 *
	 * @throws ContradictionException
	 */
	@Test
	public void testPropagEdgeFinding2() throws ContradictionException {
		int[] p = new int[]{11, 6, 5, 5, 8};
		int[] d = new int[]{39, 4, 5, 5, 14};
		int[] r = new int[]{0, 0, 0, 0, 12};
		int[] h = new int[]{1, 1, 1, 1, 2};
		CumulProblem cp = new CumulProblem(p, h);
		cp.starts = SchedUtilities.makeIntvarArray("start", r, d);
		cp.setCapacity(2);
		cp.setHorizon(50);
		cp.initializeModel();
		cp.generateSolver();
		SETTINGS.set(VHM_CEF_ALGO_N2K);
		cp.solver.propagate();
		IntDomainVar v = cp.solver.getVar(cp.starts[0]);
		assertEquals(20, v.getInf());
	}

	/**
	 * Pascal example to show that nuijten is incomplete
	 * NOTE : edge finding is not needed to do the deduction that Nuitjen is missing !!!
	 *
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */
	@Test
	public void testEdgeFinding() throws ContradictionException {
		int[] d = new int[]{65, 1, 2, 2, 2};
		int[] p = new int[]{4, 1, 1, 1, 1};
		int[] r = new int[]{0, 1, 0, 0, 2};
		int[] h = new int[]{1, 4, 2, 2, 1};
		CumulProblem cp = new CumulProblem(p, h);
		cp.starts = SchedUtilities.makeIntvarArray("start", r, d);
		cp.setCapacity(4);
		cp.setHorizon(69);
		cp.initializeModel();
		cp.generateSolver();
		SETTINGS.set(VHM_CEF_ALGO_N2K);
		cp.solver.propagate();
		IntDomainVar v = cp.solver.getVar(cp.starts[0]);
		assertEquals(2, v.getInf());
	}

	@Test
	public void testDisjCliques() {
		int[] p = new int[]{2, 3, 2, 2, 1,2};
		int[] h = new int[]{4, 3, 2, 2, 1,1};
		CumulProblem cp = new CumulProblem(p, h);
		cp.setCapacity(4);
		cp.setHorizon(7);
		cp.initializeModel();
		cp.generateSolver(AbstractTestProblem.getConfig(true));
		cp.solver.solveAll();
		assertEquals("Nb Solutions", 12, cp.solver.getSolutionCount());
		
		cp.setCapacity(5);
		cp.initializeModel();
		cp.generateSolver(AbstractTestProblem.getConfig(true));
		cp.generateSolver();
		cp.solver.solveAll();
		assertTrue("Is Feasible", cp.solver.isFeasible());
		assertTrue("more than one solution", cp.solver.getSolutionCount() > 12);
	}
	
	@Ignore
	@Test
	public void testTaskIntervalBug() {
		TaskVariable[] tasks = new TaskVariable[]{
				makeTaskVar("T0", 0, 2, 2),
				makeTaskVar("T1", 0, 2, 2),
				makeTaskVar("T2", 2, 4, 2),
				makeTaskVar("T3", 2, 4, 2),
				makeTaskVar("T4", 4, 6, 2),
				makeTaskVar("T5", 4, 6, 2),
				makeTaskVar("T6", 5, 7, 2),
				makeTaskVar("T7", 5, 8, 2),
		};
		CPModel m = new CPModel();
		Constraint mcstr = cumulativeMax(tasks, new int[]{2, 2, 2, 2, 2, 2, 2, 2}, 4);
		m.addConstraint(mcstr);
		CPSolver s = new CPSolver();
		s.setHorizon(6);
		s.read(m);
		//LOGGER.info(s.pretty());
		Cumulative cstr = (Cumulative) s.getCstr(mcstr);
		cstr.getRules().initializeEdgeFindingData();
		cstr.getRules().initializeEdgeFindingStart();
		try {
			cstr.enforceTaskConsistency();
		} catch (ContradictionException e2) {
			fail("fail to ensure initial consistency");
		}
		try {
			cstr.getRules().slowTaskIntervals();
			fail("slow Task Interval");
		} catch (ContradictionException e) {
			try {
				cstr.getRules().taskIntervals();
				fail("Task Interval");
			} catch (ContradictionException e1) {
				LOGGER.info("Task Intervals algorithms are eequivalent");
			}
		}

	}

	protected void testCumulativeMin(int nbTests, int size, boolean all) {
		int p = 2;
		int[] values = new int[size];
		Arrays.fill(values, p);
		CumulProblem pb = new CumulProblem(values, values);
		pb.capacity = Choco.makeIntVar("capa", 1, 4);
		pb.consumption = Choco.makeIntVar("cons", 3, 4);

		pb.setHorizon(size);
		int nbSols = 2; //consumption factor
		int cpt = 0;
		while (cpt < size) {
			cpt += 2;
			nbSols *= MathUtils.combinaison(cpt, 2);
		}
		//LOGGER.info(nbSols);
		launchAllRules(pb, nbTests, nbSols, -1, true);
		if (all) {
			pb.setHorizon(size + 1);
			launchAllRules(pb, nbTests, nbSols * (size / p + 1), -1, true);
		}
	}


	@Test
	public void testCumulativeMin() {
		//ChocoLogging.setVerbosity(Verbosity.VERBOSE);
		testCumulativeMin(3, 4, true);
		testCumulativeMin(2, 6, true);
		testCumulativeMin(1, 8, false);
	}

	protected void testNegativeheights(int nbTests, int size) {
		int p = 2;
		int s = 3 * size;
		int[] durations = new int[s];
		Arrays.fill(durations, 0, s, 2);
		int[] heights = new int[s];
		Arrays.fill(heights, 0, 2 * size, 2);
		Arrays.fill(heights, 2 * size, s, -1);
		int[] r = new int[s];
		int[] d = new int[s];
		for (int i = 0; i < size; i++) {
			r[i] = p * i;
			d[i] = r[i];
		}
		Arrays.fill(d, size, s, p * size);
		CumulProblem pb = new CumulProblem(durations, heights);
		pb.starts = SchedUtilities.makeIntvarArray("start", r, d);
		pb.capacity = Choco.makeIntVar("capa", 3, 4);
		pb.consumption = Choco.makeIntVar("cons", 3, 4);

		pb.setHorizon(p * size);
		int nbSols = 2; //capacity factor
		int cpt = 0;
		while (cpt++ < size) {
			final int cnp = MathUtils.combinaison(cpt, 1);
			nbSols *= cnp * cnp;
		}
		launchOnlysweep(pb, nbTests, nbSols, -1); //extract disj is forbidden

	}

	public static void launchOnlysweep(CumulProblem cp, final int nbTests, final int nbSol, final int nbNodes) {
		launchAllRules(cp, nbTests, nbSol, nbNodes, false);
	}

	//@Ignore
	@Test
	public void testNegativeHeights() {
		testNegativeheights(10, 2);
		testNegativeheights(5, 3);
		testNegativeheights(1, 4);
	}

	@Test
	public void testRequired() {
		int[] p = {2, 3, 2, 3};
		int[] h = {3, 4, 2, -1};
		int[] r = {0, 2, 5, 0};
		int[] d = {0, 2, 5, 10};
		CumulProblem pb = new CumulProblem(p, h);
		pb.starts = SchedUtilities.makeIntvarArray("start", r, d);
		pb.setCapacity(3);
		pb.consumption = Choco.constant(2);
		pb.setHorizon(10);
		launchOnlysweep(pb, 1, 1, 1);
	}

	@Test
	public void testForbidden() {
		int[] p = {2, 4, 2, 2};
		int[] h = {4, -1, 4, 1};
		int[] r = {0, 0, 2, 0};
		int[] d = {0, 0, 2, 4};
		CumulProblem pb = new CumulProblem(p, h);
		pb.starts = SchedUtilities.makeIntvarArray("start", r, d);
		pb.setCapacity(3);
		pb.consumption = Choco.constant(2);
		pb.setHorizon(4);
		launchOnlysweep(pb, 1, 0, 0);
	}

    @Test
    public void testKLS(){
        // Build model
		Model model = new CPModel();
		// Build a solver
		CPSolver s = new CPSolver();
		s.setUniqueReading(false);
		// init DATA

		int maxL = 2401;
		int maxW = 2431;

		IntegerVariable [] SXs = new IntegerVariable [2];
		IntegerVariable [] LXs = new IntegerVariable [2];
		IntegerVariable [] EXs = new IntegerVariable [2];

		IntegerVariable [] SYs = new IntegerVariable [2];
		IntegerVariable [] LYs = new IntegerVariable [2];
		IntegerVariable [] EYs = new IntegerVariable [2];

		int[] val = {800, 1200};

		for(int i = 0; i < 2; i++){
			SXs[i] = Choco.makeIntVar("SX"+i, 1, 1601, Options.V_ENUM);
			model.addVariable(SXs[i]);
			LXs[i] = Choco.makeIntVar("LX"+i, val);
			model.addVariable(LXs[i]);
			EXs[i] = Choco.makeIntVar("EX"+i, 801, 2401, Options.V_ENUM);
			model.addVariable(EXs[i]);

			SYs[i] = Choco.makeIntVar("SY"+i, 1, 1631, Options.V_ENUM);
			model.addVariable(SYs[i]);
			LYs[i] = Choco.makeIntVar("LY"+i, val);
			model.addVariable(LYs[i]);
			EYs[i] = Choco.makeIntVar("EY"+i, 801, 2431, Options.V_ENUM);
			model.addVariable(EYs[i]);
		}

		// Cumulative on X axis

		IntegerVariable mw = Choco.makeIntVar("MaxW", 0, maxW);

		Constraint cumX = DeprecatedChoco.cumulative(SXs,          // Starts
				                                  EXs,          // Ends
				                                  LXs,          // Widths as durations
				                                  LYs,          // Heights
				                                  mw, 	    	// capacity of the constraint
				                                  Options.C_CUMUL_TI);


		model.addConstraint(cumX);

		// read the model
    	s.read(model);

		// Cumulative on Y axis
    	IntegerVariable ml = Choco.makeIntVar("MaxL", 0, maxL);

    	Constraint cumY = DeprecatedChoco.cumulative(SYs,          // Starts
				                                  EYs,          // Ends
				                                  LYs,          // Widths as durations
				                                  LXs,          // Heights
				                                  ml,
				                                  Options.C_CUMUL_TI);

    	model.addConstraint(cumY);
        s.read(model);
        s.postTaskConsistencyConstraints();
		s.postMakespanConstraint();
       	try {                                              // Propagate
    		s.propagate();
    	} catch (ContradictionException e) {
			Assert.fail();
    	}
        Assert.assertEquals(2431, s.getVar(EYs[1]).getSup());
    }
}
