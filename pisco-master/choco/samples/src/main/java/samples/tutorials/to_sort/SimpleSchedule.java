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

package samples.tutorials.to_sort;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.variables.integer.IntDomainVar;

import static choco.Choco.*;

/**
 * This class present a simple scheduling problem defined in multicost-regular documentation.
 * It consists on finding a minimal cost schedule for a person with some work regulations
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Jul 6, 2009
 * Time: 7:43:05 AM
 */
public class SimpleSchedule extends CPModel
{
    private static int DAY = 0;
    private static int NIGHT = 1;
    private static int REST = 2;

    private static String[] map = new String[]{"Day","Night","Rest"};

    /**
     * Model variable for the
     */
    IntegerVariable[] sequence;

    /**
     * Bounds within whoms the accepted schedule must cost.
     */
    IntegerVariable[] bounds;

    /**
     * The cost matrix which gives assgnement cost (used for counters too)
     */
    int[][][][] costMatrix;

    /**
     * Automaton which embeds the work regulations that may be
     * represented by regular expressions
     */
    FiniteAutomaton auto;


    /**
     * Simple Constructor for the simple schedule model
     */
    public SimpleSchedule()
    {
        super();
        this.makeModel();
    }


    /**
     * Construct the variables needed for the 14 day schedule
     */
    private void makeVariables()
    {
        this.sequence = makeIntVarArray("x",14,0,2, Options.V_ENUM);
        this.bounds =  new IntegerVariable[4];
        this.bounds[0] = makeIntVar("z_0",30,80, Options.V_BOUND);
        this.bounds[1] = makeIntVar("day",0,7, Options.V_BOUND);
        this.bounds[2] = makeIntVar("night",3,7, Options.V_BOUND);
        this.bounds[3] = makeIntVar("rest",7,9, Options.V_BOUND);

    }

    /**
     * make Cost Matrix that embeds financial cost and counters
     */
    private void makeCostMatrix()
    {
        int[][][][] csts = new int[14][3][4][this.auto.getNbStates()];
        for (int i = 0 ; i < csts.length ; i++)
        {
            for (int j = 0 ;j < csts[i].length ; j++)
            {
                for (int r = 0 ; r < csts[i][j].length ; r++)
                {
                    if (r == 0)
                    {
                        if (j == DAY)
                            csts[i][j][r] = new int[]{3,5,0};
                        else if (j == NIGHT)
                            csts[i][j][r] = new int[]{8,9,0};
                        else if (j == REST)
                            csts[i][j][r] = new int[]{0,0,2};
                    }
                    else if (r == 1)
                    {
                        if (j == DAY)
                            csts[i][j][r] = new int[]{1,1,0};
                    }
                    else if (r == 2)
                    {
                        if (j == NIGHT)
                            csts[i][j][r] = new int[]{1,1,0};
                    }
                    else if (r == 3)
                    {
                        if (j != REST)
                            csts[i][j][r] = new int[]{1,1,0};
                    }
                }
            }
        }
        this.costMatrix = csts;

    }


    /**
     * Construct the automaton that allows patterns describe in multicost-regular doc.
     */
    private void makeAutomaton()
    {
        this.auto = new FiniteAutomaton();
        int idx = this.auto.addState();
        this.auto.setInitialState(idx);
        this.auto.setFinal(idx);
        idx = this.auto.addState();
        this.auto.addTransition(this.auto.getInitialState(),idx,DAY);
        int next = this.auto.addState();
        this.auto.addTransition(idx,next, DAY,NIGHT);
        this.auto.addTransition(next,auto.getInitialState(),REST);
        auto.addTransition(auto.getInitialState(),next,NIGHT);

    }


    /**
     * build the model
     */
    public void makeModel()
    {
        this.makeVariables();
        this.makeAutomaton();
        this.makeCostMatrix();


        this.addVariables(sequence);
        this.addVariables(bounds);
        this.addConstraint(multiCostRegular(bounds, sequence, auto,costMatrix));
    }



    /**
     * Print a schedule once the model is solved by the given solver
     * @param s the CPSolver that solved this model
     */
    public void printSolution(CPSolver s)
    {
        StringBuffer b = new StringBuffer("[");
        for (IntegerVariable v : sequence)
            b.append(map[s.getVar(v).getVal()]).append("-");
        b.deleteCharAt(b.length()-1);
        b.append("]");
        System.out.println("Schedule: "+b);
        System.out.println("Cost: "+s.getVar(bounds[0]).getVal());
        System.out.println("Nb Days: "+s.getVar(bounds[1]).getVal());
        System.out.println("Nb Nights: "+s.getVar(bounds[2]).getVal());
        System.out.println("Nb Rests: "+(14-s.getVar(bounds[3]).getVal()));


    }

    public IntegerVariable getCostVariable()
    {
        return bounds[0];
    }



    public static void main(String[] args)
    {


        SimpleSchedule m = new SimpleSchedule();
        CPSolver s = new CPSolver();

        s.read(m);
        IntDomainVar z = s.getVar(m.getCostVariable());



        if (s.solve())
        {
           do {
               m.printSolution(s);
               System.out.println("");
               s.postCut(s.gt(z,z.getVal()));
           }
           while(s.nextSolution());
        }

        s.printRuntimeStatistics();
        System.out.println(s.getNbSolutions()+"[+0] solutions");
    }
}