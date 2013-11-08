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

package choco.cp.solver.search;

import static choco.cp.solver.search.integer.varselector.ratioselector.ratios.RatioFactory.createDomDegRatio;
import static choco.cp.solver.search.integer.varselector.ratioselector.ratios.RatioFactory.createDomDynDegRatio;
import static choco.cp.solver.search.integer.varselector.ratioselector.ratios.RatioFactory.createMinPreservedRatio;
import choco.cp.solver.constraints.global.scheduling.precedence.ITemporalSRelation;
import choco.cp.solver.search.integer.varselector.ratioselector.DomOverWDegSelector;
import choco.cp.solver.search.integer.varselector.ratioselector.MinRatioSelector;
import choco.cp.solver.search.integer.varselector.ratioselector.RandDomOverWDegSelector;
import choco.cp.solver.search.integer.varselector.ratioselector.RandMinRatioSelector;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

public final class VarSelectorFactory {

	private VarSelectorFactory() {
		super();
	}
	
	//*************************************************************************//

	public static MinRatioSelector domDegSel(Solver solver, IntDomainVar[] vars) {
		return new MinRatioSelector(solver, createDomDegRatio(vars));
	}
	
	public static RandMinRatioSelector domDegSel(Solver solver, IntDomainVar[] vars, long seed) {
		return new RandMinRatioSelector(solver, createDomDegRatio(vars), seed);
	}
	
	//*************************************************************************//
	
	public static MinRatioSelector domDDegSel(Solver solver, IntDomainVar[] vars) {
		return new MinRatioSelector(solver, createDomDynDegRatio(vars));
	}
	
	public static RandMinRatioSelector domDDegSel(Solver solver, IntDomainVar[] vars, long seed) {
		return new RandMinRatioSelector(solver, createDomDynDegRatio(vars), seed);
	}

	//*************************************************************************//
	
	public static MinRatioSelector domWDegSel(Solver solver, IntDomainVar[] vars) {
		return new DomOverWDegSelector(solver, vars);
	}
	
	public static RandMinRatioSelector domWDegSel(Solver solver, IntDomainVar[] vars, long seed) {
		return new RandDomOverWDegSelector(solver, vars, seed);
	}
	
	
	//*************************************************************************//
	
	public static MinRatioSelector minPreserved(Solver solver, ITemporalSRelation[] precedences) {
		return new MinRatioSelector(solver, createMinPreservedRatio(precedences));
	}
	
	public static RandMinRatioSelector minPreserved(Solver solver, ITemporalSRelation[] precedences, long seed) {
		return new RandMinRatioSelector(solver, createMinPreservedRatio(precedences), seed);
	}
	
//*************************************************************************//
	
	public static MinRatioSelector maxPreserved(Solver solver, ITemporalSRelation[] precedences) {
		return new MinRatioSelector(solver, createMinPreservedRatio(precedences));
	}
	
	public static RandMinRatioSelector maxPreserved(Solver solver, ITemporalSRelation[] precedences, long seed) {
		return new RandMinRatioSelector(solver, createMinPreservedRatio(precedences), seed);
	}
		
	
 
}
