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

package choco.solver;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.GreaterOrEqualXC;
import choco.cp.solver.constraints.integer.LessOrEqualXC;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;
import org.junit.*;

import java.io.IOException;
import java.util.logging.Logger;

import static choco.Choco.*;
import static java.text.MessageFormat.format;
import static org.junit.Assert.assertEquals;

/*
 * User : charles
 * Mail : cprudhom(a)emn.fr
 * Date : 16 déc. 2008
 * Since : Choco 2.0.1
 * Update : Choco 2.0.1
 */
public class SolverTest {

	protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    Model model;

	CPSolver solver;

    @Before
    public void before(){
        model = new CPModel();
        solver = new CPSolver();
    }

    @After
    public void after(){
        model = null;
        solver = null;
    }

	@Test
	@Ignore
	public void testCharge1(){
		int cpt;
		int newcpt;
		int[] nbVar = new int[]{10, 100, 1000, 100000, 1000000};
		for(int i = 1; i < nbVar.length; i++) {
			Runtime.getRuntime().gc();
			long t = System.currentTimeMillis();
			int n = nbVar[i];
			int b = 100;
			Solver solver = new CPSolver();
			IntDomainVar[] v = new IntDomainVar[n];
			for(int k = 0; k < n; k++){
				v[k] = solver.createBoundIntVar("v_"+k, 1, b);
			}
			newcpt = (int) Runtime.getRuntime().totalMemory() - (int) Runtime.getRuntime().freeMemory();
			//                if (cpt != 0) {
			//                    assertTrue(newcpt <= cpt + 10000);
			//                }
			cpt = newcpt;
			StringBuffer st = new StringBuffer();
			st.append(format("|{0} |", StringUtils.pad("" + n, -9, " ")));
			st.append(format("|{0} |", StringUtils.pad("" + (System.currentTimeMillis() - t), -5, " ")));
			st.append(format("|{0} |", StringUtils.pad("" + cpt, -10, " ")));
			LOGGER.info(st.toString());
		}
	}


	@Test
	@Ignore
	public void testCharge2(){
		int cpt = 0;
		int newcpt;
		int[] nbCstr = new int[]{10, 100, 1000, 100000, 1000000};
		for(int i = 1; i < nbCstr.length; i++) {
			Runtime.getRuntime().gc();
			long t = System.currentTimeMillis();
			int n = nbCstr[i];
			Solver solver = new CPSolver();
			IntDomainVar v  = solver.createBoundIntVar("v", 1 ,10);
			for(int k = 0; k < n; k++){
				solver.post(solver.eq(v, 5));
			}
			newcpt = (int) Runtime.getRuntime().totalMemory() - (int) Runtime.getRuntime().freeMemory();
			//                if (cpt != 0) {
			//                    assertTrue(newcpt <= cpt + 10000);
			//                }
			cpt = newcpt;
			StringBuffer st = new StringBuffer();
			st.append(format("|{0} |", StringUtils.pad("" + n, -9, " ")));
			st.append(format("|{0} |", StringUtils.pad("" + (System.currentTimeMillis() - t), -5, " ")));
			st.append(format("|{0} |", StringUtils.pad("" + cpt, -10, " ")));
			LOGGER.info(st.toString());
		}
	}


	private void checkAndSolve(Boolean res) {
        solver.clear();
		solver.read(model);
		assertEquals("check nb Constraint after read()", model.getNbConstraints(), solver.getNbIntConstraints());
		assertEquals("check nb variables after read()", model.getNbIntVars(), solver.getNbIntVars());
		assertEquals(res, solver.solve()); 
	}

	@Test 
	public void testSolveMultipleModels() { 
		//ChocoLogging.setVerbosity(Verbosity.VERBOSE);

		IntegerVariable i = makeBooleanVar("i"); 
		IntegerVariable j = makeBooleanVar("j"); 
		IntegerVariable k = makeIntVar("k", new int[]{0, 1, 2}); 
		IntegerVariable l = makeIntVar("l", new int[]{0, 1, 2}); 
		IntegerVariable z = makeBooleanVar("z"); 

		model.addVariables(i, j, k, z); 

		Constraint e1 = gt(i, j); 
		Constraint r1 = gt(z, j); 
		Constraint r2 = gt(i, z); 

		Constraint r3 = leq(k, j); 
		Constraint r4 = leq(l, j); 
		Constraint r5 = neq(k, j); 

		model.addConstraints(e1, r1, r2); 
		assertEquals("nb Constraint", 3, model.getNbConstraints());
		checkAndSolve(Boolean.FALSE);

		model.removeConstraint(r1); 
		model.removeConstraint(r2); 
		assertEquals("nb Constraint", 1, model.getNbConstraints());
		model.addConstraints(r2,r3,r4); 
		assertEquals("nb Constraint", 4, model.getNbConstraints());
		checkAndSolve(Boolean.TRUE);

		model.removeConstraint(r2); 
		model.removeConstraint(r3); 
		model.removeConstraint(r4); 
		assertEquals("nb Constraint", 1, model.getNbConstraints());
		model.addConstraints(r3,r4,r5); 
		assertEquals("nb Constraint", 4, model.getNbConstraints());
		checkAndSolve(Boolean.FALSE); 

	}

    @Test
    public void testDeletSetConstraint(){
        SetVar sv = solver.createBoundSetVar("sv", 0, 10);
        IntDomainVar iv = solver.createEnumIntVar("sv", 0, 10);
        final AbstractSConstraint c1 = (AbstractSConstraint)solver.eq(iv, 3);
        final AbstractSConstraint c2 = (AbstractSConstraint)solver.eqCard(sv, 3);
        int nbC = solver.getNbIntConstraints();
        solver.post(c1);
        solver.post(c2);
        solver.eraseConstraint(c1);
        solver.eraseConstraint(c2);
        Assert.assertEquals(nbC, solver.getNbIntConstraints());
    }

    @Test
    public void test1() throws IOException {
        // Model definition:
        // x = [1,3]
        // x != 2
        Model model = new CPModel();
        IntegerVariable X = makeIntVar("X", 1, 3);
        model.addConstraint(neq(X, 2));

        // Solver definition, based on the model
        Solver solver = new CPSolver();
        solver.read(model);

        // push a new world, to preserve the constraint x != 2
        solver.worldPush();

        // RESOLUTIONS
        // 1. add a new constraint on x: x >= 2
        // 1a. get the current world index (usefull to restore this state after)
        int wi = solver.getWorldIndex();
        // 1b. push a new world, where the new constraint can be defined safely
        solver.worldPush();
        // 1c. add the new constraint to the solver (it will be removed upon backtracking)
        solver.post(new GreaterOrEqualXC(solver.getVar(X), 2));
        // 1d. solve the problem
        solver.solve();
        Assert.assertEquals(3, solver.getVar(X).getVal());


        // 2. go back to a state where the new constraint doesn't exist anymore
        solver.worldPopUntil(wi);

        // 3. add a new constraint on x: x <= 2
        // 3a. push a new world, where the new constraint can be defined safely
        solver.worldPush();
        // 3b. add the new constraint to the solver (it will be removed upon backtracking)
        solver.post(new LessOrEqualXC(solver.getVar(X), 2));
        // 3c. as the solver has been used once, clear the search strategies
        solver.clearGoals();
        // 3d. solve the problem
        solver.solve();
        Assert.assertEquals(1, solver.getVar(X).getVal());
    }

}
