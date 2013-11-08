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

import choco.cp.common.util.iterators.BipartiteIntDomainIterator;
import choco.cp.solver.variables.delta.BipartiteDeltaDomain;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.PropagationEngine;

import java.util.Arrays;
import java.util.Random;

/**
 * @author Guillaume Rochart
 *         This class is an integer domain implementation based on a bipartite set : values in the domain, and values outside.
 *         For instance :
 *         <p/>
 *         World 1 :      1234
 *         World 2 :      124 3
 *         World 3 :      14 23
 *         World 2 :      142 3
 *         World 3 :      42 13
 *         World 2 :      421 3
 *         World 1 :      4213
 *         <p/>
 *         On this implementation, we only need an integer list and a stored integer to know how many values are in the domain,
 *         and a list pointing all value indices in order to be able to remove values easily (without iterating the domain).
 */
public class BipartiteIntDomain extends AbstractIntDomain {

    /**
     * A random generator for random value from the domain
     */

    private final static Random random = new Random();


    /**
     * The values (not ordered) contained in the domain.
     */
    private int[] values;

    /**
     * The number of values currently in the domain.
     */
    private IStateInt valuesInDomainNumber;

    /**
     * lower bound of the domain.
     */
    private IStateInt inf;

    /**
     * lower bound of the domain.
     */
    private IStateInt sup;

    /**
     * The indices for each values (+ the minimalValue).
     */
    private int[] indices;

    /**
     * The minimum from all values.
     */
    private int offset;

    protected BipartiteIntDomainIterator _iterator = null;

    /**
     * Constructs a new domain for the specified variable and bounds.
     *
     * @param v                 The involved variable.
     * @param sortedValues      arry of sorted values.
     * @param environment
     * @param propagationEngine
     */
    public BipartiteIntDomain(final IntDomainVarImpl v, final int[] sortedValues, final IEnvironment environment, final PropagationEngine propagationEngine) {
        super(v, propagationEngine);
        assert (v != null);
        assert (sortedValues != null);
        init(sortedValues, environment);
    }

    /**
     * Constructs a new domain for the specified variable and bounds.
     *
     * @param v                 The involved variable.
     * @param low               Minimal value.
     * @param up                Maximal value.
     * @param environment
     * @param propagationEngine
     */
    public BipartiteIntDomain(final IntDomainVarImpl v, final int low, final int up, final IEnvironment environment, final PropagationEngine propagationEngine) {
        super(v, propagationEngine);
        // Pre-condition
        assert (v != null);
        assert (low <= up);
        final int[] sortedValues = new int[up - low + 1];
        for (int i = 0; i < sortedValues.length; i++) {
            sortedValues[i] = low + i;
        }
        init(sortedValues, environment);
    }

    void init(final int[] sortedValues, final IEnvironment environment) {
        // Constructor
        final int low = sortedValues[0];
        final int up = sortedValues[sortedValues.length - 1];
        final int size = sortedValues.length;
        // Initial lower bound is recorded as an offset 
        offset = low;
        // Initialisation of values
        values = new int[size];
        System.arraycopy(sortedValues, 0, values, 0, sortedValues.length);
        // By default all values are in...
        valuesInDomainNumber = environment.makeInt(size - 1);
        // Indices are first simply offset...
        indices = new int[up - low + 1];
        Arrays.fill(indices, Integer.MAX_VALUE);
        for (int i = 0; i < values.length; i++) {
            indices[values[i] - offset] = i;
        }
        // At the beginning nothing has been propageted !
        deltaDom = new BipartiteDeltaDomain(size, values, valuesInDomainNumber);
        inf = environment.makeInt(low);
        sup = environment.makeInt(up);
    }

    /**
     * @return the lower bound
     */
    public int getInf() {
        return inf.get();
    }

    /**
     * @return the upper bound
     */
    public int getSup() {
        return sup.get();
    }

    /**
     * This method is volontarely inefficient! It allows to be efficient for backtracking and so on.
     *
     * @return true if the value is still in the domain
     */
    public boolean contains(final int x) {
        return indices[x - offset] <= valuesInDomainNumber.get();
    }

    /**
     * @return the number of values in the domain
     */
    public int getSize() {
        return valuesInDomainNumber.get() + 1;
    }

    /**
     * Be careful, there is no order in the domain values in this implementation !
     *
     * @param x the previous value in the list
     * @return the next value after x
     */
    public int getNextValue(final int x) {
        if (x >= getSup()) return Integer.MAX_VALUE;
        int nextval = Integer.MAX_VALUE;//x + 1;
        for (int i = valuesInDomainNumber.get(); i >= 0; i--) {
            if (values[i] > x && values[i] < nextval)
                nextval = values[i];
        }
        return nextval;
    }

    /**
     * Be careful, there is no order in the domain values in this implementation !
     *
     * @param x a value in the list
     * @return the previous value before x
     */
    public int getPrevValue(final int x) {
        if (x <= getInf()) return Integer.MAX_VALUE;
        int prevval = Integer.MIN_VALUE;
        for (int i = valuesInDomainNumber.get(); i >= 0; i--) {
            if (values[i] < x && values[i] > prevval)
                prevval = values[i];
        }
        return prevval;
    }

    //warning : this is not the usual semantic of hasNextValue...
    public boolean hasNextValue(final int x) {
        return x < getSup();
    }

    //warning : this is not the usual semantic of hasPrevValue...
    public boolean hasPrevValue(final int x) {
        return x > getInf();
    }

    public boolean removeInterval(final int a, final int b, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
        if (a <= getInf())
            return updateInf(b + 1, cause, forceAwake);
        else if (getSup() <= b)
            return updateSup(a - 1, cause, forceAwake);
        else {
            boolean anyChange = false;
            for (int i = valuesInDomainNumber.get(); i >= 0; i--) {
                final int v = values[i];
                if (v >= a && v <= b)
                    anyChange |= removeVal(v, cause, forceAwake);
            }
            return anyChange;
        }
    }


    /**
     * Removing a value from the domain of a variable. Returns true if this
     * was a real modification on the domain.
     *
     * @param x     the value to remove
     * @param cause
     * @return wether the removal has been done
     * @throws ContradictionException contradiction excpetion
     */
    protected boolean _removeVal(final int x, final SConstraint cause) throws ContradictionException {
        final int infv = getInf();
        final int supv = getSup();
        if (infv <= x && x <= supv) {
            final boolean b = remove(x);
            if (x == infv) {
                final int possibleninf = x + 1;
                if (possibleninf > supv) {
                    propagationEngine.raiseContradiction(cause);
                }
                int min = Integer.MAX_VALUE;
                for (int i = valuesInDomainNumber.get(); i >= 0; i--) {
                    if (values[i] < min) {
                        min = values[i];
                        if (min == possibleninf) break;
                    }
                }
                inf.set(min);
                return b;
            } else if (x == supv) {
                final int possiblesup = x - 1;
                if (possiblesup < infv) {
                    propagationEngine.raiseContradiction(cause);
                }
                int max = Integer.MIN_VALUE;
                for (int i = valuesInDomainNumber.get(); i >= 0; i--) {
                    if (values[i] > max) {
                        max = values[i];
                        if (max == possiblesup) break;
                    }
                }
                sup.set(max);
                return b;
            } else {
                return b;//remove(x);
            }
        } else {
            return false;
        }
    }

    public boolean remove(final int x) {
        final int i = x - offset;
        final int mark = valuesInDomainNumber.get();
        if (indices[i] <= mark) {
            if (indices[i] < mark) {
                final int tempval = values[mark];
                values[mark] = x;
                values[indices[i]] = tempval;
                indices[tempval - offset] = indices[i];
                indices[i] = mark;
            }
            valuesInDomainNumber.add(-1);
            deltaDom.remove(mark);
            return true;
        }
        return false;
    }


    public void restrict(final int x) {
        final int i = x - offset;
        final int tempval = values[0];
        values[0] = x;
        values[indices[i]] = tempval;
        indices[tempval - offset] = indices[i];
        indices[i] = 0;
        inf.set(x);
        sup.set(x);
        deltaDom.remove(valuesInDomainNumber.get());
        valuesInDomainNumber.set(0);
    }


    public int updateInf(final int x) {
        int min = Integer.MAX_VALUE;
        for (int i = valuesInDomainNumber.get(); i >= 0; i--) {
            if (values[i] < x) {
                remove(values[i]);
            } else if (values[i] < min) {
                min = values[i];
            }
        }
        inf.set(min);
        return min;
    }

    public int updateSup(final int x) {
        int max = Integer.MIN_VALUE;
        for (int i = valuesInDomainNumber.get(); i >= 0; i--) {
            if (values[i] > x) {
                remove(values[i]);
            } else if (values[i] > max) {
                max = values[i];
            }
        }
        sup.set(max);
        return max;
    }


    public int getRandomValue() {
        if (getSize() == 1) return values[0];
        else return values[random.nextInt(valuesInDomainNumber.get() + 1)];
    }

    public DisposableIntIterator getIterator() {
        if (_iterator == null) {
            _iterator = new BipartiteIntDomainIterator();
        } else if (!_iterator.reusable()) {
//            assert false;
            _iterator = new BipartiteIntDomainIterator();
        }
        _iterator.init(valuesInDomainNumber.get(), values);
        return _iterator;
    }


    public boolean isEnumerated() {
        return true;
    }

    public String pretty() {
        final StringBuilder buf = new StringBuilder("{");
        final int maxDisplay = 15;
        int count = 1;

        int current = this.getInf();
        buf.append(current);
        while (count < maxDisplay && this.hasNextValue(current)) {
            current = this.getNextValue(current);
            count++;
            if (count > 0) buf.append(", ");
            buf.append(current);
        }
        if (this.getSize() > maxDisplay) {
            buf.append(", ..., ");
            buf.append(this.getSup());
        }
        buf.append('}');
        return buf.toString();
    }
}
