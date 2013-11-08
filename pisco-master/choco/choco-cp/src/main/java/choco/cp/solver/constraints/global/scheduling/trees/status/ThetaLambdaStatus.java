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
public class ThetaLambdaStatus extends ThetaStatus {

	protected int grayTime;

	protected int grayDuration;

	protected Object respGrayTime;

	protected Object respGrayDuration;



	public final int getGrayTime() {
		return grayTime;
	}

	public final void setGrayTime(int grayTime) {
		this.grayTime = grayTime;
	}

	public final int getGrayDuration() {
		return grayDuration;
	}

	public final void setGrayDuration(int grayDuration) {
		this.grayDuration = grayDuration;
	}

	public final Object getRespGrayTime() {
		return respGrayTime;
	}

	public final Object getRespGrayDuration() {
		return respGrayDuration;
	}

	public final void setRespGrayTime(Object respGrayTime) {
		this.respGrayTime = respGrayTime;
	}

	public final void setRespGrayDuration(Object respGrayDuration) {
		this.respGrayDuration = respGrayDuration;
	}

	protected void updateGrayDuration(ThetaLambdaStatus left, ThetaLambdaStatus right) {
		final int l=left.getGrayDuration() +right.getDuration();
		final int r=left.getDuration()+right.getGrayDuration();
		if(l>=r) {
			setRespGrayDuration(left.getRespGrayDuration());
			setGrayDuration(l);
		}else {
			setRespGrayDuration(right.getRespGrayDuration());
			setGrayDuration(r);
		}
	}

	public void updateGrayECT(ThetaLambdaStatus left, ThetaLambdaStatus right) {
		final int l=right.getGrayTime();
		final int m=left.getTime()+right.getGrayDuration();
		final int r=left.getGrayTime()+right.getDuration();
		if(l>=m && l>= r) {
			setRespGrayTime(right.getRespGrayTime());
			setGrayTime(l);
		}else if(m>=r) {
			setRespGrayTime(right.getRespGrayDuration());
			setGrayTime(m);
		}else {
			setRespGrayTime(left.getRespGrayTime());
			setGrayTime(r);
		}
	}

	public void updateGrayLST(ThetaLambdaStatus left, ThetaLambdaStatus right) {
		final int l=left.getGrayTime();
		final int m=right.getTime() - left.getGrayDuration();
		final int r=right.getGrayTime() -left.getDuration();
		if(l<=m && l<= r) {
			setRespGrayTime(left.getRespGrayTime());
			setGrayTime(l);
		}else if(m<=r) {
			setRespGrayTime(left.getRespGrayDuration());
			setGrayTime(m);
		}else {
			setRespGrayTime(right.getRespGrayTime());
			setGrayTime(r);
		}
	}

	public void update(TreeMode mode,ThetaLambdaStatus left, ThetaLambdaStatus right ) {
		super.update(mode, left, right);
		switch (mode) {
		case ECT: updateGrayECT(left, right);updateGrayDuration(left, right);break;
		case LST: updateGrayLST(left, right);updateGrayDuration(left, right);break;
		default:
			throw new UnsupportedOperationException("unknown tree mode.");
		}
	}

}