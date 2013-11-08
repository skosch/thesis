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

import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import samples.multicostregular.asap.hci.abstraction.ASAPDataHandler;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Mar 12, 2009
 * Time: 3:18:49 PM
 */
public class ASAPValSelector implements ValSelector<IntDomainVar> {


    HashMap<IntDomainVar,IntDomainVar[]> map = new HashMap<IntDomainVar,IntDomainVar[]>();
    HashMap<IntDomainVar,Integer> mapIdx = new HashMap<IntDomainVar,Integer>();

    IntDomainVar[][] vars;
    ASAPDataHandler d;


    public ASAPValSelector(Solver s, IntegerVariable[][] vars,ASAPDataHandler d)
    {
        this.vars= new IntDomainVar[vars.length][];
        this.d = d;
        for (int i = 0 ; i < vars.length ; i++)
        {
            this.vars[i] = s.getVar(vars[i]);
            for (int j = 0; j < this.vars[i].length ; j++) {
                map.put(this.vars[i][j],this.vars[i]);
                mapIdx.put(this.vars[i][j],i);
            }
        }


    }

    public int getBestVal(IntDomainVar x) {
        IntDomainVar[] col = map.get(x);
        int idx = mapIdx.get(x);
        int val = neededValue(x,idx,col);

        return val;
    }

    private int neededValue(IntDomainVar x, int idx,IntDomainVar[] col) {
        int lowb[] = Arrays.copyOf(d.getCPModel().lowb[idx],d.getCPModel().lowb[idx].length);
        int uppb[] = Arrays.copyOf(d.getCPModel().uppb[idx],d.getCPModel().uppb[idx].length);
        int[] occur = new int[lowb.length];
        for (int i = 0 ; i < col.length ;i++)
        {
            if (col[i].isInstantiated())
                occur[col[i].getVal()]++; 
        }

        for (int i = 0 ; i < lowb.length ; i++)
        {
            lowb[i]-= occur[i];
            uppb[i]-= occur[i];
        }
        if (max(x,lowb)[0] <= 0)
        {
            max(x,uppb);
        }

        return maxs[1];

        

    }

    static int[] maxs = new int[2];
    static int[] max(IntDomainVar x,int[] tmp)
    {
        maxs[0] = Integer.MIN_VALUE;
        for (int i = 0; i < tmp.length ;i++)
        {
            int a = tmp[i];
            if ( a > maxs[0] && x.canBeInstantiatedTo(i))
            {
                maxs[0] = a;
                maxs[1] = i;
            }
        }

        return maxs;
    }
}