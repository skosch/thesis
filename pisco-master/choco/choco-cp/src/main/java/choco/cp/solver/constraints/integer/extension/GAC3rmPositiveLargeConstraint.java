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
import choco.kernel.solver.constraints.integer.extension.IterTuplesTable;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * GAC3rm (GAC3 with residual supports)
 */
public final class GAC3rmPositiveLargeConstraint extends CspLargeSConstraint {

    /**
     * supports[i][j stores the index of the tuple that currently support
     * the variable-value pair (i,j)
     */
    protected int[][] supports;

    /**
     * size of the scope of the constraint
     */
    protected int arity;

    /**
     * original lower bounds
     */
    protected int[] offsets;

    /**
     * original domain sizes
     */
//    protected int[] odSize;

    /**
     * minimum number of support among all values of each variable;
     */
//    protected int[] minSupports;


    protected static final int NO_SUPPORT = -2;

    protected IterTuplesTable relation;

    //a reference on the lists of supports per variable value pair
    protected int[][][] tab;


    // check if none of the tuple is trivially outside
    //the domains and if yes use a fast valid check
    //by avoiding checking the bounds
    protected ValidityChecker valcheck;

    public GAC3rmPositiveLargeConstraint(IntDomainVar[] vs, IterTuplesTable relation) {
        super(vs, null);
        this.relation = relation;
        this.arity = vs.length;
        this.offsets = new int[arity];
        this.tab = relation.getTableLists();
//        this.odSize = new int[arity];
//        this.minSupports = new int[arity];
        this.supports = new int[arity][];
        for (int i = 0; i < arity; i++) {
            this.offsets[i] = vs[i].getInf();
            this.supports[i] = new int[vs[i].getSup() - vs[i].getInf() + 1];
        }
        int[][] tt = relation.getTupleTable();
        boolean fastValidCheckAllowed = true;
        boolean fastBooleanValidCheckAllowed = true;
        // check if all tuples are within the range
        // of the domain and if so set up a faster validity checker
        // that avoids checking original bounds first
        for (int i = 0; i < tt.length; i++) {
            for (int j = 0; j < tt[i].length; j++) {
                int lb = vs[j].getInf();
                int ub = vs[j].getSup();
                if (lb > tt[i][j] ||
                        ub < tt[i][j]) {
                    fastValidCheckAllowed = false;
                }
                if (lb < 0 || ub > 1) {
                    fastBooleanValidCheckAllowed = false;
                }
            }
            if (!fastBooleanValidCheckAllowed &&
                    !fastValidCheckAllowed) break;
        }
        if (fastBooleanValidCheckAllowed) {
            valcheck = new FastBooleanValidityChecker(arity, vars);
        } else if (fastValidCheckAllowed) {
            valcheck = new FastValidityChecker(arity, vars);
        } else valcheck = new ValidityChecker(arity, vars);
    }

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
    }

    /**
     * updates the support for all values in the domain of variable
     * and remove unsupported values for variable
     *
     * @param indexVar
     * @throws ContradictionException
     */
    public void reviseVar(final int indexVar) throws ContradictionException {
        DisposableIntIterator itv = vars[indexVar].getDomain().getIterator();
        int left = Integer.MIN_VALUE;
        int right = left;
        try {
            while (itv.hasNext()) {
                int val = itv.next();
                int nva = val - relation.getRelationOffset(indexVar);
                int currentIdxSupport = getSupport(indexVar, val);
                //check the residual support !
                if (!valcheck.isValid(relation.getTuple(currentIdxSupport))) {
                    //the residual support is not valid anymore, seek a new one
                    currentIdxSupport = seekNextSupport(indexVar, nva);
                    if (currentIdxSupport == NO_SUPPORT) {
                        if (val == right + 1) {
                            right = val;
                        } else {
                            vars[indexVar].removeInterval(left, right, this, false);
                            left = right = val;
                        }
//                        vars[indexVar].removeVal(val, this, false);
                    } else {
                        setSupport(currentIdxSupport);
                    }
                }
            }
            vars[indexVar].removeInterval(left, right, this, false);
        } finally {
            itv.dispose();
        }
    }


    /**
     * seek a new support for the pair variable-value : (indexVar, nva)
     * start the iteration from scratch in the list
     */
    public int seekNextSupport(final int indexVar, final int nva) {
        int currentIdxSupport;
        int[] currentSupport = null;
        for (int i = 0; i < tab[indexVar][nva].length; i++) {
            currentIdxSupport = tab[indexVar][nva][i];
            currentSupport = relation.getTuple(currentIdxSupport);
            if (valcheck.isValid(currentSupport)) return currentIdxSupport;
        }
        return NO_SUPPORT;
    }

    /**
     * set the support using multidirectionality
     *
     * @param idxSupport
     * @return the residual support
     */
    public void setSupport(final int idxSupport) {
        int[] tuple = relation.getTuple(idxSupport);
        for (int i = 0; i < tuple.length; i++) {
            supports[i][tuple[i] - offsets[i]] = idxSupport;
        }
    }

    /**
     * @param indexVar
     * @param value    with offset removed
     * @return the residual support
     */
    public int getSupport(final int indexVar, final int value) {
        return supports[indexVar][value - offsets[indexVar]];
    }

    /**
     * initialize the residual supports of each pair to their
     * first allowed tuple
     */
    public void initSupports() throws ContradictionException {
        for (int i = 0; i < vars.length; i++) {
            DisposableIntIterator itv = vars[i].getDomain().getIterator();
            int left = Integer.MIN_VALUE;
            int right = left;
            try {
                while (itv.hasNext()) {
                    int val = itv.next();
                    int nva = val - relation.getRelationOffset(i);
                    if (tab[i][nva].length == 0) {
                        if (val == right + 1) {
                            right = val;
                        } else {
                            vars[i].removeInterval(left, right, this, false);
                            left = right = val;
                        }
//                        vars[i].removeVal(val, this, false);
                    } else {
                        setSupport(tab[i][nva][0]);
                    }
                }
                vars[i].removeInterval(left, right, this, false);
            } finally {
                itv.dispose();
            }
        }
    }


    /**
     * @return an upper bound on the number of supports lost for variable
     *         idx due to domain reduction of the other variables
     */
//    public int getUBNbLostSupports(int idx) {
//        int ub = 1;
//        for (int i = 0; i < arity; i++) {
//            if (idx != i)
//                ub *= Math.max(odSize[i] - vars[i].getDomainSize(), 1);
//        }
//        return ub;
//    }
    // REACTION TO EVENTS
    public void propagate() throws ContradictionException {
        for (int indexVar = 0; indexVar < arity; indexVar++)
            reviseVar(indexVar);
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

    public void awake() throws ContradictionException {
        initSupports();
        propagate();
    }

    public void filter(int idx) throws ContradictionException {
        //sort variables regarding domain sizes to speedup the check !
        valcheck.sortvars();
        for (int i = 0; i < arity; i++)
            if (idx != valcheck.position[i]) reviseVar(valcheck.position[i]);
    }


    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("GAC3rmAllowedLarge({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) sb.append(", ");
            IntDomainVar var = vars[i];
            sb.append(var.pretty());
        }
        sb.append("})");
        return sb.toString();
    }

    //<hca> implementation not efficient at all because
    //this constraint never "check" tuples but iterate over them and check the domains.
    //this should only be called in the restore solution
    @Override
    public boolean isSatisfied(int[] tuple) {
        int minListIdx = -1;
        int minSize = Integer.MAX_VALUE;
        for (int i = 0; i < tuple.length; i++) {
            if (tab[i][tuple[i] - offsets[i]].length < minSize) {
                minSize = tab[i][tuple[i] - offsets[i]].length;
                minListIdx = i;
            }
        }
        int currentIdxSupport;
        int[] currentSupport;
        int nva = tuple[minListIdx] - relation.getRelationOffset(minListIdx);
        for (int i = 0; i < tab[minListIdx][nva].length; i++) {
            currentIdxSupport = tab[minListIdx][nva][i];
            currentSupport = relation.getTuple(currentIdxSupport);
            boolean isValid = true;
            for (int j = 0; isValid && j < tuple.length; j++) {
                if (tuple[j] != currentSupport[j]) {
                    isValid = false;
                }
            }
            if (isValid) return true;
        }
        return false;
    }
}
