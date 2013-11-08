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
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.JumpExplanation;
import choco.ecp.solver.search.cbj.JumpContradictionException;
import choco.ecp.solver.variables.integer.ExplainedIntVar;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solution;
import choco.kernel.solver.search.AbstractGlobalSearchLimit;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.logging.Level;

// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************


/**
 * Benders search in case of problem of the form P_{xy} (
 * an optimization function on the variable of the master AND slaves)
 * assuming that the master provide a relaxation of the whole problem
 */
public class MasterSlaveOptimizer extends MasterOptimizer {

  /**
   * store the optimal solution of each subproblem
   */
  protected int[] subOptSol;

  /**
   * store the objectives variables of each subproblem
   */
  protected IntDomainVar[] subOptVar;


  // TODO : allow some/all of the subproblems to be objectives free
  public MasterSlaveOptimizer(IntDomainVar mobj, IntDomainVar[] objs, boolean maximize, MasterSlavesRelation relation) {
    super(mobj, objs.length, relation);
    //this.master = new SubSearchStrategy(objs[0].getProblem(),false);
    this.master = new SubOptimizer(mobj, maximize, false);
    this.subproblems = new SubOptimizer(objs[0], maximize, true);
    this.maximize = maximize;
    this.subOptVar = objs;
    this.subOptSol = new int[objs.length];
    zobjective = mobj;
  }


  public void solveSubProblems() {
    boolean boundReached = false;
    if (logger.isLoggable(Level.FINE)) logMasterSolution();
    for (int i = 0; i < ((BendersSolver) solver).getNbSubProblems(); i++) { // solve the subproblems
      boundReached = goToSubProblem(i);
      if (!boundReached) {
        solver.worldPush(); // push a world
        Boolean subres = subproblems.nextOptimalSolution(masterWorld);
        if (subres != null) {
          bendersCut[i] = ((BendersSolver) solver).getContradictionExplanation();
          ((JumpExplanation) bendersCut[i]).delete(masterWorld + 1);  // remove the world pushed between master and slaves
          storeCuts(bendersCut[i], i);
          if (subres == Boolean.TRUE) {
            storePartialSolution(i);
            nbFeasibleProblems += 1;
          } else if (subres == Boolean.FALSE) {
            if (((JumpExplanation) bendersCut[i]).nogoodSize() == 0)
              feasible = false;
          }
        } // sinon un limite a �t� atteinte
        solver.worldPopUntil(masterWorld);
        if (masterWorld == solver.getEnvironment().getWorldIndex()) // clean the state if contradiction occur at the root node of the subproblem
          solver.getPropagationEngine().flushEvents();
      } else {
        bendersCut[i] = ((BendersSolver) solver).getContradictionExplanation();
        storeCuts(bendersCut[i], i);
        break;
      }
    }
  }


  public boolean goToSubProblem(int i) {
    ((SubOptimizer) subproblems).changeGoal(subgoals[i], subOptVar[i]);
    try { // speedup the resolution of subproblems by updating their bounds (not mandatory)
      if (maximize && i == ((BendersSolver) solver).getNbSubProblems()) {
        ((ExplainedIntVar) subOptVar[i]).updateInf(targetLowerBound - decomposition.computeBound(zobjective.getVal(), subOptSol, i), -1,
            ((BendersSolver) solver).makeExplanation());
      } else if (!maximize) {
        //System.out.println(targetUpperBound + "jkjkljkljlkj " + (targetUpperBound - decomposition.computeBound(subOptSol, i + 1)));
        ((ExplainedIntVar) subOptVar[i]).updateSup(targetUpperBound - decomposition.computeBound(zobjective.getVal(), subOptSol, i), -1,
            ((BendersSolver) solver).makeExplanation());
      }
    } catch (JumpContradictionException e) {
      if (logger.isLoggable(Level.FINE))
        logger.fine("Contradiction while updating the bound of subpb n�" + i + " to " + (targetUpperBound - decomposition.computeBound(zobjective.getVal(), subOptSol, i)));
      ((BendersSolver) solver).setContradictionExplanation(e.getExplanation());
      return true;
    } catch (ContradictionException e) {
      throw new Error("contradiction exception in goToSubProblem " + e);
    }
    return false;
  }

  public void storePartialSolution(int subpb) {
    super.storePartialSolution(subpb);
    subOptSol[subpb] = subOptVar[subpb].getVal();
  }

  public void manageCuts() {
    super.manageCuts();
    if (nbFeasibleProblems == ((BendersSolver) solver).getNbSubProblems()) {
      int zbound = decomposition.computeBound(zobjective.getVal(), subOptSol, 0);
      if (updateTargetBound(zbound)) {
        objective = zbound;
        restorePartialSolutions();
        recordSolution();
        printBestSol();
        if (logger.isLoggable(Level.FINE))
          logBestSol();
      }
    }
    resetSubPbData();
  }

  protected Solution makeSolutionFromCurrentState() {
    Solution sol = super.makeSolutionFromCurrentState();
    sol.recordIntObjective(objective);
    return sol;
  }

  public void resetSubPbData() {
    nbFeasibleProblems = 0;
    for (int i = 0; i < subOptSol.length; i++) {
      subOptSol[i] = -1;
    }
  }

  public boolean updateTargetBound(int globalbound) {
    if (maximize && globalbound >= targetLowerBound) {
      targetLowerBound = globalbound + 1;
      return true;
    } else if (!maximize && globalbound <= targetUpperBound) {
      targetUpperBound = globalbound - 1;
      return true;
    }
    return false;
  }

  public void nextMasterMove() {
    master.nextMove = INIT_SEARCH;
    master.traceStack = new ArrayList();
    master.solutions.clear();
    master.currentTraceIndex = -1;
    fail = null;
    solver.worldPopUntil(baseWorld);
    solver.worldPush();
    ((SubOptimizer) master).initBounds();
    try {
      postKnownBound();
    } catch (ContradictionException e) {
      stop = true;
    }
    cuts.constAwake(true);
  }

  public void postKnownBound() throws ContradictionException {
    if (maximize) {
      Explanation expl = new JumpExplanation(this.solver);
      ((ExplainedIntVar) zobjective).updateInf(objective + 1, -1, expl);
    } else {
      Explanation expl = new JumpExplanation(this.solver);
      ((ExplainedIntVar) zobjective).updateSup(objective - 1, -1, expl);
    }
  }

  // ----------------------------------------------------
  // ------------------- logs ---------------------------
  // ----------------------------------------------------

  public void printBestSol() {
    System.out.print("... global solution with costs");
    for (int i = 0; i < subOptSol.length; i++) {
      System.out.print(" " + subOptVar[i] + ":" + subOptSol[i]);
    }
    System.out.print(" = " + objective + " - ");
    for (int i = 0; i < limits.size(); i++) {
      AbstractGlobalSearchLimit lim = (AbstractGlobalSearchLimit) limits.get(i);
      System.out.print(lim.pretty() + " ");
    }
    System.out.println("");
  }


  public void logBestSol() {
    String ms = "";
    ms += ("... global solution with costs");
    for (int i = 0; i < subOptSol.length; i++) {
      ms += (" " + subOptVar[i] + ":" + subOptSol[i]);
    }
    ms += (" = " + objective + " - ");
    for (int i = 0; i < limits.size(); i++) {
      AbstractGlobalSearchLimit lim = (AbstractGlobalSearchLimit) limits.get(i);
      ms += (lim.pretty() + " ");
    }
    logger.fine(ms);
  }
}
