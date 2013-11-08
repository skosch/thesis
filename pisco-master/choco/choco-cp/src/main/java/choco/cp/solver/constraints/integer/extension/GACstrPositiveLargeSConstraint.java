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
import choco.kernel.memory.IStateIntVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.extension.LargeRelation;
import choco.kernel.solver.constraints.integer.extension.TuplesList;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * GAC maintained by STR
 */
public final class GACstrPositiveLargeSConstraint extends CspLargeSConstraint {

    protected TuplesList relation;

    // check if none of the tuple is trivially outside
    // the domains and if yes use a fast valid check
    // by avoiding checking the bounds
    protected ValidityChecker valcheck;

    /**
     * size of the scope
     */
    protected int arity;

    /**
     * original lower bounds
     */
    protected int[] offsets;

    /**
     * Variables that are not proved to be GAC yet
     */
    protected List<Integer> futureVars;

    /**
     * Values that have found a support for each variable
     */
    protected BitSet[] gacValues;

    protected int[] nbGacValues;

    /**
     * The backtrackable list of tuples representing the current
     * allowed tuples of the constraint
     */
    protected IStateIntVector ltuples;


    public GACstrPositiveLargeSConstraint(IntDomainVar[] vs, LargeRelation relation, IEnvironment environment) {
        super(vs, relation);
        this.arity = vs.length;
        this.relation = (TuplesList) relation;
        this.futureVars = new LinkedList<Integer>();
        this.gacValues = new BitSet[arity];
        this.nbGacValues = new int[arity];

        this.offsets = new int[arity];
        for (int i = 0; i < arity; i++) {
            this.offsets[i] = vs[i].getInf();
            this.gacValues[i] = new BitSet(vs[i].getSup() - vs[i].getInf() + 1);
        }

        int[] listuples = new int[this.relation.getTupleTable().length];
        for (int i = 0; i < listuples.length; i++) {
            listuples[i] = i;
        }
        ltuples = environment.makeBipartiteIntList(listuples);

        int[][] tt = this.relation.getTupleTable();
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

    public void initializeData() {
        //INITIALIZATION
        futureVars.clear();
        for (int i = 0; i < arity; i++) {
            gacValues[i].clear();
            nbGacValues[i] = 0;
            futureVars.add(i);
        }
    }

    public void pruningPhase() throws ContradictionException {
        for (Iterator<Integer> itf = futureVars.iterator(); itf.hasNext();) {
            int vIdx = itf.next();
            IntDomainVar v = vars[vIdx];
            DisposableIntIterator it3 = v.getDomain().getIterator();
            int left = Integer.MIN_VALUE;
            int right = left;
            try {
                while (it3.hasNext()) {
                    int val = it3.next();
                    if (!gacValues[vIdx].get(val - offsets[vIdx])) {
                        if (val == right + 1) {
                            right = val;
                        } else {
                            v.removeInterval(left, right, this, false);
                            left = right = val;
                        }
//                        v.removeVal(val, this, false);
                    }
                }
                v.removeInterval(left, right, this, false);
            } finally {
                it3.dispose();
            }
        }
    }

    /**
     * maintain the list by checking only the variable that has changed when
     * checking if a tuple is valid.
     *
     * @param idx : the variable changed
     */
    public void maintainList(int idx) {
        DisposableIntIterator it = ltuples.getIterator();
        while (it.hasNext()) {
            int idxt = it.next();
            int[] tuple = relation.getTuple(idxt);
            try {
                if (valcheck.isValid(tuple, idx)) {
                    //extract the supports
                    for (Iterator<Integer> itf = futureVars.iterator(); itf.hasNext();) {
                        int vIdx = itf.next();
                        if (!gacValues[vIdx].get(tuple[vIdx] - offsets[vIdx])) {
                            gacValues[vIdx].set(tuple[vIdx] - offsets[vIdx]);
                            nbGacValues[vIdx]++;
                            if (nbGacValues[vIdx] == vars[vIdx].getDomainSize()) {
                                itf.remove();
                            }
                        }
                    }
                } else {
                    //remove the tuple from the current list
                    it.remove();
                }
            } finally {
                it.dispose();
            }
        }
    }

    /**
     * maintain the list by checking all variable within isValid
     */
    public void maintainList() {
        DisposableIntIterator it = ltuples.getIterator();
        while (it.hasNext()) {
            int idxt = it.next();
            int[] tuple = relation.getTuple(idxt);

            if (valcheck.isValid(tuple)) {
                //extract the supports
                for (Iterator<Integer> itf = futureVars.iterator(); itf.hasNext();) {
                    int vIdx = itf.next();
                    if (!gacValues[vIdx].get(tuple[vIdx] - offsets[vIdx])) {
                        gacValues[vIdx].set(tuple[vIdx] - offsets[vIdx]);
                        nbGacValues[vIdx]++;
                        if (nbGacValues[vIdx] == vars[vIdx].getDomainSize()) {
                            itf.remove();
                        }
                    }
                }
            } else {
                //remove the tuple from the current list
                it.remove();
            }
        }
        it.dispose();
    }


    /**
     * Main Incremental propagation loop. It maintains the list of valid tuples
     * through the search
     *
     * @throws ContradictionException
     */
    public void gacstr(int idx) throws ContradictionException {
        initializeData();
        maintainList(idx);
        pruningPhase();
    }

    /**
     * Main propagation loop. It maintains the list of valid tuples
     * through the search
     *
     * @throws ContradictionException
     */
    public void gacstr() throws ContradictionException {
        initializeData();
        maintainList();
        pruningPhase();
        if (getCartesianProduct() <= ltuples.size()) {
            setEntailed();
        }
    }

    public double getCartesianProduct() {
        double cp = 1d;
        for (int i = 0; i < arity; i++) {
            cp *= vars[i].getDomainSize();
        }
        return cp;
    }

    public void propagate() throws ContradictionException {
        valcheck.sortvars();
        gacstr();
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
        propagate();
    }

    public void filter(int idx) throws ContradictionException {
        //sort variables regarding domain sizes to speedup the check !
        valcheck.sortvars();
        gacstr();
        //constAwake(false);
    }


    //<hca> implementation not efficient at all because
    //this constraint never "check" tuples but iterate over them and check the domains.
    //this should only be called in the restore solution
    @Override
    public boolean isSatisfied(int[] tuple) {
        int[][] tupleList = relation.getTupleTable();
        for (int i = 0; i < tupleList.length; i++) {
            boolean isValid = true;
            for (int j = 0; isValid && j < tuple.length; j++) {
                if (tuple[j] != tupleList[i][j]) {
                    isValid = false;
                }
            }
            if (isValid) return true;
        }
        return false;
    }

    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("GACStrAllowedLarge({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) sb.append(", ");
            IntDomainVar var = vars[i];
            sb.append(var.pretty());
        }
        sb.append("})");
        return sb.toString();
    }

}
