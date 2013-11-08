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

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.automata.fast_multicostregular.MultiCostRegular;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Dec 5, 2008
 * Time: 12:09:50 AM
 */
public class NSPSolver {

    public static void main(String[] args) {
        for (int i = 25 ; i <= 25 ; i+=25)
            for (int j = 501 ; j <= 501 ; j+=50)
            {
                NSPModel mod = new NSPModel("/Users/julien/These/NSP/NSPLib/N"+i+"/"+j+".nsp");
                CPSolver s = new CPSolver();
                s.read(mod);
              //  CPSolver.setVerbosity(CPSolver.SOLUTION);
               // s.setVarIntSelector(new StaticVarOrder(UtilAlgo.append(s.getVar(mod.globalCost),s.getVar(mod.flattenShifts()))));
                // s.setVarIntSelector(new StaticVarOrder(s.getVar(mod.flattenShifts(false))));
                NSPVarSelector varselec = new NSPVarSelector((NSPStruct) s.getCstr(mod.forHeuristic));
                NSPValSelector valselec = new NSPValSelector(varselec);

                s.attachGoal(new NSPBranching(varselec,valselec));


                MultiCostRegular[] cons = new MultiCostRegular[mod.constraints.length];
                for (int k  = 0 ; k < mod.constraints.length ; k++)
                    cons[k] = (MultiCostRegular) s.getCstr(mod.constraints[k]);

               // s.setValIntSelector(new RCCRValSelector(cons,false));

             //   s.setValIntIterator(new DecreasingDomain());
                
                System.out.println("N"+i+" : "+j);
                //if (s.minimize(s.getVar(mod.globalCost),false))
                if (s.solve())
                do {
                    System.out.println(mod.solution(s));
                    s.postCut(s.lt(s.getVar(mod.globalCost),s.getVar(mod.globalCost).getVal()));
                 } while(false && s.nextSolution());
                s.printRuntimeStatistics();
                System.out.println("");
                System.out.println("");
            }
    }
}
