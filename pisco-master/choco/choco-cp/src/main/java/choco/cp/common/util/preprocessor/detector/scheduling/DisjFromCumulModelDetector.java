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

package choco.cp.common.util.preprocessor.detector.scheduling;

import java.util.ArrayList;
import java.util.List;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;


public final class DisjFromCumulModelDetector extends AbstractRscDetector {


	private List<TaskVariable> tasks;
	private List<IntegerVariable> usages;

	public DisjFromCumulModelDetector(CPModel model) {
		super(model, null);
	}

	@Override
	protected ConstraintType getType() {
		return ConstraintType.CUMULATIVE;
	}

	@Override
	protected void setUp() {
		super.setUp();
		tasks = new ArrayList<TaskVariable>();
		usages = new ArrayList<IntegerVariable>();
	}


	@Override
	protected void tearDown() {
		super.tearDown();
		tasks = null;
		usages = null;
	}

	private void addTask(PPResource rsc, int idx) {
		tasks.add(rsc.getTask(idx));
		if(idx >= rsc.getParameters().getNbRegularTasks()) {
			usages.add(rsc.getUsage(idx));
		}
	}

	private TaskVariable[] tasks() {
		return tasks.toArray(new TaskVariable[tasks.size()]);	
	}

	private IntegerVariable[] usages() {
		return usages.toArray(new IntegerVariable[usages.size()]);	
	}


	@Override
	protected void apply(PPResource ppr) {
		tasks.clear();usages.clear();
		final int capa = ppr.getMaxCapa();
		//if the capacity is even, then we can add at most one task t1 such that h1 == limit.
		//Indeed, for all other task t2, h1 + h2 >= limit + (limit+1) > 2*limit = capa. 
		boolean addExtraTask = capa % 2 == 0;
		final int limit = capa/2;
		final int n = ppr.getParameters().getNbTasks();
		for (int i = 0; i < n; i++) {
			final int h = ppr.getMinHeight(i);
			if( h > limit) {
				addTask(ppr, i);
			} else if ( addExtraTask && h == limit) {
				addTask(ppr, i);
				addExtraTask = false;
			}else if( h < 0) {
				//the height is negative (producer tasks): cancel disjunction.
				tasks.clear();usages.clear();
				return;
			}
		}
		final int nc = tasks.size();
		if( nc > 2) {
			if( usages.isEmpty()) add(Choco.disjunctive(tasks(),Options.C_NO_DETECTION));
			else add(Choco.disjunctive(tasks(), usages(), Options.C_NO_DETECTION));
			if( nc == ppr.getParameters().getNbTasks()) {
				delete(ppr.getConstraint());
			}	
		}
	}
}
