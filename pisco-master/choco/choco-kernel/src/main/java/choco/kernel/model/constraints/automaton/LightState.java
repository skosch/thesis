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

package choco.kernel.model.constraints.automaton;

import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Logger;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.IntEnumeration;
import choco.kernel.memory.structure.IndexedObject;

/*
 * Created by IntelliJ IDEA.
 * User: Richaud
 * Date: 22 juin 2006
 * Since : Choco 2.0.0
 *
 */

/**
 *   Minimal data structure permitting to store a node and to enumerate his successors and predecessors.
 *   Predecessors are stored in a class Arcs composed of a previous state and the list of values leading
 *   this node.
 */
public class LightState implements IndexedObject {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    // Identifier of the state
    protected int idx;

    //Identifier of the state within its layer
    protected int layerIdx;

    // Successors
    protected Hashtable htransitions;

    // Predecessors
    protected Arcs[] trPred;


    // Set Idx
    public void setIdx(int idx) {
        this.idx = idx;
    }

    // Get Idx
    public int getIdx() {
        return this.idx;
    }

    // Set Layer Idx
    public void setLayerIdx(int idx) {
        this.layerIdx = idx;
    }

    // Get Layer Idx
    public int getLayerIdx() {
        return this.layerIdx;
    }

    //Interface to be used within a StoredBipartiteSet
    public int getObjectIdx() {
        return this.layerIdx;
    }

    public void init(LightState ls) {
        htransitions = ls.htransitions;
        trPred = ls.trPred;
        idx = ls.idx;
        layerIdx = ls.layerIdx;
    }


   // Enumeration on predecessors
    public Enumeration<? extends Arcs> getEnumerationPred() {
        return new Enumerator<Arcs>();
    }

    // Enumeration on successors
    public Enumeration<? extends Integer> getEnumerationSucc() {
        return (this.htransitions.keys());
    }

    // Get delta(value, this)
    public LightState delta(int value) {
        return (LightState)this.htransitions.get(value);
    }

    //  Has a successor
    public boolean hasDelta(int value) {
        return (this.htransitions.get(value) != null);
    }

    private class Enumerator<Arc> implements Enumeration<Arc> {
        int currentIdx = 0;
        int maxSize;

        Enumerator() {
            currentIdx = 0;
            maxSize = trPred.length;
        }

        public boolean hasMoreElements() {
            return (currentIdx < maxSize);
        }

        public Arc nextElement() {
            return (Arc)trPred[currentIdx++];
        }
    }

    // Structure used to store a node and a list of value corresponding
    //  to a predecessor and values leading to this node.
    public class Arcs {
        protected LightState st;
        protected int[] values;

        public Arcs(LightState st, BitSet values) {
            this.st = st;
            this.values = new int[values.cardinality()];
            int cTab = 0;
            for (int i = values.nextSetBit(0); i >= 0; i = values.nextSetBit(i + 1)) {
                this.values[cTab] = i;
                cTab++;
            }
        }

        public LightState getSt() {
            return st;
        }

        public int getValue(int idx) {
            return values[idx];
        }


        public IntEnumeration getEnumerationPred() {
            return new ValEnumerator();
        }

        private class ValEnumerator implements IntEnumeration {
            int currentIdx = 0;
            int maxSize;

            public ValEnumerator() {
                currentIdx = 0;
                maxSize = values.length;
            }

            public boolean hasMoreElements() {
                return (currentIdx < maxSize);
            }

            public int nextElement() {
                return values[currentIdx++];
            }
        }


    }


}
