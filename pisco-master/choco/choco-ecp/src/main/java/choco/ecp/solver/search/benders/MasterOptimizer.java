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
 * Benders search in case of problem of the form P_{y} (
 * an optimization function on the variable of the master)
 */
public class MasterOptimizer extends MasterGlobalSearchStrategy {

  /**
   * a tentative upper bound
   */
  protected int targetUpperBound = Integer.MAX_VALUE;

  /**
   * a tentative lower bound
   */
  protected int targetLowerBound = Integer.MIN_VALUE;

  /**
   * maximize or minimize the objective function
   */
  protected boolean maximize;

  /**
   * best value found
   */
  protected int objective;

  /**
   * Objective variable of the master
   */
  protected IntDomainVar zobjective;

  public MasterOptimizer(IntDomainVar obj, int nbSub, MasterSlavesRelation relation) {
    super(obj.getSolver(), nbSub, relation);
  }

  // TODO : allow some/all of the subproblems to be objectives free
  public MasterOptimizer(IntDomainVar obj, int nbSub, boolean maximize) {
    super(obj.getSolver(), nbSub);
    this.master = new SubOptimizer(obj, maximize, true);
    this.subproblems = new SubSearchStrategy(solver, true);
    this.maximize = maximize;
    zobjective = obj;
  }


  public int getOptimumValue() {
    return objective;
  }


  public void storePartialSolution(int subpb) {
    super.storePartialSolution(subpb);
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
    cuts.constAwake(true);
  }

  public void solutionFound() {
    logSolution();
    restorePartialSolutions();
    recordSolution();
    objective = zobjective.getVal();
    printBestSol();
    cleanPartialSolutions();
  }
  // ----------------------------------------------------
  // ------------------- logs ---------------------------
  // ----------------------------------------------------

  public void logMasterSolution() {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("New master solution");
      IntDomainVar[] mvs = ((BendersSolver) solver).getMasterVariables();
      for (int i = 0; i < mvs.length; i++) {
        logger.fine(mvs[i] + ":" + mvs[i].getVal());
      }
      //Logger.getLogger("i_want_to_use_this_old_version_of_choco.palm.benders").fine("z = " + objective);
    }
  }


  public void printBestSol() {
    System.out.print("... global solution with costs");
    System.out.print(" = " + objective + " - ");
    for (int i = 0; i < limits.size(); i++) {
      AbstractGlobalSearchLimit lim = (AbstractGlobalSearchLimit) limits.get(i);
      System.out.print(lim.pretty() + " ");
    }
    System.out.println("");
  }
}
