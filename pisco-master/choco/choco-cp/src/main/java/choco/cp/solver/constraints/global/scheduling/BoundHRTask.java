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

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.scheduling.HTask;

public class BoundHRTask extends RTask {

	protected final IStateInt estH,lctH;

	public BoundHRTask(IEnvironment env, AbstractResourceSConstraint constraint, int taskIdx) {
		super(constraint, taskIdx);
		estH = env.makeInt(getTaskVar().getEST());
		lctH = env.makeInt(getTaskVar().getLCT());
		htask = new HTask(taskvar, usage, estH, lctH);
	}


	public void fireHypotheticalDomain() {
		getTaskVar().updateHypotheticalDomain(-1, null, false);
	}


	public final boolean checkHypotheticalConsistency() {
		return htask.getLCT() - htask.getEST() >= htask.getMinDuration();
	}

	protected void checkHConsistency() throws ContradictionException {
		if( ! checkHypotheticalConsistency() ) {
			remove();
			fireRemoval();
		}
	}

	@Override
	public void checkConsistency() throws ContradictionException {
		super.checkConsistency();
		if(isOptional()) checkHConsistency();
	}


	@Override
	public final boolean assign() throws ContradictionException {
		final boolean assigned = super.assign();
		if( super.setEST(estH.get()) || super.setLCT(lctH.get()) ) {
			updateCompulsoryPart();
		}
		return assigned;
	}


	protected boolean setHEST(int val) throws ContradictionException {			
		if( val > htask.getEST() ) {
			estH.set(val);
			checkHConsistency();
			fireHypotheticalDomain();
			return true;
		} else return false;
	}

	@Override
	public final boolean setEST(int val) throws ContradictionException {
		if( isOptional()) return setHEST(val);
		else return super.setEST(val);
	}

	protected boolean setHDuration(int duration) throws ContradictionException {
		//did not find updates for estH,lctH
		if ( htask.getLCT() - htask.getEST() >= duration ) {
			//hypothetical duration is not consistent
			remove();
			return true;
		}
		return false;
	}

	@Override
	public final boolean setDuration(int duration) throws ContradictionException {
		if( isOptional()) return setHDuration(duration);
		else return super.setDuration(duration);
	}

	protected boolean setHECT(int val) throws ContradictionException {
		if( val > htask.getECT() ) {
			estH.set(val - htask.getMinDuration());
			checkHConsistency();
			fireHypotheticalDomain();
			return true;
		} else return false;
	}

	@Override
	public final boolean setECT(int val) throws ContradictionException {
		if( isOptional()) return setHECT(val);
		else return super.setECT(val);
	}

	public final boolean setHEndingTime(int endingTime) throws ContradictionException {
		return setHLCT(endingTime) || setHECT(endingTime);
	}

	@Override
	public final boolean setEndingTime(int endingTime)
	throws ContradictionException {
		if( isOptional()) return setHEndingTime(endingTime);
		else return super.setEndingTime(endingTime);
	}

	public final boolean setHEndNotIn(int a, int b) throws ContradictionException {
		if( a <= htask.getECT()) return setHECT(b);
		else if(b >= htask.getLCT()) return setHLCT(a);
		else return false;
	}

	@Override
	public final boolean setEndNotIn(int a, int b) throws ContradictionException {
		if( isOptional()) return setHEndNotIn(a, b);
		else return super.setEndNotIn(a, b);
	}

	protected boolean setHLCT(int val) throws ContradictionException {
		if( val < htask.getLCT() ) {
			lctH.set(val);
			checkHConsistency();
			fireHypotheticalDomain();
			return true;
		} else return false;
	}

	@Override
	public final boolean setLCT(int val) throws ContradictionException {
		if( isOptional()) return setHLCT(val);
		else return super.setLCT(val);
	}

	protected boolean setHLST(int val) throws ContradictionException {
		if( val < htask.getLST() ) {
			lctH.set(val + htask.getMaxDuration());
			checkHConsistency();
			fireHypotheticalDomain();
			return true;
		} else return false;
	}

	@Override
	public final boolean setLST(int val) throws ContradictionException {
		if( isOptional()) return setHLST(val);
		else return super.setLST(val);
	}


	@Override
	public final boolean setMaxDuration(int val) throws ContradictionException {
		if ( isRegular() ) return super.setMaxDuration(val);
		else return false;
	}


	@Override
	public final boolean setMinDuration(int val) throws ContradictionException {
		if( isOptional()) return setHDuration(val);
		else return super.setMinDuration(val);
	}


	protected final boolean setHStartingTime(int startingTime) throws ContradictionException {
		return setHEST(startingTime) || setHLST(startingTime);
	}


	@Override
	public final boolean setStartingTime(int startingTime)
	throws ContradictionException {
		if( isOptional()) return setHStartingTime(startingTime); 
		else return super.setStartingTime(startingTime);
	}

	protected final  boolean setHStartNotIn(int min, int max) throws ContradictionException {
		if( min <= htask.getEST()) return setHEST(max);
		else if(max >= htask.getLST()) return setHLST(min);
		else return false;
	}

	@Override
	public final boolean setStartNotIn(int min, int max)
	throws ContradictionException {
		if( isOptional()) return setHStartNotIn(min, max);
		else return super.setStartNotIn(min, max);
	}

	@Override
	public final boolean updateDuration(int duration)
	throws ContradictionException {
		if(isOptional()) return setHDuration(duration);
		else return super.updateDuration(duration);
	}

	@Override
	public final boolean updateECT(int val) throws ContradictionException {
		if(isOptional()) return setHECT(val);
		else return super.updateECT(val);
	}

	@Override
	public final boolean updateEndingTime(int endingTime)
	throws ContradictionException {
		if(isOptional()) return setHEndingTime(endingTime);
		else return super.updateEndingTime(endingTime);
	}

	@Override
	public final boolean updateEndNotIn(int a, int b)
	throws ContradictionException {
		if(isOptional()) return setHEndNotIn(a, b);
		else return super.updateEndNotIn(a, b);
	}

	@Override
	public final boolean updateEST(int val) throws ContradictionException {
		if(isOptional()) return setHEST(val);
		else return super.updateEST(val);
	}

	@Override
	public final boolean updateLCT(int val) throws ContradictionException {
		if(isOptional()) return setHLCT(val);
		else return super.updateLCT(val);
	}

	@Override
	public final boolean updateLST(int val) throws ContradictionException {
		if(isOptional()) return setHLST(val);
		else return super.updateLST(val);
	}

	@Override
	public final boolean updateMaxDuration(int val) throws ContradictionException {
		if(isRegular()) return super.updateMaxDuration(val);
		else return false;
	}

	@Override
	public final boolean updateMinDuration(int val) throws ContradictionException {
		if(isOptional()) return setHDuration(val);
		else return super.updateMinDuration(val);
	}

	@Override
	public final boolean updateStartingTime(int startingTime)
	throws ContradictionException {
		if(isOptional()) return setHStartingTime(startingTime);
		else return super.updateStartingTime(startingTime);
	}

	@Override
	public final boolean updateStartNotIn(int a, int b)
	throws ContradictionException {
		if(isOptional()) return setHStartNotIn(a, b);
		else return super.updateStartNotIn(a, b);
	}


}

