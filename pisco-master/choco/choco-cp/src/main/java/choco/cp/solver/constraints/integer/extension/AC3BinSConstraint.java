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

public final class AC3BinSConstraint extends CspBinSConstraint {

    public AC3BinSConstraint(IntDomainVar x0, IntDomainVar x1, BinRelation rela) {//int[][] consistencyMatrice) {
        super(x0, x1, rela);
    }

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
    }


    public Object clone() {
        return new AC3BinSConstraint(this.v0, this.v1, this.relation);
    }

    // updates the support for all values in the domain of v1, and remove unsupported values for v1
    public void reviseV1() throws ContradictionException {
        int nbs = 0;
        int left = Integer.MIN_VALUE;
        int right = left;
        DisposableIntIterator itv1 = v1.getDomain().getIterator();
        while (itv1.hasNext()) {
            int val1 = itv1.next();
            DisposableIntIterator itv0 = v0.getDomain().getIterator();
            while (itv0.hasNext()) {
                int val0 = itv0.next();
                if (relation.isConsistent(val0, val1)) {
                    nbs += 1;
                    break;
                }
            }
            itv0.dispose();
            if (nbs == 0) {
                if (val1 == right + 1) {
                    right = val1;
                } else {
                    v1.removeInterval(left, right, this, false);
                    left = right = val1;
                }
//                v1.removeVal(val1, this, false);
            }
            nbs = 0;
        }
        v1.removeInterval(left, right, this, false);
        itv1.dispose();
    }

    // updates the support for all values in the domain of v0, and remove unsupported values for v0
    public void reviseV0() throws ContradictionException {
        int nbs = 0;
        int left = Integer.MIN_VALUE;
        int right = left;
        DisposableIntIterator itv0 = v0.getDomain().getIterator();
        while (itv0.hasNext()) {
            int val0 = itv0.next();
            DisposableIntIterator itv1 = v1.getDomain().getIterator();
            while (itv1.hasNext()) {
                int val1 = itv1.next();
                if (relation.isConsistent(val0, val1)) {
                    nbs += 1;
                    break;
                }
            }
            itv1.dispose();
            if (nbs == 0) {
                if (val0 == right + 1) {
                    right = val0;
                } else {
                    v0.removeInterval(left, right, this, false);
                    left = right = val0;
                }
//                v0.removeVal(val0, this, false);
            }
            nbs = 0;
        }
        v0.removeInterval(left, right, this, false);
        itv0.dispose();
    }

    // standard filtering algorithm initializing all support counts
    public void propagate() throws ContradictionException {
        reviseV0();
        reviseV1();
    }

    public void awakeOnRem(int idx, int x) throws ContradictionException {
        if (idx == 0) {
            reviseV1();
        } else
            reviseV0();
    }
    //propagate();

    /**
     * Propagation when a minimal bound of a variable was modified.
     *
     * @param idx The index of the variable.
     * @throws choco.kernel.solver.ContradictionException
     *
     */
    // Note: these methods could be improved by considering for each value, the minimal and maximal support considered into the nbEdges
    public void awakeOnInf(int idx) throws ContradictionException {
        if (idx == 0) {
            reviseV1();
        } else
            reviseV0();
    }

    public void awakeOnSup(int idx) throws ContradictionException {
        if (idx == 0) {
            reviseV1();
        } else
            reviseV0();
    }


    /**
     * Propagation when a variable is instantiated.
     *
     * @param idx The index of the variable.
     * @throws choco.kernel.solver.ContradictionException
     *
     */

    public void awakeOnInst(int idx) throws ContradictionException {
        if (idx == 0) {
            reviseV1();
        } else
            reviseV0();
    }


    public AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
        BinRelation rela2 = (BinRelation) ((ConsistencyRelation) relation).getOpposite();
        AbstractSConstraint ct = new AC3BinSConstraint(v0, v1, rela2);
        return ct;
    }

    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("AC3(").append(v0.pretty()).append(", ").append(v1.pretty()).append(", ").
                append(this.relation.getClass().getSimpleName()).append(")");
        return sb.toString();
    }
}
