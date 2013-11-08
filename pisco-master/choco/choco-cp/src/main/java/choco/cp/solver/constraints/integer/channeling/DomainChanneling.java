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

package choco.cp.solver.constraints.integer.channeling;


import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Constraints that map the boolean assignments variables (bvars) with the standard assignment variables (var).
 * var = i -> bvars[i] = 1
 *
 * @author Xavier Lorca
 * @author Hadrien Cambazard
 * @author Fabien Hermenier
 */
public final class DomainChanneling extends AbstractLargeIntSConstraint {


    /**
     * Number of possible assignments.
     * ie, the number of boolean vars
     */
    private final int dsize;

    /**
     * The last lower bounds of the assignment var.
     */
    private final IStateInt oldinf;

    /**
     * The last upper bounds of the assignment var.
     */
    private final IStateInt oldsup;

    /**
     * Make a new Channeling.
     * Warning : no offset ! the lower bound of x_i should be O !!!!!
     *
     * @param yij         The boolean assignment var for a virtual machine
     * @param xi          the associated assignment var
     * @param environment
     */
    public DomainChanneling(IntDomainVar[] yij, IntDomainVar xi, IEnvironment environment) {
        super(ConstraintEvent.LINEAR, ArrayUtils.append(yij, new IntDomainVar[]{xi}));
        this.dsize = yij.length;
        oldinf = environment.makeInt();
        oldsup = environment.makeInt();
    }


    @Override
    /**
     * For all the binary variables, we catch only awakeOnInst. Otherwise, we catch awakeOnInst, bounds and awakeOnRem.
     */
    public int getFilteredEventMask(int idx) {
        return idx < dsize ? IntVarEvent.INSTINT_MASK : IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK + IntVarEvent.REMVAL_MASK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void awake() throws ContradictionException {
        vars[dsize].updateInf(0, this, false);
        vars[dsize].updateSup(dsize - 1, this, false);
        super.awake();
        //Set oldinf & oldsup equals to the nt bounds of the assignment var
        oldinf.set(vars[dsize].getInf());
        oldsup.set(vars[dsize].getSup());
    }

    /**
     * {@inheritDoc}
     */
    public void propagate() throws ContradictionException {
        int left = Integer.MIN_VALUE;
        int right = left;
        for (int i = 0; i < dsize; i++) {
            if (vars[i].isInstantiatedTo(0)) {
                if (i == right + 1) {
                    right = i;
                } else {
                    vars[dsize].removeInterval(left, right, this, false);
                    left = i;
                    right = i;
                }
//                vars[dsize].removeVal(i, this, false);
            } else if (vars[i].isInstantiatedTo(1)) {
                vars[dsize].instantiate(i, this, false);
                clearBooleanExcept(i);
            } else if (!vars[dsize].canBeInstantiatedTo(i)) {
                clearBoolean(i);
            }
        }
        vars[dsize].removeInterval(left, right, this, false);
        if (vars[dsize].isInstantiated()) {
            final int value = vars[dsize].getVal();
            clearBooleanExcept(value);
            vars[value].instantiate(1, this, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void awakeOnInf(int i) throws ContradictionException {
        clearBoolean(oldinf.get(), vars[i].getInf());
        oldinf.set(vars[i].getInf());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void awakeOnSup(int i) throws ContradictionException {
        clearBoolean(vars[i].getSup() + 1, oldsup.get() + 1);
        oldsup.set(vars[i].getSup());
    }

    private void clearBoolean(int val) throws ContradictionException {
        vars[val].instantiate(0, this, false);
    }


    private void clearBoolean(int begin, int end) throws ContradictionException {
        for (int i = begin; i < end; i++) {
            clearBoolean(i);
        }
    }

    /**
     * Instantiate all the boolean variable to 1 except one.
     *
     * @param val The index of the variable to keep
     * @throws ContradictionException if an error occured
     */
    private void clearBooleanExcept(int val) throws ContradictionException {
        clearBoolean(oldinf.get(), val);
        clearBoolean(val + 1, oldsup.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void awakeOnRem(int idx, int val) throws ContradictionException {
        clearBoolean(val);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void awakeOnInst(int idx) throws ContradictionException {

        //val = the current value
        final int val = vars[idx].getVal();

        if (idx == dsize) {
            //We instantiate the assignment var
            //val = index to keep
            vars[val].instantiate(1, this, false);
            clearBooleanExcept(val);
        } else {
            //We instantiate a boolean var
            if (val == 1) {
                //We report the instantiation to the associated assignment var
                vars[dsize].instantiate(idx, this, false);
                //Next line should be useless ?
                clearBooleanExcept(idx);
            } else {
                vars[dsize].removeVal(idx, this, false);
                if (vars[dsize].isInstantiated()) {
                    vars[vars[dsize].getVal()].instantiate(1, this, false);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSatisfied(int[] tuple) {
        int val = tuple[tuple.length - 1];
        for (int i = 0; i < tuple.length - 1; i++) {
            if (i != val && tuple[i] != 0) {
                return false;
            } else if (i == val && tuple[i] != 1) {
                return false;
            }
        }
        return !(val < 0 || val > tuple.length - 1);
    }


    @Override
    public String pretty() {
        StringBuilder b = new StringBuilder();
        b.append("DomainChanneling ").append(vars[dsize].pretty()).append(" <=> ");
        b.append(StringUtils.pretty(vars, 0, dsize));
        return b.toString();
    }
}
