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
import choco.ecp.solver.explanations.ExplainedSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.JumpExplanation;
import choco.ecp.solver.search.cbj.JumpAssignVar;
import choco.ecp.solver.search.cbj.JumpContradictionException;
import choco.ecp.solver.search.cbj.JumpGlobalSearchStrategy;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.AbstractIntBranching;
import choco.kernel.solver.search.IntBranchingTrace;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 26 d�c. 2004
 * Time: 11:13:01
 * To change this template use File | Settings | File Templates.
 */
public class SubSearchStrategy extends JumpGlobalSearchStrategy {

  protected boolean slave = true;

  protected IntBranchingTrace currentCtx = null;

  public SubSearchStrategy(Solver solver) {
    super(solver);
    slave = false;
  }

  /**
   * @param solver
   * @param slave is true if the global search solver is dedicated to subproblems
   */
  public SubSearchStrategy(Solver solver, boolean slave) {
    super(solver);
    this.slave = slave;
  }

  /**
   * change the goal of the search solver (called when one want to solve
   * the next sub-problem)
   *
   * @param branching
   */
  protected void changeGoal(AbstractIntBranching branching) {
    branching.setSolver(this);
    if (slave) currentCtx = null;
    traceStack = new IntBranchingTrace[solver.getNbBooleanVars() + solver.getNbIntVars() + solver.getNbSetVars()];
    currentTraceIndex = -1;
    mainGoal = branching;
    nextMove = INIT_SEARCH;
  }

  /**
   * add the new branching to the current goal to perform the search over the both goals
   *
   * @param branching to be added to the main branching of the search solver
   */
  public void fusionGoal(AbstractIntBranching branching) {
    branching.setSolver(this);
    AbstractIntBranching currentBranching = mainGoal;
    AbstractIntBranching nextBranching = mainGoal;
    do {
      currentBranching = nextBranching;
      nextBranching = (AbstractIntBranching) currentBranching.getNextBranching();
    } while ((nextBranching != null));
    currentBranching.setNextBranching(branching);
    nextMove = INIT_SEARCH;
  }

  /**
   * get the next solution of the master problem
   *
   * @return
   */
  public Boolean nextOptimalSolution(int masterWorld) {
    return nextSolution();
  }

  public Boolean nextSolution() {
    int previousNbSolutions = nbSolutions;
    encounteredLimit = null;
    IntBranchingTrace ctx = null;
    if (currentCtx != null)
      ctx = currentCtx;
    boolean stop = false;

    // specific initialization for the very first solution search (start from the tree root, instead of last leaf)
    if (nextMove == INIT_SEARCH) {
      nextMove = OPEN_NODE;
      ctx = new IntBranchingTrace(mainGoal);
    } else if (slave || !stopAtFirstSol) {
      ctx = topTrace();
    }
    while (!stop) {
      switch (nextMove) {
        case OPEN_NODE:
          {
            try {
              newTreeNode();
              Object branchingObj = null;
              AbstractIntBranching currentBranching = (AbstractIntBranching) ctx.getBranching();
              AbstractIntBranching nextBranching = currentBranching;
              do {
                currentBranching = nextBranching;
                branchingObj = currentBranching.selectBranchingObject();
                nextBranching = (AbstractIntBranching) currentBranching.getNextBranching();
              } while ((branchingObj == null) && (nextBranching != null));
              if (branchingObj != null) {
                ctx = pushTrace();
                ctx.setBranching(currentBranching);
                ctx.setBranchingObject(branchingObj);
                ctx.setBranchIndex(currentBranching.getFirstBranch(ctx.getBranchingObject()));
                nextMove = DOWN_BRANCH;
              } else {
                solutionFound(ctx);
                stop = true;
              }
            } catch (ContradictionException e) {
              currentFail = ((BendersSolver) solver).makeExplanation();
              nextMove = UP_BRANCH;
            }
            break;
          }
        case UP_BRANCH:
          {
            int contradictionLevel = ((JumpExplanation) currentFail).getLastLevel(solver.getWorldIndex());
            while (this.currentTraceIndex >= 0 && solver.getWorldIndex() > (contradictionLevel + 1)) {
              solver.worldPop();
              ctx = popTrace();
            }
            if (this.currentTraceIndex < 0) {
              stop = true;
              ((ExplainedSolver) solver).setContradictionExplanation(currentFail);
            } else {
              try {
                solver.worldPop();
                endTreeNode();
                postDynamicCut();
                //MasterGlobalSearchStrategy.logger.fine(ctx.getBranchingObject() + " != " + ctx.getBranchIndex() + " " + problem.getEnvironment().getWorldIndex() + " " + currentFail);
                ((JumpAssignVar) ctx.getBranching()).goUpBranch(ctx.getBranchingObject(), ctx.getBranchIndex(), currentFail);
                if (!ctx.getBranching().finishedBranching(ctx.getBranchingObject(), ctx.getBranchIndex())) {
                  ctx.setBranchIndex(ctx.getBranching().getNextBranch(ctx.getBranchingObject(), ctx.getBranchIndex()));
                  nextMove = DOWN_BRANCH;
                } else {
                  System.err.println("Le branching est fini et on a pas eu de contradictions : " + ctx.getBranchingObject() + " " + ((IntDomainVar) ctx.getBranchingObject()).getInf() + " -> " + ((IntDomainVar) ctx.getBranchingObject()).getSup() + ": " + ctx.getBranchIndex());
                  ctx = popTrace();
                  nextMove = UP_BRANCH;
                }
              } catch (JumpContradictionException e) {
                currentFail = e.getExplanation();
                ctx = popTrace();
                nextMove = UP_BRANCH;
              } catch (ContradictionException e) {
                System.err.println("Contradiction exception avec UP_BRANCH");
                e.printStackTrace();
                ctx = popTrace();
                nextMove = UP_BRANCH;
              }
            }
            break;
          }
        case DOWN_BRANCH:
          {
            try {
              //problem.getPropagationEngine().checkCleanState();
              solver.getEnvironment().worldPush();
              //MasterGlobalSearchStrategy.logger.fine(""+ ctx.getBranchingObject() + "=" + ctx.getBranchIndex() + " " + problem.getEnvironment().getWorldIndex());
              ctx.getBranching().goDownBranch(ctx.getBranchingObject(), ctx.getBranchIndex());
              solver.propagate();
              nextMove = OPEN_NODE;
            } catch (JumpContradictionException e) {
              currentFail = e.getExplanation();
              nextMove = UP_BRANCH;
            } catch (ContradictionException e) {
              System.err.println("Contradiction exception avec DOWN_BRANCH");
              currentFail = null;
              nextMove = UP_BRANCH;
            }
            break;
          }
      }
    }
    /*for (int i = 0; i < limits.size(); i++) {
      AbstractGlobalSearchLimit lim = (AbstractGlobalSearchLimit) limits.get(i);
      lim.reset(false);
    }*/
    if (nbSolutions > previousNbSolutions) {
      return Boolean.TRUE;
    } else if (isEncounteredLimit()) {
      return null;
    } else {
      return Boolean.FALSE;
    }
  }

  public void solutionFound(IntBranchingTrace ctx) {
    currentCtx = ctx;
    nbSolutions += 1;
    nextMove = INIT_SEARCH;
  }

  public void setCurrentFail(Explanation e) {
    currentFail = e;
  }
}
