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

package samples.multicostregular.planner;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.automata.fast_costregular.CostRegularValSelector;
import choco.cp.solver.constraints.global.automata.fast_costregular.CostRegular;
import choco.cp.solver.constraints.global.automata.fast_multicostregular.MultiCostRegular;
import choco.cp.solver.constraints.global.automata.fast_multicostregular.valselector.MCRValSelector;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import static choco.Choco.*;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 14, 2008
 * Time: 4:26:07 PM
 */
public class PlanModel {

    final static int REGULAR = 0;
    final static int COSTREG = 1;
    final static int MCR = 2;
    int type;
    Model m;
    Solver s;
    IntegerVariable[] shifts;
    IntegerVariable[] count;
    IntegerVariable z;
    IAutomaton auto;
    InstanceMaker imaker;
    String modelType;
    long seed;
    int nbAct;
    Constraint main;

    public final static int L = 0;
    public final static int P = 1;
    public final static int O = 2;
    public final static int A = 3;
    public final static int B = 4;
    public final static int C = 5;
    public final static int D = 6;
    public final static int E = 7;
    public final static int F = 8;
    public final static int G = 9;
    public final static int H = 10;
    public final static int I = 11;
    public final static int J = 12;
    public final static int K = 13;
    public final static int M = 14;
    public final static int N = 15;
    public final static int Q = 16;
    public final static int S = 17;
    public final static int T = 18;
    public final static int U = 19;
    public final static int V = 20;

    public PlanModel(int type, int nbActivities,long seed,boolean old)
    {

        this.seed =seed;
        this.imaker = new InstanceMaker();
        this.nbAct = nbActivities;
        this.auto = imaker.makeAutomaton(nbActivities);
        this.type  = type;

        switch(type)
        {
            case REGULAR : makeModelWithRegular(); break;
            case COSTREG : makeModelWithCostReg(old); break;
            case MCR     : makeModelWithMCR(old); break;
        }

    }




    private void makeModelWithMCR(boolean old)
    {

        modelType = "MULTI-COST-REGULAR";


        z = makeIntVar("z",0,Integer.MAX_VALUE/100, Options.V_BOUND);

        IntegerVariable[] costs = new IntegerVariable[4];
        costs[0] = z;

        /* int[] bInf = new int[nbAct+3];
int[] bSup = new int[nbAct+3];
bInf[0] = 0; bInf[1] = 1;bInf[2] = 64;
bSup[0] = 4; bSup[1] = 2;bSup[2] = 84;
for (int i = 3 ; i < nbAct+3 ; i++)
  bSup[i] = 32;    */

        costs[1] = makeIntVar("r1",0,4, Options.V_BOUND);
        costs[2] = makeIntVar("r2",1,2, Options.V_BOUND);
        costs[3] = makeIntVar("r3",12,32, Options.V_BOUND);

        /* for (int i = 4 ; i < nbAct+4 ; i++)
      {
          costs[i] = makeIntVar("r"+i,0,32,CPOptions.V_BOUND);
      }  */

        int[][][] csts = new int[96][nbAct+3][+3+1];

        m = new CPModel();

        shifts = makeIntVarArray("t",96,0,2+nbAct);


        m.addVariables(shifts);
        m.addVariable(z);
        m.addVariables(costs);

        int[][] tmp = imaker.getRandomCostMatrix(96,nbAct+3,seed);
        for (int i = 0 ; i < csts.length ;i++)
            for (int j = 0 ; j < csts[i].length ; j++)
                for (int k = 0 ; k < csts[i][j].length ; k++)
                {
                    if (k == 0)
                        csts[i][j][k] = tmp[i][j];
                    else if ( k < 3)
                        csts[i][j][k] = (j == (k-1))? 1 : 0 ;
                    else if (k >= 3)
                        csts[i][j][k] = ((!old && j >=3) || (old && j!= 2))?1:0;
                }

     /*    for (int i = 0 ; i < csts.length ;i++)
            for (int j = 0 ; j < csts[i].length ; j++)
                {
                    int t = csts[i][j][0];
                    csts[i][j][0] = csts[i][j][3];
                    csts[i][j][3] = t;
                }
        IntegerVariable zt = costs[0];
        costs[0] = costs[3];
        costs[3] = zt;
        z = costs[3];      */





        main = multiCostRegular(costs, shifts, auto,csts);

        m.addConstraint(main);


    }

private static int[][][] make3dim(int[][] cost, int dim)
   {
       int[][][] out = new int[cost.length][][];
       for (int i = 0 ; i < out.length ; i++)
       {
           out[i] = new int[cost[i].length][];
           for (int j = 0 ;j < out[i].length ;j++)
           {
               out[i][j] = new int[dim];
               for (int q = 0 ; q < dim ; q++)
               {
                   out[i][j][q] = cost[i][j];
               }
           }
       }
       return out;
   }

    private void makeModelWithCostReg(boolean old) {

        modelType = "COST-REGULAR+GCC";

        int[] bInf = new int[4];
        int[] bSup = new int[4];
        bInf[0] = 0; bInf[1] = 1;bInf[2] = 0; bInf[3] = 12;
        bSup[0] = 4; bSup[1] = 2;bSup[2] = 96;bSup[3] = 32;

        if (old)
        {
            bInf[O] = 64; bSup[O] = 84;
            bInf[3] = 6 ; bSup[3] = 31;
        }

        //    for (int i = 3 ; i < nbAct+3 ; i++)
        //   bSup[i] = 96;
        //bSup[nbAct+3] = bInf[nbAct+3] = 0;




        m = new CPModel();
        shifts = makeIntVarArray("t",96,0,2+nbAct);

        z = makeIntVar("z",0,Integer.MAX_VALUE/100, Options.V_BOUND);

        m.addVariables(shifts);
        m.addVariable(z);
        int[][] csts = imaker.getRandomCostMatrix(96,nbAct+3,seed);



main = costRegular(z, shifts, auto,make3dim(csts,auto.getNbStates()));
                //for (int i = 0 ; i < 96  ;i++)
        //if (i < 44 || i > 56) m.addConstraint(neq(shifts[i],InstanceMaker.L));

        m.addConstraint(main);
        /* m.addConstraint(occurrence(0,makeIntVar("tmp",0,4,CPOptions.V_BOUND),shifts));
        m.addConstraint(occurrence(1,makeIntVar("tmp",1,2,CPOptions.V_BOUND),shifts));
        m.addConstraint(occurrence(2,makeIntVar("tmp",64,84,CPOptions.V_BOUND),shifts));*/


        // m.addConstraint(globalCardinality(CPOptions.C_GCC_AC,shifts,0,2+nbAct,bInf,bSup));

        count = makeIntVarArray("count",shifts.length,0,3, Options.V_ENUM);
       // for (int i = 0 ; i < count.length; i++)
          //  m.addConstraint(new ComponentConstraint(SimpleChannelling.SimpleManager.class,null,new IntegerVariable[]{shifts[i],count[i]}));
        m.addConstraint(new ComponentConstraint(SubSetChannelling.SubSetManager.class,null, ArrayUtils.append(shifts,count)));

        m.addConstraint(globalCardinality(Options.C_GCC_AC,count,0,3,bInf,bSup));


        //IntegerVariable red = makeIntVar("red",12,32);
        //m.addConstraint(eq(red,sum(count)));



    }

    private void makeModelWithRegular()
    {
        modelType = "REGULAR+GCC+ELEMENT";


        //DFA dfa = imaker.makeHadrienAuto();
        int[] bInf = new int[nbAct+3];
        int[] bSup = new int[nbAct+3];
        bInf[0] = 0; bInf[1] = 1;bInf[2] = 64;
        bSup[0] = 4; bSup[1] = 2;bSup[2] = 84;
        for (int i = 3 ; i < nbAct+3 ; i++)
            bSup[i] = 32;
        m = new CPModel();

        shifts = makeIntVarArray("t",96,0,2+nbAct);
        IntegerVariable[] res = makeIntVarArray("res",96,0,Integer.MAX_VALUE/100, Options.V_BOUND);

        int[][] tmp = imaker.getRandomCostMatrix(96,nbAct+3,seed);

        z = makeIntVar("z",0,Integer.MAX_VALUE/100, Options.V_BOUND);
        m.addVariable(z);
        m.addVariables(res);

        m.addVariables(shifts);

        m.addConstraint(globalCardinality(Options.C_GCC_AC,shifts,0,2+nbAct,bInf,bSup));
        // m.addConstraint(regular(dfa,shifts));
        //Thou shalt respect the automaton
        int[][] csts = new int[shifts.length][nbAct+3];
        IntegerVariable zz = makeIntVar("dummy",0,0);
        m.addVariable(zz);
        main = costRegular(zz, shifts, auto,csts);
        m.addConstraint(main);

        //element constraint to handle the cost funciton...

        for (int i = 0 ; i < shifts.length ; i++)
        {
            m.addConstraint(nth(shifts[i],tmp[i],res[i]));
        }
        m.addConstraint(eq(z,sum(res)));


                                



    }

    public int[] solve(final boolean varheur, final boolean valheur) throws Exception {
        if (m == null)
            throw new Exception("Thou shalt create a model first");

        s = new CPSolver();
        // s.setTimeLimit(600000);
        //s.setFailLimit(1500);

        s.monitorFailLimit(true);

        s.read(m);

        //IntDomainVar[] tmp =s.getVar(shifts);
        //UtilAlgo.reverse(tmp);
       if (!varheur) s.setVarIntSelector(new MinDomain(s,s.getVar(shifts)));
        else s.setVarIntSelector(new CenterVarSelec(s, s.getVar(shifts)));
        //else s.setVarIntSelector(new StaticVarOrder(tmp));

        //CPSolver.setVerbosity(CPSolver.SOLUTION);
        if (type == MCR && valheur)
        {
             s.setValIntSelector(new MCRValSelector(new MultiCostRegular[]{(MultiCostRegular)s.getCstr(main)},false));
        }
        if (type == COSTREG && valheur)
        {
            s.setVarIntSelector(new MinDomain(s,s.getVar(shifts)));
             s.setValIntSelector(new CostRegularValSelector((CostRegular)s.getCstr(main),false));
        }
      //    CPSolver.setVerbosity(CPSolver.SEARCH);
      //  s.setLoggingMaxDepth(100);

        Boolean b = s.solve();
        //s.minimize(s.getVar(z),false);


        int bVal = Integer.MAX_VALUE;
        int bTime = 0 ;
        if (b != Boolean.FALSE)
        {
          ///  System.out.println("#########################\t"+modelType+"\t#########################");
            do{
                bVal = s.getVar(z).getVal();
                bTime = s.getTimeCount();

                //   System.out.println(b);
               /* System.out.print("least cost shift : ");
                for (IntegerVariable v : shifts)
                {
                    System.out.print(letterFromVal(s.getVar(v).getVal()));
                }
                System.out.println("\t with cost : "+s.getVar(z));
                //   s.getVar(z). (s.getVar(z).getVal());    */
                s.postCut(s.gt(s.getVar(z).getVal(),s.getVar(z)));

            } while(s.nextSolution() == Boolean.TRUE) ;
        }

        //CPSolver.flushLogs();
        
       // s.printRuntimeSatistics();
        //  System.out.println(s.getNbSolutions()+"[+0]"+" solution(s)");
        // System.out.println("#########################################################################");
        return new int[]{b==Boolean.TRUE?1:0,s.isEncounteredLimit()?1:0, s.getTimeCount(),s.getFailCount(),bVal,bTime};


    }

    public double filteredPercentage(long seed,int act)
    {
        System.out.println("FOR SEED "+seed+" WITH "+act+" ACTIVITIE(S)");

        s = new CPSolver();
        s.read(m);

        s.setVarIntSelector(new CenterVarSelec(s, s.getVar(shifts)));
        //s.setValIntSelector(new MCRValSelector(new MultiCostRegular[]{(MultiCostRegular)s.getCstr(main)},false));

        int bui = 0;
        for (IntDomainVar v: s.getVar(shifts)) bui+= v.getDomainSize();
        System.out.println("AU DEPART : "+bui+" val");

        try {
            s.propagate();
        } catch (ContradictionException e) {
            return 100.0;
        }

        double nbVal = 0;
        double nbFalse = 0;
        double nbTrue = 0;
        for (IntDomainVar v : s.getVar(shifts))
        {
            DisposableIntIterator it ;
            for (it = v.getDomain().getIterator(); it.hasNext() ;)
            {
                int j = it.next();
                nbVal++;
                int env = s.getEnvironment().getWorldIndex();
                s.worldPush();
                try {
                    v.setVal(j);
                } catch (ContradictionException e) {
                    System.err.println("Should not be here !");
                }
                if (s.solve()) nbTrue++;
                else nbFalse++;
                 s.worldPopUntil(env);

            }

        }

        System.out.println("NB VAL   : "+nbVal);
        System.out.println("NB TRUE  : "+nbTrue);
        System.out.println("NB FALSE : "+nbFalse);
        double perc = nbTrue/nbVal*100;
        System.out.println("");
        System.out.println("POURCENTAGE DE OK : "+perc+"%");
        return perc;

    }


    protected String letterFromVal(int val)
    {
        switch (val)
        {
            case InstanceMaker.A : return "A";
            case InstanceMaker.B : return "B";
            case InstanceMaker.C : return "C";
            case InstanceMaker.D : return "D";
            case InstanceMaker.E : return "E";
            case InstanceMaker.F : return "F";
            case InstanceMaker.G : return "G";
            case InstanceMaker.H : return "H";
            case InstanceMaker.I : return "I";
            case InstanceMaker.J : return "J";
            case InstanceMaker.K : return "K";
            case InstanceMaker.L : return "L";
            case InstanceMaker.P : return "P";
            case InstanceMaker.Q : return "Q";
            case InstanceMaker.S : return "S";
            case InstanceMaker.T : return "T";
            case InstanceMaker.U : return "U";
            case InstanceMaker.V : return "V";
            case InstanceMaker.O : return "O";
            case InstanceMaker.M : return "M";
            case InstanceMaker.N : return "N";

        }
        return "Z";
    }

    public static void main(String[] args) throws Exception {
    

        PlanModel dummy = new PlanModel(MCR,1,0,false);
        dummy.solve(false,false);
        int[] activ = {1,2,4,6,8,10,15,20,30,40,50};
       // activ = new int[]{40};
        int nbIter = 2;
        for (int varh = 1 ; varh >= 1 ; varh--)
        for (int type = 1 ; type <= 1 ; type++)
        {
            boolean old = type == 0;

            for (int nbAct : activ)
            {
              /*  int[] resCR = new int[5];
                int[] resMCR = new int[5];
                int nbSolvedCR = 0;
                int nbSolvedMCR = 0; */
                //System.out.println(nbAct+" activities,"+"L B in A ?"+old);
                //System.out.println("SEED,+NB ACT,CONTRAINTES,VARHEUR,VALHEUR,TOTALTIME,TIMETOBEST,BEST, NBFAIL");


                for (int valh = 1 ; valh <= 1 ; valh++)
                for (long seed=  200 ; seed <200+nbIter ; seed++)
                {

                    boolean varheur = varh == 1;
                    boolean valheur = valh == 1;
                    // System.out.println("NB ACT : "+nbAct+"  seed : "+seed);
                    // PlanModel pm = new PlanModel(REGULAR,nbAct,seed);
                    PlanModel pm2 = new PlanModel(COSTREG,nbAct,seed,old);
                    PlanModel pm3 = new PlanModel(MCR,nbAct,seed,old);

                 //   pm3.filteredPercentage(seed,nbAct);
                 //   System.out.println("");
                    //   pm.solve();
                    int [] mcr = pm3.solve(varheur,valheur);

                     if (mcr[0] == 1 )
                    {

                        System.out.println(seed+","+nbAct+",  MCR ,"+varheur+","+valheur+","+mcr[2]+","+mcr[5]+","+mcr[4]+","+mcr[3]);

                    }
                   // System.out.println("PASCR");

                    int[] cr = pm2.solve(varheur,valheur);


                    if (cr[0] == 1)
                    {

                        System.out.println(seed+","+nbAct+",CR+GCC,"+varheur+","+valheur+","+cr[2]+","+cr[5]+","+cr[4]+","+cr[3]);

                    

                    }
                    System.out.println(""); 


                }


            }
        }
    }

    public static void testGCC()
    {
        Model m = new CPModel();

        int low[] = {1,1,1};
        int up[] = {1,1,0};
        IntegerVariable[] vs = makeIntVarArray("x",3,1,3);


        m.addVariables(vs);
        m.addConstraint(globalCardinality(Options.C_GCC_AC,vs,1,3,low,up));

        Solver s = new CPSolver();
        s.read(m);
        if (s.solve())
        {
            for (IntegerVariable v : vs)
                System.out.print(s.getVar(v).getVal()+" ");
        }

    }

}
