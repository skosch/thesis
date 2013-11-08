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

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Arrays;
import java.util.Comparator;

public final class BoundAllDiff extends AbstractLargeIntSConstraint {
    public static boolean PROPAGATE_ON_INSTANTIATIONS = true;
    public boolean PROPAGATE_ON_BOUNDS = true;

    int[] t; // Tree links
    int[] d; // Diffs between critical capacities
    int[] h; // Hall interval links
    int[] bounds;

    int nbBounds;

    final Interval[] minsorted;
    final Interval[] maxsorted;

    boolean infBoundModified = true;
    boolean supBoundModified = true;

    public BoundAllDiff(IntDomainVar[] vars, boolean global) {
        super(ConstraintEvent.LINEAR, vars);
        int n = this.getNbVars();
        if (!global) {
            PROPAGATE_ON_BOUNDS = false;
        }

        t = new int[2 * n + 2];
        d = new int[2 * n + 2];
        h = new int[2 * n + 2];
        bounds = new int[2 * n + 2];

        Interval[] intervals = new Interval[n];
        minsorted = new Interval[n];
        maxsorted = new Interval[n];

        for (int i = 0; i < vars.length; i++) {
            intervals[i] = new Interval();
            intervals[i].var = vars[i];
            intervals[i].idx = i;
            minsorted[i] = intervals[i];
            maxsorted[i] = intervals[i];
        }
    }

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
        // return 0x0B;
    }

    static enum SORT implements Comparator<Interval> {
        MAX {
            @Override
            public int compare(Interval o1, Interval o2) {
                return o1.var.getSup() - o2.var.getSup();
            }
        },
        MIN {
            @Override
            public int compare(Interval o1, Interval o2) {
                return o1.var.getInf() - o2.var.getInf();
            }
        },;
    }

    protected void sortIt() {
        Arrays.sort(minsorted, SORT.MIN);
        Arrays.sort(maxsorted, SORT.MAX);


        int min = minsorted[0].var.getInf();
        int max = maxsorted[0].var.getSup() + 1;
        int last = min - 2;
        int nb = 0;
        bounds[0] = last;

        int i = 0, j = 0;
        while (true) {
            if (i < this.getNbVars() && min <= max) {
                if (min != last) {
                    bounds[++nb] = last = min;
                }
                minsorted[i].minrank = nb;
                if (++i < this.getNbVars()) {
                    min = minsorted[i].var.getInf();
                }
            } else {
                if (max != last) {
                    bounds[++nb] = last = max;
                }
                maxsorted[j].maxrank = nb;
                if (++j == this.getNbVars()) {
                    break;
                }
                max = maxsorted[j].var.getSup() + 1;
            }
        }

        this.nbBounds = nb;
        bounds[nb + 1] = bounds[nb] + 2;
    }

    protected void pathset(int[] tab, int start, int end, int to) {
        int next = start;
        int prev = next;

        while (prev != end) {
            next = tab[prev];
            tab[prev] = to;
            prev = next;
        }
    }

    protected int pathmin(int[] tab, int i) {
        while (tab[i] < i) {
            i = tab[i];
        }
        return i;
    }

    protected int pathmax(int[] tab, int i) {
        while (tab[i] > i) {
            i = tab[i];
        }
        return i;
    }

    protected void filterLower() throws ContradictionException {
        for (int i = 1; i <= nbBounds + 1; i++) {
            t[i] = h[i] = i - 1;
            d[i] = bounds[i] - bounds[i - 1];
        }

        for (int i = 0; i < this.getNbVars(); i++) {
            int x = maxsorted[i].minrank;
            int y = maxsorted[i].maxrank;
            int z = pathmax(t, x + 1);
            int j = t[z];

            if (--d[z] == 0) {
                t[z] = z + 1;
                z = pathmax(t, t[z]);
                t[z] = j;
            }

            pathset(t, x + 1, z, z);

            if (d[z] < bounds[z] - bounds[y]) {
                this.fail();
            }

            if (h[x] > x) {
                int w = pathmax(h, h[x]);
                maxsorted[i].var.updateInf(bounds[w], this, false);
                pathset(h, x, w, w);
            }

            if (d[z] == bounds[z] - bounds[y]) {
                pathset(h, h[y], j - 1, y);
                h[y] = j - 1;
            }
        }
    }

    protected void filterUpper() throws ContradictionException {
        for (int i = 0; i <= nbBounds; i++) {
            t[i] = h[i] = i + 1;
            d[i] = bounds[i + 1] - bounds[i];
        }
        for (int i = this.getNbVars() - 1; i >= 0; i--) {
            int x = minsorted[i].maxrank;
            int y = minsorted[i].minrank;
            int z = pathmin(t, x - 1);
            int j = t[z];

            if (--d[z] == 0) {
                t[z] = z - 1;
                z = pathmin(t, t[z]);
                t[z] = j;
            }

            pathset(t, x - 1, z, z);

            if (d[z] < bounds[y] - bounds[z]) {
                this.fail();
            }

            if (h[x] < x) {
                int w = pathmin(h, h[x]);
                minsorted[i].var.updateSup(bounds[w] - 1, this, false);
                pathset(h, x, w, w);
            }
            if (d[z] == bounds[y] - bounds[z]) {
                pathset(h, h[y], j + 1, y);
                h[y] = j + 1;
            }
        }
    }

    @Override
    public void awake() throws ContradictionException {
        int left, right;
        for (int j = 0; j < vars.length; j++) {
            left = right = Integer.MIN_VALUE;
            for (int i = 0; i < vars.length; i++) {
                if (vars[i].isInstantiated()) {
                    int val = vars[i].getVal();
                    if (i != j) {
                        if (val == right + 1) {
                            right = val;
                        } else {
                            vars[j].removeInterval(left, right, this, false);
                            left = right = val;
                        }
//                        vars[j].removeVal(vars[i].getVal(), this, true);
                    }
                }
            }
            vars[j].removeInterval(left, right, this, false);
        }

        propagate();
    }

    @Override
    public void propagate() throws ContradictionException {
        if (infBoundModified || supBoundModified) {
            sortIt();
            filterLower();
            filterUpper();
            infBoundModified = false;
            supBoundModified = false;
        }
    }

    @Override
    public void awakeOnInf(int i) throws ContradictionException {
        if (PROPAGATE_ON_BOUNDS) {
            infBoundModified = true;
            this.constAwake(false);
            for (int j = 0; j < vars.length; j++) {
                if (j != i && vars[j].isInstantiated()) {
                    if (vars[j].getVal() == vars[i].getInf()) {
                        vars[i].updateInf(vars[j].getVal() + 1, this, true);
                    }
                }
            }
        }
    }

    @Override
    public void awakeOnSup(int i) throws ContradictionException {
        if (PROPAGATE_ON_BOUNDS) {
            supBoundModified = true;
            this.constAwake(false);
            for (int j = 0; j < vars.length; j++) {
                if (j != i && vars[j].isInstantiated()) {
                    if (vars[j].getVal() == vars[i].getSup()) {
                        vars[i].updateSup(vars[j].getVal() - 1, this, true);
                    }
                }
            }
        }
    }

    @Override
    public void awakeOnInst(int i) throws ContradictionException {   // Propagation classique
        if (PROPAGATE_ON_INSTANTIATIONS) {
            infBoundModified = true;
            supBoundModified = true;
            this.constAwake(false);
            int val = vars[i].getVal();
            for (int j = 0; j < vars.length; j++) {
                if (j != i) {
                    vars[j].removeVal(val, this, true);
                }
            }
        }
    }

    @Override
    public void awakeOnBounds(int idx) throws ContradictionException {
        infBoundModified = supBoundModified = true;
        for (int j = 0; j < vars.length; j++) {
            if (j != idx && vars[j].isInstantiated()) {
                int val = vars[j].getVal();
                if (val == vars[idx].getInf()) {
                    vars[idx].updateInf(val + 1, this, false);
                }
                if (val == vars[idx].getSup()) {
                    vars[idx].updateSup(val - 1, this, false);
                }
            }
        }
    }


    @Override
    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
    }

    /**
     * This method assumes that all variables are instantiated and checks if the values are consistent with the
     * constraint.
     * Here it checks that all variables have distinct values. It uses double for loops (Thus the complixity is in O(n^2).
     *
     * @return true if values are different.
     */
    @Override
    public boolean isSatisfied(int[] tuple) {
        for (int i = 0; i < vars.length; i++) {
            for (int j = i + 1; j < vars.length; j++) {
                if (tuple[i] == tuple[j]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("BoundAllDiff({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            IntDomainVar var = vars[i];
            sb.append(var.pretty());
        }
        sb.append("})");
        return sb.toString();
    }

    private static class Interval {
        int minrank, maxrank;
        IntDomainVar var;
        int idx;
    }

    //by default, no information is known
    public int getFineDegree(int idx) {
        return vars.length - 1;
    }
}
