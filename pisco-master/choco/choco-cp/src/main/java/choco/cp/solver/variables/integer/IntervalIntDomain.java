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

import choco.cp.common.util.iterators.IntervalIntDomainIterator;
import choco.cp.solver.variables.delta.IntervalDeltaDomain;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.PropagationEngine;

import java.util.Random;

public class IntervalIntDomain extends AbstractIntDomain {
    private static final int eventBitMask = IntVarEvent.BOUNDS_MASK + IntVarEvent.REMVAL_MASK;
    /**
     * A random generator for random value from the domain
     */

    private static Random random = new Random(System.currentTimeMillis());

    /**
     * The backtrackable minimal value of the variable.
     */

    private final IStateInt inf;

    /**
     * The backtrackable maximal value of the variable.
     */

    private final IStateInt sup;

    protected IntervalIntDomainIterator _iterator = null;

    public IntervalIntDomain(final IntDomainVarImpl v, final int a, final int b, final IEnvironment environment, final PropagationEngine propagationEngine) {
        super(v, propagationEngine);
        inf = environment.makeInt(a);
        sup = environment.makeInt(b);
        deltaDom = new IntervalDeltaDomain(this, a, b);

    }

    public boolean contains(final int x) {
        return ((x >= getInf()) && (x <= getSup()));
    }

    public int getNextValue(final int x) {
        if (x < getInf()) {
            return getInf();
        } else if (x < getSup()) {
            return x + 1;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public final int fastNextValue(int x) {
        return x + 1;
    }

    public int getPrevValue(final int x) {
        if (x > getSup()) {
            return getSup();
        } else if (x > getInf()) {
            return x - 1;
        } else {
            return Integer.MIN_VALUE;
        }
    }

    public int getRandomValue() {
        final int inf = getInf();
        final int rand = random.nextInt(this.getSize());
        return inf + rand;
    }

    public final int getSize() {
        return getSup() - getInf() + 1;
    }

    public boolean hasNextValue(final int x) {
        return (x < getSup());
    }

    public boolean hasPrevValue(final int x) {
        return (x > getInf());
    }

    public DisposableIntIterator getIterator() {
        if (_iterator == null) {
            _iterator = new IntervalIntDomainIterator();
        }else
        if (!_iterator.reusable()) {
            //assert false;
            _iterator = new IntervalIntDomainIterator();
        }
        _iterator.init(this);
        return _iterator;
    }

    public boolean remove(final int x) {
        return false;
    }

    public final int getSup() {
        return sup.get();
    }

    public final int getInf() {
        return inf.get();
    }

    public void restrict(final int x) {
        if ((variable.getEvent().getPropagatedEvents() & eventBitMask) != 0) {
            deltaDom.remove(x);
        }
        inf.set(x);
        sup.set(x);
    }

    public int updateInf(final int x) {
        if ((variable.getEvent().getPropagatedEvents() & eventBitMask) != 0) {
            deltaDom.remove(x);
        }
        inf.set(x);
        return x;
    }

    public int updateSup(final int x) {
        if ((variable.getEvent().getPropagatedEvents() & eventBitMask) != 0) {
            deltaDom.remove(x);
        }
        sup.set(x);
        return x;
    }

    protected boolean _removeVal(final int x, final SConstraint cause) throws ContradictionException {
        final int infv = getInf();
        final int supv = getSup();
        if (x == infv) {
            _updateInf(x + 1, cause);
            if (getInf() == supv) _instantiate(supv, cause);
            return true;
        } else if (x == supv) {
            _updateSup(x - 1, cause);
            if (getSup() == infv) _instantiate(infv, cause);
            return true;
        } else {
            return false;
        }
    }

    public boolean isEnumerated() {
        return false;
    }

    public String pretty() {
        final StringBuilder ret = new StringBuilder(32);
        ret.append('[').append(this.getInf()).append(" .. ").append(this.getSup()).append(']');
//        ret.append(deltaDom.pretty());
        return ret.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////// DELTA DOMAIN /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void freezeDeltaDomain() {
        if ((variable.getEvent().getPropagatedEvents() & eventBitMask) != 0) {
            deltaDom.freeze();
        }
    }

    /**
     * release the delta domain
     *
     * @return wether it was a new update
     */
    @Override
    public boolean releaseDeltaDomain() {
        return (variable.getEvent().getPropagatedEvents() & eventBitMask) == 0 || deltaDom.release();
    }

    /**
     * checks whether the delta domain has indeed been released (ie: chechks that no domain updates are pending)
     */
    @Override
    public boolean getReleasedDeltaDomain() {
        return (variable.getEvent().getPropagatedEvents() & eventBitMask) == 0 || deltaDom.isReleased();
    }
}
