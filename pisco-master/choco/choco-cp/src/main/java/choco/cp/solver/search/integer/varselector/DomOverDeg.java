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

package choco.cp.solver.search.integer.varselector;

import choco.cp.solver.search.BranchingFactory;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.integer.DoubleHeuristicIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A heuristic selecting the {@link choco.cp.solver.variables.integer.IntDomainVarImpl} with smallest ration (domainSize / degree)
 * (the degree of a variable is the number of constraints linked to it)
 * @deprecated @see {@link BranchingFactory}
 */
@Deprecated
public class DomOverDeg extends DoubleHeuristicIntVarSelector {
  public DomOverDeg(Solver solver) {
    super(solver);

  }

  public DomOverDeg(Solver solver, IntDomainVar[] vs) {
    super(solver, vs);
  }

  public double getHeuristic(IntDomainVar v) {
    int dsize = v.getDomainSize();
    int deg = v.getNbConstraints();
    if (deg == 0)
      return Double.POSITIVE_INFINITY;
    else
      return (double) dsize / (double) deg;
  }
}
