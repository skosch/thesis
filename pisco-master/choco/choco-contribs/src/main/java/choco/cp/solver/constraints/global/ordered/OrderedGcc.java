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

package choco.cp.solver.constraints.global.ordered;

/**
 * Created by IntelliJ IDEA.
 * User: thierry.petit(a)emn.fr
 * Date: 4 nov. 2009
 * Time: 16:44:41
 *
 * A global cardinality constraint ordered w.r.t. cardinalities
 * Usage: distribute values in a fixed set of variables (representing penalties)
 *        "cost value v+1 is at least as undesirable as cost value v"
 *
 * OrderedGcc holds iff :
 *   nb_occ(minValue) >= minBot
 *   For each i in 0..maxValue sum_nb_occ(v s.t. v>=i) <= Imax[i]
 * 
 * Could be reformulated by :
 * globalCardinalityVars(vars,cards)
 *     vith D(card[0]) = [minBot,vars.length]
 *          D(card[1]) = [0,Imax[1]]
 *          ...
 *          D(card[cards.length-1]) = [0,Imax[cards.length-1]]
 * In conjunction with a set of arithmetic constraints :
 * card[cards.length-1] + ... + card[3] + card[2] + card[1] <= Imax[1]
 * card[cards.length-1] + ... + card[3] + card[2] <= Imax[2]
 * card[cards.length-1] + ... + card[3] <= Imax[3]
 * ...
 * card[cards.length-1] + card[cards.length-2] <= Imax[cards.length-2]
 *
 * Algorithm : performs GAC in O(|vars|+|Imax|)
 *
 */


import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class OrderedGcc extends AbstractLargeIntSConstraint {

    public static boolean debug = false;
	protected int minValue;
	protected int maxValue;
	protected int[] Imax;
	protected int minBot;

    public OrderedGcc(IntDomainVar[] vars,
                      int[] Imax,
                      int minBot) {

        this(vars,computeMin(vars),computeMax(vars),Imax, minBot);
    }

	public OrderedGcc(IntDomainVar[] vars,
				      int minValue,
				      int maxValue,
					  int[] Imax,
					  int minBot) {
		super(vars);
		paramTest(vars, minValue, maxValue, Imax, minBot);
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.Imax = new int[Imax.length];
		for(int i=0; i<Imax.length; i++) {
			this.Imax[i] = Imax[i];
		}
		this.minBot = minBot;
	}

    // minimum and maximum value of vars

    private static int computeMin(IntDomainVar[] vars) {
        int minVal = vars[0].getInf();
        for(int i=1; i<vars.length; i++){
           if(vars[i].getInf()<minVal) {
               minVal = vars[i].getInf();
           }
        }
        return minVal;
    }

    private static int computeMax(IntDomainVar[] vars) {
        int maxVal = vars[0].getSup();
        for(int i=1; i<vars.length; i++){
           if(vars[i].getSup()>maxVal) {
               maxVal = vars[i].getSup();
           }
        }
        return maxVal;
    }

    // check parameters

	private static void paramTest(IntDomainVar[] vars,
			                      int minValue,
			                      int maxValue,
			                      int[] Imax,
			                      int minBot){
        if (maxValue-minValue+1!=Imax.length) {
            throw new SolverException("OrderedGcc: maxValue-MinValue+1!=Imax.length");
        }
        if(Imax[0]<vars.length) {
        	throw new SolverException("OrderedGcc: Imax[0]<vars.length");
        }
        for(int i=0; i<Imax.length-1; i++) {
        	if(Imax[i]<Imax[i+1]) {
        		throw new SolverException("OrderedGcc: Imax: bad order");
        	}
        }
    }

    // tuple minimizing values

	protected static int[] minCovering(IntDomainVar[] vars) {
		int[] res = new int[vars.length];
		for(int i=0; i<vars.length; i++) {
			res[i] = vars[i].getInf();
		}
		return res;
	}

    // number of domains with absolute minimum value

	protected static int occMin(IntDomainVar[] vars, int minValue) {
		int res = 0;
        for(int i=0; i<vars.length; i++) {
			if(vars[i].getInf()==minValue) {
				res++;
			}
		}
		return res;
	}

    // occurences of each value in a tuple

	protected static int[] occurences(int[] tuple, int minValue, int maxValue) {
		int[] res = new int[maxValue-minValue+1];
		for(int i=0; i<tuple.length; i++) {
			res[tuple[i]-minValue]++;
		}
		return res;
	}

    // for each value v the number of variables x with t[x] >= v

    protected static int[] occurencesGreater(int[] tuple, int minValue, int maxValue) {
        int[] occs = occurences(tuple,minValue,maxValue);
        for(int i=occs.length-2; i>=0; i--) {
            occs[i]+=occs[i+1];
        }
        return occs;
    }

	public boolean isSatisfied() {
		return isSatisfied(minCovering(this.vars));
	}

	public boolean isSatisfied(int[] tuple) {
		int[] occs = occurences(tuple,this.minValue,this.maxValue);
		if(occs[0]<this.minBot) {
			return false;
		}
        occs = occurencesGreater(tuple,this.minValue,this.maxValue);
        for(int i=0; i<occs.length; i++) {
           if(occs[i]>Imax[i]) {
               return false;
           }
        }
		return true;
	}

    public void awake() throws ContradictionException {
       // find the first index with Imax[index] equal to 0
       int index = -1;
       int pos=0;
       while(index==-1 && pos<this.Imax.length) {
           if(Imax[pos]==0) { // if true then pos should be > 0
             index = pos;
           }
           pos++;
       }
       // initial pruning on variables upper bound
       if(index !=-1) {
        for(int i=0; i<this.vars.length; i++) {
            this.vars[i].removeInterval(index+minValue,
								        vars[i].getSup(),
                    this, false);
        }
       }
       this.constAwake(false);
    }

	public void propagate() throws ContradictionException {
		int[] minCovering = minCovering(this.vars);
		int[] occs = occurencesGreater(minCovering,minValue,maxValue);
		if(debug) {
			System.out.print("minCovering = ");
			display(minCovering);
			System.out.print("Occurences = ");
			display(occs);
		}
		if(!isSatisfied()) {
			this.fail();
		}
		int nbVus = 0;
		if(occMin(this.vars,this.minValue)==minBot) {  // then instantiate all those values with minValue
			for(int i=0; i<this.vars.length; i++) {
				if(this.vars[i].getInf()==this.minValue) {
					this.vars[i].instantiate(this.minValue, this, false);
					nbVus++;
				}
			}
		}
		int pos = 1;  // we must not evaluate minValue
		while(pos<occs.length && nbVus<this.vars.length) {
			if(occs[pos]==this.Imax[pos]) { // remove all values v in D(x_i) s.t. minCovering(x_i)<pos+minValue
                                            // and v>pos+minValue 
				for(int i=0; i<this.vars.length; i++) {
					if(/*!vars[i].isInstantiated() &&*/ minCovering[i]<pos+this.minValue) {
						this.vars[i].removeInterval(pos+this.minValue,
                                                    vars[i].getSup(),
                                this, false);
						nbVus++;
					}
				}
			}
			pos ++;
		}
	}
	// debug
	private static void display(int[] tab) {
		System.out.println("");
		for(int i=0; i<tab.length; i++) {
			System.out.print(tab[i] + " ");
		}
		System.out.println("");
	}

}
