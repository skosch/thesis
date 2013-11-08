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

package choco.cp.common.util.preprocessor.detector.scheduling;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectProcedure;

import java.io.ObjectInputStream.GetField;
import java.util.BitSet;

import choco.kernel.common.IDotty;
import choco.kernel.model.constraints.ITemporalRelation;

public class DisjunctiveGraph<E extends ITemporalRelation<?, ?>> implements IDotty {

	//number of nodes
	public final int nbNodes;

	//number of arcs
	protected int nbArcs = 0;

	//number of edges
	protected int nbEdges = 0;

	private BitSet[] savedPrecGraph;
	
	protected final BitSet[] precGraph;

	protected final BitSet[] disjGraph;

	protected final TIntIntHashMap setupTimes;

	public final TIntObjectHashMap<E> storedConstraints;

	public DisjunctiveGraph(int n) {
		precGraph = new BitSet[n]; 
		disjGraph = new BitSet[n]; 
		setupTimes = new TIntIntHashMap();
		for (int i = 0; i < n; i++) {
			precGraph[i] = new BitSet(n);
			disjGraph[i] = new BitSet(n);
		}
		nbNodes = n;
		storedConstraints = new TIntObjectHashMap<E>();
	}

	public final int getNbNodes() {
		return nbNodes;
	}

	public final int getNbArcs() {
		return nbArcs;
	}

	public final int getNbEdges() {
		return nbEdges;
	}

	public final int getKey(int i, int j) {
		return (i* nbNodes) +j;
	}

	public final boolean isEmpty() {
		return nbArcs == 0 && nbEdges == 0;
	}

	/**
	 * 
	 * @param i the index of the task
	 * @return a bitset with its successors indices (WARNING: do not modify the bitset!)
	 */
	public BitSet getPrecSuccessors(int i) {
		//FIXME Implement unmodifiable bitset ?
		return precGraph[i];
	}
	
	protected final void setPrecClosure() {
		savedPrecGraph = copy(precGraph);
		floydMarshallClosure(precGraph);
	}
	
	protected final void unsetPrecClosure() {
		if(savedPrecGraph != null) {
			for (int i = 0; i < savedPrecGraph.length; i++) {
				precGraph[i] = savedPrecGraph[i];
			}
		}
	}
	
	public final BitSet[] copyPrecGraph()	{
		return copy(precGraph);
	}
	

	public final BitSet[] getPrecClosure() {
		return getClosure(precGraph);
	}
	
	
	public final static BitSet[] copy(BitSet[] graph) {
		final BitSet[] res = new BitSet[graph.length]; 
		for (int i = 0; i < graph.length; i++) {
			res[i] = (BitSet) graph[i].clone();
		}
		return res;
	}
	
	public final static BitSet[] getClosure(BitSet[] graph) {
		final BitSet[] closure = copy(graph);
		floydMarshallClosure(closure);
		return closure;
	}
	
	public final static void floydMarshallClosure(BitSet[] graph) {
		//Floyd marshall : quick and dirty
		for (int k = 0; k < graph.length; k++) {
			for (int i = 0; i < graph.length; i++) {
				if(graph[i].get(k)) {
					graph[i].or(graph[k]);
				}
			}
		}
	}

	public final static BitSet[] getReduction(BitSet[] graph) {
		final BitSet[] reduction = getClosure(graph);
		floydMarshallReduction(reduction);
		return reduction;
	}

	public final static void floydMarshallReduction(BitSet[] graph) {
		//Floyd marshall : quick and dirty
		for (int k = 0; k < graph.length; k++) {
			for (int i = 0; i < graph.length; i++) {
				if(graph[i].get(k)) {
					graph[i].andNot(graph[k]);
				}
			}
		}
	}

	public final static BitSet[] getTransitive(BitSet[] graph) {
		final BitSet[] reduction = getReduction(graph);
		final BitSet[] transitive = copy(graph);
		andNot(transitive, reduction);
		return transitive;
	}
	
	public final static void andNot(BitSet[] graph,BitSet[] reduction) {
		for (int i = 0; i < graph.length; i++) {
			graph[i].andNot(reduction[i]);
		}
	}


	public final void addArc(int i, int j, int setupTime) {
		precGraph[i].set(j);
		final int key = getKey(i, j);
		setupTimes.put(key, setupTime);
		nbArcs++;
	}

	public final void addArc(int i, int j, int setupTime, E rel) {
		precGraph[i].set(j);
		final int key = getKey(i, j);
		storedConstraints.put(key, rel);
		setupTimes.put(key, setupTime);
		nbArcs++;
	}
	
	protected void deleteArc(int i, int j) {
		if(precGraph[i].get(j)) {
		precGraph[i].clear(j);
		final int key = getKey(i, j);
		storedConstraints.remove(key);
		setupTimes.remove(key);
		nbArcs++;
		}
	}

	public final void safeAddArc(int i, int j, int setupTime) {
		if(setupTime >= 0) addArc(i, j, setupTime);
	}

	public final void addEdge(int i, int j, E rel) {
		addEdge(i, j, rel.forwardSetup(), rel.backwardSetup(), rel);
	}


	protected final void addEdge(int i, int j, int forwardSetup, int backwardSetup, E rel) {
		disjGraph[i].set(j);
		disjGraph[j].set(i);
		final int key = getKey(i, j);
		storedConstraints.put(key, rel);
		setupTimes.put(key, forwardSetup);
		setupTimes.put(getKey(j, i), backwardSetup);
		nbEdges++;
	}

	public final boolean isFixed() {
		return storedConstraints.forEachValue(new TObjectProcedure<E>() {

			@Override
			public boolean execute(E arg0) {
				return arg0.isFixed();
			}


		});
	}


	public final int setupTime(int i, int j) {
		return setupTimes.get(getKey(i, j));
	}

	public final boolean containsArc(int i, int j) {
		return precGraph[i].get(j);
	}

	public final boolean containsEdge(int i, int j) {
		return disjGraph[i].get(j);
	}

	public final boolean containsRelation(int i, int j) {
		return containsArc(i, j) || containsArc(j, i) || containsEdge(i, j);
	}

	public final boolean containsConstraint(int i, int j) {
		return storedConstraints.contains(getKey(i, j));
	}

	public final E getConstraint(int i, int j) {
		return storedConstraints.get(getKey(i, j));
	}
	
	public final E getEdgeConstraint(int i, int j) {
		final E cij = storedConstraints.get(getKey(i, j));
		return cij == null ? storedConstraints.get(getKey(j, i)) : cij;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < nbNodes; i++) {
			for (int j = 0; j < nbNodes; j++) {
				if (precGraph[i].get(j)) {
					s.append("o ");
				}else if (disjGraph[i].get(j)) {
					s.append("x ");
				} else {
					s.append(". ");
				}
			}
			s.append("\n");
		}
		return s.toString();
	}

	public final String setupTimesToString() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < nbNodes; i++) {
			for (int j = 0; j < nbNodes; j++) {
				if (precGraph[i].get(j) 
						|| disjGraph[i].get(j)) {
					s.append(setupTimes.get(getKey(i, j))).append(' ');
				} else {
					s.append(". ");
				}
			}
			s.append("\n");
		}
		return s.toString();
	}

	protected final String getArcLabel(int i, int j) {
		final int st = setupTime(i, j);
		return st > 0 ? "label=\""+st+'\"' : null;
	}

	protected final String getEdgeLabel(int i, int j) {
		final int st1 = setupTime(i, j);
		final int st2 = setupTime(j, i);
		return st1 > 0 || st2 > 0 ? "label=\"("+st1+", "+st2+")\"" : null;


	}
	private final void writeArc(StringBuilder b, int i, int j) {
		b.append(i).append(" -> ").append(j);
	}

	protected final static String ARC_COLOR = "color=forestgreen";
	protected final static String EDGE_COLOR = "color=royalblue";
	protected final static String STY_BOLD_DASHED= "style=\"bold,dashed\"";
	protected final static String STY_BOLD = "style=bold";
	protected final static String ARROW_DOT = "arrowhead=dot";
	protected final static String ARROW_BIG = "arrowsize=1.5";
	protected final static String DIR_BWD = "dir=back";

	protected void writeAttributes(StringBuilder b, String...options) {
		b.append(" [");
		final int l1 = b.length();
		for (String opt : options) {
			if(opt != null && ! opt.isEmpty()) b.append(' ').append(opt).append(',');
		}
		final int l2 = b.length();
		if(l2 > l1) b.replace(l2 -1, l2, "];\n");
		else b.deleteCharAt(l1 - 1); 
	}


	protected void writeArcAttributes(StringBuilder b, int i, int j) {
		writeAttributes(b, ARC_COLOR, STY_BOLD, getArcLabel(i, j));
	}

	protected void writeEdge(StringBuilder b, E rel, int i, int j) {
		if(rel.isFixed() && rel.getDirVal() == ITemporalRelation.BWD) writeArc(b, j, i);
		else writeArc(b, i, j);
	}

	protected void writeEdgeAttributes(StringBuilder b, E rel, int i, int j) {
		if(rel.isFixed()) {
			writeAttributes(b, EDGE_COLOR, STY_BOLD, ARROW_BIG, 
					(rel.getDirVal() == ITemporalRelation.FWD ? getArcLabel(i, j) : getArcLabel(j, i)));
		} else {
			writeAttributes(b, EDGE_COLOR, STY_BOLD_DASHED, ARROW_BIG, ARROW_DOT, getEdgeLabel(i, j));
		}

	}


	protected StringBuilder toDottyNodes() {
		return new StringBuilder();
	}

	@Override
	public final String toDotty() {
		final StringBuilder  b = toDottyNodes();
		for (int i = 0; i < nbNodes; i++) {
			for (int j = precGraph[i].nextSetBit(0); j >= 0; j = precGraph[i]
					.nextSetBit(j + 1)) {
				writeArc(b, i, j);
				writeArcAttributes(b, i, j);
			}
			for (int j = disjGraph[i].nextSetBit(0); j >= 0; j = disjGraph[i]
					.nextSetBit(j + 1)) {
				E rel = storedConstraints.get(getKey(i, j) );
				if( rel != null) {
					writeEdge(b, rel, i, j); 
					writeEdgeAttributes(b,rel, i, j);
				}
			}
		}
		return b.toString();
	}


}
