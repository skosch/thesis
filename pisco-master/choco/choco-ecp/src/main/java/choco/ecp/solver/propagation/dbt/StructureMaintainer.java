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

package choco.ecp.solver.propagation.dbt;

import choco.ecp.solver.propagation.PalmIntVarListener;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.structure.PartiallyStoredIntVector;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Created by IntelliJ IDEA.
 * User: rochart
 * Date: Jan 14, 2004
 * Time: 11:25:01 AM
 * To change this template use Options | File Templates.
 */
public final class StructureMaintainer {
  public static void updateDataStructures(IntDomainVar var, int select, int newValue, int oldValue) {
    updateDataStructuresOnVariable(var, select, newValue, oldValue);
    updateDataStructuresOnConstraints(var, select, newValue, oldValue);
  }

  public static void updateDataStructuresOnVariable(IntDomainVar var, int select, int newValue, int oldValue) {
    // A redefinir si necessaire
  }

  public static void updateDataStructuresOnConstraints(IntDomainVar v, int select, int newValue, int oldValue) {
    PartiallyStoredVector constraints = v.getConstraintVector();
    PartiallyStoredIntVector indices = v.getIndexVector();

      DisposableIntIterator cit = constraints.getIndexIterator();
    for (; cit.hasNext();) {
      int idx = cit.next();
      PalmIntVarListener c = (PalmIntVarListener) constraints.get(idx);
      if (c.isActive()) {
        int i = indices.get(idx);
        c.updateDataStructuresOnConstraint(i, select, newValue, oldValue);
      }
    }
      cit.dispose();
  }

  public static void updateDataStructuresOnRestore(IntDomainVar var, int select, int newValue, int oldValue) {
    updateDataStructuresOnRestoreVariable(var, select, newValue, oldValue);
    updateDataStructuresOnRestoreConstraints(var, select, newValue, oldValue);
  }

  public static void updateDataStructuresOnRestoreVariable(IntDomainVar var, int select, int newValue, int oldValue) {
    // A redefinir si necessaire
  }

  public static void updateDataStructuresOnRestoreConstraints(IntDomainVar v, int select, int newValue, int oldValue) {
    PartiallyStoredVector constraints = v.getConstraintVector();
    PartiallyStoredIntVector indices = v.getIndexVector();

      DisposableIntIterator cit = constraints.getIndexIterator();
    for (; cit.hasNext();) {
      int idx = cit.next();
      PalmIntVarListener c = (PalmIntVarListener) constraints.get(idx);
      if (c.isActive()) {
        int i = indices.get(idx);
        c.updateDataStructuresOnRestoreConstraint(i, select, newValue, oldValue);
      }
    }
      cit.dispose();
  }

}
