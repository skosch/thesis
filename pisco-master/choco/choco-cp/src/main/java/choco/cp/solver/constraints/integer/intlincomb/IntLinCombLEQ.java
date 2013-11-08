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

package choco.cp.solver.constraints.integer.intlincomb;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 11 mars 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public final class IntLinCombLEQ extends IntLinCombOp {


    public IntLinCombLEQ(final int[] coeffs, final int nbPosVars, final int cste, final IntDomainVar[] vars, final AbstractSConstraint constraint) {
        super(coeffs, nbPosVars, cste, vars, constraint);
    }

    /**
     * Checks if the constraint is entailed.
     *
     * @return Boolean.TRUE if the constraint is satisfied, Boolean.FALSE if it
     *         is violated, and null if the filtering algorithm cannot infer yet.
     */
    public Boolean isEntailed() {
        if (coeffPolicy.computeLowerBound() > 0) {
            return Boolean.FALSE;
        } else if (coeffPolicy.computeUpperBound() <= 0) {
            return Boolean.TRUE;
        } else {
            return null;
        }

    }

    /**
     * Checks if the constraint is satisfied when all variables are instantiated.
     *
     * @return true if the constraint is satisfied
     */
    public boolean isSatisfied(int[] tuple) {
        return (compute(tuple) <= 0);
    }

    /**
     * Checks a new lower bound.
     *
     * @return true if filtering has been infered
     * @throws choco.kernel.solver.ContradictionException
     *          if a domain empties or a contradiction is
     *          infered
     */
    public boolean filterOnImprovedLowerBound()
            throws ContradictionException {
        return propagateNewLowerBound(coeffPolicy.computeLowerBound());
    }

    /**
     * Checks a new upper bound.
     *
     * @return true if filtering has been infered
     * @throws ContradictionException if a domain empties or a contradiction is
     *                                infered
     */
    public boolean filterOnImprovedUpperBound()
            throws ContradictionException {
        return false;
    }

    /**
     * Tests if the constraint is consistent
     * with respect to the current state of domains.
     *
     * @return true iff the constraint is bound consistent
     *         (weaker than arc consistent)
     */
    public boolean isConsistent() {
        return hasConsistentLowerBound();
    }

    /**
     * Computes the opposite of this constraint.
     *
     * @return a constraint with the opposite semantic  @param solver
     */
    public AbstractSConstraint opposite(Solver solver) {
        IntExp term = solver.scalar(coeffs, vars);
        return (AbstractSConstraint) solver.gt(term, -cste);
    }


    @Override
    protected String getOperator() {
        return " <= ";
    }
}