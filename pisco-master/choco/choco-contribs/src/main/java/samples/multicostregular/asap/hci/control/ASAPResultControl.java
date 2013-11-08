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

package samples.multicostregular.asap.hci.control;


import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import gnu.trove.TIntHashSet;
import samples.multicostregular.asap.hci.abstraction.ASAPDataHandler;
import samples.multicostregular.asap.hci.presentation.ASAPResultPanel;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Mar 9, 2009
 * Time: 3:25:37 PM
 */
public class ASAPResultControl implements Observer {

    ASAPResultPanel pr;


    public ASAPResultControl(ASAPResultPanel pr)
    {
        this.pr = pr;
    }


    public void update(Observable observable, Object o) {
        //ASAPDataHandler d = (ASAPDataHandler) observable;
        Integer i = (Integer)o;

        if (i.equals(ASAPDataHandler.MODEL_FED))
        {
            this.pr.removeAll();
            this.pr.setText("PRESS THE SOLVE BUTTON TO START SOLVING THE FED MODEL");
        }
        else if (i.equals(ASAPDataHandler.NO_SOLUTION))
        {
            this.pr.setText("ARGH NO SOLUTION FOUND, CONSTRAINT PROGRAMMING SUCKS");
        }
        else if (i.equals(ASAPDataHandler.SOLVING))
        {
            this.pr.setText("SOLVING IN PROGRESS... PLEASE BE (VERY) PATIENT");
            //this.pr.setSolved();
        }
        else if (i.equals(ASAPDataHandler.SOLUTION_FOUND))
        {
            this.pr.setSolved();

        }
        //this.pr.repaint();

    }


    public static void main(String[] args) {

        Automaton a;
        RegExp r;
        TIntHashSet symb = new TIntHashSet();
        symb.add(0);
        symb.add(1);
        symb.add(2);
        r = new RegExp("(0|1|2)*(0|1)(0|1)(0|1)(0|1|2)*");
        a = r.toAutomaton();
        a = a.complement();
        a.minimize();
        FiniteAutomaton b;
        b = new FiniteAutomaton();
        b.fill(a,symb);


                b.toDotty("/Users/julien/bui.dot");



    }
}