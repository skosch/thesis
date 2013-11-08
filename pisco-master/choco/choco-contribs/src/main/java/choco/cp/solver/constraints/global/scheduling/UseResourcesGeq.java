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

package choco.cp.solver.constraints.global.scheduling;

import java.util.Arrays;

import choco.cp.solver.constraints.integer.bool.sum.LeqBoolSum;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.TaskVar;

public class UseResourcesGeq extends AbstractUseResourcesSConstraint {

	public UseResourcesGeq(IEnvironment environment, TaskVar taskvar, int k,
			IntDomainVar[] usages, IRTask[] rtasks) {
		super(environment, taskvar, k, usages, rtasks);
	}
	
	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		super.awakeOnInst(idx);
		boolSumS.awakeOnGeq();
		filterHypotheticalDomains(); 
		//FIXME -  Temporary : waiting for task event management - created 4 juil. 2011 by Arnaud Malapert
	}

	@Override
	public void propagate() throws ContradictionException {
		if( boolSumS.filterGeq() ) {
			super.propagate();
		}
	}

	@Override
	public boolean isSatisfied(int[] tuple) {
		return MathUtils.sumFrom(tuple, BOOL_OFFSET) >= boolSumS.bValue;
	}

	@Override
	public Boolean isEntailed() {
		return boolSumS.isEntailedGeq();
	}

	@Override
	public AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
		return new LeqBoolSum(solver.getEnvironment(), Arrays.copyOf(boolSumS.getBoolVars(), boolSumS.getBoolVars().length), boolSumS.bValue-1);
	}
}
