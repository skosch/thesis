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

public class PalmGreaterOrEqualXYC extends AbstractPalmBinIntSConstraint {
  protected final int cste;

  public PalmGreaterOrEqualXYC(IntDomainVar v0, IntDomainVar v1, int cste) {
    super(v0, v1);
    this.cste = cste;
    this.hook = ((ExplainedSolver) this.getProblem()).makeConstraintPlugin(this);
  }

  public String toString() {
    return this.v0 + " >= " + this.v1 + " + " + this.cste;
  }

  public void propagate() throws ContradictionException {
    this.awakeOnInf(1);
    this.awakeOnSup(0);
    /*if (this.v0.hasEnumeratedDomain()) {

      if (v1.getInf() + this.cste > v0.getInf()) {
        int[] values = ((ExplainedIntVar) v0).getAllValues();
        dev.i_want_to_use_this_old_version_of_choco.palm.dbt.explain.PalmExplanation expl = new dev.i_want_to_use_this_old_version_of_choco.palm.dbt.explain.GenericExplanation(this.getProblem());
        ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
        int index = 0;
        int min = ((ExplainedIntDomain) v1.getDomain()).getOriginalInf();
        while (index < values.length && values[index] < v1.getInf() + this.cste) {
          for (int i = min; i <= values[index] - this.cste; i++)
            ((ExplainedIntVar) this.v1).self_explain(ExplainedIntDomain.VAL, i, expl);
          ((ExplainedIntVar) this.v0).removeVal(values[index], this.cIdx0, expl.copy());
          min = values[index] + 1 - this.cste;
          index++;
        }
      }
    } else {
      this.awakeOnInf(1);
    }

    if (this.v1.hasEnumeratedDomain()) {
      if (v1.getSup() + this.cste > v0.getSup()) {
        int[] values = ((ExplainedIntVar) v1).getAllValues();
        dev.i_want_to_use_this_old_version_of_choco.palm.dbt.explain.PalmExplanation expl = new dev.i_want_to_use_this_old_version_of_choco.palm.dbt.explain.GenericExplanation(this.getProblem());
        ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
        int index = values.length - 1;
        int max = ((ExplainedIntDomain) v0.getDomain()).getOriginalSup();
        while (index >= 0 && values[index] + this.cste > v0.getSup()) {
          for (int i = max; i >= values[index] + this.cste; i--)
            ((ExplainedIntVar) this.v0).self_explain(ExplainedIntDomain.VAL, i, expl);
          ((ExplainedIntVar) this.v1).removeVal(values[index], this.cIdx1, expl.copy());
          max = values[index] - 1 + this.cste;
          index--;
        }
      }
    } else {
      this.awakeOnSup(0);
    } */
  }

  public void awakeOnInf(int idx) throws ContradictionException {
    if ((idx == 1) && (v1.getInf() + this.cste > v0.getInf())) {
      if (v0.hasEnumeratedDomain()) {
        //if (v1.getInf() + this.cste > v0.getInf()) {
        int[] values = ((ExplainedIntVar) v0).getAllValues();
        Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
        ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
        int index = 0;
        int min = ((ExplainedIntDomain) v1.getDomain()).getOriginalInf();
        while (index < values.length && values[index] < v1.getInf() + this.cste) {
          for (int i = min; i <= values[index] - this.cste; i++)
            ((ExplainedIntVar) this.v1).self_explain(ExplainedIntDomain.VAL, i, expl);
          ((ExplainedIntVar) this.v0).removeVal(values[index], this.cIdx0, (Explanation) expl.copy());
          min = values[index] + 1 - this.cste;
          index++;
        }
        //}
      } else {
        Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
        ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
        ((ExplainedIntVar) this.v1).self_explain(ExplainedIntDomain.INF, expl);
        ((ExplainedIntVar) this.v0).updateInf(this.v1.getInf() + this.cste, this.cIdx0, expl);
      }
    }
  }

  public void awakeOnSup(int idx) throws ContradictionException {
    if ((idx == 0) && (v1.getSup() + this.cste > v0.getSup())) {
      if (v1.hasEnumeratedDomain()) {
        //if (v1.getSup() + this.cste > v0.getSup()) {
        int[] values = ((ExplainedIntVar) v1).getAllValues();
        Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
        ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
        int index = values.length - 1;
        int max = ((ExplainedIntDomain) v0.getDomain()).getOriginalSup();
        while (index >= 0 && values[index] + this.cste > v0.getSup()) {
          for (int i = max; i >= values[index] + this.cste; i--)
            ((ExplainedIntVar) this.v0).self_explain(ExplainedIntDomain.VAL, i, expl);
          ((ExplainedIntVar) this.v1).removeVal(values[index], this.cIdx1, (Explanation) expl.copy());
          max = values[index] - 1 + this.cste;
          index--;
        }
        //}
      } else {
        Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
        ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
        ((ExplainedIntVar) this.v0).self_explain(ExplainedIntDomain.SUP, expl);
        ((ExplainedIntVar) this.v1).updateSup(this.v0.getSup() - this.cste, this.cIdx1, expl);
      }
    }
  }

  public void awakeOnRestoreInf(int idx) throws ContradictionException {
    if (idx == 0) this.awakeOnInf(1);
  }

  public void awakeOnRestoreSup(int idx) throws ContradictionException {
    if (idx == 1) this.awakeOnSup(0);
  }

  public void awakeOnRem(int idx, int v) throws ContradictionException {
    if (idx == 0) {
      this.awakeOnSup(0);
    } else {
      this.awakeOnInf(1);
    }
    /*
    // A priori ca marche, mais a surveiller :)

    if (idx == 1) {
      if (v1.getInf() + this.cste > v0.getInf()) {
        int[] values = ((ExplainedIntVar) v0).getAllValues();
        dev.i_want_to_use_this_old_version_of_choco.palm.dbt.explain.PalmExplanation expl = new dev.i_want_to_use_this_old_version_of_choco.palm.dbt.explain.GenericExplanation(this.getProblem());
        ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
        int id = 0;
        int min = ((ExplainedIntDomain) v1.getDomain()).getOriginalInf();
        while (id < values.length && values[id] - this.cste < v1.getInf()) {
          for (int i = min; i <= values[id] - this.cste; i++)
            ((ExplainedIntVar) this.v1).self_explain(ExplainedIntDomain.VAL, i, expl);
          ((ExplainedIntVar) this.v0).removeVal(values[id], this.cIdx0, expl.copy());
          min = values[id] + 1 - this.cste;
          id++;
        }
      }
    } else {
      if (v1.getSup() + this.cste > v0.getSup()) {
        int[] values = ((ExplainedIntVar) v1).getAllValues();
        dev.i_want_to_use_this_old_version_of_choco.palm.dbt.explain.PalmExplanation expl = new dev.i_want_to_use_this_old_version_of_choco.palm.dbt.explain.GenericExplanation(this.getProblem());
        ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
        int id = values.length - 1;
        int max = ((ExplainedIntDomain) v0.getDomain()).getOriginalSup();
        while (id >= 0 && values[id] + this.cste > v0.getSup()) {
          for (int i = max; i >= values[id] + this.cste; i--)
            ((ExplainedIntVar) this.v0).self_explain(ExplainedIntDomain.VAL, i, expl);
          ((ExplainedIntVar) this.v1).removeVal(values[id], this.cIdx1, expl.copy());
          max = values[id] - 1 + this.cste;
          id--;
        }
      }
    } */
  }

  public void awakeOnRestoreVal(int idx, int val) throws ContradictionException {
    if (idx == 1) {
      this.awakeOnSup(0);
    } else {
      this.awakeOnInf(1);
    }
    // A priori ca marche, mais a surveiller :)
    /*if (idx == 1) {
      if (v1.getInf() + this.cste > val) {
        dev.i_want_to_use_this_old_version_of_choco.palm.dbt.explain.PalmExplanation expl = new dev.i_want_to_use_this_old_version_of_choco.palm.dbt.explain.GenericExplanation(this.getProblem());
        ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
        for (int i = ((ExplainedIntDomain) v1.getDomain()).getOriginalInf(); i <= val - this.cste; i++)
          ((ExplainedIntVar) this.v1).self_explain(ExplainedIntDomain.VAL, i, expl);
        ((ExplainedIntVar) this.v0).removeVal(val, this.cIdx0, expl);
      }
    } else {
      if (val + this.cste > v0.getSup()) {
        dev.i_want_to_use_this_old_version_of_choco.palm.dbt.explain.PalmExplanation expl = new dev.i_want_to_use_this_old_version_of_choco.palm.dbt.explain.GenericExplanation(this.getProblem());
        ((ExplainedConstraintPlugin) this.getPlugIn()).self_explain(expl);
        for (int i = ((ExplainedIntDomain) v0.getDomain()).getOriginalSup(); i >= val + this.cste; i--)
          ((ExplainedIntVar) this.v0).self_explain(ExplainedIntDomain.VAL, i, expl);
        ((ExplainedIntVar) this.v1).removeVal(val, this.cIdx1, expl);
      }
    } */
  }

  public boolean isSatisfied() {
    return this.v0.getVal() >= (this.v1.getSup() + this.cste);
  }

  public Boolean isEntailed() {
    if (this.v0.getSup() < this.v1.getInf() + this.cste)
      return Boolean.FALSE;
    if (this.v0.getInf() >= this.v1.getSup() + this.cste)
      return Boolean.TRUE;
    return null;
  }

  public Set whyIsFalse() {
    Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
    ((ExplainedIntVar) this.v0).self_explain(ExplainedIntDomain.SUP, expl);
    ((ExplainedIntVar) this.v1).self_explain(ExplainedIntDomain.INF, expl);
    return expl.toSet();
  }

  public Set whyIsTrue() {
    Explanation expl = ((ExplainedSolver) this.getProblem()).makeExplanation();
    ((ExplainedIntVar) this.v0).self_explain(ExplainedIntDomain.INF, expl);
    ((ExplainedIntVar) this.v1).self_explain(ExplainedIntDomain.SUP, expl);
    return expl.toSet();
  }
}
