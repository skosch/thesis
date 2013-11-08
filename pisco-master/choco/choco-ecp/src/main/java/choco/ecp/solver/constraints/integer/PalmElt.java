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

package choco.ecp.solver.constraints.integer;

import choco.ecp.solver.constraints.AbstractPalmBinIntSConstraint;
import choco.ecp.solver.explanations.ExplainedConstraintPlugin;
import choco.ecp.solver.explanations.ExplainedSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.variables.integer.ExplainedIntDomain;
import choco.ecp.solver.variables.integer.ExplainedIntVar;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 18 janv. 2004
 * Time: 19:22:28
 * To change this template use Options | File Templates.
 */
public class PalmElt extends AbstractPalmBinIntSConstraint {
  
  /**
   * Uses the cste slot: l[i + cste] = x.
   * (ex: cste = 1 allows to use and index from 0 to length(l) - 1.
   */
  protected final int cste;
  /**
   * Values the variable should be equal to.
   */
  protected int[] lvals;
  
  /**
   * Element constraint
   * accessing the ith element in a list of values, where i is a variable.
   * The slot v0 represents the index and the slot v1 represents the value.
   * Propagation with complete arc consistency from values to indices
   * (v1 to v0).
   * Propagation with interval approximation from indices to values (v0 to v1).
   *
   * @param v0 The index variable.
   * @param v1 The value variable.
   * @param cste The offset for the index in the values array.
   * @param lvals Values among which the variable should be affected to.
   */
  public PalmElt(final IntDomainVar v0, final IntDomainVar v1,
      final int cste, final int[] lvals) {
    super(v0, v1);
    this.cste = cste;
    this.lvals = lvals;
    this.hook = ((ExplainedSolver) this.getProblem()).
        makeConstraintPlugin(this);
  }
  
  /**
   * Filtering algorithm over the value variable (depending on the current
   * index variable state).
   * @throws ContradictionException if a contradiction occurs or a domain
   * becomes empty.
   */
  private void updateValueFromIndex() throws ContradictionException {
    int minVal = Integer.MAX_VALUE, maxVal = Integer.MIN_VALUE, val, j = 0;
    boolean found = false;
    Explanation e = ((ExplainedSolver) this.getProblem()).makeExplanation();
    ((ExplainedConstraintPlugin) this.hook).self_explain(e);
    ((ExplainedIntVar) this.v0).self_explain(ExplainedIntDomain.DOM, e);
    int[] values = ((ExplainedIntVar) this.v0).getAllValues();
    for (int i = 0; i < values.length; i++) {
      val = lvals[cste + values[i]];
      if (minVal > val) { minVal = val; }
      if (maxVal < val) { maxVal = val; }
    }
    ((ExplainedIntVar) v1).updateSup(maxVal,
        this.cIdx1, (Explanation) e.copy());
    ((ExplainedIntVar) v1).updateInf(minVal,
        this.cIdx1, (Explanation) e.copy());
    
    values = ((ExplainedIntVar) this.v0).getAllValues();
    int[] values2 = ((ExplainedIntVar) this.v1).getAllValues();
    // TODO : remplacer par des it�rateurs
    // propagate on holes
    if (v1.hasEnumeratedDomain()) {
      for (int i = 0; i < values2.length; i++) {
        while (!found & j < values.length) {
          if (lvals[values[j] + cste] == values2[i]) {
            found = true;
          }
          j++;
        }
        if (!found) {
          ((ExplainedIntVar) v1).removeVal(values2[i], 
              this.cIdx1, (Explanation) e.copy());
        }
        found = false;
        j = 0;
      }
    }
  }
  
  /**
   * Filtering algorithm over the index variable (depending on the current
   * value variable state).
   * @throws ContradictionException if a contradiction occurs or a domain
   * becomes empty.
   */
  private void updateIndexFromValue() throws ContradictionException {
    Explanation e = ((ExplainedSolver) this.getProblem()).makeExplanation();
    ((ExplainedIntVar) this.v1).self_explain(ExplainedIntDomain.DOM, e);
    ((ExplainedConstraintPlugin) this.hook).self_explain(e);
    int minFeasibleIndex = v0.getInf(),
        maxFeasibleIndex = v0.getSup(),
        thecause = VarEvent.NOCAUSE;
    if (v0.getInf() < cste) { minFeasibleIndex = cste; }
    if (v0.getSup() > (lvals.length - 1)) {
      maxFeasibleIndex = lvals.length - 1;
    }
    if (v1.hasEnumeratedDomain()) { thecause = cIdx0; }
    while ((minFeasibleIndex < (lvals.length - 1))
    && v0.canBeInstantiatedTo(minFeasibleIndex)
    && !v1.canBeInstantiatedTo(lvals[minFeasibleIndex + cste])) {
      minFeasibleIndex++;
    }
    ((ExplainedIntVar) v0).updateInf(minFeasibleIndex,
        thecause, (Explanation) e.copy());
    while ((maxFeasibleIndex > 0)
    && v0.canBeInstantiatedTo(maxFeasibleIndex)
    && !v1.canBeInstantiatedTo(lvals[maxFeasibleIndex + cste])) {
      maxFeasibleIndex--;
    }
    ((ExplainedIntVar) v0).updateSup(maxFeasibleIndex,
        thecause, (Explanation) e.copy());
    if (v0.hasEnumeratedDomain()) {
      for (int i = minFeasibleIndex + 1; i < maxFeasibleIndex; i++) {
        if (v0.canBeInstantiatedTo(i)
        && !v1.canBeInstantiatedTo(lvals[i + cste])) {
          Explanation expl =
              ((ExplainedSolver) this.getProblem()).makeExplanation();
          ((ExplainedConstraintPlugin) this.hook).self_explain(expl);
          ((ExplainedIntVar) this.v1).self_explain(ExplainedIntDomain.VAL,
              lvals[i + cste], expl);
          ((ExplainedIntVar) this.v0).removeVal(i, thecause, expl);
        }
      }
    }
  }
  
  /**
   * Generic propagation algorithm. It propagates from index variable to value
   * variable and then from value to index.
   * @throws ContradictionException if a contradiction occurs or a domain
   * becomes empty.
   */
  public void propagate() throws ContradictionException {
    updateIndexFromValue();
    updateValueFromIndex();
  }
  
  /**
   * Propagation when a lower bound is increased. The other variable is
   * updated accordingly.
   * @param idx the index of the modified variable
   * @throws ContradictionException if a contradiction occurs or a domain
   * becomes empty.
   */
  public void awakeOnInf(final int idx) throws ContradictionException {
    if (idx == 0) {
      updateValueFromIndex();
    } else {
      updateIndexFromValue();
    }
  }

  /**
   * Propagation when an upper bound is decreased. The other variable is
   * updated accordingly.
   * @param idx the index of the modified variable
   * @throws ContradictionException if a contradiction occurs or a domain
   * becomes empty.
   */
  public void awakeOnSup(final int idx) throws ContradictionException {
    if (idx == 0) {
      updateValueFromIndex();
    } else {
      updateIndexFromValue();
    }
  }
  
  /**
   * Propagation when a value is removed from a domain.
   * @param idx the index of the modified variable
   * @param val the removed value
   * @throws ContradictionException if a contradiction occurs or a domain
   * becomes empty.
   */
  public void awakeOnRem(final int idx, final int val) 
  throws ContradictionException {
    if (idx == 0) {
      updateValueFromIndex();
    } else {
      updateIndexFromValue();
    }
  }
  
  /**
   * Propagation when a value is restored to a domain (for repairing a
   * contradiction for instance).
   * @param idx the index of the modified variable
   * @throws ContradictionException if a contradiction occurs or a domain
   * becomes empty.
   */
  public void awakeOnRestore(final int idx) throws ContradictionException {
    Explanation expl = ((ExplainedSolver) this.getProblem()).
        makeExplanation();
    ((ExplainedConstraintPlugin) this.hook).self_explain(expl);
    ((ExplainedIntVar) v0).updateInf(cste, cIdx0, (Explanation) expl.copy());
    ((ExplainedIntVar) v0).updateSup(lvals.length - 1 + cste, 
        cIdx0, (Explanation) expl.copy());
    if (idx == 0) {
      updateIndexFromValue();
    } else {
      updateValueFromIndex();
    }
  }

  /**
   * Propagation when values are restored to a domain (for repairing a
   * contradiction for instance).
   * @param idx the index of the modified variable
   * @throws ContradictionException if a contradiction occurs or a domain
   * becomes empty.
   */  
  public void awakeOnRestoreInf(final int idx) throws ContradictionException {
    awakeOnRestore(idx);
  }

  /**
   * Propagation when values are restored to a domain (for repairing a
   * contradiction for instance).
   * @param idx the index of the modified variable
   * @throws ContradictionException if a contradiction occurs or a domain
   * becomes empty.
   */  
  public void awakeOnRestoreSup(final int idx) throws ContradictionException {
    awakeOnRestore(idx);
  }
  
  /**
   * Propagation when a value is restored to a domain (for repairing a
   * contradiction for instance).
   * @param idx the index of the modified variable
   * @param val the restored value
   * @throws ContradictionException if a contradiction occurs or a domain
   * becomes empty.
   */    
  public void awakeOnRestoreVal(final int idx, final int val) 
  throws ContradictionException {
    awakeOnRestore(idx);
  }
  
  /**
   * Checks if the constraint is entailed (it can be proven that the
   * constraint will be satisfied).
   * @return Boolean.TRUE if the constraint will be satisfied, Boolean.FALSE
   * if it will not, null if we do not know yet.
   */
  public Boolean isEntailed() {
    if (Logger.getLogger("dev.i_want_to_use_this_old_version_of_choco.prop.const").isLoggable(Level.WARNING)) {
      Logger.getLogger("dev.i_want_to_use_this_old_version_of_choco.prop.const").
          warning("Not Yet implemented : NotEqual.isEntailed");
    }
    return null;
  }
  
  /**
   * When variables are instantiated, checks that the constraint is
   * satisfied.
   * @return true if the constraint is satified by the current instantiation.
   */
  public boolean isSatisfied() {
    return lvals[v0.getVal() + cste] == v1.getVal();
  }
  
  /**
   * Determines why the constraints is satified.
   * @return an explanation justifying that the constraint is satified.
   */
  public Set whyIsTrue() {
    if (Logger.getLogger("dev.i_want_to_use_this_old_version_of_choco.prop.const").isLoggable(Level.WARNING)) {
      Logger.getLogger("dev.i_want_to_use_this_old_version_of_choco.prop.const").
          warning("Not Yet implemented : NotEqual.whyIsTrue");
    }
    return null;
  }

  /**
   * Determines why the constraints is not satified.
   * @return an explanation justifying that the constraint is not satified.
   */
  public Set whyIsFalse() {
    if (Logger.getLogger("dev.i_want_to_use_this_old_version_of_choco.prop.const").isLoggable(Level.WARNING)) {
      Logger.getLogger("dev.i_want_to_use_this_old_version_of_choco.prop.const").
          warning("Not Yet implemented : NotEqual.whyIsFalse");
    }
    return null;
  }
}
