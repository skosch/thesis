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

package choco.ecp.solver.explanations.dbt;


import choco.kernel.solver.constraints.AbstractSConstraint;

import java.util.Comparator;

/**
 * Standard Constraint Comparator. For Instance,
 * <code>Collections.min(aCollection, new BetterConstraintComparator());</code> returns the constraint
 * with the smaller weight, or if their weights are equal, the younger constraint
 * (w.r.t. the constraint timestamp).
 */

public class BetterConstraintComparator implements Comparator {

  /**
   * Compares two <code>PalmConstraint</code>. o1 is less than o2 (returns a negative value) if and only if
   * (o1.weight < o2.weight) or ( (o1.weight = o2.weight) and (o1.timestamp > o2.timestamp) ).
   *
   * @param o1 First constraint.
   * @param o2 Second constraint.
   * @return A negative value if o1 < o2, a positive value if o1 > o2, 0 else.
   */

  public int compare(Object o1, Object o2) {
    PalmConstraintPlugin plug1 = (PalmConstraintPlugin) ((AbstractSConstraint) o1).getPlugIn();
    PalmConstraintPlugin plug2 = (PalmConstraintPlugin) ((AbstractSConstraint) o2).getPlugIn();
    if (plug1.getWeight() < plug2.getWeight())
      return -1;
    else if (plug1.getWeight() == plug2.getWeight()) {
      if (plug1.getTimeStamp() > plug2.getTimeStamp())
        return -1;
      else if (plug1.getTimeStamp() < plug2.getTimeStamp())
        return 1;
      else
        return 0;
    } else
      return 1;
  }
}
