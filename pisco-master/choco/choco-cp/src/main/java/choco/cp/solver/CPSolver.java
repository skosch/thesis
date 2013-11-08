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

package choco.cp.solver;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.configure.LimitFactory;
import choco.cp.solver.configure.RestartFactory;
import choco.cp.solver.configure.StrategyFactory;
import choco.cp.solver.constraints.global.Occurrence;
import choco.cp.solver.constraints.global.scheduling.precedence.PrecedenceDisjoint;
import choco.cp.solver.constraints.global.scheduling.precedence.PrecedenceVDisjoint;
import choco.cp.solver.constraints.global.scheduling.precedence.PrecedenceVSDisjoint;
import choco.cp.solver.constraints.integer.*;
import choco.cp.solver.constraints.integer.bool.sat.ClauseStore;
import choco.cp.solver.constraints.integer.extension.*;
import choco.cp.solver.constraints.integer.intlincomb.IntLinCombFactory;
import choco.cp.solver.constraints.real.Equation;
import choco.cp.solver.constraints.real.MixedEqXY;
import choco.cp.solver.constraints.real.exp.*;
import choco.cp.solver.constraints.reified.ExpressionSConstraint;
import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.cp.solver.constraints.set.*;
import choco.cp.solver.goals.GoalSearchSolver;
import choco.cp.solver.propagation.ChocoEngine;
import choco.cp.solver.search.GlobalSearchStrategy;
import choco.cp.solver.search.GoalSearchLoop;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.branching.ImpactBasedBranching;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.cp.solver.search.real.AssignInterval;
import choco.cp.solver.search.real.CyclicRealVarSelector;
import choco.cp.solver.search.real.RealIncreasingDomain;
import choco.cp.solver.search.set.*;
import choco.cp.solver.variables.integer.*;
import choco.cp.solver.variables.real.RealVarImpl;
import choco.cp.solver.variables.set.SetVarImpl;
import choco.kernel.common.IndexFactory;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.memory.structure.StoredBipartiteVarSet;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.*;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.branch.BranchingWithLoggingStatements;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.global.MetaSConstraint;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.constraints.integer.extension.*;
import choco.kernel.solver.constraints.real.RealExp;
import choco.kernel.solver.goals.Goal;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.propagation.Propagator;
import choco.kernel.solver.propagation.listener.PropagationEngineListener;
import choco.kernel.solver.propagation.listener.SetPropagator;
import choco.kernel.solver.search.*;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.search.limit.AbstractGlobalSearchLimit;
import choco.kernel.solver.search.limit.Limit;
import choco.kernel.solver.search.set.AbstractSetVarSelector;
import choco.kernel.solver.variables.AbstractVar;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;
import choco.kernel.solver.variables.real.RealIntervalConstant;
import choco.kernel.solver.variables.real.RealMath;
import choco.kernel.solver.variables.real.RealVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.kernel.solver.variables.set.SetVar;
import gnu.trove.TLongObjectHashMap;

import java.lang.reflect.Array;
import java.util.*;
import java.util.logging.Level;

import static choco.cp.solver.search.BranchingFactory.incDomWDeg;
import static choco.cp.solver.search.BranchingFactory.incDomWDegBin;
import static choco.kernel.common.Constant.FALSE;
import static choco.kernel.common.Constant.TRUE;
import static choco.kernel.common.util.tools.StringUtils.prettyOnePerLine;


/**
 * This class serves both as a factory and as a handler for
 * AbstractGlobalSearchSolvers:
 */
public class CPSolver implements Solver {

    private final IndexFactory indexfactory;
    /**
     * Allows to know if the model is feasible (null if it was not solved).
     */
    private Boolean feasible = null;

    /**
     * The environment managing the backtrackable data.
     */
    private final IEnvironment environment;
    /**
     * The propagation engine to propagate during solving.
     */
    protected PropagationEngine propagationEngine;

    /**
     * an index useful for re-propagating cuts (static constraints) upon
     * backtracking
     */
    private final IStateInt indexOfLastInitializedStaticConstraint;

    /**
     * <br/><b>Goal</b>: specify that a solver read a model only once.
     * <br/><i>Otherwise, the redundant constraints for scheduling must be posted explicitly</i>.
     */
    private boolean uniqueReading = true;

    /**
     * The (optimization or decision) model to which the entity belongs.
     */

    protected CPModel model;

    /**
     * All the constraints of the model.
     */
    private final PartiallyStoredVector<Propagator> constraints;

    /**
     * All the search intVars in the model.
     */
    final StoredBipartiteVarSet<IntDomainVar> intVars;
    /**
     * All the set intVars in the model.
     */
    final StoredBipartiteVarSet<SetVar> setVars;
    /**
     * All the float vars in the model.
     */
    final StoredBipartiteVarSet<RealVar> floatVars;

    final StoredBipartiteVarSet<TaskVar> taskVars;
    /**
     * All the decision integer Vars in the model.
     */
    final List<IntDomainVar> intDecisionVars;
    /**
     * All the decision set Vars in the model.
     */
    final List<SetVar> setDecisionVars;
    /**
     * All the decision float vars in the model.
     */
    final List<RealVar> floatDecisionVars;

    /**
     * All the decision task vars in the model.
     */
    final List<TaskVar> taskDecisionVars;

    /**
     * All the integer constant variables in the model.
     */
    private final Map<Integer, IntDomainVar> intconstantVars;

    /**
     * All the real constant variables in the model.
     */
    private final Map<Double, RealIntervalConstant> realconstantVars;

    protected final TLongObjectHashMap<Var> mapvariables;

    protected final TLongObjectHashMap<SConstraint> mapconstraints;

    /**
     * The variable modelling the objective function
     */
    private Var objective;

    private IntDomainVar makespan;

    /**
     * A global constraint to manage nogoods (as clauses)
     */
    private ClauseStore nogoodStore;

    /**
     * propNogoodWorld give the world above which the nogood constraint
     * need to be propagated
     */
    private int propNogoodWorld = -1;


    public void setLoggingMaxDepth(int loggingMaxDepth) {
        configuration.putInt(Configuration.LOGGING_MAX_DEPTH, loggingMaxDepth);
    }

    public int getLoggingMaxDepth() {
        return configuration.readInt(Configuration.LOGGING_MAX_DEPTH);
    }


    /**
     * set the number of stored solutions.
     * it defines a default {@link ISolutionPool} for the solver. Default pools are partially resizable if 1 < capa < Integer.MaxValue.
     * you can easily provide set your own policy with @{link {@link AbstractSearchStrategy#setSolutionPool(ISolutionPool)}.
     */
    @Override
    public void setSolutionPoolCapacity(int capacity) {
        configuration.putInt(Configuration.SOLUTION_POOL_CAPACITY, capacity);
    }

    /**
     * The object controlling the global search exploration
     */
    protected AbstractGlobalSearchStrategy strategy;

    /**
     * Variable selector for integer
     */
    private VarSelector<IntDomainVar> varIntSelector = null;

    /**
     * Variable selector for real
     */
    private VarSelector<RealVar> varRealSelector = null;

    /**
     * Variable selector for set
     */
    private VarSelector<SetVar> varSetSelector = null;

    /**
     * Value iterator for integer
     */
    private ValIterator<IntDomainVar> valIntIterator;

    /**
     * Value iterator for real
     */
    private ValIterator<RealVar> valRealIterator = null;

    /**
     * Value iterator for set
     */
    private ValIterator<SetVar> valSetIterator = null;

    /**
     * Value selector for integer
     */
    private ValSelector<IntDomainVar> valIntSelector = null;

    /**
     * Value selector for real
     */
    private ValSelector<RealVar> valRealSelector = null;

    /**
     * Value selector for set
     */
    private ValSelector<SetVar> valSetSelector = null;


    protected CPModelToCPSolver mod2sol;

    /**
     * Temporary attached goal for the future generated strategy.
     */
    protected AbstractIntBranchingStrategy tempGoal;
    /**
     * Another way to define search is by using the api similar to ilog on
     * search goals.
     */
    protected Goal ilogGoal = null;

    protected final Configuration configuration;

    protected long readingTime;

    private ISolutionDisplay solutionDisplay;

    private ISolutionMonitor solutionMonitor;

    public CPSolver() {
        this(new EnvironmentTrailing());
    }

    public CPSolver(IEnvironment env) {
        this(env, null);
    }

    public CPSolver(Configuration configuration) {
        this(new EnvironmentTrailing(), configuration);
    }


    public CPSolver(IEnvironment env, Configuration configuration) {
        this.environment = env;
        this.configuration = configuration == null ? new Configuration() : new Configuration(configuration);
        mod2sol = new CPModelToCPSolver(this);
        mapvariables = new TLongObjectHashMap<Var>();
        mapconstraints = new TLongObjectHashMap<SConstraint>();
        intVars = new StoredBipartiteVarSet<IntDomainVar>(env);
        setVars = new StoredBipartiteVarSet<SetVar>(env);
        floatVars = new StoredBipartiteVarSet<RealVar>(env);
        taskVars = new StoredBipartiteVarSet<TaskVar>(env);
        intDecisionVars = new ArrayList<IntDomainVar>(16);
        setDecisionVars = new ArrayList<SetVar>(8);
        floatDecisionVars = new ArrayList<RealVar>(8);
        taskDecisionVars = new ArrayList<TaskVar>(8);
        intconstantVars = new HashMap<Integer, IntDomainVar>(10);
        realconstantVars = new HashMap<Double, RealIntervalConstant>(8);
        this.propagationEngine = new ChocoEngine(this);
        this.constraints = env.makePartiallyStoredVector();
        indexfactory = new IndexFactory();
        this.indexOfLastInitializedStaticConstraint = env.makeInt(PartiallyStoredVector.getFirstStaticIndex() - 1);
    }

    public final AbstractGlobalSearchStrategy getSearchStrategy() {
        return strategy;
    }

    /**
     * Calls {@link choco.kernel.solver.Solver#clearGoals()} and set the current strategy to <code>null</code>.
     */
    public void resetSearchStrategy() {
        clearGoals();
        strategy = null;
    }

    /**
     * Removes all of the elements from this solver (optional operation).
     * The solver will be 'empty' after this call returns.
     */
    public void clear() {
        //this.configuration.setDefault();

        mod2sol.clear();
        mapvariables.clear();
        mapconstraints.clear();
        intVars.clear();
        setVars.clear();
        floatVars.clear();
        taskVars.clear();
        intDecisionVars.clear();
        setDecisionVars.clear();
        floatDecisionVars.clear();
        taskDecisionVars.clear();
        intconstantVars.clear();
        realconstantVars.clear();
        this.constraints.clear();
        this.mapvariables.clear();
        this.mapconstraints.clear();
        this.environment.clear();
        this.propagationEngine.clear();

        this.indexOfLastInitializedStaticConstraint.set(PartiallyStoredVector.getFirstStaticIndex() - 1);

        this.strategy = null;
        this.feasible = null;
        this.uniqueReading = true;
        this.ilogGoal = null;
        this.tempGoal = null;
        this.propNogoodWorld = -1;
        this.nogoodStore = null;
        this.makespan = null;
        this.objective = null;
        this.varIntSelector = null;
        this.valIntIterator = null;
        this.valIntSelector = null;
        this.varSetSelector = null;
        this.valSetIterator = null;
        this.valSetIterator = null;
        this.varRealSelector = null;
        this.varRealSelector = null;
        this.solutionDisplay = null;
        this.solutionMonitor = null;
        this.model = null;
    }

    public final boolean isUniqueReading() {
        return uniqueReading;
    }

    public final void setUniqueReading(boolean uniqueReading) {
        this.uniqueReading = uniqueReading;
    }

    public final void setSolutionDisplay(ISolutionDisplay prettySolution) {
        this.solutionDisplay = prettySolution;
    }

    public final void setSolutionMonitor(ISolutionMonitor solutionMonitor) {
        this.solutionMonitor = solutionMonitor;
    }

    public final IndexFactory getIndexfactory() {
        return indexfactory;
    }

    public boolean contains(Variable v) {
        return mapvariables.containsKey(v.getIndex());
    }

    public String summaryToString() {
        StringBuilder buffer = new StringBuilder(40);
        buffer.append("Pb[");
        buffer.append(getNbIntVars() + getNbRealVars() + getNbSetVars())
                .append(" vars, ");
        buffer.append(getNbTaskVars()).append(" tasks, ");
        buffer.append(getNbIntConstraints()).append(" cons]");
        if (strategy != null) {
            buffer.append("\nLimits");
            buffer.append(strategy.runtimeStatistics());
        }
        return new String(buffer);
    }

    public String pretty() {
        StringBuffer buf = new StringBuffer(summaryToString());
        buf.append('\n');
        buf.append(this.varsToString());
        buf.append(this.constraintsToString());
        return new String(buf);
    }

    public String varsToString() {
        StringBuffer buf = new StringBuffer(40);
        buf.append("==== VARIABLES ====\n");
        for (int i = 0; i < getNbIntVars(); i++) {
            buf.append(getIntVar(i).pretty());
            buf.append('\n');
        }
        for (int i1 = 0; i1 < floatVars.size(); i1++) {
            Object floatVar = floatVars.get(i1);
            RealVar realVar = (RealVar) floatVar;
            buf.append(realVar.pretty());
            buf.append('\n');
        }
        for (int i = 0; i < setVars.size(); i++) {
            buf.append(getSetVar(i).pretty());
            buf.append('\n');
        }
        buf.append("==== TASKS ====\n");
        //noinspection unchecked
        buf.append(prettyOnePerLine(taskVars.toList()));
        return new String(buf);
    }

    public String constraintsToString() {
        StringBuffer buf = new StringBuffer(40);
        buf.append("==== CONSTRAINTS ====\n");
        DisposableIterator it = constraints.getIterator();
        while (it.hasNext()) {
            AbstractSConstraint c = (AbstractSConstraint) it.next();
            buf.append(c.pretty());
            buf.append('\n');
        }
        it.dispose();
        return new String(buf);
    }

    @Override
    public String toString() {
        return summaryToString();
    }

    public void read(Model m) {
        long timer = -System.currentTimeMillis();
        if (this.model == null) {
            this.model = (CPModel) m;
            initReading();
        } else {
            if (this.model != m) {
                LOGGER.severe("Reading two different models is forbidden!");
                throw new SolverException("Reading two different models is forbidden!");
            }
            LOGGER.warning("Reading a model twice or more is strongly inadvisable!");
        }
        mod2sol.readVariables(model);
        mod2sol.readDecisionVariables();
        mod2sol.readConstraints(model);
        readingTime = timer + System.currentTimeMillis();
    }

    /**
     * Prepare Solver to read the model
     */
    protected void initReading() {
        //0- create data structure for boolean variable
        this.environment.createSharedBipartiteSet(model.getNbBoolVar());

    }


    public SConstraint makeSConstraint(Constraint mc) {
        return mod2sol.makeSConstraint(mc);
    }

    public SConstraint makeSConstraint(Constraint mc, boolean b) {
        return mod2sol.makeSConstraint(mc, b);
    }

    public SConstraint[] makeSConstraintAndOpposite(Constraint mc) {
        return mod2sol.makeSConstraintAndOpposite(mc);
    }

    public SConstraint[] makeSConstraintAndOpposite(Constraint mc, boolean b) {
        return mod2sol.makeSConstraintAndOpposite(mc, b);
    }

    public void addConstraint(Constraint... tabic) {
        addConstraint(false, tabic);
    }

    public void addConstraint(boolean dynamic, Constraint... tabic) {
        Constraint ic;
        for (Constraint aTabic : tabic) {
            ic = aTabic;
            Iterator<Variable> it = ic.getVariableIterator();
            while (it.hasNext()) {
                Variable v = it.next();
                if (!mapvariables.containsKey(v.getIndex())) {
                    _readVariable(v);
                }
            }
            ic.findManager(model.properties);
            mod2sol.readConstraint(ic, model
                    .getDefaultExpressionDecomposition(), dynamic);
        }
    }

    private void _readVariable(Variable v) {
        v.findManager(model.properties);
        switch (v.getVariableType()) {
            case INTEGER:
            case SET:
            case REAL:
            case CONSTANT_INTEGER:
            case CONSTANT_DOUBLE:
            case CONSTANT_SET:
                this.mapvariables.put(v.getIndex(), mod2sol.readModelVariable(v));
                break;
            case INTEGER_EXPRESSION:
            case SET_EXPRESSION:
            case REAL_EXPRESSION:
            case MULTIPLE_VARIABLES:
                final Iterator<Variable> it = v.getVariableIterator();
                while (it.hasNext()) {
                    _readVariable(it.next());
                }
                break;
            default:
                throw new SolverException("unknown variable type :" + v.getVariableType());
        }
    }

    /**
     * Retrieves the model of the entity
     */

    public Model getModel() {
        return model;
    }

    public void setModel(Model aModel) {
        this.model = (CPModel) aModel;
    }

    /**
     * Set the precision of the search for a real model.
     */
    @Override
    public final void setPrecision(double aPrecision) {
        configuration.putDouble(Configuration.REAL_PRECISION, aPrecision);
    }

    /**
     * Get the precision of the search for a real model.
     */
    @Override
    public final double getPrecision() {
        return configuration.readDouble(Configuration.REAL_PRECISION);
    }

    /**
     * Set the minimal width reduction between two propagations.
     */
    @Override
    public final void setReduction(double aReduction) {
        configuration.putDouble(Configuration.REAL_REDUCTION, aReduction);
    }

    /**
     * Get the minimal width reduction between two propagations.
     */
    @Override
    public final double getReduction() {
        return configuration.readDouble(Configuration.REAL_REDUCTION);
    }

    /**
     * Returns the memory environment used by the model.
     */

    public final IEnvironment getEnvironment() {
        return environment;
    }

    public final void setFeasible(Boolean b) {
        this.feasible = b;
    }

    public String solutionToString() {
        if (solutionDisplay == null) {
            //Default solution output
            StringBuffer buf = new StringBuffer(40);
            for (int i = 0; i < getNbIntVars(); i++) {
                IntDomainVar v = getIntVar(i);
                if (v.isInstantiated()) {
                    buf.append(v.toString());
                    buf.append(", ");
                }
            }
            for (int j = 0; j < getNbRealVars(); j++) {
                RealVar v = getRealVar(j);
                if (v.isInstantiated()) {
                    buf.append(v.toString());
                    buf.append(", ");
                }
            }

            for (int k = 0; k < getNbSetVars(); k++) {
                SetVar v = getSetVar(k);
                if (v.isInstantiated()) {
                    buf.append(v.toString());
                    buf.append(", ");
                }
            }
            return new String(buf);
        } else {
            //Custom solution output
            return solutionDisplay.solutionToString();
        }

    }

    public void setRandomSelectors(long seed) {
        this.setRandomSelectors(new Random(seed));
    }

    public void setRandomSelectors() {
        this.setRandomSelectors(new Random());
    }

    public void setRandomSelectors(Random manager) {
        if (intDecisionVars == null || intDecisionVars.isEmpty()) {
            this.setVarIntSelector(new RandomIntVarSelector(this, manager
                    .nextLong()));
        } else {
            IntDomainVar[] v = intDecisionVars
                    .toArray(new IntDomainVar[intDecisionVars.size()]);
            this.setVarIntSelector(new RandomIntVarSelector(this, v, manager
                    .nextLong()));
        }
        this.valIntSelector = new RandomIntValSelector(manager.nextLong());

        if (setDecisionVars == null || setDecisionVars.isEmpty()) {
            this.setVarSetSelector(new RandomSetVarSelector(this, manager
                    .nextLong()));
        } else {
            SetVar[] v = setDecisionVars.toArray(new SetVar[setDecisionVars
                    .size()]);
            this.setVarSetSelector(new RandomSetVarSelector(this, v, manager
                    .nextLong()));
        }
        this.valSetSelector = new RandomSetValSelector(manager.nextLong());
    }


    //TODO: remove when GoalSearchLoop is OK
    public static final boolean GOAL = false;


    /**
     * Generate a search strategy to run over the tree search. The search
     * strategy is build, according to the choice of the user :
     * <ul>
     * <li>what are the options and goals? (ilogGoal, basic goals, objective,
     * restart),</li>
     * <li>search for one or every solution? (solve, solveAll),</li>
     * <li>what are the limits wanted? (time, node, backtrack, fail)</li>
     * <li>does it use recomputation?</li>
     * </ul>
     */
    public void generateSearchStrategy() {
        if (!(tempGoal instanceof ImpactBasedBranching)
                || strategy == null) { // <hca> really ugly to remove once
            // impact ok

            // There is no objective to reach
            if (null == objective) {
                StrategyFactory.validateCSP(this);
                // an ilogGoal has been defined
                if (GOAL) {
                    if (ilogGoal != null) {
                        strategy = new GoalSearchSolver(this, ilogGoal);
                    }
                    // Basic search strategy
                    else {
                        strategy = new GlobalSearchStrategy(this);
                    }
                } else {
                    strategy = new GlobalSearchStrategy(this);
                }
            }
            // there is an objective to reach
            else {
                // ilogGoal has been defined => Error
                if (ilogGoal != null) {
                    throw new UnsupportedOperationException(
                            "Ilog goal are not yet available in optimization");
                }
                strategy = StrategyFactory.createBranchAndBound(this);
            }
        }


        assert strategy != null;
        strategy.setSolutionPool(StrategyFactory.createSolutionPool(strategy));
        strategy.setSolutionMonitor(solutionMonitor);
        generateSearchLoop();
        strategy.setLimitManager(LimitFactory.createLimitManager(strategy));
        strategy.setShavingTools(StrategyFactory.createShavingTools(this));

        if (ilogGoal == null) {
            if (tempGoal == null) {
                generateDefaultGoal();
            } else {
                attachGoal(tempGoal);
                tempGoal = null;
            }
        }
        //logging statements
        if (ChocoLogging.getBranchingLogger().isLoggable(Level.INFO)) {
            if (strategy.mainGoal == null) {
                LOGGER.warning("can not set logging (no goals)");
            } else {
                strategy.mainGoal = BranchingWithLoggingStatements.setLoggingStatement(strategy.mainGoal);
            }
        }
    }

    protected void generateSearchLoop() {
        AbstractSearchLoop searchLoop;
        if (!GOAL) {
            if (ilogGoal != null) {
                searchLoop = new GoalSearchLoop(strategy, ilogGoal);
            } else {
                searchLoop = StrategyFactory.createSearchLoop(strategy);
            }
        } else {
            searchLoop = StrategyFactory.createSearchLoop(strategy);
        }
        strategy.setSearchLoop(searchLoop);
    }

    /**
     * Generate the default branching strategie for RealVar.
     *
     * @return
     */
    @SuppressWarnings({"SuspiciousToArrayCall"})
    public AbstractIntBranchingStrategy generateDefaultRealGoal() {
        // default strategy choice for real
        if (varRealSelector == null) {
            if (floatDecisionVars.isEmpty()) {
                varRealSelector = new CyclicRealVarSelector(this);
            } else {
                RealVar[] tvars = new RealVar[floatDecisionVars.size()];
                floatDecisionVars.toArray(tvars);
                varRealSelector = new CyclicRealVarSelector(this, tvars);
            }
        }
        if (valRealIterator == null && valRealSelector == null) {
            valRealIterator = new RealIncreasingDomain();
        }
        return valRealIterator != null ? new AssignInterval(varRealSelector,
                valRealIterator) : new AssignVar(varRealSelector,
                valRealSelector);
    }

    public AbstractIntBranchingStrategy generateSetDefaultGoal() {
        // default strategy choice for set
        if (varSetSelector == null) {
            if (setDecisionVars.isEmpty()) {
                varSetSelector = new MinDomSet(this);
            } else {
                SetVar[] t = new SetVar[setDecisionVars.size()];
                setDecisionVars.toArray(t);
                varSetSelector = new MinDomSet(this, t);
            }
        }
        if (valSetSelector == null && valSetIterator == null) {
            valSetSelector = new MinEnv();
        }
        return new AssignSetVar(varSetSelector, valSetSelector);
    }

    public AbstractIntBranchingStrategy generateDefaultIntGoal() {
        // default strategy choice for integer
        if (valIntIterator == null && valIntSelector == null) {
            valIntIterator = new IncreasingDomain();
        }

        if (varIntSelector == null) {
            if (intDecisionVars.isEmpty()) {
                return valIntIterator == null ? incDomWDegBin(this, valIntSelector) : incDomWDeg(this, valIntIterator);

            } else {
                IntDomainVar[] t = new IntDomainVar[intDecisionVars.size()];
                intDecisionVars.toArray(t);
                return valIntIterator == null ? incDomWDegBin(this, t, valIntSelector) : incDomWDeg(this, t, valIntIterator);

            }
        } else {
            return valIntIterator == null ? new AssignVar(varIntSelector, valIntSelector) : new AssignVar(varIntSelector, valIntIterator);
        }
    }

    // default strategy
    protected void generateDefaultGoal() {
        boolean first = true;
        if (getNbSetVars() > 0 && !setDecisionVars.isEmpty()) {
            addGoal(generateSetDefaultGoal());
            first = false;
        }

        if (getNbIntVars() > 0 && !intDecisionVars.isEmpty()) {
            if (first) {
                addGoal(generateDefaultIntGoal());
                first = false;
            } else {
                addGoal(generateDefaultIntGoal());
            }
        }

        if (getNbRealVars() > 0 && !floatDecisionVars.isEmpty()) {
            if (first) {
                addGoal(generateDefaultRealGoal());
            } else {
                addGoal(generateDefaultRealGoal());
            }
        }
    }

    /**
     * {@inheritDoc}
     * Attach the FIRST branching strategy to the search strategy
     *
     * @param branching the branching strategy
     * @deprecated calls {@link CPSolver#clearGoals()} then
     *             {@link CPSolver#addGoal(choco.kernel.solver.branch.AbstractIntBranchingStrategy)}
     */
    @Deprecated
    public void attachGoal(AbstractIntBranchingStrategy branching) {
        clearGoals();
        addGoal(branching);
    }

    /**
     * {@inheritDoc}
     * Add branching strategy to the search strategy. (Do not have to be called
     * before attachGoal(AbstractIntBranching branching))
     *
     * @param branching the next branching strategy Branching strategies are run in
     *                  the order given by the adding.
     */
    public void addGoal(AbstractIntBranchingStrategy branching) {
        // if no branching strategy has been already generated
        if (strategy == null) {
            // if this is the first branching strategy
            if (tempGoal == null) {
                tempGoal = branching;
            } else {
                AbstractIntBranchingStrategy br = tempGoal;
                while (br.getNextBranching() != null) {
                    br = (AbstractIntBranchingStrategy) br.getNextBranching();
                }
                br.setNextBranching(branching);
            }
        }
        // otherwise
        else {
            branching.setSolver(strategy);
            if (strategy.mainGoal == null) {
                AbstractIntBranchingStrategy br = branching;
                while (br != null) {
                    br.setSolver(strategy);
                    br = (AbstractIntBranchingStrategy) br.getNextBranching();
                }
                strategy.mainGoal = branching;
            } else {
                AbstractIntBranchingStrategy br = strategy.mainGoal;
                while (br.getNextBranching() != null) {
                    br = (AbstractIntBranchingStrategy) br.getNextBranching();
                }
                br.setNextBranching(branching);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public void clearGoals() {
        // if no branching strategy has been already generated
        if (strategy == null) {
            AbstractIntBranchingStrategy br = tempGoal;
            while (br != null) {
                AbstractIntBranchingStrategy tmp = (AbstractIntBranchingStrategy) br.getNextBranching();
                br.setNextBranching(null);
                br = tmp;
            }
        }
        //otherwise
        else {
            AbstractIntBranchingStrategy br = strategy.mainGoal;
            while (br != null) {
                // To remove properly the listener from the Propagation engine
                if (strategy.mainGoal instanceof PropagationEngineListener) {
                    ((PropagationEngineListener) strategy.mainGoal).safeDelete();
                }
                AbstractIntBranchingStrategy tmp = (AbstractIntBranchingStrategy) br.getNextBranching();
                br.setNextBranching(null);
                br = tmp;
            }
        }
    }

    /**
     * Set the ilogGoal of the search strategy
     *
     * @param anIlogGoal to take into account in the search strategy
     */
    public final void setIlogGoal(Goal anIlogGoal) {
        this.ilogGoal = anIlogGoal;
    }

    /**
     * commands the strategy to start.
     * Use {@link ChocoLogging#flushLogs()} to flush search logs.
     */
    public void launch() {
        // strategy.run();
        strategy.incrementalRun();
    }

    /**
     * returns the number of solutions encountered during the search
     *
     * @return the number of solutions to the model that were encountered during
     *         the search
     */
    public int getNbSolutions() {
        return strategy.getSolutionCount();
    }


    /**
     * Monitor the time limit (default to true)
     *
     * @param b indicates wether the search stategy monitor the time limit
     * @deprecated the limit is always monitored
     */
    @Deprecated
    public void monitorTimeLimit(boolean b) {
    }


    /**
     * Monitor the node limit (default to true)
     *
     * @param b indicates wether the search stategy monitor the node limit
     * @deprecated the limit is always monitored
     */
    @Deprecated
    public void monitorNodeLimit(boolean b) {
    }

    /**
     * Monitor the backtrack limit (default to false)
     *
     * @param b indicates wether the search stategy monitor the backtrack
     *          limit
     * @deprecated the limit is always monitored
     */
    @Deprecated
    public void monitorBackTrackLimit(boolean b) {
    }

    /**
     * Monitor the fail limit (default to false)
     *
     * @param b indicates wether the search stategy monitor the fail limit
     */
    public void monitorFailLimit(boolean b) {
        if (b) propagationEngine.getFailMeasure().safeAdd();
        else propagationEngine.getFailMeasure().safeDelete();
    }

    /**
     * Sets the time limit i.e. the maximal time before stopping the search
     * algorithm
     */
    public void setTimeLimit(int timeLimit) {
        LimitFactory.setSearchLimit(this, Limit.TIME, timeLimit);
    }


    /**
     * Sets the node limit i.e. the maximal number of nodes explored by the
     * search algorithm
     */
    public void setNodeLimit(int nodeLimit) {
        LimitFactory.setSearchLimit(this, Limit.NODE, nodeLimit);
    }

    /**
     * Sets the backtrack limit i.e. the maximal number of backtracks explored
     * by the search algorithm
     */
    public void setBackTrackLimit(int backTrackLimit) {
        LimitFactory.setSearchLimit(this, Limit.BACKTRACK, backTrackLimit);
    }

    /**
     * Sets the fail limit i.e. the maximal number of fail explored by the
     * search algorithm
     */
    public void setFailLimit(int failLimit) {
        LimitFactory.setSearchLimit(this, Limit.FAIL, failLimit);
    }

    /**
     * Sets the restart limit i.e. the maximal number of restart performed during the search algorithm.
     * The limit does not stop the search only the restart process.
     */
    public void setRestartLimit(int restartLimit) {
        LimitFactory.setRestartLimit(this, Limit.RESTART, restartLimit);
    }


    /**
     * Get the time spent in reading the model
     *
     * @return
     */
    @Override
    public int getReadingTimeCount() {
        return model == null ? 0 : (int) readingTime;
    }


    /**
     * Get the time spent in the initial propagation
     *
     * @return
     */
    @Override
    public int getInitialPropagationTimeCount() {
        return strategy == null ? 0 : 0;
    }

    /**
     * Get the time count of the search algorithm
     *
     * @return time count
     */
    public final int getTimeCount() {
        return strategy == null ? 0 : strategy.getTimeCount();
    }

    /**
     * Get the node count of the search algorithm
     *
     * @return node count
     */
    public final int getNodeCount() {
        return strategy == null ? 0 : strategy.getNodeCount();
    }

    /**
     * Get the backtrack count of the search algorithm
     *
     * @return strategy == null ? 0 : backtrack count
     */
    public final int getBackTrackCount() {
        return strategy == null ? 0 : strategy.getBackTrackCount();
    }

    /**
     * Get the fail count of the search algorithm
     *
     * @return fail count
     */
    public final int getFailCount() {
        return strategy == null ? 0 : strategy.getFailCount();
    }


    @Override
    public final int getRestartCount() {
        return strategy == null ? 0 : strategy.getRestartCount();
    }

    @Override
    public final int getSolutionCount() {
        return strategy == null ? 0 : strategy.getSolutionCount();
    }

    @Override
    public final Number getObjectiveValue() {
        if (strategy.getObjectiveManager() != null
                && existsSolution()) {
            return strategy.getObjectiveManager().getBestObjectiveValue();
        }
        return null;
    }

    @Override
    public boolean isObjectiveOptimal() {
        final boolean firstSolution = configuration.readBoolean(Configuration.STOP_AT_FIRST_SOLUTION);
        return existsSolution() && !firstSolution && !isEncounteredLimit();
    }

    @Override
    public boolean existsSolution() {
        return strategy != null && strategy.existsSolution();
    }


    /**
     * @return true if only the first solution must be found
     */
    public final boolean getFirstSolution() {
        return configuration.readBoolean(Configuration.STOP_AT_FIRST_SOLUTION);
    }

    /**
     * Sets wether only the first solution must be found
     */
    public final void setFirstSolution(boolean stopAtFirstSolution) {
        configuration.putBoolean(Configuration.STOP_AT_FIRST_SOLUTION, stopAtFirstSolution);
    }


    /**
     * Sets a unique integer variable selector the search olver should use.
     *
     * @see choco.cp.solver.CPSolver#addGoal(choco.kernel.solver.branch.AbstractIntBranchingStrategy)
     */
    public void setVarIntSelector(VarSelector<IntDomainVar> varSelector) {
        // To remove properly the listener from the Propagation engine
        if (this.varIntSelector != null && this.varIntSelector instanceof PropagationEngineListener) {
            ((PropagationEngineListener) this.varIntSelector).safeDelete();
        }
        this.varIntSelector = varSelector;
        IntDomainVar[] vars = ((AbstractIntVarSelector) varSelector).getVars();
        if (vars != null) {
            intDecisionVars.clear();
            intDecisionVars.addAll(Arrays.asList(vars));
        } else if (!intDecisionVars.isEmpty()) {
            vars = new IntDomainVar[intDecisionVars.size()];
            intDecisionVars.toArray(vars);
            ((AbstractIntVarSelector) varSelector).setVars(vars);
        } else {
            intDecisionVars.addAll(intVars.toList());
        }
    }

    /**
     * Sets a unique real variable selector the search strategy should use.
     *
     * @see choco.cp.solver.CPSolver#addGoal(choco.kernel.solver.branch.AbstractIntBranchingStrategy)
     */
    public void setVarRealSelector(VarSelector<RealVar> realVarSelector) {
        this.varRealSelector = realVarSelector;
        floatDecisionVars.addAll(floatVars.toList());
    }

    /**
     * Sets unique set variable selector the search strategy should use.
     *
     * @see choco.cp.solver.CPSolver#addGoal(choco.kernel.solver.branch.AbstractIntBranchingStrategy)
     */
    public void setVarSetSelector(VarSelector<SetVar> setVarSelector) {
        this.varSetSelector = setVarSelector;
        SetVar[] vars = ((AbstractSetVarSelector) setVarSelector).getVars();
        if (vars != null) {
            setDecisionVars.clear();
            setDecisionVars.addAll(Arrays.asList(vars));
        } else if (!setDecisionVars.isEmpty()) {
            vars = new SetVar[setDecisionVars.size()];
            setDecisionVars.toArray(vars);
            ((AbstractSetVarSelector) setVarSelector).setVars(vars);
        } else {
            setDecisionVars.addAll(setVars.toList());
        }
    }

    /**
     * Sets the integer value iterator the search should use
     */
    public void setValIntIterator(ValIterator<IntDomainVar> valIterator) {
        this.valIntIterator = valIterator;
    }

    /**
     * Sets the real value iterator the search should use
     */
    public void setValRealIterator(ValIterator<RealVar> realValIterator) {
        this.valRealIterator = realValIterator;
    }

    /**
     * Sets the integer value iterator the search should use
     */
    public void setValSetIterator(ValIterator<SetVar> valIterator) {
        this.valSetIterator = valIterator;
    }

    /**
     * Sets the integer value selector the search should use
     */
    public void setValIntSelector(ValSelector<IntDomainVar> valSelector) {
        this.valIntSelector = valSelector;
    }

    /**
     * Sets the integer value selector the search should use
     */
    public void setValRealSelector(ValSelector<RealVar> valSelector) {
        this.valRealSelector = valSelector;
    }

    /**
     * Sets the integer value selector the search should use
     */
    public void setValSetSelector(ValSelector<SetVar> setValIntSelector) {
        this.valSetSelector = setValIntSelector;
    }


    /**
     * Perform a search with restarts regarding the number of backtrack. An
     * initial allowed number of backtrack is given (parameter base) and once
     * this limit is reached a restart is performed and the new limit imposed to
     * the search is increased by multiplying the previous limit with the
     * parameter grow. Restart strategies makes really sense with strategies
     * that make choices based on the past experience of the search :
     * DomOverWdeg or Impact based search. It could also be used with a random
     * heuristic
     *
     * @param base : the initial number of fails limiting the first search
     * @param grow : the limit in number of fails grows at each restart by grow *
     *             base;
     */
    public void setGeometricRestart(int base, double grow) {
        RestartFactory.setGeometricalRestartPolicy(this, base, grow);
    }

    /**
     * Perform a search with restarts regarding the number of backtrack. An
     * initial allowed number of backtrack is given (parameter base) and once
     * this limit is reached a restart is performed and the new limit imposed to
     * the search is increased by multiplying the previous limit with the
     * parameter grow. the strategy restart until the number of restart is equal
     * to the restartLimit Restart strategies makes really sense with strategies
     * that make choices based on the past experience of the search :
     * DomOverWdeg or Impact based search. It could also be used with a random
     * heuristic
     *
     * @param base         : the initial number of fails limiting the first search
     * @param grow         : the limit in number of fails grows at each restart by grow *
     *                     base;
     * @param restartLimit the maximum number of restarts
     */
    public void setGeometricRestart(int base, double grow, int restartLimit) {
        RestartFactory.setGeometricalRestartPolicy(this, base, grow);
        LimitFactory.setRestartLimit(this, Limit.RESTART, restartLimit);
    }


    /**
     * Perform a search with restarts regarding the number of backtrack. One way
     * to describe this strategy is to say that all run lengths are power of
     * two, and that each time a pair of runs of a given length has been
     * completed, a run of twice that length is immediatly executed. <br>
     * example with growing factor of 2 : [1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1,
     * 2, 4, 8, 1,...] <br>
     * <br>
     * example with growing factor of 3 : [1, 1, 1, 3, 1, 1, 1, 3, 9,...] <br>
     * the limit is length * base.
     * <p/>
     * Restart strategies makes really sense with strategies that make choices
     * based on the past experience of the search : DomOverWdeg or Impact based
     * search. It could also be used with a random heuristic
     *
     * @param base         : the initial number of fails limiting the first search
     * @param grow         : the geometrical factor for Luby restart strategy
     * @param restartLimit the maximum number of restarts
     */
    public void setLubyRestart(int base, int grow, int restartLimit) {
        RestartFactory.setLubyRestartPolicy(this, base, grow);
        LimitFactory.setRestartLimit(this, Limit.RESTART, restartLimit);
    }

    /**
     * Perform a search with restarts regarding the number of backtrack. One way
     * to describe this strategy is to say that all run lengths are power of
     * two, and that each time a pair of runs of a given length has been
     * completed, a run of twice that length is immediatly executed. <br>
     * example with growing factor of 2 : [1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1,
     * 2, 4, 8, 1,...] <br>
     * <br>
     * example with growing factor of 3 : [1, 1, 1, 3, 1, 1, 1, 3, 9,...] <br>
     * the limit is length * base.
     * <p/>
     * Restart strategies makes really sense with strategies that make choices
     * based on the past experience of the search : DomOverWdeg or Impact based
     * search. It could also be used with a random heuristic
     *
     * @param base : the initial number of fails limiting the first search
     * @param grow : the geometrical factor for Luby restart strategy
     */
    public void setLubyRestart(int base, int grow) {
        RestartFactory.setLubyRestartPolicy(this, base, grow);
    }


    public void setRecordNogoodFromRestart(boolean recordNogoodFromRestart) {
        configuration.putBoolean(Configuration.NOGOOD_RECORDING_FROM_RESTART, recordNogoodFromRestart);
    }

    /**
     * set the optimization strategy: - restart or not after each solution found
     *
     * @param restart indicates wether to restart or not
     */
    public void setRestart(boolean restart) {
        configuration.putBoolean(Configuration.RESTART_AFTER_SOLUTION, restart);
    }


    //	public final RestartConfiguration getRestartConfiguration() {
    //		return restartConfig;
    //	}

    /**
     * a boolean indicating if the strategy minize or maximize the objective
     * function
     *
     * @param maximize indicates wether the strategy is maximizing or not (minimizing)
     */
    public void setDoMaximize(boolean maximize) {
        StrategyFactory.setDoOptimize(this, maximize);
    }

    /**
     * Set the variable to optimize
     *
     * @param anObjective objective variable
     */
    public void setObjective(Var anObjective) {
        if (strategy == null) this.objective = anObjective;
        else if (this.objective != anObjective)
            throw new SolverException("Set the objective before the generation of the strategy.");
    }

    @Override
    public final Var getObjective() {
        return objective;
    }

    @Override
    public boolean isOptimizationSolver() {
        return objective != null;
    }

    @Deprecated
    public Number getOptimumValue() {
        return getObjectiveValue();
    }


    /**
     * set the value before reading the model (>=0);
     */
    public void setHorizon(int horizon) {
        if (makespan == null) {
            configuration.putInt(Configuration.HORIZON_UPPER_BOUND, horizon);
        } else {
            throw new SolverException("cant set the scheduling horizon: makespan variable already exists.");
        }
    }

    public final int getHorizon() {
        return configuration.readInt(Configuration.HORIZON_UPPER_BOUND);
    }

    protected void setMakespan(Var makespan) {
        if (this.makespan == null) {
            this.makespan = (IntDomainVar) makespan;
        } else {
            throw new SolverException("duplicate makespan variable.");
        }
    }

    public IntDomainVar createMakespan() {
        if (makespan == null) {
            final int horizon = configuration.readInt(Configuration.HORIZON_UPPER_BOUND);
            this.makespan = createBoundIntVar("makespan", 0, horizon);
        }
        return this.makespan;
    }


    public final IntDomainVar getMakespan() {
        return makespan;
    }

    public int getMakespanValue() {
        final int horizon = configuration.readInt(Configuration.HORIZON_UPPER_BOUND);
        return makespan == null ? horizon : makespan.getVal();
    }

    /**
     * // create makespan constraint : makespan = max (end(T)
     */
    protected SConstraint makeMapespanConstraint() {
        IntDomainVar[] vars = new IntDomainVar[getNbTaskVars() + 1];
        vars[0] = makespan;
        for (int i = 0; i < getNbTaskVars(); i++) {
            vars[i + 1] = getTaskVar(i).end();
        }
        return new MaxOfAList(environment, vars);
    }


    public void postMakespanConstraint() {
        if (getNbTaskVars() > 0) {
            if (makespan != null) {
                // create makespan constraint : makespan = max (end(T)
                IntDomainVar[] vars = new IntDomainVar[getNbTaskVars() + 1];
                vars[0] = makespan;
                for (int i = 0; i < getNbTaskVars(); i++) {
                    vars[i + 1] = getTaskVar(i).end();
                }
                post(new MaxOfAList(environment, vars));
            } else {
                final int horizon = configuration.readInt(Configuration.HORIZON_UPPER_BOUND);
                if (horizon < Choco.MAX_UPPER_BOUND) {
                    // create makespan constraint : horizon >= end(T)
                    for (TaskVar t : taskVars) {
                        t.postHorizonConstraint(this, horizon);
                    }
                }
            }
        } else if (makespan != null) throw new SolverException("Unused makespan variable");
    }


    /**
     * Post the redundant constraint that captures the reasonnings on tasks consistency.
     */
    public void postTaskConsistencyConstraints() {
        for (int i = 0; i < getNbTaskVars(); i++) {
            getTaskVar(i).detectOrPostConsistencyConstraint(this);
        }
    }


    /**
     * Checks if a limit has been encountered
     */
    public boolean isEncounteredLimit() {
        return strategy != null && strategy.isEncounteredLimit();
    }

    /**
     * If a limit has been encounteres, return the involved limit
     */
    public final AbstractGlobalSearchLimit getEncounteredLimit() {
        return strategy == null ? null : strategy.getEncounteredLimit();
    }

    /**
     * Returns the propagation engine associated to the model
     */

    public final PropagationEngine getPropagationEngine() {
        return propagationEngine;
    }

    /**
     * get the list of decision integer variables.
     *
     * @return an unmodifiable list
     */
    public final IntDomainVar[] getIntDecisionVars() {
        return VariableUtils.getDecisionList(intDecisionVars, intVars.toList(), IntDomainVar.class);
    }

    /**
     * get the list of decision set variables.
     *
     * @return an unmodifiable list
     */
    public final SetVar[] getSetDecisionVars() {
        return VariableUtils.getDecisionList(setDecisionVars, setVars.toList(), SetVar.class);

    }

    /**
     * get the list of decision real variables.
     *
     * @return an unmodifiable list
     */
    public final RealVar[] getRealDecisionVars() {
        return VariableUtils.getDecisionList(floatDecisionVars, floatVars.toList(), RealVar.class);
    }

    /**
     * get the list of decision task variables.
     *
     * @return an unmodifiable list
     */
    public final TaskVar[] getTaskDecisionVars() {
        return VariableUtils.getDecisionList(taskDecisionVars, taskVars.toList(), TaskVar.class);
    }

    /**
     * <i>Network management:</i> Retrieve a variable by its index (all integer
     * variables of the model are numbered in sequence from 0 on)
     *
     * @param i
     */

    public final IntDomainVar getIntVar(int i) {
        return intVars.get(i);
    }


    public final IntDomainVar getIntVarQuick(int i) {
        return intVars.getQuick(i);
    }

    /**
     * Add a integer variable to the integer variables list
     *
     * @param v the variable to add
     */
    public void addIntVar(IntDomainVar v) {
        intVars.add(v);
    }

    public final Var getIntConstant(int i) {
        return intconstantVars.get(i);
    }

    public void addIntConstant(int value, IntDomainVar i) {
        intconstantVars.put(value, i);
    }

    public final Var getRealConstant(double i) {
        return realconstantVars.get(i);
    }

    public void addrealConstant(double value, RealIntervalConstant i) {
        realconstantVars.put(value, i);
    }

    public final Collection<Integer> getIntConstantSet() {
        return intconstantVars.keySet();
    }

    public final Collection<Double> getRealConstantSet() {
        return realconstantVars.keySet();
    }

    public final int getNbIntConstants() {
        return intconstantVars.size();
    }

    public final int getNbRealConstants() {
        return realconstantVars.size();
    }

    public final int getNbConstants() {
        return intconstantVars.size() + realconstantVars.size();
    }

    public final int getIntVarIndex(IntVar c) {
        return intVars.indexOf(c);
    }

    public final int getIntVarIndex(IntDomainVar c) {
        return intVars.indexOf(c);
    }

    public final int getSetVarIndex(SetVar c) {
        return setVars.indexOf(c);
    }

    public final int getRealVarIndex(RealVar c) {
        return floatVars.indexOf(c);
    }

    @Override
    public int getNbVars() {
        return getNbIntVars() + getNbRealVars() + getNbSetVars() + getNbTaskVars();
    }

    /**
     * retrieving the total number of variables
     *
     * @return the total number of variables in the model
     */
    public final int getNbIntVars() {
        return intVars.size();
    }

    /**
     * Returns a real variable.
     *
     * @param i index of the variable
     * @return the i-th real variable
     */
    public final RealVar getRealVar(int i) {
        return floatVars.get(i);
    }

    public final RealVar getRealVarQuick(int i) {
        return floatVars.getQuick(i);
    }

    /**
     * Add a real variable to the real variables list
     *
     * @param rv the variable to add
     */
    public void addRealVar(RealVar rv) {
        floatVars.add(rv);
    }

    /**
     * Returns the number of variables modelling real numbers.
     */
    public final int getNbRealVars() {
        return floatVars.size();
    }

    /**
     * Returns a set variable.
     *
     * @param i index of the variable
     * @return the i-th real variable
     */
    public final SetVar getSetVar(int i) {
        return setVars.get(i);
    }

    public final SetVar getSetVarQuick(int i) {
        return setVars.getQuick(i);
    }

    /**
     * Add a set variable to the set variables list
     *
     * @param sv the variable to add
     */
    public void addSetVar(SetVar sv) {
        setVars.add(sv);
    }

    /**
     * Returns the number of variables modelling real numbers.
     */
    public final int getNbSetVars() {
        return setVars.size();
    }

    @Override
    public final int getNbTaskVars() {
        return taskVars.size();
    }

    @Override
    public final TaskVar getTaskVar(int i) {
        return taskVars.get(i);
    }

    public final TaskVar getTaskVarQuick(int i) {
        return taskVars.getQuick(i);
    }


    @Override
    public int getNbConstraints() {
        return getNbIntConstraints() + (nogoodStore == null ? 0 : nogoodStore.getNbClause());
    }

    /**
     * retrieving the total number of constraints over integers
     *
     * @return the total number of constraints over integers in the model
     */
    public final int getNbIntConstraints() {
        return this.constraints.size();
    }

    /**
     * <i>Network management:</i> Retrieve a constraint by its index.
     *
     * @param i index of the constraint in the model
     * @deprecated
     */

    @Deprecated
    public final AbstractIntSConstraint getIntConstraint(int i) {
        return (AbstractIntSConstraint) constraints.get(i);
    }

    public final DisposableIterator<IntDomainVar> getIntVarIterator() {
        return intVars.quickIterator();
    }

    public final DisposableIterator<SetVar> getSetVarIterator() {
        return setVars.quickIterator();
    }

    public final DisposableIterator<RealVar> getRealVarIterator() {
        return floatVars.quickIterator();
    }


    @Override
    public DisposableIterator<TaskVar> getTaskVarIterator() {
        return taskVars.quickIterator();
    }

    /**
     * @deprecated
     */
    @Deprecated
    @SuppressWarnings({"unchecked"})
    public final DisposableIterator<SConstraint> getIntConstraintIterator() {
        return constraints.getIterator();
    }

    @SuppressWarnings({"unchecked"})
    public final DisposableIterator<SConstraint> getConstraintIterator() {
        return constraints.getIterator();
    }

    /**
     * currentElement if the model has been found to be feasible (there exist
     * solutions) or not. precondition : has to be called after a search
     *
     * @return Boolean.TRUE if a solution was found, Boolean.FALSE if the model
     *         was proven infeasible, null otherwise
     */
    public final Boolean isFeasible() {
        return feasible;
    }

    public boolean isConsistent() {
        DisposableIterator<SConstraint> ctit = this.getConstraintIterator();
        while (ctit.hasNext()) {
            if (!((AbstractSConstraint) ctit.next()).isConsistent()) {
                return false;
            }
        }
        ctit.dispose();
        return true;
    }

    /**
     * Checks if all the variables are instantiated.
     *
     * @return indicates wether every integer variables are instantiated
     */
    public boolean isCompletelyInstantiated() {
        int n = getNbIntVars();
        for (int i = 0; i < n; i++) {
            if (!(getIntVar(i).isInstantiated())) {
                return false;
            }
        }
        return true;
    }

    /**
     * removes (permanently) a constraint from the constraint network Beware,
     * this is a permanent removal, it may not be backtracked Warnin : For a
     * composition of constraint by boolean connectors, only the root constraint
     * may be removed.
     * <br/>
     * post cutted constraints can be removed at anytime, dynamic posted constraints can only be
     * removed at root node (when {@link choco.kernel.memory.IEnvironment#getWorldIndex()} == 0.
     */
    public void eraseConstraint(SConstraint c) {
        constraints.remove(c);
        ((AbstractSConstraint) c).setPassive();
        for (int i = 0; i < c.getNbVars(); i++) {
            AbstractVar v = (AbstractVar) c.getVar(i);
            v.eraseConstraint(c);
        }
    }

    protected void post(SConstraint cc, boolean dynamicPost) {
        if (cc instanceof Propagator) {
            if ((!cc.equals(TRUE) || !constraints.contains(TRUE))
                    && (!cc.equals(FALSE) || !constraints.contains(FALSE))) {
                // avoid adding the TRUE or FALSE constraint more than once
                Propagator c = (Propagator) cc;
                c.activate(environment);
                c.setPropagationEngine(propagationEngine);
                if (dynamicPost) {
                    constraints.add(c);
                } else {
                    int idx = constraints.staticAdd(c);
                    indexOfLastInitializedStaticConstraint.set(idx);
                }
                c.addListener(dynamicPost);
                propagationEngine.registerPropagator(c);
                propagationEngine.postConstAwake(c, true);
                postRedundantSetConstraints(cc);
                if (strategy != null) {
                    strategy.initMainGoal(cc);
                }
            }
        } else if (cc instanceof ExpressionSConstraint) {
            ExpressionSConstraint p = (ExpressionSConstraint) cc;
            p.setScope(this);
            decisionOnExpression(p);
        } else if (cc instanceof MetaSConstraint) {
            MetaSConstraint p = (MetaSConstraint) cc;
            p.addListener(true); //involved tasks record the constraint, useless for propagation
            int nbc = p.getNbSubConstraints();
            for (int i = 0; i < nbc; i++) {
                post(p.getSubConstraints(i), true); //post the subconstraints
            }
        } else {
            throw new SolverException(
                    "impossible to post constraint : " + (cc == null ? "(null)" : cc.getClass().getSimpleName()));
        }
    }

    /**
     * <i>Network management:</i> adding a constraint to the model. Note that
     * this does not propagate anything ! This addition of a constraint is
     * global: the constraint will NOT be un-posted upon backtracking
     *
     * @param cc the constraint to add
     */

    public void postCut(SConstraint cc) {
        post(cc, false);
    }

    /**
     * <i>Network management:</i> adding a constraint to the model. Note that
     * this does not propagate anything ! This addition of a constraint is local
     * to the current search (sub)tree: the constraint will be un-posted upon
     * backtracking
     *
     * @param cc the constraint to add
     */

    public void post(SConstraint cc) {
        post(cc, true);
    }

    public void post(SConstraint... ccs) {
        for (SConstraint cc : ccs) {
            post(cc);
        }
    }

    /**
     * Decide what kind of constraints to post in case on an Expression: -
     * Extract the list, build the table and post GAC - Post Gac on the implicit
     * table - Post FC on the explicit table
     *
     * @param exp expressions constraint
     */
    protected void decisionOnExpression(ExpressionSConstraint exp) {
        Boolean forceDecomp = exp.isDecomposeExp();
        boolean canBeDecomp = exp.checkDecompositionIsPossible();

        if (forceDecomp != null) {
            if (forceDecomp && canBeDecomp) {
                this.post(exp.getDecomposition(this));
            } else {
                this.post(exp.getExtensionnal(this));
            }
        } else
            // If it is a constant constraint
            if (exp.getNbVars() == 0) {
                if (exp.checkTuple(new int[]{})) {
                    this.post(TRUE);
                } else {
                    this.post(FALSE);
                }
            } else if (canBeDecomp) {
                this.post(exp.getDecomposition(this));
            } else {
                this.post(exp.getExtensionnal(this));
            }
    }

    /**
     * Post the redundant constraint that allows to capture the reasonnings on
     * cardinalities
     *
     * @param p constraint
     */
    public void postRedundantSetConstraints(SConstraint p) {
        if (configuration.readBoolean(Configuration.CARD_REASONNING)
                && p instanceof SetPropagator
                && p.getNbVars() > 1) {

            if (p instanceof MemberXY) {
                IntDomainVar card0 = ((SetVar) p.getVar(1)).getCard();
                post(geq(card0, 1));
            } else if (p instanceof IsIncluded) {
                IntDomainVar card0 = ((SetVar) p.getVar(0)).getCard();
                IntDomainVar card1 = ((SetVar) p.getVar(1)).getCard();
                post(leq(card0, card1));
            } else if (p instanceof SetUnion) {
                IntDomainVar card0 = ((SetVar) p.getVar(0)).getCard();
                IntDomainVar card1 = ((SetVar) p.getVar(1)).getCard();
                IntDomainVar card3 = ((SetVar) p.getVar(2)).getCard();
                post(geq(plus(card0, card1), card3));
            } else if (p instanceof SetIntersection) {
                IntDomainVar card0 = ((SetVar) p.getVar(0)).getCard();
                IntDomainVar card1 = ((SetVar) p.getVar(1)).getCard();
                IntDomainVar card3 = ((SetVar) p.getVar(2)).getCard();
                post(geq(card0, card3));
                post(geq(card1, card3));
            } else if (p instanceof Disjoint) {
                IntDomainVar card0 = ((SetVar) p.getVar(0)).getCard();
                IntDomainVar card1 = ((SetVar) p.getVar(1)).getCard();
                int ub = Math.max(((SetVar) p.getVar(0)).getEnveloppeSup(),
                        ((SetVar) p.getVar(1)).getEnveloppeSup());
                int lb = Math.min(((SetVar) p.getVar(0)).getEnveloppeInf(),
                        ((SetVar) p.getVar(1)).getEnveloppeInf());
                SetVar z = this.createBoundSetVar(StringUtils.randomName(), lb, ub);
                setVars.add(z);
                intVars.add(z.getCard());
                post(new SetUnion((SetVar) p.getVar(0), (SetVar) p.getVar(1), z));
                post(eq(plus(card0, card1), z.getCard()));
            }
        }
    }

    /**
     * Add a nogood to the solver. This method can be called at any
     * point in the search (specially at a leaf) and will ensure that
     * the nogood added is propagated through the remaining search tree.
     * A nogood is considered as a clause over boolean variables.
     *
     * @param poslit : a table of Boolean variables standing for the positive
     *               literals
     * @param neglit : a table of Boolean variables standing for the negative
     *               literals
     */
    public void addNogood(IntDomainVar[] poslit, IntDomainVar[] neglit) {
        initNogoodBase();
        nogoodStore.addNoGood(poslit, neglit);
        propNogoodWorld = this.getWorldIndex();
        nogoodStore.constAwake(false);
        //put the nogood store last in the static list
    }


    public void initNogoodBase() {
        if (nogoodStore == null) {
            nogoodStore = new ClauseStore(getBooleanVariables(), environment);
            postCut(nogoodStore);
            //FIXME Problem with variable weights (Dom/WDeg) dans strategy.initMainGoal(cc);
        }
    }

    public final ClauseStore getNogoodStore() {
        return nogoodStore;
    }

    /**
     * @return the number of boolean variables
     */
    public final int getNbBooleanVars() {
        int cpt = 0;
        for (int i = 0; i < getNbIntVars(); i++) {
            IntDomainVar v = getIntVar(i);
            if (v.hasBooleanDomain()) {
                cpt++;
            }
        }
        return cpt;
    }

    /**
     * @return the boolean variables of the problem
     */
    public IntDomainVar[] getBooleanVariables() {
        ArrayList<IntDomainVar> bvs = new ArrayList<IntDomainVar>(16);
        for (int i = 0; i < getNbIntVars(); i++) {
            IntDomainVar v = getIntVarQuick(i);
            if (v.hasBooleanDomain()) {
                bvs.add(v);
            }
        }
        IntDomainVar[] boolvars = new IntDomainVar[bvs.size()];
        bvs.toArray(boolvars);
        return boolvars;
    }

    /**
     * <i>Propagation:</i> Computes consistency on the model (the model may no
     * longer be consistent since the last propagation because of listeners that
     * have been posted and variables that have been reduced
     *
     * @throws choco.kernel.solver.ContradictionException
     *
     */

    public void propagate() throws ContradictionException {
        propagationEngine.propagateEvents();
    }

    /**
     * pushing one world on the stack
     */
    public void worldPush() {
        environment.worldPush();
    }

    /**
     * popping one world from the stack: overrides AbstractModel.worldPop
     * because the Model class adds the notion of static constraints that need
     * be repropagated upon backtracking
     */
    public void worldPop() {
        environment.worldPop();
        propagationEngine.flushEvents();
        int lastStaticIdx = constraints.getLastStaticIndex();
        for (int i = indexOfLastInitializedStaticConstraint.get() + 1; i <= lastStaticIdx; i++) {
            Propagator c = constraints.get(i);
            if (c != null && c != nogoodStore) {
                c.setPassive(); // Set passive to ensure correct first
                // propagation (as in addListerner)
                c.constAwake(true);
            }
        }
        if (propNogoodWorld > this.getWorldIndex()) {
            nogoodStore.setActiveSilently();
            nogoodStore.constAwake(false);
            propNogoodWorld = this.getWorldIndex();
        }
    }

    /**
     * Backtracks to a given level in the search tree.
     */
    public void worldPopUntil(int n) {
        while (environment.getWorldIndex() > n) {
            worldPop();
        }
    }

    /**
     * pushing the world during propagation
     */
    public void worldPushDuringPropagation() {
        propagationEngine.freeze();
        environment.worldPush();
    }

    /**
     * poping the world during propagation
     */
    public void worldPopDuringPropagation() {
        environment.worldPop();
        propagationEngine.flushEvents();
        propagationEngine.unfreeze();
    }

    /**
     * returning the index of the current worl
     */
    public final int getWorldIndex() {
        return environment.getWorldIndex();
    }

    public Boolean solve(boolean all) {
        setFirstSolution(!all);
        generateSearchStrategy();
        launch();
        return feasible;
    }

    public Boolean solve() {
        return solve(false);
    }

    public Boolean solveAll() {
        return solve(true);
    }

    public Boolean nextSolution() {
        return strategy.nextSolution();
    }

    /**
     * Solution checker. Usefull for debug and development.
     * Check also constraints with not instantiated variables
     *
     * @return a boolean indicating wether the solution is correct or not.
     */
    public Boolean checkSolution() {
        return checkSolution(true);
    }

    /**
     * Solution checker. Usefull for debug and development.
     *
     * @param enableConsistency check also constraints with not instantiated variables
     * @return a boolean indicating wether the solution is correct or not.
     */
    public Boolean checkSolution(boolean enableConsistency) {
        DEFAULT_SOLUTION_CHECKER.setEnableConsistency(enableConsistency);
        return DEFAULT_SOLUTION_CHECKER.inspectSolution(this);
    }

    /**
     * Check wether every decisions variables are instantiated
     *
     * @return true if all variables are instantiated
     */
    public boolean checkDecisionVariables() {
        boolean isOk = true;
        if (intDecisionVars != null) {
            for (IntDomainVar intDecisionVar : intDecisionVars) {
                isOk &= intDecisionVar.isInstantiated();
                //                if(LOGGER.isLoggable(Level.CONFIG)){
                //                    printFail(intDecisionVar.getName(), check);
                //                }
            }
        }

        if (setDecisionVars != null) {
            for (SetVar setDecisionVar : setDecisionVars) {
                isOk &= setDecisionVar.isInstantiated();
                //                if(LOGGER.isLoggable(Level.CONFIG)){
                //                    printFail(setDecisionVar.getName(), check);
                //                }
            }
        }

        if (floatDecisionVars != null) {
            for (RealVar floatDecisionVar : floatDecisionVars) {
                isOk &= floatDecisionVar.isInstantiated();
                //                if(LOGGER.isLoggable(Level.CONFIG)){
                //                    printFail(floatDecisionVar.getName(), check);
                //                }
            }
        }
        return isOk;
    }

    /**
     * Displays all the runtime statistics.
     */
    public void printRuntimeStatistics() {
        LOGGER.info(runtimeStatistics());
    }

    /**
     * Print runtime statistics
     *
     * @return
     */
    public String runtimeStatistics() {
        if (strategy != null) {
            return StringUtils.pretty(strategy) + " - " + strategy.limitManager.pretty();
        } else {
            return "";
        }
    }

    /**
     * <i>Resolution:</i> Searches for the solution minimizing the objective
     * criterion.
     *
     * @param obj     The variable modelling the optimization criterion
     * @param restart If true, then a new search is restarted from scratch after
     *                each solution is found; otherwise a single branch-and-bound
     *                search is performed
     */

    public Boolean minimize(Var obj, boolean restart) {
        return optimize(false, obj, restart);
    }

    public Boolean minimize(boolean restart) {
        if (this.objective == null) {
            throw new SolverException("No objective variable defined");
        }
        return optimize(false, this.objective, restart);
    }

    /**
     * <i>resolution:</i> Searches for the solution maximizing the objective
     * criterion.
     *
     * @param obj     The variable modelling the optimization criterion
     * @param restart If true, then a new search is restarted from scratch after
     *                each solution is found; otherwise a single branch-and-bound
     *                search is performed
     */
    public Boolean maximize(Var obj, boolean restart) {
        return optimize(true, obj, restart);
    }

    public Boolean maximize(boolean restart) {
        if (this.objective == null) {
            throw new SolverException("No objective variable defined");
        }
        return optimize(true, this.objective, restart);
    }

    protected Boolean optimize(boolean maximize, Var obj, boolean restart) {
        setDoMaximize(maximize);
        setObjective(obj);
        setRestart(restart);
        setFirstSolution(false);
        generateSearchStrategy();
        launch();
        return this.feasible;
    }

    public void setMinimizationObjective(IntVar obj) {
        objective = obj;
        configuration.putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MINIMIZE);
    }

    public void setMaximizationObjective(IntVar obj) {
        objective = obj;
        configuration.putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MAXIMIZE);
    }

    public boolean useRecomputation() {
        return configuration.readInt(Configuration.RECOMPUTATION_GAP) > 1;
    }

    public void setRecomputation(boolean on) {
        configuration.putInt(Configuration.RECOMPUTATION_GAP, (on ? 10 : 1));
    }

    public final int getRecomputationGap() {
        return configuration.readInt(Configuration.RECOMPUTATION_GAP);
    }

    public void setRecomputationGap(int aRecomputationGap) {
        configuration.putInt(Configuration.RECOMPUTATION_GAP, aRecomputationGap);
    }

    @SuppressWarnings({"unchecked"})
    public final <MV extends Variable, SV extends Var> SV _to(MV mv, SV sv) {
        sv = (SV) mapvariables.get(mv.getIndex());
        return sv;
    }

    @SuppressWarnings({"unchecked"})
    public final <MV extends Variable, SV extends Var> SV[] _to(MV[] mv, SV[] sv) {
        for (int i = 0; i < mv.length; i++) {
            sv[i] = (SV) mapvariables.get(mv[i].getIndex());
        }
        return sv;
    }

    @SuppressWarnings({"unchecked"})
    public final <MV extends Variable, SV extends Var> SV getVar(MV v) {
        return (SV) mapvariables.get(v.getIndex());
    }

    @SuppressWarnings({"unchecked"})
    public final <MV extends Variable, SV extends Var> SV[] getVar(Class<SV> clazz, MV[] mv) {
        SV[] svs = (SV[]) Array.newInstance(clazz, mv.length);
        for (int i = 0; i < mv.length; i++) {
            svs[i] = (SV) mapvariables.get(mv[i].getIndex());
        }
        return svs;
    }

    public final IntDomainVar getVar(IntegerVariable v) {
        return (IntDomainVar) mapvariables.get(v.getIndex());
    }

    public final IntDomainVar[] getVar(IntegerVariable... v) {
        IntDomainVar[] vs = new IntDomainVar[v.length];
        return _to(v, vs);
    }

    public final RealVar getVar(RealVariable v) {
        return (RealVar) mapvariables.get(v.getIndex());
    }

    public final RealVar[] getVar(RealVariable... v) {
        RealVar[] vs = new RealVar[v.length];
        return _to(v, vs);
    }

    public final SetVar getVar(SetVariable v) {
        return (SetVar) mapvariables.get(v.getIndex());
    }

    public final SetVar[] getVar(SetVariable... v) {
        SetVar[] vs = new SetVar[v.length];
        return _to(v, vs);
    }

    public final TaskVar getVar(TaskVariable v) {
        return (TaskVar) mapvariables.get(v.getIndex());
    }

    public final TaskVar[] getVar(TaskVariable... v) {
        TaskVar[] vs = new TaskVar[v.length];
        return _to(v, vs);
    }


    public final SConstraint getCstr(Constraint ic) {
        return mapconstraints.get(ic.getIndex());
    }

    public final void setCardReasoning(boolean creas) {
        configuration.putBoolean(Configuration.CARD_REASONNING, creas);
    }


    /**
     * Record a solution by getting every variables' value.
     */
    @Override
    public Solution recordSolution() {
        //FIXME should be delegated to the search strategy
        //For instance,solution is not checked, no logging statements.
        Solution sol = new Solution(this);
        strategy.writeSolution(sol);
        return sol;
    }


    /**
     * Restore a solution by getting every variables' value.
     */
    @Override
    public void restoreSolution(Solution sol) {
        try {
            // Integer variables
            int nbv = Math.min(getNbIntVars(), sol.getNbIntValues());
            for (int i = 0; i < nbv; i++) {
                if (sol.getIntValue(i) != Solution.NULL) {
                    getIntVarQuick(i).setVal(sol.getIntValue(i));
                }
            }
            // Set variables
            nbv = Math.min(getNbSetVars(), sol.getNbSetValues());
            for (int i = 0; i < nbv; i++) {
                getSetVarQuick(i).setVal(sol.getSetValue(i));
            }

            // Real variables
            nbv = Math.min(getNbRealVars(), sol.getNbRealValues());
            for (int i = 0; i < nbv; i++) {
                getRealVarQuick(i).intersect(sol.getRealValue(i));
            }


        } catch (ContradictionException e) {
            // TODO : see how to deal with error
            LOGGER.severe("BUG in restoring solution !!");
            throw new SolverException("Restored solution not consistent !!");
        }
    }

//	/**
//	 * Override the default configuration 
//	 * @param configuration the configuration
//	 */
//	public void setConfiguration(final Configuration configuration){
//		this.configuration = configuration;
//	}

    /**
     * Return the current configuration of the solver
     *
     * @return the configuration
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }

    // **********************************************************************
    // LOGGERS MANAGEMENT
    // **********************************************************************

    @Deprecated
    public static final int SILENT = 0;
    @Deprecated
    public static final int SOLUTION = 1;
    @Deprecated
    public static final int SEARCH = 2;
    @Deprecated
    public static final int PROPAGATION = 3;
    @Deprecated
    public static final int FINEST = 4;


    /**
     * use {@link ChocoLogging#setVerbosity(Verbosity)}
     *
     * @param verbosity logger verbosity
     * @deprecated
     */
    @SuppressWarnings({"deprecation"})
    @Deprecated
    public static void setVerbosity(int verbosity) {
        switch (verbosity) {
            case SOLUTION:
                ChocoLogging.setVerbosity(Verbosity.SOLUTION);
                break;
            case SEARCH:
                ChocoLogging.setVerbosity(Verbosity.SEARCH);
                break;
            case PROPAGATION:
                ChocoLogging.setVerbosity(Verbosity.FINEST);
                break;
            case FINEST:
                ChocoLogging.setVerbosity(Verbosity.FINEST);
                break;
            case SILENT:
                ChocoLogging.setVerbosity(Verbosity.SILENT);
                break;
            default:
                ChocoLogging.setVerbosity(Verbosity.SILENT);
                break;
        }
    }

    /**
     * use {@link ChocoLogging#flushLogs()}
     */
    @Deprecated
    public static void flushLogs() {
        ChocoLogging.flushLogs();
    }


    // All abstract methods for constructing constraint
    // that need be defined by a Model implementing a model

    // ***********************************************************************
    // VARIABLES DECLARATION
    // ***********************************************************************

    public IntDomainVar createIntVar(String name, int domainType, int min,
                                     int max) {
        IntDomainVar v = new IntDomainVarImpl(this, name, domainType, min, max);
        intVars.add(v);
        return v;
    }

    public IntDomainVar createBooleanVar(String name) {
        IntDomainVar v = new BooleanVarImpl(this, name);
        intVars.add(v);
        intDecisionVars.add(v);
        return v;
    }

    public IntDomainVar createNotBooleanVar(String name, IntDomainVar variable) {
        IntDomainVar v = new BoolVarNot(this, name, (BooleanVarImpl) variable);
        intVars.add(v);
        intDecisionVars.add(v);
        return v;
    }

    public IntDomainVar createBoundIntVar(String name, int min, int max) {
        IntDomainVar v = new IntDomainVarImpl(this, name, IntDomainVar.BOUNDS,
                min, max);
        intVars.add(v);
        intDecisionVars.add(v);
        return v;
    }

    public IntDomainVar createEnumIntVar(String name, int min, int max) {
        IntDomainVar v = new IntDomainVarImpl(this, name, IntDomainVar.BITSET,
                min, max);
        intVars.add(v);
        intDecisionVars.add(v);
        return v;
    }

    public IntDomainVar createEnumIntVar(String name, int[] sortedValues) {
        IntDomainVar v = new IntDomainVarImpl(this, name, IntDomainVar.BITSET,
                sortedValues);
        intVars.add(v);
        intDecisionVars.add(v);
        return v;
    }

    public IntDomainVar createIntVarAddCste(String name, IntDomainVar variable, int constant) {
        IntDomainVar v = new IntDomainVarAddCste(this, name, variable, constant);
        intVars.add(v);
        intDecisionVars.add(v);
        return v;
    }

    public IntDomainVar createIntVarTimeCste(String name, IntDomainVar variable, int constant) {
        IntDomainVar v = new IntDomainVarTimePosCste(this, name, variable, constant);
        intVars.add(v);
        intDecisionVars.add(v);
        return v;
    }

    public IntDomainVar createBinTreeIntVar(String name, int min, int max) {
        IntDomainVar v = new IntDomainVarImpl(this, name,
                IntDomainVar.BINARYTREE, min, max);
        intVars.add(v);
        intDecisionVars.add(v);
        return v;
    }

    public IntDomainVar createBinTreeIntVar(String name, int[] sortedValues) {
        IntDomainVar v = new IntDomainVarImpl(this, name,
                IntDomainVar.BINARYTREE, sortedValues);
        intVars.add(v);
        intDecisionVars.add(v);
        return v;
    }

    public IntDomainVar createListIntVar(String name, int min, int max) {
        IntDomainVar v = new IntDomainVarImpl(this, name,
                IntDomainVar.LINKEDLIST, min, max);
        intVars.add(v);
        intDecisionVars.add(v);
        return v;
    }

    public IntDomainVar createListIntVar(String name, int[] sortedValues) {
        IntDomainVar v = new IntDomainVarImpl(this, name,
                IntDomainVar.LINKEDLIST, sortedValues);
        intVars.add(v);
        intDecisionVars.add(v);
        return v;
    }

    public RealVar createRealVal(String name, double min, double max) {
        RealVarImpl v = new RealVarImpl(this, name, min, max, RealVar.BOUNDS);
        floatVars.add(v);
        floatDecisionVars.add(v);
        return v;
    }

    public RealIntervalConstant createRealIntervalConstant(double a, double b) {
        return new RealIntervalConstant(a, b);
    }

    /**
     * Makes a constant interval from a double d ([d,d]).
     */
    public RealIntervalConstant cst(double d) {
        return createRealIntervalConstant(d, d);
    }

    /**
     * Makes a constant interval between two doubles [a,b].
     */
    public RealIntervalConstant cst(double a, double b) {
        return createRealIntervalConstant(a, b);
    }

    public SetVar createSetVar(String name, int a, int b, IntDomainVar card) {
        SetVar s = new SetVarImpl(this, name, a, b, card);
        setVars.add(s);
        setDecisionVars.add(s);
        post(new SetCard(s, s.getCard(), true, true)); // post |v| = v.getCard()
        return s;
    }

    public SetVar createSetVar(String name, int a, int b, int type) {
        SetVar s = new SetVarImpl(this, name, a, b, null, type);
        setVars.add(s);
        setDecisionVars.add(s);
        intVars.add(s.getCard());
        intDecisionVars.add(s.getCard());
        post(new SetCard(s, s.getCard(), true, true)); // post |v| = v.getCard()
        return s;
    }

    public SetVar createBoundSetVar(String name, int a, int b) {
        return createSetVar(name, a, b, SetVar.BOUNDSET_BOUNDCARD);
    }

    public SetVar createEnumSetVar(String name, int a, int b) {
        return createSetVar(name, a, b, SetVar.BOUNDSET_ENUMCARD);
    }

    @Override
    public TaskVar createTaskVar(String name, IntDomainVar start,
                                 IntDomainVar end, IntDomainVar duration) {
        TaskVar t = new TaskVar(this, getNbTaskVars(), name, start, end, duration);
        taskVars.add(t);
        return t;
    }

    public IntDomainVar createIntegerConstant(String name, int val) {
        if (intconstantVars.containsKey(val)) {
            return intconstantVars.get(val);
        }
        IntDomainVar v = createIntVar(name, IntDomainVar.BOUNDS, val, val);
        intconstantVars.put(val, v);
        return v;
    }

    public RealIntervalConstant createRealConstant(String name, double val) {
        if (realconstantVars.containsKey(val)) {
            return realconstantVars.get(val);
        }
        RealIntervalConstant v = createRealIntervalConstant(val, val);
        realconstantVars.put(val, v);
        return v;
    }

    // TODO: can be optimized (no need to attach propagation events)
    public IntDomainVar makeConstantIntVar(String name, int val) {
        return createIntegerConstant(name, val);
    }

    public IntDomainVar makeConstantIntVar(int val) {
        return createIntegerConstant("", val);
    }

    // ************************************************************************
    // CONSTRAINTS DECLARATION
    // ************************************************************************


    private static SConstraint eq(int cste) {
        return cste == 0 ? TRUE : FALSE;
    }

    private static SConstraint geq(int cste) {
        return cste <= 0 ? TRUE : FALSE;
    }

    private static SConstraint neq(int cste) {
        return cste != 0 ? TRUE : FALSE;
    }

    public SConstraint eq(IntExp x, IntExp y) {
        if (x instanceof IntVar && y instanceof IntVar) {
            return new EqualXYC((IntDomainVar) x, (IntDomainVar) y, 0);
        } else if ((x instanceof IntTerm || x instanceof IntVar)
                && (y instanceof IntTerm || y instanceof IntVar)) {
            return eq(minus(x, y), 0);
        } else if (x == null) {
            return eq(y, 0);
        } else if (y == null) {
            return eq(x, 0);
        } else {
            throw new SolverException("IntExp not a good exp");
        }
    }

    /**
     * always succeeds to build the constraint
     */
    protected static SConstraint eq(int c0, IntDomainVar v0, int cste) {
        if (c0 == 0) {
            return eq(cste);
        } else if (cste % c0 == 0) {
            return new EqualXC(v0, cste / c0);
        } else {
            return FALSE;
        }
    }

    /**
     * could fail to build a binary constraint and give the hand to IntLinComb
     */
    protected static SConstraint eq(int c0, IntDomainVar v0, int c1, IntDomainVar v1, int cste) {
        assert (c0 != 0 && c1 != 0);
        if (c0 == -c1 && cste % c0 == 0) {
            return new EqualXYC(v0, v1, cste / c0);
        } else if (c0 == c1) {
            return cste % c0 == 0 ?
                    new EqualXY_C(v0, v1, cste / c0) : FALSE;
        }
        return null;
    }


    public SConstraint eq(IntExp x, int c) {
        if (x instanceof IntTerm) {
            IntTerm t = (IntTerm) x;
            int cste = c - t.getConstant();
            if (t.isConstant()) {
                return eq(cste);
            } else if (t.isUnary()) {
                return eq(t.getCoefficient(0), t.getIntDVar(0), cste);
            } else if (t.isBinary()) {
                SConstraint cstr = eq(t.getCoefficient(0), t.getIntDVar(0), t.getCoefficient(1), t.getIntDVar(1), cste);
                if (cstr != null) {
                    return cstr;
                }
            }
            return IntLinCombFactory.makeIntLinComb(t, -cste, IntLinComb.EQ, this);
        } else if (x instanceof IntDomainVar) {
            return new EqualXC((IntDomainVar) x, c);
        } else if (x == null) {
            return eq(c);
        } else {
            throw new SolverException("IntExp " + x + ":not a term, not a var");
        }
    }

    public SConstraint eq(int c, IntExp x) {
        return eq(x, c);
    }

    public SConstraint eq(RealVar r, IntDomainVar i) {
        return new MixedEqXY(r, i);
    }

    public SConstraint eqCard(SetVar s, IntDomainVar i) {
        // return new SetCard(s, i, true, true);
        return eq(s.getCard(), i);
    }

    public SConstraint eqCard(SetVar s, int i) {
        // IntDomainVar var = makeConstantIntVar("cste" + i, i);
        // return new SetCard(s, var, true, true);
        return eq(s.getCard(), i);
    }

    public SConstraint geq(IntExp x, IntExp y) {
        if (x instanceof IntVar && y instanceof IntVar) {
            return new GreaterOrEqualXYC((IntDomainVar) x, (IntDomainVar) y, 0);
        } else if ((x instanceof IntTerm || x instanceof IntVar)
                && (y instanceof IntTerm || y instanceof IntVar)) {
            return geq(minus(x, y), 0);
        } else if (y == null) {
            return geq(x, 0);
        } else if (x == null) {
            return geq(0, y);
        } else {
            throw new SolverException("IntExp not a good exp");
        }
    }

    /**
     * could fail to build a binary constraint and give the hand to IntLinComb
     */
    protected static SConstraint geq(int c0, IntDomainVar v0, int c1, IntDomainVar v1, int cste) {
        if (c0 == -c1 && cste % c0 == 0) {
            if (c0 > 0) {
                return new GreaterOrEqualXYC(v0, v1, cste / c0);
            }
            assert (c0 < 0);
            return new GreaterOrEqualXYC(v1, v0, cste / c1);
        } else if (c0 == c1) {
            if (c0 > 0) {
                return new GreaterOrEqualXY_C(v0, v1, MathUtils.divCeil(cste, c0));
            }
            assert (c0 < 0);
            return new LessOrEqualXY_C(v0, v1, MathUtils.divFloor(cste, c0));
        }
        return null;
    }

    /**
     * always succeeds to build the constraint
     */
    protected static SConstraint geq(int c0, IntDomainVar v0, int cste) {
        if (c0 > 0) {
            return new GreaterOrEqualXC(v0, MathUtils.divCeil(cste, c0));
        } else if (c0 < 0) {
            return new LessOrEqualXC(v0, MathUtils.divFloor(cste, c0));
        } else {
            assert (c0 == 0);
            return geq(cste);
        }
    }

    /**
     * Creates a constraint by stating that a term is greater or equal than a
     * constant
     *
     * @param x the expression
     * @param c the constant
     * @return the linear inequality constraint
     */
    public SConstraint geq(IntExp x, int c) {
        if (x instanceof IntTerm) {
            IntTerm t = (IntTerm) x;
            int cste = c - t.getConstant();
            if (t.isConstant()) {
                return geq(cste);
            } else if (t.isUnary()) {
                return geq(t.getCoefficient(0), t.getIntDVar(0), cste);
            } else if (t.isBinary()) {
                SConstraint cstr = geq(t.getCoefficient(0), t.getIntDVar(0), t.getCoefficient(1), t.getIntDVar(1), cste);
                if (cstr != null) {
                    return cstr;
                }
            }
            return IntLinCombFactory.makeIntLinComb(t, -cste, IntLinComb.GEQ, this);
        } else if (x instanceof IntDomainVar) {
            return new GreaterOrEqualXC((IntDomainVar) x, c);
        } else if (x == null) {
            return geq(c);
        } else {
            throw new SolverException("IntExp not a term, not a var");
        }
    }

    public SConstraint geq(int c, IntExp x) {
        if (x instanceof IntTerm) {
            return geq(IntTerm.opposite((IntTerm) x), -c);
        } else if (x instanceof IntVar) {
            return new LessOrEqualXC((IntDomainVar) x, c);
        } else if (x == null) {
            return geq(c);
        } else {
            throw new SolverException("IntExp not a term, not a var");
        }
    }

    public SConstraint geqCard(SetVar sv, IntDomainVar v) {
        // return new SetCard(sv, v, false, true);
        return geq(sv.getCard(), v);
    }

    public SConstraint geqCard(SetVar sv, int v) {
        // IntDomainVar var = makeConstantIntVar("cste" + sv, v);
        // return new SetCard(sv, var, false, true);
        return geq(sv.getCard(), v);
    }

    public SConstraint gt(IntExp x, IntExp y) {
        return geq(minus(x, y), 1);
    }

    public SConstraint gt(IntExp x, int c) {
        return geq(x, c + 1);
    }

    public SConstraint gt(int c, IntExp x) {
        return geq(c - 1, x);
    }

    public SConstraint leq(IntExp v1, IntExp v2) {
        return geq(v2, v1);
    }

    public SConstraint leq(IntExp v1, int v2) {
        return geq(v2, v1);
    }

    public SConstraint leq(int v1, IntExp v2) {
        return geq(v2, v1);
    }

    public SConstraint leqCard(SetVar sv, IntDomainVar i) {
        // return new SetCard(sv, i, true, false);
        return leq(sv.getCard(), i);
    }

    public SConstraint leqCard(SetVar sv, int i) {
        // IntDomainVar var = makeConstantIntVar("cste" + sv, i);
        // return new SetCard(sv, var, true, false);
        return leq(sv.getCard(), i);
    }

    public SConstraint lt(IntExp v1, IntExp v2) {
        return gt(v2, v1);
    }

    public SConstraint lt(IntExp v1, int v2) {
        return gt(v2, v1);
    }

    public SConstraint lt(int v1, IntExp v2) {
        return gt(v2, v1);
    }


    /**
     * Subtracting two terms one from another
     *
     * @param v1 first term
     * @param v2 second term
     * @return the term (a fresh one)
     */
    public static IntExp minus(IntExp v1, IntExp v2) {
        if (v1 == IntTerm.ZERO) {
            return mult(-1, v2);
        }
        if (v2 == IntTerm.ZERO) {
            return v1;
        }

        if (v1 instanceof IntTerm) {
            IntTerm t1 = (IntTerm) v1;
            if (v2 instanceof IntTerm) {
                return IntTerm.minus(t1, (IntTerm) v2);
            } else if (v2 instanceof IntVar) {
                return IntTerm.plus(t1, -1, (IntVar) v2, false);
            } else {
                throw new SolverException("IntExp not a term, not a var");
            }
        } else if (v1 instanceof IntVar) {
            if (v2 instanceof IntTerm) {
                return IntTerm.minus(1, (IntVar) v1, (IntTerm) v2);
            } else if (v2 instanceof IntVar) {
                IntTerm t = new IntTerm(2);
                t.setCoefficient(0, 1);
                t.setCoefficient(1, -1);
                t.setVariable(0, (IntVar) v1);
                t.setVariable(1, (IntVar) v2);
                t.setConstant(0);
                return t;
            } else {
                throw new SolverException("IntExp not a term, not a var");
            }
        } else {
            throw new SolverException("IntExp not a term, not a var");
        }
    }

    public static IntExp minus(IntExp t, int c) {
        if (t == IntTerm.ZERO) {
            IntTerm t2 = new IntTerm(0);
            t2.setConstant(-c);
            return t2;
        } else if (t instanceof IntTerm) {
            IntTerm t2 = new IntTerm((IntTerm) t);
            t2.setConstant(((IntTerm) t).getConstant() - c);
            return t2;
        } else if (t instanceof IntVar) {
            IntTerm t2 = new IntTerm(1);
            t2.setCoefficient(0, 1);
            t2.setVariable(0, (IntVar) t);
            t2.setConstant(-c);
            return t2;
        } else {
            throw new SolverException("IntExp not a term, not a var");
        }
    }

    public static IntExp minus(int c, IntExp t) {
        if (t instanceof IntTerm) {
            IntTerm t1 = (IntTerm) t;
            int n = t1.getSize();
            IntTerm t2 = new IntTerm(n);
            for (int i = 0; i < n; i++) {
                t2.setCoefficient(i, -t1.getCoefficient(i));
                t2.setVariable(i, t1.getVariable(i));
            }
            t2.setConstant(c - t1.getConstant());
            return t2;
        } else if (t instanceof IntVar) {
            IntTerm t2 = new IntTerm(1);
            t2.setCoefficient(0, -1);
            t2.setVariable(0, (IntVar) t);
            t2.setConstant(c);
            return t2;
        } else {
            throw new SolverException("IntExp not a term, not a var");
        }
    }

    /**
     * Adding two terms one to another
     *
     * @param v1 first term
     * @param v2 second term
     * @return the term (a fresh one)
     */
    public IntExp plus(IntExp v1, IntExp v2) {
        if (v1 == IntTerm.ZERO) {
            return v2;
        }
        if (v2 == IntTerm.ZERO) {
            return v1;
        }
        if (v1 instanceof IntTerm) {
            IntTerm t1 = (IntTerm) v1;
            if (v2 instanceof IntTerm) {
                return IntTerm.plus(t1, (IntTerm) v2);
            } else if (v2 instanceof IntVar) {
                return IntTerm.plus(t1, 1, (IntVar) v2, false);
            } else {
                throw new SolverException("IntExp not a term, not a var");
            }
        } else if (v1 instanceof IntVar) {
            if (v2 instanceof IntTerm) {
                return IntTerm.plus((IntTerm) v2, 1, (IntVar) v1, true);
            } else if (v2 instanceof IntVar) {
                IntTerm t = new IntTerm(2);
                t.setCoefficient(0, 1);
                t.setCoefficient(1, 1);
                t.setVariable(0, (IntVar) v1);
                t.setVariable(1, (IntVar) v2);
                t.setConstant(0);
                return t;
            } else {
                throw new SolverException("IntExp not a term, not a var");
            }
        } else {
            throw new SolverException("IntExp not a term, not a var");
        }
    }

    public IntExp plus(IntExp t, int c) {
        if (t == IntTerm.ZERO) {
            IntTerm t2 = new IntTerm(0);
            t2.setConstant(c);
            return t2;
        } else if (t instanceof IntTerm) {
            IntTerm t2 = new IntTerm((IntTerm) t);
            t2.setConstant(((IntTerm) t).getConstant() + c);
            return t2;
        } else if (t instanceof IntVar) {
            IntTerm t2 = new IntTerm(1);
            t2.setCoefficient(0, 1);
            t2.setVariable(0, (IntVar) t);
            t2.setConstant(c);
            return t2;
        } else {
            throw new SolverException("IntExp not a term, not a var");
        }
    }

    public IntExp plus(int c, IntExp t1) {
        return plus(t1, c);
    }

    /**
     * Utility method for constructing a term from two lists of variables, list
     * of coeffcicients and constants
     *
     * @param coeffs1 coefficients from the first term
     * @param vars1   variables from the first term
     * @param cste1   constant from the fisrt term
     * @param coeffs2 coefficients from the second term
     * @param vars2   variables from the second term
     * @param cste2   constant from the second term
     * @return the term (a fresh one)
     */
    protected static IntExp plus(int[] coeffs1, IntVar[] vars1, int cste1,
                                 int[] coeffs2, IntVar[] vars2, int cste2) {
        int n1 = vars1.length;
        int n2 = vars2.length;
        IntTerm t = new IntTerm(n1 + n2);
        for (int i = 0; i < n1; i++) {
            t.setVariable(i, vars1[i]);
            t.setCoefficient(i, coeffs1[i]);
        }
        for (int i = 0; i < n2; i++) {
            t.setVariable(n1 + i, vars2[i]);
            t.setCoefficient(n1 + i, coeffs2[i]);
        }
        t.setConstant(cste1 + cste2);
        return t;
    }

    /**
     * Creates a simple linear term from one coefficient and one variable
     *
     * @param a the coefficient
     * @param x the variable
     * @return the term
     */
    public static IntExp mult(int a, IntExp x) {
        if (a != 0 && x != IntTerm.ZERO) {
            IntTerm t = new IntTerm(1);
            t.setCoefficient(0, a);
            t.setVariable(0, (IntVar) x);
            return t;
        } else {
            return IntTerm.ZERO;
        }
    }


    /**
     * could fail to build a binary constraint and give the hand to IntLinComb
     */
    protected static SConstraint neq(int c0, IntDomainVar v0, int c1, IntDomainVar v1, int cste) {
        assert (c0 != 0 && c1 != 0);
        if (c0 == -c1 && cste % c0 == 0) {
            return new NotEqualXYC(v0, v1, cste / c0);
        } else if (c0 == c1) {
            return cste % c0 == 0 ? new NotEqualXY_C(v0, v1, cste / c0) : TRUE;
        }
        return null;
    }

    /**
     * always succeeds to build the constraint
     */
    protected static SConstraint neq(int c0, IntDomainVar v0, int cste) {
        if (c0 == 0) {
            return neq(cste);
        } else if (cste % c0 == 0) {
            return new NotEqualXC(v0, cste / c0);
        } else {
            return TRUE;
        }
    }

    /**
     * Creates a constraint by stating that a term is not equal than a constant
     *
     * @param x the expression
     * @param c the constant
     * @return the linear disequality constraint
     */
    public SConstraint neq(IntExp x, int c) {
        if (x instanceof IntTerm) {
            IntTerm t = (IntTerm) x;
            int cste = c - t.getConstant();
            if (t.isConstant()) {
                return neq(cste);
            } else if (t.isUnary()) {
                return neq(t.getCoefficient(0), t.getIntDVar(0), cste);
            } else if (t.isBinary()) {
                SConstraint cstr = neq(t.getCoefficient(0), t.getIntDVar(0), t.getCoefficient(1), t.getIntDVar(1), cste);
                if (cstr != null) {
                    return cstr;
                }
            }
            return IntLinCombFactory.makeIntLinComb(t, -cste, IntLinComb.NEQ, this);
        } else if (x instanceof IntVar) {
            return new NotEqualXC((IntDomainVar) x, c);
        } else if (x == null) {
            return neq(c);
        } else {
            throw new SolverException("IntExp not a term, not a var");
        }
    }

    public SConstraint neq(int c, IntExp x) {
        return neq(x, c);
    }

    public SConstraint neq(IntExp x, IntExp y) {
        if (x instanceof IntTerm) {
            return neq(minus(x, y), 0);
        } else if (x instanceof IntVar) {
            if (y instanceof IntTerm) {
                return neq(minus(x, y), 0);
            } else if (y instanceof IntVar) {
                return new NotEqualXYC((IntDomainVar) x, (IntDomainVar) y, 0);
            } else if (y == null) {
                return neq(x, 0);
            } else {
                throw new SolverException("IntExp not a term, not a var");
            }
        } else if (x == null) {
            return neq(0, y);
        } else {
            throw new SolverException("IntExp not a term, not a var");
        }
    }

    public static SConstraint eq(SetVar s1, SetVar s2) {
        return new SetEq(s1, s2);
    }

    public static SConstraint neq(SetVar s1, SetVar s2) {
        return new SetNotEq(s1, s2);
    }

    public SConstraint occurence(IntDomainVar[] vars, IntDomainVar occ,
                                 int value) {
        IntDomainVar[] tmpvars = new IntDomainVar[vars.length + 1];
        System.arraycopy(vars, 0, tmpvars, 0, vars.length);
        tmpvars[tmpvars.length - 1] = occ;
        return new Occurrence(tmpvars, value, true, true, environment);
    }


    public SConstraint preceding(TaskVar t1, int k1, TaskVar t2) {
        //post precedence between starting times if possible
        return leq(
                (t1.duration().isInstantiated() ? plus(t1.start(), t1.duration().getVal() + k1) : plus(t1.end(), k1)),
                t2.start()
        );
    }

    public SConstraint preceding(IntDomainVar direction, TaskVar t1, TaskVar t2) {
        return preceding(direction, t1, 0, t2, 0);
    }

    public SConstraint preceding(IntDomainVar direction, TaskVar t1, int k1, TaskVar t2, int k2) {
        if (direction == null) {
            direction = VariableUtils.createDirVar(this, t1, t2);
        } else if (!direction.hasBooleanDomain()) {
            throw new SolverException("The direction variable " + direction.pretty() + "is not a boolean variable for the precedence (" + t1 + ',' + t2 + ')');
        }
        if (direction.isInstantiatedTo(1)) {
            //forward precedence
            return preceding(t1, k1, t2);
        } else if (direction.isInstantiatedTo(0)) {
            //backward precedence
            return preceding(t2, k2, t1);
        } else {
            //disjunction
            if (t1.duration().isInstantiated() && t2.duration().isInstantiated()) {
                //both tasks have fixed duration
                return new PrecedenceDisjoint(
                        t1, t1.duration().getVal() + k1,
                        t2, t2.duration().getVal() + k2,
                        direction
                );
            } else {
                //at least one task has a variable duration
                if (k1 != 0 || k2 != 0) {
                    return new PrecedenceVSDisjoint(direction, t1, k1, t2, k2);
                } else {
                    return new PrecedenceVDisjoint(direction, t1, t2);
                }
            }
        }
    }

    // ------------------------ constraints over reals

    /**
     * Makes an equation from an expression and a constant interval. It is used
     * by all methods building constraints. This is useful for subclassing this
     * modeller for another kind of model (like PaLM).
     *
     * @param exp The expression
     * @param cst The interval this expression should be in
     * @return the equation constraint
     */
    public SConstraint makeEquation(RealExp exp, RealIntervalConstant cst) {
        // Collect the variables
        Set<RealVar> collectedVars = new HashSet<RealVar>(16);
        exp.collectVars(collectedVars);
        RealVar[] tmpVars = new RealVar[0];
        tmpVars = collectedVars.toArray(tmpVars);
        return createEquation(tmpVars, exp, cst);
    }

    /**
     * Equality constraint.
     *
     * @param exp1 the first expression
     * @param exp2 the second expression
     * @return the constraint enforcing exp1=exp2
     */
    public SConstraint eq(RealExp exp1, RealExp exp2) {
        if (exp1 instanceof RealIntervalConstant) {
            return makeEquation(exp2, (RealIntervalConstant) exp1);
        } else if (exp2 instanceof RealIntervalConstant) {
            return makeEquation(exp1, (RealIntervalConstant) exp2);
        } else {
            return makeEquation(minus(exp1, exp2), cst(0.0));
        }
    }

    public SConstraint eq(RealExp exp, double cst) {
        return makeEquation(exp, cst(cst));
    }

    public SConstraint eq(double cst, RealExp exp) {
        return makeEquation(exp, cst(cst));
    }

    /**
     * Inferority constraint.
     *
     * @param exp1 the fisrt expression
     * @param exp2 the second expression
     * @return the constraint enforcing exp1<=exp2
     */
    public SConstraint leq(RealExp exp1, RealExp exp2) {
        if (exp1 instanceof RealIntervalConstant) {
            return makeEquation(exp2, cst(exp1.getInf(),
                    Double.POSITIVE_INFINITY));
        } else if (exp2 instanceof RealIntervalConstant) {
            return makeEquation(exp1, cst(Double.NEGATIVE_INFINITY, exp2
                    .getSup()));
        } else {
            return makeEquation(minus(exp1, exp2), cst(
                    Double.NEGATIVE_INFINITY, 0.0));
        }
    }

    public SConstraint leq(RealExp exp, double cst) {
        return makeEquation(exp, cst(Double.NEGATIVE_INFINITY, cst));
    }

    public SConstraint leq(double cst, RealExp exp) {
        return makeEquation(exp, cst(cst, Double.POSITIVE_INFINITY));
    }

    /**
     * Superiority constraint.
     *
     * @param exp1 the fisrt expression
     * @param exp2 the second expression
     * @return the constraint enforcing exp1>=exp2
     */
    public SConstraint geq(RealExp exp1, RealExp exp2) {
        return leq(exp2, exp1);
    }

    public SConstraint geq(RealExp exp, double cst) {
        return leq(cst, exp);
    }

    public SConstraint geq(double cst, RealExp exp) {
        return leq(exp, cst);
    }

    /**
     * Addition of two expressions.
     *
     * @param exp1 the first expression
     * @param exp2 the second expression
     * @return the sum of exp1 and exp2 (exp1+exp2)
     */
    public RealExp plus(RealExp exp1, RealExp exp2) {
        return createRealPlus(exp1, exp2);
    }

    /**
     * Substraction of two expressions.
     *
     * @param exp1 the first expression
     * @param exp2 the second expression
     * @return the difference of exp1 and exp2 (exp1-exp2)
     */
    public RealExp minus(RealExp exp1, RealExp exp2) {
        return createRealMinus(exp1, exp2);
    }

    /**
     * Multiplication of two expressions.
     *
     * @param exp1 the first expression
     * @param exp2 the second expression
     * @return the product of exp1 and exp2 (exp1*exp2)
     */
    public RealExp mult(RealExp exp1, RealExp exp2) {
        return createRealMult(exp1, exp2);
    }

    /**
     * Power of an expression.
     *
     * @param exp   the expression to x
     * @param power the second expression
     * @return the difference of exp1 and exp2 (exp1-exp2)
     */
    public RealExp power(RealExp exp, int power) {
        return createRealIntegerPower(exp, power);
    }

    /**
     * Cosinus of an expression.
     */
    public RealExp cos(RealExp exp) {
        return createRealCos(exp);
    }

    /**
     * Sinus of an expression.
     */
    public RealExp sin(RealExp exp) {
        return createRealSin(exp);
    }

    /**
     * Arounds a double d to <code>[d - epsilon, d + epilon]</code>.
     */
    public RealIntervalConstant around(double d) {
        return cst(RealMath.prevFloat(d), RealMath.nextFloat(d));
    }

    protected RealExp createRealSin(RealExp exp) {
        return new RealSin(this, exp);
    }

    protected RealExp createRealCos(RealExp exp) {
        return new RealCos(this, exp);
    }

    protected RealExp createRealPlus(RealExp exp1, RealExp exp2) {
        return new RealPlus(this, exp1, exp2);
    }

    protected RealExp createRealMinus(RealExp exp1, RealExp exp2) {
        return new RealMinus(this, exp1, exp2);
    }

    protected RealExp createRealMult(RealExp exp1, RealExp exp2) {
        return new RealMult(this, exp1, exp2);
    }

    protected RealExp createRealIntegerPower(RealExp exp, int power) {
        return new RealIntegerPower(this, exp, power);
    }

    protected SConstraint createEquation(RealVar[] tmpVars, RealExp exp,
                                         RealIntervalConstant cst) {
        return new Equation(this, tmpVars, exp, cst);
    }

    /**
     * Building a term from a scalar product of coefficients and variables
     *
     * @param lc the array of coefficients
     * @param lv the array of variables
     * @return the term
     */
    public IntExp scalar(int[] lc, IntDomainVar[] lv) {
        int nbNonNullCoeffs = 0;
        for (int i = 0; i < lc.length; i++) {
            if (lc[i] != 0) {
                nbNonNullCoeffs++;
            }
        }

        if (nbNonNullCoeffs == 0) {
            return IntTerm.ZERO;
        } else if (nbNonNullCoeffs == lc.length) {
            return new IntTerm(lc, lv);
        } else {
            IntTerm res = new IntTerm(nbNonNullCoeffs);
            int idx = 0;
            for (int i = 0; i < lc.length; i++) {
                if (lc[i] != 0) {
                    res.setCoefficient(idx, lc[i]);
                    res.setVariable(idx, lv[i]);
                    idx++;
                }
            }
            return res;
        }
    }

    /**
     * Building a term from a scalar product of coefficients and variables
     *
     * @param lv the array of variables
     * @param lc the array of coefficients
     * @return the term
     */
    public IntExp scalar(IntDomainVar[] lv, int[] lc) {
        return scalar(lc, lv);
    }

    /**
     * Building a term from a sum of integer variables
     *
     * @param lv the array of integer variables
     * @return the term
     */
    public static IntExp sum(IntVar... lv) {
        return new IntTerm(lv);
    }

    /**
     * Building a term from a sum of integer expressions
     *
     * @param lv the array of integer expressions
     * @return the term
     */
    public IntExp sum(IntExp... lv) {
        int n = lv.length;
        IntTerm t = new IntTerm(n);
        for (int i = 0; i < n; i++) {
            t.setCoefficient(i, 1);
            if (lv[i] instanceof IntVar) {
                t.setVariable(i, (IntVar) lv[i]);
            } else {
                throw new SolverException("unexpected kind of IntExp");
            }
        }
        return t;
    }

    // ------------------------ Boolean connectors

    public SConstraint feasiblePairAC(IntDomainVar v1, IntDomainVar v2,
                                      boolean[][] mat, int ac) {
        return makePairAC(v1, v2, mat, true, ac);
    }

    public SConstraint feasiblePairAC(IntDomainVar v1, IntDomainVar v2,
                                      List<int[]> mat, int ac) {
        return makePairAC(v1, v2, mat, true, ac);
    }

    public SConstraint infeasiblePairAC(IntDomainVar v1, IntDomainVar v2,
                                        boolean[][] mat, int ac) {
        return makePairAC(v1, v2, mat, false, ac);
    }

    public SConstraint infeasiblePairAC(IntDomainVar v1, IntDomainVar v2,
                                        List<int[]> mat, int ac) {
        return makePairAC(v1, v2, mat, false, ac);
    }

    /**
     * Create a binary relation that represent the list of compatible or
     * incompatible pairs of values (depending on feas) given in argument tp be
     * stated on any pair of variables (x,y) whose domain is included in the min
     * max given in argument. So such that : min[0] <= x.getInf(), max[0] >=
     * x.getSup(), min[1] <= x.getSup(), min[1] >= y.getInf(), max[1] >=
     * y.getSup() for any pairs of variable x,y where an ac algorithm will be
     * used with this relation. This is mandatory in the api to be able to
     * compute the opposite of the relation if needed so the min[i]/max[i] can
     * be smaller/bigger than min_{j \in pairs} pairs.get(j)[i] or max_{j \in
     * pairs} pairs.get(j)[i]
     *
     * @param min
     * @param max
     * @param mat    the list of tuples defined as int[] of size 2
     * @param feas   specify if the relation is defined in feasibility or not i.e.
     *               if the tuples corresponds to feasible or infeasible tuples
     * @param bitset specify if the relation is intended to be used in ac3rm
     *               enhanced with bitwise operations
     * @return
     */
    public BinRelation makeBinRelation(int[] min, int[] max, List<int[]> mat,
                                       boolean feas, boolean bitset) {
        int n1 = max[0] - min[0] + 1;
        int n2 = max[1] - min[1] + 1;
        ExtensionalBinRelation relation;
        if (bitset) {
            relation = new CouplesBitSetTable(feas, min[0], min[1], n1, n2);
        } else {
            relation = new CouplesTable(feas, min[0], min[1], n1, n2);
        }
        for (int[] couple : mat) {
            if (couple.length != 2) {
                throw new SolverException("Wrong dimension : " + couple.length
                        + " for a couple");
            }
            relation.setCouple(couple[0], couple[1]);
        }
        return relation;
    }

    /**
     * Create a binary relation that represent the list of compatible or
     * incompatible pairs of values (depending on feas) given in argument tp be
     * stated on any pair of variables (x,y) whose domain is included in the min
     * max given in argument. So such that : min[0] <= x.getInf(), max[0] >=
     * x.getSup(), min[1] <= x.getSup(), min[1] >= y.getInf(), max[1] >=
     * y.getSup() for any pairs of variable x,y where an ac algorithm will be
     * used with this relation. This is mandatory in the api to be able to
     * compute the opposite of the relation if needed so the min[i]/max[i] can
     * be smaller/bigger than min_{j \in pairs} pairs.get(j)[i] or max_{j \in
     * pairs} pairs.get(j)[i]
     *
     * @param min
     * @param max
     * @param mat  the list of tuples defined as int[] of size 2
     * @param feas specify if the relation is defined in feasibility or not i.e.
     *             if the tuples corresponds to feasible or infeasible tuples
     * @return
     */
    public BinRelation makeBinRelation(int[] min, int[] max, List<int[]> mat,
                                       boolean feas) {
        return makeBinRelation(min, max, mat, feas, false);
    }

    /**
     * Create a binary relation from the given matrix of consistency
     *
     * @param v1
     * @param v2
     * @param mat  the consistency matrix
     * @param feas specify if the relation is defined in feasibility or not
     * @return
     */
    public static BinRelation makeBinRelation(IntDomainVar v1, IntDomainVar v2,
                                              boolean[][] mat, boolean feas, boolean bitset) {
        int n1 = v1.getSup() - v1.getInf() + 1;
        int n2 = v2.getSup() - v2.getInf() + 1;
        if (n1 == mat.length && n2 == mat[0].length) {
            ExtensionalBinRelation relation;
            relation = bitset ? new CouplesBitSetTable(feas, v1.getInf(), v2
                    .getInf(), n1, n2) : new CouplesTable(feas, v1.getInf(), v2
                    .getInf(), n1, n2);

            for (int i = 0; i < n1; i++) {
                for (int j = 0; j < n2; j++) {
                    if (mat[i][j]) {
                        relation.setCouple(i + v1.getInf(), j + v2.getInf());
                    }
                }
            }
            return relation;
        } else {
            throw new SolverException(
                    "Wrong dimension for the matrix of consistency : "
                            + mat.length + " X " + mat[0].length
                            + " instead of " + n1 + 'X' + n2);
        }
    }

    public static BinRelation makeBinRelation(IntDomainVar v1, IntDomainVar v2,
                                              boolean[][] mat, boolean feas) {
        return makeBinRelation(v1, v2, mat, feas, false);
    }

    private SConstraint makePairAC(IntDomainVar x, IntDomainVar y,
                                   List<int[]> mat, boolean feas, int ac) {
        int[] min = new int[]{x.getInf(), y.getInf()};
        int[] max = new int[]{x.getSup(), y.getSup()};
        BinRelation relation = makeBinRelation(min, max, mat, feas,
                (ac == 322));
        return relationPairAC(x, y, relation, ac);
    }

    /**
     * Create a constraint to enforce GAC on a list of feasible or infeasible
     * tuples
     *
     * @param vs
     * @param tuples the list of tuples
     * @param feas   specify if the tuples are feasible or infeasible tuples
     * @return
     */
    public SConstraint makeTupleAC(IntDomainVar[] vs, List<int[]> tuples,
                                   boolean feas) {
        int[] min = new int[vs.length];
        int[] max = new int[vs.length];
        for (int i = 0; i < vs.length; i++) {
            min[i] = vs[i].getInf();
            max[i] = vs[i].getSup();
        }
        LargeRelation relation = makeLargeRelation(min, max, tuples, feas);
        return relationTupleAC(vs, relation);
    }

    private SConstraint makePairAC(IntDomainVar v1, IntDomainVar v2,
                                   boolean[][] mat, boolean feas, int ac) {
        BinRelation relation = makeBinRelation(v1, v2, mat, feas,
                ac == 322);
        return relationPairAC(v1, v2, relation, ac);
    }

    /**
     * Create a constraint enforcing Arc Consistency on a given a given list of
     * feasible tuples. Default algorithm is GAC3rm
     *
     * @param vars
     * @param tuples : a list of int[] corresponding to feasible tuples
     */
    public SConstraint feasibleTupleAC(IntDomainVar[] vars, List<int[]> tuples) {
        return feasibleTupleAC(vars, tuples, 32);
    }

    /**
     * Create a constraint enforcing Arc Consistency on a given a given list of
     * infeasible tuples. Default algorithm is GAC3rm
     *
     * @param vars
     * @param tuples : a list of int[] corresponding to infeasible tuples
     */
    public SConstraint infeasibleTupleAC(IntDomainVar[] vars, List<int[]> tuples) {
        return infeasibleTupleAC(vars, tuples, 32);
    }

    /**
     * Create a constraint enforcing Arc Consistency on a given a given list of
     * feasible tuples
     *
     * @param vars
     * @param tuples : a list of int[] corresponding to feasible tuples
     */
    public SConstraint feasibleTupleAC(IntDomainVar[] vars, List<int[]> tuples,
                                       int ac) {
        LargeRelation relation = makeRelation(vars, tuples, true);
        if (ac == 2001) {
            return new GAC2001PositiveLargeConstraint(environment, vars,
                    (IterTuplesTable) relation);
        } else if (ac == 32) {
            return new GAC3rmPositiveLargeConstraint(vars,
                    (IterTuplesTable) relation);
        } else {
            throw new SolverException(
                    "unknown ac algorithm, must be 32 or 2001");
        }
    }

    /**
     * Create a constraint enforcing Arc Consistency on a given a given list of
     * infeasible tuples
     *
     * @param vars
     * @param tuples : a list of int[] corresponding to infeasible tuples
     */
    public SConstraint infeasibleTupleAC(IntDomainVar[] vars,
                                         List<int[]> tuples, int ac) {
        LargeRelation relation = makeRelation(vars, tuples, false);
        if (ac == 2001) {
            return new GAC2001PositiveLargeConstraint(environment, vars,
                    (IterTuplesTable) relation);
        } else if (ac == 32) {
            return new GAC3rmPositiveLargeConstraint(vars,
                    (IterTuplesTable) relation);
        } else {
            throw new SolverException(
                    "unknown ac algorithm, must be 32 or 2001");
        }
    }

    public SConstraint relationPairAC(IntDomainVar v1, IntDomainVar v2,
                                      BinRelation binR, int ac) {
        if (ac == 3) {
            return new AC3BinSConstraint(v1, v2, binR);
        } else if (ac == 4) {
            throw new SolverException("ac4 not implemented in choco2");
        } else if (ac == 2001) {
            return new AC2001BinSConstraint(v1, v2, binR, environment);
        } else if (ac == 32) {
            return new AC3rmBinSConstraint(v1, v2, binR);
        } else if (ac == 322) {
            return new AC3rmBitBinSConstraint(v1, v2, (CouplesBitSetTable) binR);
            // TODO: add the bitset implementation
        } else {
            throw new UnsupportedOperationException("Ac " + ac
                    + " algorithm not yet implemented");
        }
    }

    /**
     * @deprecated use makeLargeRelation instead
     */
    @Deprecated
    public LargeRelation makeRelation(IntVar[] vs, List<int[]> tuples,
                                      boolean feas) {
        int[] min = new int[vs.length];
        int[] max = new int[vs.length];
        for (int i = 0; i < vs.length; i++) {
            min[i] = ((IntDomainVar) vs[i]).getInf();
            max[i] = ((IntDomainVar) vs[i]).getSup();
        }
        return makeLargeRelation(min, max, tuples, feas);
    }

    /**
     * Create a nary relationship that can be used to state a GAC constraint
     * using after the api relationTupleAC(relation). Typically GAC algorithms
     * uses two main schemes to seek the next support : - either by looking in
     * the domain of the variable (here put feas = false to get such a relation)
     * - or in the table itself in which case one need to be able to iterate
     * over the tuples and not only check consistency (here put feas = true to
     * get such a relation)
     *
     * @param min    : min[i] has to be greater or equal the minimum value of any
     *               i-th variable on which this relation will be used
     * @param max    : max[i] has to be greater or equal the maximum value of any
     *               i-th variable on which this relation will be used
     * @param tuples
     * @param feas   specifies if you want an Iterable relation or not
     * @return an nary relation.
     */
    public LargeRelation makeLargeRelation(int[] min, int[] max,
                                           List<int[]> tuples, boolean feas) {
        return makeLargeRelation(min, max, tuples, feas, (feas ? 0 : 1));
    }

    /**
     * Create a nary relationship that can be used to state a GAC constraint
     * using after the api relationTupleAC(relation). Typically GAC algorithms
     * uses two main schemes to seek the next support : - either by looking in
     * the domain of the variable (here put feas = false to get such a relation)
     * - or in the table itself in which case one need to be able to iterate
     * over the tuples and not only check consistency (here put feas = true to
     * get such a relation)
     *
     * @param min    : min[i] has to be greater or equal the minimum value of any
     *               i-th variable on which this relation will be used
     * @param max    : max[i] has to be greater or equal the maximum value of any
     *               i-th variable on which this relation will be used
     * @param tuples
     * @param feas   : specifies if you want an Iterable relation or not
     * @param scheme : specifies the desired scheme allowed tuples (0) or valid
     *               tuples (1) or both (2). The GAC constraint stated on this
     *               relation will then work in the corresponding scheme. Allowed
     *               means that the search for support is made through the lists of
     *               tuples and valid that it is made through the domains of the
     *               variables
     * @return an nary relation.
     */

    public LargeRelation makeLargeRelation(int[] min, int[] max,
                                           List<int[]> tuples, boolean feas, int scheme) {
        int n = min.length;
        int[] offsets = new int[n];
        int[] sizes = new int[n];
        for (int i = 0; i < n; i++) {
            sizes[i] = max[i] - min[i] + 1;
            offsets[i] = min[i];
        }
        LargeRelation relation;
        if (scheme == 0) {
            relation = new IterTuplesTable(tuples, offsets, sizes);
        } else if (scheme == 1) {
            relation = new TuplesTable(feas, offsets, sizes);
            for (final int[] tuple1 : tuples) {
                if (tuple1.length != n) {
                    throw new SolverException("Wrong dimension : "
                            + tuple1.length + " for a tuple (should be " + n
                            + ')');
                }
                ((TuplesTable) relation).setTuple(tuple1);
            }
        } else {
            relation = new TuplesList(tuples);
        }

        return relation;
    }

    public static SConstraint makeTupleFC(IntDomainVar[] vs, List<int[]> tuples,
                                          boolean feas) {
        int n = vs.length;
        int[] offsets = new int[n];
        int[] sizes = new int[n];
        for (int i = 0; i < n; i++) {
            IntDomainVar vi = vs[i];
            sizes[i] = vi.getSup() - vi.getInf() + 1; // vi.getDomainSize();
            offsets[i] = vi.getInf();
        }
        TuplesTable relation = new TuplesTable(feas, offsets, sizes);
        for (final int[] tuple1 : tuples) {
            if (tuple1.length != n) {
                throw new SolverException("Wrong dimension : " + tuple1.length
                        + " for a tuple (should be " + n + ')');
            }
            relation.setTuple(tuple1);
        }
        return createFCLargeConstraint(vs, relation);
    }

    /**
     * Create a constraint enforcing Forward Checking on a given a given list of
     * feasible tuples
     *
     * @param vars
     * @param tuples : a list of int[] corresponding to feasible tuples
     */
    public static SConstraint feasibleTupleFC(IntDomainVar[] vars, TuplesTable tuples) {
        return new CspLargeSConstraint(vars, tuples);
    }

    /**
     * Create a constraint enforcing Forward Checking on a given a given list of
     * infeasible tuples
     *
     * @param vars
     * @param tuples : a list of int[] corresponding to infeasible tuples
     */
    public static SConstraint infeasibleTupleFC(IntDomainVar[] vars, TuplesTable tuples) {
        return new CspLargeSConstraint(vars, tuples);
    }

    /**
     * Create a constraint enforcing Forward Checking on a given a given list of
     * infeasible tuples
     *
     * @param vars
     * @param tuples : a list of int[] corresponding to infeasible tuples
     */
    public static SConstraint infeasTupleFC(IntDomainVar[] vars, List<int[]> tuples) {
        return makeTupleFC(vars, tuples, false);
    }

    /**
     * Create a constraint enforcing Forward Checking on a given a given list of
     * feasible tuples
     *
     * @param vars
     * @param tuples : a list of int[] corresponding to feasible tuples
     */
    public static SConstraint feasTupleFC(IntDomainVar[] vars, List<int[]> tuples) {
        return makeTupleFC(vars, tuples, true);
    }

    /**
     * Create a constraint enforcing Arc Consistency on a given a given list of
     * infeasible tuples
     *
     * @param vars
     * @param tuples : a list of int[] corresponding to infeasible tuples
     */
    public SConstraint infeasTupleAC(IntDomainVar[] vars, List<int[]> tuples) {
        return makeTupleAC(vars, tuples, false);
    }

    /**
     * Create a constraint enforcing Arc Consistency on a given a given list of
     * feasible tuples
     *
     * @param vars
     * @param tuples : a list of int[] corresponding to feasible tuples
     */
    public SConstraint feasTupleAC(IntDomainVar[] vars, List<int[]> tuples) {
        return makeTupleAC(vars, tuples, true);
    }

    /**
     * Create a constraint enforcing Forward Checking on a given consistency
     * relation
     *
     * @param vs
     * @param rela
     */
    public static SConstraint relationTupleFC(IntDomainVar[] vs, LargeRelation rela) {
        return createFCLargeConstraint(vs, rela);
    }

    protected static SConstraint createFCLargeConstraint(IntDomainVar[] vars,
                                                         LargeRelation relation) {
        IntDomainVar[] tmpVars = new IntDomainVar[vars.length];
        System.arraycopy(vars, 0, tmpVars, 0, vars.length);
        return new CspLargeSConstraint(tmpVars, relation);
    }

    /**
     * Create a constraint enforcing Arc Consistency on a given consistency
     * relation. The GAC algorithm depends on the kind of relation : -
     * IterIndexedLargeRelation is used in GAC3rm with allowed tuples -
     * Otherwise a GAC3rm in valid tuples will be used
     *
     * @param vs
     * @param rela
     */
    public SConstraint relationTupleAC(IntDomainVar[] vs, LargeRelation rela) {
        return relationTupleAC(vs, rela, 32);
    }

    /**
     * Create a constraint enforcing Arc Consistency on a given consistency
     * relation. The GAC algorithm depends on the kind of relation and the ac
     * algorithm : - IterIndexedLargeRelation is used in GAC3rm with allowed
     * tuples - Otherwise a GAC3rm in valid tuples will be used
     *
     * @param vs
     * @param rela
     */
    public SConstraint relationTupleAC(IntDomainVar[] vs, LargeRelation rela,
                                       int ac) {
        if (rela instanceof IterLargeRelation) {
            if (ac == 32) {
                return new GAC3rmPositiveLargeConstraint(vs,
                        (IterTuplesTable) rela);
            } else if (ac == 2001) {
                return new GAC2001PositiveLargeConstraint(environment, vs,
                        (IterTuplesTable) rela);
            } else {
                throw new SolverException(
                        "GAC algo unknown, choose between 32 or 2001");
            }
        } else {
            if (ac == 32) {
                return new GAC3rmLargeConstraint(vs, rela);
            } else if (ac == 2001) {
                return new GAC2001LargeSConstraint(vs, rela, environment);
            } else if (ac == 2008) {
                return new GACstrPositiveLargeSConstraint(vs, rela, environment);
            } else {
                throw new SolverException(
                        "GAC algo unknown, choose between 32, 2001, 2008");
            }
        }
    }

    public SConstraint relationPairAC(IntDomainVar v1, IntDomainVar v2,
                                      BinRelation binR) {
        return relationPairAC(v1, v2, binR,
                (binR instanceof CouplesBitSetTable ? 322 : 32));
    }

    public SConstraint infeasPairAC(IntDomainVar v1, IntDomainVar v2,
                                    List<int[]> mat) {
        return makePairAC(v1, v2, mat, false, 322);
    }

    public SConstraint infeasPairAC(IntDomainVar v1, IntDomainVar v2,
                                    List<int[]> mat, int ac) {
        return makePairAC(v1, v2, mat, false, ac);
    }

    public SConstraint feasPairAC(IntDomainVar v1, IntDomainVar v2,
                                  List<int[]> mat) {
        return makePairAC(v1, v2, mat, true, 322);
    }

    public SConstraint feasPairAC(IntDomainVar v1, IntDomainVar v2,
                                  List<int[]> mat, int ac) {
        return makePairAC(v1, v2, mat, true, ac);
    }

    public SConstraint infeasPairAC(IntDomainVar v1, IntDomainVar v2,
                                    boolean[][] mat) {
        return makePairAC(v1, v2, mat, false, 322);
    }

    public SConstraint infeasPairAC(IntDomainVar v1, IntDomainVar v2,
                                    boolean[][] mat, int ac) {
        return makePairAC(v1, v2, mat, false, ac);
    }

    public SConstraint feasPairAC(IntDomainVar v1, IntDomainVar v2,
                                  boolean[][] mat) {
        return makePairAC(v1, v2, mat, true, 322);
    }

    public SConstraint feasPairAC(IntDomainVar v1, IntDomainVar v2,
                                  boolean[][] mat, int ac) {
        return makePairAC(v1, v2, mat, true, ac);
    }

    public SConstraint reifiedIntConstraint(IntDomainVar binVar, SConstraint c) {
        return ReifiedFactory.builder(binVar, c, this);
    }

    public static SConstraint reifiedIntConstraint(IntDomainVar binVar, SConstraint c,
                                                   SConstraint opc, final Solver solver) {
        return ReifiedFactory.builder(binVar, c, opc, solver);
    }

}

