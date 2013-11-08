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

package choco.ecp.solver.search.benders;

import choco.ecp.solver.BendersSolver;
import choco.ecp.solver.MasterSlavesRelation;
import choco.ecp.solver.explanations.dbt.JumpExplanation;
import choco.kernel.solver.Solver;

import java.util.logging.Level;
import java.util.logging.Logger;

// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

/**
 * Benders search with approximated (not independent) substructures
 * in case of satisfaction problems.
 * <p/>
 * Provide a specific implementation of the resolution of
 * subproblems by fusionning branching of previous feasible subproblems.
 * Otherwise, it attempts to derive disjoint cuts on each subproblem.
 */
public class ApproximateMaster extends MasterGlobalSearchStrategy {


  public ApproximateMaster(Solver pb, int nbsub, MasterSlavesRelation relation) {
    super(pb, nbsub, relation);
  }

  public ApproximateMaster(Solver solver, int nbsub) {
    super(solver, nbsub);
  }

  public void solveSubProblems() {
    for (int i = 0; i < ((BendersSolver)solver).getNbSubProblems(); i++) { // solve the subproblems
      if (i > 0 && nbCutLearned == 0) { // tant que le sous-probl�me est faisable, "�largir son branching"
        if (logger.isLoggable(Level.FINE))
          logger.fine("FUSION SUBPB " + i + " with SUBPB " + (i - 1));
        Logger.getLogger("choco").getHandlers()[0].flush();
        subproblems.fusionGoal(subgoals[i]);
      } else {
        solver.worldPush();
        if (logger.isLoggable(Level.FINE))
          logger.fine("START SUBPB " + i);
        // une fois qu'on a une contradiction, n'examiner que les sous-probl�mes un � un
        subproblems.changeGoal(subgoals[i]);
      }
      Boolean sol = subproblems.nextOptimalSolution(masterWorld);
      if (sol == Boolean.FALSE) {
        fail = ((BendersSolver)solver).getContradictionExplanation();
        ((JumpExplanation) fail).delete(masterWorld + 1);
        if (((JumpExplanation) fail).nogoodSize() == 0) feasible = false;
        storeCuts(fail, i);
      } else if (sol == Boolean.TRUE && nbCutLearned == 0) {
        storePartialSolution(0, i);
      } else if (sol == null) {
        feasible = false;
      }
      if (nbCutLearned != 0 || !feasible)
        solver.worldPopUntil(masterWorld);
      if (!feasible) break;
    }
  }

  public void storePartialSolution(int firstSpb, int lastSpb) {
    for (int i = firstSpb; i <= lastSpb; i++) {
      super.storePartialSolution(i);
    }
  }
}
