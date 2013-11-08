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

package choco.cp.common.util.preprocessor;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.*;
import choco.cp.solver.constraints.integer.bool.BoolTimesXYZ;
import choco.cp.solver.constraints.reified.ExpressionSConstraint;
import choco.cp.solver.constraints.reified.leaves.ConstantLeaf;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.reified.ArithmNode;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.constraints.reified.NodeType;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 2 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 *
 * An class providing expression tools. 
 */
public final class ExpressionTools {

	private ExpressionTools(){}

	/**
	 * If the expression can be matched to a known intensional constraint, then
	 * return it as a solver constraint
	 * *
	 */
	public static SConstraint getIntentionalConstraint(final ExpressionSConstraint e, final CPSolver s) {
		SConstraint ct = null;
		ct = getSignConstraint(e);
		if (ct != null){
			return ct;
		}
		ct = getScalarConstraint(e, s);
		if (ct != null){
			return ct;
		}
		ct = getMinMaxConstraint(e, s);
		if (ct != null){
			return ct;
		}
		ct = getMultConstraint(e, s);
		if (ct != null){
			return ct;
		}
		ct = getDistanceConstraint(e, s);
		if (ct != null){
			return ct;
		}
		return ct;
	}

	//*********************************************************//
	//************* Recognize Scalar **************************//
	//*********************************************************//

	/**
	 * Try to recognize that the expression is a scalar.
	 */
	public static SConstraint getScalarConstraint(final ExpressionSConstraint e, final CPSolver s) {
		final INode expr = e.getRootNode();
		if (isAScalar(expr)) {
			final IntDomainVar[] scope = e.getVars();
			final INode right = expr.getSubtree(1);
			final INode left = expr.getSubtree(0);
			final int[] c1 = left.computeLinearExpr(scope.length);
			final int[] c2 = right.computeLinearExpr(scope.length);
			final int[] coefs = new int[scope.length];
			for (int i = 0; i < coefs.length; i++) {
				coefs[i] = c1[i] - c2[i];
			}
			final int cste = c2[scope.length] - c1[scope.length];
			if (expr.getType().equals(NodeType.GT)) {
				return s.gt(s.scalar(coefs, scope), cste);
			} else if (expr.getType().equals(NodeType.LT)) {
				return s.lt(s.scalar(coefs, scope), cste);
			} else if (expr.getType().equals(NodeType.GEQ)) {
				return s.geq(s.scalar(coefs, scope), cste);
			} else if (expr.getType().equals(NodeType.LEQ)) {
				return s.leq(s.scalar(coefs, scope), cste);
			} else if (expr.getType().equals(NodeType.EQ)) {
				return s.eq(s.scalar(coefs, scope), cste);
			} else if (expr.getType().equals(NodeType.NEQ)) {
				return s.neq(s.scalar(coefs, scope), cste);
			}
			return null;
		} else {
			return null;
		}
	}

	private static boolean isAScalar(final INode expr) {
		final boolean b = (
				expr.getType().equals(NodeType.GEQ) ||
				expr.getType().equals(NodeType.GT) ||
				expr.getType().equals(NodeType.LEQ) ||
				expr.getType().equals(NodeType.LT) ||
				expr.getType().equals(NodeType.EQ) ||
				expr.getType().equals(NodeType.NEQ));
		if (b) {
			final INode right = expr.getSubtree(0);
			final INode left = expr.getSubtree(1);
			return right.isALinearTerm() && left.isALinearTerm();
		} else {
			return false;
		}
	}

	//*********************************************************//
	//************* Sign constraint ***************************//
	//*********************************************************//

	/**
	 * Try to recognize a constraint stated on the signs of two
	 * variables :
	 * x * y >= 0 : sameSign(x,y)
	 * 0 > x * y : oppositeSign(x,y)
	 * x * y < 0 : oppositeSign(x,y)
	 * 0 =< x * y : sameSign(x,y)
	 */
	static SConstraint getSignConstraint(final ExpressionSConstraint e) {
		final IntDomainVar[] scope = e.getVars();
		final INode expr = e.getRootNode();
		if (scope.length == 2 &&
				(scope[0].canBeInstantiatedTo(0) ||
						scope[0].canBeInstantiatedTo(0))) //safety test with the value 0
		{
			return null;
		}
		if (expr.getType().equals(NodeType.GEQ)) {
			if (testXMultYAndCste(expr, 0, 1, 0)
					|| testXMultYAndCste(expr, 0, 1, 1)) {
				return new SignOp(scope[0], scope[1], true);
			} else if (testXMultYAndCste(expr, 1, 0, 0)
					|| testXMultYAndCste(expr, 1, 0, -1)) {
				return new SignOp(scope[0], scope[1], false);
			} else {
				return null;
			}
		}else if (expr.getType().equals(NodeType.GT)) {
			if (testXMultYAndCste(expr, 0, 1, 0)
					|| testXMultYAndCste(expr, 0, 1, -1)) {
				return new SignOp(scope[0], scope[1], true);
			} else if (testXMultYAndCste(expr, 1, 0, 1)
					|| testXMultYAndCste(expr, 1, 0, 0)) {
				return new SignOp(scope[0], scope[1], false);
			} else {
				return null;
			}
		} else if (expr.getType().equals(NodeType.LEQ)) {
			if (testXMultYAndCste(expr, 1, 0, 0)
					|| testXMultYAndCste(expr, 1, 0, 1)) {
				return new SignOp(scope[0], scope[1], true);
			} else if (testXMultYAndCste(expr, 0, 1, 0)
					|| testXMultYAndCste(expr, 0, 1, -1)) {
				return new SignOp(scope[0], scope[1], false);
			} else {
				return null;
			}
		} else if (expr.getType().equals(NodeType.LT)) {
			if (testXMultYAndCste(expr, 1, 0, 0)
					|| testXMultYAndCste(expr, 1, 0, -1)) {
				return new SignOp(scope[0], scope[1], true);
			} else if (testXMultYAndCste(expr, 0, 1, 1)
					|| testXMultYAndCste(expr, 0, 1, 0)) {
				return new SignOp(scope[0], scope[1], false);
			} else {
				return null;
			}
		}
		return null;
	}

	//test if the subtree contains a mult on 2 variables and the constant c
	private static boolean testXMultYAndCste(final INode expr, final int first, final int second, final int c) {
		return expr.getNbSubTrees() == 2 &&
				expr.getSubtree(first).getType().equals(NodeType.MULT) &&
				expr.getSubtree(first).hasOnlyVariablesLeaves() &&
				expr.getSubtree(second).isCsteEqualTo(c);
	}

	//*********************************************************//
	//************* Min/Max constraints ***********************//
	//*********************************************************//

	/**
	 * Try to recognize a constraint stated on min max
	 */
	static SConstraint getMinMaxConstraint(final ExpressionSConstraint e, final CPSolver s) {
		final INode expr = e.getRootNode();
		if (expr.getType().equals(NodeType.EQ)) {
			if (isMin(expr, 0, 1)) {
				final IntDomainVar[] tmpVars = new IntDomainVar[expr.getSubtree(0).getScope(s).length + 1];
				tmpVars[0] = expr.getSubtree(1).getScope(s)[0];
				System.arraycopy(expr.getSubtree(0).getScope(s), 0, tmpVars, 1, expr.getSubtree(0).getScope(s).length);
				return new MinOfAList(s.getEnvironment(), tmpVars);
			} else if (isMin(expr, 1, 0)) {
				final IntDomainVar[] tmpVars = new IntDomainVar[expr.getSubtree(1).getScope(s).length + 1];
				tmpVars[0] = expr.getSubtree(0).getScope(s)[0];
				System.arraycopy(expr.getSubtree(1).getScope(s), 0, tmpVars, 1, expr.getSubtree(1).getScope(s).length);
				return new MinOfAList(s.getEnvironment(), tmpVars);
			} else if (isMax(expr, 0, 1)) {
				final IntDomainVar[] tmpVars = new IntDomainVar[expr.getSubtree(0).getScope(s).length + 1];
				tmpVars[0] = expr.getSubtree(1).getScope(s)[0];
				System.arraycopy(expr.getSubtree(0).getScope(s), 0, tmpVars, 1, expr.getSubtree(0).getScope(s).length);
				return new MaxOfAList(s.getEnvironment(), tmpVars);
			} else if (isMax(expr, 1, 0)) {
				final IntDomainVar[] tmpVars = new IntDomainVar[expr.getSubtree(1).getScope(s).length + 1];
				tmpVars[0] = expr.getSubtree(0).getScope(s)[0];
				System.arraycopy(expr.getSubtree(1).getScope(s), 0, tmpVars, 1, expr.getSubtree(1).getScope(s).length);
				return new MaxOfAList(s.getEnvironment(), tmpVars);
			}
		}
		return null;
	}

	private static boolean isMin(final INode expr, final int first, final int second) {
		return expr.getSubtree(first).getType().equals(NodeType.MIN) &&
				expr.getSubtree(first).hasOnlyVariablesLeaves() &&
				expr.getSubtree(second).isAVariable();
	}

	private static boolean isMax(final INode expr, final int first, final int second) {
		return expr.getSubtree(first).getType().equals(NodeType.MAX) &&
				expr.getSubtree(first).hasOnlyVariablesLeaves() &&
				expr.getSubtree(second).isAVariable();
	}

	//*********************************************************//
	//************* mult constraints ***************************//
	//*********************************************************//

	/**
	 * Try to recognize a constraint stated on min max
	 */
	static SConstraint getMultConstraint(final ExpressionSConstraint e, final CPSolver s) {
		final INode expr = e.getRootNode();
		if (expr.getType().equals(NodeType.EQ)) {
			IntDomainVar[] vs = null;
			if (isMult(expr, 0, 1)) {
				vs = new IntDomainVar[3];
				final IntDomainVar[] scope0 = expr.getSubtree(0).getScope(s);
				vs[0] = scope0[0];
				if(scope0.length==2){
					vs[1] = scope0[1];
				}else{
					// specific case, when expresion is like:
						// eq(mult(Z,Z), X)
						vs[1] = scope0[0];
				}
				vs[2] = expr.getSubtree(1).getScope(s)[0];
			} else if (isMult(expr, 1, 0)) {
				vs = new IntDomainVar[3];
				vs[2] = expr.getSubtree(0).getScope(s)[0];
				final IntDomainVar[] scope1 = expr.getSubtree(1).getScope(s);
				vs[0] = scope1[0];
				if(scope1.length==2){
					vs[1] = scope1[1];
				}else{
					// specific case, when expresion is like:
						// eq(mult(Z,Z), X)
						vs[1] = scope1[0];
				}
			}
			if (vs != null) {
				if (vs[0].hasBooleanDomain() && vs[1].hasBooleanDomain() &&
						vs[2].hasBooleanDomain()) {
					return new BoolTimesXYZ(vs[0], vs[1], vs[2]);
				} else {
					return new TimesXYZ(vs[0], vs[1], vs[2]);
				}
			}

		}
		return null;
	}

	private static boolean isMult(final INode expr, final int first, final int second) {
		return expr.getSubtree(first).getType().equals(NodeType.MULT) &&
				expr.getSubtree(first).hasOnlyVariablesLeaves() &&
				expr.getSubtree(second).isAVariable();
	}

	//*********************************************************//
	//************* Distance constraints **********************//
	//*********************************************************//

	static SConstraint getDistanceConstraint(final ExpressionSConstraint e, final CPSolver s) {
		final INode expr = e.getRootNode();
		IntDomainVar x = null, y = null, z = null;
		int dist = -1;


		if (expr.getNbSubTrees() == 2) {
			final boolean b1 = isDistance(expr, 0, 1);
			final boolean b2 = isDistance(expr, 1, 0);
			if (b1 || b2) {
				final int idx1 = b1 ? 0 : 1;
				final int idx2 = b1 ? 1 : 0;
				x = expr.getSubtree(idx1).getSubtree(0).getSubtree(0).getScope(s)[0];
				y = expr.getSubtree(idx1).getSubtree(0).getSubtree(1).getScope(s)[0];
				if (expr.getSubtree(idx2).isAConstant()) {
					dist = ((ConstantLeaf) expr.getSubtree(idx2)).getConstant();
				} else {
					z = expr.getSubtree(idx2).getScope(s)[0];
				}
			} else {
				return null;
			}
			if (expr.getType().equals(NodeType.EQ)) {
				if (z == null) {
					return new DistanceXYC(x, y, dist, DistanceXYC.EQ);
				}
				else {
					return new DistanceXYZ(x, y, z, 0, DistanceXYZ.EQ);
				}
			} else if (expr.getType().equals(NodeType.LT)) {
				if (z == null) {
					return new DistanceXYC(x, y, dist, DistanceXYC.LT);
				}
				else {
					return new DistanceXYZ(x, y, z, 0, DistanceXYZ.LT);
				}
			} else if (expr.getType().equals(NodeType.GT)) {
				if (z == null) {
					return new DistanceXYC(x, y, dist, DistanceXYC.GT);
				}
				else {
					return new DistanceXYZ(x, y, z, 0, DistanceXYZ.GT);
				}
			} else if (expr.getType().equals(NodeType.LEQ)) {
				if (z == null) {
					return new DistanceXYC(x, y, dist + 1, DistanceXYC.LT);
				}
				else {
					return new DistanceXYZ(x, y, z, 1, DistanceXYZ.LT);
				}
			} else if (expr.getType().equals(NodeType.GEQ)) {
				if (z == null) {
					return new DistanceXYC(x, y, dist - 1, DistanceXYC.GT);
				}
				else {
					return new DistanceXYZ(x, y, z, -1, DistanceXYZ.GT);
				}
			} else if (expr.getType().equals(NodeType.NEQ)) {
				if (z == null) {
					return new DistanceXYC(x, y, dist, DistanceXYC.NEQ);
				}
				else {
					return null;
				}
			}
		}
		return null;
	}

	//detec if expr has the form abs(sub(X2,X3)),X4
	private static boolean isDistance(final INode expr, final int first, final int second) {
		final INode i1 = expr.getSubtree(first);
		if (i1.getNbSubTrees() == 1) {
			final INode i11 = i1.getSubtree(0);
			final INode i2 = expr.getSubtree(second);
			return (i2.isAVariable() || i2.isAConstant()) &&
					i11.hasOnlyVariablesLeaves() &&
					i11.getType().equals(NodeType.MINUS) &&
					i1.getType().equals(NodeType.ABS);
		} else {
			return false;
		}
	}

	//*********************************************************//
	//************* Detecting simple reified operators ********//
	//*********************************************************//

	//or(and(eq(X0,1),ct(Y)),and(eq(X0,0),oppositeCt(Y)))
	//for example : or(and(eq(X0,1),eq(X1,X2)),and(eq(X0,0),ne(X1,X2)))


	//*********************************************************//
	//************* Detecting disjunctions ********************//
	//*********************************************************//

	//or(le(add(X0,X1),X2),le(add(X2,X3),X0))
	//X0 et X2 variables, X1 et X3 constantes
	//duration[X0] = X1 et duration[X2] = X3

	public static SimplePrecedence getPrecedenceConstraint(final ExpressionSConstraint e) {
		final INode expr = e.getRootNode();
		final INode i1 = expr.getSubtree(0);
		if (i1.getNbSubTrees() == 2 && expr.getType().equals(NodeType.OR)) {
			final INode left = expr.getSubtree(0);
			final INode right = expr.getSubtree(1);
			if (left.getType().equals(NodeType.LEQ) && right.getType().equals(NodeType.LEQ)) {
				final INode l1 = left.getSubtree(0);
				final INode l2 = left.getSubtree(1);
				final INode r1 = right.getSubtree(0);
				final INode r2 = right.getSubtree(1);
				if (l2.isAVariable() && r2.isAVariable() &&
						isPlusVarCste(l1) && isPlusVarCste(r1)) {
					final int d1 = getConstant(l1);
					final IntegerVariable v1 = r2.getModelScope()[0];
					final int d2 = getConstant(r1);
					final IntegerVariable v2 = l2.getModelScope()[0];
					return new SimplePrecedence(v1, d1, v2, d2);
				}
			}
		}
		return null;
	}

	private static boolean isPlusVarCste(final INode plus) {
		return plus.getType().equals(NodeType.PLUS) &&
				plus.countNbVar() == 1 &&
				(plus.getSubtree(0).isAVariable() ||
						plus.getSubtree(1).isAVariable());
	}


	private static int getConstant(final INode plus) {
		final INode in = plus.getSubtree(0);
		final INode in2 = plus.getSubtree(1);
		if (in.countNbVar() == 0)
		{
			return ((ArithmNode) in).eval(null);
		}
		else {
			return ((ArithmNode) in2).eval(null);
		}
	}

	public static class SimplePrecedence {
		public final int d1;
		public final int d2;
		public final IntegerVariable v1;
		public final IntegerVariable v2;

		public SimplePrecedence(final IntegerVariable v1, final int d1, final IntegerVariable v2, final int d2) {
			this.v1 = v1;
			this.v2 = v2;
			this.d1 = d1;
			this.d2 = d2;
		}
	}

	//************************************************************************//
	//* Does the expression enforce at least a difference ********************//
	//************************************************************************//

	public static boolean encompassDiff(final ExpressionSConstraint ic) {
		if (ic.getNbVars() != 2) {
			return false;
		} else {
			IntDomainVar v1 = ic.getVars()[0];
			IntDomainVar v2 = ic.getVars()[1];
			if (v2.getDomainSize() < v1.getDomainSize()) { //pick the smallest domain
				final IntDomainVar temp = v1;
				v1 = v2;
				v2 = temp;
			}
			if (v1.getDomainSize() <= 100 && v2.getDomainSize() <= 100) {
				final DisposableIntIterator it = v1.getDomain().getIterator();
				while (it.hasNext()) {
					final int val = it.next();
					if (v2.canBeInstantiatedTo(val)) {
						if (ic.checkCouple(val, val)){
							it.dispose();
							return false;
						}
					}
				}
				it.dispose();
				return true;
			}
		}
		return false;
	}

	//*********************************************************//
	//************* Decompose expression **********************//
	//*********************************************************//

	/**
	 * Is there at least one variable within the scope of e
	 * that has more than ratioHole % of holes in the domain ?
	 *
	 * @param e expression
	 * @param configuration
	 * @return true if one of the variables has some holes
	 */
	public static boolean hasHolesWithinOneDomain(final ExpressionSConstraint e, final Configuration configuration) {
		final IntDomainVar[] vs = e.getVars();
		for (int i = 0; i < vs.length; i++) {
			final IntDomainVar v = vs[i];
			final int span = v.getSup() - v.getInf() + 1;
			if (configuration.readDouble(Configuration.RATION_HOLE) * span > v.getDomainSize()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Advise decomposition above a threshold of arity and cartesian
	 * product
	 *
	 * @param e
	 */
	public static boolean toBeDecomposed(final ExpressionSConstraint e) {
		final boolean isDecompPossible = e.checkDecompositionIsPossible();
		final int arity = e.getNbVars();
		final double cardProd = e.cardProd();
		return isDecompPossible &&
				((arity >= 6) 
						|| (arity >= 5 && cardProd >= 1000)
						|| (arity >= 4 && cardProd >= 2000) 
						|| (arity >= 3 && cardProd >= 50000))
						|| (arity >= 2 && cardProd >= 10000);
	}


	/**
	 * Check if adding an ac scheme with residue will be heavy for this
	 * expression
	 *
	 * @param e
	 * @return
	 */
	public static boolean isVeryBinaryHeavy(final ExpressionSConstraint e) {
		if (e.getNbVars() == 2) {
			final IntDomainVar v1 = e.getVars()[0];
			final IntDomainVar v2 = e.getVars()[1];
			final int maxspan = Math.max(v1.getSup() - v1.getInf() + 1,
			       v2.getSup() - v2.getInf() + 1);
			final int maxdsize = Math.max(v1.getDomainSize(), v2.getDomainSize());
			return (maxspan >= 1000 && maxdsize < maxspan);
		} else {
			return false;
		}
	}
}
