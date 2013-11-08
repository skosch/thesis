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

import choco.Choco;
import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.DFA;
import choco.kernel.model.constraints.automaton.Transition;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.*;
import static org.junit.Assert.assertEquals;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 26 d�c. 2006
 * Time: 18:47:04
 * To change this template use File | Settings | File Templates.
 */
public class RegularTest {
    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    CPModel m;
    CPSolver s;

    @Before
    public void before() {
        m = new CPModel();
        s = new CPSolver();
    }

    @After
    public void after() {
        m = null;
        s = null;
    }

    @Test
    public void testAutoExampleNegative() {
        IntegerVariable v1 = makeIntVar("v1", -5, 5);
        IntegerVariable v2 = makeIntVar("v2", -5, 5);
        IntegerVariable v3 = makeIntVar("v3", -5, 5);
        IntegerVariable v5 = makeIntVar("v5", -5, 5);
        IntegerVariable v6 = makeIntVar("v6", -5, 5);

        //remove some forbidden tuples (here, the tuples define a not_all_equal constraint)
        List<int[]> tuples = new LinkedList<int[]>();
        tuples.add(new int[]{-1, 3, 0});
        tuples.add(new int[]{2, -3, 3});
        tuples.add(new int[]{3, -3, 3});

        // post the constraint
        m.addConstraint(regular(new IntegerVariable[]{v1, v2, v3}, tuples));
        m.addConstraint(regular(new IntegerVariable[]{v2, v5, v6}, tuples));

        //m.addConstraint(regular(dfa,new IntegerVariable[]{v1, v2, v3}));
        //m.addConstraint(regular(dfa,new IntegerVariable[]{v2, v5, v6}));
        s.read(m);
        s.solve();
        if (s.isFeasible()) {
            do {
                LOGGER.info(v1 + " " + v2 + " " + v3);
                LOGGER.info(v2 + " " + v5 + " " + v6);

            } while (s.nextSolution() == Boolean.TRUE);
        }
        LOGGER.info("ExpectedSolutions 1 - nbSol " + s.getNbSolutions());
        assertEquals(1, s.getNbSolutions());
    }

    @Test
    public void testAutoExample0() {
        for (int seed = 0; seed < 10; seed++) {
            CPModel m = new CPModel();
            CPSolver s = new CPSolver();
            IntegerVariable v1 = makeIntVar("v1", 1, 2);
            IntegerVariable v2 = makeIntVar("v2", new int[]{0, 3});
            IntegerVariable v3 = makeIntVar("v3", new int[]{0, 3});

            //remove some forbidden tuples (here, the tuples define a not_all_equal constraint)
            List<int[]> tuples = new LinkedList<int[]>();
            tuples.add(new int[]{1, 3, 0});
            tuples.add(new int[]{2, 3, 3});

            // post the constraint
            m.addConstraint(regular(new IntegerVariable[]{v1, v2, v3}, tuples));

            s.read(m);
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.solveAll();

            LOGGER.info("ExpectedSolutions 2 - nbSol " + s.getNbSolutions());
            assertEquals(2, s.getNbSolutions());
        }
    }

    @Test
    public void testAutoExample01() {
        for (int seed = 0; seed < 10; seed++) {
            CPModel m = new CPModel();
            CPSolver s = new CPSolver();
            IntegerVariable[] bvars = makeIntVarArray("b", 10, 0, 1);
            IntegerVariable charge = makeIntVar("charge", 0, 4000);
            m.addVariable(Options.V_ENUM, charge);
            m.addVariables(bvars);

            //remove some forbidden tuples (here, the tuples define a not_all_equal constraint)
            List<int[]> tuples = new LinkedList<int[]>();
            tuples.add(new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
            tuples.add(new int[]{0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 2000});
            tuples.add(new int[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 4000});
            tuples.add(new int[]{0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2000});
            tuples.add(new int[]{0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 4000});
            tuples.add(new int[]{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2000});
            tuples.add(new int[]{0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 4000});
            tuples.add(new int[]{0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 4000});
            tuples.add(new int[]{0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 4000});
            tuples.add(new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4000});
            tuples.add(new int[]{0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 4000});
            tuples.add(new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4000});
            tuples.add(new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 4000});
            tuples.add(new int[]{0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 4000});

            IntegerVariable[] vars = new IntegerVariable[11];
            System.arraycopy(bvars, 0, vars, 0, bvars.length);
            vars[10] = charge;
            // post the constraint
            m.addConstraint(regular(vars, tuples));

            s.read(m);
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.solveAll();

            LOGGER.info("ExpectedSolutions 14 - nbSol " + s.getNbSolutions());
            assertEquals(14, s.getNbSolutions());
        }
    }


    @Test
    public void testAutoExample1() {
        IntegerVariable v1 = makeIntVar("v1", 1, 4);
        IntegerVariable v2 = makeIntVar("v2", 1, 4);
        IntegerVariable v3 = makeIntVar("v3", 1, 4);

        //remove some forbidden tuples (here, the tuples define a not_all_equal constraint)
        List<int[]> tuples = new LinkedList<int[]>();
        tuples.add(new int[]{1, 1, 1});
        tuples.add(new int[]{2, 2, 2});
        tuples.add(new int[]{3, 3, 3});
        tuples.add(new int[]{4, 4, 4});

        // post the constraint
        m.addConstraint(regular(new IntegerVariable[]{v1, v2, v3}, tuples, new int[]{1, 1, 1}, new int[]{4, 4, 4}));
        s.read(m);
        s.solveAll();

        LOGGER.info("ExpectedSolutions 60 - nbSol " + s.getNbSolutions());
        assertEquals(60, s.getNbSolutions());
    }

    @Test
    public void testAutoExample1Bis() {
        IntegerVariable v1 = makeIntVar("v1", 1, 4);
        IntegerVariable v2 = makeIntVar("v2", 1, 4);
        IntegerVariable v3 = makeIntVar("v3", 1, 4);

        //remove some forbidden tuples (here, the tuples define a not_all_equal constraint)
        List<int[]> tuples = new LinkedList<int[]>();
        tuples.add(new int[]{2, 2, 2});
        tuples.add(new int[]{3, 3, 3});
        tuples.add(new int[]{4, 4, 4});

        // post the constraint
        m.addConstraint(regular(new IntegerVariable[]{v1, v2, v3}, tuples, new int[]{1, 1, 1}, new int[]{4, 4, 4}));
        s.read(m);
        s.setValIntSelector(new RandomIntValSelector(12));
        s.setVarIntSelector(new RandomIntVarSelector(s, 14));

        s.solveAll();

        LOGGER.info("ExpectedSolutions 61 - nbSol " + s.getNbSolutions());
        assertEquals(61, s.getNbSolutions());
    }

    @Test
    public void testAutoExample2() {
        IntegerVariable v1 = makeIntVar("v1", 1, 4);
        IntegerVariable v2 = makeIntVar("v2", 1, 4);
        IntegerVariable v3 = makeIntVar("v3", 1, 4);

        //add some allowed tuples (here, the tuples define a all_equal constraint)
        List<int[]> tuples = new LinkedList<int[]>();
        tuples.add(new int[]{1, 1, 1});
        tuples.add(new int[]{2, 2, 2});
        tuples.add(new int[]{3, 3, 3});
        tuples.add(new int[]{4, 4, 4});

        // post the constraint
        m.addConstraint(regular(new IntegerVariable[]{v1, v2, v3}, tuples));
        s.read(m);
        s.setValIntSelector(new RandomIntValSelector(130));
        s.setVarIntSelector(new RandomIntVarSelector(s, 176));
        s.solveAll();
        LOGGER.info("ExpectedSolutions 4 - nbSol " + s.getNbSolutions());
        assertEquals(4, s.getNbSolutions());
    }

    @Test
    public void testAutoExample2Bis() {
        IntegerVariable v1 = makeIntVar("v1", 1, 4);
        IntegerVariable v2 = makeIntVar("v2", 1, 4);
        IntegerVariable v3 = makeIntVar("v3", 1, 4);

        //add some allowed tuples (here, the tuples define a all_equal constraint)
        List<int[]> tuples = new LinkedList<int[]>();
        tuples.add(new int[]{2, 2, 2});
        tuples.add(new int[]{3, 3, 3});
        tuples.add(new int[]{4, 4, 4});

        // post the constraint
        m.addConstraint(regular(new IntegerVariable[]{v1, v2, v3}, tuples));
        s.read(m);
        s.solveAll();
        LOGGER.info("ExpectedSolutions 3 - nbSol " + s.getNbSolutions());
        assertEquals(3, s.getNbSolutions());
    }


    @Test
    public void testAutoExample3() {
        int n = 8;
        IntegerVariable[] vars = new IntegerVariable[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = makeIntVar("v" + i, 0, 1);
        }
        //
        List<Transition> t = new LinkedList<Transition>();
        t.add(new Transition(0, 0, 1));
        t.add(new Transition(1, 1, 0));
        t.add(new Transition(0, 1, 2));
        t.add(new Transition(2, 0, 0));

        List<Integer> fs = new LinkedList<Integer>();
        fs.add(0);
        DFA auto = new DFA(t, fs, n);
        // post the constraint
        m.addConstraint(regular(vars, auto));
        s.read(m);
        s.setValIntSelector(new RandomIntValSelector(122));
        s.setVarIntSelector(new RandomIntVarSelector(s, 10));
        s.solveAll();
        LOGGER.info("ExpectedSolutions 16 - nbSol " + s.getNbSolutions());
        assertEquals(16, s.getNbSolutions());
    }

    @Test
    public void testAutoExample3Bis() {
        int n = 8;
        IntegerVariable[] vars = new IntegerVariable[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = makeIntVar("v" + i, 0, 2);
        }
        //
        List<Transition> t = new LinkedList<Transition>();
        t.add(new Transition(0, 0, 1));
        t.add(new Transition(1, 1, 0));
        t.add(new Transition(0, 1, 2));
        t.add(new Transition(2, 0, 2));
        t.add(new Transition(0, 2, 3));
        t.add(new Transition(1, 2, 3));
        t.add(new Transition(2, 2, 3));

        List<Integer> fs = new LinkedList<Integer>();
        fs.add(0);
        fs.add(3);
        DFA auto = new DFA(t, fs, n);
        // post the constraint
        m.addConstraint(regular(vars, auto));
        s.read(m);
        s.solveAll();
        LOGGER.info("ExpectedSolutions 16 - nbSol " + s.getNbSolutions());
        assertEquals(6, s.getNbSolutions());
    }

    @Test
    public void testAutoExample4Rostering() {
        int n = 6;
        IntegerVariable[] vars = new IntegerVariable[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = makeIntVar("v" + i, 0, 5);
        }
        //Impose exactement 3 "un" cons�cutifs (ou aucun) dans une s�quence de
        //6 variables qui prennent la valeur 3 sinon.
        List<Transition> t = new LinkedList<Transition>();
        t.add(new Transition(0, 1, 1));
        t.add(new Transition(1, 1, 2));
        t.add(new Transition(2, 1, 3));

        t.add(new Transition(3, 3, 0));
        t.add(new Transition(0, 3, 0));

        // 2 �tats finaux : 0, 3
        List<Integer> fs = new LinkedList<Integer>();
        fs.add(0);
        fs.add(3);

        DFA auto = new DFA(t, fs, n);
        // post the constraint
        m.addConstraint(regular(vars, auto));
        s.read(m);
        s.solveAll();
        LOGGER.info("ExpectedSolutions 5 - nbSol " + s.getNbSolutions());
        assertEquals(5, s.getNbSolutions());
    }

    @Test
    public void testStrechExemple() {
        ArrayList<int[]> lgt = new ArrayList<int[]>();
        lgt.add(new int[]{2, 2, 2});
        lgt.add(new int[]{0, 2, 2});
        lgt.add(new int[]{1, 2, 3});

        int n = 7;
        IntegerVariable[] vars = new IntegerVariable[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = makeIntVar("v" + i, 0, 2);
        }
        m.addConstraint(stretchPath(lgt, vars));
        s.read(m);
        s.solve();
        if (s.isFeasible()) {
            do {
                StringBuffer st = new StringBuffer();
                for (IntegerVariable var : vars) {
                    st.append(s.getVar(var).getVal());
                }
                LOGGER.info(st.toString());
            } while (s.nextSolution() == Boolean.TRUE);
        }
        assertEquals(12, s.getNbSolutions());
    }

    @Test
    public void testAutoExampleRegExp() {
        int n = 6;
        IntegerVariable[] vars = new IntegerVariable[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = makeIntVar("v" + i, 0, 5);
        }
        String regexp = "(1|2)(3*)(4|5)";
        // post the constraint
        m.addConstraint(regular(vars, regexp));
        s.read(m);
        s.solve();
        if (s.isFeasible()) {
            do {
                StringBuffer st = new StringBuffer();
                st.append("Solution: ");
                for (IntegerVariable var : vars) {
                    st.append(s.getVar(var).getVal());
                }
                LOGGER.info(st.toString());
            } while (s.nextSolution() == Boolean.TRUE);
        }
        assertEquals(4, s.getNbSolutions());
    }

    @Test
    public void testAutoExampleBigNumberRegExp() {
        int n = 6;
        IntegerVariable[] vars = new IntegerVariable[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = makeIntVar("v" + i, 0, 500);
        }
        String regexp = "(<100>|2)(<39>*)(<4>|5)";
        // post the constraint
        m.addConstraint(regular(vars, regexp));
        s.read(m);
        s.solve();
        if (s.isFeasible()) {
            do {
                StringBuffer st = new StringBuffer();
                st.append("Solution: ");
                for (IntegerVariable var : vars) {
                    st.append(StringUtils.pad(s.getVar(var).getVal() + "", 4, " "));
                }
                LOGGER.info(st.toString());
            } while (s.nextSolution() == Boolean.TRUE);
        }
        assertEquals(4, s.getNbSolutions());
    }




    @Test
    public void testNQueen5() {
        nQueen(5);
    }

    @Test
    public void testNQueen6() {
        nQueen(6);
    }

    @Test
    public void testNQueen7() {
        nQueen(7);
    }

    private static final int star = Integer.MAX_VALUE;


    private static final int[] NBSols = new int[]{0, 0, 0, 2, 10, 4, 40, 92, 352, 724, 2680, 14200, 73712};

    /**
     * Ajoute � l'automate tous les tuples de taille n contenant au moins
     * 2 valeurs identiques. => l'automate encode un alldifferent
     * @param tuples
     * @param n
     */

    public void genereAlldiffAutom(List<int[]> tuples, int n) {
        // tuples.add(new int[]{1, 1, 1, 1, 1});
        int[] tuple = new int[n];
        for (int i = 0; i < n; i++) {
            tuple[i] = star;
        }
        for (int value = 0; value < tuple.length; value++) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    tuple[i] = value;
                    tuple[j] = value;
                    int[] t = new int[n];
                    System.arraycopy(tuple, 0, t, 0, tuple.length);
                    tuples.add(t);
                    //afficheNogood(t);
                    tuple[i] = star;
                    tuple[j] = star;
                }
            }
        }
    }


    /**
     * Ajoute � l'automate tous les tuples associ�s
     * aux diagonales d'un �chiquer de taille n
     * @param tuples
     * @param n
     */
    public void genereLeftTuples(List<int[]> tuples, int n) {
        int[] tuple = new int[n];
        int nbNogood = 0;
        for (int i = 0; i < n; i++) {
            tuple[i] = star;
        }
        for (int l = 0; l < n; l++) {
            for (int i = 0; i < n; i++) {
                for (int j = l + 1; j < n; j++) {
                    if ((i + j - l) < n) {
                        tuple[l] = i;
                        tuple[j] = i + j - l;
                        int[] t = new int[n];
                        //afficheNogood(tuple);
                        System.arraycopy(tuple, 0, t, 0, tuple.length);
                        tuples.add(t);
                        nbNogood++;
                        tuple[l] = star;
                        tuple[j] = star;
                    }
                    if ((i + l - j) >= 0) {
                        tuple[l] = i;
                        tuple[j] = i + l - j;
                        int[] t = new int[n];
                        //afficheNogood(tuple);
                        System.arraycopy(tuple, 0, t, 0, tuple.length);
                        tuples.add(t);
                        nbNogood++;
                        tuple[l] = star;
                        tuple[j] = star;
                    }
                }
            }
        }
        //LOGGER.info("Nombre :  " + nbNogood);
    }

    public static void afficheNogood(int[] noGood) {
        StringBuffer st = new StringBuffer();
        for (int aNoGood : noGood) {
            st.append(MessageFormat.format(" {0}", aNoGood));
        }
        LOGGER.info(st.toString());
    }

    /**
     * Construit une contrainte contenant tous les tuples
     * interdits des n-reines et v�rifie le nombre de solutions
     * @param n
     */
    public void nQueen(int n) {
        IntegerVariable[] reines = new IntegerVariable[n];
        int[] min = new int[n];
        int[] max = new int[n];
        for (int i = 0; i < n; i++) {
            reines[i] = makeIntVar("reine-" + i, 0, n - 1);
            min[i] = 0;
            max[i] = n - 1;
        }
        long tps = System.currentTimeMillis();

        List<int[]> tuplesAllDiff = new LinkedList<int[]>();
        genereAlldiffAutom(tuplesAllDiff, n);
        genereLeftTuples(tuplesAllDiff, n);
        m.addConstraint(regular(reines, tuplesAllDiff, min, max));

        s.read(m);
        s.setValIntSelector(new RandomIntValSelector(120));
        s.setVarIntSelector(new RandomIntVarSelector(s, 112));
        s.solveAll();

        int nbsolution = s.getNbSolutions();
        int nbNode = s.getNodeCount();
        LOGGER.info("TestsAutomate test7(" + n + " reines) : " + nbsolution + " nodes " + nbNode + " tps " + (System.currentTimeMillis() - tps));
        assertEquals(NBSols[n - 1], nbsolution);

    }

    public Constraint makeKnapsack(CPSolver s, int[] coefs, IntegerVariable[] vars, IntegerVariable charge) {
        Constraint knap;
        int n = vars.length + 1;
        //nodes[i] : la liste des noeuds du graphe a la couche i
        ArrayList<Node>[] nodes = new ArrayList[n + 1];
        //transitions[i] : la liste des transitions de la couche i a i+1
        ArrayList<Transition>[] transitions = new ArrayList[n];

        nodes[0] = new ArrayList<Node>();
        nodes[0].add(new Node(0, 0, 0));
        int nodeIdx = 1;
        for (int i = 1; i < n; i++) {
            nodes[i] = new ArrayList<Node>();
            transitions[i - 1] = new ArrayList<Transition>();
            for (Object o : nodes[i - 1]) {
                Node pnode = (Node) o;
                int cidx = pnode.idx;
                int cb = pnode.b;
                if (s.getVar(vars[i - 1]).canBeInstantiatedTo(0)) {
                    Node existingNode = isNodeAlreadyAvailable(nodes[i], cb);
                    if (existingNode == null) {
                        existingNode = new Node(nodeIdx, cb, i);
                        nodeIdx++;
                        nodes[i].add(existingNode);
                    }
                    //tester si il existe un noeud de capacite cb a la couche i

                    transitions[i - 1].add(new Transition(cidx, 0, existingNode.idx));
                }
                if (s.getVar(vars[i - 1]).canBeInstantiatedTo(1)) {
                    int newCoef = cb + coefs[i - 1];
                    Node existingNode = isNodeAlreadyAvailable(nodes[i], newCoef);
                    if (existingNode == null) {
                        existingNode = new Node(nodeIdx, newCoef, i);
                        nodeIdx++;
                        nodes[i].add(existingNode);
                    }
                    transitions[i - 1].add(new Transition(cidx, 1, existingNode.idx));
                }
            }
        }
        transitions[n - 1] = new ArrayList<Transition>();
        for (Object o : nodes[n - 1]) {
            Node lnode = (Node) o;
            transitions[n - 1].add(new Transition(lnode.idx, lnode.b, nodeIdx));
        }
        List<Transition> t = new LinkedList<Transition>();
        for (ArrayList<Transition> transition : transitions) {
            for (Transition aTransition : transition) {
                t.add(aTransition);
            }
        }

        List<Integer> fs = new LinkedList<Integer>();
        fs.add(nodeIdx);

        DFA auto = new DFA(t, fs, n);
        // post the constraint
        IntegerVariable[] vs = new IntegerVariable[n];
        System.arraycopy(vars, 0, vs, 0, n - 1);
        vs[n - 1] = charge;
        knap = regular(vs, auto);
        return knap;
    }

    public Node isNodeAlreadyAvailable(ArrayList<Node> lnode, int c) {
        for (Node node : lnode) {
            if (node.b == c) {
                return node;
            }
        }
        return null;
    }

    public class Node {
        int idx;
        int b;
        int layer;

        public Node(int idx, int b, int layer) {
            this.idx = idx;
            this.b = b;
            this.layer = layer;
        }
    }

    @Test
    public void testKnapsack() {
        for (int seed = 1; seed < 2; seed++) {
            Model m = new CPModel();
            CPSolver s = new CPSolver();
            int n = 10;
            IntegerVariable[] bvars = makeIntVarArray("b", n, 0, 1);
            IntegerVariable charge = makeIntVar("charge", 0, 4000);
            m.addVariable(Options.V_ENUM, charge);
            m.addVariables(bvars);
            s.read(m);
            int[] coefs = new int[n];
            Random rand = new Random(100);
            int[] coef = new int[]{2000, 4000};
            for (int i = 0; i < coefs.length; i++) {
                coefs[i] = coef[rand.nextInt(2)];
            }
            //m.addConstraint(pb.eq(pb.scalar(,)));
            long tps = System.currentTimeMillis();
            //Constraint knapsack = Choco.eq(Choco.scalar(coefs,bvars),charge);
            Constraint knapsack = makeKnapsack(s, coefs, bvars, charge);
            LOGGER.info("tps construction " + (System.currentTimeMillis() - tps));
            m.addConstraint(knapsack);
            tps = System.currentTimeMillis();
            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.solve();
            do {
                StringBuffer st = new StringBuffer();
                st.append("tuples.add(new int[]{");
                for (IntegerVariable bvar : bvars) {
                    st.append(MessageFormat.format("{0},", s.getVar(bvar).getVal()));
                }
                LOGGER.info(MessageFormat.format("{0}}", st.toString() + s.getVar(charge).getVal()));
            } while (s.nextSolution() == Boolean.TRUE);
            LOGGER.info("tps resolution " + (System.currentTimeMillis() - tps));
            assertEquals(s.getNbSolutions(), 14);
            LOGGER.info("" + s.getNbSolutions());
        }
    }


    @Test
    public void testAnotherRegexp() {
        for (int k = 0; k < 10; k++) {
            LOGGER.info("*************************" + k);
            Model m = new CPModel();
            Solver s = new CPSolver();
            int longueur = 10;

            int n = 4;
            IntegerVariable[] vars = new IntegerVariable[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = makeIntVar("v" + i, 0, 9);
            }
            String regexp = "(1|2|3|4|5|6|7|8|9)(0|1|2|3|4|5|6|7|8|9)(0)(" + k + ")";
            // post the constraint
            m.addConstraint(regular(vars, regexp));
            s.read(m);
            s.solve();
            int tour = 0;
            if (s.isFeasible() == Boolean.TRUE) {

                do {
                    int port = 0;
                    LOGGER.info("------------Solution---------");
                    for (int i = 0; i < s.getNbIntVars(); i++) {
                        int valPort = s.getIntVar(i).getVal();
                        double mult = Math.pow(10, s.getNbIntVars()
                                - 1 - i);
                        port += valPort * mult;
                        LOGGER.info("au tour " + tour + " port = " + port + ", valPort = " + valPort + ", mult =" + mult);
                        LOGGER.info("" + s.getIntVar(i) + " = " + s.getIntVar(i).getVal());
                    }
                    tour++;
                } while (s.nextSolution() == Boolean.TRUE && tour < longueur);
            }
        }
    }


    int[] nbsol = new int[]{715, 8, 484, 10648, 127, 263, 2420, 605, 43560, 4, 3509, 385, 1639};

    @Test
    public void testAnotherKnapsack() {
        int time = (int) System.currentTimeMillis();
        for (int seed = 0; seed < nbsol.length; seed++) {
            CPModel m = new CPModel();
            CPSolver s = new CPSolver();
            int n = 10;
            IntegerVariable[] bvars = makeIntVarArray("b", n, 0, 10);
            int charge = 10;
            m.addVariables(Options.V_ENUM, bvars);
            s.read(m);
            int[] coefs = new int[n];
            Random rand = new Random(seed);
            for (int i = 0; i < coefs.length; i++) {
                coefs[i] = rand.nextInt(10);
            }

            Constraint knapsack = Choco.equation(charge, bvars, coefs);
            m.addConstraint(knapsack);
            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.solveAll();
            LOGGER.info("" + s.getNbSolutions() + "," + nbsol[seed]);
            assertEquals(nbsol[seed], s.getNbSolutions());
        }
        time = (int) System.currentTimeMillis() - time;
        LOGGER.info("time " + time);
    }

    int[] nbsol2 = new int[]{15, 0, 25, 100, 3, 5, 45, 0, 250, 0, 0, 0, 20};

    @Test
    public void testAnotherKnapsack2() {
        StringBuffer st = new StringBuffer();
        for (int seed = 0; seed < nbsol2.length; seed++) {
            CPModel m = new CPModel();
            CPSolver s = new CPSolver();
            int n = 10;
            IntegerVariable[] bvars = makeIntVarArray("b", n, new int[]{0, 3, 5, 6, 7});
            int charge = 10;
            m.addVariables(Options.V_ENUM, bvars);
            int[] coefs = new int[n];
            Random rand = new Random(seed);
            for (int i = 0; i < coefs.length; i++) {
                coefs[i] = rand.nextInt(10);
            }

            Constraint knapsack = Choco.equation(charge, bvars, coefs);
            m.addConstraint(knapsack);
            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.solveAll();
            st.append(MessageFormat.format("{0},", s.getNbSolutions()));
            assertEquals(nbsol2[seed], s.getNbSolutions());
        }
        LOGGER.info(st.toString());
    }


    @Test
    @Ignore
    //tis test currently throw a null pointer exception
    public void testAnotherAComplexeDFA() {
        int n = 3;
        IntegerVariable[] vars = Choco.makeIntVarArray("a",7,0,16);
        regular(vars, makeHadrienNSPAutomaton(n));
    }

    public static DFA makeHadrienNSPAutomaton(int nbs) {
        int D =0, E = 1,N = 2,R =3;
        ArrayList<Transition> trans = new ArrayList<Transition>();
        ArrayList<Integer> acc = new ArrayList<Integer>();
        for (int i = 0 ; i < 17 ; i++) {
            trans.add(new Transition(i,R,0));
            acc.add(i);
        }
        trans.add(new Transition(0,D,1));
        trans.add(new Transition(0,E,5));
        trans.add(new Transition(0,N,9));


        int k = 1;
        for (int i = 0 ;i < nbs; i++)
        {
            for (int j = k ; j < 3+k ;j++)
            {
                trans.add(new Transition(j,i,j+1));
            }
            k+=4;
        }

        trans.add(new Transition(1,E,13));
        trans.add(new Transition(2,E,14));
        trans.add(new Transition(3,E,15));


        trans.add(new Transition(13,E,7));
        trans.add(new Transition(14,E,8));

        trans.add(new Transition(5,N,10));
        trans.add(new Transition(6,E,11));
        trans.add(new Transition(7,E,12));



        trans.add(new Transition(4,D,16));
        trans.add(new Transition(4,E,16));
        trans.add(new Transition(8,E,16));
        trans.add(new Transition(8,N,16));
        trans.add(new Transition(12,N,16));
        trans.add(new Transition(15,E,16));

        return new DFA(trans,acc,4);
    }

    @Test
    public void testWithBound(){

        Model m = new CPModel();
        int n = 6;
        IntegerVariable[] vars = Choco.makeIntVarArray("v", n, 1, 5, Options.V_BOUND);


        String regexp = "(1|2)(3*)(2|4|5)";
        m.addConstraint(Choco.regular(vars, regexp));
        //m.addConstraint(Choco.eq(vars[0],3));

        Solver s = new CPSolver();
        s.read(m);
        s.solveAll(); //chechSolution enabled by assertions
//        s.solve();
//        if (s.isFeasible()) {
//            do {
//                Assert.assertTrue(s.checkSolution());
//            }while (s.nextSolution());
//        }
    }

    @Test
    /**
     * BUG ID: 2859126
     */
    public void stretchPath1() {
        Model model = new CPModel();
        Solver solver = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("int", 2, 1,2);

        List<int[]> tuples = new ArrayList<int[]>();
        tuples.add(new int[]{1, 1, 1});
        tuples.add(new int[]{2, 1, 1});

        model.addConstraint(stretchPath(tuples, vars));
        solver.read(model);
        solver.solveAll();
        Assert.assertEquals(2, solver.getNbSolutions());
    }

    @Test
    /**
     * BUG ID: 2859126
     */
    public void stretchPath2() {
        Model model = new CPModel();
        Solver solver = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("int", 2, 1,2);

        List<int[]> tuples = new ArrayList<int[]>();
        tuples.add(new int[]{1, 1, 1});

        model.addConstraint(stretchPath(tuples, vars));
        solver.read(model);
        solver.solveAll();

        Assert.assertEquals(3, solver.getNbSolutions());
    }

}
