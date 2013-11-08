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
import samples.multicostregular.asap.hci.abstraction.ASAPDataHandler;
import samples.multicostregular.asap.hci.control.ASAPSummaryControl;
import samples.multicostregular.asap.hci.control.ASAPButtonControl;
import samples.multicostregular.asap.hci.control.ASAPResultControl;
import samples.multicostregular.asap.hci.control.ASAPReadControl;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Mar 9, 2009
 * Time: 1:02:07 PM
 */
public class ASAPMainWindow extends JFrame {


    protected Container pane;
    protected ASAPDataHandler model;

    public ASAPMainWindow(ASAPDataHandler data)
    {
        super("A.S.A.P. Planner");
        this.model = data;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Toolkit tools = Toolkit.getDefaultToolkit();
        Dimension d = tools.getScreenSize();
        d.setSize(d.getWidth()*2/3, d.getHeight()*2/3);
        this.setSize(d);
        this.setPreferredSize(d);
        this.pane  = this.getContentPane();
        this.pane.setLayout(new BorderLayout());

        addMenuBar();
        buildCenter();
        buildLeft();



    }

    ASAPResultPanel jPanelResult;
    public void buildLeft()
    {
        int w = this.getWidth()/5;
        ASAPSummaryPanel jPanelSummary  = new ASAPSummaryPanel(w,jPanelResult);
        Dimension d = new Dimension(w,this.getHeight());
        jPanelSummary.setPreferredSize(d);


        pane.add(jPanelSummary,BorderLayout.WEST);

        ASAPSummaryControl sc = new ASAPSummaryControl(jPanelSummary);
        model.addObserver(sc);

        JButton bsolve = new JButton("Solve");
        ASAPButtonControl bc = new ASAPButtonControl(model,bsolve);
        model.addObserver(bc);
        jPanelSummary.add(bsolve,BorderLayout.SOUTH);


    }

    public void buildCenter()
    {
        jPanelResult = new ASAPResultPanel(model);
        //jPanelResult.setPreferredSize(new Dimension(this.getWidth()*4/5,this.getHeight()));
        ASAPResultControl rc = new ASAPResultControl(jPanelResult);
        model.addObserver(rc);
        

        pane.add(jPanelResult, BorderLayout.CENTER);



      //  model.addObserver(mtj);
    }


    public void addMenuBar()
    {
        JMenuBar jmb = new JMenuBar();
        setJMenuBar(jmb);
        JMenu mfile = new JMenu("File");
        jmb.add(mfile);
        JMenuItem oimage = new JMenuItem("Import XML file...");
        ASAPReadControl cr = new ASAPReadControl(model);
        oimage.addActionListener(cr);
        mfile.add(oimage);

    }


}
