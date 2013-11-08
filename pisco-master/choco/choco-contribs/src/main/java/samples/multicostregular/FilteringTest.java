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

package samples.multicostregular;

import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Mar 30, 2009
 * Time: 11:55:06 AM
 */
public class FilteringTest {

    private static IAutomaton generateRandomAutomaton(int nbVar, int[] val, Random r)
    {
        StringBuffer regexp = new StringBuffer();
        for (int i = 0 ; i  < nbVar ; i++)
        {
            regexp.append(getRandomSubset(val,r));
        }
     //   System.out.println(regexp);
        return new FiniteAutomaton(regexp.toString());

    }

    private static int[][][] generateRandomCosts(int nbVar, int maxVal, int nbR, int maxCost,Random r)
    {
        int[][][] ret = new int[nbVar][maxVal+1][nbR];
        for (int i = 0 ; i < ret.length ;i++)
            for (int j = 0 ; j < ret[i].length ;j++)
                for (int k = 0 ; k < ret[i][j].length ; k++)
                {   if (r.nextInt(3) > 1)
                    ret[i][j][k] = r.nextInt(maxCost/2)+1+maxCost;
                else
                    ret[i][j][k] = r.nextInt(maxCost/2)+1;
                }

        return ret;
    }



    private static String getRandomSubset(int[] val, Random r)
    {

        ArrayList<Integer> arr = new ArrayList<Integer>();
        for (int i : val) arr.add(i);

        Collections.shuffle(arr,r);

        int k;
        k = 1+r.nextInt(arr.size());

        StringBuffer b = new StringBuffer("(");
        for (int i : arr.subList(0,k))
        {
            if (i > 9)
                b.append('<').append(i).append('>');
            else
                b.append(i);
            b.append('|');
        }
        b.deleteCharAt(b.length()-1).append(')');

        return b.toString();

    }

    public static void firstNodeFilterTest() {


        int n = 2;
        int d = 655;
        int r = 10;
        int max = n*100;
        int iter = 10;

        double all = 0.0;

        for (int seed = 0 ; seed < iter; seed++)
        {
            Random rand = new Random(seed);
            Model m = new CPModel();
            Solver s = new CPSolver();

            IntegerVariable[] vars = makeIntVarArray("v",n,0,d, Options.V_ENUM);
            m.addVariables(vars);
            int[] val = new int[d+1];
            for (int i  = 0 ; i <= d ; i++) val[i] =i;
            IAutomaton a = generateRandomAutomaton(n,val,rand);
            int[][][] csts = generateRandomCosts(n,d,r,max,rand);


            IntegerVariable[] z = new IntegerVariable[r];
            for (int i  = 0 ; i < r ;i++)
                z[i] = makeIntVar("z_"+i,0,rand.nextInt(max*n), Options.V_BOUND);
            m.addVariables(z);

            Constraint c = multiCostRegular(z, vars, a,csts);

            m.addConstraint(c);


            // Solver PART

            s.read(m);

            int bui = 0;
            for (IntDomainVar v: s.getVar(vars)) bui+= v.getDomainSize();
            System.out.println("AU DEPART : "+bui+" val");

            try {
                s.propagate();

                double nbVal = 0;
                double nbFalse = 0;
                double nbTrue = 0;
                for (IntDomainVar v : s.getVar(vars))
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
                        //  System.err.println("LEAVING SOLVE");
                        s.worldPopUntil(env);



                    }

                }

                System.out.println("NB VAL   : "+nbVal);
                System.out.println("NB TRUE  : "+nbTrue);
                System.out.println("NB FALSE : "+nbFalse);
                double perc = nbTrue/nbVal*100;
                all+=perc;

                System.out.println("");
                System.out.println("POURCENTAGE DE OK : "+perc+"%");

            }
            catch (ContradictionException e) {
                System.out.println("PAS DE SOLUTION ET TOUT RETIRE");
                all+=100.0;
            }

        }

        System.out.println("");
        System.out.println("ALL IN ALL : "+(all/iter)+'%');

    }

    public static void swapCostAndVar(int idx, int[][][] csts, IntegerVariable[] z)
    {
        if (idx > 0)
        {
            IntegerVariable ti = z[0];
            z[0] = z[idx];
            z[idx] = ti;

            for (int i = 0 ; i < csts.length ; i++)
            {
                for (int j = 0;  j < csts[i].length ; j++)
                {
                    int tmp = csts[i][j][0];
                    csts[i][j][0] = csts[i][j][idx];
                    csts[i][j][idx] = tmp;
                }
            }
        }

    }

    private static double averageGap(int idx, int[][][] csts)
    {
        double average = 0.0;
        double nbVal = 0.0;
        for (int[][] cst : csts) {

            for (int[] aCst : cst) {
                average += aCst[idx];
                nbVal++;

            }
        }
        average/=nbVal;
        double res = 0.0;
        for (int[][] cst : csts) {

            for (int[] aCst : cst) {
                res+=Math.abs(average-aCst[idx]);

            }
        }
        return res/nbVal;
    }


    private static int[] lowestLongestCostPath(int idx, int[][][] csts)
    {
        int[] res = {0,0};
        ArrayList<ArrayList<Integer>> mat = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < csts.length ; i++)
        {
            mat.add(new ArrayList<Integer>());
            for (int j =0 ; j < csts[i].length ; j++)
            {
                mat.get(i).add(csts[i][j][idx]);
            }
        }

        for (ArrayList<Integer> aMat : mat) {

            int max = Collections.max(aMat);
            int min = Collections.min(aMat);

            res[0] += min;
            res[1] += max;

        }

        res[0]/=csts.length;
        res[1]/=csts.length;
        return res;
    }
    



    private static void print(int idx,int[][][] csts,IntegerVariable[]z)
    {

        System.out.println("#### PRINTING IDX "+idx);
        //printBound(z[idx]);
        double average = 0.0;
        double nbVal = 0.0;
        printBound(z[idx]);
        for (int[][] cst : csts) {
            for (int[] aCst : cst) {
                average=Math.max(average,aCst[idx]);
                nbVal++;
                System.out.print(StringUtils.pad("" + aCst[idx], -6, " "));
            }
            System.out.println("");
        }
        average= averageGap(idx,csts);
        int[] minmax = lowestLongestCostPath(idx,csts);
      //  System.out.println("SUP : "+z[idx].getUppB()+"\tMIN PATH COST : "+minmax[0]+"\tMAX PATH COST : "+minmax[1]+"\t RATIO UB : "+(z[idx].getUppB()/(double)minmax[1]));

    }

    private static void printBound(IntegerVariable integerVariable) {
        System.out.println(integerVariable.getUppB());
    }


    public static void orderFilterTest()
    {

        int n = 20;
        int d = 20;
        int r = 8;
        int max = n*100;
        int iter = 10;


        for (int seed = 0 ; seed < iter; seed++)
        {
            for (int order = 0;  order < r ; order++)
            {
                Random rand = new Random(seed);
                Model m = new CPModel();
                Solver s = new CPSolver();

                IntegerVariable[] vars = makeIntVarArray("v",n,0,d, Options.V_ENUM);
                m.addVariables(vars);
                int[] val = new int[d+1];
                for (int i  = 0 ; i <= d ; i++) val[i] =i;
                IAutomaton a = generateRandomAutomaton(n,val,rand);



                int[][][] csts = generateRandomCosts(n,d,r,max,rand);
                IntegerVariable[] z = new IntegerVariable[r];
                for (int i  = 0 ; i < r ;i++)
                    z[i] = makeIntVar("z_"+i,0,max*n/4+rand.nextInt(max*n/2), Options.V_BOUND);

               // if (order == 0 || order == 5)
                    print(order,csts,z);
              //  else
              //      printBound(z[order]);
                swapCostAndVar(order,csts,z);

                m.addVariables(z);

                Constraint c = multiCostRegular(z, vars, a,csts);

                m.addConstraint(c);




                // Solver


                s.read(m);
                s.monitorBackTrackLimit(true);
                
                s.solve();
                for (IntDomainVar dom : s.getVar(vars)) System.out.print(dom.getVal()+" ");
                System.out.println("");
                String feas =(StringUtils.pad(s.isFeasible()+"",6," "));
                String nc = StringUtils.pad(s.getNodeCount()+" nodes",-11," ");
                String ti = StringUtils.pad(s.getTimeCount()+"ms",-10," ");
                System.out.println(feas+nc+ti);

            }
            System.out.println("################");

        }


    }


    public static void main(String[] args) {

        orderFilterTest();

    }


}