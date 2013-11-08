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

package choco.cp.solver.variables.real;

import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.structure.Couple;
import choco.kernel.memory.structure.PartiallyStoredRealCstrList;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.real.RealExp;
import choco.kernel.solver.propagation.listener.RealPropagator;
import choco.kernel.solver.variables.AbstractVar;
import choco.kernel.solver.variables.real.*;

import java.util.List;
import java.util.Set;
/** History:
 * 2007-12-07 : FR_1873619 CPRU: DomOverDeg+DomOverWDeg
 * */
/**
 * An implementation of real variables using RealDomain domains.
 */
public final class RealVarImpl<C extends AbstractSConstraint & RealPropagator>extends AbstractVar implements RealVar {
  protected RealDomain domain;

    private final Solver solver;

  public <C extends AbstractSConstraint & RealPropagator> RealVarImpl(Solver solver, String name, double a, double b, int domaintype) {
    super(solver, name, new PartiallyStoredRealCstrList<C>(solver.getEnvironment()));
    if (domaintype == RealVar.BOUNDS) {
     this.domain = new RealDomainImpl(this, a, b, solver);
    } else throw new SolverException("Unknown real domain");
    this.event = new RealVarEvent(this);
      this.solver = solver;
  }

    public final DisposableIterator<Couple<C>> getActiveConstraints(C cstrCause){
        //noinspection unchecked
        return ((PartiallyStoredRealCstrList)constraints).getActiveConstraint(cstrCause);
    }

  @Override
public String toString() {
    return this.name + domain.toString();
  }

  public String pretty() {
    return this.toString();
  }

  public RealInterval getValue() {
    return new RealIntervalConstant(getInf(), getSup());
  }

  public RealDomain getDomain() {
    return domain;
  }

  public void silentlyAssign(RealInterval i) {
    domain.silentlyAssign(i);
  }

  public double getInf() {
    return domain.getInf();
  }

  public double getSup() {
    return domain.getSup();
  }

  public void intersect(RealInterval interval) throws ContradictionException {
    this.domain.intersect(interval);
  }

  public boolean isInstantiated() {
    return RealMath.isCanonical(this, this.solver.getPrecision());
  }

  public void tighten() {
  }

  public void project() {
  }

  public List<RealExp> subExps(List<RealExp> l) {
    l.add(this);
    return l;
  }

  public Set<RealVar> collectVars(Set<RealVar> s) {
    s.add(this);
    return s;
  }

  public boolean isolate(RealVar var, List<RealExp> wx, List<RealExp> wox) {
      return this == var;
  }

 
}
