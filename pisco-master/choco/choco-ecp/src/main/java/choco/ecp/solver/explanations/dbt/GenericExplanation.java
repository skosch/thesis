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

package choco.ecp.solver.explanations.dbt;

import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.explanations.real.RealBoundExplanation;
import choco.ecp.solver.explanations.real.RealDecSupExplanation;
import choco.ecp.solver.explanations.real.RealIncInfExplanation;
import choco.ecp.solver.search.SymbolicDecision;
import choco.ecp.solver.search.dbt.DecisionSConstraint;
import choco.ecp.solver.variables.integer.dbt.PalmIntVar;
import choco.ecp.solver.variables.real.PalmRealVar;
import choco.kernel.common.util.objects.ConstraintCollection;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.Propagator;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic implementation of explanations. It is used by filtering algorithms before specializing them
 * for specific use (bound modification, value removal, contradiction...).
 */

public class GenericExplanation implements PalmExplanation {
  private static int EXPLANATION_TIMESTAMP = 0;

  public static void reinitTimestamp() {
    EXPLANATION_TIMESTAMP = 0;
  }

  protected int timeStamp;

  /**
   * Set of all the constraint in the explain.
   */

  protected BitSet explanation;


  /**
   * The current problem.
   */

  protected PalmSolver pb;


  /**
   * Initializes the explain set.
   */

  public GenericExplanation(Solver pb) {
    //this.explain = new HashSet();
    this.explanation = new BitSet();
    this.pb = (PalmSolver) pb;
    this.timeStamp = EXPLANATION_TIMESTAMP++;
  }


  public int hashCode() {
    return this.timeStamp;
  }


  /**
   * Pretty print of the explain.
   */

  public String toString() {
    StringBuffer str = new StringBuffer();
    str.append("{");

    for (int i = explanation.nextSetBit(0); i >= 0; i = explanation.nextSetBit(i + 1)) {
      str.append(this.pb.getConstraintNb(i).pretty());
      if (explanation.nextSetBit(i + 1) >= 0)
        str.append(", ");
    }
    /*for (Iterator iterator = explain.iterator(); iterator.hasNext();) {
        str.append(iterator.next());
        if (iterator.hasNext()) str.append(", ");
    } */

    str.append("}");
    return str.toString();
  }


  /**
   * Adds a new constraint in the explain.
   *
   * @param constraint The constraint that should be added to the explain.
   *                   It must be a <code>PalmConstraint</code>.
   */

  public void add(Propagator constraint) {
    //this.explain.add(constraint);
    this.explanation.set(((PalmConstraintPlugin) constraint.getPlugIn()).getConstraintIdx());
  }


  /**
   * Deletes a constraint from the explain.
   *
   * @param constraint The constraint that must be removed.
   */

  public void delete(Propagator constraint) {
    //this.explain.remove(constraint);
    this.explanation.clear(((PalmConstraintPlugin) constraint.getPlugIn()).getConstraintIdx());
  }


  //public void addAll(Set set) {
  //    for (Iterator iterator = set.iterator(); iterator.hasNext();) {
  //        IConstraint constraint = (IConstraint) iterator.next();
  //        this.explanationB.set(((PalmConstraintPlugin)constraint.getPlugIn()).getConstraintIdx());
  //    }
  //this.explain.addAll(set);
  //}

  public void addAll(Collection collection) {
    for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
      AbstractSConstraint c = (AbstractSConstraint) iterator.next();
      this.explanation.set(((PalmConstraintPlugin) c.getPlugIn()).getConstraintIdx());
    }
  }

  /**
   * Merges an explain in the current one.
   *
   * @param explanation The explain with the constraints to add.
   */

  public void merge(ConstraintCollection explanation) {
    //this.explain.addAll(explain.toSet());
    this.explanation.or(((GenericExplanation) explanation).explanation);
    // TODO : prevoir le cas ou ce n'est pas vrai : utilisation de addAll(Set) ! (on suppose ici que la ConstraintCollection poss�de le champs explanation)
  }


  /**
   * Checks if the explain is empty (that is wether the size of the set is null).
   */

  public boolean isEmpty() {
    //return (this.explain.size() == 0);
    return (this.explanation.cardinality() == 0);
  }

  /**
   * Get the size of the bitSet
   *
   * @return the size of the explanation
   */

  public int size() {
    return this.explanation.cardinality();
  }

  /**
   * Clears the constraint set.
   */

  public void empties() {
    //this.explain.clear();
    this.explanation.clear();
  }


  /**
   * Creates a set with all the constraints in the explain.
   *
   * @return The explain as a set.
   */

  public Set toSet() {
    Set ret = new HashSet();
    for (int i = explanation.nextSetBit(0); i >= 0; i = explanation.nextSetBit(i + 1)) {
      ret.add(pb.getConstraintNb(i));
    }
    //ret.addAll(explain);
    return ret;
  }


  /**
   * Copies the explain set and returns the new bitset.
   *
   * @return The explain as a BitSet.
   */

  public BitSet getBitSet() {
    return this.explanation;
  }


  /**
   * Checks if the explain contains a constraint.
   *
   * @param constraint The constraint to search.
   */

  public boolean contains(Propagator constraint) {
    //return (this.explain.contains(constraint));
    return this.explanation.get(((PalmConstraintPlugin) constraint.getPlugIn()).getConstraintIdx());
  }


  /**
   * Clones the explain as a new one.
   */

  public ConstraintCollection copy() {
    GenericExplanation expl = new GenericExplanation(pb);
    expl.merge(this);
    return expl;
  }


  /**
   * Checks if the explain is valid, that is wether all the constraint are active.
   */

  public boolean isValid() {
    for (int i = explanation.nextSetBit(0); i >= 0; i = explanation.nextSetBit(i + 1)) {
      if (this.pb.getConstraintNb(i) == null || !((Propagator) this.pb.getConstraintNb(i)).isActive()) return false;
    }
    //for (Iterator iterator = explain.iterator(); iterator.hasNext();) {
    //    IConstraint constraint = (IConstraint) iterator.next();
    //    if (!constraint.isActive()) return false;
    //}
    return true;
  }

  public boolean isValid(int time) {
    for (int i = explanation.nextSetBit(0); i >= 0; i = explanation.nextSetBit(i + 1)) {
      if (this.pb.getConstraintNb(i) == null || !((Propagator) this.pb.getConstraintNb(i)).isActive() ||
          ((PalmConstraintPlugin) ((AbstractSConstraint) this.pb.getConstraintNb(i)).getPlugIn()).getTimeStamp() > time)
        return false;
    }
    //for (Iterator iterator = explain.iterator(); iterator.hasNext();) {
    //    IConstraint constraint = (IConstraint) iterator.next();
    //    if (!constraint.isActive()) return false;
    //}
    return true;
  }


  /**
   * Checks if another explain is included in this one.
   *
   * @param expl The explain that is tested to be included.
   */

  public boolean containsAll(ConstraintCollection expl) {
    GenericExplanation exp = (GenericExplanation) expl;
    for (int i = exp.explanation.nextSetBit(0); i >= 0; i = exp.explanation.nextSetBit(i + 1)) {
      if (!this.explanation.get(i)) return false;
    }
    // TODO : gerer le cas ou ce n'estpas une GenericExplanation... avec un set
    //for (Iterator iterator = expl.toSet().iterator(); iterator.hasNext();) {
    //    IConstraint constraint = (IConstraint) iterator.next();
    //    if (!this.contains(constraint)) return false;
    //}
    return true;
  }


  /**
   * Deletes all indirect constraints.
   */

  public void clear() {
    BitSet cp = (BitSet) this.explanation.clone();

    for (int i = cp.nextSetBit(0); i >= 0; i = cp.nextSetBit(i + 1)) {
      if (((PalmConstraintPlugin) ((AbstractSConstraint) this.pb.getConstraintNb(i)).getPlugIn()).isIndirect()) {
        this.explanation.clear(i);
      }
    }
    /*Set constraints = this.toSet(); // Copie des contraintes

    for (Iterator iterator = constraints.iterator(); iterator.hasNext();) {
        IConstraint constraint = (IConstraint) iterator.next();
        if (((PalmConstraintPlugin)constraint.getPlugIn()).isIndirect()) {
            this.delete(constraint);
        }
    } */
  }


  /**
   * Updates dependencies.
   */

  public void addDependencies() {
    //this.addDependencies(this.toSet());
    for (int i = this.explanation.nextSetBit(0); i >= 0; i = this.explanation.nextSetBit(i + 1)) {
      ((PalmConstraintPlugin) ((AbstractSConstraint) this.pb.getConstraintNb(i)).getPlugIn()).addDependency(this);
    }
  }


  ///**
  // * Update dependencies.
  // * @param constSet The set of constraint that the dependencies should be updated.
  // */

  /*private void addDependencies(Set constSet) {
for (Iterator iterator = constSet.iterator(); iterator.hasNext();) {
IConstraint constraint = (IConstraint) iterator.next();
((PalmConstraintPlugin)constraint.getPlugIn()).addDependency(this);
}
  } */

  /**
   * Removes all dependencies except for one constraint.
   *
   * @param removed
   */

  public void removeDependencies(SConstraint removed) {
    for (int i = this.explanation.nextSetBit(0); i >= 0; i = this.explanation.nextSetBit(i + 1)) {
      AbstractSConstraint constraint = (AbstractSConstraint) this.pb.getConstraintNb(i);
      if (constraint != removed)
        ((PalmConstraintPlugin) constraint.getPlugIn()).removeDependency(this);
    }
    /*for (Iterator iterator = this.toSet().iterator(); iterator.hasNext();) {
        IConstraint constraint = (IConstraint) iterator.next();
        if (constraint != removed) {
            ((PalmConstraintPlugin)constraint.getPlugIn()).removeDependency(this);
        }
    }*/
  }


  /**
   * Makes an IncInfExplanation from the current explain by adding dependencies.
   *
   * @param inf The previous value of the bound.
   * @param var The involved variable.
   */

  public IBoundExplanation makeIncInfExplanation(int inf, PalmIntVar var) {
    if (Logger.getLogger("choco").isLoggable(Level.FINE))
      Logger.getLogger("choco").fine("Make Inf PalmExplanation for : " + var);
    IncInfExplanation expl = new IncInfExplanation(this.pb, this.explanation, inf, var);
    expl.addDependencies();
    return expl;
  }


  /**
   * Makes a DecSupExplanation from the current explain by adding dependencies.
   *
   * @param sup The previous value of the bound.
   * @param var The involved variable.
   */

  public IBoundExplanation makeDecSupExplanation(int sup, PalmIntVar var) {
    if (Logger.getLogger("choco").isLoggable(Level.FINE))
      Logger.getLogger("choco").fine("Make Sup PalmExplanation for : " + var);
    DecSupExplanation expl = new DecSupExplanation(this.pb, this.explanation, sup, var);
    expl.addDependencies();
    return expl;
  }


  /**
   * Makes a RemovalExplanation from the current explain by adding dependencies.
   *
   * @param value The removed value of the domain.
   * @param var   The involved variable.
   */

  public IRemovalExplanation makeRemovalExplanation(int value, PalmIntVar var) {
    RemovalExplanation expl = new RemovalExplanation(this.pb, this.explanation, value, var);
    expl.addDependencies();
    return expl;
  }


  /**
   * Makes an IncInfExplanation from the current explain by adding dependencies.
   *
   * @param inf The previous value of the bound.
   * @param var The involved variable.
   */

  public RealBoundExplanation makeIncInfExplanation(double inf, PalmRealVar var) {
    /*if (Logger.getLogger("dev.i_want_to_use_this_old_version_of_choco.palm").isLoggable(Level.FINE))
      Logger.getLogger("dev.i_want_to_use_this_old_version_of_choco.palm").fine("Make Inf PalmExplanation for : " + var);*/
    RealIncInfExplanation expl = new RealIncInfExplanation(this.pb, this.explanation, inf, var);
    expl.addDependencies();
    return expl;
  }


  /**
   * Makes a DecSupExplanation from the current explain by adding dependencies.
   *
   * @param sup The previous value of the bound.
   * @param var The involved variable.
   */

  public RealBoundExplanation makeDecSupExplanation(double sup, PalmRealVar var) {
    /*if (Logger.getLogger("dev.i_want_to_use_this_old_version_of_choco.palm").isLoggable(Level.FINE))
      Logger.getLogger("dev.i_want_to_use_this_old_version_of_choco.palm").fine("Make Sup PalmExplanation for : " + var);*/
    RealDecSupExplanation expl = new RealDecSupExplanation(this.pb, this.explanation, sup, var);
    expl.addDependencies();
    return expl;
  }


  /**
   * Posts a restoration prop.
   *
   * @param constraint
   */

  public void postUndoRemoval(SConstraint constraint) {
    if (Logger.getLogger("choco").isLoggable(Level.WARNING))
      Logger.getLogger("choco").warning("GenericExplanation.postUndoRemoval should not be used !!");
  }

  public Set toNogood() {
    Set ret = new HashSet();
    for (int i = explanation.nextSetBit(0); i >= 0; i = explanation.nextSetBit(i + 1)) {
      if (((PalmConstraintPlugin) ((AbstractSConstraint) (pb.getConstraintNb(i))).getPlugIn()).getWeight() == 0) ret.add(pb.getConstraintNb(i));
    }
    return ret;
  }

  public Propagator getConstraint(int i) {
    return this.pb.getConstraintNb(i);
  }

  public SymbolicDecision[] getNogood() {
    ArrayList nogood = new ArrayList();
    for (int i = explanation.nextSetBit(0); i >= 0; i = explanation.nextSetBit(i + 1)) {
      if (((PalmConstraintPlugin) ((AbstractSConstraint) (pb.getConstraintNb(i))).getPlugIn()).getWeight() == 0)
        nogood.add(pb.getConstraintNb(i));
    }
    DecisionSConstraint[] nogoodToReturn = new DecisionSConstraint[nogood.size()];
    return (SymbolicDecision[]) nogood.toArray(nogoodToReturn);
  }
}
