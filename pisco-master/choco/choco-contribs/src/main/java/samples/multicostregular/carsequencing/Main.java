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

package samples.multicostregular.carsequencing;

import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Jan 26, 2009
 * Time: 5:10:53 PM
 */
public class Main {


    public static void solve(String filename, boolean all)
    {

        CarSeqModel m = new CarSeqModel(filename,false);
        Solver s = new CPSolver();

        s.read(m);
        IntegerVariable[] seqMVars = m.seqVars;
        IntDomainVar[] seqVars = s.getVar(seqMVars);

       // s.setValIntSelector(new LeastCarValHeur(m.getInstance()));
       // s.setValIntSelector(new MostOptionValHeur(m.getInstance()));
        //s.setValIntSelector(new BothValHeur(m.getInstance()));

         s.setVarIntSelector(new StaticVarOrder(s, seqVars));
        //s.setVarIntSelector(new ManInTheMiddleVarHeur(seqVars));
        s.monitorFailLimit(true);
        	



        System.out.println("Trying "+m.getInstance().name+"...");

        if (s.solve())
        {
            do {

                for (int i = 0 ; i < seqVars.length ; i++)
                {
                    System.out.print(seqVars[i].getVal()+"\t");
                    for (int j = 0 ; j < m.getInstance().nbOptions ; j++)
                        System.out.print(m.getInstance().optionRequirement[seqVars[i].getVal()][j+2]+" ");
                    System.out.println("");


                }
                System.out.println("");
                System.out.println("");
            } while(all && s.nextSolution());


        }
        s.printRuntimeStatistics();
        System.out.println(s.getNbSolutions()+" SOLUTIONS" );

    }


    public static void main(String[] args) {
        String prefix= "carseq/pb";

        for (int i = 1 ;i < 80 ; i++)
        {
            solve(prefix+i+".txt",false);
        }

    }
   

}