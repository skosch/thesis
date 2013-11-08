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


import samples.multicostregular.asap.hci.presentation.ASAPSummaryPanel;
import samples.multicostregular.asap.hci.abstraction.ASAPDataHandler;
import samples.multicostregular.asap.data.ASAPItemHandler;

import java.util.Observer;
import java.util.Observable;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Mar 9, 2009
 * Time: 2:40:14 PM
 */
public class ASAPSummaryControl implements Observer {

    ASAPSummaryPanel ps;

    public ASAPSummaryControl(ASAPSummaryPanel ps)
    {
        this.ps = ps;
    }

    public void update(Observable observable, Object o)
    {
        ASAPDataHandler d = (ASAPDataHandler) observable;
        ASAPItemHandler data = d.getCPModel().getHandler();
        Integer i = (Integer) o;

        if (i.equals(ASAPDataHandler.MODEL_FED))
        {
            this.ps.setSolving(d,false);
            this.ps.setSolved(false);
            ps.setNbDays(d.getCPModel().getNbDays());
            ps.setNbEmployee(d.getCPModel().getNbEmployees());
            ps.setStart(data.getStart());
            ps.setEnd(data.getEnd());
            ps.setFile(data.getProblemName());
            this.ps.repaint();
        }
         else if (i.equals(ASAPDataHandler.SOLVING))
        {
            this.ps.setSolving(d,true);
        }
        else if (i.equals(ASAPDataHandler.SOLUTION_FOUND) || i.equals(ASAPDataHandler.NO_SOLUTION))
        {
            this.ps.setSolving(d,false);
            this.ps.setSolved(true);
        }


    }
}