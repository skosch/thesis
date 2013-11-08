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

package choco.cp.common.util.preprocessor;

import choco.cp.model.CPModel;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.common.util.objects.DeterministicIndicedList;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.IHook;
import choco.kernel.model.variables.Variable;
import gnu.trove.THashMap;
import gnu.trove.TLongObjectHashMap;

import java.util.logging.Level;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 1 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 *
 * An abstract class to set the methods of a detector.
 * A detector analyzes a model and thanks to its analizis, it is allowed to strongly modified the model involved. 
 */
public abstract class AbstractAdvancedDetector extends AbstractDetector{

	/**
	 * Internal structure to store constraint addition instructions
	 */
	private final DeterministicIndicedList<Constraint> constraintsToAdd;

	/**
	 * Internal structure to store constraint deletion instructions
	 */
	private final DeterministicIndicedList<Constraint> constraintsToDelete;

	/**
	 * Internal structure to store variable addition instructions
	 */
	private final DeterministicIndicedList<Variable> variablesToAdd;

	/**
	 * Internal structure to store variable deletion instructions
	 */
	private final DeterministicIndicedList<Variable> variablesToDelete;

	/**
	 * Internal structure to store variable deletion instructions
	 */
	private final THashMap<Variable, Variable> variablesToReplace;

	protected AbstractAdvancedDetector(CPModel model) {
		super(model);
		constraintsToAdd = new DeterministicIndicedList<Constraint>(Constraint.class);
		constraintsToDelete = new DeterministicIndicedList<Constraint>(Constraint.class);
		variablesToAdd = new DeterministicIndicedList<Variable>(Variable.class);
		variablesToDelete = new DeterministicIndicedList<Variable>(Variable.class);
		variablesToReplace = new THashMap<Variable, Variable>();
	}

	/**
	 * Apply the detection defined within the detector.
	 */
	public abstract void apply();

	/**
	 * Add a constraint {@code c} to the model which is currently treated by the detector.<br/>
	 * The addition is recorded but not done immediatly. It must be "commited" using {@link AbstractAdvancedDetector#commit()}.
	 * @param c contraint to add
	 */
	protected final void add(Constraint c){
		constraintsToAdd.add(c);
	}

	/**
	 * Delete a constraint {@code c} to the model which is currently treated by the detector.<br/>
	 * The deletion is recorded but not done immediatly. It must be "commited" using {@link AbstractAdvancedDetector#commit()}.
	 * @param c contraint to delete
	 */
	protected final void delete(Constraint c){
		constraintsToDelete.add(c);
	}

	/**
	 * Remove deletion instruction on {@code c}.
	 * @param c contraint to keep
	 */
	protected final void keep(Constraint c){
		constraintsToDelete.remove(c);
	}

	/**
	 * Remove addition instruction on {@code c}.
	 * @param c contraint to not add
	 */
	protected final void forget(Constraint c){
		constraintsToAdd.remove(c);
	}

	/**
	 * Add a variable {@code v} to the model which is currently treated by the detector.<br/>
	 * The addition is recorded but not done immediatly. It must be "commited" using {@link AbstractAdvancedDetector#commit()}.
	 * @param v variable to add
	 */
	protected final void add(Variable v){
		variablesToAdd.add(v);
	}

	/**
	 * Delete a variable {@code v} from the model which is currently treated by the detector.<br/>
	 * The deletion is recorded but not done immediatly. It must be "commited" using {@link AbstractAdvancedDetector#commit()}.
	 * @param v variable to delete
	 */
	protected final void delete(Variable v){
		variablesToDelete.add(v);
	}

	/**
	 * Remove deletion instruction on {@code v}.
	 * @param v variable to keep
	 */
	protected final void keep(Variable v){
		variablesToDelete.remove(v);
	}

	/**
	 * Remove addition instruction on {@code v}.
	 * @param v contraint to not add
	 */
	protected final void forget(Variable v){
		variablesToAdd.remove(v);
	}

	/**
	 * Replace {@code outVar} by {@code inVar} in every constraint where {@code outVar} is involved.
	 * @param outVar deleted variable
	 * @param inVar the substitute
	 */
	protected final void replaceBy(Variable outVar, Variable inVar){
		variablesToReplace.put(outVar, inVar);
	}

	/**
	 * Send changes detected to the treated model.
	 */
	public final void commit(){
		DisposableIterator<Variable> iterV = variablesToAdd.iterator();
		while(iterV.hasNext()) {
			final Variable v = iterV.next();
			model.addVariables(v);
			if(LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.config(String.format("..add variable : %s", v.pretty()));
			}
		}
		iterV.dispose();

		DisposableIterator<Constraint> iterC = constraintsToAdd.iterator();
		while(iterC.hasNext()) {
			final Constraint c = iterC.next();
			model.addConstraint(c);
			if(LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.config(String.format("..add constraint : %s", c.pretty()));
			}
		}
		iterC.dispose();

		iterC = constraintsToDelete.iterator();
		while(iterC.hasNext()) {
			final Constraint c = iterC.next();
			model.removeConstraint(c);
			if(LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.config(String.format("..delete constraint : %s", c.pretty()));
			}
		}
		iterC.dispose();

		for(Variable outVar : variablesToReplace.keySet()){
			final Variable inVar = variablesToReplace.get(outVar);
			for(Constraint c : outVar.getConstraints()){
				c.replaceBy(outVar, inVar);
				if(!inVar._contains(c)){
					inVar._addConstraint(c);
				}
				outVar._removeConstraint(c);
			}
			if( inVar.getHook() == IHook.NO_HOOK) {
				inVar.setHook(outVar.getHook());
			}
			if(LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.config(String.format("..%s replaced by : %s", outVar.getName(), inVar.getName()));
			}
		}
		iterV = variablesToDelete.iterator();
		while(iterV.hasNext()) {
			final Variable v = iterV.next();
			model.removeVariable(v);
			if(LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.config(String.format("..delete variable : %s", v.pretty()));
			}
		} 
		iterV.dispose();
	}

	/**
	 * Remove all uncommited instructions.
	 */
	public final void rollback(){
		constraintsToAdd.clear();
		constraintsToDelete.clear();
		variablesToAdd.clear();
		variablesToDelete.clear();
		variablesToReplace.clear();
	}
}
