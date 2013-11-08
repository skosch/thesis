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

package choco.cp.solver.search.task.profile;

import gnu.trove.TIntIntHashMap;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.global.scheduling.IResource;
import choco.kernel.solver.variables.scheduling.ITask;



/**
 * Probabilistic profile of an unary resource.
 * @author Arnaud Malapert
 *
 */
public class ProbabilisticProfile  {

	private final TIntIntHashMap indexMap = new TIntIntHashMap();

	private final List<EventRProf> function = new LinkedList<EventRProf>();

	private final EventDataStructure[] structList;

	private double slope;

	private double gap;

	private int coordinate;

	protected final BitSet involved = new BitSet();

	private final MaximumDataStruct max = new MaximumDataStruct();

	public final DisjunctiveSModel disjSModel;

	/**
	 *
	 */
	public ProbabilisticProfile(ITask[] tasks, DisjunctiveSModel disjSModel) {
		this(Arrays.asList(tasks), disjSModel);
	}

	public ProbabilisticProfile(List<? extends ITask> tasks, DisjunctiveSModel disjSModel) {
		super();
		this.disjSModel = disjSModel;
		structList=new EventDataStructure[tasks.size()];
		for (int i = 0; i < structList.length; i++) {
			structList[i] = new EventDataStructure(tasks.get(i));
			indexMap.put(tasks.get(i).getID(), i);
		}
	}


	public ProbabilisticProfile(Solver solver, DisjunctiveSModel disjSModel) {
		super();
		this.disjSModel = disjSModel;
		structList=new EventDataStructure[solver.getNbTaskVars()];
		for (int i = 0; i < structList.length; i++) {
			structList[i] = new EventDataStructure(solver.getTaskVar(i));
			indexMap.put(solver.getTaskVar(i).getID(), i);
		}
	}




	public final double getIndividualContribution(final ITask task,final int coordinate) {
		return getEDS(task).getIndividualContribution(coordinate);
	}

	protected EventDataStructure getEDS(final ITask task) {
		return structList[indexMap.get(task.getID())];
	}

	public final void generateEventsList(IResource<? extends ITask> rsc) {
		function.clear();
		Iterator<? extends ITask> iter = rsc.getTaskIterator();
		while(iter.hasNext()) {
			function.addAll(Arrays.asList(getEDS(iter.next()).events));
		}
		Collections.sort(function);
	}

	protected void resetSweepData() {
		slope=gap= 0;
		coordinate=Integer.MIN_VALUE;
		involved.clear();
	}


	protected void handleEvents(final EventRProf e,final ListIterator<EventRProf> iter) {
		handleEvent(e);
		EventRProf next;
		while(iter.hasNext()) {
			next=iter.next();
			if(next.coordinate>e.coordinate) {
				iter.previous();
				break;
			}else {
				handleEvent(next);
			}

		}

	}


	protected void handleEvent(final EventRProf e) {
		slope+=e.slope;
		gap+=e.gap;
		switch (e.type) {
		case START: involved.set(e.task.getID());break;
		case END: involved.clear(e.task.getID());break;
		}
	}



	public void initializeEvents() {
		for (EventDataStructure eds : structList) {
			eds.reset();
		}
	}

	protected final void sweep() {
		final ListIterator<EventRProf> iter=function.listIterator();
		while(iter.hasNext()) {
			final EventRProf e=iter.next();
			update(e.coordinate);
			handleEvents(e,iter);
			if(gap > max.value && isValidMaximum() ) {
				max.value=gap;
				max.coordinate=e.coordinate;
				// FIXME - swap bitset instead od copying ! - created 12 août 2011 by Arnaud Malapert
				max.involved.clear();
				max.involved.or(involved);
			} 
		}
	}
	/**
	 * compute a maximum using the specified set checker
	 */
	public final void computeMaximum(IResource<?>... resources) {
		//reset Events and max data struc
		max.reset();
		//lazy computation of the maximum over all resource
		for (IResource<?> rsc : resources) {
			generateEventsList(rsc);
			resetSweepData();
			sweep();
		}
	}


	protected boolean isValidMaximum() {
		for (int i = involved.nextSetBit(0); i >= 0; i = involved.nextSetBit(i + 1)) {
			for (int j = involved.nextSetBit(i+1); j >= 0; j = involved.nextSetBit(j + 1)) {
				if(disjSModel.containsEdge(i, j) &&  ! disjSModel.getConstraint(i, j).isFixed() ) {
					return true;
				}
			}
		}
		return false;
	}

	public double getMaxProfileValue() {
		return max.value;
	}

	public int getMaxProfileCoord() {
		return max.coordinate;
	}

	public BitSet getInvolvedInMaxProf() {
		return max.involved;
	}
	
	public double compute(final int x) {
		this.resetSweepData();
		final ListIterator<EventRProf> iter=function.listIterator();
		while(iter.hasNext()) {
			final EventRProf e=iter.next();
			if(e.coordinate<=x) {
				update(e.coordinate);
				handleEvent(e);
			} else {
				break;
			}
		}
		update(x);
		return gap;
	}

	private double shift(final int x) {
		return (x-coordinate)*slope;
	}

	protected void update(final int x) {
		gap+=shift(x);
		coordinate=x;
	}
	private void drawPoint(final StringBuilder buffer) {
		buffer.append(coordinate).append(' ').append(gap).append('\n');
	}

	public StringBuilder draw() {
		this.resetSweepData();
		final StringBuilder buffer=new StringBuilder();
		final ListIterator<EventRProf> iter=function.listIterator();
		while(iter.hasNext()) {
			final EventRProf e=iter.next();
			update(e.coordinate);
			drawPoint(buffer);
			handleEvents(e,iter);
			drawPoint(buffer);

		}
		return buffer;
	}




	protected static class MaximumDataStruct {

		public int coordinate;

		public double value;

		public final BitSet involved = new BitSet();


		public void reset() {
			coordinate=Integer.MIN_VALUE;
			value=Double.MIN_VALUE;
			involved.clear();
		}

	}


	static enum EventType {START, MID, END}

	/**
	 * Event for the resource probabilistic profile
	 * @author Arnaud Malapert : arnaud(dot)malapert(at)emn(dot)fr
	 *
	 */
	static class EventRProf implements Comparable<EventRProf>{

		public final EventType type;

		public final ITask task;

		public int coordinate;

		public double slope;

		public double gap;

		public EventRProf(final EventType type) {
			this(type, null);
		}

		public EventRProf(final EventType type,final ITask task) {
			this(type, 0, 0, 0,task);
		}

		/**
		 * @param type
		 * @param coordinates
		 * @param slope
		 * @param gap
		 */
		public EventRProf(final EventType type, final int coordinates, final double slope, final double gap,final ITask task) {
			super();
			this.type = type;
			this.coordinate = coordinates;
			this.slope = slope;
			this.gap = gap;
			this.task=task;
		}



		/**
		 * @return the coordinate
		 */
		public final int getCoordinates() {
			return coordinate;
		}


		/**
		 * @param coordinates the coordinate to set
		 */
		public final void setCoordinates(final int coordinates) {
			this.coordinate = coordinates;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			final StringBuilder buffer=new StringBuilder();
			buffer.append(type);
			buffer.append(coordinate).append("(");
			buffer.append(slope).append(',').append(gap).append(')');
			return buffer.toString();
		}


		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(final EventRProf o) {
			final int x1=coordinate;
			final int x2=o.getCoordinates();
			if(x1<x2) {return -1;}
			else if(x1>x2) {return 1;}
			else {
				return 0;
			}
		}
	}


	protected static class EventDataStructure  {

		public final static int EST=0, LST=1, ECT=2, LCT=3;

		protected final ITask task;

		protected EventRProf[] events;

		/**
		 * @param task
		 */
		public EventDataStructure(final ITask task) {
			super();
			this.task = task;
			events= (EventRProf[]) Array.newInstance(EventRProf.class, 4);
			events[EST]=new EventRProf(EventType.START,task);
			events[LST]=new EventRProf(EventType.MID);
			events[ECT]=new EventRProf(EventType.MID);
			events[LCT]=new EventRProf(EventType.END,task);
		}


		public double getIndividualContribution(final int x) {
			double contrib=0;
			if(x>=events[EST].coordinate && x<events[LCT].coordinate) {
				contrib+=events[EST].gap;
				if(! task.isScheduled()) {
					contrib+= (events[EST].slope)*(Math.min(x,events[LST].coordinate)-events[EST].coordinate);
					if(x>=events[ECT].coordinate) {
						contrib+= (events[ECT].slope)*(x-events[ECT].coordinate);
					}
				}
			}
			return contrib;
		}


		private void set(final int idx,final int x,final double slope,final double gap) {
			events[idx].coordinate=x ;
			events[idx].gap=gap;
			events[idx].slope=slope;
		}

		public void reset() {
			final int dur = task.getMinDuration();
			assert(dur>0);
			final int est = task.getEST();
			// TODO - Simplify computation and event management - created 12 août 2011 by Arnaud Malapert
//			if(task.isScheduled()) {
//				set(EST, est, 0, 1);
//				set(LCT, task.getLCT(), 0,-1);
//			} else {
				final int lst = task.getLST();
				final double std=lst - est +1;
				final double gap=1/std;
				//double slope=1/std;
				final double slope= std<= dur ? 1/std : (dur-1)/(std*dur);
				set(EST, est, slope, gap);
				set(LST, lst, -slope,0);
				set(ECT, task.getECT(), -slope,0);
				set(LCT, task.getLCT(), slope, -gap);
//			}
		}
	}






}

