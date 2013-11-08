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

package choco.cp.model.managers.constraints;

import choco.cp.model.managers.MixedConstraintManager;
import choco.cp.model.managers.RealConstraintManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.*;
import choco.cp.solver.constraints.real.MixedEqXY;
import choco.cp.solver.constraints.real.exp.RealMinus;
import choco.cp.solver.constraints.reified.leaves.bool.*;
import choco.cp.solver.constraints.set.SetEq;
import choco.cp.solver.constraints.set.SetNotEq;
import choco.kernel.common.Constant;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.VariableType;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealExpressionVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.real.RealExp;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.variables.real.RealIntervalConstant;
import choco.kernel.solver.variables.real.RealVar;

import java.util.List;

import static choco.kernel.model.constraints.ConstraintType.*;

/*
 * User:    charles
 * Date:    22 août 2008
 */
public final class EqManager extends MixedConstraintManager {

    private static final int INTINT = 11;
    private static final int INTSET = 12;
    private static final int INTREAL = 13;
    private static final int SETINT = 21;
    private static final int SETSET = 22;
    private static final int REALINT = 31;
    private static final int REALREAL = 33;


    /**
     * Build a constraint for the given solver and "model variables"
     *
     * @param solver
     * @param variables
     * @param parameters : a "hook" to attach any kind of parameters to constraints
     * @param options
     * @return
     */
    public SConstraint makeConstraint(final Solver solver, final Variable[] variables, final Object parameters, final List<String> options) {
        if (solver instanceof CPSolver) {
            if (parameters instanceof ConstraintType) {
                ConstraintType type = (ConstraintType) parameters;
                CPSolver cpsolver = (CPSolver) solver;
                Variable v1 = variables[0];
                Variable v2 = variables[1];
                int ty = VariableUtils.checkType(v1.getVariableType(), v2.getVariableType());
                switch(type){
                    case EQ:
                        switch (ty) {
                            case INTINT:
                                return createIntEq(cpsolver, (IntegerVariable) v1, (IntegerVariable) v2);
                            case SETSET:
                                return new SetEq(cpsolver.getVar((SetVariable) v1), cpsolver.getVar((SetVariable) v2));
                            case REALREAL:
                                return createRealEq(cpsolver, (RealExpressionVariable) v1, (RealExpressionVariable) v2);
                            case INTREAL:
                                return new MixedEqXY(cpsolver.getVar((RealVariable) v2), cpsolver.getVar((IntegerVariable) v1));
                            case REALINT:
                                return new MixedEqXY(cpsolver.getVar((RealVariable) v1), cpsolver.getVar((IntegerVariable) v2));
                            case SETINT:
                                return createIntEq(cpsolver, ((SetVariable) v1).getCard(), (IntegerVariable) v2);
//                            return new SetCard(solver.getVar((SetVariable)v1), solver.getVar((IntegerVariable)v2), true, true);
                            case INTSET:
                                return createIntEq(cpsolver, (IntegerVariable) v1, ((SetVariable) v2).getCard());
//                            return new SetCard(solver.getVar((SetVariable)v2), solver.getVar((IntegerVariable)v1), true, true);
                            default:
                                return null;
                        }
                    case NEQ:
                        switch (ty) {
                            case INTINT:
                                return createIntNeq(cpsolver, (IntegerVariable) v1, (IntegerVariable) v2);
                            case SETSET:
                                return new SetNotEq(cpsolver.getVar((SetVariable) v1), cpsolver.getVar((SetVariable) v2));
                            case INTSET:
                                return createIntNeq(cpsolver, (IntegerVariable) v1, ((SetVariable) v2).getCard());
                            case SETINT:
                                return createIntNeq(cpsolver, ((SetVariable) v1).getCard(), (IntegerVariable) v2);
                            default:
                                return null;
                        }
                    case GEQ:
                        switch (ty) {
                            case INTINT:
                                return createIntGeq(cpsolver, (IntegerVariable) v1, (IntegerVariable) v2);
                            case SETINT:
                                return createIntGeq(cpsolver, ((SetVariable) v1).getCard(), (IntegerVariable) v2);
                            case INTSET:
                                return createIntGeq(cpsolver, (IntegerVariable) v1, ((SetVariable) v2).getCard());
                            case REALREAL:
                                return createRealLeq(cpsolver, (RealExpressionVariable) v2, (RealExpressionVariable) v1);
                            default:
                                return null;
                        }
                    case GT:
                        switch (ty) {
                            case INTINT:
                                return createIntGt(cpsolver, (IntegerVariable) v1, (IntegerVariable) v2);
                            case SETINT:
                                return createIntGt(cpsolver, ((SetVariable) v1).getCard(), (IntegerVariable) v2);
                            case INTSET:
                                return createIntGt(cpsolver, (IntegerVariable) v1, ((SetVariable) v2).getCard());
                            default:
                                return null;
                        }
                    case LEQ:
                        switch (ty) {
                            case INTINT:
                                return createIntLeq(cpsolver, (IntegerVariable) v1, (IntegerVariable) v2);
                            case SETINT:
                                return createIntLeq(cpsolver, ((SetVariable) v1).getCard(), (IntegerVariable) v2);
                            case INTSET:
                                return createIntLeq(cpsolver, (IntegerVariable) v1, ((SetVariable) v2).getCard());
                            case REALREAL:
                                return createRealLeq(cpsolver, (RealExpressionVariable) v1, (RealExpressionVariable) v2);
                            default:
                                return null;
                        }
                    case LT:
                        switch (ty) {
                            case INTINT:
                                return createIntLt(cpsolver, (IntegerVariable) v1, (IntegerVariable) v2);
                            case SETINT:
                                return createIntLt(cpsolver, ((SetVariable) v1).getCard(), (IntegerVariable) v2);
                            case INTSET:
                                return createIntLt(cpsolver, (IntegerVariable) v1, ((SetVariable) v2).getCard());
                            default:
                                return null;
                        }
                    default:
                        return null;
                }
            }
        }

        throw new ModelException("Could not found a constraint manager in " + this.getClass() + " !");
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
    public SConstraint[] makeConstraintAndOpposite(final Solver solver, final Variable[] variables, final Object parameters, final List<String> options) {
        SConstraint c = this.makeConstraint(solver, variables, parameters, options);
        SConstraint opp = c.opposite(solver);
        return new SConstraint[]{c, opp};
    }

    /**
     * Build arithm node from a IntegerExpressionVariable
     *
     * @param solver
     * @param cstrs  constraints (can be null)
     * @param vars   variables
     * @return
     */
    @Override
    public INode makeNode(final Solver solver, final Constraint[] cstrs, final Variable[] vars) {
        ComponentConstraint cc = (ComponentConstraint) cstrs[0];
        if (cc.getParameters() instanceof ConstraintType) {
            ConstraintType type = (ConstraintType) cc.getParameters();
            INode[] nt = new INode[cc.getVariables().length];
            for (int i = 0; i < cc.getVariables().length; i++) {
                IntegerExpressionVariable v = (IntegerExpressionVariable) cc.getVariable(i);
                nt[i] = v.getExpressionManager().makeNode(solver, v.getConstraints(), v.getVariables());
            }
            if (EQ == type) {
                return new EqNode(nt);
            }else if(NEQ == type){
                return new NeqNode(nt);
            }else if(GEQ == type){
                return new GeqNode(nt);
            }else if(LEQ == type){
                return new LeqNode(nt);
            }else if(GT == type){
                return new GtNode(nt);
            }else if(LT == type){
                return new LtNode(nt);
            }
        }
        return null;
    }


    //##################################################################################################################
    //###                                    Integer equalities                                                      ###
    //##################################################################################################################

    SConstraint createIntEq(final CPSolver s, final IntegerVariable v1, final IntegerVariable v2) {
        VariableType tv1 = v1.getVariableType();
        VariableType tv2 = v2.getVariableType();
        int c;
        switch (tv1) {
            case CONSTANT_INTEGER:
                c = ((IntegerConstantVariable) v1).getValue();
                switch (tv2) {
                    case CONSTANT_INTEGER:
                        return (c == ((IntegerConstantVariable) v2).getValue()?
                                Constant.TRUE: Constant.FALSE);
                    case INTEGER:
                        return new EqualXC(s.getVar(v2), c);
                }
            case INTEGER:
                switch (tv2) {
                    case CONSTANT_INTEGER:
                        c = ((IntegerConstantVariable) v2).getValue();
                        return new EqualXC(s.getVar(v1), c);
                    case INTEGER:
                        return new EqualXYC(s.getVar(v1), s.getVar(v2), 0);
                }
        }
        return null;
    }

    //##################################################################################################################
    //###                                       Real equalities                                                      ###
    //##################################################################################################################

    SConstraint createRealEq(final CPSolver s, final RealExpressionVariable v1, final RealExpressionVariable v2) {
        VariableType tv1 = v1.getVariableType();
        VariableType tv2 = v2.getVariableType();
        RealExp t1;
        RealExp t2;
        RealIntervalConstant zero = new RealIntervalConstant(0,0);

        switch (tv1) {
            case CONSTANT_DOUBLE:
                t1 = (RealIntervalConstant)s.getVar(v1);
                switch (tv2) {
                    case CONSTANT_DOUBLE:
                        t2 = ((RealIntervalConstant) s.getVar(v2));
                        return (v1.getLowB() == v2.getLowB()?
                                Constant.TRUE:Constant.FALSE);
                    case REAL:
                        t2 = (RealVar)s.getVar(v2);
                        return s.makeEquation(t2, (RealIntervalConstant)t1);
                    case REAL_EXPRESSION:
                        t2 = ((RealConstraintManager)v2.getConstraintManager()).makeRealExpression(s, v2.getVariables());
                        return s.makeEquation(t2, (RealIntervalConstant)t1);
                }
            case REAL:
                t1 = (RealVar)s.getVar(v1);
                switch (tv2) {
                    case CONSTANT_DOUBLE:
                        t2 = ((RealIntervalConstant) s.getVar(v2));
                        return s.makeEquation(t1, (RealIntervalConstant)t2);
                    case REAL:
                        t2 = (RealVar)s.getVar(v2);
                        return s.makeEquation(new RealMinus(s, t1, t2), zero);
                    case REAL_EXPRESSION:
                        t2 = ((RealConstraintManager)v2.getConstraintManager()).makeRealExpression(s, v2.getVariables());
                        return s.makeEquation(new RealMinus(s, t1, t2), zero);
                }
            case REAL_EXPRESSION:
                t1 = ((RealConstraintManager)v1.getConstraintManager()).makeRealExpression(s, v1.getVariables());
                switch (tv2) {
                    case CONSTANT_DOUBLE:
                        t2 = ((RealIntervalConstant) s.getVar(v2));
                        return s.makeEquation(t1, (RealIntervalConstant)t2);
                    case REAL:
                        t2 = (RealExp)s.getVar(v2);
                        return s.makeEquation(new RealMinus(s, t1, t2), zero);
                    case REAL_EXPRESSION:
                        t2 = ((RealConstraintManager)v2.getConstraintManager()).makeRealExpression(s, v2.getVariables());
                        return s.makeEquation(new RealMinus(s, t1, t2), zero);
                }

        }
        return null;
    }

    //##################################################################################################################
    //###                                    Integer inequalities                                                    ###
    //##################################################################################################################

    SConstraint createIntNeq(final CPSolver s, final IntegerVariable v1, final IntegerVariable v2) {
        VariableType tv1 = v1.getVariableType();
        VariableType tv2 = v2.getVariableType();
        int c;
        switch (tv1) {
            case CONSTANT_INTEGER:
                c = ((IntegerConstantVariable) v1).getValue();
                switch (tv2) {
                    case CONSTANT_INTEGER:
                        return (c != ((IntegerConstantVariable) v2).getValue()?
                                Constant.TRUE:Constant.FALSE);
                    case INTEGER:
                        return new NotEqualXC(s.getVar(v2), c);
                }
            case INTEGER:
                switch (tv2) {
                    case CONSTANT_INTEGER:
                        c = ((IntegerConstantVariable) v2).getValue();
                        return new NotEqualXC(s.getVar(v1), c);
                    case INTEGER:
                    	return new NotEqualXYC(s.getVar(v1), s.getVar(v2), 0);
                }
        }
        return null;
    }


    //##################################################################################################################
    //###                                    Integer GEQ                                                             ###
    //##################################################################################################################

    SConstraint createIntGeq(final CPSolver s, final IntegerVariable v1, final IntegerVariable v2) {
        VariableType tv1 = v1.getVariableType();
        VariableType tv2 = v2.getVariableType();
        int c;
        switch (tv1) {
            case CONSTANT_INTEGER:
                c = ((IntegerConstantVariable) v1).getValue();
                switch (tv2) {
                    case CONSTANT_INTEGER:
                        return (c >= ((IntegerConstantVariable) v2).getValue()?
                                Constant.TRUE:Constant.FALSE);
                    case INTEGER:
                        return new LessOrEqualXC(s.getVar(v2), c);
                }
            case INTEGER:
                switch (tv2) {
                    case CONSTANT_INTEGER:
                        c = ((IntegerConstantVariable) v2).getValue();
                        return new GreaterOrEqualXC(s.getVar(v1), c);
                    case INTEGER:
                        return new GreaterOrEqualXYC(s.getVar(v1), s.getVar(v2), 0);
                }
        }
        return null;
    }

    //##################################################################################################################
    //###                                    Integer GT                                                             ###
    //##################################################################################################################

    SConstraint createIntGt(final CPSolver s, final IntegerVariable v1, final IntegerVariable v2) {
        VariableType tv1 = v1.getVariableType();
        VariableType tv2 = v2.getVariableType();
        int c;
        switch (tv1) {
            case CONSTANT_INTEGER:
                c = ((IntegerConstantVariable) v1).getValue();
                switch (tv2) {
                    case CONSTANT_INTEGER:
                        return (c > ((IntegerConstantVariable) v2).getValue()?
                                Constant.TRUE:Constant.FALSE);
                    case INTEGER:
                        return new LessOrEqualXC(s.getVar(v2), c-1);
                }
            case INTEGER:
                switch (tv2) {
                    case CONSTANT_INTEGER:
                        c = ((IntegerConstantVariable) v2).getValue();
                        return new GreaterOrEqualXC(s.getVar(v1), c+1);
                    case INTEGER:
                        return new GreaterOrEqualXYC(s.getVar(v1), s.getVar(v2), 1);
                }
        }
        return null;
    }

    //##################################################################################################################
    //###                                    Integer LEQ                                                             ###
    //##################################################################################################################
    SConstraint createIntLeq(final CPSolver s, final IntegerVariable v1, final IntegerVariable v2) {
        VariableType tv1 = v1.getVariableType();
        VariableType tv2 = v2.getVariableType();
        int c;
        switch (tv1) {
            case CONSTANT_INTEGER:
                c = ((IntegerConstantVariable) v1).getValue();
                switch (tv2) {
                    case CONSTANT_INTEGER:
                        return (c <= ((IntegerConstantVariable) v2).getValue()?
                                Constant.TRUE:Constant.FALSE);
                    case INTEGER:
                        return new GreaterOrEqualXC(s.getVar(v2), c);
                }
            case INTEGER:
                switch (tv2) {
                    case CONSTANT_INTEGER:
                        c = ((IntegerConstantVariable) v2).getValue();
                        return new LessOrEqualXC(s.getVar(v1), c);
                    case INTEGER:
                        return new GreaterOrEqualXYC(s.getVar(v2), s.getVar(v1), 0);
                }
        }
        return null;
    }

    //##################################################################################################################
    //###                                       Real LEQ                                                             ###
    //##################################################################################################################

    SConstraint createRealLeq(final CPSolver s, final RealExpressionVariable v1, final RealExpressionVariable v2) {
        VariableType tv1 = v1.getVariableType();
        VariableType tv2 = v2.getVariableType();
        RealExp t1;
        RealExp t2;
        RealIntervalConstant cst;
        Double dPOS = Double.POSITIVE_INFINITY;
        Double dNEG = Double.NEGATIVE_INFINITY;
        RealIntervalConstant dINF = new RealIntervalConstant(dNEG,0.0);

        switch (tv1) {
            case CONSTANT_DOUBLE:
                cst = new RealIntervalConstant(v1.getLowB(),dPOS);
                switch (tv2) {
                    case CONSTANT_DOUBLE:
                        return (v1.getUppB() <= v2.getUppB()?
                                Constant.TRUE:Constant.FALSE);
                    case REAL:
                        t2 = (RealVar)s.getVar(v2);
                        return s.makeEquation(t2, cst);
                    case REAL_EXPRESSION:
                        t2 = ((RealConstraintManager)v2.getConstraintManager()).makeRealExpression(s, v2.getVariables());
                        return s.makeEquation(t2, cst);
                }
            case REAL:
                t1 = (RealVar)s.getVar(v1);
                switch (tv2) {
                    case CONSTANT_DOUBLE:
                        cst = new RealIntervalConstant(dNEG, v2.getLowB());
                        return s.makeEquation(t1, cst);
                    case REAL:
                        t2 = (RealVar)s.getVar(v2);
                        return s.makeEquation(new RealMinus(s, t1, t2), dINF);
                    case REAL_EXPRESSION:
                        t2 = ((RealConstraintManager)v2.getConstraintManager()).makeRealExpression(s, v2.getVariables());
                        return s.makeEquation(new RealMinus(s, t1, t2), dINF);
                }
            case REAL_EXPRESSION:
                t1 = ((RealConstraintManager)v1.getConstraintManager()).makeRealExpression(s, v1.getVariables());
                switch (tv2) {
                    case CONSTANT_DOUBLE:
                        cst = new RealIntervalConstant(dNEG, v2.getLowB());
                        return s.makeEquation(t1, cst);
                    case REAL:
                        t2 = (RealExp)s.getVar(v2);
                        return s.makeEquation(new RealMinus(s, t1, t2), dINF);
                    case REAL_EXPRESSION:
                        t2 = ((RealConstraintManager)v2.getConstraintManager()).makeRealExpression(s, v2.getVariables());
                        return s.makeEquation(new RealMinus(s, t1, t2), dINF);
                }

        }
        return null;
    }

    //##################################################################################################################
    //###                                    Integer LT                                                             ###
    //##################################################################################################################

    SConstraint createIntLt(final CPSolver s, final IntegerVariable v1, final IntegerVariable v2) {
        VariableType tv1 = v1.getVariableType();
        VariableType tv2 = v2.getVariableType();
        switch (tv1) {
            case CONSTANT_INTEGER:
                int c1 = ((IntegerConstantVariable) v1).getValue();
                switch (tv2) {
                    case CONSTANT_INTEGER:
                        return (c1 < ((IntegerConstantVariable) v2).getValue()?
                                Constant.TRUE:Constant.FALSE);
                    case INTEGER:
                        return new GreaterOrEqualXC(s.getVar(v2), c1 + 1);
                }
            case INTEGER:
                switch (tv2) {
                    case CONSTANT_INTEGER:
                        int c2 = ((IntegerConstantVariable) v2).getValue();
                        return new LessOrEqualXC(s.getVar(v1), c2 - 1);
                    case INTEGER:
                        return new GreaterOrEqualXYC(s.getVar(v2), s.getVar(v1), 1);
                }
        }
        return null;
    }

}
