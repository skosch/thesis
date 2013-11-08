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
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Mar 3, 2010
 * Time: 11:15:22 AM
 */
public class CostKnapSackTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    @Test
    public void allKnapsackTest()
    {

        Random r = new Random();
        for (int taille =10; taille <= 200 ; taille+=10)
        {
            for (int test =0 ; test <= 5 ; test++)
                simpleKnapSackTest(r.nextLong(),taille);
        }
    }

    @Test
    public void test20711762960952(){
        long seed= 20711762960952L;
        Random r = new Random(seed);
        for (int taille =10; taille <= 200 ; taille+=10)
        {
            for (int test =0 ; test <= 5 ; test++)
                simpleKnapSackTest(r.nextLong(),taille);
        }
    }

    public static void simpleKnapSackTest(long seed, int n)
    {
        Model m = new CPModel();
        Solver s = new CPSolver();
        int[] poids = new int[n];
        int[] profits = new int[n];

        Random r = new Random(seed);
        for (int i = 0 ; i < n ; i++)
        {
            poids[i] = r.nextInt(n/5);
            profits[i] = r.nextInt(n);
        }

        IntegerVariable[] vars = makeIntVarArray("x",n,0,1, Options.V_ENUM);
        int a = r.nextInt(n/5);
        int b = r.nextInt(n/5);
        int min = Math.min(a,b);
        int max = Math.max(a,b);
        IntegerVariable poid = makeIntVar("gain",min,max, Options.V_ENUM);
        IntegerVariable profit = makeIntVar("profit",0,Integer.MAX_VALUE/1000, Options.V_BOUND);

        m.addConstraint(knapsackProblem(poid, profit, vars, poids,profits));

        s.read(m);

        if (s.maximize(s.getVar(profit),false))
        {
            assertTrue(s.checkSolution());

            StringBuffer buffer = new StringBuffer();
            for (IntegerVariable v : vars)
            {
                buffer.append(s.getVar(v).getVal()).append(' ');
            }
            buffer.append(System.getProperty("line.separator" ));




            int sumProf = 0;
            int sumPoid = 0;
            for (int i = 0;  i < vars.length ; i++)
            {
                sumPoid+= poids[i]*s.getVar(vars[i]).getVal();
                sumProf+= profits[i]*s.getVar(vars[i]).getVal();
            }

            assertEquals(s.getVar(profit).getVal(),sumProf);
            assertEquals(s.getVar(poid).getVal(),sumPoid);

            buffer.append("Poids : "+s.getVar(poid).getVal()+" in ["+min+","+max+"]"+" | sum of weights = "+sumPoid);
            buffer.append(System.getProperty("line.separator" ));
            buffer.append("Profit : "+s.getVar(profit).getVal()+" | sum of profits = "+sumProf);



            LOGGER.log(Level.INFO,buffer.toString());

        }
        else
        {
            LOGGER.log(Level.INFO,"no solution found");
        }

    }

}
