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

package choco.cp.solver.propagation;


import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.propagation.event.PropagationEvent;
import choco.kernel.solver.propagation.queue.EventQueue;
import choco.kernel.visu.VisuFactory;

import java.util.ArrayDeque;
import java.util.Queue;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 29 oct. 2008
 */

public class VariableEventQueue implements EventQueue {


    /**
     * FIFO queue to deal with variable events
      */
    protected Queue<PropagationEvent> queue = new ArrayDeque<PropagationEvent>();

    /**
	 * The last popped var (may be useful for flushing popping events).
	 */
	protected PropagationEvent lastPopped = null;

   /**
     * Clear datastructures for safe reuses
     */
    public void clear(){
        queue.clear();
        lastPopped = null;
    }

	/**
	 * Checks if the queue is empty.
	 */
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	/**
	 * Propagates some events: in fact all the events of the queue, since there
	 * are the most important events.
	 *
	 * @throws choco.kernel.solver.ContradictionException
	 */
	public void propagateAllEvents() throws ContradictionException {
		while (queue.size() != 0) {
			final PropagationEvent evt = popEvent();
			//ChocoLogging.getMainLogger().info(evt.toString());
			//ChocoLogging.flushLogs();
			evt.propagateEvent();
		}
	}

	/**
	 * Propagates one single event from the queue (usefull for tracing)
	 *
	 * @throws choco.kernel.solver.ContradictionException
	 */
	public void propagateOneEvent() throws ContradictionException {
		if (queue.size() != 0) {
			popEvent().propagateEvent();
		}
	}

	/**
	 * Pops an event to propagate.
	 */
	public PropagationEvent popEvent() {
		PropagationEvent event = queue.poll();
		lastPopped = event;
		return event;
	}

	/**
	 * Adds an event to the queue.
	 */

	public boolean pushEvent(PropagationEvent event) {
		queue.add(event);
		return true;
	}

	/**
	 * Updates the priority level of an event (after adding a basic var).
	 */

	/*
	 * public void updatePriority(PropagationEvent event) {
	 * queue.updatePriority(event); }
	 */

	/**
	 * Removes all the events (including the popping one).
	 */

	public void flushEventQueue() {
		if (null != lastPopped) {
			lastPopped.clear();
		}

        while(!queue.isEmpty()){
            queue.remove().clear();
        }

	}

	/**
	 * Removes an event. This method should not be useful for variable events.
	 */

	public boolean remove(PropagationEvent event) {
		return queue.remove(event);
	}

	public int size() {
		return queue.size();
	}

	public PropagationEvent get(int idx) {
		for (PropagationEvent event : queue) {
			if (idx == 0) {
				return event;
			} else {
				idx--;
			}
		}
		return null;
	}
}
