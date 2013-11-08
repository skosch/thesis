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

package choco.cp.solver.constraints.integer.bool;

import choco.cp.solver.constraints.integer.IntLinComb;
import choco.cp.solver.variables.integer.IntDomainVarImpl;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A constraint to enforce Sigma_i coef[i]*x_i + k OP y_i where :
 * - OP belongs to >=, <=, =
 * - k is a constant
 * - x_i are boolean variable
 * - t_i is an enum variable
 * It improves the general IntLinComb by storing lower and upper bound of the expression
 * and sorting coefficient for filtering.
 * User: Hadrien
 * Date: 29 oct. 2006
 */
public final class BoolIntLinComb extends AbstractLargeIntSConstraint {

    /**
     * Field representing the type of linear constraint
     * (equality, inequality, disequality).
     */
    protected int op = -1;

    /**
     * Lower bound of the expression
     */
    protected IStateInt lb;

    /**
     * upper bound of the expression
     */
    protected IStateInt ub;


    /**
     * index of the maximum coefficient of positive sign
     */
    protected IStateInt maxPosCoeff;

    /**
     * index of the maximum coefficient of negative sign
     */
    protected IStateInt maxNegCoeff;

    /**
     * coefs and vars are sorted in increasing value of the coef
     */
    protected final int[] sCoeffs;

    /**
     * number of negative coefficients
     */
    protected int nbNegCoef;

    /**
     * coefficients of the integer variable
     */
    protected final int objCoef;

    /**
     * coefficients of the integer variable
     */
    protected final int addcste;


    protected final RightMemberBounds rmemb;

    final IntDomainVarImpl varCste;

    private final int nbVars;

    public static IntDomainVar[] makeTableVar(IntDomainVar[] vs, IntDomainVar v) {
        IntDomainVar[] nvars = new IntDomainVar[vs.length + 1];
        System.arraycopy(vs, 0, nvars, 0, vs.length);
        nvars[vs.length] = v;
        return nvars;
    }


    /**
     * Constructs the constraint with the specified variables and constant.
     * Use the Model.createIntLinComb API instead of this constructor.
     * WARNING : This constructor assumes :
     * - there are no null coefficient
     * - coefficients "coefs" are sorted from the smallest to the biggest (negative coefs first).
     * - objcoef is strictly POSITIVE
     * - op belongs to EQ, GT, NEQ and LEQ
     */
    public BoolIntLinComb(IEnvironment environment, IntDomainVar[] vs, int[] coefs, IntDomainVar c, int objcoef, int scste, int op) {
//        super(priority(vs.length), makeTableVar(vs, c));
        super(makeTableVar(vs, c));
        this.sCoeffs = coefs;
        this.op = op;
        this.nbVars = vs.length;
        this.varCste = (IntDomainVarImpl) vars[nbVars];
        this.objCoef = objcoef;
        this.addcste = scste;
        nbNegCoef = 0;
        while (nbNegCoef < nbVars && sCoeffs[nbNegCoef] < 0) {
            nbNegCoef++;
        }
        if (op == IntLinComb.EQ || op == IntLinComb.GEQ || op == IntLinComb.LEQ) {
            this.maxPosCoeff = environment.makeInt();
            this.maxNegCoeff = environment.makeInt();
        }
        if (op == IntLinComb.EQ || op == IntLinComb.GEQ) {
            this.ub = environment.makeInt();
        }
        if (op == IntLinComb.EQ || op == IntLinComb.LEQ) {
            this.lb = environment.makeInt();
        }
        if (objCoef == 1) {
            rmemb = new SimpleRightMemberBounds();
        } else {
            rmemb = new RightMemberBounds();
        }
    }

    private static int priority(int nbVars){
        switch (nbVars){
            case 0:
            case 1:
                return ConstraintEvent.UNARY;
            case 2:
                return ConstraintEvent.BINARY;
            case 3:
                return ConstraintEvent.TERNARY;
            default:
                return ConstraintEvent.LINEAR;
        }
    }


    public int getFilteredEventMask(int idx) {
        if (idx < nbVars) {
            return IntVarEvent.INSTINT_MASK;
        } else {
            return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
        }
    }


    public class RightMemberBounds {

        public int getInfRight() {
            return objCoef * varCste.getInf();
        }

        public int getSupRight() {
            return objCoef * varCste.getSup();
        }

        public int getNewInfForObj() {
            return MathUtils.divCeil(lb.get(), objCoef);
        }

        public int getNewSupForObj() {
            return MathUtils.divFloor(ub.get(), objCoef);
        }
    }

    public class SimpleRightMemberBounds extends RightMemberBounds {

        @Override
        public final int getInfRight() {
            return varCste.getInf();
        }

        @Override
        public final int getSupRight() {
            return varCste.getSup();
        }

        @Override
        public final int getNewInfForObj() {
            return lb.get();
        }

        @Override
        public final int getNewSupForObj() {
            return ub.get();
        }

    }


    /************************************************************************/
    /************** update of data structures *******************************/
    /**
     * ********************************************************************
     */

    public final void updateUbLbOnInst(int idx, int i) {
        if (sCoeffs[idx] < 0) {
            if (i == 1) {
                ub.add(sCoeffs[idx]);
            } else {
                lb.add(-sCoeffs[idx]);
            }
        } else {
            if (i == 1) {
                lb.add(sCoeffs[idx]);
            } else {
                ub.add(-sCoeffs[idx]);
            }
        }
    }

    public final void lookForNewMaxPosCoeff() {
        int i = maxPosCoeff.get() - 1;
        while (i >= nbNegCoef && vars[i].isInstantiated()) {
            i--;
        }
        maxPosCoeff.set(i);
    }

    public final void lookForNewMaxNegCoeff() {
        int i = maxNegCoeff.get() + 1;
        while (i < nbNegCoef && vars[i].isInstantiated()) {
            i++;
        }
        maxNegCoeff.set(i);
    }

    /************************************************************************/
    /************** Main methods for filtering ******************************/
    /**
     * ********************************************************************
     */

    public final boolean updateForGEQ() throws ContradictionException {
        boolean change = false;
        change = filterPosCoeffUb();
        change |= filterNegCoeffUb();
        return change;
    }

    public final boolean updateForLEQ() throws ContradictionException {
        boolean change = false;
        change = filterPosCoeffLb();
        change |= filterNegCoeffLb();
        return change;
    }

    public final void fixPointOnEQ() throws ContradictionException {
        boolean fixpoint = true;
        while (fixpoint) {
            fixpoint = false;
            varCste.updateSup(rmemb.getNewSupForObj(), this, false);
            varCste.updateInf(rmemb.getNewInfForObj(), this, false);
            fixpoint = updateForGEQ();
            fixpoint |= updateForLEQ();
        }
    }

    /* ***********************************************************************/
    /* ************* filtering based on the upperbound of the expression *****/
    /* ***********************************************************************/

    public boolean filterNegCoeffUb() throws ContradictionException {
        boolean change = false;
        int cpt = maxNegCoeff.get();
        while (cpt < nbNegCoef && vars[cpt].isInstantiated()) {
            cpt++;
        }
        while (cpt < nbNegCoef && ub.get() + sCoeffs[cpt] < rmemb.getInfRight()) {
            IntDomainVarImpl v = (IntDomainVarImpl) vars[cpt];
            v.instantiate(0, this, false);
            change = true;
            if (op == IntLinComb.EQ) {
                lb.add(-sCoeffs[cpt]);
            }
            do {
                cpt++;
            } while (cpt < nbNegCoef && vars[cpt].isInstantiated());
        }
        maxNegCoeff.set(cpt);
        return change;
    }

    public boolean filterPosCoeffUb() throws ContradictionException {
        boolean change = false;
        int cpt = maxPosCoeff.get();
        while (cpt >= nbNegCoef && vars[cpt].isInstantiated()) {
            cpt--;
        }
        while (cpt >= nbNegCoef && ub.get() - sCoeffs[cpt] < rmemb.getInfRight()) {
            IntDomainVarImpl v = (IntDomainVarImpl) vars[cpt];
            v.instantiate(1, this, false);
            change = true;
            if (op == IntLinComb.EQ) {
                lb.add(sCoeffs[cpt]);
            }
            do {
                cpt--;
            } while (cpt >= nbNegCoef && vars[cpt].isInstantiated());
        }
        maxPosCoeff.set(cpt);
        return change;
    }

    /************************************************************************/
    /************** filtering based on the lower bound of the expression ****/
    /**
     * ********************************************************************
     */

    public final boolean filterPosCoeffLb() throws ContradictionException {
        boolean change = false;
        int cpt = maxPosCoeff.get();
        while (cpt >= nbNegCoef && vars[cpt].isInstantiated()) {
            cpt--;
        }
        while (cpt >= nbNegCoef && lb.get() + sCoeffs[cpt] > rmemb.getSupRight()) {
            vars[cpt].instantiate(0, this, false);
            change = true;
            if (op == IntLinComb.EQ) {
                ub.add(-sCoeffs[cpt]);
            }
            do {
                cpt--;
            } while (cpt >= nbNegCoef && vars[cpt].isInstantiated());
        }
        maxPosCoeff.set(cpt);
        return change;
    }

    /**
     * enforce variables that would otherwise make the upper bound unreachable
     */
    public final boolean filterNegCoeffLb() throws ContradictionException {
        boolean change = false;
        int cpt = maxNegCoeff.get();
        while (cpt < nbNegCoef && vars[cpt].isInstantiated()) {
            cpt++;
        }
        while (cpt < nbNegCoef && lb.get() - sCoeffs[cpt] > rmemb.getSupRight()) {
            vars[cpt].instantiate(1, this, false);
            change = true;
            if (op == IntLinComb.EQ) {
                ub.add(sCoeffs[cpt]);
            }
            do {
                cpt++;
            } while (cpt < nbNegCoef && vars[cpt].isInstantiated());
        }
        maxNegCoeff.set(cpt);
        return change;
    }

    /************************************************************************/
    /************** React on event of the constraint ************************/
    /**
     * ********************************************************************
     */


    @Override
    public void awakeOnInst(int idx) throws ContradictionException {

        if (idx < nbVars) {
            int i = vars[idx].getVal();
            switch (op) {
                case IntLinComb.GEQ:
                    if (sCoeffs[idx] < 0 && i == 1) {
                        ub.add(sCoeffs[idx]);
                        varCste.updateSup(rmemb.getNewSupForObj(), this, false);
                        updateForGEQ();
                    } else if (sCoeffs[idx] > 0 && i == 0) {
                        ub.add(-sCoeffs[idx]);
                        varCste.updateSup(rmemb.getNewSupForObj(), this, false);
                        updateForGEQ();
                    } else if (idx == maxPosCoeff.get()) {
                        lookForNewMaxPosCoeff();
                    } else if (idx == maxNegCoeff.get()) {
                        lookForNewMaxNegCoeff();
                    }
                    break;
                case IntLinComb.LEQ:
                    if (sCoeffs[idx] > 0 && i == 1) {
                        lb.add(sCoeffs[idx]);
                        varCste.updateInf(rmemb.getNewInfForObj(), this, false);
                        updateForLEQ();
                    } else if (sCoeffs[idx] < 0 && i == 0) {
                        lb.add(-sCoeffs[idx]);
                        varCste.updateInf(rmemb.getNewInfForObj(), this, false);
                        updateForLEQ();
                    } else if (idx == maxPosCoeff.get()) {
                        lookForNewMaxPosCoeff();
                    } else if (idx == maxNegCoeff.get()) {
                        lookForNewMaxNegCoeff();
                    }
                    break;
                case IntLinComb.EQ:
                    updateUbLbOnInst(idx, i);
                    fixPointOnEQ();
                    break;
                case IntLinComb.NEQ:
                    //TODO
                    break;

            }
        } else {
            switch (op) {
                case IntLinComb.GEQ:
                    filterPosCoeffUb();
                    filterNegCoeffUb();
                    break;
                case IntLinComb.LEQ:
                    filterPosCoeffLb();
                    filterNegCoeffLb();
                    break;
                case IntLinComb.EQ:
                    fixPointOnEQ();
                    break;
                case IntLinComb.NEQ:
                    //TODO
                    break;
            }
        }
    }

    // can only be called on idx = cste
    @Override
    public void awakeOnInf(int idx) throws ContradictionException {
        if (op == IntLinComb.GEQ) {
            filterPosCoeffUb();
            filterNegCoeffUb();
        } else if (op == IntLinComb.EQ) {
            fixPointOnEQ();
        } else {
            //TODO
        }
    }

    // can only be called on idx = cste
    @Override
    public void awakeOnSup(int idx) throws ContradictionException {
        if (op == IntLinComb.EQ) {
            fixPointOnEQ();
        } else if (op == IntLinComb.LEQ) {
            filterPosCoeffLb();
            filterNegCoeffLb();
        } else {
            //TODO
        }
    }

    //@Override
    public void propagate() throws ContradictionException {
        switch (op) {
            case IntLinComb.EQ:
                maxNegCoeff.set(0);
                maxPosCoeff.set(nbVars - 1);
                initUb();
                initlb();
                propagateEQ();
                break;
            case IntLinComb.GEQ:
                maxNegCoeff.set(0);
                maxPosCoeff.set(nbVars - 1);
                initUb();
                propagateGEQ();
                break;
            case IntLinComb.LEQ:
                maxNegCoeff.set(0);
                maxPosCoeff.set(nbVars - 1);
                initlb();
                propagateLEQ();
                break;
            case IntLinComb.NEQ:
                //TODO
                break;
        }
    }

    public void propagateEQ() throws ContradictionException {
        for (int i = 0; i < nbNegCoef; i++) {
            if (ub.get() + sCoeffs[i] < rmemb.getInfRight()) {
                vars[i].instantiate(0, this, false);
            }
        }
        for (int i = nbNegCoef; i < nbVars; i++) {
            if (ub.get() - sCoeffs[i] < rmemb.getInfRight()) {
                vars[i].instantiate(1, this, false);
            }
        }

        for (int i = 0; i < nbNegCoef; i++) {
            if (lb.get() - sCoeffs[i] > rmemb.getSupRight()) {
                vars[i].instantiate(1, this, false);
            }
        }

        for (int i = nbNegCoef; i < nbVars; i++) {
            if (lb.get() + sCoeffs[i] > rmemb.getSupRight()) {
                vars[i].instantiate(0, this, false);
            }
        }

        for (int i = 0; i < nbVars; i++) {
            if (vars[i].isInstantiated()) {
                updateUbLbOnInst(i, vars[i].getVal());
            }
        }

        fixPointOnEQ();
    }

    public void propagateGEQ() throws ContradictionException {
        for (int i = 0; i < nbNegCoef; i++) {
            if (ub.get() + sCoeffs[i] < rmemb.getInfRight()) {
                vars[i].instantiate(0, this, false);
            }

            if (vars[i].isInstantiated()) {
                awakeOnInst(i);
            }
        }
        for (int i = nbNegCoef; i < nbVars; i++) {
            if (ub.get() - sCoeffs[i] < rmemb.getInfRight()) {
                vars[i].instantiate(1, this, false);
            }

            if (vars[i].isInstantiated()) {
                awakeOnInst(i);
            }
        }
        varCste.updateSup(rmemb.getNewSupForObj(), this, false);
        updateForGEQ();
    }

    public void propagateLEQ() throws ContradictionException {
        for (int i = 0; i < nbNegCoef; i++) {
            if (lb.get() - sCoeffs[i] > rmemb.getSupRight()) {
                vars[i].instantiate(1, this, true);
            }

            if (vars[i].isInstantiated()) {
                awakeOnInst(i);
            }
        }
        for (int i = nbNegCoef; i < nbVars; i++) {
            if (lb.get() + sCoeffs[i] > rmemb.getSupRight()) {
                vars[i].instantiate(0, this, true);
            }
            if (vars[i].isInstantiated()) {
                awakeOnInst(i);
            }
        }
        varCste.updateInf(rmemb.getNewInfForObj(), this, false);
        updateForLEQ();
    }

    public final void initUb() {
        int upb = addcste;
        for (int i = 0; i < sCoeffs.length; i++) {
            if (sCoeffs[i] > 0) {
                upb += sCoeffs[i];
            }
        }
        ub.set(upb);
    }

    public final void initlb() {
        int lpb = addcste;
        for (int i = 0; i < sCoeffs.length; i++) {
            if (sCoeffs[i] < 0) {
                lpb += sCoeffs[i];
            }
        }
        lb.set(lpb);
    }

    /**
     * Tests if the constraint is consistent
     * with respect to the current state of domains.
     *
     * @return true iff the constraint is bound consistent
     *         (weaker than arc consistent)
     */
    @Override
    public boolean isConsistent() {
        if (op == IntLinComb.EQ) {
            return (hasConsistentLowerBound() && hasConsistentUpperBound());
        } else if (op == IntLinComb.GEQ) {
            return hasConsistentUpperBound();
        } else if (op == IntLinComb.LEQ) {
            return hasConsistentLowerBound();
        }
        return true;
    }

    /**
     * Tests if the constraint is consistent
     * with respect to the current state of domains.
     *
     * @return true iff the constraint is bound consistent
     *         (weaker than arc consistent)
     */
    protected boolean hasConsistentUpperBound() {
        if (ub.get() < rmemb.getInfRight()) {
            return false;
        } else {
            for (int i = 0; i < nbNegCoef; i++) {
                if (ub.get() + vars[i].getSup() * sCoeffs[i] < rmemb.getInfRight()) {
                    return false;
                }
            }
            for (int i = nbNegCoef; i < nbVars; i++) {
                if (ub.get() - vars[i].getInf() * sCoeffs[i] < rmemb.getInfRight()) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Tests if the constraint is consistent
     * with respect to the current state of domains.
     *
     * @return true iff the constraint is bound consistent
     *         (weaker than arc consistent)
     */
    protected boolean hasConsistentLowerBound() {
        if (lb.get() > rmemb.getSupRight()) {
            return false;
        } else {
            for (int i = 0; i < nbNegCoef; i++) {
                if (lb.get() - vars[i].getInf() * sCoeffs[i] > rmemb.getSupRight()) {
                    return false;
                }
            }
            for (int i = nbNegCoef; i < nbVars; i++) {
                if (lb.get() + vars[i].getSup() * sCoeffs[i] > rmemb.getSupRight()) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Checks if the constraint is entailed.
     *
     * @return Boolean.TRUE if the constraint is satisfied, Boolean.FALSE if it
     *         is violated, and null if the filtering algorithm cannot infer yet.
     */
    @Override
    public Boolean isEntailed() {
        if (op == IntLinComb.EQ) {
            int lb = computeLbFromScratch();
            int ub = computeUbFromScratch();
            int cstelb = objCoef * varCste.getInf();
            int csteub = objCoef * varCste.getSup();
            if (lb > csteub || ub < cstelb) {
                return Boolean.FALSE;
            } else if (lb == ub &&
                    varCste.isInstantiated() &&
                    objCoef * varCste.getVal() == lb) {
                return Boolean.TRUE;
            } else {
                return null;
            }
        } else if (op == IntLinComb.GEQ) {
            if (computeLbFromScratch() >= rmemb.getSupRight()) {
                return Boolean.TRUE;
            } else if (computeUbFromScratch() < rmemb.getInfRight()) {
                return Boolean.FALSE;
            } else {
                return null;
            }
        } else if (op == IntLinComb.LEQ) {
            if (computeUbFromScratch() <= rmemb.getInfRight()) {
                return Boolean.TRUE;
            } else if (computeLbFromScratch() > rmemb.getSupRight()) {
                return Boolean.FALSE;
            } else {
                return null;
            }
        } else {
            throw new SolverException("NEQ not managed by boolIntLinComb");
        }
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        int exp = 0;
        for (int i = 0; i < nbVars; i++) {
            exp += tuple[i] * sCoeffs[i];
        }
        if (op == IntLinComb.GEQ) {
            return exp + addcste >= objCoef * tuple[nbVars];
        } else if (op == IntLinComb.LEQ) {
            return exp + addcste <= objCoef * tuple[nbVars];
        } else if (op == IntLinComb.EQ) {
            return exp + addcste == objCoef * tuple[nbVars];
        } else if (op == IntLinComb.NEQ) {
            return exp + addcste != objCoef * tuple[nbVars];
        } else {
            throw new SolverException("operator unknown for BoolIntLinComb");
        }
    }

    @Override
    public String pretty() {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < nbVars; i++) {
            if (i > 0) {
                sb.append(" + ");
            }
            sb.append(sCoeffs[i]).append('*').append(vars[i].pretty());
        }
        sb.append(" + ").append(addcste);
        switch (op) {
            case IntLinComb.GEQ:
                sb.append(" >= ");
                break;
            case IntLinComb.LEQ:
                sb.append(" <= ");
                break;
            case IntLinComb.EQ:
                sb.append(" = ");
                break;
            case IntLinComb.NEQ:
                sb.append(" != ");
                break;
            default:
                sb.append(" ??? ");
                break;
        }
        sb.append(objCoef).append('*').append(varCste.pretty());
        return sb.toString();
    }

    /**
     * Computes the opposite of this constraint.
     *
     * @return a constraint with the opposite semantic  @param solver
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public AbstractSConstraint<IntDomainVar> opposite(Solver solver) {
        IntDomainVar[] bvs = new IntDomainVar[nbVars];
        System.arraycopy(vars, 0, bvs, 0, nbVars);
        if (op == IntLinComb.EQ) {
            IntDomainVar[] vs = new IntDomainVar[vars.length];
            System.arraycopy(vars, 0, vs, 0, vars.length);
            int[] coeff = new int[nbVars + 1];
            System.arraycopy(sCoeffs, 0, coeff, 0, nbVars);
            coeff[nbVars] = -objCoef;
            return (AbstractSConstraint<IntDomainVar>) solver.neq(solver.scalar(vs, coeff), -addcste);
            //throw new Error("NEQ not yet implemented in BoolIntLinComb for opposite");
        } else if (op == IntLinComb.NEQ) {
            return new BoolIntLinComb(solver.getEnvironment(), bvs, sCoeffs, varCste, objCoef, addcste, IntLinComb.EQ);
        } else if (op == IntLinComb.GEQ) {
            return new BoolIntLinComb(solver.getEnvironment(), bvs, sCoeffs, varCste, objCoef, addcste + 1, IntLinComb.LEQ);
        } else if (op == IntLinComb.LEQ) {
            return new BoolIntLinComb(solver.getEnvironment(), bvs, sCoeffs, varCste, objCoef, addcste - 1, IntLinComb.GEQ);
        } else {
            throw new SolverException("operator unknown for BoolIntLinComb");
        }
    }

    /**
     * Computes an upper bound estimate of a linear combination of variables.
     *
     * @return the new upper bound value
     */
    protected final int computeUbFromScratch() {
        int s = addcste;
        int i;
        for (i = 0; i < nbNegCoef; i++) {
            s += (vars[i].getInf() * sCoeffs[i]);
        }
        for (i = nbNegCoef; i < nbVars; i++) {
            s += (vars[i].getSup() * sCoeffs[i]);
        }
        return s;
    }

    /**
     * Computes a lower bound estimate of a linear combination of variables.
     *
     * @return the new lower bound value
     */
    protected final int computeLbFromScratch() {
        int s = addcste;
        int i;
        for (i = 0; i < nbNegCoef; i++) {
            s += (vars[i].getSup() * sCoeffs[i]);
        }
        for (i = nbNegCoef; i < nbVars; i++) {
            s += (vars[i].getInf() * sCoeffs[i]);
        }
        return s;
    }
}
