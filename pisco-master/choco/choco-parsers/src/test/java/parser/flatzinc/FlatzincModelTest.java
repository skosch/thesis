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

package parser.flatzinc;

import choco.cp.solver.CPSolver;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import parser.flatzinc.ast.SolveGoal;
import parser.flatzinc.parser.FZNParser;
import parser.flatzinc.parser.TerminalParser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 13 janv. 2010
* Since : Choco 2.1.1
* 
*/
public class FlatzincModelTest {

    FZNParser fzn;
    CPSolver s;

    @Before
    public void before() {
        fzn = new FZNParser();
//        s = new CPSolver();
        s = new PreProcessCPSolver();
//        try {
//            File f = File.createTempFile("LOG", ".log");
//            System.out.println(f.getAbsolutePath());
//            ChocoLogging.setFileHandler(f);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Test
    public void test1() {
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 2: a::output_var;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_ne(a, 1);");
        SolveGoal sg = TerminalParser.parse(fzn.SOLVE_GOAL, "solve satisfy;");
        s.read(fzn.model);
        sg.defineGoal(s);
        s.launch();
        Assert.assertEquals(1, s.getSolutionCount());
    }

    @Test
    public void test2() {
        fzn.instance = "var 1 .. 2: a::output_var;\n" +
                "constraint int_ne(a, 1);\n" +
                "solve satisfy;";
        SolveGoal sg = fzn.parse();
        s.read(fzn.model);
        sg.defineGoal(s);
        s.launch();
        Assert.assertEquals(1, s.getSolutionCount());
    }

    @Test
    public void test3() {
        fzn.instance = "array[1 .. 2] of var 1 .. \n" +
                "2: q;\n" +
                "constraint int_ne(q[1], q[2]);\n" +
                "solve satisfy;";

        SolveGoal sg = fzn.parse();
        s.read(fzn.model);
        sg.defineGoal(s);
        s.launch();
        Assert.assertEquals(1, s.getSolutionCount());
    }


    @Test
    public void test4() throws URISyntaxException {
        tester("nsp_1_1.fzn", false, 0, "objective", 0);
    }


    @Test
    public void test50() throws URISyntaxException, ContradictionException {
        // Best known objective = 1
        tester("roster_model_chicroster_dataset_1.fzn", true, 1,
                "objective", 1);
    }

    @Test
    public void test51() throws URISyntaxException, ContradictionException {
        // Best known objective = 0
        tester("roster_model_chicroster_dataset_2.fzn", true, 1,
                "objective", 0);
    }

    @Test
    public void test52() throws URISyntaxException, ContradictionException {
        // Best known objective = 0
        tester("roster_model_chicroster_dataset_3.fzn", true, 1,
                "objective", 0);
    }

    @Test
    public void test6() throws URISyntaxException {
        tester("talent_scheduling_small.fzn", true, 1, "objective", 0);
    }

    @Test
    public void test7() throws URISyntaxException, IOException {
        tester("talent_scheduling_01_small.fzn", true, 1,
                "objective", 0);
    }

    @Test
    @Ignore
    public void test8() throws URISyntaxException {
        tester("black-hole_1.fzn", true, 1,
                "objective", 0);
    }

    @Test
    public void test9() throws URISyntaxException {
        tester("black-hole_17.fzn", false, 0, "objective", 0);
    }

    @Test
    public void test10() throws URISyntaxException {
        tester("debruijn_binary_02_03.fzn", false, 1, "objective", 0);
    }

    @Test
    public void test10b() throws URISyntaxException {
        tester("2DLevelPacking11.fzn", false, 2, "objective", 0);
    }

    @Test
    public void test11() {
        fzn.instance =
                "array[1 .. 3] of var 1 .. 10: vars;\n" +
                "array[1 .. 3] of int: covers = [1,5,8];\n" +
                "array[1 .. 3] of int: lbound = [0,1,0];\n" +
                "array[1 .. 3] of int: ubound = [1,1,1];\n" +
                "constraint global_globalCardinalityLowUp(vars, covers, lbound, ubound);\n" +
                "solve satisfy;";

        SolveGoal sg = fzn.parse();
        s.read(fzn.model);
        sg.defineGoal(s);
        s.launch();
        Assert.assertEquals(1, s.getSolutionCount());
    }

    /******************************************************************************************************************/
    /**
     * **************************************************************************************************************
     */

    private void tester(String filename, boolean opt, int nbSol, String objective, int bestKnownValue) throws URISyntaxException {
        //ChocoLogging.setVerbosity(Verbosity.VERBOSE);
        String f = getClass().getResource("/flatzinc").toURI().getPath();
        fzn.loadInstance(new File(f + File.separator + filename));
        SolveGoal sg = fzn.parse();
        s.read(fzn.model);
        sg.defineGoal(s);
        s.launch();

        if (opt) {
            Assert.assertTrue(s.getSolutionCount() > 0);
            Assert.assertEquals(Boolean.TRUE, s.checkSolution());
            solutionChecker(s, objective, bestKnownValue);
        } else {
            Assert.assertEquals(nbSol, s.getSolutionCount());
            if (nbSol > 0) {
                Assert.assertEquals(Boolean.TRUE, s.checkSolution());
            }
        }
    }

    private void solutionChecker(CPSolver s, String objective, int bestKnownValue) {
        Iterator<IntegerVariable> it = fzn.model.getIntVarIterator();
        while (it.hasNext()) {
            IntegerVariable v = it.next();
            if (v.getName().equals(objective)) {
                IntDomainVar vv = s.getVar(v);
                Assert.assertTrue(v.getName() + " is not well instantiated!\n Current value is : " + vv.getVal() +
                        " -- excepted value is : " + bestKnownValue,
                        vv.isInstantiatedTo(bestKnownValue));
            }
        }
    }

}
