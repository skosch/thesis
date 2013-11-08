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


package choco.cp.solver.constraints.global.matching;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.global.matching.AbstractBipartiteMatching;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Standard alldiff constraint with generalized AC
 * integer valued variables are used only for the left vertex set
 * no explicit variables are used for the right vertex set
 * the right vertex set is the interval (minValue .. maxValue)
 */
public final class AllDifferent extends AbstractBipartiteMatching {

    /**
     * API entry point: creating an ice alldifferent constraint (before posting it)
     *
     * @param vars
     * @param environment
     */
    public AllDifferent(IntDomainVar[] vars, IEnvironment environment) {
        super(environment, vars, vars.length, AllDifferent.getValueGap(vars));
        minValue = Integer.MAX_VALUE;
        maxValue = Integer.MIN_VALUE;
        for (int i = 0; i < vars.length; i++) {
            IntDomainVar var = vars[i];
            minValue = Math.min(var.getInf(), minValue);
            maxValue = Math.max(var.getSup(), maxValue);
        }
    }

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.REMVAL_MASK + IntVarEvent.INSTINT_MASK;
        // return 0x0B;
    }


    /**
     * AllDiff constraint constructor
     *
     * @param vars        the choco variable list
     * @param minValue    minimal value in vars domain
     * @param maxValue    maximal value in vars domain
     * @param environment
     */
    public AllDifferent(IntDomainVar[] vars, int minValue, int maxValue, IEnvironment environment) {
        super(environment, vars, vars.length, maxValue - minValue + 1);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    protected void init() {
        super.init();
        minValue = Integer.MAX_VALUE;
        maxValue = Integer.MIN_VALUE;
        for (int i = 0; i < vars.length; i++) {
            IntDomainVar var = vars[i];
            minValue = Math.min(var.getInf(), minValue);
            maxValue = Math.max(var.getSup(), maxValue);
        }
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Static method for one parameter constructor
     *
     * @param vars domain variable list
     * @return gap between min and max value
     */
    private static int getValueGap(IntDomainVar[] vars) {
        int minValue = Integer.MAX_VALUE, maxValue = Integer.MIN_VALUE;
        for (int i = 0; i < vars.length; i++) {
            IntDomainVar var = vars[i];
            minValue = Math.min(var.getInf(), minValue);
            maxValue = Math.max(var.getSup(), maxValue);
        }
        return maxValue - minValue + 1;
    }


    // The next two functions implement the main two events:

    /**
     * when an edge is definitely chosen in the bipartite assignment graph.
     *
     * @param i
     * @param j
     * @throws ContradictionException
     */
    public void setEdgeAndPublish(int i, int j) throws ContradictionException {
        this.setMatch(i, j);
        for (int i2 = 0; i2 < this.nbLeftVertices; i2++) {
            if (i2 != i) {
                this.vars[i2].removeVal(j + this.minValue, this, false);
            }
        }
    }

    /**
     * when an edge is definitely removed from the bipartite assignment graph.
     *
     * @param i
     * @param j
     * @throws ContradictionException
     */
    public void deleteEdgeAndPublish(int i, int j) throws ContradictionException {
        this.deleteMatch(i, j);
        this.vars[i].removeVal(j + this.minValue, this, false);
    }

    // propagation functions: reacting to choco events

    /**
     * when a value is removed from a domain var, removed the corresponding edge in current matching
     *
     * @param idx the variable index
     * @param val the removed value
     */
    public void awakeOnRem(int idx, int val) {
        this.deleteMatch(idx, val - this.minValue);
        this.constAwake(false);
    }


    /**
     * update current matching when a domain inf is increased
     *
     * @param idx the variable index
     */
    public void awakeOnInf(int idx) {
        for (int j = this.minValue; j < this.vars[idx].getInf(); j++) {
            this.deleteMatch(idx, j - this.minValue);
        }
        this.constAwake(false);
    }

    /**
     * update current matching when a domain sup is decreased
     *
     * @param idx the variable index
     */
    public void awakeOnSup(int idx) {
        for (int j = this.vars[idx].getSup() + 1; j <= this.maxValue; j++) {
            this.deleteMatch(idx, j - this.minValue);
        }
        this.constAwake(false);
    }

    /**
     * update current matching when a variable has been instantiated
     *
     * @param idx the variable index
     * @throws ContradictionException
     */
    public void awakeOnInst(int idx) throws ContradictionException {
        this.setEdgeAndPublish(idx, this.vars[idx].getVal() - this.minValue);
        this.constAwake(false);
    }

    /**
     * no specific initial propagation (awake does the same job as propagate)
     *
     * @throws ContradictionException
     */
    public void awake() throws ContradictionException {
        this.init();
        this.propagate();
    }

    /**
     * Checks if the constraint is satisfied when all variables are instantiated.
     */
    public boolean isSatisfied(int[] tuple) {
        for (int i = 0; i < vars.length; i++) {
            for (int j = 0; j < i; j++) {
                if (tuple[i] == tuple[j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("AllDifferent({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) sb.append(", ");
            IntDomainVar var = vars[i];
            sb.append(var.pretty());
        }
        sb.append("})");
        return sb.toString();
    }

    //by default, no information is known
    public int getFineDegree(int idx) {
        return vars.length - 1;
    }
}
