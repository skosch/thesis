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

import choco.kernel.common.util.objects.ConstraintCollection;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.variables.real.RealDomain;

/**
 * Interface for a PaLM real domain.
 */
public interface PalmRealDomain extends RealDomain, PalmRealInterval {
  /**
   * Lower bound of the domain should be restored to the specified value.
   */
  public void restoreInf(double newValue);

  /**
   * Upper bound of the domain should be restored to the specified value.
   */
  public void restoreSup(double newValue);

  /**
   * Updates lower bound explanations: removes all explanation that are no up-to-date anymore.
   */
  public void resetExplanationOnInf();

  /**
   * Updates upper bound explanations: removes all explanation that are no up-to-date anymore.
   */
  public void resetExplanationOnSup();

  /**
   * Returns current decisions constraints involving this domain/variable.
   */
  public ConstraintCollection getDecisionConstraints();

  /**
   * Adds a new decision constraint involving this domain/variable.
   */
  public void addDecisionConstraint(AbstractSConstraint cst);

  /**
   * Updates decision constraints: removes all removed decision constraints.
   */
  public void updateDecisionConstraints();
}
