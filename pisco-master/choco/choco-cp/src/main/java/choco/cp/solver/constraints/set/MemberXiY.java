/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
package choco.cp.solver.constraints.set;

import choco.cp.model.managers.MixedConstraintManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.cp.solver.variables.set.SetVarEvent;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.set.AbstractLargeSetIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;

import java.util.List;

/**
 * MEMBERXiY(&#9001;X1,...,Xn&#9002;, S) ensures that:
 * <br/> &#8704; i in [1,n], Xi &#8712; S
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28/06/11
 */
public class MemberXiY extends AbstractLargeSetIntSConstraint {

    public static Constraint build(IntegerVariable[] ivars, SetVariable svar) {
        return new ComponentConstraint(MemberXiY.MemberXiYManager.class, null, ArrayUtils.append(ivars, new Variable[]{svar}));
    }

    public static class MemberXiYManager extends MixedConstraintManager {
        @Override
        public SConstraint makeConstraint(Solver solver, Variable[] variables, Object parameters, List<String> options) {
            if (solver instanceof CPSolver) {
                IntDomainVar[] ivars = new IntDomainVar[variables.length - 1];
                for (int i = 0; i < variables.length - 1; i++) {
                    ivars[i] = solver.getVar(variables[i]);
                }
                SetVar svar = solver.getVar(variables[variables.length - 1]);
                return new MemberXiY(svar, ivars);
            }
            throw new UnsupportedOperationException("bouh!");
        }
    }

    public MemberXiY(SetVar setvar, IntDomainVar[] intvars) {
        super(intvars, new SetVar[]{setvar});
    }

    @Override
    public int getFilteredEventMask(int idx) {
        if(isSetVarIndex(idx)){
            return SetVarEvent.REMENV_MASK + SetVarEvent.INSTSET_MASK;
        }
        return IntVarEvent.INSTINT_MASK;
    }

    @Override
    public void propagate() throws ContradictionException {
        for (int i = 0; i < ivars.length; i++) {
            if (ivars[i].isInstantiated()) {
                svars[0].addToKernel(ivars[i].getVal(), this, false);
            }
        }
        filter();
    }

    @Override
    public void awakeOnInst(int varIdx) throws ContradictionException {
        if (isIntVarIndex(varIdx)) {
            int idx = getIntVarIndex(varIdx);
            svars[0].addToKernel(ivars[idx].getVal(), this, false);
        } else {
            filter();
        }
    }

    private void filter() throws ContradictionException {
        SetVar svar = svars[0];
        for (int k = 0; k < ivars.length; k++) {
            int left = Integer.MIN_VALUE;
            int right = left;
            IntDomainVar var = ivars[k];
            int UB = ivars[k].getSup();
            for (int val = ivars[k].getInf(); val <= UB; val = ivars[k].getNextDomainValue(val)) {
                if (!svar.isInDomainEnveloppe(val)) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        var.removeInterval(left, right, this, false);
                        left = val;
                        right = val;
                    }
                }
            }
            var.removeInterval(left, right, this, false);
        }
    }

    @Override
    public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
        // check no variable is instantiated to x
        for (int i = 0; i < ivars.length; i++) {
            if (ivars[i].isInstantiatedTo(x)) {
                this.fail();
            }
        }
    }

    @Override
    public boolean isSatisfied() {
        SetVar svar = svars[0];
        for (int i = 0; i < ivars.length; i++) {
            if (!svar.isInDomainKernel(ivars[i].getVal())) {
                return false;
            }
        }
        return true;
    }
}
