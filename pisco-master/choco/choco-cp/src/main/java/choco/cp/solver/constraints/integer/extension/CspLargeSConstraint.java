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
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.constraints.integer.extension.ConsistencyRelation;
import choco.kernel.solver.constraints.integer.extension.LargeRelation;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class CspLargeSConstraint extends AbstractLargeIntSConstraint {

    protected LargeRelation relation;

    protected int[] currentTuple;

    public CspLargeSConstraint(IntDomainVar[] vs, LargeRelation relation) {
        super(ConstraintEvent.QUADRATIC, vs);
        this.relation = relation;
        this.currentTuple = new int[vs.length];
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        CspLargeSConstraint newc = (CspLargeSConstraint) super.clone();
        newc.currentTuple = new int[this.currentTuple.length];
        System.arraycopy(this.currentTuple, 0, newc.currentTuple, 0, this.currentTuple.length);
        return newc;
    }

    public LargeRelation getRelation() {
        return relation;
    }

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
    }

    @Override
    public void propagate() throws ContradictionException {
        boolean stop = false;
        int nbUnassigned = 0;
        int index = -1, i = 0;
        while (!stop && i < vars.length) {
            if (!vars[i].isInstantiated()) {
                nbUnassigned++;
                index = i;
            } else {
                currentTuple[i] = vars[i].getVal();
            }
            if (nbUnassigned > 1) {
                stop = true;
            }
            i++;
        }
        if (!stop) {
            if (nbUnassigned == 1) {
                int left = Integer.MIN_VALUE;
                int right = left;
                DisposableIntIterator it = vars[index].getDomain().getIterator();
                try {
                    while (it.hasNext()) {
                        int val = currentTuple[index] = it.next();
                        if (!relation.isConsistent(currentTuple)) {
                            if (val == right + 1) {
                                right = val;
                            } else {
                                vars[index].removeInterval(left, right, this, false);
                                left = right = val;
                            }
//                            vars[index].removeVal(currentTuple[index], this, false);
                        }
                    }
                    vars[index].removeInterval(left, right, this, false);
                } finally {
                    it.dispose();
                }
            } else {
                if (!relation.isConsistent(currentTuple)) {
                    this.fail();
                }
            }
        }
    }

    @Override
    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
        this.constAwake(false);
    }

    @Override
    public void awakeOnBounds(int varIndex) throws ContradictionException {
        this.constAwake(false);
    }

    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        this.constAwake(false);
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        return relation.isConsistent(tuple);
    }

    @Override
    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("CSPLarge({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            IntDomainVar var = vars[i];
            sb.append(var + ", ");
        }
        sb.append("})");
        return sb.toString();
    }

    @Override
    public AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
        LargeRelation rela2 = (LargeRelation) ((ConsistencyRelation) relation).getOpposite();
        AbstractSConstraint ct = new CspLargeSConstraint(vars, rela2);
        return ct;
    }

    @Override
    public Boolean isEntailed() {
        throw new UnsupportedOperationException("isEntailed not yet implemented in CspLargeConstraint");
    }

}
