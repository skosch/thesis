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


import samples.multicostregular.asap.hci.abstraction.ASAPDataHandler;

import javax.swing.*;
import java.util.Observer;
import java.util.Observable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Mar 9, 2009
 * Time: 2:53:32 PM
 */
public class ASAPButtonControl implements Observer, ActionListener {


    JButton solve;
    ASAPDataHandler d;
    boolean next;

    public ASAPButtonControl(ASAPDataHandler d,JButton button)
    {
        this.solve =  button;
        this.solve.setEnabled(false);
        this.solve.addActionListener(this);
        this.d=d;
        this.next = false;
    }


    public void update(Observable observable, Object o) {
        Integer i = (Integer) o;

        if (i.equals(ASAPDataHandler.MODEL_FED))
        {
            this.solve.setEnabled(true);
            this.next = false;
        }
        else if (i.equals(ASAPDataHandler.SOLUTION_FOUND))
        {
            this.solve.setEnabled(true);
            this.next = true;
        }
        else if (i.equals(ASAPDataHandler.NO_SOLUTION))
        {
            this.solve.setEnabled(true);
        }
        else if (i.equals(ASAPDataHandler.SOLVING))
        {
            this.solve.setEnabled(false);
        }
        
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (!this.next)
            this.d.solve();
        else
        {
            this.d.next();
        }
        
    }
}