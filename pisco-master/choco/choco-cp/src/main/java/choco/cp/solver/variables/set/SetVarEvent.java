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

package choco.cp.solver.variables.set;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.structure.Couple;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.event.VarEvent;
import choco.kernel.solver.propagation.listener.SetPropagator;
import choco.kernel.solver.variables.set.SetSubDomain;

/*
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 6 juin 2004
 * Since : Choco 2.0.0
 *
 */

@SuppressWarnings({"unchecked"})
public class SetVarEvent<C extends AbstractSConstraint & SetPropagator> extends VarEvent<SetVarImpl> {

    /**
     * Constants for the <i>eventType</i> bitvector: index of bit for events on SetVars
     */
    public static final int REMENV = 0;
    public static final int ADDKER = 1;
    public static final int INSTSET = 2;

    public static final int REMENV_MASK = 1;
    @Deprecated
    public static final int ENVEVENT = REMENV_MASK;

    public static final int ADDKER_MASK = 2;
    @Deprecated
    public static final int KEREVENT = ADDKER_MASK;

    public static final int INSTSET_MASK = 4;
    @Deprecated
    public static final int INSTSETEVENT = INSTSET_MASK;

    private final SetSubDomain _kdomain, _edomain;

    public SetVarEvent(SetVarImpl var) {
        super(var);
        _kdomain = modifiedVar.getDomain().getKernelDomain();
        _edomain = modifiedVar.getDomain().getEnveloppeDomain();
        eventType = EMPTYEVENT;
    }

    /**
     * useful for debugging
     */
    public String toString() {
        return ("VarEvt(" + modifiedVar.toString() + ")[" + eventType + ":"
                + ((eventType & REMENV_MASK) != 0 ? "E" : "")
                + ((eventType & ADDKER_MASK) != 0 ? "K" : "")
                + ((eventType & INSTSET_MASK) != 0 ? "X" : "")
                + "]");
    }

    /**
     * Clears the var: delegates to the basic events.
     */
    public void clear() {
        this.eventType = EMPTYEVENT;
        cause = null;
        _edomain.clearDeltaDomain();
        _kdomain.clearDeltaDomain();
    }


    protected void freeze() {
        _edomain.freezeDeltaDomain();
        _kdomain.freezeDeltaDomain();
        cause = null;
        eventType = 0;

    }

    protected boolean release() {
        return _edomain.releaseDeltaDomain() &&
                _kdomain.releaseDeltaDomain();
    }

    public boolean getReleased() {
        return _edomain.getReleasedDeltaDomain() &&
                _kdomain.getReleasedDeltaDomain();
    }

    /**
     * Propagates the event through calls to the propagation engine.
     *
     * @return true if the event has been fully propagated (and can thus be discarded), false otherwise
     * @throws choco.kernel.solver.ContradictionException
     *
     */
    @Override
    public boolean propagateEvent() throws ContradictionException {
        // /!\  Logging statements really decrease performance
        //if(LOGGER.isLoggable(Level.FINER)) {LOGGER.log(Level.FINER, "propagate {0}", this);}
        // first, mark event
        int evtType = eventType;
        C evtCause = (C) cause;
        freeze();

        if ((propagatedEvents & INSTSET_MASK) != 0 && (evtType & INSTSET_MASK) != 0)
            propagateInstEvent(evtCause);
        if ((propagatedEvents & REMENV_MASK) != 0 && (evtType & REMENV_MASK) != 0)
            propagateEnveloppeEvents(evtCause);
        if ((propagatedEvents & ADDKER_MASK) != 0 && (evtType & ADDKER_MASK) != 0)
            propagateKernelEvents(evtCause);

//		if (evtType >= INSTSETEVENT)
//			propagateInstEvent(evtCause);
//		else if (evtType <= BOUNDSEVENT) {
//			if (evtType == ENVEVENT)
//				propagateEnveloppeEvents(evtCause);
//			else if (evtType == KEREVENT)
//				propagateKernelEvents(evtCause);
//			else if (evtType == BOUNDSEVENT) {
//				propagateKernelEvents(evtCause);
//				propagateEnveloppeEvents(evtCause);
//			}
//		}

        // last, release event
        return release();
    }

    /**
     * Propagates the instantiation event
     */
    public void propagateInstEvent(C evtCause) throws ContradictionException {
        SetVarImpl v = getModifiedVar();
        DisposableIterator<Couple<C>> cit = v.getActiveConstraints(evtCause);

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
     * Propagates a set of value removals
     */
    public void propagateKernelEvents(C evtCause) throws ContradictionException {
        SetVarImpl v = getModifiedVar();
        DisposableIterator<Couple<C>> cit = v.getActiveConstraints(evtCause);
        try {
            while (cit.hasNext()) {
                Couple<C> cc = cit.next();
                DisposableIntIterator kit = _kdomain.getDeltaIterator();
                try {
                    cc.c.awakeOnkerAdditions(cc.i, kit);
                } finally {
                    kit.dispose();
                }
            }
        } finally {
            cit.dispose();
        }
    }

    /**
     * Propagates a set of value removals
     */
    public void propagateEnveloppeEvents(C evtCause) throws ContradictionException {
        SetVarImpl v = getModifiedVar();
        DisposableIterator<Couple<C>> cit = v.getActiveConstraints(evtCause);
        try {
            while (cit.hasNext()) {
                Couple<C> cc = cit.next();
                DisposableIntIterator eit = _edomain.getDeltaIterator();
                try {
                    cc.c.awakeOnEnvRemovals(cc.i, eit);
                } finally {
                    eit.dispose();
                }
            }
        } finally {
            cit.dispose();
        }
    }

    private int promoteEvent(int basicEvt) {
        switch (basicEvt) {
            case INSTSET:
                return INSTSET_MASK + ADDKER_MASK + REMENV_MASK;

            case ADDKER:
                return ADDKER_MASK;

            case REMENV:
                return REMENV_MASK;

            default:
                return 1 << basicEvt;
        }
    }

    public void recordEventTypeAndCause(int basicEvt, final SConstraint constraint, final boolean forceAwake) {
        if (eventType == EMPTYEVENT) {
            // the varevent is reduced to basicEvt, and the cause is recorded
            eventType = promoteEvent(basicEvt);
            if (!forceAwake) {
                cause = constraint;
            }
        } else {
            // otherwise, this basic event is added to all previous updates that are possibly mending on the same variable
            eventType = (eventType | promoteEvent(basicEvt));
            // in case the cause of this update is different from the previous cause, all causes are forgotten
            // (so that the constraints that caused the event will be reawaken)
            if (cause != constraint) {
                cause = null;
            }
        }
    }

}
