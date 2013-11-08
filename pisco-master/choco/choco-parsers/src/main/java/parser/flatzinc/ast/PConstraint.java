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

package parser.flatzinc.ast;

import static choco.Choco.*;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.model.variables.set.SetVariable;
import parser.flatzinc.ast.expression.EAnnotation;
import parser.flatzinc.ast.expression.Expression;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 12 janv. 2010
* Since : Choco 2.1.1
*
* Constraint builder from flatzinc-like object.
*/
public final class PConstraint {

    static Logger LOGGER = ChocoLogging.getMainLogger();

    // VARIABLE TYPE
    private static final String _int = "int";
    private static final String _float = "float";
    private static final String _bool = "bool";
    private static final String _set = "set";
    private static final String _var = "_var";

    // COMPARISONS OPERATIONS
    private static final String _eq = "_eq";
    private static final String _ne = "_ne";
    private static final String _lt = "_lt";
    private static final String _gt = "_gt";
    private static final String _ge = "_ge";
    private static final String _le = "_le";

    private static final String _lin = "_lin";

    private static final String _reif = "_reif";

    // ARITHMETIC OPERATIONS
    private static final String _plus = "_plus";
    private static final String _minus = "_minus";
    private static final String _times = "_times";
    private static final String _negate = "_negate";
    private static final String _div = "_div";
    private static final String _mod = "_mod";
    private static final String _min = "_min";
    private static final String _max = "_max";
    private static final String _abs = "_abs";

    // LOGICAL OPERATIONS
    // CONJUNCTIONS
    private static final String _and = "_and";
    private static final String _or = "_or";
    private static final String _left_imp = "_left_imp";
    private static final String _right_imp = "_right_imp";
    private static final String _xor = "_xor";
    private static final String _not = "_not";
    // N-ARY CONJUNCTIONS
    private static final String _array = "array";
    // CLAUSES
    private static final String _clause = "_clause";

    // SET OPERATIONS
    private static final String _in = "_in";
    private static final String _subset = "_subset";
    private static final String _superset = "_superset";
    private static final String _union = "_union";
    private static final String _intersect = "_intersect";
    private static final String _diff = "_diff";
    private static final String _symdiff = "_symdiff";
    private static final String _card = "_card";

    // ARRAY OPERATIONS
    private static final String _element = "_element";

    // COERCION OPERATIONS
    private static final String _int2float = "int2float";
    private static final String _bool2int = "bool2int";

    // GLOBAL CONSTRAINTS
    private static final String _global = "global";
    private static final String _allDifferent = "_allDifferent";
    private static final String _allDisjoint = "_allDisjoint";
    private static final String _among = "_among";
    private static final String _cumulative = "_cumulative";
    private static final String _exactly = "_exactly";
    private static final String _setDisjoint = "_setDisjoint";
    private static final String _globalCardinalityLowUp = "_globalCardinalityLowUp";
    private static final String _globalCardinality = "_globalCardinality";
    private static final String _inverseChanneling = "_inverseChanneling";
    private static final String _inverseSet = "_inverseSet";
    private static final String _lexEq = "_lexEq";
    private static final String _lex = "_lex";
    private static final String _member = "_member";
    private static final String _sorting = "_sorting";


    public PConstraint(Model model, String id, List<Expression> exps, List<EAnnotation> annotations) {
        //TODO: manage annotations
        build(id, exps, model);
    }

    /**
     * Builder of constraint defined with flatzinc-like object.
     * @param name predicate name
     * @param exps constraint parameters
     * @param model
     */
    private static void build(String name, List<Expression> exps, Model model) {
        if(name.startsWith(_global)){
            buildGlobal(name, exps, model);
            return;
        }else
        if (name.startsWith(_int)) {
            buildInt(name, exps, model);
            return;
        } else
        if (name.startsWith(_float)) {
            buildFloat(name, exps, model);
            return;
        } else
        if (name.startsWith(_bool)) {
            buildBool(name, exps, model);
            return;
        } else
        if (name.startsWith(_set)) {
            buildSet(name, exps, model);
            return;
        } else if(name.startsWith(_array)){
            if(name.contains(_bool)){
                buildBool(name, exps, model);
                return;
            }else if(name.contains(_int)){
                buildInt(name, exps, model);
                return;
            }
        }
        LOGGER.severe("buildCstr::ERROR:: unknown type :" + name);
        throw new UnsupportedOperationException();
    }

    /**
     * Build a basic constraint based on int variables
     * @param name name of the constraint
     * @param exps parameters of the constraint
     * @param model
     */
    private static void buildInt(String name, List<Expression> exps, Model model) {
        Constraint c = null;
        if (name.contains(_lin)) {

            int[] coeffs = exps.get(0).toIntArray();
            IntegerVariable[] vars = exps.get(1).toIntVarArray();
            int result = exps.get(2).intValue();

            if (name.contains(_eq)) {
                c = (eq(scalar(coeffs, vars), result));
            } else if (name.contains(_ne)) {
                c = (neq(scalar(coeffs, vars), result));
            } else if (name.contains(_gt)) {
                c = (gt(scalar(coeffs, vars), result));
            } else if (name.contains(_lt)) {
                c = (lt(scalar(coeffs, vars), result));
            } else if (name.contains(_ge)) {
                c = (geq(scalar(coeffs, vars), result));
            } else if (name.contains(_le)) {
                c = (leq(scalar(coeffs, vars), result));
            }
        } else if (name.contains(_array) && name.contains(_element)) {
            IntegerVariable index = exps.get(0).intVarValue();
            IntegerVariable val = exps.get(2).intVarValue();
            if(name.contains(_var)){
                try{
                    IntegerVariable[] values = exps.get(1).toIntVarArray();
                    c = nth(index, values, val, 1);
                }catch (ClassCastException e){
                    int[] values = exps.get(1).toIntArray();
                    c = nth(index, values, val, 1);
                }
            }else{
                int[] values = exps.get(1).toIntArray();
                c = nth(index, values, val, 1);
            }
        } else {
            IntegerVariable[] vars = new IntegerVariable[exps.size()];
            for(int i = 0; i < vars.length; i++){
                vars[i] = exps.get(i).intVarValue();
            }

            if (name.contains(_eq)) {
                c = (eq(vars[0], vars[1]));
            } else if (name.contains(_ne)) {
                c = (neq(vars[0], vars[1]));
            } else if (name.contains(_gt)) {
                c = (gt(vars[0], vars[1]));
            } else if (name.contains(_lt)) {
                c = (lt(vars[0], vars[1]));
            } else if (name.contains(_ge)) {
                c = (geq(vars[0], vars[1]));
            } else if (name.contains(_le)) {
                c = (leq(vars[0], vars[1]));
            } else if (name.contains(_plus)) {
                c = (eq(plus(vars[0], vars[1]), vars[2]));
            } else if (name.contains(_minus)) {
                c = (eq(minus(vars[0], vars[1]), vars[2]));
            } else if (name.contains(_times)) {
                c = (times(vars[0], vars[1], vars[2]));
            } else if (name.contains(_negate)) {
                c = (eq(neg(vars[0]), vars[1]));
            } else if (name.contains(_div)) {
                c = (eq(div(vars[0], vars[1]), vars[2]));
            } else if (name.contains(_mod)) {
                c = (eq(mod(vars[0], vars[1]), vars[2]));
            } else if (name.contains(_min)) {
                c = (min(vars[0], vars[1], vars[2]));
            } else if (name.contains(_max)) {
                c = (max(vars[0], vars[1], vars[2]));
            } else if (name.contains(_abs)) {
                c = (eq(abs(vars[0]), vars[1]));
            } else if (name.contains(_int2float)) {
                //TODO: to complete
            }
        }
        if(c!=null){
            if (!name.endsWith(_reif)) {
                model.addConstraint(c);
            } else {
                IntegerVariable vr = exps.get(exps.size()-1).intVarValue();
                model.addConstraint(reifiedConstraint(vr, c));
            }
            return;
        }
        LOGGER.severe("buildInt::ERROR:: unknown type :" + name);
        throw new UnsupportedOperationException();
    }

    /**
     * Build a basic constraint based on float variables
     * @param name name of the constraint
     * @param exps parameters of the constraint
     * @param model
     */
    private static void buildFloat(String name, List<Expression> exps, Model model) {
        //TODO: to complete
        LOGGER.severe("buildFloat::ERROR:: unknown type :" + name);
        throw new UnsupportedOperationException();
    }

    /**
     * Build a basic constraint based on bool variables
     * FYI : bool == 1 is true
     * @param name name of the constraint
     * @param exps parameters of the constraint
     * @param model
     */
    private static void buildBool(String name, List<Expression> exps, Model model) {
        Constraint c = null;
        if (name.contains((_array))) {
            if (name.contains(_element)) {
                if (name.contains(_var)) {
                    IntegerVariable index = exps.get(0).intVarValue();
                    IntegerVariable[] values = exps.get(1).toIntVarArray();
                    IntegerVariable val = exps.get(2).intVarValue();
                    c = nth(index, values, val,1);
                } else {
                    IntegerVariable index = exps.get(0).intVarValue();
                    //TODO: must be change to smth like get_bools
                    int[] values = exps.get(1).toIntArray();
                    IntegerVariable val = exps.get(2).intVarValue();
                    c = nth(index, values, val,1);
                }
            } else {
                IntegerVariable[] vars = exps.get(0).toIntVarArray();
                IntegerVariable result = exps.get(1).intVarValue();
                if (name.contains(_and)) {
                    c = (reifiedAnd(result, vars));
                } else if (name.contains(_or)) {
                    c = (reifiedOr(result, vars));
                }
            }
        } else if (name.contains(_clause)) {
            IntegerVariable[] posLits = exps.get(0).toIntVarArray();
            IntegerVariable[] negLits = exps.get(1).toIntVarArray();
            c = (clause(posLits, negLits));
        } else {
            IntegerVariable[] vars = new IntegerVariable[exps.size()];
            for(int i = 0; i < vars.length; i++){
                vars[i] = exps.get(i).intVarValue();
            }
            if (name.contains(_eq)) {
                c = (eq(vars[0], vars[1]));
            } else if (name.contains(_ne)) {
                c = (eq(vars[0], vars[1]));
            } else if (name.contains(_gt)) {
                c = (gt(vars[0], vars[1]));
            } else if (name.contains(_lt)) {
                c = (lt(vars[0], vars[1]));
            } else if (name.contains(_ge)) {
                c = (geq(vars[0], vars[1]));
            } else if (name.contains(_le)) {
                c = (leq(vars[0], vars[1]));
            } else if (name.contains(_and)) {
                c = (reifiedAnd(vars[2], vars[0], vars[1]));
            } else if (name.contains(_or)) {
                c = (reifiedOr(vars[2], vars[0], vars[1]));
            } else if (name.contains(_xor)) {
                c = (reifiedXor(vars[2], vars[0], vars[1]));
            } else if (name.contains(_not)) {
                //TODO: to check
                c = neq(vars[0], vars[1]);
            } else if (name.contains(_right_imp)) {
                c = (reifiedRightImp(vars[2], vars[0], vars[1]));
            } else if (name.contains(_left_imp)) {
                c = (reifiedLeftImp(vars[2], vars[0], vars[1]));
            }else if(name.contains(_bool2int)){
                // beware... it is due to the fact that in choco, there are no boolean variable
                // but integer variable with [0,1] domain.
                c = (eq(vars[0], vars[1]));
            }
        }
        if (c != null) {
            if (!name.endsWith(_reif)) {
                model.addConstraint(c);
            } else {
                IntegerVariable vr = exps.get(exps.size()-1).intVarValue();
                model.addConstraint(reifiedConstraint(vr, c));
            }
            return;
        }
        LOGGER.severe("buildBool::ERROR:: unknown type :" + name);
        throw new UnsupportedOperationException();
    }

    /**
     * Build a basic constraint based on set variables
     * @param name name of the constraint
     * @param exps parameters of the constraint
     * @param model
     */
    private static void buildSet(String name, List<Expression> exps, Model model) {
        Constraint c = null;
        if(name.endsWith(_reif)){
            LOGGER.severe("buildSet::ERROR:: unexepected reified call :" + name);
            throw new UnsupportedOperationException();
        }
        if (name.endsWith(_in)) {
            IntegerVariable iv = exps.get(0).intVarValue();
            SetVariable sv = exps.get(1).setVarValue();
            c = member(iv, sv);
        }else if (name.contains(_card)) {
            SetVariable sv = exps.get(0).setVarValue();
            IntegerVariable iv = exps.get(1).intVarValue();
            c = eqCard(sv, iv);
        }
        else{
                SetVariable sv1 = exps.get(0).setVarValue();
                SetVariable sv2 = exps.get(1).setVarValue();
            if (name.contains(_diff)) {
                //TODO: to complete
            }else if (name.contains(_eq)) {
                c = eq(sv1, sv2);
            } else if (name.contains(_ge)) {
                //TODO: to complete
            } else if (name.contains(_gt)) {
                //TODO: to complete
            } else if (name.contains(_intersect)) {
                SetVariable inter = exps.get(2).setVarValue();
                c = setInter(sv1, sv2, inter);
            } else if (name.contains(_le)) {
                //TODO: to complete
            } else if (name.contains(_lt)) {
                //TODO: to complete
            } else if (name.contains(_ne)) {
                c = neq(sv1, sv2);
            } else if (name.contains(_subset)) {
                //TODO: to complete
            } else if (name.contains(_superset)) {
                //TODO: to complete
            } else if (name.contains(_symdiff)) {
                //TODO: to complete
            } else if (name.contains(_union)) {
                SetVariable union = exps.get(2).setVarValue();
                c = setUnion(sv1, sv2, union);
            }
        }

        if(c!=null){
//            if (!name.endsWith(_reif)) {
                model.addConstraint(c);
//            } else {
//                IntegerVariable vr = exps.get(exps.size()-1).intVarValue();
//                FZNParser.model.addConstraint(reifiedIntConstraint(vr, c));
//            }
            return;
        }
        LOGGER.severe("buildSet::ERROR:: unknown type :" + name);
        throw new UnsupportedOperationException();
    }

    /**
     * Build a global constraint
     * @param name name of the constraint
     * @param exps parameters of the constraint
     * @param model
     */
    private static void buildGlobal(String name, List<Expression> exps, Model model){
        Constraint c = null;
        if(name.contains(_allDifferent)){
            IntegerVariable[] vars = exps.get(0).toIntVarArray();
            c = allDifferent(vars);
        }else
        if(name.contains(_allDisjoint)){
            SetVariable[] vars = exps.get(0).toSetVarArray();
            c = setDisjoint(vars);
        }else
        if(name.contains(_among)){
            IntegerVariable nvar = exps.get(0).intVarValue();
            IntegerVariable[] vars = exps.get(1).toIntVarArray();
            SetVariable svar = exps.get(2).setVarValue();

            c = among(nvar, vars, svar);
        }else
        if(name.contains(_cumulative)){
            IntegerVariable[] starts = exps.get(0).toIntVarArray();
            IntegerVariable[] durations = exps.get(1).toIntVarArray();
            // build task variables
            TaskVariable[] tvars = new TaskVariable[starts.length];
            for(int i = 0; i < tvars.length; i++){
                tvars[i] = makeTaskVar("t_"+i, starts[i], durations[i]);
            }
            IntegerVariable[] heights = exps.get(2).toIntVarArray();
            IntegerVariable capa = exps.get(3).intVarValue();
            c = cumulative(name, tvars, heights, null, constant(0), capa, (IntegerVariable)null, "");
        }else
        if(name.contains(_setDisjoint)){
            SetVariable s1 = exps.get(0).setVarValue();
            SetVariable s2 = exps.get(1).setVarValue();
            c = setDisjoint(s1, s2);
        }else
        if(name.contains(_exactly)){
            int n = exps.get(0).intValue();
            IntegerVariable[] vars = exps.get(1).toIntVarArray();
            int value = exps.get(2).intValue();
            c = occurrence(n, vars, value);
        }else
        if(name.contains(_element)){
            IntegerVariable index = exps.get(0).intVarValue();
            IntegerVariable[] varArray = exps.get(1).toIntVarArray();
            IntegerVariable val = exps.get(2).intVarValue();
            c = nth(index, varArray, val,1);
        }else
        if(name.contains(_globalCardinalityLowUp)){
            IntegerVariable[] vars = exps.get(0).toIntVarArray();
            int[] covers = exps.get(1).toIntArray();
            int[] lbound = exps.get(2).toIntArray();
            int[] ubound = exps.get(3).toIntArray();

            int min  = MathUtils.min(covers)-1;
            int max = MathUtils.max(covers)+1;

            int[] low = new int[max-min];
            int[] upp = new int[max-min];
            Arrays.fill(low, 0);
            Arrays.fill(upp, 0);
            for(int i = 0; i < covers.length; i++){
                low[covers[i]] = lbound[i];
                upp[covers[i]] = ubound[i];
            }
            c = globalCardinality(vars, low, upp, 0);
        }else
        if(name.contains(_globalCardinality)){
            IntegerVariable[] vars = exps.get(0).toIntVarArray();
            IntegerVariable[] cards = exps.get(1).toIntVarArray();
            c = globalCardinality(vars, cards, 0);
        }else
        if(name.contains(_inverseChanneling)){
            IntegerVariable[] ivars1 = exps.get(0).toIntVarArray();
            IntegerVariable[] ivars2 = exps.get(1).toIntVarArray();
            c = inverseChanneling(ivars1, ivars2);
        }else
        if(name.contains(_inverseSet)){
            IntegerVariable[] ivars = exps.get(0).toIntVarArray();
            SetVariable[] svars = exps.get(1).toSetVarArray();
            c = inverseSet(ivars, svars);
        }else
        if(name.contains(_lexEq)){
            IntegerVariable[] xs = exps.get(0).toIntVarArray();
            IntegerVariable[] ys = exps.get(1).toIntVarArray();
            c = lexEq(xs, ys);
        }else
        if(name.contains(_lex)){
            IntegerVariable[] xs = exps.get(0).toIntVarArray();
            IntegerVariable[] ys = exps.get(1).toIntVarArray();
            c = lex(xs, ys);
        }else
        if(name.contains(_max)){
            IntegerVariable[] xs = exps.get(0).toIntVarArray();
            IntegerVariable max = exps.get(1).intVarValue();
            c = max(xs, max);
        }else
        if(name.contains(_member)){
            IntegerVariable ivar = exps.get(0).intVarValue();
            SetVariable svar = exps.get(1).setVarValue();
            c = member(ivar, svar);
        }else
        if(name.contains(_min)){
            IntegerVariable[] xs = exps.get(0).toIntVarArray();
            IntegerVariable min = exps.get(1).intVarValue();
            c = min(xs, min);
        }else
        if(name.contains(_sorting)){
            IntegerVariable[] xs = exps.get(0).toIntVarArray();
            IntegerVariable[] ys = exps.get(1).toIntVarArray();
            c = sorting(xs, ys);
        }
        if (c != null) {
            if (!name.endsWith(_reif)) {
                model.addConstraint(c);
            } else {
                IntegerVariable vr = exps.get(exps.size()-1).intVarValue();
                model.addConstraint(reifiedConstraint(vr, c));
            }
            return;
        }
        LOGGER.severe("buildGlob::ERROR:: unknown type :" + name);
        throw new UnsupportedOperationException();
    }

}
