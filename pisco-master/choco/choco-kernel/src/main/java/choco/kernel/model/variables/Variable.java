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

package choco.kernel.model.variables;

import java.util.Iterator;
import java.util.logging.Logger;

import choco.IPretty;
import choco.kernel.common.IIndex;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.IConstraintList;
import choco.kernel.model.IFindManager;
import choco.kernel.model.IOptions;
import choco.kernel.model.IVariableArray;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintManager;
import choco.kernel.model.constraints.ExpressionManager;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 17 mars 2008
 * Time: 18:26:04
 * Interface for Model's variable.
 * Define all the methods for a variable to be used
 * on the Model.
 */
public interface Variable extends IConstraintList,IVariableArray, IPretty, IIndex, IFindManager, IOptions, IHook {

	final static Logger LOGGER = ChocoLogging.getEngineLogger();

	String getName();
	
	public VariableType getVariableType();

	@Deprecated
	public Iterator<Constraint> getConstraintIterator();
	//replaced by
	//public Iterator<Constraint> getConstraintIterator(Model m);


	@Deprecated
	public int getNbConstraint();
	//replaced by
	//public int getNbConstraint(Model m);

	VariableManager<?> getVariableManager();
	
	ExpressionManager getExpressionManager();
	
	ConstraintManager<?> getConstraintManager();
	
}


