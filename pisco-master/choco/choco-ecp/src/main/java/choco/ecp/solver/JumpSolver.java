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

package choco.ecp.solver;

import choco.cp.solver.CPSolver;
import choco.cp.solver.propagation.ChocEngine;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.cp.solver.search.limit.NodeLimit;
import choco.cp.solver.search.limit.TimeLimit;
import choco.cp.solver.search.real.AbstractRealOptimize;
import choco.cp.solver.search.real.RealBranchAndBound;
import choco.cp.solver.search.real.RealOptimizeWithRestarts;
import choco.cp.solver.variables.integer.IntDomainVarImpl;
import choco.ecp.solver.constraints.PalmSConstraint;
import choco.ecp.solver.constraints.global.matching.PalmAllDifferent;
import choco.ecp.solver.constraints.global.matching.PalmCardinality;
import choco.ecp.solver.constraints.global.matching.PalmOccurence;
import choco.ecp.solver.constraints.real.PalmEquation;
import choco.ecp.solver.constraints.real.exp.PalmRealIntervalConstant;
import choco.ecp.solver.constraints.real.exp.PalmRealMinus;
import choco.ecp.solver.constraints.real.exp.PalmRealMult;
import choco.ecp.solver.constraints.real.exp.PalmRealPlus;
import choco.ecp.solver.explanations.ExplainedConstraintPlugin;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.GenericExplanation;
import choco.ecp.solver.explanations.dbt.JumpConstraintPlugin;
import choco.ecp.solver.explanations.dbt.JumpExplanation;
import choco.ecp.solver.variables.integer.cbj.JumpIntVar;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.real.RealExp;
import choco.kernel.solver.propagation.ConstraintEvent;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.propagation.Propagator;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;
import choco.kernel.solver.variables.real.RealIntervalConstant;
import choco.kernel.solver.variables.real.RealVar;

import java.util.BitSet;
import java.util.logging.Logger;

// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

public class JumpSolver extends CPSolver {

    protected static final Logger logger = Logger.getLogger("choco");

    /**
     * States if the release information should be displayed during the next problem instantiation.
     */
    public static boolean displayRelease = false;


    /**
     * Maximum relaxation level acceptable without user interaction.
     */

    public int maxRelaxLevel = 0;

    /**
     * Set with last erased constraints (index that can be used for posted constraints)
     */

    protected BitSet erasedCst;

    /**
     * Contradiction explanation: a conflict set justifying that the problem is inconsistent.
     */
    protected Explanation contradictionExplastrategynation = null;


    /**
     * an index useful for re-propagating cuts (static constraints)
     * upon backtracking
     */
    public IStateInt indexOfLastInitializedStaticConstraint;


    /**
     * Displays release information (date, verions, ...).
     */

    public static void ReleaseJumpDisplay() {
        logger.info("** Palm : Constraint Programming with Efficient Explanations");
        logger.info("** Palm Copyright (c) 2004 B206");
        displayRelease = false;
    }

    public JumpSolver() {
        super();
        // Ensures a determinist behaviour
        GenericExplanation.reinitTimestamp();
        erasedCst = new BitSet();

        // Specialized engine and solver for Palm
        this.propagationEngine = new ChocEngine(this);
        this.indexOfLastInitializedStaticConstraint = environment.makeInt(PartiallyStoredVector.getFirstStaticIndex() - 1);
        // Displays information about Palm
        if (displayRelease) ReleaseJumpDisplay();
    }

    public void generateSearchSolver() {
        if (null == objective) {
            strategy = new JumpGlobalSearchStrategy(this);
        } else if (restart) {
            if (objective instanceof IntDomainVar)
                strategy = new JumpRestartOptimizer((IntDomainVarImpl) objective, doMaximize);
            else if (objective instanceof RealVar)
                strategy = new RealOptimizeWithRestarts((RealVar) objective, doMaximize);
        } else {
            if (objective instanceof IntDomainVar)
                strategy = new JumpBranchAndBoundOptimizer((IntDomainVarImpl) objective, doMaximize);
            else if (objective instanceof RealVar)
                strategy = new RealBranchAndBound((RealVar) objective, doMaximize);
        }
        strategy.stopAtFirstSol = firstSolution;

        strategy.limits.add(new TimeLimit(strategy, timeLimit));
        strategy.limits.add(new NodeLimit(strategy, nodeLimit));

        generateGoal();
    }

    protected void generateGoal() {
        if (varIntSelector == null) varIntSelector = new MinDomain(this);
        if (valIntIterator == null && valIntSelector == null) valIntIterator = new IncreasingDomain();
        if (valIntIterator != null)
            attachGoal(new JumpAssignVar(varIntSelector, valIntIterator));
        else
            attachGoal(new JumpAssignVar(varIntSelector, valIntSelector));
    }

    public Number getOptimumValue() {
        if (strategy instanceof JumpAbstractOptimizer) {
            return ((JumpAbstractOptimizer) strategy).getBestObjectiveValue();
        } else if (strategy instanceof AbstractRealOptimize) {
            return ((AbstractRealOptimize) strategy).getBestObjectiveValue();
        }
        return null;
    }


    /**
     * Factory to create explanation.
     * It offers the possibility to make another kind of explanation, only by extending PalmProblem
     *
     * @return the new explanation object
     */
    public Explanation makeExplanation() {
        return new JumpExplanation(this);
    }

    /**
     * Factory to create explanation.
     * It offers the possibility to make another kind of explanation, only by extending PalmProblem
     *
     * @return the new explanation object
     */
    public Explanation makeExplanation(int level) {
        return new JumpExplanation(level, this);
    }

    /**
     * Returns all variables of the variables.
     *
     * @deprecated
     */
    public IntDomainVar[] getVars() {
        return this.intVars.toArray();
    }

    public void explainedFail(Explanation exp) throws ContradictionException {
        throw new JumpContradictionException(this, exp);
    }

    /**
     * Posts a constraints in the problem.
     * This is a local constraint post that will be undone upon backtracking
     * If it has ever been posted (but deactivated), it is
     * only reactivated and repropagated.
     *
     * @param cc The constraint to post.
     */
    public void post(SConstraint cc) {
        if (cc instanceof PalmSConstraint) {
            PalmSConstraint c = (PalmSConstraint) cc;
            int idx = constraints.add(c);
            c.addListener(true);
            ((JumpConstraintPlugin) c.getPlugIn()).setConstraintIdx(idx);//constraints.size() - 1);
            ConstraintEvent event = (ConstraintEvent) c.getEvent();
            PropagationEngine pe = getPropagationEngine();
            pe.registerEvent(event);
            pe.postConstAwake(c, true);
        } else {
            throw new Error("impossible to post to a JumpProblem constraints that are not PalmConstraints");
        }
    }

    /**
     * Posts a cut constraint in the problem.
     * (the constraint will not be undone upon backtracking)
     *
     * @param cc The constraint to post.
     */
    public void postCut(SConstraint cc) {
        if (cc instanceof PalmSConstraint) {
            PalmSConstraint c = (PalmSConstraint) cc;
            int idx = constraints.staticAdd(c);
            c.addListener(false);
            indexOfLastInitializedStaticConstraint.set(idx);
            ((JumpConstraintPlugin) c.getPlugIn()).setConstraintIdx(idx);//constraints.size() - 1);
            ConstraintEvent event = (ConstraintEvent) c.getEvent();
            PropagationEngine pe = getPropagationEngine();
            pe.registerEvent(event);
            pe.postConstAwake(c, true);
        } else {
            throw new Error("impossible to post to a JumpProblem cuts that are not PalmConstraints");
        }
    }

    /**
     * popping one world from the stack:
     * overrides AbstractProblem.worldPop because the Problem class adds
     * the notion of static constraints that need be repropagated upon backtracking
     */
    public final void worldPop() {
        super.worldPop();
        int lastStaticIdx = constraints.getLastStaticIndex();
        for (int i = indexOfLastInitializedStaticConstraint.get() + 1; i <= lastStaticIdx; i++) {
            Propagator c = constraints.get(i);
            if (c != null) {
                c.constAwake(true);
            }
        }
    }

    public ExplainedConstraintPlugin makeConstraintPlugin(AbstractSConstraint ct) {
        return new JumpConstraintPlugin(ct);
    }

    /**
     * @param nb Constraint number (number affected when posting and stored
     *           in the variable plugin)
     * @return Returns the constraint
     */
    public AbstractSConstraint getConstraintNb(int nb) {
        return (AbstractSConstraint) this.constraints.get(nb);
    }

    public Explanation getContradictionExplanation() {
        return contradictionExplanation;
    }

    public void setContradictionExplanation(Explanation contradictionExplanation) {
        this.contradictionExplanation = contradictionExplanation;
    }

    public Boolean solve(boolean all) {
        throw new Error("solve not implemented on JumpProblem");
    }

    // ------------------------------------------------------------------------

    // All abstract methods for constructing constraint
    // that need be defined by a Problem implementing a model

    protected SConstraint createEqualXC(IntVar v0, int c) {
        if (v0 instanceof IntDomainVar) {
            return new PalmEqualXC((IntDomainVar) v0, c);
        } else
            return null;
    }

    protected SConstraint createNotEqualXC(IntVar v0, int c) {
        if (v0 instanceof IntDomainVar) {
            return new PalmNotEqualXC((IntDomainVar) v0, c);
        } else
            return null;
    }

    protected SConstraint createGreaterOrEqualXC(IntVar v0, int c) {
        if (v0 instanceof IntDomainVar) {
            return new PalmGreaterOrEqualXC((IntDomainVar) v0, c);
        } else
            return null;
    }

    protected SConstraint createLessOrEqualXC(IntVar v0, int c) {
        if (v0 instanceof IntDomainVar) {
            return new PalmLessOrEqualXC((IntDomainVar) v0, c);
        } else
            return null;
    }

    protected SConstraint createEqualXYC(IntVar v0, IntVar v1, int c) {
        if ((v0 instanceof IntDomainVar) && (v1 instanceof IntDomainVar)) {
            return new PalmEqualXYC((IntDomainVar) v0, (IntDomainVar) v1, c);
        } else
            return null;
    }

    protected SConstraint createGreaterOrEqualXYC(IntVar v0, IntVar v1, int c) {
        if ((v0 instanceof IntDomainVar) && (v1 instanceof IntDomainVar)) {
            return new PalmGreaterOrEqualXYC((IntDomainVar) v0, (IntDomainVar) v1, c);
        } else
            return null;
    }

    protected SConstraint createNotEqualXYC(IntVar v0, IntVar v1, int c) {
        if ((v0 instanceof IntDomainVar) && (v1 instanceof IntDomainVar)) {
            return new PalmNotEqualXYC((IntDomainVar) v0, (IntDomainVar) v1, c);
        } else
            return null;
    }

    protected SConstraint createTimesXYZ(IntVar x, IntVar y, IntVar z) {
        throw new UnsupportedOperationException("Multiplication not implemented in Palm");
    }

    protected SConstraint createIntLinComb(IntVar[] sortedVars, int[] sortedCoeffs, int nbPositiveCoeffs, int c, int linOperator) {
        IntDomainVar[] tmpVars = new IntDomainVar[sortedVars.length];
        System.arraycopy(sortedVars, 0, tmpVars, 0, sortedVars.length);
        return new PalmIntLinComb(tmpVars, sortedCoeffs, nbPositiveCoeffs, c, linOperator);
    }

    protected SConstraint createSubscript(IntVar index, int[] values, IntVar val, int offset) {
        if ((index instanceof IntDomainVar) && (val instanceof IntDomainVar)) {
            return new PalmElt((IntDomainVar) index, (IntDomainVar) val, offset, values);
        } else {
            return null;
        }
    }

    protected SConstraint createOccurrence(IntVar[] vars, int occval, boolean onInf, boolean onSup) {
        IntDomainVar[] tmpVars = new IntDomainVar[vars.length];
        System.arraycopy(vars, 0, tmpVars, 0, vars.length);
        return new PalmOccurence(tmpVars, occval, onInf, onSup);
    }

    public SConstraint createAllDifferent(IntVar[] vars) {
        IntDomainVar[] tmpVars = new IntDomainVar[vars.length];
        System.arraycopy(vars, 0, tmpVars, 0, vars.length);
        return PalmAllDifferent.newAllDiff(tmpVars);
    }


    public SConstraint createGlobalCardinality(IntVar[] vars, int min, int max, int[] low, int[] up) {
        IntDomainVar[] tmpVars = new IntDomainVar[vars.length];
        System.arraycopy(vars, 0, tmpVars, 0, vars.length);
        return new PalmCardinality(tmpVars, min, max, low, up);
    }

    public IntDomainVar createIntVar(String name, int domainType, int min, int max) {
        return new JumpIntVar(this, name, domainType, min, max);
    }

    protected IntDomainVar createIntVar(String name, int[] sortedValues) {
        return new JumpIntVar(this, name, sortedValues);
    }

    public RealIntervalConstant createRealIntervalConstant(double a, double b) {
        return new PalmRealIntervalConstant(a, b);
    }

    protected RealExp createRealPlus(RealExp exp1, RealExp exp2) {
        return new PalmRealPlus(this, exp1, exp2);
    }

    protected RealExp createRealMinus(RealExp exp1, RealExp exp2) {
        return new PalmRealMinus(this, exp1, exp2);
    }

    protected RealExp createRealMult(RealExp exp1, RealExp exp2) {
        return new PalmRealMult(this, exp1, exp2);
    }

    protected SConstraint createEquation(RealVar[] tmpVars, RealExp exp, RealIntervalConstant cst) {
        return new PalmEquation(this, tmpVars, exp, cst);
    }
}
