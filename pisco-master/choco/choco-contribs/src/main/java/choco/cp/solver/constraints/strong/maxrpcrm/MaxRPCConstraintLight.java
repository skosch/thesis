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

package choco.cp.solver.constraints.strong.maxrpcrm;

import choco.cp.solver.constraints.strong.ISpecializedConstraint;
import choco.cp.solver.constraints.strong.SCVariable;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Arrays;
import java.util.Map;

public class MaxRPCConstraintLight extends AbstractMaxRPCConstraint {
    /**
     * Structure pour les résidus
     */
    private final int[][] last;

    private final int[] offset;

    /**
     * Implémentation de la contrainte utilisant des résidus pour les supports
     * PC construction de la liste des résidus PC à partir d'un tableau
     * prégénéré
     * 
     * @param intSConstraint
     * @param variablesMap
     */
    public MaxRPCConstraintLight(ISpecializedConstraint intSConstraint,
            Map<IntDomainVar, SCVariable> variablesMap) {
        super(intSConstraint, variablesMap);

        last = new int[2][];
        offset = new int[2];

        for (int i = 2; --i >= 0;) {
            offset[i] = scope[i].getOffset();
            last[i] = new int[1 + scope[i].getSVariable().getSup() - offset[i]];
            Arrays.fill(last[i], Integer.MAX_VALUE);
        }
    }

    public void compute3Cliques() {
        compute3Cliques(false);
    }

    private void setLast(int position, int value, int support) {
        last[position][value - offset[position]] = support;
    }

    /**
     * Contrôle la validité d'un résidu
     * 
     * @param position
     * @param value
     * @return
     */
    private boolean checkLast(int position, int value) {
        return scope[1 - position].getSVariable().canBeInstantiatedTo(
                last[position][value - offset[position]]);
    }

    /**
     * Propagation d'un arc (Contrainte, Variable)
     * 
     * @param constraint
     * @param position
     *            la position de la variable dans la contrainte
     * @throws ContradictionException
     */
    @Override
    public boolean revise(int position) throws ContradictionException {
        boolean revised = false;
        final DisposableIntIterator itr = scope[position].getSVariable()
                .getDomain().getIterator();
        try {
            while (itr.hasNext()) {
                final int a = itr.next();
                if (revise(position, a)) {
                    scope[position].removeVal(a);
                    revised = true;
                }
            }
        } finally {
            itr.dispose();
        }
        return revised;
    }

    private boolean revise(int position, int a) {
        if (checkLast(position, a)) {
            return false;
        }

        int b = firstSupport(position, a);

        while (b < Integer.MAX_VALUE) {
            if (pConsistent(position, a, b)) {
                setLast(position, a, b);
                setLast(1 - position, b, a);
                return false;
            }

            b = nextSupport(position, a, b);
        }
        return true;
    }

    protected boolean pConsistent(int position, int a, int b) {
        final Clique[] cliques = this.cliques;
        for (int c = cliques.length; --c >= 0;) {
            if (cliques[c].findPCSupport(position, a, b) >= Integer.MAX_VALUE) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean revisePC(Clique clique, int position) {
        return false;
    }
}
