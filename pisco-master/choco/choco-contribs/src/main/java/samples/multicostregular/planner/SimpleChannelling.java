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
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Jan 22, 2009
 * Time: 4:03:32 PM
 */
public class SimpleChannelling extends AbstractBinIntSConstraint {

    public SimpleChannelling(IntDomainVar x0, IntDomainVar x1) {
        super(x0, x1);
    }

    private void eq() throws ContradictionException {
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

    public void awakeOnRem(int idx, int val) throws ContradictionException {
        if (idx == 0)
        {
            if (val < 3) v1.removeVal(val, this, false);

        }
        else
        {
            if (val < 3) v0.removeVal(val, this, false);
            else v0.updateSup(2, this, false);
        }

    }


    public void awakeOnInst(int idx) throws ContradictionException {

        //this.constAwake(false);
        if (idx == 0) {
            if (v0.getVal() < 3) v1.instantiate(v0.getVal(), this, false);
            else v1.instantiate(3, this, false);
        }
        else
        {
            if (v1.getVal() < 3) v0.instantiate(v1.getVal(), this, false);
        }

    }

    public void awakeOnInf(int idx) throws ContradictionException {
        if (idx == 0)
        {
            if (v0.getInf() >= 3) v1.instantiate(3, this, false);
            else v1.updateInf(v0.getInf(), this, false);
        }
        else
        {
            if (v0.getInf() < v1.getInf()) v0.updateInf(v1.getInf(), this, false);
        }
    }

    public void awakeOnSup(int idx) throws ContradictionException {
        if (idx == 0)
        {
            if (v0.getSup() < 3) eq();
        }
        else
        {
            if (v1.getSup() < 3 && v1.getSup() < v0.getSup()) eq();
        }
    }



    public void awake() throws ContradictionException {
        if (v0.getSup() < 3)
            eq();
        else if (v0.getInf() >= 3)
            v1.instantiate(3, this, false);
        if (v1.getSup() < 3)
            eq();
        if (v1.getInf() > v0.getInf()) v0.updateInf(v1.getInf(), this, false);
    }

    public void propagate() throws ContradictionException {



    }

    public static class SimpleManager extends IntConstraintManager
    {

        public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables, Object parameters, List<String> options) {
            return new SimpleChannelling(solver.getVar(variables[0]),solver.getVar(
                    variables[1]));

        }
    }

}