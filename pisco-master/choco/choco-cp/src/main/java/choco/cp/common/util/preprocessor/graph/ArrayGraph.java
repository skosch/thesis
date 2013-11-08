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

// Petite API de Graphes pour tester BK73 
// TP 16/02/2007
// --------------------------------------

package choco.cp.common.util.preprocessor.graph;

import choco.kernel.model.constraints.Constraint;
import gnu.trove.THashMap;

import java.util.BitSet;

/**
 * A simple representation of a graph as both matrix/list of adjacency  to
 * perform clique detection among binary constraints
 */
public final class ArrayGraph {

    //matrix of adjacency
    private final BitSet[] mat;

    //lists of adjacency
    private int[][] adjmat;

    //number of nodes
    public int nbNode;

    //number of edges
    public int nbEdges = 0;

    //associate the constraint that gave rise to the edge
    public THashMap<Edge,Constraint> storeEdges;

    public ArrayGraph(int n) {
		mat = new BitSet[n]; // default is false
		adjmat = new int[n][];
		for (int i = 0; i < n; i++) {
			mat[i] = new BitSet(n);
		}
		nbNode = n;
        storeEdges = new THashMap<Edge,Constraint>();
    }

	public void addEdge(int i, int j) {
		if (!mat[i].get(j)) nbEdges++;
		if (!mat[j].get(i)) nbEdges++;
		mat[i].set(j);
		mat[j].set(i);
	}

	public void setNeighbours() {
		for (int i = 0; i < mat.length; i++) {
			adjmat[i] = new int[mat[i].cardinality()];
			int index = 0;
			for (int j = mat[i].nextSetBit(0); j >= 0; j = mat[i].nextSetBit(j + 1)) {
				adjmat[i][index] = j;
				index++;
			}
		}
	}

	public void remEdge(int i, int j) {
		if (mat[i].get(j)) nbEdges--;
		if (mat[j].get(i)) nbEdges--;
		mat[i].clear(j);
		mat[j].clear(i);
	}

	public boolean isIn(int i, int j) {
		return mat[i].get(j);
	}

	public int degree(int i) {
		return adjmat[i].length;
	}

	public int[] neighbours(int i) {
		return adjmat[i];
	}

	public int[] degrees() {
		int[] res = new int[mat.length];
		for (int i = 0; i < mat.length; i++) {
			res[i] = degree(i);
		}
		return res;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
        for (BitSet aMat : mat) {
            for (int j = 0; j < mat.length; j++) {
                if (aMat.get(j)) {
                    s.append("x ");
                } else {
                    s.append(". ");
                }
            }
            s.append('\n');
        }
		return s.toString();
	}

    public void storeEdge(Constraint c, int a, int b) {
        storeEdges.put(new Edge(a,b),c);
    }

    public Constraint getConstraintEdge(int a, int b) {
        Constraint c = storeEdges.get(new Edge(a,b));
        Constraint c2 = storeEdges.get(new Edge(b,a));
        if (c != null){
            return c;
        }
        else if (c2 != null) {
            return c2;
        }
        return null;
    }

    public static class Edge {
        int a;
        int b;

        public Edge(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public int hashCode() {
            return a * 10000000 + b; 
        }

        public boolean equals(Object obj) {
            return Edge.class.isInstance(obj) && ((Edge) obj).a == a && ((Edge) obj).b == b;
        }
    }

}

