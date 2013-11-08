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

package choco.ecp.solver.search.dbt.pathrepair;

import choco.ecp.solver.search.dbt.DecisionSConstraint;
import choco.ecp.solver.search.dbt.PalmAssignVar;
import choco.ecp.solver.variables.integer.dbt.PalmIntVar;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.search.integer.ValIterator;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: Administrateur
 * Date: 15 janv. 2004
 * Time: 17:18:37
 * To change this template use Options | File Templates.
 */
public class PathRepairAssignVar extends PalmAssignVar {

  //protected int value = Integer.MIN_VALUE;

  public PathRepairAssignVar(VarSelector varSel, ValIterator valHeuri) {
    super(varSel, valHeuri);
  }

  /**
   * return the next possible decision (variable assignement) on the variable
   *
   * @param item
   * @param previousBranch
   */

  public Object getNextBranch(Object item, Object previousBranch) {
    List<SConstraint> list = new LinkedList<SConstraint>();
    int value = wrapper.getNextBranch(item,
        ((DecisionSConstraint) ((List) previousBranch).get(0)).getBranch());
    list.add(((PalmIntVar) item).getDecisionConstraint(value));
    return list;
  }

  /**
   * Checks whether all branches have already been explored at the current choice point
   *
   * @return true if no more branches can be generated
   */
  public boolean finishedBranching(Object item, Object previousBranch) {
    return wrapper.finishedBranching(item,
        ((DecisionSConstraint) ((List) previousBranch).get(0)).getBranch());
  }
}
