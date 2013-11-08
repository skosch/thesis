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

package choco.kernel.solver.branch;


/**
 * ObjectBranching objects are specific branching objects where each branch is labeled with an Object.
 * This is typically useful for palm control objects (where the label happens to be a List of Constraint).
 */
public interface ObjectBranching extends BranchingStrategy {
  /**
   * Computes decisions that can be taken on the specified item by the strategy.
   *
   * @param item The item the strategy branchs on.
   */
  Object selectFirstBranch(Object item);

  /**
   * When several decisions can be taken (for unsure extension for instance), this methos allows to
   * choose next decisions.
   *
   * @param branchingItem  the branching object under scrutiny
   * @param previousBranch the object labelling the previous branch
   * @return the object labelling the current branch
   */
  Object getNextBranch(Object branchingItem, Object previousBranch);

  /**
   * Checks whether all branches have already been explored at the current choice point
   *
   * @return true if no more branches can be generated
   */
  public boolean finishedBranching(Object item, Object previousBranch);
}
