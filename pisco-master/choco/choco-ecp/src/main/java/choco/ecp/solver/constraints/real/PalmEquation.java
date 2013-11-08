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
// ______\_/_______     Contibutors: Fran?ois Laburthe, Hadrien Cambazard, Guillaume Rochart...

package choco.ecp.solver.constraints.real;

import choco.cp.solver.constraints.real.Equation;
import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.constraints.real.exp.PalmRealIntervalConstant;
import choco.ecp.solver.explanations.ExplainedSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.PalmConstraintPlugin;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.propagation.PalmRealVarListener;
import choco.ecp.solver.propagation.dbt.PalmEngine;
import choco.ecp.solver.search.dbt.PalmContradiction;
import choco.ecp.solver.variables.integer.dbt.PalmIntDomain;
import choco.ecp.solver.variables.integer.dbt.PalmIntVar;
import choco.ecp.solver.variables.real.PalmRealInterval;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.real.RealExp;
import choco.kernel.solver.constraints.real.RealSConstraint;
import choco.kernel.solver.variables.real.RealInterval;
import choco.kernel.solver.variables.real.RealIntervalConstant;
import choco.kernel.solver.variables.real.RealMath;
import choco.kernel.solver.variables.real.RealVar;


/**
 * Implements a constraint based on real expression that ensures that an equality
 * is true. It is based on revisited hull consistency and can use box consistency
 * for some variables.
 */
public class PalmEquation extends Equation implements RealSConstraint, PalmRealVarListener {
  /**
   * Creates an equation with an equality wetween a real expression and the
   * constant insterval for the provided problem.
   * An expression instance should be associated to only one constraint.
   */
  public PalmEquation(final Solver pb, RealVar[] collectedVars, RealExp exp, RealInterval cste) {
    super(pb, collectedVars, exp, cste);
    this.hook = new PalmConstraintPlugin(this);
    this.cste = cste;
  }

  /**
   * First propagation: update the explanation of the constant interval with
   * this constarint (since now we can be sure this constraint is posted).
   *
   * @throws ContradictionException
   */
  public void awake() throws ContradictionException {
    Explanation expl = ((PalmSolver) this.getSolver()).makeExplanation();
    ((PalmConstraintPlugin) this.hook).self_explain(expl);
    this.cste = new PalmRealIntervalConstant(cste.getInf(), cste.getSup(), (PalmExplanation) expl.copy(), expl);
    super.awake();
  }

  /**
   * On lower bound restoration, make the generic propagation called.
   *
   * @throws ContradictionException
   */
  public void awakeOnRestoreInf(int idx) throws ContradictionException {
    this.constAwake(false);
  }

  /**
   * On upper bound restoration, make the generic propagation called.
   *
   * @throws ContradictionException
   */
  public void awakeOnRestoreSup(int idx) throws ContradictionException {
    this.constAwake(false);
  }

  /**
   * Private method checking if a sub interval is consitent during box consistency
   * algorithm.
   *
   * @return
   */
  private boolean not_inconsistent(RealExp[] wx, Explanation expl) {
    ((PalmConstraintPlugin) this.hook).self_explain(expl);
    try {
      tighten(wx);
    } catch (PalmContradiction e) {
      PalmIntVar cause = (PalmIntVar) this.getSolver().getPropagationEngine().getContradictionCause();
      cause.self_explain(PalmIntDomain.DOM, expl);
      cause.restoreInf(1);
      cause.resetExplanationOnInf();
      ((PalmEngine) this.getSolver().getPropagationEngine()).setContradictory(false);
      return false;
    } catch (ContradictionException e) {
      System.err.println("Should not happen: Bug in PalmEquation !");
    }
    if (exp.getInf() > cste.getSup()) {
      ((PalmRealInterval) this.exp).self_explain(PalmRealInterval.INF, expl);
      return false;
    } else if (exp.getSup() < cste.getInf()) {
      ((PalmRealInterval) this.exp).self_explain(PalmRealInterval.SUP, expl);
      return false;
    }
    return true;
  }

  /**
   * Box consistency algorithm on one variable
   *
   * @throws ContradictionException
   */
  protected void bc(RealVar var, RealExp[] wx, RealExp[] wox) throws ContradictionException {
    RealInterval[] unexplored = new RealInterval[this.boxConsistencyDepth * 2];
    int[] depths = new int[this.boxConsistencyDepth * 2];
    int depth = 0;
    int idx = 0;
    boolean fin = false;

    double leftB = 0, rightB = 0;
    Explanation expOnInf, expOnSup;
    expOnInf = ((PalmSolver) this.getSolver()).makeExplanation();
    ((PalmRealInterval) var).self_explain(PalmRealInterval.INF, expOnInf);
    expOnSup = ((PalmSolver) this.getSolver()).makeExplanation();
    ((PalmRealInterval) var).self_explain(PalmRealInterval.SUP, expOnSup);
    RealInterval oldValue = new RealIntervalConstant(var);

    tighten(wox);

    // Left bound !
    while (!fin) {
      if (not_inconsistent(wx, expOnInf)) {
        if (this.boxConsistencyDepth <= depth) {
          leftB = var.getInf();
          rightB = var.getSup(); // Valeur provisoire
          fin = true;
        } else {
          RealInterval left = RealMath.firstHalf(var);
          RealInterval right = RealMath.secondHalf(var);

          var.silentlyAssign(left);
          depth++;
          unexplored[idx] = right;
          depths[idx] = depth;
          idx++;
        }
      } else if (idx != 0) {
        var.silentlyAssign(unexplored[--idx]);
        depth = depths[idx];
      } else {
        var.silentlyAssign(oldValue);
        var.intersect(oldValue);
        ((PalmRealInterval) var).self_explain(PalmRealInterval.SUP, expOnInf);
        ((ExplainedSolver) this.getSolver()).explainedFail(expOnInf);
      }
    }

    // Reversing not explored intervals (in order to avoid to check already checked parts of the search space.

    RealInterval[] tmp1 = new RealInterval[this.boxConsistencyDepth * 2];
    int[] tmp2 = new int[this.boxConsistencyDepth * 2];

    for (int i = 0; i < idx; i++) {
      int j = idx - i - 1;
      tmp1[i] = unexplored[j];
      tmp2[i] = depths[j];
    }

    unexplored = tmp1;
    depths = tmp2;

    // Right bound if needed
    if (idx != 0) {
      var.silentlyAssign(unexplored[--idx]);
      depth = depths[idx];
      fin = false;

      while (!fin) {
        if (not_inconsistent(wx, expOnSup)) {
          if (this.boxConsistencyDepth <= depth) {
            rightB = var.getSup();
            fin = true;
          } else {
            RealInterval left = RealMath.firstHalf(var);
            RealInterval right = RealMath.secondHalf(var);

            var.silentlyAssign(right);
            depth++;
            unexplored[idx] = left;
            depths[idx] = depth;
            idx++;
          }
        } else if (idx != 0) {
          var.silentlyAssign(unexplored[--idx]);
          depth = depths[idx];
        } else {
          fin = true;
        }
      }
    }

    // Propagation
    var.silentlyAssign(oldValue);
    var.intersect(new PalmRealIntervalConstant(leftB, rightB, expOnInf, expOnSup));
  }

  /**
   * No synchronous event handling (void method).
   */
  public void updateDataStructuresOnConstraint(int idx, int select, int newValue, int oldValue) {
  }

  /**
   * No synchronous event handling (void method).
   */
  public void updateDataStructuresOnRestoreConstraint(int idx, int select, int newValue, int oldValue) {
  }

  public void tighten(RealExp[] exps) throws ContradictionException {
    for (int i = 0; i < exps.length; i++) {
      RealExp exp = exps[i];
      exp.tighten();
      if (exp.getInf() > exp.getSup()) {
        Explanation e = ((PalmSolver) this.getSolver()).makeExplanation();
        ((PalmConstraintPlugin) this.hook).self_explain(e);
        ((PalmRealInterval) exp).self_explain(PalmRealInterval.DOM, e);
        ((ExplainedSolver) this.getSolver()).explainedFail(e);
      }
    }
  }
}
