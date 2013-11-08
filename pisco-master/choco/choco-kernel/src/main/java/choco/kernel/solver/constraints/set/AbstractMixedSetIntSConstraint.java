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

package choco.kernel.solver.constraints.set;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.propagation.listener.IntPropagator;
import choco.kernel.solver.propagation.listener.SetPropagator;
import choco.kernel.solver.variables.Var;


/**
 * A class for mixed set and int constraint. It refers to both interface
 * SetConstraint and IntConstraint and implements a default behaviour
 * for all events awakeOn...
 */
public abstract class AbstractMixedSetIntSConstraint extends AbstractSConstraint<Var> implements SetPropagator, IntPropagator {

    /**
     * Constructs a mixed set-int constraint
     *
     * @param vars list of variables
     */
    protected AbstractMixedSetIntSConstraint(Var[] vars) {
        super(vars);
    }


    /**
     * The default implementation of propagation when a variable has been modified
     * consists in iterating all values that have been removed (the delta domain)
     * and propagate them one after another, incrementally.
     *
     * @param idx
     * @throws choco.kernel.solver.ContradictionException
     *
     */
    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
        if (deltaDomain != null) {
            try {
                for (; deltaDomain.hasNext(); ) {
                    int val = deltaDomain.next();
                    awakeOnRem(idx, val);
                }
            } finally {
                deltaDomain.dispose();
            }
        }
    }

    public void awakeOnBounds(int varIndex) throws ContradictionException {
        awakeOnInf(varIndex);
        awakeOnSup(varIndex);
    }


    public void awakeOnKer(int varIdx, int x) throws ContradictionException {
        propagate();
    }

    public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
        propagate();
    }

    public void awakeOnInst(int varIdx) throws ContradictionException {
        propagate();
    }

    /**
     * Default propagation on one value removal: propagation on domain revision.
     */

    public void awakeOnRem(int varIdx, int val) throws ContradictionException {
        this.constAwake(false);
    }

    /**
     * Default propagation on improved lower bound: propagation on domain revision.
     */

    public void awakeOnInf(int varIdx) throws ContradictionException {
        this.constAwake(false);
    }

    /**
     * Default propagation on improved upper bound: propagation on domain revision.
     */

    public void awakeOnSup(int varIdx) throws ContradictionException {
        this.constAwake(false);
    }

    public void awakeOnEnvRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
        if (deltaDomain != null) {
            for (; deltaDomain.hasNext(); ) {
                int val = deltaDomain.next();
                awakeOnEnv(idx, val);
            }
        } else {
            throw new SolverException("deltaDomain should not be null in awakeOnEnvRemovals");
        }
    }

    public void awakeOnkerAdditions(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
        if (deltaDomain != null) {
            for (; deltaDomain.hasNext(); ) {
                int val = deltaDomain.next();
                awakeOnKer(idx, val);
            }
        } else {
            throw new SolverException("deltaDomain should not be null in awakeOnKerAdditions");
        }
    }


    public boolean isSatisfied(int[] tuple) {
        throw new UnsupportedOperationException(this + " needs to implement isSatisfied(int[] tuple) to be embedded in reified constraints");
    }

    @Override
    public SConstraintType getConstraintType() {
        return SConstraintType.MIXED;
    }

    /**
     * tests if the constraint is consistent with respect to the current state of domains
     *
     * @return wether the constraint is consistent
     */
    @Override
    public boolean isConsistent() {
        return (isEntailed() == Boolean.TRUE);
    }
}
