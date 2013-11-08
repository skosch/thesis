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

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.AbstractUnIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntArrayList;

/**
 * User : cprudhom
 * Mail : cprudhom(a)emn.fr
 * Date : 22 févr. 2010
 * Since : Choco 2.1.1
 */
public final class MemberEnum extends AbstractUnIntSConstraint {


    final TIntArrayList values;

    public MemberEnum(final IntDomainVar v0, final int[] values) {
        super(v0);
        this.values = new TIntArrayList(values);
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
        final DisposableIntIterator iterator = v0.getDomain().getIterator();
        try {
            int left = Integer.MIN_VALUE;
            int right = left;
            boolean rall = true;
            while (iterator.hasNext()) {
                final int val = iterator.next();
                if (!values.contains(val)) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        rall &= v0.removeInterval(left, right, this, false);
                        left = right = val;
                    }
//                    v0.removeVal(val, this, false);
                }
            }
            rall &= v0.removeInterval(left, right, this, false);
            if (rall) {
                this.setEntailed();
            }
        } finally {
            iterator.dispose();
        }
    }


    /**
     * Get the opposite constraint
     *
     * @return the opposite constraint  @param solver
     */
    @Override
    public AbstractSConstraint<IntDomainVar> opposite(final Solver solver) {
        return new NotMemberEnum(v0, values.toNativeArray());
    }

    @Override
    public String pretty() {
        final StringBuilder sb = new StringBuilder("MEMBER(");
        sb.append(v0.pretty()).append(",{");
        sb.append(StringUtils.pretty(values.toNativeArray()));
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
    public boolean isSatisfied(final int[] tuple) {
        return values.contains(tuple[0]);
    }

    /**
     * Default implementation of the isSatisfied by
     * delegating to the isSatisfied(int[] tuple)
     *
     * @return
     */
    @Override
    public boolean isSatisfied() {
        final DisposableIntIterator it = v0.getDomain().getIterator();
        try {
            while (it.hasNext()) {
                if (!values.contains(it.next())) {
                    return false;
                }
            }
            return true;
        } finally {
            it.dispose();
        }
    }

    /**
     * Indicates if the constraint is entailed, from now on will be always satisfied
     *
     * @return wether the constraint is entailed
     */
    @Override
    public Boolean isEntailed() {
        final DisposableIntIterator it = v0.getDomain().getIterator();
        int nb = 0;
        while (it.hasNext()) {
            final int val = it.next();
            if (values.contains(val)) {
                nb++;
            }
        }
        it.dispose();
        if (nb == 0) return false;
        else if (nb == v0.getDomainSize()) return true;
        return null;

    }
}
