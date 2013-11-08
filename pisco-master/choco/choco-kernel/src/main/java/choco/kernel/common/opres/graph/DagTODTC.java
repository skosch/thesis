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

package choco.kernel.common.opres.graph;

import java.util.LinkedList;
import java.util.ListIterator;



/**
 *
 * @author Arnaud Malapert
 *<table>
<tr valign="top"><td align="left">
David&nbsp;J. Pearce and Paul H.&nbsp;J. Kelly.
</td></tr>
<tr valign="top"><td align="left">
<b> A dynamic topological sort algorithm for directed acyclic graphs.</b>
</td></tr>
<tr valign="top"><td align="left">
<em>ACM Journal of Experimental Algorithms</em>, 11, 2006.
</td></tr>
 */
public class DagTODTC extends DagDTC {

	/**
	 * @param n
	 */
	public DagTODTC(final int n) {
		super(n);
	}


	private void reorder(final ListIterator<Integer> nodes,final ListIterator<Integer> index) {
		while(index.hasNext()) {
			final int ind=index.next();
			final int n=nodes.next();
			order[ind]=n;
			orderIndex[n]=ind;
		}
	}


	@Override
	protected void fireTopologicalorder(int i, int j) {
		if(orderIndex[i]>orderIndex[j]) {
			final int lb=orderIndex[j];
			final int ub=orderIndex[i];
			final LinkedList<Integer> deltaF=new LinkedList<Integer>();
			final LinkedList<Integer> deltaB=new LinkedList<Integer>();
			final LinkedList<Integer> indexList=new LinkedList<Integer>();
			//Computing sets
			for (int k = lb; k <= ub; k++) {
				final int n=order[k];
				if(index[order[lb]][n]!=null) {
					deltaF.add(n);
					indexList.add(k);
				}else if(index[n][order[ub]]!=null) {
					deltaB.add(n);
					indexList.add(k);
				}
			}
			//reorder
			deltaB.addAll(deltaF);
			reorder(deltaB.listIterator(),indexList.listIterator());
		}
	}

}

