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

package choco.cp.solver.variables.integer;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.structure.Couple;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.event.VarEvent;
import choco.kernel.solver.propagation.listener.IntPropagator;
import choco.kernel.solver.variables.integer.IntDomain;
import gnu.trove.TIntHashSet;

@SuppressWarnings({"unchecked"})
public class IntVarEvent<C extends AbstractSConstraint & IntPropagator> extends VarEvent<IntDomainVarImpl> {

    /**
     * Constants for the <i>eventType</i> bitvector: index of bit for updates to lower bound of IntVars
     */
    public static final int INCINF = 0;

    /**
     * Constants for the <i>eventType</i> bitvector: index of bit for updates to upper bound of IntVars
     */
    public static final int DECSUP = 1;

    /**
     * Constants for the <i>eventType</i> bitvector: index of bit for holes in the domain of IntVars
     */
    public static final int REMVAL = 2;

    /**
     * Constants for the <i>eventType</i> bitvector: index of bit for instantiations of IntVars
     */
    public static final int INSTINT = 3;

    /**
     * Constants for the <i>eventType</i> bitvector: value of bitvector no eventtype
     */
    public static final int NO_MASK = 0;

    /**
     * Constants for the <i>eventType</i> bitvector: value of bitvector for updates to lower bound of IntVars
     */
    public static final int INCINF_MASK = 1;
    @Deprecated
    public static final int INCINFbitvector = INCINF_MASK;

    /**
     * Constants for the <i>eventType</i> bitvector: value of bitvector for updates to upper bound of IntVars
     */
    public static final int DECSUP_MASK = 2;
    @Deprecated
    public static final int DECSUPbitvector = DECSUP_MASK;

    /**
     * Constants for the <i>eventType</i> bitvector: value of bitvector for updates to both bound of IntVars
     */
    public static final int BOUNDS_MASK = 3;
    @Deprecated
    public static final int BOUNDSbitvector = BOUNDS_MASK;

    /**
     * Constants for the <i>eventType</i> bitvector: value of bitvector for holes in the domain of IntVars
     */
    public static final int REMVAL_MASK = 4;
    @Deprecated
    public static final int REMVALbitvector = REMVAL_MASK;

    /**
     * Constants for the <i>eventType</i> bitvector: value of bitvector for instantiations of IntVars
     */
    public static final int INSTINT_MASK = 8;
    @Deprecated
    public static final int INSTINTbitvector = INSTINT_MASK;

    final IntDomain _domain;

     public static final int[] EVENTS = new int[]{INCINF_MASK, DECSUP_MASK, REMVAL_MASK, INSTINT_MASK};

    public IntVarEvent(IntDomainVarImpl var) {
        super(var);
        _domain = var.getDomain();
        eventType = EMPTYEVENT;
    }

    /**
     * useful for debugging
     */
    public String toString() {
        return ("VarEvt(" + modifiedVar.pretty() + ")[" + eventType + ":"
                + ((eventType & INCINF_MASK) != 0 ? "I" : "")
                + ((eventType & DECSUP_MASK) != 0 ? "S" : "")
                + ((eventType & REMVAL_MASK) != 0 ? "r" : "")
                + ((eventType & INSTINT_MASK) != 0 ? "X" : "")
                + "]");
    }

    /**
     * Clears the var: delegates to the basic events.
     */
    public void clear() {
        this.eventType = EMPTYEVENT;
//        oldCause = NOEVENT;
        cause = null;
        _domain.clearDeltaDomain();
    }

    /**
     * the event had been "frozen", (since the call to freeze), while it was handled by the propagation engine:
     * This meant that the meaning of the event could not be changed: it represented
     * a static set of value removals, during propagation.
     * Now, the event becomes "open" again: new value removals can be hosted, the delta domain can
     * accept that further values are removed.
     * In case value removals happened while the event was frozen, the release method returns false
     * (the event cannot be released, it must be handled once more). Otherwise (the standard behavior),
     * the method returns true
     */
    protected boolean release() {
        // we no longer use the shortcut (if eventType == EMPTYEVENT => nothing to do) because of event transformation
        // (in case a removal turned out to be an instantiation, we need to release the delta domain associated to the removal)
        // boolean anyUpdateSinceFreeze = ((eventType != EMPTYEVENT) || (cause != NOEVENT));  // note: these two tests should be equivalent
        // anyUpdateSinceFreeze = (anyUpdateSinceFreeze || !(getIntVar().getDomain().releaseDeltaDomain()));
        // return !anyUpdateSinceFreeze;
        return _domain.releaseDeltaDomain();
    }

    protected void freeze() {
        _domain.freezeDeltaDomain();
//        oldCause = NOEVENT;
        cause = null;
        eventType = EMPTYEVENT;
    }

    public boolean getReleased() {
        return _domain.getReleasedDeltaDomain();
    }

    /**
     * Propagates the event through calls to the propagation engine.
     *
     * @return true if the event has been fully propagated (and can thus be discarded), false otherwise
     * @throws ContradictionException
     */
    public boolean propagateEvent() throws ContradictionException {
        // /!\ Logging statements really decrease performance
        //if(LOGGER.isLoggable(Level.FINER)) {LOGGER.log(Level.FINER, "propagate {0}", this);}
        // first, mark event
        int evtType = eventType;
//        int evtCause = oldCause;
        C evtCause = (C) cause;
        freeze();
        if ((propagatedEvents & INSTINT_MASK) != 0 && (evtType & INSTINT_MASK) != 0)
            propagateInstEvent(evtCause);
        if ((propagatedEvents & INCINF_MASK) != 0 && (evtType & INCINF_MASK) != 0)
            propagateInfEvent(evtCause);
        if ((propagatedEvents & DECSUP_MASK) != 0 && (evtType & DECSUP_MASK) != 0)
            propagateSupEvent(evtCause);
        if ((propagatedEvents & REMVAL_MASK) != 0 && (evtType & REMVAL_MASK) != 0)
            propagateRemovalsEvent(evtCause);

        // last, release event
        return release();
    }

    /**
     * Propagates the instantiation event
     */
    public void propagateInstEvent(C evtCause) throws ContradictionException {
        IntDomainVarImpl v = modifiedVar;
        DisposableIterator<Couple<C>> cit = v.getActiveConstraints(INSTINT_MASK, evtCause);
        try {
            while (cit.hasNext()) {
                Couple<C> cc = cit.next();
                cc.c.awakeOnInst(cc.i);
            }
        } finally {
            cit.dispose();
        }
    }


    /**
     * Propagates the update to the lower bound
     */
    public void propagateInfEvent(C evtCause) throws ContradictionException {
        IntDomainVarImpl v = modifiedVar;
        DisposableIterator<Couple<C>> cit = v.getActiveConstraints(INCINF_MASK, evtCause);
        try {
            while (cit.hasNext()) {
                Couple<C> cc = cit.next();
                cc.c.awakeOnInf(cc.i);
            }
        } finally {
            cit.dispose();
        }
    }

    /**
     * Propagates the update to the upper bound
     */
    public void propagateSupEvent(C evtCause) throws ContradictionException {
        IntDomainVarImpl v = modifiedVar;
        DisposableIterator<Couple<C>> cit = v.getActiveConstraints(DECSUP_MASK, evtCause);
        try {
            while (cit.hasNext()) {
                Couple<C> cc = cit.next();
                cc.c.awakeOnSup(cc.i);
            }
        } finally {
            cit.dispose();
        }
    }

    /**
     * Propagates a set of value removals
     */
    public void propagateRemovalsEvent(C evtCause) throws ContradictionException {
        IntDomainVarImpl v = modifiedVar;
        DisposableIterator<Couple<C>> cit = v.getActiveConstraints(REMVAL_MASK, evtCause);
        try {
            while (cit.hasNext()) {
                Couple<C> cc = cit.next();
                DisposableIntIterator iter = _domain.getDeltaIterator();
                try {
                    cc.c.awakeOnRemovals(cc.i, iter);
                } finally {
                    iter.dispose();
                }
            }
        } finally {
            cit.dispose();
        }
    }

    private int promoteEvent(int basicEvt) {
        switch (basicEvt) {
            case INSTINT:
                return INSTINT_MASK + INCINF_MASK + DECSUP_MASK + REMVAL_MASK;

            case INCINF:
                return INCINF_MASK + REMVAL_MASK;

            case DECSUP:
                return DECSUP_MASK + REMVAL_MASK;

            case REMVAL:
                return REMVAL_MASK;

            default:
                return 1 << basicEvt;
        }
    }

    public void recordEventTypeAndCause(int basicEvt, final SConstraint constraint, final boolean forceAwake) {
        // if no such event was active on the same variable
//        if ((oldCause == NOEVENT) || (eventType == EMPTYEVENT)) {  // note: these two tests should be equivalent
        if (eventType == EMPTYEVENT) {
//            assert((cause == null));
            // the varevent is reduced to basicEvt, and the cause is recorded
            eventType = promoteEvent(basicEvt);
            if (!forceAwake) {
                cause = constraint;
            }
//            cause = constraint;
        } else {
            // otherwise, this basic event is added to all previous updates that are possibly mending on the same variable
            eventType = (eventType | promoteEvent(basicEvt));
            // in case the cause of this update is different from the previous cause, all causes are forgotten
            // (so that the constraints that caused the event will be reawaken)
            if (forceAwake || cause != constraint) {
                cause = null;
            }
        }
    }
}