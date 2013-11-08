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

package samples.tutorials.to_sort.packing.parser;

import gnu.trove.TIntArrayList;
import choco.kernel.common.opres.heuristics.AbstractHeuristic;
import choco.kernel.common.opres.pack.AbstractHeuristic1BP;
import choco.kernel.common.opres.pack.BestFit1BP;
import choco.kernel.common.opres.pack.FirstFit1BP;

public class CompositeHeuristics1BP extends AbstractHeuristic {

	private final AbstractHeuristic1BP ff, bf;

	public CompositeHeuristics1BP(final TIntArrayList sizes, final int capacity) {
		super();
		ff = new FirstFit1BP();
		bf = new BestFit1BP();
		ff.setItems(sizes);
		bf.setItems(sizes);
		ff.setCapacity(capacity);
		bf.setCapacity(capacity);
	}
	
	
	

	@Override
	protected int apply() {
		return Math.min( ff.apply(), bf.apply());
	}


	@Override
	public int getIterationCount() {
		return 2;
	}
	
	

}
