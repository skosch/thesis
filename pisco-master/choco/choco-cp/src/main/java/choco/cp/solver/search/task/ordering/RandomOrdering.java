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

package choco.cp.solver.search.task.ordering;

import java.util.Random;

import choco.cp.solver.constraints.global.scheduling.precedence.ITemporalSRelation;
import choco.cp.solver.search.task.OrderingValSelector;
import choco.kernel.model.constraints.ITemporalRelation;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

public class RandomOrdering implements OrderingValSelector {

	protected final Random randomBreakTie;

	public RandomOrdering(long seed) {
		super();
		randomBreakTie = new Random(seed);
	}

	protected final int nextVal() {
		return randomBreakTie.nextBoolean() ? 1 : 0;
	}

	protected final int getMaxVal(int vZero, int vOne) {
		if(vOne > vZero) return 1;
		else if(vOne < vZero) return 0;
		else return nextVal();
	}

	protected final int getMaxVal(double vZero, double vOne) {
		if(vOne > vZero) return 1;
		else if(vOne < vZero) return 0;
		else return nextVal();
	}
	
	protected final int getMinVal( int vZero, int vOne) {
		if(vOne > vZero) return 0;
		else if(vOne < vZero) return 1;
		else return nextVal();
	}
	
	protected final int getMinVal(double vZero, double vOne) {
		if(vOne > vZero) return 0;
		else if(vOne < vZero) return 1;
		else return nextVal();
	}

	@Override
	public int getBestVal(ITemporalSRelation p) {
		return nextVal();
	}

}