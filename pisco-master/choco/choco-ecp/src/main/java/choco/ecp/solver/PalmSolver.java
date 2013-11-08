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
// ______\_/_______     Contibutors: Fran�ois Laburthe, Hadrien Cambazard, Guillaume Rochart...

package choco.ecp.solver;

import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.cp.solver.search.limit.NodeLimit;
import choco.ecp.solver.constraints.PalmSConstraint;
import choco.ecp.solver.explanations.ExplainedConstraintPlugin;
import choco.ecp.solver.explanations.ExplainedSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.GenericExplanation;
import choco.ecp.solver.explanations.dbt.PalmConstraintPlugin;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.propagation.dbt.PalmEngine;
import choco.ecp.solver.search.NogoodSConstraint;
import choco.ecp.solver.search.PalmRealBranchAndBound;
import choco.ecp.solver.search.dbt.pathrepair.PathRepairAssignVar;
import choco.ecp.solver.search.dbt.pathrepair.PathRepairLearn;
import choco.ecp.solver.variables.integer.dbt.PalmIntVar;
import choco.ecp.solver.variables.real.PalmRealVarImpl;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solution;
import choco.kernel.solver.branch.AbstractBranching;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.ConstraintEvent;
import choco.kernel.solver.propagation.Propagator;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.real.RealVar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Choco problem extension involving explanations and explanation-based algorithms (mac-dbt, decision-repair...)
 */

public class PalmSolver extends JumpSolver implements ExplainedSolver {

     /**
   * The extender class the generated solver should use to choose decision constraints during search.
   */
  protected PalmExtend extend = null;

  /**
   * The state class the generated solver should use to maintain the state of the search.
   */
  protected PalmState state = null;

  /**
   * The learning class the generated solver should use to learn forbidden or authorized nodes during search.
   */
  protected PalmLearn learn = null;

  /**
   * The repair class the generated solver should use to choose which decision constraints to remove for repairing.
   */
  protected PalmRepair repair = null;

  /**
   * Lists of the branchings that should be used by the generated solver.
   */
  protected List<AbstractBranching> branchings = new ArrayList<AbstractBranching>();

  /**
   * <i>Decision Repair</i>: number of explanations the decision repair algorithm should store.
   */
  protected int prSize = -1;
  /**
   * <i>Decision Repair</i>: number of moves without improvment the decision repair algorithm should try before
   * stopping.
   */
  protected int prMoves = -1;

    /**
   * Displays release information (date, verions, ...).
   */

  public static void ReleasePalmDisplay() {
    logger.info("** JPaLM : Constraint Programming with Explanations");
    logger.info("** JPaLM v0.9b (July, 2004), Copyright (c) 2000-2004 N. Jussien");
    displayRelease = false;
  }


  /**
   * Creates a Palm Problem with the specified environment.
   */
  public PalmSolver() {
    super();

    // Ensures a determinist behaviour
    GenericExplanation.reinitTimestamp();

    // Specialized engine and solver for Palm
    this.propagationEngine = new PalmEngine(this);

    // Displays information about Palm
    if (displayRelease) ReleasePalmDisplay();
  }

    /**
   * The factory method: builds the solver needed to solve the problem (an optimization solver if a variable
   * should be minimized or maximized, a classical solver (mac-dbt) for constraint problems, or a path-repair
   * or decision-repair solver if explanations should be kept.
   */
  public void generateSearchSolver() {
    if (null == objective) {                   // MAC-DBT
      strategy = new PalmGlobalSearchStrategy(this);
    } else             // MAC-DBT + Dynamic Cuts
        if (objective instanceof IntDomainVar)
          strategy = new PalmBranchAndBound(this, (IntDomainVar) objective, doMaximize);
        else if (objective instanceof RealVar)
          strategy = new PalmRealBranchAndBound(this, (RealVar) objective, doMaximize);
      if (prSize >= 0) {  // Decision repair
      if (repair == null) repair = new PalmUnsureRepair();
      NogoodSConstraint ng = new NogoodSConstraint(intVars.toArray());
      post(ng);
      if (learn == null) {
        learn = new PathRepairLearn(prSize, ng);
      } else {
        ((PathRepairLearn) learn).setMemory(ng);
      }
    }

    // Classical solver tools
    if (state == null) state = new PalmState((PalmExplanation) makeExplanation());
    if (repair == null) repair = new PalmRepair();
    if (learn == null) learn = new PalmLearn();
    if (extend == null) extend = new PalmExtend();

    // Classical limits
    strategy.limits.add(new PalmTimeLimit(strategy, timeLimit));
    strategy.limits.add(new NodeLimit(strategy, nodeLimit));
    // TODO : limits.add(new relaxLimit());

    // Solver should stop at first solution ? TODO : see if useful !
    strategy.stopAtFirstSol = firstSolution;

    // Attach solver tools
    ((PalmGlobalSearchStrategy) strategy).attachPalmState(state);
    ((PalmGlobalSearchStrategy) strategy).attachPalmExtend(extend);
    ((PalmGlobalSearchStrategy) strategy).attachPalmLearn(learn);
    ((PalmGlobalSearchStrategy) strategy).attachPalmRepair(repair);

    // Attach branchings (with a default one if needed
    if (branchings.size() == 0) {
      if (varIntSelector == null) varIntSelector = new MinDomain(this);
      if (valIntIterator == null && valIntSelector == null) valIntIterator = new IncreasingDomain();
      if (prSize < 0)
        if (valIntIterator != null)
          branchings.add(new PalmAssignVar(varIntSelector, valIntIterator));
        else
          branchings.add(new PalmAssignVar(varIntSelector, valIntSelector));
      else if (valIntIterator != null)
        branchings.add(new PathRepairAssignVar(varIntSelector, valIntIterator));
      else
        System.err.println("Path repair cannot use valSelector");
    }
    ((PalmGlobalSearchStrategy) strategy).attachPalmBranchings(branchings);
  }

  /**
   * Attaches a list of goals -- or branching in the case of Palm -- that should be attched
   * to the generated solver.
   *
   * @param lst list of branchings (PalmAbstractBranching)
   */
  public void attachGoals(List<AbstractBranching> lst) { // TODO : dans Choco ?
    branchings = lst;
  }

  /**
   * Adds a new branching to the branching list to attach to the generated solver.
   *
   * @param br
   */
  public void addGoal(PalmAbstractBranching br) { // TODO : utiliser meme methode que CHoco ?
    branchings.add(br);
  }

  /**
   * Return the <code>i</code>th solutions.
   *
   * @param i
   * @return The i-th solution of the problem
   */
  public Solution getSolution(int i) {  // TODO : dans Choco !!
    return (Solution) strategy.solutions.get(i);
  }

  /**
   * Sets a custom extender to attach to the generated solver.
   */
  public void setPalmExtender(PalmExtend ext) {
    extend = ext;
  }

  /**
   * Sets a custom learner to attach to the generated solver.
   */
  public void setPalmLearner(PalmLearn ext) {
    learn = ext;
  }

  /**
   * Sets a custom state to attach to the generated solver.
   */
  public void setPalmState(PalmState ext) {
    state = ext;
  }

  /**
   * Sets a custom repairer to attach to the generated solver.
   */
  public void setPalmRepairer(PalmRepair ext) {
    repair = ext;
  }

  /**
   * <i>Decision Repair</i> Sets the maximal number of explanations stored and the maximal moves without
   * improvement of the decision repair algorithm.
   *
   * @param size
   * @param moves
   */
  public void setPathRepairValues(int size, int moves) {
    this.prSize = size;
    this.nodeLimit = moves;
  }

  public void setPathRepair() {
    this.prSize = 10;
    this.nodeLimit = 100000;
  }

  public Number getOptimumValue() {
    if (strategy instanceof PalmAbstractBranchAndBound) {
      return ((PalmAbstractBranchAndBound) strategy).getOptimumValue();
    }
    return null;
  }

  public void launch() {
    strategy.incrementalRun();
  }


  /**
   * Factory to create explanation.
   * It offers the possibility to make another kind of explanation, only by extending PalmProblem
   *
   * @return the new explanation object
   */
  public Explanation makeExplanation() {
    return new GenericExplanation(this);
  }

  public ExplainedConstraintPlugin makeConstraintPlugin(AbstractSConstraint ct) {
    return new PalmConstraintPlugin(ct);
  }

  public void explainedFail(Explanation exp) throws ContradictionException {
    ((PalmEngine) this.getPropagationEngine()).raisePalmFakeContradiction((PalmExplanation) exp);
  }

  /**
   * Searches one solution of the problem.
   *
   * @return True if a solution was found.
   * @deprecated
   */
  public Boolean searchOneSolution() {
    strategy.incrementalRun();
    return this.isFeasible();
  }


  /**
   * Tries to search the problem by finding one solution or all solutions.
   *
   * @param allSolutions If true, all the solutions are searched.
   * @deprecated
   */
  public Boolean solve(boolean allSolutions) {
    setFirstSolution(!allSolutions);
    generateSearchSolver();
    this.feasible = Boolean.FALSE;
    try {
      strategy.newTreeSearch();
    } catch (ContradictionException e) {
      logger.severe("Should not happen : PalmSolver.solve(boolean)");
    }
    if (allSolutions) {
      boolean soluble = (this.searchOneSolution() == Boolean.TRUE);
      while (soluble) {
        logger.info("A solution was found.");
        soluble = ((PalmGlobalSearchStrategy) strategy).getState().discardCurrentSolution()
            && (this.searchOneSolution() == Boolean.TRUE);
      }
    } else
      return this.searchOneSolution();
    strategy.endTreeSearch();
    return Boolean.TRUE;
  }

  /**
   * Tries to search the <i>first</i> solution of the problem.
   */
  public Boolean solve() {
    generateSearchSolver();
    this.feasible = Boolean.FALSE;
    try {
      strategy.newTreeSearch();
    } catch (ContradictionException e) {
      logger.severe("Should not happen : PalmSolver.solve(boolean)");
    }
    strategy.incrementalRun();
    //if (this.isFeasible() == Boolean.FALSE) {
    strategy.endTreeSearch();
    //}
    return this.isFeasible();
  }

  /**
   * Tries to find another solution.
   */
  public Boolean nextSolution() {
    if (((PalmGlobalSearchStrategy) strategy).getState().discardCurrentSolution()) {
      strategy.incrementalRun();
    } else {
      strategy.endTreeSearch();
      return Boolean.FALSE;
    }
    //if (this.isFeasible() == Boolean.FALSE) {
    strategy.endTreeSearch();
    //}
    return this.isFeasible();
  }

  /**
   * Checks if current solution is still valid.
   */
  public boolean checkSolution() {
    try {
      this.propagate();
      return true;
    } catch (PalmContradiction e) {
      return false;
    } catch (ContradictionException e) {
      logger.severe("This should not happen: PalmSolver.checkSolution()");
      return false;
    }
  }

  /**
   * Tries to find all solutions of the problem.
   */
  public Boolean solveAll() {
    generateSearchSolver();
    this.feasible = Boolean.FALSE;
    try {
      strategy.newTreeSearch();
    } catch (ContradictionException e) {
      logger.severe("Should not happen : PalmSolver.solve(boolean)");
    }
    strategy.incrementalRun();
    boolean soluble = (this.isFeasible() == Boolean.TRUE);
    while (soluble) {
      logger.info("A solution was found.");
      soluble = ((PalmGlobalSearchStrategy) strategy).getState().discardCurrentSolution();
      if (soluble) {
        strategy.incrementalRun();
        soluble = (this.isFeasible() == Boolean.TRUE);
      }
    }
    strategy.endTreeSearch();
    return isFeasible();
  }

  /**
   * Maximize an objective variable with a PalmBranchAndBound
   */
  public Boolean maximize(Var objective, boolean restart) {
    setObjective(objective);
    setDoMaximize(true);
    generateSearchSolver();
    strategy.incrementalRun();
    return this.isFeasible();
  }


  /**
   * Minimize an objective variable with a PalmBranchAndBound
   */
  public Boolean minimize(Var objective, boolean restart) {
    setObjective(objective);
    setDoMaximize(false);
    generateSearchSolver();
    strategy.incrementalRun();
    return this.isFeasible();
  }

  /**
   * Posts a constraints in the problem. If it has ever been posted (but deactivated), it is
   * only reactivated and repropagated.
   *
   * @param constraint The constraint to post.
   */
  public void post(SConstraint constraint) {
    if (constraint instanceof PalmSConstraint) {
      PalmSConstraint pconstraint = (PalmSConstraint) constraint;
      PalmConstraintPlugin pi = (PalmConstraintPlugin) pconstraint.getPlugIn();
      if (!(pi.isEverConnected())) {
        int idx;
        constraints.staticAdd((Propagator)constraint);
        idx = this.constraints.size() - 1;
        pi.setConstraintIdx(idx);
        pconstraint.addListener(false);
        ConstraintEvent event = (ConstraintEvent) pconstraint.getEvent();
        propagationEngine.registerEvent(event);
        propagationEngine.postConstAwake(pconstraint, true);
      } else {
        logger.fine("The constraint " + constraint + " is reactivated.");
        this.propagationEngine.postConstAwake(pconstraint, true);
        pconstraint.setActive();
      }
      if (pi.isDepending())
        pi.setDependance();
    } else {
      throw new Error("Impossible to post non-Palm constraints to a Palm problem");
    }
  }

  public void postCut(SConstraint constraint) {
    throw new UnsupportedOperationException();
  }

  /**
   * Posts a constraint with the specified weight.
   *
   * @param constraint The constraint to post.
   * @param w          The weight associated to the constraint.
   */
  public void post(SConstraint constraint, int w) {
    this.post(constraint);
    ((PalmConstraintPlugin) ((PalmSConstraint) constraint).getPlugIn()).setWeight(w);
  }


  /**
   * Posts an indirect constraint with an explain.
   *
   * @param constraint The constraint to post.
   * @param expl       The set of constraint this posted constraint depends on.
   */
  public void post(SConstraint constraint, PalmExplanation expl) {
    ((PalmConstraintPlugin) ((PalmSConstraint) constraint).getPlugIn()).setIndirect(expl);
    this.post(constraint);
  }

  /**
   * Posts and propagates several decision constraints (that is decisions taken by the solver).
   *
   * @param constraints The constraints to post.
   * @throws ContradictionException
   */
  public void propagateAllDecisionsConstraints(List constraints) throws ContradictionException {
    //this.palmSolver.incRuntimeStatistic(EXT, 1);  move in explore
    for (Iterator iterator = constraints.iterator(); iterator.hasNext();) {
      AbstractSConstraint constraint = (AbstractSConstraint) iterator.next();
      this.post(constraint, 0); // Avant la mise a jour de l'etat sinon la contrainte n'existe pas encore !!
      ((PalmGlobalSearchStrategy) strategy).getState().addDecision(constraint);
      this.propagate();
    }
  }


  /**
   * Tries to repair the problem after a PalmContradiction thanks to removing a responsible
   * constraint (that is a constraint in the explain of the contradiction).
   *
   * @throws ContradictionException
   */
  public void repair() throws ContradictionException {
    ((PalmGlobalSearchStrategy) strategy).repair();
  }


  /**
   * Removes properly a constraint from the problem: the constraint is deactivated and all the depending
   * filtering decisions are undone.
   *
   * @param constraint The constraint to remove.
   */
  public void remove(Propagator constraint) {
    ((PalmEngine) this.propagationEngine).remove(constraint);
    if (((PalmConstraintPlugin) ((PalmSConstraint)constraint).getPlugIn()).isEphemeral()) {
      //System.out.println("+Remove : " + ((PalmConstraintPlugin)constraint.getPlugIn()).getConstraintIdx());
      //this.eraseConstraint(((PalmConstraintPlugin)constraint.getPlugIn()).getConstraintIdx());
      constraint.delete();
    }
  }


  /**
   * Returns the maximum level the solver can relax without user interaction.
   */
  public int getMaxRelaxLevel() {
    return maxRelaxLevel;
  }

  /**
   * Sets the maximum level the solver can relax without user interaction (default value is 0, that is only
   * decision constraints).
   *
   * @param maxRelaxLevel the new level
   */
  public void setMaxRelaxLevel(int maxRelaxLevel) {
    this.maxRelaxLevel = maxRelaxLevel;
  }

  @Override
  public RealVar createRealVal(String name, double min, double max) {
    return new PalmRealVarImpl(this, name, min, max);
  }

  @Override
  public IntDomainVar createIntVar(String name, int domainType, int min, int max) {
    return new PalmIntVar(this, name, domainType, min, max);
  }

  @Override
  protected IntDomainVar createIntVar(String name, int[] sortedValues) {
    return new PalmIntVar(this, name, sortedValues);
  }
}
