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

package choco.ecp.solver.variables.integer.cbj;


import choco.cp.solver.variables.integer.BitSetIntDomain;
import choco.cp.solver.variables.integer.IntDomainVarImpl;
import choco.ecp.solver.JumpSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.propagation.dbt.StructureMaintainer;
import choco.ecp.solver.search.cbj.JumpContradictionException;
import choco.ecp.solver.variables.integer.ExplainedIntDomain;
import choco.kernel.common.util.objects.ConstraintCollection;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;

import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

public class JumpBitSetIntDomain extends BitSetIntDomain implements ExplainedIntDomain {

    /**
     * A list of explanations for value withdrawals.
     */

    public Explanation[] explanationOnVal;

    /**
     * original domain of the variable
     */
    public BitSet originalDomain;

    public JumpBitSetIntDomain(IntDomainVarImpl v, int a, int b) {
        super(v, a, b);
        this.explanationOnVal = new Explanation[b - a + 1];
        originalDomain = new BitSet(capacity);
        for (int i = 0; i < capacity; i++) {
            originalDomain.set(i);
        }
    }

    public JumpBitSetIntDomain(IntDomainVarImpl v, int[] sortedValues) {
        super(v, sortedValues);
        int a = sortedValues[0];
        int b = sortedValues[sortedValues.length - 1];
        this.explanationOnVal = new Explanation[b - a + 1];
        originalDomain = new BitSet(capacity);
        for (int i = 0; i < sortedValues.length; i++) {
            originalDomain.set(sortedValues[i] - a);
        }
    }

    /**
     * Returns the original lower bound.
     */

    public int getOriginalInf() {
        return this.offset;
    }


    /**
     * Returns the original upper bound.
     */

    public int getOriginalSup() {
        return this.offset + this.chain.length - 1;
    }


    /**
     * Returns all the value currently in the domain.
     */

    public int[] getAllValues() {
        int[] ret = new int[this.getSize()];
        int idx = 0;
        for (int val = contents.nextSetBit(0); val >= 0; val = contents.nextSetBit(val + 1)) {
            ret[idx] = val + offset;
            idx++;
        }
        return ret;
    }

    /**
     * Updates the lower bound and posts the event.
     */

    public boolean updateInf(int x, int idx, Explanation e) throws ContradictionException {
        if (x > getInf()) {
            boolean rep = false;
            int newi = x - offset;  // index of the new lower bound
            for (int i = contents.nextSetBit(0); i < newi; i = contents.nextSetBit(i + 1)) {
                rep |= this.removeVal(i + offset, idx, (Explanation) e.copy());
            }
            inf.set(contents.nextSetBit(newi) + offset);
            return rep;
        } else return false;
    }


    /**
     * Updates the upper bound and posts the event.
     */

    public boolean updateSup(int x, int idx, Explanation e) throws ContradictionException {
        if (x < getSup()) {
            boolean rep = false;
            int newi = x - offset;  // index of the new lower bound
            for (int i = contents.prevSetBit(capacity - 1); i > newi; i = contents.prevSetBit(i - 1)) {
                rep |= this.removeVal(i + offset, idx, (Explanation) e.copy());
            }
            sup.set(contents.prevSetBit(newi) + offset);
            return rep;
        } else return false;
    }


    /**
     * Removes a value and posts the event.
     */

    public boolean removeVal(int value, int idx, Explanation e) throws ContradictionException {
        if (this.removeVal(value, e)) {
            solver.getPropagationEngine().postRemoveVal(variable, value, idx);
            if (this.getSize() == 0) {
                Explanation exp = ((JumpSolver) solver).makeExplanation();
                this.self_explain(ExplainedIntDomain.DOM, exp);
                throw (new JumpContradictionException(this.solver(), exp));
                //((PalmEngine) this.getProblem().getPropagationEngine()).raisePalmContradiction((PalmIntVar) this.variable);
            }
            return true;
        }
        return false;
    }


    /**
     * Allows to get an explanation for the domain or a bound of the variable. This explanation is merge to the
     * explanation in parameter.
     *
     * @param select Should be <code>PalmIntDomain.INF</code>, <code>PalmIntDomain.SUP</code>, or <code>PalmIntDomain.DOM</code>
     */

    public void self_explain(int select, Explanation expl) {
        switch (select) {
            case DOM:
                for (int i = originalDomain.nextSetBit(0); i >= 0; i = originalDomain.nextSetBit(i + 1))
                    this.self_explain(VAL, i + offset, expl);
                break;
            case INF:
                int inf = this.getInf() - 1 - offset;
                for (int i = originalDomain.nextSetBit(0); i <= inf; i = originalDomain.nextSetBit(i + 1))
                    this.self_explain(VAL, i + offset, expl);
                break;
            case SUP:
                int sup = this.getSup() + 1 - offset;
                for (int i = originalDomain.nextSetBit(sup); i >= 0; i = originalDomain.nextSetBit(i + 1))
                    this.self_explain(VAL, i + offset, expl);
                break;
            default:
                if (Logger.getLogger("choco").isLoggable(Level.WARNING))
                    Logger.getLogger("choco").warning("PaLM: VAL needs another parameter in self_explain (IntDomainVar)");
        }
    }


    /**
     * Allows to get an explanation for a value removal from the variable. This explanation is merge to the
     * explanation in parameter.
     *
     * @param select Should be <code>PalmIntDomain.VAL</code>
     */

    public void self_explain(int select, int x, Explanation expl) {
        if (select == VAL && !this.contains(x)) {
            int realVal = x - this.getOriginalInf(); //this.bucket.getOffset();
            ConstraintCollection expla = null;
            if ((realVal >= 0) && (realVal < this.getOriginalSup() -
                    this.getOriginalInf() + 1)) { //this.bucket.size())) {
                expla = this.explanationOnVal[realVal];
            }
            if (expla != null) expl.merge(expla);
        } else if (select != VAL) {
            if (Logger.getLogger("choco").isLoggable(Level.WARNING))
                Logger.getLogger("choco").warning("PaLM: INF, SUP or DOM do not need a supplementary parameter in self_explain (IntDomainVar)");
        }
    }


    /**
     * Checks if the value is in the domain. Basically it checks if the value is in the original domain and if this case
     * only calls the super method.
     */

    public boolean contains(int val) {
        if (val < this.getOriginalInf() || val > this.getOriginalSup()) {
            return false;
        }
        return super.contains(val);
    }

    protected boolean removeVal(int value, Explanation e) {
        if (this.contains(value)) {
            this.explanationOnVal[value - this.getOriginalInf()] = e;
            //e.makeRemovalExplanation(value, (PalmIntVar) this.variable);
            this.remove(value);
            if (value == getInf()) {
                inf.set(contents.nextSetBit(value - offset) + offset);
            } else if (value == getSup()) {
                sup.set(contents.prevSetBit(value - offset) + offset);
            }
            if (this.getSize() == 1) {
                this.variable.value.set(getInf());
            } else if (this.getSize() == 0) {
                this.variable.value.set(IStateInt.UNKNOWN_INT);
            }
            StructureMaintainer.updateDataStructures(this.variable, VAL, value, 0);
            return true;
        }
        return false;
    }
}
