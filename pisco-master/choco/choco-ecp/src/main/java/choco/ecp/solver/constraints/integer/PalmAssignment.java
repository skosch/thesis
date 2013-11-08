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

import choco.ecp.solver.search.dbt.DecisionSConstraint;
import choco.ecp.solver.variables.integer.dbt.PalmIntVar;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Created by IntelliJ IDEA.
 * User: grochart
 * Date: 26 ao?t 2003
 * Time: 10:47:40
 * To change this template use Options | File Templates.
 */
public class PalmAssignment extends PalmEqualXC implements DecisionSConstraint, Comparable {
  public PalmAssignment(IntDomainVar v0, int cste) {
    super(v0, cste);
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public SConstraint negate() {
    PalmIntVar v = (PalmIntVar) this.v0;
    return v.getNegDecisionConstraint(this.cste);
    /*if (v.hasEnumeratedDomain())
      return v.getNegnConstraint(this.cste);
    else
      return v.getNegEnumerationConstraint(this.cste); */
  }

  public int getBranch() {
    return cste;
  }

  public int compareTo(Object o) {
    if (v0.hashCode() < ((PalmAssignment) o).v0.hashCode())
      return 1;
    else if (v0.hashCode() == ((PalmAssignment) o).v0.hashCode()) {
      if (cste < ((PalmAssignment) o).cste)
        return 1;
      else if (cste == ((PalmAssignment) o).cste)
        return 0;
      else
        return 1;
    } else
      return -1;
  }
}
