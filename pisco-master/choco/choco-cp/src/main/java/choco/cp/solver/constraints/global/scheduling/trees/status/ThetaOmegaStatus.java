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

package choco.cp.solver.constraints.global.scheduling.trees.status;

import choco.cp.solver.constraints.global.scheduling.trees.IVilimTree.TreeMode;

/**
 * @author abadr
 * 
 */
public class ThetaOmegaStatus extends ThetaLambdaStatus {
	
	protected int tOTime; //Contains ECT(Theta,Omega) of subtree

	protected int tODuration; //Contains Total Duration of Theta-Omega subtree.

	protected Object respTOTime; //Optional activity in Omega responsible for highest ECT. 

	protected Object respTODuration; //Optional activity in Omega responsible for highest Duration.

	public int getTOTime() {
		return tOTime;
	}

	public void setTOTime(int time) {
		tOTime = time;
	}

	public int getTODuration() {
		return tODuration;
	}

	public void setTODuration(int duration) {
		tODuration = duration;
	}

	public Object getRespTOTime() {
		return respTOTime;
	}

	public void setRespTOTime(Object respTOTime) {
		this.respTOTime = respTOTime;
	}

	public Object getRespTODuration() {
		return respTODuration;
	}

	public void setRespTODuration(Object respTODuration) {
		this.respTODuration = respTODuration;
	}
	/**
	 * Updating the total duration for Theta-Omega in internal/root node
	 * @param left: left Theta-Omega subtree
	 * @param right: right Theta-Omega subtree
	 */
	protected void updateTODuration(ThetaOmegaStatus left, ThetaOmegaStatus right) {
		final int l=left.getTODuration() +right.getDuration(); //optional task in the left hand side of the tree
		final int r=left.getDuration()+right.getTODuration();//optional task in the right hand side of the tree
		if(l>=r) {
			setRespTODuration(left.getRespTODuration());
			setTODuration(l);
		}else {
			setRespTODuration(right.getRespTODuration());
			setTODuration(r);
		}
	}
	/**
	 * Updating Earliest Completion Time for Theta-Omega in internal/root node
	 * @param left: Left Theta-Omega Subtree
	 * @param right: Right Theta-Omega Subtree
	 */
	public void updateTOECT(ThetaOmegaStatus left, ThetaOmegaStatus right) {
		final int l=right.getTOTime();
		final int m=left.getTime()+right.getTODuration();
		final int r=left.getTOTime()+right.getDuration();
		if(l>=m && l>= r) {
			setRespTOTime(right.getRespTOTime());
			setTOTime(l);
		}else if(m>=r) {
			setRespTOTime(right.getRespTODuration());
			setTOTime(m);
		}else {
			setRespTOTime(left.getRespTOTime());
			setTOTime(r);
		}
	}
	
	public void updateTOLST(ThetaOmegaStatus left, ThetaOmegaStatus right) {
		final int l=left.getTOTime();
		final int m=right.getTime() - left.getTODuration();
		final int r=right.getTOTime() -left.getDuration();
		if(l<=m && l<= r) {
			setRespTOTime(left.getRespTOTime());
			setTOTime(l);
		}else if(m<=r) {
			setRespTOTime(left.getRespTODuration());
			setTOTime(m);
		}else {
			setRespTOTime(right.getRespTOTime());
			setTOTime(r);
		}
	}
	public void update(TreeMode mode,ThetaOmegaStatus left, ThetaOmegaStatus right ) {
		super.update(mode, left, right);
		switch (mode) {
		case ECT: updateTOECT(left, right);updateTODuration(left, right);break;
		case LST: updateTOLST(left, right);updateTODuration(left, right);break;
		default:
			throw new UnsupportedOperationException("unknown tree mode.");
		}
	}

}
