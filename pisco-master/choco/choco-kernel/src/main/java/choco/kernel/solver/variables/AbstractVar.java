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

package choco.kernel.solver.variables;

import choco.kernel.common.HashCoding;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.structure.APartiallyStoredCstrList;
import choco.kernel.memory.structure.PartiallyStoredIntVector;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.Extension;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.propagation.event.VarEvent;

import java.util.HashMap;
/**
 * An abstract class for all implementations of domain variables.
 */
public abstract class AbstractVar implements Var {

	protected PropagationEngine propagationEngine;

	/**
	 * A name may be associated to each variable.
	 */
	protected String name;


	private long index;

	/**
	 * The variable var associated to this variable.
	 */
	protected VarEvent<? extends Var> event;


	/**
	 * The list of constraints (listeners) observing the variable.
	 */
	protected APartiallyStoredCstrList<? extends SConstraint> constraints;

	/**
	 * The number of extensions registered to this class
	 */
	private static int ABSTRACTVAR_EXTENSIONS_NB = 0;

	/**
	 * The set of registered extensions (in order to deliver one and only one index for each extension !)
	 */
	private static final HashMap<String, Integer> REGISTERED_ABSTRACTVAR_EXTENSIONS = new HashMap<String, Integer>();

	/**
	 * Returns a new number of extension registration
	 * @param name A name for the extension (should be an UID, like the absolute path for instance)
	 * @return a number that can be used for specifying an extension (setExtension method)
	 */
	public static int getAbstractVarExtensionNumber(String name) {
		Integer ind = REGISTERED_ABSTRACTVAR_EXTENSIONS.get(name);
		if (ind == null) {
			ind = ABSTRACTVAR_EXTENSIONS_NB++;
			REGISTERED_ABSTRACTVAR_EXTENSIONS.put(name, ind);
		}
		return ind;
	}

	/**
	 * The extensions of this constraint, in order to add some data linked to this constraint (for specific algorithms)
	 */
	private Extension[] extensions;


	public String getName() {
		return name;
	}


	/**
	 * Initializes a new variable.
	 * @param solver The model this variable belongs to
	 * @param name The name of the variable
	 * @param constraints constraints stored specific structure
	 */
	public AbstractVar(final Solver solver, final String name,
			final APartiallyStoredCstrList<? extends SConstraint> constraints) {
		this.propagationEngine = solver.getPropagationEngine();
		this.name = name;
		this.constraints = constraints;
		index = solver.getIndexfactory().getIndex();
	}



	@Override
	public int hashCode() {
		return HashCoding.hashCodeMe(new Object[]{index});
	}

	/**
	 * Unique index
	 * (Different from hashCode, can change from one execution to another one)
	 *
	 * @return the indice of the objet
	 */
	@Override
	public final long getIndex() {
		return index;
	}

	public final int getPriority() {
		return constraints.getPriority();
	}

    /**
	 * Adds a new extension.
	 * @param extensionNumber should use the number returned by getAbstractVarExtensionNumber
     */
	public void addExtension(int extensionNumber) {
		if(extensions == null) {
			extensions = new Extension[extensionNumber+1];
		}else if (extensionNumber >= extensions.length) {
			Extension[] newArray = new Extension[extensions.length * 2];
			System.arraycopy(extensions, 0, newArray, 0, extensions.length);
			extensions = newArray;
		}
		extensions[extensionNumber] = new Extension();
	}

 	/**
	 * Adds a new extension with an initial value.
	 * @param extensionNumber should use the number returned by getAbstractVarExtensionNumber
     */
	public void addExtension(int extensionNumber, int value) {
		addExtension(extensionNumber);
		extensions[extensionNumber].set(value);
	}

	/**
	 * Returns the queried extension
	 * @param extensionNumber should use the number returned by getAbstractVarExtensionNumber
	 * @return the queried extension
	 */
	@Override
	public Extension getExtension(int extensionNumber) {
		return extensions[extensionNumber];
	}

	/**
	 * Useful for debugging.
	 * @return the name of the variable
	 */
	public String toString() {
		return name;
	}

	/**
	 * Returns the variable event.
	 * @return the event responsible for propagating variable modifications
	 */
	public VarEvent<? extends Var> getEvent() {
		return event;
	}


	/**
	 * Retrieve the constraint i involving the variable.
	 * Be careful to use the correct constraint index (constraints are not
	 * numbered from 0 to number of constraints minus one, since an offset
	 * is used for some of the constraints).
	 * @param i the number of the required constraint
	 * @return the constraint number i according to the variable
	 */
	public SConstraint getConstraint(final int i) {
		return constraints.getConstraint(i);
	}


	/**
	 * Returns the number of constraints involving the variable.
	 * @return the number of constraints containing this variable
	 */
	public int getNbConstraints() {
		return constraints.getNbConstraints();
	}

	/**
	 * Access the data structure storing constraints involving a given variable.
	 * @return the backtrackable structure containing the constraints
	 */
	public PartiallyStoredVector<? extends SConstraint> getConstraintVector() {
		return constraints.getConstraintVector();
	}

	/**
	 * Access the data structure storing indices associated to constraints 
	 * involving a given variable.
	 * @return the indices associated to this variable in each constraint
	 */
	public PartiallyStoredIntVector getIndexVector() {
		return constraints.getIndexVector();
	}

	/**
	 * Returns the index of the variable in its constraint i.
	 * @param constraintIndex the index of the constraint 
	 * (among all constraints linked to the variable)
	 * @return the index of the variable
	 */
	public int getVarIndex(final int constraintIndex) {
		return constraints.getConstraintIndex(constraintIndex);
	}

	/**
	 * Removes (permanently) a constraint from the list of constraints 
	 * connected to the variable.
	 * @param c the constraint that should be removed from the list this variable
	 * maintains.
	 */
	public void eraseConstraint(final SConstraint c) {
		constraints.eraseConstraint(c);
	}

	// ============================================
	// Managing Listeners.
	// ============================================

	/**
	 * Adds a new constraints on the stack of constraints
	 * the addition can be dynamic (undone upon backtracking) or not.
	 * @param c the constraint to add
	 * @param varIdx the variable index accrding to the added constraint
	 * @param dynamicAddition states if the addition is definitic (cut) or
	 * subject to backtracking (standard constraint)
	 * @return the index affected to the constraint according to this variable
	 */
	public int addConstraint(final SConstraint c, final int varIdx,
			final boolean dynamicAddition) {
		return constraints.addConstraint(c, varIdx, dynamicAddition);
	}

	/**
	 * This methods should be used if one want to access the different constraints
	 * currently posted on this variable.
	 *
	 * Indeed, since indices are not always
	 * consecutive, it is the only simple way to achieve this.
	 *
	 * Warning ! this iterator should not be used to remove elements.
	 * The <code>remove</code> method throws an
	 * <code>UnsupportedOperationException</code>.
	 *
	 * @return an iterator over all constraints involving this variable
	 */
	public DisposableIterator<SConstraint> getConstraintsIterator() {
		return constraints.getConstraintsIterator();

	}
}
