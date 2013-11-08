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

package choco.ecp.solver.search;

import choco.ecp.solver.search.dbt.PalmAbstractBranchAndBound;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.real.RealMath;
import choco.kernel.solver.variables.real.RealVar;

/**
 * J-CHOCO
 * Copyright (C) F. Laburthe, 1999-2003
 * <p/>
 * An open-source Constraint Programming Kernel
 * for Research and Education
 * <p/>
 * Created by: Guillaume on 20 juil. 2004
 */
public class PalmRealBranchAndBound extends PalmAbstractBranchAndBound {
  /**
   * Some bounds for search use.
   */

  protected double lowerBound, upperBound;


  /**
   * Optimum value found during the search.
   */

  protected double optimum;

  /**
   * Creates the solver for the specified problepb.
   */

  public PalmRealBranchAndBound(Solver pb, RealVar obj, boolean max) {
    super(pb, obj, max);
    lowerBound = obj.getInf();
    upperBound = obj.getSup();
  }

  public SConstraint getDynamicCut() {
    double bv = getObjectiveValue();
    if (maximizing)
      lowerBound = RealMath.nextFloat(Math.max(lowerBound, bv));
    else
      upperBound = RealMath.prevFloat(Math.min(upperBound, bv));
    if (maximizing)
      return solver.geq((RealVar) objective, lowerBound);
    //new PalmGreaterOrEqualXC((IntDomainVar) objective, bv + 1);
    else
      return solver.leq((RealVar) objective, upperBound);
    //new PalmLessOrEqualXC((IntDomainVar) objective, bv - 1);
  }

  private double getObjectiveValue() {
    if (maximizing) {
      optimum = ((RealVar) objective).getSup();
      return optimum;
    } else {
      optimum = ((RealVar) objective).getInf();
      return optimum;
    }
  }

  public Number getOptimumValue() {
    return new Double(optimum);
  }
}
