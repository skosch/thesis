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


import choco.kernel.model.constraints.automaton.DFA;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.constraints.automaton.Transition;

import java.util.ArrayList;
import java.util.Random;


/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 14, 2008
 * Time: 3:27:58 PM
 */
public class InstanceMaker {

    public final static int L = 0;
    public final static int P = 1;
    public final static int O = 2;
    public final static int A = 3;
    public final static int B = 4;
    public final static int C = 5;
    public final static int D = 6;
    public final static int E = 7;
    public final static int F = 8;
    public final static int G = 9;
    public final static int H = 10;
    public final static int I = 11;
    public final static int J = 12;
    public final static int K = 13;
    public final static int M = 14;
    public final static int N = 15;
    public final static int Q = 16;
    public final static int S = 17;
    public final static int T = 18;
    public final static int U = 19;
    public final static int V = 20;









    public IAutomaton makeAutomaton(int nbAct)
    {
        FiniteAutomaton auto = new FiniteAutomaton();
        int start = auto.addState();
        auto.setInitialState(start);
        auto.addTransition(start,start,O);
        int end = auto.addState();
        auto.setFinal(end);
        auto.addTransition(end,end,O);
        int poz = auto.addState();
        int slunch = auto.addState();
        auto.addState(); auto.addState();
        int elunch = auto.addState();
        makeActivityTransitions(auto,slunch,elunch,L);

        for (int i = 3 ; i < nbAct+3; i++)
        {
            int tmpB = auto.addState();
            auto.addState();auto.addState();
            int tmpE = auto.addState();
            auto.setFinal(tmpE);
            makeActivityTransitions(auto,tmpB,tmpE,i);
            auto.addTransition(start,tmpB,i);
            auto.addTransition(tmpE,tmpE,i);
            auto.addTransition(poz,tmpB,i);
            auto.addTransition(elunch,tmpB,i);
            auto.addTransition(tmpE,poz,P);
            auto.addTransition(tmpE,slunch,L);
            auto.addTransition(tmpE,end,O);

        }

        return auto;


    }

    private void makeActivityTransitions(FiniteAutomaton auto, int start, int end, int symbol)
    {
        for (int i = start ; i < end ; i++)
        {
            auto.addTransition(i,i+1,symbol);
        }
    }


    /**
     * create a new two activities automaton corresponding with two work activities
     * a and b are activities (l,p,o) are lunch, break and rest
     * the integer representation is
     * 0 -> a
     * 1 -> b
     * 2 -> l
     * 3 -> p
     * 4 -> o
     * @return an instance of an Automaton
     */
    public FiniteAutomaton makeTwoActivitiesAutomaton()
    {
        FiniteAutomaton auto = new FiniteAutomaton();
        for (int i = 0 ; i < 15 ; i++)
        {
            auto.addState();
        }
        auto.setInitialState(0);
        auto.setFinal(11);
        auto.setFinal(13);
        auto.setFinal(14);

        // transition from 0;
        auto.addTransition(0,0,O);
        auto.addTransition(0,1,A);
        auto.addTransition(0,4,B);


        // transition from 1;
        auto.addTransition(1,5,A);

        // transition from 2;
        auto.addTransition(2,1,A);
        auto.addTransition(2,4,B);

        // transition from 3:
        auto.addTransition(3,6,L);

        // transition from 4;
        auto.addTransition(4,7,B);

        // transition from 5;
        auto.addTransition(5,8,A);

        // transition from 6 ;
        auto.addTransition(6,9,L);

        // transition from 7;
        auto.addTransition(7,10,B);

        // transition from 8;
        auto.addTransition(8,11,A);

        // transition from 9;
        auto.addTransition(9,12,L);

        // transition from 10;
        auto.addTransition(10,13,B);

        // transition from 11;
        auto.addTransition(11,11,A);
        auto.addTransition(11,2,P);
        auto.addTransition(11,3,L);
        auto.addTransition(11,14,O);

        // transition from 12
        auto.addTransition(12,1,A);
        auto.addTransition(12,4,B);

        // transition from 13;
        auto.addTransition(13,13,B);
        auto.addTransition(13,2,P);
        auto.addTransition(13,3,L);
        auto.addTransition(13,14,O);

        //transition from 14;

        auto.addTransition(14,14,O);



        return auto;
    }

    public DFA makeHadrienAuto()
    {

        ArrayList<Transition> tra = new ArrayList<Transition>();

        tra.add(new Transition(0,O,0));
        tra.add(new Transition(0,A,1));
        tra.add(new Transition(0,B,4));


        // transition from 1;
        tra.add(new Transition(1,A,5));

        // transition from 2;
        tra.add(new Transition(2,A,1));
        tra.add(new Transition(2,B,4));

        // transition from 3:
        tra.add(new Transition(3,L,6));

        // transition from 4;
        tra.add(new Transition(4,B,7));

        // transition from 5;
        tra.add(new Transition(5,A,8));

        // transition from 6 ;
        tra.add(new Transition(6,L,9));

        // transition from 7;
        tra.add(new Transition(7,B,10));

        // transition from 8;
        tra.add(new Transition(8,A,11));

        // transition from 9;
        tra.add(new Transition(9,L,12));

        // transition from 10;
        tra.add(new Transition(10,B,13));

        // transition from 11;
        tra.add(new Transition(11,A,11));
        tra.add(new Transition(11,P,2));
        tra.add(new Transition(11,L,3));
        tra.add(new Transition(11,O,14));

        // transition from 12
        tra.add(new Transition(12,A,1));
        tra.add(new Transition(12,B,4));

        // transition from 13;
        tra.add(new Transition(13,B,13));
        tra.add(new Transition(13,P,2));
        tra.add(new Transition(13,L,3));
        tra.add(new Transition(13,O,14));

        //transition from 14;
        tra.add(new Transition(14,O,14));


        ArrayList<Integer> fina = new ArrayList<Integer>();
        fina.add(11);fina.add(14);fina.add(13);

        return new DFA(tra,fina,96);
    }


    public int[][] getRandomCostMatrix(int w, int h, long seed)
    {
        Random r = new Random(seed);
        int[][] out = new int[w][h];
        for (int i = 0 ; i < out.length ;i++)
            for (int j = 0 ; j < out[i].length ; j++)
                out[i][j] = r.nextInt(101)+10;
        return out;
    }

    public static void main(String[] args) {
        InstanceMaker make = new InstanceMaker();
        make.makeTwoActivitiesAutomaton().toDotty("SophieAutomaton.dot");
    }


}
