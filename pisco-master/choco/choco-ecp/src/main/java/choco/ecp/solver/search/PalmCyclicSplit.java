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

package choco.ecp.solver.search;

import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.constraints.real.PalmSplitLeft;
import choco.ecp.solver.explanations.dbt.PalmConstraintPlugin;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.search.dbt.PalmAbstractBranching;
import choco.ecp.solver.variables.real.PalmRealDomain;
import choco.ecp.solver.variables.real.PalmRealVar;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.variables.real.RealIntervalConstant;

import java.util.LinkedList;
import java.util.List;

/**
 * A default branching for continuous variables: each variable is choosen cyclicly.
 */
public class PalmCyclicSplit extends PalmAbstractBranching {
  /**
   * The index of the last splitted variable.
   */
  protected int current = -1;

  String[] LOG_DECISION_MSG = new String[]{""};

  /**
   * Returns the next variable to split.
   */
  public Object selectBranchingObject() throws ContradictionException {
    PalmSolver solver = ((PalmSolver) this.extender.getManager().getProblem());
    int nbvars = solver.getNbRealVars();
    if (nbvars == 0) return null;
    int start = current == -1 ? nbvars - 1 : current;
    int n = (current + 1) % nbvars;
    while (n != start && solver.getRealVar(n).isInstantiated()) {
      n = (n + 1) % nbvars;
    }
    if (solver.getRealVar(n).isInstantiated()) return null;
    current = n;
    return solver.getRealVar(n);
  }

  /**
   * Returns the decision constraint to add (w.r.t. a specified variable).
   */
  public Object selectFirstBranch(Object item) {
    List list = new LinkedList();
    PalmRealVar var = (PalmRealVar) item;
    AbstractSConstraint cst = new PalmSplitLeft(var, new RealIntervalConstant(var));
    PalmExplanation expl = (PalmExplanation) ((PalmRealDomain) var.getDomain()).getDecisionConstraints();
    if (expl.size() > 0) ((PalmConstraintPlugin) cst.getPlugIn()).setDepending(expl);
    list.add(cst);
    return list;
  }

  /**
   * Checks if the constraints that should be posted are acceptable w.r.t. the learner component.
   * Not used here.
   */
  public boolean checkAcceptable(List csts) {
    throw (new UnsupportedOperationException());
  }

  /**
   * Learns from rejection: it allows to avoid to fail again for the same reason.
   * Not used here.
   */
  public void learnFromRejection() {
    throw (new UnsupportedOperationException());
  }

  /**
   * Returns the next decision constraints for a specified variable.
   * Not used here.
   */
  public Object getNextBranch(Object branchingItem, Object previousBranch) {
    throw (new UnsupportedOperationException());
  }

  public boolean finishedBranching(Object item, Object previousBranch) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String getDecisionLogMsg(int branchIndex) {
    return LOG_DECISION_MSG[0];
  }
}
