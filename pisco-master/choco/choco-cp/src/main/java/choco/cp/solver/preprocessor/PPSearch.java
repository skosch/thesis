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

package choco.cp.solver.preprocessor;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.BoundAllDiff;
import choco.cp.solver.constraints.global.matching.AllDifferent;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.cp.solver.constraints.integer.DistanceXYC;
import choco.cp.solver.constraints.integer.DistanceXYZ;
import choco.cp.solver.constraints.integer.IntLinComb;
import choco.cp.solver.constraints.integer.bool.BoolIntLinComb;
import choco.cp.solver.constraints.reified.ReifiedIntSConstraint;
import choco.cp.solver.search.BranchingFactory;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.branching.ImpactBasedBranching;
import choco.cp.solver.search.integer.branching.domwdeg.DomOverWDegBranchingNew;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.DomOverDynDeg;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.AbstractVar;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: Aug 15, 2008
 */
public class PPSearch {

    protected CPModel mod;

    /**
     * Enforce a random value ordering
     */
    protected boolean randval = false;
    protected int randvalseed = 0;

    public void setModel(CPModel m) {
        this.mod = m;
    }

    public void setRandomValueHeuristic(int seed) {
        randval = true;
        randvalseed = seed;
    }

    public boolean isNaryExtensional() {
        return mod.getNbConstraintByType(ConstraintType.TABLE) > 0;
    }

    public boolean isSat() {
        return mod.getNbConstraintByType(ConstraintType.CLAUSES) > 0;
    }

    public boolean isScheduling() {
        return mod.getNbConstraintByType(ConstraintType.DISJUNCTIVE) != 0;
    }

    public boolean isReified() {
        return mod.getConstraintByType(ConstraintType.REIFIEDCONSTRAINT).hasNext();
    }

    public boolean isMixedScheduling() {
        return mod.getNbConstraintByType(ConstraintType.DISJUNCTIVE) +
                mod.getNbConstraintByType(ConstraintType.PRECEDENCE_DISJOINT) +
                mod.getNbConstraintByType(ConstraintType.LEQ) +
                mod.getNbConstraintByType(ConstraintType.LT) +
                mod.getNbConstraintByType(ConstraintType.GT) +
                mod.getNbConstraintByType(ConstraintType.GEQ) !=
                mod.getNbConstraints();
    }

    /**
     * set the DomOverDeg heuristic
     *
     * @param s
     * @return true if the problem was not detected infeasible in the process
     */
    public boolean setVersatile(CPSolver s, int inittime) {
        int h = determineHeuristic(s);
        if (h == 2) return setImpact(s, inittime);
        else return setDomOverWeg(s, inittime);
    }

    /**
     * set the DomOverDeg heuristic
     *
     * @param s
     * @return true if the problem was not detected infeasible in the process
     */
    public boolean setDomOverDeg(CPSolver s) {
        s.setVarIntSelector(new DomOverDynDeg(s));
        if (randval)
            s.setValIntSelector(new RandomIntValSelector(randvalseed));
        else s.setValIntIterator(new IncreasingDomain());
        return true;
    }

    /**
     * set the DomOverWDeg heuristic
     *
     * @param s
     * @return true if the problem was not detected infeasible in the process
     */
    public boolean setDomOverWeg(CPSolver s, int inittime) {
        if (isSat() && s.getNbIntConstraints() == 1) {
            //when there is a single constraint domOverWdeg makes no sense
            return setImpact(s, inittime);
        } else {
            if (randval) {
                if (isScheduling()) {
                    if (!isMixedScheduling()) { //pure scheduling
                        AssignVar dwd = BranchingFactory.domWDeg(s,getBooleanVars(s), new RandomIntValSelector(randvalseed));
                        s.attachGoal(dwd);
                        AssignVar dwd2 = BranchingFactory.minDomIncDom(s, getOtherVars(s));
                        s.addGoal(dwd2);
                    } else {                    //side constraints added
                        AssignVar dwd = BranchingFactory.domWDeg(s,concat(getBooleanVars(s), getOtherVars(s)),
                                new RandomIntValSelector(randvalseed));
                        s.attachGoal(dwd);
                    }
                } else {                        //general case
                	AssignVar dwd = BranchingFactory.domWDeg(s,new RandomIntValSelector(randvalseed));
                    s.attachGoal(dwd);
                }
            } else {
                if (isScheduling()) {
                    if (!isMixedScheduling()) { //pure scheduling
                        DomOverWDegBranchingNew dwd = BranchingFactory.incDomWDeg(s, getBooleanVars(s), new IncreasingDomain());
                        s.attachGoal(dwd);
                        AssignVar dwd2 = BranchingFactory.minDomIncDom(s, getOtherVars(s));
                        s.addGoal(dwd2);
                    } else {                    //side constraints added
                        DomOverWDegBranchingNew dwd = BranchingFactory.incDomWDeg(s,
                                concat(getBooleanVars(s), getOtherVars(s)), new IncreasingDomain());
                        s.attachGoal(dwd);
                    }
                } else {                        //general case
                    DomOverWDegBranchingNew dwd = BranchingFactory.incDomWDeg(s, new IncreasingDomain());
                    s.attachGoal(dwd);
                }
            }
            return true;
        }
    }


    /**
     * set the Impact heuristic
     *
     * @param s
     * @return true if the problem was not detected infeasible in the process
     */
    public boolean setImpact(CPSolver s, int initialisationtime) {
        ImpactBasedBranching ibb;
        IntDomainVar[] bvs = getBooleanVars(s);
        IntDomainVar[] ovs = getOtherVars(s);
        if (isScheduling()) {
            if (!isMixedScheduling()) { //pure scheduling
                ibb = new ImpactBasedBranching(s, bvs);
                s.addGoal(ibb);
                if (randval) ibb.setRandomValueChoice(randvalseed);
                AssignVar dwd2 = BranchingFactory.minDomIncDom(s, ovs);
                s.addGoal(dwd2);
            } else {                    //side constraints added
                ibb = new ImpactBasedBranching(s, concat(getBooleanVars(s), getOtherVars(s)));
                if (randval) ibb.setRandomValueChoice(randvalseed);
                s.attachGoal(ibb);
            }
        } else {                        //general case
            ibb = new ImpactBasedBranching(s);
            if (randval) ibb.setRandomValueChoice(randvalseed);
            s.attachGoal(ibb);
        }
        return true;
    }

//******************************************************************//
//***************** Define the Decision Variables ******************//
//******************************************************************//

    /**
     * Sort the precedences by decreasing sum of the two
     * durations of the two tasks
     */
    public static class BoolSchedComparator implements Comparator {
        public int compare(Object o, Object o1) {
            int sd1 = ((AbstractVar) o).getExtension(2).get();
            int sd2 = ((AbstractVar) o1).getExtension(2).get();
            if (sd1 > sd2) {
                return -1;  //To change body of implemented methods use File | Settings | File Templates.
            } else if (sd1 == sd2) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    public static IntDomainVar[] getBooleanVars(CPSolver s) {
        List<IntDomainVar> ldvs = new ArrayList<IntDomainVar>(s.getNbIntVars());
        for (int i = 0; i < s.getNbIntVars(); i++) {
            IntDomainVar v = s.getIntVar(i);
            if (v.hasBooleanDomain()) {
                ldvs.add(v);
            }
        }
        IntDomainVar[] vs = new IntDomainVar[ldvs.size()];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = ldvs.get(i);
        }
        return vs;
    }

    public static IntDomainVar[] getOtherVars(CPSolver s) {
        List<IntDomainVar> ldvs = new ArrayList<IntDomainVar>(s.getNbIntVars());
        for (int i = 0; i < s.getNbIntVars(); i++) {
            IntDomainVar v = s.getIntVar(i);
            if (v.getDomainSize() > 2) {
                ldvs.add(v);
            }
        }
        IntDomainVar[] vs = new IntDomainVar[ldvs.size()];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = ldvs.get(i);
        }
        return vs;
    }

    public static IntDomainVar[] concat(IntDomainVar[] t1, IntDomainVar[] t2) {
        IntDomainVar[] vs = new IntDomainVar[t1.length + t2.length];
        System.arraycopy(t1, 0, vs, 0, t1.length);
        System.arraycopy(t2, 0, vs, t1.length, t2.length);
        return vs;
    }

    //******************************************************************//
    //***************** Heuristic Identifier ***************************//
    //******************************************************************//

    /**
     * return 1 (domWdeg) or 2 (Impact) depending on the nature of the problem
     */
    public int determineHeuristic(CPSolver s) {
        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        int heuristic = 1;
        if (isSat()) return 2; //degree is unrelevant using the clause propagator
        if (isNaryExtensional()) {
            return 1;
        }
        while(it.hasNext()) {
            SConstraint constraint = it.next();
            if (constraint instanceof Cumulative) return 2;
            if (constraint instanceof AllDifferent) return 2;
            if (constraint instanceof BoundAllDiff) {
                if (constraint.getNbVars() > 10) {
                    heuristic = 2;
                }
            }
            if (constraint instanceof ReifiedIntSConstraint) return 2;
            if (constraint instanceof IntLinComb ||
                    constraint instanceof BoolIntLinComb) {
                int arity = constraint.getNbVars();
                if (arity >= 6) {
                    return 2;
                }
            }
            if (constraint instanceof DistanceXYZ) return 1;
            if (constraint instanceof DistanceXYC) return 1;

        }
        it.dispose();
        if (getSumOfDomains(s) > 500000) {
            return 1;
        }
        return heuristic;
    }

    public static int getSumOfDomains(CPSolver s) {
        int sum = 0;
        for (int i = 0; i < s.getNbIntVars(); i++) {
            sum += s.getIntVar(i).getDomainSize();

        }
        return sum;
    }
}
