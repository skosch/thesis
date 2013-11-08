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

package choco.cp.solver.variables.real;

import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.structure.Couple;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.propagation.event.VarEvent;
import choco.kernel.solver.propagation.listener.RealPropagator;

/**
 * An event for real interval variable modifications.
 */
@SuppressWarnings({"unchecked"})
public class RealVarEvent<C extends AbstractSConstraint & RealPropagator>  extends VarEvent<RealVarImpl> {
    public static final int INCINF = 0;
    public static final int DECSUP = 1;

    public final static int EMPTYEVENT = 0;
    public final static int INFEVENT = 1;
    public final static int SUPEVENT = 2;
    public final static int BOUNDSEVENT = 3;

    public RealVarEvent(RealVarImpl var) {
        super(var);
    }

    public String toString() {
        return ("VarEvt(" + modifiedVar.toString() + ")[" + eventType + ":"
                + ((eventType & INFEVENT) != 0 ? "I" : "")
                + ((eventType & SUPEVENT) != 0 ? "S" : "")
                + "]");
    }

    public void clear() {
        this.eventType = EMPTYEVENT;
        cause = null;
        modifiedVar.getDomain().clearDeltaDomain();
    }

    protected boolean release() {
        return modifiedVar.getDomain().releaseDeltaDomain();
    }

    protected void freeze() {
        modifiedVar.getDomain().freezeDeltaDomain();
        cause = null;
        eventType = 0;
    }

    public boolean getReleased() {
        return modifiedVar.getDomain().getReleasedDeltaDomain();
    }

    public boolean propagateEvent() throws ContradictionException {
        //Logging statements really decrease performance
        //if(LOGGER.isLoggable(Level.FINER)) LOGGER.log(Level.FINER,"propagate {0}", this);
        // first, mark event
        int evtType = eventType;
        C evtCause = (C)cause;
        freeze();

        if ((propagatedEvents & INFEVENT) != 0 && (evtType & INFEVENT) != 0)
            propagateInfEvent(evtCause);
        if ((propagatedEvents & SUPEVENT) != 0 && (evtType & SUPEVENT) != 0)
            propagateSupEvent(evtCause);

//        if (evtType <= BOUNDSEVENT) {     // only two first bits (bounds) are on
//            if (evtType == INFEVENT)
//                propagateInfEvent(evtCause);
//            else if (evtType == SUPEVENT)
//                propagateSupEvent(evtCause);
//            else if (evtType == BOUNDSEVENT) {
//                propagateBoundsEvent(evtCause);
//            }
//        }
        // last, release event
        return release();
    }

    /**
     * Propagates the update to the upper bound
     */
    public void propagateSupEvent(C evtCause) throws ContradictionException {
        RealVarImpl v = getModifiedVar();
        DisposableIterator<Couple<C>> cit = v.getActiveConstraints(evtCause);

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
     * Propagates the update to the lower bound
     */
    public void propagateInfEvent(C evtCause) throws ContradictionException {
        RealVarImpl v = getModifiedVar();
        DisposableIterator<Couple<C>> cit = v.getActiveConstraints(evtCause);

        try {
            while (cit.hasNext()) {
                Couple<C> cc = cit.next();
                cc.c.awakeOnInf(cc.i);
            }
        } finally {
            cit.dispose();
        }

    }
}
