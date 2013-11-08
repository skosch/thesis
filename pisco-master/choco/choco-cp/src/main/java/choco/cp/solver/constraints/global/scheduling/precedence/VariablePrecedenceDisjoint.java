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

package choco.cp.solver.constraints.global.scheduling.precedence;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 *  Let b be a boolean variables; x0, x1 be two integer variables and k1, k2 two integers.
 * This constraint enforce x0 before x1 if b is true or x1 before x0 if b is false.
 * b0 = 1 <=> x0 + d0 <= x1
 * b0 = 0 <=> x1 + d1 <= x0
 * */
public class VariablePrecedenceDisjoint extends AbstractPrecedenceSConstraint {

		
    public VariablePrecedenceDisjoint(IntDomainVar b, IntDomainVar s0, IntDomainVar d0,
                                      IntDomainVar s1, IntDomainVar d1) {
        super(new IntDomainVar[]{b,s0,d0,s1,d1});
    }

        
    // propagate x0 + d0 <= x1 (b0 = 1)
	@Override
	public void propagateP1() throws ContradictionException {
		boolean b = true;
        while(b) {
            b = false;
            b |= vars[3].updateInf(vars[1].getInf() + vars[2].getInf(), this, false);
            b |= vars[1].updateSup(vars[3].getSup() - vars[2].getInf(), this, false);
            b |= vars[2].updateSup(vars[3].getSup() - vars[1].getInf(), this, false);
        }
    }

    // propagate x1 + d1 <= x0 (b0 = 0)
    @Override
	public void propagateP2() throws ContradictionException {
		boolean b = true;
        while(b) {
            b = false;
            vars[1].updateInf(vars[3].getInf() + vars[4].getInf(), this, false);
            vars[3].updateSup(vars[1].getSup() - vars[4].getInf(), this, false);
            vars[4].updateSup(vars[1].getSup() - vars[3].getInf(), this, false);
        }
    }

	@Override
	public Boolean isP1Entailed() {
		if (vars[1].getSup() + vars[2].getSup() <= vars[3].getInf())
			return Boolean.TRUE;
		if (vars[1].getInf() + vars[2].getInf() > vars[3].getSup())
			return Boolean.FALSE;
		return null;
	}

	@Override
	public Boolean isP2Entailed() {
		if (vars[3].getSup() + vars[4].getSup() <= vars[1].getInf())
			return Boolean.TRUE;
		if (vars[3].getInf() + vars[4].getInf() > vars[1].getSup())
			return Boolean.FALSE;
		return null;
	}


	@Override
	public boolean isSatisfied() {
		if (vars[BIDX].isInstantiatedTo(1))
			return vars[1].getVal() + vars[2].getVal() <= vars[3].getVal();
		else return vars[3].getVal() + vars[4].getVal() <= vars[1].getVal();
	}

	@Override
	public String pretty() {
		return "VDisjunction " + vars[1] +","+ vars[2]+ " - " + vars[3] + "," + vars[4];
	}

	
}
