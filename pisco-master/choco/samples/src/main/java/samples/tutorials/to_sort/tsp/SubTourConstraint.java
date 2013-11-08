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

package samples.tutorials.to_sort.tsp;

import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.CPSolver;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SubTourConstraint extends AbstractLargeIntSConstraint {

    public static class SubTourConstraintManager extends IntConstraintManager {
        public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables, Object parameters, List<String> options) {
            if(solver instanceof CPSolver){
                return new SubTourConstraint(solver.getVar(variables), solver.getEnvironment());
            }

            return null;
        }
    }

    protected boolean debug = false;
    protected boolean filter = false;

    protected int n;

    protected IntDomainVar[] s;
    protected IStateBitSet[] inPath;
    protected IStateInt[] end;

    public SubTourConstraint(IntDomainVar[] s, IEnvironment environment) {
        super(s);
        this.s = s;
        this.n = s.length;
        this.inPath = new IStateBitSet[n];
        this.end = new IStateInt[n];
        for (int i = 0; i < this.n; i++) {
            this.inPath[i] = environment.makeBitSet(n);
            this.end[i] = environment.makeInt(i);
        }
    }

    public void awake() throws ContradictionException {
        // on fait un parcours du graphe depuis le sommet origine d'index 0 et on met a jour inpath
        BitSet currentPath = new BitSet(n);
        BitSet reached = new BitSet(n);
        Queue<Integer> q = new LinkedList<Integer>();
        q.offer(0);
        reached.set(n - 1, true);
        while (!q.isEmpty()) {
            int u = q.poll();
            reached.set(u, true);
            currentPath.set(u, true);
            if (s[u].isInstantiated()) {
                int v = s[u].getVal();
                if (v != n - 1) q.offer(v);
            }
            if (q.isEmpty() && reached.cardinality() < n) {
                // currentPath contient un chemin sur et u est le dernier sommet
                for (int v = currentPath.nextSetBit(0); v >= 0; v = currentPath.nextSetBit(v + 1)) {
                    inPath[u].set(v, true);
                }
                // on commence un nouveau chemin
                currentPath.clear();
                int w = 0;
                do {
                    if (!reached.get(w)) q.offer(w);
                    else w++;
                } while (q.isEmpty());
            }
        }
        if (debug) LOGGER.info(this.showInPath());
        // on fait un parcours du graphe depuis le sommet fin d'index n-1 et on met a jour end
        reached.clear();
        q = new LinkedList<Integer>();
        q.offer(n - 1);
        reached.set(0, true);
        while (!q.isEmpty()) {
            int v = q.poll();
            reached.set(v, true);
            for (int u = 0; u < n; u++) {
                if (s[u].isInstantiatedTo(v)) {
                    end[u].set(end[v].get());
                    if (u != 0) q.offer(u);
                }
            }
            if (q.isEmpty() && reached.cardinality() < n) {
                int w = 0;
                do {
                    if (!reached.get(w)) q.offer(w);
                    else w++;
                } while (q.isEmpty());
            }
        }
        if (debug) LOGGER.info(this.showEnds());
        // on recupere le chemin sur partant de 0
        BitSet mainPath = new BitSet(n);
        q = new LinkedList<Integer>();
        q.offer(0);
        while (!q.isEmpty()) {
            int u = q.poll();
            mainPath.set(u, true);
            if (s[u].isInstantiated()) {
                int v = s[u].getVal();
                if (v != n - 1) q.offer(v);
            }
        }
        // on interdit que le dernier sommet d'un chemin sur puisse atteindre un sommet de ce meme chemin
        for (int u = 0; u < n; u++) {
            for (int v = inPath[u].nextSetBit(0); v >= 0; v = inPath[u].nextSetBit(v + 1)) {
                if (s[u].canBeInstantiatedTo(v)) {
                    if (filter) LOGGER.info("1- rem (" + u + "," + v + ")");
                    s[u].removeVal(v, this, false);
                }
            }
        }
        // on interdit de pouvoir atteindre le chemin sur partant de 0 depuis un quelconque sommet du graphe
        for (int u = 0; u < n; u++) {
            if (!mainPath.get(u)) {
                for (int v = mainPath.nextSetBit(0); v >= 0; v = mainPath.nextSetBit(v + 1)) {
                    if (end[u].get() != n-1) {
                        if (s[end[u].get()].canBeInstantiatedTo(v)) {
                            if (filter) LOGGER.info("2- rem (" + end[u].get() + "," + v + ")");
                            s[end[u].get()].removeVal(v, this, false);
                        }
                    }
                }
            }
        }
    }

    public void propagate() throws ContradictionException {
    }

    public void awakeOnInst(int u) throws ContradictionException {
        if (u != n - 1) {
            int v = s[u].getVal();
            // on met a jour le chemin sur pouvant atteindre v
            for (int w = inPath[u].nextSetBit(0); w >= 0; w = inPath[u].nextSetBit(w + 1)) {
                inPath[v].set(w, true);
            }
            if (debug) LOGGER.info(this.showInPath());
            // on met a jour end[u]
            end[u].set(end[v].get());
            if (debug) LOGGER.info(this.showEnds());
            // filtrage : on interdit tout arc de end[v] vers un sommet w de inPath[u]
            for (int w = inPath[u].nextSetBit(0); w >= 0; w = inPath[u].nextSetBit(w + 1)) {
                if ((end[v].get() != n-1 || w != 0) &&  s[end[v].get()].canBeInstantiatedTo(w)) {
                    if (filter) LOGGER.info("3- rem (" + end[v].get() + "," + w + ")");
                    s[end[v].get()].removeVal(w, this, false);
                }
            }
        }
    }

    public void awakeOnInf(int u) throws ContradictionException {
        this.constAwake(false);
    }

    public void awakeOnSup(int u) throws ContradictionException {
        this.constAwake(false);
    }

    public void awakeOnBounds(int u) throws ContradictionException {
        this.constAwake(false);
    }

    public void awakeOnRem(int u, int v) throws ContradictionException {
        this.constAwake(false);
    }

    public void awakeOnRemovals(int u, DisposableIntIterator deltaDomain) throws ContradictionException {
        this.constAwake(false);
    }

    public boolean isSatisfied() {
        return false;
    }

    private String showInPath() {
        String s = "";
        for (int i = 0; i < n; i++) {
            s += "inPath[" + i + "] = ";
            for (int j = inPath[i].nextSetBit(0); j >= 0; j = inPath[i].nextSetBit(j + 1)) {
                s += j + " ";
            }
            s += "\n";
        }
        return s;
    }

    private String showEnds() {
        String s = "";
        for (int i = 0; i < n; i++) {
            s += "end[" + i + "] = " + end[i].get() + "\n";
        }
        return s;
    }
}
