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
// ______\_/_______     Contibutors: Fran�ois Laburthe, Hadrien Cambazard, 
//                                   Guillaume Rochart...

package choco.ecp.solver.constraints.real.exp;

import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.explanations.ExplainedSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.propagation.dbt.PalmEngine;
import choco.ecp.solver.variables.real.PalmRealInterval;
import choco.kernel.common.util.objects.ConstraintCollection;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.real.RealExp;
import choco.kernel.solver.constraints.real.exp.AbstractRealBinTerm;
import choco.kernel.solver.variables.real.RealInterval;

/**
 * Abstract implementation of a real binary term (like addition for instance).
 */
public abstract class AbstractPalmRealBinTerm extends AbstractRealBinTerm
    implements PalmRealInterval {
  /**
   * Explanation of the last lower bound affectation.
   */
  protected Explanation explanationOnInf;

  /**
   * Explanation of the last upper bound affectation.
   */
  protected Explanation explanationOnSup;

  /**
   * Creates an abstract binary term with the two sub-expressions.
   * @param pb the problem of this constraint
   * @param exp1 the first expression operand
   * @param exp2 the second expression operand
   */
  public AbstractPalmRealBinTerm(final Solver pb,
      final RealExp exp1, final RealExp exp2) {
    super(pb, exp1, exp2);
    explanationOnInf = ((ExplainedSolver) pb).makeExplanation();
    explanationOnSup = ((ExplainedSolver) pb).makeExplanation();
  }

  /**
   * Updates the interval such that this interval is
   * included in the interval parameter.
   * @param interval The interval this expression should be included in
   * @param index    The index of the constraint responsible of this reduction
   * @throws ContradictionException if a domain becomes empty or if a
   * contradiction can be infered
   */
  public void intersect(final RealInterval interval, final int index)
  throws ContradictionException {
    if (interval.getInf() > inf.get()) {
      inf.set(interval.getInf());
      explanationOnInf.empties();
      ((PalmRealInterval) interval).self_explain(PalmRealInterval.INF, explanationOnInf);
    }
    if (interval.getSup() < sup.get()) {
      sup.set(interval.getSup());
      explanationOnSup.empties();
      ((PalmRealInterval) interval).self_explain(PalmRealInterval.SUP, explanationOnSup);
    }
    if (inf.get() > sup.get()) {
      PalmExplanation e = (PalmExplanation)
      ((PalmSolver) this.getSolver()).makeExplanation();
      this.self_explain(PalmRealInterval.DOM, e);
      ((PalmEngine) this.getSolver().getPropagationEngine())
      .raisePalmFakeContradiction(e);
    }
  }

  /**
   * Explains the state of this expression (lower/upper bounds or all domain).
   * @param select Specifies which part of the domain should be explained.
   * @param e      Specifies the explanation in which these explaining 
   * constraints should be added.
   */
  public void self_explain(final int select, final Explanation e) {
    switch (select) {
      case PalmRealInterval.INF:
        e.merge((ConstraintCollection) this.explanationOnInf);
        break;
      case PalmRealInterval.SUP:
        e.merge((ConstraintCollection) this.explanationOnSup);
        break;
      case PalmRealInterval.DOM:
        e.merge((ConstraintCollection) this.explanationOnInf);
        e.merge((ConstraintCollection) this.explanationOnSup);
        break;
      default:
        throw new Error("Invalid request of explanation.");
    }
  }
}
