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
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/*
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 22 avr. 2008
 * Since : Choco 2.0.0
 * enforce the two variables to take the same sign
 * 0 is considered to have both signs
 *
 */
public final class SignOp extends AbstractBinIntSConstraint {

	/**
	 * enforce the two variables to take the same sign
     * 0 is considered to have both signs
	 * if same is true and a different sign if same is false
	 */
	protected boolean same;

	/**
	 * @param x0 first IntDomainVar
	 * @param x1 second IntDomainVar
	 * @param same  The search constant used in the disequality.
	 */

	public SignOp(IntDomainVar x0, IntDomainVar x1, boolean same) {
		super(x0, x1);
		this.same = same;
	}

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
    }

    @Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void filterSame(IntDomainVar x1, IntDomainVar x2) throws ContradictionException {
        if (x1.getSup() < 0) {
			x2.updateSup(0, this, false);
			setEntailed();
		} else if (x1.getInf() > 0) {
			x2.updateInf(0, this, false);
			setEntailed();
		}
	}

	public void filterNotSame(IntDomainVar x1, IntDomainVar x2) throws ContradictionException {
        if(x1.getInf()==0)x1.updateInf( 1, this, false);
        if(x1.getSup()==0)x1.updateSup(-1, this, false);
		if (x1.getSup() < 0) {
			x2.updateInf(1, this, false);
			setEntailed();
		} else if (x1.getInf() > 0) {
			x2.updateSup(-1, this, false);
			setEntailed();
		}
	}


	public void filter() throws ContradictionException {
		if (same) {
			filterSame(v0,v1);
			filterSame(v1,v0);
		} else {
			filterNotSame(v0,v1);
			filterNotSame(v1,v0);
		}
	}

	/**
	 * The one and only propagation method, using foward checking
	 */
	@Override
	public void propagate() throws ContradictionException {
        if (!same) {
            v0.removeVal(0, this, false);
            v1.removeVal(0, this, false);
        }
        filter();
	}

	@Override
	public void awakeOnInf(int idx) throws ContradictionException {
		filter();
	}

	@Override
	public void awakeOnSup(int idx) throws ContradictionException {
		filter();
	}

	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		filter();
	}

	@Override
	public void awakeOnBounds(int varIndex) throws ContradictionException {
		filter();
	}

	@Override
	public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {

	}

    /**
	 * Checks if the listeners must be checked or must fail.
	 */

	@Override
	public Boolean isEntailed() {
           if (v0.isInstantiatedTo(0) ||
               v1.isInstantiatedTo(0)) {
                return same ? Boolean.TRUE : Boolean.FALSE;
           }
           if (v0.getInf() >= 0 && v1.getInf() >= 0 ||
               v0.getSup() <= 0 && v1.getSup() <= 0) {
               return same ? Boolean.TRUE : Boolean.FALSE;
           }
           else if (v0.getInf() > 0 && v1.getSup() < 0 ||
                    v1.getInf() > 0 && v0.getSup() < 0)
            return same ? Boolean.FALSE : Boolean.TRUE;
        return null;
	}

	/**
	 * Checks if the constraint is satisfied when the variables are instantiated.
	 */

	@Override
	public boolean isSatisfied(int[] tuple) {
		if (!same)
           return (tuple[0] != 0 && tuple[1] != 0) &&
                  (
                          (tuple[0] > 0 && tuple[1] < 0)
                          || (tuple[0] < 0 && tuple[1] > 0)
                  );
	    else
            return (tuple[0] == 0 || tuple[1] == 0) ||
                   (tuple[0] >= 0 && tuple[1] >= 0) ||
                   (tuple[0] <= 0 && tuple[1] <= 0);
    }

	/**
	 * tests if the constraint is consistent with respect to the current state of domains
	 *
	 * @return true iff the constraint is bound consistent (weaker than arc consistent)
	 */
	@Override
	public boolean isConsistent() {
		throw new UnsupportedOperationException("is consistent not implemented on SignOp");
	}

	@Override
	public AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
		return new SignOp(v0,v1,!same);
	}


	@Override
	public String pretty() {
		StringBuffer sb = new StringBuffer();
		sb.append(v0.toString());
		if (same) {
			sb.append(" of same sign than ");
		} else {
			sb.append(" of different sign than ");
		}
		sb.append(v1.toString());
		return sb.toString();
	}

}
