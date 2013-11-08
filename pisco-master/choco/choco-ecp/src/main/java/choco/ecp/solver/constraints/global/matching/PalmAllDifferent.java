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

package choco.ecp.solver.constraints.global.matching;

import choco.cp.solver.constraints.global.matching.AllDifferent;
import choco.ecp.solver.constraints.PalmSConstraint;
import choco.ecp.solver.explanations.ExplainedConstraintPlugin;
import choco.ecp.solver.explanations.ExplainedSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.propagation.PalmIntVarListener;
import choco.ecp.solver.variables.integer.ExplainedIntDomain;
import choco.ecp.solver.variables.integer.ExplainedIntVar;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

public class PalmAllDifferent extends AllDifferent implements PalmSConstraint, PalmIntVarListener {
  private Logger logger = Logger.getLogger("dev.i_want_to_use_this_old_version_of_choco.prop.const");;

  public static AllDifferent newAllDiff(IntDomainVar[] vars) {
    int minValue = Integer.MAX_VALUE, maxValue = Integer.MIN_VALUE;
    for (int i = 0; i < vars.length; i++) {
      IntDomainVar var = vars[i];
      // <hca> Il faut regarder original sup et original inf sinon pb pour le dynamique
      minValue = Math.min(((ExplainedIntDomain) var.getDomain()).getOriginalInf(), minValue);
      maxValue = Math.max(((ExplainedIntDomain) var.getDomain()).getOriginalSup(), maxValue);
    }
    return new PalmAllDifferent(vars, minValue, maxValue);
  }

  public PalmAllDifferent(IntDomainVar[] vars, int minValue, int maxValue) {
    super(vars, minValue, maxValue);
    this.hook = ((ExplainedSolver) this.getProblem()).makeConstraintPlugin(this);
  }

  public boolean isSatisfied() {
    for (int i = 0; i < vars.length; i++)
      for (int j = i + 1; j < vars.length; j++)
        if (vars[i].getVal() == vars[j].getVal()) return false;
    return true;
  }

  public void deleteEdgeAndPublish(int i, int j) throws ContradictionException {
    this.deleteMatch(i, j);

    Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
    ((ExplainedConstraintPlugin) this.hook).self_explain(expl);
    int n1 = this.nbLeftVertices;
    int n2 = this.nbRightVertices;
    for (int i2 = 0; i2 < n1; i2++) {
      if (this.componentOrder[this.component[i]][this.component[i2]]) {
        for (int j2 = 0; j2 < n2; j2++) {
          if (this.componentOrder[this.component[j2 + n1]][this.component[j + n1]]) {
            ((ExplainedIntDomain) this.vars[i2].getDomain()).self_explain(ExplainedIntDomain.VAL, j2 + this.minValue, expl);
          }
        }
      }
    }

    if (this.componentOrder[this.component[this.source]][this.component[j + n1]]) { // toujours vrai !!
      for (int j2 = 0; j2 < n2; j2++) {
        int i2 = this.inverseMatch(j2);
        if ((i2 != -1) && (this.componentOrder[this.component[i]][this.component[j2 + n1]])) {
          for (int k = 1; k < n2; k++) {
            //if (this.componentOrder[this.component[j2 + n1]][this.component[k + n1]]) {    // TODO : voir si ca serait bon !!
            if (this.component[k + n1] != this.component[j2 + n1]) {
              ((ExplainedIntDomain) this.vars[i2].getDomain()).self_explain(ExplainedIntDomain.VAL, k + this.minValue, expl);
            }
          }
        }
      }
    }

    if (this.componentOrder[this.component[i]][this.component[this.source]]) {
      // il s'agit du composant source...          // todo : hmmm...
      for (int j2 = 0; j2 < n2; j2++) {
        int i2 = this.inverseMatch(j2);
        if ((i2 == -1) && (this.componentOrder[this.component[j2 + n1]][this.component[j + n1]])) {
          for (int k = 1; k < n1; k++) {
            if (this.component[k] == this.component[j2 + n1]) {
              ((ExplainedIntDomain) this.vars[k].getDomain()).self_explain(ExplainedIntDomain.DOM, expl);
            }
          }
        }
      }
    }

    /*for (int k = 0; k < getNbVars(); k++) {
      ((ExplainedIntDomain) this.vars[k].getDomain()).self_explain(ExplainedIntDomain.DOM, expl);
    } */

    //System.out.println("" + (j + this.minValue) + " removed from " + this.vars[i] + " because of " + expl);
    ((ExplainedIntVar) this.vars[i]).removeVal(j + this.minValue, this.getConstraintIdx(i), expl);

  }

  public void takeIntoAccountStatusChange(int index) {
  }

  public void updateDataStructuresOnConstraint(int idx, int select, int newValue, int oldValue) {
    //System.out.println("UDS remove " + (newValue) + " from " + vars[idx]);
    this.deleteMatch(idx, newValue - this.minValue);
    this.constAwake(false);
  }

  public void updateDataStructuresOnRestoreConstraint(int idx, int select, int newValue, int oldValue) {
    if ((this.matchingSize.get() < this.nbLeftVertices) || (this.component[idx] != this.component[newValue - this.minValue + this.nbLeftVertices])) {
      this.constAwake(false);
    }
  }

  public void awakeOnRestoreInf(int idx) throws ContradictionException {
  }

  public void awakeOnRestoreSup(int idx) throws ContradictionException {
  }

  public void awakeOnRestoreVal(int idx, int val) throws ContradictionException {
  }

  public void awakeOnRestoreVal(int idx, DisposableIntIterator it) throws ContradictionException {
    for (; it.hasNext();) {
      awakeOnRestoreVal(idx, it.next());
    }
    it.dispose();
  }

  public void awakeOnRem(int idx, int val) {

  }

  public void awake() throws ContradictionException {
    this.matchingSize.set(0);
    for (int i = 0; i < this.refMatch.size(); i++)
      this.refMatch.set(i, -1);
    for (int i = 0; i < this.refInverseMatch.size(); i++)
      this.refInverseMatch.set(i, -1);
    this.propagate();
  }

  public Set whyIsTrue() {
    return null;
  }

  public Set whyIsFalse() {
    return null;
  }

  public void augmentFlow() throws ContradictionException {
    int eopath = this.findAlternatingPath();
    int n1 = this.nbLeftVertices;

    if (this.matchingSize.get() < n1) {
      if (logger.isLoggable(Level.INFO)) logger.info("Current flow of size: " + this.matchingSize.get());
      while (eopath >= 0) {
        this.augment(eopath);
        eopath = this.findAlternatingPath();
      }
      if (this.matchingSize.get() < n1) {
        // assert exist i, 0 <= i < n1, this.match(i) == 0
        if (logger.isLoggable(Level.INFO)) logger.info("[Matching] There exists no perfect matching.");
        //this.fail();
        Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
        ((ExplainedConstraintPlugin) this.hook).self_explain(expl);
        for (int i = 0; i < vars.length; i++) {
          IntDomainVar var = vars[i];
          ((ExplainedIntDomain) var.getDomain()).self_explain(ExplainedIntDomain.DOM, expl);
        }
        ((ExplainedSolver) this.getProblem()).explainedFail(expl);
      } else {
        if (logger.isLoggable(Level.INFO)) {
          logger.info("Found a perfect metching (size: " + this.matchingSize.get() + ").");
          for (int i = 0; i < this.nbLeftVertices; i++)
            logger.info("Match " + i + " with " + this.match(i));
        }
        // TODO CheckFlow ...
      }
    }
  }
}
