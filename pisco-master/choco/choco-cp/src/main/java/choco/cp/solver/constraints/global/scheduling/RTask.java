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

package choco.cp.solver.constraints.global.scheduling;

import choco.kernel.common.util.tools.TaskUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.ITask;
import choco.kernel.solver.variables.scheduling.TaskVar;

public class RTask implements IRTask {

	public final AbstractResourceSConstraint resource;
	
	public final int taskIdx;

	public final TaskVar taskvar;
	
	protected ITask htask;
	
	public final IntDomainVar usage;
	
	public final IntDomainVar height;
	
	private int value;
	
	public RTask(final AbstractResourceSConstraint constraint, final int taskIdx) {
		super();
		this.resource = constraint;
		this.taskIdx = taskIdx;
		this.taskvar = constraint.getTask(taskIdx);
		this.usage = constraint.getVar(constraint.getUsageIndex(taskIdx));
		this.height = constraint.getVar(constraint.getHeightIndex(taskIdx));
		this.htask = taskvar;
		if( ! (getUsage().getInf() >=0 && getUsage().getSup() <= 1)) {
			throw new SolverException(getUsage().pretty()+" is not a boolean variable.");
		}
	}

	
	@Override
	public final int getTaskIndex() {
		return taskIdx;
	}

	@Override
	public final int getStoredValue() {
		return value;
	}

	@Override
	public final void storeValue(int val) {
		this.value=val;

	}

	@Override
	public final TaskVar getTaskVar() {
		return taskvar;
	}

	@Override
	public final ITask getHTask() {
		return htask;
	}

	@Override
	public void checkConsistency() throws ContradictionException {
		updateCompulsoryPart();			
	}


	@Override
	public final boolean updateECT() throws ContradictionException {
		return updateECT(value);
	}

	@Override
	public final boolean updateEST() throws ContradictionException {
		return updateEST(value);
	}

	@Override
	public final boolean updateLCT() throws ContradictionException {
		return updateLCT(value);
	}

	@Override
	public final boolean updateLST() throws ContradictionException {
		return updateLST(value);
	}

	
	@Override
	public boolean updateDuration(int duration) throws ContradictionException {
		if( setDuration(duration)) {
			updateCompulsoryPart();
			return true;
		}
		return false;
	}



	@Override
	public boolean updateECT(int val) throws ContradictionException {
		if( setECT(val)) {
			updateCompulsoryPart();
			return true;
		}
		return false;
	}




	@Override
	public boolean updateEndingTime(int endingTime)
	throws ContradictionException {
		if( setEndingTime(endingTime)) {
			updateCompulsoryPart();
			return true;
		}
		return false;
	}




	@Override
	public boolean updateEndNotIn(int a, int b) throws ContradictionException {
		if( setEndNotIn(a, b)) {
			updateCompulsoryPart();
			return true;
		}
		return false;
	}




	@Override
	public boolean updateEST(int val) throws ContradictionException {
		if( setEST(val)) {
			updateCompulsoryPart();
			return true;
		}
		return false;
	}




	@Override
	public boolean updateLCT(int val) throws ContradictionException {
		if( setLCT(val)) {
			updateCompulsoryPart();
			return true;
		}
		return false;
	}


	@Override
	public boolean updateLST(int val) throws ContradictionException {
		if( setLST(val)) {
			updateCompulsoryPart();
			return true;
		}
		return false;
	}




	@Override
	public boolean updateMaxDuration(int val) throws ContradictionException {
		if( setMaxDuration(val)) {
			updateCompulsoryPart();
			return true;
		}
		return false;
	}




	@Override
	public boolean updateMinDuration(int val) throws ContradictionException {
		if( setMinDuration(val)) {
			updateCompulsoryPart();
			return true;
		}
		return false;
	}




	@Override
	public boolean updateStartingTime(int startingTime)
	throws ContradictionException {
		if( setStartingTime(startingTime)) {
			updateCompulsoryPart();
			return true;
		}
		return false;
	}

	@Override
	public boolean updateStartNotIn(int a, int b) throws ContradictionException {
		if( setStartNotIn(a, b)) {
			updateCompulsoryPart();
			return true;
		}
		return false;
	}


	@Override
	public boolean setDuration(final int duration) throws ContradictionException {
		assert isRegular(); //do not change the domain of optional/eliminated tasks
		return taskvar.duration().instantiate(duration, resource, false);
	}

	@Override
	public boolean setStartingTime(final int startingTime)
	throws ContradictionException {
		assert isRegular(); //do not change the domain of optional/eliminated tasks
		return taskvar.start().instantiate(startingTime, resource, false);
	}


	@Override
	public boolean setEndingTime(int endingTime)
	throws ContradictionException {
		assert isRegular(); //do not change the domain of optional/eliminated tasks
		return taskvar.end().instantiate(endingTime, resource, false);
	}

	@Override
	public boolean setEndNotIn(int a, int b)
	throws ContradictionException {
		assert isRegular(); //do not change the domain of optional/eliminated tasks
		return taskvar.end().removeInterval(a, b, resource, false);
	}

	@Override
	public boolean setStartNotIn(int min, int max)
	throws ContradictionException {
		assert isRegular(); //do not change the domain of optional/eliminated tasks
		return taskvar.start().removeInterval(min, max, resource, false);
	}


	@Override
	public boolean setECT(final int val) throws ContradictionException {
		assert isRegular(); //do not change the domain of optional/eliminated tasks
		return taskvar.end().updateInf(val, resource, false);
	}

	@Override
	public boolean setEST(final int val) throws ContradictionException {
		assert isRegular(); //do not change the domain of optional/eliminated tasks
		return taskvar.start().updateInf(val, resource, false);
	}

	@Override
	public boolean setLCT(final int val) throws ContradictionException {
		assert isRegular(); //do not change the domain of optional/eliminated tasks
		return taskvar.end().updateSup(val, resource, false);
	}

	@Override
	public boolean setLST(final int val) throws ContradictionException {
		assert isRegular(); //do not change the domain of optional/eliminated tasks
		return taskvar.start().updateSup(val, resource, false);
	}

	@Override
	public boolean setMaxDuration(final int val) throws ContradictionException {
		assert isRegular(); //do not change the domain of optional/eliminated tasks
		return taskvar.duration().updateSup(val, resource, false);
	}

	@Override
	public boolean setMinDuration(final int val) throws ContradictionException {
		assert isRegular(); //do not change the domain of optional/eliminated tasks
		return taskvar.duration().updateInf(val, resource, false);
	}

	public final void updateCompulsoryPart() throws ContradictionException {
		taskvar.updateCompulsoryPart(resource);
	}


	@Override
	public final void fail() throws ContradictionException {
		resource.fail();
	}

	@Override
	public final IntDomainVar getUsage() {
		return usage;
	}

	@Override
	public boolean assign() throws ContradictionException {
		return usage.instantiate(REGULAR, resource, false);
	}

	@Override
	public final boolean isOptional() {
		return ! usage.isInstantiated();
	}

	@Override
	public final boolean isRegular() {
		return usage.isInstantiatedTo(REGULAR);
	}

	@Override
	public final boolean isEliminated() {
		return usage.isInstantiatedTo(ELIMINATED);
	}


	@Override
	public final boolean remove() throws ContradictionException {
		return usage.instantiate(ELIMINATED, resource, false);
	}



	@Override
	public final void fireRemoval() {
		assert isEliminated(); 
		resource.fireTaskRemoval(this);
	}

	@Override
	public final IntDomainVar getHeight() {
		return height;
	}

	@Override
	public final boolean updateMaxHeight(final int val) throws ContradictionException {
		return height.updateSup(val, resource, false);
	}

	@Override
	public final boolean updateMinHeight(final int val) throws ContradictionException {
		return height.updateInf(val, resource, false);
	}

	@Override
	public final int getMaxHeight() {
		return getHeight().getSup();
	}

	@Override
	public final int getMinHeight() {
		return getHeight().getInf();
	}

	@Override
	public final long getMaxConsumption() {
		return TaskUtils.getMaxConsumption(this);
	}

	@Override
	public final long getMinConsumption() {
		return TaskUtils.getMinConsumption(this);
	}

	
	@Override
	public String toString() {
		return getHTask().toString();
	}

	@Override
	public String pretty() {
		return getHTask().pretty();
	}

	

}
