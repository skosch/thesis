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

package choco.kernel.solver.search;


import choco.IPretty;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;
import choco.kernel.solver.variables.real.RealVar;
import choco.kernel.solver.variables.set.SetVar;

/**
 * A class for keeping a trace of the search algorithm, through an IntBranching
 * (storing the current branching object, as well as the label of the current branch)
 */
public final class IntBranchingTrace implements IntBranchingDecision {

	private AbstractIntBranchingStrategy branching;

	private Object branchingObject;

	private int branchIndex;

	private int branchingValue = Integer.MAX_VALUE;



	public IntBranchingTrace() {
		super();
	}



	private IntBranchingTrace(AbstractIntBranchingStrategy branching,
			Object branchingObject, int branchIndex, int branchingValue) {
		super();
		this.branching = branching;
		this.branchingObject = branchingObject;
		this.branchIndex = branchIndex;
		this.branchingValue = branchingValue;
	}



	public final AbstractIntBranchingStrategy getBranching() {
		return branching;
	}

	public final void setBranching(AbstractIntBranchingStrategy branching) {
		this.branching = branching;
	}

	public final int getBranchIndex() {
		return branchIndex;
	}

	public final void setBranchIndex(final int branchIndex) {
		this.branchIndex = branchIndex;
	}

	public final void incrementBranchIndex() {
		branchIndex++;
	}

	public final int getBranchingValue() {
		return branchingValue;
	}

	public final void setBranchingValue(final int branchingValue) {
		this.branchingValue = branchingValue;
	}

	public final Object getBranchingObject() {
		return branchingObject;
	}

	@Override
	public final IntDomainVar getBranchingIntVar() {
		return (IntDomainVar) branchingObject;
	}

	@Override
	public final SetVar getBranchingSetVar() {
		return (SetVar) branchingObject;
	}

	@Override
	public final RealVar getBranchingRealVar() {
		return (RealVar) branchingObject;
	}

	public final void setBranchingObject(final Object branchingObject) {
		this.branchingObject = branchingObject;
	}

	public void clear() {
		branchIndex = 0;
		branchingObject = null;
		branching = null;
		branchingValue = Integer.MAX_VALUE;
	}

	public IntBranchingTrace copy() {
		return new IntBranchingTrace(branching, branchingObject, branchIndex, branchingValue);
	}
	
	//utility function
	public final void setIntVal() throws ContradictionException {
		( (IntVar) branchingObject).setVal(branchingValue);
	}
	
	public final void remIntVal() throws ContradictionException {
		( (IntDomainVar) branchingObject).remVal(branchingValue);
	}
	
	public final void setValInSet() throws ContradictionException {
		( (SetVar) branchingObject).setValIn(branchingValue);
	}
	
	public final void setValOutSet() throws ContradictionException {
		( (SetVar) branchingObject).setValOut(branchingValue);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(128);
		if (branchingObject instanceof IPretty) {
			b.append( ( (IPretty) branchingObject).pretty());
		}else {
			b.append(branchingObject);
		}
		if(branchingValue != Integer.MAX_VALUE) {
			b.append(" value=").append(branchingValue);
		}
		b.append(" branch ").append(branchIndex);
		return new String(b);
	}




}
