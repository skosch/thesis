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

package samples.jobshop;

import choco.Choco;
import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.Solver;

import java.io.IOException;
import java.util.Random;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 20 oct. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*/
public class TaskPlanif {
    private static final int NBPERIODES = 900;

    private static final int NBTACHES = 3;

    IntegerVariable pipeau = makeIntVar("debutTache", 0, NBPERIODES, new String[0]);

    public class myIntegerVariable
            extends IntegerVariable {

        /**
         * @param name
         * @param type
         * @param binf
         * @param bsup
         */
        public myIntegerVariable(String name, int binf, int bsup) {
            super(name, binf, bsup);
            // TODO Auto-generated constructor stub
        }

        public Constraint contraintes() {
            System.out.println("contraintes neq ex�cut�e ...");
            // return Choco.neq( pipeau, pipeau );
            return TRUE;


        }

    }

    public static void main(String[] args) {
        try {
            System.out.println(new TaskPlanif().demo());
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String demo() throws IOException {
        Random r = new Random(10);


        // 1- Create the model
        Model m = new CPModel();
        //

          Solver s = new CPSolver();
//        Solver s = new PreProcessCPSolver();
//         2- declaration of variables
        // public static IntegerVariable[] makeIntVarArray(String name, int dim, int lowB, int uppB, String... options)
        // {

        int iTache, iPeriode;

        // La date de fin d'une t�che, si elle commenece � "t" allant de 0 � NBPERIODES
        // � une valeur definie dans FinMinTache
        // IntegerConstantVariable[][] FinMinTache = new IntegerConstantVariable[NBTACHES][NBPERIODES];
        int[][] FinMinTache = new int[NBTACHES][NBPERIODES];
        System.out.println("");
        for (iTache = 0; iTache < NBTACHES; iTache++) {
            System.out.println("Valeur de duree pour la tache de num�ro " + iTache);
            for (iPeriode = 0; iPeriode < NBPERIODES; iPeriode++) {
                // FinMinTache[iTache][iPeriode] = new IntegerConstantVariable( ( ( iTache + 1 ) * iPeriode ) % 10 );
                FinMinTache[iTache][iPeriode] = (int) (iPeriode + r.nextDouble()* 10 + 1);

                // FinMinTache[iTache][iPeriode] = ( ( ( iTache + 1 ) * iPeriode ) % 10 ) + 1;
                System.out.print(FinMinTache[iTache][iPeriode] + ",");

            }
            System.out.println("");

        }

        IntegerVariable[] debutTache = makeIntVarArray("debutTache", NBTACHES, 0, NBPERIODES);
        IntegerVariable[] dureeTache = makeIntVarArray("dureeTache", NBTACHES, 0, NBPERIODES);
        IntegerVariable[] finTache = makeIntVarArray("finTache", NBTACHES, 0, NBPERIODES);

        TaskVariable[] tasks = makeTaskVarArray("t", debutTache, finTache, dureeTache);

        IntegerVariable[] FinMinTachePeriodeCourante =
                makeIntVarArray("FinMinTachePeriodeCourante", NBTACHES, 0, NBPERIODES);
        m.addVariables(Options.V_NO_DECISION, FinMinTachePeriodeCourante);

//        IntegerVariable[] DureeEffectiveTachePeriodeCourante =
//                Choco.makeIntVarArray("DureeEffectiveTachePeriodeCourante", NBTACHES, 0, NBPERIODES);
//        m.addVariables(CPOptions.V_NO_DECISION, DureeEffectiveTachePeriodeCourante);

        for (iTache = 0; iTache < NBTACHES; iTache++) {

            m.addConstraint(nth(debutTache[iTache], FinMinTache[iTache], FinMinTachePeriodeCourante[iTache]));

            m.addConstraint(Choco.geq(finTache[iTache], FinMinTachePeriodeCourante[iTache]));
//            m.addConstraint(Choco.eq(DureeEffectiveTachePeriodeCourante[iTache], Choco.minus(finTache[iTache],
//                    debutTache[iTache])));
        }

        // dans ce tableau, on s'indexe donc, pour une tache donn�, avec la dur�e estim�e, et la valeur
        // est la date de d�but pr�vue

        // = Choco.makeIntVarArray( "debutTache", NBTACHES, 0, NBPERIODES, new String[0] );


        for (iTache = 0; iTache < NBTACHES; iTache++) {

            if (iTache != (NBTACHES - 1))
//                m.addConstraint(gt(debutTache[iTache], finTache[iTache + 1]));
            m.addConstraint(startsAfterEnd(tasks[iTache], tasks[iTache+1]));

            for (iPeriode = 0; iPeriode < NBPERIODES; iPeriode++) {
                // Avec la contrainte suivante comment�e, plantage "IndexoutOfBounds 140" (14� =
                // 2*NBPERIODES) ?????????????????

                // m.addConstraint( Choco.geq( finTache[iTache], Choco.sum( new IntegerVariable[] {
                // Choco.constant( iPeriode ), debutTacheIndexParCharge[iTache][iPeriode] } ) ) );

            }
        }

        IntegerVariable objectiveMission = makeIntVar("objectiveMission", 0, NBPERIODES, Options.V_BOUND/*,
                CPOptions.V_NO_DECISION*/); // OK;

        IntegerExpressionVariable objectiveMissionExpression;

        IntegerVariable[] vars = ArrayUtils.append(debutTache, finTache);
        int[] coeffs = new int[]{-1,-1,-1,1,1,1};

        int n = 1;
        switch (n) {

            case 1:
                // O1 minimiser la dur�e totale du projet:
                objectiveMissionExpression = minus(max(finTache), min(debutTache));
                break;
            case 2:
                // O2 : minimiser la date de fin de mission:
                objectiveMissionExpression = max(finTache);
                break;
            case 3:
                // O3 : minimiser la somme des charges effectu�es :
//                objectiveMissionExpression = Choco.sum(DureeEffectiveTachePeriodeCourante);
//                objectiveMissionExpression = minus(sum(finTache), sum(debutTache));
                objectiveMissionExpression = scalar(coeffs, vars);
                break;
            default:
                objectiveMissionExpression = min(debutTache);
                break;
        }

        // to force decomposition on that constraint
        m.addConstraint(/*"CPOptions.E_DECOMP", */eq(objectiveMissionExpression, objectiveMission));

        m.addVariables(Options.V_OBJECTIVE, objectiveMission);

       // 5- read the model and solve it
        s.read(m);

        System.out.println("Lecture MODEL  CHOCO .. ");

        System.out.println("Lancement Solver CHOCO ............ ");
        s.setTimeLimit(120 * 80000); // 2*80 secondes
        s.monitorTimeLimit(true);
        s.minimize(false);


        if (s.isFeasible()) {
            System.out.println("Nb_sol  : " + s.getNbSolutions() + "timecount" + s.getTimeCount());
            // Commentaire : il
            // s'agit ici du nombre total de solution trouv�es. cependant, comem on a fix� un objectif
            // (maximize ou minimize), il n'een reste plus qu'une seule dans le parcours des
            // solution qui suit. Quand on ne fixe pas d'objectif, le parcour suivant donne autant de
            // solutions que getNbSolutions
            // Le flushLogs, avec vebosity "SOLUTION" permet d'anamyser toutes les solutions rencontr�es
            //
            do {

                System.out.println("Nb_sol  : " + s.getNbSolutions());


                for (iTache = 0; iTache < NBTACHES; iTache++) {
                    System.out.println("debutTache [" + iTache + "] = " + s.getVar(debutTache[iTache]).getVal());
                    System.out.println("finTache [" + iTache + "] = " + s.getVar(finTache[iTache]).getVal());

                    // System.out.println( "debutTache1=" + s.getVar( debutTache1 ).getVal() );


                }
                System.out.println("objectiveMission = " + +s.getVar(objectiveMission).getVal());

            }

            while (s.nextSolution());
        } else {
            System.out.println("Pas de solution !!!!!! ");
            // System.out.println( "FinMinTache = " + +s.getVar( FinMinTache[0][5] ).getVal() );
        }
        // 6- Print the number of solutions found
        System.out.println("Nb_sol 2 : " + s.getNbSolutions());


        return "end";
    }


}