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

package choco.ecp.solver.explanations.dbt;

import choco.cp.solver.search.integer.branching.AssignVar;
import choco.ecp.solver.explanations.ExplainedSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.search.Assignment;
import choco.ecp.solver.search.SymbolicDecision;
import choco.ecp.solver.search.cbj.JumpGlobalSearchStrategy;
import choco.ecp.solver.variables.integer.ExplainedIntVar;
import choco.kernel.common.util.objects.ConstraintCollection;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.solver.Solver;
import choco.kernel.solver.propagation.Propagator;
import choco.kernel.solver.search.IntBranchingTrace;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

public class JumpExplanation implements Explanation {

  protected BitSet originalConstraints;
  protected BitSet originalStaticConstraints;
  protected BitSet decisionConstraints;
  protected Solver solver;

    public JumpExplanation(Solver solver) {
    originalConstraints = new BitSet();
    originalStaticConstraints = new BitSet();
    decisionConstraints = new BitSet();
    this.solver = solver;
  }

  public JumpExplanation(int level, Solver solver) {
    this(solver);
    decisionConstraints.set(level);
  }


  public void merge(ConstraintCollection collection) {
    if (collection instanceof JumpExplanation) {
      JumpExplanation exp = (JumpExplanation) collection;
      originalConstraints.or(exp.originalConstraints);
      decisionConstraints.or(exp.decisionConstraints);
      originalStaticConstraints.or(exp.originalStaticConstraints);
    } else {
      System.err.println("dev.i_want_to_use_this_old_version_of_choco.palm.jump.JumpExplanation merge");
    }
  }

  public ConstraintCollection copy() {
    JumpExplanation expl = new JumpExplanation(solver);
    expl.merge(this);
    return expl;
  }

  public void add(Propagator constraint) {
    int idx = ((JumpConstraintPlugin) constraint.getPlugIn()).getConstraintIdx();
    if (PartiallyStoredVector.isStaticIndex(idx)) {
      this.originalStaticConstraints.set(PartiallyStoredVector.getSmallIndex(idx));
    } else {
      this.originalConstraints.set(PartiallyStoredVector.getSmallIndex(idx));
    }
  }

  public void add(int level) {
    this.decisionConstraints.set(level);
  }

  public void add(int from, int to) {
    this.decisionConstraints.set(from, to);
  }

  public void delete(Propagator constraint) {
    int idx = ((JumpConstraintPlugin) constraint.getPlugIn()).getConstraintIdx();
    if (PartiallyStoredVector.isStaticIndex(idx)) {
      this.originalStaticConstraints.clear(PartiallyStoredVector.getSmallIndex(idx));
    } else {
      this.originalConstraints.clear(PartiallyStoredVector.getSmallIndex(idx));
    }
  }

  public void delete(int level) {
    this.decisionConstraints.clear(level);
  }

  public void addAll(Collection collection) {
    //TODO
  }

  public boolean isEmpty() {
    return originalConstraints.isEmpty() && originalStaticConstraints.isEmpty()
        && decisionConstraints.isEmpty();
  }

  public int size() {
    return originalConstraints.cardinality() + originalStaticConstraints.cardinality() +
        decisionConstraints.cardinality();
  }

  public void clear() {
    originalConstraints.clear();
    originalStaticConstraints.clear();
    decisionConstraints.clear();
  }

  public boolean contains(Propagator ct) {
    int idx = ((JumpConstraintPlugin) ct.getPlugIn()).getConstraintIdx();
    if (PartiallyStoredVector.isStaticIndex(idx)) {
      return this.originalStaticConstraints.get(PartiallyStoredVector.getSmallIndex(idx));
    } else {
      return this.originalConstraints.get(PartiallyStoredVector.getSmallIndex(idx));
    }
  }

  public boolean contains(int level) {
    return this.decisionConstraints.get(level);
  }

  public int getLastLevel(int currentLevel) {
    for (int i = currentLevel; i >= 0; i--) {
      if (decisionConstraints.get(i)) return i;
    }
    return -1;
  }

  // TODO
  public boolean containsAll(ConstraintCollection collec) {
    return false;
  }

  // TODO : currentElement
  public Set toSet() {
    Set ret = new HashSet();
    for (int i = originalConstraints.nextSetBit(0); i >= 0; i = originalConstraints.nextSetBit(i + 1)) {
      ret.add(((ExplainedSolver) solver).getConstraintNb(i));
    }
    return ret;
  }

  public boolean isValid() {
    return false;
  }

  public void empties() {
    clear();
  }

  public int nogoodSize() {
    return decisionConstraints.cardinality();
  }

  public BitSet getDecisionBitSet() {
    return decisionConstraints;
  }

  public BitSet getOriginalConstraintBitSet() {
    return originalConstraints;
  }

  public Propagator getConstraint(int i) {
    IntBranchingTrace btrace = (IntBranchingTrace) ((JumpGlobalSearchStrategy) solver.getSearchStrategy()).traceStack.get(i - 1);
    if (btrace.getBranching() instanceof AssignVar)
      return new Assignment((ExplainedIntVar) btrace.getBranchingObject(), btrace.getBranchIndex());
    else {
      throw new UnsupportedOperationException("the branching " + btrace.getBranching() + " is not yet supported by the JumpExplanation");
    }
  }

  public SymbolicDecision[] getNogood() {
    SymbolicDecision[] nogood = new SymbolicDecision[decisionConstraints.cardinality()];
    int cpt = 0;
    for (int i = decisionConstraints.nextSetBit(0); i >= 0; i = decisionConstraints.nextSetBit(i + 1)) {
      nogood[cpt] = (SymbolicDecision) this.getConstraint(i);
      cpt++;
    }
    return nogood;
  }

  public String toString() {
    StringBuffer str = new StringBuffer();
    str.append("{");

    for (int i = originalConstraints.nextSetBit(0); i >= 0; i = originalConstraints.nextSetBit(i + 1)) {
      str.append(((ExplainedSolver) this.solver).getConstraintNb(PartiallyStoredVector.getGlobalIndex(i, false)));
      if (originalConstraints.nextSetBit(i + 1) >= 0)
        str.append(", ");
    }
    str.append(" || ");
    for (int i = originalStaticConstraints.nextSetBit(0); i >= 0; i = originalStaticConstraints.nextSetBit(i + 1)) {
      str.append(((ExplainedSolver) this.solver).getConstraintNb(PartiallyStoredVector.getGlobalIndex(i, true)));
      if (originalStaticConstraints.nextSetBit(i + 1) >= 0)
        str.append(", ");
    }
    str.append(" || ");
    for (int i = decisionConstraints.nextSetBit(0); i >= 0; i = decisionConstraints.nextSetBit(i + 1)) {
      str.append("decision nb : " + i);
      if (decisionConstraints.nextSetBit(i + 1) >= 0)
        str.append(", ");
    }
    str.append("}");
    return str.toString();
  }
}
