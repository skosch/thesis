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

package samples.documentation;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.real.CyclicRealVarSelector;
import choco.cp.solver.search.real.RealIncreasingDomain;
import choco.cp.solver.search.set.MinDomSet;
import choco.cp.solver.search.set.MinEnv;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealExpressionVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.Configuration;

import java.text.MessageFormat;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 7 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 */
class Dintroduction {

    public static void main(String[] args) {
        Dintroduction.cyclohexane();
    }

    public static void magicsquare() {
        //totex imagicsquare1
        // Constant declaration
        int n = 3; // Order of the magic square
        int magicSum = n * (n * n + 1) / 2; // Magic sum
        // Build the model
        CPModel m = new CPModel();
        //totex

        //totex imagicsquare2
        // Creation of an array of variables
        IntegerVariable[][] var = new IntegerVariable[n][n];
        // For each variable, we define its name and the boundaries of its domain.
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                var[i][j] = Choco.makeIntVar("var_" + i + "_" + j, 1, n * n);
                // Associate the variable to the model.
                m.addVariable(var[i][j]);
            }
        }
        //totex

        //totex imagicsquare3
        // All cells of the matrix must be different
        for (int i = 0; i < n * n; i++) {
            for (int j = i + 1; j < n * n; j++) {
                Constraint c = (Choco.neq(var[i / n][i % n], var[j / n][j % n]));
                m.addConstraint(c);
            }
        }
        //totex

        //totex imagicsquare4
        // All row's sum must be equal to the magic sum
        for (int i = 0; i < n; i++) {
            m.addConstraint(Choco.eq(Choco.sum(var[i]), magicSum));
        }
        //totex

        //totex imagicsquare5
        IntegerVariable[][] varCol = new IntegerVariable[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // Copy of var in the column order
                varCol[i][j] = var[j][i];
            }
            // All column's sum is equal to the magic sum
            m.addConstraint(Choco.eq(Choco.sum(varCol[i]), magicSum));
        }
        //totex

        //totex imagicsquare6
        IntegerVariable[] varDiag1 = new IntegerVariable[n];
        IntegerVariable[] varDiag2 = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            varDiag1[i] = var[i][i]; // Copy of var in varDiag1
            varDiag2[i] = var[(n - 1) - i][i]; // Copy of var in varDiag2
        }
        // All diagonal's sum has to be equal to the magic sum
        m.addConstraint(Choco.eq(Choco.sum(varDiag1), magicSum));
        m.addConstraint(Choco.eq(Choco.sum(varDiag2), magicSum));
        //totex

        //totex imagicsquare7
        // Build the solver
        CPSolver s = new CPSolver();
        //totex
        //totex imagicsquare8
        // Read the model
        s.read(m);
        // Solve the model
        s.solve();
        // Print the solution
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(MessageFormat.format("{0} ", s.getVar(var[i][j]).getVal()));
            }
            System.out.println();
        }
        //totex
    }

    public static void nqueen() {
        //totex inqueen
        int nbQueen = 8;
        //1- Create the model
        CPModel m = new CPModel();
        //2- Create the variables
        IntegerVariable[] queens = Choco.makeIntVarArray("Q", nbQueen, 1, nbQueen);
        //3- Post constraints
        for (int i = 0; i < nbQueen; i++) {
            for (int j = i + 1; j < nbQueen; j++) {
                int k = j - i;
                m.addConstraint(Choco.neq(queens[i], queens[j]));
                m.addConstraint(Choco.neq(queens[i], Choco.plus(queens[j], k))); // diagonal constraints
                m.addConstraint(Choco.neq(queens[i], Choco.minus(queens[j], k))); // diagonal constraints
            }
        }
        //4- Create the solver
        CPSolver s = new CPSolver();
        s.read(m);
        s.solveAll();
        //5- Print the number of solutions found
        System.out.println("Number of solutions found:"+s.getSolutionCount());
        //totex
    }

    public static void ternarysteiner() {
        //totex iternarysteiner
        //1- Create the problem
        CPModel mod = new CPModel();
        int m = 7;
        int n = m * (m - 1) / 6;

        //2- Create Variables
        SetVariable[] vars = new SetVariable[n]; // A variable for each set
        SetVariable[] intersect = new SetVariable[n * n]; // A variable for each pair of sets
        for (int i = 0; i < n; i++)
            vars[i] = Choco.makeSetVar("set " + i, 1, n);
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
                intersect[i * n + j] = Choco.makeSetVar("interSet " + i + " " + j, 1, n);

        //3- Post constraints
        for (int i = 0; i < n; i++)
            mod.addConstraint(Choco.eqCard(vars[i], 3));
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++) {
                // the cardinality of the intersection of each pair is equal to one
                mod.addConstraint(Choco.setInter(vars[i], vars[j], intersect[i * n + j]));
                mod.addConstraint(Choco.leqCard(intersect[i * n + j], 1));
            }

        //4- Search for a solution
        CPSolver s = new CPSolver();
        s.read(mod);
        s.setVarSetSelector(new MinDomSet(s, s.getVar(vars)));
        s.setValSetSelector(new MinEnv());
        s.solve();

        //5- Print the solution found
        for(SetVariable var: vars){
            System.out.println(s.getVar(var).pretty());
        }
        //totex
    }

    public static void cyclohexane() {
        //totex icyclohexane
        //1- Create the problem
        
        CPModel pb = new CPModel();

        //2- Create the variable
        RealVariable x = Choco.makeRealVar("x", -1.0e8, 1.0e8);
        RealVariable y = Choco.makeRealVar("y", -1.0e8, 1.0e8);
        RealVariable z = Choco.makeRealVar("z", -1.0e8, 1.0e8);

        //3- Create and post the constraints
        RealExpressionVariable exp1 = Choco.plus(Choco.mult(Choco.power(y, 2), Choco.plus(1, Choco.power(z, 2))),
                Choco.mult(z, Choco.minus(z, Choco.mult(24, y))));

        RealExpressionVariable exp2 = Choco.plus(Choco.mult(Choco.power(z, 2), Choco.plus(1, Choco.power(x, 2))),
                Choco.mult(x, Choco.minus(x, Choco.mult(24, z))));

        RealExpressionVariable exp3 = Choco.plus(Choco.mult(Choco.power(x, 2), Choco.plus(1, Choco.power(y, 2))),
                Choco.mult(y, Choco.minus(y, Choco.mult(24, x))));

        Constraint eq1 = Choco.eq(exp1, -13);
        Constraint eq2 = Choco.eq(exp2, -13);
        Constraint eq3 = Choco.eq(exp3, -13);

        pb.addConstraint(eq1);
        pb.addConstraint(eq2);
        pb.addConstraint(eq3);

        //4- Search for all solution
        CPSolver s = new CPSolver();
        s.getConfiguration().putDouble(Configuration.REAL_PRECISION, 1e-8);
        s.read(pb);
        s.setVarRealSelector(new CyclicRealVarSelector(s));
        s.setValRealIterator(new RealIncreasingDomain());
        s.solve();
        //5- print the solution found
        System.out.println("x " + s.getVar(x).getValue());
        System.out.println("y " + s.getVar(y).getValue());
        System.out.println("z " + s.getVar(z).getValue());
        //totex
    }
}
