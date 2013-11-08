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

import choco.cp.solver.constraints.global.scheduling.trees.AltDisjTreeTLTO.AltDisjStatusTLTO;
import choco.kernel.common.opres.graph.IBinaryNode;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.ITask;

/**
 * @author Arnaud Malapert</br> 
 * @since 2 mars 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public final class AltDisjTreeTL extends DisjTreeTL {

	
	public AltDisjTreeTL(List<? extends ITask> tasks) {
		super(tasks);
	}
	
	public int getTaskType(IRTask rtask){
		final IBinaryNode leaf = getLeaf(rtask.getHTask());
		final AltDisjStatusTL status = (AltDisjStatusTL) leaf.getNodeStatus();
		switch(status.getType()){
		case THETA:
			return 1;
		case LAMBDA:
			return 2;
		case NIL:
			return 3;
		default:
			throw new SolverException("Leaf node has an invalid node type");
		}
	}
	public void initializeEdgeFinding(final TreeMode mode, final Iterable<IRTask> rtasks) {
		this.setMode(mode);
		for (IRTask rtask : rtasks) {
			if(rtask.isRegular()) {
				final IBinaryNode leaf = getLeaf(rtask.getHTask());
				final ThetaTreeLeaf status =  (ThetaTreeLeaf) leaf.getNodeStatus();
				if(status.getType() == AbstractVilimTree.NodeType.NIL) {
					status.insertInTheta();
				}else {
					throw new SolverException("cant initialize Alternative Edge finding TL Tree.");
				}
			}
		}
		fireTreeChanged();
	}
	
	@Override
	public void insert(final ITask task) {
		insertTask(task, new AltDisjStatusTL(NodeType.NIL), new DisjStatusTL(NodeType.INTERNAL));
	}


	protected final class AltDisjStatusTL extends DisjStatusTL {

		public AltDisjStatusTL(NodeType type) {
			super(type);
		}

		@Override
		public void reset() {
			removeFromTheta();
			status.setRespGrayTime(null);
			status.setRespGrayDuration(null);
		}
	}

}
