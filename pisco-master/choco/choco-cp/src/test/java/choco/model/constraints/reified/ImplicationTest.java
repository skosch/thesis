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

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 3 nov. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*/
public class ImplicationTest {

    @Test
    public void test1(){
        for (int i = 0; i< 200; i++){
            IntegerVariable b = Choco.makeBooleanVar("b");
            IntegerVariable[] bs = Choco.makeBooleanVarArray("bs", 2);

            Constraint c = Choco.reifiedRightImp(b, bs[0], bs[1]);

            List<int[]> feas = new ArrayList<int[]>();
            feas.add(new int[]{1,1,1});
            feas.add(new int[]{0,1,0});
            feas.add(new int[]{1,0,1});
            feas.add(new int[]{1,0,0});
            Constraint verif = Choco.feasTupleAC(feas, ArrayUtils.append(new IntegerVariable[]{b}, bs));

            Model m = new CPModel();
            m.addConstraint(c);

            Model m_v = new CPModel();
            m_v.addConstraint(verif);

            Solver s = new CPSolver();
            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, i));
            s.setValIntSelector(new RandomIntValSelector(i));
            s.solve();
            do{
                if(s.getVar(b).getVal() == 1) {
                    Assert.assertTrue(s.getVar(bs[0]).getVal()<=s.getVar(bs[1]).getVal());
                }else{
                    Assert.assertTrue(s.getVar(bs[0]).getVal()>s.getVar(bs[1]).getVal());
                }
            }while(s.nextSolution());

            Solver s_v = new CPSolver();
            s_v.read(m);
            s_v.setVarIntSelector(new RandomIntVarSelector(s_v, i));
            s_v.setValIntSelector(new RandomIntValSelector(i));
            s_v.solveAll();

            Assert.assertEquals("nb sol", s_v.getNbSolutions(), s.getSolutionCount());
        }
    }

    @Test
    public void test1_(){
        for (int i = 0; i< 200; i++){
            IntegerVariable b = Choco.makeBooleanVar("b");
            IntegerVariable[] bs = Choco.makeBooleanVarArray("bs", 2);

            Constraint c = Choco.reifiedLeftImp(b, bs[0], bs[1]);

            List<int[]> feas = new ArrayList<int[]>();
            feas.add(new int[]{1,1,1});
            feas.add(new int[]{0,0,1});
            feas.add(new int[]{1,1,0});
            feas.add(new int[]{1,0,0});
            Constraint verif = Choco.feasTupleAC(feas, ArrayUtils.append(new IntegerVariable[]{b}, bs));

            Model m = new CPModel();
            m.addConstraint(c);

            Model m_v = new CPModel();
            m_v.addConstraint(verif);

            Solver s = new CPSolver();
            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, i));
            s.setValIntSelector(new RandomIntValSelector(i));
            s.solve();
            do{
                if(s.getVar(b).getVal() == 1) {
                    Assert.assertTrue(s.getVar(bs[0]).getVal()>=s.getVar(bs[1]).getVal());
                }else{
                    Assert.assertTrue(s.getVar(bs[0]).getVal()<s.getVar(bs[1]).getVal());
                }
            }while(s.nextSolution());


            Solver s_v = new CPSolver();
            s_v.read(m);
            s_v.setVarIntSelector(new RandomIntVarSelector(s_v, i));
            s_v.setValIntSelector(new RandomIntValSelector(i));
            s_v.solveAll();

            Assert.assertEquals("nb sol", s_v.getNbSolutions(), s.getSolutionCount());
            Assert.assertEquals("nb sol", s_v.getNbSolutions(), s.getSolutionCount());
        }
    }

    @Test
    public void test2(){
        for (int i = 0; i< 200; i++){
            IntegerVariable b = Choco.makeBooleanVar("b");
            IntegerVariable[] bs = Choco.makeBooleanVarArray("bs", 2);

            Constraint c = Choco.reifiedRightImp(b, bs[0], bs[1]);

            Constraint verif = Choco.reifiedConstraint(b, Choco.leq(bs[0], bs[1]));

            Model m = new CPModel();
            m.addConstraint(c);

            Model m_v = new CPModel();
            m_v.addConstraint(verif);

            Solver s = new CPSolver();
            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, i));
            s.setValIntSelector(new RandomIntValSelector(i));
            s.solve();
            do{
                if(s.getVar(b).getVal() == 1) {
                    Assert.assertTrue(s.getVar(bs[0]).getVal()<=s.getVar(bs[1]).getVal());
                }else{
                    Assert.assertTrue(s.getVar(bs[0]).getVal()>s.getVar(bs[1]).getVal());
                }
            }while(s.nextSolution());

            Solver s_v = new CPSolver();
            s_v.read(m);
            s_v.setVarIntSelector(new RandomIntVarSelector(s_v, i));
            s_v.setValIntSelector(new RandomIntValSelector(i));
            s_v.solveAll();

            Assert.assertEquals("nb sol", s_v.getNbSolutions(), s.getSolutionCount());
        }
    }

    @Test
    public void test2_(){
        for (int i = 0; i< 200; i++){
            IntegerVariable b = Choco.makeBooleanVar("b");
            IntegerVariable[] bs = Choco.makeBooleanVarArray("bs", 2);

            Constraint c = Choco.reifiedLeftImp(b, bs[0], bs[1]);

            Constraint verif = Choco.reifiedConstraint(b, Choco.geq(bs[0], bs[1]));

            Model m = new CPModel();
            m.addConstraint(c);

            Model m_v = new CPModel();
            m_v.addConstraint(verif);

            Solver s = new CPSolver();
            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, i));
            s.setValIntSelector(new RandomIntValSelector(i));
            s.solve();
            do{
                if(s.getVar(b).getVal() == 1) {
                    Assert.assertTrue(s.getVar(bs[0]).getVal()>=s.getVar(bs[1]).getVal());
                }else{
                    Assert.assertTrue(s.getVar(bs[0]).getVal()<s.getVar(bs[1]).getVal());
                }
            }while(s.nextSolution());

            Solver s_v = new CPSolver();
            s_v.read(m);
            s_v.setVarIntSelector(new RandomIntVarSelector(s_v, i));
            s_v.setValIntSelector(new RandomIntValSelector(i));
            s_v.solveAll();

            Assert.assertEquals("nb sol", s_v.getNbSolutions(), s.getSolutionCount());
        }
    }


    @Test
    public void test3(){
        Random r;
        for (int i = 0; i< 300; i++){
            r = new Random(i);
            IntegerVariable b = Choco.makeBooleanVar("b");
            IntegerVariable[] bs = Choco.makeBooleanVarArray("bs", 2);

            Constraint c = Choco.reifiedRightImp(b, bs[0], bs[1]);

            Constraint verif = Choco.reifiedConstraint(b, Choco.leq(bs[0], bs[1]));

            Constraint eq = Choco.TRUE;
            int nbSol = 4;
            switch(r.nextInt(7)){
                case 0:
                    eq = Choco.eq(b, 0);
                    nbSol = 1;
                    break;
                case 1:
                    eq = Choco.eq(bs[0], 0);
                    nbSol = 2;
                    break;
                case 2:
                    eq = Choco.eq(bs[1], 0);
                    nbSol = 2;
                    break;
                case 3:
                    eq = Choco.eq(b, 1);
                    nbSol = 3;
                    break;
                case 4:
                    eq = Choco.eq(bs[0], 1);
                    nbSol = 2;
                    break;
                case 5:
                    eq = Choco.eq(bs[1], 1);
                    nbSol = 2;
                    break;
            }

            Model m = new CPModel();
            m.addConstraint(c);
            m.addConstraint(eq);

            Model m_v = new CPModel();
            m_v.addConstraint(verif);
            m_v.addConstraint(eq);

            Solver s = new CPSolver();
            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, i));
            s.setValIntSelector(new RandomIntValSelector(i));
            s.solve();
            do{
                if(s.getVar(b).getVal() == 1) {
                    Assert.assertTrue("seed:"+i,s.getVar(bs[0]).getVal()<=s.getVar(bs[1]).getVal());
                }else{
                    Assert.assertTrue("seed:"+i,s.getVar(bs[0]).getVal()>s.getVar(bs[1]).getVal());
                }
            }while(s.nextSolution());

            Solver s_v = new CPSolver();
            s_v.read(m);
            s_v.setVarIntSelector(new RandomIntVarSelector(s_v, i));
            s_v.setValIntSelector(new RandomIntValSelector(i));
            s_v.solveAll();

            Assert.assertEquals("seed:"+i, s_v.getNbSolutions(), s.getSolutionCount());
            Assert.assertEquals("seed:"+i, nbSol, s.getSolutionCount());
        }
    }

    @Test
    public void test3_(){
        Random r;
        for (int i = 0; i< 300; i++){
            r = new Random(i);
            IntegerVariable b = Choco.makeBooleanVar("b");
            IntegerVariable[] bs = Choco.makeBooleanVarArray("bs", 2);

            Constraint c = Choco.reifiedLeftImp(b, bs[0], bs[1]);

            Constraint verif = Choco.reifiedConstraint(b, Choco.leq(bs[0], bs[1]));

            Constraint eq = Choco.TRUE;
            int nbSol = 4;
            switch(r.nextInt(7)){
                case 0:
                    eq = Choco.eq(b, 0);
                    nbSol = 1;
                    break;
                case 1:
                    eq = Choco.eq(bs[0], 0);
                    nbSol = 2;
                    break;
                case 2:
                    eq = Choco.eq(bs[1], 0);
                    nbSol = 2;
                    break;
                case 3:
                    eq = Choco.eq(b, 1);
                    nbSol = 3;
                    break;
                case 4:
                    eq = Choco.eq(bs[0], 1);
                    nbSol = 2;
                    break;
                case 5:
                    eq = Choco.eq(bs[1], 1);
                    nbSol = 2;
                    break;
            }

            Model m = new CPModel();
            m.addConstraint(c);
            m.addConstraint(eq);

            Model m_v = new CPModel();
            m_v.addConstraint(verif);
            m_v.addConstraint(eq);

            Solver s = new CPSolver();
            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, i));
            s.setValIntSelector(new RandomIntValSelector(i));
            s.solve();
            do{
                if(s.getVar(b).getVal() == 1) {
                    Assert.assertTrue("seed:"+i,s.getVar(bs[0]).getVal()>=s.getVar(bs[1]).getVal());
                }else{
                    Assert.assertTrue("seed:"+i,s.getVar(bs[0]).getVal()<s.getVar(bs[1]).getVal());
                }
            }while(s.nextSolution());

            Solver s_v = new CPSolver();
            s_v.read(m);
            s_v.setVarIntSelector(new RandomIntVarSelector(s_v, i));
            s_v.setValIntSelector(new RandomIntValSelector(i));
            s_v.solveAll();

            Assert.assertEquals("seed:"+i, s_v.getNbSolutions(), s.getSolutionCount());
            Assert.assertEquals("seed:"+i, nbSol, s.getSolutionCount());
        }
    }

}
