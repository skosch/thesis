/**
 * Copyright (c) 1999-2010, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package trace;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.integer.BooleanVarImpl;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.pack.PackModel;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import junit.framework.Assert;
import org.junit.Test;
import trace.visualizers.*;

import static choco.Choco.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 6 déc. 2010
 */
public class CPVizTest {

    private String dir = System.getProperty("user.dir");

    @Test
    public void testNoLog() {
        int n = 4;

        Model m = new CPModel();
        IntegerVariable[] Q = Choco.makeIntVarArray("Q", n, 1, n);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                m.addConstraint(Choco.neq(Q[i], Q[j]));
                m.addConstraint(Choco.neq(Q[i], Choco.plus(Q[j], k)));
                m.addConstraint(Choco.neq(Q[i], Choco.minus(Q[j], k)));
            }
        }

        Solver s = new CPSolver();
        s.read(m);

        s.solveAll();
    }

    @Test
    public void testVector() {
        int n = 4;

        Model m = new CPModel();
        IntegerVariable[] Q = Choco.makeIntVarArray("Q", n, 1, n);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                m.addConstraint(Choco.neq(Q[i], Q[j]));
                m.addConstraint(Choco.neq(Q[i], Choco.plus(Q[j], k)));
                m.addConstraint(Choco.neq(Q[i], Choco.minus(Q[j], k)));
            }
        }

        Solver s = new CPSolver();
        s.read(m);

        Visualization visu = new Visualization("Vector", s, dir + "/out");

        visu.createTree();
        visu.createViz();

        Vector vector = new Vector(s.getVar(Q), "expanded", n, n);
        vector.setMinMax(1, n);

        visu.addVisualizer(vector);

        s.solveAll();

        visu.close();
    }

    @Test
    public void testVectorSize() {
        int n = 13;

        Model m = new CPModel();
        IntegerVariable[] Q = Choco.makeIntVarArray("Q", n, 1, n);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                m.addConstraint(Choco.neq(Q[i], Q[j]));
                m.addConstraint(Choco.neq(Q[i], Choco.plus(Q[j], k)));
                m.addConstraint(Choco.neq(Q[i], Choco.minus(Q[j], k)));
            }
        }

        Solver s = new CPSolver();
        s.read(m);

        Visualization visu = new Visualization("VectorSize", s, dir + "/out");

        visu.createTree();
        visu.createViz();

        VectorSize vector = new VectorSize(s.getVar(Q), "expanded", n, n);
        vector.setMinMax(1, n);

        visu.addVisualizer(vector);

        s.solve();

        visu.close();
    }

    @Test
    public void testVectorWaterfall() {
        int n = 4;

        Model m = new CPModel();
        IntegerVariable[] Q = Choco.makeIntVarArray("Q", n, 1, n);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                m.addConstraint(Choco.neq(Q[i], Q[j]));
                m.addConstraint(Choco.neq(Q[i], Choco.plus(Q[j], k)));
                m.addConstraint(Choco.neq(Q[i], Choco.minus(Q[j], k)));
            }
        }

        Solver s = new CPSolver();
        s.read(m);

        Visualization visu = new Visualization("VectorWaterfall", s, dir + "/out");

        visu.createTree();
        visu.createViz();

        VectorWaterfall visualizer = new VectorWaterfall(s.getVar(Q), "expanded", n, n);
        visualizer.setMinMax(1, n);

        visu.addVisualizer(visualizer);

        s.solveAll();

        visu.close();
    }

    @Test
    public void testAllDifferent() {
        Model model;
        IntegerVariable S, E, N, D, M, O, R, Y;
        IntegerVariable[] SEND, MORE, MONEY;

        model = new CPModel();

        S = makeIntVar("S", 0, 9);
        E = makeIntVar("E", 0, 9);
        N = makeIntVar("N", 0, 9);
        D = makeIntVar("D", 0, 9);
        M = makeIntVar("M", 0, 9);
        O = makeIntVar("0", 0, 9);
        R = makeIntVar("R", 0, 9);
        Y = makeIntVar("Y", 0, 9);
        SEND = new IntegerVariable[]{S, E, N, D};
        MORE = new IntegerVariable[]{M, O, R, E};
        MONEY = new IntegerVariable[]{M, O, N, E, Y};

        model.addConstraints(neq(S, 0), neq(M, 0));
        model.addConstraint(allDifferent(S, E, N, D, M, O, R, Y));
        model.addConstraints(
                eq(plus(scalar(new int[]{1000, 100, 10, 1}, SEND),
                        scalar(new int[]{1000, 100, 10, 1}, MORE)),
                        scalar(new int[]{10000, 1000, 100, 10, 1}, MONEY))
        );


        Solver solver = new CPSolver();
        solver.read(model);

        Visualization visu = new Visualization("AllDifferent", solver, dir + "/out");

        visu.createTree();
        visu.createViz();

        Vector visualizer = new Vector(solver.getVar(S, E, N, D, M, O, R, Y), "expanded", 0, 0, 8, 10, "SENDMORY", 0, 9);

        visu.addVisualizer(visualizer);

        solver.solve();

        visu.close();
    }

    @Test
    public void testElement() {
        Model m = new CPModel();
        Solver solver = new CPSolver();
        int[] values = new int[]{1, 2, 0, 4, -10};
        IntegerVariable index = makeIntVar("index", -3, 10);
        IntegerVariable value = makeIntVar("value", -20, 20);
        m.addConstraint(nth(index, values, value));
        solver.read(m);

        Visualization visu = new Visualization("Element", solver, dir + "/out");

        visu.createTree();
        visu.createViz();

        Element visualizer = new Element(solver.getVar(index), values, solver.getVar(value), "expanded", 13, 40);
        visualizer.setMinMax(-20, 20);

        visu.addVisualizer(visualizer);

        solver.solve();

        visu.close();
    }

    @Test
    public void testBinaryVector() {
        Model m = new CPModel();
        Solver solver = new CPSolver();
        IntegerVariable var = makeIntVar("var", 1, 8);
        IntegerVariable[] bool = makeBooleanVarArray("b", 8);
        m.addConstraint(domainChanneling(var, bool));

        solver.read(m);

        Visualization visu = new Visualization("BinaryVector", solver, dir + "/out");

        visu.createTree();
        visu.createViz();


        BinaryVector visualizer = new BinaryVector(solver.getVar(bool), "expanded", 8, 8);
        visualizer.setMinMax(0, 8);

        visu.addVisualizer(visualizer);

        solver.solve();

        visu.close();
    }

    @Test
    public void testDomainMatrix() {
        int n = 3;
        Model model = new CPModel();
        final int ub = n * n;
        final int ms = n * (n * n + 1) / 2;
        IntegerVariable[][] vars = makeIntVarArray("v", n, n, 1, ub);
        // All cells of the matrix must be different
        model.addConstraint(allDifferent(ArrayUtils.flatten(vars)));
        final IntegerVariable[] varDiag1 = new IntegerVariable[n];
        final IntegerVariable[] varDiag2 = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            // All rows must be equal to the magic sum
            model.addConstraint(eq(sum(vars[i]), ms));
            // All columns must be equal to the magic sum
            model.addConstraint(eq(sum(ArrayUtils.getColumn(vars, i)), ms));
            //record diagonals variable
            varDiag1[i] = vars[i][i];
            varDiag2[i] = vars[(n - 1) - i][i];
        }
        // Every diagonal have to be equal to the magic sum
        model.addConstraint(eq(sum(varDiag1), ms));
        model.addConstraint(eq(sum(varDiag2), ms));
        //symmetry breaking constraint: enforce that the upper left corner contains the minimum corner value.
        model.addConstraint(and(
                lt(vars[0][0], vars[0][n - 1]),
                lt(vars[0][0], vars[n - 1][n - 1]),
                lt(vars[0][0], vars[n - 1][0])
        ));

        Solver solver = new CPSolver();
        solver.read(model);

        Visualization visu = new Visualization("DomainMatrix", solver, dir + "/out");

        visu.createTree();
        visu.createViz();


        IntDomainVar[][] solVars = new IntDomainVar[n][n];
        for(int i = 0; i < n; i++){
            solVars[i] = solver.getVar(vars[i]);
        }
        DomainMatrix visualizer = new DomainMatrix(solVars, "expanded", 3, 3);
        visualizer.setMinMax(1, ub);

        visu.addVisualizer(visualizer);

        solver.solve();

        visu.close();
    }

    @Test
    public void testAllDifferentMatrix() {
        int n = 3;
        Model model = new CPModel();
        final int ub = n * n;
        final int ms = n * (n * n + 1) / 2;
        IntegerVariable[][] vars = makeIntVarArray("v", n, n, 1, ub);
        // All cells of the matrix must be different
        model.addConstraint(allDifferent(ArrayUtils.flatten(vars)));
        final IntegerVariable[] varDiag1 = new IntegerVariable[n];
        final IntegerVariable[] varDiag2 = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            // All rows must be equal to the magic sum
            model.addConstraint(eq(sum(vars[i]), ms));
            // All columns must be equal to the magic sum
            model.addConstraint(eq(sum(ArrayUtils.getColumn(vars, i)), ms));
            //record diagonals variable
            varDiag1[i] = vars[i][i];
            varDiag2[i] = vars[(n - 1) - i][i];
        }
        // Every diagonal have to be equal to the magic sum
        model.addConstraint(eq(sum(varDiag1), ms));
        model.addConstraint(eq(sum(varDiag2), ms));
        //symmetry breaking constraint: enforce that the upper left corner contains the minimum corner value.
        model.addConstraint(and(
                lt(vars[0][0], vars[0][n - 1]),
                lt(vars[0][0], vars[n - 1][n - 1]),
                lt(vars[0][0], vars[n - 1][0])
        ));

        Solver solver = new CPSolver();
        solver.read(model);

        Visualization visu = new Visualization("AllDifferentMatrix", solver, dir + "/out");

        visu.createTree();
        visu.createViz();


        IntDomainVar[][] solVars = new IntDomainVar[n][n];
        for(int i = 0; i < n; i++){
            solVars[i] = solver.getVar(vars[i]);
        }
        AllDifferentMatrix visualizer = new AllDifferentMatrix(solVars, "expanded", 3, 3);
        visualizer.setMinMax(1, ub);

        visu.addVisualizer(visualizer);

        solver.solve();

        visu.close();
    }

    @Test
    public void testBinaryMatrix() {
        Model m = new CPModel();
        int n = 4;
        Solver solver = new CPSolver();
        IntegerVariable[] var = makeIntVarArray("var", n, 0, n-1);
        IntegerVariable[][] bool = new IntegerVariable[n][n];
        for(int i = 0 ; i < n; i++){
            bool[i] = Choco.makeBooleanVarArray("bool_"+i, n);
            m.addConstraint(domainChanneling(var[i], bool[i]));
        }
        m.addConstraint(Choco.allDifferent(var));

        solver.read(m);

        Visualization visu = new Visualization("BinaryMatrix", solver, dir + "/out");

        visu.createTree();
        visu.createViz();


        BooleanVarImpl[][] solVars = new BooleanVarImpl[n][n];
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                solVars[i][j] = (BooleanVarImpl)solver.getVar(bool[i][j]);
            }
        }
        BinaryMatrix visualizer = new BinaryMatrix(solVars, "expanded", n, n);

        visu.addVisualizer(visualizer);

        solver.solve();
        Assert.assertTrue(solver.isFeasible());
        visu.close();
    }

    @Test
    public void testBoolChanneling() {
        Model m = new CPModel();
        Solver solver = new CPSolver();
        IntegerVariable var = makeIntVar("var", 1, 8);
        IntegerVariable[] bool = makeBooleanVarArray("b", 8);
        m.addConstraint(domainChanneling(var, bool));

        solver.read(m);

        Visualization visu = new Visualization("BoolChanneling", solver, dir + "/out");

        visu.createTree();
        visu.createViz();


        BoolChanneling visualizer = new BoolChanneling(solver.getVar(var), solver.getVar(bool), "expanded", 8, 8);
        visualizer.setMinMax(0, 8);

        visu.addVisualizer(visualizer);

        solver.solve();

        visu.close();
    }

    @Test
    public void testBinPacking() {
        IntegerVariable[] loads = new IntegerVariable[3];
        loads[0] = Choco.makeIntVar("load1", 0, 5);
        loads[1] = Choco.makeIntVar("load2", 0, 5);
        loads[2] = Choco.makeIntVar("load3", 0, 5);
        IntegerVariable[] bins = Choco.makeIntVarArray("bin", 4, 0, 1);
        int[] _sizes = new int[]{4, 3, 1, 1};
        IntegerConstantVariable[] sizes = constantArray(_sizes);

        Model m = new CPModel();
        m.addConstraint(Choco.pack(new PackModel(bins, sizes, loads)));

        Solver solver = new CPSolver();
        solver.read(m);

        Visualization visu = new Visualization("BinPacking", solver, dir + "/out");
        visu.createTree();
        visu.createViz();

        BinPacking visualizer = new BinPacking(solver.getVar(bins), _sizes, solver.getVar(loads), "expanded", 15, 15);

        visu.addVisualizer(visualizer);
        solver.solve();


        visu.close();
    }

    @Test
    public void testLexLe() {
        IntegerVariable[] X = Choco.makeIntVarArray("X", 3, 0, 1);
        IntegerVariable[] Y = Choco.makeIntVarArray("Y", 3, 0, 1);

        Model m = new CPModel();
        m.addConstraint(Choco.lexEq(X, Y));

        Solver solver = new CPSolver();
        solver.read(m);

        Visualization visu = new Visualization("LexLe", solver, dir + "/out");
        visu.createTree();
        visu.createViz();

        LexLe visualizer = new LexLe(solver.getVar(X), solver.getVar(Y), "expanded", 3, 2);

        visu.addVisualizer(visualizer);

        solver.solve();

        visu.close();
    }

    @Test
    public void testInverse() {
        IntegerVariable[] X = Choco.makeIntVarArray("X", 3, 0, 2);
        IntegerVariable[] Y = Choco.makeIntVarArray("Y", 3, 0, 2);

        Model m = new CPModel();
        m.addConstraint(Choco.inverseChanneling(X, Y));

        Solver solver = new CPSolver();
        solver.read(m);

        Visualization visu = new Visualization("Inverse", solver, dir + "/out");
        visu.createTree();
        visu.createViz();

        Inverse visualizer = new Inverse(solver.getVar(X), solver.getVar(Y), "expanded", 2, 3);

        visu.addVisualizer(visualizer);

        solver.solve();

        visu.close();
    }

    @Test
    public void testGcc() {
        IntegerVariable[] X = Choco.makeIntVarArray("X", 3, 0, 4);
        int[] values = new int[]{1, 2, 3};
        int[] low = new int[]{0, 1, 0};
        int[] up = new int[]{1, 2, 1};

        Model m = new CPModel();
        m.addConstraint(Choco.globalCardinality(X, values, low, up));

        Solver solver = new CPSolver();
        solver.read(m);

        Visualization visu = new Visualization("Gcc", solver, dir + "/out");
        visu.createTree();
        visu.createViz();

        Gcc visualizer = new Gcc(solver.getVar(X), values, low, up, "expanded", 30, 30);

        visu.addVisualizer(visualizer);

        solver.solve();

        visu.close();
    }

    @Test
    public void testCumulative() {
        CPModel m = new CPModel();
        // data
        int n = 11 + 3; //number of tasks (include the three fake tasks)
        int[] heights_data = new int[]{2, 1, 4, 2, 3, 1, 5, 6, 2, 1, 3, 1, 1, 2};
        int[] durations_data = new int[]{1, 1, 1, 2, 1, 3, 1, 1, 3, 4, 2, 3, 1, 1};
        // variables
        IntegerVariable capa = constant(7);
        IntegerVariable[] starts = makeIntVarArray("start", n, 0, 5, Options.V_BOUND);
        IntegerVariable[] ends = makeIntVarArray("end", n, 0, 6, Options.V_BOUND);
        IntegerVariable[] duration = new IntegerVariable[n];
        IntegerVariable[] height = new IntegerVariable[n];
        for (int i = 0; i < height.length; i++) {
            duration[i] = constant(durations_data[i]);
            height[i] = makeIntVar("height " + i, new int[]{0, heights_data[i]});
        }
        TaskVariable[] tasks = Choco.makeTaskVarArray("Task", starts, ends, duration);

        IntegerVariable[] bool = makeIntVarArray("taskIn?", n, 0, 1);
        IntegerVariable obj = makeIntVar("obj", 0, n, Options.V_BOUND, Options.V_OBJECTIVE);
        //post the cumulative
        m.addConstraint(cumulative("cumulative", tasks, height, constant(0), capa,
        		Options.C_CUMUL_TI));
        //post the channeling to know if the task is scheduled or not
        for (int i = 0; i < n; i++) {
            m.addConstraint(boolChanneling(bool[i], height[i], heights_data[i]));
        }
        //state the objective function
        m.addConstraint(eq(sum(bool), obj));
        CPSolver solver = new CPSolver();
        solver.read(m);
        //set the fake tasks to establish the profile capacity of the ressource
        try {
            solver.getVar(starts[0]).setVal(1);
            solver.getVar(ends[0]).setVal(2);
            solver.getVar(height[0]).setVal(2);
            solver.getVar(starts[1]).setVal(2);
            solver.getVar(ends[1]).setVal(3);
            solver.getVar(height[1]).setVal(1);
            solver.getVar(starts[2]).setVal(3);
            solver.getVar(ends[2]).setVal(4);
            solver.getVar(height[2]).setVal(4);
        } catch (ContradictionException e) {
            System.out.println("error, no contradiction expected at this stage");
        }
        Visualization visu = new Visualization("Cumulative", solver, dir + "/out");
        visu.createTree();
        visu.createViz();

        Cumulative visualizer = new Cumulative(solver.getVar(tasks), solver.getVar(capa), solver.getMakespan(), "expanded", 30, 30);

        visu.addVisualizer(visualizer);

        // maximize the number of tasks placed in this profile
//        solver.maximize(solver.getVar(obj), false);
        solver.solve();
        System.out.println("Objective : " + (solver.getVar(obj).getVal() - 3));
        for (int i = 3; i < starts.length; i++) {
            if (solver.getVar(height[i]).getVal() != 0)
                System.out.println("[" + solver.getVar(starts[i]).getVal() + " - "
                        + (solver.getVar(ends[i]).getVal() - 1) + "]:"
                        + solver.getVar(height[i]).getVal());
        }


        visu.close();
    }
}
