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

import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.AbstractUnIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * User : cprudhom
 * Mail : cprudhom(a)emn.fr
 * Date : 22 févr. 2010
 * Since : Choco 2.1.1
 */
public final class NotMemberEnum extends AbstractUnIntSConstraint {


    final int[] values;

    public NotMemberEnum(IntDomainVar v0, int[] values) {
        super(v0);
        this.values = values;
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
        int left = Integer.MIN_VALUE;
        int right = left;
        boolean rall = true;
        for (int val : values) {
            if (val == right + 1) {
                right = val;
            } else {
                rall &= v0.removeInterval(left, right, this, false);
                left = val;
                right = val;
            }
//            v0.removeVal(val, this, false);
        }
        rall &= v0.removeInterval(left, right, this, false);
        if (rall) {
            this.setEntailed();
        }
    }


    /**
     * Get the opposite constraint
     *
     * @return the opposite constraint  @param solver
     */
    @Override
    public AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
        return new MemberEnum(v0, values);
    }

    @Override
    public String pretty() {
        StringBuffer sb = new StringBuffer("DISJOINT(");
        sb.append(v0.pretty()).append(",{");
        StringUtils.pretty(values);
        sb.append("})");
        return sb.toString();
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
        for (int val : values) {
            if (tuple[0] == val) {
                return false;
            }
        }
        return true;
    }

    /**
     * Default implementation of the isSatisfied by
     * delegating to the isSatisfied(int[] tuple)
     *
     * @return
     */
    @Override
    public boolean isSatisfied() {
        for (int val : values) {
            if (v0.canBeInstantiatedTo(val)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Indicates if the constraint is entailed, from now on will be always satisfied
     *
     * @return wether the constraint is entailed
     */
    @Override
    public Boolean isEntailed() {
        int nb = 0;
        for (int val : values) {
            if (v0.canBeInstantiatedTo(val)) {
                nb++;
            }
        }
        if (nb == 0) return true;
        else if (nb == v0.getDomainSize()) return false;
        return null;
    }
}