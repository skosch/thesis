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

package choco.cp.solver.search.real.objective;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solution;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.search.IObjectiveManager;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.real.RealIntervalConstant;
import choco.kernel.solver.variables.real.RealVar;

public abstract class RealObjectiveManager implements IObjectiveManager {

	public final RealVar objective;

	protected RealIntervalConstant boundInterval;

	protected double bound;

	protected double floorBound;

	protected double targetBound;

	public RealObjectiveManager(RealVar objective) {
		super();
		this.objective = objective;
	}

	public abstract double getInitialBoundValue();

	public abstract double getFloorValue();

	public abstract double getCeilValue();

	@Override
	public final Var getObjective() {
		return objective;
	}

	@Override
	public final Number getObjectiveValue() {
		return Double.valueOf(getFloorValue());
	}

	@Override
	public final Number getBestObjectiveValue() {
		return Double.valueOf(bound);
	}

	@Override
	public final Number getObjectiveTarget() {
		return Double.valueOf(targetBound);
	}

	@Override
	public final Number getObjectiveFloor() {
		return Double.valueOf(floorBound);
	}
	
	@Override
	public final void initBounds() {
		bound = getInitialBoundValue();
		floorBound = getFloorValue();
		targetBound = getCeilValue();
        setBoundInterval();
	}

    protected abstract void setBoundInterval();

	@Override
	public final void postTargetBound() throws ContradictionException {
		objective.intersect(boundInterval);
	}

	@Override
	public final void postFloorBound() throws ContradictionException {
		throw new SolverException("not yet implemented");
	}

	@Override
	public final void postIncFloorBound() throws ContradictionException {
		throw new SolverException("not yet implemented");
	}


	@Override
	public final void incrementFloorBound() {
		throw new SolverException("not yet implemented");
	}

	@Override
	public final void writeObjective(Solution sol) {
		sol.recordRealObjective(getFloorValue());
	}
}
