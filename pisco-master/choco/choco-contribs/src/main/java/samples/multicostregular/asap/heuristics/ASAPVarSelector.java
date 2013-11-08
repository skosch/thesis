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

import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.AbstractSearchHeuristic;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Mar 12, 2009
 * Time: 2:46:42 PM
 */
public class ASAPVarSelector extends AbstractSearchHeuristic implements VarSelector<IntDomainVar> {

    IntDomainVar[][] vars;
    AbstractIntVarSelector[] varselec;

    public ASAPVarSelector(IntDomainVar[][] vars, Solver solver)
    {
    	super(solver);
    	this.vars=  vars;
        this.varselec = new AbstractIntVarSelector[vars.length];
        for (int i = 0; i < this.varselec.length ;i++)
        {
            varselec[i] = new MinDomain(solver,vars[i]);
        }

    }
    public ASAPVarSelector(Solver s,IntegerVariable[][] vars)
    {
    	super(s);
        this.vars = new IntDomainVar[vars.length][];
        for (int i = 0 ; i < vars.length ; i++)
        {
            this.vars[i] = s.getVar(vars[i]);
        }
        this.varselec = new AbstractIntVarSelector[vars.length];
        for (int i = 0; i < this.varselec.length ;i++)
        {
            varselec[i] = new MinDomain(s,this.vars[i]);
        }

    }

    public int getNbInstanciated(int i)
    {
        int out = 0;
        for (IntDomainVar v : vars[i])
        {
            if (v.isInstantiated()) out++;
        }
        return out;

    }

    public IntDomainVar selectVar() {
        int idx =0;
        int num = -1;
        int n = vars[0].length;
        for (int i = 0 ; i < vars.length ;i++)
        {
            int nb = this.getNbInstanciated(i);
            if (nb <n && nb > num)
            {
                idx = i;
                num = nb;
            }
        }
        return varselec[idx].selectVar();
    }
}