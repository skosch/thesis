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

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.propagation.event.PropagationEvent;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 3 août 2009
* Since : Choco 2.1.0
* Update : Choco 2.1.0
*
*
* INSTRUCTIONS :
*
* - change in CPSolver:
*   private int eventQueueType = EventQueueFactory.BASIC;
* to
*   private int eventQueueType = EventQueueFactory.BLOCKING;
*
* - Run DisTest class
* 
*/
public class BlockingVarEventQueue extends VariableEventQueue {

    public static boolean _LOG = true;

    /**
     * FIFO queue to deal with variable events
     */
    protected final Queue<PropagationEvent> queue;

    private Thread[] robbers;

    private ContradictionException cex;

    public BlockingVarEventQueue() {
        queue = new ArrayDeque<PropagationEvent>();
    }

    /**
     * Clear datastructures for safe reuses
     */
    public void clear(){
        queue.clear();
        Arrays.fill(robbers, null);
        cex = null;
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
     *
     */
    public void propagateAllEvents() throws ContradictionException {
        try{
            if(_LOG)LOGGER.info("START PROPAGATION");
            // Initialisation de l'exception qui peut être levée
            cex = null;
            // Création des voleurs de job
            robbers = new Thread[2];
            for (int i = 0; i < robbers.length; i++) {
                robbers[i] = new Robber("w" + i);
            }
            //TODO : laisser aux voleurs le temps de voler un job...
            // Destruction des voleurs de job
            for (Thread robber : robbers) {
                if (_LOG) LOGGER.info(robber.getName() + " interrupts");
                robber.interrupt();
            }
            // Lastly we join ourself to each of the Worker threads, so
            // that we only continue once all the worker threads are
            // finished.
            for (Thread robber : robbers) {
                try {
                    if (_LOG) LOGGER.info(robber.getName() + " R.I.P.");    
                    robber.join();
                } catch (InterruptedException ex) {
                }
            }

            // Si une exception a été rencontré, on la lève
            if (cex != null) {
                if(_LOG)LOGGER.info("throw Exception");
                throw cex;
            }
        }finally {
            if(_LOG)LOGGER.info("END PROPAGATION");
        }
    }

    /**
     * Propagates one single event from the queue (usefull for tracing)
     *
     * @throws choco.kernel.solver.ContradictionException
     *
     */
    public void propagateOneEvent() throws ContradictionException {
        throw new Error("not yet implemented");
    }

    /**
     * Pops an event to propagate.
     */
    public PropagationEvent popEvent() {
        throw new Error("not yet implemented");
    }

    /**
     * Adds an event to the queue.
     */
    public boolean pushEvent(PropagationEvent event) {
        pushInQueue(event);
        return true;
    }

    /**
     * Ajout d'un Job dans la queue
     * @param j le job à executer
     */
    private void pushInQueue(PropagationEvent j){
        synchronized (queue){
            if(_LOG)LOGGER.info("add : " + j);
            queue.add(j);
            queue.notifyAll();
        }
    }

    /**
     * Récuperation du premier job ajouté
     * @return le prochain job à executer
     */
    private PropagationEvent pollFromQueue() throws InterruptedException {
        synchronized (queue) {
            if(queue.isEmpty()) {
                if(_LOG)LOGGER.info("Q sleeps");
                ChocoLogging.flushLogs();
                queue.wait();
                if(_LOG)LOGGER.info("Q wakes up");
            }
            return queue.poll();
        }
    }

    /**
     * On nettoie les evenements restant dans la queue
     */
    private void clearQueue(){
        synchronized (queue){
            for(PropagationEvent evt : queue){
                evt.clear();
            }
            queue.clear();
        }
    }

    /**
     * Removes all the events (including the popping one).
     */
    public void flushEventQueue() {
        if(_LOG)LOGGER.info("Q flushes ("+queue.size()+")");
        clearQueue();
    }

    /**
     * Removes an event. This method should not be useful for variable events.
     */
    public boolean remove(PropagationEvent event) {
        throw new Error("not yet implemented");
    }

    public int size() {
        return queue.size();
    }

    public PropagationEvent get(int idx) {
        throw new Error("not yet implemented");
    }

    /******************************************************************************************************************/
    /*                                          WORKER                                                                */
    /******************************************************************************************************************/

    private class Robber extends Thread {
        public Robber(String name) {
            super(name);
            if(_LOG)LOGGER.info(name+" starts");
            start();
        }

        public void run() {
            try {
                // Tant que le voleur est en activité
                while (!isInterrupted()) {
                    // Il vole un job
                    PropagationEvent evt = pollFromQueue();
                    // on execute le sale boulot
                    try {
                        if(_LOG)LOGGER.info(evt +" runs");
                        // Propagation de l'évenement
                        evt.propagateEvent();
                        if(_LOG)LOGGER.info(evt+" ends");
                    } catch (ContradictionException e) {
                        // Si une exception est rencontrée
                        if(_LOG)LOGGER.info(evt + " fails");
                        // On vide la queue de jobs restant à faire
                        clearQueue();
                        // on mémorise la contradiction
                        cex = e;
                        // On tue le voleur executant la tache courante
                        if(_LOG)LOGGER.info(this.getName()+" must die");
                        ChocoLogging.flushLogs();
                        synchronized (this){
                            this.interrupt();
                        }
                    }
                }

            } catch (InterruptedException ex) {
                if(_LOG)LOGGER.info(this.getName()+" dies");
                Thread.currentThread().interrupt();
            }
        }
    }

}
