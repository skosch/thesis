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

package choco.ecp.solver.search.dbt;


import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.explanations.dbt.PalmConstraintPlugin;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;

import java.util.*;

public abstract class PalmLearn extends PalmAbstractSolverTool {
  public void learnFromContradiction(PalmExplanation expl) {
  }

  public abstract void learnFromRemoval(SConstraint constraint);

  public abstract boolean checkAcceptable(List constraints);

  public abstract boolean checkAcceptableRelaxation(SConstraint constraint);

  public ArrayList sortConstraintToUndo(PalmExplanation expl) {
    PalmSolver pb = ((PalmSolver) this.getManager().getSolver());
    ArrayList<AbstractSConstraint> list = new ArrayList<AbstractSConstraint>();
    BitSet bset = expl.getBitSet();    // iterate on the bitset and avoid HashSet !!!!!!
    for (int i = bset.nextSetBit(0); i >= 0; i = bset.nextSetBit(i + 1)) {
      AbstractSConstraint ct = pb.getConstraintNb(i);
      if (((PalmConstraintPlugin) (ct).getPlugIn()).getWeight() == 0)
        list.add(ct);
    }
    // We assume that all decision constraints use the same comparator defined by the user
    if (!list.isEmpty()) {
      Comparator<AbstractSConstraint> Comp = ((PalmConstraintPlugin) ((AbstractSConstraint) list.get(0)).getPlugIn()).getSearchInfo().getComparator();
      Collections.sort(list, Comp);
    }
    return list;
  }

}
