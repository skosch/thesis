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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;

import choco.kernel.common.util.objects.DoubleLinkedList;


/**
 * State
 *
 * @author Cambazard Hadrien
 * @author Richaud Guillaume
 * @version 0.1  Nov 19, 2005.
 */


public class State extends LightState {
	// Reference to the automaton to which it belongs
	protected LayeredDFA auto;

	// Layer
	protected int level;

	// Index of the state on his layer
	protected int idxLevel;


	// Transitions table
	private State[] delta;

	// Linked list of sucessors
	protected DoubleLinkedList transitions;

	// Hashedtable of predecessors
	protected Hashtable<State, BitSet> hashPred;

	// Constructor
	public State(LayeredDFA a, int level) {
		this.auto = a;
		this.idx = this.auto.getNextIdx();
		this.delta = new State[a.domSizes[level]];
		this.transitions = new DoubleLinkedList(a.domSizes[level]);
		this.hashPred = new Hashtable<State, BitSet>();
		this.level = level;
		this.idxLevel = this.auto.levelStates[level].size();
		this.auto.levelStates[level].add(this);
	}

	// Constructor (copy)
	public State(State origin) {
		this.auto = origin.auto;
		this.level = origin.level;
		this.idx = this.auto.getNextIdx();
		this.delta = new State[origin.auto.domSizes[level]];
		System.arraycopy(origin.delta, 0, this.delta, 0, origin.delta.length);
		this.transitions = new DoubleLinkedList(origin.transitions);
		this.hashPred = new Hashtable<State, BitSet>();
		this.clonePred(origin);
		this.idxLevel = this.auto.levelStates[level].size();
		this.auto.levelStates[level].add(this);
	}


	// Modify incoming edges of the successors  (copy)
	public void clonePred(State origin) {
		for (origin.transitions.restart(); origin.transitions.hasNext();) {
			int i = origin.transitions.next();
			BitSet bsn = ((BitSet) (origin.delta[i].hashPred.get(origin)).clone());
			origin.delta[i].hashPred.put(this, bsn);
		}
	}


	// Set state's level
	public void setLevel(int lvl) {
		this.level = lvl;
	}

	// Get state's level
	public int getLevel() {
		return this.level;
	}

	// Set Idx state (level)
	public void setIdxLevel(int idxLevel) {
		this.idxLevel = idxLevel;
	}

	// Get Idx state (level)
	public int getIdxLevel() {
		return this.idxLevel;
	}

	// Get delta(this,value)
	protected State getNext(int value) {
		return delta[value];
	}

	// delta(noeud, value) != Garbage State ?
	protected boolean hasNext(int value) {
		return (delta[value] != null);
	}

	// Remove the incoming edge: delta(st, value) = this
	protected void retraitTransition(State st, int value) {
		if (this.hashPred.containsKey(st)) {
			BitSet vals = (this.hashPred.get(st));
			vals.clear(value);
			this.hashPred.put(st, vals);

			if (vals.isEmpty()) {
				this.hashPred.remove(st);
			}

			if (this.hashPred.isEmpty()) {
				this.resetState();
				this.auto.removeState(this);
			}
		}
	}

	// Remove unreachable node
	protected void removeIfNoPred() {
		if (this.hashPred.isEmpty()) {
			this.resetState();
			this.auto.removeState(this);
		}
	}

	// Add ingoing edge: delta(st, value) = this
	protected void ajoutInTransition(State st, int value) {
		if (this.hashPred.containsKey(st)) {
			BitSet vals = (this.hashPred.get(st));
			vals.set(value);
			this.hashPred.put(st, vals);
		} else {
			BitSet vals = new BitSet();
			vals.set(value);
			this.hashPred.put(st, vals);
		}
	}

	// Remove all the outgoing transitions
	protected void resetState() {
		this.removeOutTransitions();
	}


	// Replace "delta(node, value) = this" by "delta(node, value) = st1"
	protected void remplaceRef(State st1) {
		for (Enumeration<State> e = this.hashPred.keys(); e.hasMoreElements();) {
			State stPred = e.nextElement();
			// MAJ des transitions chez les pred.
			stPred.remplaceNext(st1, this.hashPred.get(stPred));
			// MAJ des hashTable de this et st1
			if (st1.hashPred.containsKey(stPred)) {
				BitSet bs = st1.hashPred.get(stPred);
				bs.or(this.hashPred.get(stPred));
				st1.hashPred.put(stPred, bs);
			} else {
				BitSet bs = this.hashPred.get(stPred);
				st1.hashPred.put(stPred, bs);
			}
		}
		this.hashPred.clear();
	}

	// Remplace delta(this, *)  == st
	protected void remplaceNext(State st, BitSet bs) {
		for (int value = bs.nextSetBit(0); value >= 0; value = bs.nextSetBit(value + 1)) {
			this.delta[value] = st;
		}
	}

	// Remove ingoings states: this.hashTable et delta des pred.
	protected void removeInTransitions() {
		for (Enumeration<State> e = this.hashPred.keys(); e.hasMoreElements();) {
			State stPred = e.nextElement();
			stPred.deleteNext(this.hashPred.get(stPred));
		}
		this.hashPred.clear();
	}



	//Set delta(this, *)  == Garbage state
	private void deleteNext(BitSet bs) {
		for (int value = bs.nextSetBit(0); value >= 0; value = bs.nextSetBit(value + 1)) {
			this.transitions.removeVal(value);
			this.delta[value] = null;
		}
	}

	// Remove ingoings nodes from successors
	protected void removeOutTransitions() {
		for (this.transitions.restart(); this.transitions.hasNext();) {
			int t = this.transitions.next();
			this.delta[t].hashPred.remove(this);
		}
	}

	// Set:  delta(noeud, value) = st
	protected boolean addNext(State st, int value) {
		if (this.delta[value] == null) {
			this.transitions.addVal(value);
		} else {
			if (this.delta[value] == st) return false;
			this.delta[value].retraitTransition(this, value);
		}
		st.ajoutInTransition(this, value);
		this.delta[value] = st;
		return true;
	}

	// Set delta(noeud, value) = Garbage state
	protected void removeNext(int value) {
		if (this.delta[value] != null) {
			this.transitions.removeVal(value);
			this.delta[value].retraitTransition(this, value);
			this.delta[value] = null;
		}
	}


	// Check if two states have the same outgoing transition
	protected boolean equalState(State st) {
		if (this.transitions.getSize() != st.transitions.getSize()) return false;
		for (this.transitions.restart(); this.transitions.hasNext();) {
			int t = this.transitions.next();
			if (this.delta[t] != st.delta[t]) return false;
		}
		return true;
	}

	// Check if it's the same state
	public boolean equals(State st) {
		return idx == st.idx;
	}

	// Has successor ?
	protected boolean hasSuccessor() {
		return (transitions.getSize() != 0);
	}

	// State to lightState
	protected LightState convertState(Hashtable ht) {
		//int tot = getNbInGoing();
		int cTab = 0;
		trPred = new Arcs[this.hashPred.size()];
		for (Enumeration<State> e = this.hashPred.keys(); e.hasMoreElements();) {
			State cNode = e.nextElement();
			BitSet predBs = this.hashPred.get(cNode);
			trPred[cTab] = new Arcs((LightState)ht.get(cNode), predBs);
			cTab++;
		}


		htransitions = new Hashtable();
		for (this.transitions.restart(); this.transitions.hasNext();) {
			int t = this.transitions.next();
			this.htransitions.put(t, ht.get(this.delta[t]));
		}

		return this;
	}


	// Print
	public void pretty() {
		if(LOGGER.isLoggable(Level.INFO)) {
			StringBuilder b = new StringBuilder();
			for (this.transitions.restart(); this.transitions.hasNext();) {
				int t = this.transitions.next();
				b.append(" val(").append(t).append(") -> Dest(");
				b.append(delta[t].idx).append(")\n");
			}
			LOGGER.log(Level.INFO,"Noeud({0}):\n{1}", new Object[]{idx , b});
		}
	}

	// File
	public void toDotty(BufferedWriter bw) throws IOException {
		State[] delta2 = new State[this.delta.length];
		System.arraycopy(this.delta, 0, delta2, 0, this.delta.length);

		for (int i = 0; i < delta2.length; i++) {
			if (delta2[i] != null) {
				StringBuilder label = new StringBuilder();
                label.append("   ").append(idx).append(" -> ")
                        .append(delta2[i].idx).append("  [ label = \"{").append(i);

				for (int j = i + 1; j < delta2.length; j++) {
					if ((delta2[j] != null) && delta2[i].equals(delta2[j])) {
						label.append("," + j);
						delta2[j] = null;
					}
				}
				label.append("}\" ];");
				bw.write(label.toString());
				bw.newLine();
			}
		}
	}

}

