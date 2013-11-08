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

package choco.cp.solver.variables.real;

import choco.kernel.memory.IStateDouble;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.variables.real.RealDomain;
import choco.kernel.solver.variables.real.RealInterval;
import choco.kernel.solver.variables.real.RealVar;

/**
 * An implmentation of real variable domains using two stored floats for storing bounds.
 */
public class RealDomainImpl implements RealDomain {

	//public double width_zero = 1.e-8;
	//public double reduction_factor = 0.99;

    final PropagationEngine propagationEngine;

	/**
	 * for the delta domain: current value of the inf (domain lower bound) when the bound started beeing propagated
	 * (just to check that it does not change during the propagation phase)
	 */
	protected double currentInfPropagated = Double.NEGATIVE_INFINITY;

	/**
	 * for the delta domain: current value of the sup (domain upper bound) when the bound started beeing propagated
	 * (just to check that it does not change during the propagation phase)
	 */
	protected double currentSupPropagated = Double.POSITIVE_INFINITY;

	protected IStateDouble inf;

	protected IStateDouble sup;

	protected RealVar variable;

    private final Solver solver;

	public RealDomainImpl(RealVar v, double a, double b, Solver solver) {
		variable = v;
        this.solver = solver;
        this.propagationEngine = solver.getPropagationEngine();

        inf = solver.getEnvironment().makeFloat(a);
		sup = solver.getEnvironment().makeFloat(b);
	}

	@Override
	public String toString() {
		return "[" +this.getInf() +", "+this.getSup()+"]";
	}

	public String pretty() {
		return this.toString();
	}

	public double getInf() {
		return inf.get();
	}

	public double getSup() {
		return sup.get();
	}

	public void intersect(RealInterval interval) throws ContradictionException {
		if ((interval.getInf() > this.getSup()) || (interval.getSup() < this.getInf())) {
			propagationEngine.raiseContradiction(this);
		}

		double old_width = this.getSup() - this.getInf();
		double new_width = Math.min(interval.getSup(), this.getSup()) -
		Math.max(interval.getInf(), this.getInf());
		boolean toAwake = (solver.getPrecision() / 100. <= old_width)
		&& (new_width < old_width * solver.getReduction());

		if (interval.getInf() > this.getInf()) {
			if (toAwake) propagationEngine.postUpdateInf(variable, null, true);
			inf.set(interval.getInf());
		}

		if (interval.getSup() < this.getSup()) {
			if (toAwake) propagationEngine.postUpdateSup(variable, null, true);
			sup.set(interval.getSup());
		}
	}

	public void clearDeltaDomain() {
		currentInfPropagated = Double.NEGATIVE_INFINITY;
		currentSupPropagated = Double.POSITIVE_INFINITY;
	}

	public boolean releaseDeltaDomain() {
		boolean noNewUpdate = ((getInf() == currentInfPropagated) && (getSup() == currentSupPropagated));
		currentInfPropagated = Double.NEGATIVE_INFINITY;
		currentSupPropagated = Double.POSITIVE_INFINITY;
		return noNewUpdate;
	}

	public void freezeDeltaDomain() {
		currentInfPropagated = getInf();
		currentSupPropagated = getSup();
	}

	public boolean getReleasedDeltaDomain() {
		return true;
	}

	public void silentlyAssign(RealInterval i) {
		inf.set(i.getInf());
		sup.set(i.getSup());
	}
}
