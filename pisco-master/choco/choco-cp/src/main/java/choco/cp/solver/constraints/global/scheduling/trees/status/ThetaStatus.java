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
 * @author Arnaud Malapert</br> 
 * @since 10 févr. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public class ThetaStatus {

	protected int time;

	protected int duration;


	public final int getTime() {
		return time;
	}


	public final void setTime(int time) {
		this.time = time;
	}

	public final int getDuration() {
		return duration;
	}


	public final void setDuration(int duration) {
		this.duration = duration;
	}

	protected void updateDuration(ThetaStatus lcs, ThetaStatus rcs) {
		setDuration(lcs.getDuration() + rcs.getDuration());
	}

	public void updateECT(ThetaStatus lcs, ThetaStatus rcs) {
		setTime( Math.max(rcs.getTime() ,lcs.getTime()+rcs.getDuration()));
		updateDuration(lcs, rcs);
	}

	public void updateLST(ThetaStatus lcs, ThetaStatus rcs) {
		setTime( Math.min(lcs.getTime(),rcs.getTime() - lcs.getDuration()));
		updateDuration(lcs, rcs);
	}

	public void update(TreeMode mode,ThetaStatus left, ThetaStatus right ) {
		switch (mode) {
		case ECT: updateECT(left, right);updateDuration(left, right);break;
		case LST: updateLST(left, right);updateDuration(left, right);break;
		default:
			throw new UnsupportedOperationException("unknown tree mode.");
		}
	}
}