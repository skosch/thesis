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

package choco.cp.solver.constraints.real;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.real.AbstractBinRealIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.real.RealIntervalConstant;
import choco.kernel.solver.variables.real.RealVar;

/**
 * Let x be an integer variable with n values and v be a real variable. Given n constant values a1 to an,
 * this constraint ensures that:
 * <p/>
 * <code>x = i iff v = ai</code>
 * <p/>
 * a1... an sequence is supposed to be ordered (a1&lt;a2&lt;... an)
 */
public final class MixedCstElt extends AbstractBinRealIntSConstraint{
  protected double[] values;

  public MixedCstElt(RealVar v0, IntDomainVar v1, double[] values) {
    super(v0, v1);
    this.values = values;
  }

  public Object clone() throws CloneNotSupportedException {
    MixedCstElt newc = (MixedCstElt) super.clone();
    newc.values = new double[this.values.length];
    System.arraycopy(this.values, 0, newc.values, 0, this.values.length);
    return newc;
  }

  public void awake() throws ContradictionException {
    v1.updateSup(values.length - 1, this, false);
    v1.updateInf(0, this, false);
    this.propagate();
  }

  public void propagate() throws ContradictionException {
    updateIInf();
    updateISup();
    updateReal();
  }

  public void awakeOnInf(int idx) throws ContradictionException {
    if (idx == 0) {
      updateIInf();
      updateReal();
    } else {
      updateReal();
    }
  }

  public void awakeOnSup(int idx) throws ContradictionException {
    if (idx == 0) {
      updateISup();
      updateReal();
    } else {
      updateReal();
    }
  }

  public void awakeOnBounds(int idx) throws ContradictionException {
    if (idx == 0) {
      updateIInf();
      updateISup();
      updateReal();
    } else {
      updateReal();
    }
  }

  public void awakeOnInst(int varIdx) throws ContradictionException {
    awakeOnBounds(varIdx);
  }

  public void awakeOnRem(int varIdx, int val) throws ContradictionException {
  }

  public void awakeOnRemovals(int varIdx, DisposableIntIterator deltaDomain) throws ContradictionException {
  }

  public void updateIInf() throws ContradictionException {
    int inf = v1.getInf();
    while (values[inf] < v0.getInf()) {
      inf++;
    }
    if (inf > v1.getSup()) propagationEngine.raiseContradiction(this);
    v1.updateInf(inf, this, false);
  }

  public void updateISup() throws ContradictionException {
    int sup = v1.getSup();
    while (values[sup] > v0.getSup()) {
      sup--;
    }
    if (sup < v1.getInf()) propagationEngine.raiseContradiction(this);
    v1.updateSup(sup, this, false);
  }

  public void updateReal() throws ContradictionException {
    v0.intersect(new RealIntervalConstant(values[v1.getInf()], values[v1.getSup()]));
  }

  public boolean isConsistent() {
    return values[v1.getInf()] <= v0.getSup() && v0.getInf() <= values[v1.getSup()];
  }

  public boolean isSatisfied() {
    return isConsistent();
  }

}
