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

import choco.kernel.solver.Solution;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Solution for a Palm problem.
 */

public class PalmSolution extends Solution {

  /**
   * Statistics for this solution (CPU, extensiions...).
   */

  protected int[] lstat;


  /**
   * Creates a solution for the specified problem.
   */

  public PalmSolution(Solver pb) {
    super(pb);
    this.lstat = new int[((PalmGlobalSearchStrategy) pb.getSearchStrategy()).getNbLimit()];
  }


  public void recordStatistic(int statIndex, int statValue) {
    lstat[statIndex] = statValue;
  }


  /**
   * Returns a statistic about the solution.
   *
   * @param stat The statitic constant (<code>PalmProblem.RLX/EXT/CPU</code>).
   */

  public int getStatistic(int stat) {
    return lstat[stat];
  }

  /**
   * Returns the value of the variable <code>idx</code>.
   *
   * @param idx the variable number
   * @return the value of the variable
   */

  public int getValue(int idx) {    // TODO : dans Choco ?
    return this.intVarValues[idx];
  }


  /**
   * Pretty display of the solution.
   */

  public String toString() {
    StringBuffer buf = new StringBuffer();

    buf.append("SOL ");
    for (int i = 0; i < this.solver.getNbIntVars(); i++) {
      IntDomainVar var = (IntDomainVar) this.solver.getIntVar(i);
      buf.append(var + ":" + this.intVarValues[i] + " \n");
    }

    return buf.toString();
  }
}
