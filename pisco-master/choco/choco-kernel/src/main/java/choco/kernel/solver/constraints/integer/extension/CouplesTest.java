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

package choco.kernel.solver.constraints.integer.extension;

public abstract class CouplesTest extends ConsistencyRelation implements BinRelation {

  /**
   * the default constructor build a relation in feasability
   */
  protected CouplesTest() {
    feasible = true;
  }

  protected CouplesTest(boolean feasible) {
    this.feasible = feasible;
  }

  /**
   * check if the couple (x,y) is consistent according
   * to the feasability of the relation and the checkCouple method.
   * checkCouple have to be overriden by any concrete CouplesTest
   * relation.
   */
  public boolean isConsistent(int x, int y) {
    return checkCouple(x, y) == feasible;
  }

  /**
   * @return the opposite relation
   */
  public ConsistencyRelation getOpposite() {
    CouplesTest ct = null;
    try {
      ct = (CouplesTest) this.clone();
      ct.feasible = !feasible;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return (ConsistencyRelation) ct;
  }
}
