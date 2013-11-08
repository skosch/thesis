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

package samples.multicostregular.asap.heuristics;

import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.AbstractSearchHeuristic;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: 5 janv. 2010
 * Time: 13:39:56
 */
public class CoverVarSelector extends AbstractSearchHeuristic implements VarSelector<IntDomainVar>, ValSelector<IntDomainVar> {


    AbstractIntVarSelector other;

    IntDomainVar[][] vars;
    IntDomainVar selected;
    int nVal;

    IStateInt lastCol;
    int[][] lowb;

    public CoverVarSelector(IntDomainVar[][] vars, int[][] lowb, Solver solver)
    {
        super(solver);
    	this.vars = vars;
        this.lowb = lowb;
        this.other = new StaticVarOrder(solver, ArrayUtils.flatten(vars));

        lastCol = solver.getEnvironment().makeInt(0);

    }

    private int scanCol(int idx)
    {
        int[] tmp = new int[10];
        IntDomainVar[] col = vars[idx];
        int[] low = lowb[idx];
        for (IntDomainVar v : col)
        {
            if (v.isInstantiated())
                tmp[v.getVal()]++;
        }
        for (int i = 0;  i < tmp.length ; i++)
        {
            if (tmp[i] < low[i])
                return i;
        }
        return Integer.MAX_VALUE;
    }

    private IntDomainVar getFirstVar(int idx)
    {
        for (IntDomainVar v : vars[idx])
            if (!v.isInstantiated())
                return v;
        return null;
    }

    @Override
    public IntDomainVar selectVar() {
        int tmp = 0;
        while (lastCol.get() < vars.length && (tmp = scanCol(lastCol.get())) == Integer.MAX_VALUE)
        {
            lastCol.add(1);
        }
        if (lastCol.get() == vars.length)
        {
            selected= other.selectVar();
            nVal = selected == null ? 0 : selected.getSup();

        }
        else
        {
            selected = getFirstVar(lastCol.get());
            nVal = tmp;
        }



        return selected;

    }

    @Override
    public int getBestVal(IntDomainVar x) {
        if (x == selected && x.canBeInstantiatedTo(nVal))
            return nVal;
        else
            return x.getSup();

    }
}
