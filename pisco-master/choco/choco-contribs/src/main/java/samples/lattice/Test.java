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

package samples.lattice;

import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Dec 3, 2009
 * Time: 12:41:43 PM
 */
public class Test {

    int n;

    public Test(int seqSize)
    {
        this.n = seqSize;
    }


    public dk.brics.automaton.Automaton makeLengthAccept(int[] alphabet, int size)
    {
        dk.brics.automaton.Automaton auto = new dk.brics.automaton.Automaton();
        State start = new State();
        State tmp = start;
        State last =start;

        for (int i = 0 ; i < size ; i++)
        {
            last = new State();
            for (int k : alphabet)
            {
                tmp.addTransition(new Transition((char) FiniteAutomaton.getCharFromInt(k),last));
            }
            tmp = last;

        }
        last.setAccept(true);
        auto.setInitialState(start);
        return auto;

    }


    public dk.brics.automaton.Automaton makeStartWithWork()
    {
        return new RegExp(StringUtils.toCharExp("(1|2)(0|1|2)*")).toAutomaton();
    }

    public dk.brics.automaton.Automaton makeNoMoreTwoConsRest()
    {
        return new RegExp(StringUtils.toCharExp("(0|1|2)*000(0|1|2)*")).toAutomaton().complement();
    }

    public dk.brics.automaton.Automaton testInter()
    {
        dk.brics.automaton.Automaton auto = makeLengthAccept(new int[]{0,1,2},n);
        auto = auto.intersection(makeStartWithWork());
        auto = auto.intersection(makeNoMoreTwoConsRest());
        auto.minimize();
        return auto;
    }

    public dk.brics.automaton.Automaton unionWithRule1()
    {
        dk.brics.automaton.Automaton auto = testInter();
        auto = auto.union((makeLengthAccept(new int[]{0,1,2},n).intersection(makeStartWithWork())));
   //     auto.minimize();
        auto.determinize();
        return auto;
    }


    public void printAuto(dk.brics.automaton.Automaton auto, String name)
    {
        name+=".dot";
        try {
            FileWriter fw = new FileWriter(name);
            fw.write(auto.toDot());
            fw.close();
        } catch (IOException e) {
            System.err.println("Unable to write automaton");
        }
    }


    public static void main(String[] args) {
        Test t = new Test(8);
        dk.brics.automaton.Automaton auto = t.testInter();
        t.printAuto(auto,"autoTest");
        t.printAuto(t.unionWithRule1(),"unionTest");

    }


}