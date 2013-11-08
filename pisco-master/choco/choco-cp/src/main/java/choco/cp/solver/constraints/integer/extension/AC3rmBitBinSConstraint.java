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

import choco.cp.solver.variables.integer.BitSetIntDomain;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.extension.ConsistencyRelation;
import choco.kernel.solver.constraints.integer.extension.CouplesBitSetTable;
import choco.kernel.solver.constraints.integer.extension.CspBinSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/*
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: Jul 29, 2008
 * Since : Choco 2.0.0
 *
 */
public final class AC3rmBitBinSConstraint extends CspBinSConstraint {

    protected int offset0;
    protected int offset1;

    protected int minS0;    //value with minimum number of supports for v0
    protected int minS1;    //value with minimum number of supports for v1

    protected int initDomSize0;
    protected int initDomSize1;

    protected BitSetIntDomain v0Domain, v1Domain;

    public AC3rmBitBinSConstraint(IntDomainVar x0, IntDomainVar x1, CouplesBitSetTable relation) {
        super(x0, x1, relation);
        v0Domain = (BitSetIntDomain) v0.getDomain();
        v1Domain = (BitSetIntDomain) v1.getDomain();
    }

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
    }

    public void fastInitNbSupports() {
        int[] initS1 = new int[v1.getSup() - v1.getInf() + 1];
        minS0 = Integer.MAX_VALUE;
        minS1 = Integer.MAX_VALUE;
        DisposableIntIterator itv0 = v0.getDomain().getIterator();
        while (itv0.hasNext()) {
            int val0 = itv0.next();
            int initS0 = 0;
            DisposableIntIterator itv1 = v1.getDomain().getIterator();
            while (itv1.hasNext()) {
                int val1 = itv1.next();
                if (relation.isConsistent(val0, val1)) {
                    initS0++;
                    initS1[val1 - offset1]++;
                }
            }
            if (initS0 < minS0) minS0 = initS0;
            itv1.dispose();
        }
        itv0.dispose();
        for (int i = 0; i < initS1.length; i++) {
            if (initS1[i] < minS1) minS1 = initS1[i];
        }
    }

    public Object clone() {
        return new AC3rmBitBinSConstraint(this.v0, this.v1, (CouplesBitSetTable) this.relation);
    }

    public AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
        return new AC3rmBitBinSConstraint(this.v0, this.v1, (CouplesBitSetTable) ((ConsistencyRelation) this.relation).getOpposite());
    }

    // updates the support for all values in the domain of v1, and remove unsupported values for v1
    public void reviseV1() throws ContradictionException {
        int v0Size = v0Domain.getSize();
        if (minS1 <= (initDomSize0 - v0Size)) {
            int left = Integer.MIN_VALUE;
            int right = left;
            DisposableIntIterator itv1 = v1Domain.getIterator();
            try {
                while (itv1.hasNext()) {
                    int y = itv1.next();
                    if (!((CouplesBitSetTable) relation).checkValue(1, y, v0Domain)) {
                        if (y == right + 1) {
                            right = y;
                        } else {
                            v1.removeInterval(left, right, this, false);
                            left = right = y;
                        }
//                        v1.removeVal(y, this, false);
                    }
                }
                v1.removeInterval(left, right, this, false);
            } finally {
                itv1.dispose();
            }
        }
    }

    // updates the support for all values in the domain of v0, and remove unsupported values for v0
    public void reviseV0() throws ContradictionException {
        int v1Size = v1Domain.getSize();
        if (minS0 <= (initDomSize1 - v1Size)) {
            int left = Integer.MIN_VALUE;
            int right = left;
            DisposableIntIterator itv0 = v0Domain.getIterator();
            try {
                while (itv0.hasNext()) {
                    int x = itv0.next();
                    if (!((CouplesBitSetTable) relation).checkValue(0, x, v1Domain)) {
                        if (x == right + 1) {
                            right = x;
                        } else {
                            v0.removeInterval(left, right, this, false);
                            left = right = x;
                        }
//                        v0.removeVal(x, this, false);
                    }
                }
                v0.removeInterval(left, right, this, false);
            } finally {
                itv0.dispose();
            }
        }
    }


    public void init() {
        offset0 = v0.getInf();
        offset1 = v1.getInf();

        initDomSize0 = v0.getDomainSize();
        initDomSize1 = v1.getDomainSize();

        fastInitNbSupports();
    }

    public void awake() throws ContradictionException {
        init();
        int left = Integer.MIN_VALUE;
        int right = left;
        DisposableIntIterator itv0 = v0Domain.getIterator();
        try {
            while (itv0.hasNext()) {
                int val0 = itv0.next();
                if (!((CouplesBitSetTable) relation).checkValue(0, val0, v1Domain)) {
                    if (val0 == right + 1) {
                        right = val0;
                    } else {
                        v0.removeInterval(left, right, this, false);
                        left = right = val0;
                    }
//                    v0.removeVal(val0, this, false);
                }
            }
            v0.removeInterval(left, right, this, false);
        } finally {
            itv0.dispose();
        }
        itv0 = v1Domain.getIterator();
        left = right = Integer.MIN_VALUE;
        try {
            while (itv0.hasNext()) {
                int val1 = itv0.next();
                if (!((CouplesBitSetTable) relation).checkValue(1, val1, v0Domain)) {
                    if (val1 == right + 1) {
                        right = val1;
                    } else {
                        v1.removeInterval(left, right, this, false);
                        left = right = val1;
                    }
//                    v1.removeVal(val1, this, false);
                }
            }
            v1.removeInterval(left, right, this, false);
        } finally {
            itv0.dispose();
        }
    }

    public void propagate() throws ContradictionException {
        reviseV0();
        reviseV1();
    }

    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
        revise(idx);
    }

    public void awakeOnInf(int idx) throws ContradictionException {
        revise(idx);
    }

    public void awakeOnSup(int idx) throws ContradictionException {
        revise(idx);
    }

    public void awakeOnRem(int idx, int x) throws ContradictionException {
        revise(idx);
    }

    public void awakeOnBounds(int varIndex) throws ContradictionException {
        revise(varIndex);
    }

    public void revise(int idx) throws ContradictionException {
        if (idx == 0)
            reviseV1();
        else
            reviseV0();
    }

    public void awakeOnInst(int idx) throws ContradictionException {
        if (idx == 0) {
            int value = v0.getVal();
            int left = Integer.MIN_VALUE;
            int right = left;
            DisposableIntIterator itv1 = v1Domain.getIterator();
            try {
                while (itv1.hasNext()) {
                    int val = itv1.next();
                    if (!relation.isConsistent(value, val)) {
                        if (val == right + 1) {
                            right = val;
                        } else {
                            v1.removeInterval(left, right, this, false);
                            left = right = val;
                        }
//						v1.removeVal(val, this, false);
                    }
                }
                v1.removeInterval(left, right, this, false);
            } finally {
                itv1.dispose();
            }
        } else {
            int value = v1.getVal();
            int left = Integer.MIN_VALUE;
            int right = left;
            DisposableIntIterator itv0 = v0Domain.getIterator();
            try {
                while (itv0.hasNext()) {
                    int val = itv0.next();
                    if (!relation.isConsistent(val, value)) {
                        if (val == right + 1) {
                            right = val;
                        } else {
                            v0.removeInterval(left, right, this, false);
                            left = right = val;
                        }
//                        v0.removeVal(val, this, false);
                    }
                }
                v0.removeInterval(left, right, this, false);
            } finally {
                itv0.dispose();
            }
        }
    }

    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("AC3rmBitSet(").append(v0.pretty()).append(", ").append(v1.pretty()).append(", ").
                append(this.relation.getClass().getSimpleName()).append(")");
        return sb.toString();
    }
}

