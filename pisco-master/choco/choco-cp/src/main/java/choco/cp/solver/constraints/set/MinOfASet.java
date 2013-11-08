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
 * @author Arnaud Malapert</br>
 * @since 8 déc. 2008 version 2.0.1</br>
 * @version 2.0.1</br>
 */
public final class MinOfASet extends AbstractBoundOfASet {


	/**
	 * Index of the minimum variable.
	 */
	protected final IStateInt indexOfMinimumVariable;


	public MinOfASet(IEnvironment environment, IntDomainVar[] intvars, SetVar setvar, EmptySetPolicy emptySetPolicy) {
		super(environment, intvars, setvar, emptySetPolicy);
		indexOfMinimumVariable = environment.makeInt(-1);
	}

	@Override
	protected boolean removeFromEnv(int idx) throws ContradictionException {
		return removeLowerFromEnv(idx, ivars[BOUND_INDEX].getInf());
	}

	@Override
	protected int  updateIndexOfCandidateVariable() {
		int minMin = Integer.MAX_VALUE;
		int minMinIdx = -1;
		int minMin2 = Integer.MAX_VALUE;
		DisposableIntIterator iter= this.getSetDomain().getEnveloppeIterator();
		while(iter.hasNext()) {
			final int idx = iter.next() + VARS_OFFSET;
			final int val = ivars[idx].getInf();
			if (val <= minMin) {
				minMin2 = minMin;
				minMin = val;
				minMinIdx = idx;
			} else if (val < minMin2) {
				minMin2 = val;
			}
		}
		iter.dispose();
		return minMin2 > ivars[BOUND_INDEX].getSup() ? minMinIdx : -1;
	}


	protected final int minInf() {
		if( isNotEmptySet()) {
			int min = Integer.MAX_VALUE;
			final DisposableIntIterator iter= getSetDomain().getEnveloppeIterator();
			while(iter.hasNext()) {
				int val = ivars[VARS_OFFSET + iter.next()].getInf();
				if(val < min) min = val;
			}
			iter.dispose();
			return min;
		} else return Integer.MIN_VALUE;
	}

	protected final int minSup() {
		int min = Integer.MAX_VALUE;
		//if the set can be empty : we do nothing
		DisposableIntIterator iter= getSetDomain().getKernelIterator();
		while(iter.hasNext()) {
			int val = ivars[VARS_OFFSET+iter.next()].getSup();
			if(val<min) {min=val;}
		}
		iter.dispose();
		return min;	
	}

	protected final void updateKernelInf() throws ContradictionException {
		final int minValue = ivars[BOUND_INDEX].getInf();
		DisposableIntIterator iter= svars[SET_INDEX].getDomain().getKernelIterator();
		while(iter.hasNext()) {
			ivars[VARS_OFFSET + iter.next()].updateInf(minValue, this, false);
		}
		iter.dispose();
	}

	/**
	 * Propagation of the constraint.
	 *
	 * @throws choco.kernel.solver.ContradictionException if a domain becomes empty.
	 */
	@Override
	public void filter() throws ContradictionException {
		do {
			updateBoundInf(minInf());
		} while(remFromEnveloppe());
		updateBoundSup(minSup());
		updateKernelInf();
		onlyOneCandidatePropagation();
	}

	/**
	 * Propagation when lower bound is increased.
	 *
	 * @param idx the index of the modified variable.
	 * @throws ContradictionException if a domain becomes empty.
	 */
	@Override
	public void awakeOnInf(final int idx) throws ContradictionException {
		if (idx >= 2*VARS_OFFSET) { // Variable in the list
			final int i = idx-2*VARS_OFFSET;
			if(isInEnveloppe(i)) {
				//maxOfaList case
				do {
					updateBoundInf(minInf());
				} while(remFromEnveloppe());
				onlyOneCandidatePropagation();
			}
		} else { // Maximum variable
			while( remFromEnveloppe() ) {
				updateBoundInf(minInf());
			}
			updateKernelInf();
			onlyOneCandidatePropagation();
		}
	}

	/**
	 * Propagation when upper bound is decreased.
	 *
	 * @param idx the index of the modified variable.
	 * @throws choco.kernel.solver.ContradictionException if a domain becomes empty.
	 */
	@Override
	public void awakeOnSup(final int idx) throws ContradictionException {
		if (idx >= 2*VARS_OFFSET) { // Variable in the list
			final int i = idx-2*VARS_OFFSET;
			if(isInEnveloppe(i)) {
				if(isInKernel(i)) {
					updateBoundSup(minSup());
				} else {
					while( remFromEnveloppe() ) {
						updateBoundInf(minInf());
					}
				}
			}
		} // else // Maximum variable
		onlyOneCandidatePropagation();

	}

	
	@Override
	public void awakeOnRem() throws ContradictionException {
		do {
			updateBoundInf(minInf());
		} while(remFromEnveloppe());
		onlyOneCandidatePropagation();
	}

	@Override
	public void awakeOnKer() throws ContradictionException {
		updateBoundSup(minSup());
		onlyOneCandidatePropagation();
	}

	@Override
	protected int getSatisfiedValue(DisposableIntIterator iter) {
		int v = Integer.MAX_VALUE;
		do {
			v = Math.min(v, ivars[VARS_OFFSET +iter.next()].getVal());
		}while(iter.hasNext());
		iter.dispose();
		return v;
	}

	@Override
	public String pretty() {
		return pretty("min");
	}


}
