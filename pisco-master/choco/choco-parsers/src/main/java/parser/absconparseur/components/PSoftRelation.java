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

import parser.absconparseur.InstanceTokens;
import parser.absconparseur.Toolkit;

import java.util.Arrays;

public class PSoftRelation extends PRelation {

	private final int[] weights;

	/**
	 * defaultCost = Integer.MAX_VALUE if defaultCost is infinity
	 */
	private final int defaultCost;

	/**
	 * The max of all weights values and defaultCost.
	 */
	private final int maximalCost;

	public int[] getWeights() {
		return weights;
	}

	public int getDefaultCost() {
		return defaultCost;
	}

	public int getMaximalCost() {
		return maximalCost;
	}

	public PSoftRelation(String name, int arity, int nbTuples, String semantics, int[][] tuples, int[] weights, int defaultCost) {
		super(name, arity, nbTuples, semantics, tuples);
		this.weights = weights;
		this.defaultCost = defaultCost;
		int _maximalCost = defaultCost;
		for (int w : weights){
			if (w > _maximalCost){
				_maximalCost = w;
            }
        }
        maximalCost = _maximalCost;
	}

	public int computeCostOf(int[] tuple) {
		int position = Arrays.binarySearch(tuples, tuple, Toolkit.lexicographicComparator);
		return position >= 0 ? weights[position] : defaultCost;
	}

	public String toString() {
		int displayLimit = 5;
		StringBuilder s = new StringBuilder(256);
        s.append("  relation ").append(name).append(" with arity=").append(arity).append(", semantics=")
                .append(semantics).append(", nbTuples=").append(nbTuples).append(", defaultCost=")
                .append(defaultCost).append(" : ");
		for (int i = 0; i < Math.min(nbTuples, displayLimit); i++) {
			s.append('(');
			for (int j = 0; j < arity; j++)
				s.append(tuples[i][j]).append(j < arity - 1 ? "," : "");
			s.append(") ");
			if (weights != null)
                s.append(" with cost=").append(weights[i]).append(", ");
		}
		return s + (nbTuples > displayLimit ? "..." : "");
	}

	public boolean isSimilarTo(int arity, int nbTuples, String semantics, int[][] tuples, int[] weights, int defaultCost) {
		if (!super.isSimilarTo(arity, nbTuples, semantics, tuples))
			return false;
		if (this.defaultCost != defaultCost)
			return false;
		for (int i = 0; i < weights.length; i++)
			if (this.weights[i] != weights[i])
				return false;
		return true;
	}

	public String getStringListOfTuples() {
        StringBuilder sb = new StringBuilder(128);
		int currentWeigth = -1;
		for (int i = 0; i < tuples.length; i++) {
			if (i != 0)
				sb.append('|');
			if (weights[i] != currentWeigth) {
				currentWeigth = weights[i];
                sb.append(currentWeigth).append(InstanceTokens.COST_SEPARATOR);
			}
			for (int j = 0; j < tuples[i].length; j++) {
				sb.append(tuples[i][j]);
				if (j != tuples[i].length - 1)
					sb.append(' ');
			}
		}
		return sb.toString();
	}

}
