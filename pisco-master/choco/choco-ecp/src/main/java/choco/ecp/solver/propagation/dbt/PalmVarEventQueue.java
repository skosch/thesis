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

//     ___  ___         PaLM library for Choco system
//    /__ \/ __\        (c) 2001 - 2004 -- Narendra Jussien
//   ____\ |/_____
//  /_____\/_____ \     PalmExplanation based constraint programming tool
// |/   (_)(_)   \|
//       \ /            Version 0.1
//       \ /            January 2004
//       \ /
// ______\_/_______     Contibutors: Fran�ois Laburthe, Hadrien Cambazard, Guillaume Rochart...

package choco.ecp.solver.propagation.dbt;

import choco.cp.solver.propagation.BasicVarEventQueue;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.solver.propagation.PropagationEvent;

import java.util.Iterator;
import java.util.Queue;

public class PalmVarEventQueue extends BasicVarEventQueue {
  /**
   * Reset the last event if a contradiction occured when it was handled.
   */

  public void resetPopping() {
    if ((this.lastPopped != null) && ((PalmVarEvent) this.lastPopped).isPopping()) {
      ((PalmVarEvent) this.lastPopped).setPopping(false);
      ((PalmVarEvent) this.lastPopped).reset();
      if (!this.queue.contains(this.lastPopped)) {
        this.queue.add(this.lastPopped);
      }
    }

  }


  /**
   * Resets all the events of the queue.
   */

  public void reset() {
    this.resetPopping();
    /*for (Iterator iterator = queue.iterator(); iterator.hasNext();) {
      PalmIntVarEvent event = (PalmIntVarEvent) iterator.next();
      //event.reset();
    } */
  }


  /**
   * Updates variable explanations.
   */

  public void restoreVariableExplanations() {
    for (Iterator iterator = queue.iterator(); iterator.hasNext();) {
      PalmVarEvent event = (PalmVarEvent) iterator.next();
      event.restoreVariableExplanation();
    }
  }

  public void assertValidQueue() {
    // TODO : TESTS
    Iterator it = this.getQueue().iterator();
    while (it.hasNext()) {
      PalmIntVarEvent evt = (PalmIntVarEvent) it.next();
      assert(evt.getEventType() != IntVarEvent.EMPTYEVENT);
    }
  }

  // TODO : dans Choco ?
  public boolean contains(Object obj) {
    return this.queue.contains(obj);
  }

  public Queue<PropagationEvent> getQueue() {
    return queue;
  }
}
