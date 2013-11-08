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

import choco.IExtensionnable;
import choco.IPretty;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.Var;

import java.util.logging.Logger;

public interface SConstraint<V extends Var> extends Cloneable,IPretty, IExtensionnable {


	/**
	 * Reference to an object for logging trace statements related to constraints over integers (using the java.util.logging package)
	 */
	public final static Logger LOGGER = ChocoLogging.getEngineLogger();


	/**
	 * <i>Network management:</i>
	 * Get the number of variables involved in the constraint.
     * @return number of variables involved in the constraint
     */

	int getNbVars();

	/**
	 * <i>Network management:</i>
	 * Accessing the ith variable of a constraint.
	 *
	 * @param i index of the variable in the constraint
     * @return the i^th variable involved in the constraint
	 */

	V getVar(int i);

	/**
	 * <i>Network management:</i>
	 * Accessing the ith variable of a constraint.
	 *
	 * @param i index of the variable in the constraint
     * @return the i^th variable involved in the constraint
	 */

	V getVarQuick(int i);
	
	/**
	 * <i>Network management:</i>
	 * Setting (or overwriting)  the ith variable of a constraint.
	 *
	 * @param i index of the variable in the constraint
	 * @param v the variable (may be an IntDomainVar, SetVar, RealVar, ...
	 */
	void setVar(int i, V v);

	/**
	 * <i>Semantic:</i>
	 * Testing if the constraint is satisfied.
	 * Note that all variables involved in the constraint must be
	 * instantiated when this method is called.
     * @return true if the constraint is satisfied
     */

	boolean isSatisfied();

	/**
	 * computes the constraint modelling the counter-opposite condition of this
	 *
	 * @param solver the current solver
     * @return a new constraint (modelling the opposite condition)  @param solver
	 */
	AbstractSConstraint<V> opposite(Solver solver);

	/**
	 * <i>Network management:</i>
	 * Storing that among all listeners linked to the i-th variable of c,
	 * this (the current constraint) is found at index idx.
	 *
	 * @param i   index of the variable in the constraint
	 * @param idx index of the constraint in the among all listeners linked to that variable
	 */

	void setConstraintIndex(int i, int idx);

	/**
	 * <i>Network management:</i>
	 * Among all listeners linked to the idx-th variable of c,
	 * find the index of constraint c.
	 *
	 * @param idx index of the variable in the constraint
     * @return  index of the constraint within the variable network
	 */

	int getConstraintIdx(int idx);


    /**
     * Return the type of constraint, ie the type of variable involved in the constraint
     * @return
     */
    public SConstraintType getConstraintType();
    
	/**
	 * Some global constraint might be able to provide
	 * some fine grained information about the "real" degree of a variables.
	 * For example the global constraint on clauses can give the real number of
	 * clauses on each variable
	 *
	 * @param idx index of the variable in the constraint
	 * @return a weight given to the variable by the constraint
	 */
	int getFineDegree(int idx);

}
