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

package choco.cp.solver.constraints.integer.bool.sum;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A special case of sums over boolean variables only
 */
public final class BoolSumStructure {

    protected final IntDomainVar[] vars;

    protected final AbstractSConstraint<?> cstr;
    /**
     * The number of variables instantiated to zero in the sum
     */
    public final IStateInt nbz;

    /**
     * The number of variables instantiated to one in the sum
     */
    public final IStateInt nbo;

    public final int bGap;

    public final int bValue;

    public BoolSumStructure(IEnvironment environment, AbstractSConstraint<?> cstr, IntDomainVar[] vars, int bValue) {
        super();
        this.cstr = cstr;
        for (IntDomainVar var : vars) {
            if (!var.hasBooleanDomain())
                throw new SolverException("BoolSum takes only boolean variables: " + var.pretty());
        }
        this.vars = vars;
        this.bValue = bValue;
        this.bGap = vars.length - bValue;
        nbz = environment.makeInt(0);
        nbo = environment.makeInt(0);
    }


    public final IntDomainVar[] getBoolVars() {
        return vars;
    }


    public final IStateInt getNbZero() {
        return nbz;
    }


    public final IStateInt getNbOne() {
        return nbo;
    }


    public final int getbGap() {
        return bGap;
    }


    public final int getbValue() {
        return bValue;
    }


    public final void reset() {
        nbz.set(0);
        nbo.set(0);
    }

    public final boolean filterLeq() throws ContradictionException {
        if (bValue == 0) {
            //putAllZero();
            forceAllZero();
            return false;
        }
        return true;
    }

    public final boolean filterGeq() throws ContradictionException {
        if (bValue == vars.length) {
            //putAllOne();
            forceAllOne();
            return false;
        }
        return true;
    }

    public final void putAllZero() throws ContradictionException {
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].isInstantiated()) {
                vars[i].instantiate(0, cstr, false);
            }
        }
    }

    public final void forceAllZero() throws ContradictionException {
        for (int i = 0; i < vars.length; i++) {
            vars[i].instantiate(0, cstr, false);
        }
    }

    public final void putAllOne() throws ContradictionException {
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].isInstantiated()) {
                vars[i].instantiate(1, cstr, false);
            }
        }
    }

    public final void forceAllOne() throws ContradictionException {
        for (int i = 0; i < vars.length; i++) {
            vars[i].instantiate(1, cstr, false);
        }
    }


    public final void addOne() {
        nbo.add(1);
    }

    public final void addZero() {
        nbz.add(1);
    }

    public void awakeOnEq() throws ContradictionException {
        if (nbo.get() > bValue || nbz.get() > bGap) {
            cstr.fail();
        } else if (nbo.get() == bValue) {
            putAllZero();
        } else if (nbz.get() == bGap) {
            putAllOne();
        }
    }

    public void awakeOnGeq() throws ContradictionException {
        if (nbo.get() >= bValue) {
            cstr.setEntailed();
        } else if (nbz.get() > bGap) {
            cstr.fail();
        } else if (nbz.get() == bGap) {
            putAllOne();
        }
    }

    public void awakeOnLeq() throws ContradictionException {
        if (nbz.get() >= bGap) {
            cstr.setEntailed();
        } else if (nbo.get() > bValue) {
            cstr.fail();
        } else if (nbo.get() == bValue) {
            putAllZero();
        }
    }

    public void awakeOnNeq() throws ContradictionException {
        if (nbo.get() > bValue || nbz.get() > bGap) {
            cstr.setEntailed();
        } else if (nbo.get() == bValue) {
            if (nbz.get() == bGap - 1) putAllOne();
            else if (nbz.get() == bGap) cstr.fail();
        } else if (nbz.get() == bGap) {
            if (nbo.get() == bValue - 1) putAllZero();
            else if (nbo.get() == bValue) cstr.fail();
        }
    }

    /**
     * Computes an upper bound estimate of a linear combination of variables.
     *
     * @return the new upper bound value
     */
    public final int computeUbFromScratch() {
        int s = 0;
        for (int i = 0; i < vars.length; i++) {
            s += vars[i].getSup();
        }
        return s;
    }

    /**
     * Computes a lower bound estimate of a linear combination of variables.
     *
     * @return the new lower bound value
     */
    public final int computeLbFromScratch() {
        int s = 0;
        for (int i = 0; i < vars.length; i++) {
            s += vars[i].getInf();
        }
        return s;
    }

    public Boolean isEntailedEq() {
        final int lb = computeLbFromScratch();
        final int ub = computeUbFromScratch();
        if (lb > bValue || ub < bValue) {
            return Boolean.FALSE;
        } else if (lb == ub && bValue == lb) {
            return Boolean.TRUE;
        } else {
            return null;
        }
    }

    public Boolean isEntailedGeq() {
        if (computeLbFromScratch() >= bValue) {
            return Boolean.TRUE;
        } else if (computeUbFromScratch() < bValue) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }

    public Boolean isEntailedLeq() {
        if (computeUbFromScratch() <= bValue) {
            return Boolean.TRUE;
        } else if (computeLbFromScratch() > bValue) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }

    public Boolean isEntailedNeq() {
        final int lb = computeLbFromScratch();
        final int ub = computeUbFromScratch();
        if (lb > bValue || ub < bValue) {
            return Boolean.TRUE;
        } else if (lb == ub && bValue == lb) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }


    public String pretty(String operator) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < vars.length; i++) {
            b.append(vars[i]).append(" + ");
        }
        b.delete(b.length() - 2, b.length());
        b.append(operator).append(' ').append(bValue);
        return b.toString();
    }


}
