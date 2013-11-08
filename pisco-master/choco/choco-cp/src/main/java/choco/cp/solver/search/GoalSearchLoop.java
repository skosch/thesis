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
package choco.cp.solver.search;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.goals.Goal;
import choco.kernel.solver.goals.GoalType;
import choco.kernel.solver.goals.solver.ChoicePoint;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import choco.kernel.solver.search.AbstractSearchLoop;

import java.util.ArrayList;
import java.util.List;

import static choco.kernel.solver.search.AbstractGlobalSearchStrategy.*;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 27 oct. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*/
public class GoalSearchLoop extends AbstractSearchLoop {

    final protected Goal mainGoal;
    private int previousNbSolutions;
    protected ChoicePoint currentChoice;
    protected List<Goal> currentGoalStack;
    protected int currentChoiceIndex;
    protected List<GoalTrace> goalTraceStack;
    protected boolean globalContradiction = false;
    private int node_count;


    public GoalSearchLoop(AbstractGlobalSearchStrategy searchStrategy, Goal mainGoal) {
        super(searchStrategy);
        this.mainGoal = mainGoal;
    }

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

    public void setGlobalContradiction() {
        globalContradiction = true;
    }

    @Override
    public void initLoop() {
        previousNbSolutions = searchStrategy.getSolutionCount();
        searchStrategy.setEncounteredLimit(null);
    }

    @Override
    public Boolean endLoop() {
        searchStrategy.limitManager.reset();
        if (searchStrategy.getSolutionCount() > previousNbSolutions) {
            return Boolean.TRUE;
        } else if (searchStrategy.isEncounteredLimit()) {
            return null;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public void initialize() {
        previousNbSolutions = 0;
        super.initialize();
    }

    @Override
    public final int getNodeCount() {
		return node_count;
	}


    //*****************************************************************//
    //*******************  OPEN_NODE  ********************************//
    //***************************************************************//

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

    @Override
    public void openNode() {
        try {
            while (currentChoice == null && !stop) {
                Goal g = popGoal();
                if (g == null) {
                    searchStrategy.recordSolution();
                    searchStrategy.nextMove = UP_BRANCH;
                    stop = true;
                } else {
//                    LOGGER.info("pop "+g.pretty());
                    GoalType gt = g.getType();
                    switch (gt){
                        case CHOICE:
                            currentChoice = (ChoicePoint) g;
                            currentChoiceIndex = 0;
                            // AMAL : popTrace is mandatory for Root node condition
                            searchStrategy.pushTrace();
                            searchStrategy.nextMove = DOWN_BRANCH;
                            break;
                        case GEN:
                            // AMAL : node count increasing should be done there...
                            node_count++;
                            searchStrategy.limitManager.newNode();
                        default:
                            Goal newG = g.execute(searchStrategy.solver);
                            switch (gt){
                                case SET:
                                case REM:
                                    searchStrategy.solver.propagate();
                                    break;
                                case GEN:
                                case INST:
                                    if (newG != null) pushGoal(newG);
                                    break;

                            }
//                            if (newG != null) pushGoal(newG);
                    }

                }
            }
        } catch (ContradictionException e) {
//            LOGGER.finest("[GOAL] contradiction");
            searchStrategy.nextMove = UP_BRANCH;
        }
    }

    //*****************************************************************//
    //*******************  UP_BRANCH  ********************************//
    //***************************************************************//

    public void popGoalTrace() {
        int l = goalTraceStack.size();
        if (l == 0) {
            currentGoalStack = null;
            currentChoice = null;
            currentChoiceIndex = -1;
        } else {
            GoalTrace trace = goalTraceStack.remove(l - 1);            
            currentGoalStack = trace.goalStack;
            currentChoice = trace.choicePoint;
            currentChoiceIndex = trace.choiceIndex;
        }
    }

    @Override
    public void upBranch() {
        popGoalTrace();
        if (currentChoice == null) {
            stop = true;
        } else {
            if (globalContradiction) searchStrategy.nextMove = UP_BRANCH;
            try {
                searchStrategy.solver.worldPop();
                searchStrategy.limitManager.endNode();
                searchStrategy.postDynamicCut();

                currentChoiceIndex++;
                if (currentChoiceIndex < currentChoice.getNbChoices()) {
                    searchStrategy.nextMove = DOWN_BRANCH;
                } else {
                    // AMAL : popTrace is mandatory for Root node condition
                    searchStrategy.popTrace();
                    searchStrategy.nextMove = UP_BRANCH;
                }
            } catch (ContradictionException e) {
                // AMAL : popTrace is mandatory for Root node condition
                searchStrategy.popTrace();
                searchStrategy.nextMove = e.getContradictionMove();
            }
        }
    }

    //*****************************************************************//
    //*******************  DOWN_BRANCH  ******************************//
    //***************************************************************//

    public void pushGoalTrace() {
        GoalTrace trace = new GoalTrace(currentChoice, currentGoalStack, currentChoiceIndex);
        goalTraceStack.add(trace);
        List<Goal> l2 = new ArrayList<Goal>();
        l2.addAll(currentGoalStack);
        currentChoice = null;
        currentChoiceIndex = -1;
        currentGoalStack = l2;
    }

    @Override
    public void downBranch() {
        searchStrategy.solver.worldPush();
        Goal g = currentChoice.getChoice(currentChoiceIndex);
        pushGoalTrace();
        pushGoal(g);
        searchStrategy.nextMove = OPEN_NODE;
    }

    //*****************************************************************//
    //*******************  RESTART  **********************************//
    //***************************************************************//

    @Override
    public void restart() {}

    @Override
    public void initSearch() {
        searchStrategy.nextMove = OPEN_NODE;
          currentGoalStack = new ArrayList<Goal>();
          currentGoalStack.add(mainGoal);
          goalTraceStack = new ArrayList<GoalTrace>();
    }

}
