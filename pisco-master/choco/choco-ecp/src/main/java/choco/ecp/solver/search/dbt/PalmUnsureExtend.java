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

package choco.ecp.solver.search.dbt;

import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.propagation.dbt.PalmEngine;
import choco.ecp.solver.variables.PalmVar;
import choco.ecp.solver.variables.integer.dbt.PalmIntDomain;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.branch.AbstractBranching;

import java.util.List;

public class PalmUnsureExtend extends PalmExtend {

  public void explore(PalmAbstractBranching branching) throws ContradictionException {
    Object item = branching.selectBranchingObject();
    if (item != null) {
      this.getManager().newTreeNode();
      ((PalmSolver) this.manager.getProblem()).propagateAllDecisionsConstraints((List) this.selectAuthorizedDecisions(branching, item));
    } else {
      AbstractBranching br = branching.getNextBranching();
      if (br != null) {
        this.explore((PalmAbstractBranching) br);
      } else {
        this.manager.setFinished(true);
      }
    }
  }

  /**
   * Check if a decision is valid (according to the learner)
   */
  public boolean checkAcceptable(List csts) {
    return true;
  }


  /**
   * check if the variable has been emptied
   */

  public void learnFromRejection(PalmAbstractBranching branching, Object item, Object decisionList) throws ContradictionException {
    if (branching.finishedBranching(item, decisionList)) {
      PalmExplanation expl = (PalmExplanation) ((PalmSolver) this.getManager().getProblem()).makeExplanation();
      ((PalmVar) item).self_explain(PalmIntDomain.DOM, expl);
      ((PalmEngine) this.getManager().getProblem().getPropagationEngine()).raisePalmFakeContradiction(expl);
    }
  }

  /*
   * Computes decisions authorized by the learner that can be taken on the specified item by the solver.
   * @param var The item the solver branchs on.
   */
  public Object selectAuthorizedDecisions(PalmAbstractBranching branching, Object item) throws ContradictionException {
    PalmLearn learner = this.getManager().getLearning();
    List decisionlist = (List) branching.selectFirstBranch(item);
    while (!checkAcceptable(decisionlist) | !learner.checkAcceptable(decisionlist)) {
      learnFromRejection(branching, item, decisionlist);
      decisionlist = (List) branching.getNextBranch(item, decisionlist);
    }
    return decisionlist;
  }

}
