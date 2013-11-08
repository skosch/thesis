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
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.integer.extension.LargeRelation;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;


/**
 * GAC 2001 in valid tuples (do not support bound variables)
 */

public final class GAC2001LargeSConstraint extends CspLargeSConstraint {

    // Last valid supports Last(x_i, val) = supports( (blocks(i) + val) * size )

    protected IStateInt[] supports;

    protected int[] blocks;

    // Cardinality
    protected int size;

    // offsets(i) = Min(x_i)
    protected int[] offsets;

    // check if none of the tuple is trivially outside
    //the domains and if yes use a fast valid check
    //by avoiding checking the bounds
    protected ValidityChecker valcheck;

    public GAC2001LargeSConstraint(IntDomainVar[] vs, LargeRelation relation, IEnvironment environment) {
        super(vs, relation);
        this.size = vs.length;
        this.blocks = new int[size];
        this.offsets = new int[size];

        int nbElt = 0;
        boolean allboolean = true;
        for (int i = 0; i < size; i++) {
            offsets[i] = vs[i].getInf();
            blocks[i] = nbElt;
            if (!vars[i].hasBooleanDomain()) allboolean = false;
            if (!vars[i].hasEnumeratedDomain()) {
                throw new SolverException("GAC2001 can not be used with bound variables");
            } else nbElt += vars[i].getSup() - vars[i].getInf() + 1;
        }
        this.supports = new IStateInt[nbElt * size];

        for (int i = 0; i < supports.length; i++) {
            supports[i] = environment.makeInt(Integer.MIN_VALUE);
        }
        if (allboolean)
            valcheck = new FastBooleanValidityChecker(size, vars);
        else
            valcheck = new FastValidityChecker(size, vars);
    }

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
    }


    // updates the support for all values in the domain of variable
    // and remove unsupported values for variable
    public void reviseVar(int indexVar, boolean fromScratch) throws ContradictionException {
        int[] currentSupport;
        IntDomain dom = vars[indexVar].getDomain();
        int left = Integer.MIN_VALUE;
        int right = left;
        int val;
        for (val = vars[indexVar].getInf(); val <= vars[indexVar].getSup(); val = dom.getNextValue(val)) {
            currentSupport = seekNextSupport(indexVar, val, fromScratch);
            if (currentSupport != null) {
                setSupport(indexVar, val, currentSupport);
            } else {
                if (val == right + 1) {
                    right = val;
                } else {
                    vars[indexVar].removeInterval(left, right, this, false);
                    left = right = val;
                }
//                vars[indexVar].removeVal(val, this, false);
            }
        }
        vars[indexVar].removeInterval(left, right, this, false);
    }

    // Store Last(x_i, val) = support
    public void setSupport(int indexVar, int value, int[] support) {
        for (int i = 0; i < vars.length; i++) {
            supports[(blocks[indexVar] + value - offsets[indexVar]) * size + i].set(support[i]);
        }
    }


    // Get Last(x_i, val)
    public int[] getSupport(int indexVar, int value) {
        int[] resultat = new int[size];
        for (int i = 0; i < size; i++) {
            resultat[i] = supports[(blocks[indexVar] + value - offsets[indexVar]) * size + i].get();
        }
        return resultat;
    }


    // Get Last(x_i, val)
    public int[] lastSupport(int indexVar, int value) {
        return getSupport(indexVar, value);
    }

    /**
     * seek a new support for (variable, value), the smallest tuple greater than currentSupport
     * the search is made through valid tuples until and allowed one is found.
     */
    public int[] seekNextSupport(int indexVar, int val, boolean fromscratch) {
        int[] currentSupport = new int[size];
        int k = 0;
        if (fromscratch) {
            for (int i = 0; i < size; i++) {
                if (i != indexVar)
                    currentSupport[i] = vars[i].getInf();
                else currentSupport[i] = val;
            }
            if (relation.isConsistent(currentSupport)) {
                return currentSupport;
            }
        } else {
            currentSupport = getSupport(indexVar, val);
            if (valcheck.isValid(currentSupport)) {
                return currentSupport;
            } else {
                currentSupport = getFirstValidTupleFrom(currentSupport, indexVar);
                if (currentSupport == null) return null;
                if (relation.isConsistent(currentSupport))
                    return currentSupport;
            }
        }

        while (k < vars.length) {
            if (k == indexVar) k++;
            if (k < vars.length) {
                if (!vars[k].getDomain().hasNextValue(currentSupport[k])) {
                    currentSupport[k] = vars[k].getInf();
                    k++;
                } else {
                    currentSupport[k] = vars[k].getDomain().getNextValue(currentSupport[k]);
                    if ((relation.isConsistent(currentSupport))) {
                        return currentSupport;
                    }
                    k = 0;
                }
            }
        }

        return null;
    }

    /**
     * t is a consistent tuple not valid anymore, we need to go to the first valid tuple
     * greater than t before searching among the valid tuples
     *
     * @param t
     * @param indexVar
     * @return
     */
    public int[] getFirstValidTupleFrom(int[] t, int indexVar) {
        int k = 0;
        while (k < vars.length) {
            if (k == indexVar) k++;
            if (k < vars.length) {
                if (!vars[k].getDomain().hasNextValue(t[k])) {
                    t[k] = vars[k].getInf();
                    k++;
                } else {
                    t[k] = vars[k].getDomain().getNextValue(t[k]);
                    if (valcheck.isValid(t)) {
                        return t;
                    }
                    k = 0;
                }
            }
        }
        return null;
    }

    public void awake() throws ContradictionException {
        for (int i = 0; i < vars.length; i++) {
            reviseVar(i, true);
        }
        propagate();
    }


    public void propagate() throws ContradictionException {
        for (int i = 0; i < size; i++)
            reviseVar(i, false);
    }


    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
        filter(idx);
    }

    public void awakeOnInf(int idx) throws ContradictionException {
        filter(idx);
    }

    public void awakeOnSup(int idx) throws ContradictionException {
        filter(idx);
    }

    public void awakeOnRem(int idx, int x) throws ContradictionException {
        filter(idx);
    }

    public void awakeOnBounds(int varIndex) throws ContradictionException {
        filter(varIndex);
    }

    public void awakeOnInst(int idx) throws ContradictionException {
        filter(idx);
    }


    public void filter(int idx) throws ContradictionException {
        //sort variables regarding domain sizes to speedup the check !
        valcheck.sortvars();
        if (vars[idx].hasEnumeratedDomain()) {
            for (int i = 0; i < size; i++)
                if (idx != valcheck.position[i])
                    reviseVar(valcheck.position[i], false);
        } else {
            for (int i = 0; i < size; i++)
                reviseVar(valcheck.position[i], false);
        }
    }


    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("GAC2001ValidLarge({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) sb.append(", ");
            IntDomainVar var = vars[i];
            sb.append(var.pretty());
        }
        sb.append("})");
        return sb.toString();
    }
}

