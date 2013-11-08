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

package choco.cp.solver.constraints.set;

import static choco.cp.solver.variables.set.SetVarEvent.*;
import static choco.cp.solver.variables.integer.IntVarEvent.*;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.set.AbstractLargeSetIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetDomain;
import choco.kernel.solver.variables.set.SetVar;

/**
 * An abstract class used for MaxOfASet and MinOfaSet constraints
 *
 * @author Arnaud Malapert</br>
 * @version 2.0.1</br>
 * @since 8 déc. 2008 version 2.0.1</br>
 */
public abstract class AbstractBoundOfASet extends AbstractLargeSetIntSConstraint {


	public static enum EmptySetPolicy { INF, NONE, SUP};

	/**
	 * Index of the set variable
	 */
	public static final int SET_INDEX = 0;

	/**
	 * Index of the maximum variable.
	 */
	public static final int BOUND_INDEX = 0;

	/**
	 * First index of the variables among which the maximum should be chosen.
	 */
	public static final int VARS_OFFSET = 1;

	private final EmptySetPolicy emptySetPolicy;

	private final IStateBool awakeOnFirstKer;

	private IStateInt indexOfMinimumVariable;

	public AbstractBoundOfASet(IEnvironment environment, IntDomainVar[] intvars, SetVar setvar, EmptySetPolicy emptySetPolicy) {
		super(intvars, new SetVar[]{setvar});
		this.emptySetPolicy = emptySetPolicy;
		awakeOnFirstKer = environment.makeBool(true);
		indexOfMinimumVariable = environment.makeInt(-1);
		if (setvar.getEnveloppeDomainSize() > 0 && (setvar.getEnveloppeInf() < 0 || setvar.getEnveloppeSup() > intvars.length - 2)) {
			throw new SolverException("The enveloppe of the set variable " + setvar.pretty() + " is greater than the array");
		}
	}


	@Override
	public int getFilteredEventMask(int idx) {
		return idx > 0 ? INSTINT_MASK + BOUNDS_MASK : INSTSET_MASK + ADDKER_MASK + REMENV_MASK;
	}

	protected final boolean isInKernel(int idx) {
		return svars[SET_INDEX].isInDomainKernel(idx);
	}

	protected final boolean isInEnveloppe(int idx) {
		return svars[SET_INDEX].isInDomainEnveloppe(idx);
	}

	protected final SetDomain getSetDomain() {
		return svars[SET_INDEX].getDomain();
	}

	protected final boolean isEmptySet() {
		return this.svars[SET_INDEX].getEnveloppeDomainSize() == 0;
	}

	protected final boolean isNotEmptySet() {
		return this.svars[SET_INDEX].getKernelDomainSize() > 0;
	}

	protected final boolean isSetInstantiated() {
		return svars[SET_INDEX].isInstantiated();
	}

	protected final boolean updateBoundInf(int val) throws ContradictionException {
		return ivars[BOUND_INDEX].updateInf(val, this, false);
	}

	protected final boolean updateBoundSup(int val) throws ContradictionException {
		return ivars[BOUND_INDEX].updateSup(val, this, false);
	}

	protected abstract boolean removeFromEnv(int idx) throws ContradictionException;

	protected final boolean removeGreaterFromEnv(int idx, int maxValue) throws ContradictionException {
		return ivars[VARS_OFFSET + idx].getInf() > maxValue && this.svars[SET_INDEX].remFromEnveloppe(idx, this, false);
	}

	protected final boolean removeLowerFromEnv(int idx, int minValue) throws ContradictionException {
		return ivars[VARS_OFFSET + idx].getSup() < minValue && this.svars[SET_INDEX].remFromEnveloppe(idx, this, false);
	}


	protected final boolean remFromEnveloppe() throws ContradictionException {
		final DisposableIntIterator iter= getSetDomain().getOpenDomainIterator();
		boolean update = false;
		while(iter.hasNext()) {
			update |= removeFromEnv(iter.next());
		}
		iter.dispose();
		return update;
	}

	@Override
	public final void awakeOnEnvRemovals(int idx, DisposableIntIterator deltaDomain)
			throws ContradictionException {
		assert (idx == SET_INDEX);
		if(deltaDomain.hasNext()) awakeOnRem(); //not instantiatded
	}


	@Override
	public final void awakeOnRem(int varIdx, int val) throws ContradictionException {
		assert varIdx == SET_INDEX;
		awakeOnRem();
	}

	protected abstract void awakeOnRem() throws ContradictionException;

	@Override
	public final void awakeOnkerAdditions(int idx, DisposableIntIterator deltaDomain)
			throws ContradictionException {
		assert(idx == SET_INDEX);
		if(deltaDomain.hasNext()) {//not instantiated
			if(awakeOnFirstKer.get()) {
				propagate();
				awakeOnFirstKer.set(false);
			}
			else awakeOnKer(); 
		}
	}

	@Override
	public final void awakeOnKer(int varIdx, int x) throws ContradictionException {
		assert varIdx == SET_INDEX;
		if(awakeOnFirstKer.get()) {
			propagate();
			awakeOnFirstKer.set(false);
		}
		else awakeOnKer(); 
	}

	protected abstract void awakeOnKer() throws ContradictionException;


	@Override
	public boolean isConsistent() {
		return false;
	}

	//	private final void filterEmptySet() throws ContradictionException {
	//		
	//	}

	@Override
	public final void propagate() throws ContradictionException {
		if (isEmptySet()) {
			switch (emptySetPolicy) {
			case INF: ivars[BOUND_INDEX].instantiate(ivars[BOUND_INDEX].getInf(), this, false);break;
			case SUP: ivars[BOUND_INDEX].instantiate(ivars[BOUND_INDEX].getSup(), this, false);break;
			default:
				break;
			}
			setEntailed();
		}
		else filter();
	}

	protected abstract void filter() throws ContradictionException;


	/**
	 * Propagation when a variable is instantiated.
	 *
	 * @param idx the index of the modified variable.
	 * @throws choco.kernel.solver.ContradictionException
	 *          if a domain becomes empty.
	 */
	@Override
	public final void awakeOnInst(final int idx) throws ContradictionException {
		//CPSolver.flushLogs();
		if (idx >= 2 * VARS_OFFSET) { //of the list
			final int i = idx - 2 * VARS_OFFSET;
			if (isInEnveloppe(i)) { //of the set
				propagate();
			}
		} else propagate();
	}

	protected abstract int updateIndexOfCandidateVariable();

	/**
	 * If only one candidate to be the max of the list, some additionnal
	 * propagation can be performed (as in usual x == y constraint).
	 */
	protected void onlyOneCandidatePropagation() throws ContradictionException {
		if(isNotEmptySet()) {
			int idx = indexOfMinimumVariable.get();
			if (idx == -1) {
				idx = updateIndexOfCandidateVariable();
			}
			if (idx != -1) {
				indexOfMinimumVariable.set(idx);
				svars[SET_INDEX].addToKernel(idx- VARS_OFFSET, this, false);
				updateBoundInf(ivars[idx].getInf());
				updateBoundSup(ivars[idx].getSup());
				ivars[idx].updateInf(ivars[BOUND_INDEX].getInf(), this, false);
				ivars[idx].updateSup(ivars[BOUND_INDEX].getSup(), this, false);
			}
		}
	}


	protected abstract int getSatisfiedValue(DisposableIntIterator iter);

	@Override
	public boolean isSatisfied() {
		return isNotEmptySet() ? 
				getSatisfiedValue(svars[SET_INDEX].getDomain().getKernelIterator()) == ivars[BOUND_INDEX].getVal() : true;
	}

	protected String pretty(String name) {
		StringBuilder sb = new StringBuilder(32);
		sb.append(ivars[BOUND_INDEX].pretty());
		sb.append(" = ").append(name).append('(');
		sb.append(svars[SET_INDEX].pretty()).append(", ");
		sb.append(StringUtils.pretty(ivars, VARS_OFFSET, ivars.length));
		sb.append(", Policy:").append(emptySetPolicy);
		sb.append(')');
		return new String(sb);

	}

}
