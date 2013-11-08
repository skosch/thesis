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

package choco.cp.model.managers.constraints.global;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.precedence.PrecedenceDisjoint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;



/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 11 août 2008
 * Time: 16:49:22
 */
public final class PrecedenceDisjointManager extends AbstractPrecedenceManager {

	
	@Override
	protected SConstraint makeIntConstraintB0(CPSolver s, IntDomainVar x1,
			int k1, IntDomainVar x2, int k2) {
		return s.leq( s.plus(x2,k2), x1);
	}

	@Override
	protected SConstraint makeIntConstraint(CPSolver s, IntDomainVar x1,
			int k1, IntDomainVar x2, int k2, IntDomainVar dir) {
		return new PrecedenceDisjoint(x1, k1, x2, k2, dir);
	}
	

	@Override
	protected SConstraint makeTaskConstraintB0(CPSolver s, TaskVar t1, int k1,
			TaskVar t2, int k2) {
		return s.preceding(t2, k2, t1);
	}
	
	@Override
	protected SConstraint makeTaskConstraintB1(CPSolver s, TaskVar t1, int k1,
			TaskVar t2, int k2) {
		return s.preceding(t1, k1, t2);
	}

	
	@Override
	protected SConstraint makeTaskConstraint(CPSolver s, TaskVar t1, int k1,
			TaskVar t2, int k2, IntDomainVar dir) {
			//post into solver constraints;
			return s.preceding(dir, t1, k1, t2, k2);
	}

}
