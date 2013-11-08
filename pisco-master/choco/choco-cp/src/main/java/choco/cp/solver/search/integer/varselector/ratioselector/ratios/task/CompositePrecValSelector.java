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

package choco.cp.solver.search.integer.varselector.ratioselector.ratios.task;

import choco.cp.solver.search.integer.varselector.ratioselector.IntVarRatioSelector;
import choco.cp.solver.search.task.OrderingValSelector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.search.integer.IntVarValPair;
import choco.kernel.solver.search.integer.VarValPairSelector;


public final class CompositePrecValSelector implements VarValPairSelector {

	private final ITemporalRatio[] precRatios;

	private IntVarRatioSelector varSel;
	
	private OrderingValSelector valSel;

	public CompositePrecValSelector(ITemporalRatio[] precRatios, IntVarRatioSelector varSel, OrderingValSelector valSel) {
		this.precRatios = precRatios;
		this.varSel = varSel;
		this.valSel = valSel;
	}
	
	@Override
	public IntVarValPair selectVarValPair() throws ContradictionException {
		final int idx = varSel.selectIntRatioIndex();
		return idx >= 0 ?new IntVarValPair(precRatios[idx].getIntVar(), valSel.getBestVal(precRatios[idx].getTemporalRelation())) : null;
	}
}
