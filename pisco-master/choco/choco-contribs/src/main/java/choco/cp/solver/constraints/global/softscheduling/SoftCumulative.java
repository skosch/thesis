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
 * Date: 5 nov. 2009
 * Time: 10:46:30
 *
 * A softCumulative with over-loads variables at each point of time
 * Sweep and task interval procedures are independent from discretization of time
 * 
 */

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Collections;
import java.util.Iterator;

public class SoftCumulative extends AbstractSoftCumulative {

	protected int wishCapa;
	protected int allScheduled;
	protected int costVarsLength;
    
    public static boolean debug = false;

	public SoftCumulative(IntDomainVar[] starts,
		                  IntDomainVar[] ends,
		                  IntDomainVar[] durations,
		                  IntDomainVar[] heights,
		                  IntDomainVar[] costVars,
		                  int wishCapa,
		                  int capa) {
		super(starts,ends,durations,heights,capa);
		this.costVarsLength = costVars.length;
		this.wishCapa = wishCapa;
		consistentData(ends);
		recomputeVars(costVars);
	}

	// no end of task can be outside costVars indexes
	protected void consistentData(IntDomainVar[] ends) {
		for(int i=0; i<ends.length; i++) {
			if(ends[i].getSup()>costVarsLength) { // intervals are of the form [s;e[
				throw new Error("SoftCumulative : possible end of task outside costVars");
			}
		}
	}

	// extend vars array with costVars
	protected void recomputeVars(IntDomainVar[] costVars) {
		IntDomainVar[] res = new IntDomainVar[vars.length+costVars.length];
		for(int i=0; i<vars.length; i++) {
			res[i] = vars[i];
		}
		for(int i=vars.length; i<vars.length+costVars.length; i++) {
			res[i] = costVars[i-vars.length];
		}
		vars = res;
		cIndices = new int[vars.length];
		if(debug) {
			for(int i=0; i<vars.length;i++)
			System.out.println(vars[i].pretty());
		}
	}

	// ----------------------------------------------------------------
	// Constructor with fixed durations and heights
	// maximum capacity is deduced from greater upper bound in costVars
	// ----------------------------------------------------------------

	public SoftCumulative(IntDomainVar[] starts,
                          int[] durations,
                          int[] heights,
                          IntDomainVar[] costVars,
                          int wishCapa, Solver solver) {
		this(starts,
			 createEndVarArray(starts, durations, solver),
			 createIntVarArray("duration",durations,solver),
			 createIntVarArray("heights",heights,solver),
			 costVars,
			 wishCapa,
			 computeCapa(costVars, wishCapa));
	}

    // ----------------------------------------------------------------
	// Constructor with fixed durations and heights
	// maximum capacity is deduced from greater upper bound in costVars
	// ----------------------------------------------------------------

	// create end variables from start variables and fixed durations
	protected static IntDomainVar[] createEndVarArray(IntDomainVar[] starts, int[] durations, Solver solver) {
        if(durations.length!=starts.length) {
			throw new Error("SofCumulative : duration.length != starts.length");
		} else {
			IntDomainVar[] res = new IntDomainVar[starts.length];
			for(int i=0; i<starts.length; i++) {
				res[i] = solver.createBoundIntVar("end: "+i, starts[i].getInf()+durations[i], starts[i].getSup()+durations[i]);
			}
			return res;
		}
	}

	// Create singleton domain integer variables from an integer array
	protected static IntDomainVar[] createIntVarArray(String name, int[] source, Solver pb) {
		IntDomainVar[] res = new IntDomainVar[source.length];
		for(int i=0; i<res.length; i++) {
			res[i] = pb.createBoundIntVar(name+": "+i, source[i], source[i]);
		}
		return res;
	}

	// compute the maximum initial capacity (never used except initialization)
	protected static int computeCapa(IntDomainVar[] costVars, int wishCapa) {
		int res = 0;
		for(int i=0; i<costVars.length; i++) {
			if(costVars[i].getSup()>res) {
				res = costVars[i].getSup();
			}
		}
		res += wishCapa;
		return res;
	}

	// ---------------
	// costVar getters
	// ---------------

	// nbTask*4+costVarsLength == vars.length only for SoftCumulative
	protected boolean isCostVar(int varIndex) {
		return varIndex>=nbTask*4 && varIndex<nbTask*4+costVarsLength;
	}

	// costVar at time point t
	protected IntDomainVar getCostVar(int t) {
		return vars[nbTask*4+t];
	}

	// ---------------------------------
	// AbsractLargeIntConstraint methods
	// ---------------------------------

     public void propagate() throws ContradictionException {
        filter();
    }
	public boolean isSatisfied() {
        // Todo
	    throw new Error("isSatisfied not yet implemented on softCumulative");
	}

	public Boolean isEntailed() {
	    throw new Error("isEntailed not yet implemented on softCumulative");
	}

	// -----------------
	// Events generation
	// -----------------

	public boolean generateEvents() {
        events.clear();
        allScheduled = 0;
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
            } else {
            	allScheduled++;
            }
        }
        return someprof;
    }

	protected boolean allIsScheduled() {
		return allScheduled == nbTask;
	}

	// ---------------
	// Sweep Filtering
	// ---------------

	// check if the current upper bound of costVar is over-loaded at point eventDate
	protected boolean violateMaxCapa(int eventDate) {
		  if(eventDate>=costVarsLength) {
			  return false;
		  }
		  return sum_height-wishCapa>getCostVar(eventDate).getSup();
	 }

	// check if the current upper bound of costVar is over-loaded in [evtS, evtF[
	protected boolean violateMaxCapa(int evtS, int evtF) {
		for(int i=evtS; i<evtF; i++) {
			if(violateMaxCapa(i)) {
				return true;
			}
		}
		return false;
	}

	// return the entailed cost of sum_heigth
	 protected int violateWishCapa() {
		  int res = sum_height-wishCapa;
		  if(res<0) {
			  res = 0;
		  }
		  return res;
	  }

	 // verify consistence of assigned costs (used when all tasks are fixed)
	 protected void checkCosts(int low, int up) throws ContradictionException {
		int value = 0;
		 if(sum_height>wishCapa) {
			 value = sum_height-wishCapa;
		 }
		 for(int i=low; i<=up; i++) {
			 if(i<costVarsLength) { // last end can be outside
				 if(getCostVar(i).isInstantiated() && getCostVar(i).getVal()!=value) {
					 this.fail();
				 } else {
					 if(getCostVar(i).getInf()>value || getCostVar(i).getSup()<value) {
						 this.fail();
					 } else {
						 getCostVar(i).setVal(value);
						 if(debug) {
							 System.out.println(getCostVar(i).pretty());
						 }
					 }
				 }
			 }
		 }
	 }

	 public void filter() throws ContradictionException {
		if (debug) System.out.println("Compulsry parts");
        updateCompulsoryPart();
        fixPoint = true;
        while (fixPoint) {  // apply the sweep process until saturation
            initMainIteration();
            if (debug) System.out.println("Start sweep");
            sweep();
        }
        if (taskInter) {
            if (debug) System.out.println("Task interval");
            taskIntervals();
            if (debug) System.out.println("Filtering OK");
        }

     }

     // -------------------------------------------------------
	 // Sweep algorithm redefined
	 // CHECKPROF profile events : start/end of mandatory parts
	 // PRUNING events : start of tasks
     // -------------------------------------------------------

	 public void sweep() throws ContradictionException {
	        if (generateEvents()) {
	        	boolean finalState = allIsScheduled();
	            Collections.sort(events, evtComp);
	            for (int i = 0; i < contributions.length; i++) {
	                contributions[i] = 0;
	            }
	            sum_height = 0;
	            int d = events.get(0).getDate();
	            Iterator it = events.iterator();
	            while (it.hasNext()) {
	                Event evt = (Event) it.next();
	                if (debug) {
	                	System.out.println("" + evt + " s:" + sum_height);
	                }
	                if (evt.type != Event.PRUNING) { // profile event
	                    if (d != evt.date) {
	                        if (violateMaxCapa(d,evt.date-1)) {
	                            this.fail();
	                        }
	                        if(finalState) {
	                        	checkCosts(d,evt.date-1);
	                        }
	                        prune(d, evt.date - 1);
	                        updateCost(d, evt.date-1); // consider [low, up]
	                        d = evt.date;
	                    }
	                    if (evt.type == Event.CHECKPROF) {
	                        sum_height += evt.prof_increment;
	                        contributions[evt.task] += evt.prof_increment;
	                    } else {
	                        throw new Error("" + evt.type + " should not be used");
	                    }
	                } else { // pruning event
	                    taskToPrune.add(evt.task);
	                }
	            }
	            if(violateMaxCapa(d)) {
	                this.fail();
	            }
	            prune(d, d);
	            updateCost(d,d);
	        }
	    }

	   // --------------------------------------------------------------------------
	   // Pruning according to maximum of wishCapa+costVar[i].getSup() in this range
       // Update inf of costVars[i] from the profile
       // --------------------------------------------------------------------------

       // maximum allowed
	   public int maxCapaMax(int low, int up) {
		   int res = wishCapa;
		   for(int i=low; i<=up; i++) {
			   if(i<costVarsLength) {
				   if(getCostVar(i).getSup()>res-wishCapa) {
					   res = wishCapa+getCostVar(i).getSup();
				   }
			   }
		   }
		   return res;
	   }

       protected boolean overlaps(int height, int indexVar, int low, int up) { // consider [low, up]
    	   return (sum_height - contributions[indexVar] + height > maxCapaMax(low, up));
       }

       public void prune(int low, int up) throws ContradictionException { // consider [low, up]
           DisposableIntIterator it = taskToPrune.iterator();
           while (it.hasNext()) {
               int idx = it.next();
               IntDomainVar s =  getStart(idx);
               IntDomainVar e =  getEnd(idx);
               IntDomainVar d =  getDuration(idx);
               IntDomainVar h =  getHeight(idx);
               int height = h.getInf();
               if(overlaps(height,idx,low,up)) {
                   if (debug) { System.out.println("Start pruning with task " + idx + " between [" + low + "," + up + "]"); }
                   if (debug) { System.out.println("s:" + sum_height + " c:" + contributions[idx] + " h:" + height + " CAPA:" + capaMax); }
                   fixPoint |= s.removeInterval(low - d.getInf() + 1, up, this, false);
                   fixPoint |= e.removeInterval(low + 1, up + d.getInf(), this, false);
                   int maxd = Math.max(Math.max(low - s.getInf(), 0), e.getSup() - up - 1);
                   fixPoint |= d.updateSup(maxd, this, false);
                   if (debug) { System.out.println("En pruning of " + idx); }
                   if (e.getSup() <= up + 1) it.remove();
               }
               if (e.getInf()>low && s.getSup() <= up && d.getInf() > 0) {
                   fixPoint |= h.updateSup(maxCapaMax(low, up) - (sum_height - contributions[idx]), this, false);
               }
           }
       }

       protected void updateCost(int low, int up) throws ContradictionException { // consider [low, up]
           for(int i=low; i<=up; i++) {
        	   if(i<costVarsLength) {
        	       if(getCostVar(i).getSup()<sum_height-wishCapa) {
        		      this.fail();
        	       }
        	       if(sum_height>wishCapa) {
        		      fixPoint |= getCostVar(i).updateInf(sum_height-wishCapa, this, false);
        	       }
        	   }
           }
       }

       // -------------
       // Task interval
	   // Todo : use task interval for pruning starts
       // -------------

       protected long capaMaxDiff(int left, int right) { // surface : consider [left, right[ !!!
    	   long res = 0;
    	   for(int i=left; i<right; i++){
    		   if(i<costVarsLength) { // should never occur
    		      res += wishCapa +  getCostVar(i).getSup();
    		   }
    	   }
    	   return res;
       }
       public void taskIntervals() throws ContradictionException {
           Collections.sort(Xtasks, stComp);
           Collections.sort(Ytasks, endComp);
           for (int i = 0; i < nbTask; i++) {
               int D = getEnd(Ytasks.get(i)).getSup();
               long energy = 0;
               for (int j = nbTask - 1; j >= 0; j--) {
                   int t = Xtasks.get(j);
                   int h = getHeight(t).getInf();
                   int minDur = getDuration(t).getInf();
                   long e = minDur * h;
                   if (getLE(t) > D) e = Math.min(e, (D - getLS(t)) * h);
                   if (e > 0) {
                       energy += e;
                       long capaMaxDiff = capaMaxDiff(getES(t),D);
                       if (capaMaxDiff < energy) this.fail();
                   }
               }
           }
       }

}
