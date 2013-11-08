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

import java.util.List;

import choco.cp.solver.constraints.global.scheduling.trees.status.ThetaLambdaStatus;
import choco.kernel.common.opres.graph.IBinaryNode;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.ITask;



/**
 * @author Arnaud Malapert</br> 
 * @since version 2.0.0</br>
 * @version 2.0.3</br>
 */
public class DisjTreeTL extends AbstractThetaTree implements IThetaLambdaTree {

	
	
	public DisjTreeTL(List<? extends ITask> tasks) {
		super(tasks);
	}

	protected final ThetaLambdaStatus getRootStatus() {
		return getNodeStatus(getRoot()).getStatus();
	}
	protected final DisjStatusTL getNodeStatus(IBinaryNode node) {
		return (DisjStatusTL) node.getNodeStatus();
	}

	@Override
	public final int getGrayTime() {
		return getRootStatus().getGrayTime();
	}

	@Override
	public final Object getResponsibleTask() {
		return getRootStatus().getRespGrayTime();
	}

	
	@Override
	public final boolean insertInLambda(IRTask rtask) {
		final IBinaryNode leaf = getLeaf(rtask.getHTask());
		final DisjStatusTL status =  getNodeStatus(leaf);
		if(status.getType() == AbstractVilimTree.NodeType.NIL) {
			status.insertInLambda(rtask);
			leaf.fireStatusChanged();
			return true;
		}
		return false;
	}

	@Override
	public final boolean removeFromLambda(ITask task) {
		final IBinaryNode leaf = getLeaf(task);
		final DisjStatusTL status =  getNodeStatus(leaf);
		if(status.getType() == AbstractVilimTree.NodeType.LAMBDA) {
			status.removeFromLambda();
			leaf.fireStatusChanged();
			return true;
		}
		return false;
	}

	private boolean removeFromThetaAndInsertInLambda(ITask task, Object resp) {
		final IBinaryNode leaf = getLeaf(task);
		final DisjStatusTL status =  getNodeStatus(leaf);
		if(status.getType() == AbstractVilimTree.NodeType.THETA) {
			status.insertInLambda(resp);
			leaf.fireStatusChanged();
			return true;
		}
		return false;
	}
	
	@Override
	public final boolean removeFromThetaAndInsertInLambda(ITask task) {
		return this.removeFromThetaAndInsertInLambda(task, task);
	}
	
	@Override
	public final boolean removeFromThetaAndInsertInLambda(IRTask rtask) {
		return removeFromThetaAndInsertInLambda(rtask.getHTask(), rtask);
	}

	@Override
	public final int getTime() {
		return getRootStatus().getTime();

	}

	@Override
	public void insert(ITask task) {
		insertTask(task, new DisjStatusTL(NodeType.NIL), new DisjStatusTL(NodeType.INTERNAL));
	}



	
	protected class DisjStatusTL extends AbstractVilimStatus<ThetaLambdaStatus> implements ThetaTreeLeaf {

		public DisjStatusTL(NodeType type) {
			super(type, new ThetaLambdaStatus());
		}
		
		
		@Override
		public final void insertInTheta(IRTask rtask) {
			insertInTheta();
		}

		protected final int getLeafTime() {
			return getMode().value() ? task.getEST()+task.getMinDuration()  : task.getLCT()-task.getMinDuration();
		}
		
		@Override
		public final void removeFromTheta() {
			setType(NodeType.NIL);
			status.setTime( getResetIntValue(getMode()));
			status.setDuration(0);
			status.setGrayTime( getResetIntValue(getMode()));
			status.setGrayDuration(0);
		}


		public final void insertInTheta() {
			setType(NodeType.THETA);
			getStatus().setTime( getLeafTime());
			getStatus().setDuration(task.getMinDuration());
			getStatus().setGrayTime( getStatus().getTime());
			getStatus().setGrayDuration( getStatus().getDuration());
		}

		public final void insertInLambda(Object resp) {
			setType(NodeType.LAMBDA);
			status.setTime( getResetIntValue(getMode()));
			status.setDuration(0);
			getStatus().setGrayTime( getLeafTime());
			getStatus().setGrayDuration( task.getMinDuration());
			status.setRespGrayTime(resp);
			status.setRespGrayDuration(resp);
		}

		public final void removeFromLambda() {
			setType(NodeType.NIL);
			status.setGrayTime( getResetIntValue(getMode()));
			status.setGrayDuration(0);
			status.setRespGrayTime(null);
			status.setRespGrayDuration(null);
			
		}

		
		@Override
		public void reset() {
			this.insertInTheta();			
		}


		@Override
		public final void setTask(ITask task) {
			super.setTask(task);
		}

		@Override
		protected final void writeDotStatus(StringBuilder buffer) {
			writeRow(buffer, getMode().label(), format(status.getTime()),"P", String.valueOf(status.getDuration()));
			buffer.append('|');
			writeRow(buffer,"Gr" + getMode().label(), format(status.getGrayTime()),"GrP", String.valueOf(status.getGrayDuration()));
			if(getType() == NodeType.INTERNAL) {
				buffer.append('|');
				final String t1 =  status.getRespGrayTime() == null ? "?": status.getRespGrayTime().toString();
				final String t2 =  status.getRespGrayDuration() == null ? "?": status.getRespGrayDuration().toString();
				writeRow(buffer,"Gr" + getMode().label(), t1,"GrP", t2);
			}
		}

		@Override
		public void updateInternalNode(IBinaryNode node) {
			if(node.getLeftChild().getNodeStatus() instanceof DisjStatusTL) {
				final ThetaLambdaStatus left = ( (DisjStatusTL) node.getLeftChild().getNodeStatus()).getStatus();
				if(node.getRightChild().getNodeStatus() instanceof DisjStatusTL) {
					final ThetaLambdaStatus right = ( (DisjStatusTL) node.getRightChild().getNodeStatus()).getStatus();
					this.status.update(getMode(), left, right);
					return;
				}
			}
			throw new SolverException("cant update node");
		}
	}
}
