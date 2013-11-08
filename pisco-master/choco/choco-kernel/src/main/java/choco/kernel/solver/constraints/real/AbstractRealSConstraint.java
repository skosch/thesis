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

package choco.kernel.solver.constraints.real;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.propagation.listener.RealPropagator;
import choco.kernel.solver.variables.real.RealVar;

/**
 * User : cprudhom
 * Mail : cprudhom(a)emn.fr
 * Date : 15 févr. 2010
 * Since : Choco 2.1.1
 */
public abstract class AbstractRealSConstraint extends AbstractSConstraint<RealVar> implements RealPropagator {

    /**
     * Constructs a constraint with the specified priority.
     *
     * @param priority The wished priority.
     */
    protected AbstractRealSConstraint(int priority, RealVar[] vars) {
        super(priority, vars);
    }

    /**
     * Constraucts a constraint with the priority 0.
     */
    protected AbstractRealSConstraint(RealVar[] vars) {
        super(vars);
    }

    /**
     * Default propagation on improved lower bound: propagation on domain revision.
     */
    @Override
    public void awakeOnInf(int idx) throws ContradictionException {
        this.constAwake(false);
    }

    /**
     * Default propagation on improved upper bound: propagation on domain revision.
     */
    @Override
    public void awakeOnSup(int idx) throws ContradictionException {
        this.constAwake(false);
    }

    @Override
    public SConstraintType getConstraintType() {
        return SConstraintType.REAL;
    }

}
