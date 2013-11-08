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

/**
 *
 */
package samples;


import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import choco.visu.components.chart.dataset.MyXYTaskDataset;


/**
 * A proof-of-concept for an XYPlot-based chart showing one or more series
 * of tasks.  This is similar to the CategoryPlot-based Gantt chart, but will
 * permit combining the plot with a time series chart.
 */
public final class MyXYTaskDatasetDemo extends ApplicationFrame {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs the demo application.
	 *
	 * @param title  the frame title.
	 */
	public MyXYTaskDatasetDemo(String title) {
		super(title);
		this.setLayout(new GridLayout(2,2));
		addChart(true, false);
		addChart(true, true);
		addChart(false, false);
		addChart(false, true);
	}

	protected void addChart(boolean transposed, boolean inverted) {
		JPanel chartPanel = createDemoPanel(transposed,inverted);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 300));
		this.add(chartPanel);
	}

	private static JFreeChart createChart(MyXYTaskDataset dataset,boolean transposed, boolean inverted) {
		JFreeChart chart = ChartFactory.createXYBarChart("Transposed: "+transposed+" Inverted: "+inverted ,
				"Date/Time", true, "Resource", dataset, PlotOrientation.VERTICAL,
				true, false, false);
		dataset.setTransposed(transposed);
		dataset.setInverted(inverted);

		XYPlot plot = (XYPlot) chart.getPlot();
		DateAxis daxis = new DateAxis("Date/Time");
		SymbolAxis raxis = new SymbolAxis("Series", new String[] {"37_D","37_R", "38_D", "38_R"});
		raxis.setGridBandsVisible(false);

		if(dataset.isTransposed()) {
			plot.setRangeAxis(raxis);
			plot.setDomainAxis(daxis);
		}else {
			plot.setRangeAxis(daxis);
			plot.setDomainAxis(raxis);
		}
		XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
		renderer.setBaseItemLabelsVisible(true);
		renderer.setUseYInterval(true);

		plot.setRenderer(renderer);

		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.black);
		plot.setRangeGridlinePaint(Color.black);

		return chart;
	}


	public static JPanel createDemoPanel(boolean transposed, boolean inverted) {
		return new ChartPanel(createChart(createDataset(),transposed,inverted));
	}

	private static MyXYTaskDataset createDataset() {
		return new MyXYTaskDataset(createTasks());
	}

	protected static TaskSeriesCollection createTasks() {
		TaskSeriesCollection dataset = new TaskSeriesCollection();
		TaskSeries s1 = new TaskSeries("37_D");
		// FIXME - subtasks ignored - created 4 juil. 2011 by Arnaud Malapert
//		Task t = new Task("T1a", new Day());
//		t.addSubtask(new Task("T1a-1", new Hour(2, new Day())));
//		t.addSubtask(new Task("T1a-2", new Hour(0, new Day())));
		s1.add(new Task("T1a", new Hour(11, new Day())));
		//s1.add(t);
		s1.add(new Task("T1b", new Hour(14, new Day())));
		s1.add(new Task("T1c", new Hour(16, new Day())));
		s1.add(new Task("T1d", new Hour(18, new Day())));
		TaskSeries s2 = new TaskSeries("37_R");
		s2.add(new Task("T2a", new Hour(13, new Day())));
		s2.add(new Task("T2b", new Hour(19, new Day())));
		s2.add(new Task("T2c", new Hour(21, new Day())));
		s2.add(new Task("T2d", new Hour(2, new Day())));
		TaskSeries s3 = new TaskSeries("38_D");
		s3.add(new Task("T3a", new Hour(10, new Day())));
		s3.add(new Task("T3b", new Hour(20, new Day())));
		s3.add(new Task("T3c", new Hour(22, new Day())));
		s3.add(new Task("T3d", new Hour(3, new Day())));
		TaskSeries s4 = new TaskSeries("38_R");
		s4.add(new Task("T4a", new Hour(8, new Day())));
		s4.add(new Task("T4b", new Hour(23, new Day())));
		s4.add(new Task("T4c", new Hour(6, new Day())));
		s4.add(new Task("T4d", new Hour(5, new Day())));
		dataset.add(s1);
		dataset.add(s2);
		dataset.add(s3);
		dataset.add(s4);
		return dataset;
	}

	/**
	 * Starting point for the demonstration application.
	 *
	 * @param args  ignored.
	 */
	public static void main(String[] args) {
		MyXYTaskDatasetDemo demo = new MyXYTaskDatasetDemo("JFreeChart : Task charts demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}

}