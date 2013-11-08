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

package choco.ecp.solver.variables.real;

import choco.cp.solver.variables.real.RealVarImpl;
import choco.ecp.solver.constraints.real.PalmSplitLeft;
import choco.ecp.solver.constraints.real.PalmSplitRightS;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.PalmConstraintPlugin;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.propagation.PalmRealVarEvent;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.real.RealIntervalConstant;
import choco.kernel.solver.variables.real.RealVar;

/**
 * Default implementation of PaLM real variables.
 */
public class PalmRealVarImpl extends RealVarImpl implements PalmRealVar {

  /**
   * Creates a variable with the specified bounds and the specified name.
   */
  public PalmRealVarImpl(Solver pb, String name, double a, double b) {
    super(pb, name, a, b, RealVar.BOUNDS);

    // Helps GC
    this.event = null;
    this.domain = null;

    // New Palm event and domain
    this.event = new PalmRealVarEvent(this);
    this.domain = new PalmRealDomainImpl(this, a, b);
  }

  /**
   * The name of the variable.
   */
  public String toString() {
    return name;
  }

  /**
   * Updates lower bound explanations: it removes not up-to-date explanations.
   */
  public void resetExplanationOnInf() {
    ((PalmRealDomain) this.domain).resetExplanationOnInf();
  }

  /**
   * Updates upper bound explanations: it removes not up-to-date explanations.
   */
  public void resetExplanationOnSup() {
    ((PalmRealDomain) this.domain).resetExplanationOnSup();
  }

  /**
   * Updates decision constraints on this variable: it removes all erased constraints.
   */
  public void updateDecisionConstraints() {
    ((PalmRealDomain) this.domain).updateDecisionConstraints();
  }

  /**
   * Lower bound of the domain should be restored to the specified value.
   */
  public void restoreInf(double newValue) {
    ((PalmRealDomain) this.domain).restoreInf(newValue);
  }

  /**
   * Upper bound of the domain should be restored to the specified value.
   */
  public void restoreSup(double newValue) {
    ((PalmRealDomain) this.domain).restoreSup(newValue);
  }

  /**
   * Merge explanation of the specified part of the domain to a constraint collection.
   */
  public void self_explain(int select, Explanation e) {
    ((PalmRealDomain) this.domain).self_explain(select, e);
  }

  public SConstraint getDecisionConstraint(int val) {
    AbstractSConstraint cst = null;
    if (val == 1)
      cst = new PalmSplitLeft(this, new RealIntervalConstant(this));
    else // val == 2
      cst = new PalmSplitRightS(this, new RealIntervalConstant(this));
    PalmExplanation expl = (PalmExplanation) ((PalmRealDomain) this.getDomain()).getDecisionConstraints();
    if (expl.size() > 0) ((PalmConstraintPlugin) cst.getPlugIn()).setDepending(expl);
    return cst;
  }

  /**
   * Adds a new constraints, and makes it active if needed.
   */
  // TODO: this needs to become backtrackable
  public int addConstraint(SConstraint c, int varIdx) {
    int idx;
    constraints.staticAdd(c);
    indices.staticAdd(varIdx);
    idx = constraints.size() - 1;
    return idx;
  }
}
