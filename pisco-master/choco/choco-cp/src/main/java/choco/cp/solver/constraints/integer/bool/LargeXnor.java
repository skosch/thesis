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
package choco.cp.solver.constraints.integer.bool;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 27 oct. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*
* maintain v1 XNOR v2 XNOR ... XNOR vn where v1, v2, ..., vn are boolean variables
* i.e variables of domain {0,1}
*/
public final class LargeXnor extends AbstractLargeIntSConstraint {

    /**
     * A constraint to ensure :
     * b = XNOR_{i} vars[i]
     *
     * @param vars boolean variables
     */
    LargeXnor(IntDomainVar[] vars) {
        super(ConstraintEvent.LINEAR, vars);
    }

    @Override
    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK;
        // return 0x0B;
    }

    /**
     * Default initial propagation: full constraint re-propagation.
     */
    @Override
    public void awake() throws ContradictionException {
        for(int i = 0; i < vars.length; i++){
            if(vars[i].isInstantiated()){
                filter(i);
                break;
            }
        }
    }

    private void filter(int idx) throws ContradictionException {
        int val = vars[idx].getVal();
        for(int i = 0; i < vars.length; i++){
            if(idx!=i){
                vars[i].instantiate(val, this, false);
            }
        }
    }

    @Override
    public void propagate() throws ContradictionException {
    }

    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        filter(idx);
    }

    @Override
    public void awakeOnInf(int varIdx) throws ContradictionException {

    }
    @Override
    public void awakeOnSup(int varIdx) throws ContradictionException {

    }
    @Override
    public void awakeOnBounds(int varIndex) throws ContradictionException {

    }
    @Override
    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {

    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        for (int aTuple : tuple) {
            if (aTuple != tuple[0]) return false;
        }
        return true;
    }

    @Override
    public Boolean isEntailed() {
        for (IntDomainVar var : vars) {
            if (var.isInstantiated()){
                if(var.getVal()!=vars[0].getVal()){
                    return Boolean.FALSE;
                }
            }else{
                return null;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * Get the opposite constraint
     *
     * @return the opposite constraint  @param solver
     */
    @Override
    public AbstractSConstraint<IntDomainVar> opposite(final Solver solver) {
        return BooleanFactory.xor(vars);
    }
}
