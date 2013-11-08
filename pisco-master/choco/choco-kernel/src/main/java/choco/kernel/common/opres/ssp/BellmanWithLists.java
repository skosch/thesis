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

package choco.kernel.common.opres.ssp;

import choco.kernel.solver.SolverException;

import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.ListIterator;




/**
 * @author Arnaud Malapert
 *
 */
public class BellmanWithLists extends AbstractSubsetSumSolver {



	private int[] setX;

	private final LinkedList<Integer> reachables=new LinkedList<Integer>();

	public BellmanWithLists(int[] sizes, int capacity) {
		super(sizes, capacity);
	}

	@Override
	public void reset() {
		super.reset();
		reachables.clear();
		Arrays.fill(setX, NONE);
	}


	@Override
	public void setCapacity(Long capacity) {
		if(!capacity.equals(this.capacity)) {
			setX=new int[Long.valueOf(capacity).intValue()+1];
			Arrays.fill(setX, NONE);
		}
		super.setCapacity(capacity);
	}

	@Override
	public String getName() {
		return "Bellman with lists";
	}

	@Override
	public long run() {
		reachables.add(0);
		for (int item = 0; item < sizes.length; item++) {
			handleItem(item);
			if(setX[capacity.intValue()]!=NONE) {
				break;
			}
		}
		objective=reachables.getLast();
		return objective;
	}


	@Override
	public BitSet getSolution() {
		int cpt= Long.valueOf(objective).intValue();
		BitSet solution=new BitSet(sizes.length);
		while(cpt>0) {
			solution.set(cpt);
			cpt-=sizes[setX[cpt]];
		}
		if(cpt!=0) {throw new SolverException("internal error of "+getName());}
		return solution;
	}




	public final BitSet getCoveredSet() {
		BitSet res=new BitSet(capacity.intValue()+1);
		for (Integer r : reachables) {
			res.set(r);
		}
		return res;
	}

	public void handleItem(final int item) {
		LinkedList<Integer> tmp=new LinkedList<Integer>();
		ListIterator<Integer> old=reachables.listIterator();
		while(old.hasNext()) {
			final Integer current=old.next();
			//adding new reachables
			old.previous();
			while( !tmp.isEmpty() && tmp.getFirst()<current) {
				old.add(tmp.removeFirst());
			}
			old.next();
			//complete list
			final Integer value=current+sizes[item];
			if(value<=capacity && setX[value]==NONE) {
				tmp.add(value);
				setX[value]=item;
			}
		}
		reachables.addAll(tmp);
	}


}
