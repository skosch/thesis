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

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;

/**
 * Implements a constraint X = max(Y_i | i \in S). I only modified the
 * maxOfAList constraint
 * 
 * @author Arnaud Malapert</br>
 * @since 8 déc. 2008 version 2.0.1</br>
 * @version 2.0.1</br>
 */
public final class MaxOfASet extends AbstractBoundOfASet {



	public MaxOfASet(IEnvironment environment, IntDomainVar[] intvars,
			SetVar setvar, EmptySetPolicy emptySetPolicy) {
		super(environment, intvars, setvar, emptySetPolicy);
	}

	@Override
	protected boolean removeFromEnv(int idx) throws ContradictionException {
		return removeGreaterFromEnv(idx, ivars[BOUND_INDEX].getSup());
	}

	@Override
	protected int updateIndexOfCandidateVariable() {
		int maxMax = Integer.MIN_VALUE, maxMaxIdx = -1;
		int maxMax2 = Integer.MIN_VALUE;
		final DisposableIntIterator iter = this.getSetDomain()
				.getEnveloppeIterator();
		while (iter.hasNext()) {
			final int idx = iter.next() + VARS_OFFSET;
			final int val = ivars[idx].getSup();
			if (val >= maxMax) {
				maxMax2 = maxMax;
				maxMax = val;
				maxMaxIdx = idx;
			} else if (val > maxMax2) {
				maxMax2 = val;
			}
		}
		iter.dispose();
		return maxMax2 < ivars[BOUND_INDEX].getInf() ?  maxMaxIdx : -1;
	}

	protected final int maxInf() {
		DisposableIntIterator iter = getSetDomain().getKernelIterator();
		int max = Integer.MIN_VALUE;
		while (iter.hasNext()) {
			int val = ivars[VARS_OFFSET + iter.next()].getInf();
			if (val > max) {
				max = val;
			}
		}
		iter.dispose();
		return max;
	}

	protected final int maxSup() {
		if (isNotEmptySet()) {
			int max = Integer.MIN_VALUE;
			// if the set could be empty : we do nothing
			DisposableIntIterator iter = getSetDomain().getEnveloppeIterator();
			while (iter.hasNext()) {
				int val = ivars[VARS_OFFSET + iter.next()].getSup();
				if (val > max) {
					max = val;
				}
			}
			iter.dispose();
			return max;
		} else {
			return Integer.MAX_VALUE;
		}
	}

	protected final void updateKernelSup() throws ContradictionException {
		final int maxValue = ivars[BOUND_INDEX].getSup();
		DisposableIntIterator iter = svars[SET_INDEX].getDomain()
				.getKernelIterator();
		try {
			while (iter.hasNext()) {
				final int i = VARS_OFFSET + iter.next();
				ivars[i].updateSup(maxValue, this, false);
			}
		} finally {
			iter.dispose();
		}
	}

	/**
	 * Propagation of the constraint.
	 * 
	 * @throws choco.kernel.solver.ContradictionException
	 *             if a domain becomes empty.
	 */
	@Override
	public void filter() throws ContradictionException {
		do {
			updateBoundSup(maxSup());
		} while( remFromEnveloppe());
		updateBoundInf(maxInf());
		updateKernelSup();
		onlyOneCandidatePropagation();
	}

	/**
	 * Propagation when lower bound is increased.
	 * 
	 * @param idx
	 *            the index of the modified variable.
	 * @throws ContradictionException
	 *             if a domain becomes empty.
	 */
	@Override
	public void awakeOnInf(final int idx) throws ContradictionException {
		if (idx >= 2 * VARS_OFFSET) { // Variable in the list
			final int i = idx - 2 * VARS_OFFSET;
			if (isInEnveloppe(i)) {
				if(isInKernel(i) ){
					updateBoundInf(maxInf());
				} else {
					while(remFromEnveloppe()) {
						updateBoundSup(maxSup());
					}
				}
			}
		} 
		onlyOneCandidatePropagation();
	}

	/**
	 * Propagation when upper bound is decreased.
	 * 
	 * @param idx
	 *            the index of the modified variable.
	 * @throws choco.kernel.solver.ContradictionException
	 *             if a domain becomes empty.
	 */
	@Override
	public void awakeOnSup(final int idx) throws ContradictionException {
		if (idx >= 2 * VARS_OFFSET) { // Variable in the list
			final int i = idx - 2 * VARS_OFFSET;
			if (isInEnveloppe(i)) {
				do {
					updateBoundSup(maxSup());
				} while(remFromEnveloppe());
				onlyOneCandidatePropagation();

			}
		} else { // Maximum variable
			while(remFromEnveloppe()) {
				updateBoundSup(maxSup());
			}
			updateKernelSup();
			onlyOneCandidatePropagation();
		}
	}


	@Override
	public void awakeOnRem() throws ContradictionException {
		do {
			updateBoundSup(maxSup());
		} while(remFromEnveloppe());
		onlyOneCandidatePropagation();
	}


	@Override
	public void awakeOnKer() throws ContradictionException {
		updateBoundInf(maxInf());
		onlyOneCandidatePropagation();
	}

	@Override
	protected int getSatisfiedValue(DisposableIntIterator iter) {
		int v = Integer.MIN_VALUE;
		do {
			v = Math.max(v, ivars[VARS_OFFSET + iter.next()].getVal());
		} while (iter.hasNext());
		iter.dispose();
		return v;
	}

	@Override
	public String pretty() {
		return pretty("max");
	}

}
