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

package samples.tutorials.to_sort.socialgolfer;

import choco.cp.model.CPModel;
import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;

import static choco.Choco.makeIntVar;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 2 juin 2008
 * Since : Choco 2.0.0
 *
 */
public class ScalarAtMost extends AbstractLargeIntSConstraint {

    public static class ScalarAtMostManager extends IntConstraintManager {
        public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables, Object parameters, List<String> options) {
            int[] params = (int[])parameters;
            return new ScalarAtMost(solver.getEnvironment(), solver.getVar(variables), params[0], params[1]);
        }
    }

    public IStateBitSet instPairs; // est-ce que la paire i à déjà été comptabilisé dans nbEqs
    public IStateInt nbEqs;        // nombre de paires instanciées à la même valeur 1
    public int n;
    public int k;                  // le nombre de rencontres (nombre de valeur à 1)


    // vars is the complete table of pairs
    // invariant vars.length == 2*n
    // les paires correspondent à (vars[i],vars[i + n])
    // impose le produit sigma_i (vars[i] * vars[i + n]) = k
    public ScalarAtMost(IEnvironment environment, IntDomainVar[] vars, int n, int k) {
        super(vars);
        this.n = n;
        this.k = k;
        nbEqs = environment.makeInt(0);
        instPairs = environment.makeBitSet(n);
    }

    public boolean productNull(IntDomainVar v1, IntDomainVar v2) {
        return v1.isInstantiatedTo(0) || v2.isInstantiatedTo(0);
    }

    public boolean productOne(IntDomainVar v1, IntDomainVar v2) {
        return v1.isInstantiatedTo(1) && v2.isInstantiatedTo(1);
    }

    public void updateDataStructure(int idx) {
        if (!instPairs.get(idx) && productOne(vars[idx], vars[idx + n])) {
            instPairs.set(idx);
            nbEqs.add(1);
        }
    }

    @Override
	public void awake() throws ContradictionException {
        nbEqs.set(0);
        for (int i = 0; i < n; i++) {
            updateDataStructure(i);
        }
        propagate();
    }

    public void propagateDiff(int i) throws ContradictionException {
        if (vars[i].isInstantiatedTo(1)) {
            vars[i + n].removeVal(1, this, false);
        } else if (vars[i + n].isInstantiatedTo(1)) {
            vars[i].removeVal(1, this, false);
        }
    }

    public void filter() throws ContradictionException {
        if (nbEqs.get() > k) {
			this.fail();
		}
        if (nbEqs.get() == k) {
            for (int i = 0; i < n; i++) {
                if (!instPairs.get(i)) {
					propagateDiff(i);
				}
            }
        }
    }


    @Override
	public void propagate() throws ContradictionException {
        filter();
    }


    @Override
	public void awakeOnInst(int idx) throws ContradictionException {
        //LOGGER.info("awake on " + vars[idx] + " to " + vars[idx].getVal());
        if (idx < n) {         // paire (idx, idx + n)
           updateDataStructure(idx);
        } else if (idx >= n) { // paire (idx - n, idx)
           updateDataStructure(idx - n);
        }
        filter();
    }

    @Override
	public String toString() {
        String s = "ScalarAtMost";
        for (int i = 0; i < n; i++) {
            s += vars[i] + "*" + vars[i + n] + "+";
        }
        return s + " = " + k;
    }

    public static void main(String[] args) {
        for (int seed = 0; seed < 100; seed++) {
            Model m = new CPModel();
            int n = 4;
            int k = 1;
            IntegerVariable[] vs1 = new IntegerVariable[2*n];
            for (int i = 0; i < 2*n; i++) {
                vs1[i] = makeIntVar("" + i, 0, 1);
            }
            m.addConstraint(new ComponentConstraint(ScalarAtMostManager.class,  new int[]{n, k}, vs1));
            Solver s = new CPSolver();
            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.solveAll();

            int nbthsol = Cnk(n, k) * (int) Math.pow(3, n - k);
            LOGGER.info("NbSol : " + s.getNbSolutions() + " The " + nbthsol);
        }
    }

    public static int Cnk(int n, int k) {
        int num = n;
        for (int i = n - 1; i > (n - k); i--) {
            num *= i;
        }
        int den = 1;
        for (int i = 2; i <= k; i++) {
            den *= i;
        }
        return num / den;
    }

    @Override
	public boolean isSatisfied() {
        throw new UnsupportedOperationException("isSatisfied not yet implemented");
    }

    public void propagateEq(int i) throws ContradictionException {
         //LOGGER.info(this + " instantiate " + vars[i + n] + " et " + vars[i] + " a 1");
         vars[i + n].instantiate(1, this, false);
         vars[i].instantiate(1, this, false);
     }

}
