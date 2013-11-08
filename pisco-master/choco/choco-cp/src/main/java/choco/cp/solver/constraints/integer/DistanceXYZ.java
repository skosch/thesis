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

package choco.cp.solver.constraints.integer;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.integer.AbstractTernIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A constraint to state |x0 - x1| operator x2 + c
 * where operator can be =, <=, >= and x1, x2, x3 are variables
 * Warning: only achieves BoundConsistency for the moment !
 **/
public final class DistanceXYZ extends AbstractTernIntSConstraint {

	protected int operator;

	protected final int cste;

	public final static int EQ = 0;

	public final static int LT = 1;

	public final static int GT = 2;

	/**
	 * Enforces |x0 - x1| op x2 + c
	 * where op can be =, <, >
	 * @param x0
	 * @param x1
	 * @param x2
	 * @param c the constant
	 * @param op the operator to be chosen among {0,1,2} standing for (eq,lt,gt)
	 */
	public DistanceXYZ(IntDomainVar x0, IntDomainVar x1, IntDomainVar x2, int c, int op) {
		super(x0, x1, x2);
		cste = c;
		operator = op;
	}

    @Override
    public int getFilteredEventMask(int idx) {
        return IntVarEvent.BOUNDS_MASK;
    }

    //*************************************************************//
	//********************** Bounds on Z **************************//
	//*************************************************************//

	//update lower bound of v2 if we have v0 != v1
	public boolean filterFromXYtoLBZ() throws ContradictionException {
		if (v1.getInf() - v0.getSup() > 0) { // x < y
			return v2.updateInf(v1.getInf() - v0.getSup() - cste, this, false);
		} else if (v0.getInf() - v1.getSup() > 0) { // x > y
			return v2.updateInf(v0.getInf() - v1.getSup() - cste, this, false);
		} else {
			return false;
		}
	}

	//update upper bound of v2 as max(|v1.sup - v0.inf|, |v1.inf - v0.sup|)
	public boolean filterFromXYtoUBZ() throws ContradictionException {
		int a = Math.abs(v1.getSup() - v0.getInf());
		int b = Math.abs(v0.getSup() - v1.getInf());
		return v2.updateSup(Math.max(a,b) - cste, this, false);
	}

	//*************************************************************//
	//********************** EQ on XY *****************************//
	//*************************************************************//

	public boolean filterEQFromYZToX() throws ContradictionException {
		int lb = v1.getInf() - v2.getSup() - cste;
		int ub = v1.getSup() + v2.getSup() + cste;
		int lbv0 = v1.getSup() - v2.getInf() - cste + 1;
		int ubv0 = v1.getInf() + v2.getInf() + cste - 1;
		return 	v0.updateInf(lb, this, false) ||
				v0.updateSup(ub, this, false) ||
				v0.removeInterval(lbv0, ubv0, this, false);
	}

	public boolean filterEQFromXZToY() throws ContradictionException {
		int lb = v0.getInf() - v2.getSup() - cste;
		int ub = v0.getSup() + v2.getSup() + cste;
		int lbv1 = v0.getSup() - v2.getInf() - cste + 1;
		int ubv1 = v0.getInf() + v2.getInf() + cste - 1;
		return 	v1.updateInf(lb, this, false) ||
				v1.updateSup(ub, this, false) ||
				v1.removeInterval(lbv1, ubv1, this, false);
	}

	//*************************************************************//
	//********************** LT on XY *****************************//
	//*************************************************************//

	// LEQ: update x from the domain of z and y
	public boolean filterLTFromYZtoX() throws ContradictionException {
		int lb = v1.getInf() - v2.getSup() - cste + 1;
		int ub = v1.getSup() + v2.getSup() + cste - 1;
		return 	v0.updateInf(lb, this, false) || v0.updateSup(ub, this, false);
	}

	// LEQ: update x from the domain of z and y
	public boolean filterLTFromXZtoY() throws ContradictionException {
		int lb = v0.getInf() - v2.getSup() - cste + 1;
		int ub = v0.getSup() + v2.getSup() + cste - 1;
		return 	v1.updateInf(lb, this, false) || v1.updateSup(ub, this, false);
	}

	//*************************************************************//
	//********************** GT on XY *****************************//
	//*************************************************************//

	// GEQ: remove interval for x from the domain of z and y
	public boolean filterGTFromYZtoX() throws ContradictionException {
//        DisposableIntIterator it = v0.getDomain().getIterator();
//        boolean b = false;
//        while(it.hasNext()) {
//            int val = it.next();
//            if (Math.max(Math.abs(val- v1.getInf()),Math.abs(val - v1.getSup())) <= v2.getInf() + cste) {
//                b |= v0.removeVal(val,cIdx0);
//            }
//        }
		int lbv0 = v1.getSup() - v2.getInf() - cste;
		int ubv0 = v1.getInf() + v2.getInf() + cste;
		// remove interval [lbv0, ubv0] from domain of v0
		return v0.removeInterval(lbv0, ubv0, this, false);
	}

	// GEQ: remove interval for y from the domain of x and y
	public boolean filterGTFromXZtoY() throws ContradictionException {
//         DisposableIntIterator it = v1.getDomain().getIterator();
//         boolean b = false;
//         while(it.hasNext()) {
//             int val = it.next();
//             if (Math.max(Math.abs(v0.getInf() - val),Math.abs(v0.getSup() - val)) <= v2.getInf() + cste) {
//                 b |= v1.removeVal(val,cIdx1);
//             }
//         }
//         return b;
		int lbv1 = v0.getSup() - v2.getInf() - cste;
		int ubv1 = v0.getInf() + v2.getInf() + cste;
		// remove interval [lbv0, ubv0] from domain of v0
		return v1.removeInterval(lbv1, ubv1, this, false);
	}

	//*************************************************************//
	//********************** main filter **************************//
	//*************************************************************//

	public void filterFixPoint() throws ContradictionException {
		boolean change = true;
		while (change) {
			if (operator == EQ) {
			   change = filterFromXYtoLBZ();
			   change |= filterFromXYtoUBZ();
			   change |= filterEQFromXZToY();
			   change |= filterEQFromYZToX();
			} else if (operator == LT) {
				change = filterFromXYtoLBZ();
				change |= filterLTFromXZtoY();
				change |= filterLTFromYZtoX();
			} else { //GT
				change = filterFromXYtoUBZ();
				change |= filterGTFromXZtoY();
				change |= filterGTFromYZtoX();
			}
		}
	}

	@Override
	public void awakeOnInf(int idx) throws ContradictionException {
		filterFixPoint();
	}

	@Override
	public void awakeOnSup(int idx) throws ContradictionException {
		filterFixPoint();
	}

	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		filterFixPoint();
	}

    public void awakeOnBounds(int varIndex) throws ContradictionException {
        filterFixPoint();
    }

    @Override
	public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
		//do nothing on removals for BC
	}

	@Override
	public void propagate() throws ContradictionException {
		if (operator <= LT ) {
            v2.updateInf(-cste, this, false);
        }
		filterFixPoint();
    }

	@Override
	public String toString() {
		String op;
		if (operator == EQ) {
			op = "=";
		} else if (operator == GT) {
			op = ">";
		} else if (operator == LT) {
			op = "<";
		} else {
			throw new SolverException("unknown operator");
		}
		return "|" + v0 + " - " + v1 + "| " + op + " " + v2 + " + " + cste;
	}

	@Override
	public String pretty() {
		StringBuilder sb = new StringBuilder();
		sb.append("| ").append(v0.pretty()).append(" - ").append(v1.pretty()).append(" | ");
		switch (operator) {
			case EQ:
				sb.append("=");
				break;
			case GT:
				sb.append(">");
				break;
			case LT:
				sb.append("<");
				break;
			default:
				sb.append("???");
				break;
		}
		sb.append(v2 + " + " + cste);
		return sb.toString();
	}

	@Override
	public Boolean isEntailed() {
		throw new UnsupportedOperationException("isEntailed not yet implemented on DistanceXYC constraint");
	}

	@Override
	public boolean isSatisfied(int[] tuple) {
		if (operator == EQ) {
			return Math.abs(tuple[0] - tuple[1]) == tuple[2] + cste;
		} else if (operator == LT) {
			return Math.abs(tuple[0] - tuple[1]) < tuple[2] + cste;
		} else if (operator == GT) {
			return Math.abs(tuple[0] - tuple[1]) > tuple[2] + cste;
		} else {
			throw new SolverException("operator not known");
		}
	}


}
