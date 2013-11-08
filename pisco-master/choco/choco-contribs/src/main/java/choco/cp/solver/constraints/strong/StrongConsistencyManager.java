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

package choco.cp.solver.constraints.strong;

import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.constraints.reified.ExpressionSConstraint;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.VariableType;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.constraints.reified.BoolNode;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class StrongConsistencyManager extends IntConstraintManager {

    @Override
    public int[] getFavoriteDomains(List<String> options) {
        // TODO Auto-generated method stub
        return new int[0];
    }

    @Override
    public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables,
            Object parameters, List<String> options) {
        final Class<? extends AbstractStrongConsistency> scImplementation;
        final Constraint[] modelConstraints;
        try {
            final Object[] params = (Object[]) ((Object[]) parameters)[0];
            scImplementation = (Class<? extends AbstractStrongConsistency>) params[0];
            modelConstraints = (Constraint[]) params[1];
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Please give an array { Class<? extends AbstractStrongConsistency>, SConstraints[] } as parameter, got "
                            + parameters, e);
        }

        // Récupération des variables solveur
        final IntDomainVar[] solverVariables = new IntDomainVar[variables.length];

        for (int i = variables.length; --i >= 0;) {
            solverVariables[i] = solver.getVar(variables[i]);
        }

        // Création des contraintes solveur
        final Collection<ISpecializedConstraint> constraints = new ArrayList<ISpecializedConstraint>(
                modelConstraints.length);

        for (int i = modelConstraints.length; --i >= 0;) {
            final ComponentConstraint mc = (ComponentConstraint) modelConstraints[i];

            if (allSimpleVariable(mc.getVariables())) {
                final SConstraint solverConstraint = mc.getConstraintManager().makeConstraint(
                        solver, mc.getVariables(), mc.getParameters(),
                        mc.getOptions());
                if (solverConstraint instanceof ISpecializedConstraint) {
                    constraints.add((ISpecializedConstraint) solverConstraint);
                } else {
                    try {
                        constraints.add(new Adapter(
                                (AbstractIntSConstraint) solverConstraint));
                    } catch (Exception e) {
                        throw new IllegalArgumentException(solverConstraint
                                + " is not treatable", e);
                    }
                }
            } else {
                final AbstractIntSConstraint solverConstraint = (AbstractIntSConstraint) createMetaConstraint(
                        mc, solver).getExtensionnal(solver);
                constraints.add(new Adapter(solverConstraint));
            }

        }

        // Création de la contrainte globale
        final Constructor<? extends AbstractStrongConsistency> constructor;
        try {
            constructor = scImplementation.getConstructor(IntDomainVar[].class,
                    ISpecializedConstraint[].class, Set.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Could not find suitable constructor for "
                            + scImplementation, e);
        }

        try {
            return constructor.newInstance(solverVariables, constraints
                    .toArray(new ISpecializedConstraint[constraints.size()]),
                    options);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not instanciate "
                    + scImplementation, e);
        }
    }

    /**
     * Build a constraint and its opposite for the given solver and "model variables"
     *
     * @param solver
     * @param variables
     * @param parameters
     * @param options
     * @return array of 2 SConstraint object, the constraint and its opposite
     */
    @Override
    public SConstraint[] makeConstraintAndOpposite(Solver solver, IntegerVariable[] variables, Object parameters, List<String> options) {
        SConstraint c = makeConstraint(solver, variables, parameters, options);
        SConstraint opp = c.opposite(solver);
        return new SConstraint[]{c, opp};
    }

    protected ExpressionSConstraint createMetaConstraint(Constraint ic,
            Solver cpsolver) {
        ExpressionSConstraint c = new ExpressionSConstraint(buildBoolNode(ic,
                cpsolver));
        c.setDecomposeExp(false);
        c.setScope(cpsolver);
        // important step to deal properly with linear equation
        // SConstraint intensional = expDetect.getScalarConstraint(c, cpsolver);
        // if (intensional != null) {
        // return intensional;
        // } else {
        return c;
        // }
    }

    protected BoolNode buildBoolNode(Constraint ic, Solver cpsolver) {
        IntegerExpressionVariable[] vars = null;
        if (ic.getNbVars() > 0) {
            vars = new IntegerExpressionVariable[ic.getNbVars()];
            for (int i = 0; i < ic.getVariables().length; i++) {
                vars[i] = (IntegerExpressionVariable) ic.getVariables()[i];
            }
        }
        return (BoolNode) ic.getExpressionManager().makeNode(cpsolver,
                new Constraint[] { ic }, vars);
    }

    /**
     * Check wether a pool of variables is composed of simple variables or not
     * 
     * @param vars
     *            pool of variables
     * @return true if only simple variable,
     */
    private boolean allSimpleVariable(Variable[] vars) {
        if (vars == null)
            return true;
        for (int i = 0; i < vars.length; i++) {
            Variable v = vars[i];
            VariableType type = v.getVariableType();
            if (type == VariableType.INTEGER_EXPRESSION) {
                return false;
            }
        }
        return true;
    }

    @Override
    public INode makeNode(Solver solver, Constraint[] cstrs,
            Variable[] vars) {
        // TODO Auto-generated method stub
        return null;
    }

}
