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

package choco.kernel.solver.search;

import choco.IPretty;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.search.restart.UniversalRestartStrategy;


/**
 * The interface of objects limiting the global search exploration
 */
public interface GlobalSearchLimitManager extends IPretty {


	/**
	 * @return strategy the controller of the search exploration, managing the limit
	 */
	AbstractGlobalSearchStrategy getSearchStrategy();

	/**
	 * initialize the limit.
	 */
	void initialize();

	/**
	 * resets the limit (the counter run from now on)
	 *
	 */
	void reset();

	/**
	 * notify the limit object whenever a new node is created in the search tree
	 *
	 * @throws ContradictionException if the limit does not accept the creation of the new node.
	 */
	void newNode() throws ContradictionException;

	/**
	 * notify the limit object whenever the search closes a node in the search tree
	 *
	 *
	 * @throws ContradictionException if the limit does not accept the death of the node.
	 */
	void endNode() throws ContradictionException;

	void endTreeSearch();

	/**
	 * notify the limit object whenever the search has been restarted.
	 * return <code>true</code> if the limit does stop the restart process.
	 */
	boolean newRestart();
	
	/**
	 * Get the time in milliseconds elapsed since the beginning of the search.
	 */
	int getTimeCount();
	
	/**
	 * get the restart strategy, if any.
	 */
	UniversalRestartStrategy getRestartStrategy();
	
	/**
	 * cancel the restart strategy.
	 */
	void cancelRestartStrategy();
	
	/**
	 * get the number of restarts caused by the restart strategy.
	 * @return
	 */
	int getRestartFromStrategyCount();
	
	
}

