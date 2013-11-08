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

package choco.cp.solver.constraints.global.scheduling;

import choco.Choco;
import choco.cp.solver.constraints.integer.bool.sum.BoolSumStructure;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.global.scheduling.AbstractTaskSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.TaskVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public abstract class AbstractUseResourcesSConstraint extends AbstractTaskSConstraint {

	private final IRTask[] rtasks;

	protected final BoolSumStructure boolSumS;

	protected static final int BOOL_OFFSET = 3;
	
	private static final int TASK_IDX = 0;

	public AbstractUseResourcesSConstraint(IEnvironment environment, TaskVar taskvar, int k, IntDomainVar[] usages, IRTask[] rtasks) {
		super(new TaskVar[]{taskvar}, usages);
		this.rtasks = rtasks;
		this.boolSumS = new BoolSumStructure(environment, this, usages,k);
	}	


	@Override
	public int getFilteredEventMask(int idx) {
		//listen only usage inst. (ignore changes on the real domain of the task)
		return idx < BOOL_OFFSET ? 0 : IntVarEvent.INSTINT_MASK;
	}

	@Override
	public void propagate() throws ContradictionException {
		boolSumS.reset();
		for (int i = BOOL_OFFSET; i < vars.length; i++) {
			if (vars[i].isInstantiated()) {
				awakeOnInst(i);
			}
		}
		filterHypotheticalDomains();
	}

	public void filterHypotheticalDomains() throws ContradictionException {
		if( boolSumS.nbo.get() < boolSumS.bValue) {
			final int k = boolSumS.bValue - boolSumS.nbo.get();
			if(k > 1) {
				filterEarliestStartingTime(k);
				filterLatestCompletionTime(k);
			}else {
				assert k == 1;
				filterEarliestStartingTime();
				filterLatestCompletionTime();
			}
		}
	}


	protected final void filterLatestCompletionTime() throws ContradictionException {
		int maxLctI = Choco.MIN_LOWER_BOUND;
		for (int i = 0; i < rtasks.length; i++) {
			if(rtasks[i].isOptional()) {
				final int lctI= rtasks[i].getHTask().getLCT(); 
				if(lctI > maxLctI) maxLctI = lctI;
			}
		}
		if(maxLctI > Choco.MIN_LOWER_BOUND && maxLctI < taskvars[TASK_IDX].getLCT()) {
			vars[getEndIndex(TASK_IDX)].updateSup(maxLctI, this,false);
		}
	}

	protected final void filterLatestCompletionTime(int k) throws ContradictionException {
		ArrayList <Integer> lctOp = new ArrayList<Integer>(rtasks.length);
		for(IRTask r: rtasks){
			if(r.isOptional())
				lctOp.add(r.getHTask().getLCT());
		}
		Comparator<Integer> c = Collections.reverseOrder();
		Collections.sort(lctOp, c);
		assert k <= lctOp.size();
		final int kMaxLCT = lctOp.get(k-1);

		if(kMaxLCT < taskvars[TASK_IDX].getLCT()){
			vars[getEndIndex(TASK_IDX)].updateSup(kMaxLCT, this, false);
		}
	}

	private void filterEarliestStartingTime() throws ContradictionException {
		int minEstI = Choco.MAX_UPPER_BOUND;
		for (int i = 0; i < rtasks.length; i++) {
			if(rtasks[i].isOptional()) {
				final int estI= rtasks[i].getHTask().getEST(); 
				if(estI < minEstI) 
					minEstI = estI;
			}
		}
		if(minEstI < Choco.MAX_UPPER_BOUND && minEstI > taskvars[TASK_IDX].getEST()) {
			vars[getStartIndex(TASK_IDX)].updateInf(minEstI, this,false);
		}
	}


	protected void filterEarliestStartingTime(int k) throws ContradictionException {
		ArrayList<Integer> estOp = new ArrayList<Integer>(rtasks.length);
		for(IRTask r: rtasks){
			if(r.isOptional())
				estOp.add(r.getHTask().getEST());
		}
		//sorting EST in ascending order
		Collections.sort(estOp);
		//get k-th minimum
		assert k <= estOp.size();
		final int kMinEST = estOp.get(k-1);
		if(kMinEST > taskvars[TASK_IDX].getEST()){
			vars[getStartIndex(TASK_IDX)].updateInf(kMinEST, this, false);
		}
	}

	
	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		assert idx >= BOOL_OFFSET;
		final int val = vars[idx].getVal();
		if (val == 0) {
			boolSumS.addZero();
			filterHypotheticalDomains();
		}
		else boolSumS.addOne();
	}
	
}
