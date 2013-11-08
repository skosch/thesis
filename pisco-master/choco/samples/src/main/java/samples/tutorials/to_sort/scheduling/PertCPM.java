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

package samples.tutorials.to_sort.scheduling;

import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel;
import choco.cp.model.CPModel;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.preprocessor.PreProcessConfiguration;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.visu.VisuFactory;
import samples.tutorials.PatternExample;

import java.util.logging.Level;

import static choco.Choco.*;


/**
 * The following example is inspired from an example of the Ilog Scheduler userguide.
 * The task network has been slightly modified.
 *
 * @author Arnaud Malapert
 *         Date : 2 déc. 2008
 *         Since : 2.0.1
 *         Update : 2.1.1
 */
public class PertCPM extends PatternExample {


	public final int nbTasks = 10;

	private final int horizon; // sum of durations (default)
	
	private TaskVariable masonry, carpentry, plumbing, ceiling, 
	roofing, painting, windows,facade, garden, moving;

	private IntegerVariable direction;
	
	public PertCPM() {
		this(29);
	}

	
	public PertCPM(int horizon) {
		super();
		this.horizon = horizon;
	}


	@Override
	public void buildModel() {
		model = new CPModel();
		//build tasks
		masonry=makeTaskVar("masonry",horizon, 7);
		carpentry=makeTaskVar("carpentry",horizon, 3);
		plumbing=makeTaskVar("plumbing",horizon, 8);
		ceiling=makeTaskVar("ceiling",horizon, 3);
		roofing=makeTaskVar("roofing",horizon, 1);
		painting=makeTaskVar("painting",horizon, 2);
		windows=makeTaskVar("windows",horizon, 1);
		facade=makeTaskVar("facade",horizon, 2);
		garden=makeTaskVar("garden",horizon, 1);
		moving=makeTaskVar("moving",horizon, 1);
		//add precedence constraints
		model.addConstraints(
				startsAfterEnd(carpentry,masonry),
				startsAfterEnd(plumbing,masonry),
				startsAfterEnd(ceiling,masonry),
				startsAfterEnd(roofing,carpentry),
				startsAfterEnd(roofing,ceiling),
				startsAfterEnd(windows,roofing),
				startsAfterEnd(painting,windows),
				startsAfterEnd(facade,roofing),
				//startsAfterEnd(facade,plumbing), //old version
				startsAfterEnd(garden,roofing),
				startsAfterEnd(garden,plumbing),
				startsAfterEnd(moving,facade),
				startsAfterEnd(moving,garden),
				startsAfterEnd(moving,painting)
		);
		//add a single disjonction
		direction = makeBooleanVar( "b_" + facade.getName() + "_" + plumbing.getName());
		model.addConstraint(
				precedenceDisjoint(facade, plumbing, direction, 1, 3)
		);
	}

	private void showGUI(Object disjMod) {
		VisuFactory.getDotManager().show(disjMod);
	}

	@Override
	public void buildSolver() {
		PreProcessCPSolver s = new PreProcessCPSolver();
		solver = s;
		PreProcessConfiguration.keepSchedulingPreProcess(s);
		s.createMakespan();
		s.read(model);
		showGUI(s.getDisjModel());
	}

	@Override
	public void solve() {
		final IntDomainVar dir = solver.getVar(direction);
		final IntDomainVar makespan = solver.getMakespan();
		final DisjunctiveSModel disjSMod = new DisjunctiveSModel((PreProcessCPSolver) solver);
		try {
			solver.propagate();
			showGUI(disjSMod);
			
			solver.worldPush();
						
			dir.instantiate(1, null, true); //set forward
			solver.propagate();
			makespan.instantiate(makespan.getInf(), null, true); //instantiate makespan 
			solver.propagate(); //compute slack times
			showGUI(disjSMod);
			
			solver.worldPop();
			
			dir.instantiate(0, null, true); //set backward
			solver.propagate();
			makespan.instantiate(makespan.getInf(), null, true); //instantiate makespan and compute slack times
			solver.propagate();
			showGUI(disjSMod);
			
		} catch (ContradictionException e) {
			LOGGER.log(Level.SEVERE, "Pert/CPM should not raise a contradiction !", e);
		}
	}


	@Override
	public void prettyOut() {
		if(LOGGER.isLoggable(Level.INFO)) LOGGER.info( StringUtils.prettyOnePerLine(solver.getTaskVarIterator()));
	}

	public static void main(String[] args) {
		//ChocoLogging.toVerbose();
		(new PertCPM()).execute();
	}
}

