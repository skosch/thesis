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

package choco.ecp.solver.explanations.benders;

import choco.cp.solver.search.integer.branching.AssignVar;
import choco.ecp.solver.JumpSolver;
import choco.ecp.solver.explanations.dbt.JumpExplanation;
import choco.ecp.solver.search.Assignment;
import choco.ecp.solver.search.benders.MasterGlobalSearchStrategy;
import choco.ecp.solver.variables.integer.ExplainedIntVar;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.Propagator;
import choco.kernel.solver.search.IntBranchingTrace;


// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

public class BendersExplanation extends JumpExplanation {

  public BendersExplanation(Solver s) {
    super(s);
  }

  public BendersExplanation(int level, Solver s) {
    super(level, s);
  }

  public Propagator getConstraint(int i) {
    return (Propagator) getMasterConstraint(i);
  }

  public SConstraint getMasterConstraint(int i) {
    IntBranchingTrace btrace = (IntBranchingTrace) ((MasterGlobalSearchStrategy) ((JumpSolver) solver).getSearchStrategy()).getMaster().traceStack.get(i - 1);
    if (btrace.getBranching() instanceof AssignVar)
      return new Assignment((ExplainedIntVar) btrace.getBranchingObject(), btrace.getBranchIndex());
    else {
      throw new UnsupportedOperationException("the branching " + btrace.getBranching() + " is not yet supported by the JumpExplanation");
    }
  }

  public SConstraint getCurrentSubConstraint(int i) {
    // The initial propagation is not stored in the stack of subproblems, the index is therefore used directly (i instead i-1 for the master)
    IntBranchingTrace btrace = (IntBranchingTrace) ((MasterGlobalSearchStrategy) ((JumpSolver) solver).getSearchStrategy()).getSubproblems().traceStack.get(i);
    if (btrace.getBranching() instanceof AssignVar)
      return new Assignment((ExplainedIntVar) btrace.getBranchingObject(), btrace.getBranchIndex());
    else {
      throw new UnsupportedOperationException("the branching " + btrace.getBranching() + " is not yet supported by the JumpExplanation");
    }
  }
}

