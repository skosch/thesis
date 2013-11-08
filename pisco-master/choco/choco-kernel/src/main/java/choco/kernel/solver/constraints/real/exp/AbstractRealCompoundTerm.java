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

package choco.kernel.solver.constraints.real.exp;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateDouble;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.real.RealExp;
import choco.kernel.solver.variables.real.RealInterval;

/**
 * A compound expression depending on other terms.
 */
public abstract class AbstractRealCompoundTerm implements RealExp {
  protected IStateDouble inf;
  protected IStateDouble sup;
    /**
     * The (optimization or decision) model to which the entity belongs.
     */

    public Solver solver;


  public AbstractRealCompoundTerm(Solver solver) {
    this.solver =solver;
    IEnvironment env = solver.getEnvironment();
    inf = env.makeFloat(Double.NEGATIVE_INFINITY);
    sup = env.makeFloat(Double.POSITIVE_INFINITY);
  }

  /**
   * Retrieves the solver of the entity
   */

  public Solver getSolver() {
    return solver;
  }

  public void setSolver(Solver solver) {
    this.solver = solver;
  }

  public String toString() {
    return "[" + inf.get() + "," + sup.get() + "]";
  }

  public double getInf() {
    return inf.get();
  }

  public double getSup() {
    return sup.get();
  }

  public void intersect(RealInterval interval) throws ContradictionException {
    if (interval.getInf() > inf.get()) inf.set(interval.getInf());
    if (interval.getSup() < sup.get()) sup.set(interval.getSup());
    if (inf.get() > sup.get()) {
      this.solver.getPropagationEngine().raiseContradiction(this);
    }
  }


  public String pretty() {
    return toString();
  }
}
