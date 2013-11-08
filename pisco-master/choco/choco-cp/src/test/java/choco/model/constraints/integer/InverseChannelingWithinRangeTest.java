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
package choco.model.constraints.integer;


import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.set.RandomSetValSelector;
import choco.cp.solver.search.set.RandomSetVarSelector;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 26/04/11
 * Time: 13:59
 */
public class InverseChannelingWithinRangeTest {


    public boolean isSatisfied(int[] tuple, int nbx, int nby, int min) {

        for (int i = 0; i < nbx; i++) {
            int x = tuple[i];
            if (x - min < nby && (tuple[x - min + nbx] != i + min)) return false;

        }

        for (int i = nbx ; i < tuple.length ; i++) {
            int x = tuple[i];
            if (x - min < nbx  &&(tuple[x - min] != i - nbx + min)) return false ;
        }

        return true;
    }


    @Test
    public void test01() {
        // first basic test

        Model model = new CPModel();

        IntegerVariable[] x = Choco.makeIntVarArray("x", 3, 0, 4);
        IntegerVariable[] y = Choco.makeIntVarArray("y", 4, 0, 3);

        model.addVariables(x);
        model.addVariables(y);

        Constraint c = Choco.inverseChannelingWithinRange(x,y);


        model.addConstraint (c);

        Solver solver = new CPSolver();
        solver.read(model);
        solver.solveAll();


        Model m2 = new CPModel();
        m2.addVariables(x);
        m2.addVariables(y);

        Solver s2 = new CPSolver();
        s2.read(m2);

        int min = s2.getVar(x[0]).getInf();

        int nbsolutions = 0;
        s2.solve();
        do  {

            int[] vals = new int[x.length + y.length];

            for (int i = 0 ; i < x.length ; i++) {
                vals[i]  = s2.getVar(x[i]).getVal();
            }

            for (int i = 0 ; i < y.length ; i++) {
                vals[i + x.length] = s2.getVar(y[i]).getVal();
            }

            if (isSatisfied(vals, x.length, y.length, min)) {
                nbsolutions++;
            }
        } while (s2.nextSolution());


        Assert.assertEquals(nbsolutions, solver.getNbSolutions());
    }

    @Test
    public void test02() {

        // comparing InverseChannelingWithinRange with InverseChanneling
        // testing also the "min" factor

        IntegerVariable[] x = Choco.makeIntVarArray("x", 5, 1, 5);
        IntegerVariable[] y = Choco.makeIntVarArray("y", 5, 1, 5);


        CPModel[] models = new CPModel[2];
        for (int i = 0; i < models.length; i++) {
            models[i] = new CPModel();
        }

        models[0].addConstraint(Choco.inverseChanneling(x,y));
        models[1].addConstraint(Choco.inverseChannelingWithinRange(x,y));


//        ChocoLogging.toSolution();

        Solver[] solvers = new CPSolver[2];
        for (int i = 0; i < solvers.length; i++) {
            solvers[i] = new CPSolver();
            Solver solver = solvers[i];
            solver.read(models[i]);
            solver.solveAll();
        }

//        ChocoLogging.flushLogs();

        Assert.assertEquals(solvers[0].getNbSolutions(), solvers[1].getNbSolutions());

    }


    @Test
    public void test03() {

        // the same as test02 with bound variables and randomized search

        IntegerVariable[] x = Choco.makeIntVarArray("x", 5, 1, 5, Options.V_BOUND);
        IntegerVariable[] y = Choco.makeIntVarArray("y", 5, 1, 5, Options.V_BOUND);


        CPModel[] models = new CPModel[2];
        for (int i = 0; i < models.length; i++) {
            models[i] = new CPModel();
        }

        models[0].addConstraint(Choco.inverseChanneling(x,y));
        models[1].addConstraint(Choco.inverseChannelingWithinRange(x,y));


//        ChocoLogging.toSolution();

        Solver[] solvers = new CPSolver[2];
        for (int i = 0; i < solvers.length; i++) {
            solvers[i] = new CPSolver();
            Solver solver = solvers[i];
            solver.read(models[i]);

            solver.setVarSetSelector(new RandomSetVarSelector(solver, i));
            solver.setValSetSelector(new RandomSetValSelector(i));

            solver.solveAll();
        }

//        ChocoLogging.flushLogs();

        Assert.assertEquals(solvers[0].getNbSolutions(), solvers[1].getNbSolutions());

    }

    @Test
    public void test04() {
        // basic test with min

        Model model = new CPModel();

        IntegerVariable[] x = Choco.makeIntVarArray("x", 3, 1, 5);
        IntegerVariable[] y = Choco.makeIntVarArray("y", 4, 1, 4);

        model.addVariables(x);
        model.addVariables(y);

        Constraint c = Choco.inverseChannelingWithinRange(x,y);


        model.addConstraint (c);

        Solver solver = new CPSolver();
        solver.read(model);
        solver.solveAll();


        Model m2 = new CPModel();
        m2.addVariables(x);
        m2.addVariables(y);

        Solver s2 = new CPSolver();
        s2.read(m2);

        int min = s2.getVar(x[0]).getInf();

        int nbsolutions = 0;
        s2.solve();
        do  {

            int[] vals = new int[x.length + y.length];

            for (int i = 0 ; i < x.length ; i++) {
                vals[i]  = s2.getVar(x[i]).getVal();
            }

            for (int i = 0 ; i < y.length ; i++) {
                vals[i + x.length] = s2.getVar(y[i]).getVal();
            }

            if (isSatisfied(vals, x.length, y.length, min)) {
                nbsolutions++;
            }
        } while (s2.nextSolution());


        Assert.assertEquals(nbsolutions, solver.getNbSolutions());
    }

    @Test
    public void test05() {
        // testing a bug identified by Tanguy Lapegue
        Model model = new CPModel();

        IntegerVariable[] x = new IntegerVariable[4];
        x[0] = Choco.makeIntVar("x0", 2, 3, "cp:enum");
        x[1] = Choco.makeIntVar("x1", 0, 2, "cp:enum");
        x[2] = Choco.makeIntVar("x2", 2, 3, "cp:enum");
        x[3] = Choco.makeIntVar("x3", 0, 1, "cp:enum");

        IntegerVariable[] y = new IntegerVariable[2];
        y[0] = Choco.makeIntVar("y0", 2, 3, "cp:enum");
        y[1] = Choco.makeIntVar("y1", 0, 2, "cp:enum");


        model.addVariables(x);
        model.addVariables(y);

        Constraint c = Choco.inverseChannelingWithinRange(x,y);


        model.addConstraint (c);
        Solver solver = new CPSolver();
        solver.read(model);

        solver.solveAll();

        Assert.assertEquals(4, solver.getNbSolutions());
    }

    @Test
    public void test06() {
        // testing a bug identified by Tanguy Lapegue
        Model model = new CPModel();

        IntegerVariable[] x = new IntegerVariable[4];
        x[0] = Choco.makeIntVar("x0", 3, 4, "cp:enum");
        x[1] = Choco.makeIntVar("x1", 1, 3, "cp:enum");
        x[2] = Choco.makeIntVar("x2", 3, 4, "cp:enum");
        x[3] = Choco.makeIntVar("x3", 1, 2, "cp:enum");

        IntegerVariable[] y = new IntegerVariable[2];
        y[0] = Choco.makeIntVar("y0", 3, 4, "cp:enum");
        y[1] = Choco.makeIntVar("y1", 1, 3, "cp:enum");


        model.addVariables(x);
        model.addVariables(y);

        Constraint c = Choco.inverseChannelingWithinRange(x,y);


        model.addConstraint (c);
        Solver solver = new CPSolver();
        solver.read(model);

        solver.solveAll();

        Assert.assertEquals(4, solver.getNbSolutions());
    }


}
