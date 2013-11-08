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

package choco.ecp.solver.variables.integer.cbj;

import choco.cp.solver.variables.integer.IntDomainVarImpl;
import choco.cp.solver.variables.integer.IntervalIntDomain;
import choco.ecp.solver.JumpSolver;
import choco.ecp.solver.explanations.ExplainedSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.propagation.dbt.StructureMaintainer;
import choco.ecp.solver.search.cbj.JumpContradictionException;
import choco.ecp.solver.variables.integer.ExplainedIntDomain;
import choco.ecp.solver.variables.integer.ExplainedIntVar;
import choco.kernel.common.util.objects.ConstraintCollection;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.propagation.VarEvent;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

public class JumpIntervalIntDomain extends IntervalIntDomain implements ExplainedIntDomain {

  /**
   * A stack of explanations for lower bound modifications.
   */

  protected final LinkedList explanationOnInf;


  /**
   * A stack of explanations for upper bound modifications.
   */

  protected final LinkedList explanationOnSup;

  /**
   * The number of valid inf explanation at the current world level
   */
  protected IStateInt nbexpinf;


  /**
   * The number of valid inf explanation at the current world level
   */
  protected IStateInt nbexpsup;

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

  public JumpIntervalIntDomain(IntDomainVarImpl v, int a, int b) {
    super(v, a, b);
    JumpSolver pb = (JumpSolver) this.getSolver();
    this.explanationOnInf = new LinkedList();
    this.explanationOnSup = new LinkedList();
    this.explanationOnInf.add((pb.makeExplanation()));
    this.explanationOnSup.add((pb.makeExplanation()));
    this.nbexpinf = pb.getEnvironment().makeInt();
    this.nbexpinf.set(1);
    this.nbexpsup = pb.getEnvironment().makeInt();
    this.nbexpsup.set(1);
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
    int[] ret = new int[getSup() - getInf() + 1];
    for (int i = 0; i < ret.length; i++)
      ret[i] = getInf() + i;
    return ret;
  }

  /**
   * Updates the upper bound and posts the event.
   */

  public boolean updateSup(int x, int idx, Explanation e) throws ContradictionException {
    if (this.updateSup(x, e)) {
      int cause = VarEvent.NOCAUSE;
      if (x == this.getSup()) cause = idx;
      solver.getPropagationEngine().postUpdateSup(variable, cause);
      if (x < this.getInf()) {
        this.variable.value.set(IStateInt.UNKNOWN_INT);
        Explanation exp = ((ExplainedSolver) solver).makeExplanation();
        this.self_explain(ExplainedIntDomain.DOM, exp);
        throw (new JumpContradictionException(this.getSolver(), exp));
        //((PalmEngine) this.getProblem().getPropagationEngine()).raisePalmContradiction((PalmIntVar) this.variable);
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
      solver.getPropagationEngine().postUpdateInf(variable, cause);
      if (x > this.getSup()) {
        this.variable.value.set(IStateInt.UNKNOWN_INT);
        Explanation exp = ((ExplainedSolver) solver).makeExplanation();
        this.self_explain(ExplainedIntDomain.DOM, exp);
        throw (new JumpContradictionException(this.getSolver(), exp));
        //((PalmEngine) this.getProblem().getPropagationEngine()).raisePalmContradiction((PalmIntVar) this.variable);
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
   * Allows to get an explanation for the domain or a bound of the variable. This explanation is merge to the
   * explanation in parameter.
   *
   * @param select Should be <code>PalmIntDomain.INF</code>, <code>PalmIntDomain.SUP</code>, or <code>PalmIntDomain.DOM</code>
   */

  public void self_explain(int select, Explanation expl) {
    switch (select) {
      case DOM:
        ensureUpToDateExplanations();
        expl.merge((ConstraintCollection) this.explanationOnInf.getLast());
        expl.merge((ConstraintCollection) this.explanationOnSup.getLast());
        break;
      case INF:
        ensureUpToDateExplanations();
        expl.merge((ConstraintCollection) this.explanationOnInf.getLast());
        break;
      case SUP:
        ensureUpToDateExplanations();
        expl.merge((ConstraintCollection) this.explanationOnSup.getLast());
        break;
      default:
        if (Logger.getLogger("choco").isLoggable(Level.WARNING))
          Logger.getLogger("choco").warning("PaLM: VAL needs another parameter in self_explain (IntDomainVar)");
    }
  }

  public void ensureUpToDateExplanations() {
    while (nbexpinf.get() < explanationOnInf.size()) {
      explanationOnInf.removeLast();
    }
    while (nbexpsup.get() < explanationOnSup.size()) {
      explanationOnSup.removeLast();
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
      ensureUpToDateExplanations();
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

  protected boolean updateSup(int x, Explanation e) {
    if (x < this.getSup()) {
      int oldValue = this.getSup();
      ((ExplainedIntVar) this.variable).self_explain(SUP, e);
      this.explanationOnSup.add(e);//.makeDecSupExplanation(this.getSup(), (PalmIntVar) this.variable));
      this.nbexpsup.add(1);
      this.updateSup(x);
      if (this.inf.get() == this.sup.get()) {
        this.variable.value.set(this.getInf());
      }
      StructureMaintainer.updateDataStructures(this.variable, SUP, x, oldValue);
      //((PalmIntVar) this.variable).updateDataStructures(PalmIntVar.SUP, x, oldValue);
      return true;
    }
    return false;
  }

  protected boolean updateInf(int x, Explanation e) {
    if (x > this.getInf()) {
      int oldValue = this.getInf();
      ((ExplainedIntVar) this.variable).self_explain(INF, e);
      this.explanationOnInf.add(e); //.makeIncInfExplanation(this.getInf(), (PalmIntVar) this.variable));
      this.nbexpinf.add(1);
      this.updateInf(x);
      if (this.inf.get() == this.sup.get()) {
        this.variable.value.set(this.getInf());
      }
      StructureMaintainer.updateDataStructures(this.variable, INF, x, oldValue);
      //((PalmIntVar) this.variable).updateDataStructures(PalmIntVar.INF, x, oldValue);
      return true;
    }
    return false;
  }
}
