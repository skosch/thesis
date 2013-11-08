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

package choco.cp.solver.search.integer.varselector.ratioselector;

import choco.cp.solver.search.integer.varselector.ratioselector.ratios.IntRatio;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.SimpleRatio;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.AbstractSearchHeuristic;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntArrayList;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
/**
 * A class that select the min/max ratio (a/b) among an arrays.
 * the for-loop of search stores the index of the first not instantiated variable.
 * Two ratios are compared by the formula a1 * b2 <=/>= a2 * b1 (note cast to long avoids integer overflow
 * )
 * @author Arnaud Malapert</br> 
 * @since 26 mars 2010 version 2.1.1</br>
 * @version 2.1.1</br>
 */
public abstract class AbstractIntVarRatioSelector extends AbstractSearchHeuristic implements IntVarRatioSelector  {

	protected final IntRatio[] ratios;

	private final IStateInt begin;

	private final SimpleRatio bestRatio = new SimpleRatio();

	private int reuseIdx;

	public AbstractIntVarRatioSelector(Solver solver, IntRatio[] ratios) {
		super(solver);
		this.ratios = ratios;
		this.begin = solver.getEnvironment().makeInt(0);
	}

	@Override
	public final IntRatio[] getRatios() {
		return ratios;
	}


	private long getLeftMember(int idx) {
		return bestRatio.getLeftMember(ratios[idx]);
	}

	private long getRightMember(int idx) {
		return bestRatio.getRightMember(ratios[idx]);
	}

	protected abstract boolean isUp(long leftM, long rightM);

	private void initialize() {
		reuseIdx =begin.get();
		while(reuseIdx < ratios.length && ! ratios[reuseIdx].isActive()) {reuseIdx++;}
		begin.set(reuseIdx);
	}


	protected final int selectRandIntRatioIndex(final Random randomBreakTies, final TIntArrayList canWriteList) {
		initialize();
		if(reuseIdx < ratios.length) {
			canWriteList.resetQuick();
			canWriteList.add(reuseIdx);
			bestRatio.setRatio(ratios[reuseIdx]);
			reuseIdx++;
			while(reuseIdx < ratios.length) {
				if( ratios[reuseIdx].isActive() ) {
					final long leftM = getLeftMember(reuseIdx);
					final long rightM = getRightMember(reuseIdx);
					if(isUp(leftM, rightM)) {
						canWriteList.resetQuick();
						canWriteList.add(reuseIdx);
						bestRatio.setRatio(ratios[reuseIdx]);
					}else if( leftM == rightM) {
						canWriteList.add(reuseIdx);
					}
				}
				reuseIdx++;
			}
			final int n = canWriteList.size();
			switch (canWriteList.size()) {
			case 1:return canWriteList.get(0);
			default: return canWriteList.get( randomBreakTies.nextInt(n));
			}
		}
		return NULL;
	}

	@Override
	public int selectIntRatioIndex() {
		initialize();
		int bestIdx = NULL;
		if(reuseIdx < ratios.length) {
			bestIdx = reuseIdx;
			bestRatio.setRatio(ratios[reuseIdx]);
			reuseIdx++;
			while(reuseIdx < ratios.length) {
				if( ratios[reuseIdx].isActive() ) {
					if( isUp(getLeftMember(reuseIdx), getRightMember(reuseIdx))) {
						bestIdx = reuseIdx;
						bestRatio.setRatio(ratios[reuseIdx]);
					}
				}
				reuseIdx++;
			}
		}
		return bestIdx;
	}



	@Override
	public final IntRatio selectIntRatio() {
		final int bestIdx = selectIntRatioIndex();
		return bestIdx >= 0 ? ratios[bestIdx] : null;
	}

	@Override
	public final IntDomainVar selectVar() {
		final int bestIdx = selectIntRatioIndex();
		return bestIdx >= 0 ? ratios[bestIdx].getIntVar() : null;
	}

	@Override
	public final List<IntDomainVar> selectTiedIntVars() {
		initialize();
		final List<IntDomainVar> vars = new LinkedList<IntDomainVar>();
		if(reuseIdx < ratios.length) {
			vars.add(ratios[reuseIdx].getIntVar());
			bestRatio.setRatio(ratios[reuseIdx]);
			reuseIdx++;
			while(reuseIdx < ratios.length) {
				if( ratios[reuseIdx].isActive() ) {
					final long leftM = getLeftMember(reuseIdx);
					final long rightM = getRightMember(reuseIdx);
					if(isUp(leftM, rightM)) {
						vars.clear();
						bestRatio.setRatio(ratios[reuseIdx]);
						vars.add(ratios[reuseIdx].getIntVar());
					}else if( leftM == rightM) {
						vars.add(ratios[reuseIdx].getIntVar());
					}
				}
				reuseIdx++;
			}
		}
		return vars;
	}



}



abstract class AbstractRandomizedRatioSelector extends AbstractIntVarRatioSelector {

	protected final TIntArrayList reuseList = new TIntArrayList();

	protected final Random randomBreakTies;

	public AbstractRandomizedRatioSelector(Solver solver, IntRatio[] ratios, long seed) {
		super(solver, ratios);
		this.randomBreakTies = new Random(seed);
	}

	@Override
	public int selectIntRatioIndex() {
		return super.selectRandIntRatioIndex(randomBreakTies, reuseList);
	}


}



