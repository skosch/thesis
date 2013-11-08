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


package choco.cp.solver.constraints.integer.channeling;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * a simple channeling constraint :
 * y_ij = 1 si x_i = j
 * y_ij = 0 sinon
 */
public final class BooleanChanneling extends AbstractBinIntSConstraint {

  protected int cste;

  public BooleanChanneling(IntDomainVar yij, IntDomainVar xi, int j) {
    super(yij, xi);
    this.cste = j;
  }

    @Override
    public int getFilteredEventMask(int idx) {
        if(idx == 0){
            if(v0.hasEnumeratedDomain()){
                return IntVarEvent.REMVAL_MASK;
            }else{
                return IntVarEvent.BOUNDS_MASK;
            }
        }else{
            if(v1.hasEnumeratedDomain()){
                return IntVarEvent.REMVAL_MASK;
            }else{
                return IntVarEvent.BOUNDS_MASK;
            }
        }
    }

    public void filterFromBtoX() throws ContradictionException {
    if (v0.isInstantiated()) {
      if (v0.isInstantiatedTo(0)) { // on retire la valeur j de x;
        v1.removeVal(cste, this, false);
      } else { // on instancie x � j
        v1.instantiate(cste, this, false);
      }
    }
  }

  public void filterFromXtoB() throws ContradictionException {
    if (v1.canBeInstantiatedTo(cste)) {
      if (v1.isInstantiatedTo(cste)) { // on instancie y_ij � 1
        v0.instantiate(1, this, false);
      }
    } else {  // on instancie y_ij � 0;
      v0.instantiate(0, this, false);
    }
  }

  public void propagate() throws ContradictionException {
    filterFromXtoB();
    filterFromBtoX();
  }

  public void awakeOnInf(int idx) throws ContradictionException {
    if (idx == 0) {
      filterFromBtoX();
    } else {
      filterFromXtoB();
    }
  }

  public void awakeOnSup(int idx) throws ContradictionException {
    if (idx == 0) {
      filterFromBtoX();
    } else {
      filterFromXtoB();
    }
  }

  public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
    if (idx == 0) {
      filterFromBtoX();
    } else {
      filterFromXtoB();
    }
  }

  public Boolean isEntailed() {
    if (!v1.canBeInstantiatedTo(cste)) {
      return Boolean.TRUE;
    } else
      return null;
  }

  public boolean isSatisfied(int[] tuple) {
    int val = tuple[1];
    return (val == cste && tuple[0] == 1) || (val != cste && tuple[0] == 0);
  }

  public String pretty() {
    return "(" + v0.pretty() + " = 1)  <=> (" + v1.pretty() + " = " + cste + ")";
  }
}
