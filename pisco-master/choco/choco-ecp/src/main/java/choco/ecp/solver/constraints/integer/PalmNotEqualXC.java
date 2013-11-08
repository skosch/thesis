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


import choco.ecp.solver.constraints.AbstractPalmUnIntSConstraint;
import choco.ecp.solver.explanations.ExplainedConstraintPlugin;
import choco.ecp.solver.explanations.ExplainedSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.variables.integer.ExplainedIntDomain;
import choco.ecp.solver.variables.integer.ExplainedIntVar;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: grochart
 * Date: 26 ao?t 2003
 * Time: 09:18:02
 * To change this template use Options | File Templates.
 */
public class PalmNotEqualXC extends AbstractPalmUnIntSConstraint {
  protected final int cste;

  public PalmNotEqualXC(IntDomainVar v0, int cste) {
      super(v0);
    this.v0 = v0;
    this.cste = cste;
    this.hook = ((ExplainedSolver) this.getProblem()).makeConstraintPlugin(this);
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public String toString() {
    return this.v0 + " !== " + this.cste;
  }

  public void propagate() throws ContradictionException {
    if (this.v0.hasEnumeratedDomain()) {
      Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
      ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
      ((ExplainedIntVar) this.v0).removeVal(this.cste, this.cIdx0, expl);
    } else {
      if (this.v0.getInf() == this.cste) {
        Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
        ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
        ((ExplainedIntVar) this.v0).updateInf(this.cste + 1, this.cIdx0, expl);
      }
      if (this.v0.getSup() == this.cste) {
        Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
        ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
        ((ExplainedIntVar) this.v0).updateSup(this.cste - 1, this.cIdx0, expl);
      }
    }
  }

  public void awakeOnInf(int idx) throws ContradictionException {
    if (this.v0.getInf() == this.cste) {
      Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
      ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
      ((ExplainedIntVar) this.v0).updateInf(this.cste + 1, this.cIdx0, expl);
    }
  }

  public void awakeOnSup(int idx) throws ContradictionException {
    if (this.v0.getSup() == this.cste) {
      Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
      ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
      ((ExplainedIntVar) this.v0).updateSup(this.cste - 1, this.cIdx0, expl);
    }
  }

  public void awakeOnRem() {
  }

  public void awakeOnRestoreInf(int idx) throws ContradictionException {
    this.awakeOnInf(idx);
  }

  public void awakeOnRestoreSup(int idx) throws ContradictionException {
    this.awakeOnSup(idx);
  }

  public void awakeOnRestoreVal(int idx, int val) throws ContradictionException {
    if (this.cste == val) {
      Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
      ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
      ((ExplainedIntVar) this.v0).removeVal(val, this.cIdx0, expl);
    }
  }

  public Boolean isEntailed() {
    if (this.v0.isInstantiatedTo(this.cste))
      return Boolean.FALSE;
    else if (!(this.v0.canBeInstantiatedTo(this.cste))) return Boolean.TRUE;
    return null;
  }

  public boolean isSatisfied() {
    return this.v0.getVal() != this.cste;
  }

  public Set whyIsTrue() {
    Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
    ((ExplainedIntVar) this.v0).self_explain(ExplainedIntDomain.VAL, this.cste, expl);
    return expl.toSet();
  }

  public Set whyIsFalse() {
    Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
    ((ExplainedIntVar) this.v0).self_explain(ExplainedIntDomain.DOM, expl);
    return expl.toSet();
  }
}
