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

package choco.kernel.solver.constraints;

import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.Extension;
import choco.kernel.solver.variables.Var;

/**
 * a class that is used to represent a syntatic formula involving unknowns.
 * It is not a propagator (formulas have no behaviors, no semantic)
 * By defaut, an AbstractModeler creates formulas instead of constraints
 */
public class Formula implements SConstraint {

	/** possible static values for the constraintOperator field */
	public final static int EQUAL_XC = 0;
	public final static int NOT_EQUAL_XC = 1;
	public final static int GREATER_OR_EQUAL_XC = 2;
	public final static int LESS_OR_EQUAL_XC = 3;

	public final static int EQUAL_XYC = 4;
	public final static int NOT_EQUAL_XYC = 5;
	public final static int GREATER_OR_EQUAL_XYC = 6;
	public final static int TIMES_XYZ = 12;

	public final static int INT_LIN_COMB = 7;
	public final static int OCCURRENCE = 8;
	public final static int ALL_DIFFERENT = 9;
	public final static int GLOBAL_CARDINALITY = 10;
	public final static int NTH = 11;

	/**
	 * this slots characterizes the type of formula being stored (the predicate/relation/operator)
	 */
	public int constraintOperator;

	/**
	 * storing the variables (IntVar, SetVar, ...) involved in the constraint
	 */
	public Var[] variables;

	/**
	 * storing the parameters of the constraint
	 */
	public Object[] parameters;

	public Formula(Var v0, int c, int cop) {
		variables = new Var[]{v0};
		parameters = new Object[]{c};
		constraintOperator = cop;
	}

	public Formula(Var v0,  Var v1, int c, int cop) {
		variables = new Var[]{v0, v1};
		parameters = new Object[]{c};
		constraintOperator = cop;
	}

	public Formula(Var v0,  Var v1, Var v2, int cop) {
		variables = new Var[]{v0, v1, v2};
		parameters = new Object[]{};
		constraintOperator = cop;
	}

	public Formula(Var[] vars, int[] coeffs, int c1, int c2, int cop) {
		variables = vars;
		parameters = new Object[]{coeffs, c1, c2};
		constraintOperator = cop;
	}

	public Formula(Var[] vars, int[] coeffs, int c1, int c2, int c3, int cop) {
		variables = vars;
		parameters = new Object[]{coeffs, c1, c2, c3};
		constraintOperator = cop;
	}

	public int getNbVars() {
		return variables.length;
	}

	public Var getVar(int i) {
		return variables[i];
	}

	public Var getVarQuick(int i) {
		return variables[i];
	}

	public void setVar(int i, Var v) {
		variables[i] = v;
	}

	public boolean isSatisfied() {
		throw new UnsupportedOperationException();
	}

	public AbstractSConstraint opposite(Solver solver) {
		throw new UnsupportedOperationException();
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public int getConstraintOperator() {
		return constraintOperator;
	}

	public void setConstraintIndex(int i, int idx) {
	}

	public int getConstraintIdx(int idx) {
		throw new UnsupportedOperationException();
	}

	public String pretty() {
		return null;
	}

	/**
	 * Return the type of constraint, ie the type of variable involved in the constraint
	 *
	 * @return
	 */
	@Override
	public SConstraintType getConstraintType() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Extension getExtension(int extensionNumber) {
		return null;
	}

    /**
     * Adds a new extension.
     *
     * @param extensionNumber should use the number returned by getAbstractSConstraintExtensionNumber
     */
    @Override
    public void addExtension(final int extensionNumber) {}

	@Override
	public int getFineDegree(int idx) {
		return 1;
	}
    
    
}
