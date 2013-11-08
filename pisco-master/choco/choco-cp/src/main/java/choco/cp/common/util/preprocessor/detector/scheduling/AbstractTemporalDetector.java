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

package choco.cp.common.util.preprocessor.detector.scheduling;

import java.util.Iterator;

import choco.Choco;
import choco.Options;
import choco.cp.common.util.preprocessor.AbstractAdvancedDetector;
import choco.cp.model.CPModel;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.constraints.TemporalConstraint;

abstract class AbstractSchedulingDectector extends
AbstractAdvancedDetector {

	public final DisjunctiveModel disjMod;

	public AbstractSchedulingDectector(CPModel model, DisjunctiveModel disjMod) {
		super(model);
		this.disjMod = disjMod;
	}

	public final DisjunctiveModel getDisjunctiveModel() {
		return disjMod;
	}

	protected boolean isInPreprocess(Constraint c) {
		return ! c.getOptions().contains(Options.C_NO_DETECTION);
	}

}


abstract class AbstractSchedulingConstraintDetector extends AbstractSchedulingDectector {

	public AbstractSchedulingConstraintDetector(CPModel model, DisjunctiveModel disjMod) {
		super(model, disjMod);
	}

	protected abstract ConstraintType getType();

	protected abstract void setUp();

	protected abstract void apply(Constraint ct);

	protected abstract void tearDown();

	@Override
	public final void apply() {
		setUp();
		final Iterator<Constraint> iter = model.getConstraintByType(getType());
		while(iter.hasNext()) {
			final Constraint c = iter.next();
			if( isInPreprocess(c)) apply(c);
		}
		tearDown();
	}

}

public abstract class AbstractTemporalDetector extends AbstractSchedulingConstraintDetector {


	public AbstractTemporalDetector(CPModel model, DisjunctiveModel disjMod) {
		super(model, disjMod);
	}

	@Override
	protected boolean isInPreprocess(Constraint c) {
		if( super.isInPreprocess(c) ) {
			if (c instanceof TemporalConstraint) {
				TemporalConstraint ct = (TemporalConstraint) c;
				return ct.isInPreprocess();
			}
		}
		return false;	
	}



	@Override
	protected void setUp() {}



	@Override
	protected void tearDown() {}


	@Override
	protected final void apply(Constraint c) {
		apply( (TemporalConstraint) c);
	}

	protected abstract void apply(TemporalConstraint ct);

	protected final void reformulateImpliedReified(TemporalConstraint ct) {
		assert ct.getConstraintType() == ConstraintType.PRECEDENCE_IMPLIED || ct.getConstraintType() == ConstraintType.PRECEDENCE_REIFIED;
		if(disjMod.containsArc(ct.getOrigin(), ct.getDestination())) {
			delete(ct);
			replaceBy(ct.getDirection(), Choco.ONE);
			if(ct.forwardSetup() > disjMod.setupTime(ct.getOrigin(), ct.getDestination())) {
				add(Choco.precedence(ct.getOrigin(), ct.getDestination(), ct.forwardSetup()));
			}
		} else if(disjMod.containsArc(ct.getDestination(), ct.getOrigin())) {
			delete(ct);
			replaceBy(ct.getDirection(), Choco.ZERO);
		}
	}

}
