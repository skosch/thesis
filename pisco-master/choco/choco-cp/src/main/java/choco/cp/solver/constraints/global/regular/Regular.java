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

package choco.cp.solver.constraints.global.regular;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.IntEnumeration;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.structure.IndexedObject;
import choco.kernel.memory.structure.StoredIndexedBipartiteSet;
import choco.kernel.memory.structure.iterators.BipartiteSetIterator;
import choco.kernel.model.constraints.automaton.DFA;
import choco.kernel.model.constraints.automaton.LightLayeredDFA;
import choco.kernel.model.constraints.automaton.LightState;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Enforce the sequence of variable vs to be a word recognized by DFA auto
 */
public final class Regular extends AbstractLargeIntSConstraint {


    public final static boolean INCREMENTAL = true;

    // the list of states acting as support of assignment V_i = j
    protected StoredIndexedBipartiteSet[] Qij;

    // data structure to speedup the acces to index of Q_ij
    protected int[] offset;
    protected int[] start;
    protected int[] sizes;

    protected LightLayeredDFA autom;

    protected int nbNode;
    /**
     * Stored data structured map to the original automaton
     */
    protected PropagationData sdata;

    private int nbVars;

    public Regular(final DFA auto, final IntDomainVar[] vs, final int[] lbs, final int[] dsize, final IEnvironment environment) {
        super(ConstraintEvent.LINEAR, vs);
        init(auto.lightGraph, lbs, dsize, environment);
    }

    /**
     * Enforce the sequence of variable vs to be a word recognized by DFA auto
     *
     * @param auto
     * @param vs
     * @param environment
     */
    public Regular(final DFA auto, final IntDomainVar[] vs, final IEnvironment environment) {
        super(ConstraintEvent.LINEAR, vs);
        final int[] offset = new int[vars.length];
        final int[] sizes = new int[vars.length];
        for (int i = 0; i < vars.length; i++) {
            offset[i] = vars[i].getInf();
            sizes[i] = vars[i].getSup() - vars[i].getInf() + 1;
        }
        init(auto.lightGraph, offset, sizes, environment);
    }

    public void init(final LightLayeredDFA auto, final int[] lbs, final int[] dsize, final IEnvironment environment) {
        autom = auto;
        nbVars = vars.length;
        start = new int[vars.length];
        offset = lbs;
        sizes = dsize;
        // nbNode = autom.getNbStates();
        nbNode = autom.getAutomateSize();
        sdata = new PropagationData(this, environment);
        start[0] = 0;
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) start[i] = start[i - 1] + sizes[i - 1];
        }
        Qij = new StoredIndexedBipartiteSet[start[nbVars - 1] + sizes[nbVars - 1]];
        final ArrayList[] qijvalues = new ArrayList[Qij.length];
        for (int i = 0; i < qijvalues.length; i++) {
            qijvalues[i] = new ArrayList<IndexedObject>();
        }
        initQij(qijvalues);
        for (int i = 0; i < Qij.length; i++) {
            Qij[i] = (StoredIndexedBipartiteSet)environment.makeBipartiteSet(qijvalues[i]);
        }
    }

    //<hca> this is initialized regarding the current domains and thus
    // can not be used as a cut...
    public void initQij(final ArrayList[] qijvalues) {
        mark = new BitSet(nbNode);
        Ni = new ArrayList[nbVars + 1]; // le dernier niveau
        for (int i = 0; i < nbVars + 1; i++) {
            Ni[i] = new ArrayList<LightState>();
        }
        Ni[0].add(autom.getInitState());
        for (int i = 0; i < Ni.length - 1; i++) {
            for (final LightState st : Ni[i]) {
                final DisposableIntIterator domIt = vars[i].getDomain().getIterator();
                for (; domIt.hasNext();) {
                    final int val = domIt.next();
                    if (st.hasDelta(val - autom.getOffset(i))) {
                        qijvalues[start[i] + val - offset[i]].add(st);
                        final LightState nst = st.delta(val - autom.getOffset(i));
                        if (!mark.get(nst.getIdx())) { // st is a candidate for support
                            Ni[i + 1].add(nst);
                            mark.set(nst.getIdx());
                        }
                    }
                }
                domIt.dispose();
            }
        }
    }

    public int getFilteredEventMask(final int idx) {
        return IntVarEvent.REMVAL_MASK;
        // return 0x0B;
    }

    public StoredIndexedBipartiteSet getQij(final int var, final int val) {
        return Qij[start[var] + val - offset[var]];
    }

    /***************************************************/
    /*************** Initial propagation ***************/
    /***************************************************/


    // temporary data structures to intialize the Qij set
    protected ArrayList<LightState>[] Ni; // the list of states of each level (each variable)
    protected BitSet mark;
    protected HashSet<IndexedObject>[] qijvalues;

    public void initData() {
        mark = new BitSet(nbNode);
        qijvalues = new HashSet[Qij.length];
        for (int i = 0; i < Qij.length; i++) {
            qijvalues[i] = new HashSet<IndexedObject>();
        }
        Ni = new ArrayList[nbVars + 1]; // le dernier niveau
        for (int i = 0; i < nbVars + 1; i++) {
            Ni[i] = new ArrayList<LightState>();
        }
    }

    /**
     * marks allow to know whether a state is reachable from q_0 (during
     * the forward phase) or whether a state can not reach q_n (during the backward phase).
     * they are therefore re-initialized between the two phase
     */
    public void initMarck() {
        mark.clear();
        mark.set(0);
        mark.set(nbNode - 1);
    }


    /**
     * Only consider states st that can be reached from q0 (which are on a path (qo ~> st))
     */
    public void forwardUpdate() {
        Ni[0].add(autom.getInitState());
        for (int i = 0; i < Ni.length - 1; i++) {
            forwardOnLevel(i);
        }
    }

    public void forwardOnLevel(final int i) {
        for (final LightState st : Ni[i]) {
            final DisposableIntIterator domIt = vars[i].getDomain().getIterator();
            for (; domIt.hasNext();) {
                final int val = domIt.next();
                if (st.hasDelta(val - autom.getOffset(i))) {
                    qijvalues[start[i] + val - offset[i]].add(st);                    
                    final LightState nst = st.delta(val - autom.getOffset(i));
                    if (!mark.get(nst.getIdx())) { // st is a candidate for support                       
                        Ni[i + 1].add(nst);
                        mark.set(nst.getIdx());
                    }
                }
            }domIt.dispose();
        }
    }


    /**
     * Only consider states st that reached qn (which are on a path (st ~> qn))
     */
    public void backwardUpdate() {
        for (int i = Ni.length - 2; i >= 0; i--) {
            backward2OnLevel(i);
            backwardOnLevel(i);
            for (Iterator it = Ni[i].iterator(); it.hasNext();) {
                final LightState st = (LightState) it.next();
                if (!mark.get(st.getIdx()))
                    it.remove();
            }
        }
    }

    public void backwardOnLevel(final int i) {
        final DisposableIntIterator domIt = vars[i].getDomain().getIterator();
        for (; domIt.hasNext();) {
            final int val = domIt.next();
            final StoredIndexedBipartiteSet qij = getQij(i, val);
            final BipartiteSetIterator it = qij.getObjectIterator();
            while(it.hasNext()) {
                final LightState st = (LightState) it.nextObject();
                final LightState nst = st.delta(val - autom.getOffset(i));
                if (nst != null && mark.get(nst.getIdx())) { //isMark(ctIdx)) {     // st confirmed as a support
                    mark.set(st.getIdx()); //st.mark(ctIdx);
                    sdata.incrementOutdeg(st);
                    sdata.incrementIndeg(nst);
                } else {
                    it.remove();
                }
            }
            it.dispose();
        }
        domIt.dispose();
    }

    public void backward2OnLevel(final int i) {
        final DisposableIntIterator domIt = vars[i].getDomain().getIterator();
        for (; domIt.hasNext();) {
            final int val = domIt.next();
            final StoredIndexedBipartiteSet qij = getQij(i, val);
            final BipartiteSetIterator it = qij.getObjectIterator();
            while(it.hasNext()) {
                final LightState st = (LightState) it.nextObject();
                if (!qijvalues[start[i] + val - offset[i]].contains(st)) { //isMark(ctIdx)) {     // st confirmed as a support
                    it.remove();
                }
            }
            it.dispose();
        }
        domIt.dispose();
    }


    /**
     * removes values that are not supported by any state of the automata
     *
     * @throws choco.kernel.solver.ContradictionException
     *
     */
    public void cleanUp() throws ContradictionException {
        for (int i = 0; i < nbVars; i++) {
            final int fin = i == (nbVars - 1) ? Qij.length : start[i + 1];
            for (int j = start[i]; j < fin; j++) {
                if (Qij[j].isEmpty()) {
                    final int val = j - start[i];
                    if (vars[i].canBeInstantiatedTo(val + offset[i])) {// why Qij is empty ?
                        prune(i, val + offset[i]);
                    }
                }
            }
        }
    }

    public void propagate() throws ContradictionException {
        if (!autom.isEmpty()) {
            sdata.resetPropagationData(nbNode);
            initData();
            initMarck();
            forwardUpdate();
            initMarck();
            backwardUpdate();
            cleanUp();
            mark = null; // free memory
            Ni = null;
        } else this.fail();
    }

    /*******************************************************/
    /*************** Incremental propagation ***************/
    /*******************************************************/

    /**
     *
     * @param i
     * @param val
     * @throws ContradictionException
     */
    public void prune(final int i, final int val) throws ContradictionException {
        vars[i].removeVal(val, this, false);
    }


    public void awake() throws ContradictionException {
        propagate();
    }

    /**
     * Incremental propagation of a value removal
     *
     * @throws ContradictionException
     */
    public void propagateRemoval(final int i, final int j) throws ContradictionException {
        final StoredIndexedBipartiteSet qij = getQij(i, j);
        for (int k = 0; k < qij.size(); k++) {
            final LightState st = (LightState) qij.getObject(k);
            final LightState nst = st.delta(j - autom.getOffset(i));
            decrement_outdeg(st, i);
            decrement_indeg(nst, i + 1);
        }
        qij.clear();
    }

    /**
     * Decrement the out-degree of state st located on the i-th layer
     */
    public void decrement_outdeg(final LightState st, final int i) throws ContradictionException {
        sdata.decrementOutdeg(st);
        if (sdata.getOutdeg(st) == 0) {
            propagateNullOutDeg(st, i);
        }
    }

    public void propagateNullOutDeg(final LightState st, final int i) throws ContradictionException {
        final Enumeration pred = st.getEnumerationPred();
        while (pred.hasMoreElements()) {
            final LightState.Arcs ap = (LightState.Arcs) pred.nextElement();
            final LightState pst = ap.getSt();
            final IntEnumeration valpred = ap.getEnumerationPred();
            if (sdata.isAccurate(pst)) {
                while (valpred.hasMoreElements()) {
                    final int val = valpred.nextElement();
                    final int realval = val + autom.getOffset(i - 1);
                    if (vars[i - 1].canBeInstantiatedTo(realval)) {
                        final StoredIndexedBipartiteSet supports = getQij(i - 1, realval);
                        supports.remove(pst);
                        if (supports.isEmpty()) {
                            prune(i - 1, realval);
                        }
                        decrement_outdeg(pst, i - 1);
                    }
                }
            }
        }
    }

    /**
     * Decrement the in-degree of state st located on the i-th layer
     */
    public void decrement_indeg(final LightState st, final int i) throws ContradictionException {
        sdata.decrementIndeg(st);
        if (sdata.getIndeg(st) == 0) {
            propagateNullInDeg(st, i);
        }
    }

    public void propagateNullInDeg(final LightState st, final int i) throws ContradictionException {
        final Enumeration succ = st.getEnumerationSucc();
        while (succ.hasMoreElements()) {
            final int val = (Integer) succ.nextElement();
            final int realval = val + autom.getOffset(i);
            final LightState nst = st.delta(val);
            if (vars[i].canBeInstantiatedTo(realval)) {
                final StoredIndexedBipartiteSet supports = getQij(i, realval);
                supports.remove(st);
                if (supports.isEmpty()) {
                    prune(i, realval);
                }
                decrement_indeg(nst, i + 1);
            }
        }
    }


    public void awakeOnRem(final int idx, final int x) throws ContradictionException {
        if (INCREMENTAL) {// && domaincopy[idx].get(x - offset[idx])) {

            propagateRemoval(idx, x);
        } else this.constAwake(false);
        if (!vars[idx].hasEnumeratedDomain()){
            StoredIndexedBipartiteSet supports = getQij(idx, vars[idx].getInf());
            if (supports.isEmpty()) {
                vars[idx].removeVal(vars[idx].getInf(), this, true);
            }
            supports = getQij(idx, vars[idx].getSup());
            if (supports.isEmpty()) {
                vars[idx].removeVal(vars[idx].getSup(), this, true);
            }
        }
    }


    public boolean isSatisfied(final int[] tuple) {
        LightState tmp = autom.getInitState();
        for (int i = 0; i < tuple.length; i++) {
            tmp = tmp.delta(tuple[i] - autom.getOffset(i));
            if (tmp == null)
                return false;
        }
        return autom.getLastState() == tmp;
    }

    public String pretty() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Regular({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) sb.append(", ");
            final IntDomainVar var = vars[i];
            sb.append(var.pretty());
        }
        sb.append("})");
        return sb.toString();
    }


    public String toString() {
        final StringBuilder autstring = new StringBuilder("auto : ");
        for (int i = 0; i < vars.length; i++) {
            autstring.append(vars[i]).append(' ');
        }
        return autstring.toString();
    }

    /*******************************************************/
    /*************** Propragation data structure ***********/
    /**
     * ***************************************************
     */

    class PropagationData {

        protected Solver solver;
        /**
         * in degre of the state (for incremental propagation of the automaton)
         */
        protected IStateInt[] indeg;

        /**
         * out degre of the state (for incremental propagation of the automaton)
         */
        protected IStateInt[] outdeg;

        protected int fstate;

        public PropagationData(final AbstractSConstraint ct, final IEnvironment environment) {
            initDegree(autom.getAutomateSize(), ct, environment);
        }

        public void initDegree(final int nbNode, final AbstractSConstraint ct, final IEnvironment environment) {
            indeg = new IStateInt[nbNode];
            outdeg = new IStateInt[nbNode];
            fstate = nbNode - 1;
            for (int node = 0; node < nbNode; node++) {
                indeg[node] = environment.makeInt(0);
                outdeg[node] = environment.makeInt(0);
            }
        }

        public void resetPropagationData(final int nbNode) {
            for (int node = 0; node < nbNode; node++) {
                indeg[node].set(0);
                outdeg[node].set(0);
            }
        }

        public boolean isAccurate(final LightState st) {
            if (st.getIdx() == 0) return outdeg[st.getIdx()].get() > 0;
            if (st.getIdx() == fstate) return indeg[st.getIdx()].get() > 0;
            return (indeg[st.getIdx()].get() > 0) && (outdeg[st.getIdx()].get() > 0);
        }

        public int getIndeg(final LightState st) {
            return indeg[st.getIdx()].get();
        }

        public void setIndeg(final IStateInt indeg, final LightState st) {
            this.indeg[st.getIdx()] = indeg;
        }

        public int getOutdeg(final LightState st) {
            return outdeg[st.getIdx()].get();
        }

        public void setOutdeg(final IStateInt outdeg, final LightState st) {
            this.outdeg[st.getIdx()] = outdeg;
        }

        public void incrementIndeg(final LightState st) {
            indeg[st.getIdx()].add(1);
        }

        public void decrementIndeg(final LightState st) {
            indeg[st.getIdx()].add(-1);
        }

        public void incrementOutdeg(final LightState st) {
            outdeg[st.getIdx()].add(1);
        }

        public void decrementOutdeg(final LightState st) {
            outdeg[st.getIdx()].add(-1);
        }
    }
}
