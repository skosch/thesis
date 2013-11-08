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

import java.util.Arrays;

import gnu.trove.TIntFunction;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntProcedure;

class AbstractComponentDDFF {
	
	public int capacity = Integer.MAX_VALUE;

	public final int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		assert capacity >=0;
		this.capacity = capacity;
	}
	
	
}

abstract class AbstractFindParameters extends AbstractComponentDDFF implements TIntProcedure {

	private final TIntHashSet parameters = new TIntHashSet();
	
	protected int midCapacity;

	public AbstractFindParameters() {
		super();
	}
	
	protected final void storeParameter(int k) {
		assert(k>= 0 && k <= midCapacity);
		parameters.add(k);
	}

	@Override
	public void setCapacity(int capacity) {
		this.capacity = capacity;
		midCapacity = capacity/2; 
	}

	public TIntHashSet getParameters() {
		parameters.remove(0); //avoid checking at each parameter insertion.
		parameters.add(1); //compute L0 (no transformation of items)
		return parameters;
	}

	public void clearParameters() {
		parameters.clear();
	}

	@Override
	public String toString() {
		int[] values = parameters.toArray();
		Arrays.sort(values);
		return Arrays.toString(values);
	}
	
	

}

public abstract class AbstractFunctionDFF extends AbstractComponentDDFF implements TIntFunction {

	public int parameter=1;
	
	public AbstractFunctionDFF() {
		super();
	}

	public final int getParameter() {
		return parameter;
	}
	
	protected abstract void fireValueChanged();
	
	public void setParameter(int parameter) {
		this.parameter = parameter;
		fireValueChanged();
	}
	
	@Override
	public void setCapacity(int capacity) {
		super.setCapacity(capacity);
		fireValueChanged();
		
	}
	
	public abstract int transformCapacity();
}

