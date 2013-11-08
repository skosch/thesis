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

import choco.cp.solver.variables.real.RealDomainImpl;
import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.explanations.real.RealBoundExplanation;
import choco.ecp.solver.propagation.dbt.PalmEngine;
import choco.kernel.common.util.objects.ConstraintCollection;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.real.RealInterval;
import choco.kernel.solver.variables.real.RealVar;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Default implementation of PaLM real domain.
 */
public class PalmRealDomainImpl extends RealDomainImpl implements PalmRealDomain {
  /**
   * A stack of explanations for lower bound modifications.
   */
  protected final LinkedList explanationOnInf;

  /**
   * A stack of explanations for upper bound modifications.
   */
  protected final LinkedList explanationOnSup;

  /**
   * Original lower bound.
   */
  protected final double originalInf;

  /**
   * Original upper bound.
   */
  protected final double originalSup;

  /**
   * All active decision constraints on this variable (except the last one).
   */
  protected PalmExplanation decisionConstraints;

  /**
   * Last decision constraint.
   */
  protected SConstraint lastDC;

  /**
   * States if modfication should be quiet, that is no PaLM must be created when the bounds are
   * modified.
   */
  protected boolean silent = false;

  /**
   * Creates a real domain for the specified variable.
   */
  public PalmRealDomainImpl(RealVar v, double a, double b) {
    super(v, a, b);
    PalmSolver pb = (PalmSolver) this.getProblem();
    this.explanationOnInf = new LinkedList();
    this.explanationOnSup = new LinkedList();
    this.explanationOnInf.add(((PalmExplanation) pb.makeExplanation()).makeIncInfExplanation(this.getInf(), (PalmRealVar) this.variable));
    this.explanationOnSup.add(((PalmExplanation) pb.makeExplanation()).makeDecSupExplanation(this.getSup(), (PalmRealVar) this.variable));
    this.originalInf = a;
    this.originalSup = b;
    this.decisionConstraints = (PalmExplanation) pb.makeExplanation();
  }

  /**
   * Returns all decision constraints.
   */
  public ConstraintCollection getDecisionConstraints() {
    return decisionConstraints.copy();
  }

  /**
   * Adds a new decision constraint on this variable.
   */
  public void addDecisionConstraint(AbstractSConstraint cst) {
    decisionConstraints.add(cst);
  }

  /**
   * Returns the original lower bound.
   */
  public double getOriginalInf() {
    return this.originalInf;
  }


  /**
   * Returns the original upper bound.
   */
  public double getOriginalSup() {
    return this.originalSup;
  }

  /**
   * Makes this domain be included in the specified interval.
   *
   * @throws ContradictionException
   */
  public void intersect(RealInterval interval, int index) throws ContradictionException {
    silent = false;
    boolean modified = false;
    double old_width = this.getSup() - this.getInf();
    double new_width = Math.min(interval.getSup(), this.getSup()) -
        Math.max(interval.getInf(), this.getInf());
    boolean toAwake = (variable.getProblem().getPrecision() / 100. <= old_width)
        && (new_width < old_width * variable.getProblem().getReduction());

    double oldInf = this.getInf();
    double newInf = interval.getInf();
    if (newInf > oldInf) {
      modified = true;
      PalmExplanation e = (PalmExplanation) ((PalmSolver) this.getProblem()).makeExplanation();
      ((PalmRealInterval) interval).self_explain(PalmRealInterval.INF, e); // New bound
      this.self_explain(PalmRealInterval.INF, e); //  Old bound ... TODO : really needed ?!
      explanationOnInf.add(e.makeIncInfExplanation(oldInf, (PalmRealVar) this.variable));
      this.inf.set(newInf);
      //RealStructureMaintainer.updateDataStructures(this.variable, INF, newInf, oldInf); // TODO
      if (toAwake) ((PalmEngine) this.getProblem().getPropagationEngine()).postUpdateInf(this.variable, index);
    }
    double oldSup = this.getSup();
    double newSup = interval.getSup();
    if (newSup < oldSup) {
      modified = true;
      PalmExplanation e = (PalmExplanation) ((PalmSolver) this.getProblem()).makeExplanation();
      ((PalmRealInterval) interval).self_explain(PalmRealInterval.SUP, e);
      this.self_explain(PalmRealInterval.SUP, e);
      explanationOnSup.add(e.makeDecSupExplanation(oldSup, (PalmRealVar) this.variable));
      this.sup.set(newSup);
      // RealStructureMaintainer.updateDataStructure(this.variable, SUP, newSup, oldSup); // TODO
      if (toAwake) ((PalmEngine) this.getProblem().getPropagationEngine()).postUpdateSup(this.variable, index);
    }

    if (modified && this.getInf() > this.getSup()) {
      ((PalmEngine) this.getProblem().getPropagationEngine()).raisePalmContradiction(this.variable);
    }
  }

  /**
   * Quietly assign this domain to the values of the specified interval. No PaLM events are trown.
   */
  public void silentlyAssign(RealInterval i) {
    inf.set(i.getInf());
    sup.set(i.getSup());
    silent = true;
  }

  /**
   * Explains the state of this domain
   *
   * @param select The part of the domain that should be explained
   * @param e      Constraint collection this explanation must be added to.
   */
  public void self_explain(int select, Explanation e) {
    if (!silent)
      switch (select) {
        case PalmRealInterval.INF:
          e.merge((ConstraintCollection) this.explanationOnInf.getLast());
          break;
        case PalmRealInterval.SUP:
          e.merge((ConstraintCollection) this.explanationOnSup.getLast());
          break;
        case PalmRealInterval.DOM:
          e.merge((ConstraintCollection) this.explanationOnInf.getLast());
          e.merge((ConstraintCollection) this.explanationOnSup.getLast());
          break;
      }
  }

  /**
   * Restores lower bound to the specified value.
   */
  public void restoreInf(double newValue) {
    if (this.getInf() > newValue) {
      double oldValue = this.getInf();
      this.inf.set(newValue);
      // RealStructureMaintainer.updateDataStructuresOnRestore(this.variable, INF, newValue, oldValue); // TODO
      ((PalmEngine) this.getProblem().getPropagationEngine()).postRestoreInf((PalmRealVar) this.variable);
    }
  }

  /**
   * Restores upper bound to the specified value.
   */
  public void restoreSup(double newValue) {
    if (this.getSup() < newValue) {
      double oldValue = this.getSup();
      this.sup.set(newValue);
      // RealStructureMaintainer.updateDataStructuresOnRestore(this.variable, SUP, newValue, oldValue); // TODO
      ((PalmEngine) this.getProblem().getPropagationEngine()).postRestoreSup((PalmRealVar) this.variable);
    }
  }

  /**
   * Reset lower bound explanatioins: not up-to-date explanations are removed.
   */
  public void resetExplanationOnInf() {
    boolean keep = true;
    for (ListIterator iterator = explanationOnInf.listIterator(); iterator.hasNext();) {
      RealBoundExplanation expl = (RealBoundExplanation) iterator.next();
      if (expl.getPreviousValue() >= this.getInf()) {
        if (expl.getPreviousValue() == this.getOriginalInf() && keep) {
          keep = false;
        } else {
          iterator.remove();
        }
      }
    }
  }

  /**
   * Reset upper bound explanatioins: not up-to-date explanations are removed.
   */
  public void resetExplanationOnSup() {
    boolean keep = true;
    for (ListIterator iterator = explanationOnSup.listIterator(); iterator.hasNext();) {
      RealBoundExplanation expl = (RealBoundExplanation) iterator.next();
      if (expl.getPreviousValue() <= this.getSup()) {
        if (expl.getPreviousValue() == this.getOriginalSup() && keep) {
          keep = false;
        } else {
          iterator.remove();
        }
      }
    }
  }

  /**
   * Updates decision constraints by removing not up-to-date ones.
   */
  public void updateDecisionConstraints() {
    java.util.BitSet constraints = decisionConstraints.getBitSet();
    for (int i = constraints.nextSetBit(0); i >= 0; i = constraints.nextSetBit(i + 1)) {
      if (((PalmSolver) this.problem).getConstraintNb(i) == null || !((PalmSolver) this.problem).getConstraintNb(i).isActive()) {
        constraints.clear(i);
      }
    }
  }
}
