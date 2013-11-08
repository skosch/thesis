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

package samples.comparaison;

import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.variables.integer.IntDomainVar;
import dk.brics.automaton.RegExp;
import gnu.trove.TIntHashSet;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Nov 23, 2009
 * Time: 5:54:38 PM
 */
public class RegCRMCRComp
{

    public final static int REG = 0;
    public final static int COR = 1;
    public final static int MCR = 2;


    IAutomaton automaton;
    IntegerVariable[] vars;
    IntegerVariable objectif;
    int[][] costs;
    Random rand;

    public RegCRMCRComp(int size, long seed)
    {

        if (seed >=0)
            this.rand = new Random(seed);
        else
            this.rand = new Random();
        this.automaton = makeAutomaton();
        this.costs = makeMatrix(size);

        this.vars = makeIntVarArray("x",size,0,2);



    }

    int[][] makeMatrix(int size)
    {
        int[][] ret = new int[size][3];
        for (int i  = 0 ; i < ret.length ; i++)
            for (int j  = 0 ; j < ret[i].length ; j++)
                ret[i][j] = rand.nextInt(1000);

        return ret;

    }


    IAutomaton makeAutomaton()
    {
        dk.brics.automaton.Automaton auto;
        // On commence toujours par du travail :)
        auto = new RegExp(StringUtils.toCharExp("0(0|1|2)*")).toAutomaton().complement();

        // un repos après deux jours de boulot;
        auto = auto.intersection(new RegExp(StringUtils.toCharExp("(0|1|2)*(1|2)(1|2)(1|2)(0|1|2)*")).toAutomaton().complement());
        auto.minimize();

        // Deux repos d'affiler.
        auto = auto.intersection(new RegExp(StringUtils.toCharExp("((1|2)*00+(1|2)*)*")).toAutomaton());
        auto.minimize();

        //deux jour d'affiler au moins
        auto = auto.intersection(new RegExp(StringUtils.toCharExp("(0*(1|2)(1|2)+0*)*")).toAutomaton());
        auto.minimize();


        TIntHashSet alpha = new TIntHashSet(new int[]{0,1,2});

        FiniteAutomaton ret = new FiniteAutomaton();
        ret.fill(auto,alpha);
        System.out.println(ret);

        return ret;

    }

    public void launch(int type)
    {
        switch(type)
        {
            case REG : launchRegular(); break;
            case COR : launchCRegular(); break;
            case MCR : launchMCRegular(); break;
        }
    }

    private void launchRegular()
    {
        objectif = makeIntVar("obj",0,1000*vars.length, Options.V_BOUND);
        CPModel m = new CPModel();
        m.addVariable(objectif);

        m.addConstraint(regular(vars, automaton));

        IntegerVariable[] cvar  = makeIntVarArray("c",vars.length,0,1000, Options.V_BOUND);
        IntegerVariable[] gccvar = makeIntVarArray("gcc",3,0,vars.length);
        IntegerVariable nbTravail = makeIntVar("trav",6,8, Options.V_BOUND);
        IntegerVariable nbRepos = makeIntVar("rep",costs.length-8,costs.length-6, Options.V_BOUND);

        for (int i  = 0 ; i < vars.length ; i++)
        {
            m.addConstraint(nth(vars[i],costs[i],cvar[i]));
        }

        m.addConstraint(globalCardinality(vars,gccvar, 0));
        m.addConstraint(eq(nbTravail,(plus(gccvar[1],gccvar[2]))));
        m.addConstraint(eq(nbRepos,(gccvar[0])));

        m.addConstraint(eq(objectif,sum(cvar)));



        CPSolver s = new CPSolver();


        s.read(m);
        s.setVarIntSelector(new StaticVarOrder(s, s.getVar(vars)));

        s.minimize(s.getVar(objectif),false);
        //s.solve();
        printSolution(s);






    }

    private void launchCRegular()
    {
        objectif = makeIntVar("obj",0,1000*vars.length, Options.V_BOUND);
        CPModel m = new CPModel();
        m.addVariable(objectif);
        m.addConstraint(costRegular(objectif, vars, automaton,costs));

        IntegerVariable[] gccvar = makeIntVarArray("gcc",3,0,vars.length);
        IntegerVariable nbTravail = makeIntVar("trav",6,8, Options.V_BOUND);
        IntegerVariable nbRepos = makeIntVar("rep",costs.length-8,costs.length-6, Options.V_BOUND);
        m.addConstraint(globalCardinality(vars,gccvar, 0));
        m.addConstraint(eq(nbTravail,(plus(gccvar[1],gccvar[2]))));
        m.addConstraint(eq(nbRepos,(gccvar[0])));


        CPSolver s = new CPSolver();


        s.read(m);
        s.setVarIntSelector(new StaticVarOrder(s, s.getVar(vars)));

        s.minimize(s.getVar(objectif),false);
        printSolution(s);


    }

    private void launchMCRegular()
    {
        objectif = makeIntVar("obj",0,1000*vars.length, Options.V_BOUND);
        CPModel m = new CPModel();
        m.addVariable(objectif);

        IntegerVariable nbTravail = makeIntVar("trav",6,8, Options.V_BOUND);
        IntegerVariable nbRepos = makeIntVar("rep",costs.length-8,costs.length-6, Options.V_BOUND);


        int[][][] csts = new int[costs.length][costs[0].length][3];
        for (int i = 0 ; i < csts.length ; i++)
        {
            for (int j = 0 ; j< csts[i].length ; j++)
            {
                csts[i][j][0] = costs[i][j];
                csts[i][j][1] = (j==1||j==2)?1:0;
                csts[i][j][2] = (j==1||j==2)?0:1;

            }
        }
        IntegerVariable[] cvars = new IntegerVariable[]{objectif,nbTravail,nbRepos};
        m.addConstraint(multiCostRegular(cvars, vars, automaton,csts));


        CPSolver s = new CPSolver();


        s.read(m);
        s.setVarIntSelector(new StaticVarOrder(s, s.getVar(vars)));

        s.minimize(s.getVar(objectif),false);
        printSolution(s);



    }


    private void printSolution(CPSolver s)
    {
        System.out.println("REALISABLE ? "+s.isFeasible());
        for (IntDomainVar v : s.getVar(vars))
        {
            System.out.print(v.getVal()+ " ");
        }
        System.out.println("");
        System.out.println("COST : "+s.getVar(objectif).getVal());
        s.printRuntimeStatistics();
    }


    public static void main(String[] args) {

        RegCRMCRComp pb = new RegCRMCRComp(40,1);
        pb.launch(RegCRMCRComp.REG);
        pb.launch(RegCRMCRComp.COR);
        pb.launch(RegCRMCRComp.MCR);



    }



}