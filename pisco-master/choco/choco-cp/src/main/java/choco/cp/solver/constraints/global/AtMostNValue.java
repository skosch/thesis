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

package choco.cp.solver.constraints.global;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntArrayList;

import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Enforce the number of identical values wihtin a list of variables to
 * be at most a given variable.
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 08-Jan-2007
 * Time: 09:13:13
 */
public final class AtMostNValue extends AbstractLargeIntSConstraint {

	// temporary data structure
	// The set of instantiated values
	private final BitSet gval;

	// number of ground variables : |gVal|
	private int nGval;

	// list of non instantiated variables
	private final TIntArrayList freeVars;
	// list of variables defining the nodes of the intersection graph
	private TIntArrayList dVar;
	// Intersection graph encoded by the neigbhoors of each variable
	private final BitSet[] ngraph;

	private final int maxDSize;

	private final int nVars;

    private final int offset;

    private static IntDomainVar[] makeVarTable(final IntDomainVar[] vars, final IntDomainVar nvalue) {
		final IntDomainVar[] vs = new IntDomainVar[vars.length + 1];
		System.arraycopy(vars, 0, vs, 0, vars.length);
		vs[vars.length] = nvalue;
		return vs;
	}

	public AtMostNValue(final IntDomainVar[] vars, final IntDomainVar nvalue) {
		super(ConstraintEvent.QUADRATIC, makeVarTable(vars, nvalue));
		int toffset = Integer.MAX_VALUE;
		int tsize = 0;
		for (final IntDomainVar v : vars) {
			if (toffset > v.getInf()){
                toffset = v.getInf();
            }
			if (tsize > (v.getSup() - v.getInf() + 1)){
				tsize = v.getSup() - v.getInf() + 1;
            }
		}
		// An offset to deal with negative domains and positive domains whose minimum is not 0
		this.offset = -toffset;
        this.maxDSize = tsize;
		gval = new BitSet(tsize);
		dVar = new TIntArrayList(vars.length);
		freeVars = new TIntArrayList(vars.length);
		ngraph = new BitSet[vars.length];
		for (int i = 0; i < ngraph.length; i++) {
			ngraph[i] = new BitSet(vars.length);
		}
		nVars = vars.length;
	}

	// *****************************************************
	// Restricting efficiently a domain to a set of values
	// *****************************************************

	// /!\  Logging statements really decrease performance
	// calculer l'ensemble des valeurs interdites ?
	// forbiddenvalues = (domain ^ allowedvalues) xor allowedvalues
	//TODO : improve it with IStateBitSet api on intersection , union ....
	void restrict(final IntDomainVar v, final BitSet allowvalues) throws ContradictionException {
		//if (!v.intersect(BitSet val, cste)) this.fail()!
		final int toffset = v.getInf();
		final BitSet allowedDomain = allowvalues.get(v.getInf() + this.offset, v.getSup() + this.offset + 1);
		final int newInf = allowedDomain.nextSetBit(0) + toffset;
		final int newSup = allowedDomain.length() + toffset;
		//LOGGER.log(Level.INFO, "{0} updateInf {1} of {2}", new Object[]{db, newInf, v});
		v.updateInf(newInf, this, true);
		v.updateSup(newSup, this, true);
		if (v.hasEnumeratedDomain()) {
			final IntDomain dom = v.getDomain();
			final DisposableIntIterator it = dom.getIterator();
			try{
                while(it.hasNext()) {
                    final int val = it.next();
                    if (!allowedDomain.get(val - toffset)) {
                        //LOGGER.log(Level.INFO, "2 remove value {0} from {1}", new Object[]{val, v});
                        v.removeVal(val, this, true);
                    }
                }
            }finally {
                it.dispose();
            }
		}
	}



	// *****************************************************
	// *****************************************************



	/**
	 * @param v : Test if the intersection of the domain of v and gval is empty
	 * @return false if v has at least one value in gval.
	 */
	boolean emptyIntersectionWithGval(final IntDomainVar v) {
		final DisposableIntIterator vdom = v.getDomain().getIterator();
		while (vdom.hasNext()) {
			if (gval.get(vdom.next() + offset)){
                vdom.dispose();
                return false;
            }
		}
        vdom.dispose();
		return true;
	}

	/**
	 * @return the intersection of all domains included in dVar as
	 *         a BitSet
	 */
    BitSet intersectionDomains() {
		if (!dVar.isEmpty()) {
			final List<Integer> inter = new LinkedList<Integer>();
			final DisposableIntIterator vdom = vars[dVar.get(0)].getDomain().getIterator();
			while (vdom.hasNext()) {
				inter.add(vdom.next());
			}
            vdom.dispose();

            for(int i = 0; i < dVar.size(); i++){
                final int next = dVar.get(i);
				final IntDomainVar v = vars[next];
				for (final Iterator it = inter.iterator(); it.hasNext();) {
					if (!v.canBeInstantiatedTo((Integer) it.next())){
						it.remove();
                    }
				}
			}
			final BitSet interDvar = new BitSet(maxDSize);
			for (final Integer i : inter) {
				interDvar.set(i + offset);
			}
			return interDvar;
		} else {
            return new BitSet(0);
        }
	}

	/**
	 * Build the reduced intersection graph and check for special cases where the complete
	 * algorithm do not need to be called
	 *
	 * @return true if the the complete pruning has to be done
	 * @throws choco.kernel.solver.ContradictionException contradiction on the nvalue variable
	 */
    boolean basicPruning() throws ContradictionException {
		nGval = 0;
		gval.clear();
		dVar.clear();
		freeVars.clear();

		// basic lower bound based on instantiated variables
		for (int i = 0; i < nVars; i++) {
			final IntDomainVar v = vars[i];
			if (v.isInstantiated()) {
				if (!gval.get(v.getVal() + offset)) {
					nGval++;
				}
				gval.set(v.getVal() + offset);
			} else {
				freeVars.add(i);
			}
		}
		vars[nVars].updateInf(nGval, this, false);
		final int k = vars[vars.length - 1].getSup() - nGval;

		if (k == 0) {
			pruningK0();
			return false;
		}

		// build dVar so that the variables are sorted according to their indexes
		// this fact is used later by the computeNeighborsGraph method
		if (nGval == 0) {
			dVar = (TIntArrayList)freeVars.clone();
		} else {
            for(int i = 0; i < freeVars.size(); i++){
				final int j = freeVars.get(i);
				if(emptyIntersectionWithGval(vars[j])){
                    dVar.add(j);
                }
			}
		}

		// update the lower bound of the nvalue variable
		// pruning for special cases where the bound is reached or almost reached
		if (k == 1) {
			if (!dVar.isEmpty()){
				pruningK1();
            }
			return false;
		}
		return true;
	}


	// case of Nvalue instantiated to nGval
    void pruningK0() throws ContradictionException {
        for(int k = 0; k < freeVars.size(); k++){
				final int i = freeVars.get(k);
                restrict(vars[i],gval);
            }
	}

	// case of Nvalue reduced to [nGval,nGval + 1]
    void pruningK1() throws ContradictionException {
		final BitSet interDvar = intersectionDomains();
		interDvar.or(gval);
        for(int k = 0; k < freeVars.size(); k++){
				final int i = freeVars.get(k);
                restrict(vars[i],interDvar);
            }
	}

	// a greedy approach to compute an independent set of the intersection graph
    void mdPruning() throws ContradictionException {
		if (basicPruning() && (nGval + dVar.size() >= vars[nVars].getSup())) {
			final LinkedList<IntDomainVar> a = new LinkedList<IntDomainVar>();
			computeNeighborsGraph();
			while (!dVar.isEmpty()) {
				int min = Integer.MAX_VALUE;
				int y = -1;
                for(int i = 0; i < dVar.size(); i++){
                final int next = dVar.get(i);
					if (min >= ngraph[next].size()) {
						min = ngraph[next].size();
						y = next;
					}
				}
                for(int i = 0; i < dVar.size(); i++){
                final int next = dVar.get(i);
					if (next == y || ngraph[y].get(next)){
                        dVar.remove(i--);
                    }
				}
				a.add(vars[y]);
			}
			final int lb = a.size() + nGval;
			vars[nVars].updateInf(lb, this, false);
			if (lb == vars[nVars].getSup()){
				pruning(a);
            }
		}

	}

	void computeNeighborsGraph() {
		for (int i = 0; i < ngraph.length; i++) {
			ngraph[i].clear();
		}
        for(int i = 0; i < dVar.size(); i++){
            final int x = dVar.get(i);
            for(int j = 0; i < dVar.size(); j++){
                final int y = dVar.get(j);
				if (x <= y) {
                    break;
                }
				else if (vars[x].canBeEqualTo(vars[y])) {
					ngraph[x].set(y);
					ngraph[y].set(x);
				}
			}
		}
	}

	void pruning(final LinkedList<IntDomainVar> a) throws ContradictionException {
		final BitSet unionOfAllowedValue = gval;
		for (final IntDomainVar x : a) {
			final DisposableIntIterator it = x.getDomain().getIterator();
            try{
			while (it.hasNext()) {
				final int value = it.next();
				unionOfAllowedValue.set(value + offset);
			}
            }finally {
                it.dispose();
            }
		}
        for(int k = 0; k < freeVars.size(); k++){
            final int i = freeVars.get(k);
            restrict(vars[i],unionOfAllowedValue);
        }

	}
	
	// /!\  Logging statements really decrease performance

	public void awakeOnInf(final int idx) throws ContradictionException {
		//LOGGER.log(Level.INFO, "inf {0} to {1}", new Object[]{vars[idx], vars[idx].getInf()});
		constAwake(false);
	}

	public void awakeOnInst(final int idx) throws ContradictionException {
		//LOGGER.log(Level.INFO, "instantiate {0} to {1}", new Object[]{vars[idx], vars[idx].getVal()});
		constAwake(false);
	}

	public void awakeOnSup(final int idx) throws ContradictionException {
		//LOGGER.log(Level.INFO, "sup {0} to {1}", new Object[]{vars[idx], vars[idx].getSup()});
		constAwake(false);
	}

	public void awakeOnRemovals(final int idx, final DisposableIntIterator deltaDomain) throws ContradictionException {
//		while (deltaDomain.hasNext()) {
//				LOGGER.log(Level.INFO, "remove from {0} value {1}", new Object[]{vars[idx], deltaDomain.next()});
//			}
//		}
		constAwake(false);
	}

	public void awake() throws ContradictionException {
		propagate();
	}

	public void propagate() throws ContradictionException {
		mdPruning();
	}

	/**
	 * This method assumes that all variables are instantiated and checks if the values are consistent with the
	 * constraint.
	 * Here it couns how many values are in the set, thanks to a BitSet in order to know if a value has already been
	 * counted.
	 * @return true if the number of different values in all variables except the last one is less (or equal) than
	 * the value of the last variable
	 */
	public boolean isSatisfied(final int[] tuple) {
		final BitSet b = new BitSet(nVars);
		int nval = 0;
		for (int i = 0; i < nVars; i++) {
			final int val = tuple[i];
			if (!b.get(val)) {
				nval ++;
				b.set(val);
			}
		}
		return nval <= tuple[nVars];
	}

	public String pretty() {
		final StringBuilder sb = new StringBuilder(17);
		sb.append("AtMostNValue({");
		for (int i = 0; i < vars.length - 1; i++) {
			if (i > 0){
                sb.append(", ");
            }
			final IntDomainVar var = vars[i];
			sb.append(var.pretty());
		}
		sb.append("}, ").append(vars[vars.length-1]).append(')');
		return sb.toString();
	}
}

