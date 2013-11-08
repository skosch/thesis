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

/*
 * Created on 19 aout 08 by coletta 
 *
 */
package choco.cp.solver.constraints.set;

import choco.cp.solver.variables.set.SetVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.set.AbstractLargeSetSConstraint;
import choco.kernel.solver.variables.set.SetVar;
import gnu.trove.TIntIntHashMap;

import java.util.Arrays;

public final class SetNaryUnion extends AbstractLargeSetSConstraint {

    protected SetVar[] setVars;
    protected SetVar unionSet;
    /*
     * store the number of occurences of each value
     * in envellop of setvars set variables
     */
    protected IStateInt[] occurCpt;
    protected int offset;
    protected final static int UNION_SET_INDEX = 0;

    protected final IEnvironment environment;

    public SetNaryUnion(SetVar[] vars, IEnvironment environment) {
        super(vars);
        unionSet = vars[UNION_SET_INDEX];
        setVars = Arrays.copyOfRange(vars, 1, vars.length);
        this.environment = environment;
    }

    @Override
    public int getFilteredEventMask(int idx) {
        return SetVarEvent.INSTSET_MASK + SetVarEvent.ADDKER_MASK + SetVarEvent.REMENV_MASK;
    }

    @Override
    public void awake() throws ContradictionException {
        TIntIntHashMap allValues = new TIntIntHashMap();
        for (SetVar v : setVars) {
            DisposableIntIterator it = v.getDomain().getEnveloppeIterator();
            while (it.hasNext()) {
                int val = it.next();
                if (allValues.containsKey(val)) {
                    allValues.put(val, allValues.get(val) + 1);
                } else allValues.put(val, 1);
            }
            it.dispose();
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int[] values = allValues.keys();
        for (int v : values) {
            max = (v > max ? v : max);
            min = (v < min ? v : min);
        }
        occurCpt = new IStateInt[max - min + 1];
        offset = min;
        for (int v : values) {
            occurCpt[v - offset] = environment.makeInt(allValues.get(v));
        }
        this.propagate();
    }

    /**
     * Default propagation on kernel modification: propagation on adding a value to the kernel.
     */
    public void awakeOnKer(int varIdx, int x) throws ContradictionException {
        if (varIdx > UNION_SET_INDEX)
            unionSet.addToKernel(x, this, false);
        else //x has been add to the unionSet kernel
            instanciateIfLastOccurence(x);
    }

    /**
     * Default propagation on enveloppe modification: propagation on removing a value from the enveloppe.
     */
    public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
        if (varIdx == UNION_SET_INDEX)
            for (int idx = 0; idx < setVars.length; idx++)
                setVars[idx].remFromEnveloppe(x, this, false);
        else
            decOccurence(x);
    }

    /**
     * Default propagation on instantiation.
     */
    public void awakeOnInst(int varIdx) throws ContradictionException {
        DisposableIntIterator it = null;
        try {
            if (varIdx == UNION_SET_INDEX) {
                it = unionSet.getDomain().getKernelIterator();
                while (it.hasNext()) {
                    int val = it.next();
                    instanciateIfLastOccurence(val);
                }
            } else {
                it = vars[varIdx].getDomain().getKernelIterator();
                while (it.hasNext()) {
                    int val = it.next();
                    unionSet.addToKernel(val, this, false);
                }
                it.dispose();
                it = vars[varIdx].getDomain().getEnveloppeIterator();
                while (it.hasNext()) {
                    int val = it.next();
                    if (getNbOccurence(val) == 0) unionSet.remFromEnveloppe(val, this, false);
                }
            }
        } finally {
            it.dispose();
        }
    }

    public void propagate() throws ContradictionException {
        DisposableIntIterator it = null;
        try {
            for (int idx = 0; idx < setVars.length; idx++) {
                it = setVars[idx].getDomain().getKernelIterator();
                while (it.hasNext()) {
                    int val = it.next();
                    unionSet.addToKernel(val, this, false);
                }
                it.dispose();
            }

            it = unionSet.getDomain().getKernelIterator();
            while (it.hasNext()) {
                int val = it.next();
                instanciateIfLastOccurence(val);
            }
            it.dispose();

            it = unionSet.getDomain().getEnveloppeIterator();
            while (it.hasNext()) {
                int val = it.next();
                if (getNbOccurence(val) == 0) {
                    unionSet.remFromEnveloppe(val, this, false);
                }
            }
        } finally {
            it.dispose();
        }
    }

    @SuppressWarnings({"ConstantConditions"})
    public boolean isSatisfied() {
        DisposableIntIterator it = null;
        try {
            for (int idx = 0; idx < setVars.length; idx++) {
                it = setVars[idx].getDomain().getKernelIterator();
                while (it.hasNext()) {
                    int val = it.next();
                    if (!unionSet.isInDomainKernel(val)) return false;
                }
                it.dispose();
            }
            it = unionSet.getDomain().getKernelIterator();
            while (it.hasNext()) {
                int val = it.next();
                boolean isInASet = false;
                for (int idx = 0; idx < setVars.length; idx++)
                    if (setVars[idx].isInDomainKernel(val)) {
                        isInASet = true;
                        break;
                    }
                if (!isInASet) return false;
            }
            return true;
        } finally {
            it.dispose();
        }
    }

    public boolean isConsistent() {
        return isSatisfied();
    }

    private int getNbOccurence(int x) {
        if (x >= offset && x < offset + occurCpt.length && occurCpt[x - offset] != null) {
            return occurCpt[x - offset].get();
        }
        return 0;
    }

    private void decOccurence(int x) throws ContradictionException {
        occurCpt[x - offset].add(-1);
        instanciateIfLastOccurence(x);
    }

    private void instanciateIfLastOccurence(int x) throws ContradictionException {
        if (occurCpt[x - offset].get() <= 1 && unionSet.isInDomainKernel(x)) {
            if (occurCpt[x - offset].get() <= 0) {
                fail();
            }
            int removed = 0;
            for (int idx = 0; idx < setVars.length; idx++) {
                if (setVars[idx].isInDomainEnveloppe(x)) {
                    removed++;
                    setVars[idx].addToKernel(x, this, false);
                }
            }
            if (removed == 0) {
                fail();
            }
            assert (removed == 1);
        }
    }


    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("Union({");
        for (int i = 0; i < setVars.length; i++) {
            if (i > 0) sb.append(", ");
            SetVar var = setVars[i];
            sb.append(var.pretty());
        }
        sb.append("}) = " + unionSet.pretty());
        return sb.toString();
    }


    public String toString() {
        String autstring = "Union : ";
        for (int i = 0; i < vars.length; i++) {
            autstring += vars[i] + " ";
        }
        autstring += unionSet;
        return autstring;
    }

}
