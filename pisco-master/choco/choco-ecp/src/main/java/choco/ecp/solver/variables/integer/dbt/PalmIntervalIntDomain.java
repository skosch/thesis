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

package choco.ecp.solver.variables.integer.dbt;

import choco.cp.solver.variables.integer.IntDomainVarImpl;
import choco.cp.solver.variables.integer.IntervalIntDomain;
import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.constraints.integer.PalmAssignment;
import choco.ecp.solver.constraints.integer.PalmNotEqualXC;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.explanations.integer.IBoundExplanation;
import choco.ecp.solver.propagation.dbt.PalmEngine;
import choco.ecp.solver.propagation.dbt.StructureMaintainer;
import choco.kernel.common.util.objects.ConstraintCollection;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.VarEvent;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PalmIntervalIntDomain extends IntervalIntDomain implements PalmIntDomain {
  /**
   * A stack of explanations for lower bound modifications.
   */

  protected final LinkedList<IBoundExplanation> explanationOnInf;


  /**
   * A stack of explanations for upper bound modifications.
   */

  protected final LinkedList<IBoundExplanation> explanationOnSup;


  /**
   * Decision constraints on the variable for branching purpose.
   */

  protected final Hashtable<Integer, SConstraint> decisionConstraints;


  /**
   * Negation of decision constraints on the variable for branching purpose.
   */

  protected final Hashtable<Integer, SConstraint> negDecisionConstraints;


  /**
   * Original lower bound.
   */

  protected final int originalInf;


  /**
   * Original upper bound.
   */

  protected final int originalSup;


  /**
   * Builds a interval domain for the specified variable.
   *
   * @param v Involved variable.
   * @param a Lower bound.
   * @param b Upper bound.
   */

  public PalmIntervalIntDomain(IntDomainVarImpl v, int a, int b) {
    super(v, a, b);
    PalmSolver pb = (PalmSolver) this.getSolver();
    this.explanationOnInf = new LinkedList<IBoundExplanation>();
    this.explanationOnSup = new LinkedList<IBoundExplanation>();
    this.explanationOnInf.add(((PalmExplanation) (pb.makeExplanation())).makeIncInfExplanation(this.getInf(), (PalmIntVar) this.variable));
    this.explanationOnSup.add(((PalmExplanation) (pb.makeExplanation())).makeDecSupExplanation(this.getSup(), (PalmIntVar) this.variable));
    this.decisionConstraints = new Hashtable<Integer, SConstraint>();
    this.negDecisionConstraints = new Hashtable<Integer, SConstraint>();
    this.originalInf = a;
    this.originalSup = b;
  }


  /**
   * Returns the original lower bound.
   */

  public int getOriginalInf() {
    return this.originalInf;
  }


  /**
   * Returns the original upper bound.
   */

  public int getOriginalSup() {
    return this.originalSup;
  }


  /**
   * Returns all the value currently in the domain.
   */

  public int[] getAllValues() {
    //if (bucket != null) {
    //    return bucket.domainSet();
    //} else {
    int[] ret = new int[getSup() - getInf() + 1];
    for (int i = 0; i < ret.length; i++)
      ret[i] = getInf() + i;
    return ret;
    //}
  }


  /**
   * Returns the decision constraint assigning the domain to the specified value. The constraint is created if
   * it is not yet created.
   */

  public SConstraint getDecisionConstraint(int val) {
    SConstraint cons = this.decisionConstraints.get(new Integer(val - this.getOriginalInf()));
    if (cons != null) {
      return cons;
    } else {
      cons = new PalmAssignment(this.variable, val);
      this.decisionConstraints.put(val - this.getOriginalInf(), cons);
      this.negDecisionConstraints.put(val - this.getOriginalInf(), new PalmNotEqualXC(this.variable, val));
      return cons;
    }
  }


  /**
   * Returns the negated decision constraint.
   */

  public SConstraint getNegDecisionConstraint(int val) {
    return this.negDecisionConstraints.get(new Integer(val - this.getOriginalInf()));
  }


  /**
   * Updates the upper bound and posts the event.
   */

  public boolean updateSup(int x, int idx, Explanation e) throws ContradictionException {
    if (this.updateSup(x, e)) {
      int cause = VarEvent.NOCAUSE;
      if (x == this.getSup()) cause = idx;
      this.getSolver().getPropagationEngine().postUpdateSup(this.variable, cause);
      if (x < this.getInf()) {
          //TODO: a garder?
        this.variable.setVal(IStateInt.UNKNOWN_INT);
        ((PalmEngine) this.getSolver().getPropagationEngine()).raisePalmContradiction(this.variable);
      }
      return true;
    }
    return false;
  }


  /**
   * Updates the lower bound and posts the event.
   */

  public boolean updateInf(int x, int idx, Explanation e) throws ContradictionException {
    if (this.updateInf(x, e)) {
      int cause = VarEvent.NOCAUSE;
      if (x == this.getInf()) cause = idx;
      this.getSolver().getPropagationEngine().postUpdateInf(this.variable, cause);
      if (x > this.getSup()) {
          //TODO: a garder?
        this.variable.setSup(IStateInt.UNKNOWN_INT);
        ((PalmEngine) this.getSolver().getPropagationEngine()).raisePalmContradiction(this.variable);
      }
      return true;
    }
    return false;
  }


  /**
   * Removes a value and posts the event.
   */

  public boolean removeVal(int value, int idx, Explanation e) throws ContradictionException {
    if (value == this.getInf()) {
      return this.updateInf(value + 1, idx, e);
    } else if (value == this.getSup()) {
      return this.updateSup(value - 1, idx, e);
    }
    return false;
  }


  /**
   * Restores a lower bound and posts the event.
   */

  public void restoreInf(int newValue) throws ContradictionException {
    if (this.getInf() > newValue) {
      int oldValue = this.getInf();
      this.inf.set(newValue);
      if (this.getInf() != this.getSup()) {
          //TODO: a garder?
        this.variable.setVal(IStateInt.UNKNOWN_INT);
      } else {
          //TODO: a garder?
        this.variable.setVal(this.getInf());
      }
      StructureMaintainer.updateDataStructuresOnRestore(this.variable, INF, newValue, oldValue);
      //((PalmIntVar) this.variable).updateDataStructuresOnRestore(PalmIntVar.INF, newValue, oldValue);
      ((PalmEngine) this.getSolver().getPropagationEngine()).postRestoreInf((PalmIntVar) this.variable);
    }
  }


  /**
   * Restores an upper bound and posts the event.
   */

  public void restoreSup(int newValue) throws ContradictionException {
    if (this.getSup() < newValue) {
      int oldValue = this.getSup();
      this.sup.set(newValue);
      if (this.getInf() != this.getSup()) {
          //TODO: a garder?
        this.variable.setVal(IStateInt.UNKNOWN_INT);
      } else {
        this.variable.setVal(this.getInf());
      }
      StructureMaintainer.updateDataStructuresOnRestore(this.variable, SUP, newValue, oldValue);
      //((PalmIntVar) this.variable).updateDataStructuresOnRestore(PalmIntVar.SUP, newValue, oldValue);
      ((PalmEngine) this.getSolver().getPropagationEngine()).postRestoreSup((PalmIntVar) this.variable);
    }
  }


  /**
   * Restores a value and posts the event. Not supported for such a domain.
   */

  public void restoreVal(int val) {
    System.err.println("restoreVal should not be called on a IntervalIntdomain !");
  }


  /**
   * Allows to get an explanation for the domain or a bound of the variable. This explanation is merge to the
   * explanation in parameter.
   *
   * @param select Should be <code>PalmIntDomain.INF</code>, <code>PalmIntDomain.SUP</code>, or <code>PalmIntDomain.DOM</code>
   */

  public void self_explain(int select, Explanation expl) {
    switch (select) {
      case DOM:
        this.self_explain(INF, expl);
        this.self_explain(SUP, expl);
        break;
      case INF:
        expl.merge((Explanation) this.explanationOnInf.getLast());
        break;
      case SUP:
        expl.merge((Explanation) this.explanationOnSup.getLast());
        break;
      default:
        if (Logger.getLogger("choco").isLoggable(Level.WARNING))
          Logger.getLogger("choco").warning("PaLM: VAL needs another parameter in self_explain (IntDomainVar)");
    }
  }


  /**
   * Allows to get an explanation for a value removal from the variable. This explanation is merge to the
   * explanation in parameter.
   *
   * @param select Should be <code>PalmIntDomain.VAL</code>
   */

  public void self_explain(int select, int x, Explanation expl) {
    if (select == VAL) {
      // TODO : on ne peut pas prendre une explication plus precise ?
      if (x < this.getInf())
        expl.merge((ConstraintCollection) this.explanationOnInf.getLast());
      else if (x > this.getSup())
        expl.merge((ConstraintCollection) this.explanationOnSup.getLast());
    } else {
      if (Logger.getLogger("choco").isLoggable(Level.WARNING))
        Logger.getLogger("choco").warning("PaLM: INF, SUP or DOM do not need a supplementary parameter in self_explain (IntDomainVar)");
    }
  }


  /**
   * When a value is restored, it deletes the explanation associated to the value removal.
   */

  public void resetExplanationOnVal(int val) {
  }


  /**
   * When a lower bound is restored, it deletes the explanation associated to the value removal.
   */

  public void resetExplanationOnInf() {
    boolean keep = true;
    for (ListIterator iterator = explanationOnInf.listIterator(); iterator.hasNext();) {
      IBoundExplanation expl = (IBoundExplanation) iterator.next();
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
   * When an upper bound is restored, it deletes the explanation associated to the value removal.
   */

  public void resetExplanationOnSup() {
    boolean keep = true;
    for (ListIterator iterator = explanationOnSup.listIterator(); iterator.hasNext();) {
      IBoundExplanation expl = (IBoundExplanation) iterator.next();
      if (expl.getPreviousValue() <= this.getSup()) {
        if (expl.getPreviousValue() == this.getOriginalSup() && keep) {
          keep = false;
        } else {
          iterator.remove();
        }
      }
    }
  }

  protected boolean updateSup(int x, Explanation e) throws ContradictionException {
    if (x < this.getSup()) {
      int oldValue = this.getSup();
      ((PalmIntVar) this.variable).self_explain(SUP, e);
      this.explanationOnSup.add(((PalmExplanation) e).makeDecSupExplanation(this.getSup(), (PalmIntVar) this.variable));
      this.updateSup(x);
      if (this.inf.get() == this.sup.get()) {
          //TODO: a garder?
        this.variable.setVal(this.getInf());
      }
      StructureMaintainer.updateDataStructures(this.variable, SUP, x, oldValue);
      //((PalmIntVar) this.variable).updateDataStructures(PalmIntVar.SUP, x, oldValue);
      return true;
    }
    return false;
  }

  protected boolean updateInf(int x, Explanation e) throws ContradictionException {
    if (x > this.getInf()) {
      int oldValue = this.getInf();
      ((PalmIntVar) this.variable).self_explain(INF, e);
      this.explanationOnInf.add(((PalmExplanation) e).makeIncInfExplanation(this.getInf(), (PalmIntVar) this.variable));
      this.updateInf(x);
      if (this.inf.get() == this.sup.get()) {
        this.variable.setVal(this.getInf());
      }
      StructureMaintainer.updateDataStructures(this.variable, INF, x, oldValue);
      //((PalmIntVar) this.variable).updateDataStructures(PalmIntVar.INF, x, oldValue);
      return true;
    }
    return false;
  }
}
