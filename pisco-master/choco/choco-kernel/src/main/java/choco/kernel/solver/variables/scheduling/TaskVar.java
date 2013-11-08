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

/**
 * 
 */
package choco.kernel.solver.variables.scheduling;

import java.util.HashMap;

import choco.kernel.common.IIndex;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.structure.APartiallyStoredCstrList;
import choco.kernel.memory.structure.Couple;
import choco.kernel.memory.structure.PartiallyStoredIntVector;
import choco.kernel.memory.structure.PartiallyStoredTaskCstrList;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.model.variables.scheduling.ITaskVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.Extension;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.global.scheduling.AbstractTaskSConstraint;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.propagation.event.TaskVarEvent;
import choco.kernel.solver.propagation.event.VarEvent;
import choco.kernel.solver.propagation.listener.TaskPropagator;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * @author Arnaud Malapert</br> 
 * @since 23 janv. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public final class TaskVar<C extends AbstractSConstraint & TaskPropagator> extends AbstractTask implements Var, ITaskVariable<IntDomainVar>, IIndex {

	private final int id;
	
	private final String name;
	
	protected final IntDomainVar start;

	protected final IntDomainVar end;

	protected final IntDomainVar duration;

	private long index;
	/**
	 * The list of constraints (listeners) observing the variable.
	 */
	protected APartiallyStoredCstrList<C> constraints;

	protected final VarEvent<? extends Var> event;

	private final PropagationEngine propagationEngine;

	/**
	 * The number of extensions registered to this class
	 */
	private static int TASKVAR_EXTENSIONS_NB = 0;
	
	/**
	 * The set of registered extensions (in order to deliver one and only one index for each extension !)
	 */
	private static final HashMap<String, Integer> REGISTERED_TASKVAR_EXTENSIONS = new HashMap<String, Integer>();

    /**
	 * Returns a new number of extension registration
	 * @param name A name for the extension (should be an UID, like the absolute path for instance)
	 * @return a number that can be used for specifying an extension (setExtension method)
	 */
	public static int getTaskVarExtensionNumber(String name) {
		Integer ind = REGISTERED_TASKVAR_EXTENSIONS.get(name);
		if (ind == null) {
			ind = TASKVAR_EXTENSIONS_NB++;
			REGISTERED_TASKVAR_EXTENSIONS.put(name, ind);
		}
		return ind;
	}
	
	/**
	 * The extensions of this constraint, in order to add some data linked to this constraint (for specific algorithms)
	 */
	private Extension[] extensions;
	
	/**
	 * Initializes a new variable.
	 * @param solver The model this variable belongs to
	 * @param name The name of the variable
	 */
	public TaskVar(final Solver solver, final int id, final String name, final IntDomainVar start, final IntDomainVar end, final IntDomainVar duration) {
		super();
		this.id = id;
		this.name = name;
		this.start = start;
		this.end = end;
		this.duration = duration;
		constraints = new PartiallyStoredTaskCstrList<C>(solver.getEnvironment());
		index = solver.getIndexfactory().getIndex();
		this.event = new TaskVarEvent<C>(this);
		this.propagationEngine = solver.getPropagationEngine();
	}

	

	@Override
	public final boolean isPreemptionAllowed() {
		return false;
	}



	@Override
	public final int getID() {
		return id;
	}

	@Override
	public final String getName() {
		return name;
	}


	/**
	 * Unique index of an object in the master object
	 * (Different from hashCode, can change from one execution to another one)
	 *
	 * @return
	 */
	@Override
	public long getIndex() {
		return index;
	}

    @Override
    public int getPriority() {
        return constraints.getPriority();
    }


    //*****************************************************************//
	//*******************  TaskVariable  ********************************//
	//***************************************************************//

	public final IntDomainVar start() {
		return start;
	}

	public final IntDomainVar end() {
		return end;
	}

	public final IntDomainVar duration() {
		return duration;
	}

	//*****************************************************************//
	//*******************  ITask  ********************************//
	//***************************************************************//




	public int getECT() {
		return end.getInf();
	}

	public int getEST() {
		return start.getInf();
	}

	public int getLCT() {
		return end.getSup();
	}

	public int getLST() {
		return start.getSup();
	}

	public int getMaxDuration() {
		return duration.getSup();
	}

	public int getMinDuration() {
		return duration.getInf();
	}

	public boolean hasConstantDuration() {
		return duration.isInstantiated();
	}

	@Override
	public boolean isScheduled() {
		return isInstantiated();
	}


	//*****************************************************************//
	//*******************  Var  ********************************//
	//***************************************************************//
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
	public PartiallyStoredVector<C> getConstraintVector() {
		return constraints.getConstraintVector();
	}

	/**
	 * Access the data structure storing indices associated to constraints 
	 * involving a given variable.
	 * @return the indices associated to this variable in each constraint
	 */
	public PartiallyStoredIntVector getIndexVector() {
		return null;
	}

	/**
	 * Returns the index of the variable in its constraint i.
	 * @param constraintIndex the index of the constraint 
	 * (among all constraints linked to the variable)
	 * @return the index of the variable
	 */
	public int getVarIndex(final int constraintIndex) {
		return -1;
	}

	/**
	 * Removes (permanently) a constraint from the list of constraints 
	 * connected to the variable.
	 * @param c the constraint that should be removed from the list this variable
	 * maintains.
	 */
	public void eraseConstraint(final SConstraint<? extends Var> c) {
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

	@SuppressWarnings({"unchecked"})
	public final DisposableIterator<Couple<C>> getActiveConstraints(C cstrCause){
		return ((PartiallyStoredTaskCstrList)constraints).getActiveConstraint(cstrCause);
	}

	@Override
	public boolean isInstantiated() {
		return  start.isInstantiated() && end.isInstantiated() && duration.isInstantiated();
	}

	public final void updateCompulsoryPart(SConstraint cause) throws ContradictionException {
		boolean fixPoint;
		do {
			fixPoint = false;
			fixPoint |= start.updateInf(end.getInf() - duration.getSup(), cause, false);
			fixPoint |= start.updateSup(end.getSup() - duration.getInf(), cause, false);
			fixPoint |= end.updateInf(start.getInf() + duration.getInf(), cause, false);
			fixPoint |= end.updateSup(start.getSup() + duration.getSup(), cause, false);
			fixPoint |= duration.updateInf(end.getInf() - start.getSup(), cause, false);
			fixPoint |= duration.updateSup(end.getSup() - start.getInf(), cause, false);
		}while (fixPoint);
	}


	/**
	 * Call awake on TaskVar.
	 * @param idx index of the constraint calling #awake().
	 * @param constraint
	 * @param forceAwake
	 */
	public void updateHypotheticalDomain(int idx, final SConstraint constraint, final boolean forceAwake){
		propagationEngine.postEvent(this, TaskVarEvent.HYPDOMMOD, constraint, forceAwake);
	}


	/**
	 * Returns the queried extension
	 * @param extensionNumber should use the number returned by getTaskVarExtensionNumber
	 * @return the queried extension
	 */
	@Override
	public Extension getExtension(int extensionNumber) {
		return extensions[extensionNumber];
	}

	/**
	 * Adds a new extension value.
	 * @param extensionNumber should use the number returned by getTaskVarExtensionNumber
     */
	@Override
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
	 * @param extensionNumber should use the number returned by getTaskVarExtensionNumber
     */
		public void addExtension(int extensionNumber, int value) {
			addExtension(extensionNumber);
			extensions[extensionNumber].set(value);
		}


    public final boolean detectOrPostConsistencyConstraint(Solver solver) {
		final DisposableIterator<SConstraint> iter = getConstraintsIterator();
		while(iter.hasNext()) {
			final SConstraint<?> c = iter.next();
			if (c instanceof AbstractTaskSConstraint &&
					( (AbstractTaskSConstraint) c).isTaskConsistencyEnforced() ) {
				return true;				
			}
		}
		iter.dispose();
		postConsistencyConstraint(solver);
		return false;
	}

	public final void postConsistencyConstraint(Solver solver) {
		// we must enforce the task consistency
		if (duration().isInstantiatedTo(0) && // nil duration
				! start().equals(end()) ) { // not fictive
			solver.post(solver.eq(start(), end()));
		} else {
			// s + d = e
			solver.post(solver.eq(solver.plus(start(), duration()), end()));
		}
	}

	public final void postHorizonConstraint(Solver solver, int horizon) {
		if(getLCT() > horizon) {
			// create makespan constraint : horizon >= end(T)
			solver.post( solver.leq(end(), horizon));
		}
	}


}
