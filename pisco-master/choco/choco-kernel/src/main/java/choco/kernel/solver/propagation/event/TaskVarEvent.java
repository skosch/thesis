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

package choco.kernel.solver.propagation.event;

import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.structure.Couple;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.propagation.listener.TaskPropagator;
import choco.kernel.solver.variables.scheduling.TaskVar;

/**
 * User : cprudhom
 * Mail : cprudhom(a)emn.fr
 * Date : 18 févr. 2010
 * Since : Choco 2.1.1
 *
 * Use carefully.
 * !! This is required ONLY for transverse constraints!!
 * Normal behaviour of task var modification should be based on IntVarEvent.
 */
@SuppressWarnings({"unchecked"})
public class TaskVarEvent <C extends AbstractSConstraint & TaskPropagator>extends VarEvent<TaskVar> {

    public final static int HYPDOMMOD = 0;

    public final static int HYPDOMMODbitvector = 1;


    /**
     * Constructs a variable event for the specified variable and with the given
     * basic events.
     */
    public TaskVarEvent(TaskVar var) {
        super(var);
    }


    /**
     * Clears the var: delegates to the basic events.
     */
    @Override
    public void clear() {
        this.eventType = EMPTYEVENT;
        cause = null;
    }

    /**
     * Propagates the event through calls to the propagation engine.
     *
     * @return true if the event has been fully propagated (and can thus be discarded), false otherwise
     * @throws choco.kernel.solver.ContradictionException
     *
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public boolean propagateEvent() throws ContradictionException {
        int evtType = eventType;
		C evtCause = (C)cause;

        if ((propagatedEvents & HYPDOMMODbitvector) != 0 && (evtType & HYPDOMMODbitvector) != 0)
            propagateHypDomModEvent(evtCause);
        
        cause = null;
        eventType = EMPTYEVENT;
        return false;
    }

    /**
	 * Propagates the instantiation event
	 */
	public void propagateHypDomModEvent(C evtCause) throws ContradictionException {
		TaskVar v = getModifiedVar();
        DisposableIterator<Couple<C>> cit = v.getActiveConstraints(evtCause);
        try{
            while(cit.hasNext()){
                Couple<C> cc = cit.next();
                cc.c.awakeOnHypDomMod(cc.i);
            }
        }finally{
            cit.dispose();
        }
	}
}
