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

package choco.cp.solver.constraints.global.automata.fast_multicostregular.example;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.constraints.automaton.penalty.IPenaltyFunction;
import choco.kernel.model.constraints.automaton.penalty.IsoPenaltyFunction;
import choco.kernel.model.constraints.automaton.penalty.LinearPenaltyFunction;
import choco.kernel.model.variables.integer.IntegerVariable;

import java.util.logging.Level;

import static choco.Choco.*;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Apr 27, 2010
 * Time: 5:39:09 PM
 */
public class SoftExample
{

public static void main2(String[] args)
{
        int n  = 28;
                int c = 3;
                CPModel m = new CPModel();
                CPSolver s = new CPSolver();
                ChocoLogging.getEngineLogger().setLevel(Level.INFO);


                IntegerVariable[] x = makeIntVarArray("x",n,0,2);
                IntegerVariable[] y = makeIntVarArray("y",c,13,13);

                IntegerVariable Z = makeIntVar("Z",39,39,"cp:bound",Options.V_NO_DECISION);



                FiniteAutomaton fa = new FiniteAutomaton();
                int start = fa.addState();
                int last = fa.addState();
                fa.setFinal(start,last);
                fa.setInitialState(start);

                fa.addTransition(start,start,2);
                fa.addTransition(start,last,0,1);
                fa.addTransition(last,last,0,1);
                fa.addTransition(last,start,2);

                int[][][][] cst = new int[n][3][c+1][2];

                for (int i = 0 ;i < cst.length ; i++)
                {
                        cst[i][0][1][0] = cst[i][0][1][1] = 1;    // on compte le nb de 0
                        cst[i][2][2][0] = cst[i][2][2][1] = 1;    // on compte le nb de 2
                        cst[i][0][3][1] = cst[i][1][3][1] = 1;   // Apres une activitŽ => Žtat 1 => on paye 1.

                        for (int j = 0 ; j < 3 ; j++)
                                for (int q = 0 ;q < 2 ; q++)
                                {
                                        cst[i][j][0][q] = cst[i][j][1][q] + cst[i][j][2][q] + cst[i][j][3][q];
                                }
                }

                Constraint cons = multiCostRegular(ArrayUtils.append(new IntegerVariable[]{Z},y), x, fa,cst);

                m.addConstraint(cons);
        m.addConstraint(eq(sum(y),Z));


                s.read(m);


                int sol = 0;
                //s.maximize(s.getVar(Z),false);
                if (s.solve())
                {
                        do
                        {
                                sol++;
                                System.out.println(s.isFeasible());
                                System.out.println(s.pretty());

                        } while(s.nextSolution());
                }
                System.out.println("NB SOL : "+sol);
                s.printRuntimeStatistics();


}

public static void main1(String[] args)
{
        int n  = 5;
        int c = 3;
        CPModel m = new CPModel();
        CPSolver s = new CPSolver();
        ChocoLogging.getEngineLogger().setLevel(Level.INFO);


        IntegerVariable[] x = makeIntVarArray("x",n,0,2);
        IntegerVariable[] y = makeIntVarArray("y",c,0,13);
        IntegerVariable[] z = makeIntVarArray("z",c,0,n*10,Options.V_NO_DECISION);

        IntegerVariable Z = makeIntVar("Z",0,390,"cp:bound");

        IPenaltyFunction[] f = new IPenaltyFunction[z.length];
        f[0] = new LinearPenaltyFunction(0,2,10,n,2,10);
        f[1] = new LinearPenaltyFunction(0,2,10,n,2,10);
        f[2] = new IsoPenaltyFunction(5);


        FiniteAutomaton fa = new FiniteAutomaton();
        int start = fa.addState();
        int last = fa.addState();
        fa.setFinal(start,last);
        fa.setInitialState(start);

        fa.addTransition(start,start,2);
        fa.addTransition(start,last,0,1);
        fa.addTransition(last,last,0,1);
        fa.addTransition(last,start,2);

        int[][][][] cst = new int[n][3][c][2];

        for (int i = 0 ;i < cst.length ; i++)
        {
                cst[i][0][0][0] = cst[i][0][0][1] = 1;    // on compte le nb de 0
                cst[i][2][1][0] = cst[i][2][1][1] = 1;    // on compte le nb de 2
                cst[i][0][2][1] = cst[i][1][2][1] = 1;   // Apres une activitŽ => Žtat 1 => on paye 1.
        }

        Constraint cons = softMultiCostRegular(x,y,z,Z,f,fa,cst);

        m.addConstraint(cons);


        s.read(m);


        int sol = 0;
        //s.maximize(s.getVar(Z),false);
        if (s.solve())
        {
                do
                {
                        sol++;
                        System.out.println(s.isFeasible());
                        System.out.println(s.pretty());
                        s.postCut(s.lt(s.getVar(Z),s.getVar(Z).getVal()));

                } while(s.nextSolution());
        }
        System.out.println("NB SOL : "+sol);
        s.printRuntimeStatistics();





}

public static void main(String[] args)
{
//        main1(args);
        main2(args);
}


}
