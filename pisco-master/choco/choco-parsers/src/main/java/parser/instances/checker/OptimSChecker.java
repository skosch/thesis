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

package parser.instances.checker;

import choco.kernel.solver.search.checker.SolutionCheckerException;
import parser.instances.ResolutionStatus;

public final class OptimSChecker implements IOptimChecker {
	
	protected final int minObjValue;

	protected final int maxObjValue;

	public OptimSChecker(int optObjValue) {
		this(optObjValue, optObjValue);
	}
	
	public OptimSChecker(int minObjValue, int maxObjValue) {
		super();
		this.minObjValue = minObjValue;
		this.maxObjValue = maxObjValue;
	}

	public int getMinObjValue() {
		return minObjValue;
	}

	public int getMaxObjValue() {
		return maxObjValue;
	}

	@Override
	public boolean checkLowerBound(boolean doMaximize, int lbVal) {
		return doMaximize ? lbVal >= minObjValue : lbVal <= maxObjValue;
	}

	@Override
	public boolean checkOptimum(int optVal) {
		return optVal >= minObjValue && optVal <= maxObjValue;
	}

	@Override
	public boolean checkUpperBound(boolean doMaximize, int ubVal) {
		return doMaximize ? ubVal <= maxObjValue : ubVal >= minObjValue;
	}

	@Override
	public boolean checkLowerBound(boolean doMaximize, Number lbVal) {
		return lbVal != null && checkLowerBound(doMaximize, lbVal.intValue());
	}

	@Override
	public boolean checkUpperBound(boolean doMaximize, Number ubVal) {
		return ubVal != null && checkUpperBound(doMaximize, ubVal.intValue());
	}

	@Override
	public boolean checkOptimum(Number optVal) {
		return optVal != null && checkOptimum(optVal.intValue());
	}
	
	private void fail(ResolutionStatus status, Number objective)  throws SolutionCheckerException {
		throw new SolutionCheckerException("check-status...["+pretty()+"][status:"+status+"][obj:"+objective+ ']');
	}
	
	@Override
	public void checkStatus(Boolean doMaximize, ResolutionStatus status, Number objective) throws SolutionCheckerException {
		if(doMaximize == null) throw new SolutionCheckerException("check-status...[invalid-state]");
		switch (status) {
		case OPTIMUM: {
			if( ! checkOptimum(objective) ) fail(status, objective);
			else break;
		}
		case SAT: {
			if( ! checkUpperBound(doMaximize, objective)) fail(status, objective);
			else break;
		}
		case UNSAT : {
			fail(status, objective);
		}
		default: 
		}
	}

	@Override
	public String pretty() {
		return "check-optim:"+( minObjValue == maxObjValue ? minObjValue : "[" + minObjValue + ',' +maxObjValue+ ']');
	}

	@Override
	public String toString() {
		return pretty();
	}

	
}
