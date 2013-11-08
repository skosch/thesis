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

package choco.model.constraints.global;

import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 24-Jan-2007
 * Time: 19:31:59
 */
public class NValueTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    @Test
    public void testSolve1() {
        for (int k = 0; k < 10; k++) {
            Model pb = new CPModel();
            int n = 5;
            IntegerVariable[] vars = makeIntVarArray("v", n, 1, 3);
            IntegerVariable v = makeIntVar("nvalue", 2, 2);
            pb.addConstraint(atMostNValue(v, vars));
            CPSolver s = new CPSolver();
            s.read(pb);
            s.setVarIntSelector(new RandomIntVarSelector(s, k));
            s.setValIntSelector(new RandomIntValSelector(k + 1));
            s.solveAll();
            LOGGER.info("noeud : " + s.getNodeCount());
            LOGGER.info("temps : " + s.getTimeCount());
            assertEquals(s.getNbSolutions(),93);
        }
    }

    @Test
    public void testSolve2() {
        for (int k = 0; k < 10; k++) {
            Model pb = new CPModel();
            int n = 5;
            IntegerVariable[] vars = makeIntVarArray("v", n, 1, 3);
            IntegerVariable v = makeIntVar("nvalue", 2, 2);
            pb.addVariables(Options.V_BOUND, vars);
            pb.addVariable(Options.V_BOUND, v);
            pb.addConstraint(atMostNValue(v, vars));
            CPSolver s = new CPSolver();
            s.read(pb);
            s.setVarIntSelector(new RandomIntVarSelector(s, k));
            s.setValIntSelector(new RandomIntValSelector(k + 1));
            s.solveAll();
            LOGGER.info("noeud : " + s.getNodeCount());
            LOGGER.info("temps : " + s.getTimeCount());
            assertEquals(s.getNbSolutions(),93);
        }

    }

    /**
     * Domination de graphes de reines. Peux t on "dominer" un echiquier
     * de taille n*n par val reines (toutes les cases sont attaquees).
     * @param n
     * @param val
     * @return la liste des positions des reines.
     */
    public List dominationQueen(int n, int val) {
        LOGGER.info("domination queen Q" + n + ":" + val);
        Model pb = new CPModel();
        IntegerVariable[] vars = new IntegerVariable[n * n];
        //une variable par case avec pour domaine la reine qui l attaque. (les reines
        //sont ainsi designees par les valeurs, et les cases par les variables)
        for (int i = 0; i < vars.length; i++) {
            vars[i] = makeIntVar("v", 1, n * n);
            pb.addVariable(Options.V_LINK, vars[i]);
        }
        IntegerVariable v = makeIntVar("nvalue", val, val);
        pb.addVariables(vars);
        pb.addVariable(v);
        CPSolver s = new CPSolver();
        s.read(pb);
        //i appartient a la variable j ssi la case i est sur une ligne/colonne/diagonale de j
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                //pour chaque case
                for (int k = 1; k <= n; k++) {
                    for (int l = 1; l <= n; l++) {
                        if (!(k == i || l == j || Math.abs(i - k) == Math.abs(j - l))) {
                            try {
                                //LOGGER.info("remove from " + (n * (i - 1) + j - 1) + " value " + ((k - 1) * n + l));
                                s.getVar(vars[n * (i - 1) + j - 1]).remVal((k - 1) * n + l);
                            } catch (ContradictionException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        // une seule contrainte
        s.addConstraint(atMostNValue(v, vars));
        //s.setTimeLimit(30000);
        s.solve();
        LOGGER.info("noeud : " + s.getNodeCount());
        LOGGER.info("temps : " + s.getTimeCount());
        List<Integer> values = new LinkedList<Integer>();
        if (s.isFeasible()) {
        for (int i = 0; i < n*n; i++) {
            if (!values.contains(s.getVar(vars[i]).getVal()))
                values.add(s.getVar(vars[i]).getVal());
        }
            for (Object value : values) {
                LOGGER.info("" + value);
            }
        } else LOGGER.info("pas de solution");
        return values;
    }

    @Test
    public void testDomination1() {
        assertEquals(dominationQueen(6,3).size(),3);
    }

    @Test
    public void testDomination2() {
        assertEquals(dominationQueen(7,4).size(),4);
    }

    @Test
    public void testDomination3() {
        assertEquals(dominationQueen(8,5).size(),5);
    }

    //todo : currentElement trop long, voir perf de l intersection de domaines...
    //public void testDomination4() {
    //    assertEquals(dominationQueen(9,5).size(),0);
    //}

    @Test
    public void testIsSatisfied() {
        Model pb = new CPModel();
        IntegerVariable v1 = makeIntVar("v1", 1, 1);
        IntegerVariable v2 = makeIntVar("v2", 2, 2);
        IntegerVariable v3 = makeIntVar("v3", 3, 3);
        IntegerVariable v4 = makeIntVar("v4", 4, 4);
        IntegerVariable n = makeIntVar("n", 3, 3);
        Constraint c1 = atMostNValue(n, new IntegerVariable[]{v1, v2, v3});
        Constraint c2 = atMostNValue(n, new IntegerVariable[]{v1, v2, v3, v4});
        pb.addConstraints(c1, c2);
        CPSolver s = new CPSolver();
        s.read(pb);
        LOGGER.info(c1.pretty());
        assertTrue(s.getCstr(c1).isSatisfied());
        assertFalse(s.getCstr(c2).isSatisfied());
    }

}
