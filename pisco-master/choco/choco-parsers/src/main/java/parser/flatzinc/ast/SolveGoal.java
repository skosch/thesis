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

import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.valselector.MaxVal;
import choco.cp.solver.search.integer.valselector.MidVal;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.*;
import choco.cp.solver.search.set.*;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.ValIterator;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;
import parser.flatzinc.ast.expression.EAnnotation;
import parser.flatzinc.ast.expression.EArray;
import parser.flatzinc.ast.expression.EIdentifier;
import parser.flatzinc.ast.expression.Expression;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 12 janv. 2010
* Since : Choco 2.1.1
*
* Class for solve goals definition based on flatzinc-like objects.
*
* A solve goal is defined with:
* </br> 'solve annotations satisfy;'
* </br> or 'solve annotations maximize expression;'
* /br> or 'solve annotations minimize expression;' 
*/
public class SolveGoal {

    static Logger LOGGER = ChocoLogging.getMainLogger();

    final List<EAnnotation> annotations;
    final Solver type;
    final Expression expr;
    int nbStrategies = 0;

    public enum Solver {
        SATISFY, MINIMIZE, MAXIMIZE
    }

    public SolveGoal(List<EAnnotation> annotations, Solver type, Expression expr) {
        this.annotations = annotations;
        this.type = type;
        this.expr = expr;
    }

    public boolean defineGoal(CPSolver solver){
        boolean searchSet = readAnnotations(annotations, solver);
        switch (type) {
            case SATISFY:
                solver.setFirstSolution(true);
                break;
            case MAXIMIZE:
                solver.setDoMaximize(true);
                Variable max = expr.intVarValue();
                solver.setObjective(solver.getVar(max));
                solver.setRestart(true);
                solver.setFirstSolution(false);
                break;
            case MINIMIZE:
                solver.setDoMaximize(false);
                Variable min = expr.intVarValue();
                solver.setObjective(solver.getVar(min));
                solver.setRestart(true);
                solver.setFirstSolution(false);

                break;
        }
        solver.generateSearchStrategy();
        return searchSet;
    }

    /**
     * Read and treat annotations. These are search strategy annotations.
     * @param annotations search strategy annotations
     * @param solver solver within the search is defined
     * @return {@code true} if a search strategy is defined
     */
    private boolean readAnnotations(List<EAnnotation> annotations, CPSolver solver) {
        boolean defined = false;
        for (EAnnotation ann : annotations) {
            // read search sequences
            if (ann.id.value.equals("seq_search")) {
                // read search annotation
                for (Expression e : ann.exps) {
                    if (e.getTypeOf().equals(Expression.EType.ANN)) {
                        defined |= readSearchAnnotation((EAnnotation) e, solver);
                    } else if (e.getTypeOf().equals(Expression.EType.ARR)) {
                        EArray ea = (EArray)e;
                        List<EAnnotation> sub_ann = new ArrayList<EAnnotation>(ea.what.size());
                        for(Expression ex : ea.what){
                            sub_ann.add((EAnnotation)ex);
                        }
                        defined |= readAnnotations(sub_ann, solver);
                    }else {
                        LOGGER.severe(MessageFormat.format("SolveGoal#readAnnotations : unknown type \"{0}\"", e.getTypeOf()));
                    }
                }
            } else {
                if (ann.getTypeOf().equals(Expression.EType.ANN)) {
                        defined |=readSearchAnnotation(ann, solver);
                    } else {
                        LOGGER.severe(MessageFormat.format("SolveGoal#readAnnotations : unknown type \"{0}\"", ann.getTypeOf()));
                    }
            }
        }
        return defined;
    }

    private static final String[] sannos = {"int_search", "bool_search", "set_search"};

    private static final String[] varchoiceannos = {
            "input_order", "first_fail", "anti_first_fail", "smallest",
            "largest", "occurence", "most_constrained", "max_regret"
    };

    private static final String[] assignmentannos = {
            "indomain_min", "in_domain_max", "in_domain_middle", "indomain_median", "indomain",
            "indomain_random", "indomain_split", "indomain_reverse_split", "indomain_interval"
    };

    private static final String[]strategyannos = {"complete"};

    /**
     * Read search annotation and build corresponding strategy
     * @param e {@link parser.flatzinc.ast.expression.EAnnotation}
     * @param solver solver within the search is defined
     * @return {@code true} if a search strategy is defined
     */
    private boolean readSearchAnnotation(EAnnotation e, CPSolver solver) {
        Expression[] exps = new Expression[e.exps.size()];
        e.exps.toArray(exps);

        // int_search or bool_search
        if (sannos[0].equals(e.id.value) || sannos[1].equals(e.id.value)) {
            IntegerVariable[] scope = exps[0].toIntVarArray();
            return setIntSearchStrategy(solver.getVar(scope), (EIdentifier)exps[1], (EIdentifier)exps[2], solver);
        } else
            // set_search
            if (sannos[2].equals(e.id.value)) {
                SetVariable[] scope = exps[0].toSetVarArray();
                return setSetSearchStrategy(solver.getVar(scope), (EIdentifier)exps[1], (EIdentifier)exps[2], solver);
            }
        return false;
    }

    /**
     * Return the corresponding index of {@code v} in {@code vs}.
     * @param v value to search
     * @param vs pool of value
     * @return index of {@code v} in {@code vs}, -1 otherwise
     */
    private static int index(String v, String[] vs){
        for(int i = 0; i < vs.length; i++){
            if(vs[i].equals(v))return i;
        }
        return -1;
    }

    /**
     * Read and apply (if fully recognize) the search strategy for integer variable scope.
     * @param scope solver int variable
     * @param exp {@link parser.flatzinc.ast.expression.EIdentifier} defining the variable choice
     * @param exp1 {@link parser.flatzinc.ast.expression.EIdentifier} defining the assignment choice
     * @param solver solver within the search is defined
     * @return {@code true} if a search strategy is defined
     */
    private boolean setIntSearchStrategy(IntDomainVar[] scope, EIdentifier exp, EIdentifier exp1, CPSolver solver) {
        VarSelector<IntDomainVar> varSelector;
        switch (index(exp.value, varchoiceannos)){
            case 0:
                varSelector = new StaticVarOrder(solver, scope);
                break;
            case 1:
                varSelector = new MinDomain(solver, scope);
                break;
            case 2:
                varSelector = new MaxDomain(solver, scope);
                break;
            case 3:
                varSelector = new MinValueDomain(solver, scope);
                break;
            case 4:
                varSelector = new MaxValueDomain(solver, scope);
                break;
            case 5:
                varSelector = new MostConstrained(solver, scope);
                break;
            case 6:
                varSelector = new DomOverDeg(solver, scope);
                break;
            case 7:
                varSelector = new MaxRegret(solver, scope);
                break;
            default: return false;
        }
        ValSelector<IntDomainVar> vals = null;
        ValIterator<IntDomainVar> vali = null;
        switch (index(exp1.value, assignmentannos)){
            case 0:
                vals = new MinVal();
                break;
            case 1:
                vals = new MaxVal();
                break;
            case 2:
                //TODO: implements DomainClosestMiddle
                return false;
            case 3:
                vals = new MidVal();
                break;
            case 4:
                vali = new IncreasingDomain();
                break;
            case 5:
                vals = new RandomIntValSelector();
                break;
            case 6:
                //TODO: implements DomainFirstHalf
                return false;
            case 7:
                //TODO: implements DomainSecondtHalf
                return false;
            case 8:
                //TODO: implements DomainInterval
                return false;
            default : return false;
        }
        AssignVar as = null;
        if(vals != null){
            as = new AssignVar(varSelector, vals);
        }else if(vali != null){
            as = new AssignVar(varSelector, vali);
        }
        if(as!=null){
            if(nbStrategies==0){
                solver.attachGoal(as);
                nbStrategies++;
            }else{
                solver.addGoal(as);
                nbStrategies++;
            }
            return true;
        }
        return false;
    }


    /**
     * Read and apply (if fully recognize) the search strategy for set variable scope.
     * @param scope solver set variable
     * @param exp {@link parser.flatzinc.ast.expression.EIdentifier} defining the variable choice
     * @param exp1 {@link parser.flatzinc.ast.expression.EIdentifier} defining the assignment choice
     * @param solver solver within the search is defined
     * @return {@code true} if a search strategy is defined
     */
    private boolean setSetSearchStrategy(SetVar[] scope, EIdentifier exp, EIdentifier exp1, CPSolver solver) {
        VarSelector<SetVar> varSelector;
        switch (index(exp.value, varchoiceannos)){
            case 0:
                varSelector = new StaticSetVarOrder(solver, scope);
                break;
            case 1:
                varSelector = new MinDomSet(solver, scope);
                break;
            case 2:
                varSelector = new MaxDomSet(solver, scope);
                break;
            case 3:
                varSelector = new MinValueDomSet(solver, scope);
                break;
            case 4:
                varSelector = new MaxValueDomSet(solver, scope);
                break;
            case 5:
                varSelector = new MostConstrainedSet(solver, scope);
                break;
            case 6:
                //TODO: implements DomOverDeg
                return false;
            case 7:
                varSelector = new MaxRegretSet(solver, scope);
                break;
            default: return false;
        }
        ValSelector<SetVar> vals;
        switch (index(exp1.value, assignmentannos)){
            case 0:
                vals = new MinEnv();
                break;
            case 1:
                //TODO: implements MaxEnv
                return false;
            case 2:
                //TODO: implements DomainClosestMiddle
                return false;
            case 3:
                //TODO: implements MidEnv
                return false;
            case 4:
                //TODO: implements MeddianEnv
                return false;
            case 5:
                vals = new RandomSetValSelector();
                break;
            case 6:
                //TODO: implements DomainFirstHalf
                return false;
            case 7:
                //TODO: implements DomainSecondtHalf
                return false;
            case 8:
                //TODO: implements DomainInterval
                return false;
            default : return false;
        }
        if(vals != null){
            AssignSetVar as = new AssignSetVar(varSelector, vals);
            if(nbStrategies == 0){
                solver.attachGoal(as);
                nbStrategies++;
            }else{
                solver.addGoal(as);
                nbStrategies++;
            }
            return true;
        }
        return false;
    }


}
