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


package samples.tutorials.scheduling;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import org.kohsuke.args4j.Option;
import samples.tutorials.PatternExample;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * n landing must land on a landing strip.
 * Each plane has an arrival time, a landing duration time and a number of passengers.
 * We want to prioritize landing according to the number of passengers.
 * the objective is to minimize the weighted sum of tardiness.
 * <p/>
 * http://people.brunel.ac.uk/~mastjjb/jeb/orlib/airlandinfo.html
 *
 * @author Arnaud Malapert
 */
public class AircraftLanding extends PatternExample {

    @Option(name = "-f", usage = "File name.", required = false)
    String filename;

    int n = 10;

    @Option(name = "-tl", usage = "Time limit", required = false)
    int tl = 30000;

    //DATA
    private int[][] data = {
            {54, 129, 155, 559, 10, 10, 99999, 3, 15, 15, 15, 15, 15, 15, 15, 15},
            {120, 195, 258, 744, 10, 10, 3, 99999, 15, 15, 15, 15, 15, 15, 15, 15},
            {14, 89, 98, 510, 30, 30, 15, 15, 99999, 8, 8, 8, 8, 8, 8, 8},
            {21, 96, 106, 521, 30, 30, 15, 15, 8, 99999, 8, 8, 8, 8, 8, 8},
            {35, 110, 123, 555, 30, 30, 15, 15, 8, 8, 99999, 8, 8, 8, 8, 8},
            {45, 120, 135, 576, 30, 30, 15, 15, 8, 8, 8, 99999, 8, 8, 8, 8},
            {49, 124, 138, 577, 30, 30, 15, 15, 8, 8, 8, 8, 99999, 8, 8, 8},
            {51, 126, 140, 573, 30, 30, 15, 15, 8, 8, 8, 8, 8, 99999, 8, 8},
            {60, 135, 150, 591, 30, 30, 15, 15, 8, 8, 8, 8, 8, 8, 99999, 8},
            {85, 160, 180, 657, 30, 30, 15, 15, 8, 8, 8, 8, 8, 8, 8, 99999},
    };
    //    private static final int AT = 0;
    private static final int ELT = 1;
    private static final int TT = 2;
    private static final int LLT = 3;
    private static final int PCBT = 4;
    private static final int PCAT = 5;
    private static final int ST = 6;

    IntegerVariable[] landing, tardiness, earliness;
    IntegerVariable[] bVars;
    int[] costLAT;
    int[] LLTs;

    IntegerVariable objective;

    @Override
    public void printDescription() {
        if (filename != null) {
            parse(filename);
        }

        LOGGER.info("Given a set of planes and runways, the objective is to minimize the total (weighted) ");
        LOGGER.info("deviation from the target landing time for each plane. ");
        LOGGER.info("There are costs associated with landing either earlier or later than a target landing time for each plane. ");
        LOGGER.info("Each plane has to land on one of the runways within its predetermined time windows ");
        LOGGER.info("such that separation criteria between all pairs of planes are satisfied.");
        LOGGER.info("(http://people.brunel.ac.uk/~mastjjb/jeb/orlib/airlandinfo.html)");
    }

    @Override
    public void buildModel() {
        model = new CPModel();
        model.setDefaultExpressionDecomposition(true);
        landing = new IntegerVariable[n];
        tardiness = new IntegerVariable[n];
        earliness = new IntegerVariable[n];
        LLTs = new int[n];
        int obj_ub = 0;
        for (int i = 0; i < n; i++) {
            landing[i] = Choco.makeIntVar("p_" + i, data[i][ELT], data[i][LLT], Options.V_BOUND);

            earliness[i] = Choco.makeIntVar("a_" + i, 0, data[i][TT] - data[i][ELT], Options.V_BOUND);
            tardiness[i] = Choco.makeIntVar("t_" + i, 0, data[i][LLT] - data[i][TT], Options.V_BOUND);

            obj_ub += Math.max(
                    (data[i][TT] - data[i][ELT]) * data[i][PCBT],
                    (data[i][LLT] - data[i][TT]) * data[i][PCAT]
            );

            model.addConstraint(Choco.eq(earliness[i], Choco.max(0, Choco.minus(data[i][TT], landing[i]))));

            model.addConstraint(Choco.eq(tardiness[i], Choco.max(0, Choco.minus(landing[i], data[i][TT]))));


            LLTs[i] = data[i][LLT];
        }
        List<IntegerVariable> booleans = new ArrayList<IntegerVariable>();
        //disjunctive
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                IntegerVariable boolVar = Choco.makeBooleanVar("b_" + i + "_" + j);
                booleans.add(boolVar);

                Constraint c1 = precedence(landing[i], data[i][ST + j], landing[j]);
                Constraint c2 = precedence(landing[j], data[j][ST + i], landing[i]);
                Constraint cr = Choco.reifiedConstraint(boolVar, c1, c2);
                model.addConstraint(cr);
            }
        }

        bVars = booleans.toArray(new IntegerVariable[booleans.size()]);

        objective = Choco.makeIntVar("obj", Options.V_BOUND, Options.V_OBJECTIVE);

        // build cost array
        costLAT = new int[2 * n];
        for (int i = 0; i < n; i++) {
            costLAT[i] = data[i][PCBT];
            costLAT[n + i] = data[i][PCAT];
        }

        IntegerVariable obj_e = Choco.makeIntVar("obj_e", 0, obj_ub, Options.V_BOUND);
        model.addConstraint(Choco.eq(Choco.scalar(earliness, Arrays.copyOfRange(costLAT, 0, n)), obj_e));

        IntegerVariable obj_t = Choco.makeIntVar("obj_e", 0, obj_ub, Options.V_BOUND);
        model.addConstraint(Choco.eq(Choco.scalar(tardiness, Arrays.copyOfRange(costLAT, n, 2 * n)), obj_t));
        model.addConstraint(Choco.eq(Choco.scalar(new IntegerVariable[]{obj_e, obj_t, objective}, new int[]{1, 1, -1}), 0));

        model.addConstraint(Choco.allDifferent(landing));
    }

    static Constraint precedence(IntegerVariable x, int duration, IntegerVariable y) {
        return Choco.leq(Choco.scalar(new IntegerVariable[]{x, y}, new int[]{1, -1}), -duration);
    }


    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
        solver.addGoal(new AssignOrForbidIntVarVal(new MinDomain(solver, solver.getVar(landing)), new MinVal()));
        solver.addGoal(new AssignOrForbidIntVarVal(new MinDomain(solver), new MinVal()));
        solver.setTimeLimit(tl);

        //solver.clearGoals();
    }


    @Override
    public void solve() {
        solver.minimize(false);
    }

    @Override
    public void prettyOut() {
        LOGGER.info("\nSolution");
        for (int i = 0; i < n; i++) {
            int diff = data[i][TT] - solver.getVar(landing[i]).getVal();
            LOGGER.info("landing time for plane " + i + " :" + data[i][TT] + " " + (diff == 0 ? "" : (diff > 0 ? "-" : "+") + Math.abs(diff)));
        }
        LOGGER.info("Penalty :" + solver.getVar(objective).getVal());
    }


    public static void main(String[] args) {
        new AircraftLanding().execute(args);
    }


    ////////////////////////////////////////////

    private String groupSeparator = "\\,";
    private String decimalSeparator = "\\.";
    private String non0Digit = "[\\p{javaDigit}&&[^0]]";

    private Pattern decimalPattern;

    public void parse(String fileName) {
        try {
            buildFloatAndDecimalPattern();
            Scanner sc = new Scanner(new File(fileName));
            int nb = sc.nextInt();
            data = new int[nb][6 + nb];
            sc.nextLine();
            for (int i = 0; i < nb; i++) {
                data[i][0] = sc.nextInt(); // appearance time
                data[i][1] = sc.nextInt(); // earliest landing time
                data[i][2] = sc.nextInt(); // target landing time
                data[i][3] = sc.nextInt(); // latest landing time
                Double tt = Double.parseDouble(sc.next(decimalPattern));
                data[i][4] = (int) Math.ceil(tt); // penalty cost per unit of time for landing before target
                tt = Double.parseDouble(sc.next(decimalPattern));
                data[i][5] = (int) Math.ceil(tt); // penalty cost per unit of time for landing after target
                for (int j = 0; j < nb; j++) {
                    data[i][6 + j] = sc.nextInt();
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void buildFloatAndDecimalPattern() {
        // \\p{javaDigit} may not be perfect, see above
        String digit = "([0-9])";
        String groupedNumeral = "(" + non0Digit + digit + "?" + digit + "?(" +
                groupSeparator + digit + digit + digit + ")+)";
        // Once again digit++ is used for performance, as above
        String numeral = "((" + digit + "++)|" + groupedNumeral + ")";
        String decimalNumeral = "(" + numeral + "|" + numeral +
                decimalSeparator + digit + "*+|" + decimalSeparator +
                digit + "++)";
        String decimal = "([-+]?" + decimalNumeral + ")";
        decimalPattern = Pattern.compile(decimal);
    }
}

