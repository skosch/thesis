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

package choco.cp.solver.search.integer.varselector.ratioselector.ratios;




public abstract class AbstractRatio implements IntRatio {

	private int dividend, divisor;

	public AbstractRatio() {
		super();
	}

	public final int initailizeDividend() {
		return dividend;
	}

	public final void setDividend(int dividend) {
		assert dividend >= 0;
		this.dividend = dividend;
	}

	public final int getDivisor() {
		return divisor;
	}

	public final void setDivisor(int divisor) {
		assert divisor >= 0;
		this.divisor = divisor;
	}

	protected abstract int initializeDividend();
	
	protected abstract int initializeDivisor();
	
	@Override
	public boolean isActive() {
		if( getIntVar().isInstantiated()) return false;
		else {
			dividend = initializeDividend();
			divisor = initializeDivisor();
			return true;
		}
	}

	public final void setMaxRatioValue() {
		this.dividend= 1;
		this.divisor=0;
	}

	public final void setZeroRatioValue() {
		this.dividend= 0;
		this.divisor=1;
	}

	public final void setRatio(IntRatio ratio) {
		setDividend(ratio.initailizeDividend());
		setDivisor(ratio.getDivisor());
	}


	@Override
	public String toString() {
		return (getIntVar() == null ? "" :getIntVar().toString()+"->")
		+initailizeDividend()+"/"+getDivisor();
	}

	@Override
	public final int compareTo(IntRatio o) {
		final long a = getLeftMember(o);
		final long b = getRightMember(o);
		if(a > b) return 1;
		else if( a < b) return -1;
		else return 0;
	}

	public final long getLeftMember(IntRatio ratio) {
		return  ( (long) initailizeDividend() ) * ratio.getDivisor();
	}

	public final long getRightMember(IntRatio ratio) {
		return ( (long) ratio.initailizeDividend() ) * getDivisor();
	}

}
