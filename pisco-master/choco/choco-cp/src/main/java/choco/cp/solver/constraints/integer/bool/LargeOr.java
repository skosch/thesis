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
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

/*
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: Aug 6, 2008
 * Since : Choco 2.0.0
 *
 * maintain v1 OR v2 OR ... OR vn where v1, v2, ..., vn are boolean variables
* i.e variables of domain {0,1}
 */
public final class LargeOr extends AbstractLargeIntSConstraint {


    /**
     * Nb literals set to 0 (false).
     */
    private final IStateInt toZERO;


    /**
     * A constraint to ensure :
     * b = OR_{i} vars[i]
     *
     * @param vars
     * @param environment
     */
    LargeOr(IntDomainVar[] vars, IEnvironment environment) {
        super(ConstraintEvent.LINEAR, vars);
        toZERO = environment.makeInt(0);
    }

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK;
    }

    public void propagate() throws ContradictionException {
        int toZERO = 0;
        int lastIdx = 0;
        for(int i = 0; i < vars.length; i++){
            if(vars[i].isInstantiatedTo(1)){
                setEntailed();
                return;
            }else if(vars[i].isInstantiatedTo(0)){
                toZERO++;
            }else{
                lastIdx = i;
            }
        }
        if(toZERO == vars.length){
            this.fail();
        }else if((toZERO == vars.length - 1)){
            vars[lastIdx].instantiate(1, this, false);
            setEntailed();
            return;
        }
        this.toZERO.set(toZERO);
    }

    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        int val = vars[idx].getVal();
        switch (val){
            case 1:
                setEntailed();
                break;
            case 0:
                toZERO.add(1);
                // 1 var inconnue
                if(toZERO.get()>= vars.length-1){
                    filter();
                }
                break;
        }
    }

    private void filter() throws ContradictionException {
        int toZero = toZERO.get();
        int n = vars.length;
        if(toZero == n){
            this.fail();
        }else{
            for(int i = 0; i < n; i++){
                if(!vars[i].isInstantiated()){
                    vars[i].instantiate(1, this, false);
                    setEntailed();
                    break;
                }
                // speed up
                else if(!vars[(n-1)-i].isInstantiated()){
                    vars[(n-1)-i].instantiate(1, this, false);
                    setEntailed();
                    break;
                }
            }
        }
    }

    public void awakeOnInf(int varIdx) throws ContradictionException {
    }

    public void awakeOnSup(int varIdx) throws ContradictionException {
    }

    public void awakeOnBounds(int varIndex) throws ContradictionException {
    }

    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {

    }

    public boolean isSatisfied(int[] tuple) {
        for (int i = 0; i < tuple.length; i++) {
            if (tuple[i] == 1) return true;
        }
        return false;
    }

    public Boolean isEntailed() {
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].isInstantiatedTo(1))
                return Boolean.TRUE;
        }
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].fastCanBeInstantiatedTo(1))
                return null;
        }
        return Boolean.FALSE;
    }

    /**
     * Get the opposite constraint
     *
     * @return the opposite constraint  @param solver
     */
    @Override
    public AbstractSConstraint<IntDomainVar> opposite(final Solver solver) {
        return BooleanFactory.nor(vars);
    }

}
