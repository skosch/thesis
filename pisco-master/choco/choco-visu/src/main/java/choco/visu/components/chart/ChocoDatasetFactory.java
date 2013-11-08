/**
 *  Copyright (c) 1999-2010, Ecole des Mines deM Nantes
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

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.XYSeries;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.pack.PackSConstraint;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.constraints.pack.PackModel;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.Solution;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.global.scheduling.ICumulativeResource;
import choco.kernel.solver.constraints.global.scheduling.IResource;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import choco.kernel.solver.search.limit.Limit;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.ITask;
import choco.kernel.solver.variables.scheduling.ITimePeriodList;
import choco.kernel.solver.variables.scheduling.TaskVar;

public final class ChocoDatasetFactory {



	/** empty constructor */
	private ChocoDatasetFactory() {}


	//*****************************************************************//
	//*******************  Tasks  ********************************//
	//***************************************************************//
	public static Task createTask(ITask t) {
		if(t.isPartiallyScheduled()) {
			ITimePeriodList periods = t.getTimePeriodList();
			final Task res = new Task(t.getName(),getTimePeriod(periods.getPeriodFirst(),periods.getPeriodLast()));
			res.setPercentComplete(1);
			// TODO -  setPercentComplete() - created 4 avr. 2012 by A. Malapert
			if(t.isInterrupted()) {
				for (int i = 0; i < periods.getTimePeriodCount(); i++) {
					final Task subtask = new Task(t.getName(),getTimePeriod(periods.getPeriodStart(i),periods.getPeriodEnd(i)));
					subtask.setPercentComplete(1);
					res.addSubtask(subtask);
				}
			}
			return res;
		} else return null;
	}

	public static Task createTask(ITask t, int releaseDate) {
		assert releaseDate <= t.getEST();
		Task t1 = new Task(t.getName(),getTimePeriod(t.getEST(),t.getLCT()));
		t1.addSubtask( new Task("Waiting",getTimePeriod(releaseDate, t.getEST())));
		t1.addSubtask( new Task("Execution",getTimePeriod(t.getEST(), t.getLCT())));
		t1.getSubtask(0).setPercentComplete(0);
		t1.getSubtask(1).setPercentComplete(1);
		return t1;
	}

	public static Task createEarlinessTardiness(ITask t, int dueDate) {
		Task t1;
		if(t.getECT() <= dueDate) {
			t1 = new Task(t.getName(),getTimePeriod(t.getECT(), dueDate));
			t1.setPercentComplete(0);
		} else {
			t1 = new Task(t.getName(),getTimePeriod(dueDate, t.getECT()));
			t1.setPercentComplete(1);	
		}
		return t1;
	}



	public static Task createTask(CPSolver s,TaskVariable t) {
		return createTask(s.getVar(t));
	}

	public static TimePeriod getTimePeriod(final long begin,final long end) {
		return new SimpleTimePeriod(new Date(begin), new Date(end));
	}


	public static TaskSeries createTaskSeries(String name, ITask... tasks) {
		final TaskSeries s = new TaskSeries(name);
		for (int i = 0; i < tasks.length; i++) {
			s.add(createTask(tasks[i]));
		}
		return s;
	}

	public static TaskSeries createTaskSeries(IResource<TaskVar> rsc) {
		final TaskSeries s = new TaskSeries(rsc.getRscName());
		for (int i = 0; i < rsc.getNbTasks(); i++) {
			if( rsc.getRTask(i).isRegular()) {
				s.add(createTask(rsc.getTask(i)));
			}
		}
		return s;
	}
	
	public static TaskSeriesCollection createTaskCollection(Solver solver, String prefix, TaskVariable[][] tasks) {
		TaskSeriesCollection c = new TaskSeriesCollection();
		for (int i = 0; i < tasks.length; i++) {
			c.add(createTaskSeries(prefix+(i+1), solver.getVar(tasks[i])));
		}
		return c;
	}

	public static TaskSeriesCollection createTaskCollection(IResource<TaskVar>... resources) {
		TaskSeriesCollection c = new TaskSeriesCollection();
		for (IResource<TaskVar> rsc : resources) {
			c.add(createTaskSeries(rsc));
		}
		return c;
	}


	public static TaskSeriesCollection createTaskCollection(Solver s, final Iterator<Constraint> iter) {
		final TaskSeriesCollection coll = new TaskSeriesCollection();
		while(iter.hasNext()) {
			final SConstraint<?> c = s.getCstr(iter.next());
			if (c instanceof IResource<?>) {
				coll.add(createTaskSeries((IResource<TaskVar>) c));
			}
		}
		return coll;
	}

	protected static Integer[] createDates(ICumulativeResource<TaskVar> rsc) {
		final Set<Integer> dateSet = new HashSet<Integer>();
		Iterator<TaskVar> iter = rsc.getTaskIterator();
		while(iter.hasNext()) {
			TaskVar<?> t = iter.next();
			dateSet.add(t.start().getVal());
			dateSet.add(t.end().getVal());
		}
		Integer[] res = dateSet.toArray(new Integer[dateSet.size()]);
		Arrays.sort(res);
		return res;
	}

	public static TimeTableXYDataset createCumulativeDataset(ICumulativeResource<TaskVar> rsc) {
		Integer[] dates = createDates(rsc);
		TimePeriod[] periods = new TimePeriod[dates.length-1];
		for (int i = 0; i < periods.length; i++) {
			periods[i] = new SimpleTimePeriod(dates[i],dates[i+1]);
		}
		final TimeTableXYDataset dataset = new TimeTableXYDataset();
		for (int i = 0; i < rsc.getNbTasks(); i++) {
			if(rsc.getRTask(i).isRegular()) {
				TaskVar<?> t = rsc.getTask(i);
				int b = Arrays.binarySearch(dates, t.start().getVal());
				int e = Arrays.binarySearch(dates, t.end().getVal());
				int h = rsc.getHeight(i).getVal();
				for (int j = b; j < e; j++) {
					dataset.add(periods[j], h, t.getName());
				}
			}
		}
		return dataset;
	}

	//*****************************************************************//
	//*******************  Pack  ********************************//
	//***************************************************************//


	public static CategoryDataset[] createPackDataset(String title, Solver s) {
		final int n = s.getModel().getNbConstraintByType(ConstraintType.PACK);
		Iterator<Constraint> cstr = s.getModel().getConstraintByType(ConstraintType.PACK);
		CategoryDataset[] datasets = new CategoryDataset[n];
		for (int i = 0; i < n; i++) {
			PackSConstraint pack = (PackSConstraint) s.getCstr(cstr.next());
			datasets[i] = createPackDataset(pack.getNbBins(), pack.getBins(), pack.getSizes());
		}
		return datasets;
	}

	public static CategoryDataset createPackDataset(int nbBins, IntDomainVar[] bins,int[] sizes) {
		DefaultCategoryDataset   dataset =   new   DefaultCategoryDataset();
		int[] series = new int[nbBins];
		for (int i = 0; i < bins.length; i++) {
			if(bins[i].isInstantiated()) {
				int b = bins[i].getVal();
				dataset.addValue(sizes[i],   "Series "+series[b],   "B"+b);	
				series[b]++;
			}
		}
		return dataset;
	}

	public static CategoryDataset createPackDataset(Solver s,PackModel modeler) {
		return createPackDataset(modeler.getNbBins()
				, s.getVar(modeler.getBins())
				, VariableUtils.getConstantValues(s.getVar(modeler.getSizes()))
				);
	}

	//*****************************************************************//
	//*******************  Gantt *************************************//
	//***************************************************************//

	public static TaskSeriesCollection createGanttDataset(ITask[] tasks) {
		final TaskSeries s1 = new TaskSeries("Tasks");
		for (int i = 0; i < tasks.length; i++) {
			final Task t = createTask(tasks[i]);
			if( t != null) {s1.add(createTask(tasks[i]));};
		}
		final TaskSeriesCollection coll = new TaskSeriesCollection();
		coll.add(s1);
		return coll;
	}
	
	public static TaskSeriesCollection createGanttDataset(ITask[] tasks, int[] dueDates) {
		TaskSeriesCollection coll = createGanttDataset(tasks);
		final TaskSeries s1 = new TaskSeries("Earliness/Tardiness");
		for (int i = 0; i < tasks.length; i++) {
			s1.add(createEarlinessTardiness(tasks[i], dueDates[i]));
		}
		coll.add(s1);
		return coll;
	}
	//*****************************************************************//
	//*******************  Solver solutions  ********************************//
	//***************************************************************//

	public static XYSeries createSolutionXYSeries(CPSolver s, Limit limit) {
		XYSeries series = new XYSeries("solver sol.");
		final AbstractGlobalSearchStrategy strat = s.getSearchStrategy();
		for (Solution sol : strat.getStoredSolutions()) {
			series.add(limit.getValue(sol.getMeasures()), sol.getObjectiveValue());
		}

		return series;
	}

	public static CategoryDataset createSolutionCategoryDataset(CPSolver s, Limit limit) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		final AbstractGlobalSearchStrategy strat = s.getSearchStrategy();
		final String series = "Solver sol.";
		//reversed loop
		final List<Solution> sols = strat.getStoredSolutions();
		for (int i = sols.size()-1; i >=0; i--) {
			final Solution sol = sols.get(i);
			dataset.addValue(sol.getObjectiveValue(), series, Integer.valueOf(limit.getValue(sol.getMeasures())));
		}
		return dataset;
	}

	//	public static CategoryDataset createHeuristicsCategoryDataset(ListHeuristics heuristics) {
	//		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	//		List<HeuristicsEvent> func = heuristics.getEvents().getFunction();
	//		final String series = "Heuristics sol.";
	//		for (HeuristicsEvent evt : func) {
	//			dataset.addValue(evt.getMakespan(), series, Integer.valueOf(evt.getCoordinate()));
	//		}
	//		return dataset;
	//	}


}


