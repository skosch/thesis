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

package choco.kernel.solver.goals.choice;


import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.AbstractBranchingStrategy;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.branch.BranchingWithLoggingStatements;
import choco.kernel.solver.goals.Goal;
import choco.kernel.solver.goals.GoalType;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: grochart
 * Date: 19 mai 2007
 * Time: 10:28:16
 * To change this template use File | Settings | File Templates.
 */
public final class SetVal implements Goal {


	protected IntDomainVar var;
	protected int val;

	public SetVal(IntDomainVar var, int val) {
		this.var = var;
		this.val = val;
	}

	public String pretty() {
		return var.pretty() + " <= " + val;
	}

	public Goal execute(Solver s) throws ContradictionException {
		if (LOGGER.isLoggable(Level.INFO)) { 
			final int ws = s.getWorldIndex();
			if( ws <= s.getLoggingMaxDepth() ) {
				LOGGER.log(Level.INFO, "{0}{1} {2} {3} {4}", new Object[]{BranchingWithLoggingStatements.makeLoggingMsgPrefix(ws), AbstractBranchingStrategy.LOG_DOWN_MSG, var, AbstractIntBranchingStrategy.LOG_DECISION_MSG_ASSIGN, val});
			}
		}
		var.setVal(val);
		return null;
	}

    @Override
    public GoalType getType() {
        return GoalType.SET;
    }
}