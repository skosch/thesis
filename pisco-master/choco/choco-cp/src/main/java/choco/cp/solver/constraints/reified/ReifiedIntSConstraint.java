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

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.Extension;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.variables.AbstractVar;
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
public class ReifiedIntSConstraint extends AbstractLargeIntSConstraint{

    AbstractIntSConstraint cons;
    AbstractIntSConstraint oppositeCons;

    //scopeCons[i] = j means that the i-th variable of cons is the j-th in reifiedIntConstraint
    protected int[] scopeCons;
    //scopeOCons[i] = j means that the i-th variable of oppositeCons is the j-th in reifiedIntConstraint
    protected int[] scopeOCons;

    @SuppressWarnings({"unchecked"})
    private static IntDomainVar[] makeTableVar(IntDomainVar bool,
                                              AbstractIntSConstraint cons, AbstractIntSConstraint oppcons) {
        THashSet<IntDomainVar> consV = new THashSet<IntDomainVar>(cons.getNbVars() + oppcons.getNbVars()+1);
        for (int i = 0; i < cons.getNbVars(); i++){
            consV.add(cons.getVar(i));
        }
        for (int i = 0; i < oppcons.getNbVars(); i++){
            consV.add(oppcons.getVar(i));
        }
        consV.add(bool);
        IntDomainVar[] vars = new IntDomainVar[consV.size()];
        consV.remove(bool);
        vars[0] = bool;
        int i = 1;
        for (IntDomainVar var: consV) {
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
    ReifiedIntSConstraint(final IntDomainVar bool, final AbstractIntSConstraint cons, final Solver solver) {
        this(bool, cons, (AbstractIntSConstraint) cons.opposite(solver));
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
    ReifiedIntSConstraint(final IntDomainVar bool, final AbstractIntSConstraint cons, final AbstractIntSConstraint oppositeCons) {
        super(Math.max(cons.getPriority(), oppositeCons.getPriority()), makeTableVar(bool, cons, oppositeCons));
        this.cons = cons;
        this.oppositeCons = oppositeCons;
        init();
    }

    /**
     * Adds a new extension.
     *
     * @param extensionNumber should use the number returned by getAbstractSConstraintExtensionNumber
     */
    @Override
    public void addExtension(final int extensionNumber) {
        super.addExtension(extensionNumber);
        final Extension ext = extensions[extensionNumber];
        cons.setExtension(ext, extensionNumber);
        oppositeCons.setExtension(ext, extensionNumber);
    }

    void init() {
        tupleCons = new int[cons.getNbVars()];
        tupleOCons = new int[oppositeCons.getNbVars()];
        scopeCons = new int[cons.getNbVars()];
        scopeOCons = new int[oppositeCons.getNbVars()];
        for (int i = 0; i < cons.getNbVars(); i++) {
            final IntDomainVar v = cons.getVar(i);
            for (int j = 0; j < vars.length; j++) {
                if (v.equals(vars[j])) {
                    scopeCons[i] = j;
                    break;
                }
            }
        }
        for (int i = 0; i < oppositeCons.getNbVars(); i++) {
            final IntDomainVar v = oppositeCons.getVar(i);
            for (int j = 0; j < vars.length; j++) {
                if (v.equals(vars[j])) {
                    scopeOCons[i] = j;
                    break;
                }
            }
        }
    }

    //assume that the boolean is known
    void filterReifiedConstraintFromBool() throws ContradictionException {
        if (vars[0].isInstantiatedTo(1)) {
            cons.awake();
        } else {
            oppositeCons.awake();
        }
    }

    public void filterReifiedConstraintFromCons() throws ContradictionException {
        Boolean isEntailed = cons.isEntailed();
        if (isEntailed != null) {
            if (isEntailed) {
                vars[0].instantiate(1, this, false);
            } else {
                vars[0].instantiate(0, this, true);
            }
        }
    }

    @Override
    public final int getFilteredEventMask(final int idx) {
        if (vars[idx].hasEnumeratedDomain()) {
            return IntVarEvent.REMVAL_MASK;
        } else {
            return IntVarEvent.BOUNDS_MASK;
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

    public final void awakeOnInf(final int idx) throws ContradictionException {
        filter();
    }

    public final void awakeOnSup(final int idx) throws ContradictionException {
        filter();
    }

    public final void awakeOnInst(final int idx) throws ContradictionException {
        filter();
    }

    public final void awakeOnRem(final int idx, final int x) throws ContradictionException {
        filter();
    }

    public final void awakeOnRemovals(final int idx, final DisposableIntIterator deltaDomain) throws ContradictionException {
        filter();
    }

    public final void awakeOnBounds(final int varIndex) throws ContradictionException {
        filter();
    }

    public void awake() throws ContradictionException {
        filter();
    }

    void addListener(final AbstractIntSConstraint thecons) {
        if (thecons instanceof ReifiedIntSConstraint) {
            final ReifiedIntSConstraint rcons = (ReifiedIntSConstraint) thecons;
            addListener(rcons.cons);
            addListener(rcons.oppositeCons);
        }
        final int n = thecons.getNbVars();
        for (int i = 0; i < n; i++) {
            thecons.setConstraintIndex(i, getIndex((AbstractVar) thecons.getVar(i)));
        }
    }

    int getIndex(final AbstractVar v) {
        for (int i = 0; i < vars.length; i++) {
            if (vars[i] == v) return cIndices[i];
        }
        return -1; //should never go there !
    }

    public void addListener(final boolean dynamicAddition) {
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
    public void setPropagationEngine(final PropagationEngine propEng) {
        super.setPropagationEngine(propEng);
        cons.setPropagationEngine(propEng);
        oppositeCons.setPropagationEngine(propEng);
    }

    public String pretty() {
        final StringBuilder sb = new StringBuilder("(");
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

    //temporary data to store tuples
    int[] tupleCons;
    int[] tupleOCons;

    /**
     * TEMPORARY: if not overriden by the constraint, throws an error
     * to avoid bug using reified constraints in constraints
     * that have not been changed to fulfill this api yet !
     *
     * @param tuple value for each variable
     * @return true if the tuple satisfies the constraint
     */
    public boolean isSatisfied(final int[] tuple) {
        final int val = tuple[0];
        for (int i = 0; i < tupleCons.length; i++) {
            tupleCons[i] = tuple[scopeCons[i]];
        }
        if (val == 1) {
            return cons.isSatisfied(tupleCons);
        } else {
            for (int i = 0; i < tupleOCons.length; i++) {
                tupleOCons[i] = tuple[scopeOCons[i]];
            }
            return oppositeCons.isSatisfied(tupleOCons);
        }
    }
}
