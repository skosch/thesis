package choco.model.constraints.set;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.set.RandomSetValSelector;
import choco.cp.solver.search.set.RandomSetVarSelector;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.cnf.Literal;
import choco.kernel.model.constraints.cnf.Node;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import gnu.trove.TIntHashSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 22/12/10
 * Time: 09:25
 * To change this template use File | Settings | File Templates.
 */
public class InverseSetTests {

    @Test
    public void test01() {
        Model model = new CPModel();

        SetVariable[] x = Choco.makeSetVarArray("x", 2, 0, 2);
        SetVariable[] y = Choco.makeSetVarArray("y", 2, 0, 1, Options.V_NO_DECISION);

        model.addConstraint(Choco.inverseSet(x, y));

        Solver solver = new CPSolver();
        solver.read(model);
//        ChocoLogging.toSolution();
        solver.solveAll();
//        ChocoLogging.flushLogs();

        Assert.assertEquals(16, solver.getNbSolutions());

    }

    private int[] buildValues(Random r, int low, int up) {
        int nb = 2 + r.nextInt(up - low + 1);
        TIntHashSet set = new TIntHashSet(nb);
        for (int i = 0; i < nb; i++) {
            set.add(low + r.nextInt(up - low + 1));
        }
        int[] values = set.toArray();
        Arrays.sort(values);
        return values;
    }

    private void postInverse(CPModel model, SetVariable[] x, SetVariable[] y, int min, int max) {

        for (int valx = min; valx <= max; valx++) {
            for (int i = 0; i < x.length; i++) {
                SetVariable variable = x[i];
                if (valx < y.length) {
                    IntegerVariable[] bs = Choco.makeBooleanVarArray("bs", 2);
                    model.addConstraint(Choco.reifiedConstraint(bs[0], Choco.member(valx, variable)));
                    model.addConstraint(Choco.reifiedConstraint(bs[1], Choco.member(i, y[valx])));
                    model.addConstraints(Choco.clauses(Node.ifOnlyIf(Literal.pos(bs[0]), Literal.pos(bs[1]))));
                } else {
                    model.addConstraint(Choco.notMember(valx, variable));
                }
            }
        }

        for (int valy = 0; valy <= x.length + 3; valy++) {
            for (int i = 0; i < y.length; i++) {
                SetVariable variable = y[i];
                if (valy < x.length) {
                    IntegerVariable[] bs = Choco.makeBooleanVarArray("bs", 2);
                    model.addConstraint(Choco.reifiedConstraint(bs[0], Choco.member(valy, variable)));
                    model.addConstraint(Choco.reifiedConstraint(bs[1], Choco.member(i, x[valy])));
                    model.addConstraints(Choco.clauses(Node.ifOnlyIf(Literal.pos(bs[0]), Literal.pos(bs[1]))));
                } else {
                    model.addConstraint(Choco.notMember(valy, variable));
                }
            }

        }

    }


    @Test
    public void testRandom01() {
        for (int test = 0; test < 5; test++) {
            Random rgen = new Random(test * 123456);
            int size = Math.max(5 + test, 8);
            int min = rgen.nextInt(5);
            int max = min + 1 + rgen.nextInt(10);
            SetVariable[] x = new SetVariable[size];
            SetVariable[] y = new SetVariable[max]; // on purpose too little variables ;)
            for (int i = 0; i < x.length; i++) {
                x[i] = Choco.makeSetVar("x" + i, buildValues(rgen, min, max));
            }

            for (int i = 0; i < y.length; i++) {
                y[i] = Choco.makeSetVar("y" + i, buildValues(rgen, 0, size + 3)); // on purpose too large domains
            }

            CPModel[] models = new CPModel[2];
            for (int i = 0; i < models.length; i++) {
                models[i] = new CPModel();
            }

            models[0].addConstraint(Choco.inverseSet(x, y));

            postInverse(models[1], x, y, min, max);


            Solver[] solvers = new CPSolver[2];
            for (int i = 0; i < solvers.length; i++) {
                solvers[i] = new CPSolver();
                Solver solver = solvers[i];
                solver.read(models[i]);
                solver.setVarSetSelector(new RandomSetVarSelector(solver, i));
                solver.setValSetSelector(new RandomSetValSelector(i));

                solver.solveAll();
            }

            Assert.assertEquals(solvers[0].getNbSolutions(), solvers[1].getNbSolutions());
        }

    }

    @Test
    public void testRandom02() {
        for (int test = 0; test < 5; test++) {
            Random rgen = new Random(test * 123456);
            int size = Math.max(5 + test, 8);
            int min = rgen.nextInt(5);
            int max = min + 1 + rgen.nextInt(10);
            SetVariable[] x = new SetVariable[size];
            SetVariable[] y = new SetVariable[max]; // on purpose too little variables ;)
            for (int i = 0; i < x.length; i++) {
                x[i] = Choco.makeSetVar("x" + i, buildValues(rgen, min, max));
            }

            for (int i = 0; i < y.length; i++) {
                y[i] = Choco.makeSetVar("y" + i, buildValues(rgen, 0, size + 3), Options.V_NO_DECISION); // on purpose too large domains
            }

            Model model = new CPModel();

            model.addConstraint(Choco.inverseSet(x, y));


            Solver solver = new CPSolver();

            solver.read(model);
            solver.setVarSetSelector(new RandomSetVarSelector(solver, test));
            solver.setValSetSelector(new RandomSetValSelector(test));


            solver.solve();
            do {
                for (SetVariable var : y) {
                    Assert.assertTrue(solver.getVar(var).isInstantiated());
                }
            } while (solver.nextSolution());
        }
    }

    @Test
    public void testRandom03() {
        for (int test = 0; test < 5; test++) {
            Random rgen = new Random(test * 123456);
            int size = Math.max(5 + test, 10);
            int min = rgen.nextInt(5);
            int max = min + 1 + rgen.nextInt(10);
            SetVariable[] x = new SetVariable[size];
            SetVariable[] y = new SetVariable[max]; // on purpose too little variables ;)
            for (int i = 0; i < x.length; i++) {
                x[i] = Choco.makeSetVar("x" + i, buildValues(rgen, min, max));
            }

            for (int i = 0; i < y.length; i++) {
                y[i] = Choco.makeSetVar("y" + i, buildValues(rgen, 0, size + 3), Options.V_NO_DECISION); // on purpose too large domains
            }

            Model model = new CPModel();

            model.addConstraint(Choco.inverseSet(x, y));


            Solver solver = new CPSolver();

            solver.read(model);
            solver.setVarSetSelector(new RandomSetVarSelector(solver, test));
            solver.setValSetSelector(new RandomSetValSelector(test));

            int idx = rgen.nextInt(x.length);
            try {
                solver.getVar(x[idx]).addToKernel(solver.getVar(x[idx]).getEnveloppeInf(), null, false);
                idx = rgen.nextInt(y.length);
                solver.getVar(y[idx]).addToKernel(solver.getVar(y[idx]).getEnveloppeInf(), null, false);


                if (solver.solve()) {
                    do {
                        for (SetVariable var : y) {
                            Assert.assertTrue(solver.getVar(var).isInstantiated());
                        }
                    } while (solver.nextSolution());
                }
            } catch (ContradictionException e) {
                Assert.assertTrue(true);
            }


        }
    }


}
