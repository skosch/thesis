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

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.structure.Couple;
import choco.kernel.memory.structure.PartiallyStoredIntCstrList;
import choco.kernel.memory.structure.PartiallyStoredIntVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.propagation.event.VarEvent;
import choco.kernel.solver.propagation.listener.IntPropagator;
import choco.kernel.solver.variables.AbstractVar;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Implements search valued domain variables.
 */
public class IntDomainVarImpl<C extends AbstractSConstraint & IntPropagator> extends AbstractVar implements IntDomainVar {

    /**
     * The backtrackable domain of the variable.
     */

    protected AbstractIntDomain domain;

    /**
     * Default constructor
     *
     * @param solver master solver
     * @param name   name of the variable
     */
    @SuppressWarnings({"unchecked"})
    protected <C extends AbstractIntSConstraint & IntPropagator> IntDomainVarImpl(Solver solver, String name) {
        super(solver, name, new PartiallyStoredIntCstrList<C>(solver.getEnvironment(), IntVarEvent.EVENTS));
    }

    /**
     * Constructs a new variable for the specified model and with the
     * specified name and bounds.
     *
     * @param solver     The model of the variable.
     * @param name       Its name.
     * @param domainType the type of encoding for the domain (BOUNDS, BITSET, ...)
     * @param a          Its minimal value.
     * @param b          Its maximal value.
     */

    public IntDomainVarImpl(Solver solver, String name, int domainType, int a, int b) {
        this(solver, name);
        switch (domainType) {
            case IntDomainVar.BITSET:
                domain = new BitSetIntDomain(this, a, b, solver.getEnvironment(), propagationEngine);
                break;
            case IntDomainVar.BOUNDS:
                domain = new IntervalIntDomain(this, a, b, solver.getEnvironment(), propagationEngine);
                break;
            case IntDomainVar.LINKEDLIST:
                domain = new LinkedIntDomain(this, a, b, solver.getEnvironment(), propagationEngine);
                break;
            case IntDomainVar.BINARYTREE:
                domain = new IntervalBTreeDomain(this, a, b, solver.getEnvironment(), propagationEngine);
                break;
            case IntDomainVar.BIPARTITELIST:
                domain = new BipartiteIntDomain(this, a, b, solver.getEnvironment(), propagationEngine);
                break;
            case IntDomainVar.BOOLEAN:
                domain = new BooleanDomain(this, solver.getEnvironment(), propagationEngine);
                break;
            case IntDomainVar.ONE_VALUE:
                domain = new OneValueIntDomain(this, a, propagationEngine);
                break;
            default:
                domain = new IntervalIntDomain(this, a, b, solver.getEnvironment(), propagationEngine);
                break;
        }
        this.event = new IntVarEvent<C>(this);
    }

    public IntDomainVarImpl(Solver solver, String name, int domainType, int[] distinctSortedValues) {
        this(solver, name);
        switch (domainType) {
            case IntDomainVar.BITSET:
                domain = new BitSetIntDomain(this, distinctSortedValues, solver.getEnvironment(), propagationEngine);
                break;
            case IntDomainVar.BINARYTREE:
                domain = new IntervalBTreeDomain(this, distinctSortedValues, solver.getEnvironment(), propagationEngine);
                break;
            case IntDomainVar.LINKEDLIST:
                domain = new LinkedIntDomain(this, distinctSortedValues, solver.getEnvironment(), propagationEngine);
                break;
            case IntDomainVar.BIPARTITELIST:
                domain = new BipartiteIntDomain(this, distinctSortedValues, solver.getEnvironment(), propagationEngine);
                break;
            case IntDomainVar.ONE_VALUE:
                domain = new OneValueIntDomain(this, distinctSortedValues[0], propagationEngine);
                break;
            default:
                domain = new BitSetIntDomain(this, distinctSortedValues, solver.getEnvironment(), propagationEngine);
                break;
        }
        this.event = new IntVarEvent<C>(this);
    }

    public final DisposableIterator<Couple<C>> getActiveConstraints(int evtType, C cstrCause) {
        //noinspection unchecked
        return ((PartiallyStoredIntCstrList) constraints).getActiveConstraint(evtType, cstrCause);
    }

    public final PartiallyStoredIntVector[] getEventsVector() {
        return ((PartiallyStoredIntCstrList) constraints).getEventsVector();
    }

    // ============================================
    // Methods of the interface
    // ============================================

    /**
     * Checks if the variable is instantiated to a specific value.
     */

    public boolean isInstantiatedTo(int x) {
        return isInstantiated() && (getVal() == x);
    }


    /**
     * Checks if the variables is instantiated to any value.
     */

    public boolean isInstantiated() {
        return domain.getSize() == 1;//(value.isKnown());
    }


    /**
     * Checks if a value is still in the domain.
     */

    public boolean canBeInstantiatedTo(int x) {
        //return domain.contains(x);
        return (getInf() <= x && x <= getSup() && (domain == null || domain.contains(x)));
    }

    /**
     * Checks if a value is still in the domain assuming the value is
     * in the initial bound of the domain
     */
    public boolean fastCanBeInstantiatedTo(int x) {
        return domain.contains(x);
    }

    /**
     * Sets the minimum value.
     */

    public void setInf(int x) throws ContradictionException {
        updateInf(x, null, true);
    }

    /**
     * @deprecated replaced by setInf
     */
    public void setMin(int x) throws ContradictionException {
        updateInf(x, null, true);
    }

    /**
     * Sets the maximal value.
     */

    public void setSup(int x) throws ContradictionException {
        updateSup(x, null, true);
    }

    /**
     * @deprecated replaced by setSup
     */
    public void setMax(int x) throws ContradictionException {
        updateSup(x, null, true);
    }

    /**
     * Instantiates the variable.
     *
     * @throws choco.kernel.solver.ContradictionException
     *
     */

    public void setVal(int x) throws ContradictionException {
        instantiate(x, null, true);
    }


    /**
     * Removes a value.
     *
     * @throws choco.kernel.solver.ContradictionException
     *
     */

    public void remVal(int x) throws ContradictionException {
        removeVal(x, null, true);
    }

    public void wipeOut() throws ContradictionException {
        propagationEngine.raiseContradiction(this);
    }

    public boolean hasEnumeratedDomain() {
        return domain.isEnumerated();
    }

    public boolean hasBooleanDomain() {
        return domain.isBoolean();
    }

    public IntDomain getDomain() {
        return domain;
    }

    /**
     * Gets the domain size.
     */

    public int getDomainSize() {
        return domain.getSize();
    }

    /**
     * Checks if it can be equals to another variable.
     */

    public boolean canBeEqualTo(IntDomainVar x) {
        if (x.getInf() <= this.getSup()) {
            if (this.getInf() <= x.getSup()) {
                if (!this.hasEnumeratedDomain() || !x.hasEnumeratedDomain())
                    return true;
                else {
                    DisposableIntIterator it = this.getDomain().getIterator();
                    for (; it.hasNext();) {
                        int v = it.next();
                        if (x.canBeInstantiatedTo(v)){
                            it.dispose();
                            return true;
                        }
                    }
                    it.dispose();
                    return false;
                }
            } else
                return false;
        } else
            return false;
    }


    /**
     * Checks if the variables can be instantiated to at least one value
     * in the array.
     *
     * @param sortedValList The value array.
     * @param nVals         The number of interesting value in this array.
     */

    public boolean canBeInstantiatedIn(int[] sortedValList, int nVals) {
        if (getInf() <= sortedValList[nVals - 1]) {
            if (getSup() >= sortedValList[0]) {
                if (domain == null)
                    return true;
                else {
                    for (int i = 0; i < nVals; i++) {
                        if (canBeInstantiatedTo(sortedValList[i]))
                            return true;
                    }
                    return false;
                }
            } else
                return false;
        } else
            return false;
    }


    /**
     * Returns a randomly choosed value in the domain.
     * <p/>
     * Not implemented yet.
     */

    public int getRandomDomainValue() {
        if (domain == null)
            return getInf();
            // TODO     return inf.get() + random(sup.get() - inf.get() + 1);
        else
            return domain.getRandomValue();
    }


    /**
     * Gets the next value in the domain.
     */

    public int getNextDomainValue(int currentv) {
        if (currentv < getInf())
            return getInf();
        else if (domain == null)
            return currentv + 1;
        else
            return domain.getNextValue(currentv);
    }

    @Override
    public int fastNextDomainValue(int i) {
        return domain.fastNextValue(i);
    }

    /**
     * Gets the previous value in the domain.
     */

    public int getPrevDomainValue(int currentv) {
        if (currentv > getSup())
            return getSup();
        else if (domain == null)
            return currentv - 1;
        else
            return domain.getPrevValue(currentv);
    }

    @Override
    public int fastPrevDomainValue(int i) {
        return domain.fastPrevValue(i);
    }

    @Deprecated
    private SConstraint getCause(int idx) {
        if (idx < -1) {
//            this.getConstraintVector().get(VarEvent.domOverWDegInitialIdx(idx)));
            return this.getConstraint(VarEvent.domOverWDegInitialIdx(idx));
        } else if (idx > -1) {
            return this.getConstraint(idx);
        }
        return null;
    }

    /**
     * Internal var: update on the variable lower bound caused by its i-th
     * constraint.
     * Returns a boolean indicating whether the call indeed added new information
     *
     * @param x          The new lower bound.
     * @param cause
     * @param forceAwake
     */

    public boolean updateInf(int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
        return domain.updateInf(x, cause, forceAwake);
    }

    @Override
    @Deprecated
    public boolean updateInf(final int x, final int idx) throws ContradictionException {
        return domain.updateInf(x, getCause(idx), (idx < 0));
    }

    /**
     * Internal var: update on the variable upper bound caused by its i-th
     * constraint.
     * Returns a boolean indicating whether the call indeed added new information.
     *
     * @param x          The new upper bound
     * @param cause
     * @param forceAwake
     */

    public boolean updateSup(int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
        return domain.updateSup(x, cause, forceAwake);
    }

    @Override
    @Deprecated
    public boolean updateSup(final int x, final int idx) throws ContradictionException {
        return domain.updateSup(x, getCause(idx), (idx < 0));
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
     */

    public boolean removeVal(int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
        return domain.removeVal(x, cause, forceAwake);
    }

    @Override
    @Deprecated
    public boolean removeVal(final int x, final int idx) throws ContradictionException {
        return domain.removeVal(x, getCause(idx), (idx < 0));
    }

    /**
     * Internal var: remove an interval (a sequence of consecutive values) from
     * the domain of a variable caused by its i-th constraint.
     * Returns a boolean indicating whether the call indeed added new information.
     *
     * @param a          the first removed value
     * @param b          the last removed value
     * @param cause
     * @param forceAwake
     */

    public boolean removeInterval(int a, int b, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
        return domain.removeInterval(a, b, cause, forceAwake);
    }

    @Override
    @Deprecated
    public boolean removeInterval(final int a, final int b, final int idx) throws ContradictionException {
        return domain.removeInterval(a, b, getCause(idx), (idx >= 0));
    }

    /**
     * Internal var: instantiation of the variable caused by its i-th constraint
     * Returns a boolean indicating whether the call indeed added new information.
     *
     * @param x          the new upper bound
     * @param cause
     * @param forceAwake
     */

    public boolean instantiate(int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
        return domain.instantiate(x, cause, forceAwake);
    }

    @Override
    @Deprecated
    public boolean instantiate(final int x, final int idx) throws ContradictionException {
        return domain.instantiate(x, getCause(idx), (idx < 0));
    }

    /**
     * Gets the minimal value of the variable.
     */

    public int getInf() {
        return domain.getInf();
    }


    /**
     * Gets the maximal value of the variable.
     */

    public int getSup() {
        return domain.getSup();
    }


    /**
     * Gets the value of the variable if instantiated.
     */

    public int getVal() {
        return domain.getInf();//value.get();
    }

    /**
     * @deprecated replaced by getVal
     */
    public int getValue() {
        return domain.getInf();//value.get();
    }

    /**
     * pretty printing
     *
     * @return a String representation of the variable
     */
    @Override
    public String toString() {
        return (super.toString() + ':' + (isInstantiated() ? getVal() : "?"));
    }

    /**
	 * pretty printing
	 *
	 * @return a String representation of the variable
	 */
	public String pretty() {
		return (this.toString() + '[' + this.domain.getSize() + ']' + this.domain.pretty());// +" ~ "+ Arrays.toString(this.extensions);
	}
}
