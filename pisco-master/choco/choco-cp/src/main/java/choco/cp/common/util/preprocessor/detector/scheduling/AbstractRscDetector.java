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

import choco.cp.model.CPModel;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.constraints.global.scheduling.ResourceParameters;

final class PPResource {

	private ComponentConstraint resource;

	private ResourceParameters params;

	public PPResource() {
		super();
	}

	public final void setResource(Constraint rsc) {
		if (rsc instanceof ComponentConstraint) resource = (ComponentConstraint) rsc;
		else throw new ModelException("unknown type resource constraint");

		if (resource.getParameters() instanceof ResourceParameters) params = (ResourceParameters) resource.getParameters();
		else throw new ModelException("unknown resource parameters");
	}

	public final ComponentConstraint getConstraint() {
		return resource;
	}

	public final ResourceParameters getParameters() {
		return params;
	}

	public final TaskVariable getTask(int idx) {
		return (TaskVariable) resource.getVariable(idx);
	}

	public final IntegerVariable getUsage(int idx) {
		return (IntegerVariable) resource.getVariable(params.getUsagesOffset() + idx - params.getNbRegularTasks());
	}

	public final int getMinHeight(int idx) {
		return getHeight(idx).getLowB();
	}

	public final IntegerVariable getHeight(int idx) {
		return (IntegerVariable) resource.getVariable(params.getHeightsOffset() + idx);
	}

	public final IntegerVariable getCons() {
		return (IntegerVariable) resource.getVariable(params.getConsOffset());
	}

	public final IntegerVariable getCapa() {
		return (IntegerVariable) resource.getVariable(params.getCapaOffset());
	}

	public final int getMaxCapa() {
		return getCapa().getUppB();
	}

	public final boolean isNotProducerConsumer() {
		for (int i = params.getHeightsOffset(); i < params.getConsOffset(); i++) {
			if( ( (IntegerVariable) resource.getVariable(i)).getLowB() < 0) return false;
		}
		return true;
	}

}

public abstract class AbstractRscDetector extends AbstractSchedulingConstraintDetector {

	private PPResource rsc;
	
	public AbstractRscDetector(CPModel model, DisjunctiveModel disjMod) {
		super(model, disjMod);
	}
	
	
	
	@Override
	protected boolean isInPreprocess(Constraint c) {
		return super.isInPreprocess(c) && c instanceof ComponentConstraint;
	}

	@Override
	protected void setUp() {
		rsc = new PPResource();
	}

	@Override
	protected void tearDown() {
		rsc = null;
	}

	@Override
	protected final void apply(Constraint ct) {
		rsc.setResource(ct);
		apply(rsc);
	}

	protected abstract void apply(PPResource rsc);

}

