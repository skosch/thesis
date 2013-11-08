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

import choco.IObservable;
import choco.IObserver;
import choco.kernel.solver.propagation.event.PropagationEvent;

import java.util.Vector;

public class ObservableVarEventQueue extends VariableEventQueue implements IObservable{

    private Vector obs;

    public ObservableVarEventQueue() {
        this.obs = new Vector();
    }

    /**
     * Clear datastructures for safe reuses
     */
    @Override
    public void clear() {
        super.clear();
        this.obs.clear();
    }

    /**
   * Pops an event to propagate.
   */
  public PropagationEvent popEvent() {
    PropagationEvent event = super.popEvent();
     notifyObservers(event);
      return event;
  }


  /**
   * Adds an event to the queue.
   */

  public boolean pushEvent(PropagationEvent event) {
      notifyObservers(event);
      return super.pushEvent(event);
  }


  /**
   * Removes all the events (including the popping one).
   */

  public void flushEventQueue() {
    super.flushEventQueue();
    notifyObservers(1);
  }

    ////////////////////////////////IObservable implementation////////////////////////////

    /**
     * Adds an observer to the set of observers for this object, provided
     * that it is not the same as some observer already in the set.
     * The order in which notifications will be delivered to multiple
     * observers is not specified. See the class comment.
     *
     * @param o an observer to be added.
     * @throws NullPointerException if the parameter o is null.
     */
    public synchronized void addObserver(IObserver o) {
        if (o == null)
            throw new NullPointerException();
        if (!obs.contains(o)) {
            obs.addElement(o);
        }
    }

    /**
     * If this object has changed, as indicated by the
     * <code>hasChanged</code> method, then notify all of its observers
     * and then call the <code>clearChanged</code> method to indicate
     * that this object has no longer changed.
     * <p/>
     * Each observer has its <code>update</code> method called with two
     * arguments: this observable object and the <code>arg</code> argument.
     *
     * @param arg any object.
     * @see choco.IObserver#update(choco.IObservable , Object)
     */
    public void notifyObservers(Object arg) {
	/*
         * a temporary array buffer, used as a snapshot of the state of
         * current Observers.
         */
        Object[] arrLocal;

	synchronized (this) {
	    /* We don't want the Observer doing callbacks into
	     * arbitrary code while holding its own Monitor.
	     * The code where we extract each Observable from
	     * the Vector and store the state of the Observer
	     * needs synchronization, but notifying observers
	     * does not (should not).  The worst result of any
	     * potential race-condition here is that:
	     * 1) a newly-added Observer will miss a
	     *   notification in progress
	     * 2) a recently unregistered Observer will be
	     *   wrongly notified when it doesn't care
	     */
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((IObserver)arrLocal[i]).update(this, arg);
    }
}