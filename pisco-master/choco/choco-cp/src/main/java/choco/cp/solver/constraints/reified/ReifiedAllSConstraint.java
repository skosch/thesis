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

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.Extension;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.propagation.listener.IntPropagator;
import choco.kernel.solver.propagation.listener.RealPropagator;
import choco.kernel.solver.propagation.listener.SetPropagator;
import choco.kernel.solver.variables.AbstractVar;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.THashSet;                                                       

/**
 * A constraint that allows to reify another constraint into a boolean value.
 * b = 1 <=> cons is satisfied
 * b = 0 <=> oppositeCons is satisfied
 * <p/>
 * cons and oppositeCons do not need to be really the constraint and its
 * opposite, it can be two different constraints as well
 */
public class ReifiedAllSConstraint extends AbstractSConstraint implements IntPropagator, SetPropagator, RealPropagator {

    protected final AbstractSConstraint cons;
    protected final AbstractSConstraint oppositeCons;

    private final IntDomainVar bool;

    @SuppressWarnings({"unchecked"})
    public static <C extends AbstractSConstraint> Var[] makeTableVar(IntDomainVar bool, C cons, C oppcons) {
        THashSet<Var> consV = new THashSet<Var>(cons.getNbVars() + oppcons.getNbVars()+1);
        for (int i = 0; i < cons.getNbVars(); i++){
            consV.add(cons.getVar(i));
        }
        for (int i = 0; i < oppcons.getNbVars(); i++){
            consV.add(oppcons.getVar(i));
        }
        consV.add(bool);
        Var[] vars = new Var[consV.size()];
        consV.remove(bool);
        vars[0] = bool;
        int i = 1;
        for (Var var: consV) {
            vars[i] = var;
            i++;
        }
        return vars;
    }

    /**
     * A constraint that allows to reify another constraint into a boolean value.
     * b = 1 <=> cons is satisfied
     * b = 0 <=> oppositeCons is satisfied
     * if the opposite methode of the constraint is not defined, use the other constructor
     * by giving yourself the opposite constraint !
     * @param bool reified variable
     * @param cons the reified constraint
     * @param solver
     */
    ReifiedAllSConstraint(final IntDomainVar bool, final AbstractSConstraint cons, final Solver solver) {
        this(bool, cons, cons.opposite(solver));
    }

    /**
     * A constraint that allows to reify another constraint into a boolean value.
     * b = 1 <=> cons is satisfied
     * b = 0 <=> oppositeCons is satisfied
     * <p/>
     * cons and oppositeCons do not need to be really the constraint and its
     * opposite, it can be two different constraints as well
     * @param bool reified variable
     * @param cons the reified constraint
     * @param oppositeCons the opposite reified constraint
     */
    ReifiedAllSConstraint(final IntDomainVar bool, final AbstractSConstraint cons,
                                 final AbstractSConstraint oppositeCons) {
        super(makeTableVar(bool, cons, oppositeCons));
        this.cons = cons;
        this.oppositeCons = oppositeCons;
        this.bool = bool;
    }

    /**
     * Adds a new extension.
     *
     * @param extensionNumber should use the number returned by getAbstractSConstraintExtensionNumber
     */
    @Override
    public final void addExtension(final int extensionNumber) {
        super.addExtension(extensionNumber);
        Extension ext = extensions[extensionNumber];
        cons.setExtension(ext, extensionNumber);
        oppositeCons.setExtension(ext, extensionNumber);
    }

    //assume that the boolean is known
    public final void filterReifiedConstraintFromBool() throws ContradictionException {
        if (bool.isInstantiatedTo(1)) {
            cons.awake();
        } else {
            oppositeCons.awake();
        }
    }

    public void filterReifiedConstraintFromCons() throws ContradictionException {
        Boolean isEntailed = cons.isEntailed();
        if (isEntailed != null) {
            if (isEntailed) {
                bool.instantiate(1, this, false);
            } else {
                bool.instantiate(0, this, true);
            }
        }
    }

    public final void filter() throws ContradictionException {
        if (vars[0].isInstantiated()) {
            filterReifiedConstraintFromBool();
        } else {
            filterReifiedConstraintFromCons();
        }
    }

    public final void propagate() throws ContradictionException {
        filter();
    }

    /**
     * Default propagation on improved lower bound: propagation on domain revision.
     */
    @Override
    public void awakeOnInf(final int varIdx) throws ContradictionException {
        filter();
    }

    /**
     * Default propagation on improved upper bound: propagation on domain revision.
     */
    @Override
    public void awakeOnSup(final int varIdx) throws ContradictionException {
        filter();
    }

    /**
     * Default propagation on one value removal: propagation on domain revision.
     */
    @Override
    public void awakeOnRem(final int varIdx, final int val) throws ContradictionException {
        filter();
    }

    @Override
    public void awakeOnRemovals(final int varIdx, final DisposableIntIterator deltaDomain) throws ContradictionException {
        filter();
    }

    @Override
    public void awakeOnBounds(final int varIdx) throws ContradictionException {
        filter();
    }

    /**
     * Default propagation on kernel modification: propagation on adding a value to the kernel.
     */
    @Override
    public void awakeOnKer(final int varIdx, final int x) throws ContradictionException {
        filter();
    }

    /**
     * Default propagation on enveloppe modification: propagation on removing a value from the enveloppe.
     */
    @Override
    public void awakeOnEnv(final int varIdx, final int x) throws ContradictionException {
        filter();
    }

    /**
     * Default propagation on instantiation.
     */
    @Override
    public void awakeOnInst(final int varIdx) throws ContradictionException {
        filter();
    }

    @Override
    public void awakeOnkerAdditions(final int sIdx, final DisposableIntIterator deltaDomain) throws ContradictionException {
        filter();
    }

    @Override
    public void awakeOnEnvRemovals(final int sIdx, final DisposableIntIterator deltaDomain) throws ContradictionException {
        filter();
    }

    /**
     * tests if the constraint is consistent with respect to the current state of domains
     *
     * @return wether the constraint is consistent
     */
    @Override
    public final boolean isConsistent() {
        return Boolean.TRUE.equals(isEntailed());
    }

    @SuppressWarnings({"unchecked"})
    public final void addListener(AbstractSConstraint thecons) {
        if (thecons instanceof ReifiedAllSConstraint) {
            ReifiedAllSConstraint rcons = (ReifiedAllSConstraint) thecons;
            addListener(rcons.cons);
            addListener(rcons.oppositeCons);
        }
        int n = thecons.getNbVars();
        for (int i = 0; i < n; i++) {
            thecons.setConstraintIndex(i, getIndex((AbstractVar) thecons.getVar(i)));
        }
    }

    public final int getIndex(AbstractVar v) {
        for (int i = 0; i < vars.length; i++) {
            if (vars[i] == v) return cIndices[i];
        }
        return -1; //should never go there !
    }

    public final void addListener(boolean dynamicAddition) {
        super.addListener(dynamicAddition);
        addListener(cons);
        addListener(oppositeCons);
    }

    /**
     * Define the propagation engine within the constraint.
     * Mandatory to throw {@link choco.kernel.solver.ContradictionException}.
     *
     * @param propEng the current propagation engine
     */
    @Override
    public final void setPropagationEngine(PropagationEngine propEng) {
        super.setPropagationEngine(propEng);
        cons.setPropagationEngine(propEng);
        oppositeCons.setPropagationEngine(propEng);
    }

    public final String pretty() {
        StringBuilder sb = new StringBuilder("(");
        sb.append(" 1");
        sb.append("<=>").append(cons.pretty());
        if (oppositeCons != null) {
            sb.append(" -- 0");
            sb.append("<=>").append(oppositeCons.pretty());
        }
        sb.append(')');
        sb.append('~').append(vars[0].pretty());
        return sb.toString();
    }

    /**
     * <i>Semantic:</i>
     * Testing if the constraint is satisfied.
     * Note that all variables involved in the constraint must be
     * instantiated when this method is called.
     *
     * @return true if the constraint is satisfied
     */
    @Override
    public boolean isSatisfied() {
        if(isCompletelyInstantiated()){
            if(bool.isInstantiatedTo(1)){
                return cons.isSatisfied();
            }else{
                return oppositeCons.isSatisfied();
            }
        }
        return false;
    }

    @Override
    public SConstraintType getConstraintType() {
        return SConstraintType.MIXED;
    }

    @Override
    public boolean isSatisfied(final int[] tuple) {
        throw new UnsupportedOperationException(this + " needs to implement isSatisfied(int[] tuple) to be embedded in reified constraints");
    }

}