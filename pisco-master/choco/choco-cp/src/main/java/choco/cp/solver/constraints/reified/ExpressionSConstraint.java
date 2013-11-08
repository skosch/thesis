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


import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.extension.FCBinSConstraint;
import choco.cp.solver.constraints.reified.leaves.bool.NotNode;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.branch.Extension;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.constraints.integer.extension.BinRelation;
import choco.kernel.solver.constraints.integer.extension.TuplesTest;
import choco.kernel.solver.constraints.reified.BoolNode;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.*;

/**
 * User: hcambaza
 * A constraint to deal with complex expression of variables involving a wide
 * range of operators
 */
public final class ExpressionSConstraint extends TuplesTest implements SConstraint, BinRelation {

    /**
     * The scope of the predicat
     */
    protected IntDomainVar[] vars;

    /**
     * A reference to the Expression itself
     */
    protected INode expr;

    /**
     * Tell the solver if this expression should be decomposed or
     * not (dealt as an extensional constraint if not)
     * if null, the solver will make a decision by itself
     */
    protected Boolean decomposeExp = null;

    /**
     * Enforce the level of consistency if the expression is posted
     * as an extentionnal constraint.
     * 0: AC
     * 1: FC
     */
    protected int levelAc = -1;


    protected SConstraint knownIntensionalConstraint = null;
    
    /**
     * Construct an Expression from a root Node
     *
     * @param expr
     */
    public ExpressionSConstraint(BoolNode expr, Boolean decomp) {
        this.expr = (INode) expr;
        bint = new int[2];
        decomposeExp = decomp;
    }

    /**
     * Construct an Expression from a root Node
     *
     * @param expr
     */
    public ExpressionSConstraint(BoolNode expr) {
        this.expr = (INode) expr;
        bint = new int[2];
    }

    public INode getRootNode() {
        return expr;
    }

    public Boolean isDecomposeExp() {
        return decomposeExp;
    }


    public void setDecomposeExp(Boolean decomposeExp) {
        this.decomposeExp = decomposeExp;
    }

    /**
     * compute the scope of this predicat as
     * the union of the scopes of all the leaves
     * and set the indexes of each variable of each leave
     * regarding its position in the scope "vars"
     * This is called once when posting the propagator
     */
    public void setScope(Solver s) {
        if (vars == null) {
            vars = expr.getScope(s);
            expr.setIndexes(vars);
        }
    }

    /**
     * @return the scope of the expression computed as solver variables
     */
    public IntDomainVar[] getVars() {
        return vars;
    }

    /**
     * @return the size of the scope
     */
    public int getNbVars() {
        if (vars != null)            
            return vars.length;
        else return 0;
    }

    public Var getVar(int i) {
        return vars[i];
    }
    
    @Override
	public Var getVarQuick(int i) {
    	return vars[i];
	}

	public void setVar(int i, Var v) {
        vars[i] = (IntDomainVar) v;
    }

    public int getLevelAc() {
        return levelAc;
    }

    /**
     * Set the level of AC in case the expression is not decomposed
     * 0 gives AC, 1 gives FC, -1 let the expression choose automatically
     *
     * @param levelAc
     */
    public void setLevelAc(int levelAc) {
        this.levelAc = levelAc;
    }

    /**
     * @return true iff all the operators are decomposable
     *         (choco provides an intensional constraint for them)
     */
    public boolean checkDecompositionIsPossible() {
        return expr.isDecompositionPossible();
    }

    /**
     * @return true is the expression contains an operator of reification i.e
     *         or, and, not.
     */
    public boolean checkIsReified() {
        return expr.isReified();
    }

    @Override
    public ExpressionSConstraint getOpposite() {
        return new ExpressionSConstraint(new NotNode(new INode[]{expr}), decomposeExp);
    }

    public SConstraint getKnownIntensionalConstraint() {
        return knownIntensionalConstraint;
    }

    public void setKnownIntensionalConstraint(SConstraint knownIntensionalConstraint) {
        this.knownIntensionalConstraint = knownIntensionalConstraint;
    }

    //*********************************************************//
    //******************* Expression as Checker ***************//
    //*********************************************************//

    //temporary table to avoid allocating a table for each check of a pair
    protected int[] bint;

    /**
     * Api to check a couple for binary AC
     *
     * @param a
     * @param b
     * @return true if the given couple satisfy the expression
     */
    public boolean checkCouple(int a, int b) {
        bint[0] = a;
        bint[1] = b;
        return checkTuple(bint);
    }


    // an expression encodes necessarily the feasible tuples
    public boolean isConsistent(int x, int y) {
        return checkCouple(x, y);
    }


    /**
     * Api to check a tuple
     *
     * @param tuple
     * @return true if the given tuple satisfy the expression
     */
    public boolean checkTuple(int[] tuple) {
        return ((BoolNode) expr).checkTuple(tuple);
    }

    /**
     * return the extensional propagator intended to propagate the expression
     *
     * @param s
     */
   public SConstraint getExtensionnal(Solver s) {
        if (getNbVars() == 0) //case of TRUE, FALSE constraint for example
            return getDecomposition(s);
        if (getNbVars() == 2
                && vars[0].hasEnumeratedDomain()
                && vars[1].hasEnumeratedDomain()) {

            int maxspan = Math.max(vars[0].getSup() - vars[0].getInf() + 1,
                    vars[1].getSup() - vars[1].getInf() + 1);
            int maxdsize = Math.max(vars[0].getDomainSize(), vars[1].getDomainSize());
            if (levelAc == 1 || (levelAc == -1 && maxspan >= 1000 && maxdsize < maxspan)) {
                return new FCBinSConstraint(vars[0],vars[1],this);//((CPSolver) s).relationTupleFC(vars, this);
            } else {
                return ((CPSolver) s).relationPairAC(vars[0], vars[1], this);
            }
        } else if ((levelAc == -1 && getNbVars() < 6) || levelAc == 0) {
            return s.relationTupleAC(vars, this);
        } else {
            return CPSolver.relationTupleFC(vars, this);
        }
    }

    //*********************************************************//
    //*** Expression as a set of intensional constraints ******//
    //*********************************************************//

    /**
     * Perform the decomposition of the Expression into
     * elementary constraints.
     *
     * @param s
     */
    public SConstraint getDecomposition(Solver s) {
        return ((BoolNode) expr).extractConstraint(s);
    }


    /**
     * @return the size of the cartesian products representing the Expression
     */
    public double cardProd() {
        double prodsize = 1d;
        for (int i = 0; i < vars.length; i++) {
            prodsize *= vars[i].getDomainSize();
        }
        return prodsize;
    }

    /**
     * Generate the list of tuples corresponding to this predicat
     *
     * @return
     */
    public List<int[]> getTuples(Solver s) {
        setScope(s);
        LinkedList<int[]> ltuples = new LinkedList<int[]>();
        int size = vars.length;
        int[] currentSupport = new int[size];
        DisposableIntIterator[] seekIter = new DisposableIntIterator[size];
        for (int i = 0; i < size; i++) {
            seekIter[i] = vars[i].getDomain().getIterator();
            currentSupport[i] = seekIter[i].next();
        }
        if (isConsistent(currentSupport)) {
            ltuples.add(copy(currentSupport));
        }
        int k = 0;
        while (k < vars.length) {
            if (!seekIter[k].hasNext()) {
                 seekIter[k].dispose();
                 seekIter[k] = vars[k].getDomain().getIterator();
                currentSupport[k] = seekIter[k].next();
                k++;
            } else {
                currentSupport[k] = seekIter[k].next();
                if (isConsistent(currentSupport)) {
                    ltuples.add(copy(currentSupport));
                }
                k = 0;
            }
        }
        return ltuples;
    }

    /**
     * Sort variables from the biggest domain to the smallest
     */
    public static class VarMinDomComparator implements Comparator {
        public int compare(Object o, Object o1) {
            IntDomainVar v1 = (IntDomainVar) o;
            IntDomainVar v2 = (IntDomainVar) o1;
            if (v1.getDomainSize() < v2.getDomainSize()) {
                return -1;
            } else if (v1.getDomainSize() == v2.getDomainSize()) {
                return 0;
            } else {
                return 1;
            }
        }
    }


    public String pretty() {
        return expr.pretty();
    }

    public SConstraintType getConstraintType() {
        return SConstraintType.EXPRESSION;
    }

    public Iterator<IntegerVariable> getVariableIterator() {
        IntegerVariable[] vs = expr.getModelScope();
        List<IntegerVariable> lvar = new LinkedList<IntegerVariable>();
        lvar.addAll(Arrays.asList(vs));
        return lvar.iterator();
    }


    public void setConstraintIndex(int i, int idx) {
        throw new SolverException("setConstraintIdx should not be called on a predicat");
    }

    public int getConstraintIdx(int idx) {
        throw new SolverException("getConstraintIdx should not be called on a predicat");
    }

    public boolean isSatisfied() {
        throw new SolverException("isSatisfiedTo should not be called on a predicat");
    }

    public AbstractSConstraint opposite(Solver solver) {
        throw new SolverException("opposite should not be called on a predicat");
    }

    public static int[] copy(int[] tab) {
        int[] tab2 = new int[tab.length];
        System.arraycopy(tab, 0, tab2, 0, tab.length);
        return tab2;
    }

	@Override
	public Extension getExtension(int extensionNumber) {
		return null;
	}

    /**
     * Adds a new extension.
     *
     * @param extensionNumber should use the number returned by getAbstractSConstraintExtensionNumber
     */
    @Override
    public void addExtension(final int extensionNumber) {}

	@Override
	public int getFineDegree(int idx) {
		return 1;
	}
    
    
}
