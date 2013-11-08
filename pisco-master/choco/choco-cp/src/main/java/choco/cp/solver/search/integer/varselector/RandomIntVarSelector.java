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

package choco.cp.solver.search.integer.varselector;

import choco.kernel.solver.Solver;
import choco.kernel.solver.search.integer.IntHeuristicIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.Random;

public class RandomIntVarSelector extends IntHeuristicIntVarSelector {

	private ArrayList<IntDomainVar> reuseList = new ArrayList<IntDomainVar>();
	protected final Random random;

	/**
	 * Creates a new random-based integer domain variable selector
	 */
	public RandomIntVarSelector(Solver solver) {
		super(solver);
		this.random = new Random();
	}

    /**
	 * Creates a new random-based integer domain variable selector with the specified seed
	 * (to make the experiment determinist)
	 */
	public RandomIntVarSelector(Solver solver, long seed) {
		super(solver);
		this.random = new Random(seed);
	}

	public RandomIntVarSelector(Solver solver, IntDomainVar[] vs, long seed) {
		super(solver, vs);
		this.random = new Random(seed);
	}

    @Override
    public int getHeuristic(IntDomainVar v) {
        return random.nextInt();
    }

}
