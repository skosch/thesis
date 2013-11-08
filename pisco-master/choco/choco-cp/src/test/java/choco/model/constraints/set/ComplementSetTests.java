package choco.model.constraints.set;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.set.RandomSetValSelector;
import choco.cp.solver.search.set.RandomSetVarSelector;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.cnf.Literal;
import choco.kernel.model.constraints.cnf.Node;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
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
 * Time: 00:00
 * To change this template use File | Settings | File Templates.
 */
public class ComplementSetTests {

    @Test
    public void test01() {
        Model m = new CPModel();

        SetVariable[] sv = Choco.makeSetVarArray("s", 2, 1, 3);

        m.addVariables(sv);
        m.addConstraint(Choco.complementSet(sv[0], sv[1]));

        Solver s = new CPSolver();
//        ChocoLogging.toSolution();
        s.read(m);
        s.solveAll();
//        ChocoLogging.flushLogs();

        Assert.assertEquals(8, s.getNbSolutions());
    }

    private void postComplement(CPModel m, SetVariable x, SetVariable y, int min, int max) {
        for (int i = min ; i <= max; i++) {
            IntegerVariable[] bs = Choco.makeBooleanVarArray("bs", 2);
            m.addConstraint(Choco.reifiedConstraint(bs[0],Choco.member(i, x)));
            m.addConstraint(Choco.reifiedConstraint(bs[1],Choco.member(i, y)));
            m.addConstraints(Choco.clauses(Node.ifOnlyIf(Literal.neg(bs[0]), Literal.pos(bs[1]))));
            //m.addConstraints(Choco.clauses(Node.or(Literal.pos(bs[0]), Literal.pos(bs[1]))));
            //m.addConstraints(Choco.clauses(Node.or(Literal.neg(bs[1]), Literal.neg(bs[0]))));
        }

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


    @Test
    public void testRandom01() {
        for (int test = 0; test < 20 ; test++)  {
            Random rgen = new Random(test * 123456);
            int min = rgen.nextInt(10);
            int max = min + 1 + rgen.nextInt(10);
            SetVariable[] svars = new SetVariable[4];
            for (int i = 0; i < svars.length; i++) {
                svars[i] = (i % 2) == 0 ? Choco.makeSetVar("sv"+i,  buildValues(rgen, min, max))
                        : Choco.makeSetVar("sv"+i, min, max);
            }

            CPModel[] models = new CPModel[2];
            for (int i = 0; i < models.length; i++) {
                models[i] = new CPModel();
            }

            models[0].addConstraint(Choco.complementSet(svars[0], svars[1]));
            models[0].addConstraint(Choco.complementSet(svars[2], svars[3]));

            postComplement(models[1],svars[0], svars[1], min, max);
            postComplement(models[1],svars[2], svars[3], min, max);


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
        for (int test = 0; test < 200 ; test++)  {
            Random rgen = new Random(test * 123456);
            int min = rgen.nextInt(10);
            int max = min + 1 + rgen.nextInt(20);
            SetVariable[] svars = new SetVariable[4];
            int[] mins = new int[4];
            int[] maxs = new int[4];
            for (int i = 0; i < svars.length; i++) {
                int[]  vals =  buildValues(rgen, min, max);
                svars[i] = Choco.makeSetVar("sv"+i, vals);
                mins[i] = vals[0];
                maxs[i] = vals[vals.length - 1];
            }

            CPModel[] models = new CPModel[2];
            for (int i = 0; i < models.length; i++) {
                models[i] = new CPModel();
            }

            models[0].addConstraint(Choco.complementSet(svars[0], svars[1]));
            models[0].addConstraint(Choco.complementSet(svars[2], svars[3]));

            postComplement(models[1],svars[0], svars[1], Math.min(mins[0], mins[1]), Math.max(maxs[0], maxs[1]));
            postComplement(models[1],svars[2], svars[3], Math.min(mins[2], mins[3]), Math.max(maxs[2], maxs[3]));


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
}
