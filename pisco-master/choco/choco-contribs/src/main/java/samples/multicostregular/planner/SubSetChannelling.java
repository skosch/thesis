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

package samples.multicostregular.planner;

import choco.cp.model.managers.IntConstraintManager;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Jan 19, 2009
 * Time: 12:55:34 PM
 */
public class SubSetChannelling extends AbstractLargeIntSConstraint {

    int n;

    public SubSetChannelling(IntDomainVar[] vars) {
        super(vars);
        this.n = vars.length/2;


    }

    private void eq(int idx1, int idx2) throws ContradictionException {
        IntDomainVar v0 = vars[idx1];
        IntDomainVar v1 = vars[idx2];
        int sup = Math.min(v0.getSup(),v1.getSup());
        int inf = Math.max(v0.getInf(),v1.getInf());
        if (v0.getSup() > sup) v0.updateSup(sup, this, false);
        else if (v1.getSup() > sup) v1.updateSup(sup, this, false);
        if (v0.getInf() < inf) v0.updateInf(inf, this, false);
        else if (v1.getInf() < inf) v1.updateInf(inf, this, false);

        for (int i = inf+1 ; i < sup ; i++)
        {
            if (v0.canBeInstantiatedTo(i) && !v1.canBeInstantiatedTo(i)) v0.removeVal(i, this, false);
            else if (v1.canBeInstantiatedTo(i) && !v0.canBeInstantiatedTo(i)) v1.removeVal(i, this, false);
        }


    }
public boolean isSatisfied(int[] sol)
{
        return true; 
}

    public void awakeOnRem(int idx, int val) throws ContradictionException {
        if (idx < n)
        {

            if (val < 3) vars[idx+n].removeVal(val, this, false);

        }
        else
        {
            if (val < 3) vars[idx-n].removeVal(val, this, false);
            else vars[idx-n].updateSup(2, this, false);
        }

    }


    public void awakeOnInst(int idx) throws ContradictionException {

        //this.constAwake(false);
        if (idx <n) {
            if (vars[idx].getVal() < 3) vars[idx+n].instantiate(vars[idx].getVal(), this, false);
            else vars[idx+n].instantiate(3, this, false);
        }
        else
        {
            if (vars[idx].getVal() < 3) vars[idx-n].instantiate(vars[idx].getVal(), this, false);
        }

    }

    public void awakeOnInf(int idx) throws ContradictionException {
        if (idx < n)
        {
            if (vars[idx].getInf() >= 3) vars[idx+n].instantiate(3, this, false);
            else vars[idx+n].updateInf(vars[idx].getInf(), this, false);
        }
        else
        {
            if (vars[idx-n].getInf() < vars[idx].getInf()) vars[idx-n].updateInf(vars[idx].getInf(), this, false);
        }
    }

    public void awakeOnSup(int idx) throws ContradictionException {
        if (idx < n)
        {
            if (vars[idx].getSup() < 3) eq(idx,idx+n);
        }
        else
        {
            if (vars[idx].getSup() < 3 && vars[idx].getSup() < vars[idx-n].getSup()) eq(idx-n,idx);
        }
    }



    public void awake() throws ContradictionException {
        for (int i = 0 ; i < n; i++)
        {
            IntDomainVar v0 = vars[i];
            IntDomainVar v1 = vars[i+n];
            if (v0.getSup() < 3)
                eq(i,i+n);
            else if (v0.getInf() >= 3)
                v1.instantiate(3, this, false);
            if (v1.getSup() < 3)
                eq(i,i+n);
            if (v1.getInf() > v0.getInf()) v0.updateInf(v1.getInf(), this, false);
        }

    }

    public void propagate() throws ContradictionException {
             awake();


    }


    public static class SubSetManager extends IntConstraintManager
    {

        public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables, Object parameters, List<String> options) {
            if (parameters == null)
            {
                IntDomainVar[] vs = solver.getVar((IntegerVariable[]) variables);
                return new SubSetChannelling(vs);

            }
            return null;
        }
    }




}