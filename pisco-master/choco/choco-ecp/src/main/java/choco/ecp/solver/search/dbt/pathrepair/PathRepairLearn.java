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

import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.explanations.dbt.PalmConstraintPlugin;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.search.NogoodSConstraint;
import choco.ecp.solver.search.dbt.PalmGlobalSearchStrategy;
import choco.ecp.solver.search.dbt.PalmLearn;
import choco.kernel.common.util.objects.ConstraintCollection;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;


public class PathRepairLearn extends PalmLearn {

  protected int maxSize = 7;
  protected NogoodSConstraint explanations;

  public PathRepairLearn() {
  }

  public PathRepairLearn(int lSize, NogoodSConstraint ngc) {
    this.maxSize = lSize;
    explanations = ngc;
  }

  public PathRepairLearn(int lSize) {
    this(lSize, null);
  }

  public void setMemory(NogoodSConstraint exp) {
    this.explanations = exp;
  }

  public void addSolution() {
    PalmGlobalSearchStrategy man = this.getManager();
    explanations.addPermanentNogood(man.getState().getPath().copy());
  }

  /**
   * Update the tabou list of nogood
   *
   * @param nogood
   */
  public void addForbiddenSituation(ConstraintCollection nogood) {
    if (explanations.getMemory().size() == maxSize) {
      explanations.removeLastNogood();
    }
    explanations.addNogoodFirst(nogood);
  }


  public void learnFromContradiction(PalmExplanation expl) {
    PalmGlobalSearchStrategy man = this.getManager();
    PalmSolver pb = ((PalmSolver) man.getSolver());
    BitSet bset = expl.getBitSet();    // iterate on the bitset and avoid HashSet !!!!!!
    PalmExplanation nogood = (PalmExplanation) pb.makeExplanation();  // create the nogood associated to the conflict
    for (int i = bset.nextSetBit(0); i >= 0; i = bset.nextSetBit(i + 1)) {
      AbstractSConstraint ct = pb.getConstraintNb(i);
      if (((PalmConstraintPlugin) (ct).getPlugIn()).getWeight() == 0)
        nogood.add(ct);
    }
    addForbiddenSituation(nogood);                    // add it in the tabou list
    informConstraintsInExplanation(nogood);
    //System.out.print("Nogood obtenu : ");
    //debugNogood(nogood);
    //System.out.println();
  }

  /**
   * maintain the searchInfo parameter on each constraint concerned by the conflict
   *
   * @param expl
   */
  public void informConstraintsInExplanation(PalmExplanation expl) {
    if (!expl.isEmpty()) {
      PalmSolver pb = ((PalmSolver) this.getManager().getSolver());
      float sCoef = 1 / (float) expl.size();
      //Iterator it = expl.toSet().iterator();
      BitSet bset = expl.getBitSet();    // iterate on the bitset and avoid HashSet !!!!!!
      for (int i = bset.nextSetBit(0); i >= 0; i = bset.nextSetBit(i + 1)) {
        AbstractSConstraint ct = pb.getConstraintNb(i);
        PathRepairSearchInfo sInfo = (PathRepairSearchInfo) ((PalmConstraintPlugin) (ct).getPlugIn()).getSearchInfo();
        if (sInfo == null) {
          sInfo = new PathRepairSearchInfo();
          ((PalmConstraintPlugin) (ct).getPlugIn()).setSearchInfo(sInfo);
        }
        sInfo.add(sCoef);
      }
    }
  }

  public void learnFromRemoval(AbstractSConstraint ct) {
    PathRepairSearchInfo sInfo = (PathRepairSearchInfo) ((PalmConstraintPlugin) (ct).getPlugIn()).getSearchInfo();
    sInfo.set(0);
  }


  private void debugNogood(PalmExplanation nogood) {
    Set no = nogood.toSet();
    Iterator it = no.iterator();
    while (it.hasNext()) {
      AbstractSConstraint ct = (AbstractSConstraint) it.next();
      System.out.print("" + ct + " : " + ((PathRepairSearchInfo) ((PalmConstraintPlugin) (ct).getPlugIn()).getSearchInfo()).getWeigth() + " ");
    }
  }

  private void debugMemory() {
    System.out.println("-----------");
    System.out.print("Chemin de decision :");
    debugDecisionPath();
    System.out.println("Memoire de DR :");
    Iterator it = explanations.getMemory().listIterator();
    for (; it.hasNext();) {
      debugNogood((PalmExplanation) it.next());
      System.out.println();
    }
    System.out.println("-----------");
  }

  private void debugDecisionPath() {
    Iterator it = this.getManager().getState().getPath().toSet().iterator();
    for (; it.hasNext();) {
      SConstraint ct = (SConstraint) it.next();
      System.out.print(" - " + ct);
    }
    System.out.println();
  }

  public void assertValidSearchInfo(PalmExplanation expl) {
    Iterator it = expl.toSet().iterator();
    for (; it.hasNext();) {
      AbstractSConstraint ct = (AbstractSConstraint) it.next();
      if (((PalmConstraintPlugin) (ct).getPlugIn()).getWeight() == 0) {
        PathRepairSearchInfo sInfo = (PathRepairSearchInfo) ((PalmConstraintPlugin) (ct).getPlugIn()).getSearchInfo();
        assert(sInfo != null);
      }
    }
  }
}
