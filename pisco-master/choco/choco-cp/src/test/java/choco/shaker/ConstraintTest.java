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

package choco.shaker;

import static choco.Choco.distanceEQ;
import static choco.shaker.tools.factory.ConstraintFactory.CUMUL_SCOPE;
import static choco.shaker.tools.factory.ConstraintFactory.DISJ_SCOPE;
import static choco.shaker.tools.factory.ConstraintFactory.TEMPORAL_SCOPE;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.preprocessor.PreProcessConfiguration;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.common.util.objects.DeterministicIndicedList;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.shaker.tools.factory.CPModelFactory;
import choco.shaker.tools.factory.MetaConstraintFactory;
import choco.shaker.tools.factory.OperatorFactory;
import choco.shaker.tools.factory.VariableFactory;
import choco.shaker.tools.factory.ConstraintFactory.C;

/*
 * User : charles
 * Mail : cprudhom(a)emn.fr
 * Date : 12 mars 2009
 * Since : Choco 2.0.1
 * Update : Choco 2.0.1
 */
public class ConstraintTest {

	protected final static Logger LOGGER = ChocoLogging.getTestLogger();

	public static void setLogOnTest() {
		ChocoLogging.setVerbosity(Verbosity.DEFAULT);
		ChocoLogging.getTestLogger().setLevel(Level.INFO);
	}
	
	Model m;
	Solver s;
	static Random random;
	int seed;
	boolean print=false;

	@Before
	public void before(){
		m = new CPModel();
		s = new CPSolver();
		random = new Random();
	}

	@After
	public void after(){
		m = null;
		s = null;
	}



	private CPModelFactory getSchedulingModF(C...uses) {
		final CPModelFactory modF = new CPModelFactory();
		modF.limits(30);
		modF.depth(0);
		modF.uses(VariableFactory.V.ENUMVAR, VariableFactory.V.BOUNDVAR,
				VariableFactory.V.BLISTVAR, VariableFactory.V.BTREEVAR,
				VariableFactory.V.LINKVAR, VariableFactory.V.BOOLVAR,
				VariableFactory.V.CST);
		modF.uses(MetaConstraintFactory.MC.NONE);
		modF.uses(OperatorFactory.O.NONE);
		modF.setSchedulingVarFactory();
		modF.uses(uses);
		return modF;
	}

	public void testModel(CPModelFactory modF, int seed, int nbChecks) {
		final Random r = new Random(seed);
		m = modF.model(r);
		LOGGER.log(Level.INFO, "testModel - seed:{0} constr:{1}", new Object[]{seed, m.getConstraint(0).getConstraintType()});
		int sl = 0;
		try{
			while(sl < nbChecks){
				sl++;
				checker(r);
			}
		}catch (UnsupportedOperationException e){
			LOGGER.severe(MessageFormat.format("seed:{0}({1}) : {2}", seed, sl, e.getMessage()));
		}catch (Exception e){
			System.err.println(MessageFormat.format("seed:{0}({1}) : {2}", seed, sl, e.getMessage()));
			System.err.println(m.pretty());
			e.printStackTrace();
			Assert.fail();
		}catch (AssertionError e){
			System.err.println(MessageFormat.format("seed:{0}({1}) : {2}", seed, sl, e.getMessage()));
			System.err.println(m.pretty());
			e.printStackTrace();
			Assert.fail();
		}
		ChocoLogging.flushLogs();
	}
	
	public void testPP(CPModelFactory modF, int seed, int nbC) {
		final String msg = "testPP - seed:"+seed+" nbC:"+nbC;
		LOGGER.info(msg);
		final Random r = new Random(seed);
		m = modF.model(nbC, r);
		CPSolver s1 = new CPSolver();
		s1.createMakespan();
		s1.read(m);
		s1.setRandomSelectors(seed);
		s1.setTimeLimit(2000);
		s1.minimize(s1.getMakespan(), false);
		
		PreProcessCPSolver s2 = new PreProcessCPSolver();
		s2.createMakespan();
		final Configuration config = s2.getConfiguration();
		PreProcessConfiguration.cancelPreProcess(config);
		config.putTrue(PreProcessConfiguration.DISJUNCTIVE_MODEL_DETECTION);
		config.putTrue(PreProcessConfiguration.DMD_USE_TIME_WINDOWS);
		config.putTrue(PreProcessConfiguration.DMD_REMOVE_DISJUNCTIVE);
		config.putTrue(PreProcessConfiguration.DISJUNCTIVE_FROM_CUMULATIVE_DETECTION);
		s2.read(m);
		s2.setRandomSelectors(seed);
		s2.setTimeLimit(2000);
		s2.minimize(s2.getMakespan(), false);
		if(s1.isEncounteredLimit() || s2.isEncounteredLimit() ) {
			LOGGER.info("solveAll [FAIL]");
			Assert.assertFalse(msg, s1.isFeasible() == Boolean.TRUE && s2.isFeasible() == Boolean.FALSE);
			Assert.assertFalse(msg, s1.isFeasible() == Boolean.FALSE && s2.isFeasible() == Boolean.TRUE);
		} else {
			LOGGER.info("solveAll [OK]");
			Assert.assertNotNull(msg, s1.isFeasible());
			Assert.assertNotNull(msg, s2.isFeasible());
			Assert.assertEquals(msg, s1.isFeasible(), s2.isFeasible());
			Assert.assertEquals(msg, s1.getObjectiveValue(), s2.getObjectiveValue());
		}
		ChocoLogging.flushLogs();
	}



	@Test
	public void testTemporalConstraint() {
		for (int i = 0; i < 25; i++) {
			testModel(getSchedulingModF(TEMPORAL_SCOPE), i, 500);
		}
	}

	@Test
	public void testDisjunctiveConstraint() {
		for (int i = 0; i < 15; i++) {
			testModel(getSchedulingModF(DISJ_SCOPE), i, 250);
		}
	}
	
	@Test
	public void testCumulativeConstraint() {
		for (int i = 0; i < 15; i++) {
			testModel(getSchedulingModF(CUMUL_SCOPE), i, 250);
		}
	}

	@Test
	public void testTemporalPP() {
		for (int i = 0; i < 10; i++) {
			testPP(getSchedulingModF(TEMPORAL_SCOPE), 10, 2);
			testPP(getSchedulingModF(TEMPORAL_SCOPE), 10, 3);
			testPP(getSchedulingModF(TEMPORAL_SCOPE), 10, 5);
			testPP(getSchedulingModF(TEMPORAL_SCOPE), 10, 8);
		}
	}

	@Test
	public void testDisjunctivePP() {
		for (int i = 0; i < 20; i++) {
			testPP(getSchedulingModF(DISJ_SCOPE), i, 1);
			testPP(getSchedulingModF(DISJ_SCOPE), i, 2);
			testPP(getSchedulingModF(DISJ_SCOPE), i, 3);
		}
	}
	
	@Test
	public void testCumulativePP() {
		//setLogOnTest();
		for (int i = 0; i < 20; i++) {
			testPP(getSchedulingModF(CUMUL_SCOPE), i, 1);
			testPP(getSchedulingModF(CUMUL_SCOPE), i, 2);
			testPP(getSchedulingModF(CUMUL_SCOPE), i, 3);
		}
	}
	
	@Test
	public void testSchedulingPP() {
		for (int i = 0; i < 20; i++) {
			testPP(getSchedulingModF(ArrayUtils.append(DISJ_SCOPE, CUMUL_SCOPE, TEMPORAL_SCOPE)), i, 2);
			testPP(getSchedulingModF(ArrayUtils.append(DISJ_SCOPE, CUMUL_SCOPE, TEMPORAL_SCOPE)), i, 3);
		}
	}
	
	@Test
	@Ignore
	public void testConstraint() {
		//        print = true;
		//        LOGGER.setLevel(Level.OFF);
		int mainLoop = 200000;
		int subLoop = 1000;
		int sl = 0;
		for (int i = 0; i < mainLoop; i++) {
			seed = i;
			//System.out.println(seed);
			Random r = new Random(seed);
			CPModelFactory mf = new CPModelFactory();

			mf.limits(10);
			mf.depth(0);
			mf.uses(VariableFactory.V.ENUMVAR, VariableFactory.V.BOUNDVAR,
					VariableFactory.V.BLISTVAR, VariableFactory.V.BTREEVAR,
					VariableFactory.V.LINKVAR, VariableFactory.V.BOOLVAR,
					VariableFactory.V.CST);
			mf.uses(MetaConstraintFactory.MC.NONE);
			mf.uses(OperatorFactory.O.NONE);
			m = mf.model(r);
			try{
				sl = 0;
				while(sl < subLoop){
					sl++;
					checker(r);
				}
			}catch (UnsupportedOperationException e){
				LOGGER.severe(MessageFormat.format("seed:{0}({1}) : {2}", seed, sl, e.getMessage()));
			}catch (Exception e){
				System.err.println(MessageFormat.format("seed:{0}({1}) : {2}", seed, sl, e.getMessage()));
				e.printStackTrace();
				Assert.fail();
			}catch (AssertionError e){
				System.err.println(MessageFormat.format("seed:{0}({1}) : {2}", seed, sl, e.getMessage()));
				e.printStackTrace();
				Assert.fail();
			}
		}

	}


	@Test
	public void test1(){
		m  = new CPModel();
		IntegerVariable[] vars = new IntegerVariable[4];
		vars[0] = Choco.makeBooleanVar("b1");
		vars[1] = Choco.makeBooleanVar("b2");
		vars[2] = Choco.makeIntVar("v3", 4,6, Options.V_ENUM);
		vars[3] = Choco.makeIntVar("v4", 9,9, Options.V_ENUM);

		m.addConstraint(Choco.reifiedConstraint(vars[0], distanceEQ(vars[1], vars[2], vars[3], -2)));

		checker(new Random(37));
	}

	@Test
	public void test2(){
		m  = new CPModel();
		IntegerVariable[] vars = new IntegerVariable[10];
		vars[0] = Choco.makeIntVar("v1", 1, 1);
		vars[1] = Choco.makeIntVar("v2", -2, 2);
		vars[2] = Choco.makeIntVar("v3", 8, 8);
		vars[3] = Choco.makeIntVar("v4", -5, -1);
		vars[4] = Choco.makeIntVar("v5", -3, -3);
		vars[5] = Choco.makeBooleanVar("b6");
		vars[6] = Choco.makeIntVar("v7", 3, 3);
		vars[7] = Choco.makeIntVar("v8", 2, 3);
		vars[8] = Choco.makeBooleanVar("b9");
		vars[9] = Choco.makeIntVar("v10", 2, 4);

		m.addConstraint(Choco.lexEq(new IntegerVariable[]{vars[0], vars[1], vars[2], vars[3], vars[4]},
				new IntegerVariable[]{vars[5], vars[6], vars[7], vars[8], vars[9]}));

		checker(new Random(50));
	}

	@Test
	public void test3(){
		m  = new CPModel();
		IntegerVariable[] vars = new IntegerVariable[2];
		vars[0] = Choco.makeIntVar("v1", 2, 2);
		vars[1] = Choco.makeBooleanVar("b2");

		m.addConstraint(Choco.distanceNEQ(vars[0], vars[1], -2));

		checker(new Random(94));
	}

	@Test
	public void test4(){
		m  = new CPModel();
		IntegerVariable[] vars = new IntegerVariable[5];
		vars[0] = Choco.makeBooleanVar("b1");
		vars[1] = Choco.makeIntVar("v2", 3,3);
		vars[2] = Choco.makeIntVar("v3", -1,1, Options.V_BLIST);
		vars[3] = Choco.makeBooleanVar("b4");
		vars[4] = Choco.makeBooleanVar("b5");

		m.addConstraint(Choco.reifiedConstraint(
				vars[0],
				Choco.oppositeSign(vars[1], vars[2]),
				Choco.distanceEQ(vars[3], vars[4], 0)
		)
		);

		checker(new Random(4098));
	}


	@Test
	public void test5(){
		m  = new CPModel();
		IntegerVariable[] vars = new IntegerVariable[5];
		vars[0] = Choco.makeIntVar("v1", -1, -1);
		vars[1] = Choco.makeIntVar("v2", 0, 1);
		vars[2] = Choco.makeIntVar("v3", -3, 5);
		vars[3] = Choco.makeIntVar("v4", -2, -2);
		vars[4] = Choco.makeIntVar("v5", -4, 2);

		int low[] = new int[]{0,0,2,0,0,0,3,0,0,0};
		int up[] = new int[]{5,5,5,5,5,5,5,5,5,5};

		m.addConstraint(Choco.globalCardinality(Options.C_GCC_BC,vars, low, up, -4));

		checker(new Random(14106));
	}

	@Test
	public void test6(){
		m  = new CPModel();
		IntegerVariable[] vars = new IntegerVariable[5];
		vars[0] = Choco.makeIntVar("v1", -3, -3);
		vars[1] = Choco.makeIntVar("v2", -2, 3);
		vars[2] = Choco.makeIntVar("v3", 1, 3);
		vars[3] = Choco.makeIntVar("v4", 2, 2);
		vars[4] = Choco.makeIntVar("v5", 0, 1);

		int low[] = new int[]{2,0,0,0,0,1,2};
		int up[] = new int[]{5,4,0,0,0,1,5};

		m.addConstraint(Choco.globalCardinality(Options.C_GCC_BC,vars, low, up, -3));

		checker(new Random(0));
	}


	@Test
	public void test7(){
		m  = new CPModel();
		IntegerVariable[] vars = new IntegerVariable[10];
		vars[0] = Choco.makeIntVar("v1", 0, 1);
		vars[1] = Choco.makeIntVar("v2", 0, 9);
		vars[2] = Choco.makeIntVar("v3", -4, 2);
		vars[3] = Choco.makeIntVar("v4", -4, 2);
		vars[4] = Choco.makeIntVar("v5", 4, 8);
		vars[5] = Choco.makeIntVar("v6", 0, 1);
		vars[6] = Choco.makeIntVar("v7", -5, 1);
		vars[7] = Choco.makeIntVar("v8", -4, 5);
		vars[8] = Choco.makeIntVar("v9", 0, 0);
		vars[9] = Choco.makeIntVar("v10", -7, 2);

		m.addConstraint(Choco.reifiedConstraint(
				vars[0],
				Choco.allDifferent(vars[1], vars[2], vars[3], vars[4], vars[5], vars[6], vars[7]),
				Choco.oppositeSign(vars[8], vars[9])
		));

		checker(new Random(0));
	}

	@Test
	public void test9(){
		m  = new CPModel();
		IntegerVariable[] vars = new IntegerVariable[10];
		vars[0] = Choco.makeIntVar("v1", -1, 3);
		vars[1] = Choco.makeIntVar("v2", 1, 1);
		vars[2] = Choco.makeIntVar("v3", 4, 4);
		vars[3] = Choco.makeIntVar("v4", 3, 3);
		vars[4] = Choco.makeIntVar("v5", 2, 9);
		vars[5] = Choco.makeIntVar("v6", 4, 4);
		vars[6] = Choco.makeIntVar("v7", 0, 1);
		vars[7] = Choco.makeIntVar("v8", 0, 1);

		m.addConstraint(Choco.reifiedConstraint(
				vars[7],
				Choco.globalCardinality(new IntegerVariable[]{vars[0], vars[1], vars[2], vars[3], vars[4]},
						new int[]{0,5,0,0,0,0,0,0,0,0,0}, new int[]{5,5,2,5,5,5,5,5,5,5,5}, -1) ,
						Choco.gt(vars[5], vars[6])
		));

		checker(new Random(4096));
	}

	@Test
	public void test10(){
		m  = new CPModel();
		IntegerVariable[] vars = new IntegerVariable[10];
		vars[0] = Choco.makeIntVar("v1", 0, 1);
		vars[1] = Choco.makeIntVar("v2", 3, 8);

		m.addConstraint(Choco.reifiedConstraint(
				vars[0],
				Choco.neq(vars[1], 4),
				Choco.eq(vars[1], 4)
		));

		checker(new Random(4099));
	}

	@Test
	public void test11(){
		m  = new CPModel();
		TaskVariable[] tasks = Choco.makeTaskVarArray("T", 0, 60, ArrayUtils.oneToN(10));
		m.addConstraint(Choco.disjunctive(tasks));
		checker(new Random(4099));
	}

	/**
	 * Generate s tuple and check if this tuple satifies or not the problem.
	 * Then, shake and check CHOCO to ensure it leads to the correct conclusion (solution or not).
	 * @param r
	 */
	private void checker(Random r) {
		s = new CPSolver();
		s.read(m);

		DeterministicIndicedList<IntDomainVar> vars = new DeterministicIndicedList<IntDomainVar>(IntDomainVar.class, s.getNbIntVars());
		ArrayList<AbstractIntSConstraint> cstrs = new ArrayList<AbstractIntSConstraint>(s.getNbIntConstraints());
		init(vars);

		// Simule an instanciation of eeach variable
		int[] values = pickValues(vars, r);
		boolean satisfied = satisifies(vars, cstrs, values);

		// If the constraints are satisfied with this tuple,
		// we ensure that CHOCO find this solution
		if(satisfied){
			goesToSolution(vars, cstrs, values, r);
		}else
			// We ensure CHOCO goes to a fail
		{
			goesToFail(vars, cstrs, values, r);
		}
	}



	/**
	 * Generate random events that lead to instanciation based on values array
	 * and propagate them to ensure CHOCO finds a solution
	 * @param vars
	 * @param cstrs
	 * @param values
	 * @param r
	 */
	private void goesToSolution(DeterministicIndicedList<IntDomainVar> vars, ArrayList<AbstractIntSConstraint> cstrs,
			int[] values, Random r) {
		while(!fullyInstanciated(vars)) {
			try {
				// Propagate before generation of event to get first propagation
				s.propagate();
				AbstractIntSConstraint sc = cstrs.get(r.nextInt(cstrs.size()));
				int v = r.nextInt(sc.getNbVars());
				generateEvent(sc.getVar(v), values[vars.get(sc.getVar(v))], r);
				stillInstanciable(vars, values);
			} catch (ContradictionException e) {
				Assert.fail("(seed:" + seed + ") unexpected behaviour in propagation...\n"+e.getMessage()+"\n" + s.pretty());
			}
		}
		Assert.assertEquals("(seed:"+seed+") not satisfied", true, s.checkSolution());
	}

	/**
	 * Generate random events taht lead to instanciation based on values array
	 * and propagate them to ensure CHOCO does not find a solution
	 * @param vars
	 * @param cstrs
	 * @param values
	 * @param r
	 */
	private void goesToFail(DeterministicIndicedList<IntDomainVar> vars, ArrayList<AbstractIntSConstraint> cstrs,
			int[] values, Random r) {
		Level level = ChocoLogging.getEngineLogger().getLevel();
		ChocoLogging.getEngineLogger().setLevel(Level.OFF);
		int loop = 10;
		while(loop>0) {
			s.worldPush();
			int subloop = vars.size()*10;
			while(subloop>0){
				try {
					// Propagate before generation of event to get first propagation
					s.propagate();
					AbstractIntSConstraint sc = cstrs.get(r.nextInt(cstrs.size()));
					int v;
					v = r.nextInt(sc.getNbVars());
					generateEvent(sc.getVar(v), values[vars.get(sc.getVar(v))], r);
					stillInstanciable(vars, values);
					if(fullyInstanciated(vars)){
						if(s.checkSolution()){
							ChocoLogging.getEngineLogger().setLevel(level);
							Assert.fail("(seed:" + seed + ") satisfied...\n" + s.pretty());
						}
					}
					subloop--;
				} catch (ContradictionException e) {
					//can fail, it is an expected behaviour
					subloop = 0;
				}
			}
			loop--;
			s.worldPop();
		}
		ChocoLogging.getEngineLogger().setLevel(level);
	}


	/**
	 * Initialise structure
	 * @param vars
	 * @return
	 */
	private void init(DeterministicIndicedList<IntDomainVar> vars){
		DisposableIterator<SConstraint> it = s.getConstraintIterator();
		while(it.hasNext()){
			SConstraint c = it.next();
			for(int v = 0; v < c.getNbVars(); v++){
				IntDomainVar var = (IntDomainVar)c.getVar(v);
				vars.add(var);
			}
		}
		it.dispose();
	}

	/**
	 *  Determine whether the tuple generated staisfies the constraints
	 * @param vars
	 * @param cstrs
	 * @param values
	 * @return
	 */
	private boolean satisifies(DeterministicIndicedList<IntDomainVar> vars, ArrayList<AbstractIntSConstraint> cstrs,
			int[] values){
		// Check if constraints are satisfied
		DisposableIterator<SConstraint> it = s.getConstraintIterator();
		boolean satisfied = true;
		while(it.hasNext()){
			AbstractIntSConstraint c = (AbstractIntSConstraint)it.next();
			cstrs.add(c);
			int[] tuple = new int[c.getNbVars()];
			for(int i = 0; i < c.getNbVars(); i++){
				tuple[i] = values[vars.get((IntDomainVar)c.getVar(i))];
			}
			satisfied &= c.isSatisfied(tuple);
		}
		it.dispose();
		return satisfied;
	}


	/**
	 * Check wether at least one variable can be instanciated to the decided value anymore
	 * @param vars
	 * @param values
	 * @throws ContradictionException
	 */
	private void stillInstanciable(DeterministicIndicedList<IntDomainVar> vars, int[] values) throws ContradictionException{
		Iterator<IntDomainVar> itv = vars.iterator();
		IntDomainVar v;
		while(itv.hasNext()){
			v = itv.next();
			if(!v.canBeInstantiatedTo(values[vars.get(v)])){
				s.getPropagationEngine().raiseContradiction("stillInstanciable");
			}
		}
	}


	/**
	 * Check wether every variables of the solver are instanciated
	 * @param vars
	 * @return
	 */
	private boolean fullyInstanciated(DeterministicIndicedList<IntDomainVar> vars){
		Iterator<IntDomainVar> itv = vars.iterator();
		while(itv.hasNext()){
			if(!itv.next().isInstantiated()){
				return false;
			}
		}
		return true;
	}

	/**
	 * Generate an event based on the variables of the solver
	 * @param var
	 * @param value
	 * @param r
	 * @throws ContradictionException
	 */
	private void generateEvent(IntDomainVar var, int value, Random r) throws ContradictionException {
		int event = r.nextInt(4);
		int v;
		switch (event) {
		case 0: // INSTANTIATION
			if(print)LOGGER.info(var.getName()+" = "+value);
			var.instantiate(value, null, true);
			break;
		case 1: // LOWER BOUND
			if(value > var.getInf()){
				v = value - var.getInf();
				v = value - r.nextInt(v);
				if(print)LOGGER.info(var.getName()+" >= "+v);
				var.updateInf(v, null, true);
			}
			break;
		case 2: // UPPER BOUND
			if(value < var.getSup()){
				v = var.getSup() - value;
				v = value + r.nextInt(v);
				if(print)LOGGER.info(var.getName()+" <= "+v);
				var.updateSup(v, null, true);
			}
			break;
		case 3: // REMOVAL
			if (var.getDomainSize() > 1) {
				v = getRandomValue(var, r);
				while (v == value) {
					v = getRandomValue(var, r);
				}
				if(print)LOGGER.info(var.getName()+" != "+v);
				var.removeVal(v, null, true);
			}
			break;
		}
	}

	/**
	 * Randomly create a tuple of value
	 * @param vars
	 * @param r
	 * @return
	 */
	private int[] pickValues(DeterministicIndicedList<IntDomainVar> vars, Random r) {
		int[] tuple = new int[vars.size()];
		for (int i = 0; i < tuple.length; i++) {
			IntDomainVar var = vars.get(i);
			tuple[i] = getRandomValue(var, r);
			if(print)LOGGER.info(var.getName()+":"+tuple[i]);
		}
		return tuple;
	}

	/**
	 * Get a random value in the domain
	 * BEWARE : do not use Domain#getRandomDomainValue() because can not
	 * be replayed.
	 * @param var
	 * @param r
	 * @return
	 */
	private int getRandomValue(IntDomainVar var, Random r) {
		int v = var.getInf() + r.nextInt(var.getSup() - var.getInf() + 1);
		while (!var.canBeInstantiatedTo(v)) {
			v = var.getInf() + r.nextInt(var.getSup() - var.getInf() + 1);
		}
		return v;
	}



}
