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

package choco.cp.solver.constraints.global.scheduling.precedence;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.TaskUtils;
import choco.kernel.model.constraints.ITemporalRelation;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

public abstract class AbstractPrecedenceSConstraint extends AbstractLargeIntSConstraint 
implements ITemporalSRelation {

	protected final static int BIDX = 0;

	protected TaskVar task1, task2;

	protected int k1, k2;

	public AbstractPrecedenceSConstraint(IntDomainVar[] vars) {
		super(ConstraintEvent.LINEAR, vars);
	}

	@Override
	public int getFilteredEventMask(int idx) {
		return idx == 0 ? IntVarEvent.INSTINT_MASK : IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
	}


	public final void setTasks(final TaskVar t1, final TaskVar t2) {
		this.task1 = t1;
		this.task2 = t2;
	}
	// TODO - Record temporal constraint in the constraint list of takss ? - created 4 juil. 2011 by Arnaud Malapert//TODO record tasks in the constraint list ? 
	//In this case, change the postRedundantTaskConstraint

	@Override
	public final TaskVar getOrigin() {
		return task1;
	}

	@Override
	public final TaskVar getDestination() {
		return task2;
	}

	@Override
	public IntDomainVar getDirection() {
		return vars[BIDX];
	}

	@Override
	public final boolean isFixed() {
		return vars[BIDX].isInstantiated();
	}

	@Override
	public int getDirVal() {
		return vars[BIDX].getVal();
	}

	@Override
	public final int backwardSetup() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public final int forwardSetup() {
		throw new UnsupportedOperationException("not yet implemented");
	}



	/**
	 * propagate vars[idx1] <= vars[idx2]
	 */
	protected final void propagate(final int idx1, final int idx2) throws ContradictionException {
		vars[idx2].updateInf(vars[idx1].getInf(), this, false);
		vars[idx1].updateSup(vars[idx2].getSup(), this, false);
	}

	/**
	 * propagate vars[idx1] + k1 <= vars[idx2]
	 */
	protected final void propagate(final int idx1, final int k1, final int idx2) throws ContradictionException {
		vars[idx2].updateInf(vars[idx1].getInf() + k1, this, false);
		vars[idx1].updateSup(vars[idx2].getSup() - k1, this, false);
	}


	public abstract void propagateP1() throws ContradictionException;

	public abstract void propagateP2() throws ContradictionException;

	/**
	 * isEntailed vars[idx1] <= vars[idx2]
	 */
	protected final Boolean isEntailed(final int idx1, final int idx2) {
		if (vars[idx1].getSup() <= vars[idx2].getInf())
			return Boolean.TRUE;
		if (vars[idx1].getInf() > vars[idx2].getSup())
			return Boolean.FALSE;
		return null;

	}

	/**
	 * isEntailed vars[idx1] + k1 <= vars[idx2]
	 */

	protected final Boolean isEntailed(final int idx1, final int k1, final int idx2) {
		if (vars[idx1].getSup() + k1 <= vars[idx2].getInf())
			return Boolean.TRUE;
		if (vars[idx1].getInf() + k1 > vars[idx2].getSup())
			return Boolean.FALSE;
		return null;
	}



	public abstract Boolean isP1Entailed();


	public abstract Boolean isP2Entailed();


	protected final boolean isSatisfied(final int idx1, final int idx2) {
		return vars[idx1].getVal() <= vars[idx2].getVal();
	}

	protected final boolean isSatisfied(final int idx1, final int k1, final int idx2) {
		return vars[idx1].getVal() + k1 <= vars[idx2].getVal();
	}

	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		if (idx == 0) {        // booleen de decision
			if (vars[BIDX].getVal() == FWD) propagateP1();
			else propagateP2();
		} else {
			filterOnP1P2TowardsB();
		}
	}

	// TODO - Change to private - created 17 avr. 2012 by A. Malapert
	protected Boolean reuseBool;
	//idx = 1 ou idx = 2
	public void filterOnP1P2TowardsB() throws ContradictionException {
		//				Boolean b = isP1Entailed();
		//				if (b != null) {
		//					if (b) {
		//						v0.instantiate(1, cIdx0);
		//					} else {
		//						v0.instantiate(0, cIdx0);
		//		                propagateP2();
		//					}
		//				}
		//				b = isP2Entailed();
		//				if (b != null) {
		//					if (b) {
		//						v0.instantiate(0, cIdx0);
		//					} else {
		//						v0.instantiate(1, cIdx0);
		//		                propagateP1();
		//					}
		//		        }
		reuseBool = isP1Entailed();
		if (reuseBool == Boolean.TRUE) {
			vars[BIDX].instantiate(FWD, this, false);
		} else if(reuseBool == Boolean.FALSE){
			vars[BIDX].instantiate(BWD, this, false);
			propagateP2();
		}else {
			reuseBool = isP2Entailed();
			if (reuseBool == Boolean.TRUE) { 
				vars[BIDX].instantiate(BWD, this, false);
			} else if(reuseBool == Boolean.FALSE) {
				vars[BIDX].instantiate(FWD, this, false);
				propagateP1();
			}
		}
	}

	@Override
	public final void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
		LOGGER.warning("awakeOnRemovals sould be inactive");
	}


	@Override
	public final void awakeOnSup(final int idx) throws ContradictionException {
		awakeOnBounds(idx);
	}

	@Override
	public final void awakeOnInf(final int idx) throws ContradictionException {
		awakeOnBounds(idx);
	}

	@Override
	public void awakeOnBounds(final int idx) throws ContradictionException {
		propagate();
	}

	@Override
	public void propagate() throws ContradictionException {
		if (vars[BIDX].isInstantiatedTo(0))
			propagateP2();
		else if (vars[BIDX].isInstantiatedTo(1))
			propagateP1();
		else filterOnP1P2TowardsB(); //idx ne peut pas valoir 0 ici		
	}


	protected final String pretty(final int idx1, final int k1, final int idx2) {
		return vars[idx1]+" + "+k1+" <= "+vars[idx2];
	}

	protected final String pretty(final int idx1, final int idx2) {
		return vars[idx1]+" <= "+vars[idx2];
	}

	protected final String pretty(String name, String trueStr, String falseStr) {
		return name +" "+ vars[BIDX] + " ( "+
				( vars[BIDX].isInstantiatedTo(1) ? trueStr : 
					vars[BIDX].isInstantiatedTo(0) ? falseStr : trueStr+" || "+falseStr)
					+" )";
	}


	@Override
	public int getTotalSlack() {
		return TaskUtils.getTotalSlack(task1, task2);
	}

	@Override
	public double getForwardPreserved() {
		return TaskUtils.getPreserved(task1, task2);
	}

	@Override
	public double getBackwardPreserved() {
		return TaskUtils.getPreserved(task2, task1);
	}

	@Override
	public String toString() {
		return pretty();
	}

}
