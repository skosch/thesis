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
import choco.kernel.solver.ContradictionException;

public final class EnumHRTask extends BoundHRTask {


	public EnumHRTask(IEnvironment env,
			AbstractResourceSConstraint constraint, int taskIdx) {
		super(env, constraint, taskIdx);
	}

	@Override
	protected boolean setHEST(int val) throws ContradictionException {			
		if( val > htask.getEST() ) {
			if(taskvar.start().canBeInstantiatedTo(val)){
				estH.set(val);
				checkHConsistency();
				fireHypotheticalDomain();
			}else{
				//get value, if any, after this one, and available in the domain
				if(val < taskvar.start().getSup()){
					final int newEST = taskvar.start().getNextDomainValue(val);
					assert newEST > val;
					estH.set(newEST);
					checkHConsistency();
					fireHypotheticalDomain();
				}
				else{
					remove();
					fireRemoval();
				}
			}
			return true;
		} else return false;
	}


	@Override
	protected boolean setHDuration(int duration) throws ContradictionException {
		if(taskvar.duration().canBeInstantiatedTo(duration)) {
			return super.setHDuration(duration);
		} else {
			remove();
			fireRemoval();
			return true;
		}
	}


	@Override
	protected boolean setHECT(int val) throws ContradictionException {
		if( val > htask.getECT() ) {
			if(taskvar.end().canBeInstantiatedTo(val)){
				estH.set(val - htask.getMinDuration());
				checkHConsistency();
				fireHypotheticalDomain();
			}else{
				//update value not existing in main domain
				if(val < taskvar.end().getSup()){
					final int newECT = taskvar.end().getNextDomainValue(val);
					assert newECT > val;
					estH.set(newECT - htask.getMinDuration());
					checkHConsistency();
					fireHypotheticalDomain();
				}
				else{
					remove();
					fireRemoval();
				}
			}
			return true;
		} else return false;
	}




	@Override
	protected boolean setHLCT(int val) throws ContradictionException {
		if( val < htask.getLCT() ) {
			if(taskvar.end().canBeInstantiatedTo(val)){
				lctH.set(val);
				checkHConsistency();
				fireHypotheticalDomain();
			}else{
				if(val > taskvar.end().getInf()){
					final int newLCT = taskvar.end().getPrevDomainValue(val);
					assert newLCT < val;
					lctH.set(newLCT);
					checkHConsistency();
					fireHypotheticalDomain();
				}
				else{
					remove();
					fireRemoval();
				}
			}
			return true;
		} else return false;
	}


	@Override
	protected boolean setHLST(int val) throws ContradictionException {
		if( val < htask.getLST() ) {
			if(taskvar.start().canBeInstantiatedTo(val)){
				lctH.set(val + htask.getMaxDuration());
				checkHConsistency();
				fireHypotheticalDomain();
			}else{
				if(val > taskvar.start().getInf()){
					final int newLST = taskvar.start().getPrevDomainValue(val);
					assert newLST < val;
					lctH.set(newLST + htask.getMaxDuration());
					checkHConsistency();
					fireHypotheticalDomain();
				}else{
					remove();
					fireRemoval();
				}
			}
			return true;
		} else return false;
	}


}

