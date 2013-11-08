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

package choco.ecp.solver.constraints.real.exp;

import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.variables.real.PalmRealInterval;
import choco.ecp.solver.variables.real.PalmRealMath;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.real.RealExp;

/**
 * Implementation of a real addition expression.
 */
public class PalmRealPlus extends AbstractPalmRealBinTerm {
  /**
   * Creates the addition of two sub expressions.
   */
  public PalmRealPlus(Solver pb, RealExp exp1, RealExp exp2) {
    super(pb, exp1, exp2);
  }

  /**
   * Tightens the value of the expressions, that is affects values with respect to the values
   * (and explanations) of sub expressions.
   */
  public void tighten() {
    PalmRealInterval res = PalmRealMath.add((PalmSolver) this.getSolver(), exp1, exp2);
    inf.set(res.getInf());
    sup.set(res.getSup());
    explanationOnInf.empties();
    res.self_explain(INF, explanationOnInf);
    explanationOnSup.empties();
    res.self_explain(SUP, explanationOnSup);
  }

  /**
   * Projects current value on sub-expressions.
   *
   * @throws ContradictionException
   */
  public void project() throws ContradictionException {
    exp1.intersect(PalmRealMath.sub((PalmSolver) this.getSolver(), this, exp2));
    exp2.intersect(PalmRealMath.sub((PalmSolver) this.getSolver(), this, exp1));
  }
}
