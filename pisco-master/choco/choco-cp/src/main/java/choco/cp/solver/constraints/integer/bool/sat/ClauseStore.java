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

package choco.cp.solver.constraints.integer.bool.sat;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TLongIntHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;

/**
 * A global constraint to store and propagate all clauses
 */
public final class ClauseStore extends AbstractLargeIntSConstraint {


    public static final boolean nonincprop = false;

    //a flag to allow quick entailment tests
    public boolean efficient_entailment_test = false;

    // a data structure for managing all variable/value pairs
    protected final Lits voc;

    protected final ArrayList<WLClause> listclause;

    protected final LinkedList<WLClause> listToPropagate;

    // if we get clause of size one, we instantiate them directly
    // to the correct value
    protected final LinkedList<IntDomainVar> instToOne;
    protected final LinkedList<IntDomainVar> instToZero;

    private final TLongIntHashMap indexes;

    protected final int[] fineDegree;

    protected int nbNonBinaryClauses;

    //clause_entailed[i][0] : the set of NOT BINARY clauses (as a bitset of indexes)
    //                        entailed by setting x_i to 0
    //clause_entailed[i][1] : the set of NOT BINARY clauses (as a bitset of indexes)
    //                        entailed by setting x_i to 1    
    protected int[][][] clause_entailed;
    //the set of clauses not yet entailed
    protected IStateIntVector clauses_not_entailed;

    private final IEnvironment environment;
    /**
     * @param vars must be a table of BooleanVarImpl
     * @param environment
     */
    public ClauseStore(IntDomainVar[] vars, IEnvironment environment) {
        this(vars, new ArrayList<WLClause>(12), new Lits(), environment);
        voc.init(vars);
    }

    public ClauseStore(IntDomainVar[] vars, ArrayList<WLClause> listclause, Lits voc, IEnvironment environment) {
        super(ConstraintEvent.QUADRATIC, vars);
        this.environment = environment;
        this.voc = voc;
        this.listclause = listclause;
        listToPropagate = new LinkedList<WLClause>();
        instToOne = new LinkedList<IntDomainVar>();
        instToZero = new LinkedList<IntDomainVar>();
        nbNonBinaryClauses = 0;
        fineDegree = new int[vars.length];
        indexes = new TLongIntHashMap(vars.length);
        for (int v = 0; v < vars.length; v++) {
            indexes.put(vars[v].getIndex(), v);
        }
    }

    public ArrayList<WLClause> getClauses() {
        return listclause;
    }

    public void setEfficientEntailmentTest() {
        efficient_entailment_test = true;
    }

    public void clearEfficientEntailmentTest() {
        efficient_entailment_test = false;
    }


    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK;
    }

    public Lits getVoc() {
        return voc;
    }


    public void awakeOnInst(int idx) throws ContradictionException {
        if (nonincprop) {
            constAwake(false);
        } else {
            filterOnInst(idx);
        }
    }

    public void filterOnInst(int idx) throws ContradictionException {
        int val = vars[idx].getVal();
        int sidx = idx + 1;
        if (val == 1) {
            int vocidx = -sidx;
            Vec<WLClause> wlist = voc.watches(vocidx);
            if (wlist != null) {
                for (int i = 0; i < wlist.size(); i++) {
                    WLClause clause = wlist.get(i);
                    if (clause.propagate(vocidx, i)) i--;
                }
            }
        } else {
            Vec<WLClause> wlist = voc.watches(sidx);
            if (wlist != null) {
                for (int i = 0; i < wlist.size(); i++) {
                    WLClause clause = wlist.get(i);
                    if (clause.propagate(sidx, i)) i--;
                }
            }
        }

        //for efficient entailment tests
        if (efficient_entailment_test) {
            maintainEfficientEntailment(idx, val);
        }
    }

    public void maintainEfficientEntailment(int idx, int val) {
        if (val == 1) {
            //tot_clauses_entailed.or(clauses_entailed[idx][1]);
            for (int i = 0; i < clause_entailed[idx][1].length; i++) {
                clauses_not_entailed.remove(clause_entailed[idx][1][i]);
            }
        } else {
            for (int i = 0; i < clause_entailed[idx][0].length; i++) {
                clauses_not_entailed.remove(clause_entailed[idx][0][i]);
            }
        }

    }

    public void addClause(int[] lits) {
        listclause.add(new WLClause(lits, voc));
    }

    public static IntDomainVar[] removeRedundantVars(IntDomainVar[] vs) {
        HashSet<IntDomainVar> filteredVars = new HashSet<IntDomainVar>(12);
        for (int i = 0; i < vs.length; i++) {
            if (!filteredVars.contains(vs[i]))
                filteredVars.add(vs[i]);
        }
        IntDomainVar[] filteredTab = new IntDomainVar[filteredVars.size()];
        filteredVars.toArray(filteredTab);
        return filteredTab;
    }

    public int[] computeLits(IntDomainVar[] plit, IntDomainVar[] nlit) {
        int[] lits = new int[plit.length + nlit.length];
        int cpt = 0;
        for (IntDomainVar aPlit : plit) {
            int lit = findIndex(aPlit);
            lits[cpt] = lit;
            cpt++;
        }
        for (IntDomainVar aNlit : nlit) {
            int lit = findIndex(aNlit);
            lits[cpt] = -lit;
            cpt++;
        }
        return lits;
    }

    public void updateDegree(int[] lit) {
        for (int i = 0; i < lit.length; i++) {
            int l = (lit[i] < 0) ? -lit[i] - 1 : lit[i] - 1;
            fineDegree[l]++;
        }
    }

    /**
     * add a clause in the store
     * WARNING : this method assumes that the variables are
     * in the scope of the ClauseStore
     *
     * @param positivelits
     * @param negativelits
     */
    public void addClause(IntDomainVar[] positivelits, IntDomainVar[] negativelits) {
        IntDomainVar[] plit = removeRedundantVars(positivelits);
        IntDomainVar[] nlit = removeRedundantVars(negativelits);

        int[] lits = computeLits(plit, nlit);
        updateDegree(lits);
        if (lits.length == 1) { //dealing with clauses of size one
            if (plit.length == 1) {
                instToOne.add(vars[lits[0] - 1]);
            } else {
                instToZero.add(vars[-lits[0] - 1]);
            }
        } else {
            WLClause cl;
            if (lits.length == 2)
                cl = new BinaryWLClause(lits, voc);
            else cl = new WLClause(lits, voc);
            cl.setIdx(listclause.size());
            listclause.add(cl);
            if (lits.length > 2) nbNonBinaryClauses++;
        }
    }

    public int findIndex(IntDomainVar v) {
        return indexes.get(v.getIndex()) + 1;
    }

    public void addNoGood(IntDomainVar[] positivelits, IntDomainVar[] negativelits) {
        IntDomainVar[] plit = removeRedundantVars(positivelits);
        IntDomainVar[] nlit = removeRedundantVars(negativelits);

        int[] lits = computeLits(plit, nlit);
        updateDegree(lits);
        if (lits.length == 1) { //dealing with clauses of size one
            if (plit.length == 1) {
                instToOne.add(vars[lits[0] - 1]);
            } else {
                instToZero.add(vars[-lits[0] - 1]);
            }
        } else {
            DynWLClause clause = new DynWLClause(lits, voc);
            clause.setIdx(listclause.size());
            listclause.add(clause);
            listToPropagate.addLast(clause);
            if (lits.length > 2) nbNonBinaryClauses++;
        }
    }

    /**
     * Add a clause given the set of literals
     *
     * @param lits
     * @return
     */
    public DynWLClause fast_addNoGood(int[] lits) {
        updateDegree(lits);
        if (lits.length == 1) { //dealing with clauses of size one
            if (lits[0] > 0) {
                instToOne.add(vars[lits[0] - 1]);
            } else {
                instToZero.add(vars[-lits[0] - 1]);
            }
            return null;
        } else {
            DynWLClause clause = new DynWLClause(lits, voc);
            clause.setIdx(listclause.size());
            listclause.add(clause);
            listToPropagate.addLast(clause);
            if (lits.length > 2) nbNonBinaryClauses++;
            return clause;
        }
    }

    /**
     * Remove a clause from the store
     *
     * @param wlc
     */
    public void delete(WLClause wlc) {
        if (wlc.getIdx() != (listclause.size() - 1)) {
            WLClause lastclause = listclause.remove(listclause.size() - 1);
            listclause.set(wlc.getIdx(), lastclause);
            lastclause.setIdx(wlc.getIdx());
        } else listclause.remove(listclause.size() - 1);
        if (wlc.getLits().length > 2) nbNonBinaryClauses--;
        listToPropagate.remove(wlc);
        wlc.unregister();
    }

    public void createEntailmentStructures() {
        int[] lclauses = new int[listclause.size()];
        for (int i = 0; i < lclauses.length; i++) {
            lclauses[i] = i;
        }
        clauses_not_entailed = environment.makeBipartiteSet(lclauses);
    }

    public void initEntailmentStructures() {
        int[][] count = new int[vars.length][2];
        for (WLClause cl : listclause) {
            int[] lits = cl.lits;
            for (int i = 0; i < lits.length; i++) {
                int lit = lits[i];
                if (lit > 0) {
                    count[lit - 1][1]++;
                } else {
                    count[-lit - 1][0]++;
                }
            }
        }
        int idxcl = 0;
        clause_entailed = new int[vars.length][2][];
        for (int i = 0; i < vars.length; i++) {
            clause_entailed[i][0] = new int[count[i][0]];
            clause_entailed[i][1] = new int[count[i][1]];
        }
        count = new int[vars.length][2];
        for (WLClause cl : listclause) {
            int[] lits = cl.lits;
            for (int i = 0; i < lits.length; i++) {
                int lit = lits[i];
                if (lit > 0) {
                    clause_entailed[lit - 1][1][count[lit - 1][1]] = idxcl;
                    count[lit - 1][1]++;
                } else {
                    clause_entailed[-lit - 1][0][count[-lit - 1][0]] = idxcl;
                    count[-lit - 1][0]++;
                }
            }
            idxcl++;
        }
    }

    public void awake() throws ContradictionException {
        if (efficient_entailment_test) {
            createEntailmentStructures();
            initEntailmentStructures();
        }

        for (WLClause cl : listclause) {
            if (!cl.isRegistered())
                cl.register(this);
        }
        propagate();
    }

    public void propagateUnitClause() throws ContradictionException {
        for (IntDomainVar v : instToOne) {
            v.instantiate(1, null, true);
        }
        for (IntDomainVar v : instToZero) {
            v.instantiate(0, null, true);
        }
    }

    public void propagate() throws ContradictionException {
        if (nonincprop) {
            filterFromScratch();
        } else {
            for (Iterator<WLClause> iterator = listToPropagate.iterator(); iterator.hasNext();) {
                WLClause cl = iterator.next();
                if (cl.register(this)) {
                    iterator.remove();
                }
            }
            if (efficient_entailment_test) {
                for (int i = 0; i < vars.length; i++) {
                    if (vars[i].isInstantiated()) {
                        maintainEfficientEntailment(i,vars[i].getVal());
                    }
                }
            }
            propagateUnitClause();
        }
    }

    public void filterFromScratch() throws ContradictionException {
        for (WLClause cl : listclause) {
            cl.simplePropagation(this);
        }
    }

    public Boolean isEntailed() {
        if (efficient_entailment_test) {
            if (clauses_not_entailed.isEmpty())
                return Boolean.TRUE;
            return null;
        } else {
            boolean unknownflag = false;
            for (WLClause cl : listclause) {
                Boolean b = cl.isEntailed();
                if (b != null) {
                    if (!b) return Boolean.FALSE;
                } else unknownflag = true;
            }
            if (unknownflag) return null;
            else return Boolean.TRUE;
        }
    }

    public boolean isSatisfied() {
        for (WLClause cl : listclause) {
            // only check static clauses,
            // because nogoods can be unsatisfied due to the backtrack
            if (!cl.isNogood()) {
                if (!cl.isSatisfied())
                    return false;
            }
        }
        return true;
    }

    public boolean isSatisfied(int[] tuple) {
        for (WLClause cl : listclause) {
            // only check static clauses,
            // because nogoods can be unsatisfied due to the backtrack
            if (!cl.isNogood()) {
                int[] lit = cl.getLits();
                int[] clt = new int[lit.length];
                for (int i = 0; i < lit.length; i++) {
                    //the literals are offset by one
                    clt[i] = tuple[Math.abs(lit[i]) - 1];
                }
                if (!cl.isSatisfied(clt))
                    return false;
            }
        }
        return true;
    }

    //by default, no information is known
    public int getFineDegree(int idx) {
        return fineDegree[idx];    //To change body of overridden methods use File | Settings | File Templates.
    }

    public int getNbEntailedClauseFrom(int idx, int val) {
        int nbentailedclause = 0;
        for (int i = 0; i < clause_entailed[idx][val].length; i++) {
            if (clauses_not_entailed.contain(clause_entailed[idx][val][i]))
                nbentailedclause++;

        }
        return nbentailedclause;
    }

    public int getNbClause() {
        return listclause.size();
    }

    public final void printClauses() {
        if (LOGGER.isLoggable(Level.INFO)) {
            StringBuilder b = new StringBuilder(32);
            for (WLClause wlClause : listclause) {
                b.append(wlClause);
            }
            LOGGER.info(new String(b));
        }
    }
}

