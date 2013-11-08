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

package choco.model.variables.integer;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

import static choco.Choco.makeIntVar;
import static choco.Choco.neq;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 20 nov. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*/
public class LinkedListTest {

    @Test
     public void test_patakm1() {
        Model m = new CPModel();
        IntegerVariable[] arr = new IntegerVariable[1];
        int[] values = {-20000000, 20000000};
        for (int i = 0; i < arr.length; i++) {
            arr[i] = makeIntVar("", values, Options.V_LINK);
            m.addConstraint(neq(arr[i], 1));
        }

        Solver s = new CPSolver();
        s.read(m);
        s.solveAll();
        Assert.assertEquals(2, s.getSolutionCount());
    }

    @Test
     public void test_patakm2() {
        String option = Options.V_LINK;
        for(int i = 0; i < 1000; i++){
            Random r = new Random(i);
            Model m = new CPModel();
            int lb = r.nextInt(100) * (r.nextBoolean()?1:-1);
            int ub = lb+ r.nextInt(100);
            boolean isArray = r.nextBoolean();
            IntegerVariable link = (isArray?Choco.makeIntVar("v", new int[]{lb, ub}, option):Choco.makeIntVar("v", lb, ub, option));
            m.addVariable(link);
            Solver s = new CPSolver();
            s.read(m);
            s.solveAll();
            Assert.assertEquals("["+lb+","+ (isArray?"...":"")+ub+"]", isArray?(ub-lb==0?1:2):(ub-lb+1), s.getNbSolutions());
        }
    }
}
