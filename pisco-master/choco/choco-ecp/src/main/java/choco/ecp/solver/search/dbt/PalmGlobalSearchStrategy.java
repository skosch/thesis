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

//     ___  ___         PaLM library for Choco system
//    /__ \/ __\        (c) 2001 - 2004 -- Narendra Jussien
//   ____\ |/_____
//  /_____\/_____ \     PalmExplanation based constraint programming tool
// |/   (_)(_)   \|
//       \ /            Version 0.1
//       \ /            January 2004
//       \ /
// ______\_/_______     Contibutors: Fran?ois Laburthe, Hadrien Cambazard, Guillaume Rochart...

package choco.ecp.solver.search.dbt;

import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.constraints.PalmSConstraint;
import choco.ecp.solver.explanations.dbt.PalmConstraintPlugin;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.propagation.dbt.PalmEngine;
import choco.ecp.solver.search.dbt.pathrepair.PathRepairLearn;
import choco.ecp.solver.variables.PalmVar;
import choco.ecp.solver.variables.integer.dbt.PalmIntDomain;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import choco.kernel.solver.search.GlobalSearchLimit;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A default solver for Palm. By default, it uses <code>mac-dbt</code> algorithm (for extension and repairing).
 */

public class PalmGlobalSearchStrategy extends AbstractGlobalSearchStrategy {

  public static final int LIMIT_TIME = 0;
  public static final int LIMIT_NODES = 1;

  /**
   * States if the search is finished.
   */

  protected boolean finished = false;


  /**
   * States if the problem is feasible.
   */

  protected boolean feasible = false;


  /**
   * Maintains the state of the search (past branching decisions).
   */

  protected PalmState state;


  /**
   * Learning algorithms.
   */

  protected PalmLearn learning;

  /**
   * Extension algrithm using some branching strategies.
   */

  protected PalmExtend extending;


  /**
   * Repairing algorithm.
   */

  protected PalmRepair repairing;


  /**
   * Creates a solver for the specified problem. It initiliazes all contained structures (repairer, learner...).
   *
   * @param pb The problem to search.
   */

  public PalmGlobalSearchStrategy(Solver pb) {
    super(pb);
  }

    public void newTreeSearch() {
    reset();
    nbSolutions = 0;
  }

  public void endTreeSearch() {
      reset();
    if (LOGGER.isLoggable(Level.SEVERE)) {
      if (solver.isFeasible()== Boolean.TRUE) {
        LOGGER.log(Level.INFO, "solve => " + nbSolutions + " solutions");
      } else {
        LOGGER.info("solve => no solution");
      }
      printRuntimeStatistics();
    }
  }

  /**
   * Resets the solver (statistics and business data).
   */

  public void reset() {
    this.finished = false;
    ((PalmEngine) this.solver.getPropagationEngine()).resetDummyVariable();
    limitManager.reset();
    //for (int i = 0; i < 3; i++) {
    //  this.setRuntimeStatistic(i, 0);
    //}
  }

  /**
   * Attaches a new PalmState to the solver.
   *
   * @param ext The PalmState that should be used for maintaining state of the search.
   */

  public void attachPalmState(PalmState ext) {
    this.state = ext;
    ext.setManager(this);
  }


  /**
   * Attaches a new extension tool.
   */

  public void attachPalmExtend(PalmExtend ext) {
    this.extending = ext;
    ext.setManager(this);
  }


  /**
   * Attaches a new list of branching. It creates a linked list from the specified list of branching.
   */

  public void attachPalmBranchings(List lbr) {
    PalmAbstractBranching previous = null;
    for (Iterator iterator = lbr.iterator(); iterator.hasNext();) {
      PalmAbstractBranching branching = (PalmAbstractBranching) iterator.next();
      if (previous != null) previous.setNextBranching(branching);
      branching.setExtender(this.extending);
      previous = branching;
    }
    if (lbr.size() > 0) this.extending.setBranching((PalmAbstractBranching) lbr.get(0));
  }

  /**
   * Attache a new learning tool.
   *
   * @param ext The learning extension the solver shoudl use.
   */

  public void attachPalmLearn(PalmLearn ext) {
    this.learning = ext;
    ext.setManager(this);
  }


  /**
   * Attaches a new PalmRepair algorithm.
   *
   * @param ext The new repairing algorithm the solver must use.
   */

  public void attachPalmRepair(PalmRepair ext) {
    this.repairing = ext;
    ext.setManager(this);
  }

  /**
   * Chechs if the solver has finished searching a solution.
   */

  public boolean isFinished() {
    return finished;
  }


  /**
   * Sets if the solver has finished searching a solution.
   *
   * @param f New value.
   */

  public void setFinished(boolean f) {
    this.finished = f;
  }


  /**
   * Stores the current solution.
   */

  public void recordSolution() {
    if (this.solver.isFeasible()) {
      PalmSolution solution = new PalmSolution(this.solver);

      for (int i = 0; i < this.solver.getNbIntVars(); i++) {
        solution.recordIntValue(i, ((IntDomainVar) this.solver.getIntVar(i)).getInf());
      }

      if (this instanceof PalmBranchAndBound) {
        // TODO : il faut rendre ce generique pour entier ou flottant...
        //solution.recordIntObjective(((PalmBranchAndBound) this).getObjectiveValue());
      }
      if (learning instanceof PathRepairLearn) { // When a solveAll is called with decision repair, we store the solution as a nogood
        ((PathRepairLearn) learning).addSolution();
      }
      for (int i = 0; i < limitManager.limits.size(); i++) {
        solution.recordStatistic(i, limitManager.limits.get(i).getNb());
      }

      this.getSolutionPool().recordSolution(solver);
      this.nbSolutions += 1;
    }
  }


  /**
   * Starts solving.
   */

  public void incrementalRun() {
    try {
      this.finished = false;
      try {
        this.solver.propagate();
      } catch (PalmContradiction e) {
        this.repair();
      }
      while (!this.finished) {
        try {
          if (Logger.getLogger("choco").isLoggable(Level.FINE))
            Logger.getLogger("choco").fine("New extension launched.");
          this.extend();
          this.solver.propagate();
        } catch (PalmContradiction e) {
          this.repair();
        }
      }
      this.solver.setFeasible(Boolean.TRUE);
      this.recordSolution();
    } catch (ContradictionException e) {
      this.finished = true;
      //this.problem.feasible = Boolean.FALSE;
    }
  }


  /**
   * Extends the state of the search thanks to the extending algorithm.
   *
   * @throws ContradictionException
   */

  public void extend() throws ContradictionException {
    this.extending.explore(this.extending.getBranching());
  }


  /**
   * Tries to repair the state for finding a consistent state.
   *
   * @throws ContradictionException
   */

  public void repair() throws ContradictionException {
    PalmEngine pe = (PalmEngine) this.solver.getPropagationEngine();
    if (pe.isContradictory()) {
      PalmVar cause = (PalmVar) pe.getContradictionCause();
      PalmExplanation expl = (PalmExplanation) ((PalmSolver) this.solver).makeExplanation();


      cause.self_explain(PalmIntDomain.DOM, expl);
      if (Logger.getLogger("choco").isLoggable(Level.FINE)) {
        Logger.getLogger("choco").fine("Repairing");
        Logger.getLogger("choco").fine("Cause : " + cause);
        Logger.getLogger("choco").fine("Expl : " + expl);
      }

      pe.setContradictory(false);
      this.learning.learnFromContradiction(expl);
      if (expl.isEmpty()) {
        pe.raiseSystemContradiction();
      } else {
        PalmSConstraint constraint = (PalmSConstraint) this.repairing.selectDecisionToUndo(expl);
        if (constraint != null) {
          if (((PalmConstraintPlugin) constraint.getPlugIn()).getWeight() <= ((PalmSolver) this.solver).getMaxRelaxLevel()) {
            //this.incRuntimeStatistic(PalmSolver.RLX, 1);
            endTreeNode();
            if (((PalmConstraintPlugin) constraint.getPlugIn()).getWeight() > 0) {
            } else {
              this.state.removeDecision(constraint);
            }

            int timeStamp = PalmConstraintPlugin.getLastTimeStamp();
            try {
              ((PalmSolver) this.solver).remove(constraint);
              this.solver.propagate();
            } catch (PalmContradiction e) {
              this.repair();
            }

            if (((PalmConstraintPlugin) constraint.getPlugIn()).getWeight() == 0) {
              // Negation
              expl.delete(constraint);

              SConstraint negCons = ((DecisionSConstraint) constraint).negate();
              if (negCons != null) {
                if (expl.isValid(timeStamp)) {
                  expl.clear();
                  try {
                    if (Logger.getLogger("choco").isLoggable(Level.FINE))
                      Logger.getLogger("choco").fine("Negation posted and propagated.");
                    ((PalmSolver) this.solver).post(negCons, expl);
                    this.solver.propagate();
                  } catch (PalmContradiction e) {
                    this.repair();
                  }
                }
              }
            }
          } else {
            if (Logger.getLogger("choco").isLoggable(Level.INFO))
              Logger.getLogger("choco").info("Contradiction because of: " + expl);
            ((PalmSolver) solver).setContradictionExplanation((PalmExplanation) expl.copy());
            pe.raiseSystemContradiction();
          }
        } else {
          if (Logger.getLogger("choco").isLoggable(Level.INFO))
            Logger.getLogger("choco").info("Contradiction because of: " + expl);
          ((PalmSolver) solver).setContradictionExplanation((PalmExplanation) expl.copy());
          pe.raiseSystemContradiction();
        }
      }
    }
  }


  /**
   * Gets the PalmState tool.
   */

  public PalmState getState() {
    return state;
  }


  /**
   * Gets the PalmLearn tool.
   */

  public PalmLearn getLearning() {
    return learning;
  }

  /**
   * Gets the PalmExtend tool.
   */

  public PalmExtend getExtending() {
    return extending;
  }

  public int getTimeLimit() {
    return limitManager.limits.get(LIMIT_TIME).getNbMax();
  }

  public void setTimeLimit(int timeLimit) {
    limitManager.limits.get(LIMIT_TIME).setNbMax(timeLimit);
  }

  public int getNodeLimit() {
    return limitManager.limits.get(LIMIT_NODES).getNbMax();
  }

  public void setNodeLimit(int nodeLimit) {
    limitManager.limits.get(LIMIT_NODES).setNbMax(nodeLimit);
  }

  public GlobalSearchLimit getLimit(int i) {
    return limitManager.limits.get(i);
  }

  public int getNbLimit() {
    return limitManager.limits.size();
  }
}
