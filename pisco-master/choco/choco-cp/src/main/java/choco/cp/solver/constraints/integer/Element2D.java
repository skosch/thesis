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

package choco.cp.solver.constraints.integer;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractTernIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;

/**
 * Ensure that lvals[v0][v1] = v2 where lvals is an int[][]
 * User: hcambaza
 * Date: 16-Jan-2007
 * Time: 18:54:33
 */
public final class Element2D extends AbstractTernIntSConstraint {

    protected int[][] lvals;
    protected int dim1;
    protected int dim2;
    protected int cste;

    /**
     * 2D Element constraint
     * that lvals[v0][v1] = v2 where lvals is an int[][]
     *
     * @param v0    index1
     * @param v1    index2
     * @param v2    valeur
     * @param lvals
     */
    public Element2D(IntDomainVar v0, IntDomainVar v1, IntDomainVar v2, int[][] lvals) {
        super(v0, v1, v2);
        this.lvals = lvals;
        this.dim1 = lvals.length;
        this.dim2 = lvals[0].length;
        this.cste = 0;
        for (int i = 0; i < dim1; i++) {
            for (int j = 0; j < dim2; j++) {
                if (lvals[i][j] < 0 && lvals[i][j] < -cste) {
                    this.cste = -lvals[i][j];
                }
            }
        }
    }

    @Override
    public int getFilteredEventMask(int idx) {
        if (idx == 0) {
            if (v0.hasEnumeratedDomain()) {
                return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
            } else {
                return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
            }
        } else if (idx == 1) {
            if (v1.hasEnumeratedDomain()) {
                return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
            } else {
                return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
            }
        } else {
            if (v2.hasEnumeratedDomain()) {
                return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
            } else {
                return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
            }
        }
    }


    public void updateValueFromIndex() throws ContradictionException {
        int minVal = Integer.MAX_VALUE, maxVal = Integer.MIN_VALUE, val;
        int UB0 = v0.getSup();
        int UB1;
        for (int i = v0.getInf(); i <= UB0; i = v0.getNextDomainValue(i)) {
            //while (v0It.hasNext()) {
            //    int i = v0It.next();
            UB1 = v1.getSup();
            for (int j = v1.getInf(); j <= UB1; j = v1.getNextDomainValue(j)) {
                val = lvals[i][j];
                if (minVal > val) minVal = val;
                if (maxVal < val) maxVal = val;
            }
        }
        v2.updateSup(maxVal, this, false);
        v2.updateInf(minVal, this, false);
        // propagate on holes
        if (v2.hasEnumeratedDomain()) {
            BitSet feasValues = new BitSet(v2.getDomainSize());
            UB0 = v0.getSup();
            for (int i = v0.getInf(); i <= UB0; i = v0.getNextDomainValue(i)) {
                UB1 = v1.getSup();
                for (int j = v1.getInf(); j <= UB1; j = v1.getNextDomainValue(j)) {
                    feasValues.set(lvals[i][j] + cste);
                }
            }
            int UB2 = v2.getSup();
            for (int i = v2.getInf(); i <= UB2; i = v2.getNextDomainValue(i)) {  // on parcourt la valeur
                if (!feasValues.get(i + cste))
                    v2.removeVal(i, this, false);
            }
        }
    }

    public boolean testValueVarV0(int idx) {
        boolean ret = false;
        int ub = v1.getSup();
        for (int val = v1.getInf(); !ret && val <= ub; val = v1.getNextDomainValue(val)) {
            ret = v2.canBeInstantiatedTo(lvals[idx][val]);
        }
        return ret;
    }

    public boolean testValueVarV1(int idx) {
        boolean ret = false;
        int ub = v0.getSup();
        for (int val = v0.getInf(); !ret && val <= ub; val = v0.getNextDomainValue(val)) {
            ret = v2.canBeInstantiatedTo(lvals[val][idx]);
        }
        return ret;
    }

    public void updateIndexFromValue() throws ContradictionException {
        int minFeasibleIndex1 = v0.getInf(), minFeasibleIndex2 = v1.getInf();
        int maxFeasibleIndex1 = v0.getSup(), maxFeasibleIndex2 = v1.getSup();
        int thecause1 = -1, thecause2 = -1;
        if (v2.hasEnumeratedDomain()) thecause1 = getConstraintIdx(0);
        if (v2.hasEnumeratedDomain()) thecause2 = getConstraintIdx(1);

        if (v0.hasEnumeratedDomain()) {
            IntDomain v0Dom = this.v0.getDomain();
            for (int i = v0Dom.getNextValue(minFeasibleIndex1 - 1); i <= maxFeasibleIndex1; i = v0Dom.getNextValue(i)) {
                if (!testValueVarV0(i)) {
                    this.v0.removeVal(i, this, false);
                }
            }
        } else {
            // update index1
            //DisposableIntIterator v0It = this.v0.getDomain().getIterator();
            int UB0 = v0.getSup();
            for (int val = v0.getInf(); val <= UB0; val = v0.getNextDomainValue(val)) {
                if (!testValueVarV0(val)) minFeasibleIndex1 = val;
                else break;
            }
            v0.updateInf(minFeasibleIndex1, this, false);

            // Todo : update the prevValue api on BitSetIntDomain to perform a more efficient iteration
            while ((maxFeasibleIndex1 > 0) && v0.canBeInstantiatedTo(maxFeasibleIndex1) &&
                    !testValueVarV0(maxFeasibleIndex1))
                maxFeasibleIndex1--;
            v0.updateSup(maxFeasibleIndex1, this, false);
        }

        if (v1.hasEnumeratedDomain()) {
            IntDomain v1Dom = this.v1.getDomain();
            for (int i = v1Dom.getNextValue(minFeasibleIndex2 - 1); i <= maxFeasibleIndex2; i = v1Dom.getNextValue(i)) {
                if (!testValueVarV1(i)) {
                    this.v1.removeVal(i, this, false);
                }
            }
        } else {
            // update index2
            DisposableIntIterator v1It = this.v1.getDomain().getIterator();
            while (v1It.hasNext()) {
                int v1val = v1It.next();
                if (!testValueVarV1(v1val)) minFeasibleIndex2 = v1val;
                else break;
            }
            v1It.dispose();
            v1.updateInf(minFeasibleIndex2, this, false);

            // Todo : update the prevValue api on BitSetIntDomain to perform a more efficient iteration
            while ((maxFeasibleIndex2 > 0) && v1.canBeInstantiatedTo(maxFeasibleIndex2) &&
                    !testValueVarV1(maxFeasibleIndex2))
                maxFeasibleIndex2--;
            v1.updateSup(maxFeasibleIndex2, this, false);
        }

    }

    public void propagate() throws ContradictionException {
        v0.updateInf(0, this, false);
        v1.updateInf(0, this, false);
        v0.updateSup(dim1 - 1, this, false);
        v1.updateSup(dim2 - 1, this, false);
        updateIndexFromValue();
        updateValueFromIndex();
    }

    public void awakeOnInf(int idx) throws ContradictionException {
        if (idx <= 1)
            updateValueFromIndex();
        else
            updateIndexFromValue();
    }

    public void awakeOnSup(int idx) throws ContradictionException {
        if (idx <= 1)
            updateValueFromIndex();
        else
            updateIndexFromValue();
    }

    public void awakeOnBounds(int idx) throws ContradictionException {
        if (idx <= 1)
            updateValueFromIndex();
        else
            updateIndexFromValue();
    }

    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
        if (idx <= 1)
            updateValueFromIndex();
        else
            updateIndexFromValue();
    }

    public void awakeOnInst(int idx) throws ContradictionException {
        if (idx <= 1)
            updateValueFromIndex();
        else
            updateIndexFromValue();
    }

    public Boolean isEntailed() {
        if (this.v2.isInstantiated()) {
            boolean b = true;
            int ub0 = v0.getSup();
            for (int v0val = v0.getInf(); b && v0val <= ub0; v0val = v0.getNextDomainValue(v0val)) {
                int ub1 = v1.getSup();
                for (int v1val = v1.getInf(); b && v1val <= ub1; v1val = v1.getNextDomainValue(v1val)) {
                    b &= (lvals[v0val][v1val] == v2.getVal());
                }
            }
            if (b) return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    public boolean isSatisfied(int[] tuple) {
        return lvals[tuple[0]][tuple[1]] == tuple[2];
    }

    public String pretty() {
        return (this.v2.pretty() + " = nth(" + this.v0.pretty() + ", " + this.v1.pretty() + ", " + StringUtils.pretty(this.lvals) + ")");
    }
}
