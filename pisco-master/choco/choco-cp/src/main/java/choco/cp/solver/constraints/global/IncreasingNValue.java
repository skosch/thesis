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

package choco.cp.solver.constraints.global;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntHashSet;

/**
 * User : xlorca
 * Mail : xlorca(a)emn.fr
 * Date : 29 janv. 2010
 * Since : Choco 2.1.1
 */
public final class IncreasingNValue extends AbstractLargeIntSConstraint {

    public enum Mode {
        ATMOST, ATLEAST, BOTH
    }

    /*
     * well-ordered variables : x[i] <= x[j], i < j
     */
    IntDomainVar[] x;
    int n;

    /*
     * occurence variable on the number of stretches
     */
    IntDomainVar occ;

    OrderedSparseArray infSuffix;
    OrderedSparseArray supSuffix;
    OrderedSparseArray minS;
    OrderedSparseArray maxS;

    OrderedSparseArray infPrefix;
    OrderedSparseArray supPrefix;
    OrderedSparseArray minP;
    OrderedSparseArray maxP;

    Mode m;

    public static IntDomainVar[] concat(final IntDomainVar occ, final IntDomainVar[] vars) {
        final IntDomainVar[] mesVars = new IntDomainVar[vars.length + 1];
        System.arraycopy(vars, 0, mesVars, 0, vars.length);
        mesVars[vars.length] = occ;
        return mesVars;
    }

    public IncreasingNValue(final IntDomainVar occ, final IntDomainVar[] x, final Mode m) {
        super(ConstraintEvent.LINEAR, concat(occ, x));
        this.x = x;
        this.n = x.length;
        this.occ = occ;
        this.m = m;

        this.infSuffix = new OrderedSparseArray(n);
        this.supSuffix = new OrderedSparseArray(n);
        this.minS = new OrderedSparseArray(n, true);
        this.maxS = new OrderedSparseArray(n, true);
        this.infPrefix = new OrderedSparseArray(n);
        this.supPrefix = new OrderedSparseArray(n);
        this.minP = new OrderedSparseArray(n, true);
        this.maxP = new OrderedSparseArray(n, true);
    }

    public static int getPrev(final IntDomainVar v, final int val) {
        if (val > v.getInf()) {
            return v.getPrevDomainValue(val);
        } else {
            return val;
        }
    }

    public static int getNext(final IntDomainVar v, final int val) {
        if (val < v.getSup()) {
            return v.getNextDomainValue(val);
        } else {
            return val;
        }
    }

    // Algorithm 6
    public final void buildSuffix() {
        minS.allocate(x, n + 1);
        maxS.allocate(x, 0);
        infSuffix.scanInit(n - 1, false);
        supSuffix.scanInit(n - 1, false);
        int v = x[n - 1].getSup();
        int w;

        // initial values for the minimum and maximum number of stretches for suffixes
        do {
            infSuffix.set(n - 1, v, 1);
            supSuffix.set(n - 1, v, 1);
            w = v;
            v = getPrev(x[n - 1], v);
        } while (w != v);

        for (int i = n - 2; i >= 0; i--) {
            infSuffix.scanInit(i + 1, false);
            supSuffix.scanInit(i + 1, false);
            minS.scanInit(i + 1, false);
            maxS.scanInit(i + 1, false);
            v = x[i + 1].getSup();
            do {
                if (v < x[i + 1].getSup()) {
                    int t = Math.min(minS.get(i + 1, v + 1), infSuffix.get(i + 1, v));
                    minS.set(i + 1, v, t);
                    t = Math.max(maxS.get(i + 1, v + 1), supSuffix.get(i + 1, v));
                    maxS.set(i + 1, v, t);
                } else {
                    minS.set(i + 1, v, infSuffix.get(i + 1, v));
                    maxS.set(i + 1, v, supSuffix.get(i + 1, v));
                }
                w = v;
                v = getPrev(x[i + 1], v);
            } while (w != v);

            infSuffix.scanInit(i, false);
            supSuffix.scanInit(i, false);
            infSuffix.scanInit(i + 1, false);
            supSuffix.scanInit(i + 1, false);
            minS.scanInit(i + 1, false);
            maxS.scanInit(i + 1, false);
            v = x[i].getSup();
            do {
                if (v == x[i + 1].getSup()) {
                    infSuffix.set(i, v, infSuffix.get(i + 1, v));
                    supSuffix.set(i, v, supSuffix.get(i + 1, v));
                } else {
                    if (v >= x[i + 1].getInf()) {
                        infSuffix.set(i, v, Math.min(infSuffix.get(i + 1, v), minS.get(i + 1, v + 1) + 1));
                        supSuffix.set(i, v, Math.max(supSuffix.get(i + 1, v), maxS.get(i + 1, v + 1) + 1));
                    } else {
                        infSuffix.set(i, v, minS.get(i + 1, x[i + 1].getInf()) + 1);
                        supSuffix.set(i, v, maxS.get(i + 1, x[i + 1].getInf()) + 1);
                    }
                }
                w = v;
                v = getPrev(x[i], v);
            } while (w != v);
        }
    }

    // Algorithm 7
    public final void buildPrefix() {
        minP.allocate(x, n + 1);
        maxP.allocate(x, 0);
        infPrefix.scanInit(0, true);
        supPrefix.scanInit(0, true);
        int v = x[0].getInf();
        int w;
        // initial values for the minimum and maximum number of stretches for suffixes
        do {
            infPrefix.set(0, v, 1);
            supPrefix.set(0, v, 1);
            w = v;
            v = getNext(x[0], v);
        } while (w != v);
        for (int i = 1; i < n; i++) {
            infPrefix.scanInit(i - 1, true);
            supPrefix.scanInit(i - 1, true);
            minP.scanInit(i - 1, true);
            maxP.scanInit(i - 1, true);
            v = x[i - 1].getInf();
            do {
                if (v > x[i - 1].getInf()) {
                    int t = Math.min(minP.get(i - 1, v - 1), infPrefix.get(i - 1, v));
                    minP.set(i - 1, v, t);
                    t = Math.max(maxP.get(i - 1, v - 1), supPrefix.get(i - 1, v));
                    maxP.set(i - 1, v, t);
                } else {
                    minP.set(i - 1, v, infPrefix.get(i - 1, v));
                    maxP.set(i - 1, v, supPrefix.get(i - 1, v));
                }
                w = v;
                v = getNext(x[i - 1], v);
            } while (w != v);
            infPrefix.scanInit(i, true);
            supPrefix.scanInit(i, true);
            infPrefix.scanInit(i - 1, true);
            supPrefix.scanInit(i - 1, true);
            minP.scanInit(i - 1, true);
            maxP.scanInit(i - 1, true);
            v = x[i].getInf();
            do {
                if (v == x[i - 1].getInf()) {
                    infPrefix.set(i, v, infPrefix.get(i - 1, v));
                    supPrefix.set(i, v, supPrefix.get(i - 1, v));
                } else {
                    if (v <= x[i - 1].getSup()) {
                        infPrefix.set(i, v, Math.min(infPrefix.get(i - 1, v), minP.get(i - 1, v - 1) + 1));
                        supPrefix.set(i, v, Math.max(supPrefix.get(i - 1, v), maxP.get(i - 1, v - 1) + 1));
                    } else {
                        infPrefix.set(i, v, minP.get(i - 1, x[i - 1].getSup()) + 1);
                        supPrefix.set(i, v, maxP.get(i - 1, x[i - 1].getSup()) + 1);
                    }
                }
                w = v;
                v = getNext(x[i], v);
            } while (w != v);
        }
    }

    /*
     * It adjusts the minimum of domain of variable idx to value val.
     * It returns true if the domain of idx is empty after the operation, false otherwise.
     */
    public final void adjustMin(final int idx, final int val) throws ContradictionException {
        vars[idx].updateInf(val, this, false);
    }

    /*
     * It adjusts the maximum of domain of variable idx to value val. I
     * It returns true if the domain of idx is empty after the operation, false otherwise.
     */
    public final void adjustMax(final int idx, final int val) throws ContradictionException {
        vars[idx].updateSup(val, this, false);
    }

    /*
     * It returns the minimum value of infSuffix[k][].
     */
    public final int minInfSuffix(final int k) {
        int min = Integer.MAX_VALUE;
        final DisposableIntIterator it = x[k].getDomain().getIterator();
        while (it.hasNext()) {
            final int v = it.next();
            final int w = infSuffix.get(k, v);
            if (min > w) {
                min = w;
            }
        }
        it.dispose();
        return min;
    }

    /*
     * It returns the maximum value of supSuffix[k][].
     */
    public final int maxSupSuffix(final int k) {
        int max = Integer.MIN_VALUE;
        final DisposableIntIterator it = x[k].getDomain().getIterator();
        while (it.hasNext()) {
            final int v = it.next();
            final int w = supSuffix.get(k, v);
            if (max < w) {
                max = w;
            }
        }
        it.dispose();
        return max;
    }

    // Algorithm 8
    public void propagate() throws ContradictionException {
        if (n == 1) {
            occ.instantiate(1, this, false);
        }
        for (int i = 1; i < n; i++) {
            adjustMin(i, x[i - 1].getInf());
        }
        for (int i = n - 2; i >= 0; i--) {
            adjustMax(i, x[i + 1].getSup());
        }
        //initialize structures
        infSuffix.allocate(x, n + 1);
        supSuffix.allocate(x, 0);
        infPrefix.allocate(x, n + 1);
        supPrefix.allocate(x, 0);
        buildSuffix();
        buildPrefix();

        /*System.out.println(occ.pretty()+ "\n");
        System.out.println(printer());
        System.out.println(infSuffix.printer("sInf"));
        System.out.println(supSuffix.printer("sSup"));
        System.out.println(infPrefix.printer("minS"));
        System.out.println(supPrefix.printer("maxS"));*/

        infSuffix.scanInit(0, true);
        supSuffix.scanInit(0, true);
        if ((m == Mode.ATMOST || m == Mode.BOTH)) adjustMin(n, minInfSuffix(0));
        if ((m == Mode.ATLEAST || m == Mode.BOTH)) adjustMax(n, maxSupSuffix(0));
        int left, right;
        for (int i = 0; i < n; i++) {
            infPrefix.scanInit(i, true);
            supPrefix.scanInit(i, true);
            infSuffix.scanInit(i, true);
            supSuffix.scanInit(i, true);
            int v = x[i].getInf();
            int w;
            left = right = Integer.MIN_VALUE;
            do {
                final int mind = infPrefix.get(i, v) + infSuffix.get(i, v) - 1;
                final int maxd = supPrefix.get(i, v) + supSuffix.get(i, v) - 1;
                if ((m == Mode.ATMOST) && mind > occ.getSup()) {
                    if (v == right + 1) {
                        right = v;
                    } else {
                        x[i].removeInterval(left, right, this, false);
                        left = right = v;
                    }
//                    x[i].removeVal(v, this, false);
                }
                if ((m == Mode.ATLEAST) && maxd < occ.getInf()) {
                    if (v == right + 1) {
                        right = v;
                    } else {
                        x[i].removeInterval(left, right, this, false);
                        left = right = v;
                    }
//                    x[i].removeVal(v, this, false);
                }
                if (m == Mode.BOTH && test(mind, maxd)) {
                    if (v == right + 1) {
                        right = v;
                    } else {
                        x[i].removeInterval(left, right, this, false);
                        left = right = v;
                    }
//                    x[i].removeVal(v, this, false);
                }
                w = v;
                v = getNext(x[i], v);
            } while (w != v);
            x[i].removeInterval(left, right, this, false);
        }
    }

    public final boolean test(final int min, final int max) {
        int i = min;
        while (i <= max && !occ.canBeInstantiatedTo(i)) {
            i++;
        }
        return i > max;
    }

    public String printer() {
        final StringBuilder s = new StringBuilder();
        for (final IntDomainVar v : x) {
            s.append(v.pretty()).append('\n');
        }
        return s.toString();
    }

    /**
     * Default implementation of the isSatisfied by
     * delegating to the isSatisfied(int[] tuple)
     *
     * @return
     */
    @Override
    public boolean isSatisfied() {
        if (isCompletelyInstantiated()) {
            final TIntHashSet values = new TIntHashSet();
            values.add(vars[0].getVal());
            for (int i = 1; i < n; i++) {
                if (vars[i - 1].getVal() > vars[i].getVal()) {
                    return false;
                }
                values.add(vars[i].getVal());
            }
            return values.size() == occ.getVal();
        }
        return false;
    }

    /**
     * TEMPORARY: if not overriden by the constraint, throws an error
     * to avoid bug using reified constraints in constraints
     * that have not been changed to fulfill this api yet !
     *
     * @param tuple
     * @return
     */
    @Override
    public boolean isSatisfied(final int[] tuple) {
        final TIntHashSet values = new TIntHashSet();
        values.add(tuple[0]);
        for (int i = 1; i < n; i++) {
            if (tuple[i - 1] > tuple[i]) {
                return false;
            }
            values.add(tuple[i]);
        }
        return values.size() == tuple[n];
    }
}