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

import choco.cp.solver.constraints.integer.intlincomb.*;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Implements a constraint Sigma (ai Xi) <=/>=/= C,
 * with Xi variables, ai and C constants.
 */
public final class IntLinComb extends AbstractLargeIntSConstraint {
    /**
     * Constant, to be assigned to <code>op</code>,
     * representing linear equalities.
     */
    public static final int EQ = 0;

    /**
     * Constant, to be assigned to <code>op</code>,
     * representing linear inequalities.
     */
    public static final int GEQ = 1;

    /**
     * Constant, to be assigned to <code>op</code>,
     * representing linear disequalities.
     */
    public static final int NEQ = 2;

    /**
     * Constant, to be assigned to <code>op</code>,
     * representing linear inequalities.
     * Only used vby BoolIntLinComb
     */
    public static final int LEQ = 3;

    /**
     * Field representing the number of variables
     * with positive coeffficients in the linear combination.
     */
    protected final int nbPosVars;

    /**
     * Filter based on the operator
     */
    protected final IntLinCombOp intlincomb;

    /**
     * Constructs the constraint with the specified variables and constant.
     * Use the Model.createIntLinComb API instead of this constructor.
     * This constructor assumes that there are no null coefficient
     * and that the positive coefficients come before the negative ones.
     *
     * @param lvars       the variables of the constraint
     * @param lcoeffs     the constant coefficients
     * @param nbPositive  number of positive coefficients
     * @param c           the constant value of the constraint (the value the linear
     *                    expression must equal)
     * @param linOperator the operator to use (equality, inequality...)
     */
    public IntLinComb(final IntDomainVar[] lvars, final int[] lcoeffs,
                      final int nbPositive, final int c, final int linOperator) {
        // create the appropriate data structure
        super(priority(lvars.length),lvars);
        this.nbPosVars = nbPositive;
        switch (linOperator) {
            case EQ:
                intlincomb = new IntLinCombEQ(lcoeffs, nbPositive, c, lvars, this);
                break;
            case NEQ:
                intlincomb = new IntLinCombNEQ(lcoeffs, nbPositive, c, lvars, this);
                break;
            case GEQ:
                intlincomb = new IntLinCombGEQ(lcoeffs, nbPositive, c, lvars, this);
                break;
            case LEQ:
                intlincomb = new IntLinCombLEQ(lcoeffs, nbPositive, c, lvars, this);
                break;
            default:
                intlincomb = null;
        }
    }

    private static int priority(int nbVars){
        switch (nbVars){
            case 0:
            case 1:
                return ConstraintEvent.UNARY;
            case 2:
                return ConstraintEvent.BINARY;
            case 3:
                return ConstraintEvent.TERNARY;
            default:
                return ConstraintEvent.LINEAR;
        }
    }


    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
    }

    /**
     * Launchs the filtering algorithm.
     *
     * @throws ContradictionException if a domain empties or a contradiction is
     *                                infered
     */
    public void propagate() throws ContradictionException {
        intlincomb.filter(true, 2);
    }


    /**
     * Propagation whenever the lower bound of a variable is modified.
     *
     * @param idx the index of the modified variable
     * @throws ContradictionException if a domain empties or a contradiction is
     *                                infered
     */
    public void awakeOnInf(final int idx) throws ContradictionException {
        if (idx < nbPosVars) {
            intlincomb.filter(true, 1);
        } else {
            intlincomb.filter(false, 1);
        }
    }

    /**
     * Propagation whenever the upper bound of a variable is modified.
     *
     * @param idx the index of the modified variable
     * @throws ContradictionException if a domain empties or a contradiction is
     *                                infered
     */
    public void awakeOnSup(final int idx) throws ContradictionException {
        if (idx < nbPosVars) {
            intlincomb.filter(false, 1);
        } else {
            intlincomb.filter(true, 1);
        }
    }

    /**
     * Propagation whenever a variable is instantiated.
     *
     * @param idx the index of the modified variable
     * @throws ContradictionException if a domain empties or a contradiction is
     *                                infered
     */
    public void awakeOnInst(final int idx) throws ContradictionException {
        propagate();
    }

    /**
     * Propagation whenever a value is removed from the variable domain.
     *
     * @param idx the index of the modified variable
     * @param x   the removed value
     * @throws ContradictionException if a domain empties or a contradiction is
     *                                infered
     */
    public void awakeOnRem(final int idx, final int x)
            throws ContradictionException {
    }

    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
    }


    /**
     * Checks if the constraint is satisfied when all variables are instantiated.
     *
     * @return true if the constraint is satisfied
     */
	public boolean isSatisfied(int[] tuple) {
		return intlincomb.isSatisfied(tuple);
	}

    /**
     * Get the opposite constraint
     *
     * @return the opposite constraint  @param solver
     */
    @Override
    public AbstractSConstraint<IntDomainVar> opposite(final Solver solver) {
        return intlincomb.opposite(solver);
    }

    @Override
    public String pretty() {
        return intlincomb.pretty();
    }
}
