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

package choco.kernel.common.opres.heuristics;

import choco.kernel.common.TimeCacheThread;

public abstract class AbstractHeuristic implements IHeuristic {
	
	private int objective;
	
	private boolean hasSearched = false;
	
	private double time;

	@Override
	public void reset() {
		hasSearched = false;
		time= 0;
		objective = Integer.MAX_VALUE;
	}
	
	@Override
	public boolean isObjectiveOptimal() {
		return false;
	}
	
	@Override
	public int getIterationCount() {
		return hasSearched() ? 1 : 0;
	}


	@Override
	public int getSolutionCount() {
		return hasSearched() ? 1 : 0;
	}



	@Override
	public final void execute() {
		time = - TimeCacheThread.currentTimeMillis;
		objective = apply();
		time += TimeCacheThread.currentTimeMillis;
		time/=1000;
		hasSearched = true;
	}
	
	public final void executeQuick() {
		objective = apply();
		hasSearched = true;
	}



	protected abstract int apply();


	public final int getNumberOfBins() {
		return objective;
	}
	
	@Override
	public final Number getObjectiveValue() {
		return Integer.valueOf(objective);
	}

	@Override
	public final double getTimeCount() {
		return time;
	}

	@Override
	public final boolean hasSearched() {
		return hasSearched;
	}


	@Override
	public boolean existsSolution() {
		return hasSearched();
	}

	@Override
	public String solutionToString() {
		return null;
	}
	
	
}

