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

package choco.kernel.common.opres.pack;

import gnu.trove.TIntProcedure;



//*****************************************************************//
//*******************  DFF: f_{CCM,1}^k  *************************//
//***************************************************************//

final class FindParametersFCCM1 extends AbstractFindParameters implements TIntProcedure {

	

	@Override
	public boolean execute(int arg0) {
		if(arg0 > midCapacity) {
			storeParameter(capacity - arg0);
		}else {
			storeParameter(arg0);
		}
		return true;
	}
	
	
}

public final class FunctionFCCM1 extends AbstractFunctionDFF {
	
	
	private int capaDivParam;
	private int midCapa;
	private boolean isCapaOdd;
	
	
	@Override
	protected void fireValueChanged() {
		capaDivParam = capacity/parameter;
		
	}
	
	

	@Override
	public void setCapacity(int capacity) {
		super.setCapacity(capacity);
		midCapa=capacity/2;
		isCapaOdd = (capacity %2 == 1);
	}



	@Override
	public int transformCapacity() {
		return 2*capaDivParam;
	}

	@Override
	public int execute(int arg0) {
		return arg0 > midCapa ? 2* (capaDivParam - (capacity- arg0)/parameter) :
			isCapaOdd || arg0 < midCapa ?  2*(arg0/parameter) : capaDivParam;
		
	}
	
	
}
//
//public class FunctionF2 extends AbstractFunctionDDFF {
//
//	private int currentCapacity;
//
//	private int currentMidCapacity;
//
//	public FunctionF2(int capacity) {
//		super(capacity);
//	}
//
//	@Override
//	public void setParameter(int k) {
//		super.setParameter(k);
//		currentMidCapacity = (capacity / k);
//		currentCapacity = 2 * currentMidCapacity;
//	}
//
//	@Override
//	public int apply(int size) {
//		if( size > midCapacity) {
//			return currentCapacity - ( ( (capacity - size) / k)  << 1);
//		}else if( (size << 1) == capacity) return currentMidCapacity;
//		else return (size/k) << 1;
//	}
//
//	@Override
//	public int findParameter(int size) {
//		if( size > midCapacity) return  size == midCapacity + 1 ? midCapacity : capacity - size +1;
//		else return size; 
//	}
//
//	@Override
//	public int getCurrentCapacity() {
//		return currentCapacity;
//	}
//
//}
