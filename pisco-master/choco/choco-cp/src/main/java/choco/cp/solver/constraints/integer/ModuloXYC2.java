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
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;


/**
 * Created by IntelliJ IDEA.
 * User: Richaud
 * Date: 9 janv. 2007
 * Time: 16:58:01
 * To change this template use File | Settings | File Templates.
 */
public final class ModuloXYC2 extends AbstractBinIntSConstraint {
    /**
     * The search constant of the constraint
     */
    protected final int m;
    /*  IStateBitSet[] x;
  IStateBitSet xm; */

    /**
     * Constructs the constraint with the specified variables and constant.
     *
     * @param x0 first IntDomainVar
     * @param x1 second IntDomainVar
     * @param c  The search constant used in the disequality.
     */

    public ModuloXYC2(IntDomainVar x0, IntDomainVar x1, int c) {
        super(x0, x1);
        this.m = c;
    }

    @Override
    public int getFilteredEventMask(int idx) {
        if(idx == 0){
            if(v0.hasEnumeratedDomain()){
                return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
            }else{
                return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
            }
        }else{
            if(v1.hasEnumeratedDomain()){
                return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
            }else{
                return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
            }
        }
    }



    public void awake() throws ContradictionException {
        v0.setInf(0);
        v0.setSup(m - 1);
        propagate();
    }

    /**
     * The one and only propagation method, using foward checking
     */

    public void propagate() throws ContradictionException {
        BitSet sup = searchSupports();
        if (v0.hasEnumeratedDomain()) {
            for (int j = sup.nextSetBit(0); j >= 0; j = sup.nextSetBit(j + 1)) {
                v0.removeVal(j, this, false);
            }
        } else {
            int min = sup.nextClearBit(0);
            int max = min;
            for (int j = min; j >= 0 && j < m; j = sup.nextClearBit(j + 1)) {
                max = j;
            }

            v0.updateSup(max, this, false);
            v0.updateInf(min, this, false);
        }

        if (v1.hasEnumeratedDomain()) {
            IntDomain dom = v1.getDomain();
            for (int val = dom.getInf(); val <= dom.getSup(); val = dom.getNextValue(val)) {
                if (!v0.canBeInstantiatedTo(val % m)) {
                    v1.removeVal(val, this, false);
                }
            }

        } else {
            v1.setInf(searchInfV1());
            v1.setSup(searchSupV1());
        }
    }

    //
    public BitSet searchSupportsP() {
        BitSet bs = new BitSet();
        IntDomain dom = v1.getDomain();
        for (int val = dom.getInf(); (val <= dom.getSup()) && bs.cardinality() < m; val = dom.getNextValue(val)) {
            bs.set(val % m);
        }
        return bs;
    }

    //
    public BitSet searchSupports() {
        BitSet bs = new BitSet();
        bs.set(0, m);
        IntDomain dom = v1.getDomain();
        for (int val = dom.getInf(); (val <= dom.getSup()) && bs.cardinality() > 0; val = dom.getNextValue(val)) {
            if (bs.get(val % m)) {
                //   supports[val % m].set(val);
                bs.clear(val % m);
            }
        }
        return bs;
    }



    // *****************************************************************************************
    // Modification d'une borne inf.      v0 = v1 mod C
    // *****************************************************************************************
    public void awakeOnInf(int idx) throws ContradictionException {
        if (idx == 0) {
            reviseV1OnInf();
        } else if (idx == 1) {
            reviseV0OnInf(searchSupports());
        }
    }

    // Modification de la borne inf. de v1
    public void reviseV0OnInf(BitSet supports) throws ContradictionException {
        if (v0.hasEnumeratedDomain()) {
            for (int i = supports.nextSetBit(0); i >= 0; i = supports.nextSetBit(i + 1)) {
                v0.removeVal(i, this, false);
            }
        } else {
            int min = supports.nextClearBit(0);
            v0.updateInf(min, this, false);
        }
    }

    // Modification de la borne inf. de v0
    public void reviseV1OnInf() throws ContradictionException {
        if (v1.hasEnumeratedDomain()) {
            IntDomain dom = v1.getDomain();
            int inf = v0.getInf();
            for (int val = dom.getInf(); val <= dom.getSup(); val = dom.getNextValue(val)) {
                if (val % m < inf) {
                    v1.removeVal(val, this, false);
                } /*else {
                    val = Math.max((val / m) * m + inf, dom.getInf());

                }    */
            }
        } else {
            v1.setInf(searchInfV1());
        }
    }

    public int searchInfV1() {
        int n = (v1.getInf() / m) * m;
        if (v1.getInf() % m > v0.getSup()) {
            return v0.getInf() + n + m;
        } else if (v1.getInf() % m == v0.getSup()) {
            return v1.getInf();
        } else if (v1.getInf() % m <= v0.getInf()) {
            return v0.getInf() + n;
        } else {
            IntDomain dom = v0.getDomain();
            int inf = dom.getNextValue((v1.getInf() % m)-1);
            return (inf + n);
        }
    }


    // *****************************************************************************************
    //Modification d'une borne sup.      v0 = v1 mod C
    // *****************************************************************************************
    public void awakeOnSup(int idx) throws ContradictionException {
        if (idx == 0) {
            reviseV1OnSup();
        } else if (idx == 1) {
            reviseV0OnSup(searchSupports());
        }
    }

    // Modification de la borne sup. de v1
    public void reviseV0OnSup(BitSet supports) throws ContradictionException {
        if (v0.hasEnumeratedDomain()) {
            for (int i = supports.nextSetBit(0); i >= 0; i = supports.nextSetBit(i + 1)) {
                v0.removeVal(i, this, false);
            }
        } else {
            int max = supports.nextClearBit(0);
            for (int j = max; j >= 0 && j < m; j = supports.nextClearBit(j + 1)) {
                max = j;
            }
            v0.updateSup(max, this, false);
        }
    }

    // Modification de la borne sup. de v0    
    public void reviseV1OnSup() throws ContradictionException {
        if (v1.hasEnumeratedDomain()) {
            IntDomain dom = v1.getDomain();
            int sup = v0.getSup();
            for (int val = dom.getInf(); val <= dom.getSup(); val = dom.getNextValue(val)) {
                if (val % m > sup) {
                    v1.removeVal(val, this, false);
                } /* else {
                    val = Math.min((val / m) * m + sup,dom.getSup());
                } */
            }
        } else {
            v1.setSup(searchSupV1());
        }
    }

    public int searchSupV1() {
        int n = (v1.getSup() / m) * m;
        if (v1.getSup() % m >= v0.getSup()) {
            return v0.getSup() + n;
        } else if (v1.getSup() % m < v0.getInf()) {
            return v0.getSup() + n - m;
        } else if (v1.getSup() % m == v0.getInf()) {
            return v1.getSup();
        } else {
            IntDomain dom = v0.getDomain();
            int sup = dom.getPrevValue((v1.getSup() % m)+1);
            return (sup + n);
        }
    }

    // *****************************************************************************************
    // Inst. d'une variable       v0 = v1 mod C
    // *****************************************************************************************
    public void awakeOnInst(int idx) throws ContradictionException {
        if (idx == 0) {
            reviseV1OnInst(v0.getVal());
        } else if (idx == 1) {
            reviseV0OnInst(v1.getVal());
        }
    }

    public void reviseV0OnInst(int inst) throws ContradictionException {
        v0.instantiate(inst % m, this, false);
    }

    public void reviseV1OnInst(int inst) throws ContradictionException {
        if (v1.hasEnumeratedDomain()) {
            IntDomain dom = v1.getDomain();
            for (int val = dom.getInf(); val <= dom.getSup(); val = dom.getNextValue(val)) {
                if (val % m != inst)
                    v1.removeVal(val, this, false);
            }
        } else {

            v1.setInf(searchInfV1());
            v1.setSup(searchSupV1());
        }
    }

    // *****************************************************************************************
    // Retrait d'une valeur      v0 = v1 mod C
    // *****************************************************************************************
    public void awakeOnRem(int idx, int valeur) throws ContradictionException {
        if (idx == 0) {
            reviseV1OnRem(valeur);
        } else if (idx == 1) {
            reviseV0OnRem(searchSupports(), valeur);
        }
    }

    public void reviseV0OnRem(BitSet supports, int valeur) throws ContradictionException {
        if (v0.hasEnumeratedDomain()) {
            if (supports.get(valeur % m))
                v0.removeVal(valeur % m, this, false);
        } else {
            int min = supports.nextClearBit(0);
            int max = min;
            for (int j = min; j >= 0 && j < m; j = supports.nextClearBit(j + 1)) {
                max = j;
            }

            v0.updateInf(min, this, false);
            v0.updateSup(max, this, false);
        }
    }


    public void reviseV1OnRem(int valeur) throws ContradictionException {
        if (v1.hasEnumeratedDomain()) {
            IntDomain dom = v1.getDomain();
            for (int q = dom.getInf() / m; q <= dom.getSup() / m; q++) {
                v1.removeVal(valeur + q * m, this, false);
            }
        } else {
            v1.setInf(searchInfV1());
            v1.setSup(searchSupV1());
        }
    }
/**
 * Checks if the listeners must be checked or must fail.
 */
/*
public Boolean isEntailed() {
}   */

    /**
     * Checks if the constraint is satisfied when the variables are instantiated.
     */

    public boolean isSatisfied() {
        return (v0.getVal() == (v1.getVal() % m));
    }


      @Override
    public boolean isSatisfied(int[] tuple) {
        return (tuple[0] == tuple[1] % m);
    }
    /**
     * tests if the constraint is consistent with respect to the current state of domains
     *
     * @return true iff the constraint is bound consistent (weaker than arc consistent)
     */
/*  public boolean isConsistent() {
    return ((v0.getInf() == v1.getInf() + cste) && (v0.getSup() == v1.getSup() + cste));
}*/
    public String pretty() {
        StringBuffer sb = new StringBuffer();
        sb.append(v0.toString());
        sb.append(" = ");
        sb.append(v1.toString());
        sb.append(" % ");
        // sb.append(Arithm.pretty(this.m));
        return sb.toString();
    }


}
