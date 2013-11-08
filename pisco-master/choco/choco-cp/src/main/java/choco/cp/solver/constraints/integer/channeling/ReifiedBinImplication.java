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

package choco.cp.solver.constraints.integer.channeling;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractTernIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 2 nov. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*/
public final class ReifiedBinImplication extends AbstractTernIntSConstraint {

    /**
     * A constraint to ensure :
     * b = v1 xnor v2
     */
    public ReifiedBinImplication(IntDomainVar b, IntDomainVar v1, IntDomainVar v2) {
        super(b, v1, v2);
    }

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK;
    }

    private boolean updateOnV0(int val) throws ContradictionException {
        boolean mod = false;
        if (val == 0) {
            mod = mod | v1.instantiate(1, this, false);
            mod |=v2.instantiate(0, this, false);
        } else {
            if (v1.isInstantiatedTo(0)) {
                setEntailed();
            } else if (v1.isInstantiatedTo(1)) {
                mod = mod | v2.instantiate(1, this, false);
            }
        }
        return mod;
    }

    private void updateOnV1(int val) throws ContradictionException {
        if (val == 0) {
            v0.instantiate(1, this, false);
        } else {
            if (v0.isInstantiated()) {
                v2.instantiate(v0.getVal(), this, false);
            } else if (v2.isInstantiated()) {
                v0.instantiate(v2.getVal(), this, false);
            }
        }
    }

    private void updateOnV2(int val) throws ContradictionException {
        if (val == 0) {
            if (v0.isInstantiated()) {
                v1.instantiate(Math.abs(v0.getVal() - 1), this, false);
            } else if (v1.isInstantiated()) {
                v0.instantiate(Math.abs(v1.getVal() - 1), this, false);
            }
        } else {
            v0.instantiate(1, this, false);
            setEntailed();
        }
    }

    public void propagate() throws ContradictionException {
        if (v0.isInstantiated()) {
            updateOnV0(v0.getVal());
        }
        if (v1.isInstantiated()) {
            updateOnV1(v1.getVal());
        }
        if (v2.isInstantiated()) {
            updateOnV2(v2.getVal());
        }
    }


    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        switch (idx) {
            case 0:
                updateOnV0(v0.getVal());
                break;
            case 1:
                updateOnV1(v1.getVal());
                break;
            case 2:
                updateOnV2(v2.getVal());
                break;
        }
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


    public boolean isSatisfied(int[] tuple) {
        if(tuple[0] == 0){
            return tuple[1] == 1 && tuple[2] == 0;
        }else{
            return tuple[1] <= tuple[2];
        }
    }

    public Boolean isEntailed() {

        return null;
    }
}
