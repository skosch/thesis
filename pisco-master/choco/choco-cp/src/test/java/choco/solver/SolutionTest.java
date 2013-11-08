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

package choco.solver;

import static choco.Choco.lt;
import static choco.Choco.makeIntVar;
import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solution;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.ISolutionPool;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 18 déc. 2008
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class SolutionTest {

	
	
	private final CPModel model;

	public SolutionTest() {
		super();
		model = new CPModel();
        IntegerVariable v = makeIntVar("v", 1, 10);
        model.addConstraint(lt(v, 11));
        
	}
	
	private Solver run(int capa) {
		final Solver s = new CPSolver();
        s.setSolutionPoolCapacity(capa);
        s.read(model);
        s.solveAll();
        return s;
  	
	}
	@Test
    public void test0(){
		Solver s = run(0);
        ISolutionPool pool = s.getSearchStrategy().getSolutionPool();
        assertEquals(0, pool.size());
        
        s = run(1);
        pool = s.getSearchStrategy().getSolutionPool();
        assertEquals(1, pool.size());
        
        s = run(5);
        pool = s.getSearchStrategy().getSolutionPool();
        assertEquals(5, pool.size());
        List<Solution> sols = pool.asList();
        for (int i = 0; i <5; i++) {
        	s.worldPop();
        	s.worldPush();
        	s.restoreSolution(sols.get(i));
            assertEquals(10 - i, s.getIntVar(0).getVal());
		}
        
        s = run(12);
        pool = s.getSearchStrategy().getSolutionPool();
        assertEquals(10, pool.size());
        sols = pool.asList();
        for (int i = 0; i <10; i++) {
        	s.worldPop();
        	s.worldPush();
        	s.restoreSolution(sols.get(i));
            assertEquals(10 - i, s.getIntVar(0).getVal());
		}
        
    }
    
    @Test
    public void test1(){
    	Solver s = run(1);
        ISolutionPool pool = s.getSearchStrategy().getSolutionPool();
        assertEquals(1, pool.size());
        s.createBoundIntVar("NEW", 0, 5);
    	s.restoreSolution(pool.asList().get(0));
    }
}
