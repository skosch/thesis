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

import choco.kernel.solver.ContradictionException;

/**
 * A clause is a set of litterals used within the watched literals
 * propagation based global constraint for clauses (ClauseStore)
 */
public class WLClause {

    // a table of assignments (i,j) such that V_{i,j} x_i = j doit etre verifie pour la recherche future
    // Each assignment is indexed by an integer managed by the vocabulary
    protected final int[] lits;

    protected final Lits voc;

    protected boolean isreg = false;

    protected boolean nogood = false;

    protected int idx = -1; //index of the clause in the list of the clause store

    protected ClauseStore propagator;

    public WLClause(int[] ps, Lits voc) {
        lits = ps;
        this.voc = voc;
    }

    public void setIdx(int id) {
        this.idx = id;
    }

    public int getIdx() {
        return this.idx;
    }

    public int getLitZero() {
        return lits[0];
    }

    // jl = 0 or 1
    public void findLiteral(int start) {
        for (int i = start; i < lits.length; i++) {
            if (!voc.isFalsified(lits[i])) {
                int tlit = lits[start];
                lits[start] = lits[i];
                lits[i] = tlit;
                break;
            }
        }
    }

    /**
     * register this clause in the watching lists of the propagator.
     * Basically find two literals to watch
     *
     * @param propagator
     * @throws ContradictionException
     */
    public boolean register(ClauseStore propagator) throws ContradictionException {
        assert lits.length > 1;
        this.propagator = propagator;
        if (isreg) return true;
        findLiteral(0);  // find a non falsified literal and exchange it with lits[0]
        if (voc.isFalsified(lits[0])) { // if none, raise a contradiction
            propagator.fail();
        }
        findLiteral(1);  // find a second non falsified literal and exchange it with lits[1]
        if (voc.isFalsified(lits[1])) { // if none, propagate lits[0]
            updateDomain();
        }

        // ajoute la clause a la liste des clauses controles.
        if (nogood || (voc.isFree(lits[0]) && voc.isFree(lits[1]))) {
            isreg = true;
            voc.watch(lits[0], this);
            voc.watch(lits[1], this);
        }
        return isreg;
    }

    public void unregister() {
        if (isreg) {
            voc.unwatch(lits[0], this);
            voc.unwatch(lits[1], this);
        }
    }

    /**
     * propagate the clause because one of the watched literals has changed
     *
     * @param p     the watched literals that has just changed
     * @param idxcl the index of the clause within the propagator
     * @return if the literals being watche have changed
     * @throws ContradictionException
     */
    public boolean propagate(int p, int idxcl) throws ContradictionException {
        // Lits[1] doit contenir le litteral falsifie
        if (lits[0] == p) {
            lits[0] = lits[1];
            lits[1] = p;
        }

        //inutile de mettre a jour lits[1] si lits[0] est satisfait
        if (voc.isSatisfied(lits[0]))
            return false;

        // Recherche un nouveau litteral
        for (int i = 2; i < lits.length; i++) {
            if (!voc.isFalsified(lits[i])) {
                lits[1] = lits[i];
                lits[i] = p;
                voc.unwatch(p, idxcl);
                voc.watch(lits[1], this);
                return true;
            }
        }
        // La clause est unitaire ou nulle, propager lits[0]
        updateDomain();
        return false;
    }

    public void updateDomain() throws ContradictionException {
        if (lits[0] > 0) {
            if (voc.boolvars[lits[0]].isInstantiatedTo(0)) {
                propagator.updateDegree(lits);
            }
            voc.boolvars[lits[0]].instantiate(1, this.propagator, true);//propagator.cIndices[lits[0] - 1]);
        } else {
            if (voc.boolvars[-lits[0]].isInstantiatedTo(1)) {
                propagator.updateDegree(lits);
            }
            voc.boolvars[-lits[0]].instantiate(0, this.propagator, true);//propagator.cIndices[-lits[0] - 1]);
        }
    }


    /**
     * propagate the clause from scratch
     *
     * @param propagator
     * @return
     * @throws ContradictionException
     */
    public void simplePropagation(ClauseStore propagator) throws ContradictionException {
        int ivalid = -1;
        for (int i = 0; i < lits.length; i++) {
            if (!voc.isFalsified(lits[i])) {
                if (ivalid != -1) return;
                ivalid = i;
            }
        }
        if (ivalid == -1) {
            propagator.fail();
        } else {
            int litzero = lits[0];
            lits[0] = lits[ivalid];
            lits[ivalid] = litzero;
            updateDomain();
        }
    }

    /**
     * @return true if the clause is properly watched by two free literals
     * @throws ContradictionException
     */
    public boolean update() throws ContradictionException {
        if (voc.isFalsified(lits[0]) && !voc.isSatisfied(lits[1])) {
            int temp = lits[0];
            lits[0] = lits[1];
            lits[1] = temp;
            updateDomain();
        } else if (voc.isFalsified(lits[1]) && !voc.isSatisfied(lits[0])) {
            updateDomain();
        }
        return voc.isFree(lits[0]) && voc.isFree(lits[1]);
    }


    public boolean learnt() {
        return false;
    }

    public int size() {
        return lits.length;
    }

    public Lits getVocabulary() {
        return voc;
    }

    public int[] getLits() {
        int[] tmp = new int[size()];
        System.arraycopy(lits, 0, tmp, 0, size());
        return tmp;
    }

    public boolean isSatisfied() {
        for (int i = 0; i < lits.length; i++) {
            if (voc.isSatisfied(lits[i])) return true;
        }
        return false;
    }

    public Boolean isEntailed() {
        boolean unknown = false;
        for (int i = 0; i < lits.length; i++) {
            Boolean b = voc.isEntailed(lits[i]);
            if (b == null) unknown = true;
            else if (b) return true;
        }
        if (unknown)
            return null;
        else
            return false;
    }

    public boolean isSatisfied(int[] tuple) {
        for (int i = 0; i < lits.length; i++) {
            if (Lits.isSatisfied(lits[i], tuple[i])) return true;
        }
        return false;
    }

    public boolean isRegistered() {
        return isreg;
    }

    public boolean isNogood() {
        return nogood;
    }

    public String toString() {
        StringBuilder clname = new StringBuilder(32);
        for (int i = 0; i < lits.length; i++) {
            if (lits[i] > 0) {
                clname.append(voc.boolvars[lits[i]]);
            } else clname.append('!').append(voc.boolvars[-lits[i]]);
            if (i < lits.length - 1)
                clname.append(" v ");
        }
        return clname.toString();
    }

}
