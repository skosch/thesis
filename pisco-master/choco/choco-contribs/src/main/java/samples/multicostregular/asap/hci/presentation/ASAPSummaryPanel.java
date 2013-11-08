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

package samples.multicostregular.asap.hci.presentation;

import choco.kernel.solver.Solver;

import javax.swing.*;
import java.awt.*;

import samples.multicostregular.asap.data.base.ASAPDate;
import samples.multicostregular.asap.hci.abstraction.ASAPDataHandler;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Mar 9, 2009
 * Time: 1:57:07 PM
 */
public class ASAPSummaryPanel extends JPanel
{

    String name;
    int nbe;
    int nbd;
    ASAPDate start;
    ASAPDate end;
    int width;
    private boolean solving;
    private boolean solved;
    private long time  = 0;
    private Solver solver;
    ASAPResultPanel rp;

    public ASAPSummaryPanel(int width,ASAPResultPanel rp)
    {
        super(new BorderLayout());
        this.width = width;
        this.name = "";
        this.start = new ASAPDate(1970,1,1);
        this.end = new ASAPDate(1970,1,1);
        this.solving = false;
        this.solved = false;
        this.rp =rp;
        this.setBorder(BorderFactory.createBevelBorder(1));

    }


    public void paint(Graphics g)
    {
        super.paint(g);
        String s = "";
        for (int i = 0; i < width/12 - 6 ; i++)
            s+=" ";


        int x = 30;
        int y = 30;
        g.drawString(s+"SUMMARY",x,y);
        y+=10;
        g.drawString(s+"--------",x,y);
        x = 15;
        y = 80;
        g.drawString("File name \t:\t"+name,x,y);
        y+=20;
        g.drawString("Start Date \t:\t"+start.toString(),x,y);
        y+=20;
        g.drawString("End Date \t:\t"+end.toString(),x,y);
        y+=20;
        g.drawString("Nb employees \t:\t"+nbe,x,y);
        y+=20;
        g.drawString("Nb Days \t:\t"+nbd,x,y);
        y+=100;
        if (solving || solved)
        {
            long a;
            if (solving)
            {
                a = System.currentTimeMillis()-time;

            }
            else
            {
                a = this.solver.getTimeCount();
            }
            long i = a/1000;
            long d = (a/100)%10;
            String t = i+","+d;


            g.drawString("Time \t:\t"+t+" s",x,y);
            y+=20;
            try{

            g.drawString("Fails \t:\t"+this.solver.getNodeCount(),x,y);
            this.rp.setSolved();

            }
            catch (NullPointerException ignored)
            {

            }
        }
    }

    public void setFile(String n)
    {
        this.name = n;
    }

    public void setStart(ASAPDate d)
    {
        this.start =d;
    }
    public void setEnd(ASAPDate d)
    {
        this.end =d;
    }

    public void setNbEmployee(int n)
    {
        this.nbe =n;
    }

    public void setNbDays(int n)
    {
        this.nbd =n;
    }




    public void setSolving(ASAPDataHandler d,boolean b) {
        this.solving = b;
        this.solver = d.getCPSolver();
        
        
        if (b){
            time = System.currentTimeMillis();
            UpdateThread t = new UpdateThread();
            t.start();
        }
    }

    public void setSolved(boolean b)
    {
        this.solved = b;
    }



    private class UpdateThread extends Thread
    {

        public void run()
        {
            while (solving)
            {
                repaint();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    System.err.println("Should not be interrupted");
                }
            }

        }

    }
}