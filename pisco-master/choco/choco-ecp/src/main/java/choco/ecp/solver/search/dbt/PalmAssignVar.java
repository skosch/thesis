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

import choco.ecp.solver.variables.PalmVar;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.integer.ValIterator;
import choco.kernel.solver.search.integer.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A classic implementation of branching algorithm for Palm. It selects the variable with a minimal domain size
 * and tries to instantiate it to the inf bound.
 */

public class PalmAssignVar extends PalmAbstractBranching {
  protected VarSelector varHeuristic;
  protected ValIterator valHeuristic;
  protected ValSelector valSHeuristic;
  protected ValueChooserWrapper wrapper;
  String[]  LOG_DECISION_MSG = new String[]{"=="};


  public PalmAssignVar(VarSelector varSel, ValIterator valHeuri) {
    varHeuristic = varSel;
    valHeuristic = valHeuri;
    wrapper = new ValIteratorWrapper();
  }

  public PalmAssignVar(VarSelector varSel, ValSelector valHeuri) {
    varHeuristic = varSel;
    valSHeuristic = valHeuri;
    wrapper = new ValSelectorWrapper();
  }

  /**
   * Selects an item to branch on. In this algorithm, the variable with the minimal domain size is choosen.
   *
   * @return An item to branch on.
   */

  public Object selectBranchingObject() throws ContradictionException {
    return varHeuristic.selectVar();
  }


  /**
   * Returns a decision to take on the choosen item. In this algorithm, the variable is instantiated to the
   * inf bound.
   *
   * @param item The variable (item) involved.
   * @return The decision the solver should take.
   */

  public Object selectFirstBranch(Object item) {
    List list = new LinkedList();
    PalmVar var = (PalmVar) item;
    list.add(var.getDecisionConstraint(wrapper.getFirstBranch(var)));
    return list;
  }


  /**
   * Not defined in this algorithm.
   *
   * @param branchingItem
   * @param previousBranch
   */

  public Object getNextBranch(Object branchingItem, Object previousBranch) {
    if (Logger.getLogger("choco").isLoggable(Level.SEVERE))
      Logger.getLogger("choco").severe("PalmAssignVar.getNextDecisions() should not be not be used !");
    return null;
  }

  /**
   * Not defined in this algorithm.
   */

  public boolean finishedBranching(Object item, Object previousBranch) {
    if (Logger.getLogger("choco").isLoggable(Level.SEVERE))
      Logger.getLogger("choco").severe("PalmAssignVar.finishedBranching(...) should not be not be used !");
    return false;
  }

  protected interface ValueChooserWrapper {
    public boolean finishedBranching(Object x, int i);

    public int getFirstBranch(Object x);

    public int getNextBranch(Object x, int i);
  }

  protected class ValIteratorWrapper implements ValueChooserWrapper {
    public boolean finishedBranching(Object x, int i) {
      return (!valHeuristic.hasNextVal((IntDomainVar) x, i));
    }

    public int getFirstBranch(Object x) {
      return valHeuristic.getFirstVal((IntDomainVar) x);
    }

    public int getNextBranch(Object x, int i) {
      return valHeuristic.getNextVal((IntDomainVar) x, i);
    }
  }

  protected class ValSelectorWrapper implements ValueChooserWrapper {
    public boolean finishedBranching(Object x, int i) {
      if (Logger.getLogger("choco").isLoggable(Level.SEVERE))
        Logger.getLogger("choco").severe("A ValSelector cannot be used with path-repair !");
      return true;
    }

    public int getFirstBranch(Object x) {
      return valSHeuristic.getBestVal((IntDomainVar) x);
    }

    public int getNextBranch(Object x, int i) {
      if (Logger.getLogger("choco").isLoggable(Level.SEVERE))
        Logger.getLogger("choco").severe("A ValSelector cannot be used with path-repair !");
      return Integer.MIN_VALUE;
    }
  }

  public String getDecisionLogMsg(int branchIndex) {
    return LOG_DECISION_MSG[0];
  }
}
