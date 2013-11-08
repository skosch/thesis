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

package choco.cp.solver.constraints.global.scheduling.trees;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import choco.kernel.common.opres.graph.IBinaryNode;
import choco.kernel.common.opres.graph.ProperBinaryTree;
import choco.kernel.common.util.comparator.TaskComparators;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.scheduling.ITask;




/**
 * @author Arnaud Malapert</br> 
 * @since 9 févr. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public abstract class AbstractVilimTree extends ProperBinaryTree implements IVilimTree {

	enum NodeType {THETA, LAMBDA, OMEGA, NIL, INTERNAL}

	private TreeMode mode;

	private final Map<ITask, IBinaryNode> map;

	public AbstractVilimTree(List<? extends ITask> tasks) {
		this.map = new HashMap<ITask, IBinaryNode>(tasks.size());
		for (ITask task : tasks) {
			insert(task);
		}
	}

	protected  Comparator<ITask> getTaskComparator() {
		switch(mode) {
		case ECT : return TaskComparators.makeEarliestStartingTimeCmp(); //EST
		case LST : return TaskComparators.makeLatestCompletionTimeCmp(); //LCT
		default : return null;
		}
	}

	protected IBinaryNode getLeaf(ITask task) {
		return map.get(task);
	}

	protected void insertTask(ITask task, AbstractVilimStatus<?> leafStatus, AbstractVilimStatus<?> internalStatus) {
		leafStatus.setTask(task);
		IBinaryNode leaf = insert(leafStatus, internalStatus, false);
		map.put(task, leaf);		
		
	}

	public void reset() {
		for (IBinaryNode leaf : map.values()) {
			( (AbstractVilimStatus<?>) leaf.getNodeStatus() ).reset();
		}
		this.fireTreeChanged();
	}



	protected void applySort(IBinaryNode current, ListIterator<ITask> iter) {
		if(current.isLeaf()) {
			final ITask t = iter.next();
			AbstractVilimStatus<?> s = (AbstractVilimStatus<?>) current.getNodeStatus();
			s.setTask(t);
			s.reset();
			map.put(t, current);
		}else {
			applySort(current.getLeftChild(), iter);
			applySort(current.getRightChild(), iter);
			current.getNodeStatus().updateInternalNode(current);
		}
	}

	public void sort() {
		if(getNbLeaves()>1) {
			//TODO store array to avoid memory issue ? need to check size at each creation FIXME -  Temporary : waiting for task event management - created 4 juil. 2011 by Arnaud Malapert
			final ITask[] tmp = map.keySet().toArray(new ITask[map.keySet().size()]);
			Arrays.sort(tmp, getTaskComparator());
			map.clear();
			// TODO - Do not clear the map - created 4 juil. 2011 by Arnaud Malapert

			final ListIterator<ITask> iter = Arrays.asList(tmp).listIterator();
			applySort(getRoot(), iter);
			if(iter.hasNext()) {
				throw new SolverException("inconsistent vilim tree");
			}
		}else {
			//no need to sort, reset only the root node
			 ((AbstractVilimStatus<?>) getRoot().getNodeStatus()).reset();
		}
	}


	
	public final TreeMode getMode() {
		return mode;
	}

	@Override
	public void setMode(TreeMode mode) {
		this.mode = mode;
		sort();
	}


	@Override
	public boolean contains(ITask task) {
		return map.containsKey(task);
	}

	@Override
	public void remove(ITask task) {
		if(map.containsKey(task)) {
			remove(map.remove(task), false);
		}
	}


	@Override
	public String toDotty() {
		return getRoot().toDotty();
	}
	

}
