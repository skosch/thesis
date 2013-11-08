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

package choco.cp.solver.constraints.global.scheduling.cumulative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import choco.cp.solver.constraints.global.scheduling.trees.CumTreeT;
import choco.cp.solver.constraints.global.scheduling.trees.IVilimTree.TreeMode;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.comparator.TaskComparators;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.TaskVar;

/**
 * @author Arnaud Malapert</br> 
 * @since 4 mars 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public class CumulRules implements ICumulRules {
	/**
	 * A class to manipulate the different consumption
	 * of tasks and their indexes in R
	 */
	private static class Consumption {
		// denote a height or consumption of a task and its index in R
		public int h, idx;

		// Tells the algorithm that the R values have already been computed for
		// this height/consumption (for lazy computation purposes)
		public boolean dyncomputation;

		public Consumption(final int h, final int idx) {
			this.h = h;
			this.idx = idx;
			dyncomputation = false;
		}
	}

	public final AbstractCumulativeSConstraint rsc;

	protected final LinkedList<IRTask> tasksLX;

	protected final List<IRTask> tasksLY;

	protected CumTreeT<TaskVar> thetatree;

	/**
	 * The different ressource consumptions of all tasks.
	 * Sc.lenght <= nbTask
	 * It can varies over time with variable consumption (or heights) !
	 */
	protected Consumption[] Sc;

	/**
	 * Reference to the consumption object related to each task
	 */
	protected Consumption[] taskheights;

	/**
	 * temporary data for edge finding (initialized by dynprog)
	 * to store the inner maximization of the edge finding bound on the start/end variables of each task.
	 */
	protected long[][] R;




	public CumulRules(final AbstractCumulativeSConstraint rsc) {
		super();
		this.rsc = rsc;
		tasksLY= new ArrayList<IRTask>(rsc.asRTaskList());
		tasksLX= new LinkedList<IRTask>(rsc.asRTaskList());
		taskheights = new Consumption[rsc.getNbTasks()];
	}

	@Override
	public void initializeEdgeFindingData() {
		final HashMap<Integer, Consumption> map = new HashMap<Integer, Consumption>();
		//extract all different heights among all the tasks
		int cpt = 0;
		for (int i = 0; i < rsc.getNbTasks(); i++) {
			final int h = rsc.getHeight(i).getInf();
			Consumption cons = map.get(h);
			if (cons == null) {
				cons = new Consumption(h, cpt);
				map.put(h, cons);
				cpt++;
			}
			taskheights[i] = cons;
		}
		//build the consumption table
		Sc = new Consumption[map.size()];
		final Set<Map.Entry<Integer, Consumption>> entries = map.entrySet();
		for (final Iterator<Map.Entry<Integer, Consumption>> it = entries.iterator(); it.hasNext();) {
			final Consumption cons = it.next().getValue();
			Sc[cons.idx] = cons;
		}
		R = new long[Sc.length][rsc.getNbTasks()];

	}

	@Override
	public void initializeEdgeFindingEnd() {
		Collections.sort(tasksLX, TaskComparators.makeReverseRLatestCompletionTimeCmp());
		Collections.sort(tasksLY, TaskComparators.makeReverseREarliestStartingTimeCmp());
	}

	@Override
	public void initializeEdgeFindingStart() {
		Collections.sort(tasksLX, TaskComparators.makeREarliestStartingTimeCmp());
		Collections.sort(tasksLY, TaskComparators.makeRLatestCompletionTimeCmp());
	}

	@Override
	public void reinitConsumption() {
		for (int i = 0; i < Sc.length; i++) {
			Sc[i].dyncomputation = false;
		}

	}

	//****************************************************************//
	//********* Tasks Intervals **************************************//
	//********* - version n^2 ****************************************//
	//********* - version nlog(n) with a theta-tree ******************//
	//****************************************************************//

	public void oldSlowTaskIntervals() throws ContradictionException {
		int D = Integer.MIN_VALUE; 
		for (IRTask i : tasksLY) {
			if(D != i.getTaskVar().getLCT()) {	//intervals were not already checked
				D = i.getTaskVar().getLCT(); // D is the end of this interval
				long tote = 0; // energy
				final ListIterator<IRTask> iter=tasksLX.listIterator(tasksLX.size());
				while(iter.hasPrevious()) {
					final IRTask crt=iter.previous();
					final TaskVar t= crt.getTaskVar();
					long e= crt.getMinConsumption();
					if (t.getLCT() > D) {
						// if task can end after D, part (or all) of it can be outside
						e = Math.min(e, (D - t.getLST()) * crt.getMinHeight());
					}
					if (e > 0) { // e<=0 means that task can be fully outside
						tote += e;
						final long diff = D - t.getEST();		   //avoid integer overload
						final long capaMaxDiff = rsc.getMaxCapacity() * diff;
						if (capaMaxDiff < tote) {
							rsc.fail();
						}
					}
				}
			}
		}

	}

	private final void checkInterval(int startI, int endI, long energy) throws ContradictionException {
		final long diff = endI - startI;		   //avoid integer overload
		final long capaMaxDiff = rsc.getMaxCapacity() * diff;
		if (capaMaxDiff < energy) {
			//SConstraint.LOGGER.warning("fail");
			//ChocoLogging.flushLogs();
			//System.out.println(StringUtils.pretty(tasksLY));
			rsc.fail();
		}
	}


	@Override
	public void slowTaskIntervals() throws ContradictionException {
		long e, tote;
		int startI, endI = Integer.MIN_VALUE;
		for (IRTask i : tasksLY) {
			if(endI > i.getTaskVar().getLCT()) {
				endI = i.getTaskVar().getLCT(); // D is the end of this interval
				startI = endI;
				tote = 0; // energy
				final ListIterator<IRTask> iter=tasksLX.listIterator(tasksLX.size());
				while(iter.hasPrevious()) {
					final IRTask crt=iter.previous();
					final TaskVar t= crt.getTaskVar();
					//insert consumption
					e= 0;
					//positive height
					if (t.getLCT() <= endI) {
						//task is fully inside
						e= crt.getMinHeight() * crt.getTaskVar().getMinDuration();
					}else if(t.getLST() < endI) {
						// part of the task is outside
						e = (endI - t.getLST()) * crt.getMinHeight();
					}
					if(t.getEST() < startI) {
						checkInterval(startI, endI, tote);
						startI = t.getEST();
					}
					//ChocoLogging.getSolverLogger().info(t.pretty()+ ": "+tote+"+="+e);
					tote += e;
				}
				checkInterval(startI, endI, tote); //last check, for example est_i = e and lct_i =d.
			}
		}
	}


		@Override
		public void taskIntervals() throws ContradictionException {
			//sort in ascending order of lct_i
			//thetatree.reset(); <hca> todo: the reset should resort the leaves...
			if(thetatree==null) {thetatree = new CumTreeT<TaskVar>(this.rsc);}
			thetatree.setMode(TreeMode.ECT);
			final int maxCapa = rsc.getMaxCapacity();
			for (IRTask i : tasksLY) {
				//VizFactory.toDotty(thetatree);
				thetatree.insertInTheta(i);
				if (thetatree.getEnergy() >  maxCapa * i.getTaskVar().getLCT()) {
					rsc.fail();
				}
			}
	
		}

	//*************************************************************//
	//********* Edge finding for updating earliest start **********//
	//********* - version O(n^2k) without theta-lambda-tree *******//
	//********* - version O(n^2k) with    theta-lambda-tree *******//
	//*************************************************************//

	/**
	 * Lazy computation of the inner maximization of
	 * the edge finding. Instead of precumputed the R values, we call this method
	 * for a given consumption
	 * this method assumes that the task intervals have not failed !
	 */
	protected void calcR_start(final Consumption cons) {
		final int c = cons.h;
		final int i = cons.idx;
		long[] E = new long[rsc.getNbTasks()];
		for (int j = 0; j < rsc.getNbTasks(); j++) {
			//E[j] = 0;
			R[i][j] = Long.MIN_VALUE;
		}
		final ListIterator<IRTask> iter = tasksLX.listIterator(tasksLX.size());
		while (iter.hasPrevious()) {
			final IRTask crx = iter.previous();
			final TaskVar x = crx.getTaskVar();
			final long ex = crx.getMinConsumption();
			for (int k = 0; k < rsc.getNbTasks(); k++) {
				final IRTask cry = tasksLY.get(k);
				final int ylct = cry.getTaskVar().getLCT();
				final int yidx = cry.getTaskIndex();
				if (x.getLCT() <= ylct) {
					E[yidx] += ex;
					final long rest = E[yidx] - (rsc.getMaxCapacity() - c) * (ylct - x.getEST());
					final long q1 = R[i][yidx];
					final long q2 = (k == 0) ? Long.MIN_VALUE : R[i][tasksLY.get(k - 1).getTaskIndex()];
					final long q3 = (rest > 0) ? x.getEST() + (long) Math.ceil((double) rest / (double) c) : Long.MIN_VALUE;
					R[i][yidx] = Math.max(Math.max(q1, q2), q3);
				}
			}
		}

	}

	@Override
	public boolean calcEF_start() throws ContradictionException {
		final int nbTask = rsc.getNbTasks();
		final int[] newSdates = new int[nbTask];
		long[] E = new long[nbTask];
		for (IRTask tv : tasksLX) {
			newSdates[tv.getTaskIndex()] = tv.getTaskVar().getEST();
		}

		for (IRTask cry : tasksLY) {
			final TaskVar y = cry.getTaskVar();
			long ETot = 0;
			final ListIterator<IRTask> iter = tasksLX.listIterator(tasksLX.size());
			while (iter.hasPrevious()) {
				final IRTask x = iter.previous();
				if (x.getTaskVar().getLCT() <= y.getLCT()) {
					ETot += x.getMinConsumption();
				}
				E[x.getTaskIndex()] = ETot;
			}
			long CEF = Long.MIN_VALUE;
			for (IRTask crx : tasksLX) {
				final TaskVar x = crx.getTaskVar();
				final int j = crx.getTaskIndex();
				final long ex = crx.getMinConsumption();
				CEF = Math.max(CEF, E[j] - rsc.getMaxCapacity() * (y.getLCT() - x.getEST()));
				if (CEF + ex > 0 && x.getLCT() > y.getLCT()) {
					if (!taskheights[j].dyncomputation) { //lazy edge finding
						calcR_start(taskheights[j]);
						taskheights[j].dyncomputation = true;
					}
					newSdates[j] = (int) Math.max(newSdates[j], R[taskheights[j].idx][cry.getTaskIndex()]);
				}
			}
		}
		boolean modif = false;
		//pruning phase
		for (IRTask x : tasksLX) {
			final int i = x.getTaskIndex();
			//			if (LOGGER.isLoggable(Level.FINE) && newSdates[i] > x.getTaskVar().getEST()) {
			//				LOGGER.fine("edge finding update lb of " + x.getTaskVar().getEST() + " to " + newSdates[i]);
			//			}
			modif |= x.updateEST(newSdates[i]);
		}
		return modif;
	}

	@Override
	public boolean vilimStartEF() throws ContradictionException {
		throw new SolverException("Vilim version of edge finding for starting dates remain to be done");
	}

	//*************************************************************//
	//********* Edge finding for updating latestend ***************//
	//********* - version O(n^2k) without theta-lambda-tree *******//
	//********* - version O(n^2k) with    theta-lambda-tree *******//
	//*************************************************************//

	/**
	 * precomputation for the edge finding using dynamic programming
	 * this method assumes that the task intervals have not failed !
	 */
	protected void calcR_end(final Consumption cons) {
		final int nbTask = rsc.getNbTasks();
		final int c = cons.h;
		final int i = cons.idx;
		final long[] E = new long[nbTask];
		for (int j = 0; j < nbTask; j++) {
			E[j] = 0;
			R[i][j] = Long.MAX_VALUE;
		}
		final ListIterator<IRTask> iter = tasksLX.listIterator(tasksLX.size());
		while (iter.hasPrevious()) {
			final IRTask crx = iter.previous();
			final TaskVar x = crx.getTaskVar();
			final long ex = crx.getMinConsumption();
			for (int k = 0; k < nbTask; k++) {
				final IRTask cry = tasksLY.get(k);
				final TaskVar y = cry.getTaskVar();
				final int yidx = cry.getTaskIndex();
				if (x.getEST() >= y.getEST()) {
					E[yidx] += ex;
					final long rest = E[yidx] - (rsc.getMaxCapacity() - c) * (x.getLCT() - y.getEST());
					final long q1 = R[i][yidx];
					final long q2 = (k == 0) ? Long.MAX_VALUE : R[i][tasksLY.get(k - 1).getTaskIndex()];
					final long q3 = (rest > 0) ? x.getLCT() - (long) Math.ceil((double) rest / (double) c) : Long.MAX_VALUE;
					R[i][yidx] = Math.min(Math.min(q1, q2), q3);
				}
			}
		}
	}



	@Override
	public boolean calcEF_end() throws ContradictionException {
		final int nbTask = rsc.getNbTasks();
		final int[] newEdates = new int[nbTask];
		final long[] E = new long[nbTask];
		for (IRTask tv : tasksLX) {
			newEdates[tv.getTaskIndex()] = tv.getTaskVar().getLCT();
		}
		for (IRTask cry : tasksLY) {
			final TaskVar y = cry.getTaskVar();
			long ETot = 0;
			final ListIterator<IRTask> iter = tasksLX.listIterator(tasksLX.size());
			while (iter.hasPrevious()) {
				final IRTask crx = iter.previous();
				if (crx.getTaskVar().getEST() >= y.getEST()) {
					ETot += crx.getMinConsumption();
				}
				E[crx.getTaskIndex()] = ETot;
			}
			long CEF = Long.MIN_VALUE;
			for (IRTask crx : tasksLX) {
				final TaskVar x = crx.getTaskVar();
				final int j = crx.getTaskIndex();
				final long ex = crx.getMinConsumption();
				CEF = Math.max(CEF, E[j] - rsc.getMaxCapacity() * (x.getLCT() - y.getEST()));
				if (CEF + ex > 0 && x.getEST() < y.getEST()) {
					if (!taskheights[j].dyncomputation) { //lazy edge finding
						calcR_end(taskheights[j]);
						taskheights[j].dyncomputation = true;
					}
					newEdates[j] = (int) Math.min(newEdates[j], R[taskheights[j].idx][cry.getTaskIndex()]);
				}
			}
		}
		boolean modif = false;
		//pruning phase
		for (IRTask crx : tasksLX) {
			//final TaskVar x = crx.getTaskVar();
			final int i = crx.getTaskIndex();
			//			if (LOGGER.isLoggable(Level.FINE) && newEdates[i] < x.getLCT()) {
			//				LOGGER.fine("edge finding update ub of " + x.end() + " to " + newEdates[i]);
			//			}
			modif |= crx.updateLCT(newEdates[i]);
		}
		return modif;
	}



	@Override
	public boolean vilimEndEF() throws ContradictionException {
		throw new UnsupportedOperationException("Vilim version of edge finding for the ending dates remains to be done");
	}









}
