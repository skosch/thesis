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

package choco.kernel.solver.constraints.real;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.real.RealInterval;
import choco.kernel.solver.variables.real.RealVar;

import java.util.List;
import java.util.Set;

/**
 * An interface for real expressions.
 */
public interface RealExp extends RealInterval {
  /**
   * Computes the narrowest bounds with respect to sub terms.
   */
  public void tighten();

  /**
   * Projects computed bounds to the sub expressions.
   *
   * @throws choco.kernel.solver.ContradictionException
   */
  public void project() throws ContradictionException;

  /**
   * Computes recursively the sub expressions (avoids to tighten and project recursively).
   *
   * @return the flattened list of subexpressions
   */
  public List<RealExp> subExps(List<RealExp> l);

  /**
   * Collects recursively all the variable this expression depends on.
   *
   * @return the collected set
   */
  public Set<RealVar> collectVars(Set<RealVar> s);

  /**
   * Isolates sub terms depending or not on a variable x.
   *
   * @param var
   * @param wx
   * @param wox
   * @return TODO
   */
  public boolean isolate(RealVar var, List<RealExp> wx, List<RealExp> wox);


}
