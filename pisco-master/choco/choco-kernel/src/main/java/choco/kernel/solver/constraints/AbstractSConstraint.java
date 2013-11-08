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
import choco.kernel.solver.SolverException;
import choco.kernel.solver.branch.Extension;
import choco.kernel.solver.propagation.Propagator;
import choco.kernel.solver.variables.Var;

import java.util.Arrays;
import java.util.HashMap;
/** History:
 * 2007-12-07 : FR_1873619 CPRU: DomOverDeg+DomOverWDeg
 * */

/**
 * An abstract class for all implementations of listeners
 */
public abstract class AbstractSConstraint<V extends Var> extends Propagator implements SConstraint<V> {

	//TODO provide fast disposable iterators over variables(/indices/couples ?)
	//TODO provide separate iterator for unary,binary,ternary constraints ?
	//TODO set final status
	/**
	 * The list of variables of the constraint.
	 */

	protected V[] vars;


	/**
	 * The list, containing, for each variable, the index of the constraint among all
	 * its incident listeners.
	 */

	public int[] cIndices;

	/**
	 * Return the type of constraint.
	 * Can be INTEGER, SET, REAL, MIXED
	 */
	protected SConstraintType constraintType;


	/**
	 * The number of extensions registered to this class
	 */
	private static int ABSTRACTSCONSTRAINT_EXTENSIONS_NB = 0;

	/**
	 * The set of registered extensions (in order to deliver one and only one index for each extension !)
	 */
	private static final HashMap<String, Integer> REGISTERED_ABSTRACTSCONSTRAINT_EXTENSIONS = new HashMap<String, Integer>();


	/**
	 * Returns a new number of extension registration
	 *
	 * @param name A name for the extension (should be an UID, like the anbsolute path for instance)
	 * @return a number that can be used for specifying an extension (setExtension method)
	 */
	public static int getAbstractSConstraintExtensionNumber(String name) {
		Integer index = REGISTERED_ABSTRACTSCONSTRAINT_EXTENSIONS.get(name);
		if (index == null) {
			index = ABSTRACTSCONSTRAINT_EXTENSIONS_NB++;
			REGISTERED_ABSTRACTSCONSTRAINT_EXTENSIONS.put(name, index);
		}
		return index;
	}

	/**
	 * The extensions of this constraint, in order to add some data linked to this constraint (for specific algorithms)
	 */
	public Extension[] extensions = new Extension[1];

	/**
	 * Constructs a constraint with the priority 0.
	 * @param vars variables involved in the constraint
	 */

	protected AbstractSConstraint(V[] vars) {
		super();
		this.vars = vars;
		cIndices = new int[vars.length];
	}

	/**
	 * Constructs a constraint with the specified priority.
	 *
	 * @param priority The wished priority.
	 * @param vars variables involved in the constraint
	 */

	protected AbstractSConstraint(int priority, V[] vars) {
		super(priority);
		this.vars = vars;
		cIndices = new int[vars.length];
	}

	private boolean rangeCheck(int i) {
		return i >= 0 && i < vars.length;
	}
	/**
	 * Adds a new extension.
	 *
	 * @param extensionNumber should use the number returned by getAbstractSConstraintExtensionNumber
	 */
	public void addExtension(int extensionNumber) {
		if (extensionNumber > extensions.length) {
			Extension[] newArray = new Extension[extensions.length * 2];
			System.arraycopy(extensions, 0, newArray, 0, extensions.length);
			extensions = newArray;
		}
		extensions[extensionNumber] = new Extension();
	}

	/**
	 * Returns the queried extension
	 *
	 * @param extensionNumber should use the number returned by getAbstractSConstraintExtensionNumber
	 * @return the queried extension
	 */
	public final Extension getExtension(int extensionNumber) {
		return extensions[extensionNumber];
	}

	public final void setExtension(Extension ext, int extensionNumber){
		if (extensionNumber > extensions.length) {
			Extension[] newArray = new Extension[extensions.length * 2];
			System.arraycopy(extensions, 0, newArray, 0, extensions.length);
			extensions = newArray;
		}
		extensions[extensionNumber] = ext;
	}


	/**
	 * Indicates if the constraint is entailed, from now on will be always satisfied
	 *
	 * @return wether the constraint is entailed
	 */
	@Override
	public Boolean isEntailed() {
		if (isCompletelyInstantiated()) {
			return isSatisfied();
		} else {
			return null;
		}

	}

	/**
	 * This function connects a constraint with its variables in several ways.
	 * Note that it may only be called once the constraint
	 * has been fully created and is being posted to a model.
	 * Note that it should be called only once per constraint.
	 * This can be a dynamic addition (undone upon backtracking) or not
	 *
	 * @param dynamicAddition if the addition should be dynamical
	 */

	public void addListener(boolean dynamicAddition) {
		final int n = getNbVars();
		for (int i = 0; i < n; i++) {
			setConstraintIndex(i, getVar(i).addConstraint(this, i, dynamicAddition));
			getVar(i).getEvent().addPropagatedEvents(getFilteredEventMask(i));
		}
	}

	/**
	 * Let <i>v</i> be the <i>i</i>-th var of <i>c</i>, records that <i>c</i> is the
	 * <i>n</i>-th constraint involving <i>v</i>.
	 */
	public final void setConstraintIndex(int i, int val) {
		if (rangeCheck(i)) {
			cIndices[i] = val;
		} else {
			throw new SolverException("bug in setConstraintIndex i:" + i + " this: " + this);
		}
	}


	/**
	 * Returns the index of the constraint in the specified variable.
	 */

	public final int getConstraintIdx(int i) {
		return rangeCheck(i) ? cIndices[i] : -1;
	}


	/**
	 * Checks wether all the variables are instantiated.
	 */

	@Override
	public final boolean isCompletelyInstantiated() {
		final int nVariables = vars.length;
		for (int i = 0; i < nVariables; i++) {
			if (!(vars[i].isInstantiated())) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Returns the number of variables.
	 */

	public final int getNbVars() {
		return vars.length;
	}


	/**
	 * Returns the <code>i</code>th variable.
	 */

	public final V getVar(int i) {
		return rangeCheck(i) ? vars[i] : null;
	}

	@Override
	public final V getVarQuick(int i) {
		return vars[i];
	}

	public final void setVar(int i, V v) {
		if (rangeCheck(i)) {
			this.vars[i] = v;
		} else {
			throw new SolverException("BUG in CSP network management: too large index for setVar");
		}
	}

	/**
	 * Get the opposite constraint
	 *
	 * @return the opposite constraint  @param solver
	 */
	public AbstractSConstraint<V> opposite(Solver solver) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Clone the constraint
	 *
	 * @return the clone of the constraint
	 * @throws CloneNotSupportedException Clone not supported exception
	 */
	public Object clone() throws CloneNotSupportedException {
		AbstractSConstraint newc = (AbstractSConstraint) super.clone();
		newc.vars = Arrays.copyOf(vars, vars.length);
		newc.cIndices = Arrays.copyOf(cIndices, cIndices.length);
		return newc;

	}

	/**
	 * CPRU 07/12/2007: DomOverWDeg implementation
	 * This method returns the number of variables not already instanciated
	 *
	 * @return the number of failure
	 */
	public final int getNbVarNotInst() {
		int notInst = 0;
		final int nbVars = this.getNbVars();
		for (int i = 0; i < nbVars; i++) {
			if ( ! vars[i].isInstantiated()) {
				notInst++;
			}
		}
		return notInst;
	}    

	@Override
	public String toString() {
		return pretty();
	}

	public String pretty() {
		return getClass().getSimpleName()+Arrays.toString(vars);
	}


	/**
	 * Some global constraint might be able to provide
	 * some fine grained information about the "real" degree of a variables.
	 * For example the global constraint on clauses can give the real number of
	 * clauses on each variable
	 *
	 * @param idx index of the variable in the constraint
	 * @return a weight given to the variable by the constraint
	 */
	public int getFineDegree(int idx) {
		return 1;
	}



}
