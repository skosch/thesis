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

package parser.absconparseur.components;


import choco.kernel.model.constraints.automaton.DFA;
import choco.kernel.solver.constraints.integer.extension.BinRelation;
import choco.kernel.solver.constraints.integer.extension.LargeRelation;
import parser.absconparseur.InstanceTokens;
import parser.absconparseur.Toolkit;
import parser.chocogen.XmlClause;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PRelation {

	protected String name;

	protected int nbTuples;

	protected int arity;

	protected String semantics;

	protected int[][] tuples;

	private List<int[]> ltuples;

    private int index;

    /**
	 * weights = null if semantics is different from InstanceTokens.SOFT
	 */
	private int[] weights;

	/**
	 * defaultCost = Integer.MAX_VALUE if defaultCost is infinity
	 */
	private int defaultCost;

	/**
	 * The max of all weights values and deafultCost. It is 1 for an ordinary relation.
	 */
	private int maximalCost;

	/**
	 * Choco relation representing this PRelation if it is binary
	 */
	protected BinRelation brel;

	/**
	 * Choco relation representing this PRelation if it is nrary
	 */
	protected LargeRelation lrel;


    /**
     * Representing this extentional constraint by a set of
     * clauses
     */
    protected List<XmlClause> satencoding;

    /**
	 * DFA to represent the table
	 * @return
	 */
	protected DFA dfa;

	protected boolean eqInTuples;

	protected boolean neqInTuples;

	public String getName() {
		return name;
	}

	public int getArity() {
		return arity;
	}

	public int getNbTuples() {
		return nbTuples;
	}

	public String getSemantics() {
		return semantics;
	}

	public int[][] getTuples() {
		return tuples;
	}

	public BinRelation getBrel() {
		return brel;
	}

	public void setBrel(BinRelation brel) {
		this.brel = brel;
	}

	public LargeRelation getLrel() {
		return lrel;
	}

	public void setLrel(LargeRelation lrel) {
		this.lrel = lrel;
	}

	public DFA getDfa() {
		return dfa;
	}

	public void setDfa(DFA dfa) {
		this.dfa = dfa;
	}

    public void setClauseEncoding(List<XmlClause> encoding) {
        satencoding = encoding;
    }

    public List<XmlClause> getSatEncoding() {
        return satencoding;
    }

    public boolean isNeqInTuples() {
		return neqInTuples;
	}

	public void setNeqInTuples(boolean neqInTuples) {
		this.neqInTuples = neqInTuples;
	}

	public boolean isEqInTuples() {
		return eqInTuples;
	}

	public void setEqInTuples(boolean eqInTuples) {
		this.eqInTuples = eqInTuples;
	}

	public boolean checkEqInCouples() {
        for (int[] t : ltuples) {
            if (t[0] != t[1]) {
                return false;
            }
        }
		return true;
	}

	public boolean checkNeqInCouples() {
        for (int[] t : ltuples) {
            if (t[0] == t[1]) {
                return false;
            }
        }
		return true;
	}

	public List<int[]> getListTuples() {
		if (ltuples != null) return ltuples;
		else {
			ltuples = new LinkedList<int[]>();
            ltuples.addAll(Arrays.asList(tuples));
			tuples = null;
		}
		return ltuples;
	}

	public void eraseListTuple() {
		ltuples = null;
	}

	public int[] getWeights() {
		return weights;
	}

	public int getDefaultCost() {
		return defaultCost;
	}

	public int getMaximalCost() {
		return maximalCost;
	}

	public PRelation(String name, int arity, int nbTuples, String semantics, int[][] tuples, int[] weights, int defaultCost) {
		this.name = name;
		this.arity = arity;
		this.nbTuples = nbTuples;
		this.semantics = semantics;
		this.tuples = tuples;
		this.weights = weights;
		this.defaultCost = defaultCost;
		if (weights == null)
			maximalCost=1;
		else {
			maximalCost=defaultCost;
			for (int w : weights)
				if (w > maximalCost)
					maximalCost=w;
    }
        this.index = Integer.parseInt(name.substring(1,name.length()));
        Arrays.sort(tuples,Toolkit.lexicographicComparator);
    }

	public PRelation(String name, int arity, int nbTuples, String semantics, int[][] tuples) {
		this(name, arity, nbTuples, semantics, tuples, null, semantics.equals(InstanceTokens.SUPPORTS) ? 1 : 0);
	}

	public int computeCostOf(int[] tuple) {
		int position = Arrays.binarySearch(tuples, tuple, Toolkit.lexicographicComparator);
		if (semantics.equals(InstanceTokens.SOFT))
			return position >= 0 ? weights[position] : defaultCost;
		if (semantics.equals(InstanceTokens.SUPPORTS))
			return position >= 0 ? 0 : 1;
		return position >= 0 ? 1 : 0;
	}

	public String toString() {
		int displayLimit = 5;
		String s = "  relation " + name + " with arity=" + arity + ", semantics=" + semantics + ", nbTuples=" + nbTuples + " : ";
		for (int i = 0; i < Math.min(nbTuples, displayLimit); i++) {
			s += "(";
			for (int j = 0; j < arity; j++)
				s += (tuples[i][j] + (j < arity - 1 ? "," : ""));
			s += ") ";
			if (weights != null)
				s += " with cost=" + weights[i] + ", ";
		}
		return s + (nbTuples > displayLimit ? "..." : "");
	}

	public boolean isSimilarTo(int arity, int nbTuples, String semantics, int[][] tuples) {
		if (semantics.equals(InstanceTokens.SOFT))
			throw new IllegalArgumentException();
		if (this.arity != arity || this.nbTuples != nbTuples)
			return false;
		if (!this.semantics.equals(semantics))
			return false;
		for (int i = 0; i < tuples.length; i++)
			for (int j = 0; j < tuples[i].length; j++)
				if (this.tuples[i][j] != tuples[i][j])
					return false;
		return true;
	}

	public String getStringListOfTuples() {
        StringBuilder s = new StringBuilder(128);
		for (int i = 0; i < tuples.length; i++) {
			for (int j = 0; j < tuples[i].length; j++) {
				s.append(tuples[i][j]);
				if (j != tuples[i].length - 1)
					s.append(' ');
			}
			if (i != tuples.length - 1)
				s.append('|');
		}
		return s.toString();
	}

    public int hashCode() {
        return index;
}
}
