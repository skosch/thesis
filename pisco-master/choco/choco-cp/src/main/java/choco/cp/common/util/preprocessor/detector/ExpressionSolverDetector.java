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

package choco.cp.common.util.preprocessor.detector;

import choco.Choco;
import choco.cp.common.util.preprocessor.AbstractAdvancedDetector;
import choco.cp.common.util.preprocessor.ExpressionTools;
import choco.cp.model.CPModel;
import choco.cp.solver.constraints.reified.ExpressionSConstraint;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.kernel.model.constraints.*;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.constraints.SConstraint;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 2 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 */
public class ExpressionSolverDetector extends AbstractAdvancedDetector {

    private final PreProcessCPSolver ppsolver;

    public ExpressionSolverDetector(final CPModel model, final PreProcessCPSolver solver) {
        super(model);
        ppsolver = solver;
    }

    /**
     * Apply the detection defined within the detector.
     */
    @Override
    public void apply() {
        final Iterator<Constraint> it = model.getConstraintIterator();
        final List<Constraint> neqToAdd = new LinkedList<Constraint>();
        while (it.hasNext()) {
            final Constraint ic = it.next();
            if (!ppsolver.contains(ic) && isAValidExpression(ic)) {
                final ExpressionSConstraint c = new ExpressionSConstraint(ppsolver.getMod2Sol().buildNode(ic));
                c.setScope(ppsolver);
                ppsolver.getMod2Sol().storeExpressionSConstraint(ic, c);
                final SConstraint intensional = ExpressionTools.getIntentionalConstraint(c, ppsolver);
                if (intensional != null) {
                    c.setKnownIntensionalConstraint(intensional);
                } else {
                    if (ExpressionTools.encompassDiff(c)) {
                       final IntegerVariable[] vars = ((AbstractConstraint) ic).getIntVariableScope();
                       neqToAdd.add(Choco.neq(vars[0],vars[1]));
                    }
                }
            }
        }
        for (final Constraint aNeqToAdd : neqToAdd) {
            model.addConstraint(aNeqToAdd);
        }
    }

    private static boolean isAValidExpression(final Constraint ic) {
        return ic instanceof MetaConstraint ||
                (ic instanceof ComponentConstraint &&
                 (ic.getConstraintType() == ConstraintType.EQ ||
                 ic.getConstraintType() == ConstraintType.NEQ ||
                 ic.getConstraintType() == ConstraintType.LEQ ||
                 ic.getConstraintType() == ConstraintType.GEQ ||
                 ic.getConstraintType() == ConstraintType.GT ||
                 ic.getConstraintType() == ConstraintType.LT));
    }
}
