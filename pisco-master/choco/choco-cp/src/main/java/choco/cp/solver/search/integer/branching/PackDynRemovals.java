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

package choco.cp.solver.search.integer.branching;

import choco.cp.solver.constraints.global.pack.PackSConstraint;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A specialized search strategy for packing problem.
 * At every backtrack, we state that the item can not be packed in equivalent bins.
 * Two bins are equivalent if they have the same remaining space.
 * @author Arnaud Malapert</br>
 * @since 7 déc. 2008 version 2.0.1</br>
 * @version 2.0.1</br>
 */
public class PackDynRemovals extends AssignVar {

	public final PackSConstraint pack;


	public PackDynRemovals(VarSelector<IntDomainVar> varSel, ValSelector<IntDomainVar> valHeuri,
			PackSConstraint pack) {
		super(varSel, valHeuri);
		this.pack = pack;
	}


	public void removeEmptyBins(IntDomainVar bin) throws ContradictionException {
		final DisposableIntIterator iter=bin.getDomain().getIterator();
        try{
            while(iter.hasNext()) {
                final int b=iter.next();
                if(pack.isEmpty(b)) {
                    bin.remVal(b);
                }
            }
        }finally{
            iter.dispose();
        }
	}
	public final void fail() throws ContradictionException {
		getManager().solver.getPropagationEngine().raiseContradiction(this);
	}

	public void removeEquivalentBins(IntDomainVar bin,int bup) throws ContradictionException {
		final DisposableIntIterator iter=bin.getDomain().getIterator();
		final int space = pack.getRemainingSpace(bup);
        try{
		while(iter.hasNext()) {
			final int b=iter.next();
			if(pack.getRemainingSpace(b)==space) {bin.remVal(b);}
		}
        }finally {
            iter.dispose();
        }
	}
	
	private int reuseVal;
	/**
	 * @see choco.cp.solver.search.integer.branching.AssignVar#goUpBranch(java.lang.Object, int)
	 */
	@Override
	public void goUpBranch(final IntBranchingDecision decision) throws ContradictionException {
		super.goUpBranch(decision);
		reuseVal = decision.getBranchingValue();
		if(pack.svars[reuseVal].isInstantiated()) {
			//we cant pack another item into the bin, so the free space is lost.
			//the previous partial assignment dominates any assignment where the item is packed into antother bin
			fail();
		}else if(pack.isEmpty(reuseVal)) {
			//there was a single item into the bin, so we cant pack the item into a empty bin again
			removeEmptyBins(decision.getBranchingIntVar());
		} else {
			//there was other items into the bin, so we cant pack the item into a bin with the same available space again
			removeEquivalentBins(decision.getBranchingIntVar(), reuseVal);
		}
	}


}
