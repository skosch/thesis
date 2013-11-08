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

import choco.cp.solver.constraints.integer.intlincomb.policy.CoeffPolicy;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 11 mars 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public abstract class IntLinCombOp {

    /**
	 * The coefficients of the linear equations.
	 * The positive coefficents should be the first ones.
	 */
	final int[] coeffs;

	/**
	 * Field representing the number of variables
	 * with positive coeffficients in the linear combination.
	 */
	private final int nbPosVars;

	/**
	 * The constant of the constraint.
	 */
	final int cste;

    final IntDomainVar[] vars;

    private final AbstractSConstraint constraint;

    final CoeffPolicy coeffPolicy;


    IntLinCombOp(final int[] coeffs, final int nbPosVars, final int cste, final IntDomainVar[] vars, final AbstractSConstraint constraint) {
        this.coeffs = coeffs;
        this.nbPosVars = nbPosVars;
        this.cste = cste;
        this.vars = vars;
        this.constraint = constraint;
        coeffPolicy = CoeffPolicy.build(vars, coeffs, nbPosVars, cste);
    }

/*    	*//**
	 * Initializes the constraint by copying the coefficent array.
	 * @param lcoeffs the coefficients of the linear equation
	 *//*
	public void init(final int[] lcoeffs) {
		this.coeffs = new int[lcoeffs.length];
		System.arraycopy(lcoeffs, 0, this.coeffs, 0, lcoeffs.length);
	}*/

    /**
	 * Checks if the constraint is entailed.
	 * @return Boolean.TRUE if the constraint is satisfied, Boolean.FALSE if it
	 * is violated, and null if the filtering algorithm cannot infer yet.
	 */
	public abstract Boolean isEntailed();

    /**
	 * Checks if the constraint is satisfied when all variables are instantiated.
	 * @param tuple array of values
     * @return true if the constraint is satisfied
	 */
	public abstract boolean isSatisfied(int[] tuple);

    public final int compute(int[] tuple){
        int s = cste;
		int nbVars = vars.length;
		int i;
		for (i = 0; i < nbVars; i++) {
			s += (tuple[i] * coeffs[i]);
		}
        return s;
    }

    /**
	 * Checks a new lower bound.
	 * @return true if filtering has been infered
	 * @throws choco.kernel.solver.ContradictionException if a domain empties or a contradiction is
	 * infered
	 */
    protected abstract boolean filterOnImprovedLowerBound()throws ContradictionException;

	/**
	 * Checks a new upper bound.
	 * @return true if filtering has been infered
	 * @throws ContradictionException if a domain empties or a contradiction is
	 * infered
	 */
	protected abstract boolean filterOnImprovedUpperBound() throws ContradictionException;

    /**
	 * Tests if the constraint is consistent
	 * with respect to the current state of domains.
	 * @return true iff the constraint is bound consistent
	 * (weaker than arc consistent)
	 */
	public abstract boolean isConsistent();

    /**
	 * Computes the opposite of this constraint.
	 * @param solver containing solver
     * @return a constraint with the opposite semantic  @param solver
	 */
	public abstract AbstractSConstraint opposite(Solver solver);

    // Note: additional propagation pass are sometimes useful:
	// For instance : 3*X[0.3] + Y[1.10] = 10
	//                Y >= 2 causes X < 3 -> updateSup(X,2)
	//                and this very var (the new sup of X) causes (Y >= 4).
	//                this induced var (Y>=4) could not be infered
	//                at first (with only Y>=2)
	//

	/**
	 * A strategy for chaotic iteration with two rules (LB and UB propagation).
	 * The fix point is reached individually for each rule in one function call
	 * but this call may break the stability condition for the other rule
	 * (in which case the second rule infers new information from the fresh
	 * inferences made by the first rule) .
	 * The algorithm oscilates between both rules until
	 * a global fix point is reached.
	 * @param startWithLB whether LB must be the first rule applied
	 * @param minNbRules  minimum number of rules required to reach fix point.
	 * @throws ContradictionException if a domain empties or a contradiction is
	 * infered
	 */
	public final void filter(final boolean startWithLB,
			final int minNbRules) throws ContradictionException {
		boolean lastRuleEffective = true;
		// whether the last rule indeed perform some reductions
		int nbr = 0;
		// number of rules applied
		boolean nextRuleIsLB = startWithLB;
		// whether the next rule that should be filtered is LB (or UB)
		while (lastRuleEffective || nbr < minNbRules) {
			if (nextRuleIsLB) {
				lastRuleEffective = filterOnImprovedLowerBound();
			} else {
				lastRuleEffective = filterOnImprovedUpperBound();
			}
			nextRuleIsLB ^= true; //!nextRuleIsLB;
			nbr++;
		}
		//////////////////////////////////////////////////////////////////////////////////
	}

    /**
	 * Propagates the constraint sigma(ai Xi) + c <= 0
	 * where mylb = sigma(ai inf(Xi)) + c.
	 * Note: this does not reach saturation (fix point),
	 * but returns a boolean indicating whether
	 * it infered new information or not.
	 * @param mylb the computed lower bound
	 * @return true if filtering has been infered
	 * @throws ContradictionException if a domain empties or a contradiction
	 * is infered
	 */
	final boolean propagateNewLowerBound(final int mylb)
	throws ContradictionException {
		boolean anyChange = false;
		int nbVars = vars.length;
		if (mylb > 0) {
			this.constraint.fail();
		}
		int i;
		for (i = 0; i < nbPosVars; i++) {
			int newSupi = coeffPolicy.getSupPV(i, mylb);//MathUtils.divFloor(-(mylb), coeffs[i]) + vars[i].getInf();
			if (vars[i].updateSup(newSupi, this.constraint, false)) {
				anyChange = true;
			}
		}
		for (i = nbPosVars; i < nbVars; i++) {
			int newInfi = coeffPolicy.getInfNV(i, mylb);//MathUtils.divCeil(mylb, -(coeffs[i])) + vars[i].getSup();
			if (vars[i].updateInf(newInfi, this.constraint, false)) {
				anyChange = true;
			}
		}
		return anyChange;
	}

	/**
	 * Propagates the constraint sigma(ai Xi) + c <= 0
	 * where myub = sigma(ai sup(Xi)) + c.
	 * Note: this does not reach saturation (fix point),
	 * but returns a boolean indicating whether
	 * it infered new information or not.
	 * @param myub the computed upper bound
	 * @return true if filtering has been infered
	 * @throws ContradictionException if a domain empties or a contradiction
	 * is infered
	 */
	final boolean propagateNewUpperBound(final int myub)
	throws ContradictionException {
		boolean anyChange = false;
		int nbVars = vars.length;
		if (myub < 0) {
			this.constraint.fail();
		}
		int i;
		for (i = 0; i < nbPosVars; i++) {
			int newInfi = coeffPolicy.getInfPV(i, myub);//MathUtils.divCeil(-(myub), coeffs[i]) + vars[i].getSup();
			if (vars[i].updateInf(newInfi, this.constraint, false)) {
				anyChange = true;
			}
		}
		for (i = nbPosVars; i < nbVars; i++) {
			int newSupi = coeffPolicy.getSupNV(i, myub);//MathUtils.divFloor(myub, -(coeffs[i])) + vars[i].getInf();
			if (vars[i].updateSup(newSupi, this.constraint, false)) {
				anyChange = true;
			}
		}
		return anyChange;
	}

	/**
	 * Tests if the constraint is consistent
	 * with respect to the current state of domains.
	 * @return true iff the constraint is bound consistent
	 * (weaker than arc consistent)
	 */
	final boolean hasConsistentLowerBound() {
		int lb = coeffPolicy.computeLowerBound();
		int nbVars = vars.length;

		if (lb > 0) {
			return false;
		} else {
			for (int i = 0; i < nbPosVars; i++) {
				int newSupi = MathUtils.divFloor(-(lb), coeffs[i]) + vars[i].getInf();
				if (vars[i].getSup() < newSupi) {
					return false;
				}
			}
			for (int i = nbPosVars; i < nbVars; i++) {
				int newInfi = MathUtils.divCeil(lb, -(coeffs[i])) + vars[i].getSup();
				if (vars[i].getInf() > newInfi) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Tests if the constraint is consistent
	 * with respect to the current state of domains.
	 * @return true iff the constraint is bound consistent
	 * (weaker than arc consistent)
	 */
	protected final boolean hasConsistentUpperBound() {
		int ub = coeffPolicy.computeUpperBound();
		int nbVars = vars.length;

		if (ub < 0) {
			return false;
		} else {
			for (int i = 0; i < nbPosVars; i++) {
				int newInfi = MathUtils.divCeil(-(ub), coeffs[i]) + vars[i].getSup();
				if (vars[i].getInf() > newInfi) {
					return false;
				}
			}
			for (int i = nbPosVars; i < nbVars; i++) {
				int newSupi = MathUtils.divFloor(ub, -(coeffs[i])) + vars[i].getInf();
				if (vars[i].getSup() < newSupi) {
					return false;
				}
			}
			return true;
		}
	}

    protected abstract String getOperator();

    /**
	 * Pretty print for this constraint. This method prints the complete
	 * equations.
	 * @return a strring representation of the constraint
	 */
	public String pretty() {
		StringBuilder linComb = new StringBuilder(16);
		for (int i = 0; i < coeffs.length - 1; i++) {
            linComb.append(coeffs[i]).append('*').append(vars[i]).append(" + ");
		}
        linComb.append(coeffs[coeffs.length - 1]).append('*').append(vars[coeffs.length - 1]);
        linComb.append(getOperator());
        linComb.append(-cste);
		return linComb.toString();
	}
}
