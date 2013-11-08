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
 * Time: 14:48:09
 *
 * SoftCumulative with task interval based lower bound of the sum of costVars
 * ref: TR 09-06-Info, Mines Nantes
 *
 */


import choco.kernel.memory.trailing.EnvironmentTrailing;
import choco.kernel.memory.trailing.StoredInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Collections;


public class SoftCumulativeSum extends SoftCumulative {

	protected StoredInt profileMinSum;  // must be maintained for interval-based LB

	public SoftCumulativeSum(IntDomainVar[] starts,
                             IntDomainVar[] ends,
                             IntDomainVar[] durations,
                             IntDomainVar[] heights,
                             IntDomainVar[] costVars,
                             IntDomainVar obj,
                             int wishCapa,
                             int capa, Solver solver) {
		super(starts,ends,durations,heights,costVars,wishCapa,capa);
		initialize(costVars, obj, solver);
	}

	protected void initialize(IntDomainVar[] costVars, IntDomainVar obj, Solver solver) {
		recomputeVars(obj);
        solver.post(solver.eq(solver.sum(costVars), obj)); //  to be improved
		EnvironmentTrailing env = (EnvironmentTrailing) solver.getEnvironment();
		profileMinSum = new StoredInt(env,0);
	}

	protected void recomputeVars(IntDomainVar obj) {
		IntDomainVar[] res = new IntDomainVar[vars.length+1];
		for(int i=0; i<vars.length; i++) {
			res[i] = vars[i];
		}
		res[vars.length]=obj;
		vars = res;
		cIndices = new int[vars.length];
		if(debug) {
			for(int i=0; i<vars.length;i++)
			System.out.println(vars[i].pretty());
		}
	}

	public SoftCumulativeSum(IntDomainVar[] starts,
                             int[] durations,
                             int[] heights,
                             IntDomainVar[] costVars,
                             IntDomainVar obj,
                             int wishCapa, Solver solver) {
		super(starts,durations,heights,costVars,wishCapa, solver);
		initialize(costVars, obj, solver);
	}

	// --------------
	// Obj var getter
	// --------------

	// nbTask*4+costVarsLength == vars.length-1 only for SoftCumulativeMinimize
    protected IntDomainVar getObj() {
    	return vars[nbTask*4+costVarsLength];
    }

    // --------------------------------------------
    // update costs while maintaining profileMinSum
    // --------------------------------------------

    protected void updateCost(int low, int up) throws ContradictionException { // consider [low, up]
        for(int i=low; i<=up; i++) {
     	   if(i<costVarsLength) {
     	       if(getCostVar(i).getSup()<sum_height-wishCapa) {
     		      this.fail();
     	       }
     	       if(sum_height>wishCapa) {
     	    	  int prev = getCostVar(i).getInf();
     	    	  if(prev<sum_height-wishCapa) {
     		         fixPoint |= getCostVar(i).updateInf(sum_height-wishCapa, this, false);
     		         profileMinSum.set(profileMinSum.get()+getCostVar(i).getInf()-prev);
     	          }
     	       }
           }
       }
    }

    // -------------------------------------
    // Task interval with global lower bound
	// -------------------------------------

    public int computeIncreasing(int energy, int left, int right) {
    	int diff = wishCapa * (right-left); // if all costs are = 0;
    	for(int i=left; i<right; i++){
 		   if(i<costVarsLength) { // should never occur
 		      diff += getCostVar(i).getInf();
 		   }
    	}
    	if(diff < energy) {
    		return energy-diff;
    	}
    	return 0;
    }

    public void taskIntervals() throws ContradictionException {
        Collections.sort(Xtasks, stComp);
        Collections.sort(Ytasks, endComp);
        int maxInc = 0;
        for (int i = 0; i < nbTask; i++) {
            int D = getEnd(Ytasks.get(i)).getSup();
            int energy = 0; // int to use updateInf
            for (int j = nbTask - 1; j >= 0; j--) {
                int t = Xtasks.get(j);
                int h = getHeight(t).getInf();
                int minDur = getDuration(t).getInf();
                int e = minDur * h; // int to use updateInf
                if (getLE(t) > D) e = Math.min(e, (D - getLS(t)) * h);
                if (e > 0) {
                    energy += e;
                    //System.out.println(energy);
                    long capaMaxDiff = capaMaxDiff(getES(t),D);
                    if (capaMaxDiff < energy) {
                    	this.fail();
                    } else {
                    	int inc = computeIncreasing(energy,getES(t),D);
                    	if(inc>maxInc) {
                    		maxInc = inc;
                    	}
                    }
                }
            }
        }
        IntDomainVar obj = getObj();
        if(debug) {
            System.out.println(maxInc + ", obj = [" + obj.getInf() + ", " + obj.getSup() + "]");
        }
        if(maxInc>0) {
        	if(debug) {
                System.out.println(profileMinSum.get() + "-" + "obj inf = " + obj.getInf() + "- increasing = "+maxInc);
            }
        	if(profileMinSum.get()+maxInc>obj.getSup()) {
        		this.fail();
            } else {
                obj.updateInf(profileMinSum.get()+maxInc, this, false);
            }
        }
    }
}
