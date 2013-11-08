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
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.variables.real.PalmRealInterval;
import choco.kernel.common.util.objects.ConstraintCollection;
import choco.kernel.solver.variables.real.RealIntervalConstant;

/**
 * Implementation of a constant interval.
 */
public class PalmRealIntervalConstant extends RealIntervalConstant implements PalmRealInterval {
  /**
   * The lower bound explanation if needed.
   */
  PalmExplanation explanationOnInf;

  /**
   * The upper bound explanation if needed.
   */
  PalmExplanation explanationOnSup;

  /**
   * Creates a constant from the current state of an interval.
   *
   * @param pb The current problem (needed for creating explanations).
   */
  public PalmRealIntervalConstant(PalmRealInterval interval, PalmSolver pb) {
    super(interval);
    explanationOnInf = (PalmExplanation) pb.makeExplanation();
    explanationOnSup = (PalmExplanation) pb.makeExplanation();
    interval.self_explain(PalmRealInterval.INF, explanationOnInf);
    interval.self_explain(PalmRealInterval.SUP, explanationOnSup);
  }

  /**
   * Creates a constant with the specified values.
   */
  public PalmRealIntervalConstant(double inf, double sup, Explanation expOnInf, Explanation expOnSup) {
    super(inf, sup);
    explanationOnInf = (PalmExplanation) expOnInf;
    explanationOnSup = (PalmExplanation) expOnSup;
  }

  /**
   * Creates a constant without explanations from the current state of the interval.
   */
  public PalmRealIntervalConstant(PalmRealInterval interval) {
    super(interval);
    explanationOnInf = null;
    explanationOnSup = null;
  }

  /**
   * Creates a constant without explanations.
   */
  public PalmRealIntervalConstant(double inf, double sup) {
    super(inf, sup);
    explanationOnInf = null;
    explanationOnSup = null;
  }

  /**
   * Explains the values of the constant.
   *
   * @param select The part if the domain that should be explained.
   * @param e      The constraint collection in which this explanation should be added.
   */
  public void self_explain(int select, Explanation e) {
    if (explanationOnInf != null)
      switch (select) { // Sinon explication vide...
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
      }
  }
}
