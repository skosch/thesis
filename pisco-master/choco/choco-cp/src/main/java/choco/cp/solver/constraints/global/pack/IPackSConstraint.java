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

package choco.cp.solver.constraints.global.pack;

import choco.kernel.common.opres.nosum.NoSumList;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * @author Arnaud Malapert</br>
 * @since 5 déc. 2008 <b>version</b> 2.0.1</br>
 * @version 2.0.3</br>
 */
public interface IPackSConstraint {

	int getNbBins();
	
	int getNbItems();
	
	IntDomainVar[] getBins();
	
	IntDomainVar[] getLoads();

	int[] getSizes();

	void fail() throws ContradictionException;

	NoSumList getStatus(int bin);
	
	IStateIntVector getAvailableBins();

	void fireAvailableBins();
	
	/**
	 * Update the minimal load of a given bin.
	 *
	 * @param bin the index of bin
	 * @param load the new load
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	boolean updateInfLoad(final int bin,final int load) throws ContradictionException;


	/**
	 * Update the maximal load of a given bin.
	 *
	 * @param bin the index of bin
	 * @param load the new load
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	boolean updateSupLoad(final int bin,final int load) throws ContradictionException;

	/**
	 * update the number of non empty bins.
	 *
	 */
	boolean updateNbNonEmpty(int min, int max) throws ContradictionException;

	/**
	 * Pack an item into a bin
	 * @return true, if successful
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	boolean pack(final int item,final int bin) throws ContradictionException;


	/**
	 * Remove a possible assignment of an item into a bin.
	 *
	 * @return true, if successful
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	boolean remove(final int item,final int bin) throws ContradictionException;

}
