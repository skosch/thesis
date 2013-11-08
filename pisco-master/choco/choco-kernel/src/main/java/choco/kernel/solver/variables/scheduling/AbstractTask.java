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

package choco.kernel.solver.variables.scheduling;

import java.awt.Point;

import choco.Choco;
import choco.kernel.common.util.tools.StringUtils;

public abstract class AbstractTask implements ITask {

	
	private final ITimePeriodList timePeriodList;
		
	/**
	 * Warning: preemption should not be allowed.
	 */
	public AbstractTask() {
		super();
		timePeriodList = new SingleTimePeriod();
	}
	/**
	 * possibly allow preemption.
	 */
	public AbstractTask(ITimePeriodList timePeriodList) {
		super();
		this.timePeriodList = timePeriodList;
	}

	@Override
	public String getName() {
		return "T"+getID();
	}
	
	@Override
	public boolean hasConstantDuration() {
		return getMinDuration() == getMaxDuration();
	}

	@Override
	public int getECT() {
		return getEST() + getMinDuration();
	}

	@Override
	public int getLST() {
		return getLCT() - getMinDuration();
	}

	@Override
	public String toDotty() {
		return StringUtils.toDotty(this, null, true);
	}


	@Override
	public String pretty() {
		return StringUtils.pretty(this);					
	}


	@Override
	public String toString() {
		return pretty();
	}

	@Override
	public boolean isScheduled() {
		return timePeriodList.getExpendedDuration() == getMaxDuration();
	}

	@Override
	public final boolean isPartiallyScheduled() {
		return ! timePeriodList.isEmpty();
	}

	@Override
	public final boolean isInterrupted() {
		return timePeriodList.getTimePeriodCount() > 1;
	}
	
	@Override
	public final ITimePeriodList getTimePeriodList() {
		return timePeriodList;
	}

	/**
	 * 
	 * The mandatory part of the task. 
	 */
	private final class SingleTimePeriod implements ITimePeriodList {

		@Override
		public final void reset() {}

		@Override
		public final int getExpendedDuration() {
			return getECT() - getLST();
		}

		@Override
		public final boolean isEmpty() {
			return getExpendedDuration() <= 0;
		}

		@Override
		public final int getTimePeriodCount() {
			return isEmpty() ? 0 : 1;
		}

		@Override
		public final Point getTimePeriod(int i) {
			return i == 0 && ! isEmpty() ? new Point(getLST(), getECT()) : null ;
		}

		
		@Override
		public int getPeriodFirst() {
			return getLST();
		}

		@Override
		public int getPeriodLast() {
			return getECT();
		}

		@Override
		public final int getPeriodStart(int i) {
			return i == 0 ? getLST() : Choco.MIN_LOWER_BOUND;
		}

		@Override
		public final int getPeriodEnd(int i) {
			return i == 0 ? getECT() : Choco.MIN_LOWER_BOUND;
		}
	}
}
