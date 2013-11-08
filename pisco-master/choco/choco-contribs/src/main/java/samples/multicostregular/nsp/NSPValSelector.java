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

package samples.multicostregular.nsp;

import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Dec 9, 2008
 * Time: 12:57:27 AM
 */
public class NSPValSelector implements ValSelector<IntDomainVar> {

    NSPStruct struct;
    NSPVarSelector varselec;
    public NSPValSelector(NSPVarSelector varsel)
    {
        this.struct = varsel.struct;
        this.varselec = varsel;

    }

    public int getBestVal(IntDomainVar x) {
        int[] pos = varselec.map.get(x);



        int idx = pos[0]*struct.instance.nbDays+pos[1];

        int val = -1;
        int dayIndex = idx%struct.instance.nbDays;
        int max = Integer.MIN_VALUE/100;
        for (int i = 0 ; i < struct.instance.nbShifts ; i++)
        {
            int tmp =struct.need[dayIndex][i].get();
            {
                if (max < tmp)
                {
                    max = tmp;
                    val = i;

                }
            }

        }
        if (max > 0&& x.canBeInstantiatedTo(val))
            return val;
        else return x.getSup();

    }
}
