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

package choco.cp.solver.constraints.integer.extension;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.extension.BinRelation;
import choco.kernel.solver.constraints.integer.extension.ConsistencyRelation;
import choco.kernel.solver.constraints.integer.extension.CspBinSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A binary constraint for simple forward checking
 */
public final class FCBinSConstraint extends CspBinSConstraint {

    public FCBinSConstraint(IntDomainVar x0, IntDomainVar x1, BinRelation rela) {//int[][] consistencyMatrice) {
        super(x0, x1, rela);
    }

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK;
    }


    public Object clone() {
        return new FCBinSConstraint(this.v0, this.v1, this.relation);
    }

    // standard filtering algorithm initializing all support counts
    public void propagate() throws ContradictionException {
        if (v0.isInstantiated())
            awakeOnInst(0);
        if (v1.isInstantiated())
            awakeOnInst(1);
    }

    public void awakeOnInst(int idx) throws ContradictionException {
		int left, right;
        if (idx == 0) {
			int value = v0.getVal();
			DisposableIntIterator itv1 = v1.getDomain().getIterator();
			left = right = Integer.MIN_VALUE;
            try {
				while (itv1.hasNext()) {
					int val = itv1.next();
					if (!relation.isConsistent(value, val)) {
                        if(val == right +1){
                            right = val;
                        }else{
                            v1.removeInterval(left, right, this, false);
                            left = right = val;
                        }
					}
				}
                v1.removeInterval(left, right, this, false);
			} finally {
				itv1.dispose();
			}
		} else {
			int value = v1.getVal();
			DisposableIntIterator itv0 = v0.getDomain().getIterator();
			left = right = Integer.MIN_VALUE;
            try {
				while (itv0.hasNext()) {
					int val = itv0.next();
					if (!relation.isConsistent(val, value)) {
						if(val == right +1){
                            right = val;
                        }else{
                            v0.removeInterval(left, right, this, false);
                            left = right = val;
                        }
					}
				}
                v0.removeInterval(left, right, this, false);
			} finally {
				itv0.dispose();
			}
		}
    }


    public AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
        BinRelation rela2 = (BinRelation) ((ConsistencyRelation) relation).getOpposite();
        return new FCBinSConstraint(v0, v1, rela2);
    }

    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("FC(").append(v0.pretty()).append(", ").append(v1.pretty()).append(", ").
                append(this.relation.getClass().getSimpleName()).append(")");
        return sb.toString();
    }
}
