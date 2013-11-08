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

package choco.cp.solver.constraints.global.scheduling.disjunctive;

import java.util.Arrays;
import java.util.Comparator;

import choco.cp.solver.constraints.global.scheduling.trees.IThetaLambdaTree;
import choco.cp.solver.constraints.global.scheduling.trees.IThetaOmegaTree;
import choco.cp.solver.constraints.global.scheduling.trees.IThetaTree;
import choco.cp.solver.constraints.global.scheduling.trees.IVilimTree;
import choco.cp.solver.constraints.global.scheduling.trees.IVilimTree.TreeMode;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.scheduling.IRMakespan;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.ITask;


/**
 * @author Arnaud Malapert</br> 
 * @since version 2.0.3</br>
 * @version 2.0.3</br>
 */
public abstract class AbstractDisjRules implements IDisjRules {

	protected final IRTask[] rtasks;
	
	protected final UpdateManager updateManager;

	protected final IRMakespan makespan;

	private TreeMode state;

	public AbstractDisjRules(IRTask[] rtasks, IRMakespan makespan, boolean enableRemove) {
		super();
		this.rtasks = Arrays.copyOf(rtasks, rtasks.length);
		this.makespan = makespan;
		this.updateManager = new UpdateManager(this, rtasks.length, enableRemove);
		fireDomainChanged();
	}


	protected final ITask[] getTaskArray() {
		final ITask[] tasks = new ITask[rtasks.length];
		for (int i = 0; i < tasks.length; i++) {
			tasks[i] = rtasks[i].getHTask();
		}
		return tasks;
	}


	@Override
	public void initialize() {
		updateManager.clear();
		fireDomainChanged();

	}


	@Override
	public void fireDomainChanged() {
		state = null;
	}

	protected void sortRTasks(Comparator<IRTask> cmp) {
		Arrays.sort(rtasks, cmp);
	}

	protected final static <E> void sortQueue(IBipartiteQueue<E> queue, Comparator<E> cmp) {
		queue.reset();
		queue.sort(cmp);
	}


	protected final void setupMasterTree(IVilimTree tree,TreeMode mode) {
		if(state == mode) {
			tree.reset();
		}else {
			state = mode;
			tree.setMode(state);
		}
	}

	protected final void setMakespanLB(final IThetaTree tree) throws ContradictionException {
		makespan.updateInf(tree.getTime());
	}


	@Override
	public final boolean detectablePrecedence() throws ContradictionException {
		return detectablePrecedenceEST() | detectablePrecedenceLCT() ;
	}

	@Override
	public final boolean edgeFinding() throws ContradictionException {
		return edgeFindingEST() | edgeFindingLCT();
	}

	@Override
	public final boolean notFirstNotLast() throws ContradictionException {
		return notFirst() | notLast();
	}

}

interface IBipartiteQueue<E> {

	void reset();

	boolean isEmpty();

	E poll();

	E peek();

	void sort(Comparator<? super E> cmp);
}


final class UpdateManager {

	public final IDisjRules rules;

	private final IRTask[] updateL;

	private int updateCount;

	private final IRTask[] removeL;

	private int removeCount;

	protected UpdateManager(IDisjRules rules, int capacity, boolean enableRemove) {
		super();
		this.rules = rules;
		removeL = enableRemove ? new IRTask[capacity] : null;
		updateL = new IRTask[capacity];
	}

	public void clear() {
		Arrays.fill(updateL, null);
		updateCount=0;
		if(removeL != null) {
			Arrays.fill(removeL, null);
			removeCount = 0;
		}
	}

	public void storeUpdate(IRTask t, int value) {
		t.storeValue(value);
		updateL[updateCount++]=t;
	}

	public void storeRemoval(IRTask t) throws ContradictionException {
		t.remove();
		assert t.isEliminated();
		removeL[removeCount++]=t;
	}

	public void storeLambdaRemoval(IThetaLambdaTree tree) throws ContradictionException {
		final IRTask t = (IRTask) tree.getResponsibleTask();
		assert t != null;
		storeRemoval(t);
		tree.removeFromLambda(t.getHTask());
	}
	
	public void storeLambdaRemoval(IRTask t, IThetaLambdaTree tree) throws ContradictionException {
		storeRemoval(t);
		tree.removeFromLambda(t.getHTask());
	}

	public void storeOmegaRemoval(IRTask t, IThetaOmegaTree tree) throws ContradictionException {
		storeRemoval(t);
		tree.removeFromOmega(t);
	}



	public final void fireRemovals() {
		if(removeCount > 0) {
			for (int i = 0; i < removeCount; i++) {
				removeL[i].fireRemoval();
			}
			removeCount=0;
			rules.fireDomainChanged();
		}
	}


	public boolean updateEST() throws ContradictionException {
		if(updateCount > 0) {
			boolean noFixPoint=false;
			for (int i = 0; i < updateCount; i++) {
				if(!updateL[i].isEliminated())
					//applying updates only to regular, and optional tasks
					noFixPoint |= updateL[i].updateEST();
			}
			updateCount=0;
			if(noFixPoint) rules.fireDomainChanged();
			return noFixPoint;
		}else return false;
	}

	public boolean fireAndUpdateEST() throws ContradictionException {
		fireRemovals();
		return updateEST();
	}

	public boolean updateLCT() throws ContradictionException {
		if(updateCount > 0) {
			boolean noFixPoint=false;
			for (int i = 0; i < updateCount; i++) {
				if(!updateL[i].isEliminated())
					//applying updates only to regular, and optional tasks
					noFixPoint |= updateL[i].updateLCT();
			}
			updateCount=0;
			if(noFixPoint) rules.fireDomainChanged();
			return noFixPoint;
		}else return false;
	}

	public boolean fireAndUpdateLCT() throws ContradictionException {
		fireRemovals();
		return updateLCT();
	}

}


