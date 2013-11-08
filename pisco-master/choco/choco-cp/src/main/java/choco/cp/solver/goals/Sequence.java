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

package choco.cp.solver.goals;

import choco.cp.solver.CPSolver;
import choco.cp.solver.search.GoalSearchLoop;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.goals.Goal;
import choco.kernel.solver.goals.GoalType;


/*
 * Created by IntelliJ IDEA.
 * User: grochart
 * Date: 19 mai 2007
 * Since : Choco 2.0.0
 *
 */
public class Sequence implements Goal {
  protected Goal[] sequence;

  public Sequence(Goal[] goals) {
    this.sequence = goals;
  }

  public String pretty() {
    StringBuilder sb = new StringBuilder();
    sb.append("and(");
    for (int i = 0; i < sequence.length; i++) {
      if (i>0) sb.append(", ");
      Goal goal = sequence[i];
      sb.append(goal.pretty());
    }
    sb.append(")");
    return sb.toString();
  }

  public Goal execute(Solver s) throws ContradictionException {
    if(CPSolver.GOAL){
        GoalSearchSolver gsl = (GoalSearchSolver) s.getSearchStrategy();
        for (int i = sequence.length - 1; i >= 0; i--) {
          Goal goal = sequence[i];
          gsl.pushGoal(goal);
        }
    }else{

      GoalSearchLoop gsl = (GoalSearchLoop) s.getSearchStrategy().searchLoop;
        for (int i = sequence.length - 1; i >= 0; i--) {
            Goal goal = sequence[i];
            gsl.pushGoal(goal);
        }
    }
      return null;
  }

    @Override
    public GoalType getType() {
        return GoalType.SEQ;
    }
}
