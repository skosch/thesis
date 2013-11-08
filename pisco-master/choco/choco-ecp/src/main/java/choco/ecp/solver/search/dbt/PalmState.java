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


import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.propagation.dbt.PalmEngine;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.AbstractSConstraint;

import java.util.logging.Logger;

/**
 * Tool for maintaining the state of the search taht is the active posted decision constraint.
 */

public class PalmState extends PalmAbstractSolverTool {
  protected static final Logger logger = Logger.getLogger("dev.i_want_to_use_this_old_version_of_choco.search");

  /**
   * The current state.
   */

  protected PalmExplanation path;

  public PalmExplanation getPath() {
    return path;
  }

  /**
   * Initializes the PalmState with the specified explain.
   *
   * @param expl The initial state.
   */

  public PalmState(PalmExplanation expl) {
    this.path = expl;
  }


  /**
   * Adds a new decision constraints in the state.
   *
   * @param constraint New decision constraint posted.
   */

  public void addDecision(AbstractSConstraint constraint) {
    this.path.add(constraint);
  }


  /**
   * Reverses a decision constraint. (The difference with removing is that it does not call the learning
   * tool).
   *
   * @param constraint The constraint to reverse.
   */

  public void reverseDecision(AbstractSConstraint constraint) {
    this.path.delete(constraint);
  }


  /**
   * Removes a decision constraint.
   *
   * @param constraint The involved constraint.
   */

  public void removeDecision(AbstractSConstraint constraint) {
    this.manager.getLearning().learnFromRemoval(constraint);
    this.path.delete(constraint);
  }


  /**
   * Discards the current solutions in order to find the next one : it raises a fake contradiction and tries
   * repairing the state.
   * @return
   */

  public boolean discardCurrentSolution() {
    try {
      this.manager.reset();
      try {
        ((PalmEngine) this.manager.getSolver().getPropagationEngine()).raisePalmFakeContradiction((PalmExplanation) this.path.copy());
      } catch (PalmContradiction e) {
        this.manager.repair();
      }
      return true;
    } catch (ContradictionException e) {
      return false;
    }
  }
}
