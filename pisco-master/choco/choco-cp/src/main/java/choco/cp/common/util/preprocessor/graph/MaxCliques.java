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

// Calcul de Cliques Max dans un graphe
// TP 16/02/2007
// -------------------------------------

package choco.cp.common.util.preprocessor.graph;

import choco.kernel.common.logging.ChocoLogging;

import java.util.Random;
import java.util.logging.Logger; // main de test

/**
 * 	Algorithme de Bron and Kerbosch 197 to find maxumum cliques
 */
public final class MaxCliques {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

	private final ArrayGraph graph;
	private int[][]  cliques;
	
	public MaxCliques(ArrayGraph g) {
		graph   = g;
		computeCliques();
	}
	
	// Algorithme de Bron and Kerbosch 1973
	public void computeCliques(){
		graph.setNeighbours();
		boolean[] cand   = new boolean[graph.nbNode]; // graph.length = nombre de sommets
		boolean[] K	     = new boolean[graph.nbNode];
		boolean[] gammaK = new boolean[graph.nbNode];
		int[] degrees = graph.degrees();
		boolean empty = true;
		for(int i=0; i<graph.nbNode; i++) {
			if(degrees[i]>0) {
				empty = false;
				cand[i]   = true;    // sommets candidats    
				K[i]      = false;   // sommets de la clique courante
				gammaK[i] = true;    // sommets candidats connect�s � la clique courante
			}
		}
		cliques = new int[0][0];  
		if(!empty) { 
			BronKerbosh(cand, K, gammaK); 
		}
	}
	
	private boolean BronKerbosh(boolean[] cand, 
							 boolean[] K, 
							 boolean[] gammaK){
		int x = getAndRemoveMaxCand(cand);
		boolean[] updatedCand = removeNeighbours(x,cand); 
		if(!empty(updatedCand)) {
			boolean b = BronKerbosh(updatedCand,K.clone(),gammaK.clone());
			if (!b) return false;
		}
		K[x] = true; 
		boolean[] updatedGammaK = updateGammaK(x, gammaK); 
		if(!empty(updatedGammaK)) {
			return BronKerbosh(updatedGammaK,K,updatedGammaK);
		} else {
			cliques = storeCliques(K);

            return cliques.length <= 2000;
		}
	}
	
	// Routines
	
	private static boolean empty(boolean[] array) {
		for(int i=0; i<array.length; i++) {
			if(array[i]) {
				return false;  
			}
		}
		return true;
	}
	
	private int getAndRemoveMaxCand(boolean[] cand) {
		int index = -1; 
		if (!empty(cand)) {     
			int max = 0; 
			int[] degrees = graph.degrees(); 
			for(int i=0; i<cand.length; i++) {
				if(degrees[i]>max && cand[i]) {
					max   = degrees[i]; 
					index = i; 
				}
			}
			cand[index] = false; 
		}
		return index; 
	}
	
	private boolean[] removeNeighbours(int x, boolean[] cand) {
		boolean[] res = cand.clone(); 
		int[] neighbours = graph.neighbours(x); 
		for(int i=0; i<neighbours.length; i++) {
			res[neighbours[i]]=false;
		}
		return res;
	}
	
	private boolean[] updateGammaK(int x, boolean[] gammaK) {
		boolean[] res = gammaK.clone(); 
		for(int i=0; i<gammaK.length; i++) {
			if(res[i]) {
				boolean isIn = graph.isIn(x,i); 
				if(!isIn) {
					res[i] = false; 
				}
			}
		}
		res[x] = false; 
		return res; 
	}
	
	private int[][] storeCliques(boolean[] K) {
		int[][] updated = new int[cliques.length+1][];
        System.arraycopy(cliques, 0, updated, 0, updated.length - 1);
		int size = 0;
		for(int i=0; i<K.length; i++) {
			if(K[i]) {
				size++;
			}
		}
		updated[updated.length-1] = new int[size];  
		int index = 0; 
		for(int i=0; i<K.length; i++) {
			if(K[i]) {
				updated[updated.length-1][index] = i; 
				index ++; 
			}
		}
		return updated; 
	}
	
	// API utilisateur
	
	public int[][] getMaxCliques() {
		return cliques; 
	}
	
	// ******************************************** //
    // **************** Test ********************** //
    // ******************************************** //


    public static String display(int [] array){
		StringBuilder s = new StringBuilder("[");
		for(int i=0; i<array.length; i++) {
			s.append(array[i]);
			if(i<array.length-1) { 
				s.append(',');
			}
		}
		s.append(']');
		return s.toString();
	}
	
	public static String display(int [][] array){
		StringBuilder s = new StringBuilder();
		for(int i=0; i<array.length; i++) {
			s.append(display(array[i]));
			s.append('\n');
		}
		return s.toString();
	}
	
	public static ArrayGraph generateGraph(int n, int m, int seed, double start) {
		LOGGER.info("Generating graph... "); 
		ArrayGraph g = new ArrayGraph(n); 	
		if(m>n*(n+1)/2) {
			m = n*(n+1)/2; 
		}
	    Random r = new Random(seed);
        int nbNode = g.nbNode;
	    for(int i=0; i<m; i++) {
	    	int v1 = Math.abs(r.nextInt())%nbNode;
	    	int v2 = Math.abs(r.nextInt())%nbNode;
	    	while(v1==v2 || g.isIn(v1,v2)) { // no loop 
	    		v1 = Math.abs(r.nextInt())%nbNode;
	    		v2 = Math.abs(r.nextInt())%nbNode;
	    	}
	    	g.addEdge(v1,v2);
	    }	
	    LOGGER.info("done " + '(' + (System.currentTimeMillis()-start) + " ms).\n");
	    return g;
	}
}

