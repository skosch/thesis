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

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.real.RealVar;
import choco.kernel.solver.variables.set.SetVar;

public interface IntBranchingDecision {
	
	/**
	 * get the branching object. It is often a variable
	 */
	Object getBranchingObject();
	
	/**
	 * get the next value to try, if any (optional).
	 */
	int getBranchingValue();
	
	/**
	 * set the next value to try.
	 */
	void setBranchingValue(final int branchingValue);
	
	/**
	 * get the index of the current alternative (branch).
	 * @return
	 */
	int getBranchIndex();
	
	/**
	 * get and cast the branching object.
	 */
	IntDomainVar getBranchingIntVar();
	
	/**
	 * get and cast the branching object.
	 */
	SetVar getBranchingSetVar();
	/**
	 * get and cast the branching object.
	 */
	RealVar getBranchingRealVar();
	
	//utility functions
	/**
	 * apply the integer assignment decision, i.e. assign the branching value to the branching int var.
	 */
	void setIntVal() throws ContradictionException;
	
	/**
	 * apply the integer removal decision, i.e. remove the branching value from the domain of the branching int var.
	 */
	void remIntVal() throws ContradictionException;
	
	/**
	 * apply the set assignment decision, i.e. put the value into the kernel.
	 */
	void setValInSet() throws ContradictionException;

	/**
	 * apply the set removal decision, i.e. remove the value from the enveloppe.
	 */
	void setValOutSet() throws ContradictionException;
}
