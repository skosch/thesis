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

package choco.ecp.solver.constraints.integer;

import choco.cp.solver.constraints.integer.IntLinComb;
import choco.ecp.solver.constraints.PalmSConstraint;
import choco.ecp.solver.explanations.ExplainedConstraintPlugin;
import choco.ecp.solver.explanations.ExplainedSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.propagation.PalmIntVarListener;
import choco.ecp.solver.variables.integer.ExplainedIntDomain;
import choco.ecp.solver.variables.integer.ExplainedIntVar;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: rochart
 * Date: Jan 13, 2004
 * Time: 7:58:45 AM
 * To change this template use Options | File Templates.
 */
public class PalmIntLinComb extends IntLinComb implements PalmIntVarListener, PalmSConstraint {
  public PalmIntLinComb(IntDomainVar[] vars, int[] coeffs, int nbPositive, int c, int linOperator) {
    super(vars, coeffs, nbPositive, c, linOperator);
    this.hook = ((ExplainedSolver) this.getProblem()).makeConstraintPlugin(this);
  }

  public void awakeOnRem(int idx, int x) throws ContradictionException {
    throw new Error("AwakeOnRem in IntLinComb should not be called");
    //this.propagate();
  }

  public void awakeOnVar(int idx) throws ContradictionException {
    throw new Error("Appel au awakeOnvar");
  }

  public void awakeOnRemovals(int idx, DisposableIntIterator it) throws ContradictionException {
    this.propagate();
  }

  public void awakeOnRestoreInf(int idx) throws ContradictionException {
    this.propagate();
  }

  public void awakeOnRestoreSup(int idx) throws ContradictionException {
    this.propagate();
  }

  public void awakeOnRestoreVal(int idx, int val) throws ContradictionException {
    throw new Error("AwakeOnRestoreVal in IntLinComb should not be called");
  }

  public void awakeOnRestoreVal(int idx, DisposableIntIterator it) throws ContradictionException {
    this.propagate();
  }

  public Set whyIsTrue() {
    return null;
  }

  public Set whyIsFalse() {
    return null;
  }

  public void updateDataStructuresOnConstraint(int idx, int select, int newValue, int oldValue) {
  }

  public void updateDataStructuresOnRestoreConstraint(int idx, int select, int newValue, int oldValue) {
  }

  public void takeIntoAccountStatusChange(int index) {
  }

  private Explanation explainVariablesLB() {
    Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
    ((ExplainedConstraintPlugin) this.hook).self_explain(expl);
    for (int i = 0; i < nbPosVars; i++) {
      ((ExplainedIntVar) this.vars[i]).self_explain(ExplainedIntDomain.INF, expl);
    }
    for (int i = nbPosVars; i < this.getNbVars(); i++) {
      ((ExplainedIntVar) this.vars[i]).self_explain(ExplainedIntDomain.SUP, expl);
    }
    return expl;
  }

  private Explanation explainVariablesUB() {
    Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
    ((ExplainedConstraintPlugin) this.hook).self_explain(expl);
    for (int i = 0; i < nbPosVars; i++) {
      ((ExplainedIntVar) this.vars[i]).self_explain(ExplainedIntDomain.SUP, expl);
    }
    for (int i = nbPosVars; i < this.getNbVars(); i++) {
      ((ExplainedIntVar) this.vars[i]).self_explain(ExplainedIntDomain.INF, expl);
    }
    return expl;
  }

  protected boolean propagateNewLowerBound(int mylb) throws ContradictionException {
    Explanation expl = this.explainVariablesLB();
    boolean anyChange = false;
    int nbVars = getNbVars();
    if (mylb > 0) {
      AbstractIntSConstraint.logger.finer("lb = " + mylb + " > 0 => fail");
      ((ExplainedSolver) this.getProblem()).explainedFail(expl);
    }
    int i;
    for (i = 0; i < nbPosVars; i++) {
      int newSupi = Arithm.divFloor(-(mylb), coeffs[i]) + vars[i].getInf();
      if (((ExplainedIntVar) vars[i]).updateSup(newSupi, cIndices[i], expl)) {
        AbstractIntSConstraint.logger.finer("SUP(" + vars[i].toString() + ") <= " + -(mylb) + "/" + coeffs[i] + " + " + vars[i].getInf() + " = " + newSupi);
        anyChange = true;
      }
    }
    for (i = nbPosVars; i < nbVars; i++) {
      int newInfi = Arithm.divCeil(mylb, -(coeffs[i])) + vars[i].getSup();
      if (((ExplainedIntVar) vars[i]).updateInf(newInfi, cIndices[i], expl)) {
        AbstractIntSConstraint.logger.finer("INF(" + vars[i].toString() + ") >= " + mylb + "/" + -(coeffs[i]) + " + " + vars[i].getSup() + " = " + newInfi);
        anyChange = true;
      }
    }
    return anyChange;
  }

  protected boolean propagateNewUpperBound(int myub) throws ContradictionException {
    Explanation expl = this.explainVariablesUB();
    boolean anyChange = false;
    int nbVars = getNbVars();
    if (myub < 0) {
      AbstractIntSConstraint.logger.finer("ub = " + myub + " < 0 => fail");
      ((ExplainedSolver) this.getProblem()).explainedFail(expl);
    }
    int i;
    for (i = 0; i < nbPosVars; i++) {
      int newInfi = MathUtils.divCeil(-(myub), coeffs[i]) + vars[i].getSup();
      if (((ExplainedIntVar) vars[i]).updateInf(newInfi, cIndices[i], expl)) {
        AbstractIntSConstraint.logger.finer("INF(" + vars[i].toString() + ") >= " + -(myub) + "/" + coeffs[i] + " + " + vars[i].getSup() + " = " + newInfi);
        anyChange = true;
      }
    }
    for (i = nbPosVars; i < nbVars; i++) {
      int newSupi = MathUtils.divFloor(myub, -(coeffs[i])) + vars[i].getInf();
      if (((ExplainedIntVar) vars[i]).updateSup(newSupi, cIndices[i], expl)) {
        AbstractIntSConstraint.logger.finer("SUP(" + vars[i].toString() + ") <= " + myub + "/" + -(coeffs[i]) + " + " + vars[i].getInf() + " = " + newSupi);
        anyChange = true;
      }
    }
    return anyChange;
  }

}
