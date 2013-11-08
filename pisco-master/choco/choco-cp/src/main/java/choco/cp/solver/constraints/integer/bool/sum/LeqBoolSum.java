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

package choco.cp.solver.constraints.integer.bool.sum;

import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Arrays;

public final class LeqBoolSum extends AbstractBoolSum {

	public LeqBoolSum(IEnvironment environment, IntDomainVar[] vars, int bValue) {
		super(environment, vars, bValue);
	}

	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		super.awakeOnInst(idx);
		boolSumS.awakeOnLeq();
	}

    @Override
    public void awake() throws ContradictionException {
        int min = boolSumS.computeLbFromScratch();
        int val = boolSumS.bValue;
        if(val < min){
            this.fail();
        }
        this.propagate();
    }

	@Override
	public void propagate() throws ContradictionException {
		if ( boolSumS.filterLeq() ) {
			super.propagate();
		} 
	}

	@Override
	public boolean isSatisfied(int[] tuple) {
		return MathUtils.sum(tuple) <= boolSumS.bValue;
	}

	@Override
	public Boolean isEntailed() {
		return boolSumS.isEntailedLeq();
	}

	@Override
	public AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
		return new GeqBoolSum(solver.getEnvironment(), Arrays.copyOf(vars, vars.length), boolSumS.bValue + 1);
	}

	@Override
	public String pretty() {
		return boolSumS.pretty("<=");
	}
}
