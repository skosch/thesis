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

package choco.kernel.solver.search.set;

import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.AbstractSearchHeuristic;
import choco.kernel.solver.variables.set.SetVar;

public abstract class AbstractSetVarSelector extends AbstractSearchHeuristic implements VarSelector<SetVar> {

    /**
     * a specific array of SetVars from which the object seeks the one with smallest domain
     */
    protected final SetVar[] vars;


    public AbstractSetVarSelector(Solver solver) {
        this(solver, VariableUtils.getSetVars(solver));
    }


    public AbstractSetVarSelector(Solver solver, SetVar[] vars) {
        super(solver);
        this.vars = vars;
    }

    public SetVar selectVar() {
        int min = Integer.MAX_VALUE;
        SetVar v0 = null;
        final int n = vars.length;
        for (int i = 0; i < n; i++) {
            SetVar v = vars[i];
            if (!v.isInstantiated()) {
                int domSize = getHeuristic(v);
                if (domSize < min) {
                    min = domSize;
                    v0 = v;
                }
            }
        }
        return v0;
    }

    /**
     * Get decision vars
     *
     * @return decision vars
     */
    public SetVar[] getVars() {
        return vars;
    }

    /**
     * Set decision vars
     *
     * @return decision vars
     */
    @Deprecated
    public void setVars(SetVar[] vars) {
        throw new UnsupportedOperationException("setVars is final");
    }

    public abstract int getHeuristic(SetVar v);

}
