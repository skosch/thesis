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
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;


interface IAltRTask {

	public final static int ELIMINATED=0;	
	public final static int REGULAR=1;
	
	IntDomainVar getUsage();
	
	boolean isOptional();

	boolean isRegular();

	boolean isEliminated();

	boolean assign() throws ContradictionException;

	boolean remove() throws ContradictionException;
	
	void fireRemoval();

}

interface IEnergyRTask {

	long getMinConsumption();

	long getMaxConsumption();

	//IntDomainVar getConsumption();

}


interface ICumulRTask extends IEnergyRTask {

	int getMinHeight();

	int getMaxHeight();

	IntDomainVar getHeight();

	boolean updateMaxHeight(int val) throws ContradictionException;

	boolean updateMinHeight(int val) throws ContradictionException;

}

/**
 * Update operations update the domain and ensure task consistency whereas set operations update the domain without checking. 
 * @author Arnaud Malapert</br> 
 * @since 4 sept. 2009 version 2.1.1</br>
 * @version 2.1.1</br>
 */
public interface IRTask extends ICumulRTask,IAltRTask, IPretty {
	
	int getTaskIndex();

	TaskVar getTaskVar();
	
	ITask getHTask();

	void checkConsistency() throws ContradictionException;
	
	void updateCompulsoryPart() throws ContradictionException;

	void fail() throws ContradictionException;

	/**
	 * Update the Earliest Completion Time (ECT).
	 */
	boolean updateECT(final int val) throws ContradictionException;

	/**
	 * Update the Earliest Starting Time (EST).
	 */
	boolean updateEST(final int val) throws ContradictionException;

	/**
	 * Update the Latest Completion Time (LCT).
	 *
	 */
	boolean updateLCT(final int val) throws ContradictionException;

	/**
	 * Update the Latest Starting Time (LST).
	 */
	boolean updateLST(final int val) throws ContradictionException;

	/**
	 * The task can not start in the interval [a,b].
	 */
	boolean updateStartNotIn(final int a, final int b) throws ContradictionException;
	
	/**
	 * The task can not end in the interval [a,b].
	 */
	boolean updateEndNotIn(final int a, final int b) throws ContradictionException;

	boolean updateMinDuration(final int val) throws ContradictionException;

	boolean updateMaxDuration(final int val) throws ContradictionException;

	boolean updateDuration(final int duration) throws ContradictionException;

	boolean updateStartingTime(final int startingTime) throws ContradictionException;

	boolean updateEndingTime(final int endingTime) throws ContradictionException;


	/**
	 * Update the Earliest Completion Time (ECT).
	 */
	boolean setECT(final int val) throws ContradictionException;

	/**
	 * Update the Earliest Starting Time (EST).
	 */
	boolean setEST(final int val) throws ContradictionException;

	/**
	 * Update the Latest Completion Time (LCT).
	 *
	 */
	boolean setLCT(final int val) throws ContradictionException;

	/**
	 * Update the Latest Starting Time (LST).
	 */
	boolean setLST(final int val) throws ContradictionException;

	/**
	 * The task can not start in the interval [a,b].
	 */
	boolean setStartNotIn(final int a, final int b) throws ContradictionException;
	
	/**
	 * The task can not end in the interval [a,b].
	 */
	boolean setEndNotIn(final int a, final int b) throws ContradictionException;

	boolean setMinDuration(final int val) throws ContradictionException;

	boolean setMaxDuration(final int val) throws ContradictionException;

	boolean setDuration(final int duration) throws ContradictionException;

	boolean setStartingTime(final int startingTime) throws ContradictionException;

	boolean setEndingTime(final int endingTime) throws ContradictionException;

	/**
	 * Utility: A filtering algorithm can store a value to perform update operations (noargs) later.
	 */
	void storeValue(int val);
	
	int getStoredValue();
	
	/**
	 * Update using {@link IRTask#getStoredValue()}   .
	 */
	boolean updateECT() throws ContradictionException;
	
	/**
	 * Update using {@link IRTask#getStoredValue()}   .
	 */
	boolean updateEST() throws ContradictionException;
	
	/**
	 * Update using {@link IRTask#getStoredValue()}   .
	 */
	boolean updateLCT() throws ContradictionException;
	
	/**
	 * Update using {@link IRTask#getStoredValue()}   .
	 */
	boolean updateLST() throws ContradictionException;
}
