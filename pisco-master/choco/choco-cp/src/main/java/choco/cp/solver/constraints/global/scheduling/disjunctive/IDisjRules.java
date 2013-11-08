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

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.scheduling.IRTask;

/**
 * 
 * The interface represents the classical filtering rules for an unary resource.
 * @author Arnaud Malapert</br> 
 * @since 23 févr. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public interface IDisjRules {
	
	void initialize();
	
	void fireDomainChanged();
	
	boolean isActive();
	
	void overloadChecking() throws ContradictionException;

	boolean notFirst() throws ContradictionException;

	boolean notLast() throws ContradictionException;
	
	boolean notFirstNotLast() throws ContradictionException;
	
	boolean detectablePrecedenceEST() throws ContradictionException;

	boolean detectablePrecedenceLCT() throws ContradictionException;
	
	boolean detectablePrecedence() throws ContradictionException;
	
	boolean edgeFindingEST() throws ContradictionException;
	
	boolean edgeFindingLCT() throws ContradictionException;
	
	boolean edgeFinding() throws ContradictionException;
	
	/** optional operation */
	void remove(IRTask rtask);
		
}