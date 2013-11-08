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

package choco.solver.search;

import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.DomOverWDegBranching;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.varselector.DomOverWDeg;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.limit.Limit;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 11 avr. 2008
 * Time: 18:26:25
 */
public class HeuristicTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

  @Test
  public void testDomWdeg() {
//    int nb1 = testHeuristic(0);
//    int nb2 = testHeuristic(1);
//    int nb3 = testHeuristic(2);
    testHeuristic(3);
//    LOGGER.info(nb1 + " " + nb2 + " " + nb3);
//    assertTrue(nb1 >= nb2);
  }

  public int testHeuristic(int domWdeg) {
    long start = System.currentTimeMillis();
    Model m = new CPModel();
    IntegerVariable[] vars = makeIntVarArray("vtabA", 6, 0, 1);
    IntegerVariable[] vars2 = makeIntVarArray("vtabB", 5, 0, 3);
    for (int i = 0; i < vars2.length; i++) {
      for (int j = i + 1; j < vars2.length; j++) {
        m.addConstraint(neq(vars2[i], vars2[j]));
      }
    }
    m.addConstraint(eq(3, sum(vars)));
    Solver s = new CPSolver();
    s.read(m);
    if (domWdeg == 0) {
      s.setVarIntSelector(new MinDomain(s));
    } else if (domWdeg == 1) {
      s.setVarIntSelector(new DomOverWDeg(s));
    } else {
      s.attachGoal(new DomOverWDegBranching(s, new IncreasingDomain()));
    }

    s.setFirstSolution(true);
    s.monitorBackTrackLimit(true);
    s.monitorFailLimit(true);
    
    if (domWdeg == 3) {
            ( (CPSolver) s).setGeometricRestart(14, 1.5);
            ( (CPSolver) s).getConfiguration().putEnum(Configuration.RESTART_POLICY_LIMIT, Limit.NODE);
//            
//    		s.getSearchStrategy().setSearchLoop(new SearchLoopWithRestart(s.getSearchStrategy(),
//          new RestartStrategy() {
//            int nodesLimit = 14;
//            double mult = 1.5;
//
//            public boolean shouldRestart(AbstractGlobalSearchStrategy search) {
//              boolean shouldRestart =  (search.getSearchMeasures().getNodeCount() >= nodesLimit);
//              if (shouldRestart) {
//				nodesLimit *= mult;
//			}
//              return shouldRestart;
//            }
//          }));
    }
    s.generateSearchStrategy();
    s.launch();
    assertTrue(!s.isFeasible());
    int nb = s.getNodeCount();
    long delta = System.currentTimeMillis() - start;
    LOGGER.info(nb + " nodes in " + delta + " ms");
    s.printRuntimeStatistics();
    return nb;
  }
}