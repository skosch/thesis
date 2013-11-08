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

package choco.kernel.solver.search.measure;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.propagation.listener.PropagationEngineListener;


/**
 * Measure counting the number of fails
 */
public class FailMeasure implements PropagationEngineListener {

	private final PropagationEngine propagationEngine;

	private int failCount = Integer.MIN_VALUE;

	public FailMeasure(PropagationEngine propagationEngine) {
		super();
		this.propagationEngine = propagationEngine;
	}


	/**
	 * Define action to do just before a addition.
	 */
	public final void safeAdd() {
		if ( ! propagationEngine.containsPropagationListener(this)) {
			propagationEngine.addPropagationEngineListener(this);
			failCount = 0;
		}
	}
	/**
	 * Define action to do just before a deletion.
	 */
	@Override
	public final void safeDelete() {
		propagationEngine.removePropagationEngineListener(this);
		failCount = Integer.MIN_VALUE;
	}

	public final void safeReset() {
		failCount = propagationEngine.containsPropagationListener(this) ? 0 : Integer.MIN_VALUE;
	}

	public final int getFailCount() {
		return failCount;
	}

	public void contradictionOccured(ContradictionException e) {
		if(!e.isSearchLimitCause()) {failCount++;}
	}
}

