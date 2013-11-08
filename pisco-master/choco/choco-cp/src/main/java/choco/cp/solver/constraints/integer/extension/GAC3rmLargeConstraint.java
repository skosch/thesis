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
import choco.kernel.solver.constraints.integer.extension.ConsistencyRelation;
import choco.kernel.solver.constraints.integer.extension.LargeRelation;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Arrays;

/*
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 14 avr. 2008
 * Since : Choco 2.0.0
 *
 */
public final class GAC3rmLargeConstraint extends CspLargeSConstraint {

    // Last valid supports Last(x_i, val) = supports( (blocks(i) + val) * size )

    protected int[] supports;

    protected int[] blocks;

    // Cardinality
    protected int size;

    // offsets(i) = Min(x_i)
    protected int[] offsets;


    public GAC3rmLargeConstraint(IntDomainVar[] vs, LargeRelation relation) {
        super(vs, relation);

        this.size = vs.length;
        this.blocks = new int[size];
        this.offsets = new int[size];

        int nbElt = 0;

        for (int i = 0; i < size; i++) {
            offsets[i] = vs[i].getInf();
            blocks[i] = nbElt;
            if (!vars[i].hasEnumeratedDomain()) {
                nbElt += 2;
            } else nbElt += vars[i].getSup() - vars[i].getInf() + 1;
        }

        this.supports = new int[nbElt * size];
        this.seekIter = new DisposableIntIterator[size];
        for (int i = 0; i < size; i++) {
            seekIter[i] = vars[i].getDomain().getIterator();
        }
        Arrays.fill(supports, Integer.MIN_VALUE);

    }

    @Override
    public AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
        LargeRelation rela2 = (LargeRelation) ((ConsistencyRelation) relation).getOpposite();
        AbstractSConstraint ct = new GAC3rmLargeConstraint(vars, rela2);
        return ct;
    }

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
    }

    /**
     * initialize the supports of each value of indexVar
     *
     * @param indexVar
     * @throws ContradictionException
     */
    public void initializeSupports(int indexVar) throws ContradictionException {
        int[] currentSupport;
        IntDomain dom = vars[indexVar].getDomain();
        int val;
        if (vars[indexVar].hasEnumeratedDomain()) {
            DisposableIntIterator it = dom.getIterator();
            int left = Integer.MIN_VALUE;
            int right = left;
            try {
                while (it.hasNext()) {
                    val = it.next();
                    if (lastSupport(indexVar, val)[0] == Integer.MIN_VALUE) { // no supports initialized yet for this value
                        currentSupport = seekNextSupport(indexVar, val);
                        if (currentSupport != null) {
                            setSupport(currentSupport);
                        } else {
                            if (val == right + 1) {
                                right = val;
                            } else {
                                vars[indexVar].removeInterval(left, right, this, false);
                                left = right = val;
                            }
//                        vars[indexVar].removeVal(val, this, false);
                        }
                    }
                }
                vars[indexVar].removeInterval(left, right, this, false);
            } finally {
                it.dispose();
            }
        } else {
            for (val = vars[indexVar].getInf(); val <= vars[indexVar].getSup(); val++) {
                currentSupport = seekNextSupport(indexVar, val);
                if (currentSupport != null) {
                    setBoundSupport(indexVar, 0, currentSupport);
                    break; //stop at the first consistent lower bound !
                }
            }
            vars[indexVar].updateInf(val, this, false);
            for (val = vars[indexVar].getSup(); val >= vars[indexVar].getInf(); val--) {
                currentSupport = seekNextSupport(indexVar, val);
                if (currentSupport != null) {
                    setBoundSupport(indexVar, 1, currentSupport);
                    break; //stop at the first consistent upper bound !
                }
            }
            vars[indexVar].updateSup(val, this, false);
        }
    }


    // updates the support for all values in the domain of variable
    // and remove unsupported values for variable
    public void reviseVar(int indexVar) throws ContradictionException {
        int[] currentSupport;
        IntDomain dom = vars[indexVar].getDomain();
        int val;
        if (vars[indexVar].hasEnumeratedDomain()) {
            DisposableIntIterator it = dom.getIterator();
            int left = Integer.MIN_VALUE;
            int right = left;
            try {
                while (it.hasNext()) {
                    val = it.next();
                    if (!isValid(lastSupport(indexVar, val))) {
                        currentSupport = seekNextSupport(indexVar, val);
                        if (currentSupport != null) {
                            setSupport(currentSupport);
                        } else {
                            if (val == right + 1) {
                                right = val;
                            } else {
                                vars[indexVar].removeInterval(left, right, this, false);
                                left = right = val;
                            }
//                            vars[indexVar].removeVal(val, this, false);
                        }
                    }
                }
                vars[indexVar].removeInterval(left, right, this, false);
            } finally {
                it.dispose();
            }
        } else {
            int[] inf_supports = lastBoundSupport(indexVar, 0);
            if (vars[indexVar].getInf() != inf_supports[indexVar] || !isValid(inf_supports)) {
                for (val = vars[indexVar].getInf(); val <= vars[indexVar].getSup(); val++) {
                    currentSupport = seekNextSupport(indexVar, val);
                    if (currentSupport != null) {
                        setBoundSupport(indexVar, 0, currentSupport);
                        break; //stop at the first consistent lower bound !
                    }
                }
                vars[indexVar].updateInf(val, this, false);
            }
            int[] sup_supports = lastBoundSupport(indexVar, 1);
            if (vars[indexVar].getSup() != sup_supports[indexVar] || !isValid(sup_supports)) {
                for (val = vars[indexVar].getSup(); val >= vars[indexVar].getInf(); val--) {
                    currentSupport = seekNextSupport(indexVar, val);
                    if (currentSupport != null) {
                        setBoundSupport(indexVar, 1, currentSupport);
                        break; //stop at the first consistent upper bound !
                    }
                }
                vars[indexVar].updateSup(val, this, false);
            }
        }
    }

    // Store Last(x_i, val) = support
    public void setSupport(int[] support) {
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].hasEnumeratedDomain())
                setOneSupport(i, support[i], support);
        }
    }

    public void setOneSupport(int indexVar, int value, int[] support) {
        for (int i = 0; i < vars.length; i++) {
            supports[(blocks[indexVar] + value - offsets[indexVar]) * size + i] = support[i];
        }
    }

    // Store Last(x_i, val) = support
    public void setBoundSupport(int indexVar, int idxBound, int[] support) {
        for (int i = 0; i < vars.length; i++) {
            supports[(blocks[indexVar] + idxBound) * size + i] = support[i];
        }
    }


    // Get Last(x_i, val)
    public int[] getSupport(int indexVar, int value) {
        int[] resultat = new int[size];
        for (int i = 0; i < size; i++) {
            resultat[i] = supports[(blocks[indexVar] + value - offsets[indexVar]) * size + i];
        }
        return resultat;
    }


    // return the support standing for the lower bound
    // of indexVar if idxBound = 0 or upperbound if idxBound = 1
    public int[] getBoundSupport(int indexVar, int idxBound) {
        int[] resultat = new int[size];
        for (int i = 0; i < size; i++) {
            resultat[i] = supports[(blocks[indexVar] + idxBound) * size + i];
        }
        return resultat;
    }


    // Get Last(x_i, val)
    public int[] lastSupport(int indexVar, int value) {
        return getSupport(indexVar, value);
    }

    // return the support standing for the lower bound
    // of indexVar if idxBound = 0 or upperbound if idxBound = 1
    public int[] lastBoundSupport(int indexVar, int idxBound) {
        return getBoundSupport(indexVar, idxBound);
    }

    // Is tuple valide ?
    public boolean isValid(int[] tuple) {
        for (int i = 0; i < size; i++)
            if (!vars[i].canBeInstantiatedTo(tuple[i])) return false;
        return true;
    }

    protected DisposableIntIterator[] seekIter;

    // seek a new support for (variable, value), the smallest tuple greater than currentSupport
    public int[] seekNextSupport(int indexVar, int val) {
        int[] currentSupport = new int[size];
        int k = 0;
        for (int i = 0; i < size; i++) {
            seekIter[i].dispose();
            seekIter[i] = vars[i].getDomain().getIterator();
            if (i != indexVar)
                currentSupport[i] = seekIter[i].next();
            else currentSupport[i] = val;
        }
        if (relation.isConsistent(currentSupport)) {
            return currentSupport;
        }

        while (k < vars.length) {
            if (k == indexVar) k++;
            if (k < vars.length) {
                if (!seekIter[k].hasNext()) {
                    seekIter[k].dispose();
                    seekIter[k] = vars[k].getDomain().getIterator();
                    currentSupport[k] = seekIter[k].next();
                    k++;
                } else {
                    currentSupport[k] = seekIter[k].next();
                    if ((relation.isConsistent(currentSupport))) {
                        return currentSupport;
                    }
                    k = 0;
                }
            }
        }

        return null;
    }


    public void awake() throws ContradictionException {
        for (int i = 0; i < vars.length; i++) {
            initializeSupports(i);
        }
        propagate();
    }


    public void propagate() throws ContradictionException {
        for (int i = 0; i < size; i++)
            reviseVar(i);
    }


    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
        for (int i = 0; i < size; i++)
            if (idx != i) reviseVar(i);
        if (!vars[idx].hasEnumeratedDomain()) {
            reviseVar(idx);
        }

    }

    public void awakeOnInf(int idx) throws ContradictionException {
        for (int i = 0; i < size; i++)
            if (idx != i) reviseVar(i);
        if (!vars[idx].hasEnumeratedDomain()) {
            reviseVar(idx);
        }
    }

    public void awakeOnSup(int idx) throws ContradictionException {
        for (int i = 0; i < size; i++)
            if (idx != i) reviseVar(i);
        if (!vars[idx].hasEnumeratedDomain()) {
            reviseVar(idx);
        }
    }

    public void awakeOnRem(int idx, int x) throws ContradictionException {
        for (int i = 0; i < size; i++)
            if (idx != i) reviseVar(i);
        if (!vars[idx].hasEnumeratedDomain()) {
            reviseVar(idx);
        }
    }

    public void awakeOnBounds(int varIndex) throws ContradictionException {
        for (int i = 0; i < size; i++)
            if (varIndex != i) reviseVar(i);
        if (!vars[varIndex].hasEnumeratedDomain()) {
            reviseVar(varIndex);
        }
    }

    public void awakeOnInst(int idx) throws ContradictionException {
        for (int i = 0; i < size; i++)
            if (idx != i) reviseVar(i);
        if (!vars[idx].hasEnumeratedDomain()) {
            reviseVar(idx);
        }
    }

    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("GAC3rmValidLarge({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) sb.append(", ");
            IntDomainVar var = vars[i];
            sb.append(var.pretty());
        }
        sb.append("}) relation: ");
        return sb.toString();
    }


}
