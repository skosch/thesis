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

package choco.cp.solver.constraints.strong;

import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Map;

/**
 * This class provides a skeletal implementation of the MyConstraint interface,
 * to minimize the effort required to implement this interface.
 * 
 * @author vion
 */
public class SCConstraint {
	/**
	 * Enclosed Choco Solver constraint
	 */
	private final ISpecializedConstraint sConstraint;

	/**
	 * Scope of the constraint
	 */
	protected final SCVariable[] scope;

	private int weight = 1;

	/**
	 * @param sConstraint
	 *            Contrainte encapsulée
	 * @param pool
	 *            Map de contraintes entre IntDomainVar et MyVariable pour faire
	 *            la correspondance dans MyConstraint
	 */
	public SCConstraint(ISpecializedConstraint sConstraint,
			Map<IntDomainVar, SCVariable> pool) {
		this.sConstraint = sConstraint;

		scope = new SCVariable[2];

		for (int i = 2; --i >= 0;) {
			scope[i] = pool.get(sConstraint.getVar(i));
			scope[i].addConstraint(this);
		}
	}

	public final boolean check(int[] tuple) {
		return sConstraint.check(tuple);
	}

	public int firstSupport(int position, int value) {
		return sConstraint.firstSupport(position, value);
	}

	public int nextSupport(int position, int value, int lastSupport) {
		return sConstraint.nextSupport(position, value, lastSupport);
	}

	public SCVariable<? extends SCConstraint> getVariable(int position) {
		return scope[position];
	}

	public int getArity() {
		return 2;
	}

	@Override
	public String toString() {
		return "sc" + sConstraint;
	}

	public int getWeight() {
		return weight;
	}

	public void increaseWeight() {
		weight++;

	}

	public SConstraint getSConstraint() {
		return sConstraint;
	}
}