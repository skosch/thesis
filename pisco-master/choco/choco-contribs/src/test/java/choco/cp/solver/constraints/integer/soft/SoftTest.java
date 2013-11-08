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
package choco.cp.solver.constraints.integer.soft;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.matching.AllDifferent;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.Test;

import java.util.logging.Logger;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 9 nov. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*/
public class SoftTest {

    static Logger LOGGER = ChocoLogging.getTestLogger();

    @Test
    public void test1(){
        Solver s = new CPSolver();
        IntDomainVar v1 = s.createEnumIntVar("v1", 1, 5);
        IntDomainVar v2 = s.createEnumIntVar("v2", 1, 5);
        IntDomainVar dist = s.createBooleanVar("dist");

        AbstractIntSConstraint eq = (AbstractIntSConstraint)s.eq(v1, v2);
        SoftIntSConstraint softC = new SoftIntSConstraint(dist, eq);

        s.post(softC);

        s.solveAll();
    }

    @Test
    public void test2(){
        Solver s = new CPSolver();
        IntDomainVar v1 = s.createEnumIntVar("v1", 1, 5);
        IntDomainVar v2 = s.createEnumIntVar("v2", 1, 5);
        IntDomainVar dist = s.createBooleanVar("dist");

        AbstractIntSConstraint neq = (AbstractIntSConstraint)s.neq(v1, v2);
        SoftIntSConstraint softC = new SoftIntSConstraint(dist, neq);

        s.post(softC);
        s.solveAll();
    }

    @Test
    public void test3(){
        Solver s = new CPSolver();
        int n = 3;
        IntDomainVar[] vars = new IntDomainVar[n];
        for(int i = 0; i < vars.length; i++){
            vars[i] = s.createEnumIntVar("v1", 1, n);
        }
        IntDomainVar dist = s.createBooleanVar("dist");

        AbstractIntSConstraint allDiff = new AllDifferent(vars, s.getEnvironment());
        SoftIntSConstraint softC = new SoftIntSConstraint(dist, allDiff);

        s.post(softC);
        s.solveAll();
    }

    @Test
    public void test4(){
        Solver s = new CPSolver();
        int n = 10;
        IntDomainVar[] vars = new IntDomainVar[n];
        for(int i = 0; i < vars.length; i++){
            vars[i] = s.createEnumIntVar("v_"+i, 1, n);
        }
        IntDomainVar[] dists = new IntDomainVar[n/2+1];
        for(int i = 0; i < dists.length; i++){
            dists[i] = s.createEnumIntVar("d_"+i, 0,1);
        }

        int k =0;
        AbstractIntSConstraint allDiff = new AllDifferent(vars, s.getEnvironment());
        SoftIntSConstraint softC = new SoftIntSConstraint(dists[k++], allDiff);
        s.post(softC);


        for(int i = 0; i < n ; i+=2){
            AbstractIntSConstraint eq = (AbstractIntSConstraint)s.eq(vars[i], vars[i+1]);
            SoftIntSConstraint seq = new SoftIntSConstraint(dists[k++],eq);
            s.post(seq);
        }

        IntDomainVar obj = s.createEnumIntVar("obj", 0, n);
        s.post(s.eq(obj, s.sum(dists)));

       // System.out.println(s.pretty());
        s.minimize(obj, true);
    }

}
