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

package choco.cp.solver.constraints.global.scheduling.disjunctive;


import static choco.Options.C_DISJ_DP;
import static choco.Options.C_DISJ_EF;
import static choco.Options.C_DISJ_NFNL;
import static choco.Options.C_DISJ_OC;

import java.util.List;

import choco.Options;
import choco.cp.solver.constraints.global.scheduling.AbstractResourceSConstraint;
import choco.kernel.common.util.bitmask.StringMask;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;


/**
 * @author Arnaud Malapert</br> 
 * @since version 2.0.0</br>
 * @version 2.0.3</br>
 */
public class Disjunctive extends AbstractResourceSConstraint {

	private static final long serialVersionUID = -5081383350524112067L;

	public final static StringMask OVERLOAD_CHECKING = new StringMask(C_DISJ_OC,1);
	public final static StringMask NF_NL = new StringMask(C_DISJ_NFNL,1 << 2);
	public final static StringMask DETECTABLE_PRECEDENCE = new StringMask(C_DISJ_DP,1 << 3);
	public final static StringMask EDGE_FINDING_D = new StringMask(C_DISJ_EF,1 << 4);
	
	public static enum Policy {DEFAULT, VILIM, NOT_FIRST, NOT_LAST, DP_EST, DP_LCT, EF_EST, EF_LCT}

	protected Policy policy=Policy.DEFAULT;

	protected IDisjRules rules;

	private boolean noFixPoint;

	protected Disjunctive(Solver solver, String name, TaskVar[] taskvars,
			int nbOptionalTasks, boolean enableHypotheticalDomain, IntDomainVar[] intvars) {
		super(solver, name, taskvars, nbOptionalTasks, false, enableHypotheticalDomain, intvars);

	}

	public Disjunctive(String name, TaskVar[] taskvars, IntDomainVar makespan, Solver solver) {
		super(solver, name, taskvars, makespan);
		this.rules = new DisjRules(rtasks, this.makespan);
	}

	@Override
	public void readOptions(List<String> options) {
		flags.read(options, OVERLOAD_CHECKING, NF_NL, DETECTABLE_PRECEDENCE, EDGE_FINDING_D);
		if(flags.isEmpty()) flags.set(NF_NL, DETECTABLE_PRECEDENCE, EDGE_FINDING_D);
		if(options.contains(Options.C_DISJ_VF)) {policy=Policy.VILIM;}
		else {policy=Policy.DEFAULT;}
	}

	@Override
	public boolean isTaskConsistencyEnforced() {
		return true;
	}

	public final void setFilteringPolicy(final Policy rule) {
		this.policy=rule;
	}

	public final void noSingleRule() {
		this.policy=Policy.DEFAULT;
	}


	protected final boolean applySingleRule() throws ContradictionException {
		switch (policy) {
		case NOT_FIRST: return rules.notFirst();
		case NOT_LAST: return rules.notLast();
		case DP_EST: return rules.detectablePrecedenceEST();
		case DP_LCT: return rules.detectablePrecedenceLCT();
		case EF_EST: return rules.edgeFindingEST();
		case EF_LCT: return rules.edgeFindingLCT();
		default:
			throw new IllegalArgumentException("no rule activated in Disjunctive constraint");
		}
	}

	//****************************************************************//
	//********* EVENTS - PROPAGATION *********************************//
	//****************************************************************//


	protected final void singleRuleFiltering() throws ContradictionException {
		do {
			rules.fireDomainChanged();
			noFixPoint = applySingleRule();
		} while(noFixPoint);
	}

	protected final void defaultFiltering() throws ContradictionException {
		do {
			noFixPoint=false;
			rules.fireDomainChanged();
			if( flags.contains(EDGE_FINDING_D) ) {
				noFixPoint |= rules.edgeFinding();
			} else if ( flags.contains(OVERLOAD_CHECKING)) {
				rules.overloadChecking();
			}
			if(flags.contains(NF_NL)) {
				noFixPoint |= rules.notLast();
				if(flags.contains(DETECTABLE_PRECEDENCE)) {
					noFixPoint |= rules.detectablePrecedenceEST();
					noFixPoint |= rules.notFirst();
					noFixPoint |= rules.detectablePrecedenceLCT();
				} else {
					noFixPoint |= rules.notFirst();
				}
			} else if(flags.contains(DETECTABLE_PRECEDENCE)) {
				noFixPoint |= rules.detectablePrecedenceEST();
				noFixPoint |= rules.detectablePrecedenceLCT();
			}
		}while(noFixPoint);


	}


	protected final void vilimFiltering() throws ContradictionException {
		boolean noGlobalFixPoint;
		do {
			noGlobalFixPoint=false;
			if( flags.contains(EDGE_FINDING_D)) {
				do {
					rules.fireDomainChanged();
					noFixPoint= rules.edgeFinding();
				} while (noFixPoint);
			} else if ( flags.contains(OVERLOAD_CHECKING)) {
				rules.overloadChecking();
			}

			if(flags.contains(NF_NL)) {
				do {
					rules.fireDomainChanged();
					noFixPoint = rules.notFirstNotLast();
					noGlobalFixPoint |= noFixPoint;
				} while (noFixPoint);
			}

			if(flags.contains(DETECTABLE_PRECEDENCE)) {
				do {
					rules.fireDomainChanged();
					noFixPoint = rules.detectablePrecedence();
					noGlobalFixPoint |= noFixPoint;
				} while (noFixPoint);
			}

		} while (noGlobalFixPoint);
	}

	/**
	 * Propagate.
	 * called for any bound events.
	 * @throws ContradictionException the contradiction exception
	 *
	 * @see choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint#propagate()
	 */
	@Override
	public void propagate() throws ContradictionException {
		//Solver.flushLogs();
		if(rules.isActive()) {
			rules.initialize();
			// TODO - Set the constraint passive if necessary - created 4 juil. 2011 by Arnaud Malapert 
			switch (policy) {
			case DEFAULT:defaultFiltering();break;
			case VILIM:vilimFiltering();break;
			default:
				singleRuleFiltering();break;
			}
		}
	}


	@Override
	public boolean isSatisfied(int[] tuple) {
		return isCumulativeSatisfied(tuple, 0, 1);
	}


}











