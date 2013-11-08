/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
package choco.cp.solver.constraints.integer;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 26/04/11
 * Time: 13:24
 * <p/>
 * X collection int-variable
 * Y collection int-variable
 * <p/>
 * if the ith variable of the collection X is assigned to j and if j is less than or equal
 * to the number of items of the collection Y then the jth variable of the collection Y is assigned to i.
 * Conversely, if the jth variable of the collection Y is assigned to i and if i is less than or equal
 * to the number of items of the collection X then the ith variable of the collection X is assigned to j.
 * <p/>
 * note : this version (as inverseChanneling) takes into account on offset defined as the minimum value
 * of the first variable of the X sequence
 * <p/>
 * derived from inverseChanneling
 * <p/>
 * see <a href="http://www.emn.fr/z-info/sdemasse/gccat/Cinverse_within_range.html">inverse_within_range</a>
 * <p/>
 * <p/>
 */
public class InverseChannelingWithinRange extends AbstractLargeIntSConstraint {

    protected int nbx;
    protected int nby;
    private int min;

    /**
     * link x and y so that x[i] = j <=> y[j] = i
     * It is used to maintain both models on permutation problems
     *
     * @param allVars arrays of variables (x + y)
     * @param n       nb of x
     */
    public InverseChannelingWithinRange(IntDomainVar[] allVars, int n) {
        super(ConstraintEvent.LINEAR, allVars); // was CUBIC <nj>
        this.nbx = n;

        this.min = Integer.MAX_VALUE;
        for (int i = 0; i < allVars.length; i++) {
            IntDomainVar var = allVars[i];
            this.min = Math.min(this.min, var.getInf());
        }

        //this.min = allVars[0].getInf();
        this.nby = allVars.length - n;
    }


    @Override
    public int getFilteredEventMask(int idx) {
        if (vars[idx].hasEnumeratedDomain()) {
            return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
        } else {
            return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
        }
    }

    public void propagate() throws ContradictionException {


        for (int idx = 0; idx < nbx; idx++) {
            for (int i = 0; i < nby; i++) {
                if (!vars[idx].canBeInstantiatedTo(i + min)) {
                    vars[i + nbx].removeVal(idx + min, this, false);
                }
            }
        }

        for (int idx = nbx; idx < vars.length; idx++) {
            for (int i = 0; i < nbx; i++) {
                if (!vars[idx].canBeInstantiatedTo(i + min)) {
                    vars[i].removeVal(idx - nbx + min, this, false);
                }
            }
        }

    }


    public void awakeOnInf(int idx) throws ContradictionException {

        int val = vars[idx].getInf() - min;

        if (idx < nbx && val < nby) {
            for (int i = 0; i < val; i++) {
                vars[i + nbx].removeVal(idx + min, this, false);
            }
        } else if (idx >= nbx && val < nbx) {
            for (int i = 0; i < val; i++) {
                vars[i].removeVal(idx - nbx + min, this, false);
            }
        }
    }


    public void awakeOnSup(int idx) throws ContradictionException {
//        System.out.println("InverseChannelingWithinRange.awakeOnSup");
        int val = vars[idx].getSup() - min + 1;
        if (idx < nbx && val < nby) {
            for (int i = val; i < nby; i++) {
                vars[i + nbx].removeVal(idx + min, this, false);
            }
        } else if (idx >= nbx && val < nbx) {
            for (int i = val; i < nbx; i++) {
                vars[i].removeVal(idx - nbx + min, this, false);
            }
        }    //To change body of overridden methods use File | Settings | File Templates.
    }


    public void awakeOnRem(int idx, int x) throws ContradictionException {
        if (idx < nbx && x < nby) {
            vars[x - min + nbx].removeVal(idx + min, this, false);
        } else if (idx >= nbx && x < nbx) {
            vars[x - min].removeVal(idx - nbx + min, this, false);
        }
    }


    public void awakeOnInst(int idx) throws ContradictionException {

        int val = vars[idx].getVal() - min;


        if (idx < nbx && val < nby) {
            vars[val + nbx].instantiate(idx + min, this, false);
            for (int i = 0; i < nbx; i++) {
                if (i != idx) {
                    vars[i].removeVal(val + min, this, false);
                }
            }
        } else if (idx >= nbx && val < nbx) {
            vars[val].instantiate(idx - nbx + min, this, false);
            for (int i = nbx; i < vars.length; i++) {
                if (i != idx) {
                    vars[i].removeVal(val + min, this, false);
                }
            }
        }
    }


    public boolean isSatisfied(int[] tuple) {
        for (int i = 0; i < nbx; i++) {
            int x = tuple[i];
            if (x < nby && (tuple[x - min + nbx] != i + min)) {
                return false;
            }
        }

        for (int i = nbx; i < tuple.length; i++) {
            int x = tuple[i];
            if (x < nbx && (tuple[x - min] != i - nbx + min)) {
                return false;
            }
        }

        return true;
    }


    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("InverseChannelingWithinRange({");
        for (int i = 0; i < nbx; i++) {
            if (i > 0) sb.append(", ");
            IntDomainVar var = vars[i];
            sb.append(var.pretty());
        }
        sb.append("}, {");
        for (int i = 0; i < nby; i++) {
            if (i > 0) sb.append(", ");
            IntDomainVar var = vars[nbx + i];
            sb.append(var.pretty());
        }
        sb.append("})");
        return sb.toString();
    }


}