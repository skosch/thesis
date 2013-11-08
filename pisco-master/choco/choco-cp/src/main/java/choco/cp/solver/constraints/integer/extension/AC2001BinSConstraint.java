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
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.extension.BinRelation;
import choco.kernel.solver.constraints.integer.extension.CspBinSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class AC2001BinSConstraint extends CspBinSConstraint {

    protected IStateInt[] currentSupport0;
    protected IStateInt[] currentSupport1;

    protected int offset0;
    protected int offset1;

    public AC2001BinSConstraint(IntDomainVar x0, IntDomainVar x1, BinRelation relation, IEnvironment environment) {
        super(x0, x1, relation);
        offset0 = x0.getInf();
        offset1 = x1.getInf();
        currentSupport0 = new IStateInt[x0.getSup() - x0.getInf() + 1];
        currentSupport1 = new IStateInt[x1.getSup() - x1.getInf() + 1];
        for (int i = 0; i < currentSupport0.length; i++) {
            currentSupport0[i] = environment.makeInt();
            currentSupport0[i].set(-1);
        }
        for (int i = 0; i < currentSupport1.length; i++) {
            currentSupport1[i] = environment.makeInt();
            currentSupport1[i].set(-1);
        }
    }

    public final int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
    }

//    public Object clone() {
//        return new AC2001BinSConstraint(this.v0, this.v1, this.relation, solver.getEnvironment());
//    }

    // updates the support for all values in the domain of v1, and remove unsupported values for v1
    public void reviseV1() throws ContradictionException {
        DisposableIntIterator itv1 = v1.getDomain().getIterator();
        int left = Integer.MIN_VALUE;
        int right = left;
        while (itv1.hasNext()) {
            int x = itv1.next();
            if (!v0.canBeInstantiatedTo(currentSupport1[x - offset1].get())) {
                boolean found = false;
                int support = currentSupport1[x - offset1].get();
                int max1 = v0.getSup();
                while (!found && support < max1) {
                    support = v0.getDomain().getNextValue(support);
                    if (relation.isConsistent(support, x)) found = true;
                }
                if (found){
                    currentSupport1[x - offset1].set(support);
                }else {
                    if (x == right + 1) {
                        right = x;
                    } else {
                        v1.removeInterval(left, right, this, false);
                        left = right = x;
                    }
//                    v1.removeVal(y, this, false);
                }
            }
        }
        v1.removeInterval(left, right, this, false);
        itv1.dispose();
    }

    // updates the support for all values in the domain of v0, and remove unsupported values for v0
    public void reviseV0() throws ContradictionException {
        DisposableIntIterator itv0 = v0.getDomain().getIterator();
        int left = Integer.MIN_VALUE;
        int right = left;
        while (itv0.hasNext()) {
            int x = itv0.next();
            if (!v1.canBeInstantiatedTo(currentSupport0[x - offset0].get())) {
                boolean found = false;
                int support = currentSupport0[x - offset0].get();
                int max2 = v1.getSup();
                while (!found && support < max2) {
                    support = v1.getDomain().getNextValue(support);
                    if (relation.isConsistent(x, support)) found = true;
                }
                if (found)
                    currentSupport0[x - offset0].set(support);
                else {
                    if (x == right + 1) {
                        right = x;
                    } else {
                        v0.removeInterval(left, right, this, false);
                        left = right = x;
                    }
//                    v0.removeVal(x, this, false);
                }
            }
        }
        v0.removeInterval(left, right, this, false);
        itv0.dispose();
    }

    public void awake() throws ContradictionException {
        DisposableIntIterator itv0 = v0.getDomain().getIterator();
        int support = 0;
        boolean found = false;
        int left = Integer.MIN_VALUE;
        int right = left;
        while (itv0.hasNext()) {
            DisposableIntIterator itv1 = v1.getDomain().getIterator();
            int val0 = itv0.next();
            while (itv1.hasNext()) {
                int val1 = itv1.next();
                if (relation.isConsistent(val0, val1)) {
                    support = val1;
                    found = true;
                    break;
                }
            }
            itv1.dispose();
            if (!found) {
                if (val0 == right + 1) {
                    right = val0;
                } else {
                    v0.removeInterval(left, right, this, false);
                    left = val0;
                    right = val0;
                }
//                v0.removeVal(val0, this, false);
            } else
                currentSupport0[val0 - offset0].set(support);

            found = false;
        }
        v0.removeInterval(left, right, this, false);
        itv0.dispose();
        found = false;
        right = left = Integer.MIN_VALUE;
        DisposableIntIterator itv1 = v1.getDomain().getIterator();
        while (itv1.hasNext()) {
            itv0 = v0.getDomain().getIterator();
            int val1 = itv1.next();
            while (itv0.hasNext()) {
                int val0 = itv0.next();
                if (relation.isConsistent(val0, val1)) {
                    support = val0;
                    found = true;
                    break;
                }
            }
            itv0.dispose();
            if (!found) {
                if (val1 == right + 1) {
                    right = val1;
                } else {
                    v1.removeInterval(left, right, this, false);
                    left = val1;
                    right = val1;
                }
//                v1.removeVal(val1, this, false);
            } else
                currentSupport1[val1 - offset1].set(support);
            found = false;
        }
        v1.removeInterval(left, right, this, false);
        itv1.dispose();
        //propagate();
    }

    public void propagate() throws ContradictionException {
        reviseV0();
        reviseV1();
    }

    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
        if (idx == 0)
            reviseV1();
        else
            reviseV0();
    }

    public void awakeOnInf(int idx) throws ContradictionException {
        if (idx == 0)
            reviseV1();
        else
            reviseV0();
    }

    public void awakeOnSup(int idx) throws ContradictionException {
        if (idx == 0)
            reviseV1();
        else
            reviseV0();
    }

    public void awakeOnRem(int idx, int x) throws ContradictionException {
        if (idx == 0)
            reviseV1();
        else
            reviseV0();
    }

    public void awakeOnBounds(int varIndex) throws ContradictionException {
        if (varIndex == 0)
            reviseV1();
        else
            reviseV0();
    }

    public void awakeOnInst(int idx) throws ContradictionException {
        if (idx == 0) {
            int value = v0.getVal();
            int left = Integer.MIN_VALUE;
            int right = left;
            DisposableIntIterator itv1 = v1.getDomain().getIterator();
            while (itv1.hasNext()) {
                int val = itv1.next();
                if (!relation.isConsistent(value, val)) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        v1.removeInterval(left, right, this, false);
                        left = val;
                        right = val;
                    }
//                    v1.removeVal(val, this, false);
                }
            }
            v1.removeInterval(left, right, this, false);
            itv1.dispose();
        } else {
            int value = v1.getVal();
            int left = Integer.MIN_VALUE;
            int right = left;
            DisposableIntIterator itv0 = v0.getDomain().getIterator();
            while (itv0.hasNext()) {
                int val = itv0.next();
                if (!relation.isConsistent(val, value)) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        v0.removeInterval(left, right, this, false);
                        left = val;
                        right = val;
                    }
//                    v0.removeVal(val, this, false);
                }
            }
            v0.removeInterval(left, right, this, false);
            itv0.dispose();
        }
    }

    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("AC2001(").append(v0.pretty()).append(", ").append(v1.pretty()).append(", ").
                append(this.relation.getClass().getSimpleName()).append(")");
        return sb.toString();
    }
}
