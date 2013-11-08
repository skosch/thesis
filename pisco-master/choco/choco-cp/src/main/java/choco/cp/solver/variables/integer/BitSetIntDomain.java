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

import choco.cp.common.util.iterators.BitSetIntDomainIterator;
import choco.cp.solver.variables.delta.BitSetDeltaDomain;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.structure.OneWordSBitSet32;
import choco.kernel.memory.structure.OneWordSBitSet64;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.variables.integer.IBitSetIntDomain;

import java.util.Random;

public final class BitSetIntDomain extends AbstractIntDomain implements IBitSetIntDomain {
    /**
     * A random generator for random value from the domain
     */

    private final static Random random = new Random();

    /**
     * The offset, that is the minimal value of the domain (stored at index 0).
     * Thus the entry at index i corresponds to x=i+offset).
     */

    private final int offset;


    /**
     * Number of present values.
     */
    private final IStateInt size;

    /**
     * The backtrackable minimal value of the variable.
     */

    private final IStateInt inf;

    /**
     * The backtrackable maximal value of the variable.
     */

    private final IStateInt sup;


    /**
     * A bit set indicating for each value whether it is present or not
     */

    private final IStateBitSet contents;

    /**
     * the initial size of the domain (never increases)
     */
    private final int capacity;


    protected BitSetIntDomainIterator _iterator = null;

    /**
     * Constructs a new domain for the specified variable and bounds.
     *
     * @param v                 The involved variable.
     * @param a                 Minimal value.
     * @param b                 Maximal value.
     * @param environment
     * @param propagationEngine
     */

    public BitSetIntDomain(final IntDomainVarImpl v, final int a, final int b, final IEnvironment environment, final PropagationEngine propagationEngine) {
        super(v, propagationEngine);
        capacity = b - a + 1;           // number of entries
        this.offset = a;
        size = environment.makeInt(capacity);
        if (capacity < 32) {
            contents = new OneWordSBitSet32(environment, capacity);
        } else if (capacity < 64) {
            contents = new OneWordSBitSet64(environment, capacity);
        } else {
            contents = environment.makeBitSet(capacity);
        }
        contents.set(0, capacity);
        deltaDom = new BitSetDeltaDomain(capacity, offset);
        inf = environment.makeInt(a);
        sup = environment.makeInt(b);
    }

    public BitSetIntDomain(final IntDomainVarImpl v, final int[] sortedValues, final IEnvironment environment, final PropagationEngine propagationEngine) {
        super(v, propagationEngine);
        final int a = sortedValues[0];
        final int b = sortedValues[sortedValues.length - 1];
        capacity = b - a + 1;           // number of entries
        this.offset = a;
        size = environment.makeInt(sortedValues.length);
        if (capacity < 32) {
            contents = new OneWordSBitSet32(environment, capacity);
        } else if (capacity < 64) {
            contents = new OneWordSBitSet64(environment, capacity);
        } else {
            contents = environment.makeBitSet(capacity);
        }
        // TODO : could be improved...
        for (final int sortedValue : sortedValues) {
            contents.set(sortedValue - a);
        }
        deltaDom = new BitSetDeltaDomain(capacity, offset);
        inf = environment.makeInt(a);
        sup = environment.makeInt(b);
    }

    public IStateBitSet getContent() {
        return contents;
    }

    /**
     * Returns the minimal present value.
     */
    public int getInf() {
        return inf.get();
        //return contents.nextSetBit(0) + offset;
    }


    /**
     * Returns the maximal present value.
     */
    public int getSup() {
        return sup.get();
        //return contents.prevSetBit(capacity - 1) + offset;
    }

    /**
     * Sets a new minimal value.
     *
     * @param x New bound value.
     */

    public int updateInf(final int x) {
        final int newi = x - offset;  // index of the new lower bound
        for (int i = inf.get() - offset; i < newi; i = contents.nextSetBit(i + 1)) {
            assert (contents.get(i));
            //LOGGER.severe("Bug in BitSetIntDomain.updateInf ?");
            removeIndex(i);
        }
        inf.set(contents.nextSetBit(newi) + offset);
        return inf.get();
    }

    /**
     * Sets a new maximal value.
     *
     * @param x New bound value.
     */

    public int updateSup(final int x) {
        final int newi = x - offset;  // index of the new lower bound
        for (int i = sup.get() - offset; i > newi; i = contents.prevSetBit(i - 1)) {
            assert (contents.get(i));
            //LOGGER.severe("Bug in BitSetIntDomain.updateSup ?");
            removeIndex(i);
        }
        sup.set(contents.prevSetBit(newi) + offset);
        return sup.get();
    }

    /**
     * Checks if the value is present.
     *
     * @param x The value to check.
     */

    public boolean contains(final int x) {
        return (contents.get(x - offset));
    }

    /**
     * Removes a value.
     */
    public boolean remove(final int x) {
        final int i = x - offset;
        if (contents.get(i)) {
            removeIndex(i);
            return true;
        } else {
            return false;
        }
    }

    private void removeIndex(final int i) {
        //assert(i != firstIndexToBePropagated);
        //LOGGER.severe("Bug in BitSetIntDomain.removeIndex ?");
        contents.clear(i);
        deltaDom.remove(i + offset);
        assert (!contents.get(i));
        //LOGGER.severe("Bug in BitSetIntDomain.removeIndex ?");
        size.add(-1);
    }

    /**
     * Removes all the value but the specified one.
     */

    public void restrict(final int x) {
        final int xi = x - offset;
        for (int i = contents.nextSetBit(0); i >= 0; i = contents.nextSetBit(i + 1)) {
            if (i != xi) {
                deltaDom.remove(i + offset);
            }
        }
        contents.clear();
        contents.set(xi);
        size.set(1);
        sup.set(x);
        inf.set(x);
    }

    /**
     * Returns the current size of the domain.
     */

    public int getSize() {
        return size.get();
    }


    public DisposableIntIterator getIterator() {
        if (_iterator == null) {
            _iterator = new BitSetIntDomainIterator();
        }else if (!_iterator.reusable()) {
//            assert false;
            _iterator = new BitSetIntDomainIterator();
        }
        _iterator.init(offset, contents);
        return _iterator;
    }

    /**
     * Returns the value following <code>x</code>
     */

    public final int getNextValue(final int x) {
        final int i = x - offset;
        if (i < 0 || x < inf.get()) return getInf();
        final int bit = contents.nextSetBit(i + 1);
        if (bit < 0) return Integer.MAX_VALUE;
        else return bit + offset;
    }

    @Override
    public final int fastNextValue(int x) {
        int bit = contents.nextSetBit(x - offset + 1);
        if (bit < 0)
            return Integer.MAX_VALUE;
        return bit + offset;
    }

    /**
     * Returns the value preceding <code>x</code>
     */

    public int getPrevValue(final int x) {
        final int i = x - offset;
        if (x > sup.get()) return sup.get();
        return contents.prevSetBit(i - 1) + offset;
    }


    /**
     * Checks if the value has a following value.
     */

    public boolean hasNextValue(final int x) {
        //int i = x - offset;
        return x < sup.get();
        //return (contents.nextSetBit(i + 1) != -1);
    }


    /**
     * Checks if the value has a preceding value.
     */

    public boolean hasPrevValue(final int x) {
        //int i = x - offset;
        return x > inf.get();
        //return (contents.prevSetBit(i - 1) != -1);
    }

    /**
     * Returns a value randomly choosed in the domain.
     */

    public int getRandomValue() {
        final int size = getSize();
        if (size == 1) return this.getInf();
        else {
            final int rand = random.nextInt(size);
            int val = this.getInf() - offset;
            for (int o = 0; o < rand; o++) {
                val = contents.nextSetBit(val + 1);
            }
            return val + offset;
        }
    }

    public boolean isEnumerated() {
        return true;
    }

    protected DisposableIntIterator _cachedDeltaIntDomainIterator = null;

    public String toString() {
        return "{" + getInf() + "..." + getSup() + '}';
    }

    public String pretty() {
        final StringBuilder buf = new StringBuilder("{");
        final int maxDisplay = 15;
        int count = 0;
        final DisposableIntIterator it = this.getIterator();
        for (; (it.hasNext() && count < maxDisplay); ) {
            final int val = it.next();
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
