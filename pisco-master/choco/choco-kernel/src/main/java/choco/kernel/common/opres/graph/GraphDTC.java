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

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;
import choco.kernel.common.IDotty;




/**
 *
 * opération sur les enfants : insertion, suppression, recherche
 * @author Arnaud Malapert
 *
 */
final class TreeNode {

	protected TreeNode father;

	public int index;

	protected int incomingIndex=0;

	protected TIntObjectHashMap<TreeNode> children = new TIntObjectHashMap<TreeNode>();

	/**
	 * @param index
	 */
	public TreeNode(final int index) {
		super();
		this.index = index;
	}

	@Override
	public String toString() {
		return String.valueOf(index);
	}

	public void removeChild(final int i) {
		TreeNode child = children.remove(i);
		child.father=null; //NOPMD
	}

	public void setRoot() {
		if(father!=null) {
			father.removeChild(this.index);
		}
	}

	public void addChild(final TreeNode child) {
		child.father=this;
		this.children.put(child.index, child);
	}

	public TreeNode[] copyChildren() {
		return children.getValues( new TreeNode[children.size()]);
	}

	public boolean isChild(final int i) {
		return children.containsKey(i);
	}


}


/**
 * @author Arnaud Malapert  </br></br>
 *
 *
<table>
<tr valign="top"><td align="left">
Daniele Frigioni, Tobias Miller, Umberto Nanni, and Christos&nbsp;D. Zaroliagis.
</td></tr>
<tr valign="top"><td align="left">
<b> An experimental study of dynamic algorithms for transitive closure.</b>
</td></tr>
<tr valign="top"><td align="left">
 <em>ACM Journal of Experimental Algorithms</em>, 6:9, 2001.
</td></tr>
</table>
 *
 *
 *
 */
public class GraphDTC implements IDotty {

	public final static int ADDED=0;

	public final static int CYCLE=1;

	public final static int TRANSITIVE=2;

	public final static int INTERNAL_ERROR=3;

	public final static int EXISTING=4;

	public final int n;

	protected int nbEdges = 0;

	protected boolean transitiveArcAdded = true;

	protected final TreeNode[][] index;

	protected final TIntArrayList[] successors;

	protected final TIntArrayList[] predecessors;

	public GraphDTC(final int n) {
		super();
		this.n=n;
		successors = new TIntArrayList[n];
		predecessors = new TIntArrayList[n];
		for (int i = 0; i < n; i++) {
			successors[i] = new TIntArrayList();
			predecessors[i] = new TIntArrayList();
		}
		index=initIndex();
	}


	public final boolean acceptTransitiveArcs() {
		return transitiveArcAdded;
	}


	public final void setAcceptTransitiveArcs(boolean transitiveArcAdded) {
		this.transitiveArcAdded = transitiveArcAdded;
	}




	private final TreeNode[][] initIndex() {
		TreeNode[][] res=new TreeNode[this.n][this.n];
		for (int i = 0; i < n; i++) {
			res[i][i]=new TreeNode(i);
		}
		return res;
	}



	protected final  boolean isNotTransitive(final int i,final int j) {
		return index[i][j]==null;
	}



	public boolean isTransitive(final int i,final int j) {
		return index[i][j]!=null;
	}

	protected final void meld(final int i, final int j, final int u, final int v) {
		index[i][v]=new TreeNode(v);
		index[i][u].addChild(index[i][v]);
		if(index[j][v]!=null) {
			for (TreeNode w	: index[j][v].copyChildren()) {
				if(index[i][w.index]==null) {
					meld(i, j, v, w.index);
				}
			}
		}

	}



	public int add(final int i,final int j) {
		if(isNotTransitive(i,j)) {
			successors[i].add(j);
			predecessors[j].add(i);
			for (int u = 0; u < this.n; u++) {
				if(index[u][i]!=null && index[u][j]==null) {
					meld(u,j,i,j);
				}
			}
			nbEdges++;
			return ADDED;
		}else {
			if(successors[i].contains(j)) {
				return EXISTING;
			}else if( acceptTransitiveArcs()) {
				successors[i].add(j);
				predecessors[j].add(i);
				nbEdges++;
			}
			return TRANSITIVE;
		}
	}

	/**
	 * @see choco.kernel.common.IDotty#toDotty()
	 */
	@Override
	public String toDotty() {
		return toDotty(true);
	}

	protected String toDotty(boolean primalOrDual) {
		final TIntArrayList[] graph = primalOrDual ? successors : predecessors;
		DotProcedure proc = new DotProcedure();
		return proc.toDotty(graph);
	}

	final class DotProcedure implements TIntProcedure {

		public final StringBuilder buffer = new StringBuilder();

		public int origin = 0;

		protected String toDotty(TIntArrayList[] graph) {
			for (origin = 0; origin < graph.length; origin++) {
				graph[origin].forEach(this);
			}
			return new String(buffer);
		}

		@Override
		public boolean execute(int arg0) {
			buffer.append(origin).append("->").append(arg0).append(";\n");
			return true;
		}
	}

	public boolean isDisconnected(int i) {
		return !hasPredecessor(i) && !hasSuccessor(i);
	}

	public final boolean hasPredecessor(final int i) {
		return !predecessors[i].isEmpty();
	}

	public final boolean hasSuccessor(final int i) {
		return !successors[i].isEmpty();
	}

	public final int getNbPredecessors(final int i) {
		return predecessors[i].size();
	}

	public final TIntArrayList getPredecessors(final int i) {
		return predecessors[i];
	}

	public final int getNbSuccessors(final int i) {
		return successors[i].size();
	}

	public final TIntArrayList getSuccessors(final int i) {
		return successors[i];
	}

	

	public final int getN() {
		return n;
	}


	public final int getNbEdges() {
		return nbEdges;
	}


	public final boolean isEmpty() {
		return nbEdges == 0;
	}


	public final boolean[][] toTreeNodeMatrix() {
		boolean[][] r=new boolean[n][n];
		for (int i = 0; i < index.length; i++) {
			for (int j = 0; j < index[i].length; j++) {
				r[i][j]= index[i][j]!=null;
			}
		}
		return r;
	}


}


