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

import static choco.cp.solver.constraints.global.scheduling.trees.IVilimTree.TreeMode.ECT;
import static choco.cp.solver.constraints.global.scheduling.trees.IVilimTree.TreeMode.LST;
import static choco.kernel.common.util.comparator.TaskComparators.makeLatestStartingTimeCmp;
import static choco.kernel.common.util.comparator.TaskComparators.makeREarliestCompletionTimeCmp;
import static choco.kernel.common.util.comparator.TaskComparators.makeREarliestStartingTimeCmp;
import static choco.kernel.common.util.comparator.TaskComparators.makeRLatestCompletionTimeCmp;
import static choco.kernel.common.util.comparator.TaskComparators.makeReverseEarliestCompletionTimeCmp;
import static choco.kernel.common.util.comparator.TaskComparators.makeReverseREarliestCompletionTimeCmp;
import static choco.kernel.common.util.comparator.TaskComparators.makeReverseRLatestCompletionTimeCmp;

import java.util.Arrays;
import java.util.Comparator;

import choco.cp.solver.constraints.global.scheduling.trees.DisjTreeT;
import choco.cp.solver.constraints.global.scheduling.trees.DisjTreeTL;
import choco.cp.solver.constraints.global.scheduling.trees.IThetaLambdaTree;
import choco.cp.solver.constraints.global.scheduling.trees.IThetaTree;
import choco.cp.solver.constraints.global.scheduling.trees.IVilimTree.TreeMode;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.scheduling.IRMakespan;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.ITask;

/**
 * @author Arnaud Malapert</br>
 * @since version 2.0.0</br>
 * @version 2.0.3</br>
 */
public final class DisjRules extends AbstractDisjRules {

	protected final IBipartiteQueue<IRTask> rqueue;

	protected final IBipartiteQueue<ITask> queue;

	/**
	 * The data structure used for Not-First/Not Last, overload checking and
	 * detectable precedence rules.
	 */
	protected final IThetaTree disjTreeT;

	/** The data structure used for EdgeFinding rule. */
	protected final IThetaLambdaTree disjTreeTL;

	/**
	 * Instantiates a new disjunctive.
	 * 
	 * @param tasks
	 *            the vars the tasks involved in the constraint
	 * @param constraint
	 *            their processing times
	 */
	public DisjRules(IRTask[] rtasks, IRMakespan makespan) {
		super(rtasks, makespan, false);
		ITask[] tasks = getTaskArray();
		this.rqueue = new BipartiteQueue<IRTask>(rtasks);
		this.queue = new BipartiteQueue<ITask>(tasks);
		this.disjTreeT = new DisjTreeT(Arrays.asList(tasks));
		this.disjTreeTL = new DisjTreeTL(Arrays.asList(tasks));
	}

	@Override
	public final boolean isActive() {
		return true;
	}

	@Override
	public void remove(IRTask rtask) {
		throw new UnsupportedOperationException(
				"The resource is not alternative");
	}

	private void setupListsAndTreeT(final Comparator<IRTask> taskComp,
			final Comparator<ITask> queueComp, TreeMode mode) {
		sortQueue(queue, queueComp);
		sortRTasks(taskComp);
		setupMasterTree(disjTreeT, mode);
	}

	// ****************************************************************//
	// ********* Overload checking *************************************//
	// ****************************************************************//

	/**
	 * Overload checking rule.
	 * 
	 * @return <code>true</code> if the resource is overloaded.
	 * @throws ContradictionException
	 */
	public void overloadChecking() throws ContradictionException {
		sortRTasks(makeRLatestCompletionTimeCmp());
		setupMasterTree(disjTreeT, ECT);
		for (IRTask t : rtasks) {
			final ITask i = t.getTaskVar();
			disjTreeT.insertInTheta(i);
			if (disjTreeT.getTime() > i.getLCT()) {
				t.fail();
			}
		}
		setMakespanLB(disjTreeT);
	}

	// ****************************************************************//
	// ********* NotFirst/NotLast *************************************//
	// ****************************************************************//

	@Override
	public boolean notFirst() throws ContradictionException {
		setupListsAndTreeT(makeReverseREarliestCompletionTimeCmp(),
				makeReverseEarliestCompletionTimeCmp(), LST);
		ITask j = null;
		for (IRTask rti : rtasks) {
			final ITask i = rti.getTaskVar();
			while (!queue.isEmpty() && i.getEST() < queue.peek().getECT()) {
				j = queue.poll();
				disjTreeT.insertInTheta(j);
			}
			final boolean rm = disjTreeT.removeFromTheta(i);
			if (disjTreeT.getTime() < i.getECT()) {
				updateManager.storeUpdate(rti, j.getECT());
			}
			if (rm) {
				disjTreeT.insertInTheta(i);
			}

		}
		return updateManager.updateEST();
	}

	/**
	 * NotLast rule.
	 * 
	 * @throws ContradictionException
	 * 
	 */
	@Override
	public boolean notLast() throws ContradictionException {
		setupListsAndTreeT(makeRLatestCompletionTimeCmp(),
				makeLatestStartingTimeCmp(), ECT);
		ITask j = null;
		for (IRTask rti : rtasks) {
			final ITask i = rti.getTaskVar();
			// update tree
			while (!queue.isEmpty() && i.getLCT() > queue.peek().getLST()) {
				j = queue.poll();
				disjTreeT.insertInTheta(j);
			}
			// compute pruning
			disjTreeT.removeFromTheta(i);
			if (disjTreeT.getTime() > i.getLST()) {
				updateManager.storeUpdate(rti, j.getLST());
			}
			disjTreeT.insertInTheta(i);
		}
		setMakespanLB(disjTreeT);
		return updateManager.updateLCT();
	}

	// //****************************************************************//
	// //********* detectable Precedence*********************************//
	// //****************************************************************//
	/**
	 * DetectablePrecedence rule.
	 * 
	 * @throws ContradictionException
	 * 
	 */
	@Override
	public boolean detectablePrecedenceEST() throws ContradictionException {
		setupListsAndTreeT(makeREarliestCompletionTimeCmp(),
				makeLatestStartingTimeCmp(), ECT);
		for (IRTask rti : rtasks) {
			final ITask i = rti.getTaskVar();
			while (!queue.isEmpty() && i.getECT() > queue.peek().getLST()) {
				disjTreeT.insertInTheta(queue.poll());
			}
			final boolean rm = disjTreeT.removeFromTheta(i);
			updateManager.storeUpdate(rti, disjTreeT.getTime());
			if (rm) {
				disjTreeT.insertInTheta(i);
			}
		}
		setMakespanLB(disjTreeT);
		return updateManager.updateEST();
	}

	/**
	 * symmetric DetectablePrecedence rule.
	 * 
	 * @throws ContradictionException
	 * 
	 */
	@Override
	public boolean detectablePrecedenceLCT() throws ContradictionException {
		setupListsAndTreeT(makeReverseRLatestCompletionTimeCmp(),
				makeReverseEarliestCompletionTimeCmp(), LST);
		for (IRTask rti : rtasks) {
			final ITask i = rti.getTaskVar();
			while (!queue.isEmpty() && i.getLCT() <= queue.peek().getECT()) {
				disjTreeT.insertInTheta(queue.poll());
			}
			final boolean rm = disjTreeT.removeFromTheta(i);
			updateManager.storeUpdate(rti, disjTreeT.getTime());
			// be sure that i was active in disjTreeT
			if (rm) {
				disjTreeT.insertInTheta(i);
			}
		}
		return updateManager.updateLCT();

	}

	// //****************************************************************//
	// //********* Edge Finding *****************************************//
	// //****************************************************************//

	/**
	 * EdgeFinding rule.
	 * 
	 * @throws ContradictionException
	 * 
	 */
	@Override
	public boolean edgeFindingEST() throws ContradictionException {
		sortQueue(rqueue, makeReverseRLatestCompletionTimeCmp());
		this.disjTreeTL.setMode(ECT);
		setMakespanLB(disjTreeTL);
		IRTask rtj = rqueue.peek();
		ITask j = rtj.getTaskVar();
		if (disjTreeTL.getTime() > j.getLCT()) {
			rtj.fail();
		}// erreur pseudo-code papier sinon on ne traite pas la tete de la queue
		do {
			rqueue.poll();
			if (rtj.isRegular()) {
				disjTreeTL.removeFromThetaAndInsertInLambda(rtj);
				if (!rqueue.isEmpty()) {
					rtj = rqueue.peek();
					j = rtj.getTaskVar();
				} else {
					break;
				}
				if (disjTreeTL.getTime() > j.getLCT()) {
					rtj.fail();
				}
				while (disjTreeTL.getGrayTime() > j.getLCT()) {
					final IRTask rti = (IRTask) disjTreeTL.getResponsibleTask();
					final ITask i = rti.getTaskVar();
					if (disjTreeTL.getTime() > i.getEST()) {
						updateManager.storeUpdate(rti, disjTreeTL.getTime());
					}
					disjTreeTL.removeFromLambda(i);
				}
			}
		} while (!rqueue.isEmpty());
		return updateManager.updateEST();
	}

	/**
	 * symmetric EdgeFinding rule.
	 * 
	 * @throws ContradictionException
	 * 
	 */
	@Override
	public boolean edgeFindingLCT() throws ContradictionException {
		sortQueue(rqueue, makeREarliestStartingTimeCmp());
		disjTreeTL.setMode(LST);
		IRTask rtj = rqueue.peek();
		ITask j = rtj.getTaskVar();
		if (disjTreeTL.getTime() < j.getEST()) {
			rtj.fail();
		}
		do {
			rqueue.poll();
			if (rtj.isRegular()) {
				disjTreeTL.removeFromThetaAndInsertInLambda(rtj);
				if (!rqueue.isEmpty()) {
					rtj = rqueue.peek();
					j = rtj.getTaskVar();
				} else {
					break;
				}
				if (disjTreeTL.getTime() < j.getEST()) {
					rtj.fail();
				}
				while (disjTreeTL.getGrayTime() < j.getEST()) {
					final IRTask rti = (IRTask) disjTreeTL.getResponsibleTask();
					final ITask i = rti.getTaskVar();
					updateManager.storeUpdate(rti, disjTreeTL.getTime());
					disjTreeTL.removeFromLambda(i);
				}
			}
		} while (!rqueue.isEmpty());
		return updateManager.updateLCT();
	}

}

final class BipartiteQueue<E> implements IBipartiteQueue<E> {

	private final E[] elementData;

	private int level;

	public BipartiteQueue(E[] elementData) {
		super();
		this.elementData = Arrays.copyOf(elementData, elementData.length);
		this.reset();
	}

	@Override
	public void reset() {
		level = 0;
	}

	@Override
	public boolean isEmpty() {
		return level == elementData.length;
	}

	@Override
	public E poll() {
		return elementData[level++];
	}

	@Override
	public E peek() {
		return elementData[level];
	}

	@Override
	public void sort(Comparator<? super E> cmp) {
		Arrays.sort(elementData, level, elementData.length, cmp);
	}

}
