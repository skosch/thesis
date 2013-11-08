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

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntHashSet;

/**
 * User : cprudhom, fhermenie
 * Mail : cprudhom(a)emn.fr
 * Date : 22 fevr. 2010
 * Since : Choco 2.1.1
 * <p/>
 * GCCAT:
 * NVAR is the number of variables of the collection VARIABLES that take their value in VALUES.
 * <br/><a href="http://www.emn.fr/x-info/sdemasse/gccat/Camong.html">gccat among</a>
 * <br/>
 * Propagator :
 * C. Bessiere, E. Hebrard, B. Hnich, Z. Kiziltan, T. Walsh,
 * Among, common and disjoint Constraints
 * CP-2005
 */
public final class AmongGAC extends AbstractLargeIntSConstraint {

    private final int[] values;
    private final int nb_vars;
    private final IStateBitSet both;
    private final IStateInt LB;
    private final IStateInt UB;

    private TIntHashSet setValues;

    private IStateInt[] occs;
    private IStateInt[] sizes;

    /**
     * Constructs a constraint with the specified priority.
     * <p/>
     * The last variables of {@code vars} is the counter.
     *
     * @param vars        (n-1) variables + N as counter
     * @param values      counted values
     * @param environment
     */
    public AmongGAC(IntDomainVar[] vars, int[] values, IEnvironment environment) {
        super(ConstraintEvent.QUADRATIC, vars);
        nb_vars = vars.length - 1;
        this.values = values;
        both = environment.makeBitSet(nb_vars);
        LB = environment.makeInt(0);
        UB = environment.makeInt(0);
        this.setValues = new TIntHashSet(values);
        this.occs = new IStateInt[nb_vars];
        this.sizes = new IStateInt[nb_vars];
        for (int i = 0; i < nb_vars; i++) {
            occs[i] = environment.makeInt(0);
            sizes[i] = environment.makeInt(0);
        }
    }

    @Override
    public int getFilteredEventMask(int idx) {
        if (idx == nb_vars) {
            return IntVarEvent.INSTINT_MASK + IntVarEvent.INCINF_MASK + IntVarEvent.DECSUP_MASK;
        }
        return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
    }

    protected void init() {
        LB.set(0);
        UB.set(0);
        both.clear();
        for (int i = 0; i < nb_vars; i++) {
            this.occs[i].set(0);
        }
    }

    /**
     * <i>Propagation:</i>
     * Propagating the constraint for the very first time until local
     * consistency is reached.
     *
     * @throws choco.kernel.solver.ContradictionException
     *          contradiction exception
     */
    @Override
    public void awake() throws ContradictionException {
        this.init();
        int lb = 0;
        int ub = nb_vars;
        for (int i = 0; i < nb_vars; i++) {
            IntDomainVar var = vars[i];
            int nb = 0;
            for (int value : values) {
                nb += (var.canBeInstantiatedTo(value) ? 1 : 0);
            }
            occs[i].set(nb);
            int size = var.getDomainSize();
            sizes[i].set(size);
            if (nb == size) {
                lb++;
            } else if (nb == 0) {

                ub--;
            } else if (nb > 0) {
                both.set(i, true);
            }
        }
        LB.set(lb);
        UB.set(ub);

        filter();
    }

    private void filter() throws ContradictionException {
        int lb = LB.get();
        int ub = UB.get();
        vars[nb_vars].updateInf(lb, this, false);
        vars[nb_vars].updateSup(ub, this, false);

        int min = Math.max(vars[nb_vars].getInf(), lb);
        int max = Math.min(vars[nb_vars].getSup(), ub);

        if (max < min) this.fail();

        if (lb == min && lb == max) {
            removeOnlyValues();
            setEntailed();
        }

        if (ub == min && ub == max) {
            removeButValues();
            setEntailed();
        }
    }


    /**
     * <i>Propagation:</i>
     * Propagating the constraint until local consistency is reached.
     *
     * @throws choco.kernel.solver.ContradictionException
     *          contradiction exception
     */
    @Override
    public void propagate() throws ContradictionException {
        filter();
    }


    /**
     * Default propagation on instantiation: full constraint re-propagation.
     */
    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        if (idx == nb_vars) {
            filter();
        } else {
            if (both.get(idx)) {
                IntDomainVar var = vars[idx];
                int val = var.getVal();
                if (setValues.contains(val)) {
                    LB.add(1);
                    both.set(idx, false);
                    filter();
                } else {
                    UB.add(-1);
                    both.set(idx, false);
                    filter();
                }
            }
        }
    }

    /**
     * Default propagation on one value removal: propagation on domain revision.
     */
    @Override
    public void awakeOnRem(int varIdx, int val) throws ContradictionException {
        if (varIdx < nb_vars) {
            if (both.get(varIdx)) {
                if (setValues.contains(val)) {
                    occs[varIdx].add(-1);
                }
                sizes[varIdx].add(-1);
                IntDomainVar var = vars[varIdx];
                int nb = occs[varIdx].get();
                if (nb == sizes[varIdx].get()) {  //Can only be instantiated to a value in the group
                    LB.add(1);
                    both.set(varIdx, false);
                    filter();
                } else if (nb == 0) { //No value in the group
                    UB.add(-1);
                    both.set(varIdx, false);
                    filter();
                }
            }
        }
    }

    /**
     * Default propagation on improved lower bound: propagation on domain revision.
     */
    @Override
    public void awakeOnInf(int varIdx) throws ContradictionException {
        if (varIdx == nb_vars) {
            filter();
        }
    }

    /**
     * Default propagation on improved upper bound: propagation on domain revision.
     */
    @Override
    public void awakeOnSup(int varIdx) throws ContradictionException {
        if (varIdx == nb_vars) {
            filter();
        }
    }

    /**
     * Remove from {@code v} every values contained in {@code values}.
     *
     * @throws ContradictionException if contradiction occurs.
     */
    private void removeOnlyValues() throws ContradictionException {
        int left, right;
        for (int i = both.nextSetBit(0); i >= 0; i = both.nextSetBit(i + 1)) {
            IntDomainVar v = vars[i];
            left = right = Integer.MIN_VALUE;
            for (int value : values) {
                /*
                USELESS => will be entailed just after
                if (setValues.contains(value)) {
                    occs[i].add(-1);
                    sizes[i].add(-1);
                }*/
                if (value == right + 1) {
                    right = value;
                } else {
                    v.removeInterval(left, right, this, false);
                    left = right = value;
                }
//                v.removeVal(value, this, false);
            }
            v.removeInterval(left, right, this, false);
        }
    }

    /**
     * Remove from {@code v} each value but {@code values}.
     *
     * @throws ContradictionException if contradiction occurs.
     */
    private void removeButValues() throws ContradictionException {
        int left, right;
        for (int i = both.nextSetBit(0); i >= 0; i = both.nextSetBit(i + 1)) {
            IntDomainVar v = vars[i];
            DisposableIntIterator it = v.getDomain().getIterator();
            left = right = Integer.MIN_VALUE;
            while (it.hasNext()) {
                int value = it.next();
                if (!setValues.contains(value)) {
                    if (value == right + 1) {
                        right = value;
                    } else {
                        v.removeInterval(left, right, this, false);
                        left = right = value;
                    }
//                    v.removeVal(val, this, false);
                }
            }
            v.removeInterval(left, right, this, false);
            it.dispose();
        }
    }

    @Override
    public String pretty() {
        StringBuffer sb = new StringBuffer("AMONG(");
        sb.append("[");
        for (int i = 0; i < nb_vars; i++) {
            if (i > 0) sb.append(",");
            sb.append(vars[i].pretty());
        }
        sb.append("],{");
        StringUtils.pretty(values);
        sb.append("},");
        sb.append(vars[nb_vars].pretty()).append(")");
        return sb.toString();
    }

    /**
     * Default implementation of the isSatisfied by
     * delegating to the isSatisfied(int[] tuple)
     *
     * @return
     */
    @Override
    public boolean isSatisfied() {
        if (isCompletelyInstantiated()) {
            int nb = 0;
            for (int i = 0; i < nb_vars; i++) {
                if (setValues.contains(vars[i].getVal())) {
                    nb++;
                }
            }
            return vars[nb_vars].getVal() == nb;
        }
        return false;
    }

    /**
     * TEMPORARY: if not overriden by the constraint, throws an error
     * to avoid bug using reified constraints in constraints
     * that have not been changed to fulfill this api yet !
     *
     * @param tuple
     * @return
     */
    @Override
    public boolean isSatisfied(int[] tuple) {
        int nb = 0;
        for (int i = 0; i < nb_vars; i++) {
            if (setValues.contains(tuple[i])) {
                nb++;
            }
        }
        return tuple[nb_vars] == nb;
    }
}
