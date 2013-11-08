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

package choco.model.constraints.reified;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.EqualXC;
import choco.cp.solver.constraints.integer.channeling.ReifiedLargeNand;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.integer.extension.TuplesTest;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

import static choco.Choco.*;

/**
 * Created by IntelliJ IDEA.
 * User: cprudhom
 * Date: 28 mai 2010
 * Time: 17:49:14
 * To change this template use File | Settings | File Templates.
 */
public class NandTest {

    @Test
    public void test1(){
        for(int i = 0; i < 50; i++){
            Model m1 = new CPModel();
            Model m2 = new CPModel();
            Solver s1 = new CPSolver();
            Solver s2 = new CPSolver();

            IntegerVariable[] bool = makeBooleanVarArray("b", 2);

            m1.addConstraint(nand(bool));

            m2.addConstraint(nand(eq(bool[0],1), eq(bool[1], 1)));

            s1.read(m1);
            s2.read(m2);

            s1.setVarIntSelector(new RandomIntVarSelector(s1, s1.getVar(bool), i));
            s2.setVarIntSelector(new RandomIntVarSelector(s2, s2.getVar(bool), i));
            s1.setValIntSelector(new RandomIntValSelector(i));
            s2.setValIntSelector(new RandomIntValSelector(i));

            s1.solveAll();
            s2.solveAll();

            Assert.assertEquals("solutions", s2.getNbSolutions(), s1.getNbSolutions());
            Assert.assertEquals("nodes", s2.getNodeCount(), s1.getNodeCount());
        }
    }

    @Test
    public void test2(){
        Random r;
        for(int i = 0; i < 50; i++){
            r = new Random(i);
            Model m1 = new CPModel();
            Model m2 = new CPModel();
            Solver s1 = new CPSolver();
            Solver s2 = new CPSolver();

            IntegerVariable[] bool = makeBooleanVarArray("b", 5+r.nextInt(5));

            m1.addConstraint(nand(bool));

            Constraint[] cs = new Constraint[bool.length];
            for(int j = 0; j < bool.length; j++){
                cs[j] = eq(bool[j],1);
            }

            m2.addConstraint(nand(cs));

            s1.read(m1);
            s2.read(m2);

            s1.setVarIntSelector(new RandomIntVarSelector(s1, s1.getVar(bool), i));
            s2.setVarIntSelector(new RandomIntVarSelector(s2, s2.getVar(bool), i));
            s1.setValIntSelector(new RandomIntValSelector(i));
            s2.setValIntSelector(new RandomIntValSelector(i));

            s1.solveAll();
            s2.solveAll();

            Assert.assertEquals("solutions", s2.getNbSolutions(), s1.getNbSolutions());
//            Assert.assertEquals("nodes", s2.getNodeCount(), s1.getNodeCount());
        }
    }

    @Test
    public void test3(){
        Random r;
        for(int i = 0; i < 50; i++){
            r = new Random(9);
            Model m1 = new CPModel();
            Model m2 = new CPModel();
            Solver s1 = new CPSolver();
            Solver s2 = new CPSolver();

            IntegerVariable bin = makeBooleanVar("bin");
            IntegerVariable[] bool = makeBooleanVarArray("b", 1+r.nextInt(20));

            Constraint c = reifiedNand(bin, bool);

            m1.addConstraint(c);
            m1.addConstraint(eq(bin,0));

            m2.addConstraint(c);
            m2.addConstraint(eq(bin,1));


            s1.read(m1);
            s2.read(m2);

            s1.setVarIntSelector(new RandomIntVarSelector(s1, i));
            s1.setValIntSelector(new RandomIntValSelector(i));

            s2.setVarIntSelector(new RandomIntVarSelector(s2, i));
            s2.setValIntSelector(new RandomIntValSelector(i));


            s1.solveAll();
            s2.solveAll();
            int nbSol = (int)Math.pow(2,bool.length)-1;

            Assert.assertEquals("solutions", 1 , s1.getNbSolutions());
            Assert.assertEquals("solutions", nbSol, s2.getNbSolutions());
        }
    }

    @Test
    public void test4(){
        Random r;
        for(int i = 1; i< 100; i++){
            r = new Random(i);
            Model m1 = new CPModel();
            IntegerVariable[] bool = makeBooleanVarArray("b", 3);
            Constraint c = reifiedNand(bool[0], bool[1],bool[2]);
            m1.addConstraint(c);
            int nbSol = 4;
            int ad = r.nextInt(7);
            switch (ad){
                case 0 :
                    m1.addConstraint(eq(bool[0], 0));
                    nbSol = 1;
                    break;
                case 1:
                    m1.addConstraint(eq(bool[0], 1));
                    nbSol = 3;
                    break;
                case 2 :
                    m1.addConstraint(eq(bool[1], 0));
                    nbSol = 2;
                    break;
                case 3:
                    m1.addConstraint(eq(bool[1], 1));
                    nbSol = 2;
                    break;
                case 4 :
                    m1.addConstraint(eq(bool[2], 0));
                    nbSol = 2;
                    break;
                case 5:
                    m1.addConstraint(eq(bool[2], 1));
                    nbSol = 2;
                    break;
            }
            Solver s1 = new CPSolver();
            s1.read(m1);

            s1.setVarIntSelector(new RandomIntVarSelector(s1, i));
            s1.setValIntSelector(new RandomIntValSelector(i));
            s1.solveAll();
            Assert.assertEquals("solutions -- seed:"+i, nbSol , s1.getNbSolutions());

        }
    }

    @Test
    public void test5(){
        Random r;
        for(int i = 0; i< 200; i++){
            r = new Random(i);
            Model m1 = new CPModel();
            IntegerVariable[] bin = makeBooleanVarArray("bin", 1);
            IntegerVariable[] bool = makeBooleanVarArray("b", 1+r.nextInt(10));
            IntegerVariable[] bools = ArrayUtils.append(bin, bool);
            Constraint c = reifiedNand(bin[0], bool);
            m1.addConstraint(c);
            int idx = r.nextInt(bools.length);
            int val = r.nextInt(2);
            m1.addConstraint(eq(bools[idx], val));
            int nbSol = (int)Math.pow(2,bool.length)/2;
            if(idx == 0){
                if(val ==0){
                    nbSol = 1;
                }else{
                    nbSol = (int)Math.pow(2,bool.length)-1;
                }
            }

            Solver s1 = new CPSolver();
            s1.read(m1);

            s1.setVarIntSelector(new RandomIntVarSelector(s1, i));
            s1.setValIntSelector(new RandomIntValSelector(i));
            s1.solveAll();
            Assert.assertEquals("solutions  -- seed:"+i, nbSol , s1.getNbSolutions());

        }
    }

    private static class NandChecker extends TuplesTest {
        public boolean checkTuple(int[] tuple) {
            int size = tuple.length-1;
            int bin = tuple[0];
            int sum = 0;
            int i  = 1 ;
            while (i <  tuple.length) {
                sum += tuple[i];
                i++;
            }
            return (bin == 1 && sum < size)||(bin == 0 && sum == size);
        }
    }

    @Test
    public void test6(){
        Random r;
        for(int i = 0; i< 200; i++){
            r = new Random(i);
            Model m1 = new CPModel();
            Model m2 = new CPModel();

            IntegerVariable[] bin = makeBooleanVarArray("bin", 1);
            IntegerVariable[] bool = makeBooleanVarArray("b", 1+r.nextInt(10));
            IntegerVariable[] bools = ArrayUtils.append(bin, bool);
            Constraint c1 = reifiedNand(bin[0], bool);
            Constraint c2 = relationTupleAC(bools, new NandChecker());

            m1.addConstraint(c1);
            m2.addConstraint(c2);

            int idx = r.nextInt(bools.length);
            int val = r.nextInt(2);

            m1.addConstraint(eq(bools[idx], val));

            m2.addConstraint(eq(bools[idx], val));

            int nbSol = (int)Math.pow(2,bool.length)/2;
            if(idx == 0){
                if(val ==0){
                    nbSol = 1;
                }else{
                    nbSol = (int)Math.pow(2,bool.length)-1;
                }
            }
            Solver s1 = new CPSolver();
            s1.read(m1);
            Solver s2 = new CPSolver();
            s2.read(m2);

            s1.setVarIntSelector(new RandomIntVarSelector(s1, i));
            s1.setValIntSelector(new RandomIntValSelector(i));
            s1.solveAll();

            s2.setVarIntSelector(new RandomIntVarSelector(s2, i));
            s2.setValIntSelector(new RandomIntValSelector(i));
            s2.solveAll();
            Assert.assertEquals("solutions  -- seed:"+i, nbSol , s1.getNbSolutions());
            Assert.assertEquals("solutions -- seed:"+i, s2.getNbSolutions() , s1.getNbSolutions());

        }
    }

    @Test
    public void test7(){
        Solver s = new CPSolver();
        IntDomainVar a  = s.createEnumIntVar("a", 0, 0);
        IntDomainVar b  = s.createEnumIntVar("b", 1, 1);
        IntDomainVar c  = s.createEnumIntVar("c", 1, 1);

        s.post(new ReifiedLargeNand(new IntDomainVar[]{a,b,c}, s.getEnvironment()));

        s.solveAll();

        Assert.assertEquals(1, s.getNbSolutions());

    }

    @Test
    public void test8(){
        Solver s = new CPSolver();
        IntDomainVar a  = s.createEnumIntVar("a", 0, 1);
        IntDomainVar b  = s.createEnumIntVar("b", 0, 1);
        IntDomainVar c  = s.createEnumIntVar("c", 0, 0);
        IntDomainVar d  = s.createEnumIntVar("d", 0, 0);
        IntDomainVar e  = s.createEnumIntVar("e", 0, 1);
        IntDomainVar f  = s.createEnumIntVar("f", 0, 0);
        IntDomainVar g  = s.createEnumIntVar("g", 0, 1);
        IntDomainVar h  = s.createEnumIntVar("h", 0, 0);

        s.post(new EqualXC(b, 0));
        s.post(new ReifiedLargeNand(new IntDomainVar[]{a,b,c, d, e, f, g, h}, s.getEnvironment()));

        s.setVarIntSelector(new StaticVarOrder(s, new IntDomainVar[]{a,b ,c, d, e, f, g, h}));
        s.solveAll();

        Assert.assertEquals(4, s.getNbSolutions());

    }
}
