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

import choco.kernel.common.util.tools.TaskUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.TaskVar;

import java.util.*;


/**
 * @author Arnaud Malapert</br> 
 * @since 3 mars 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public class CumulSweep implements ICumulSweep {

	public final AbstractCumulativeSConstraint rsc;

	protected final List<IRTask> tasks;

	private final List<IRTask> taskToPrune = new LinkedList<IRTask>();
	/**
	 * first data structure of the sweep algorithm
	 * event point series : list of Event
	 */
	private final LinkedList<Event> events = new LinkedList<Event>();
	/**
	 * sorting event regarding their starting date
	 */
	private final Comparator<Event> evtComp=new EventComparator();

	/**
	 * temporary data structure for the sweep
	 */
	private final int[] capaContributions;

	/**
	 * temporary data structure representing the current height
	 * of the profile when sweeping
	 */
	private int capaSumHeight;

	/**
	 * temporary data structure for the sweep
	 */
	private final int[] consContributions;

	/**
	 * temporary data structure representing the current height
	 * of the profile when sweeping
	 */
	private int consSumHeight;

	/**
	 * temporary counter which store the number of tasks in the set CHECK
	 */
	int nbTasks = 0;

	private final EventTaskStructure[] task_evts;



	private boolean noFixPoint;

	public CumulSweep(final AbstractCumulativeSConstraint rsc, final List<IRTask> tasks) {
		super();
		this.rsc = rsc;
		this.tasks = tasks;
		this.capaContributions = new int[rsc.getNbTasks()];
		this.consContributions = new int[rsc.getNbTasks()];
		task_evts = new EventTaskStructure[rsc.getNbTasks()];
		for (int i = 0; i < rsc.getNbTasks(); i++) {
			task_evts[i] = new EventTaskStructure( rsc.getRTask(i));
		}

	}

	private boolean overlapForSure(TaskVar t, int low, int up) {
		return t .getECT() > low && t.getLST() <= up && t.getMinDuration() > 0;
	}


	public boolean generateEvents() {
		events.clear();
		boolean someprof = false;
		//we assume that the lists contains only optional or regular task
		for (IRTask rtask: tasks) {
			final int i = rtask.getTaskIndex();
			final TaskVar task = rtask.getTaskVar();
			if(rtask.isRegular() && TaskUtils.hasCompulsoryPart(task)) {
				//Check events
				if( rtask.getMaxHeight() < Math.max(0, rsc.getMaxConsumption())) {
					task_evts[i].setCheckEvts(events, task);
				}
				//Compulsory part events
				final int capaInc = Math.max(0, rtask.getMinHeight());
				final int consInc = Math.min(0, rtask.getMaxHeight());
				if( capaInc != 0 || consInc!= 0) {
					task_evts[i].setCompProfEvts(events, task, capaInc, consInc);
					someprof = true;
				}
			}
			if( ! rtask.isEliminated()) {
				//Domain Events
				final int capaInc = Math.min(0, rtask.getMinHeight());
				final int consInc = Math.max(0, rtask.getMaxHeight());
				if( capaInc != 0 || consInc!= 0) {
					task_evts[i].setDomProfEvts(events, task, capaInc, consInc);
					someprof = true;
				}
			}
			//pruning events
			if (!task.isScheduled() || rtask.isOptional() || !rtask.getHeight().isInstantiated() ) {
				task_evts[i].setPruningEvt(events, task);
			}


		}
		return someprof;
	}

	protected final void checkConsAndCapa() throws ContradictionException {
		if (capaSumHeight > rsc.getMaxCapacity() || 
				( nbTasks > 0 && consSumHeight < rsc.getMinConsumption()) ) { //
			//capacity exceeded or minimal consumption can not be reached in a relevant interval
			rsc.fail();
		}
		//update minimal capacity and maximal consumption
		rsc.updateMinCapacity(capaSumHeight);
		if(nbTasks>0) {rsc.updateMaxConsumption(consSumHeight);}

	}

	protected final void initializeSweep() {
		taskToPrune.clear();
		Collections.sort(events, evtComp);  // sort event by date
		Arrays.fill(capaContributions, 0);
		Arrays.fill(consContributions, 0);
		nbTasks = 0;
		capaSumHeight = 0;
		consSumHeight = 0;
	}

	public boolean sweep() throws ContradictionException {
		noFixPoint = false;
		//CPSolver.flushLogs();
		if (generateEvents()) { // events are start/end of mandatory parts (CHECKPROF event) and start of tasks (PRUNING events)
			initializeSweep();
			int d = events.getFirst().getDate(); // get first date
			final ListIterator<Event> it = events.listIterator(); // about to iterate on events
			while (it.hasNext()) {
				final Event evt = it.next();  // get next event
				//----- pruning event
				if (evt.type == Event.PRUNING) {
					taskToPrune.add(evt.task); // if not a pruning event then add the new task to the list of "active" tasks (taskToPrune is decreased in the prune method ?)
				} else {				
					if (d != evt.date) { // if event of a different date it means that all profile events <= d have been taken into account
						checkConsAndCapa();
						prune(d, evt.date - 1); // caution: prune is called on non-pruning events !
						d = evt.date; // register new date
					}
					if (evt.type == Event.CHECK) { // if part of the capacity profile  
						nbTasks += evt.capaProfIncrement;
					}else if (evt.type == Event.PROFILE) { // if part of the consumption profile
						//update contributions and profile
						capaSumHeight += evt.capaProfIncrement; 
						capaContributions[evt.task.getTaskIndex()] += evt.capaProfIncrement; 
						consSumHeight += evt.consProfIncrement;
						consContributions[evt.task.getTaskIndex()] += evt.consProfIncrement; 

					} else {
						throw new IllegalArgumentException(evt.type + " should not be used");
					}
				}
			}
			checkConsAndCapa();
			prune(d, d); // <hca> hum c'est quoi ca ? c'est spécifié dans le papier (surement un effet de bord).
			//			// Insert the last pruning phase
		}
		return noFixPoint;
	}



	protected boolean pruneRequired(final IRTask rtask, final int low,final int up) throws ContradictionException {
		final int idx = rtask.getTaskIndex();
		boolean modified=false;
		if( (nbTasks >0 && consSumHeight - consContributions[idx] < rsc.getMinConsumption()) //needed to ensure minimal consumption 
				|| capaSumHeight - capaContributions[idx] > rsc.getMaxCapacity() ) { //needed to not exceed capacity
			rtask.assign();
			final TaskVar t = rtask.getTaskVar();
			modified = rtask.setEST(up - t.getMaxDuration() + 1);
			//modified |= t.start().updateInf(up - t.getMaxDuration() + 1, rsc.getCIndiceStart(idx));
			modified |= rtask.setLST(low);
			//modified |= t.start().updateSup(low, rsc.getCIndiceStart(idx));
			modified |= rtask.setLCT(low + t.getMaxDuration());
			//modified |= t.end().updateSup( low + t.getMaxDuration(), rsc.getCIndiceEnd(idx));
			modified |= rtask.setECT(up +1);
			//modified |= t.end().updateInf( up +1, rsc.getCIndiceEnd(idx));
			modified |= rtask.setMinDuration(Math.min( up - t.start().getSup() + 1, t.end().getInf()-low));
			//modified |= t.duration().updateInf(Math.min( up - t.start().getSup() + 1, t.end().getInf()-low), rsc.getCIndiceDuration(idx));
			if(modified) rtask.updateCompulsoryPart();
		}
		return modified;
	}


	protected final boolean pruneForbidden(final IRTask rtask, final int low,final int up) throws ContradictionException {
		final int idx = rtask.getTaskIndex();
		boolean modified=false;
		if (capaSumHeight - capaContributions[idx] + rtask.getMinHeight() > rsc.getMaxCapacity() //capa exceeded
				|| consSumHeight - consContributions[idx] + rtask.getMaxHeight() <rsc.getMinConsumption()) {//cons not reached
			final TaskVar t = rtask.getTaskVar();
			if(overlapForSure(t, low, up)) {
				// exclure celles qui overlap for sure
				rtask.remove();
			}else if(rtask.isRegular() && t.getMinDuration()>0) {
				// call removeInterval on start since some starting time are not possible anymore
				modified = rtask.setStartNotIn(low - t.getMinDuration() + 1, up);
				if( ! t.duration().isInstantiated()) {
					//this second removeInterval is only relevant if a duration variable exists
					modified |= rtask.setEndNotIn(low + 1, up + t.getMinDuration());
					final int maxd = Math.max(Math.max(low - t.getEST(), 0), t.getLCT() - up - 1);
					modified |= rtask.setMaxDuration(maxd);  // t is either to the left or to the right of this interval -> it has an impact on the duration !
				}
				if(modified) {rtask.updateCompulsoryPart();}
			}
		}
		return modified;
	}



	protected boolean pruneHeight(final IRTask rtask, final int low,final int up) throws ContradictionException {
		boolean modified = false;
		final TaskVar t = rtask.getTaskVar();
		if( rtask.isRegular() && overlapForSure(t, low, up)) {
			final int idx = rtask.getTaskIndex();
			modified = rtask.updateMaxHeight(rsc.getMaxCapacity()- (capaSumHeight - capaContributions[idx]));
			modified |= rtask.updateMinHeight(rsc.getMinConsumption() - (consSumHeight - consContributions[idx]));
		}
		return modified;
	}

	protected void prune(final int low,final int up) throws ContradictionException {
		final ListIterator<IRTask> it = taskToPrune.listIterator();
		while (it.hasNext()) { // prune all task that intersect with the current time
			final IRTask crt = it.next();
			// we remove contribution of task v and imagine that if overlaps some date between low and up (plateau of the current profile)
			noFixPoint |= pruneForbidden(crt, low, up);
			noFixPoint |= pruneRequired(crt, low, up);
			// prune the height of tasks that overlap for sure
			noFixPoint |= pruneHeight(crt, low, up);
			//Mise � jour de TaskToPrune: on retire les taches telles que t.end().sup() < date
			if ( crt.getTaskVar().getLCT() <= up + 1) {
				it.remove();
			}
		}
	}

	/**
	 * A Class to preallocate the events needed per tasks
	 */
	protected static class EventTaskStructure {
		//CHECK Events
		protected final Event checkEvtS;
		protected final Event checkEvtE;

		//Compulsory part profile events
		protected final Event compProfEvtS;
		protected final Event compProfEvtE;

		//domain profile events
		protected final Event domProfEvtS;
		protected final Event domProfEvtE;

		//Pruning events
		protected final Event pruneEvt;


		/**
		 * Build the event structure of task i
		 *
		 * @param t
		 */
		public EventTaskStructure(final IRTask t) {
			checkEvtS = new Event(Event.CHECK, t, 1, 1);
			checkEvtE = new Event(Event.CHECK, t, -1, -1);
			compProfEvtS = new Event(Event.PROFILE, t);
			compProfEvtE = new Event(Event.PROFILE, t);
			domProfEvtS = new Event(Event.PROFILE, t);
			domProfEvtE = new Event(Event.PROFILE, t);
			pruneEvt = new Event(Event.PRUNING, t,0,0);
		}


		public void setPruningEvt(final List<Event> events, final TaskVar t) {
			pruneEvt.date = t.getEST();
			events.add(pruneEvt);
		}
		public void setCheckEvts(final List<Event> events, final TaskVar t) {
			checkEvtS.date = t.getLST();
			checkEvtE.date = t.getECT();
			events.add(checkEvtS);
			events.add(checkEvtE);
		}

		public void setCompProfEvts(final List<Event> events, final TaskVar t, final int capaInc, final int consInc) {
			compProfEvtS.set(t.getLST(), capaInc, consInc);
			compProfEvtE.set(t.getECT(), -capaInc, -consInc);
			events.add(compProfEvtS);
			events.add(compProfEvtE);
		}

		public void setDomProfEvts(final List<Event> events, final TaskVar t, final int capaInc, final int consInc) {
			domProfEvtS.set(t.getEST(), capaInc, consInc);
			domProfEvtE.set(t.getLCT(), -capaInc, -consInc);
			events.add(domProfEvtS);
			events.add(domProfEvtE);
		}

	}


	protected final static class Event {
		public final static int CHECK = 0;  
		public final static int PROFILE = 1; 
		public final static int PRUNING = 2;

		public int type; // among CHECK, PROFILE, and PRUNING

		public IRTask task;
		public int date;
		public int capaProfIncrement;
		public int consProfIncrement;

		public Event(final int type, IRTask task, final int capaProfIncrement,
				final int consProfIncrement) {
			super();
			this.type = type;
			this.task = task;
			this.date= -1;
			this.capaProfIncrement = capaProfIncrement;
			this.consProfIncrement = consProfIncrement;
		}

		public Event(final int type, final IRTask task) {
			this(type, task, -1, -1);
		}

		public void set(final int date, final int capaInc, final int consInc) {
			this.date = date;
			this.capaProfIncrement = capaInc;
			this.consProfIncrement = consInc;
		}

		@Override
		public String toString() {
			String typ;
			switch (type) {
			case 0:
				typ = "CHECK  ";
				break;
			case 1:
				typ = "PROFILE";
				break;
			case 2:
				typ = "PRUNING";
				break;
			case 3:
				typ = "CHECK-PROFILE";
				break;
			default :
				typ="";
			}
			return "[" + typ + ", " + task + ", d=" + date + ", capaInc=" + capaProfIncrement + ", consInc="+consProfIncrement+"]";
		}

		public int getType() {
			return type;
		}

		public IRTask getTask() {
			return task;
		}

		public int getDate() {
			return date;
		}

		public final int getCapaProfIncrement() {
			return capaProfIncrement;
		}

	}
	//	***************************************************************//
	//	********* Events managment ************************************//
	//	***************************************************************//


	protected static class EventComparator implements Comparator<Event> {

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(final Event o1,final Event o2) {
			final int date1 = o1.getDate();
			final int date2 = o2.getDate();
			if (date1 < date2) {
				return -1;
			} else if (date1 == date2) {
				return 0;
			} else {
				return 1;
			}
		}
	}
}
