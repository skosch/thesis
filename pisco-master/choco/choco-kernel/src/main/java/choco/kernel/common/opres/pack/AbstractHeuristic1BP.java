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

import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;
import gnu.trove.TLinkableAdapter;
import choco.kernel.common.opres.heuristics.AbstractHeuristic;

/**
 * The Class AbstractHeurisic1BP.
 */
public abstract class AbstractHeuristic1BP extends AbstractHeuristic implements TIntProcedure {

	protected int capacity;

	protected TIntArrayList items;

	protected int[] bins;
	
	protected int pos;
	
	protected int full;
	
	private final static int DEFAULT_CAPACITY = 10;
	
	
	public AbstractHeuristic1BP() {
		super();
		bins = new int[DEFAULT_CAPACITY];
	}
	
	
	
	private void ensureCapacity() {
        if (pos >= bins.length) {
            final int newCap = bins.length << 1;
            final int[] tmp = new int[newCap];
            System.arraycopy(bins, 0, tmp, 0,bins.length);
            bins = tmp;
        }
    }
	
	/**
	 * 
	 * @param size the size of the first item packed into the bin
	 */
	protected void createBin(int size) {
		ensureCapacity();
		bins[pos] = capacity - size;
		pos++;
	}
	
	protected void removeBin(int offset) {
		if(pos > 1) bins[offset] = bins[--pos];
		else pos--;
		full++;
	}
	
	protected void pack(int offset, int size) {
		bins[offset] -= size;
	}

	@Override
	public void reset() {
		super.reset();
		pos=0;
		full=0;
	}

	public final TIntArrayList getItems() {
		return items;
	}

	public final void setItems(TIntArrayList items) {
		this.items = items;
	}
	
	public final void setCapacity(int capacity) {
		assert capacity > 0;
		this.capacity = capacity;
	}
	public final int getCapacity() {
		return capacity;
	}

	@Override
	public final int apply() {
		items.forEachDescending(this);
		return full + pos;
	}

	public final int executeQuick(TIntArrayList items) {
		reset();
		setItems(items);
		return apply();
	}
	
	public final int executeQuick(TIntArrayList items, int capacity) {
		reset();
		setItems(items);
		setCapacity(capacity);
		return apply();
	}
}

	





