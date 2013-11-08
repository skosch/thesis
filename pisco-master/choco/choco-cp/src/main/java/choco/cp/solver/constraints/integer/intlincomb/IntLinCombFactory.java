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

package choco.cp.solver.constraints.integer.intlincomb;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.IntLinComb;
import choco.cp.solver.constraints.integer.bool.BoolIntLinComb;
import choco.cp.solver.constraints.integer.bool.sum.EqBoolSum;
import choco.cp.solver.constraints.integer.bool.sum.GeqBoolSum;
import choco.cp.solver.constraints.integer.bool.sum.LeqBoolSum;
import choco.cp.solver.constraints.integer.bool.sum.NeqBoolSum;
import choco.cp.solver.variables.integer.IntTerm;
import static choco.kernel.common.Constant.FALSE;
import static choco.kernel.common.Constant.TRUE;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 11 mars 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public final class IntLinCombFactory {

    // rewriting utility: remove all null coefficients
    // TODO: could be improved to remove duplicates (variables that would appear twice in the linear combination)
    private static int countNonNullCoeffs(int[] lcoeffs) {
        int nbNonNull = 0;
        for (int lcoeff : lcoeffs) {
            if (lcoeff != 0) {
                nbNonNull++;
            }
        }
        return nbNonNull;
    }

    private static SConstraint eq(int cste) {
        return cste == 0 ? TRUE : FALSE;
    }

    private static SConstraint geq(int cste) {
        return cste <= 0 ? TRUE : FALSE;
    }

    private static SConstraint neq(int cste) {
        return cste != 0 ? TRUE : FALSE;
    }

    /**
     * does not consider IntTerm.getConstant() anymore.
     */
    public static SConstraint makeIntLinComb(IntTerm t, int c,
                                             int linOperator, final CPSolver solver) {
        IntVar[] lvars = t.getVariables();
        int[] lcoeffs = t.getCoefficients();
        int nbNonNullCoeffs = countNonNullCoeffs(lcoeffs);
        if (nbNonNullCoeffs == 0) { // All coefficients of the linear
            switch (linOperator) {
                case IntLinComb.EQ:
                    return eq(c);
                case IntLinComb.GEQ:
                    return geq(c);
                case IntLinComb.NEQ:
                    return neq(c);
                default:
                    return FALSE;
            }
        } else {
            int posIdx = 0;
            int negIdx = nbNonNullCoeffs - 1;
            int[] sortedCoeffs = new int[nbNonNullCoeffs];
            IntDomainVar[] sortedVars = new IntDomainVar[nbNonNullCoeffs];
            // fill it up with the coefficients and variables in the right order
            for (int i = 0; i < lvars.length; i++) {
                if (lcoeffs[i] > 0) {
                    //insert positive coeffs at the beginning
                    sortedVars[posIdx] = (IntDomainVar) lvars[i];
                    sortedCoeffs[posIdx] = lcoeffs[i];
                    posIdx++;
                } else if (lcoeffs[i] < 0) {
                    //insert negative coeffs at the end in reverse order
                    //avoid another loop to insert coeffs in original order.
                    sortedVars[negIdx] = (IntDomainVar) lvars[i];
                    sortedCoeffs[negIdx] = lcoeffs[i];
                    negIdx--;
                }
            }
            return createIntLinComb(sortedVars, sortedCoeffs, posIdx, c,
                    linOperator, solver);
        }
    }

    private static SConstraint createIntLinComb(IntDomainVar[] sortedVars,
                                                  int[] sortedCoeffs, int nbPositiveCoeffs, int c, int linOperator, final CPSolver solver) {
        //should be useless because the original array (user) are always copied in the IntTerm !
        //Furthermore, we sort the array before calling this function and we still copy the variable in the constraint.
        //TODO: BoolIntLinComb do not deal with NEQ!!
        if (isBoolLinComb(sortedVars)
                && linOperator!= IntLinComb.NEQ) {
            return createBoolLinComb(sortedVars, sortedCoeffs, c, linOperator, solver);
        } else {
            return new
                    IntLinComb(sortedVars, sortedCoeffs, nbPositiveCoeffs, c,
                    linOperator);
        }
    }

    /**
     * Check if the combination is made of a single integer variable and only
     * boolean variables
     */
    public static boolean isBoolLinComb(IntDomainVar[] lvars) {
        /*if (linOperator == IntLinComb.NEQ) {
            return false;
        }*/
        if (lvars.length <= 1) {
            return false;
        }
        int nbEnum = 0;
        for (IntDomainVar lvar : lvars) {
            if (!lvar.hasBooleanDomain()) {
                nbEnum++;
            }
            if (nbEnum > 1) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({"SuspiciousSystemArraycopy"})
    public static SConstraint createBoolLinComb(IntVar[] vars, int[] lcoeffs,
                                                   int c, int linOperator, final CPSolver solver) {
        IntDomainVar[] lvars = new IntDomainVar[vars.length];
        System.arraycopy(vars, 0, lvars, 0, vars.length);
        int idxSingleEnum = -1; // index of the enum intvar (the single non boolean var)
        int coefSingleEnum = Integer.MIN_VALUE; // coefficient of the enum
        // intvar
        for (int i = 0; i < lvars.length; i++) {
            if (!lvars[i].hasBooleanDomain()) {
                idxSingleEnum = i;
                coefSingleEnum = -lcoeffs[i];
            }
        }
        // construct arrays of coefficients and variables
        int nbVar = (idxSingleEnum == -1) ? lvars.length : lvars.length - 1;
        IntDomainVar[] vs = new IntDomainVar[nbVar];
        int[] coefs = new int[nbVar];
        int cpt = 0;
        for (int i = 0; i < lvars.length; i++) {
            if (i != idxSingleEnum) {
                vs[cpt] = lvars[i];
                coefs[cpt] = lcoeffs[i];
                cpt++;
            }
        }
        if (idxSingleEnum == -1) {
            return createBoolLinComb(vs, coefs, null, Integer.MAX_VALUE, c,
                    linOperator, solver);
        } else {
            return createBoolLinComb(vs, coefs, lvars[idxSingleEnum],
                    coefSingleEnum, c, linOperator, solver);
        }
    }

    private static SConstraint createBoolLinComb(IntDomainVar[] vs, int[] coefs,
                                                   IntDomainVar obj, int objcoef, int c, int linOperator, final CPSolver solver) {
        VariableUtils.quicksort(coefs, vs, 0, coefs.length - 1);
        if (obj == null) { // is there an enum variable ?
            boolean isAsum = true;
            for (int i = 0; i < vs.length && isAsum; i++) {
                if (coefs[i] != 1) {
                    isAsum = false;
                    break;
                }
            }
            if (isAsum) {
                switch (linOperator) {
                    case IntLinComb.EQ:
                        return new EqBoolSum(solver.getEnvironment(), vs, -c);
                    case IntLinComb.NEQ:
                        return new NeqBoolSum(solver.getEnvironment(), vs, -c);
                    case IntLinComb.LEQ:
                        return new LeqBoolSum(solver.getEnvironment(), vs, -c);
                    case IntLinComb.GEQ:
                        return new GeqBoolSum(solver.getEnvironment(), vs, -c);
                    default:
                        throw new SolverException("Unknown operator for BoolSum");
                }

            } else {
                IntDomainVar dummyObj = solver.makeConstantIntVar(-c);
                return new BoolIntLinComb(solver.getEnvironment(), vs, coefs, dummyObj, 1, 0,
                        linOperator);
            }
        } else {
            int newLinOp = linOperator;
            if (objcoef < 0) {
                if (linOperator != IntLinComb.NEQ) {
                    objcoef = -objcoef;
                    c = -c;
                    VariableUtils.reverse(coefs, vs);
                    ArrayUtils.inverseSign(coefs);
                }
                if (linOperator == IntLinComb.GEQ) {
                    newLinOp = IntLinComb.LEQ;
                } else if (linOperator == IntLinComb.LEQ) {
                    newLinOp = IntLinComb.GEQ;
                }
            }
            return new BoolIntLinComb(solver.getEnvironment(), vs, coefs, obj, objcoef, c, newLinOp);
        }
    }

}
