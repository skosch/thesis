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

package choco.cp.solver.constraints.global.scheduling.cumulative;


import choco.kernel.solver.ContradictionException;

public interface ICumulRules {

	/**
	 * fast task intervals in n*log(n)
	 */
	void taskIntervals() throws ContradictionException;

	/**
	 * a basic n^2 tasks interval
	 */
	void slowTaskIntervals() throws ContradictionException;

	/**
	 * reset all the flags for dynamic computation of R
	 */
	void reinitConsumption();

	/**
	 * Initialize some data structure for the edge finding.
	 * If the height are constant, this is done only once
	 * at the beginning, otherwise it has to be recomputed at each call.
	 * Shall we maintain it incrementally ?
	 */
	void initializeEdgeFindingData();
	
	void initializeEdgeFindingStart();
	
	void initializeEdgeFindingEnd();

	/**
	 * Edge finding algorithm for starting dates in O(n^2 \times k) where
	 * k is the number of distinct heights.
	 */
	boolean calcEF_start() throws ContradictionException;

	/**
	 * Edge finding algorithm for starting dates in O(n^2 \times k) where
	 * k is the number of distinct heights. Vilim version based on the theta-
	 * lambda tree.
	 */
	boolean vilimStartEF() throws ContradictionException;

	/**
	 * Edge finding algorithm for ending dates in O(n^2 \times k) where
	 * k is the number of distinct heights.
	 */
	boolean calcEF_end() throws ContradictionException;

	/**
	 * Edge finding algorithm for ending dates in O(n^2 \times k) where
	 * k is the number of distinct heights. Vilim version based on the theta-
	 * lambda tree.
	 */
	boolean vilimEndEF() throws ContradictionException;

}