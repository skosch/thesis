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

package choco.cp.solver.preprocessor;

import choco.Choco;
import choco.cp.common.util.preprocessor.AbstractAdvancedDetector;
import choco.cp.model.CPModel;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;

import java.util.Iterator;

/**
 * Simple symetry detection.
 */
public class SymetryBreakingModelDetector extends AbstractAdvancedDetector {

    private IntegerVariable[] maxclique = null;

    public SymetryBreakingModelDetector(final CPModel model) {
        super(model);
    }

    public void setMaxClique(IntegerVariable[] clique) {
        if (maxclique == null || maxclique.length < clique.length) {
            maxclique = clique;
        }
    }

    /**
     * Break symetries in graph coloring by instantiating
     * the largest clique. Conditions are :
     * - a unique domain and only difference constraints
     */
    @Override
    public final void apply() {
        if (maxclique != null && checkOnlyOneDomain(model) && checkOnlyDiff(model)) {
            DisposableIntIterator it = model.getIntVar(0).getDomainIterator();
            for (int i = 0; i < maxclique.length && it.hasNext(); i++) {
                model.addConstraint(Choco.eq(maxclique[i], it.next()));
            }
            it.dispose();
        }
    }

    /**
     * Are all domains identical ?
     * @param m the cpmodel
     * @return boolean
     */
    private static boolean checkOnlyOneDomain(CPModel m) {
        Iterator<IntegerVariable> it = m.getIntVarIterator();
        if (it.hasNext()) {
            IntegerVariable v = it.next();
            int lb = v.getLowB();
            int ub = v.getUppB();
            if (v.getValues() != null)
                return false;
            while (it.hasNext()) {
                IntegerVariable v2 = it.next();
                if (v2.getLowB() != lb ||
                    v2.getUppB() != ub ||
                    v2.getValues() != null)
                return false;
            }
        }
        return true;
    }

    /**
     * Check is only difference constraints have been
     * posted to the solver
     * @param m the cp model
     * @return boolean
     */
    private static boolean checkOnlyDiff(CPModel m) {
        Iterator<Constraint> it = m.getConstraintIterator();
        for (; it.hasNext();) {
            Constraint ct =  it.next();
            if (ct.getConstraintType() != ConstraintType.ALLDIFFERENT &&
                (ct.getConstraintType() != ConstraintType.NEQ ||
                isComplexNeq(ct))) {
                return false;
            }

        }
        return true;
    }

    /**
     * Check wether a constraint is a neq constraint and is only made of IntegerVariable or not
     * @param ct the constraint
     * @return false if it is a simple neq constraint
     */
    private static boolean isComplexNeq(Constraint ct) {
        if(ct.getConstraintType().equals(ConstraintType.NEQ)){
            Iterator<Variable> it = ct.getVariableIterator();
            while(it.hasNext()){
                if(!(it.next() instanceof IntegerVariable)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Fake Symetry detector, do not do anything
     */
    public static final class EmptySymetryBreakingModelDetector extends SymetryBreakingModelDetector{

        public EmptySymetryBreakingModelDetector(final CPModel model) {
            super(model);
        }

        @Override
        public void setMaxClique(final IntegerVariable[] clique) {
            //nothing to do
        }
    }
}
