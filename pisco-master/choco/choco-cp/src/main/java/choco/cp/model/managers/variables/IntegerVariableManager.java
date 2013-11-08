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

package choco.cp.model.managers.variables;

import choco.Choco;
import choco.Options;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.reified.leaves.ConstantLeaf;
import choco.cp.solver.constraints.reified.leaves.VariableLeaf;
import choco.cp.solver.variables.integer.BooleanVarImpl;
import choco.cp.solver.variables.integer.IntDomainVarImpl;
import choco.kernel.model.Model;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.VariableManager;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 8 août 2008
 * Time: 10:14:01
 */
public final class IntegerVariableManager implements VariableManager<IntegerVariable> {

    private static final String CSTE = "CSTE_";

    protected static IntDomainVar makeConstant(CPSolver solver, IntegerVariable iv) {
        int value = iv.getLowB();
        IntDomainVar v = (IntDomainVar) solver.getIntConstant(value);
        // PATCH: for clarity, the constant name will be like "CSTE_<value>"
        if (v == null) {
            if (iv.isBoolean()) {
                v = new BooleanVarImpl(solver, CSTE + value);
                v.getDomain().restrict(value);
            } else {
                v = new IntDomainVarImpl(solver, CSTE + value, IntDomainVar.ONE_VALUE, value, value);
            }
            solver.addIntConstant(value, v);
        }
        return v;
    }

    /**
     * Build a integer variable for the given solver
     *
     * @param solver the solver defining the variable
     * @param var    model variable
     * @return an integer variable
     */
    public Var makeVariable(Solver solver, IntegerVariable var) {
        if (solver instanceof CPSolver) {
            IntDomainVar v = null;
            // Interception of boolean variable
            if (var.isConstant()) {
                return makeConstant((CPSolver) solver, var);
            } else if (var.isBoolean()) {
                v = new BooleanVarImpl(solver, var.getName());
            } else if (var.getValues() == null) {
                if (var.getLowB() != var.getUppB()) {
                    int type; // default type
                    if (var.getOptions().contains(Options.V_ENUM)) {
                        type = IntDomainVar.BITSET;
                    } else if (var.getOptions().contains(Options.V_BOUND)) {
                        type = IntDomainVar.BOUNDS;
                    } else if (var.getOptions().contains(Options.V_LINK)) {
                        type = IntDomainVar.LINKEDLIST;
                    } else if (var.getOptions().contains(Options.V_BTREE)) {
                        type = IntDomainVar.BINARYTREE;
                    } else if (var.getOptions().contains(Options.V_BLIST)) {
                        type = IntDomainVar.BIPARTITELIST;
                    } else {
                        type = getIntelligentDomain(solver.getModel(), var);
                    }
                    v = new IntDomainVarImpl(solver, var.getName(), type, var.getLowB(), var.getUppB());
                }
            } else {
                int[] values = var.getValues();
                if (values.length > 1) {
                    int type = IntDomainVar.BITSET; // default type
                    if (var.getOptions().contains(Options.V_LINK)) {
                        type = IntDomainVar.LINKEDLIST;
                    } else if (var.getOptions().contains(Options.V_BTREE)) {
                        type = IntDomainVar.BINARYTREE;
                    } else if (var.getOptions().contains(Options.V_BLIST)) {
                        type = IntDomainVar.BIPARTITELIST;
                    }
                    v = new IntDomainVarImpl(solver, var.getName(), type, values);
                }
            }
            ((CPSolver) solver).addIntVar(v);
            return v;
        }
        throw new ModelException("Could not found a variable manager in " + this.getClass() + " !");
    }

    /**
     * Build a expression node
     *
     * @param solver associated solver
     * @param cstrs  constraints
     * @param vars   variables
     * @return a variable leaf or constant leaf (for expression tree)
     */
    public INode makeNode(Solver solver, Constraint[] cstrs, Variable[] vars) {
        if (vars[0] instanceof IntegerConstantVariable) {
            IntegerConstantVariable c = (IntegerConstantVariable) vars[0];
            return new ConstantLeaf(c.getValue());
        } else if (vars[0] instanceof IntegerVariable) {
            return new VariableLeaf((IntegerVariable) vars[0]);
        }
        return null;
    }

    /**
     * try to find the most suitable domain for v regarding constraints wish
     * a simple heuristic is applied to rank the domains
     *
     * @param v unknown domain type variable
     * @return a domain type
     */
    public static int getIntelligentDomain(Model model, IntegerVariable v) {
        // specific case, deal with unbounded domain
        if (v.getLowB() <= Choco.MIN_LOWER_BOUND && v.getUppB() >= Choco.MAX_UPPER_BOUND) {
            return IntDomainVar.BOUNDS;
        }

        int[] scoreForDomain = new int[10]; // assume there is no more than 10 kind of domains

        //all type of domains are initially possible
        BitSet posDom = new BitSet(10);
        posDom.set(0, 20);
        if (v.getValues() != null) { //if initial holes, bounds impossible
            posDom.clear(IntDomainVar.BOUNDS);
        }
        // big domain without "holes"
        if (v.getUppB() - v.getLowB() + 1 == v.getDomainSize()) {
            if (v.getDomainSize() > 300) {
                posDom.clear(IntDomainVar.BITSET);
                posDom.clear(IntDomainVar.LINKEDLIST);
            }
        }

        //take preferences and possibilities of constraints
        Iterator<Constraint> it = v.getConstraintIterator(model);
        while (it.hasNext()) {
            Constraint cc = it.next();
            int[] prefereddoms = cc.getFavoriteDomains();
            if (prefereddoms.length > 0) {
                BitSet posCdom = new BitSet(5);
                for (int i = 0; i < prefereddoms.length; i++) {
                    scoreForDomain[prefereddoms[i]] += (i + 1);
                    posCdom.set(prefereddoms[i]);
                }
                posDom.and(posCdom);
            }
        }

        //find the best prefered domain
        int bestDom = possibleArgMin(scoreForDomain, posDom);
        if (bestDom == -1) {
            throw new ModelException("no suitable domain for " + v + " that can be accepted by all constraints");
        }
        return bestDom;
    }


    public static int possibleArgMin(int[] tab, BitSet posDom) {
        int bestDom = -1;
        int minScore = Integer.MAX_VALUE;
        for (int i = 0; i < tab.length; i++) {
            if (posDom.get(i) && minScore > tab[i]) {
                minScore = tab[i];
                bestDom = i;
            }
        }
        return bestDom;
    }
}
