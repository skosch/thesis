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

package choco.cp.solver.constraints.global.scheduling.trees;


import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.ITask;

/**
 * @author Arnaud Malapert</br> 
 * @since 10 févr. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public interface IThetaLambdaTree extends IThetaTree {

	int getGrayTime();

	Object getResponsibleTask();

	
	boolean insertInLambda(IRTask task);
	
	boolean removeFromLambda(ITask task);
	/**
	 * remove from the set thea and insert the atsk into lambda (optional operation).
	 * If some additional information is needed, it throws an {@link UnsupportedOperationException} and you should use {@link IThetaLambdaTree#removeFromThetaAndInsertInLambda(IRTask)}
	 * @param task
	 * @return <code>true</code> if it succeeds
	 */
	boolean removeFromThetaAndInsertInLambda(ITask task);
	
	
	boolean removeFromThetaAndInsertInLambda(IRTask task);
	
}