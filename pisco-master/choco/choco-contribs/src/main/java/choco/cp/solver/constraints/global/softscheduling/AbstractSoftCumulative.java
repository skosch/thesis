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

package choco.cp.solver.constraints.global.softscheduling;

/**
 * Created by IntelliJ IDEA.
 * User: thierry
 * Date: 4 nov. 2009
 * Time: 18:20:30
 *
 * Abstract class for sweep-based soft versions of Cumulative
 * Factorizes the control of events in the profile
 *
 */

import choco.kernel.common.util.objects.IntList;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.Comparator;

public abstract class AbstractSoftCumulative extends AbstractLargeIntSConstraint {

    public static boolean taskInter = true;


    protected int nbTask;
    protected int capaMax;

    // --------------
	// List of events
	// --------------

    protected ArrayList<Event> events;
    protected Comparator evtComp;

    // ---------------------------------
	// Sweep line status data structures
	// ---------------------------------

    protected int sum_height;
    protected IntList taskToPrune;

    private static IntDomainVar[] createAllVarsArray(IntDomainVar[] starts,
                                                     IntDomainVar[] ends,
                                                     IntDomainVar[] duration,
                                                     IntDomainVar[] heights) {
      int n = starts.length;
      IntDomainVar[] ret = new IntDomainVar[4*n];
      for (int i = 0; i < n; i++) {
          ret[4 * i] = starts[i];
          ret[4 * i + 1] = ends[i];
          ret[4 * i + 2] = duration[i];
          ret[4 * i + 3] = heights[i];
      }
      return ret;
    }

    public AbstractSoftCumulative(IntDomainVar[] starts, IntDomainVar[] ends, IntDomainVar[] duration, IntDomainVar[] heights, int Capa) {
        super(createAllVarsArray(starts, ends, duration,heights));
        //heights = h;
        nbTask = starts.length;
        Xtasks = new ArrayList<Integer>();
        Ytasks = new ArrayList<Integer>();

        for (int i = 0; i < nbTask; i++) {
            Xtasks.add(i);
            Ytasks.add(i);
        }
        taskToPrune = new IntList(nbTask);
        taskToPrune.reInit();
        events = new ArrayList<Event>();
        evtComp = new EventComparator();
        stComp = new StartingDateComparator();
        endComp = new EndingDateComparator();
        contributions = new int[nbTask];
        this.capaMax = Capa;
    }

    public IntDomainVar getStart(int i) {
        return vars[i*4];
    }

    public IntDomainVar getEnd(int i) {
        return vars[i*4 + 1];
    }

    public IntDomainVar getDuration(int i) {
        return vars[i*4 + 2];
    }

    public IntDomainVar getHeight(int i) {
        return vars[i*4 + 3];
    }

    public boolean isScheduled(int i) {
        return getStart(i).isInstantiated() && getEnd(i).isInstantiated() && getDuration(i).isInstantiated() && getHeight(i).isInstantiated();
    }

    // -------------------
	// Earliest start of i
	// -------------------

    public int getES(int i) {
        return getStart(i).getInf();
      }

    // -----------------
	// Latest start of i
	// -----------------

    public int getLS(int i) {
       return getStart(i).getSup();
    }

    // -----------------
	// Earliest end of i
	// -----------------

    public int getEE(int i) {
      return getEnd(i).getInf();
    }

    // ---------------
	// Latest end of i
	// ---------------

    public int getLE(int i) {
       return getEnd(i).getSup();
    }


    public boolean generateEvents() {
        events.clear();
        boolean someprof = false;
        for (int i = 0; i < nbTask; i++) {
            if (getStart(i).getSup() < getEnd(i).getInf()) { // t has a compulsory part
                final int h = getHeight(i).getInf();
                events.add(new Event(Event.CHECKPROF, i, getStart(i).getSup(), h)); // for variable heights : min(h)
                events.add(new Event(Event.CHECKPROF, i, getEnd(i).getInf(), -h));  // for variable heights : min(h)
                someprof=true;
            }
            if (!isScheduled(i)) {
                events.add(new Event(Event.PRUNING, i, getStart(i).getInf(), 0));
            }
        }
        return someprof;
    }

    protected int[] contributions;
    protected boolean fixPoint;

    public void initMainIteration() {
        fixPoint = false;
        taskToPrune.reInit();
    }

    public void updateCompulsoryPart() throws ContradictionException {
        for (int i = 0; i < nbTask; i++) {
            fixPoint = true;
            while (fixPoint) {
                fixPoint = false;
                IntDomainVar s =  getStart(i);
                IntDomainVar e =  getEnd(i);
                IntDomainVar d =  getDuration(i);
                fixPoint |= s.updateInf(e.getInf() - d.getSup(), this, false);
                fixPoint |= s.updateSup(e.getSup() - d.getInf(), this, false);
                fixPoint |= e.updateInf(s.getInf() + d.getInf(), this, false);
                fixPoint |= e.updateSup(s.getSup() + d.getSup(), this, false);
                fixPoint |= d.updateInf(e.getInf() - s.getSup(), this, false);
                fixPoint |= d.updateSup(e.getSup() - s.getInf(), this, false);
            }
        }
    }

    protected ArrayList<Integer> Xtasks; // tasks sorted by increasing starting date
    protected Comparator stComp;

    protected ArrayList<Integer> Ytasks; // tasks sorted by increasing ending date
    protected Comparator endComp;

    protected class EventComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            int date1 = ((Event) o1).getDate();
            int date2 = ((Event) o2).getDate();

            if (date1 < date2)
                return -1;
            else if (date1 == date2) {
                return 0;
            } else
                return 1;
        }
    }

    protected class StartingDateComparator implements Comparator {

        public StartingDateComparator() {
        }

        public int compare(Object o1, Object o2) {
            int date1 = getStart((Integer) o1).getInf();
            int date2 = getStart((Integer) o2).getInf();

            if (date1 < date2)
                return -1;
            else if (date1 == date2) {
                return 0;
            } else
                return 1;
        }
    }

    protected class EndingDateComparator implements Comparator {

        public EndingDateComparator() {
        }

        public int compare(Object o1, Object o2) {
            int date1 = getEnd((Integer) o1).getSup();
            int date2 = getEnd((Integer) o2).getSup();

            if (date1 < date2)
                return -1;
            else if (date1 == date2) {
                return 0;
            } else
                return 1;
        }
    }

    protected class Event {
        public final static int CHECK = 0;
        public final static int PROFILE = 1; 
        public final static int PRUNING = 2;
        public final static int CHECKPROF = 3;
        public int type; // among CHECK, PROFILE, CHECKPROFILE and PRUNING
        public int task;
        public int date;
        public int prof_increment;
        public Event(int type, int task, int date, int pinc) {
            this.type = type;
            this.task = task;
            this.date = date;
            this.prof_increment = pinc;
        }
        public String toString() {
            String typ = "";
            switch (type) {
                case 0 :
                    typ = "CHECK  ";
                    break;
                case 1 :
                    typ = "PROFILE";
                    break;
                case 2 :
                    typ = "PRUNING";
                    break;
                case 3 :
                    typ = "CHECK-PROFILE";
                    break;
            }
            return "[" + typ + " on task " + task + " at date " + date + " with incH " + prof_increment + "]";
        }
        public int getType() {
            return type;
        }
        public int getTask() {
            return task;
        }
        public int getDate() {
            return date;
        }
        public int getProfIncrement() {
            return prof_increment;
        }
    }

}