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

package choco.model.constraints.real;

import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.solver.Solver;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 10 juin 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*
* These tests don't have any sens.
* Counting the number of solutions is not a deterministic way to check the behaviour of such problem,
* as "solutions" can be different, based on the way the variables are choosen.
  *
*/
 @Ignore
public class EqTest {

    @Test
    public void test1(){
        RealVariable x = new RealVariable("x", -.1, .1);
        Constraint c1= geq(x, 0);
        Constraint c2= leq(x, 0);

        Model m1 = new CPModel();
        m1.addConstraint(c1);

        Model m2 = new CPModel();
        m2.addConstraint(c2);


        Solver s1 = new CPSolver();
        s1.setPrecision(0.01);
        s1.read(m1);
       // System.out.println(s1.pretty());
        s1.solveAll();

        Solver s2 = new CPSolver();
        s2.setPrecision(0.01);
        s2.read(m2);
        s2.solveAll();

        Assert.assertEquals("not same number of solutions", s1.getNbSolutions(), s2.getNbSolutions());
    }


    @Test
    public void test2(){
        RealVariable x = new RealVariable("x", -.1, .1);
        Constraint c1= geq(0, x);
        Constraint c2= leq(0, x);

        Model m1 = new CPModel();
        m1.addConstraint(c1);

        Model m2 = new CPModel();
        m2.addConstraint(c2);

        Solver s1 = new CPSolver();
        s1.setPrecision(0.01);
        s1.read(m1);
        s1.solveAll();

        Solver s2 = new CPSolver();
        s2.setPrecision(0.01);
        s2.read(m2);
        s2.solveAll();

        Assert.assertEquals("not same number of solutions", s1.getNbSolutions(), s2.getNbSolutions());
    }


    @Test
    public void test3(){
        RealVariable x = new RealVariable("x", -.1, .1);
        RealVariable y = new RealVariable("y", -.1, .1);
        Constraint c1= geq(x, y);
        Constraint c2= leq(x, y);

        Model m1 = new CPModel();
        m1.addConstraint(c1);

        Model m2 = new CPModel();
        m2.addConstraint(c2);


        Solver s1 = new CPSolver();
        s1.setPrecision(0.01);
        s1.read(m1);
        s1.solveAll();

        Solver s2 = new CPSolver();
        s2.setPrecision(0.01);
        s2.read(m2);
        s2.solveAll();

        Assert.assertEquals("not same number of solutions", s1.getNbSolutions(), s2.getNbSolutions());
    }

    @Test
    public void test4(){
        RealVariable x = new RealVariable("x", -.1, .1);
        RealVariable y = new RealVariable("y", -.1, .1);
        RealVariable z = new RealVariable("z", -.1, .1);

        Constraint c1= geq(z, minus(x, y));
        Constraint c2= leq(x, plus(y, z));

        Model m1 = new CPModel();
        m1.addVariables(x,y,z);
        m1.addConstraint(c1);

        Model m2 = new CPModel();
        m2.addVariables(x,y,z);
        m2.addConstraint(c2);


        Solver s1 = new CPSolver();
        s1.setPrecision(0.01);
        s1.read(m1);
        s1.solveAll();

        Solver s2 = new CPSolver();
        s2.setPrecision(0.1);
        s2.read(m2);
//        s2.solveAll();

//        Assert.assertEquals("not same number of solutions", s1.getNbSolutions(), s2.getNbSolutions());
    }

    @Test
    public void test41(){
        RealVariable x = new RealVariable("x", -.1, .1);
        RealVariable y = new RealVariable("y", -.1, .1);
        RealVariable z = new RealVariable("z", -.1, .1);
        Constraint c1= geq(z, minus(x, y));

        Model m1 = new CPModel();
        m1.addConstraint(c1);

        Model m2 = new CPModel();
        m2.addVariables(x,y,z);
        m2.addConstraint(c1);


        Solver s1 = new CPSolver();
        s1.setPrecision(0.1);
        s1.read(m1);
        s1.solveAll();

        Solver s2 = new CPSolver();
        s2.setPrecision(0.1);
        s2.read(m2);
        s2.solveAll();

        Assert.assertEquals("not same number of solutions", s1.getNbSolutions(), s2.getNbSolutions());
    }

    @Test
    public void test42(){
        RealVariable x = new RealVariable("x", 0, .05);
        RealVariable y = new RealVariable("y", -.05, .1);
        RealVariable z = new RealVariable("z", 0, .05);
        Constraint c1= geq(z, minus(x, y));

        Model m1 = new CPModel();
        m1.addConstraint(c1);

        Model m2 = new CPModel();
        m2.addVariables(x,y,z);
        m2.addConstraint(c1);


        Solver s1 = new CPSolver();
        s1.setPrecision(0.1);
        s1.read(m1);
        s1.solveAll();

        Solver s2 = new CPSolver();
        s2.setPrecision(0.1);
        s2.read(m2);
        s2.solveAll();

        Assert.assertEquals("not same number of solutions", s1.getNbSolutions(), s2.getNbSolutions());
    }

    @Test
    public void test5(){
        RealVariable x = new RealVariable("x", -.1, .1);
        RealVariable y = new RealVariable("y", -.1, .1);
        RealVariable z = new RealVariable("z", -.1, .1);
        Constraint c1= geq(x, minus(y, z));
        Constraint c2= leq(x, minus(y, z));

        Model m1 = new CPModel();
        m1.addConstraint(c1);

        Model m2 = new CPModel();
        m2.addConstraint(c2);


        Solver s1 = new CPSolver();
        s1.setPrecision(0.01);
        s1.read(m1);
        s1.solveAll();

        Solver s2 = new CPSolver();
        s2.setPrecision(0.01);
        s2.read(m2);
        s2.solveAll();

        Assert.assertEquals("not same number of solutions", s1.getNbSolutions(), s2.getNbSolutions());
    }
}
