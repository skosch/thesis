package choco.model.constraints.set;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.set.MinEnv;
import choco.cp.solver.search.set.RandomSetValSelector;
import choco.cp.solver.search.set.RandomSetVarSelector;
import choco.cp.solver.search.set.StaticSetVarOrder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.cnf.ALogicTree;
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
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 17 déc. 2010
 * Time: 13:21:19
 * Testing the SetValuePrecede constraint
 */
public class SetValuePrecedeTests {


    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    SetVariable[] _svariable;

    private int[] buildValues(Random r, int low, int up) {
        int nb = 1 + r.nextInt(up - low + 1);
        TIntHashSet set = new TIntHashSet(nb);
        for (int i = 0; i < nb; i++) {
            set.add(low + r.nextInt(up - low + 1));
        }
        int[] values = set.toArray();
        Arrays.sort(values);
        return values;
    }


    private CPModel[] model(Random r, int size) {
        int low = r.nextInt(size);
        int up = low + 1 + r.nextInt(2 * size);

        _svariable = new SetVariable[size];
        for (int i = 0; i < _svariable.length; i++) {
            _svariable[i] = Choco.makeSetVar("s"+i, buildValues(r, low, up)) ;
        }

        SetVariable avar = _svariable[r.nextInt(size)];
        int s = avar.getValues()[r.nextInt(avar.getValues().length)];
        int t;
        do {
            SetVariable anothervar = _svariable[r.nextInt(size)];
            t = anothervar.getValues()[r.nextInt(anothervar.getValues().length)];
        }
        while (s == t);

        CPModel[] ms = new CPModel[2];
        for (int i = 0; i < ms.length; i++) {
            CPModel m = new CPModel();
            switch (i) {
                case 0:
                    m.addConstraint(Choco.setValuePrecede(_svariable, s, t));
                    break;
                case 1:
                    IntegerVariable[] bs = Choco.makeBooleanVarArray("bs", 2);
                    m.addConstraint(Choco.reifiedConstraint(bs[0], Choco.member(s, _svariable[0])));
                    m.addConstraint(Choco.reifiedConstraint(bs[1], Choco.notMember(t, _svariable[0])));
                    m.addConstraints(Choco.clauses(Node.or(Literal.pos(bs))));

                    for (int j = 1; j < size; j++) {
                        IntegerVariable[] b = Choco.makeBooleanVarArray("b", 2);
                        m.addConstraint(Choco.reifiedConstraint(b[0], Choco.member(s, _svariable[0])));
                        m.addConstraint(Choco.reifiedConstraint(b[1], Choco.notMember(t, _svariable[0])));
                        ALogicTree tree = Node.and(Literal.pos(b[0]), Literal.pos(b[1]));

                        for (int it = 1; it < j; it++) {
                            b = Choco.makeBooleanVarArray("b", 2);
                            m.addConstraint(Choco.reifiedConstraint(b[0], Choco.member(s, _svariable[it])));
                            m.addConstraint(Choco.reifiedConstraint(b[1], Choco.notMember(t, _svariable[it])));
                            tree = Node.or(tree, Node.and(Literal.pos(b[0]), Literal.pos(b[1])));
                        }
                        b = Choco.makeBooleanVarArray("b", 2);
                        m.addConstraint(Choco.reifiedConstraint(b[0], Choco.notMember(s, _svariable[j])));
                        m.addConstraint(Choco.reifiedConstraint(b[1], Choco.member(t, _svariable[j])));
                        m.addConstraints(Choco.clauses(Node.implies(Node.and(Literal.pos(b[0]), Literal.pos(b[1])), tree)));
                    }
                    break;
            }
            ms[i] = m;
        }
        return ms;
    }

    private CPSolver solve(int seed, CPModel m, boolean sta_tic) {
        CPSolver s = new CPSolver();
        s.read(m);
        if (sta_tic) {
            s.setVarSetSelector(new StaticSetVarOrder(s, s.getVar(_svariable)));
            s.setValSetSelector(new MinEnv());
        } else {
            s.setVarSetSelector(new RandomSetVarSelector(s, seed));
            s.setValSetSelector(new RandomSetValSelector(seed));
        }
        s.solveAll();
        return s;
    }




    @Test
    public void testRandom01() {
        Random r;
        for (int i = 0; i < 5; i++) {

            r = new Random(i);
            CPModel[] ms = model(r, 4);
            CPSolver[] ss = new CPSolver[ms.length];
            for (int j = 0; j < ms.length; j++) {
                ss[j] = solve(i, ms[j], false);
            }
            for (int j = 1; j < ms.length; j++) {
                Assert.assertEquals("nb solutions, seed:" + i, ss[0].getSolutionCount(), ss[j].getSolutionCount());
            }
        }
    }


    @Test
    public void test01() {  // example in section 4.2

        Model model = new CPModel();

        SetVariable[] vars = Choco.makeSetVarArray("sv", 5, 1, 3);

        model.addConstraint(Choco.notMember(vars[0],1));
        model.addConstraint(Choco.notMember(vars[1],3));
        model.addConstraint(Choco.notMember(vars[3], 1));
        model.addConstraint(Choco.notMember(vars[4], 3));

        model.addConstraint(Choco.member(vars[1], 2));

        model.addConstraint(Choco.setValuePrecede(vars, 1, 2));

        Solver solver = new CPSolver();
//        ChocoLogging.toVerbose();
        solver.read(model);
        Boolean res = solver.solve();
//        ChocoLogging.flushLogs();

        Assert.assertTrue(res);

    }

    @Test
    public void test02() {
        Model model = new CPModel();
        SetVariable[] setVariables = Choco.makeSetVarArray("sv", 2, 1, 2);

        model.addConstraint(Choco.setValuePrecede(setVariables, 1, 2));

        Solver solver = new CPSolver();
//        ChocoLogging.toSolution();
        solver.read(model);
        solver.solveAll();
//        ChocoLogging.flushLogs();

//        System.out.println("solver.getNbSolutions() = " + solver.getNbSolutions());

        Assert.assertEquals(10, solver.getNbSolutions());
        // compter les solutions
        // pos2 = 0 => pos1 = 0 et 2 accompagne 1 partout  sols = 3 (soit 1,2 pour s1 soit pas de 2)
        // pos2 = 1 => pas de 1 en 0 ni après ou bien singleton 1 en 0 (car 1,2 déjà compté) sols = 2 + 2
        // pas de 2 => sols = 3
    }



    @Test
    public void test03() {

        Model m = new CPModel();
        int s = 1;
        int t = 2;

        SetVariable[] _svariable = Choco.makeSetVarArray("sv", 2, 1 ,2);

        IntegerVariable[] bs = Choco.makeBooleanVarArray("bs", 2);
        m.addConstraint(Choco.reifiedConstraint(bs[0], Choco.member(s, _svariable[0])));
        m.addConstraint(Choco.reifiedConstraint(bs[1], Choco.notMember(t, _svariable[0])));
        m.addConstraints(Choco.clauses(Node.or(Literal.pos(bs))));

        for (int j = 1; j < _svariable.length; j++) {
            IntegerVariable[] b = Choco.makeBooleanVarArray("b", 2);
            m.addConstraint(Choco.reifiedConstraint(b[0], Choco.member(s, _svariable[0])));
            m.addConstraint(Choco.reifiedConstraint(b[1], Choco.notMember(t, _svariable[0])));
            ALogicTree tree = Node.and(Literal.pos(b[0]), Literal.pos(b[1]));

            for (int it = 1; it < j; it++) {
                b = Choco.makeBooleanVarArray("b", 2);
                m.addConstraint(Choco.reifiedConstraint(b[0], Choco.member(s, _svariable[it])));
                m.addConstraint(Choco.reifiedConstraint(b[1], Choco.notMember(t, _svariable[it])));
                tree = Node.or(tree, Node.and(Literal.pos(b[0]), Literal.pos(b[1])));
            }
            b = Choco.makeBooleanVarArray("b", 2);
            m.addConstraint(Choco.reifiedConstraint(b[0], Choco.notMember(s, _svariable[j])));
            m.addConstraint(Choco.reifiedConstraint(b[1], Choco.member(t, _svariable[j])));
            m.addConstraints(Choco.clauses(Node.implies(Node.and(Literal.pos(b[0]), Literal.pos(b[1])), tree)));
        }

        Solver solver = new CPSolver();
//        ChocoLogging.toSolution();
        solver.read(m);
        solver.solveAll();
        Assert.assertEquals(10, solver.getNbSolutions());
    }


    @Test
    public void test04() {
        Model model = new CPModel();
        SetVariable[] setVariables = Choco.makeSetVarArray("sv", 3, 0, 4);

        model.addConstraint(Choco.setValuePrecede(setVariables, 2, 4));

        model.addConstraint(Choco.member(setVariables[0], 0));
        model.addConstraint(Choco.member(setVariables[0], 1));
        model.addConstraint(Choco.notMember(setVariables[0], 4));

        model.addConstraint(Choco.notMember(setVariables[1], 0));
        model.addConstraint(Choco.notMember(setVariables[1], 1));


        model.addConstraint(Choco.notMember(setVariables[2], 0));
        model.addConstraint(Choco.notMember(setVariables[2], 1));
        model.addConstraint(Choco.notMember(setVariables[2], 2));
        model.addConstraint(Choco.notMember(setVariables[2], 3));



        Solver solver = new CPSolver();
//        ChocoLogging.toSolution();
        solver.read(model);
        solver.solveAll();
//        ChocoLogging.flushLogs();

        Assert.assertEquals(48, solver.getNbSolutions());
    }




}
