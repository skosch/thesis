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

package choco.cp.solver.preprocessor;

import choco.cp.common.util.preprocessor.AbstractDetector;
import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveModel;
import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel;
import choco.cp.model.CPModel;
import choco.cp.model.preprocessor.ModelDetectorFactory;
import choco.cp.solver.CPSolver;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.variables.Variable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Iterator;

/*
 *
 * User:    charles
 * Date:    19 août 2008
 *
 * Black box solver
 */
public class PreProcessCPSolver extends CPSolver {

	/**
	 * Search component
	 */
	private final PPSearch ppsearch;
	
	private DisjunctiveModel disjMod;
	
	private DisjunctiveSModel disjSMod;

	/**
	 * initial propagation time (to decide whether to perform or not
	 * singleton and restarts). If propagation is too heavy, we will avoid
	 * restarting
	 */
	private int proptime;

	public PreProcessCPSolver() {
		this(new PreProcessConfiguration());
	}

	public PreProcessCPSolver(Configuration configuration) {
		this(new EnvironmentTrailing(), configuration);
	}

	public PreProcessCPSolver(final IEnvironment env, Configuration configuration) {
		super(env, configuration);
		this.mod2sol = new PPModelToCPSolver(this);
		this.ppsearch = new PPSearch();
		//optionsSet.addAll(Arrays.asList(options));
	}

	public final DisjunctiveModel getDisjModel() {
		return disjMod;
	}

	public final DisjunctiveSModel getDisjSModel() {
		if(disjSMod == null && disjMod != null) {
			disjSMod = new DisjunctiveSModel(this, disjMod);
		}
		return disjSMod;
	}
	
	public PPModelToCPSolver getMod2Sol() {
		return (PPModelToCPSolver) mod2sol;
	}

	public void setRandomValueOrdering(final int seed) {
		ppsearch.setRandomValueHeuristic(seed);
	}

	/**
	 * read of the black box solver
	 *
	 * @param m model
	 */
	public void read(final Model m) {
		this.model = (CPModel) m;
		ppsearch.setModel(model);

		super.initReading();

		SolverDetectorFactory.associateIndexes(model);

		if (configuration.readBoolean(PreProcessConfiguration.INT_EQUALITY_DETECTION)) {
			SolverDetectorFactory.intVarEqDet(model, this).applyThenCommit();
		}

		mod2sol.readIntegerVariables(model);
		mod2sol.readRealVariables(model);
		mod2sol.readSetVariables(model);
		mod2sol.readConstants(model);

		if (configuration.readBoolean(PreProcessConfiguration.TASK_EQUALITY_DETECTION)) {
			SolverDetectorFactory.taskVarEqDet(model, this).applyThenCommit();
		}

		mod2sol.readMultipleVariables(model);

		if (configuration.readBoolean(PreProcessConfiguration.DISJUNCTIVE_DETECTION)) {
			SolverDetectorFactory.disjunctionDetector(model, this).applyThenCommit();
		}

		if (configuration.readBoolean(PreProcessConfiguration.EXPRESSION_DETECTION)) {
			SolverDetectorFactory.expressionDetector(model, this).applyThenCommit();
		}

		if (configuration.readBoolean(PreProcessConfiguration.CLIQUES_DETECTION)) {
			final boolean symBreak = configuration.readBoolean(PreProcessConfiguration.SYMETRIE_BREAKING_DETECTION);
			ModelDetectorFactory.cliqueDetector(model, symBreak).applyThenCommit();
		}

		if (configuration.readBoolean(PreProcessConfiguration.DISJUNCTIVE_FROM_CUMULATIVE_DETECTION)) {
			ModelDetectorFactory.disjFromCumulDetector(model).applyThenCommit();
		}

		if (configuration.readBoolean(PreProcessConfiguration.DISJUNCTIVE_MODEL_DETECTION) ) {
			disjMod = new DisjunctiveModel(model);
			if (configuration.readBoolean(PreProcessConfiguration.DMD_USE_TIME_WINDOWS) ) {
				ModelDetectorFactory.precFromTimeWindowDetector(model, disjMod).applyThenCommit();
			}
			final AbstractDetector[] detectors = ModelDetectorFactory.disjunctiveModelDetectors(model, disjMod, configuration.readBoolean(PreProcessConfiguration.DMD_GENERATE_CLAUSES));
			for (AbstractDetector det : detectors) {
				det.applyThenCommit();
			}
			if (configuration.readBoolean(PreProcessConfiguration.DMD_REMOVE_DISJUNCTIVE) ) {
				ModelDetectorFactory.rmDisjDetector(model).applyThenCommit();
			}
			
		}

		mod2sol.readVariables(model);

		getMod2Sol().readBBDecisionVariables();
		getMod2Sol().readConstraints(model);
	}

	/**
	 * set a heuristic that automatically choose between impact and DomWdeg
	 *
	 * @param s        solver
	 * @param inittime init time
	 * @return true if the problem is still feasible
	 */
	public boolean setVersatile(final CPSolver s, final int inittime) {
		return ppsearch.setVersatile(s, inittime);
	}

	/**
	 * set the DomOverDeg heuristic
	 *
	 * @param s solver
	 * @return true if the problem was not detected infeasible in the process
	 */
	public boolean setDomOverDeg(final CPSolver s) {
		return ppsearch.setDomOverDeg(s);
	}

	/**
	 * set the DomOverWDeg heuristic
	 *
	 * @param s        solver
	 * @param inittime init time
	 * @return true if the problem was not detected infeasible in the process
	 */
	public boolean setDomOverWeg(final CPSolver s, final int inittime) {
		return ppsearch.setDomOverWeg(s, inittime);
	}


	/**
	 * set the Impact heuristic
	 *
	 * @param s                  solver
	 * @param initialisationtime init time
	 * @return true if the problem was not detected infeasible in the process
	 */
	public boolean setImpact(final CPSolver s, final int initialisationtime) {
		return ppsearch.setImpact(s, initialisationtime);
	}

	/**
	 * return true if contains at least one disjunctive
	 *
	 * @return a boolean
	 */
	boolean isScheduling() {
		if (model == null) {
			throw new SolverException("you must read the model before !");
		} else return ppsearch.isScheduling();
	}

	/**
	 * return true if contains at least one extensional constraints
	 *
	 * @return a boolean
	 */
	public boolean isExtensionnal() {
		if (model == null) {
			throw new SolverException("you must read the model before !");
		} else return model.getNbConstraintByType(ConstraintType.TABLE) > 0;
	}

	/**
	 * return true if contains at least one extensional constraints
	 *
	 * @return a boolean
	 */
	boolean isBinaryExtensionnal() {
		if (model == null) {
			throw new SolverException("you must read the model before !");
		} else {
			if (model.getNbConstraintByType(ConstraintType.TABLE) == 0)
				return false;
			final Iterator<Constraint> it = model.getConstraintIterator();
			while (it.hasNext()) {
				final Constraint ct = it.next();
				if (ct.getNbVars() > 2) return false;
			}
			return true;
		}
	}

	public PPSearch getBBSearch() {
		return ppsearch;
	}

	public final <MV extends Variable, SV extends Var> void setVar(MV v, SV sv) {
		mapvariables.put(v.getIndex(), sv);
	}

	public final <MC extends Constraint, SC extends SConstraint> void setCstr(MC c, SC sc) {
		mapconstraints.put(c.getIndex(), sc);
	}

	public final boolean contains(final Constraint c) {
		return mapconstraints.containsKey(c.getIndex());
	}


	//******************************************************************//
	//***************** Root node Propagation tools ********************//
	//******************************************************************//


	/**
	 * Perform initial propagarion and measure its time
	 *
	 * @return true if the problem is still consistent at this stage
	 */
	public boolean initialPropagation() {
		proptime = (int) System.currentTimeMillis();
		try {
			propagate();
		} catch (ContradictionException e) {
			setFeasible(Boolean.FALSE);
			return false;
		}
		proptime = (int) System.currentTimeMillis() - proptime;
		//        restartMode = !isBinaryExtensionnal();
		return true;
	}

	/**
	 * Apply a step of singleton consistency
	 *
	 * @param timelimit the time limit to respect
	 * @return boolean
	 */
	public boolean rootNodeSingleton(final int timelimit) {
		final boolean sched = isScheduling();
		final int time = (int) System.currentTimeMillis();
		if (proptime <= 1500) {
			int nbvrem = 0;
			int nbvtried = 0;
			final int threshold = 100;
			worldPush();
			for (int i = 0; i < getNbIntVars(); i++) {
				final IntDomainVar v = getIntVar(i);
				final DisposableIntIterator it = v.getDomain().getIterator();
				if ((sched && v.getDomainSize() == 2) || (!sched && v.hasEnumeratedDomain())) {
					while (it.hasNext()) {
						final int val = it.next();
						nbvtried++;
						boolean cont = false;
						worldPush();
						try {
							v.instantiate(val, null, true);
							propagate();
						} catch (ContradictionException e) {
							cont = true;
						}
						worldPop();
						if (cont) {
							try {
								nbvrem++;
								v.remVal(val);
								propagate();
							} catch (ContradictionException e) {
								return false;
							}
						}
						//gardefou (limite de temps)
						if ((int) (System.currentTimeMillis() - time) > timelimit) {
							break;
						}
						if (nbvtried == threshold && nbvrem == 0 && proptime >= 200) {
							break;
						}
					}
					it.dispose();
				}
			}
		}
		return true;
	}
}
