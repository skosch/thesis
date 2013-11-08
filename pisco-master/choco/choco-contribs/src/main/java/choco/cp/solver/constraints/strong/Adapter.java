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

package choco.cp.solver.constraints.strong;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

//FIXME should not work anymore 
public class Adapter extends AbstractIntSConstraint implements ISpecializedConstraint {

    private static final int[] tuple = new int[2];

    private final AbstractIntSConstraint sConstraint;

    public Adapter(AbstractIntSConstraint sConstraint) {
        super(ConstraintEvent.LINEAR, new IntDomainVar[]{});
        this.sConstraint = sConstraint;
    }

    public int firstSupport(int position, int value) {
        tuple[position] = value;

        final DisposableIntIterator itr = getVar(1 - position).getDomain()
                .getIterator();

        while (itr.hasNext()) {
            tuple[1 - position] = itr.next();
            if (check(tuple)) {
                itr.dispose();
                return tuple[1 - position];
            }
        }

        itr.dispose();
        return Integer.MAX_VALUE;
    }

    public int nextSupport(int position, int value, int lastSupport) {
        tuple[position] = value;
        final IntDomainVar iterateOver = getVar(1 - position);
        tuple[1 - position] = iterateOver.getNextDomainValue(lastSupport);

        while (tuple[1 - position] < Integer.MAX_VALUE && !check(tuple)) {
            tuple[1 - position] = iterateOver
                    .getNextDomainValue(tuple[1 - position]);
        }
        return tuple[1 - position];
    }

    @Override
    public String toString() {
        return sConstraint.toString();
    }

//    @Override
//    public int getConstraintIdx(int idx) {
//        return sConstraint.getConstraintIdx(idx);
//    }
//
//    @Override
//    public int getNbVars() {
//        return sConstraint.getNbVars();
//    }
//
//    @Override
//    public void setConstraintIndex(int i, int idx) {
//        sConstraint.setConstraintIndex(i, idx);
//    }
//
//    @Override
//    public void setVar(int i, IntDomainVar v) {
//        sConstraint.setVar(i, v);
//    }

    @Override
    public String pretty() {
        return sConstraint.pretty();
    }

    @Override
    public boolean isSatisfied() {
        return sConstraint.isSatisfied();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean check(int[] tuple) {
        return sConstraint.isSatisfied(tuple);
    }

//    @Override
//    public IntDomainVar getVar(int i) {
//        return sConstraint.getVar(i);
//    }
//
//    @Override
//    public boolean isCompletelyInstantiated() {
//        return sConstraint.isCompletelyInstantiated();
//    }

    @Override
    public boolean isConsistent() {
        return sConstraint.isConsistent();
    }

    @Override
    public void propagate() throws ContradictionException {
        sConstraint.propagate();
    }

}