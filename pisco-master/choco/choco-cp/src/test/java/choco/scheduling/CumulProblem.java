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

package choco.scheduling;

import choco.Choco;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 26 avr. 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
class CumulProblem extends AbstractTestProblem {

	public IntegerVariable[] heights;

	public IntegerVariable capacity;

	public IntegerVariable consumption = Choco.constant(0);


	public CumulProblem(IntegerVariable[] starts, IntegerVariable[] durations, IntegerVariable[] heights) {
		super(starts, durations);
		this.heights = heights;
	}

	public CumulProblem(IntegerVariable[] durations, IntegerVariable[] heights) {
		this(null, durations, heights);
	}

	public CumulProblem(int[] durations, int[] heights) {
		this(null, choco.Choco.constantArray(durations), choco.Choco.constantArray(heights));
	}

	public final void setCapacity(int capacity) {
		this.capacity = choco.Choco.constant(capacity);
	}

	@Override
	protected Constraint[] generateConstraints() {
		return new Constraint[]{Choco.cumulative(null, tasks, heights, consumption, capacity)};
	}

	public void generateSolver() {
		generateSolver(null);
	}
	
	@Override
	public void generateSolver(Configuration conf) {
		super.generateSolver(conf);
		this.setFlags(TestCumulative.SETTINGS);
	}

}
