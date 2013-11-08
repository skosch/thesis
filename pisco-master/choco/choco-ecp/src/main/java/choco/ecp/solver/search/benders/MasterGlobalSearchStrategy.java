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
import choco.ecp.solver.search.Assignment;
import choco.ecp.solver.search.NogoodSConstraint;
import choco.ecp.solver.search.SymbolicDecision;
import choco.ecp.solver.search.cbj.JumpGlobalSearchStrategy;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.AbstractIntBranching;
import choco.kernel.solver.search.AbstractGlobalSearchLimit;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 26 d�c. 2004
 * Time: 11:12:39
 * To change this template use File | Settings | File Templates.
 */

/**
 * Default implementation of Benders search (used for P_{0})
 * basis for {P'_{0},P_{y}, and P_{xy}
 */
public class MasterGlobalSearchStrategy extends JumpGlobalSearchStrategy {

  protected static Logger logger = Logger.getLogger("choco");

  /**
   * The nogood constraint gathering all benders cuts
   */
  protected NogoodSConstraint cuts;

  /**
   * A search solver corresponding to the master
   */
  protected SubSearchStrategy master;

  /**
   * A search solver used for the subproblems
   */
  protected SubSearchStrategy subproblems;

  /**
   * the goal corresponding to the variables of each subproblems
   */
  protected AbstractIntBranching[] subgoals;

  /**
   * store bendersCuts extracted on each subproblem
   */
  protected Explanation[] bendersCut;

  /**
   * number of cuts extracted at the current iteration
   */
  protected int nbCutLearned = 0;

  /**
   * number of feasible sub-problems found at the current iteration
   */
  protected int nbFeasibleProblems = 0;

  /**
   * Store the solutions found on sub-problems. As only one global search solver is used,
   * solutions of sub-problems need to be stored
   */
  protected ArrayList partialSol; // TODO : Should be a Solution object

  /**
   * feasability of the whole problem
   */
  protected boolean feasible = true;

  /**
   * Objective function formulated as a specific relation
   */
  protected MasterSlavesRelation decomposition;

  public MasterGlobalSearchStrategy(Solver solver, int nbsub, MasterSlavesRelation relation) {
    super(solver);
    partialSol = new ArrayList();//int[pb.getNbIntVars()];
    subgoals = new AbstractIntBranching[nbsub];
    bendersCut = new Explanation[nbsub];
    for (int i = 0; i < nbsub; i++) {
      partialSol.add(new int[((BendersSolver) solver).getSubvariablesList(i).size()]);
    }
    decomposition = relation;
  }

  public MasterGlobalSearchStrategy(Solver solver, int nbsub) {
    super(solver);
    master = new SubSearchStrategy(solver, false);
    subproblems = new SubSearchStrategy(solver, true);
    subgoals = new AbstractIntBranching[nbsub];
    bendersCut = new Explanation[nbsub];
    partialSol = new ArrayList();//int[pb.getNbIntVars()];
    for (int i = 0; i < nbsub; i++) {
      partialSol.add(new int[((BendersSolver) solver).getSubvariablesList(i).size()]);
    }
    decomposition = new MasterSlavesRelation();
  }

  /**
   * set the limit objects to both master and slaves.
   * They share the same limits.
   */
  public void updateLimit() {
    for (int i = 0; i < limits.size(); i++) {
      master.limits.add(limits.get(i));
      subproblems.limits.add(limits.get(i));
    }
  }

  /**
   * getter on the number of cuts stored (without inclusion)
   *
   * @return the number of cuts learned
   */
  public int getNbCuts() {
    return cuts.getPermanentMemorySize();
  }

  /**
   * @return the global search solver associated to the subproblems
   */
  public SubSearchStrategy getSubproblems() {
    return subproblems;
  }

  /**
   * @return the global search solver associated to the master
   */
  public SubSearchStrategy getMaster() {
    return master;
  }

  /**
   * set the branching of the master solver
   */
  public void setMainGoal(AbstractIntBranching branch) {
    master.mainGoal = branch;
  }

  /**
   * set the branching of subproblem number i
   */
  public void setSubGoal(int i, AbstractIntBranching branch) {
    subgoals[i] = branch;
  }

  /**
   * set the way BendersCut are managed
   */
  public void setCutsConstraint(NogoodSConstraint cuts) {
    this.cuts = cuts;
  }

  /**
   * return -1 as it is a satisfaction problem
   */
  public int getOptimumValue() {
    return -1;
  }

  protected Explanation fail;

  protected int masterWorld;

  protected boolean stop = false;

  public void incrementalRun() {
    baseWorld = solver.getEnvironment().getWorldIndex();
    boolean feasibleRootState = true;
    try {
      newTreeSearch();
      solver.propagate();
    } catch (ContradictionException e) {
      feasibleRootState = false;
    }
    if (feasibleRootState) {
      solver.worldPush();
      while (!stop && master.nextOptimalSolution(masterWorld) == Boolean.TRUE) {
        masterWorld = solver.getWorldIndex();
        solveSubProblems(); // solve the subproblems
        stop = (nbCutLearned == 0);
        manageCuts();                  // add the benders cuts to the master problem
        if (stop && feasible) {        // store and stop if a solution has been found
          solutionFound();
        } else if (!stop && feasible) { // if cuts have been identified, come back to the master in a consistent state
          nextMasterMove();
        } else if (!feasible) {
          stop = true;
        }
      }
      for (int i = 0; i < limits.size(); i++) {
        AbstractGlobalSearchLimit lim = (AbstractGlobalSearchLimit) limits.get(i);
        lim.reset(false);
      }
      if ((maxNbSolutionStored > 0) && (!stopAtFirstSol) && existsSolution()) {
        solver.worldPopUntil(baseWorld);
        restoreBestSolution();
      } else if (!existsSolution()) {
        solver.setFeasible(Boolean.FALSE);
      }
    } else {
      solver.setFeasible(Boolean.FALSE);
    }
    endTreeSearch();
  }

  public Boolean nextSolution() {
    throw new Error("api not yet available on benders solver (MasterGlobalSearchStrategy)");
  }

  /**
   * Main iteration over the subproblems
   */
  public void solveSubProblems() {
    for (int i = 0; i < solver.getNbSubProblems(); i++) { // solve the subproblems
      if (logger.isLoggable(Level.FINE))
        logger.fine("START SUBPB " + i);
      subproblems.changeGoal(subgoals[i]);
      Boolean sol = subproblems.nextOptimalSolution(masterWorld);
      if (sol == Boolean.FALSE) {
        fail = solver.getContradictionExplanation();
        if (((JumpExplanation) fail).nogoodSize() == 0) feasible = false;
        storeCuts(fail, i);
      } else if (sol == Boolean.TRUE && nbCutLearned == 0) {
        storePartialSolution(i);
      } else if (sol == null) {
        feasible = false;
      }
      solver.worldPopUntil(masterWorld);
      if (masterWorld == solver.getEnvironment().getWorldIndex())
        solver.getPropagationEngine().flushEvents();
      if (!feasible) break;
    }
  }

  /**
   * Describes the way the master search solver has to be set
   * to look for the next solution.
   */
  public void nextMasterMove() {
    master.setCurrentFail((Explanation) fail.copy()); // choose one explanation among all cuts
    master.nextMove = AbstractGlobalSearchStrategy.UP_BRANCH; // restart the master from the current master solution
    fail = null;
    if (logger.isLoggable(Level.FINE))
      logger.fine("START MASTERPB ");
  }

  public void storeCuts(Explanation expl, int i) {
    bendersCut[i] = expl;
    nbCutLearned++;
  }

  /**
   * compute the global cut using the MasterSlavesRelation
   * and add the cut the nogood constraint managing BendersCut
   */
  public void manageCuts() {
    if (feasible) {
      ArrayList currentCuts;
      currentCuts = decomposition.computeExpl(bendersCut);
      addCuts(currentCuts);
    }
    nbCutLearned = 0;
    for (int i = 0; i < bendersCut.length; i++) {
      bendersCut[i] = null;
    }
  }

  public void addCuts(ArrayList currentCuts) {
    logCuts(currentCuts);
    for (int i = 0; i < currentCuts.size(); i++) {
      SymbolicDecision[] exp = (SymbolicDecision[]) ((Explanation) currentCuts.get(i)).getNogood();
      cuts.addPermanentNogood(exp);
    }
  }

  public void solutionFound() {
    logSolution();
    restorePartialSolutions();
    recordSolution();
    /*if (!stopAtFirstSol) {
      System.out.println("Asking for all solutions is not yet implemented");
    }*/
    cleanPartialSolutions();
  }

  // ----------------------------------------------------
  // ---------------- Partial Solution managment -------
  // ----------------------------------------------------

  public void cleanPartialSolutions() {
    for (Iterator iterator = partialSol.iterator(); iterator.hasNext();) {
      int[] vs = (int[]) iterator.next();
      for (int i = 0; i < vs.length; i++) {
        vs[i] = -1;
      }
    }
  }

  public void storePartialSolution(int subpb) {
    int[] vs = (int[]) partialSol.get(subpb);
    for (int i = 0; i < vs.length; i++) {
      vs[i] = ((IntDomainVar) ((BendersSolver) solver).getSubvariablesList(subpb).get(i)).getVal();
    }
  }

  public void restorePartialSolutions() {
    try {
      int subPb = 0;
      for (Iterator iterator = partialSol.iterator(); iterator.hasNext();) {
        int[] vs = (int[]) iterator.next();
        for (int i = 0; i < vs.length; i++) {
          ((IntDomainVar) ((BendersSolver) solver).getSubvariablesList(subPb).get(i)).setVal(vs[i]);
        }
        subPb++;
      }
      solver.propagate();
    } catch (ContradictionException e) {
      throw new Error("Bug in restoring partial solutions in benders master solver");
    }
  }

  // ----------------------------------------------------
  // ------------------- logs ---------------------------
  // ----------------------------------------------------

  public void logSolution() {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Une solution de trouv�e !");
      for (int i = 0; i < solver.getNbIntVars(); i++) {
        logger.fine(solver.getIntVar(i) + " = " + ((IntDomainVar) solver.getIntVar(i)).getVal());
      }
    }
  }

  public void logCuts(ArrayList li) {
    if (logger.isLoggable(Level.FINE)) {
      String mes = "Cuts added : {";
      Iterator it = li.iterator();
      while (it.hasNext()) {
        SymbolicDecision[] cut = (SymbolicDecision[]) ((Explanation) it.next()).getNogood();
        for (int i = 0; i < cut.length; i++) {
          Assignment d = (Assignment) cut[i];
          mes += d.getVar(0) + "==" + d.getBranch();
          if (i < (cut.length - 1)) mes += ",";
        }
        if (it.hasNext()) mes += " | ";
      }
      mes += "}";
      //System.out.println(mes);
      logger.fine(mes);
    }
  }
}
