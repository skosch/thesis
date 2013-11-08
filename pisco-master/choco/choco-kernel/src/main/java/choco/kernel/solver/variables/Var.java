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

import choco.IExtensionnable;
import choco.IPretty;
import choco.kernel.common.IIndex;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.structure.PartiallyStoredIntVector;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.event.VarEvent;

import java.util.logging.Logger;


/** History:
 * 2007-12-07 : FR_1873619 CPRU: DomOverDeg+DomOverWDeg
 * */

/**
 * Interface for all implementations of domain variables.
 */
public interface Var extends IPretty, IIndex, IExtensionnable {

	/**
	 * Reference to an object for logging trace statements related to IntDomainVar (using the java.util.logging package)
	 */
	public final static Logger LOGGER = ChocoLogging.getEngineLogger();

	public String getName();
	/**
	 * Returns the number of listeners involving the variable.
	 * @return the numbers of listeners involving the variable
	 */

	public int getNbConstraints();

	/**
	 * Returns the <code>i</code>th constraint. <code>i</code>
	 * should be more than or equal to 0, and less or equal to
	 * the number of constraint minus 1.
	 * @param i number of constraint to be returned
	 * @return the ith constraint
	 */

	public SConstraint getConstraint(int i);


	/**
	 * returns the index of the variable in it i-th constraint
	 *
	 * @param constraintIndex the index of the constraint (among all constraints linked to the variable)
	 * @return the index of the variable (0 if this is the first variable of that constraint)
	 */
	public int getVarIndex(int constraintIndex);


	/**
	 * access the data structure storing constraints involving a given variable
	 *
	 * @return the vector of constraints
	 */
	public PartiallyStoredVector<? extends SConstraint> getConstraintVector();

	/**
	 * access the data structure storing indices associated to constraints involving a given variable
	 *
	 * @return the vector of index
	 */
	public PartiallyStoredIntVector getIndexVector();

	/**
	 * <b>Public user API:</b>
	 * <i>Domains :</i> testing whether a variable is instantiated or not.
	 * @return a boolean giving if a variable is instanciated or not
	 */

	public boolean isInstantiated();

	/**
	 * Adds a new listener for the variable, that is a constraint which
	 * should be informed as soon as the variable domain is modified.
	 * The addition can be dynamic (undone upon backtracking) or not
	 * @param c the constraint to add
	 * @param varIdx index of the variable
	 * @param dynamicAddition dynamical addition or not
	 *  @return the number of the listener
	 */
	public int addConstraint(SConstraint c, int varIdx, boolean dynamicAddition);

	/**
	 * returns the object used by the propagation engine to model a propagation event associated to the variable
	 * (an update to its domain)
	 *
	 * @return the propagation event
	 */
	public VarEvent<? extends Var> getEvent();

	/**
	 * This methods should be used if one want to access the different constraints
	 * currently posted on this variable.
	 * <p/>
	 * Indeed, since indices are not always
	 * consecutive, it is the only simple way to achieve this.
	 *
	 * @return an iterator over all constraints involving this variable
	 */
	public DisposableIterator<SConstraint> getConstraintsIterator();

    /**
     * Return the priority of <code>this</code> according to the related constraints (minimum over all constraints priority)
     * @return
     */
    int getPriority();
}
