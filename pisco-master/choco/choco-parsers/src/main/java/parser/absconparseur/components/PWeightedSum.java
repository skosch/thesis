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

import parser.absconparseur.PredicateTokens;


public class PWeightedSum extends PGlobalConstraint {
	private final int[] coeffs;

	private final PredicateTokens.RelationalOperator operator;

	private final int limit;

	public int[] getCoeffs() {
		return coeffs;
	}

	public PredicateTokens.RelationalOperator getOperator() {
		return operator;
	}

	public PWeightedSum(String name, PVariable[] scope, int[] coeffs, PredicateTokens.RelationalOperator operator, int limit) {
		super(name, scope);
		this.coeffs = coeffs;
		this.operator = operator;
		this.limit = limit;
	}

	public int getLimit() {
		return limit;
	}

	public long computeCostOf(int[] tuple) {
		int sum = 0;
		for (int i = 0; i < coeffs.length; i++)
			sum += coeffs[i] * tuple[i];
		boolean satisfied = operator == PredicateTokens.RelationalOperator.EQ ? sum == limit : operator == PredicateTokens.RelationalOperator.NE ? sum != limit : operator == PredicateTokens.RelationalOperator.GE ? sum >= limit : operator == PredicateTokens.RelationalOperator.GT ? sum > limit
				: operator == PredicateTokens.RelationalOperator.LE ? sum <= limit : sum < limit;
		return satisfied ? 0 : 1;
	}

	public String toString() {
		String s = super.toString() + " : weightedSum\n\t";
		for (int i = 0; i < coeffs.length; i++)
			s += coeffs[i] + "*" + scope[i].getName() + ' ';
		s += PredicateTokens.RelationalOperator.getStringFor(operator) + ' ' + limit;
		return s;
	}

	public boolean isGuaranteedToBeOverflowFree() {
		int sumL = 0;
		double sumD = 0;

		for (int i = 0; i < scope.length; i++) {
			int[] values = scope[i].getDomain().getValues();
			int maxAbsoluteValue = Math.max(Math.abs(values[0]), Math.abs(values[values.length - 1]));
			sumL+=Math.abs(coeffs[i])*maxAbsoluteValue;
			sumD+=Math.abs(coeffs[i])*maxAbsoluteValue;
		}
        return !(sumL != sumD || Double.isInfinite(sumD));
    }
}
