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

package choco.cp.solver.constraints.global;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Enforce a lexicographic ordering on two vectors of integer
 * variables x <_lex y with x = <x_0, ..., x_n>, and y = <y_0, ..., y_n>.
 * ref : Global Constraints for Lexicographic Orderings (Frisch and al)
 */
public final class Lex extends AbstractLargeIntSConstraint {

	public int n;            // size of both vectors
	public IStateInt alpha;  // size of both vectors
	public IStateInt beta;
	public IStateBool entailed;
	public IntDomainVar[] x;
	public IntDomainVar[] y;

	public boolean strict = false;

	// two vectors of same size n vars = [.. v1 ..,.. v2 ..]
	public Lex(IntDomainVar[] vars, int n, boolean strict, IEnvironment environment) {
		super(ConstraintEvent.LINEAR, vars);
		x = new IntDomainVar[n];
		y = new IntDomainVar[n];
		for (int i = 0; i < n; i++) {
			x[i] = vars[i];
			y[i] = vars[i + n];
		}
		this.strict = strict;
		this.n = n;
		alpha = environment.makeInt(0);
		beta = environment.makeInt(0);
		entailed = environment.makeBool(false);
	}

    @Override
    public int getFilteredEventMask(int idx) {
        if(vars[idx].hasEnumeratedDomain()){
            return IntVarEvent.REMVAL_MASK;
        }else{
            return IntVarEvent.INSTINT_MASK +IntVarEvent.BOUNDS_MASK;
        }
    }

    public boolean groundEq(IntDomainVar x1, IntDomainVar y1) {
		if (x1.isInstantiated() && y1.isInstantiated()) {
			return x1.getVal() == y1.getVal();
		}
		return false;
	}

	public boolean leq(IntDomainVar x1, IntDomainVar y1) {
		return x1.getSup() <= y1.getInf();
	}

	public boolean less(IntDomainVar x1, IntDomainVar y1) {
		return x1.getSup() < y1.getInf();
	}

	public boolean greater(IntDomainVar x1, IntDomainVar y1) {
		return x1.getInf() > y1.getSup();
	}

	public boolean checkLex(int i) {
		if (!strict) {
			if (i == n - 1) {
				return leq(x[i], y[i]);
			} else {
				return less(x[i], y[i]);
			}
		} else {
			return less(x[i], y[i]);
		}
	}

	public void ACleq(int i) throws ContradictionException {
		x[i].updateSup(y[i].getSup(), this, false);
		y[i].updateInf(x[i].getInf(), this, false);
	}

	public void ACless(int i) throws ContradictionException {
		x[i].updateSup(y[i].getSup() - 1, this, false);
		y[i].updateInf(x[i].getInf() + 1, this, false);
	}

	public void updateAlpha(int i) throws ContradictionException {
		if (i == beta.get()) {
			this.fail();
		}
		if (i == n) {
			entailed.set(true);
		} else {
			if (!groundEq(x[i], y[i])) {
				alpha.set(i);
				filter(i);
			} else {
				updateAlpha(i + 1);
			}
		}
	}

	public void updateBeta(int i) throws ContradictionException {
		if ((i + 1) == alpha.get()) {
			this.fail();
		}
		if (x[i].getInf() < y[i].getSup()) {
			beta.set(i + 1);
			if (x[i].getSup() >= y[i].getInf()) {
				filter(i);
			}
		} else if (x[i].getInf() == y[i].getSup()) {
			updateBeta(i - 1);
		}
	}

	public void initialize() throws ContradictionException {
		entailed.set(false);
		int i = 0;
		while (i < n && groundEq(x[i], y[i])) {
			i++;
		}
		if (i == n) {
			if (!strict) {
				entailed.set(true);
			} else {
				this.fail();
			}
		} else {
			alpha.set(i);
			if (checkLex(i)) {
				entailed.set(true);
			}
			beta.set(-1);
			while (i != n && x[i].getInf() <= y[i].getSup()) {
				if (x[i].getInf() == y[i].getSup()) {
					if (beta.get() == -1) {
						beta.set(i);
					}
				} else {
					beta.set(-1);
				}
				i++;
			}
			if (i == n) {
				if (!strict) {
					beta.set(Integer.MAX_VALUE);
				} else {
					beta.set(n);
				}
			} else if (beta.get() == -1) {
				beta.set(i);
			}
			if (alpha.get() >= beta.get()) {
				this.fail();
			}
			filter(alpha.get());
		}
	}

	public void filter(int i) throws ContradictionException {
		if (i < beta.get() && !entailed.get()) {                   //Part A
			if (i == alpha.get() && (i + 1 == beta.get())) {        //Part B
				ACless(i);
				if (checkLex(i)) {
					entailed.set(true);
				}
			} else if (i == alpha.get() && (i + 1 < beta.get())) {  //Part C
				ACleq(i);
				if (checkLex(i)) {
					entailed.set(true);
				} else if (groundEq(x[i], y[i])) {
					updateAlpha(i + 1);
				}
			} else if (alpha.get() < i && i < beta.get()) {         //Part D
				if (((i == beta.get() - 1) && x[i].getInf() == y[i].getSup()) || greater(x[i], y[i])) {
					updateBeta(i - 1);
				}
			}
		}
	}

	@Override
	public void awakeOnInf(int idx) throws ContradictionException {
		if (idx < n) {
			filter(idx);
		} else {
			filter(idx - n);
		}
	}

	@Override
	public void awakeOnSup(int idx) throws ContradictionException {
		if (idx < n) {
			filter(idx);
		} else {
			filter(idx - n);
		}
	}

	@Override
	public void awakeOnRem(int idx, int x) throws ContradictionException {
		if (idx < n) {
			filter(idx);
		} else {
			filter(idx - n);
		}
	}

	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		if (idx < n) {
			filter(idx);
		} else {
			filter(idx - n);
		}
	}

	@Override
	public void propagate() throws ContradictionException {
		filter(alpha.get());
	}

	@Override
	public void awake() throws ContradictionException {
		initialize();
	}

	@Override
	public boolean isSatisfied(int[] tuple) {
		for (int i = 0; i < x.length; i++) {
			int xi = tuple[i];
			int yi = tuple[i + n];
			if (xi < yi) {
				return true;
			}else if(xi>yi){
                return false;
            }//else xi == yi
		}
		if (strict) {
			return false;
		} else {
			return (tuple[n - 1] == tuple[n - 1 + n]);
		}
	}

	@Override
	public String pretty() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (int i = 0; i < x.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			IntDomainVar var = x[i];
			sb.append(var.pretty());
		}
		sb.append("} <");
		if (!strict) {
			sb.append("=");
		}
		sb.append("_lex {");
		for (int i = 0; i < y.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			IntDomainVar var = y[i];
			sb.append(var.pretty());
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public Boolean isEntailed() {
		throw new UnsupportedOperationException("isEntailed not yet implemented on choco.cp.cpsolver.constraints.global.Lex");
	}
}
