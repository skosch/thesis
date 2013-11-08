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

package choco.ecp.solver;

import choco.cp.solver.propagation.ChocEngine;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.cp.solver.search.limit.NodeLimit;
import choco.cp.solver.search.limit.TimeLimit;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.benders.BendersExplanation;
import choco.ecp.solver.search.NogoodSConstraint;
import choco.ecp.solver.search.benders.ApproximateMaster;
import choco.ecp.solver.search.benders.MasterGlobalSearchStrategy;
import choco.ecp.solver.search.benders.MasterOptimizer;
import choco.ecp.solver.search.benders.MasterSlaveOptimizer;
import choco.ecp.solver.search.cbj.JumpAssignVar;
import choco.kernel.solver.branch.AbstractIntBranching;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.integer.ValIterator;
import choco.kernel.solver.search.integer.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 28 d�c. 2004
 * Time: 13:39:20
 * To change this template use File | Settings | File Templates.
 */

/**
 * A solver for the benders algorithm
 *
 * Choco problem extension involving explanations and a Benders
 * decomposition algorithm based on the use of explanations.
 * The decomposition is made among the variables and the choco model need only
 * to be enrich by indicating for each variable the problem to which it belongs.
 * Warning : subproblems must be independent once the master is instantiated.
 * If it is not the case, it has to be precised with the use of setApproximatedStructure().
 */
public class BendersSolver extends JumpSolver {

  public static Logger logger = Logger.getLogger("choco");
  /**
   * Branching heuristics for each sub-problem
   */
  protected ArrayList<VarSelector> subVarSelector;

  protected ArrayList<ValIterator> subValIterator;

  protected ArrayList<ValSelector> subValSelector;

  protected IntDomainVar[] objectives;

  /**
   * Objective function
   */
  protected MasterSlavesRelation relation;

    /**
   * The number of sub problems considered at each iteration
   */
  protected int nbSubProblems = 1;

  /**
   * List of the Master variables.
   */
  protected ArrayList<IntDomainVar> masterVariables;

  /**
   * for each subproblems, a table of variables corresponding to the
   * subproblem n�i is stored in subvariables.get(i)
   */
  protected ArrayList<ArrayList<IntDomainVar>> subvariables;

  /**
   * Boolean indicating whether the subproblems are completely independant once
   * master variables instantiated.
   */
  protected boolean approximatedStructure;

  public BendersSolver() {
    super();
    subVarSelector = new ArrayList<VarSelector>();
    subValIterator = new ArrayList<ValIterator>();
    subValSelector = new ArrayList<ValSelector>();

      masterVariables = new ArrayList<IntDomainVar>();
    subvariables = new ArrayList<ArrayList<IntDomainVar>>();
    subvariables.add(new ArrayList<IntDomainVar>());
    // Specialized engine and solver for Palm
    this.propagationEngine = new ChocEngine(this);
  }

  public void initHeuristic(ArrayList heuri) {
    while (heuri.size() < (this).getNbSubProblems())
      heuri.add(null);
  }


  /**
   * Sets the variable selector the search solver should use.
   */
  public void setSubVarSelector(int i, VarSelector varSelector) {
    initHeuristic(subVarSelector);
    this.subVarSelector.set(i, varSelector);
  }

  /**
   * Sets the value iterator the search should use
   */
  public void setSubValIterator(int i, ValIterator valIterator) {
    initHeuristic(subValIterator);
    subValIterator.set(i, valIterator);
  }

  /**
   * Sets the value selector the search should use
   */
  public void setSubValSelector(int i, ValSelector valSelector) {
    initHeuristic(subValSelector);
    this.subValSelector.set(i, valSelector);
  }

  public void generateSearchSolver() {
    if (null == objectives && null == objective) {
      if (this.isApproximatedStructure())
        strategy = new ApproximateMaster(this, getNbSubProblems());
      else
        strategy = new MasterGlobalSearchStrategy(this, getNbSubProblems());
    } else {
      if (objectives == null)
        strategy = new MasterOptimizer((IntDomainVar) objective, getNbSubProblems(), doMaximize);
      else
        strategy = new MasterSlaveOptimizer((IntDomainVar) objective, objectives, doMaximize, relation);
    }
    strategy.stopAtFirstSol = firstSolution;
    if (!firstSolution && objectives == null && objective == null)
      throw new UnsupportedOperationException("Searching for all solutions is not yet available within the decomposition");
    strategy.limits.add(new TimeLimit(strategy, timeLimit));
    strategy.limits.add(new NodeLimit(strategy, nodeLimit));
    ((MasterGlobalSearchStrategy) strategy).updateLimit();

    // Add the cut manager for master problem
    NogoodSConstraint ng = new NogoodSConstraint(intVars.toArray(new IntDomainVar[intVars.size()]));//getMasterVariables());
    ((MasterGlobalSearchStrategy) strategy).setCutsConstraint(ng);
    post(ng);

    generateMasterGoal();
    generateSubGoals();
  }


  //  default var and valselector are heuristics of the master problem

  protected void generateMasterGoal() {
    if (varIntSelector == null)
      varIntSelector = new MinDomain(this, getMasterVariables());
    if (valIntIterator == null && valIntSelector == null) valIntIterator = new IncreasingDomain();
    if (valIntIterator != null)
      attachMasterGoal(new JumpAssignVar(varIntSelector, valIntIterator));
    else
      attachMasterGoal(new JumpAssignVar(varIntSelector, valIntSelector));
  }

  protected void attachMasterGoal(AbstractIntBranching branching) {
    branching.setSolver(strategy);
    ((MasterGlobalSearchStrategy) strategy).setMainGoal(branching);
  }

  protected void generateSubGoals() {
    int n = getNbSubProblems();
    initHeuristic(subVarSelector);
    initHeuristic(subValIterator);
    initHeuristic(subValSelector);
    for (int i = 0; i < n; i++) {
      if (subVarSelector.get(i) == null)
        subVarSelector.set(i, new MinDomain(this, getSubvariables(i)));
      if (subValIterator.get(i) == null && subValSelector.get(i) == null)
        subValIterator.set(i, new IncreasingDomain());
      if (subValIterator.get(i) != null)
        attachSubGoal(i, new JumpAssignVar(subVarSelector.get(i), subValIterator.get(i)));
      else
        attachSubGoal(i, new JumpAssignVar(subVarSelector.get(i), subValSelector.get(i)));
    }
  }

  protected void attachSubGoal(int i, AbstractIntBranching branching) {
    branching.setSolver(strategy);
    ((MasterGlobalSearchStrategy) strategy).setSubGoal(i, branching);
  }

  public void setRelation(MasterSlavesRelation relation) {
    this.relation = relation;
  }

  public void setObjectives(IntDomainVar[] objectives) {
    this.objectives = objectives;
  }

  public Number getOptimumValue() {
    return ((MasterGlobalSearchStrategy) strategy).getOptimumValue();
  }


    /**
   * Add a variable to the master set.
   *
   * @param v the variable to be added to the master
   */
  public void addMasterVariables(IntDomainVar v) {
    masterVariables.add(v);
  }

  /**
   * Add a variable to a given sub-problem.
   *
   * @param i the number of considered sub-problem
   * @param v the variable to be added
   */
  public void addSubVariables(int i, IntDomainVar v) {
    while (subvariables.size() <= i) {
      subvariables.add(new ArrayList<IntDomainVar>());
      nbSubProblems += 1;
    }
    subvariables.get(i).add(v);
  }

  /**
   * factory to build explanation within the Benders Framework
   *
   * @return
   */
  public Explanation makeExplanation() {
    return new BendersExplanation(this);
  }

  /**
   * factory to build an explanation at a given level within the Benders Framework
   *
   * @return
   */
  public Explanation makeExplanation(int level) {
    return new BendersExplanation(level, this);
  }

  public boolean isApproximatedStructure() {
    return approximatedStructure;
  }

  /**
   * precise that the structures used as subproblems is not ideal
   * but has week relationships
   * WARNING : it can only be used in case of satisfaction problem.
   */
  public void setApproximatedStructure() {
    this.approximatedStructure = true;
  }

  /**
   * @return the number of subproblems considered by the algorithm
   */
  public int getNbSubProblems() {
    return nbSubProblems;
  }

  /**
   * @return the number of cuts learned during the search
   */
  public int getNbCutsLearned() {
    return ((MasterGlobalSearchStrategy) strategy).getNbCuts();
  }

  /**
   * @return the array of master variables
   */
  public IntDomainVar[] getMasterVariables() {
    IntDomainVar[] mvs = new IntDomainVar[masterVariables.size()];
    masterVariables.toArray(mvs);
    return mvs;
  }

  /**
   * @return the list of master variables
   */
  public ArrayList getMasterVariablesList() {
    return masterVariables;
  }

  /**
   * @return the list of variable of the subproblem number i
   */
  public ArrayList getSubvariablesList(int i) {
    return subvariables.get(i);
  }

  /**
   * @return the array of variable of the subproblem number i
   */
  public IntDomainVar[] getSubvariables(int i) {
    IntDomainVar[] subvs = new IntDomainVar[subvariables.get(i).size()];
    ((ArrayList) subvariables.get(i)).toArray(subvs);
    return subvs;
  }

  /**
   * minimize an objective function over both the master and sub-problems.
   *
   * @param mobj     objective variable of the master
   * @param objs     objectives variables of each subproblem
   * @param relation a relation representing the objective function
   */
  public void minimize(IntDomainVar mobj, IntDomainVar[] objs, MasterSlavesRelation relation) {
    optimize(false, mobj, objs, relation);
  }

  /**
   * minimize an objective function only including variables of the master.
   *
   * @param obj objective variable of the master
   */
  public void minimize(IntDomainVar obj) {
    optimize(false, obj, null);
  }

  /**
   * maximize an objective function over both the master and sub-problems.
   *
   * @param mobj     objective variable of the master
   * @param objs     objectives variables of each subproblem
   * @param relation a relation representing the objective function
   */
  public void maximize(IntDomainVar mobj, IntDomainVar[] objs, MasterSlavesRelation relation) {
    optimize(true, mobj, objs, relation);
  }

  /**
   * minimize an objective function only including variables of the master.
   *
   * @param obj objective variable of the master
   */
  public void maximize(IntDomainVar obj) {
    optimize(true, obj, null);
  }

  protected Boolean optimize(boolean maximize, IntDomainVar obj, MasterSlavesRelation relation) {
    return optimize(maximize, obj, null, relation);
  }

  protected Boolean optimize(boolean maximize, IntDomainVar mobj, IntDomainVar[] objs, MasterSlavesRelation relation) {
    setDoMaximize(maximize);
    setObjectives(objs);
    setObjective(mobj);
    setRelation(relation);
    setFirstSolution(false);
    generateSearchSolver();
    launch();
    return this.isFeasible();
  }
}
