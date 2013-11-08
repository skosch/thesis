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

import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.propagation.listener.IntPropagator;

/**
 * An event dedicated to boolean variables
 **/
public class BoolVarEvent<C extends AbstractSConstraint & IntPropagator> extends IntVarEvent<C> {

    public BoolVarEvent(IntDomainVarImpl var) {
        super(var);
    }


    @Override
    protected void freeze() {
        cause = null;
        eventType = EMPTYEVENT;
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
    @Override
    protected boolean release() {
        return true;
    }
}
