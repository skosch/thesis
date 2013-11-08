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

import choco.cp.common.util.iterators.BooleanDomainIterator;
import choco.cp.solver.variables.delta.BooleanDeltaDomain;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.structure.StoredIndexedBipartiteSet;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.PropagationEngine;

import java.util.Random;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 18 déc. 2008
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public final class BooleanDomain extends AbstractIntDomain {

    /**
     * A random generator for random value from the domain
     */

    private static final Random random = new Random(System.currentTimeMillis());

    /**
     * The offset, that is the minimal value of the domain (stored at index 0).
     * Thus the entry at index i corresponds to x=i+offset).
     */

    private final int offset;


    /**
     * indicate the value of the domain : false = 0, true = 1
     */
    private int value;

    /**
     * A bi partite set indicating for each value whether it is present or not.
     * If the set contains the domain, the variable is not instanciated.
     */

    private final StoredIndexedBipartiteSet notInstanciated;

    protected BooleanDomainIterator _iterator = null;

    /**
     * Constructs a new domain for the specified variable and bounds.
     *
     * @param v                 The involved variable.
     * @param environment
     * @param propagationEngine
     */

    public BooleanDomain(final IntDomainVarImpl v, final IEnvironment environment, final PropagationEngine propagationEngine) {
        super(v, propagationEngine);
        notInstanciated = (StoredIndexedBipartiteSet) environment.getSharedBipartiteSetForBooleanVars();
        this.offset = environment.getNextOffset();
        value = 0;
        deltaDom = new BooleanDeltaDomain();
    }


    /**
     * This method is not relevant if the variable is not instantiated.
     * For performance issue, this test is not
     *
     * @return the value IF the variable is instantiated
     */
    public final int getValueIfInst() {
        return value;
    }


    /**
     * @return true if the boolean is instantiated
     */
    public final boolean isInstantiated() {
        return !notInstanciated.contain(offset);
    }

    /**
     * Returns the minimal present value.
     */
    public final int getInf() {
        if (!notInstanciated.contain(offset)) {
            return value;
        }
        return 0;
    }


    /**
     * Returns the maximal present value.
     */
    public final int getSup() {
        if (!notInstanciated.contain(offset)) {
            return value;
        }
        return 1;
    }


    /**
     * Sets a new minimal value.
     *
     * @param x New bound value.
     */

    public int updateInf(final int x) {
        throw new SolverException("Unexpected call of updateInf");
    }

    /**
     * Sets a new maximal value.
     *
     * @param x New bound value.
     */

    public int updateSup(final int x) {
        throw new SolverException("Unexpected call of updateSup");
    }

    /**
     * Checks if the value is present.
     *
     * @param x The value to check.
     */

    public final boolean contains(final int x) {
        if (!notInstanciated.contain(offset)) {
            return value == x;
        }
        return x == 0 || x == 1;
    }

    /**
     * Removes a value.
     */
    public boolean remove(final int x) {
        throw new SolverException("Unexpected call of remove");
    }

    /**
     * Removes all the value but the specified one.
     */

    public final void restrict(final int x) {
        notInstanciated.remove(offset);
        deltaDom.remove(1 - x);
        value = x;
    }

    /**
     * Returns the current size of the domain.
     */

    public final int getSize() {
        return (notInstanciated.contain(offset) ? 2 : 1);
    }

    public DisposableIntIterator getIterator() {
        if (_iterator == null) {
            _iterator = new BooleanDomainIterator();
        } else if (!_iterator.reusable()) {
            //assert false;
            _iterator = new BooleanDomainIterator();
        }
        _iterator.init(this);
        return _iterator;
    }

    /**
     * Returns the value following <code>x</code>
     */

    public final int getNextValue(final int x) {
        if (!notInstanciated.contain(offset)) {
            final int val = value;
            return (val > x) ? val : Integer.MAX_VALUE;
        } else {
            if (x < 0) return 0;
            if (x == 0) return 1;
            return Integer.MAX_VALUE;
        }
    }


    /**
     * Returns the value preceding <code>x</code>
     */

    public final int getPrevValue(final int x) {
        if (x > getSup()) return getSup();
        if (x > getInf()) return getInf();
        return Integer.MIN_VALUE;
    }


    /**
     * Checks if the value has a following value.
     */

    public final boolean hasNextValue(final int x) {
        return (x < getSup());
    }


    /**
     * Checks if the value has a preceding value.
     */

    public final boolean hasPrevValue(final int x) {
        return (x > getInf());
    }


    /**
     * Returns a value randomly choosed in the domain.
     */

    public final int getRandomValue() {
        if (!notInstanciated.contain(offset)) {
            return value;
        } else {
            return random.nextInt(2);
        }
    }

    public boolean isEnumerated() {
        return true;
    }

    public boolean isBoolean() {
        return true;
    }

    public String toString() {
        return "[" + getInf() + ',' + getSup() + ']';
    }

    public String pretty() {
        return toString();
    }


    /**
     * Internal var: update on the variable upper bound caused by its i-th
     * constraint.
     * Returns a boolean indicating whether the call indeed added new information.
     *
     * @param x          The new upper bound
     * @param cause
     * @param forceAwake
     * @return a boolean indicating whether the call indeed added new information.
     * @throws ContradictionException contradiction exception
     */
    public boolean updateSup(final int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
        if (_updateSup(x, cause)) {
            propagationEngine.postInstInt(variable, cause, forceAwake);

            return true;
        } else
            return false;
    }

    /**
     * Internal var: update on the variable lower bound caused by its i-th
     * constraint.
     * Returns a boolean indicating whether the call indeed added new information
     *
     * @param x          The new lower bound.
     * @param cause
     * @param forceAwake
     * @return a boolean indicating whether the call indeed added new information
     * @throws ContradictionException contradiction exception
     */

    public boolean updateInf(final int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
        if (_updateInf(x, cause)) {
            propagationEngine.postInstInt(variable, cause, forceAwake);
            return true;
        } else
            return false;
    }

    /**
     * Internal var: update (value removal) on the domain of a variable caused by
     * its i-th constraint.
     * <i>Note:</i> Whenever the hole results in a stronger var (such as a bound update or
     * an instantiation, then we forget about the index of the var generating constraint.
     * Indeed the propagated var is stronger than the initial one that
     * was generated; thus the generating constraint should be informed
     * about such a new var.
     * Returns a boolean indicating whether the call indeed added new information.
     *
     * @param x          The removed value
     * @param cause
     * @param forceAwake
     * @return a boolean indicating whether the call indeed added new information.
     * @throws ContradictionException contradiction exception
     */

    public final boolean removeVal(final int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
        if (_removeVal(x, cause)) {
            propagationEngine.postInstInt(variable, cause, forceAwake);
            return true;
        } else
            return false;
    }

    /**
     * Internal var: instantiation of the variable caused by its i-th constraint
     * Returns a boolean indicating whether the call indeed added new information.
     *
     * @param x          the new upper bound
     * @param cause
     * @param forceAwake
     * @return a boolean indicating whether the call indeed added new information.
     * @throws ContradictionException contradiction exception
     */

    public final boolean instantiate(final int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
        if (_instantiate(x, cause)) {
            propagationEngine.postInstInt(variable, cause, forceAwake);
            return true;
        } else
            return false;
    }

    private void failOnIndex(final SConstraint cause) throws ContradictionException {
        propagationEngine.raiseContradiction(cause);
    }


    /**
     * Instantiating a variable to an search value. Returns true if this was
     * a real modification or not
     *
     * @param x     the new instantiate value
     * @param cause
     * @return wether it is a real modification or not
     * @throws choco.kernel.solver.ContradictionException
     *          contradiction exception
     */
    @Override
    protected final boolean _instantiate(final int x, final SConstraint cause) throws ContradictionException {
        if (!notInstanciated.contain(offset)) {
            if (value != x) {
                failOnIndex(cause);
            }
            return false;
        } else {
            if (x == 0 || x == 1) {
                restrict(x);
                return true;
            } else {
                failOnIndex(cause);
                return false;
            }
        }
    }


    /**
     * Improving the lower bound.
     *
     * @param x     the new lower bound
     * @param cause
     * @return a boolean indicating wether the update has been done
     * @throws choco.kernel.solver.ContradictionException
     *          contradiction exception
     */
    @Override
    protected final boolean _updateInf(final int x, final SConstraint cause) throws ContradictionException {
        if (isInstantiated()) {
            if (value < x) {
                failOnIndex(cause);
            }
            return false;
        } else {
            if (x > 1) {
                failOnIndex(cause);
            } else if (x == 1) {
                restrict(1);
                //variable.value.set(1);
                return true;
            }
        }
        return false;
    }


    /**
     * Improving the upper bound.
     *
     * @param x     the new upper bound
     * @param cause
     * @return wether the update has been done
     * @throws choco.kernel.solver.ContradictionException
     *          contradiction exception
     */
    @Override
    protected final boolean _updateSup(final int x, final SConstraint cause) throws ContradictionException {
        if (isInstantiated()) {
            if (value > x) {
                failOnIndex(cause);
            }
            return false;
        } else {
            if (x < 0) {
                failOnIndex(cause);
            } else if (x == 0) {
                restrict(0);
                //variable.value.set(0);
                return true;
            }
        }
        return false;
    }


    /**
     * Removing a value from the domain of a variable. Returns true if this
     * was a real modification on the domain.
     *
     * @param x     the value to remove
     * @param cause
     * @return wether the removal has been done
     * @throws choco.kernel.solver.ContradictionException
     *          contradiction excpetion
     */
    @Override
    protected final boolean _removeVal(final int x, final SConstraint cause) throws ContradictionException {
        if (isInstantiated()) {
            if (value == x) {
                failOnIndex(cause);
            }
            return false;
        } else {
            if (x == 0) {
                restrict(1);
                //variable.value.set(1);
                return true;
            } else if (x == 1) {
                restrict(0);
                //variable.value.set(0);
                return true;
            }
        }
        return false;
    }

    public final int getOffset() {
        return offset;
    }
}
