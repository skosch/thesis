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

package choco.cp.solver.variables.integer;

import choco.cp.common.util.iterators.IntDomainIterator;
import choco.cp.solver.variables.delta.StackDeltaDomain;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.solver.propagation.PropagationEngine;
import gnu.trove.TIntIntHashMap;

import java.util.Random;

/**
 * Integer domain implementation using linked list of indices. This implementation is more
 * costful in terms of memory than bit set implementation but should be more efficient in terms
 * of CPU (mainly for interating the domain).
 * <p/>
 * This implementation should be extendable to deal with large sparse domains, since we mainly
 * deal with indices of values (which is managed implicitely here: offset + index).
 * <p/>
 * Author: Guillaume Rochart
 * Creation date: January, 20th 2007
 */
public class LinkedIntDomain extends AbstractIntDomain {
    /**
     * A random generator for random value from the domain
     */

    private static final Random random = new Random(System.currentTimeMillis());

    /**
     * A vector containing the index of the next value in the domain. The value is -1 if the
     * value in not in the domain anymore.
     */
    private final IStateIntVector nextIndex;

    /**
     * A vector containing the index of the previous value in the domain. The value is -1 if the
     * value in not in the domain anymore.
     */
    private final IStateIntVector prevIndex;

    /**
     * The (dynamic) lower bound of the domain.
     */
    private final IStateInt lowerBound;

    /**
     * The (dynamic) upper bound of the domain.
     */
    private final IStateInt upperBound;

    /**
     * The (dynamic) size of the domain.
     */
    private final IStateInt size;

    /**
     * The value of the original lower bound of the domain.
     */
    private final int offset;


    private final int[] sortedValues;

    private final TIntIntHashMap val2ind;

    protected IntDomainIterator _iterator;

    /**
     * Constructs a new domain for the specified variable and bounds.
     *
     * @param v                 The involved variable.
     * @param a                 Minimal value.
     * @param b                 Maximal value.
     * @param environment
     * @param propagationEngine
     */

    public LinkedIntDomain(IntDomainVarImpl v, int a, int b, IEnvironment environment, PropagationEngine propagationEngine) {
        super(v, propagationEngine);
        this.offset = a;
        lowerBound = environment.makeInt(a);
        upperBound = environment.makeInt(b);
        int size = b - a + 1;
        sortedValues = new int[size];
        val2ind = new TIntIntHashMap();
        this.size = environment.makeInt(size);
        int[] prevIndices = new int[size];
        int[] nextIndices = new int[size];
        for (int i = 0; i < nextIndices.length; i++) {
            sortedValues[i] = a + i;
            val2ind.put(a + i, i);
            nextIndices[i] = (i + 1) % size;
            prevIndices[i] = (i - 1 + size) % size;
        }
        nextIndex = environment.makeIntVector(nextIndices);
        prevIndex = environment.makeIntVector(prevIndices);

        deltaDom = new StackDeltaDomain();

    }

    public LinkedIntDomain(IntDomainVarImpl v, int[] sortedValues, IEnvironment environment, PropagationEngine propagationEngine) {
        super(v, propagationEngine);
        this.offset = sortedValues[0];
        lowerBound = environment.makeInt(sortedValues[0]);
        upperBound = environment.makeInt(sortedValues[sortedValues.length - 1]);
        int size = sortedValues.length;
        this.sortedValues = sortedValues;
        val2ind = new TIntIntHashMap();
        this.size = environment.makeInt(size);
        int[] prevIndices = new int[size];
        int[] nextIndices = new int[size];
        for (int i = 0; i < sortedValues.length; i++) {
            val2ind.put(sortedValues[i], i);
            nextIndices[i] = (i + 1) % sortedValues.length;
            prevIndices[i] = (i - 1 + sortedValues.length) % sortedValues.length;
        }
        nextIndex = environment.makeIntVector(nextIndices);
        prevIndex = environment.makeIntVector(prevIndices);

        deltaDom = new StackDeltaDomain();

    }

    /**
     * Function to find the value from the index.
     *
     * @param index the index of the value.
     * @return the designed value in the domain.
     */
    int indexToValue(int index) {
//    return index + offset;
        return sortedValues[index];
    }

    /**
     * Function to find the index of a given value in the domain. Warning! There is no check!
     *
     * @param value the value of the domain (It Should be checked before calling!!)
     * @return the index of this value.
     */
    int valueToIndex(int value) {
//    return value - offset;
        if (val2ind.containsKey(value)) {
            return val2ind.get(value);
        } else {
            return -1;
        }
    }

    /**
     * Inner function in order to maintain data structure when a value is removed.
     *
     * @param indexToRemove should be a valid index of value in the domain.
     */
    void removeIndex(int indexToRemove) {
        nextIndex.set(indexToRemove, -1);
        prevIndex.set(indexToRemove, -1);
        deltaDom.remove(indexToRemove + offset);
        size.add(-1);
    }

    /**
     * Returns the lower bound of the domain in O(1).
     *
     * @return the lower bound of the domain.
     */
    public int getInf() {
        return lowerBound.get();
    }

    /**
     * Returns the upper bound of the domain in O(1).
     *
     * @return the upper bound of the domain.
     */
    public int getSup() {
        return upperBound.get();
    }

    /**
     * Checks if the value x is in the current domain. It is done in O(1).
     *
     * @param x the value to check wetehr it is in the domain.
     *          It can be completely outside of the original domain in this implementation.
     * @return true if the value x is in the domain.
     */
    public boolean contains(int x) {
        int xIndex = valueToIndex(x);
        return !(xIndex < 0 || xIndex >= nextIndex.size()) && nextIndex.get(xIndex) != -1;
    }

    /**
     * Updates the lower bound of the domain to the next value contained in the domain
     * which is more or equal to x.
     * <p/>
     * This is done in O(n) with n the size of the domain.
     *
     * @param x a value the lower bound should be more than. x should be less than the upper bound!
     * @return the new lower bound of the domain.
     */
    public int updateInf(int x) {
        int xIndex = valueToIndex(x);
        int currentInf = valueToIndex(lowerBound.get());
        int sup = valueToIndex(upperBound.get());
        while (currentInf < xIndex && currentInf <= sup) {
            int next = nextIndex.get(currentInf);
            removeIndex(currentInf);
            currentInf = next;
        }
        prevIndex.set(currentInf, sup);
        nextIndex.set(sup, currentInf);
        lowerBound.set(indexToValue(currentInf));
        return indexToValue(currentInf);
    }

    /**
     * Updates the upper bound of the domain to the next value contained in the domain
     * which is less or equal to x.
     * <p/>
     * This is done in O(n) with n the size of the domain.
     *
     * @param x a value the upper bound should be less than. x should be more than lower bound!
     * @return the new upper bound of the domain.
     */
    public int updateSup(int x) {
        int xIndex = valueToIndex(x);
        int currentSup = valueToIndex(upperBound.get());
        int inf = valueToIndex(lowerBound.get());
        while (currentSup > xIndex) {
            int prev = prevIndex.get(currentSup);
            removeIndex(currentSup);
            currentSup = prev;
        }
        prevIndex.set(inf, currentSup);
        nextIndex.set(currentSup, inf);
        upperBound.set(indexToValue(currentSup));
        return indexToValue(currentSup);
    }

    /**
     * Restricts the domain to the given value.
     * <p/>
     * This is done in O(n) with n the size of the domain.
     *
     * @param x the value to which this domain should be restricted to.
     */
    public void restrict(int x) {
        int xIndex = valueToIndex(x);
        int currentInf = valueToIndex(lowerBound.get());
        int currentSup = valueToIndex(upperBound.get());
        while (currentInf < xIndex) {
            int next = nextIndex.get(currentInf);
            removeIndex(currentInf);
            currentInf = next;
        }
        while (currentSup > xIndex) {
            int prev = prevIndex.get(currentSup);
            removeIndex(currentSup);
            currentSup = prev;
        }
        prevIndex.set(xIndex, xIndex);
        nextIndex.set(xIndex, xIndex);
        lowerBound.set(x);
        upperBound.set(x);
    }

    /**
     * Removes a precise value from the domain.
     * <p/>
     * This is done in O(1).
     *
     * @param x the value in the domain.
     * @return true if a value was actually removed.
     */
    public boolean remove(int x) {
        if (!contains(x)) return false;
        int xIndex = valueToIndex(x);
        int next = nextIndex.get(xIndex);
        int prev = prevIndex.get(xIndex);
        if (x == lowerBound.get()) lowerBound.set(indexToValue(next));
        if (x == upperBound.get()) upperBound.set(indexToValue(prev));
        removeIndex(xIndex);
        prevIndex.set(next, prev);
        nextIndex.set(prev, next);
        return true;
    }

    /**
     * Retuens the dynamic size of the domain, that is the number of possible values in the domain when
     * the method is called.
     *
     * @return the size of the domain.
     */
    public int getSize() {
        return size.get();
    }

    public DisposableIntIterator getIterator() {
        if (_iterator == null) {
            _iterator = new IntDomainIterator();
        }else if (!_iterator.reusable()) {
            //assert false;
            _iterator = new IntDomainIterator();
        }
        _iterator.init(this);
        return _iterator;
    }

    /**
     * Looks for the value after x in the domain. It is safe with value out of the domain.
     * <p/>
     * It is done in O(1) if x is a value of the domain, O(n) else.
     *
     * @param x A value (in or out of the domain).
     * @return The value in the domain after x, Integer.MAX_VALUE if none.
     */
    public int getNextValue(int x) {
        int inf = lowerBound.get();
        if (x < inf) return inf;
        if (!hasNextValue(x)) return Integer.MAX_VALUE;
        int xIndex = valueToIndex(x);
        if (nextIndex.get(xIndex) != -1) {
            return indexToValue(nextIndex.get(xIndex));
        }
        xIndex++;
        while (nextIndex.get(xIndex) == -1) xIndex++;
        return indexToValue(xIndex);
    }

    /**
     * Looks for the value before x in the domain. It is safe with value out of the domain.
     * <p/>
     * It is done in O(1) if x is a value of the domain, O(n) else.
     *
     * @param x A value (in or out of the domain).
     * @return The value in the domain before x, Integer.MIN_VALUE if none.
     */
    public int getPrevValue(int x) {
        int sup = upperBound.get();
        if (x > sup) return sup;
        if (!hasPrevValue(x)) return Integer.MIN_VALUE;
        int xIndex = valueToIndex(x);
        if (prevIndex.get(xIndex) != -1) {
            return indexToValue(prevIndex.get(xIndex));
        }
        xIndex--;
        while (prevIndex.get(xIndex) == -1) xIndex--;
        return indexToValue(xIndex);
    }

    /**
     * Checks if there is a value after x in the domain. Basically checks that x if less than
     * the upper bound (in O(1)).
     *
     * @param x value in or out of the domain.
     * @return true if there is value in the domain greater than x
     */
    public boolean hasNextValue(int x) {
        return x < upperBound.get();
    }

    /**
     * Checks if there is a value before x in the domain. Basically checks that x if more than
     * the lower bound (in O(1)).
     *
     * @param x value in or out of the domain.
     * @return true if there is value in the domain smaller than x
     */
    public boolean hasPrevValue(int x) {
        return x > lowerBound.get();
    }

    /**
     * Returns a value randomly choosed in the domain.
     * <p/>
     * It is done in O(n).
     *
     * @return a random value from the domain.
     */
    public int getRandomValue() {
        int size = getSize();
        if (size == 1) return this.getInf();
        else {
            int rand = random.nextInt(size);
            int val = this.getInf();
            for (int o = 0; o < rand; o++) {
                val = getNextValue(val);
            }
            return val;
        }
    }

    /**
     * Interface method to know if this domain is enumerated. Always true here.
     *
     * @return true
     */
    public boolean isEnumerated() {
        return true;
    }

    public String toString() {
        return "{" + getInf() + "..." + getSup() + '}';
    }

    public String pretty() {
        StringBuilder buf = new StringBuilder("{");
        int maxDisplay = 15;
        int count = 0;
        DisposableIntIterator it = this.getIterator();
        for (; (it.hasNext() && count < maxDisplay); ) {
            int val = it.next();
            count++;
            if (count > 1) buf.append(", ");
            buf.append(val);
        }
        it.dispose();
        if (this.getSize() > maxDisplay) {
            buf.append("..., ");
            buf.append(this.getSup());
        }
        buf.append('}');
        return buf.toString();
    }

}

