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

package choco.kernel.common.opres.graph;

import choco.kernel.common.util.tools.ArrayUtils;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;




/**
 * @author Arnaud Malapert
 *
 */
public class DagDTC extends GraphDTC {

	protected final int[] orderIndex;

	protected int[] order;

	private TopoAlgoStruct topStruct;

	/**
	 * @param n
	 */
	public DagDTC(final int n) {
		super(n);
		order = ArrayUtils.zeroToN(n);
		orderIndex = ArrayUtils.zeroToN(n);
	}


	@Override
	public int add(int i, int j) {
		if(isNotCyclic(i, j)) {
			final int val = super.add(i,j);
			if(val == ADDED) {fireTopologicalorder(i, j);}
			return val;
		}
		else {return CYCLE;}

	}

	protected void fireTopologicalorder(int i, int j) {
		computeTopologicalOrder();
	}

	public boolean remove(int i, int j) {
		boolean changes=false;
		if(removeEdges(i, j)) {
			for (int u = 0; u < n; u++) {
				if(index[u][i]!=null && index[u][i].isChild(j)) {
					changes |= hook(u,j);
				}
			}

		}
		return changes;
	}

	private final boolean removeEdges(int i, int j) {
		int idx = successors[i].lastIndexOf(j);
		if(idx != -1) {
			successors[i].remove(idx);
			idx = predecessors[j].lastIndexOf(i);
			predecessors[j].remove(idx);
			return true;
		}
		return false;
	}

	private final boolean hasNextNode(final TreeNode node) {
		if(node.incomingIndex<predecessors[node.index].size()) {return true;}
		else {
			node.incomingIndex=predecessors[node.index].size() ;
			return false;
		}
	}

	private final int nextNode(final TreeNode node) {
		node.incomingIndex++;
		return predecessors[node.index].get(node.incomingIndex-1);

	}
	private final boolean hook(final int i, final int j) {
		final TreeNode tij=index[i][j];
		while( hasNextNode(tij)) {
			final int x= nextNode(tij);
			if(index[i][x]!=null) {
				//transforming tree
				tij.father.removeChild(j);
				index[i][x].addChild(index[i][j]);
				return false;
			}
		}
		//j is no more in the transitive closure
		index[i][j].setRoot();
		index[i][j]=null; //NOPMD
		//we have to copy the children list because we will continue to modify it
		TreeNode[] children = tij.copyChildren();
		for (TreeNode u : children) {
			hook(i, u.index);
		}
		return true;
	}

	private final boolean isNotCyclic(final int i,final int j) {
		return index[j][i]==null;
	}

	public final boolean isCyclic(final int i,final int j) {
		return index[j][i]!=null;
	}


	protected final void computeTopologicalOrder() {
		if(topStruct == null) {topStruct = new TopoAlgoStruct();}
		topStruct.reset();
		int cpt=0;
		while(! topStruct.free.isEmpty()) {
			//set node
			order[cpt]= topStruct.free.remove(0);
			orderIndex[order[cpt]] = cpt;
			successors[order[cpt]].forEach(topStruct);
			cpt++;
		}
	}

	public final int[] getTopologicalOrderIndex() {
		return orderIndex;
	}

	public final int[] getTopologicalOrder() {
		return order;
	}

	protected class TopoAlgoStruct implements TIntProcedure {


		public final int nbPredecessors[];

		public final TIntArrayList free;

		public TopoAlgoStruct() {
			super();
			nbPredecessors = new int[n];
			free = new TIntArrayList();
		}


		void reset() {
			free.clear();
			for (int i = 0; i < n; i++) {
				if( predecessors[i].size() == 0) {
					free.add(i);
				}else {
					nbPredecessors[i] = predecessors[i].size();
				}
			}
		}


		@Override
		public boolean execute(int arg0) {
			nbPredecessors[arg0]--;
			if(nbPredecessors[arg0] == 0) {free.add(arg0);}
			return true;
		}

	}

}