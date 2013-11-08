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

package choco.ecp.solver.search.cbj;

import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.JumpExplanation;
import choco.ecp.solver.variables.integer.ExplainedIntVar;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.search.AbstractGlobalSearchLimit;
import choco.kernel.solver.variables.integer.IntDomainVar;

// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

public class JumpAbstractOptimizer extends JumpGlobalSearchStrategy {
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
   * constructor
   *
   * @param obj      the objective variable
   * @param maximize maximization or minimization ?
   */
  protected JumpAbstractOptimizer(IntDomainVar obj, boolean maximize) {
    super(obj.getProblem());
    objective = (ExplainedIntVar) obj;
    doMaximize = maximize;
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

  /**
   * TODO
   */
  public void recordSolution() {
    problem.feasible = Boolean.TRUE;
    //nbSolutions = nbSolutions + 1;
    // trace(SVIEW,"... solution with ~A:~S [~S]\n",obj.name,objval,a.limits),  // v1.011 <thb>
    StringBuffer b = new StringBuffer();
    b.append("... solution with cost ").append(objective)
        .append(": ").append(objective.getVal()).append("   ");
    for (int i = 0; i < limits.size(); i++) {
      AbstractGlobalSearchLimit lim = (AbstractGlobalSearchLimit) limits.get(i);
      b.append(lim.pretty()).append(" ");
    }
    AbstractSolver.logger.info(b.toString());
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
    if (problem.feasible != Boolean.TRUE) {
      // trace(STALK,"search first sol ...")
    } else {
      // trace(STALK,"search target: ~A >= ~S ... ",a.objective.name,newBound))
      targetLowerBound = newBound;
    }
  }

  protected void setTargetUpperBound() {
    int newBound = upperBound - 1;
    if (problem.feasible != Boolean.TRUE) {
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
      postTargetLowerBound();
    } else {
      postTargetUpperBound();
    }
  }

  public void postTargetLowerBound() throws ContradictionException {
    Explanation expl = new JumpExplanation(this.problem);
    ((JumpExplanation) expl).add(1, problem.getWorldIndex());
    objective.updateInf(targetLowerBound, -1, expl);
    //objective.setInf(targetLowerBound);
  }

  public void postTargetUpperBound() throws ContradictionException {
    Explanation expl = new JumpExplanation(this.problem);
    ((JumpExplanation) expl).add(1, problem.getWorldIndex());
    objective.updateSup(targetUpperBound, -1, expl);
    //objective.setSup(targetUpperBound);
  }

  /**
   * we use  targetBound data structures for the optimization cuts
   */
  public void postDynamicCut() throws ContradictionException {
    postTargetBound();
    //problem.propagate();
  }
}
