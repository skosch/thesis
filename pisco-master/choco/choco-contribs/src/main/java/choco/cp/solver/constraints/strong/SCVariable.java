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

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.Collection;

public class SCVariable<MyConstraint extends SCConstraint> {

	private final IntDomainVar sVariable;

	private final int id;

	// private final Collection<Arc> arcs;

	private final Collection<MyConstraint> constraints;

	private final int offset;

	private int cid;

	public SCVariable(IntDomainVar sVariable, int id) {
		this.sVariable = sVariable;
		this.id = id;

		// arcs = new ArrayList<Arc>();
		constraints = new ArrayList<MyConstraint>();
		offset = sVariable.getInf();
	}

	public int getId() {
		return id;
	}

	public IntDomainVar getSVariable() {
		return sVariable;
	}

	public void addConstraint(MyConstraint constraint) {
		constraints.add(constraint);
	}

	public Collection<MyConstraint> getConstraints() {
		return constraints;
	}

	public String toString() {
		return "my" + sVariable;
	}

	public int getWDeg() {
		int wdeg = 0;
		for (SCConstraint c : constraints) {
			for (int i = c.getArity(); --i >= 0;) {
				final SCVariable<? extends SCConstraint> v = c.getVariable(i);
				if (v == this || v.sVariable.isInstantiated()) {
					continue;
				}
				wdeg += c.getWeight();
			}
		}
		return wdeg;
	}

	public int getDDeg() {
		int ddeg = 0;
		for (SCConstraint c : constraints) {
			if (!c.getSConstraint().getVar(0).isInstantiated() &&
					!c.getSConstraint().getVar(1).isInstantiated()){
				ddeg++;
			}
		}
		return ddeg;
	}

	public int getOffset() {
		return offset;
	}

	public void setCId(AbstractStrongConsistency<? extends SCVariable> asc) {
		cid = asc.cIndices[id];
//		for (DisposableIntIterator it = sVariable.getConstraintVector()
//				.getIndexIterator(); it.hasNext();) {
//			final int i = it.next();
//			if (sVariable.getConstraint(i) == asc) {
//				cid = i;
//				return;
//			}
//		}
//		throw new IllegalArgumentException();
	}

	public void removeVal(int value) throws ContradictionException {
		sVariable.remVal(value);
		//sVariable.removeVal(value, cid);
	}
}
