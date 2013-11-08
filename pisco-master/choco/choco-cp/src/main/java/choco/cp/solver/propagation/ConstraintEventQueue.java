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

import choco.kernel.common.util.objects.BipartiteSet;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.propagation.event.PropagationEvent;
import choco.kernel.solver.propagation.queue.EventQueue;

import java.util.Iterator;

/**
 * Implements an {@link choco.kernel.solver.propagation.queue.EventQueue} for managing the constraint awake events.
 */
public class ConstraintEventQueue implements EventQueue {

	/**
	 * The propagation engine using this queue.
	 */

	private PropagationEngine engine;


	/**
	 * A private structure to store all the constraint. The left part of the bipartite
	 * set contains the events to propagate.
	 */

	private BipartiteSet<PropagationEvent> partition;


	/**
	 * Constructs a new queue for the specified engine.
	 */

	public ConstraintEventQueue(PropagationEngine engine) {
		this.engine = engine;
		this.partition = new BipartiteSet<PropagationEvent>();
	}

	/**
	 * Clear datastructures for safe reuses
	 */
	public void clear() {
		partition.clear();
	}


	/**
	 * Checks if the queue is empty.
	 */

	public boolean isEmpty() {
		return this.partition.getNbLeft() == 0;
	}


	/**
	 * Pops the next var to propagate.
	 */

	public PropagationEvent popEvent() {
		PropagationEvent event = this.partition.moveLastLeft();
		assert(event != null);
		//        if (event == null) {
		//            LOGGER.severe("Error: There is no more events in the queue.");
		//        } else {
		if (!((ConstraintEvent) event).isInitialized()) {
			engine.decPendingInitConstAwakeEvent();
		}
		//        }
		return event;
	}


	/**
	 * Adds a new var in the queue.
	 *
	 * @return True if the var had to be added.
	 */

	public boolean pushEvent(PropagationEvent event) {
		if (!this.partition.isLeft(event)) {
			this.partition.moveLeft(event);
			return true;
		}
		return false;
	}


	/**
	 * Removes all the events from the queue.
	 */

	public void flushEventQueue() {
		this.partition.moveAllRight();
	}


	/**
	 * Adds a new constraint in the right part of the set (will not be propagated).
	 * It should be done just after creating the constraint.
	 */

	public void add(PropagationEvent event) {
		if (this.partition.isIn(event)) {
			this.partition.moveRight(event);
		} else {
			this.partition.addRight(event);
		}
	}


	/**
	 * Removes the var from the left part.
	 */

	public boolean remove(PropagationEvent event) {
		if (this.partition.isLeft(event)) {
			if (!((ConstraintEvent) event).isInitialized()) {
				engine.decPendingInitConstAwakeEvent();
			}
			this.partition.moveRight(event);
			return true;
		}
		return false;
	}


	/**
	 * Propagates one var in the queue.
	 *
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */

	public void propagateAllEvents() throws ContradictionException {
		while (partition.getNbLeft() != 0) {
			this.popEvent().propagateEvent();
		}
	}

	/**
	 * Propagates one var in the queue.
	 *
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */

	public void propagateOneEvent() throws ContradictionException {
		if (partition.getNbLeft() != 0) {
			this.popEvent().propagateEvent();
		}
	}

	public int size() {
		return partition.getNbLeft();
	}

	public PropagationEvent get(int idx) {
		for (Iterator<PropagationEvent> it = partition.leftIterator(); it.hasNext();) {
			PropagationEvent event = it.next();
			if (idx == 0) {
				return event;
			} else {
				idx--;
			}
		}
		return null;
	}
}
