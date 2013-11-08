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

import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.DFA;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.constraints.automaton.Transition;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Dec 4, 2008
 * Time: 5:12:44 PM
 */
public class NSPModel extends CPModel {

    public static int D = 0;
    public static int E = 1;
    public static int N = 2;
    public static int R = 3;

    Constraint [] constraints;
    Constraint forHeuristic;
    NSPInstance instance;
    IntegerVariable[][] shifts;
    IntegerVariable globalCost;
    IAutomaton auto;

    public NSPModel(String file)
    {
        super();
        this.instance = NSPParser.parseNSPFile(file);
        makeModel();
    }
    public NSPModel(NSPInstance  instance)
    {
        super();
        this.instance = instance;
        makeModel();
    }

    IntegerVariable[] flattenShifts(boolean horizontal)
    {
        IntegerVariable[] out = new IntegerVariable[shifts.length*shifts[0].length];
        if (horizontal)
        {
            for (int i = 0 ; i < shifts.length ; i++)
                System.arraycopy(shifts[i], 0, out, (i * shifts[i].length), shifts[i].length);
        }
        else
        {
            for (int i = 0 ; i < shifts.length ; i++)
                for (int j = 0 ; j < shifts[i].length ;j++)
                    out[j*(shifts.length)+i] = shifts[i][j];
        }
        return out;
    }



    private void makeModel()
    {
        //workload : max : 5D/week or 4E/week or 2N/week min 2R/week
        this.shifts = makeIntVarArray("shift",instance.nbNurses,instance.nbDays,0,instance.nbShifts-1, Options.V_ENUM);
        this.auto = makeNSPAutomaton();
        //int[] lowerBounds = {0,0,0,2};  // au minimum zero D, zero E et zero N, mais au moins 2R par semaine
        //int[] upperBounds = {5,4,2,7};  // au max 5 D, 4E, 2N par semaine, pas de limite sur le repos (7)

        globalCost = makeIntVar("cost",0,Integer.MAX_VALUE/10, Options.V_BOUND);
        IntegerVariable[] zs = makeIntVarArray("zs",instance.nbNurses,0,Integer.MAX_VALUE/1000, Options.V_BOUND);
        //Automaton constraint :
        Constraint[] cauto = new Constraint[instance.nbNurses];
        //Redondant gcc over nurse activities
        Constraint[] credo = new Constraint[instance.nbNurses];
        //Redondant regular constraints;
        Constraint[] cregu = new Constraint[instance.nbNurses];
        for (int i = 0 ;i < cauto.length ;i++)
        {
            int[] lowerBounds = {0,0,0,2};
            int[] upperBounds = {5,4,2,7};
            IntegerVariable[] cvars = new IntegerVariable[5];
            cvars[0] = zs[i];
            cvars[1] = makeIntVar("r_"+i+"_1",0,5, Options.V_BOUND);
            cvars[2] = makeIntVar("r_"+i+"_2",0,4, Options.V_BOUND);
            cvars[3] = makeIntVar("r_"+i+"_3",0,2, Options.V_BOUND);
            cvars[4] = makeIntVar("r_"+i+"_4",2,7, Options.V_BOUND);
            this.addVariables(cvars);

            int[][][] csts = new int[instance.nbDays][4][5];
            for (int d = 0 ; d < csts.length ; d++)
                for (int j  =0 ;j < csts[d].length ;j++)
                    for (int k= 0 ; k< csts[d][j].length ; k++)
                    {
                        if (k == 0)
                        {
                            csts[d][j][0] = instance.prefs[i][d*4+j];
                        }
                        else if (j == k-1)
                            csts[d][j][k] = 1;
                    }
            cauto[i] = multiCostRegular(cvars, shifts[i], auto,csts);
            credo[i] = globalCardinality(Options.C_GCC_BC,shifts[i],0,3,lowerBounds,upperBounds);
            cregu[i] =  regular(shifts[i], makeHadrienNSPAutomaton());
        }


        constraints = cauto;
        //GCC constraints nb of nurses required per shift (coverage).
        Constraint[] cgcc = new Constraint[instance.nbDays];
        IntegerVariable[][] tmp = new IntegerVariable[shifts[0].length][shifts.length];
        for (int i= 0 ; i < shifts.length ; i++)
            for (int j = 0 ;j < shifts[i].length; j++)
            {
                tmp[j][i] = shifts[i][j];
            }
        //        UtilAlgo.invert(shifts);


        for (int d = 0 ; d < tmp.length ; d++)
        {
            int[] gmax = new int[4];
            //System.arraycopy(instance.coverages[d], 0, gmax, 0, gmax.length - 1);
            //gmax[gmax.length-1] = instance.nbNurses;
            for (int l : instance.coverages[d])
                System.out.print(l+"\t");
            System.out.println("");
            Arrays.fill(gmax,instance.nbNurses);
            cgcc[d] = globalCardinality(Options.C_GCC_BC,tmp[d],0,3,instance.coverages[d],gmax);
        }


        Constraint ccost = eq(globalCost,sum(zs));

        forHeuristic = new ComponentConstraint(NSPStruct.NSPStrucManager.class,instance,flattenShifts(true));


        this.addConstraints(cauto);
        this.addConstraints(cgcc);
        this.addConstraint(ccost);
        this.addConstraint(forHeuristic);
        this.addConstraints(credo);
        // this.addConstraint(eq(globalCost,310));
        this.addConstraints(cregu);

    }
    public String NbToString(int val)
    {
        switch(val) {
            case 0 : return "D";
            case 1 : return "E";
            case 2 : return "N";
            case 3 : return "R";
            default : return null;
        }
    }

    public String solution(Solver s)
    {
        StringBuffer b = new StringBuffer("########### SOLUTION ##########");
        b.append((char)Character.LINE_SEPARATOR);
        for (int i = 0 ;i < shifts.length ; i++)
        {
            b.append("Nurse ").append(i + 1).append(" : ");
            for (int j = 0 ; j < shifts[i].length ;j++)
            {
                b.append(NbToString(s.getVar(shifts[i][j]).getVal())).append(" ");
            }
            b.append((char)Character.LINE_SEPARATOR);


        }
        b.append((char)Character.LINE_SEPARATOR);
        b.append("Cost : ").append(s.getVar(globalCost).getVal());
        b.append((char)Character.LINE_SEPARATOR);

        return b.toString();
    }


    private FiniteAutomaton makeNSPAutomaton()
    {
        FiniteAutomaton auto = new FiniteAutomaton();
        for (int i = 0 ; i < 17 ; i++)
        {
            auto.addState();
            auto.setFinal(i);
            auto.addTransition(i,0,R);
        }
        auto.setInitialState(0);

        // i -> i (i= D, E, N) // 5*3 transitions
        int k = 1;
        for (int i = 0; i < instance.nbShifts-1 ; i++) {
            auto.addTransition(0,k,i);
            for (int j = 1 ; j <= 3 ; j++)	{
                auto.addTransition(k,k+1,i); k++;
            }
            auto.addTransition(k,16,i); k++;
        }

        // D -> E -> E // 2*3+1 transitions
        auto.addTransition(1,13,E);
        auto.addTransition(13,7,E);
        auto.addTransition(2,14,E);
        auto.addTransition(14,8,E);
        auto.addTransition(3,15,E);
        auto.addTransition(15,16,E);
        auto.addTransition(4,16,E);

        // D -> N // 4 transitions
        auto.addTransition(1,10,N);
        auto.addTransition(2,11,N);
        auto.addTransition(3,12,N);
        auto.addTransition(4,16,N);

        // E -> N // 4 transitions
        auto.addTransition(5,10,N);
        auto.addTransition(6,11,N);
        auto.addTransition(7,12,N);
        auto.addTransition(8,16,N);

        ///////// assert nbTransition == 30
        auto.toDotty("NSPDotty.dot");

        return auto;
    }

    private DFA makeHadrienNSPAutomaton()
    {


        ArrayList<Transition> trans = new ArrayList<Transition>();
        ArrayList<Integer> acc = new ArrayList<Integer>();
        for (int i = 0 ; i < 17 ; i++)
        {
            acc.add(i);
            trans.add(new Transition(i,R,0));
        }
        // i -> i (i= D, E, N) // 5*3 transitions
        int k = 1;
        for (int i = 0; i < instance.nbShifts-1 ; i++) {
            trans.add(new Transition(0,i,k));
            for (int j = 1 ; j <= 3 ; j++)	{
                trans.add(new Transition(k,i,k+1)); k++;
            }
            trans.add(new Transition(k,i,16)); k++;
        }

        // D -> E -> E // 2*3+1 transitions
        trans.add(new Transition(1,E,13));
        trans.add(new Transition(13,E,7));
        trans.add(new Transition(2,E,14));
        trans.add(new Transition(14,E,8));
        trans.add(new Transition(3,E,15));
        trans.add(new Transition(15,E,16));
        trans.add(new Transition(4,E,16));

        // D -> N // 4 transitions
        trans.add(new Transition(1,N,10));
        trans.add(new Transition(2,N,11));
        trans.add(new Transition(3,N,12));
        trans.add(new Transition(4,N,16));

        // E -> N // 4 transitions
        trans.add(new Transition(5,N,10));
        trans.add(new Transition(6,N,11));
        trans.add(new Transition(7,N,12));
        trans.add(new Transition(8,N,16));

        ///////// assert nbTransition == 30


        return new DFA(trans,acc,7);


    }

}
