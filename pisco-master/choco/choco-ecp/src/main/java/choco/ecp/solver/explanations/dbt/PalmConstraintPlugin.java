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
// ______\_/_______     Contibutors: Fran?ois Laburthe, Hadrien Cambazard, Guillaume Rochart...

package choco.ecp.solver.explanations.dbt;

import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.constraints.PalmSConstraint;
import choco.ecp.solver.explanations.ExplainedConstraintPlugin;
import choco.ecp.solver.explanations.Explanation;
import choco.kernel.common.util.objects.ConstraintCollection;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PalmConstraintPlugin implements ExplainedConstraintPlugin {

  /**
   * Global time stamp for dating all constraint plugins when activating.
   */

  private static int PALM_TIME_STAMP;


  /**
   * Constraint associated with this plugin.
   */

  private AbstractSConstraint touchedConstraint;


  /**
   * Time stamp of this plugin (updated when the constraint is activated).
   */

  private int timeStamp = 0;


  /**
   * Weight of the constraint.
   */

  private int weight = Integer.MAX_VALUE;


  /**
   * States if the constraint is already connected to the involved variables.
   */

  private boolean everConnected = false;


  /**
   * States if the constraint is indirect, that is a logically deduced constraint (an indirect constraint
   * is a depending constraint).
   */

  private boolean indirect = false;


  /**
   * States if the constraint is depending, that is depends on the validity of other constraints.
   */

  protected boolean depending = false;


  /**
   * Explanations for indirect constraints. It contains all the constraints it depends on.
   */

  //private Set addingExplanation;
  private PalmExplanation addingExplanation;


  /**
   * Depending constraints, that is constraints that depend on this one.
   */

  private BitSet dependingConstraints;


  /**
   * a.k.a. the alpha set, this is the explanations of filtering decisions depending on this constraint.
   */

  private Set dependencyNet;


  /**
   * List of controlling constraints (boolean constraints for instance).
   */

  private List controllingConstraints;


  /**
   * Open slot for search use.
   */

  private SearchInfo searchInfo;


  /**
   * The index of the constraint in the problem
   */
  private int constraintIdx = -1;

  /**
   * States if the constraint is ephemeral (that is must be erased as soon a removed from the constraint net).
   */
  protected boolean ephemeral = false;


  /**
   * Constructs a plugin for a constraint.
   *
   * @param constraint The constraint this plugin is created for. It should be a <code>PalmConstraint</code>.
   */

  public PalmConstraintPlugin(AbstractSConstraint constraint) {
    this.touchedConstraint = constraint;
    this.dependencyNet = new HashSet();
    this.dependingConstraints = new BitSet();
    this.addingExplanation = (PalmExplanation) ((PalmSolver) this.touchedConstraint.getProblem()).makeExplanation();
    this.controllingConstraints = new LinkedList();
  }


  /**
   * @return The associated number (determined when posting)
   */

  public int getConstraintIdx() {
    return this.constraintIdx;
  }


  /**
   * Sets the constraint number during the post of teh variable.
   *
   * @param nb Number affected to the constraint.
   */

  public void setConstraintIdx(int nb) {
    this.constraintIdx = nb;
  }


  /**
   * Adds a depending constraint.
   *
   * @param constraint The constraint to add as depending.
   */

  void addDependingConstraint(AbstractSConstraint constraint) {
    this.dependingConstraints.set(((PalmConstraintPlugin) constraint.getPlugIn()).constraintIdx);
    //this.dependingConstraints.add(constraint);
  }


  /**
   * Removes a depending constraint.
   *
   * @param constraint The constraint to remove from depending ones.
   */

  void removeDependingConstraint(AbstractSConstraint constraint) {
    this.dependingConstraints.clear(((PalmConstraintPlugin) constraint.getPlugIn()).constraintIdx);
    //this.dependingConstraints.remove(constraint);
  }


  /**
   * Adds the explain of a depending filtering decision.
   *
   * @param e The explain of the filtering decision.
   */

  public void addDependency(ConstraintCollection e) {
    this.dependencyNet.add(e);
  }


  /**
   * Removes the explain of a depending filtering decision.
   *
   * @param e The explain of the filtering decision.
   */

  public void removeDependency(ConstraintCollection e) {
    this.dependencyNet.remove(e);
  }


  /**
   * Removes past effects of the constraint in order to remove it.
   */

  public void undo() {
    for (Iterator iterator = dependencyNet.iterator(); iterator.hasNext();) {
      PalmExplanation explanation = (PalmExplanation) iterator.next();
      explanation.postUndoRemoval(this.touchedConstraint);
    }
    this.dependencyNet.clear();
  }


  /**
   * Checks if the constraint is indirect.
   */

  public boolean isIndirect() {
    return indirect;
  }


  /**
   * Sets the constraint indirect.
   *
   * @param e The explain containing all the constraint this one depends on.
   */

  public void setIndirect(PalmExplanation e) {
    this.addingExplanation = (PalmExplanation) e.copy();
    this.indirect = true;
    this.depending = true;
  }


  /**
   * Checks if the constraint is depending.
   */

  public boolean isDepending() {
    return depending;
  }


  /**
   * Sets the constraint depending.
   */

  public void setDepending(PalmExplanation e) {
    this.addingExplanation = (PalmExplanation) e.copy();
    this.indirect = false;
    this.depending = true;
  }


  /**
   * Sets the constraint direct.
   */

  public void setDirect() {
    this.addingExplanation = (PalmExplanation) ((PalmSolver) this.touchedConstraint.getProblem()).makeExplanation();
    this.indirect = false;
    this.depending = false;
  }


  /**
   * Sets indirect dependance of this constraint: it updates the depending constraints list of all the
   * constraints this one depends on.
   */

  public void setDependance() {
    PalmExplanation e = this.addingExplanation;
    for (Iterator iterator = e.toSet().iterator(); iterator.hasNext();) {
      AbstractSConstraint constraint = (AbstractSConstraint) iterator.next();
      ((PalmConstraintPlugin) constraint.getPlugIn()).addDependingConstraint(this.touchedConstraint);
    }
  }


  /**
   * Removes indirect dependance of this constraint: it updates the depending constraints list of all the
   * constraints this one depends on.
   */

  public void removeDependance() {
    BitSet b = addingExplanation.getBitSet();
    for (int i = b.nextSetBit(0); i >= 0; i = b.nextSetBit(i + 1)) {
      AbstractSConstraint constraint = ((PalmSolver) this.touchedConstraint.getProblem()).getConstraintNb(i);
      ((PalmConstraintPlugin) constraint.getPlugIn()).removeDependingConstraint(this.touchedConstraint);
    }
    //for (Iterator iterator = addingExplanation.iterator(); iterator.hasNext();) {
    //    IConstraint constraint = (IConstraint) iterator.next();
    //    ((PalmConstraintPlugin)constraint.getPlugIn()).removeDependingConstraint(this.touchedConstraint);
    //}
  }


  /**
   * Informs all the constrolling constraints of modifications on this one.
   */

  public void informControllersOfDeactivation() {
    for (int i = 0; i < controllingConstraints.size(); i++) {
      PalmControlConstraint controler = (PalmControlConstraint) controllingConstraints.get(i);
      ((PalmSConstraint) controler.getConstraint()).takeIntoAccountStatusChange(controler.getIndex());
    }
  }


  /**
   * Adds a controlling constraint.
   *
   * @param father A new controlling constraint.
   * @param index  The index of this constraint in the controlling one.
   */

  public void addControl(SConstraint father, int index) {
    this.controllingConstraints.add(new PalmControlConstraint(father, index));
  }


  /**
   * Computes the explain associated to this constraint: either this constraint itself if direct,
   * or the <code>addingExplanation</code> if indirect.
   *
   * @param e The explain on which this explain should be merged.
   */

  public void self_explain(Explanation e) {
    if (indirect) {
      ((PalmExplanation) e).merge(this.addingExplanation);
    } else {
      e.add(this.touchedConstraint);
    }
  }


  /**
   * Activates the current constraint.
   */

  private void activate() {
    this.timeStamp = PALM_TIME_STAMP;
    PALM_TIME_STAMP++;
    if (PALM_TIME_STAMP == Integer.MAX_VALUE) System.err.println("Palm time stamp overflow !");
  }


  /**
   * Deactivates the current constraint.
   */

  private void deactivate() {
    //IConstraint[] constraints = new IConstraint[dependingConstraints.size()];
    //constraints = (IConstraint[]) dependingConstraints.toArray(constraints);
    BitSet bitset = (BitSet) this.dependingConstraints.clone();

    for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
      //for (int i = 0; i < constraints.length; i++) {
      AbstractSConstraint constraint = (AbstractSConstraint) ((PalmSolver) this.touchedConstraint.getProblem()).getConstraintNb(i);
      //PalmConstraintPlugin plugin = (PalmConstraintPlugin)constraints[i].getPlugIn();
      PalmConstraintPlugin plugin = (PalmConstraintPlugin) constraint.getPlugIn();
      plugin.informControllersOfDeactivation();
      //constraints[i].setPassive();
      if (((PalmConstraintPlugin) constraint.getPlugIn()).isEphemeral()) {
        ((PalmSolver) this.touchedConstraint.getProblem()).remove(constraint);
      } else {
        plugin.removeDependance();
        constraint.setPassive();
      }
      plugin.setDirect();
    }

    if (dependingConstraints.cardinality() != 0) {
      if (Logger.getLogger("choco").isLoggable(Level.SEVERE)) {
        Logger.getLogger("choco").severe("Depending Constraints size : " + dependingConstraints.size());
        Logger.getLogger("choco").severe("***** In PalmConstraintPlugin.deactivate(), dependingConstraints should be empty !!!");
        Logger.getLogger("choco").severe("***** Report bug and all possible information to jussien@emn.fr.");
      }
    }
  }


  /**
   * Reacts when this constraint is added as listener on each involved variable.
   */

  public void addListener() {
    this.activate();
    this.everConnected = true;
  }


  /**
   * Reacts when this listener is reactivated.
   */

  public void activateListener() {
    this.activate();
  }


  /**
   * Reacts when this listener is deactivated.
   */

  public void deactivateListener() {
    this.deactivate();
  }


  /**
   * Checks if this constraint is ever connected.
   */

  public boolean isEverConnected() {
    return everConnected;
  }


  /**
   * Gets the weight of the constraint.
   *
   * @return The weight contained in this plugin.
   */

  public int getWeight() {
    return weight;
  }


  /**
   * Sets the weight of the constraint.
   *
   * @param weight The new weight.
   */

  public void setWeight(int weight) {
    this.weight = weight;
  }


  /**
   * Time stamp of the last activation.
   */

  public int getTimeStamp() {
    return timeStamp;
  }

  /**
   * Returns the last affected time stamp
   */
  public static int getLastTimeStamp() {
    return PALM_TIME_STAMP - 1;
  }

  public void setSearchInfo(SearchInfo searchInfo) {
    this.searchInfo = searchInfo;
  }

  public SearchInfo getSearchInfo() {
    return searchInfo;
  }

  public Set getDependencyNet() {
    return dependencyNet;
  }

  public boolean isEphemeral() {
    return ephemeral;
  }

  public void setEphemeral(boolean ephemeral) {
    this.ephemeral = ephemeral;
  }
}
