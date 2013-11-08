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


import static choco.Choco.makeIntVarArray;
import static choco.Choco.regular;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.variables.integer.IntegerVariable;
import dk.brics.automaton.RegExp;
import gnu.trove.TIntHashSet;

import java.util.ArrayList;
import java.util.HashSet;
/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Nov 16, 2009
 * Time: 7:25:31 PM
 */
public class MinimumRegularSet {


    ArrayList<dk.brics.automaton.Automaton> automata;
    ArrayList<String> rules;
    ArrayList<Integer> indexes;
    ArrayList<dk.brics.automaton.Automaton> inSet;
    ArrayList<dk.brics.automaton.Automaton> outSet;

    dk.brics.automaton.Automaton current;

    CPModel model;
    CPSolver solver;
    IntegerVariable[] vs;
    Constraint in;
    TIntHashSet alpha;



    public MinimumRegularSet(ArrayList<String> rules,ArrayList<Integer> indexes, int nbVars, int[] domains)
    {
        this.rules = rules;
        this.indexes = indexes;
        this.automata = new ArrayList<dk.brics.automaton.Automaton>();
        this.inSet = new ArrayList<dk.brics.automaton.Automaton>() ;
        this.outSet = new ArrayList<dk.brics.automaton.Automaton>() ;


        for (String s : rules)
        {
            dk.brics.automaton.Automaton tmp = new dk.brics.automaton.RegExp(StringUtils.toCharExp(s)).toAutomaton().complement();
            automata.add(tmp);
            inSet.add(tmp);
            if (current == null)
                current = tmp;
            else
                current = current.intersection(tmp);

        }

        model = new CPModel();
        vs = makeIntVarArray("x",nbVars,domains, Options.V_ENUM);
        solver = new CPSolver();

        this.alpha = new TIntHashSet(domains);



    }

    public boolean solveCurrent()
    {
        model.removeConstraint(in);
        solver.clear();
        FiniteAutomaton auto = new FiniteAutomaton();
        auto.fill(current,alpha);
        in = regular(vs, auto);
        model.addConstraint(in);
        solver.read(model);

        boolean b = solver.solve();
        if (b)
        {
            for (IntegerVariable v: vs)
                System.out.print(solver.getVar(v).getVal()+" ");
            System.out.println("");
        }
        solver.printRuntimeStatistics();
        return b;
    }

    public void buildWithInSet()
    {
        current = null;
        for (dk.brics.automaton.Automaton a : inSet)
        {
            if (current == null)
                current = a;
            else
                current = a.intersection(current);
            current.minimize();
        }
    }

    public void findMinimumSet()
    {
        dk.brics.automaton.Automaton last = null;
        while(!solveCurrent())
        {
            last = inSet.get(inSet.size()-1);
            outSet.add(last);
            inSet.remove(last);
            buildWithInSet();


        }
    }

    public HashSet<dk.brics.automaton.Automaton> solve()
    {
        findMinimumSet();

        HashSet<dk.brics.automaton.Automaton> sure = new HashSet<dk.brics.automaton.Automaton>();
        while(!outSet.isEmpty())
        {
            dk.brics.automaton.Automaton last = outSet.get(outSet.size()-1);
            outSet.remove(last);
            sure.add(last);
            inSet.addAll(outSet);
            outSet.clear();
            buildWithInSet();
            findMinimumSet();

        }


        return sure;


    }

    public void printRulesToBeRemoved(HashSet<dk.brics.automaton.Automaton> set)
    {
        HashSet<Integer> rls  = new HashSet<Integer>();
        for (dk.brics.automaton.Automaton a : set) {
            rls.add(indexes.get(automata.indexOf(a)));
        }
        System.out.println(rls);
    }



    public static void main(String[] args) {
        ArrayList<String> rules = new ArrayList<String>();
        ArrayList<Integer> indexes = new ArrayList<Integer>();

         //rules 1 : START WITH A WORKING DAY
        rules.add("0(0|1|2)*"); indexes.add(1);

        //rules 2 : NO MORE THAN TWO CONSECUTIVE RESTS
        rules.add("(0|1|2)*000(0|1|2)*");indexes.add(2);


        //rules 8 : NO SHIFT :)
        rules.add("(0|1|2)*");  indexes.add(8);

        //rules 3 : REST AFTER A NIGHT SHIFT SET
        rules.add("(0|1|2)*2+(1)(0|1|2)*");  indexes.add(3);

         //rules 4 : AT LEAST THREE CONSECUTIVE WORKING DAYS
        rules.add("((0|1|2)*(1|2)(1|2)0(0|1|2)*)|((0|1|2)*(1|2)0(0|1|2)*)"); indexes.add(4);

        //rules 5 : AFTER TWO WORKING DAYS => A REST
        rules.add("(0|1|2)*(1|2)(1|2)(1|2)(0|1|2)*"); indexes.add(5);

        //rules 6 : AT LEAST TWO REST IN A ROW
        rules.add("(0|1|2)*0(1|2)(0|1|2)*"); indexes.add(6);

        //rules 7 : AT LEAST TWO RESTS DURING THE SCHEDULE
         rules.add("(1|2)*|(1|2)*0(1|2)*");  indexes.add(7);



           








        MinimumRegularSet ms = new MinimumRegularSet(rules,indexes,5,new int[]{0,1,2});
        System.out.println(ms.solveCurrent());
        ms.printRulesToBeRemoved(ms.solve());

    }


    public static void test()
    {

        int n = 5;
        RegExp reg = new RegExp(StringUtils.toCharExp("(0|1|2)*(1|2)(1|2)(1|2)(0|1|2)*"));
        TIntHashSet alpha = new TIntHashSet();
        alpha.addAll(new int[]{0,1,2});
        dk.brics.automaton.Automaton auto = reg.toAutomaton().complement();
        auto.minimize();
        FiniteAutomaton a = new FiniteAutomaton();
        a.fill(auto,alpha);
        System.out.println(a);

        Model m = new CPModel();
        CPSolver s = new CPSolver();


        IntegerVariable[] vs = makeIntVarArray("x",n,0,2, Options.V_ENUM);

        m.addConstraint(regular(vs, a));


        s.read(m);

        if (s.solve())
        {
            do
            {
                for (IntegerVariable v : vs)
                    System.out.print(s.getVar(v).getVal()+" ");
                System.out.println("");
            } while(s.nextSolution());
        }
        else
        {
            System.err.println("NO SOLUTION");
        }



    }



}