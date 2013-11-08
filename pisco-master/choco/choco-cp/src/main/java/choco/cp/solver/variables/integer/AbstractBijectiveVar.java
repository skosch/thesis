/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
package choco.cp.solver.variables.integer;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.structure.PartiallyStoredIntVector;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.event.VarEvent;
import choco.kernel.solver.variables.AbstractVar;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/05/11
 */
public abstract class AbstractBijectiveVar extends AbstractVar implements IntDomainVar {

    final IntDomainVar variable;


    /**
     * Build a variable Y such as Y = X op c.
     *
     * @param solver   The model this variable belongs to
     * @param name     The name of the variable
     * @param variable constraints stored specific structure
     */
    public AbstractBijectiveVar(final Solver solver, String name, IntDomainVar variable) {
        super(solver, name, null);
        this.variable = variable;
    }

    @Override
    public void wipeOut() throws ContradictionException {
        variable.wipeOut();
    }

    @Override
    public int fastNextDomainValue(int i) {
        return variable.getNextDomainValue(i);
    }

    @Override
    public int fastPrevDomainValue(int i) {
        return variable.getPrevDomainValue(i);
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return variable.hasEnumeratedDomain();
    }

    @Override
    public boolean hasBooleanDomain() {
        return variable.hasBooleanDomain();
    }

    @Override
    public IntDomain getDomain() {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean canBeEqualTo(IntDomainVar x) {
        if (x.getInf() <= this.getSup()) {
            if (this.getInf() <= x.getSup()) {
                if (!this.hasEnumeratedDomain() || !x.hasEnumeratedDomain())
                    return true;
                else {
                    DisposableIntIterator it = this.getDomain().getIterator();
                    for (; it.hasNext();) {
                        int v = it.next();
                        if (x.canBeInstantiatedTo(v))
                            return true;
                    }
                    it.dispose();
                    return false;
                }
            } else
                return false;
        } else
            return false;
    }

    @Override
    public int getDomainSize() {
        return variable.getDomainSize();
    }

    @Override
    public boolean isInstantiated() {
        return variable.isInstantiated();
    }

    @Override
    public VarEvent<? extends Var> getEvent() {
        return variable.getEvent();
    }

    @Override
    public SConstraint getConstraint(int i) {
        return variable.getConstraint(i);
    }

    @Override
    public int getNbConstraints() {
        return variable.getNbConstraints();
    }

    @Override
    public PartiallyStoredVector<? extends SConstraint> getConstraintVector() {
        return variable.getConstraintVector();
    }

    @Override
    public PartiallyStoredIntVector getIndexVector() {
        return variable.getIndexVector();
    }

    @Override
    public int getVarIndex(int constraintIndex) {
        return variable.getVarIndex(constraintIndex);
    }

    @Override
    public void eraseConstraint(SConstraint c) {
        ((AbstractVar) variable).eraseConstraint(c);
    }

    @Override
    public int addConstraint(SConstraint c, int varIdx, boolean dynamicAddition) {
        return variable.addConstraint(c, varIdx, dynamicAddition);
    }

    @Override
    public DisposableIterator<SConstraint> getConstraintsIterator() {
        return variable.getConstraintsIterator();
    }
}