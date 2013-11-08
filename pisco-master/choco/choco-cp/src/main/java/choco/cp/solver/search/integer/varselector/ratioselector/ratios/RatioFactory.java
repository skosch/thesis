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

package choco.cp.solver.search.integer.varselector.ratioselector.ratios;

import choco.cp.solver.constraints.global.scheduling.precedence.ITemporalSRelation;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.degree.DomDegRatio;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.degree.DomDynDegRatio;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.degree.DomWDegRatio;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.degree.IncDomWDegRatio;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.task.ITemporalRatio;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.task.preserved.IncPreservedWDegRatio;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.task.preserved.MaxPreservedRatio;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.task.preserved.MinPreservedRatio;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.task.preserved.PreservedWDegRatio;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.task.slack.IncSlackWDegRatio;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.task.slack.SlackWDegRatio;
import choco.kernel.solver.variables.integer.IntDomainVar;

public final class RatioFactory {

	private RatioFactory() {
		super();
	}

	public static SimpleRatio[] createDefaultRatio(int[] dividends, int[] divisors) {
		final int n = dividends.length;
		if( n != divisors.length) throw new IllegalArgumentException("the sizes are different.");
		final SimpleRatio[] ratios = new SimpleRatio[n];
		for (int i = 0; i < n; i++) {
			ratios[i] = new SimpleRatio(dividends[i], divisors[i]);
		}
		return ratios;
	}

	public static IntRatio[] createDomDegRatio(IntDomainVar[] vars) {
		final int n = vars.length;
		final IntRatio[] ratios = new IntRatio[n];
		for (int i = 0; i < n; i++) {
			ratios[i] = new DomDegRatio(vars[i]);
		}
		return ratios;
	}

	public static IntRatio[] createDomDynDegRatio(IntDomainVar[] vars) {
		final int n = vars.length;
		final IntRatio[] ratios = new IntRatio[n];
		for (int i = 0; i < n; i++) {
			ratios[i] = new DomDynDegRatio(vars[i]);
		}
		return ratios;
	}

	public static IntRatio[] createDomWDegRatio(IntDomainVar[] vars, boolean incremental) {
		final int n = vars.length;
		final IntRatio[] ratios = new IntRatio[n];
		if(incremental) {
			for (int i = 0; i < n; i++) {
				ratios[i] = new IncDomWDegRatio(vars[i]);
			}	
		}else {
			for (int i = 0; i < n; i++) {
				ratios[i] = new DomWDegRatio(vars[i]);
			}
		}

		return ratios;
	}


	public static ITemporalRatio[] createSlackWDegRatio(ITemporalSRelation[] precedences, boolean incremental) {
		final int n = precedences.length;
		final ITemporalRatio[] ratios = new ITemporalRatio[n];
		if(incremental) {
			for (int i = 0; i < n; i++) {
				ratios[i] = new IncSlackWDegRatio(precedences[i]);
			}	
		}else {
			for (int i = 0; i < n; i++) {
				ratios[i] = new SlackWDegRatio(precedences[i]);
			}
		}
		return ratios;
	}

	public static ITemporalRatio[] createPreservedWDegRatio(ITemporalSRelation[] precedences,boolean incremental) {
		final int n = precedences.length;
		final ITemporalRatio[] ratios = new ITemporalRatio[n];
		if(incremental) {
			for (int i = 0; i < n; i++) {
				ratios[i] = new IncPreservedWDegRatio(precedences[i]);
			}	
		}else {
			for (int i = 0; i < n; i++) {
				ratios[i] = new PreservedWDegRatio(precedences[i]);
			}
		}
		return ratios;
	}

	public static MaxPreservedRatio[] createMaxPreservedRatio(ITemporalSRelation[] precedences) {
		final int n = precedences.length;
		final MaxPreservedRatio[] ratios = new MaxPreservedRatio[n];
		for (int i = 0; i < n; i++) {
			ratios[i] = new MaxPreservedRatio(precedences[i]);
		}
		return ratios;
	}

	public static MinPreservedRatio[] createMinPreservedRatio(ITemporalSRelation[] precedences) {
		final int n = precedences.length;
		final MinPreservedRatio[] ratios = new MinPreservedRatio[n];
		for (int i = 0; i < n; i++) {
			ratios[i] = new MinPreservedRatio(precedences[i]);
		}
		return ratios;
	}

}
