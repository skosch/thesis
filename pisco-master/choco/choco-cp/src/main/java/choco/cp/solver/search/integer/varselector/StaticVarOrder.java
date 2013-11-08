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

import choco.kernel.memory.IStateInt;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A variable selector selecting the first non instantiated variable according to a given static order
 */
public class StaticVarOrder extends AbstractIntVarSelector {

    private final IStateInt last;

    public StaticVarOrder(Solver solver) {
        super(solver);
        this.last = solver.getEnvironment().makeInt(0);
    }

    public StaticVarOrder(Solver solver, IntDomainVar[] vars) {
        super(solver, vars);
        this.last = solver.getEnvironment().makeInt(0);
    }

    /**
     * Select what seems to be most interesting variable, considering the current state of the variables
     * @return
     */
    public IntDomainVar selectVar() {
        //<hca> it starts at last.get() and not last.get() +1 to be
        //robust to restart search loop
        for (int i = last.get(); i < vars.length; i++) {
            if (!vars[i].isInstantiated()) {
                last.set(i);
                return vars[i];

            }
        }
        return null;
    }
}
