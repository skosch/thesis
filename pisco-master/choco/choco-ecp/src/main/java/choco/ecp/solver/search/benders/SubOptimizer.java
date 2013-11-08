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
import choco.ecp.solver.JumpSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.JumpExplanation;
import choco.ecp.solver.variables.integer.ExplainedIntVar;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.branch.AbstractIntBranching;
import choco.kernel.solver.search.AbstractGlobalSearchLimit;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import choco.kernel.solver.search.IntBranchingTrace;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
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
 * A searchsolver for optimization
 */
public class SubOptimizer extends SubSearchStrategy {

  /**
   * a boolean indicating whether we want to maximize (true) or minize (false) the objective variable
   */
  public boolean doMaximize;
  /**
   * the variable modelling the objective value
   */
  public ExplainedIntVar objective;
  /**
   * the lower bound of the objective value.
   * This value comes from the problem definition; it is strengthened by the search history (solutions found & no-goods)
   */
  public int lowerBound = Integer.MIN_VALUE;
  /**
   * the upper bound of the objective value
   * This value comes from the problem definition; it is strengthened by the search history (solutions found & no-goods)
   */
  public int upperBound = Integer.MAX_VALUE;

  /**
   * a tentative upper bound
   */
  public int targetUpperBound = Integer.MAX_VALUE;

  /**
   * a tentative lower bound
   */
  public int targetLowerBound = Integer.MIN_VALUE;

  /**
   * Master solution
   */
  protected int[] msol;


  /**
   * constructor
   *
   * @param obj      the objective variable
   * @param maximize maximization or minimization ?
   */
  protected SubOptimizer(IntDomainVar obj, boolean maximize, boolean slave) {
    super(obj.getSolver(), slave);
    if (!slave)
      msol = new int[((BendersSolver) obj.getSolver()).getMasterVariablesList().size()];
    objective = (ExplainedIntVar) obj;
    doMaximize = maximize;
    stopAtFirstSol = false;
  }

  protected void changeGoal(AbstractIntBranching branching, IntDomainVar newObjective) {
    super.changeGoal(branching);
    objective = (ExplainedIntVar) newObjective;
    solutions.clear();
    initBounds();
  }

  public Boolean nextOptimalSolution(int masterWorld) {
    while (nextSolution() == Boolean.TRUE);
    if ((maxNbSolutionStored > 0) && existsSolution()) {
      if (!slave) {
        solver.worldPopUntil(baseWorld);
        restoreBestSolutionBySearch();
      } else {
        solver.worldPopUntil(masterWorld);
        restoreBestSolution();
      }
      return Boolean.TRUE;
    } else if (isEncounteredLimit())
      return null;
    else
      return Boolean.FALSE;
  }


  public void solutionFound(IntBranchingTrace ctx) {
    recordSolution();
    currentFail = ((BendersSolver) solver).makeExplanation();
    ((JumpExplanation) currentFail).add(1, solver.getWorldIndex());
    if (!slave) {
      storeMasterSolution();
    }
    nextMove = AbstractGlobalSearchStrategy.UP_BRANCH;
  }

  /**
   * v1.0 accessing the objective value of an optimization problem
   * (note that the objective value may not be instantiated, while all other variables are)
   *
   * @return the current objective value
   */
  public int getObjectiveValue() {
    if (doMaximize) {
      return objective.getSup();
    } else {
      return objective.getInf();
    }
  }

  public int getBestObjectiveValue() {
    if (doMaximize) {
      return lowerBound;
    } else {
      return upperBound;
    }
  }

  /**
   * the target for the objective function: we are searching for a solution at least as good as this (tentative bound)
   */
  public int getObjectiveTarget() {
    if (doMaximize) {
      return targetLowerBound;
    } else {
      return targetUpperBound;
    }
  }

  /**
   * initialization of the optimization bound data structure
   */
  public void initBounds() {
    lowerBound = objective.getInf();
    upperBound = objective.getSup();
    targetLowerBound = objective.getInf();
    targetUpperBound = objective.getSup();
  }

  public void recordSolution() {
    logIntermediateSol();
    solver.setFeasible(Boolean.TRUE);
    setBound();
    setTargetBound();
    super.recordSolution();
  }

  /**
   * resetting the optimization bounds
   */
  public void setBound() {
    int objval = getObjectiveValue();
    if (doMaximize) {
      lowerBound = MathUtils.max(lowerBound, objval);
    } else {
      upperBound = MathUtils.min(upperBound, objval);
    }
  }

  /**
   * resetting the values of the target bounds (bounds for the remaining search)
   */
  public void setTargetBound() {
    if (doMaximize) {
      setTargetLowerBound();
    } else {
      setTargetUpperBound();
    }
  }

  protected void setTargetLowerBound() {
    int newBound = lowerBound + 1;
    if (solver.isFeasible() != Boolean.TRUE) {
      // trace(STALK,"search first sol ...")
    } else {
      // trace(STALK,"search target: ~A >= ~S ... ",a.objective.name,newBound))
      targetLowerBound = newBound;
    }
  }

  protected void setTargetUpperBound() {
    int newBound = upperBound - 1;
    if (solver.isFeasible() != Boolean.TRUE) {
      // trace(STALK,"search first sol ...")
    } else {
      // trace(STALK,"search target: ~A <= ~S ... ",a.objective.name,newBound))
      targetUpperBound = newBound;
    }
  }

  /**
   * propagating the optimization cuts from the new target bounds
   */
  public void postTargetBound() throws ContradictionException {
    if (doMaximize) {
      postLowerBound(targetLowerBound);
    } else {
      postUpperBound(targetUpperBound);
    }
  }

  public void postKnownBound() throws ContradictionException {
    if (doMaximize) {
      Explanation expl = new JumpExplanation(this.solver);
      objective.updateInf(lowerBound, -1, expl);
    } else {
      Explanation expl = new JumpExplanation(this.solver);
      objective.updateSup(upperBound, -1, expl);
    }
  }

  public void postLowerBound(int lb) throws ContradictionException {
    Explanation expl = new JumpExplanation(this.solver);
    if (solver.getWorldIndex() >= 1)
      ((JumpExplanation) expl).add(1, solver.getWorldIndex());
    objective.updateInf(lb, -1, expl);
  }

  public void postUpperBound(int ub) throws ContradictionException {
    Explanation expl = new JumpExplanation(this.solver);
    if (solver.getWorldIndex() >= 1)
      ((JumpExplanation) expl).add(1, solver.getWorldIndex());
    objective.updateSup(ub, -1, expl);
  }

  /**
   * we use  targetBound data structures for the optimization cuts
   */
  public void postDynamicCut() throws ContradictionException {
    postTargetBound();
    //solver.propagate();
  }

  public void storeMasterSolution() {
    for (int i = 0; i < msol.length; i++) {
      msol[i] = ((IntDomainVar) ((BendersSolver) solver).getMasterVariablesList().get(i)).getVal();
    }
  }

  public void restoreBestSolutionBySearch() {
    try {
      traceStack.clear();
      ArrayList mvs = ((BendersSolver) solver).getMasterVariablesList();
      for (int i = 0; i < msol.length; i++) {
        if (!((IntDomainVar) mvs.get(i)).isInstantiated() && mvs.get(i) != objective) {
          solver.worldPush();
          ExplainedIntVar y = (ExplainedIntVar) mvs.get(i);
          Explanation exp = ((JumpSolver) solver).makeExplanation(solver.getWorldIndex());
          // new JumpExplanation(manager.solver.getWorldIndex() - 1, manager.solver);
          y.instantiate(msol[i], -1, exp);
          solver.propagate();
          IntBranchingTrace ctx = new IntBranchingTrace();
          ctx.setBranching(mainGoal);
          ctx.setBranchingObject(y);
          ctx.setBranchIndex(msol[i]);
          traceStack.add(ctx);
        }
      }
      //TODO : set and real
      solver.propagate();
      if (!objective.isInstantiated()) {
        postKnownBound();
        solver.propagate();
      }
    } catch (ContradictionException e) {
      LOGGER.severe("BUG in restoring solution !!!!!!!!!!!!!!!!");
      throw(new Error("Restored solution not consistent !!"));
    }
  }

  public void logIntermediateSol() {
    if (Logger.getLogger("choco").isLoggable(Level.FINE)) {
      String msg = "... solution with cost " + objective + ": " + objective.getVal() + "   ";
        for (AbstractGlobalSearchLimit limit : limits) {
            msg += limit.pretty() + " ";
        }
      Logger.getLogger("choco").fine(msg);
    }
  }

}
