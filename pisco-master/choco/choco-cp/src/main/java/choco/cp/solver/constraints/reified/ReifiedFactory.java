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

package choco.cp.solver.constraints.reified;

import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 20 mai 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public class ReifiedFactory {

    private ReifiedFactory() {
    }

    /**
     * Following the type of constraints, create a reified constraint.
     *
     * @param bool   boolean variable for reification
     * @param cons   a constraint
     * @param solver the solver
     * @return a SConstraint
     * @throws UnsupportedOperationException when an expression constraint is in the scope.
     */
    public static SConstraint builder(IntDomainVar bool, SConstraint cons, Solver s) {
        return builder(bool, cons, cons.opposite(s), s);
    }


    /**
     * Following the type of constraints, create a reified constraint.
     *
     * @param bool    boolean variable for reification
     * @param cons    a constraint
     * @param oppcons the opposite constraint of {@code cons}
     * @param solver
     * @return a SConstraint
     * @throws UnsupportedOperationException when an expression constraint is in the scope.
     */
    public static SConstraint builder(IntDomainVar bool, SConstraint cons, SConstraint oppcons, final Solver solver) {
        SConstraintType c_int = cons.getConstraintType();
        SConstraintType oc_int = oppcons.getConstraintType();
//        if (!c_int.canBeReified() || !oc_int.canBeReified()) {
//            throw new UnsupportedOperationException(MessageFormat.format("{0} or {1} can not be reified", cons.pretty(),
//                    oppcons.pretty()));
//        }
        SConstraintType globalType = merge(c_int, oc_int);
        switch (globalType) {
            case INTEGER:
                return new ReifiedIntSConstraint(bool, (AbstractIntSConstraint) cons, (AbstractIntSConstraint) oppcons);
            case EXPRESSION:
                ExpressionSConstraint ec = (ExpressionSConstraint) cons;
                ExpressionSConstraint oec = (ExpressionSConstraint) oppcons;
                IntDomainVar vec = ec.expr.extractResult(solver);
                IntDomainVar voec = oec.expr.extractResult(solver);
                solver.post(solver.neq(vec, voec));
                return solver.eq(bool, vec);
            default:
                return new ReifiedAllSConstraint(bool, (AbstractSConstraint) cons, (AbstractSConstraint) oppcons);

        }
    }

    /**
     * Scan and return the merged {@link choco.kernel.solver.constraints.SConstraintType} of the two constraints
     * {@code c_int} and {@code oc_int}.
     *
     * @param c_int  a constraint
     * @param oc_int another constraint
     * @return the type of the both constraint
     */
    private static SConstraintType merge(final SConstraintType c_int, final SConstraintType oc_int) {
        if (c_int.equals(oc_int)) {
            return c_int;
        } else {
            if (SConstraintType.EXPRESSION.equals(c_int)
                    || SConstraintType.EXPRESSION.equals(oc_int)) {
                return SConstraintType.EXPRESSION;
            } else {
                return SConstraintType.MIXED;
            }
        }
    }

}
