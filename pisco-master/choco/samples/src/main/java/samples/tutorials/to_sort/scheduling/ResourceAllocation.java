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

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.BranchingFactory;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valselector.MaxVal;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.common.util.tools.PermutationUtils;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ResolutionPolicy;
import choco.kernel.solver.variables.integer.IntDomainVar;
import samples.tutorials.PatternExample;

import java.util.Arrays;

import static choco.Choco.*;

/**
 * The aim is to maximize the benefits associated with the presence of schedule-based activities, in
 * which an activity may be omitted from the final schedule (e.g. due to different plans being selected).
 * The time windows of tasks are known by advance and
 * they must be processed by a disjunctive machine and a non renewable ressource.
 * A cost (benefits) is associated with the presence of the task in the final schedule.
 *
 * @author Arnaud Malapert</br>
 * @version 2.1.1</br>
 * @since 6 juil. 2010 version 2.1.1</br>
 */
public class ResourceAllocation extends PatternExample {

	//Task are scheduled within given time windows
	public int[][] timeWindows = {{0, 4}, {3, 5}, {4, 8}, {4, 8}, {8, 12}};
	//Benefits related tothe presence of the task in the final schedule
	public int[] durations = {3, 1, 3, 2, 3};
	public TaskVariable[] operationRequests;

	//Indicate if a task belongs to the final schedule
	protected IntegerVariable[] usages;
	//Benefits related tothe presence of the task in the final schedule
	protected int[] costs = {7, 10, 4, 8, 7};

	//Non renewable resource
	protected int[] sizes = {3, 4, 4, 8, 6};
	protected int capacity = 13;

	protected IntegerVariable objective;


	@Override
	public void buildModel() {
		model = new CPModel();
		//define variables
		final int n = timeWindows.length;
		operationRequests = new TaskVariable[n];
		for (int i = 0; i < n; i++) {
			operationRequests[i] = makeTaskVar("OR-" + i, timeWindows[i][0], timeWindows[i][1], durations[i]);
		}
		usages = makeBooleanVarArray("usage", n);
		objective = makeIntVar("objective", 0, MathUtils.sum(costs), Options.V_ENUM, Options.V_OBJECTIVE, Options.V_NO_DECISION);
		//Tasks does not overlap in the final schedule
		model.addConstraints(
				disjunctive(operationRequests, usages),//non-overlaping
				leq(scalar(sizes, usages), capacity), //non renewable resource
				eq(scalar(costs, usages), objective) //objective
		);
		//schedule non allocated task at the origin (dirty, should be integrated within branching)
		for (int i = 0; i < n; i++) {
			model.addConstraints(Choco.implies(eq(usages[i], 0), eq(operationRequests[i].start(), operationRequests[i].start().getLowB())));
		}
	}


	@Override
	public void buildSolver() {
		solver = new CPSolver();
		solver.getConfiguration().putFalse(Configuration.STOP_AT_FIRST_SOLUTION);
		solver.getConfiguration().putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MAXIMIZE);
		solver.read(model);
		//search heuristics: schedule the remaining operation request with highest cost.
		final IntDomainVar[] sortedUsages = new IntDomainVar[operationRequests.length];
		PermutationUtils.getSortingPermuation(costs, true).applyPermutation(solver.getVar(usages), sortedUsages);
		solver.attachGoal(new AssignVar(new StaticVarOrder(solver, sortedUsages), new MaxVal()));
		solver.addGoal(BranchingFactory.setTimes(solver));
	}

	@Override
	public void prettyOut() {
		//LOGGER.info(solver.pretty());
		LOGGER.info(Arrays.toString(solver.getVar(usages)));


	}

	@Override
	public void solve() {
		solver.generateSearchStrategy();
		solver.launch();
	}

	public static void main(String[] args) {
		(new ResourceAllocation()).execute(args);
	}

}
