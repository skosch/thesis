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

package choco.cp.solver.constraints.global.scheduling.disjunctive;

import choco.cp.solver.constraints.global.scheduling.AbstractResourceSConstraint;
import choco.kernel.common.opres.ssp.BellmanWithLists;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

import java.util.BitSet;
import java.util.List;


/**
 * specialized filtering for makespan minimization on unary resource.
 * The rules is efficient on Open-Shop problem.
 * It is better if the ratio load/makespan is high.
 * @author Arnaud Malapert</br> 
 * @since 23 janv. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public class ForbiddenIntervals extends AbstractResourceSConstraint {

	private final ExtendedBitSet forbidden;

	private final int load;

	public ForbiddenIntervals(Solver solver, String name, final TaskVar[] taskvars, final IntDomainVar upperBound) {
		super(solver, name, taskvars, upperBound);
		//initialize intervals by solving a Subset-Sum Problem
		int[] p=new int[getNbTasks()];
		IntDomainVar d;
		int l=0;
		for (int i = 0; i < p.length; i++) {
			d = getTask(i).duration();
			if(d.isInstantiated()) {
				p[i]=d.getVal();
			}else {throw new SolverException("forbidden intervals are only available for resources with constant duration");}
			l+=p[i];
		}
		load = l;
		final BellmanWithLists bell=new BellmanWithLists(p,load);
		bell.run();
		forbidden=new ExtendedBitSet(bell.getCoveredSet(),load);
	}

	
	@Override
	public void readOptions(List<String> options) {}


	private boolean checkHead(final int head, final int ub) {
		if(!forbidden.get(head)) {
			final int before=forbidden.prevSetBit(head);
			final int after=this.load-before;
			if(after+head> ub) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Update the head of an operation.
	 *
	 */
	private void updateHead(final int operation) throws ContradictionException {
		final TaskVar t= getTask(operation);
		final int ub = this.vars[indexUB].getSup();
		int head=t.getEST();
		if(checkHead(head, ub)) {
			rtasks[operation].updateEST(forbidden.nextSetBit(head));
		}
		head=t.getECT();
		if(checkHead(head, ub)) {
			rtasks[operation].updateECT(forbidden.nextSetBit(head));
		}
	}

	private int checkTail(final int tail, int ub) {
		if(!forbidden.get(tail)) {
			final int before=forbidden.prevSetBit(tail);
			final int after= ub -(this.load-before);
			if(tail>after) {
				return after;
			}
		}
		return Integer.MIN_VALUE;
	}


	/**
	 * Update the tail of an operation.
	 *
	 */
	private void updateTail(final int operation) throws ContradictionException {
		final TaskVar t= getTask(operation);
		final int ub = this.vars[indexUB].getSup();
		int val=checkTail(t.getLST(), ub);
		if(val>=0) {
			rtasks[operation].updateLST(val);
		}
		val=checkTail(t.getLCT(), ub);
		if(val>=0) {
			rtasks[operation].updateLCT(val);
		}
	}


	//****************************************************************//
	//********* CHOCO EVENTS *******************************************//
	//****************************************************************//


	@Override
	public void awakeOnInf(final int idx) throws ContradictionException {
		if(idx< getNbTasks()) {updateHead(idx);}
	}


	@Override
	public void awakeOnInst(final int idx) throws ContradictionException {
		if( idx < getNbTasks()) {
			updateHead(idx);
			updateTail(idx);
		}else if(idx== indexUB){
			propagate();
		}
	}

	@Override
	public void awakeOnSup(final int idx) throws ContradictionException {
		if(idx== indexUB){
			propagate();
		}else if(idx >= startOffset) {
			updateTail(idx - startOffset);
		}
	}


	/**
	 * Empty method.
	 *
	 */
	@Override
	public void propagate() throws ContradictionException {
		for (int o = 0; o < getNbTasks(); o++) {
			updateHead(o);
			updateTail(o);
		}
	}

	@Override
	public boolean isSatisfied(int[] tuple) {
		final int n = getNbTasks();
		final int makespan = tuple[indexUB];
		for (int i = 0; i < n; i++) {
			final int end = tuple[ startOffset + i];
			final int tail = checkTail(end, makespan);
			if( checkHead(tuple[i], makespan) || 
					( tail >= 0 && tail < end) ) {
				return false; 
			}
		}
		return true;
	}




}


class ExtendedBitSet {

	private final int capacity;

	private final BitSet original;

	private final BitSet symmetric;

	/**
	 * @param original
	 * @param capacity
	 */
	public ExtendedBitSet(final BitSet original, final int capacity) {
		this.capacity = capacity;
		this.original=original;
		this.symmetric=new BitSet(this.capacity+1);
		for (int i = this.original.nextSetBit(0); i >= 0; i = this.original.nextSetBit(i + 1)) {
			this.set(capacity-i);
		}
	}

	public final boolean get(final int idx) {
		return original.get(idx);
	}

	public final void set(final int idx) {
		if(capacity-idx>=0) {
			original.set(idx);
			symmetric.set(capacity-idx);
		}
	}

	public int nextSetBit(final int idx) {
		return original.nextSetBit(idx);
	}

	public int prevSetBit(final int idx) {
		final int sidx=capacity-idx;
		if(sidx<0) {return original.length()-1;}
		else {
			final int n=symmetric.nextSetBit(sidx);
			return n<0 ? n : capacity-n;
		}
	}

	@Override
	public String toString() {
		return original.toString();
	}


}

