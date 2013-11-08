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

package choco.ecp.solver.search;

import choco.ecp.solver.variables.integer.ExplainedIntVar;
import choco.kernel.solver.variables.Var;

/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 29 d�c. 2004
 * Time: 16:41:51
 * To change this template use File | Settings | File Templates.
 */
public class Assignment extends AbstractDecision implements Comparable {

  protected ExplainedIntVar var;
  protected int value;

  public Assignment(ExplainedIntVar var, int value) {
    super(var.getProblem());
    this.value = value;
    this.var = var;
  }

  public int getBranch() {
    return value;
  }

  public int getNbVars() {
    return 1;
  }

  public Var getVar(int i) {
    return var;
  }

  public void setVar(int i, Var v) {
    this.var = (ExplainedIntVar) v;
  }

  public boolean isCompletelyInstantiated() {
    return var.isInstantiated();
  }

  public boolean isSatisfied() {
    return var.isInstantiatedTo(value);
  }

  public String pretty() {
    return var + " == " + value;
  }

  public boolean equals(Assignment dec) {
    return (this.value == dec.value) && (this.var == dec.var);
  }

  public int compareTo(Object o) {
    if (var.hashCode() < o.hashCode())
      return -1;
    else if (var.hashCode() == ((Assignment) o).getVar(0).hashCode()) {
      if (value < ((Assignment) o).getBranch())
        return -1;
      else if (value == ((Assignment) o).getBranch())
        return 0;
      else
        return 1;
    } else
      return 1;
  }

  public void delete() { // TODO
  }

  public void constAwake(boolean b) {
  }
}
