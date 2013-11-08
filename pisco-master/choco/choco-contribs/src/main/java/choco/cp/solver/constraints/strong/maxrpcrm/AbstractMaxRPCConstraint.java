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
import choco.cp.solver.constraints.strong.SCConstraint;
import choco.cp.solver.constraints.strong.SCVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractMaxRPCConstraint extends SCConstraint {
    /**
     * Toutes les 3-cliques où apparaissent cette contrainte (à initialiser avec
     * compute3Cliques une fois que toutes les contraintes sont créées et liées
     * aux variables)
     */
    protected Clique[] cliques;

    protected final MaxRPCVariable[] scope;

    public AbstractMaxRPCConstraint(ISpecializedConstraint sConstraint,
            Map<IntDomainVar, SCVariable> pool) {
        super(sConstraint, pool);

        this.scope = new MaxRPCVariable[super.scope.length];
        for (int i = scope.length; --i >= 0;) {
            this.scope[i] = (MaxRPCVariable) super.scope[i];
        }
    }

    public abstract void compute3Cliques();

    protected void compute3Cliques(boolean useSupports) {
        final List<Clique> cliques = new ArrayList<Clique>();
        for (AbstractMaxRPCConstraint c1 : scope[0].getConstraints()) {
            if (c1 == this) {
                continue;
            }
            final int alt1 = (c1.getVariable(0) == scope[0] ? 1 : 0);
            final MaxRPCVariable altVariable = c1.getVariable(alt1);
            for (AbstractMaxRPCConstraint c2 : scope[1].getConstraints()) {
                final int alt2 = (c2.getVariable(0) == scope[1] ? 1 : 0);
                if (altVariable == c2.getVariable(alt2)) {
                    cliques.add(new Clique(this, (AbstractMaxRPCConstraint) c1,
                            alt1, (AbstractMaxRPCConstraint) c2, alt2,
                            useSupports));
                }
            }
        }
        this.cliques = cliques.toArray(new Clique[cliques.size()]);
    }


    public int getNbCliques() {
        return cliques.length;
    }

    public MaxRPCVariable getVariable(int position) {
        return scope[position];
    }

    public abstract boolean revise(int position) throws ContradictionException;

    public abstract boolean revisePC(Clique clique, int position)
            throws ContradictionException;

}
