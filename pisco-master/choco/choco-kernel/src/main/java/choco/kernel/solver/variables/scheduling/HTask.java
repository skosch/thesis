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

package choco.kernel.solver.variables.scheduling;

import static choco.kernel.common.util.tools.TaskUtils.isRegular;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.variables.integer.IntDomainVar;

public final class HTask extends AbstractTask {

	private final ITask task;
	
	private IntDomainVar usage;
	
	private final IStateInt estH, lctH;
		
	public HTask(ITask task, IntDomainVar usage, IStateInt estH, IStateInt lctH) {
		super();
		this.task = task;
		this.usage = usage;
		this.estH = estH;
		this.lctH = lctH;
	}
	

	@Override
	public final boolean isPreemptionAllowed() {
		return task.isPreemptionAllowed();
	}


	//ITask interface : integrate the hypothetical domains for filtering algorithms.
	@Override
	public int getECT() {
		final int valR = task.getECT();
		if( isRegular(usage))
			return valR;
		else
		{
			final int valH = estH.get() + task.getMinDuration();
			return valR >  valH ? valR : valH;
		}
	}

	@Override
	public int getEST() {
		final int valR = task.getEST();
		if(isRegular(usage))
			return valR;
		else
		{
			final int valH = estH.get();
			return valR >  valH ? valR : valH;
		}
	}

	@Override
	public int getID() {
		return task.getID();
	}

	@Override
	public int getLCT() {
		final int valR = task.getLCT();
		if(isRegular(usage))
			return valR;
		else
		{
			final int valH = lctH.get();
			return valR >  valH ? valH : valR;
		}
	}

	@Override
	public int getLST() {
		final int valR = task.getLST();
		if(isRegular(usage))
			return valR;
		else
		{
			final int valH = lctH.get() - task.getMinDuration();
			return valR >  valH ? valH : valR;
		}
	}

	@Override
	public int getMaxDuration() {
		final int valR = task.getMaxDuration();
		if(isRegular(usage))
			return valR;
		else{
			final int valH = lctH.get() - estH.get();
			return valR >  valH ? valH : valR;
		}
	}

	@Override
	public int getMinDuration() {
		return task.getMinDuration();
	}

	@Override
	public String getName() {
		return task.getName();
	}

	@Override
	public boolean hasConstantDuration() {
		return task.hasConstantDuration();
	}

	@Override
	public boolean isScheduled() {
		return task.isScheduled();
	}



	

}
