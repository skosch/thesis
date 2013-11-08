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

package samples.multicostregular.carsequencing.heuristics;

import choco.kernel.solver.Solver;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Jan 28, 2009
 * Time: 7:37:45 PM
 */
public class ManInTheMiddleVarHeur extends AbstractIntVarSelector {


    int center;
    boolean l;


    public ManInTheMiddleVarHeur(Solver solver, IntDomainVar[] vars)
    {
    	super(solver, vars);
        this.l = true;
        center =   this.vars.length/2;
    }


    public IntDomainVar selectVar()
    {
        IntDomainVar out = null;

        if (l)
        {
            l=!l;
            for (int i = center ; i >= 0 ; i--)
                if(!vars[i].isInstantiated())
                    return vars[i];
            for (int i = center+1 ; i < vars.length ; i++)
                if(!vars[i].isInstantiated())
                    return vars[i];

        }
        else        {
            l=!l;
            for (int i = center+1 ; i < vars.length ; i++)
                if(!vars[i].isInstantiated())
                    return vars[i];

            for (int i = center ; i >= 0 ; i--)
                if(!vars[i].isInstantiated())
                    return vars[i];

        }

        return out;

    }
}