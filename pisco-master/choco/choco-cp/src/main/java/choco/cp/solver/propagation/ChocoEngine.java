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

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.cp.solver.variables.set.SetVarEvent;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.Propagator;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.propagation.event.PropagationEvent;
import choco.kernel.solver.propagation.event.VarEvent;
import choco.kernel.solver.propagation.queue.EventQueue;
import choco.kernel.solver.variables.Var;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Implementation of an {@link AbstractPropagationEngine} for Choco.
 */
public class ChocoEngine extends AbstractPropagationEngine {
    private static int[] indice;
    static {
        indice = new int[1 << ConstraintEvent.NB_PRIORITY];
        indice[0] = -1;
        for (int i = 0; i < ConstraintEvent.NB_PRIORITY; i++) {
            for (int j = (1 << i); j < (1 << i + 1); j++) {
                indice[j] = i;
            }
        }
    }

    private final static char CHAR_NB_P = Character.forDigit(ConstraintEvent.NB_PRIORITY, 10);

    /**
     * The different queues for the constraint awake events.
     */

    protected ConstraintEventQueue[] constEventQueues;
    private int c_active;

    /**
     * Number of pending init constraint awake events.
     */
    protected int nbPendingInitConstAwakeEvent;

    /**
     * The queue with all the variable events.
     */

    protected VariableEventQueue[] varEventQueue;
    private int v_active;

    private ArrayList<PropagationEvent> freeze;
    private int nbFrozenVE;

    private int[] v_order;
    private int[] c_order;

    /**
     * Constructs a new engine by initializing the var queues.
     *
     * @param solver Solver master
     */
    public ChocoEngine(Solver solver) {
        super(solver);
        loadSettings(solver.getConfiguration());
        constEventQueues = new ConstraintEventQueue[ConstraintEvent.NB_PRIORITY];
        for (int i = 1; i < ConstraintEvent.NB_PRIORITY; i++) {
            constEventQueues[i] = new ConstraintEventQueue(this);
        }
        varEventQueue = new VariableEventQueue[ConstraintEvent.NB_PRIORITY];
        for (int i = 1; i < ConstraintEvent.NB_PRIORITY; i++) {
            varEventQueue[i] = new VariableEventQueue();
        }
        nbPendingInitConstAwakeEvent = 0;
    }

    private static int[] toInt(String value) {
        int[] values = new int[value.length() + 1];
        char[] value_c = value.toCharArray();
        for (int i = 0; i < value.length(); i++) {
            values[i + 1] = CHAR_NB_P - value_c[i];
        }
        return values;
    }

    public void loadSettings(Configuration configuration){
        v_order = toInt(configuration.readString(Configuration.VEQ_ORDER));
        c_order = toInt(configuration.readString(Configuration.CEQ_ORDER));
    }


    //****************************************************************************************************************//
    //****************************************************************************************************************//
    //****************************************************************************************************************//

    /**
     * Clear datastructures for safe reuses
     */
    public void clear() {
        super.clear();
        for (int i = 1; i < varEventQueue.length; i++) {
            this.varEventQueue[i].clear();
        }
        v_active = 0;

        for (int i = 1; i < constEventQueues.length; i++) {
            this.constEventQueues[i].clear();
        }
        c_active = 0;
        nbPendingInitConstAwakeEvent = 0;
        if (freeze != null) {
            freeze.clear();
        }

        nbFrozenVE = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // idee: - si on est "frozen", devenir en plus "redondant" (ie: double).
    //       - par ailleurs, noter le changement (garder la vieille valeur de la borne ou
    //       - devenir enqueued
    public void postEvent(final Var v, final int basicEvt, final SConstraint constraint, final boolean forceAwake) {
        VarEvent<? extends Var> event = v.getEvent();
        boolean alreadyEnqueued = event.isEnqueued();
        event.recordEventTypeAndCause(basicEvt, constraint, forceAwake);
        if (!alreadyEnqueued) {
            int p = v_order[event.getPriority()];
            varEventQueue[p].pushEvent(event);
            v_active |= (1 << p);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean postConstAwake(final Propagator constraint, final boolean init) {
        final ConstraintEvent event = (ConstraintEvent) constraint.getEvent();
        if (constEventQueues[c_order[event.getPriority()]].pushEvent(event)) {
            int p = c_order[event.getPriority()];
            c_active |= (1 << p);
            event.setInitialized(!init);
            if (init) {
                this.incPendingInitConstAwakeEvent();
            }
            return true;
        } else
            return false;
    }


    /**
     * {@inheritDoc}
     * It should be called before call to {@link choco.kernel.solver.propagation.Propagator#propagate()}.
     */
    @Override
    public void registerPropagator(final Propagator c) {
        PropagationEvent event = c.getEvent();
        constEventQueues[c_order[event.getPriority()]].add(event);
    }


    /**
     * {@inheritDoc}.
     * First, it propagates every variable events (respecting priority hierarchy).
     * Then, it propagates one constraint event and checks for variable events to propagate.
     * Loops until no event found.
     */
    @Override
    public void propagateEvents() throws ContradictionException {
        do {
            // first empty variable events
            int idx;
            while (v_active > 0) {
                idx = indice[v_active];
                this.varEventQueue[idx].propagateAllEvents();
                if (this.varEventQueue[idx].isEmpty()) {
                    v_active -= 1 << idx;
                }
            }

            // then propagate one constraint event
            if (c_active > 0) {
                idx = indice[c_active];
                if (this.constEventQueues[idx].size() == 1) {
                    c_active -= 1 << idx;
                }
                this.constEventQueues[idx].propagateOneEvent();
            }
        } while (v_active > 0 || c_active > 0);
        assert checkCleanState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void desactivatePropagator(Propagator propagator) {
        PropagationEvent event = propagator.getEvent();
        int idx = c_order[propagator.getPriority()];
        if (constEventQueues[idx].remove(event)) {
            if (this.constEventQueues[idx].isEmpty()) {
                c_active -= 1 << idx;
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decPendingInitConstAwakeEvent() {
        this.nbPendingInitConstAwakeEvent--;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void incPendingInitConstAwakeEvent() {
        this.nbPendingInitConstAwakeEvent++;
    }

    /**
     * Return the number of pending events.
     *
     * @return number of pending events.
     */
    public int getNbPendingEvents() {
        int nbEvts = 0;
        for (int i = 1; i < ConstraintEvent.NB_PRIORITY; i++) {
            nbEvts += varEventQueue[i].size();
        }
        for (int i = 1; i < ConstraintEvent.NB_PRIORITY; i++) {
            nbEvts += constEventQueues[i].size();
        }
        return nbEvts;
    }

    /**
     * getter without side effect:
     * returns the i-ht pending event (without popping any event from the queues)
     *
     * @param idx indice of the event
     * @return a propagation event
     */
    public PropagationEvent getPendingEvent(int idx) {
        int varsSize = 0;
        for (int i = 1; i < varEventQueue.length; i++) {
            if (nbPendingInitConstAwakeEvent > 0) {
                idx += varEventQueue[i].size();
            }
            varsSize += varEventQueue[i].size();
            if (idx < varsSize) {
                return varEventQueue[i].get(idx);
            }
        }
        EventQueue q;
        int size = varsSize;
        int qidx = 1;
        do {
            idx -= size;
            q = constEventQueues[qidx++];
            size = q.size();
        } while (idx >= size && qidx < constEventQueues.length);
        if (idx <= size) {
            return q.get(idx);               // return an event from one of the constraint event queues
        } else if (nbPendingInitConstAwakeEvent > 0) {
            // return an event from the variable event queues
            for (int i = 1; i < varEventQueue.length; i++) {
                varsSize += varEventQueue[i].size();
                if (idx < varsSize) {
                    return varEventQueue[i].get(idx);
                }
            }
        }
        return null;              // return no event, as the index is greater than the total number of pending events
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flushEvents() {
        for (int i = 1; i < varEventQueue.length; i++) {
            this.varEventQueue[i].flushEventQueue();
        }
        v_active = 0;

        for (int i = 1; i < constEventQueues.length; i++) {
            this.constEventQueues[i].flushEventQueue();
        }
        c_active = 0;

        this.nbPendingInitConstAwakeEvent = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkCleanState() {
        boolean ok = true;
        final int nbiv = solver.getNbIntVars();
        for (int i = 0; i < nbiv; i++) {
            final IntVarEvent evt = (IntVarEvent) solver.getIntVar(i).getEvent();
            if (!(evt.getReleased())) {
                LOGGER.log(Level.SEVERE, "var event non released {0}", evt);
                //        new Exception().printStackTrace();
                ok = false;
            }
        }
        final int nbsv = solver.getNbSetVars();
        for (int i = 0; i < nbsv; i++) {
            final SetVarEvent evt = (SetVarEvent) solver.getSetVar(i).getEvent();
            if (!(evt.getReleased())) {
                LOGGER.log(Level.SEVERE, "var event non released {0}", evt);
                //        new Exception().printStackTrace();
                ok = false;
            }
        }
        return ok;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freeze() {
        if (freeze == null) {
            freeze = new ArrayList<PropagationEvent>();
        }
        int idx;
        while (v_active > 0) {
            idx = indice[v_active];
            while (!this.varEventQueue[idx].isEmpty()) {
                freeze.add(this.varEventQueue[idx].popEvent());
            }
            this.varEventQueue[idx].clear();
            v_active -= 1 << idx;
        }
        nbFrozenVE = freeze.size();
        while (c_active > 0) {
            idx = indice[c_active];
            while (!this.constEventQueues[idx].isEmpty()) {
                freeze.add(this.constEventQueues[idx].popEvent());
            }
            c_active -= 1 << idx;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unfreeze() {
        for (int i = freeze.size() - 1; i >= nbFrozenVE; i--) {
            PropagationEvent event = this.freeze.remove(i);
            int p = c_order[event.getPriority()];
            constEventQueues[p].pushEvent(event);
            c_active |= (1 << p);
        }
        for (int i = nbFrozenVE - 1; i >= 0; i--) {
            PropagationEvent event = this.freeze.remove(i);
            int p = v_order[event.getPriority()];
            varEventQueue[p].pushEvent(event);
            v_active |= (1 << p);
        }
        nbFrozenVE = 0;
    }
}
