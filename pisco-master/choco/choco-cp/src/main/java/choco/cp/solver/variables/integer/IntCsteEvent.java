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

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.propagation.listener.IntPropagator;

@SuppressWarnings({"unchecked"})
public class IntCsteEvent<C extends AbstractSConstraint & IntPropagator> extends IntVarEvent<C> {

    public IntCsteEvent(IntDomainVarImpl var) {
        super(var);
        eventType = EMPTYEVENT;
    }

    /**
     * useful for debugging
     */
    public String toString() {
        return ("VarEvt(" + modifiedVar + ")[" + eventType + ':'
                + ((eventType & INCINF_MASK) != 0 ? "I" : "")
                + ((eventType & DECSUP_MASK) != 0 ? "S" : "")
                + ((eventType & REMVAL_MASK) != 0 ? "r" : "")
                + ((eventType & INSTINT_MASK) != 0 ? "X" : "")
                + ']');
    }

    /**
     * Clears the var: delegates to the basic events.
     */
    public void clear() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    protected void freeze() {
        throw new UnsupportedOperationException();
    }

    public boolean getReleased() {
        throw new UnsupportedOperationException();
    }

    /**
     * Propagates the event through calls to the propagation engine.
     *
     * @return true if the event has been fully propagated (and can thus be discarded), false otherwise
     * @throws choco.kernel.solver.ContradictionException
     */
    public boolean propagateEvent() throws ContradictionException {
        throw new UnsupportedOperationException();
    }
}