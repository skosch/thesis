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



import choco.IPretty;
import choco.kernel.common.IDotty;


/**
 * The Interface ITask represent a scheduling entity : a task, activity, job.
 *
 * @author Arnaud Malapert
 */
public interface ITask extends IDotty, IPretty {


	/**
	 * Gets the ID of the task.
	 *
	 * @return the iD
	 */
	int getID();

	/**
	 * Gets the name of the task.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Gets the Earliest Starting Time (EST).
	 *
	 * @return the EST
	 */
	int getEST();

	/**
	 * Gets the Earliest Completion Time (ECT).
	 *
	 * @return the ECT
	 */
	int getECT();

	/**
	 * Gets the Latest Starting Time (LST).
	 *
	 * @return the LST
	 */
	int getLST();

	/**
	 * Gets the Latest Completion Time (LCT).
	 *
	 * @return the LCT
	 */
	int getLCT();


	/**
	 * Gets the minimum duration.
	 *
	 * @return the minimum duration the task
	 */
	int getMinDuration();

	/**
	 * Gets the maximum duration.
	 *
	 * @return the max duration
	 */
	int getMaxDuration();

	/**
	 * Checks for if the duration is constant.
	 *
	 * @return true, if the duration is constant.
	 */
	boolean hasConstantDuration();

	/**
	 * Checks if the task is scheduled. The task is scheduled if its starting time and its duration are fixed.
	 *
	 * @return true, if the tasks is scheduled
	 */
	boolean isScheduled();

	/**
	 * Checks whether the preemption is allowed.
	 *
	 * @return true, if preemption is allowed
	 */
	boolean isPreemptionAllowed();

	/**
	 * Checks whether a preemptive task has been partially scheduled.
	 *
	 * @return true, if a preemptive task has been partially scheduled.
	 */
	boolean isPartiallyScheduled();
		
	/**
	 * Checks if the task is interrupted (preempted).
	 *
	 * @return true, if the tasks is interrupted
	 */
	boolean isInterrupted();

	/**
	 * Gets the list of time periods in which the task is executed
	 *
	 * @return a list of time period.
	 */
	ITimePeriodList getTimePeriodList();
	
	
	
}




