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

package choco.cp.solver.search.integer.branching.domwdeg;

import choco.cp.solver.search.integer.branching.IRandomBreakTies;
import choco.cp.solver.search.integer.varselector.ratioselector.IntVarRatioSelector;
import choco.cp.solver.search.integer.varselector.ratioselector.MinRatioSelector;
import choco.cp.solver.search.integer.varselector.ratioselector.RandMinRatioSelector;
import choco.cp.solver.search.integer.varselector.ratioselector.ratios.IntRatio;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.AbstractLargeIntBranchingStrategy;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.propagation.listener.PropagationEngineListener;
import choco.kernel.solver.variables.AbstractVar;
import choco.kernel.solver.variables.Var;

import static choco.cp.solver.search.integer.branching.domwdeg.DomWDegUtils.*;

public abstract class AbstractDomOverWDegBranching extends
AbstractLargeIntBranchingStrategy implements PropagationEngineListener, IRandomBreakTies {

	protected final Solver solver;

	protected final IntRatio[] varRatios;

	private IntVarRatioSelector ratioSelector;
	
	//helps to synchronize incremental weights
	protected int updateWeightsCount;

	public AbstractDomOverWDegBranching(Solver solver, IntRatio[] varRatios, Number seed) {
		super();
		this.solver = solver;
		this.varRatios = varRatios;
		initConstraintExtensions(this.solver);
		initVarExtensions(this.solver);
		this.solver.getPropagationEngine().addPropagationEngineListener(this);
		if(seed == null) cancelRandomBreakTies();
		else setRandomBreakTies(seed.longValue());
	}


	public final Solver getSolver() {
		return solver;
	}

	public final IntVarRatioSelector getRatioSelector() {
		return ratioSelector;
	}
	

	@Override
	public void cancelRandomBreakTies() {
		ratioSelector = new MinRatioSelector(solver, varRatios);
	}


	@Override
	public void setRandomBreakTies(long seed) {
		ratioSelector = new RandMinRatioSelector(solver, varRatios, seed);

	}


	//*****************************************************************//
	//*******************  Weighted degrees and failures managment ***//
	//***************************************************************//

	@Override
	public void initConstraintForBranching(SConstraint c) {
		addConstraintExtension(c);
		addConstraintToVarWeights(c);
	}


	protected abstract int getExpectedUpdateWeightsCount();
	
	@Override
	public final void initBranching() {
		final int n = solver.getNbIntVars();
		for (int i = 0; i < n; i++) {
			final Var v = solver.getIntVar(i);
			getVarExtension(v).set(computeWeightedDegreeFromScratch(v));
		}
		updateWeightsCount =  getExpectedUpdateWeightsCount();
	}

	protected final void reinitBranching() {
		if( updateWeightsCount != getExpectedUpdateWeightsCount()) initBranching();
	}

	private void updateVarWeights(final Var currentVar, final SConstraint<?> cstr, final int delta) {
		if(delta != 0) {
			final int n = cstr.getNbVars();
			for (int k = 0; k < n; k++) {
				final AbstractVar var = (AbstractVar) cstr.getVarQuick(k);
				if (var != currentVar && ! var.isInstantiated()) {
					getVarExtension(var).add(delta);
                    //check robustness of the incremental weights
                    assert getVarExtension(var).get() >= 0:
                            ""+var.getName()+ " weight is negative ("+getVarExtension(var).get()+"). This is due to incremental computation of weights in Dom/WDeg.";
				}
			}
		}
	}


	private boolean isDisconnected(SConstraint<?> cstr) {
		return SConstraintType.INTEGER.equals(cstr.getConstraintType()) && hasTwoNotInstVars(cstr); 
	}

	protected final void increaseVarWeights(final Var currentVar) {
		updateWeightsCount--;
		final DisposableIterator<SConstraint> iter = currentVar.getConstraintsIterator();
		while(iter.hasNext()) {
			final SConstraint cstr = iter.next();
			if (isDisconnected(cstr) ) {
				updateVarWeights(currentVar, cstr, getConstraintExtension(cstr).get());
			}
		}
        iter.dispose();
	}

	protected final void decreaseVarWeights(final Var currentVar) {
		updateWeightsCount++;
		final DisposableIterator<SConstraint> iter = currentVar.getConstraintsIterator();
		while(iter.hasNext()) {
			final SConstraint cstr = iter.next();
			if (isDisconnected(cstr) ) {
				updateVarWeights(currentVar, cstr, - getConstraintExtension(cstr).get());
			}
		}
        iter.dispose();
	}

	@Override
	public final void contradictionOccured(ContradictionException e) {
		if( updateWeightsCount == getExpectedUpdateWeightsCount() ) {
			addIncFailure(e.getDomOverDegContradictionCause());
		} else {
			//weights are already out-of-date
			addFailure(e.getDomOverDegContradictionCause());
		}
	}

	
	@Override
	public final void safeDelete() {
		solver.getPropagationEngine().removePropagationEngineListener(this);
	}
	//*****************************************************************//
	//*******************  Variable Selection *************************//
	//***************************************************************//

	public Object selectBranchingObject() throws ContradictionException {
		reinitBranching();
//		System.out.println(DomWDegUtils.checkVariableIncWDeg(solver));
		return ratioSelector.selectVar();
	}
	
	@Override
	public String toString() {
		return "nbUpdates: "+updateWeightsCount+"\n"+getVariableIncWDeg(solver) + "\n" + getConstraintFailures(solver);
	}



}