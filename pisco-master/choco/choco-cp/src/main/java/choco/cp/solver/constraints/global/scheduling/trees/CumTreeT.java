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

import choco.cp.solver.constraints.global.scheduling.trees.status.ConsumptionStatus;
import choco.kernel.common.opres.graph.IBinaryNode;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.global.scheduling.ICumulativeResource;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.ITask;




/**
 * @author Arnaud Malapert</br> 
 * @since version 2.0.0</br>
 * @version 2.0.3</br>
 */
public final class CumTreeT<T extends ITask> extends AbstractThetaTree {

	protected final ICumulativeResource<T> rsc;
	
	public CumTreeT(ICumulativeResource<T> rsc) {
		super(rsc.asTaskList());
		this.rsc = rsc;
	}

	@Override
	public void insert(final ITask task) {
		insertTask(task, new CumStatusT(NodeType.NIL), new CumStatusT(NodeType.INTERNAL));
	}
	
	
	public long getEnergy() {
		return ( (CumStatusT) getRoot().getNodeStatus()).getStatus().getTime();
	}
	
	

	@Override
	public boolean insertInTheta(ITask task) {
		throw new UnsupportedOperationException("unauthorized operation");
	}

	@Override
	public int getTime() {
		throw new UnsupportedOperationException("energy is given instead of a time.");
	}

	
	@Override
	public void setMode(final TreeMode mode) {
		if(mode.value()) {
		super.setMode(mode);
		}else {
			throw new UnsupportedOperationException("unsupported tree mode:"+mode);
		}
	}


	final class CumStatusT extends AbstractVilimStatus<ConsumptionStatus> implements ThetaTreeLeaf {

		public CumStatusT(final NodeType type) {
			super(type, new ConsumptionStatus());
		}

		public void insertInTheta(final IRTask task) {
			setType(NodeType.THETA);
			final long cons = task.getMinConsumption();
			status.setTime( rsc.getMaxCapacity()*task.getTaskVar().getEST()+cons);
			status.setConsumption(cons);
			
		}
		
		public void insertInTheta() {
			throw new UnsupportedOperationException("cant insert without argument.");
		}

		public void removeFromTheta() {
			setType(NodeType.NIL);
			getStatus().setTime( getResetLongValue(getMode()));
			getStatus().setConsumption(0);
		}

		@Override
		public void reset() {
			removeFromTheta();
		}

		@Override
		protected void writeDotStatus(final StringBuilder buffer) {
			writeRow(buffer, "Energ.", format(status.getTime()),"C", String.valueOf(status.getConsumption()));
		}

		@Override
		public void updateInternalNode(final IBinaryNode node) {
			if(node.getLeftChild().getNodeStatus() instanceof CumTreeT.CumStatusT) {
				final ConsumptionStatus left = ( (CumStatusT) node.getLeftChild().getNodeStatus()).getStatus();
				if(node.getRightChild().getNodeStatus() instanceof CumTreeT.CumStatusT) {
					final ConsumptionStatus right = ( (CumStatusT) node.getRightChild().getNodeStatus()).getStatus();
					this.status.update(getMode(), left, right);
					return;
				}
			}
			throw new SolverException("cant update node");
		}
	}
}
