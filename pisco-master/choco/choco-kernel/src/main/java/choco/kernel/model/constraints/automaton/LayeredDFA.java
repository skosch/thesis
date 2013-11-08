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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Pattern;


/**
 * A layered automaton that offers the possibility to dynamically add or remove tuples incrementally.
 * This structure is used for constructing the automaton corresponding to a n-ary constraint defined by
 * its allowed or forbidden tuples. Once the automaton is build, it is replaced by a LightLayeredDFA which uses less
 * memory but can not be dynamically modified anymore.
 * @author Cambazard Hadrien
 * @author Richaud Guillaume
 * @version 0.1  Nov 19, 2005.
 */


public class LayeredDFA {

    // Size of the domain [0.. domSize -1]
    protected int[] domSizes;

    // Size of the domain [0.. domSize -1]
    protected int[] offsets;


    // Starting state
    protected State initState;

    // Last state
    protected State lastState;

    // Current number of state built (with deleted one)
    protected int nbState = 0;

    // Store nodes of each layer
    protected ArrayList<State>[] levelStates;

    // Number of layer
    protected int nbLevel;


    /**
     * construct an initial automaton.
     * The automaton initially accepts all words
     *
     * @param domSize : same size for all domains
     * @param nblevel : number of layers
     */
    @SuppressWarnings({"unchecked"})
    public LayeredDFA(int domSize, int nblevel) {
        this.domSizes = new int[nblevel];
        for (int i = 0; i < nblevel - 1; i++)
            this.domSizes[i] = domSize;
        this.domSizes[nblevel - 1] = 0;
        this.offsets = new int[nblevel];
        this.nbLevel = nblevel;
        this.levelStates = new ArrayList[nblevel];

        for (int i = 0; i < levelStates.length; i++) {
            levelStates[i] = new ArrayList<State>();
        }
        automatAll();
    }

    /**
     * construct an initial automaton with different domain sizes per layer
     * The automaton initially accepts all words
     *
     * @param domSizes : size of each layers
     * @param nblevel  : number of layers
     */
    @SuppressWarnings({"unchecked"})
    public LayeredDFA(int[] domSizes, int nblevel) {
        this.domSizes = new int[nblevel];
        System.arraycopy(domSizes, 0, this.domSizes, 0, domSizes.length);
        this.domSizes[nblevel - 1] = 0;
        this.offsets = new int[nblevel];
        this.nbLevel = nblevel;
        this.levelStates = new ArrayList[nblevel];

        for (int i = 0; i < levelStates.length; i++) {
            levelStates[i] = new ArrayList<State>();
        }
        automatAll();
    }

    // Build automaton which accept all the words
    public void automatAll() {
        nbState = 0;
        State currentState = makeState(this, 0);
        setInitState(currentState);
        for (int i = 1; i < nbLevel; i++) {
            State nextState = makeState(this, i);
            for (int j = 0; j < domSizes[i - 1]; j++) {
                currentState.addNext(nextState, j);
            }
            currentState = nextState;
        }
        setLastState(currentState);
    }

    // Build an empty automaton
    public void clearAutomate() {
        for (int i = 0; i < levelStates.length; i++) {
            levelStates[i] = new ArrayList<State>();
        }
        nbState = 0;
        State currentState = makeState(this, 0);
        setInitState(currentState);
        currentState = makeState(this, levelStates.length - 1);
        setLastState(currentState);
    }

    public void buildAnEmptyAutomaton() {
        for (int i = 0; i < levelStates.length; i++) {
            levelStates[i] = new ArrayList<State>();
        }
        nbState = 0;
        setInitState(null);
        setLastState(null);
    }

    // Convert State to lightState
 /*   public void convertAutomate() {
        nbState = 0;
        for (int i = 0; i < nbLevel; i++) {
            for (int j = 0; j < this.levelStates[i].size(); j++) {
                ((State) this.levelStates[i].get(j)).setIdx(nbState++);
            }
        }
        for (int i = 0; i < nbLevel; i++) {
            for (int j = 0; j < this.levelStates[i].size(); j++) {
                ((State) this.levelStates[i].get(j)).convertState();
            }
        }
    } */

    // Get current number of states
    public int getAutomateSize() {
        int tot = 0;
        for (int i = 0; i < this.levelStates.length; i++)
            tot += this.levelStates[i].size();
        return tot;
    }

    // Get current number of states
    public int nbTransitions() {
        int tot = 0;
        for (int i = 0; i < nbLevel; i++) {
            for (int j = 0; j < this.levelStates[i].size(); j++) {
                tot += this.levelStates[i].get(j).transitions.getSize();
            }
        }

        return tot;
    }

    //  Create a new node on layer "level"
    public State makeState(LayeredDFA auto, int level) {
        return new State(auto, level);
    }

    //  Copy the node origin
    public State makeState(State origin) {
        return new State(origin);
    }


    //Get number of layer
    public int getNbLevel() {
        return levelStates.length;
    }

    // Size of the domain at layer i
    public int getDomSize(int i) {
        return domSizes[i];
    }

    public void setDomSize(int layer, int domSize) {
        this.domSizes[layer] = domSize;
    }

    // Size of the domain at layer i
    public int getOffset(int i) {
        return offsets[i];
    }

    public void setOffset(int layer, int domSize) {
        this.offsets[layer] = domSize;
    }

    // Get next unused idx
    public int getNextIdx() {
        return nbState++;
    }

    // Get iterator on state of the layer level
    public Iterator getLevelIterator(int level) {
        return this.levelStates[level].iterator();
    }


    // Set Starting state
    public void setInitState(State init) {
        initState = init;
    }

    // Set Last state
    public void setLastState(State last) {
        lastState = last;
    }

    // Get Starting state
    public State getInitState() {
        return initState;
    }


    // Get Last state
    public State getLastState() {
        return lastState;
    }

    // Is i-th layer empty ?
    public boolean isEmpty() {
        return this.levelStates[1].isEmpty();
    }

    // Add a state on layer level
    public State addState(int level) {
        return makeState(this, level);
    }


    // Remove State st
    public void removeState(State st) {
        ArrayList<State> currentAList = levelStates[st.getLevel()];
        int idxLevel = st.getIdxLevel();
        st.setIdxLevel(-1);
        if (idxLevel == (currentAList.size() - 1)) {
            currentAList.remove(idxLevel);
        } else {
            State lastL = currentAList.get(currentAList.size() - 1);
            lastL.setIdxLevel(idxLevel);
            currentAList.set(idxLevel, lastL);
            currentAList.remove(currentAList.size() - 1);
        }
    }


    // Add an edge (with value transition) from st1 to st2
    public void addTransition(State st1, State st2, int transition) {
        st1.addNext(st2, transition);
    }

    // Load automaton from fileName
    @SuppressWarnings({"unchecked"})
    public void loadDotty(String fileName) {
        try {
            Hashtable<Integer,State> builtStates = new Hashtable<Integer, State>();
            BufferedReader inst = new BufferedReader(new FileReader(new File(fileName)));
            Pattern p = Pattern.compile("\\D+");
            String[] nodeInfo;

            inst.readLine();
            inst.readLine();
            inst.readLine();
            this.nbLevel = Integer.parseInt(p.split(inst.readLine())[1]);
            this.domSizes = new int[nbLevel];
            for (int i = 0; i < nbLevel; i++)
                this.domSizes[i] = Integer.parseInt(p.split(inst.readLine())[1]);
            this.levelStates = new ArrayList[nbLevel];
            for (int i = 0; i < levelStates.length; i++) {
                levelStates[i] = new ArrayList<State>();
            }

            nodeInfo = p.split(inst.readLine());
            nbState = 0;
            State firstState = makeState(this, 0);
            builtStates.put(0, firstState);
            setInitState(firstState);

            while (nodeInfo.length != 0) {
                State currentState = builtStates.get(Integer.parseInt(nodeInfo[1]));
                State newState = builtStates.get(Integer.parseInt(nodeInfo[2]));
                if (newState == null) {
                    newState = makeState(this, currentState.getLevel() + 1);
                    builtStates.put(Integer.parseInt(nodeInfo[2]), newState);
                    setLastState(newState);
                }
                for (int i = 3; i < nodeInfo.length; i ++) {

                    currentState.addNext(newState, Integer.parseInt(nodeInfo[i]));
                }
                nodeInfo = p.split(inst.readLine());
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    // Store automaton to file f
    public void toDotty(String f) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(f)));
            bw.write("digraph finite_state_machine {");
            bw.newLine();
            bw.write("rankdir=LR;");
            bw.newLine();
            bw.write("node [shape = circle];");
            bw.newLine();
            bw.write("// nblevel " + this.nbLevel);
            bw.newLine();
            for (int i = 0; i < this.domSizes.length; i++) {
                bw.write("// domSize " + this.domSizes[i]);
                bw.newLine();
            }
            for (int i = 0; i < nbLevel; i++) {
                for (int j = 0; j < this.levelStates[i].size(); j++) {
                    this.levelStates[i].get(j).toDotty(bw);
                }
            }

            bw.write("}");
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Copy a state
    public State cloneState(State origin) {
        return makeState(origin);
    }

    // Clone state: build a node with the same outgoing transitions
    public State cloneState(State origin, Hashtable<State, State> clonedStates, ArrayList<State> newStates) {
        //State st = makeState(origin);
        if (origin.getLevel() == 0) return this.getInitState();
        if (origin.getLevel() == (nbLevel - 1)) return this.getLastState();
        //if ((origin.hashPred.size() == 1) && (((BitSet) origin.hashPred.get(prev)).cardinality()) == 1) return origin;
        State st = clonedStates.get(origin);
        if (st != null) return st;
        st = makeState(origin);
        newStates.add(st);
        clonedStates.put(origin, st);
        return st;
    }

/****************************************************************************/
/*********************** adding or removing words ***************************/
/****************************************************************************/

    public void removeOffset(int[] tuple) {
        for (int i = 0; i < tuple.length; i++) {
            if (tuple[i] < Integer.MAX_VALUE)
                tuple[i] -= offsets[i];
        }
    }
    /**
     * Add a word to the automaton.
     * A word is a table int[] of int among [0 .. domsize[i]] (denoting the letters) for
     * each layer i except that Integer.MAX_VALUE stands for *
     */
    public void union(int[] tuple) {
        assert(tuple.length == (nbLevel - 1));
        int[] ctuple = new int[tuple.length];
	    System.arraycopy(tuple,0,ctuple,0,tuple.length);
	    removeOffset(ctuple);
        State newState;
        State currentState = initState;
        Hashtable<State, State> clonedStates = new Hashtable<State, State>();

        ArrayList<State> newStates = new ArrayList<State>();
        newStates.add(initState);
        int size = ctuple.length;

        for (int i = 0; i < size; i++) {
            int currentAlpha = ctuple[i];
            if (currentState.hasNext(currentAlpha)) {
                newState = cloneState(currentState.getNext(currentAlpha), clonedStates, newStates);
            } else {
                if (i == size - 1) {
                    newState = getLastState();
                } else {
                    newState = makeState(this, i + 1);
                }
            }
            currentState.addNext(newState, currentAlpha);
            newStates.add(newState);
            currentState = newState;
        }
        removeUnreachablesNodes();
        minimize(newStates);
    }

    /**
     * Remove a word from the automaton.
     * A word is a table int[] of int among [0 .. domsize[i]] (denoting the letters) for
     * each layer i except that Integer.MAX_VALUE stands for *
     */
    public void substract(int[] tuple) {
	    int[] ctuple = new int[tuple.length];
		System.arraycopy(tuple,0,ctuple,0,tuple.length);
        removeOffset(ctuple);
        BitSet[] nogoodBS = new BitSet[ctuple.length];
        for (int i = 0; i < tuple.length; i++) {
            nogoodBS[i] = new BitSet();
            if (ctuple[i] == Integer.MAX_VALUE)
                nogoodBS[i].set(0, this.domSizes[i]);
            else
                nogoodBS[i].set(ctuple[i]);
        }
        substract(nogoodBS);
    }


    /**
     * Remove a SET of words from the automaton.
     * A BitSet per layer gives the letters considered per layer and
     * the words are given as the cartesian product of the set bits of each BitSet.
     * Each BitSet, gnogood[i] is therefore assumed to be of size domSizes[i]
     */
    public void substract(BitSet[] gnogood) {
        State nextState;
        State currentState;
        Hashtable<State, State> clonedStates = new Hashtable<State,State>();

        ArrayList<State> newStates = new ArrayList<State>();
        newStates.add(initState);

        for (int j = 0; j < newStates.size(); j++) {
            currentState = newStates.get(j);
            int i = currentState.getLevel();
            if (i >= gnogood.length) break;
            BitSet currentAlpha = gnogood[i];

            for (int alpha = currentAlpha.nextSetBit(0); alpha >= 0; alpha = currentAlpha.nextSetBit(alpha + 1)) {
                if (currentState.hasNext(alpha)) {
                    nextState = cloneState(currentState.getNext(alpha), clonedStates, newStates);

                    if (i == gnogood.length - 1) {
                        currentState.removeNext(alpha);
                    } else {
                        currentState.addNext(nextState, alpha);
                    }
                }
            }

        }
        //     removeUnreachablesNodes();
        removeNonTerminalsNodes();
        //   this.toDotty();
        minimize(newStates);

    }

/****************************************************************************/
    /**
     * ************************************************************************
     */

    // Remove unreachable nodes
    public void removeUnreachablesNodes() {
        boolean removeU = true;
        for (int i = 1; i < nbLevel; i++) {
            if (!removeU) break;
            removeU = false;
            ArrayList currentAL = this.levelStates[i];
            for (int j = (currentAL.size() - 1); j > -1; j--) {
                ((State) currentAL.get(j)).removeIfNoPred();
                removeU = true;
            }
        }

    }

    public void removeGarbageNodes() {
        for (int i = (nbLevel - 2); i > -1; i--) {
            ArrayList currentAL = this.levelStates[i];
            for (int j = (currentAL.size() - 1); j > -1; j--) {
                State st = (State) currentAL.get(j);
                if (!st.hasSuccessor()) {
                    st.removeInTransitions();
                    removeState(st);
                }
            }
        }
    }


    // Remove nodes which don't permit to reach the final node
    public void removeNonTerminalsNodes() {
        for (int i = (nbLevel - 2); i > -1; i--) {
            ArrayList currentAL = this.levelStates[i];
            for (int j = (currentAL.size() - 1); j > -1; j--) {
                State st = (State) currentAL.get(j);
                if (!st.hasSuccessor()) {
                    st.removeInTransitions();
                    removeState(st);
                }
            }
        }
    }

    // Minimize equivalents states
    public void minimize(ArrayList<State> newStates) {
        State st = newStates.get(0);

        for (int i = (newStates.size() - 1); i > -1; i--) {
            st = newStates.get(i);
            if (st.getIdxLevel() != -1) {
                int currentLvl = st.getLevel();

                for (int j = (this.levelStates[currentLvl].size() - 1); j > -1; j--) {
                    State st2 = this.levelStates[currentLvl].get(j);
                    if ((!st.equals(st2)) && (st.equalState(st2))) {
                        mergeStates(st2, st);
                        break;
                    }
                }
            }
        }
    }


    // Merge two states
    public void mergeStates(State st1, State st_supp) {
        st_supp.remplaceRef(st1);
        st_supp.resetState();
        this.removeState(st_supp);
    }


}
