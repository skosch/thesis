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

package choco.cp.solver.constraints.global.automata.fast_multicostregular.example;
import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.variables.integer.IntDomainVar;
import dk.brics.automaton.RegExp;
import gnu.trove.TIntHashSet;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Nov 23, 2009
 * Time: 9:46:44 AM
 */
public class RuleModel extends CPModel {

    dk.brics.automaton.Automaton full;

    TIntHashSet alpha;
    String work;
    String all;

    IntegerVariable[][] vs;
    IntegerVariable[][] cvs;


    public RuleModel()
    {



        int[] tmp = {0,1,2};
        alpha = new TIntHashSet();
        alpha.addAll(tmp);
        work = "(";
        for (int i = 0 ; i < tmp.length-1 ;i++)
            work+=tmp[i]+"|";
        work = work.substring(0,work.length()-1)+")";
        all = "("+tmp[2]+"|"+work.substring(1,work.length());


    }


    public void buildConsecutiveWERule()
    {
        String frule ="((";
        for (int j = 0 ; j < 3 ; j++)
        {
            for (int i = 0 ; i < 5 ; i++)
            {
                frule+=all;
            }
            frule+=work;
            frule+=work;
        }
        for (int j = 0 ; j < 7 ; j++)
        {
            frule+=all;
        }

        frule+=")|(";

        for (int j = 0 ; j < 7 ; j++)
        {
            frule+=all;
        }

        for (int j = 0 ; j < 3 ; j++)
        {
            for (int i = 0 ; i < 5 ; i++)
            {
                frule+=all;
            }
            frule+=work;
            frule+=work;
        }

        frule+="))";

        full = new RegExp(StringUtils.toCharExp(frule)).toAutomaton().complement();


    }

    public void buildNoNightBeforeFreeWE()
    {
        String ret = "((";
        for (int i = 0 ; i < 4 ; i++)
        {
            ret+=all;
        }
        ret+="122";

        for (int j = 0 ; j < 3 ; j++)
            for (int i = 0; i < 7 ; i++)
                ret+=all;
        ret+=")|(";

        for (int i = 0; i < 7 ; i++)
            ret+=all;


        for (int i = 0 ; i < 4 ; i++)
        {
            ret+=all;
        }
        ret+="122";

        for (int j = 0 ; j < 2 ; j++)
            for (int i = 0; i < 7 ; i++)
                ret+=all;
        ret+=")|(";


        for (int j = 0 ; j < 2 ; j++)
            for (int i = 0; i < 7 ; i++)
                ret+=all;

        for (int i = 0 ; i < 4 ; i++)
        {
            ret+=all;
        }
        ret+="122";

        for (int i = 0; i < 7 ; i++)
            ret+=all;

        ret+=")|(";

        for (int j = 0 ; j < 3 ; j++)
            for (int i = 0; i < 7 ; i++)
                ret+=all;

        for (int i = 0 ; i < 4 ; i++)
        {
            ret+=all;
        }
        ret+="122";
        ret+="))";

        full = full.intersection(new RegExp(StringUtils.toCharExp(ret)).toAutomaton().complement());
        full.minimize();





    }

    public void buildNoMoreThanDayRule()
    {
        String ret = all+"*";
        for (int i = 0 ;i < 6 ; i++)
            ret+=work;
        ret+=all+"*";

        full = full.intersection(new RegExp(StringUtils.toCharExp(ret)).toAutomaton().complement());
        full.minimize();


    }

    public void buildRestAfterNight()
    {
        String ret = all+"*";
        ret+="1+0";
        ret+=all+"*";

        full = full.intersection(new RegExp(StringUtils.toCharExp(ret)).toAutomaton().complement());
        full.minimize();


    }

    public void buildCompleteWE()
    {
        StringBuffer b = new StringBuffer("(");
        String patter = "((2(0|1))|((0|1)2))";
        int nd = 2;
        int nw = 4;
        int sd = 6;

        for (int w = 0 ; w < nw ; w++)
        {
            b.append("(");
            for (int i = 1; i < sd+(7*w) ;i++)
                b.append(all);
            b.append(patter);
            for (int i = (sd+7*w)+nd ; i <=28 ; i++)
                b.append(all);
            b.append(")|");
        }
        b.deleteCharAt(b.length()-1).append(")");


        full = full.intersection(new RegExp(StringUtils.toCharExp(b.toString())).toAutomaton().complement());
        full.minimize();


    }


    void fillModel()
    {

        vs = makeIntVarArray("x",8,28,0,2);

        int[][][] csts = new int[vs[0].length][3][7];


        for (int i = 0 ; i < csts.length ; i++)
        {
            for (int j = 0 ; j < csts[i].length ; j++)
            {
                if (j == 0 || j == 1)
                    csts[i][j][4] = 1;
                if (j==1)
                    csts[i][j][5] = 1;
                if (j==2)
                    csts[i][j][6] = 1;

                if (j == 0 || j == 1)
                {
                    csts[i][j][i/7] = 1;
                }


            }
        }

        cvs = new IntegerVariable[8][7];
        for (int i  = 0 ; i < 4 ;i++)
        {
            IntegerVariable[] tmp = cvs[i];
            tmp[4] = makeIntVar("z_{"+i+",0}",0,18, Options.V_BOUND);
            tmp[5] = makeIntVar("z_{"+i+",1}",0,4, Options.V_BOUND);
            tmp[6] = makeIntVar("z_{"+i+",2}",10,28, Options.V_BOUND);
            for (int j = 0 ; j < 4 ; j++)
                tmp[j] = makeIntVar("z_{"+i+","+j+"}",4,5, Options.V_BOUND);
            this.addVariables(tmp);
        //    this.addConstraint(eq(minus(tmp[4],28),minus(0,tmp[6])));

            this.addVariables(vs[i]);

        }

        for (int i  = 4 ; i < 8 ;i++)
        {
            IntegerVariable[] tmp = cvs[i];
            tmp[4] = makeIntVar("z_{"+i+",0}",0,10, Options.V_BOUND);
            tmp[5] = makeIntVar("z_{"+i+",1}",0,4, Options.V_BOUND);
            tmp[6] = makeIntVar("z_{"+i+",1}",18,28, Options.V_BOUND);

            for (int j = 0 ; j < 4 ; j++)
                tmp[j] = makeIntVar("z_{"+i+","+j+"}",2,3, Options.V_BOUND);
            this.addVariables(tmp);
          //  this.addConstraint(eq(plus(tmp[4],tmp[6]),28));
            this.addVariables(vs[i]);

        }

        FiniteAutomaton auto = new FiniteAutomaton();
        auto.fill(full,alpha);


        for (int i  = 0 ;i < 8 ; i++)
        {
            Constraint mr = multiCostRegular(cvs[i], vs[i], auto,csts);

            this.addConstraint(mr);

        }


        int[] low = {3,1,4};
        int[] up = {3,1,4};

        IntegerVariable[][] trans = ArrayUtils.transpose(vs);
        for (int i  = 0 ; i < 28 ; i++)
        {
            this.addConstraint(Options.C_GCC_BC,globalCardinality(trans[i],low,up, 0));
        }



    }

    public void addMinCoverConstraint()
    {
        IntegerVariable[] worked = ArrayUtils.transpose(cvs)[4];
        IntegerVariable[] night = ArrayUtils.transpose(cvs)[5];
        IntegerVariable[] rest = ArrayUtils.transpose(cvs)[6];
        //3 days and 1 night per day -> 4*28 working days
        this.addConstraint(eq(sum(worked),4*28));
        this.addConstraint(eq(sum(night),28));
        this.addConstraint(eq(sum(rest),28*4));

        IntegerVariable[] week0 = ArrayUtils.transpose(cvs)[0];
        this.addConstraint(eq(sum(week0),4*7));

        IntegerVariable[] week1 = ArrayUtils.transpose(cvs)[1];
        this.addConstraint(eq(sum(week1),4*7));

        IntegerVariable[] week2 = ArrayUtils.transpose(cvs)[2];
        this.addConstraint(eq(sum(week2),4*7));

        IntegerVariable[] week3 = ArrayUtils.transpose(cvs)[3];
        this.addConstraint(eq(sum(week3),4*7));


    }

    public void addLexConstraint()
    {
        IntegerVariable[][] a = new IntegerVariable[2][28];
        IntegerVariable[][] b = new IntegerVariable[3][28];

        System.arraycopy(vs[0],0,a[0],0,a[0].length);
        System.arraycopy(vs[2],0,a[1],0,a[1].length);


        System.arraycopy(vs[5],0,b[0],0,b[0].length);
        System.arraycopy(vs[6],0,b[1],0,b[1].length);
        System.arraycopy(vs[7],0,b[2],0,b[2].length);




      //  this.addConstraints(lexChainEq(a));
      //  this.addConstraint(lexChainEq(b));
              

    }

    public void addMandatoryShift()
    {

        this.addConstraint(eq(vs[0][0],0));
        this.addConstraint(eq(vs[0][1],0));

        this.addConstraint(eq(vs[2][0],0));
        this.addConstraint(eq(vs[2][1],0));

        this.addConstraint(eq(vs[3][0],1));
        this.addConstraint(eq(vs[3][1],1));

        this.addConstraint(eq(vs[4][0],0));
        this.addConstraint(eq(vs[4][1],0));
        


        


        


    }


    public static void main(String[] args) {
        RuleModel m = new RuleModel();

        m.buildConsecutiveWERule();
        m.buildNoNightBeforeFreeWE();
        m.buildNoMoreThanDayRule();
        m.buildRestAfterNight();
        m.buildCompleteWE();

        m.fillModel();

        m.addLexConstraint();
        m.addMandatoryShift();
        m.addMinCoverConstraint();

        CPSolver s = new CPSolver();
        s.read(m);


        ArrayList<IntDomainVar> mars = new ArrayList<IntDomainVar>();
        for (int i  = 0 ; i < 8 ; i++)
            mars.add(s.getVar(m.cvs[i][4]));


        int[][] lowb = new int[28][3];
        {
            for (int i = 0 ; i < lowb.length ; i++)
            {
                lowb[i][0]= 3;
                lowb[i][1]= 1;
                lowb[i][2]= 4;
            }
        }

        CoverVarValSelector sel = new CoverVarValSelector(s,m.vs,lowb);
     //   s.attachGoal(new AssignVar(sel,sel));


       // s.attachGoal(new AssignVar(new StaticVarOrder(mars.toArray(new IntDomainVar[8])),new DecreasingDomain()));
        s.attachGoal(new AssignVar(new StaticVarOrder(s, s.getVar(ArrayUtils.flatten(ArrayUtils.transpose(m.vs)))),new IncreasingDomain()));
        //s.addGoal(new AssignVar(new RandomIntVarSelector(s,s.getVar(ArrayUtils.flatten(ArrayUtils.transpose(m.vs))),0),new RandomIntValSelector()));



        if (s.solve())
        {

            int i = 0 ;
            for (IntegerVariable[] va : m.vs)
            {

                for (IntDomainVar v : s.getVar(va))
                {
                    System.out.print(toChar(v.getVal())+" ");
                }
                System.out.print("     |   ");
                for (IntDomainVar v : s.getVar(m.cvs[i++]))
                {
                    System.out.print(v.getVal()+" ");
                }
                System.out.println("");
            }

        }
        s.printRuntimeStatistics();
    }


    static char toChar(int i)
    {
        switch(i)
        {
            case 0 : return 'D';
            case 1 : return 'N';
            case 2 : return 'R';
            default : return 'E';
        }
    }


}
