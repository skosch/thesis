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

package samples.fastregular;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import gnu.trove.TIntHashSet;

import static choco.Choco.*;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Nov 5, 2009
 * Time: 6:34:02 PM
 */


public class NRPExample {


    public static void main(String[] args) {

        int days = 10;
        int pl = 2;


        //2 repos d'affilé
        dk.brics.automaton.Automaton auto = new dk.brics.automaton.Automaton();


        State start = new State();
        start.setAccept(true);
        State s2 = new State();
        start.addTransition(new Transition((char)1,(char)2,start));
        start.addTransition(new Transition((char)0,s2));
        s2.addTransition(new Transition((char)0,start));

        auto.setInitialState(start);
      //  System.out.println(auto);

        //Après des nuits, on dort :)
        dk.brics.automaton.Automaton auto2 = new dk.brics.automaton.Automaton();
        State start2 = new State();
        start2.setAccept(true);
        State s3 = new State();
        start2.addTransition(new Transition((char)0,(char)1,start2));
        start2.addTransition(new Transition((char)2,s3));

        s3.addTransition(new Transition((char)0,start2));
        s3.addTransition(new Transition((char)2,s3));
        s3.setAccept(true);

        auto2.setInitialState(start2);
    //    System.out.println(auto2);


        //Au moins trois jours de taf d'affiler :)
        dk.brics.automaton.Automaton auto3 = new dk.brics.automaton.Automaton();
        State start3 = new State();
        start3.setAccept(true);
        State s4 = new State();
        State s5 = new State();
        start3.addTransition(new Transition((char)0,start3));
        start3.addTransition(new Transition((char)1,(char)2,s4));
        s4.addTransition(new Transition((char)1,(char)2,s5));
        s5.addTransition(new Transition((char)1,(char)2,s5));
        s5.addTransition(new Transition((char)0,start3));

        auto3.setInitialState(start3);



        FiniteAutomaton usable = new FiniteAutomaton();
        TIntHashSet alpha = new TIntHashSet(new int[]{2,0,1});
        usable.fill(auto.intersection(auto2).intersection(auto3),alpha);
        System.out.println(usable);

        Model m  = new CPModel();

        IntegerVariable[][] vars;
        vars = makeIntVarArray("x",pl,days,0,2);

        for (int i  = 0 ;i < pl ; i++)
        {
            m.addConstraint(regular(vars[i], usable));
            m.addConstraint(occurrence(makeIntVar("_",2,6),vars[i], 0));

        }

        for (int i  = 2 ; i < 3 ; i++)
        {

            IntegerVariable[] v = ArrayUtils.getColumn(vars,i);
            m.addConstraint(occurrence(makeIntVar("_",1,1),v, 2));
        }


        Solver s = new CPSolver();

        s.read(m);

        if (s.solve())
        {
            for (int i = 0 ; i < pl ; i++)
            {
                IntDomainVar[] v = s.getVar(vars[i]);
                for (IntDomainVar iv : v)
                {
                    System.out.print(StringUtils.pad(iv.getVal()+"",3," "));
                }
                System.out.println("");
            }

        }


        s.printRuntimeStatistics();



    }


}