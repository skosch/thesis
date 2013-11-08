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


package choco.cp.solver.constraints.integer;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.propagation.event.VarEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A class implementing the constraint A[I] == X, with I and X being IntVars and A an array of IntVars
 */
public final class ElementV extends AbstractLargeIntSConstraint {
    protected IStateBool valueUpdateNeeded;
    protected IStateBool indexUpdateNeeded;
    private int offset;

    public ElementV(IntDomainVar[] vars, int offset, IEnvironment environment) {
        super(ConstraintEvent.QUADRATIC, vars);
        this.offset = offset;
        initElementV(environment);
    }

    private void initElementV(IEnvironment environment) {
        valueUpdateNeeded = environment.makeBool(true);
        indexUpdateNeeded = environment.makeBool(true);
    }

//  public Object clone() throws CloneNotSupportedException {
//    Object res = super.clone();
//    ((ElementV) res).initElementV(environment);
//    return res;
//  }

    public String toString() {
        return "eltV";
    }

    @Override
    public int getFilteredEventMask(int idx) {
        if (vars[idx].hasEnumeratedDomain()) {
            return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
        } else {
            return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
        }
    }

    public String pretty() {
        return (this.getValueVar().toString() + " = nth(" + this.getIndexVar().toString() + ", " + StringUtils.pretty(this.vars, 0, vars.length - 2) + ")");
    }


    protected IntDomainVar getIndexVar() {
        return vars[vars.length - 2];
    }

    protected IntDomainVar getValueVar() {
        return vars[vars.length - 1];
    }

    public boolean isSatisfied(int[] tuple) {
        return tuple[tuple[vars.length - 2] + offset] == tuple[vars.length - 1]; //getValueVar().getVal());
    }

    protected void updateValueFromIndex() throws ContradictionException {
        IntDomainVar idxVar = getIndexVar();
        IntDomainVar valVar = getValueVar();
        int minval = Integer.MAX_VALUE;
        int maxval = Integer.MIN_VALUE;
        DisposableIntIterator iter = idxVar.getDomain().getIterator();
        for (; iter.hasNext();) {
            int feasibleIndex = iter.next();
            minval = Math.min(minval, vars[feasibleIndex + offset].getInf());
            maxval = Math.max(maxval, vars[feasibleIndex + offset].getSup());
        }
        iter.dispose();
        // further optimization:
        // I should consider for the min, the minimum value in domain(c.vars[feasibleIndex) that is >= to valVar.inf
        // (it can be greater than valVar.inf if there are holes in domain(c.vars[feasibleIndex]))
        valVar.updateInf(minval, this, false);
        valVar.updateSup(maxval, this, false);
        // v1.0: propagate on holes when valVar has an enumerated domain
        if (valVar.hasEnumeratedDomain()) {
            for (int v = valVar.getInf(); v < valVar.getSup(); v = valVar.getNextDomainValue(v)) {
                boolean possibleV = false;
                DisposableIntIterator it = idxVar.getDomain().getIterator();
                while ((it.hasNext()) && !(possibleV)) {
                    int tentativeIdx = it.next();
                    //      for (int tentativeIdx = idxVar.getInf(); tentativeIdx <= idxVar.getSup(); tentativeIdx = idxVar.getNextDomainValue(tentativeIdx)) {
                    if (vars[tentativeIdx + offset].canBeInstantiatedTo(v)) {
                        possibleV = true;
                        break;
                    }
                }
                it.dispose();
                if (!possibleV) {
                    valVar.removeVal(v, this, false);
                }
            }
        }
        valueUpdateNeeded.set(false);
    }

    protected void updateIndexFromValue() throws ContradictionException {
        IntDomainVar idxVar = getIndexVar();
        IntDomainVar valVar = getValueVar();
        int minFeasibleIndex = Math.max(0 - offset, idxVar.getInf());
        int maxFeasibleIndex = Math.min(idxVar.getSup(), vars.length - 3 - offset);
        int cause = cIndices[vars.length - 2];
        if (valVar.hasEnumeratedDomain()) {
            cause = VarEvent.NOCAUSE;
        }
        while (idxVar.canBeInstantiatedTo(minFeasibleIndex) &&
                !(valVar.canBeEqualTo(vars[minFeasibleIndex + offset]))) {
            minFeasibleIndex++;
        }
        idxVar.updateInf(minFeasibleIndex, this, false);


        while (idxVar.canBeInstantiatedTo(maxFeasibleIndex) &&
                !(valVar.canBeEqualTo(vars[maxFeasibleIndex + offset]))) {
            maxFeasibleIndex--;
        }
        idxVar.updateSup(maxFeasibleIndex, this, false);

        if (idxVar.hasEnumeratedDomain()) { //those remVal would be ignored for variables using an interval approximation for domain
            for (int i = minFeasibleIndex + 1; i < maxFeasibleIndex - 1; i++) {
                if (idxVar.canBeInstantiatedTo(i) && !valVar.canBeEqualTo(vars[i + offset])) {
                    idxVar.removeVal(i, this, false);
                }
            }
        }
        // if the domain of idxVar has been reduced to one element, then it behaves like an equality
        if (idxVar.isInstantiated()) {
            equalityBehaviour();
        }
        indexUpdateNeeded.set(false);
    }

    // Once the index is known, the constraints behaves like an equality : valVar == c.vars[idxVar.value]
// This method must only be called when the value of idxVar is known.
    protected void equalityBehaviour() throws ContradictionException {
        assert (getIndexVar().isInstantiated());
        int indexVal = getIndexVar().getVal();
        IntDomainVar valVar = getValueVar();
        IntDomainVar targetVar = vars[indexVal + offset];
        // code similar to awake@Equalxyc
        valVar.updateInf(targetVar.getInf(), this, false);
        valVar.updateSup(targetVar.getSup(), this, false);
        targetVar.updateInf(valVar.getInf(), this, false);
        targetVar.updateSup(valVar.getSup(), this, false);
        if (targetVar.hasEnumeratedDomain()) {
            int left = Integer.MIN_VALUE;
            int right = left;
            for (int val = valVar.getInf(); val < valVar.getSup(); val = valVar.getNextDomainValue(val)) {
                if (!targetVar.canBeInstantiatedTo(val)) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        valVar.removeInterval(left, right, this, false);
                        left = val;
                        right = val;
                    }
                    //valVar.removeVal(val, this, false);
                }
            }
            valVar.removeInterval(left, right, this, false);
        }
        if (valVar.hasEnumeratedDomain()) {
            int left = Integer.MIN_VALUE;
            int right = left;
            for (int val = targetVar.getInf(); val < targetVar.getSup(); val = targetVar.getNextDomainValue(val)) {
                if (!valVar.canBeInstantiatedTo(val)) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        targetVar.removeInterval(left, right, this, false);
                        left = val;
                        right = val;
                    }
//                    targetVar.removeVal(val, this, false);
                }
            }
            targetVar.removeInterval(left, right, this, false);
        }
    }

    public void awake() throws ContradictionException {
        int n = vars.length;
        IntDomainVar idxVar = getIndexVar();
        idxVar.updateInf(0 - offset, this, false);
        idxVar.updateSup(n - 3 - offset, this, false);
        propagate();
    }

    public void propagate() throws ContradictionException {
        if (indexUpdateNeeded.get()) {
            updateIndexFromValue();
        }
        if (getIndexVar().isInstantiated()) {
            equalityBehaviour();
        } else if (valueUpdateNeeded.get()) {
            updateValueFromIndex();
        }
    }

    public void awakeOnInf(int idx) throws ContradictionException {
        IntDomainVar idxVar = getIndexVar();
        IntDomainVar valVar = getValueVar();
        if (idx == vars.length - 2) {        // the event concerns idxVar
            if (idxVar.isInstantiated()) {
                equalityBehaviour();
            } else {
                updateValueFromIndex();
            }
        } else if (idx == vars.length - 1) { // the event concerns valVar
            if (idxVar.isInstantiated()) {
                int idxVal = idxVar.getVal();
                vars[idxVal + offset].updateInf(valVar.getInf(), this, false);
            } else {
                updateIndexFromValue();
            }
        } else {                            // the event concerns a variable from the array
            if (idxVar.isInstantiated()) {
                int idxVal = idxVar.getVal();
                if (idx == idxVal + offset) {
                    valVar.updateInf(vars[idx].getInf(), this, false);
                }
            } else if (idxVar.canBeInstantiatedTo(idx - offset)) {  //otherwise the variable is not in scope
                if (!valVar.canBeEqualTo(vars[idx])) {
                    idxVar.removeVal(idx - offset, this, true);
                    // NOCAUSE because if it changes the domain of IndexVar (what is not sure if idxVar
                    // uses an interval approximated domain) then it must cause updateValueFromIndex(c)
                } else if (vars[idx].getInf() > valVar.getInf()) {
                    // only the inf can change if the index is not removed
                    int minval = Integer.MAX_VALUE;
                    DisposableIntIterator it = idxVar.getDomain().getIterator();
                    for (; it.hasNext();) {
                        int feasibleIndex = it.next() + this.offset;
                        minval = Math.min(minval, vars[feasibleIndex].getInf());
                    }
                    it.dispose();
                    valVar.updateInf(minval, this, true);
                    // NOCAUSE because if valVar takes a new min, then it can have consequence
                    // on the constraint itself (ie remove indices such that l[i].sup < value.inf)
                }
            }
        }
    }

    public void awakeOnSup(int idx) throws ContradictionException {
        IntDomainVar idxVar = getIndexVar();
        IntDomainVar valVar = getValueVar();
        if (idx == vars.length - 2) {        // the event concerns idxVar
            if (idxVar.isInstantiated()) {
                equalityBehaviour();
            } else {
                updateValueFromIndex();
            }
        } else if (idx == vars.length - 1) {  // the event concerns valVar
            if (idxVar.isInstantiated()) {
                int idxVal = idxVar.getVal();
                vars[idxVal + offset].updateSup(valVar.getSup(), this, false);
            } else {
                updateIndexFromValue();
            }
        } else {                            // the event concerns a variable from the array
            if (idxVar.isInstantiated()) {
                int idxVal = idxVar.getVal();
                if (idx == idxVal + offset) {
                    valVar.updateSup(vars[idx].getSup(), this, false);
                }
            } else if (idxVar.canBeInstantiatedTo(idx - offset)) {  //otherwise the variable is not in scope
                if (!valVar.canBeEqualTo(vars[idx])) {
                    idxVar.removeVal(idx - offset, this, true);
                    // NOCAUSE because if it changes the domain of IndexVar (what is not sure if idxVar
                    // uses an interval approximated domain) then it must cause updateValueFromIndex(c)
                } else if (vars[idx].getSup() < valVar.getSup()) {
                    // only the sup can change if the index is not removed
                    int maxval = Integer.MIN_VALUE;
                    DisposableIntIterator it = idxVar.getDomain().getIterator();
                    for (; it.hasNext();) {
                        int feasibleIndex = it.next() + this.offset;
                        maxval = Math.max(maxval, vars[feasibleIndex].getSup());
                    }
                    it.dispose();
                    valVar.updateSup(maxval, this, true);
                    // NOCAUSE because if valVar takes a new min, then it can have consequence
                    // on the constraint itself (ie remove indices such that l[i].sup < value.inf)
                }
            }
        }
    }

    public void awakeOnInst(int idx) throws ContradictionException {
        IntDomainVar idxVar = getIndexVar();
        IntDomainVar valVar = getValueVar();
        if (idx == vars.length - 2) {        // the event concerns idxVar
            equalityBehaviour();
        } else if (idx == vars.length - 1) {  // the event concerns valVar
            if (idxVar.isInstantiated()) {
                int idxVal = idxVar.getVal();
                vars[idxVal + offset].instantiate(valVar.getVal(), this, false);
            } else {
                updateIndexFromValue();
            }
        } else {                            // the event concerns a variable from the array
            if (idxVar.isInstantiated()) {
                int idxVal = idxVar.getVal();
                if (idx == idxVal + offset) {
                    valVar.instantiate(vars[idx].getVal(), this, false);
                }
            } else if (idxVar.canBeInstantiatedTo(idx - offset)) {  //otherwise the variable is not in scope
                if (!valVar.canBeEqualTo(vars[idx])) {
                    idxVar.removeVal(idx - offset, this, true);
                    // NOCAUSE because if it changes the domain of IndexVar (what is not sure if idxVar
                    // uses an interval approximated domain) then it must cause updateValueFromIndex(c)
                } else {
                    updateValueFromIndex(); // both the min and max may have changed
                }
            }
        }
    }

    public void awakeOnRem(int idx, int x) throws ContradictionException {
        IntDomainVar idxVar = getIndexVar();
        IntDomainVar valVar = getValueVar();
        if (idx == vars.length - 2) {        // the event concerns idxVar
            updateValueFromIndex();
        } else if (idx == vars.length - 1) {  // the event concerns valVar
            if (idxVar.isInstantiated()) {
                int idxVal = idxVar.getVal();
                vars[idxVal + offset].removeVal(x, this, false);
            } else {
                updateIndexFromValue();
            }
        } else {                            // the event concerns a variable from the array
            if (idxVar.isInstantiated()) {
                int idxVal = idxVar.getVal();
                if (idx == idxVal + offset) {
                    valVar.removeVal(x, this, false);
                }
            } else if ((idxVar.canBeInstantiatedTo(idx - offset)) && (valVar.hasEnumeratedDomain())) {
                boolean existsSupport = false;
                DisposableIntIterator it = idxVar.getDomain().getIterator();
                for (; it.hasNext();) {
                    int feasibleIndex = it.next() + this.offset;
                    if (vars[feasibleIndex].canBeInstantiatedTo(x)) {
                        existsSupport = true;
                    }
                }
                it.dispose();
                if (!existsSupport) {
                    valVar.removeVal(x, this, true);
                }
            }
        }
    }


    public Boolean isEntailed() {
        Boolean isEntailed = null;
        IntDomainVar idxVar = getIndexVar();
        IntDomainVar valVar = getValueVar();
        if ((valVar.isInstantiated()) &&
                (idxVar.getInf() + this.offset >= 0) &&
                (idxVar.getSup() + this.offset < vars.length - 2)) {
            boolean allEqualToValVar = true;
            DisposableIntIterator it = idxVar.getDomain().getIterator();
            for (; it.hasNext();) {
                int feasibleIndex = it.next() + this.offset;
                if (!vars[feasibleIndex].isInstantiatedTo(valVar.getVal())) {
                    allEqualToValVar = false;
                }
            }
            it.dispose();
            if (allEqualToValVar) {
                isEntailed = Boolean.TRUE;
            }
        }
        if (isEntailed != Boolean.TRUE) {
            boolean existsSupport = false;
            DisposableIntIterator it = idxVar.getDomain().getIterator();
            for (; it.hasNext();) {
                int feasibleIndex = it.next() + this.offset;
                if ((feasibleIndex >= 0) && (feasibleIndex < vars.length - 2) && (valVar.canBeEqualTo(vars[feasibleIndex]))) {
                    existsSupport = true;
                }
            }
            it.dispose();
            if (!existsSupport) isEntailed = Boolean.FALSE;
        }
        return isEntailed;
    }


}
