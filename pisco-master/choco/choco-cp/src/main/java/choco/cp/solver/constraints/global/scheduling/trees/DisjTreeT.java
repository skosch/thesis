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

import choco.cp.solver.constraints.global.scheduling.trees.AbstractVilimTree.NodeType;
import choco.cp.solver.constraints.global.scheduling.trees.status.ThetaStatus;
import choco.kernel.common.opres.graph.IBinaryNode;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.ITask;

import java.util.List;


interface ThetaTreeLeaf {

	NodeType getType();

	void insertInTheta();

	void insertInTheta(IRTask rtask);

	void removeFromTheta();
}

abstract class AbstractThetaTree extends AbstractVilimTree implements IThetaTree {

	public AbstractThetaTree(final List<? extends ITask> tasks) {
		super(tasks);
	}


	private ThetaTreeLeaf getLeafStatus(final IBinaryNode node) {
		return  (ThetaTreeLeaf) node.getNodeStatus();
	}


	@Override
	public boolean insertInTheta(final ITask task) {
		final IBinaryNode leaf = getLeaf(task);
		final ThetaTreeLeaf status = getLeafStatus(leaf);
		if(status.getType() == AbstractVilimTree.NodeType.NIL) {
			status.insertInTheta();
			leaf.fireStatusChanged();
			return true;
		}
		return false;
	}



	@Override
	public final boolean insertInTheta(final IRTask rtask) {
		final IBinaryNode leaf = getLeaf(rtask.getHTask());
		final ThetaTreeLeaf status = getLeafStatus(leaf);
		if(status.getType() == AbstractVilimTree.NodeType.NIL) {
			status.insertInTheta(rtask);
			leaf.fireStatusChanged();
			return true;
		}
		return false;
	}


	@Override
	public final boolean removeFromTheta(final ITask task) {
		final IBinaryNode leaf = getLeaf(task);
		final ThetaTreeLeaf status = getLeafStatus(leaf);
		if(status.getType() == AbstractVilimTree.NodeType.THETA) {
			status.removeFromTheta();
			leaf.fireStatusChanged();
			return true;
		}
		return false;
	}
}


/**
 * @author Arnaud Malapert</br> 
 * @since version 2.0.0</br>
 * @version 2.0.3</br>
 */
public final class DisjTreeT extends AbstractThetaTree {

	public DisjTreeT(final List<? extends ITask> tasks) {
		super(tasks);
	}

	@Override
	public void insert(final ITask task) {
		insertTask(task, new DisjStatusT(NodeType.NIL), new DisjStatusT(NodeType.INTERNAL));
	}

	@Override
	public int getTime() {
		return ( (DisjStatusT) getRoot().getNodeStatus()).getStatus().getTime();
	}


	final class DisjStatusT extends AbstractVilimStatus<ThetaStatus> implements ThetaTreeLeaf {

		public DisjStatusT(final NodeType type) {
			super(type, new ThetaStatus());
		}

		public void insertInTheta() {
			setType(NodeType.THETA);
			getStatus().setTime( getMode().value() ? task.getEST()+task.getMinDuration()  : task.getLCT()-task.getMinDuration());
			getStatus().setDuration(task.getMinDuration());
		}


		@Override
		public void insertInTheta(final IRTask rtask) {
			this.insertInTheta();			
		}

		public void removeFromTheta() {
			setType(NodeType.NIL);
			getStatus().setTime( getResetIntValue(getMode()));
			getStatus().setDuration(0);
		}

		@Override
		public void reset() {
			removeFromTheta();
		}

		@Override
		protected void writeDotStatus(final StringBuilder buffer) {
			writeRow(buffer, getMode().label(), format(status.getTime()),"P", String.valueOf(status.getDuration()));
		}

		@Override
		public void updateInternalNode(final IBinaryNode node) {
			if(node.getLeftChild().getNodeStatus() instanceof DisjStatusT) {
				final ThetaStatus left = ( (DisjStatusT) node.getLeftChild().getNodeStatus()).getStatus();
				if(node.getRightChild().getNodeStatus() instanceof DisjStatusT) {
					final ThetaStatus right = ( (DisjStatusT) node.getRightChild().getNodeStatus()).getStatus();
					this.status.update(getMode(), left, right);
					return;
				}
			}
			throw new SolverException("cant update node");
		}
	}
}