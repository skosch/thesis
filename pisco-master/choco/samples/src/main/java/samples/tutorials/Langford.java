/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
package samples.tutorials;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.cp.solver.search.integer.valselector.MaxVal;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.variables.integer.IntegerVariable;
import org.kohsuke.args4j.Option;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25/05/12
 */
public class Langford extends PatternExample {

    @Option(name = "-k", usage = "Number of sets.", required = false)
    private int k = 2;

    @Option(name = "-n", usage = "Upper bound.", required = false)
    private int n = 10;

    protected IntegerVariable[] position;

    @Override
    public void buildModel() {
        model = new CPModel();
        position = Choco.makeIntVarArray("p", n * k, 0, k * n - 1, Options.V_ENUM);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < this.k - 1; j++) {
                model.addConstraint(Choco.eq(Choco.plus(position[i + j * n], i + 2), position[i + (j + 1) * n]));
            }
        }
        model.addConstraint(Choco.lt(position[0], position[n * k - 1]));
        model.addConstraint(Choco.allDifferent(position));
    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
        solver.addGoal(new AssignOrForbidIntVarVal(new MinDomain(solver, solver.getVar(position)), new MaxVal()));
    }

    @Override
    public void solve() {
        ChocoLogging.toVerbose();
        solver.solve();
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        for(int i = 0 ; i< 10;i++)new Langford().execute(args);
    }
}
