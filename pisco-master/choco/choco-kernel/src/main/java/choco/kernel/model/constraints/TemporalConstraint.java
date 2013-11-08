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

package choco.kernel.model.constraints;

import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;

public class TemporalConstraint extends ComponentConstraint implements ITemporalRelation<TaskVariable, IntegerVariable> {


	private static final long serialVersionUID = 5410687647692627875L;

	public TemporalConstraint(ConstraintType constraintType,
			Object parameters, Variable[] variables) {
		super(constraintType, parameters, variables);
	}

	public final boolean isInPreprocess() {
		assert checkDomains();
		if (getParameters() instanceof Boolean) {
			return (Boolean) getParameters() 
			&& getForwardSetup().getLowB() >= 0
			&& getBackwardSetup().getLowB() >= 0;
		}
		return false;
	}

    public final boolean checkDomains() {
		return getDirection().isBoolean()
		&& getForwardSetup().isConstant()
		&& getBackwardSetup().isConstant();
	}

	/* (non-Javadoc)
	 * @see choco.kernel.model.constraints.ITemporalRelation#getOrigin()
	 */
	public final TaskVariable getOrigin() {
		return (TaskVariable) getVariable(0);
	}

	public final int getOHook() {
		return getOrigin().getHook();
	}

	public final IntegerVariable getForwardSetup() {
		return (IntegerVariable) getVariable(1);
	}

	@Override
	public final int forwardSetup() {
		return getForwardSetup().getLowB();
	}
	
	public final void setForwardSetup(int val) {
		replaceByConstantAt(1, val);
	}

	@Override
	public final TaskVariable getDestination() {
		return (TaskVariable) getVariable(2);
	}

	public final int getDHook() {
		return getDestination().getHook();
	}

	public final IntegerVariable getBackwardSetup() {
		return (IntegerVariable) getVariable(3);
	}

	
	@Override
	public final int backwardSetup() {
		return getBackwardSetup().getLowB();
	}

	public final void setBackwardSetup(int val) {
		replaceByConstantAt(3, val);
	}

	@Override
	public final IntegerVariable getDirection() {
		return (IntegerVariable) getVariable(4);
	}
	
	@Override
	public final boolean isFixed() {
		return getDirection().isConstant();
	}

	@Override
	public int getDirVal() {
		return getDirection().getLowB();
	}

	@Override
	public String toString() {
		return pretty();
	}

	
}
