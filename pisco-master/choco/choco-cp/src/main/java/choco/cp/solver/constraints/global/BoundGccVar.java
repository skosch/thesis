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

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Arrays;
import java.util.Comparator;

/**
 * User: hcambaza
 * Bound Global cardinality : Given an array of variables vars, an array of variables card to represent the cardinalities, the constraint ensures that the number of occurences
 * of the value i among the variables is equal to card[i].
 * this constraint enforces :
 * - Bound Consistency over vars regarding the lower and upper bounds of cards
 * - maintain the upperbound of card by counting the number of variables in which each value
 * can occur
 * - maintain the lowerbound of card by counting the number of variables instantiated to a value
 * - enforce card[0] + ... + card[m] = n (n = the number of variables, m = number of values)
 */
public class BoundGccVar extends AbstractLargeIntSConstraint {

    private int[] treelinks; // Tree links
    private int[] d; // Diffs between critical capacities
    private int[] h; // Hall interval links
    private int[] bounds;

    private int[] stableInterval;
    private int[] potentialStableSets;
    private int[] newMin;

    int offset = 0;

    private int nbBounds;
    int nbVars;  //number of variables (without the cardinalities variables)
    private IntDomainVar[] card;

    Interval[] minsorted, maxsorted;

    private final int[] minOccurrences, maxOccurrences;

    PartialSum l, u;

    private int firstValue;
    int range;

    //desynchornized copy of domains to make sure we properly counting
    //the number of variables that still have value i in their domain
    //(table val_maxOcc)
    IStateInt[] val_maxOcc;
    IStateInt[] val_minOcc;

    public static IntDomainVar[] makeVarTable(IntDomainVar[] vars,
                                              IntDomainVar[] card) {
        if (card != null) {
            IntDomainVar[] allvars = new IntDomainVar[vars.length + card.length];
            System.arraycopy(vars, 0, allvars, 0, vars.length);
            System.arraycopy(card, 0, allvars, vars.length, card.length);
            return allvars;
        } else {
            return vars;
        }
    }

    /**
     * Bound Global cardinality : Given an array of variables vars, min the minimal value over all variables,
     * and max the maximal value over all variables (or a table IntDomainVar to represent the cardinalities), the constraint ensures that the number of occurences
     * of the value i among the variables is between low[i - min] and up[i - min]. Note that the length
     * of low and up should be max - min + 1.
     * Use the propagator of :
     * C.-G. Quimper, P. van Beek, A. Lopez-Ortiz, A. Golynski, and S.B. Sadjad.
     * An efficient bounds consistency algorithm for the global cardinality constraint.
     * CP-2003.
     */
    public BoundGccVar(IntDomainVar[] vars,
                       IntDomainVar[] card,
                       int firstCardValue,
                       int lastCardValue, IEnvironment environment) {
        super(ConstraintEvent.LINEAR, makeVarTable(vars, card));
        this.card = card;
        int n = vars.length;
        this.range = lastCardValue - firstCardValue + 1;
        this.nbVars = n;
        treelinks = new int[2 * n + 2];
        d = new int[2 * n + 2];
        h = new int[2 * n + 2];
        bounds = new int[2 * n + 2];
        stableInterval = new int[2 * n + 2];
        potentialStableSets = new int[2 * n + 2];
        newMin = new int[n];

        final Interval[] intervals = new Interval[n];
        minsorted = new Interval[n];
        maxsorted = new Interval[n];
        for (int i = 0; i < nbVars; i++) {
            intervals[i] = new Interval();
            intervals[i].var = vars[i];
            intervals[i].idx = i;
            minsorted[i] = intervals[i];
            maxsorted[i] = intervals[i];
        }
        this.offset = firstCardValue;
        this.firstValue = firstCardValue;
        val_maxOcc = new IStateInt[range];
        val_minOcc = new IStateInt[range];
        for (int i = 0; i < range; i++) {
            val_maxOcc[i] = environment.makeInt(0);
            val_minOcc[i] = environment.makeInt(0);
        }

        l = new PartialSum(firstValue, range);
        u = new PartialSum(firstValue, range);
        minOccurrences = new int[range];
        maxOccurrences = new int[range];
    }

    public int getMaxOcc(int i) {
        return card[i].getSup();
    }

    public int getMinOcc(int i) {
        return card[i].getInf();
    }

    protected void init() {
        for (int i = 0; i < range; i++) {
            val_maxOcc[i].set(0);
            val_minOcc[i].set(0);
        }
    }

//    public void updateSup(IntDomainVar v, int nsup, int idx) throws ContradictionException {
//        v.updateSup(nsup, VarEvent.domOverWDegIdx(cIndices[idx]));//cIndices[idx]); //<hca> why is it not idempotent ?
//    }
//
//    public void updateInf(IntDomainVar v, int ninf, int idx) throws ContradictionException {
//        v.updateInf(ninf, VarEvent.domOverWDegIdx(cIndices[idx]));//cIndices[idx]); //<hca> why is it not idempotent ?
//    }

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

    protected final void sortIt() {
        Arrays.sort(minsorted, SORT.MIN);
        Arrays.sort(maxsorted, SORT.MAX);

        int min = minsorted[0].var.getInf();
        int max = maxsorted[0].var.getSup() + 1;
        int last = l.firstValue + 1; //change here compared to the boundalldiff
        int nb = 0;
        bounds[0] = last;

        int i = 0, j = 0;
        while (true) {
            if (i < nbVars && min <= max) {
                if (min != last) {
                    bounds[++nb] = last = min;
                }
                minsorted[i].minrank = nb;
                if (++i < nbVars) {
                    min = minsorted[i].var.getInf();
                }
            } else {
                if (max != last) {
                    bounds[++nb] = last = max;
                }
                maxsorted[j].maxrank = nb;
                if (++j == nbVars) {
                    break;
                }
                max = maxsorted[j].var.getSup() + 1;
            }
        }

        this.nbBounds = nb;
        bounds[nb + 1] = u.lastValue + 1; //change here compared to the boundalldiff
    }

    protected final void pathset(int[] tab, int start, int end, int to) {
        int next = start;
        int prev = next;

        while (prev != end) {
            next = tab[prev];
            tab[prev] = to;
            prev = next;
        }
    }

    protected final int pathmin(int[] tab, int i) {
        while (tab[i] < i) {
            i = tab[i];
        }
        return i;
    }

    protected final int pathmax(int[] tab, int i) {
        while (tab[i] > i) {
            i = tab[i];
        }
        return i;
    }

    /**
     * Shrink the lower bounds for the max occurences
     *
     * @throws choco.kernel.solver.ContradictionException
     *
     */
    protected final void filterLowerMax() throws ContradictionException {
        int i, j, w, x, y, z;

        for (i = 1; i <= nbBounds + 1; i++) {
            treelinks[i] = h[i] = i - 1;
            d[i] = u.sum(bounds[i - 1], bounds[i] - 1);
        }
        for (i = 0; i < nbVars; i++) { // visit intervals in increasing max order
            // get interval bounds
            x = maxsorted[i].minrank;
            y = maxsorted[i].maxrank;
            j = treelinks[z = pathmax(treelinks, x + 1)];
            if (--d[z] == 0) {
                treelinks[z = pathmax(treelinks, treelinks[z] = z + 1)] = j;
            }
            pathset(treelinks, x + 1, z, z);
            if (d[z] < u.sum(bounds[y], bounds[z] - 1)) {
                this.fail();
            }
            if (h[x] > x) {
                w = pathmax(h, h[x]);
//                updateInf(maxsorted[i].var, bounds[w], maxsorted[i].idx);
                maxsorted[i].var.updateInf(bounds[w], this, true);
                pathset(h, x, w, w);
            }
            if (d[z] == u.sum(bounds[y], bounds[z] - 1)) {
                pathset(h, h[y], j - 1, y); // mark hall interval
                h[y] = j - 1; //("hall interval [%d,%d)\n",bounds[j],bounds[y]);
            }
        }
    }

    /**
     * Shrink the upper bounds for the max occurences
     *
     * @throws ContradictionException
     */
    protected final void filterUpperMax() throws ContradictionException {
        int i, j, w, x, y, z;

        for (i = 0; i <= nbBounds; i++) {
            d[i] = u.sum(bounds[i], bounds[treelinks[i] = h[i] = i + 1] - 1);
        }
        for (i = nbVars; --i >= 0; ) { // visit intervals in decreasing min order
            // get interval bounds
            x = minsorted[i].maxrank;
            y = minsorted[i].minrank;
            j = treelinks[z = pathmin(treelinks, x - 1)];
            if (--d[z] == 0) {
                treelinks[z = pathmin(treelinks, treelinks[z] = z - 1)] = j;
            }
            pathset(treelinks, x - 1, z, z);
            if (d[z] < u.sum(bounds[z], bounds[y] - 1)) {
                this.fail();
            }
            if (h[x] < x) {
                w = pathmin(h, h[x]);
//                updateSup(minsorted[i].var, bounds[w] - 1, minsorted[i].idx);
                minsorted[i].var.updateSup(bounds[w] - 1, this, false);
                pathset(h, x, w, w);
            }
            if (d[z] == u.sum(bounds[z], bounds[y] - 1)) {
                pathset(h, h[y], j + 1, y);
                h[y] = j + 1;
            }
        }
    }

    /*
      * Shrink the lower bounds for the min occurrences model.
      * called as: filterLowerMin(t, d, h, stableInterval, potentialStableSets, newMin);
      */
    public final void filterLowerMin() throws ContradictionException {
        int i, j, w, x, y, z, v;

        for (w = i = nbBounds + 1; i > 0; i--) {
            potentialStableSets[i] = stableInterval[i] = i - 1;
            d[i] = l.sum(bounds[i - 1], bounds[i] - 1);
            // If the capacity between both bounds is zero, we have
            // an unstable set between these two bounds.
            if (d[i] == 0) {
                h[i - 1] = w;
            } else {
                w = h[w] = i - 1;
            }
        }

        for (i = w = nbBounds + 1; i >= 0; i--) {
            if (d[i] == 0) {
                treelinks[i] = w;
            } else {
                w = treelinks[w] = i;
            }
        }

        for (i = 0; i < nbVars; i++) { // visit intervals in increasing max order
            // Get interval bounds
            x = maxsorted[i].minrank;
            y = maxsorted[i].maxrank;
            j = treelinks[z = pathmax(treelinks, x + 1)];
            if (z != x + 1) {
                // if bounds[z] - 1 belongs to a stable set,
                // [bounds[x], bounds[z]) is a sub set of this stable set
                v = potentialStableSets[w = pathmax(potentialStableSets, x + 1)];
                pathset(potentialStableSets, x + 1, w, w); // path compression
                w = y < z ? y : z;
                pathset(potentialStableSets, potentialStableSets[w], v, w);
                potentialStableSets[w] = v;
            }

            if (d[z] <= l.sum(bounds[y], bounds[z] - 1)) {
                // (potentialStableSets[y], y] is a stable set
                w = pathmax(stableInterval, potentialStableSets[y]);
                pathset(stableInterval, potentialStableSets[y], w, w); // Path compression
                pathset(stableInterval, stableInterval[y], v = stableInterval[w], y);
                stableInterval[y] = v;
            } else {
                // Decrease the capacity between the two bounds
                if (--d[z] == 0) {
                    treelinks[z = pathmax(treelinks, treelinks[z] = z + 1)] = j;
                }

                // If the lower bound belongs to an unstable or a stable set,
                // remind the new value we might assigned to the lower bound
                // in case the variable doesn't belong to a stable set.
                if (h[x] > x) {
                    w = newMin[i] = pathmax(h, x);
                    pathset(h, x, w, w); // path compression
                } else {
                    newMin[i] = x; // Do not shrink the variable
                }

                // If an unstable set is discovered
                if (d[z] == l.sum(bounds[y], bounds[z] - 1)) {
                    if (h[y] > y) {
                        y = h[y]; // Equivalent to pathmax since the path is fully compressed
                    }
                    pathset(h, h[y], j - 1, y); // mark the new unstable set
                    h[y] = j - 1;
                }
            }
            pathset(treelinks, x + 1, z, z); // path compression
        }

        // If there is a failure set
        if (h[nbBounds] != 0) {
            this.fail();
        }

        // Perform path compression over all elements in
        // the stable interval data structure. This data
        // structure will no longer be modified and will be
        // accessed n or 2n times. Therefore, we can afford
        // a linear time compression.
        for (i = nbBounds + 1; i > 0; i--) {
            if (stableInterval[i] > i) {
                stableInterval[i] = w;
            } else {
                w = i;
            }
        }

        // For all variables that are not a subset of a stable set, shrink the lower bound
        for (i = nbVars - 1; i >= 0; i--) {
            x = maxsorted[i].minrank;
            y = maxsorted[i].maxrank;
            if ((stableInterval[x] <= x) || (y > stableInterval[x])) {
//                updateInf(maxsorted[i].var, l.skipNonNullElementsRight(bounds[newMin[i]]), maxsorted[i].idx);
                maxsorted[i].var.updateInf(l.skipNonNullElementsRight(bounds[newMin[i]]), this, false);
            }
        }
    }

    /*
    * Shrink the upper bounds for the min occurrences model.
    * called as: filterUpperMin(t, d, h, stableInterval, newMin);
    */
    public final void filterUpperMin() throws ContradictionException {
        int i, w = 0, n = nbVars;
        for (i = 0; i <= nbBounds; i++) {
            d[i] = l.sum(bounds[i], bounds[i + 1] - 1);
            if (d[i] == 0) {
                treelinks[i] = w;
            } else {
                w = treelinks[w] = i;
            }
        }
        treelinks[w] = i;
        w = 0;
        for (i = 1; i <= nbBounds; i++) {
            if (d[i - 1] == 0) {
                h[i] = w;
            } else {
                w = h[w] = i;
            }
        }
        h[w] = i;
        for (i = n - 1; i >= 0; i--) { // visit intervals in decreasing min order
            // Get interval bounds
            int x = minsorted[i].maxrank;
            int y = minsorted[i].minrank;

            // Solve the lower bound model
            int z = pathmin(treelinks, x - 1);
            int j = treelinks[z];

            // If the variable is not in a discovered stable set
            // Possible optimization: Use the array stableInterval to perform this palm
            if (d[z] > l.sum(bounds[z], bounds[y] - 1)) {
                if (--d[z] == 0) {
                    treelinks[z] = z - 1;
                    z = pathmin(treelinks, treelinks[z]);
                    treelinks[z] = j;
                }
                if (h[x] < x) {
                    w = pathmin(h, h[x]);
                    newMin[i] = w;       // re-use the table newMin to store the max
                    pathset(h, x, w, w); // path compression
                } else {
                    newMin[i] = x;
                }
                if (d[z] == l.sum(bounds[z], bounds[y] - 1)) {
                    if (h[y] < y) {
                        y = h[y];
                    }
                    pathset(h, h[y], j + 1, y);
                    h[y] = j + 1;
                }
            }
            pathset(treelinks, x - 1, z, z);
        }
        // For all variables that are not subsets of a stable set, shrink the upper bound
        for (i = n - 1; i >= 0; i--) {
            int x = minsorted[i].minrank;
            int y = minsorted[i].maxrank;
            if ((stableInterval[x] <= x) || (y > stableInterval[x])) {
//                updateSup(minsorted[i].var, l.skipNonNullElementsLeft(bounds[newMin[i]] - 1), minsorted[i].idx);
                minsorted[i].var.updateSup(l.skipNonNullElementsLeft(bounds[newMin[i]] - 1), this, false);
            }
        }

    }

    public final void initBackDataStruct() throws ContradictionException {
        for (int i = 0; i < range; i++) {
            for (int j = 0; j < nbVars; j++) {
                if (vars[j].canBeInstantiatedTo(i + offset)) {
                    val_maxOcc[i].add(1);
                }
                if (vars[j].isInstantiatedTo(i + offset)) {
                    val_minOcc[i].add(1);
                }
            }
        }
    }

    private void initCard() throws ContradictionException {
        for (int i = 0; i < range; i++) {
            if (val_maxOcc[i].get() == 0) {
                card[i].instantiate(0, this, false);
            } else {
                card[i].updateInf(val_minOcc[i].get(), this, false);
            }
        }
    }

    @Override
    public void awake() throws ContradictionException {
        init();
        initBackDataStruct();
        initCard();
        for (int i = 0; i < vars.length; i++) {
            IntDomainVar var = vars[i];
            if (var.isInstantiated()) {
                // if a value has been instantiated to its max number of occurrences
                // remove it from all variables
                if (i < nbVars) {
                    int val = vars[i].getVal();
                    filterBCOnInst(val);
                } else {
                    filterBCOnInst(i - nbVars + offset);
                }
            }
        }
        if (directInconsistentCount())
            this.fail();
        propagate();
    }

    public boolean directInconsistentCount() {
        for (int i = 0; i < range; i++) {
            if (val_maxOcc[i].get() < card[i].getInf() ||
                    val_minOcc[i].get() > card[i].getSup())
                return true;
        }
        return false;
    }

    public final void dynamicInitOfPartialSum() {
        for (int i = 0; i < range; i++) {
            maxOccurrences[i] = card[i].getSup();
            minOccurrences[i] = card[i].getInf();
        }
        l.compute(minOccurrences);
        u.compute(maxOccurrences);
    }

    @Override
    public void propagate() throws ContradictionException {
        propagateSumCard();
        dynamicInitOfPartialSum();
        sortIt();

        // The variable domains must be inside the domain defined by
        // the lower bounds (l) and the upper bounds (u).
        assert (l.minValue() == u.minValue());
        assert (l.maxValue() == u.maxValue());
        assert (l.minValue() <= minsorted[0].var.getInf());
        assert (maxsorted[nbVars - 1].var.getSup() <= u.maxValue());
        assert (!directInconsistentCount());

        // Checks if there are values that must be assigned before the
        // smallest interval or after the last interval. If this is
        // the case, there is no solution to the problem
        // This is not an optimization since
        // filterLower{Min,Max} and
        // filterUpper{Min,Max} do not check for this case.
        if ((l.sum(l.minValue(), minsorted[0].var.getInf() - 1) > 0) ||
                (l.sum(maxsorted[nbVars - 1].var.getSup() + 1, l.maxValue()) > 0)) {
            this.fail();
        }
        filterLowerMax();
        filterLowerMin();
        filterUpperMax();
        filterUpperMin();
    }


    @Override
    public void awakeOnInf(int i) throws ContradictionException {
        this.constAwake(false);
        if (i < nbVars) {
            if (!vars[i].hasEnumeratedDomain()) {
                filterBCOnInf(i);
            }
        }
    }

    //in case of bound variables, the bound has to be checked
    public final void filterBCOnInf(int i) throws ContradictionException {
        int inf = vars[i].getInf();
        int nbInf = val_minOcc[inf - offset].get();
        if (vars[i].isInstantiatedTo(inf)) {
            nbInf--;
        }
        if (nbInf == getMaxOcc(inf - offset)) {
            vars[i].updateInf(inf + 1, this, true);
        }
    }

    @Override
    public void awakeOnSup(int i) throws ContradictionException {
        this.constAwake(false);
        if (i < nbVars) {
            if (!vars[i].hasEnumeratedDomain()) {
                filterBCOnSup(i);
            }
        }
    }

    //in case of bound variables, the bound has to be checked
    public final void filterBCOnSup(int i) throws ContradictionException {
        int sup = vars[i].getSup();
        int nbSup = val_minOcc[sup - offset].get();
        if (vars[i].isInstantiatedTo(sup)) {
            nbSup--;
        }
        if (nbSup == getMaxOcc(sup - offset)) {
            vars[i].updateSup(sup - 1, this, true);
        }
    }

    @Override
    public void awakeOnInst(int i) throws ContradictionException {   // Propagation classique
        int val = vars[i].getVal();
        constAwake(false);
        // if a value has been instantiated to its max number of occurrences
        // remove it from all variables
        if (i < nbVars) {
            //update lower bounds of cardinalities
            val_minOcc[val - offset].add(1);
            card[val - offset].updateInf(val_minOcc[val - offset].get(), this, false);
            filterBCOnInst(val);
        } else {
            filterBCOnInst(i - nbVars + offset);
        }
    }

    /**
     * Enforce simple occurrences reasonnings on value val
     * no need to reason on the number of possible (instead of sure) values
     * as this will be done as part of the BC on vars
     *
     * @param val
     * @throws ContradictionException
     */
    public final void filterBCOnInst(int val) throws ContradictionException {
        int nbvalsure = val_minOcc[val - offset].get();
        if (nbvalsure > getMaxOcc(val - offset)) {
            this.fail();
        } else if (nbvalsure == getMaxOcc(val - offset)) {
            for (int j = 0; j < nbVars; j++) {
                if (!vars[j].isInstantiatedTo(val)) {
                    vars[j].removeVal(val, this, true);// cIndices[j]); not idempotent because data structure is maintained in awakeOnX methods
                }
            }
        }
    }

    public final void filterBCOnRem(int val) throws ContradictionException {
        int nbpos = val_maxOcc[val - offset].get();
        if (nbpos < getMinOcc(val - offset)) {
            this.fail();
        } else if (nbpos == getMinOcc(val - offset)) {
            for (int j = 0; j < nbVars; j++) {
                if (vars[j].canBeInstantiatedTo(val)) {
                    vars[j].instantiate(val, this, true);// cIndices[j]); not idempotent because data structure is maintained in awakeOnX methods
                }
            }
        }

    }

    /**
     * Only maintain the data structure and update upperbounds of card
     *
     * @throws ContradictionException
     */
    @Override
    public void awakeOnRem(int idx, int i) throws ContradictionException {
        if (idx < nbVars) {
            val_maxOcc[i - offset].add(-1);
            card[i - offset].updateSup(val_maxOcc[i - offset].get(), this, true);
        }
    }

    /**
     * Enforce sum of the cardinalities = nbVariable
     *
     * @throws ContradictionException
     */
    public final void propagateSumCard() throws ContradictionException {
        boolean fixpoint = true;
        while (fixpoint) {
            fixpoint = false;
            int lb = 0;
            int ub = 0;
            for (int i = 0; i < range; i++) {
                lb += card[i].getInf();
                ub += card[i].getSup();

            }
            for (int i = 0; i < range; i++) {
                fixpoint |= card[i].updateSup(nbVars - (lb - card[i].getInf()), this, false);
                fixpoint |= card[i].updateInf(nbVars - (ub - card[i].getSup()), this, false);
            }
        }
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        int[] occurrences = new int[this.range];
        for (int i = 0; i < nbVars; i++) {
            occurrences[tuple[i] - this.offset]++;
        }
        for (int i = 0; i < occurrences.length; i++) {
            int occurrence = occurrences[i];
            if (tuple[nbVars + i] != occurrence) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("BoundGccV({");
        for (int i = 0; i < nbVars; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            IntDomainVar var = vars[i];
            sb.append(var.pretty());
        }
        sb.append("}, {");
        for (int i = 0; i < this.range; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("#").append(this.offset + i).append(" = ").append(vars[nbVars + i].pretty());
        }
        sb.append("})");
        return sb.toString();
    }

    @Override
    public Boolean isEntailed() {
        throw new UnsupportedOperationException("isEntailed not yet implemented on package choco.kernel.solver.constraints.global.BoundAlldiff");
    }

    protected static class Interval {
        int minrank, maxrank;
        IntDomainVar var;
        int idx;
    }

    /**
     * A class to deal with partial sum data structure adapted to
     * the filterLower{Min,Max} and filterUpper{Min,Max} functions.
     * Two elements before and after the element list will be added with a weight of 1
     */
    static final class PartialSum {
        private int[] sum;
        private int[] ds;
        private int firstValue, lastValue, range;


        public PartialSum(int firstValue, int count) {
            this.range = count;
            this.sum = new int[count + 5];
            this.ds = new int[count + 5];
            this.firstValue = firstValue - 3;
            this.lastValue = firstValue + count + 1;
        }

        public void compute(int[] elt) {
            sum[0] = 0;
            sum[1] = 1;
            sum[2] = 2;
            int i, j;
            for (i = 2; i < range + 2; i++) {
                sum[i + 1] = sum[i] + elt[i - 2];
            }
            sum[i + 1] = sum[i] + 1;
            sum[i + 2] = sum[i + 1] + 1;

            i = range + 3;
            for (j = i + 1; i > 0; ) {
                while (sum[i] == sum[i - 1]) {
                    ds[i--] = j;
                }
                j = ds[j] = i--;
            }
            ds[j] = 0;
        }

        public int sum(int from, int to) {
            if (from <= to) {
                return sum[to - firstValue] - sum[from - firstValue - 1];
            } else {
                return sum[to - firstValue - 1] - sum[from - firstValue];
            }
        }

        public int minValue() {
            return firstValue + 3;
        }

        public int maxValue() {
            return lastValue - 2;
        }

        public int skipNonNullElementsRight(int value) {
            value -= firstValue;
            return (ds[value] < value ? value : ds[value]) + firstValue;
        }

        public int skipNonNullElementsLeft(int value) {
            value -= firstValue;
            return (ds[value] > value ? ds[ds[value]] : value) + firstValue;
        }
    }


}