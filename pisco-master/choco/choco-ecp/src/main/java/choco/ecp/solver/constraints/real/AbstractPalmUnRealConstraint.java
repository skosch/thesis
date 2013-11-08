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

package choco.ecp.solver.constraints.real;

import choco.ecp.solver.propagation.PalmRealVarListener;
import choco.kernel.solver.constraints.real.AbstractUnRealSConstraint;
import choco.kernel.solver.constraints.real.RealSConstraint;
import choco.kernel.solver.variables.real.RealVar;

/**
 * Abstract implementation of an unary constraint on one real variable.
 */

public abstract class AbstractPalmUnRealConstraint extends AbstractUnRealSConstraint
    implements RealSConstraint, PalmRealVarListener {
    /**
     * Constraucts a constraint with the priority 0.
     */
    protected AbstractPalmUnRealConstraint(RealVar v0) {
        super(v0);
    }

    /**
   * Synchronous update events are not handled by default.
   *
   * @param idx      the index of the modified variable
   * @param select   the modificatino on this variable
   * @param newValue the new value for the involved property
   * @param oldValue the old value for the involved property
   */
  public void updateDataStructuresOnConstraint(int idx, int select, int newValue, int oldValue) {
  }

  /**
   * Synchronous restoration events are not handled by default.
   *
   * @param idx      the index of the modified variable
   * @param select   the modificatino on this variable
   * @param newValue the new value for the involved property
   * @param oldValue the old value for the involved property
   */
  public void updateDataStructuresOnRestoreConstraint(int idx, int select, int newValue, int oldValue) {
  }
}
