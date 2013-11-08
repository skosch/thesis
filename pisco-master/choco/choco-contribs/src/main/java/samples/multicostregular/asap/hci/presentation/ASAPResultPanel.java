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

import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.variables.integer.IntDomainVar;

import javax.swing.*;
import java.awt.*;

import samples.multicostregular.asap.ASAPCPModel;
import samples.multicostregular.asap.data.base.ASAPDate;
import samples.multicostregular.asap.hci.abstraction.ASAPDataHandler;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Mar 9, 2009
 * Time: 2:24:20 PM
 */
public class ASAPResultPanel extends JPanel {

    String text;
    ASAPCPModel model;
    ASAPDataHandler d;

    public ASAPResultPanel(ASAPDataHandler d)
    {
        super(new BorderLayout());
        this.d = d;
        this.setBorder(BorderFactory.createBevelBorder(1));

        this.text = "NO INPUT FILE SELECTED...";
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        this.model = d.getCPModel();
        g.drawString(this.text,100,100);
        /*else
        {
            drawSolution(g);
            g.drawString("TEST",0,510);
        }    */
    }





    public void setText(String s)
    {
        this.text =s;
        this.repaint();
    }
    public void setSolved()
    {
        this.text ="";
        //this.repaint();
        this.model = d.getCPModel();
        this.removeAll();

        int w = this.model.getNbDays();
        int h = this.model.getNbEmployees()+1;
        this.setLayout(new BorderLayout());
        JPanel p1 = new JPanel(new GridLayout(h,1));
        JPanel p2 = new JPanel(new GridLayout(h,w));

        this.add(p1,BorderLayout.WEST);
        this.add(p2,BorderLayout.CENTER);


        int i = 0;

        addPanel(p1,"");
        for (int j = 0 ; j < model.shifts[0].length ;j++)
        {
            addPanel(p2, ASAPDate.names[(j+1)%7].substring(0,3));
        }


        for(IntegerVariable[] ivt : model.shifts)
        {



            String print = this.model.getHandler().orderedEmployees.get(i++)+" : ";
            addPanel(p1,print);
            for (IntDomainVar iv : d.getCPSolver().getVar(ivt))
            {
                final String car;
                if (iv.isInstantiated())
                   car = this.model.getHandler().inverseMap.get(iv.getVal());
                else
                    car = "NO";
                addPanel(p2,car);
            }

        }
        this.setVisible(false);
        this.setVisible(true);


    }



    private void addPanel(JPanel panel,final String car) {
        final JPanel jp = new JPanel()
        {
            public void paint(Graphics g)
            {
                super.paint(g);
                int h = this.getHeight();
                int w = this.getWidth();

                int cW = g.getFontMetrics().stringWidth(car);
                int cH = g.getFontMetrics().getHeight();
                int dH = (h-cH)/2;

                g.setColor(Color.BLACK);
                g.drawString(car,(w-cW)/2,h-dH);
                this.setPreferredSize(new Dimension(cW,cH));



            }
        };
        jp.setBorder(BorderFactory.createBevelBorder(1));
        Color c = model.getColor(car);
        if (c != null)
            jp.setBackground(model.getColor(car));
        else if (car.equals("Sat") || car.equals("Sun"))
            jp.setBackground(Color.GRAY);
        panel.add(jp);


    }
}