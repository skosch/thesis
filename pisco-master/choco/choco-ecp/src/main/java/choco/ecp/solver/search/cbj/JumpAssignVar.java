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

package choco.ecp.solver.search.cbj;

import choco.cp.solver.search.integer.branching.AssignVar;
import choco.ecp.solver.JumpSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.JumpExplanation;
import choco.ecp.solver.variables.integer.ExplainedIntVar;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.integer.ValIterator;
import choco.kernel.solver.search.integer.ValSelector;

// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

/**
 * An variable assigning heuristic used by search algorithm.
 */
public class JumpAssignVar extends AssignVar {

  /**
   * Builds an assign variable heuristic.
   * @param varSel a variable selector
   * @param valHeuri a value iterator
   */
  public JumpAssignVar(final VarSelector varSel,
      final ValIterator valHeuri) {
    super(varSel, valHeuri);
  }

  /**
   * Builds an assign variable heuristic.
   * @param varSel a variable selector
   * @param valHeuri a value selector
   */
  public JumpAssignVar(final VarSelector varSel,
      final ValSelector valHeuri) {
    super(varSel, valHeuri);
  }

  /**
   * Actually posts the choice taken if this search tree node. Here the
   * variable will be instantiated to the value i.
   * @param x the variable involved in the choice
   * @param i the value chosen for this variable
   * @throws ContradictionException if a contradiction occurs due to this
   * choice
   */
  public void goDownBranch(final Object x, final int i) 
  throws ContradictionException {
    logDownBranch(x, i);
    ExplainedIntVar y = (ExplainedIntVar) x;
    Explanation exp = ((JumpSolver) manager.solver).
        makeExplanation(manager.solver.getWorldIndex() - 1);
    y.instantiate(i, -1, exp);
    manager.solver.propagate();
  }

  /**
   * A previous choice is undone. Here the bad value is removed from the
   * domain and correctly explained.
   * @param x the involved variable 
   * @param i the bad value
   * @param e the explanation about inconsistancy
   * @throws ContradictionException if a contradiction occurs due to the
   * implied domain reduction
   */
  public void goUpBranch(final Object x, final int i, final Explanation e) 
  throws ContradictionException {
    logUpBranch(x, i);
    ExplainedIntVar y = (ExplainedIntVar) x;
    //if (((JumpExplanation)e).contains(manager.problem.getWorldIndex() + 2))
    ((JumpExplanation) e).delete(manager.solver.getWorldIndex());
    y.removeVal(i, -1, e);
    manager.solver.propagate();
  }
}
