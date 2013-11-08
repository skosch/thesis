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

package choco.ecp.solver.constraints.real;

import choco.cp.solver.constraints.real.MixedCstElt;
import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.constraints.real.exp.PalmRealIntervalConstant;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.PalmConstraintPlugin;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.variables.integer.dbt.PalmIntVar;
import choco.ecp.solver.variables.real.PalmRealInterval;
import choco.ecp.solver.variables.real.PalmRealVar;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.real.RealVar;

import java.util.Set;

/**
 * Let x be an integer variable with n values and v be a real variable. Given n constant values a1 to an,
 * this constraint ensures that:
 * <p/>
 * <code>x = i iff v = ai</code>
 * <p/>
 * a1... an sequence is supposed to be ordered (a1&lt;a2&lt;... an)
 */
public class PalmMixedCstElt extends MixedCstElt implements PalmMixedConstraint {

  public PalmMixedCstElt(RealVar v0, IntDomainVar v1, double[] values) {
    super(v0, v1, values);
    this.hook = new PalmConstraintPlugin(this);
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public void awake() throws ContradictionException {
    // Ensures that integer domain is correct !
    Explanation e = ((PalmSolver) this.problem).makeExplanation();
    ((PalmConstraintPlugin) this.getPlugIn()).self_explain(e);
    ((PalmIntVar) v1).updateSup(values.length - 1, cIdx1, (PalmExplanation) e.copy());
    ((PalmIntVar) v1).updateInf(0, cIdx1, e);
    this.propagate();
  }

  public void awakeOnRemovals(int varIdx, DisposableIntIterator deltaDomain) throws ContradictionException {
    this.propagate();
  }

  public void updateIInf() throws ContradictionException {
    Explanation e = ((PalmSolver) this.problem).makeExplanation();
    ((PalmRealVar) v0).self_explain(PalmRealInterval.INF, e);
    ((PalmConstraintPlugin) this.getPlugIn()).self_explain(e);

    int inf = v1.getInf();
    while (values[inf] < v0.getInf()) {
      inf++;
    }
    ((PalmIntVar) v1).updateInf(inf, cIdx1, e);
  }

  public void updateISup() throws ContradictionException {
    Explanation e = ((PalmSolver) this.problem).makeExplanation();
    ((PalmRealVar) v0).self_explain(PalmRealInterval.SUP, e);
    ((PalmConstraintPlugin) this.getPlugIn()).self_explain(e);

    int sup = v1.getSup();
    while (values[sup] > v0.getSup()) {
      sup--;
    }
    ((PalmIntVar) v1).updateSup(sup, cIdx1, e);
  }

  public void updateReal() throws ContradictionException {
    Explanation inf = ((PalmSolver) this.problem).makeExplanation();
    Explanation sup = ((PalmSolver) this.problem).makeExplanation();
    ((PalmIntVar) v1).self_explain(PalmRealInterval.INF, inf);
    ((PalmConstraintPlugin) this.getPlugIn()).self_explain(inf);
    ((PalmIntVar) v1).self_explain(PalmRealInterval.SUP, sup);
    ((PalmConstraintPlugin) this.getPlugIn()).self_explain(sup);
    v0.intersect(new PalmRealIntervalConstant(values[v1.getInf()], values[v1.getSup()], inf, sup));
  }

  public void awakeOnRestoreInf(int idx) throws ContradictionException {
    if (idx == 0) {
      updateReal();
    } else {
      updateIInf();
      updateReal();
    }
  }

  public void awakeOnRestoreSup(int idx) throws ContradictionException {
    if (idx == 0) {
      updateReal();
    } else {
      updateISup();
      updateReal();
    }
  }

  public void awakeOnRestoreVal(int idx, int val) throws ContradictionException {
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
}
