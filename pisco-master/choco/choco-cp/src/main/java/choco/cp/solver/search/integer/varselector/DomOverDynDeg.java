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

import choco.cp.solver.search.BranchingFactory;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.search.integer.DoubleHeuristicIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/* 
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 10 oct. 2006
 * Since : Choco 2.0.0
 *
 */
/**
 * @deprecated @see {@link BranchingFactory}
 */
@Deprecated
public class DomOverDynDeg extends DoubleHeuristicIntVarSelector {
	public DomOverDynDeg(Solver solver) {
		super(solver);

	}

	public DomOverDynDeg(Solver solver, IntDomainVar[] vs) {
		super(solver, vs);
	}

	@Override
	public double getHeuristic(IntDomainVar v) {
		int dsize = v.getDomainSize();
		int deg = getDynDeg(v);
		if (deg == 0)
			return Double.MAX_VALUE;
		else
			return (double) dsize / (double) deg;
	}

	public int getDynDeg(IntDomainVar v) {
		int ddeg = 0;
		int idx = 0;
		DisposableIntIterator it = v.getIndexVector().getIndexIterator();
		while (it.hasNext()) {
			idx = it.next();
			AbstractSConstraint ct = (AbstractSConstraint) v.getConstraint(idx);
			if ( ct.getNbVarNotInst() > 1) {
				ddeg+= ct.getFineDegree(v.getVarIndex(idx));
			}
		}
		return ddeg;
	}

}
