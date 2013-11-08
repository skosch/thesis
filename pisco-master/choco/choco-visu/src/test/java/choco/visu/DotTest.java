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

package choco.visu;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.Model;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.visu.components.panels.VarChocoPanel;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static choco.Choco.*;
import static choco.visu.components.papplets.ChocoPApplet.DOTTYTREESEARCH;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 25 sept. 2008
 * Time: 18:23:06
 */
public class DotTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    @Before
    public void checkEnvironment(){
        GraphicsEnvironment ge = GraphicsEnvironment
        .getLocalGraphicsEnvironment();
        if(ge.isHeadlessInstance()){
            System.exit(0);
        }
    }

	public static String createDotFileName(String prefix) {
		try {
			String filename = File.createTempFile(prefix,".dot").getAbsolutePath();
			LOGGER.info("generated filename : "+filename);
			return filename;
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;

	}

    @Test
    public void nQueensNaifRedSolve() {
        int n = 6;
        Model m = new CPModel();

        IntegerVariable[] queens = new IntegerVariable[n];
        IntegerVariable[] queensdual = new IntegerVariable[n];

        for (int i = 0; i < n; i++) {
            queens[i] = makeIntVar("Q" + i, 1, n);
            queensdual[i] = makeIntVar("QD" + i, 1, n);
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                m.addConstraint(neq(queens[i], queens[j]));
                m.addConstraint(neq(queens[i], plus(queens[j], k)));  // diagonal constraints
                m.addConstraint(neq(queens[i], minus(queens[j], k))); // diagonal constraints
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                m.addConstraint(neq(queensdual[i], queensdual[j]));
                m.addConstraint(neq(queensdual[i], plus(queensdual[j], k)));  // diagonal constraints
                m.addConstraint(neq(queensdual[i], minus(queensdual[j], k))); // diagonal constraints
            }
        }
        m.addConstraint(inverseChanneling(queens, queensdual));

        String url = createDotFileName("choco");
        Solver s = new CPSolver();
            s.read(m);

    //    s.setVarIntSelector(new MinDomain(s,s.getVar(queens)));
        s.attachGoal(new AssignVar(new MinDomain(s,s.getVar(queens)),new IncreasingDomain()));

        int timeLimit = 60000;
        s.setTimeLimit(timeLimit);

        Visu v = Visu.createVisu(220, 200);
        Variable[] vars = ArrayUtils.append(queens, queensdual);
        v.addPanel(new VarChocoPanel("Dotty", vars, DOTTYTREESEARCH, new Object[]{url, 100, null, null, null}));

        // Solve the model
        s.setFirstSolution(true);
        s.generateSearchStrategy();
        v.listen(s);
        s.launch();


        v.kill();
    }


    @Test
    public void nQueensNaifRedSolveAll() {
        int n = 6;
        Model m = new CPModel();

        IntegerVariable[] queens = new IntegerVariable[n];
        IntegerVariable[] queensdual = new IntegerVariable[n];

        for (int i = 0; i < n; i++) {
            queens[i] = makeIntVar("Q" + i, 1, n);
            queensdual[i] = makeIntVar("QD" + i, 1, n);
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                m.addConstraint(neq(queens[i], queens[j]));
                m.addConstraint(neq(queens[i], plus(queens[j], k)));  // diagonal constraints
                m.addConstraint(neq(queens[i], minus(queens[j], k))); // diagonal constraints
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                m.addConstraint(neq(queensdual[i], queensdual[j]));
                m.addConstraint(neq(queensdual[i], plus(queensdual[j], k)));  // diagonal constraints
                m.addConstraint(neq(queensdual[i], minus(queensdual[j], k))); // diagonal constraints
            }
        }
        m.addConstraint(inverseChanneling(queens, queensdual));

        String url = createDotFileName("choco");
        Solver s = new CPSolver();
            s.read(m);

    //    s.setVarIntSelector(new MinDomain(s,s.getVar(queens)));
        s.attachGoal(new AssignVar(new MinDomain(s,s.getVar(queens)),new IncreasingDomain()));

//        int timeLimit = 60000;
//        s.setTimeLimit(timeLimit);
        Visu v = Visu.createVisu(220, 200);
        Variable[] vars = ArrayUtils.append(queens, queensdual);
        v.addPanel(new VarChocoPanel("Dotty", vars, DOTTYTREESEARCH, new Object[]{url, 100, null, null, Boolean.TRUE}));

        // Solve the model
        s.setFirstSolution(false);
        s.generateSearchStrategy();
        v.listen(s);
        s.launch();
        v.kill();
    }

    @Test
    public void knapSack() {
        IntegerVariable obj1;
        IntegerVariable obj2;
        IntegerVariable obj3;
        IntegerVariable c;

        Model m = new CPModel();

        obj1 = makeIntVar("obj1", 0, 5);
        obj2 = makeIntVar("obj2", 0, 7);
        obj3 = makeIntVar("obj3", 0, 10);
        c = makeIntVar("cost", 1, 1000000);
        m.addVariable(Options.V_BOUND, c);

        int capacity = 34;

        int[] volumes = new int[]{7, 5, 3};
        int[] energy = new int[]{6, 4, 2};

        m.addConstraint(leq(scalar(volumes, new IntegerVariable[]{obj1, obj2, obj3}), capacity));
        m.addConstraint(eq(scalar(energy, new IntegerVariable[]{obj1, obj2, obj3}), c));

        String url = createDotFileName("choco");
        Solver s = new CPSolver();
        s.read(m);

        //s.setValIntIterator(new DecreasingDomain());
        Visu v = Visu.createVisu(220, 200);
        Variable[] vars = new Variable[]{obj1, obj2, obj3};
        v.addPanel(new VarChocoPanel("Dotty", vars, DOTTYTREESEARCH, new Object[]{url, 100, s.getVar(c), Boolean.TRUE, Boolean.TRUE}));

        // Solve the model
        s.setDoMaximize(true);
        s.setObjective(s.getVar(c));
        s.setRestart(true);
        s.setFirstSolution(false);

        s.generateSearchStrategy();
        v.listen(s);

        s.launch();
        LOGGER.info("obj1: " + s.getVar(obj1).getVal());
        LOGGER.info("obj2: " + s.getVar(obj2).getVal());
        LOGGER.info("obj3: " + s.getVar(obj3).getVal());
        LOGGER.info("cost: " + s.getVar(c).getVal());
        v.kill();
    }

}
