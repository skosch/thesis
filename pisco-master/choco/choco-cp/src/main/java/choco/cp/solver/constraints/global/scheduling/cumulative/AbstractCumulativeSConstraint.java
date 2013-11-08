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

package choco.cp.solver.constraints.global.scheduling.cumulative;

import static choco.Options.C_CUMUL_EF;
import static choco.Options.C_CUMUL_STI;
import static choco.Options.C_CUMUL_TI;
import static choco.Options.C_CUMUL_VEF;

import java.util.List;

import choco.cp.solver.constraints.global.scheduling.AbstractResourceSConstraint;
import choco.kernel.common.util.bitmask.StringMask;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.global.scheduling.ICumulativeResource;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.TaskVar;

/**
 * @author Arnaud Malapert</br> 
 * @since 23 janv. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public abstract class AbstractCumulativeSConstraint extends AbstractResourceSConstraint implements ICumulativeResource<TaskVar> {

	public final static StringMask TASK_INTERVAL = new StringMask(C_CUMUL_TI,1);
	public final static StringMask TASK_INTERVAL_SLOW = new StringMask(C_CUMUL_STI,1 << 2);
	public final static StringMask VILIM_CEF_ALGO = new StringMask(C_CUMUL_VEF,1 << 3);
	public final static StringMask VHM_CEF_ALGO_N2K = new StringMask(C_CUMUL_EF,1 << 4);
	
	/** indicates if all heights are instantiated (lazy computation) */
	private final IStateBool fixedHeights;

	/** indicates if all heights are positive or nil (lazy computation) */
	private final IStateBool positiveHeights;

	/** indicates if at least one regular tasks has a negative height (lazy computation) */
	private final IStateBool regularWithNegativeHeight;

	protected final int indexConsumption;

	protected final int indexCapacity;

	public AbstractCumulativeSConstraint(Solver solver, final String name, final TaskVar[] taskvars,int nbOptionalTasks,
                                         final IntDomainVar consumption, final IntDomainVar capacity,
                                         final IntDomainVar uppBound, final IntDomainVar... otherVars) {
		super(solver, name, taskvars, nbOptionalTasks, true, false, ArrayUtils.append(otherVars,new IntDomainVar[]{consumption, capacity, uppBound}));
		indexConsumption = taskIntVarOffset + otherVars.length;
		indexCapacity = indexConsumption + 1;
		final IEnvironment env = solver.getEnvironment();
		fixedHeights = env.makeBool(false);
		positiveHeights = env.makeBool(false);
		regularWithNegativeHeight = env.makeBool(false);


	}

	
	@Override
	public void readOptions(List<String> options) {
		flags.read(options, TASK_INTERVAL, TASK_INTERVAL_SLOW, VHM_CEF_ALGO_N2K, VILIM_CEF_ALGO);
	}



	@Override
	public boolean isTaskConsistencyEnforced() {
		return true;
	}
	
	public final boolean updateMinCapacity(final int val) throws ContradictionException {
		return vars[indexCapacity].updateInf(val, this, false);
	}

	public final boolean updateMaxCapacity(final int val) throws ContradictionException {
		return vars[indexCapacity].updateSup(val, this, false);
	}

	public final boolean updateMinConsumption(final int val) throws ContradictionException {
		return vars[indexConsumption].updateInf(val, this, false);
	}
	public final boolean updateMaxConsumption(final int val) throws ContradictionException {
		return vars[indexConsumption].updateSup(val, this, false);
	}


	@Override
	public final IntDomainVar getCapacity() {
		return getVar(indexCapacity);
	}

	@Override
	public final int getMaxCapacity() {
		return getCapacity().getSup();
	}


	@Override
	public final int getMinCapacity() {
		return getCapacity().getInf();
	}

	@Override
	public final IntDomainVar getConsumption() {
		return vars[indexConsumption];
	}

	@Override
	public final int getMaxConsumption() {
		return getConsumption().getSup();
	}

	@Override
	public final int getMinConsumption() {
		return getConsumption().getInf();
	}

	public final IntDomainVar getHeight(final int idx) {
		return getVar(getHeightIndex(idx));
	}

	@Override
	public final boolean isInstantiatedHeights() {
		if( ! fixedHeights.get()) {
			for (int i = 0; i < getNbTasks(); i++) {
				final IRTask rtask = getRTask(i);
				if( !rtask.isEliminated() && !rtask.getHeight().isInstantiated()) {
					return false;
				}
			}
			fixedHeights.set(true);
		}
		return true;
	}



	@Override
	public final boolean hasOnlyPosisiveHeights() {
		if(! positiveHeights.get()) {
			if(regularWithNegativeHeight.get()) {return false;}
			else {
				for (int i = 0; i < getNbTasks(); i++) {
					final IRTask rtask = getRTask(i);
					if( !rtask.isEliminated() && rtask.getMinHeight() < 0) {
						if(rtask.isRegular()) {regularWithNegativeHeight.set(true);}
						return false;
					}
				}
				positiveHeights.set(true);
			}
		}
		return true;
	}
	
	@Override
	public final boolean isSatisfied(int tuple[]) {
		return isCumulativeSatisfied(tuple, tuple[indexConsumption], tuple[indexCapacity]);
	}
	
	@Override
	public Boolean isEntailed() {
		throw new UnsupportedOperationException("isEntailed not yet implemented on choco.global.scheduling.Cumulative");
	}

}
