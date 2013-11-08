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

package choco.cp.solver.goals;


import choco.cp.solver.goals.choice.Generate;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.goals.Goal;
import choco.kernel.solver.goals.solver.ChoicePoint;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: grochart
 * Date: 19 mai 2007
 */
@Deprecated
public class GoalSearchSolver extends AbstractGlobalSearchStrategy {


  private static class GoalTrace {
    final ChoicePoint choicePoint;
    int choiceIndex;
    final List<Goal> goalStack;

    GoalTrace(ChoicePoint choicePoint, List<Goal> goalStack, int choiceIndex) {
      this.goalStack = goalStack;
      this.choicePoint = choicePoint;
      this.choiceIndex = choiceIndex;
    }
  }

  final protected Goal mainGoal;
  protected List<GoalTrace> goalTraceStack;
  protected List<Goal> currentGoalStack;
  protected ChoicePoint currentChoice;
  protected int currentChoiceIndex;
  protected boolean globalContradiction = false;


  public GoalSearchSolver(Solver s, Goal mainGoal) {
    super(s);
    this.mainGoal = mainGoal;
  }

  public void setGlobalContradiction() {
    globalContradiction = true;
  }

  public void pushGoalTrace() {
    GoalTrace trace = new GoalTrace(currentChoice, currentGoalStack, currentChoiceIndex);
    goalTraceStack.add(trace);

    List<Goal> l2 = new ArrayList<Goal>(8);
    l2.addAll(currentGoalStack);
    currentChoice = null;
    currentChoiceIndex = -1;
    currentGoalStack = l2;
  }

  public void popGoalTrace() {
    int l = goalTraceStack.size();
    if (l == 0) {
      currentGoalStack = null;
      currentChoice = null;
      currentChoiceIndex = -1;
    } else {
      GoalTrace trace = goalTraceStack.get(l - 1);
      goalTraceStack.remove(l - 1);
      currentGoalStack = trace.goalStack;
      currentChoice = trace.choicePoint;
      currentChoiceIndex = trace.choiceIndex;
    }
  }

  public ChoicePoint lastChoicePoint() {
    int l = goalTraceStack.size();
    if (l == 0) {
      return null;
    } else {
      GoalTrace trace = goalTraceStack.get(l - 1);
      return trace.choicePoint;
    }
  }

  public Goal popGoal() {
    int l = currentGoalStack.size();
    if (l == 0) return null;
    else {
      Goal g = currentGoalStack.get(l - 1);
      currentGoalStack.remove(l - 1);
      return g;
    }
  }

  public void pushGoal(Goal g) {
    currentGoalStack.add(g);
  }

//  int lastRealMove = DOWN_BRANCH;

  public Boolean nextSolution() {
    int previousNbSolutions = getSolutionCount();
    encounteredLimit = null;
    boolean stop = false;

    // specific initialization for the very first solution search (start from the tree root, instead of last leaf)
    if (nextMove == INIT_SEARCH) {
      nextMove = OPEN_NODE;
      currentGoalStack = new ArrayList<Goal>(8);
      currentGoalStack.add(mainGoal);
      goalTraceStack = new ArrayList<GoalTrace>(8);
    } /*else {
      topGoalTrace();
    }   */
    while (!stop) {
      switch (nextMove) {
        case OPEN_NODE: {
          try {
            while (currentChoice == null && !stop) {
              Goal g = popGoal();
              if (g == null) {
                recordSolution();
                // showSolution();
                nextMove = UP_BRANCH;
                stop = true;
              } else {
                if (g instanceof ChoicePoint) {
                  //newTreeNode();
                  currentChoice = (ChoicePoint) g;
                  currentChoiceIndex = 0;
                  nextMove = DOWN_BRANCH;
                } else {
                  if (g instanceof Generate) {
                    if(LOGGER.isLoggable(Level.FINEST)) {LOGGER.log(Level.FINEST, "[GOAL] generate {0} pop", g.pretty());}
                    limitManager.newNode();
                  }
                  Goal newG = g.execute(this.getSolver());
                  this.getSolver().propagate();
                  if (newG != null) pushGoal(newG);
                }
              }
            }
          } catch (ContradictionException e) {
            LOGGER.finest("[GOAL] contradiction");
            nextMove = UP_BRANCH;
          }
          break;
        }
        case UP_BRANCH: {
          popGoalTrace();
          //lastRealMove = UP_BRANCH;
          if (currentChoice == null) {
            stop = true;
          } else {
            if (globalContradiction) nextMove = UP_BRANCH;
            try {
              this.getSolver().worldPop();
              //endTreeNode();
              //problem.propagate();
              limitManager.endNode();
              postDynamicCut();
              currentChoiceIndex++;
              if (currentChoiceIndex < currentChoice.getNbChoices()) {
                nextMove = DOWN_BRANCH;
              } else {
                nextMove = UP_BRANCH;
              }
            } catch (ContradictionException e) {
              //ctx = popTrace();
              nextMove = UP_BRANCH;
            }
          }
          break;
        }
        case DOWN_BRANCH: {
          //try {
          //if (lastRealMove == DOWN_BRANCH)
            this.getSolver() .worldPush();
          //lastRealMove = DOWN_BRANCH;
          Goal g = currentChoice.getChoice(currentChoiceIndex);
          pushGoalTrace();
          pushGoal(g);
          nextMove = OPEN_NODE;
          //} catch (ContradictionException e) {
          //  nextMove = UP_BRANCH;
          //}
          break;
        }
      }
    }
    limitManager.reset();
    if (getSolutionCount() > previousNbSolutions) {
      return Boolean.TRUE;
    } else if (isEncounteredLimit()) {
      return null;
    } else {
      return Boolean.FALSE;
    }
  }
}