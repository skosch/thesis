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

import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.variables.integer.ExplainedIntDomain;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;

/**
 * Created by IntelliJ IDEA.
 * User: grochart
 * Date: 7 janv. 2004
 * Time: 13:50:57
 * To change this template use Options | File Templates.
 */
public interface PalmIntDomain extends ExplainedIntDomain {
  int DOM = 0;
  int INF = 1;
  int SUP = 2;
  int VAL = 3;


  /**
   * When a value is restored, it deletes the explanation associated to the value removal.
   */

  public void resetExplanationOnVal(int val);


  /**
   * When a lower bound is restored, it deletes the explanation associated to the value removal.
   */

  public void resetExplanationOnInf();


  /**
   * When an upper bound is restored, it deletes the explanation associated to the value removal.
   */

  public void resetExplanationOnSup();


  /**
   * Allows to get an explanation for the domain or a bound of the variable. This explanation is merge to the
   * explanation in parameter.
   *
   * @param select Should be <code>PalmIntDomain.INF</code>, <code>PalmIntDomain.SUP</code>, or <code>PalmIntDomain.DOM</code>
   */

  public void self_explain(int select, Explanation expl);


  /**
   * Allows to get an explanation for a value removal from the variable. This explanation is merge to the
   * explanation in parameter.
   *
   * @param select Should be <code>PalmIntDomain.VAL</code>
   */

  public void self_explain(int select, int x, Explanation expl);


  /**
   * Updates the upper bound and posts the event.
   */

  public boolean updateSup(int x, int idx, Explanation e) throws ContradictionException;


  /**
   * Updates the lower bound and posts the event.
   */

  public boolean updateInf(int x, int idx, Explanation e) throws ContradictionException;


  /**
   * Removes a value and posts the event.
   */

  public boolean removeVal(int value, int idx, Explanation e) throws ContradictionException;


  /**
   * Restores a lower bound and posts the event. Not supported for such a domain.
   */

  public void restoreInf(int newValue) throws ContradictionException;


  /**
   * Restores an upper bound and posts the event. Not supported for such a domain.
   */

  public void restoreSup(int newValue) throws ContradictionException;


  /**
   * Restores a value and posts the event.
   */

  public void restoreVal(int val);


  /**
   * Returns the decision constraint assigning the domain to the specified value. The constraint is created if
   * it is not yet created.
   */

  public SConstraint getDecisionConstraint(int val);


  /**
   * Returns the negated decision constraint.
   */

  public SConstraint getNegDecisionConstraint(int val);

}
