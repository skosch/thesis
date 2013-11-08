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

/**
 *
 */
package parser.instances;

import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.MIN_LOWER_BOUND;
import static choco.Choco.makeIntVar;
import choco.Options;
import choco.cp.solver.CPSolver;
import choco.cp.solver.preprocessor.PreProcessConfiguration;
import choco.kernel.common.opres.heuristics.IHeuristic;
import choco.kernel.common.opres.heuristics.NoHeuristic;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ResolutionPolicy;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.checker.SolutionCheckerException;
import choco.kernel.visu.IVisuManager;

/**
 * @author Arnaud Malapert
 *
 */
public abstract class AbstractMinimizeModel extends AbstractInstanceModel {

	private IHeuristic heuristics;

	private int computedLowerBound;

	private IVisuManager chartManager;
	// FIXME - Not instantiated in all subclasses - created 20 juil. 2011 by Arnaud Malapert

	public AbstractMinimizeModel(InstanceFileParser parser, Configuration settings) {
		super(parser, settings);
		cancelHeuristic();
		settings.putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MINIMIZE);
		settings.putFalse(Configuration.STOP_AT_FIRST_SOLUTION);
	}


	public final void cancelHeuristic() {
		if(heuristics != null) {heuristics.reset();}
		heuristics = NoHeuristic.getInstance();
	}

	public final IHeuristic getHeuristic() {
		return heuristics;
	}

	public final void setHeuristic(IHeuristic heuristic) {
		this.heuristics = heuristic;
	}

	public final void cancelLowerBound() {
		setComputedLowerBound(MIN_LOWER_BOUND);
	}

	public int getComputedLowerBound() {
		return computedLowerBound;
	}

	public final void setComputedLowerBound(int computedLowerBound) {
		this.computedLowerBound = computedLowerBound;
	}


	public final IVisuManager getChartManager() {
		return chartManager;
	}

	public final void setChartManager(IVisuManager chartManager) {
		this.chartManager = chartManager;
	}


	@Override
	public void initialize() {
		super.initialize();
		cancelLowerBound();
	}

	@Override
	public Boolean preprocess() {
		if(heuristics != null && 
				defaultConf.readBoolean(BasicSettings.PREPROCESSING_HEURISTICS)) {
			heuristics.execute();
			if( heuristics.existsSolution()) {
				objective = heuristics.getObjectiveValue();
				return Boolean.TRUE;
			}
		}
		return null;
	}


	protected IntegerVariable buildObjective(String name, int defaultUpperBound) {
		return makeIntVar(name, 
				Math.max(getComputedLowerBound(), MIN_LOWER_BOUND),
				Math.min(isFeasible() == Boolean.TRUE ? objective.intValue() - 1 : defaultUpperBound, MAX_UPPER_BOUND),
				Options.V_OBJECTIVE,Options.V_NO_DECISION, Options.V_BOUND
				);
	}
	
	protected double getGapILB() {
		return objective.doubleValue() / computedLowerBound;
	}
	
	@Override
	protected void logOnDiagnostics() {
		super.logOnDiagnostics();
		if(computedLowerBound != MIN_LOWER_BOUND) {
			logMsg.appendDiagnostic("INITIAL_LOWER_BOUND", computedLowerBound);
			if(getStatus() == ResolutionStatus.SAT) {
				logMsg.appendDiagnostic("ILB_GAP", getGapILB());
			}
		}
		if(heuristics != null && heuristics.hasSearched()) {
			logMsg.appendDiagnostic("HEUR_TIME", heuristics.getTimeCount());
			logMsg.appendDiagnostic("HEUR_ITERATION", heuristics.getIterationCount());
		}
	}


	@Override
	public ResolutionStatus postAnalyzePP() {
		//register diagnostics and config
		final ResolutionStatus r = super.postAnalyzePP();
		if( r == ResolutionStatus.SAT 
				&& objective.intValue() == getComputedLowerBound()) {
			return ResolutionStatus.OPTIMUM;
		}
		return r;
	}


	@Override
	public Solver buildSolver() {
		CPSolver solver = new CPSolver(this.defaultConf);
		BasicSettings.updateTimeLimit(solver.getConfiguration(),  - getPreProcTime());
		return solver;
	}


	@Override
	public Boolean solve() {
		solver.launch();
		return solver.isFeasible();
	}


	protected abstract Object makeSolutionChart();

	protected final void displayChart(Object chart, IVisuManager chartManager) {
		if( chart == null || chartManager == null) {
			LOGGER.config("visu...[chart][FAIL]");
		}else {
			if( defaultConf.readBoolean(BasicSettings.SOLUTION_EXPORT)) {
				chartManager.export(getOutputDirectory(), getInstanceName(), chart);
			} else {
				chartManager.show(chart);
			}
		}
	}

	
	@Override
	public String getValuesMessage() {
		String str = super.getValuesMessage();
		if(str == null && heuristics.existsSolution()) {
			str = heuristics.solutionToString();
		}
		return str;
	}


	@Override
	public void makeReports() {
		super.makeReports();
		if( defaultConf.readBoolean(BasicSettings.SOLUTION_REPORT) ) {
			displayChart(makeSolutionChart(), getChartManager());
		}
	}

}
