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

public final class MixedEqXY extends AbstractBinRealIntSConstraint{

  public MixedEqXY(RealVar v0, IntDomainVar v1) {
    super(v0, v1);
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public boolean isConsistent() {
    return v1.getInf() <= v0.getSup() && v0.getInf() <= v1.getSup();
  }

  public boolean isSatisfied() {
    return isConsistent();
  }

  public void propagate() throws ContradictionException {
    if (v0.getInf() > v1.getInf()) {
      updateIInf();
    }
    if (v0.getSup() < v1.getSup()) {
      updateISup();
    }
    updateReal();
  }

  public void awakeOnInf(int idx) throws ContradictionException {
    if (idx == 0) {
      if (v0.getInf() > v1.getInf()) {
        updateIInf();
        updateReal();
      }
    } else if (idx == 1) {
      updateReal();
    }
  }

  public void awakeOnSup(int idx) throws ContradictionException {
    if (idx == 0) {
      if (v0.getSup() < v1.getSup()) {
        updateISup();
        updateReal();
      }
    } else if (idx == 1) {
      updateReal();
    }
  }

  public void awakeOnInst(int varIdx) throws ContradictionException {
    if (varIdx == 1) {
      updateReal();
    }
  }

  public void awakeOnRem(int varIdx, int val) throws ContradictionException {
  }

  public void awakeOnRemovals(int varIdx, DisposableIntIterator deltaDomain) throws ContradictionException {
  }

  public void awakeOnBounds(int varIdx) throws ContradictionException {
    if (varIdx == 0) {
      if (v0.getInf() > v1.getInf()) {
        updateIInf();
      }
      if (v0.getSup() < v1.getSup()) {
        updateISup();
      }
      updateReal();
    } else if (varIdx == 1) {
      updateReal();
    }
  }

  protected void updateIInf() throws ContradictionException {
    v1.updateInf((int) Math.ceil(v0.getInf()), this, false);
  }

  protected void updateISup() throws ContradictionException {
    v1.updateSup((int) Math.floor(v0.getSup()), this, false);
  }

  protected void updateReal() throws ContradictionException {
    v0.intersect(new RealIntervalConstant(v1.getInf(), v1.getSup()));
  }

}
