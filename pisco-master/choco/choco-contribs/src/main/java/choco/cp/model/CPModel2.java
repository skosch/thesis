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

package choco.cp.model;

import choco.Choco;
import choco.kernel.model.Model2;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;

import java.util.List;

public class CPModel2 extends CPModel implements Model2 {

	public CPModel2() {
		super();
	}

	/**
	 * A constraint that ensures x < y
	 * 
	 * @param x
	 *            an integer expression variable
	 * @param y
	 *            an integer expression variable
	 */
	@Override
	public void lt(IntegerExpressionVariable x, IntegerExpressionVariable y) {
		addConstraint(Choco.lt(x, y));
	}

	@Override
	public void eq(IntegerExpressionVariable intV, int c) {
		addConstraint(Choco.eq(intV, c));
	}

	@Override
	public void eq(IntegerExpressionVariable x, IntegerExpressionVariable y) {
		addConstraint(Choco.eq(x, y));
	}

	@Override
	public void allDifferent(String options, IntegerVariable[] vars) {
		addConstraint(Choco.allDifferent(options, vars));
	}

	@Override
	public void leq(IntegerExpressionVariable x, IntegerExpressionVariable y) {
		addConstraint(Choco.leq(x, y));
	}

	@Override
	public void leq(IntegerExpressionVariable x, int c) {
		addConstraint(Choco.leq(x, c));
	}

	@Override
	public void feasTupleAC(List<int[]> tuples, IntegerVariable... vars) {
		addConstraint(Choco.feasTupleAC(tuples, vars));
	}

	@Override
	public IntegerVariable makeIntVar(String name, int lb, int ub,
			String... options) {
		final IntegerVariable var = Choco.makeIntVar(name, lb, ub, options);
		addVariable(var);
		return var;
	}

}
