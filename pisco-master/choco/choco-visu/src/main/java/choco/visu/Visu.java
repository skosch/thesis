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

package choco.visu;

import choco.cp.solver.propagation.ObservableVarEventQueue;
import choco.cp.solver.propagation.VariableEventQueue;
import choco.kernel.model.variables.Variable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.search.AbstractSearchLoop;
import choco.kernel.solver.variables.Var;
import choco.kernel.visu.IVisu;
import choco.kernel.visu.components.IVisuVariable;
import choco.kernel.visu.components.panels.AVarChocoPanel;
import choco.visu.searchloop.IObservableStepSearchLoop;
import choco.visu.searchloop.ObservableStepSearchLoop;
import choco.visu.variables.VisuVariable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import static choco.visu.VisuButton.NEXT;
import static choco.visu.VisuButton.PLAY;

public final class Visu implements IVisu {

	private final HashMap<Var, VisuVariable> visuvars;

	private int width, heigth;

	private final JFrame frame;

	private final JPanel mainpanel;

	private final JTabbedPane tabbedpane;

	private final JButton next, play, pause;

	private final JSlider duration;

	private IObservableStepSearchLoop ssl;

	public final Tracer tracer;

	private final boolean[] visible = new boolean[]{false, false};


	protected ArrayList<AVarChocoPanel> panelList;

	/**
	 * Create the visu object
	 *
	 * @param width width of the window
	 * @param height height of the window
	 * @param buttons the buttons to draw
	 */
	private Visu(int width, int height, final VisuButton... buttons) {
		this.width = width;
		this.heigth = height;
		tracer = new Tracer();
		frame = new JFrame();
		mainpanel = new JPanel();
		tabbedpane = new JTabbedPane();
		next = new JButton("next");
		play = new JButton("play");
		pause = new JButton("pause");
		duration = new JSlider(JSlider.HORIZONTAL, 0, 500, 10);

		for (VisuButton vb : buttons) {
			switch (vb) {
			case NEXT:
				visible[0] = true;
				break;
			case PLAY:
				visible[1] = true;
				break;
			}
		}

		buildFrame();
		buildPanels();

		panelList = new ArrayList<AVarChocoPanel>(3);
		visuvars = new HashMap<Var, VisuVariable>(50);
	}


	//Available methods to build this Visu.
	public static Visu createFullVisu(){
		return new Visu(480, 640, NEXT, PLAY);
	}

	public static Visu createFullVisu(int width, int height){
		return new Visu(width, height, NEXT, PLAY);
	}

	public static Visu createVisu(final VisuButton... buttons){
		return new Visu(480, 640, buttons);
	}

	public static Visu createVisu(int width, int height, final VisuButton... buttons){
		return new Visu(width, height, buttons);
	}

	public final void kill(){
		frame.dispose();
	}


	/**
	 * Build the visu's frame
	 */
	 private void buildFrame() {
		 frame.setTitle("CHOCO solver");
		 frame.setLocationRelativeTo(null); //Center it in the middle of the window
		 frame.setResizable(true);
		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 frame.setContentPane(mainpanel);
		 frame.setVisible(false);
	 }

	 /**
	  *
	  */
	 private void buildPanels() {
		 final JPanel north = new JPanel();
		 final JPanel center = new JPanel();

		 mainpanel.setLayout(new BorderLayout());
		 mainpanel.add(north, BorderLayout.NORTH);
		 mainpanel.add(center, BorderLayout.CENTER);

		 center.setLayout(new BorderLayout());
		 center.add(tabbedpane, BorderLayout.CENTER);

		 north.setLayout(new BoxLayout(north, BoxLayout.PAGE_AXIS));

		 final JPanel bpanel = new JPanel();
		 bpanel.setLayout(new BoxLayout(bpanel, BoxLayout.LINE_AXIS));
		 next.setVisible(visible[0]);
		 bpanel.add(next);
		 play.setVisible(visible[1]);
		 bpanel.add(play);
		 // If PLAY button visiblen then PAUSE button visible
		 pause.setVisible(visible[1]);
		 bpanel.add(pause);

		 north.add(bpanel);

		 final JPanel spanel = new JPanel();
		 spanel.setLayout(new BoxLayout(spanel, BoxLayout.PAGE_AXIS));
		 final JLabel sliderLabel = new JLabel("Break duration in ms", JLabel.CENTER);
		 sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		 spanel.add(sliderLabel);
		 duration.setMajorTickSpacing(100);
		 duration.setMinorTickSpacing(25);
		 duration.setPaintTicks(true);
		 duration.setPaintLabels(true);
		 duration.addChangeListener(new Updater());
		 spanel.add(duration);
		 // If NEXT button or PLAY button visible, slider visible too.
		 spanel.setVisible(visible[0] || visible[1]);
		 north.add(spanel);

	 }


	 protected final void buildButtons(final IObservableStepSearchLoop ssl) {
		 this.ssl = ssl;
		 next.addActionListener(new NextActionListener());
		 play.addActionListener(new PlayActionListener());
		 pause.addActionListener(new PauseActionListener());
	 }

	 private final class NextActionListener implements ActionListener {
		 /**
		  * Invoked when an action occurs.
		  */
		 public final void actionPerformed(final ActionEvent e) {
			 ssl.runStepByStep();
		 }
	 }

	 private final class PlayActionListener implements ActionListener {
		 /**
		  * Invoked when an action occurs.
		  */
		 public final void actionPerformed(final ActionEvent e) {
			 ssl.runForAWhile();
		 }
	 }

	 private final class PauseActionListener implements ActionListener {
		 /**
		  * Invoked when an action occurs.
		  */
		 public final void actionPerformed(final ActionEvent e) {
			 ssl.pause();
		 }
	 }

	 private final class Updater implements ChangeListener {
		 /**
		  * Invoked when the target of the listener has changed its state.
		  *
		  * @param e a ChangeEvent object
		  */
		 public final void stateChanged(final ChangeEvent e) {
			 tracer.setBreaklength(duration.getValue());
		 }
	 }


	 /**
	  * Shows or hides this {@code IVisu} depending on the value of parameter
	  * {@code visible}.
	  *
	  * @param visible if {@code true}, makes the {@code IVisu} visible,
	  *                otherwise hides the {@code IVisu}.
	  */
	 public final void setVisible(final boolean visible) {
		 frame.setVisible(visible);
	 }

	 /**
	  * Add a new panel to the main frame of the Choco visualizer.
	  * Allow user to observe variables during resolution.
	  *
	  * @param panel the new panel to add
	  */
	 public final void addPanel(final AVarChocoPanel panel) {
		 //Add the panel to the frame
		 JScrollPane scpane = new JScrollPane(panel);
		 tabbedpane.addTab(panel.getPanelName(), null, scpane, "variable view");
		 //add the panel to the panel list
		 panelList.add(panel);
	 }


	 /**
	  * Initializes the {@code IVisu} from the {@code Solver}
	  *
      * @param s the solver
      */
	 public final void listen(final Solver s) {
		 for (AVarChocoPanel vp : panelList) {
			 // Get the list of visuvars
			 vp.init(getVisuvariables(s, vp.getVariables()));
			 //check wether the current size is not too small
			 width = Math.max(width, (int) vp.getDimensions().getWidth());
			 heigth = Math.max(heigth, (int) vp.getDimensions().getHeight());
		 }
         PropagationEngine pe = s.getPropagationEngine();
         try {
             Field field = pe.getClass().getDeclaredField("varEventQueue");
             field.setAccessible(true);
             VariableEventQueue[] old_veq = (VariableEventQueue[]) field.get(pe);
             VariableEventQueue[] new_veq = new VariableEventQueue[old_veq.length];
             for(int i = 0 ; i < old_veq.length; i++){
                 ObservableVarEventQueue tmp = new ObservableVarEventQueue();
                 tracer.addObservable(tmp);
                 new_veq[i] = tmp;
             }
             field.set(pe, new_veq);
             field.setAccessible(false);
         } catch (NoSuchFieldException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         }


//		 Set the variables to observe
		 tracer.setVariables(visuvars.values());

		 // Change the actual search loop by an observable and "step" one
		 IObservableStepSearchLoop ssl = chooseSearchLoop(s);

		 this.buildButtons(ssl);
		 // If there is no button, run the resolution
		 if (!visible[0] && !visible[1]) {
			 ssl.setAction(IObservableStepSearchLoop.Step.PLAY);
             duration.setValue(1);
		 }
		 tracer.setBreaklength(2);
		 tracer.addObservable(ssl);

		 //Define the correct size of the frame
		 frame.setSize(width, heigth);


         this.setVisible(true);
	 }

	 private static IObservableStepSearchLoop chooseSearchLoop(final Solver s) {
         AbstractSearchLoop ssl = s.getSearchStrategy().getSearchLoop();
         if(ssl instanceof ObservableStepSearchLoop){
             return (ObservableStepSearchLoop)ssl;
         }
         return new ObservableStepSearchLoop(s.getSearchStrategy());
	 }

	 /**
	  * Return the list of variables observed
	  *
	  * @param s the solver
	  * @param vars the array of variables
	  * @return ArrayList of IVisuVariable
	  */
	 private ArrayList<IVisuVariable> getVisuvariables(final Solver s, final Variable[] vars) {
		 ArrayList<IVisuVariable> list = new ArrayList<IVisuVariable>(vars.length);
		 for (Variable var1 : vars) {
			 Var var = s.getVar(var1);
			 VisuVariable vv = visuvars.get(var);
			 if (vv == null) {
				 vv = new VisuVariable(var);
				 visuvars.put(var, vv);
			 }
			 list.add(vv);
		 }
		 return list;
	 }
}
