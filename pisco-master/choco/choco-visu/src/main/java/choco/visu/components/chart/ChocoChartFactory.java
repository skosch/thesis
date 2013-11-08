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

package choco.visu.components.chart;

import static choco.visu.components.chart.ChocoDatasetFactory.createPackDataset;
import static choco.visu.components.chart.ChocoDatasetFactory.createSolutionCategoryDataset;
import static choco.visu.components.chart.ChocoDatasetFactory.createTaskCollection;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.labels.IntervalCategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.CombinedRangeCategoryPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.pack.IPackSConstraint;
import choco.cp.solver.constraints.global.pack.PackSConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.constraints.pack.PackModel;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.global.scheduling.ICumulativeResource;
import choco.kernel.solver.constraints.global.scheduling.IResource;
import choco.kernel.solver.search.limit.Limit;
import choco.kernel.solver.variables.scheduling.ITask;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.kernel.visu.AbstractVisuManager;
import choco.visu.components.chart.axis.Log2Axis;
import choco.visu.components.chart.dataset.MyXYTaskDataset;
import choco.visu.components.chart.labels.CumulTaskToolTipGenerator;
import choco.visu.components.chart.labels.TaskLabelGenerator;
import choco.visu.components.chart.labels.TaskToolTipGenerator;
import choco.visu.components.chart.renderer.MyXYBarRenderer;
import choco.visu.components.chart.renderer.MyXYBarRenderer.ResourceRenderer;
import choco.visu.components.chart.ui.ChocoChartPanel;

public final class ChocoChartFactory {


	public final static StandardChartTheme CHOCO_THEME= new StandardChartTheme("Choco");

	/** Optionally set the look and feel. */
	public static boolean useSystemLookAndFeel = true;

	static {
		CHOCO_THEME.setPlotBackgroundPaint(Color.white);
		CHOCO_THEME.setDomainGridlinePaint(Color.black);
		CHOCO_THEME.setRangeGridlinePaint(Color.black);
		CHOCO_THEME.setXYBarPainter(new StandardXYBarPainter());
		CHOCO_THEME.setShadowVisible(false);
		CHOCO_THEME.setErrorIndicatorPaint(Color.RED);
		CHOCO_THEME.setAxisOffset(new RectangleInsets());
		setColorTerminal();
		//setMonochromeTerminal();
		//setJFreeColorTerminal();

	}

	public final static DateFormat INTEGER_DATE_FORMAT = new IntegerDateFormat();

	private ChocoChartFactory() {}

	public static void setMonochromeTerminal() {
		CHOCO_THEME.setDrawingSupplier(ChocoColor.createMonochromeDrawingSupplier());
	}

	public static void setColorTerminal() {
		CHOCO_THEME.setDrawingSupplier( ChocoColor.createDefaultDrawingSupplier());
	}

	public static void setJFreeColorTerminal() {
		CHOCO_THEME.setDrawingSupplier( ChocoColor.createJFreeDrawingSupplier());
	}
	public static DateFormat getIntegerDateFormat() {
		return INTEGER_DATE_FORMAT;
	}
	public final static Marker createCapacityMarker(int value, String label, Color color) {
		return createMarker(value, label, color, TextAnchor.BASELINE_LEFT, LengthAdjustmentType.EXPAND);
	}

	public final static Marker createMarker(int value, String label, Color color, TextAnchor anchor, LengthAdjustmentType adjust) {
		final Marker lbMarker = new ValueMarker(value);
		if(label!=null) {
			lbMarker.setLabel(label);
			lbMarker.setLabelTextAnchor(anchor);
			lbMarker.setLabelOffsetType(adjust);
		}
		lbMarker.setStroke(new BasicStroke(3));
		lbMarker.setPaint(color);
		return lbMarker;
	}


	public final static void run() {

	}
	//*****************************************************************//
	//*******************  Pack Visualization  ***********************//
	//***************************************************************//

	protected static void setPackRendererSettings(StackedBarRenderer3D renderer) {
		renderer.setRenderAsPercentages(false);
		renderer.setDrawBarOutline(false);
		renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator( "{2}",   NumberFormat.getIntegerInstance()));
		renderer.setBaseItemLabelsVisible(true);
		renderer.setBaseToolTipGenerator( new StandardCategoryToolTipGenerator("{2}",   NumberFormat.getIntegerInstance()));
		renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER));
		renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER));
	}



	public static JFreeChart createPackChart(String title, Solver s,PackModel modeler) {
		return createPackChart(title, createPackDataset(s,modeler), modeler.getMaxCapacity(), false);
	}

	public static JFreeChart createPackChart(String title, Solver s,PackModel... modelers) {
		final int n = modelers.length;
		CategoryDataset[] datasets = new CategoryDataset[n];
		int capa[] = new int[n];
		for (int i = 0; i < n; i++) {
			datasets[i] = createPackDataset(s,modelers[i]);
			capa[i] = modelers[i].getMaxCapacity();
		}
		return createPackChart(title, datasets, capa, false);
	}

	public static JFreeChart createPackChart(String title, Solver s) {
		final int n = s.getModel().getNbConstraintByType(ConstraintType.PACK);
		Iterator<Constraint> cstr = s.getModel().getConstraintByType(ConstraintType.PACK);
		CategoryDataset[] datasets = new CategoryDataset[n];
		for (int i = 0; i < n; i++) {
			PackSConstraint pack = (PackSConstraint) s.getCstr(cstr.next());
			datasets[i] = createPackDataset(pack.getNbBins(), pack.getBins(), pack.getSizes());
		}
		return createPackChart(title, datasets, null, false);
	}

	public static JFreeChart createPackChart(String title, IPackSConstraint pack) {
		return createPackChart(title, createPackDataset(pack.getNbBins(), pack.getBins(), pack.getSizes()), -1, false);
	}


	public static JFreeChart createPackChart(String title, CategoryDataset dataset, int capacity, boolean legend) {
		final JFreeChart chart = 
			ChartFactory.createBarChart3D(title, "Bins","Load", dataset, PlotOrientation.VERTICAL, legend, false, false);
		final CategoryPlot plot = chart.getCategoryPlot();
		StackedBarRenderer3D   renderer   =   new StackedBarRenderer3D();
		setPackRendererSettings(renderer);
		plot.setRenderer(renderer);
		if(capacity>0) {
			Marker capaMarker = createCapacityMarker(capacity, "Capacity", Color.lightGray);
			plot.addRangeMarker(0, capaMarker, Layer.FOREGROUND);
		}
		CHOCO_THEME.apply(chart);
		plot.setDomainGridlinesVisible(false);
		return chart;

	}


	public static JFreeChart createPackChart(String title, CategoryDataset[] datasets, int[] capacity, boolean legend) {
		//Renderer
		StackedBarRenderer3D   renderer = new StackedBarRenderer3D();
		setPackRendererSettings(renderer);
		//Axis
		final NumberAxis laxis = new NumberAxis("Load");
		laxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		final CategoryAxis baxis = new CategoryAxis("Bins");
		//create and add subplots
		final CombinedDomainCategoryPlot plot = new CombinedDomainCategoryPlot(baxis);
		plot.setGap(10);
		for (int i = 0; i < datasets.length; i++) {
			final CategoryPlot subplot = new CategoryPlot(datasets[i],null, laxis,renderer);
			int w =1;
			if(capacity==null || capacity[i]>0) {
				Marker capaMarker = createCapacityMarker(capacity[i], "Capacity", Color.lightGray);
				plot.addRangeMarker(0, capaMarker, Layer.FOREGROUND);
				w =capacity[i];
			}
			plot.add(subplot, w);
		}
		JFreeChart chart = new JFreeChart(
				title, JFreeChart.DEFAULT_TITLE_FONT,
				plot, legend
		);
		CHOCO_THEME.apply(chart);
		plot.setDomainGridlinesVisible(false);
		return chart;
	}


	//*****************************************************************//
	//*******************  Unary Resources   *************************//
	//***************************************************************//

	protected static String[] generateRscLabels(MyXYTaskDataset dataset) {
		String[] res = null;
		final TaskSeriesCollection coll = dataset.getTasks();
		if(dataset.isInverted()) {
			int nbi = 1;
			for (int i = 0; i < coll.getSeriesCount(); i++) {
				if(nbi<coll.getSeries(i).getItemCount()) {
					nbi = coll.getSeries(i).getItemCount();
				}
			}
			res =new String[nbi];
			for (int i = 0; i < nbi; i++) {
				res[i] = "rsc "+i;
			}	
		}else {
			final int nbs = dataset.getSeriesCount();
			res =new String[nbs];
			for (int i = 0; i < nbs; i++) {
				res[i] = coll.getSeries(i).getKey().toString();
			}
		}
		return res;

	}


	protected static void generateAxis(MyXYTaskDataset dataset,XYPlot plot, String[] rscAxisLabels) {
		final DateAxis daxis = createDateAxis();
		final SymbolAxis raxis =  new SymbolAxis("Resources", 
				rscAxisLabels==null ? generateRscLabels(dataset) : rscAxisLabels) ;
		raxis.setGridBandsVisible(false);
		if(dataset.isTransposed()) {
			plot.setRangeAxis(raxis);
			plot.setDomainAxis(daxis);
		}else {
			plot.setRangeAxis(daxis);
			plot.setDomainAxis(raxis);
		}
	}

	public static DateAxis createDateAxis() {
		DateAxis domainAxis = new DateAxis("Time");
		domainAxis.setPositiveArrowVisible(true);
		domainAxis.setDateFormatOverride(ChocoChartFactory.INTEGER_DATE_FORMAT);
		return domainAxis;
	}

	public static NumberAxis createIntegerAxis(String title) {
		NumberAxis rangeAxis = new NumberAxis(title);
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setPositiveArrowVisible(true);
		return rangeAxis;		
	}


	public static JFreeChart createUnaryHChart(String title, IResource<TaskVar> rsc) {
		return createUnaryHChart(title, createTaskCollection(rsc), ResourceRenderer.COLUMN);
	}

	public static JFreeChart createUnaryHChart(String title, Solver scheduler) {
		return createUnaryHChart(title, createTaskCollection(scheduler, scheduler.getModel().getConstraintByType(ConstraintType.DISJUNCTIVE)), ResourceRenderer.COORD);
	}

	public static JFreeChart createUnaryHChart(String title, Solver scheduler, Constraint... resources) {
		return createUnaryHChart(title, createTaskCollection(scheduler, Arrays.asList(resources).iterator()), ResourceRenderer.COLUMN);
	}
	
	public static JFreeChart createUnaryHChart(String title, Solver scheduler, TaskVariable[][] resources) {
		return createUnaryHChart(title, createTaskCollection(scheduler, "M_", resources), ResourceRenderer.COLUMN);
	}

	public static JFreeChart createUnaryVChart(String title, Solver scheduler) {
		return createUnaryVChart(title, createTaskCollection(scheduler, scheduler.getModel().getConstraintByType(ConstraintType.DISJUNCTIVE)), ResourceRenderer.COORD);
	}


	public static JFreeChart createUnaryHChart(String title, TaskSeriesCollection coll, ResourceRenderer renderer) {
		final MyXYTaskDataset dataset = new MyXYTaskDataset(coll);
		dataset.setTransposed(true);
		dataset.setInverted(false);
		return createUnaryChart(title, dataset, false, renderer, null);
	}

	public static JFreeChart createUnaryVChart(String title, TaskSeriesCollection coll, ResourceRenderer renderer) {
		final MyXYTaskDataset dataset = new MyXYTaskDataset(coll);
		dataset.setTransposed(false);
		dataset.setInverted(false);
		return createUnaryChart(title, dataset, false, renderer, null);
	}



	public static JFreeChart createUnaryChart(String title,MyXYTaskDataset dataset,boolean legend, ResourceRenderer type, String[] rscAxisLabels) {
		JFreeChart chart = ChartFactory.createXYBarChart(title,
				"Date/Time", true, "Resources", dataset, PlotOrientation.VERTICAL,
				legend, false, false);

		XYPlot plot = chart.getXYPlot();
		generateAxis(dataset, plot, rscAxisLabels);
		//Renderer
		XYBarRenderer renderer = new MyXYBarRenderer(type);
		renderer.setBaseItemLabelGenerator(new TaskLabelGenerator("{0}"));
		renderer.setBaseToolTipGenerator(new TaskToolTipGenerator("{0}: {1} -> {3}"));
		renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.BOTTOM_CENTER,TextAnchor.CENTER, 0.0), true);
		renderer.setBaseItemLabelsVisible(true);
		renderer.setUseYInterval(true);
		//Plot
		plot.setRenderer(renderer);
		CHOCO_THEME.apply(chart);
		return chart;
	}


	//*****************************************************************//
	//*******************  Cumulative Resource ***********************//
	//***************************************************************//


	@SuppressWarnings("unchecked")
	public static JFreeChart createCumulativeChart(String title, CPSolver s, Constraint rsc, boolean legend) {
		SConstraint cstr = s.getCstr(rsc);
		if (cstr instanceof ICumulativeResource<?>) {
			ICumulativeResource<TaskVar> cumul = (ICumulativeResource<TaskVar>) cstr;
			return createCumulativeChart(title, cumul, legend);
		}
		return null;
	}

	public static JFreeChart createCumulativeChart(String title, ICumulativeResource<TaskVar> cumul, boolean legend) {
		if(cumul.hasOnlyPosisiveHeights()) {
			return createCumulativeChart(title, ChocoDatasetFactory.createCumulativeDataset(cumul),
					cumul.getCapacity().getVal(), legend,INTEGER_DATE_FORMAT,new CumulTaskToolTipGenerator(cumul));
		}
		return null;
	}

	public static JFreeChart createCumulativeChart(String title, TableXYDataset dataset, int capacity,boolean legend, DateFormat format,XYToolTipGenerator tooltip) {
		// Renderer
		StackedXYBarRenderer renderer = new StackedXYBarRenderer();
		renderer.setShadowVisible(false);
		renderer.setShadowXOffset(0);
		if(tooltip==null) {
			renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: h={2}", new SimpleDateFormat(), NumberFormat.getInstance()));
		}else {
			renderer.setBaseToolTipGenerator(tooltip);
		}
		renderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator("{0}",new SimpleDateFormat(), NumberFormat.getInstance()));
		renderer.setBaseItemLabelsVisible(true);
		// Plot
		XYPlot plot =  new XYPlot(dataset, createDateAxis(), createIntegerAxis("Load"), renderer);
		//Marker
		if(capacity>0) {
			Marker capaMarker = createCapacityMarker(capacity, "Capacity", Color.red);
			plot.addRangeMarker(0, capaMarker, Layer.FOREGROUND);
		}
		// Chart
		JFreeChart chart = new JFreeChart(title,plot);
		CHOCO_THEME.apply(chart);
		return chart;

	}
	//*****************************************************************//
	//*******************  Gantt Charts  ********************************//
	//***************************************************************//

	public static JFreeChart createGanttChart(String title, ITask[] tasks) {
		return createGanttChart(title, ChocoDatasetFactory.createGanttDataset(tasks), false, null);
	}
	
	public static JFreeChart createGanttChart(String title, ITask[] tasks, int[] dueDates) {
		return createGanttChart(title, ChocoDatasetFactory.createGanttDataset(tasks, dueDates), false, null);
	}

	
	public static JFreeChart createGanttChart(String title, IntervalCategoryDataset dataset, boolean legend, XYToolTipGenerator tooltip) {
		CategoryAxis categoryAxis = new CategoryAxis("Tasks");
        DateAxis dateAxis = createDateAxis();

        GanttRenderer renderer = new GanttRenderer();
        renderer.setStartPercent(0.4);
        renderer.setEndPercent(0.6);
        renderer.setIncompletePaint(ChocoColor.COLOR_211_1);
        renderer.setCompletePaint(ChocoColor.COLOR_274_3);
        renderer.setBaseToolTipGenerator(
                new IntervalCategoryToolTipGenerator(
                    "{1}: {3} -> {4}", ChocoChartFactory.INTEGER_DATE_FORMAT
                )
            );
        
        CategoryPlot plot = new CategoryPlot(
            dataset, categoryAxis, dateAxis, renderer
        );
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        JFreeChart chart = new JFreeChart(
            title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend
        );
    	CHOCO_THEME.apply(chart);
        return chart;
	}

	//*****************************************************************//
	//*******************  Solution Charts  ********************************//
	//***************************************************************//




	private final static CategoryAxis createSolutionAxis(String name) {
		final CategoryAxis axis = new CategoryAxis(name);
		axis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		return axis;
	}

	protected final static BarRenderer createSolutionBarRenderer() {
		final BarRenderer renderer = new BarRenderer();
		renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		final ItemLabelPosition p = new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, Math.PI/2);
		renderer.setBasePositiveItemLabelPosition(p);
		renderer.setBaseItemLabelsVisible(true);
		return renderer;
	}


	//	public static JFreeChart createHeuristicsChart(ListHeuristics heuritics) {
	//		final JFreeChart chart = createBarChart("Heuristics Solutions", createHeuristicsCategoryDataset(heuritics), "Itérations", 0);
	//		CategoryPlot plot =  chart.getCategoryPlot();
	//		plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
	//		CHOCO_THEME.apply(chart);
	//		return chart;
	//	}

	public static JFreeChart createSolutionChart(CPSolver s, Limit l) {
		final JFreeChart chart = createBarChart("Solver Solutions", createSolutionCategoryDataset(s, l), l.toString(), 0);
		CategoryPlot plot = chart.getCategoryPlot();
		plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		CHOCO_THEME.apply(chart);
		return chart;
	}

	//	public static JFreeChart createSolutionChart(GenericShopProblem shop, Limit limit) {
	//		if (shop.getHeuristics() instanceof ListHeuristics) {
	//			ListHeuristics h = (ListHeuristics) shop.getHeuristics();
	//			if(h.hasSolution()) {
	//				if(shop.getCPSearch().hasSolution()) {
	//					final CategoryDataset[] datasets = { createHeuristicsCategoryDataset(h),createSolutionCategoryDataset((CPSolver) shop.getSolver(), limit)};
	//					final String[] labels = {"Itération", limit.toString()};
	//					return createCombinedBarChart("Solutions", datasets, labels, shop.getComputedLowerBound());
	//
	//				}else {
	//					return createHeuristicsChart(h);
	//				}
	//			}
	//		}
	//		return createSolutionChart( (CPSolver) shop.getSolver(), limit);
	//	}

	public static JFreeChart createBarChart(String title, CategoryDataset dataset,String categoryAxisLabel, int lowerBound) {
		JFreeChart result = ChartFactory.createBarChart(title, categoryAxisLabel, "Objective", dataset, 
				PlotOrientation.VERTICAL, true, true, false);
		CategoryPlot plot =  result.getCategoryPlot();
		Marker marker = createMarker(lowerBound,"Lower Bound",Color.red,TextAnchor.CENTER_LEFT,LengthAdjustmentType.EXPAND);
		plot.addRangeMarker(0, marker, Layer.FOREGROUND);
		plot.setRenderer(createSolutionBarRenderer());
		CHOCO_THEME.apply(result);
		return result;
	}

	public static JFreeChart createCombinedBarChart(String title, CategoryDataset[] datasets,String[] categoryAxisLabel, int lowerBound) {
		final Marker lbMarker =  createMarker(lowerBound,"Lower Bound",Color.red,TextAnchor.CENTER_LEFT,LengthAdjustmentType.EXPAND);
		final CombinedRangeCategoryPlot plot = new CombinedRangeCategoryPlot(new NumberAxis("Objective"));
		plot.setOrientation(PlotOrientation.VERTICAL);
		for (int i = 0; i < datasets.length; i++) {
			final CategoryPlot subplot = new CategoryPlot(datasets[i], createSolutionAxis(categoryAxisLabel[i]), null, createSolutionBarRenderer());
			subplot.addRangeMarker(0, lbMarker, Layer.FOREGROUND);
			plot.add(subplot, datasets[i].getColumnCount());
		}
		JFreeChart chart = new JFreeChart(
				title,
				new Font("SansSerif", Font.BOLD, 12),
				plot,
				true
		);
		CHOCO_THEME.apply(chart);
		return chart;
	}

	//*****************************************************************//
	//*******************  Deviation Chart  **************************//
	//***************************************************************//

	public static JFreeChart createDeviationLineChart(String title,String xlabel, String ylabel, YIntervalSeriesCollection dataset) {
		DeviationRenderer renderer = new DeviationRenderer(true, false);
		LogAxis lxaxis = new LogAxis(xlabel);
		lxaxis.setLowerMargin(0);
		//FIXME lxaxis.setMinorTickMarksVisible(true);
		LogAxis lyaxis = new Log2Axis(ylabel);
		XYPlot plot = new XYPlot(dataset, lxaxis, lyaxis, renderer);
		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,plot, true);
		CHOCO_THEME.apply(chart);
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			//renderer.setSeriesStroke(0, new BasicStroke(2f));
			Color color = (Color) renderer.lookupSeriesPaint(i);
			renderer.setSeriesFillPaint(i,color.brighter().brighter());
		}  
		return chart;
	}


	//*****************************************************************//
	//*******************  Util **************************************//
	//***************************************************************//

	protected final static int DEFAULT_WIDTH = 800;

	protected final static int DEFAULT_HEIGHT = 600;

	public static void createAndShowGUI(final String title,final int width,final int height, final LayoutManager layout, final Component... components) {
		//Schedule a job for the event dispatch thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (useSystemLookAndFeel) {
					try {
						UIManager.setLookAndFeel(
								UIManager.getSystemLookAndFeelClassName());
					} catch (Exception e) {}
				}
				final ApplicationFrame frame = new ApplicationFrame(title);
				if(layout!=null) {frame.setLayout(layout);}
				final Dimension dim = new   Dimension(width, height);
				for (Component c : components) {
					c.setPreferredSize(dim);
					frame.add(c);
				}
				frame.pack();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				RefineryUtilities.centerFrameOnScreen(frame);
				frame.setVisible(true);
			}});
	}

	public static void createAndShowGUI(String title,int width,int height, LayoutManager layout, JFreeChart...   charts) {
		Component[] components = new Component[charts.length];
		for (int i = 0; i < charts.length; i++) {
			components[i] = new ChartPanel(charts[i]);
		}
		createAndShowGUI(title, width, height, layout, components);
	}


	public static void createAndShowGUI(String title, JFreeChart jfreechart) {
		createAndShowGUI(title, DEFAULT_WIDTH, DEFAULT_HEIGHT, null, jfreechart);
	}

	public static void createAndShowGUI(String title, Solver solver) {
		createAndShowGUI(title, DEFAULT_WIDTH, DEFAULT_HEIGHT, null, new ChocoChartPanel(solver));
	}
	
	public static JFreeChartManager getJFreeChartManager() {
		return JFreeChartManager.getInstance();
	}

	
}

final class IntegerDateFormat extends SimpleDateFormat {

	private static final long serialVersionUID = 9044123458512557258L;

	public IntegerDateFormat() {
		super();
		this.setNumberFormat(NumberFormat.getInstance());
	}


	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo,
			FieldPosition pos) {
		return numberFormat.format(date.getTime(), toAppendTo, pos);
	}

}


class JFreeChartManager extends AbstractVisuManager {
	
	/** 
	 * The shared instance. 
	 */
	private final static JFreeChartManager SINGLOTON = new JFreeChartManager();

	/** 
	 * Private constructor. 
	 */
	private JFreeChartManager() {
		super();
		setDefaultWidth(ChocoChartFactory.DEFAULT_WIDTH);
		setDefaultHeight(ChocoChartFactory.DEFAULT_HEIGHT);
	}

	/** 
	 * Returns this shared instance. 
	 * 
	 * @returns The shared instance 
	 */
	public final static JFreeChartManager getInstance() {
		return SINGLOTON;
	}

	@Override
	protected String getFileExtension() {
		return "pdf";
	}

	@Override
	protected boolean doExport(File file, Object chart, int width, int height)
			throws IOException {
		if (chart instanceof JFreeChart) {
			PdfExport.saveChartAsPDF(file, (JFreeChart) chart, width, height);
			return true;			
		} else return false;
	}

	@Override
	protected boolean doShow(Object chart, int width, int height) {
		if (chart instanceof JFreeChart) {
			JFreeChart jfc = (JFreeChart) chart;
			ChocoChartFactory.createAndShowGUI(jfc.getTitle().getText(), width, height, null, jfc);
			return true;
		} else return false;
		
	}
	
	
	
}
