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

import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A constraint to enforce BoundConsistency on a global cardinality
 * based on the implementation of :
 * C.-G. Quimper, P. van Beek, A. Lopez-Ortiz, A. Golynski, and S.B. Sadjad.
 * An efficient bounds consistency algorithm for the global cardinality constraint. CP-2003.
 */
public final class BoundGcc extends BoundGccVar {

    private int[] maxOccurrences;
    private int[] minOccurrences;

    /**
     * Bound Global cardinality : Given an array of variables vars, min the minimal value over all variables,
     * and max the maximal value over all variables, the constraint ensures that the number of occurences
     * of the value i among the variables is between low[i - min] and up[i - min]. Note that the length
     * of low and up should be max - min + 1.
     * Use the propagator of :
     * C.-G. Quimper, P. van Beek, A. Lopez-Ortiz, A. Golynski, and S.B. Sadjad.
     * An efficient bounds consistency algorithm for the global cardinality constraint.
     * CP-2003.
     */
    public BoundGcc(IntDomainVar[] vars,
                    int firstDomainValue,
                    int lastDomainValue,
                    int[] minOccurrences,
                    int[] maxOccurrences, IEnvironment environment) {
        super(vars, null, firstDomainValue, lastDomainValue, environment);
        this.maxOccurrences = maxOccurrences;
        this.minOccurrences = minOccurrences;
    }

    @Override
    public int getMaxOcc(int i) {
        return maxOccurrences[i];
    }

    @Override
    public int getMinOcc(int i) {
        return minOccurrences[i];
    }

    protected void init() {
        super.init();
        l.compute(minOccurrences);
        u.compute(maxOccurrences);
    }

//	@Override
//	public void updateSup(IntDomainVar v, int nsup, int idx) throws ContradictionException {
//		v.updateSup(nsup, VarEvent.domOverWDegIdx(cIndices[idx]));//cIndices[idx]);
//	}
//
//	@Override
//	public void updateInf(IntDomainVar v, int ninf, int idx) throws ContradictionException {
//		v.updateInf(ninf, VarEvent.domOverWDegIdx(cIndices[idx]));//cIndices[idx]);
//	}


    @Override
    public void awake() throws ContradictionException {
        init();
        initBackDataStruct();
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].isInstantiated()) {
                filterBCOnInst(vars[i].getVal());
            }
        }
        for (int i = 0; i < nbVars; i++) {
            for (int val = vars[i].getInf() + 1; val < vars[i].getSup(); val++) {
                if (!vars[i].canBeInstantiatedTo(val))
                    filterBCOnRem(val);
            }
        }
        if (directInconsistentCount())
            this.fail();
        propagate();

    }

    public boolean directInconsistentCount() {
        for (int i = 0; i < range; i++) {
            if (val_maxOcc[i].get() < minOccurrences[i] ||
                    val_minOcc[i].get() > maxOccurrences[i])
                return true;
        }
        return false;
    }

    @Override
    public void propagate() throws ContradictionException {
        sortIt();

        // The variable domains must be inside the domain defined by
        // the lower bounds (l) and the upper bounds (u).
        assert (l.minValue() == u.minValue());
        assert (l.maxValue() == u.maxValue());
        assert (l.minValue() <= minsorted[0].var.getInf());
        assert (maxsorted[nbVars - 1].var.getSup() <= u.maxValue());
        assert (!directInconsistentCount());
        // Checks if there are values that must be assigned before the
        // smallest interval or after the last interval. If this is
        // the case, there is no solution to the problem
        // This is not an optimization since
        // filterLower{Min,Max} and
        // filterUpper{Min,Max} do not check for this case.

        if ((l.sum(l.minValue(), minsorted[0].var.getInf() - 1) > 0) ||
                (l.sum(maxsorted[getNbVars() - 1].var.getSup() + 1, l.maxValue()) > 0)) {
            this.fail();
        }
        filterLowerMax();
        filterLowerMin();
        filterUpperMax();
        filterUpperMin();
    }

    @Override
    public void awakeOnInf(int i) throws ContradictionException {
        this.constAwake(false);
        if (!vars[i].hasEnumeratedDomain()) {
            filterBCOnInf(i);
        }
    }

    @Override
    public void awakeOnSup(int i) throws ContradictionException {
        this.constAwake(false);
        if (!vars[i].hasEnumeratedDomain()) {
            filterBCOnSup(i);
        }
    }

    @Override
    public void awakeOnInst(int i) throws ContradictionException {   // Propagation classique
        int val = vars[i].getVal();
        constAwake(false);
        // if a value has been instantiated to its max number of occurrences
        // remove it from all variables
        val_minOcc[val - offset].add(1);
        filterBCOnInst(val);
    }

    @Override
    public void awakeOnRem(int idx, int val) throws ContradictionException {
        val_maxOcc[val - offset].add(-1);
        filterBCOnRem(val);
    }

    @Override
    public boolean isSatisfied() {
        int[] occurrences = new int[this.range];
        for (int i = 0; i < vars.length; i++) {
            IntDomainVar var = vars[i];
            occurrences[var.getVal() - this.offset]++;
        }
        for (int i = 0; i < occurrences.length; i++) {
            int occurrence = occurrences[i];
            if ((this.minOccurrences[i] > occurrence) || (occurrence > this.maxOccurrences[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        int[] occurrences = new int[this.range];
        for (int i = 0; i < nbVars; i++) {
            occurrences[tuple[i] - this.offset]++;
        }
        for (int i = 0; i < occurrences.length; i++) {
            int occurrence = occurrences[i];
            if ((this.minOccurrences[i] > occurrence) || (occurrence > this.maxOccurrences[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("BoundGcc({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            IntDomainVar var = vars[i];
            sb.append(var.pretty());
        }
        sb.append("}, {");
        for (int i = 0; i < minOccurrences.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            int minOccurrence = minOccurrences[i];
            int maxOccurrence = maxOccurrences[i];
            sb.append(minOccurrence).append(" <= #").append(this.offset + i).append(" <= ").append(maxOccurrence);
        }
        sb.append("})");
        return sb.toString();
    }

    @Override
    public Boolean isEntailed() {
        throw new UnsupportedOperationException("isEntailed not yet implemented on package choco.kernel.solver.constraints.global.BoundAlldiff");
    }

}
