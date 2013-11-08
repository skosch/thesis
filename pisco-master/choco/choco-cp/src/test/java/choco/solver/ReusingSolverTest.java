package choco.solver;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.model.variables.integer.IntegerVariable;
import org.junit.Test;

import java.util.Vector;

import static choco.Choco.*;

//import choco.kernel.model.constraints.Constraint;


public class ReusingSolverTest {

    @Test
    public void test() {
        CPModel m = new CPModel();

        IntegerVariable[] vars = new IntegerVariable[3];
        vars[0] = makeIntVar("x", 0, 2);
        vars[1] = makeIntVar("y", 0, 2);
        vars[2] = makeIntVar("z", 0, 2);


        m.addConstraint(neq(vars[0], vars[1]));
        m.addConstraint(eq(2, vars[2]));

        CPSolver s = new CPSolver();
        s.read(m);

        //Store the initial s
        s.worldPush();
        int currentWorld = s.getWorldIndex();


        //Get the first solution with y = 0
        //Restore the intial solver s
        s.worldPopUntil(currentWorld);
        s.worldPush();

        s.addConstraint(true, eq(vars[1], 0));
        s.solve();

        Vector<Integer> firstSolY0 = new Vector<Integer>();
        if (s.isFeasible()) {

            for (int j = 0; j < 3; j++) {
                firstSolY0.add(s.getVar(m.getIntVar(j)).getVal());
            }
            System.out.println("The first sol " + firstSolY0);
        }


        //Get the first solution with y = 1
        //Restore the intial solver s
        s.worldPopUntil(currentWorld);
        s.worldPush();

        s.addConstraint(true, eq(vars[1], 1));
        s.setVarIntSelector(new StaticVarOrder(s));
        s.solve();

        Vector<Integer> firstSolY1 = new Vector<Integer>();

        if (!s.isFeasible()) {
            System.out.println("There is no solution");
        } else {
            for (int j = 0; j < 3; j++) {
                firstSolY1.add(s.getVar(m.getIntVar(j)).getVal());
            }
            System.out.println("The first solution with y = 1 " + firstSolY1);
        }

        //Restore the intial solver s
        s.worldPopUntil(currentWorld);
        s.worldPush();

        s.addConstraint(true, eq(vars[1], 0));

        //Search the second solution
        IntegerVariable[] CSTS = constantArray(new int[]{1, 2, 3});
        for (int i = 0; i < CSTS.length; i++) {
            if (!s.contains(CSTS[i])) {
                System.out.println("Unknown variable");
            }
        }
        s.addConstraint(true, lex(vars, CSTS));


        s.solve();

        Vector<Integer> secondSolY0 = new Vector<Integer>();
        if (!s.isFeasible()) {
            System.out.println("There is no solution???");
        } else {
            for (int j = 0; j < 3; j++) {
                secondSolY0.add(s.getVar(m.getIntVar(j)).getVal());
            }
            System.out.println("The second sol in case y = 0 " + secondSolY0);
        }


    }


}
