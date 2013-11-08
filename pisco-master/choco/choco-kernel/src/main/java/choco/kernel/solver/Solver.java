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

package choco.kernel.solver;

import choco.IPretty;
import choco.kernel.common.IndexFactory;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.constraints.integer.extension.BinRelation;
import choco.kernel.solver.constraints.integer.extension.LargeRelation;
import choco.kernel.solver.goals.Goal;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import choco.kernel.solver.search.ISolutionDisplay;
import choco.kernel.solver.search.ValIterator;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.search.checker.SolutionCheckerEngine;
import choco.kernel.solver.search.limit.AbstractGlobalSearchLimit;
import choco.kernel.solver.search.measure.IMeasures;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;
import choco.kernel.solver.variables.real.RealConstant;
import choco.kernel.solver.variables.real.RealVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.kernel.solver.variables.set.SetVar;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;


/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 12 mars 2008
 * Time: 16:43:08
 * Interface for Solver class, declare main expected methods.
 */
public interface Solver extends ISolutionDisplay, IMeasures, IPretty {

	public static final SolutionCheckerEngine DEFAULT_SOLUTION_CHECKER = new SolutionCheckerEngine();

	/**
	 * Reference to an object for logging trace statements related to Abtract
	 * Solver (using the java.util.logging package)
	 */
	public final static Logger LOGGER = ChocoLogging.getEngineLogger();


	/**
	 * Removes all of the elements from this solver (optional operation).
	 * The solver will be 'empty' after this call returns.
	 */
	public void clear();

	public IndexFactory getIndexfactory();

	public Model getModel();

	public void setModel(Model model);

	public void generateSearchStrategy();

    /**
     * @deprecated calls {@link choco.kernel.solver.Solver#clearGoals()} then
     * {@link Solver#addGoal(choco.kernel.solver.branch.AbstractIntBranchingStrategy)}
     */
    @Deprecated 
	public void attachGoal(AbstractIntBranchingStrategy branching);

    /**
     * Adds a new branching goal to the end of list of goals
     * @param branching new goal to add
     */
	public void addGoal(AbstractIntBranchingStrategy branching);

    /**
     * Clears the list of goals defined
     */
    public void clearGoals();


	/**
	 * Check wether every decisions variables are instantiated
	 * @return true if all variables are instantiated
	 */
	public boolean checkDecisionVariables();
	/**
	 * commands the strategy to start
	 */
	public void launch();

	/**
	 * returns the number of solutions encountered during the search
	 *
	 * @return the number of solutions to the model that were encountered during the search
	 */

	public int getNbSolutions();

	/**
	 * Monitor the time limit (default to true)
	 * @param b indicates wether the search stategy monitor the time limit
	 */
	@Deprecated
	public void monitorTimeLimit(boolean b);

	/**
	 * Monitor the node limit (default to true)
	 * @param b indicates wether the search stategy monitor the node limit
	 */
	@Deprecated
	public void monitorNodeLimit(boolean b);

	/**
	 * Monitor the backtrack limit (default to false)
	 * @param b indicates wether the search stategy monitor the backtrack limit
	 */
	@Deprecated
	public void monitorBackTrackLimit(boolean b);

	/**
	 * Monitor the fail limit (default to false)
	 * @param b indicates wether the search stategy monitor the fail limit
	 */
	public void monitorFailLimit(boolean b);

	/**
	 * Sets the time limit i.e. the maximal time before stopping the search algorithm
	 */
	public void setTimeLimit(int timeLimit);

	/**
	 * Sets the node limit i.e. the maximal number of nodes explored by the search algorithm
	 */
	public void setNodeLimit(int nodeLimit);

	/**
	 * Sets the backtrack limit i.e. the maximal number of backtracks before stopping the search algorithm
	 */
	public void setBackTrackLimit(int backtracklimit);

	/**
	 * Sets the fail limit i.e. the maximal numnber of fails before stopping the search algorithm
	 */
	public void setFailLimit(int failLimit);

	/**
	 * Sets the restart limit i.e. the maximal number of restart performed during the search algorithm.
	 * The limit does not stop the search only the restart process.
	 */
	public void setRestartLimit(int restartLimit);

	/**
	 * @return true if only the first solution must be found
	*/
	public boolean getFirstSolution();

	/**
	 * Sets wether only the first solution must be found
	 */
	public void setFirstSolution(boolean firstSolution);

	/**
	 * Sets the integer variable selector the search olver should use.
	 */
	public void setVarIntSelector(VarSelector<IntDomainVar> varSelector);

	/**
	 * Sets the real variable selector the search strategy should use.
	 */
	public void setVarRealSelector(VarSelector<RealVar> realVarSelector);

	/**
	 * Sets the set variable selector the search strategy should use.
	 */
	public void setVarSetSelector(VarSelector<SetVar> setVarIntSelector);

	/**
	 * Sets the integer value iterator the search should use
	 */
	public void setValIntIterator(ValIterator<IntDomainVar> valIterator);

	/**
	 * Sets the real value iterator the search should use
	 */
	public void setValRealIterator(ValIterator<RealVar> realValIterator);

	/**
	 * Sets the integer value iterator the search should use
	 */
	public void setValSetIterator(ValIterator<SetVar> valIterator);

	/**
	 * Sets the integer value selector the search should use
	 */
	public void setValIntSelector(ValSelector<IntDomainVar> valSelector);

	/**
	 * Sets the integer value selector the search should use
	 */
	public void setValRealSelector(ValSelector<RealVar> valSelector);

	/**
	 * Sets the integer value selector the search should use
	 */
	public void setValSetSelector(ValSelector<SetVar> setValIntSelector);

	/**
	 * @deprecated
	 */
	@Deprecated
	public DisposableIterator<SConstraint> getIntConstraintIterator();

	public DisposableIterator<SConstraint> getConstraintIterator();

	public DisposableIterator<IntDomainVar> getIntVarIterator();

	public DisposableIterator<SetVar> getSetVarIterator();

	public DisposableIterator<RealVar> getRealVarIterator();

	public DisposableIterator<TaskVar> getTaskVarIterator();

	/**
	 * Returns the propagation engine associated to the model
	 */

	public PropagationEngine getPropagationEngine();

	/**
	 * set the optimization strategy:
	 * - restart or not after each solution found
	 *
	 * @param restart
	 */
	public void setRestart(boolean restart);

	/**
	 * a boolean indicating if the strategy minize or maximize the objective function
	 *
	 * @param doMaximize
	 */
	public void setDoMaximize(boolean doMaximize);

	/**
	 * Set the variable to optimize
	 *
	 * @param objective variable to optimize
	 */
	public void setObjective(Var objective);

	public Var getObjective();

	public boolean isOptimizationSolver();

	public Number getOptimumValue();

	/**
	 * set the scheduling horizon.
	 * @param horizon scheduling horizon
	 * @deprecated
	 */
	void setHorizon(int horizon);

	/**
	 * get the scheduling horizon.
	 * @deprecated
	 */
	int getHorizon();

	/**
	 * Get the makespan variable if any
	 * @return makespan variable
	 */
	IntDomainVar getMakespan();

	/**
	 * get the makespan value or +inf.
	 * @return makespan value
	 */
	int getMakespanValue();

	/**
	 * Checks if a limit has been encountered
	 * @return a boolean
	 */
	public boolean isEncounteredLimit();

	/**
	 * If a limit has been encountered, return the involved limit
	 * @return the limit encountered
	 */
	public AbstractGlobalSearchLimit getEncounteredLimit();

	public AbstractGlobalSearchStrategy getSearchStrategy();

	public abstract void post(SConstraint c);

	public abstract void postCut(SConstraint c);

	/**
	 * <i>Network management:</i>
	 * Retrieve a variable by its index (all integer variables of
	 * the model are numbered in sequence from 0 on)
	 *
	 * @param i index of the variable in the model
	 * @return number of integer variables
	 */

	public IntDomainVar getIntVar(int i);

	IntDomainVar getIntVarQuick(int i);

	public int getIntVarIndex(IntVar c);

	/**
	 * retrieving the total number of variables
	 *
	 * @return the total number of variables in the model
	 */
	public int getNbVars();

	/**
	 * retrieving the total number of integer variables
	 *
	 * @return the total number of integer variables in the model
	 */
	public int getNbIntVars();

	/**
	 * retrieving the total number of constants
	 *
	 * @return the total number of constants in the model
	 */
	public int getNbConstants();

	/**
	 * Returns the constant corresponding to the int i.
	 *
	 * @param i object (value) of the constant represented as an instantiated "variable"
	 * @return the constant corresponding to the object i.
	 */
	public Var getIntConstant(int i);

	/**
	 * Returns the constant corresponding to the real i.
	 *
	 * @param i object (value) of the constant represented as an instantiated "variable"
	 * @return the constant corresponding to the object i.
	 */
	public Var getRealConstant(double i);

	/**
	 * Returns the collection of integer constant values
	 * @return the set of values
	 */
	public Collection<Integer> getIntConstantSet();

	/**
	 * Returns the collection of real constant values
	 * @return the set of values
	 */
	public Collection<Double> getRealConstantSet();

	/**
	 * Returns a real variable.
	 *
	 * @param i index of the variable
	 * @return the i-th real variable
	 */
	public RealVar getRealVar(int i);

	RealVar getRealVarQuick(int i);

	/**
	 * Returns the number of variables modelling real numbers.
	 * @return number of real variables
	 */
	public int getNbRealVars();

	/**
	 * Returns a set variable.
	 *
	 * @param i index of the variable
	 * @return the i-th real variable
	 */
	public SetVar getSetVar(int i);

	SetVar getSetVarQuick(int i);

	/**
	 * Returns the number of variables modelling real numbers.
	 * @return number of set variables
	 */
	public int getNbSetVars();


	/**
	 * Returns a task variable.
	 *
	 * @param i index of the variable
	 * @return the i-th task variable
	 */
	public TaskVar getTaskVar(int i);

	TaskVar getTaskVarQuick(int i);

	/**
	 * Returns the number of variables modelling tasks.
	 * @return actual number of task vars
	 */
	public int getNbTaskVars();

	/**
	 * Returns the number of variables modelling boolean.
	 * @return the number of boolean variables.
	 */
	public int getNbBooleanVars();

	/**
	 * get the list of decision integer variables.
	 *
	 * @return an unmodifiable list
	 */

	IntDomainVar[] getIntDecisionVars();

	/**
	 * get the list of decision set variables.
	 *
	 * @return an unmodifiable list
	 */
	SetVar[] getSetDecisionVars();

	/**
	 * get the list of decision real variables.
	 *
	 * @return an unmodifiable list
	 */
	RealVar[] getRealDecisionVars();

	/**
	 * get the list of decision task variables.
	 *
	 * @return an unmodifiable list
	 */
	TaskVar[] getTaskDecisionVars();

	/**
	 * Set the precision of the search for a real model.
	 * @param precision the new precision
	 */
	public void setPrecision(double precision);
	/**
	 * Get the precision of the search for a real model.
	 * @return the actual precision
	 */
	public double getPrecision();

	/**
	 * Set the minimal width reduction between two propagations.
	 * @param reduction new width reduction
	 */
	public void setReduction(double reduction);

	/**
	 * Get the minimal width reduction between two propagations.
	 * @return width reduction
	 */
	public double getReduction();


	/**
	 * <i>Propagation:</i>
	 * Computes consistency on the model (the model may no longer
	 * be consistent since the last propagation because of listeners
	 * that have been posted and variables that have been reduced
	 *
	 * @throws ContradictionException
	 */
	public void propagate() throws ContradictionException;


	public Boolean maximize(boolean restart);

	public Boolean minimize(boolean restart);

	public Boolean maximize(Var obj, boolean restart);

	public Boolean minimize(Var obj, boolean restart);

	public void setSolutionPoolCapacity(int capacity);

	public void printRuntimeStatistics();

	public String runtimeStatistics();
	/**
	 * set the maximal search depth for logging statements
	 * @see ChocoLogging
	 */
	public void setLoggingMaxDepth(int loggingMaxDepth);

	/**
	 * get the maximal search depth for logging statements
	 * @see ChocoLogging
	 */
	public int getLoggingMaxDepth();

	/**
	 * pushing one world on the stack
	 */
	public void worldPush();

	/**
	 * popping one world from the stack:
	 * overrides AbstractModel.worldPop because the Model class adds
	 * the notion of static constraints that need be repropagated upon backtracking
	 */
	public void worldPop();

	/**
	 * Backtracks to a given level in the search tree.
	 * @param n number of world to pop
	 */
	public void worldPopUntil(int n);

	/**
	 * pushing the world during propagation
	 */
	public void worldPushDuringPropagation();

	/**
	 * poping the world during propagation
	 */
	public void worldPopDuringPropagation();


	/**
	 * Record a solution by getting every variables' value.
	 *
	 * @return the recorded solution
	 */
	public Solution recordSolution();

	/**
	 * Set Custom pretty print of solutions
	 * @param solutionDisplay the solution printer
	 */
	void setSolutionDisplay(ISolutionDisplay solutionDisplay);
	
	/**
	 * Restore a solution by setting value to every variable
	 * @param sol solution to restore
	 */
	public void restoreSolution(Solution sol);


	/**
	 * Override the default configuration
	 * @param configuration the configuration
	 */
	//public void setConfiguration(Configuration configuration);

	/**
	 * Return the current configuration of the solver
	 * @return the configuration
	 */
	public Configuration getConfiguration();

	/**
	 * Returns the memory environment used by the model.
	 * @return memory environment
	 */

	public IEnvironment getEnvironment();

	public void setFeasible(Boolean b);

	/**
	 * returning the index of the current worl
	 * @return current world index
	 */
	public int getWorldIndex();

	public void eraseConstraint(SConstraint c);

	/**
	 * retrieving the total number of constraints over integers
	 *
	 * @return the total number of constraints over integers in the model
	 */
	public int getNbConstraints();

	/**
	 * retrieving the total number of constraints
	 *
	 * @return the total number of constraints in the model
	 */
	public int getNbIntConstraints();

	/**
	 * <i>Network management:</i>
	 * Retrieve a constraint by its index.
	 *
	 * @param i index of the constraint in the model
	 * @deprecated
	 * @return the ith constraint
	 */

	@Deprecated
	public AbstractIntSConstraint getIntConstraint(int i);

	public abstract IntExp plus(IntExp v1, int v2);

	public abstract IntExp plus(int v1, IntExp v2);

	public abstract IntExp plus(IntExp v1, IntExp v2);

	public SConstraint lt(IntExp x, int c);

	public SConstraint lt(int c, IntExp x);

	public SConstraint lt(IntExp x, IntExp y);

	public SConstraint leq(IntExp x, int c);

	public SConstraint leq(int c, IntExp x);

	public SConstraint leq(IntExp x, IntExp y);

	public SConstraint geq(IntExp x, int c);

	public SConstraint geq(int c, IntExp x);

	public SConstraint geq(IntExp x, IntExp y);

	public SConstraint eq(IntExp x, IntExp y);

	public SConstraint eq(IntExp x, int c);

	public SConstraint eq(int c, IntExp x);

	public SConstraint eq(RealVar r, IntDomainVar i);

	public SConstraint gt(IntExp x, IntExp y);

	public SConstraint gt(IntExp x, int c);

	public SConstraint gt(int c, IntExp x);

	public SConstraint neq(IntExp x, int c);

	public SConstraint neq(int c, IntExp x);

	public SConstraint neq(IntExp x, IntExp y);

	public IntExp scalar(int[] lc, IntDomainVar[] lv);

	public IntExp scalar(IntDomainVar[] lv, int[] lc);

	public IntExp sum(IntExp...lv);

	public void read(Model m);

	public Boolean solve(boolean all);

	public Boolean solve();

	public Boolean solveAll();

	public Boolean isFeasible();

	/**
	 * Solution checker.
	 * Usefull for debug and development.
	 * @return a boolean indicating wether the solution is correct or not.
	 */
	public Boolean checkSolution();

	public Boolean nextSolution();

	public <MV extends Variable, SV extends Var> SV _to(MV mv, SV sv);

	public <MV extends Variable, SV extends Var> SV[] _to(MV[] mv, SV[] sv);

	public <MV extends Variable, SV extends Var> SV getVar(MV v);

	public <MV extends Variable, SV extends Var> SV[] getVar(Class<SV> clazz, MV[] mv);

	public IntDomainVar getVar(IntegerVariable v);

	public IntDomainVar[] getVar(IntegerVariable...v);

	public RealVar getVar(RealVariable v);

	public RealVar[] getVar(RealVariable... v);

	public SetVar getVar(SetVariable v);

	public SetVar[] getVar(SetVariable... v);

	public TaskVar getVar(TaskVariable v);

	public TaskVar[] getVar(TaskVariable... v);

	public SConstraint getCstr(Constraint ic);

	public void setIlogGoal(Goal ilogGoal);


	public IntDomainVar createIntVar(String name, int domainType, int min, int max);

	public IntDomainVar createBooleanVar(String name);

	public IntDomainVar createEnumIntVar(String name, int min, int max);

	public IntDomainVar createBoundIntVar(String name, int min, int max);

	public IntDomainVar createBinTreeIntVar(String name, int min, int max);

	public IntDomainVar createEnumIntVar(String name, int[] sortedValues);

	public IntDomainVar createBinTreeIntVar(String name, int[] sortedValues);

	public RealVar createRealVal(String name, double min, double max);

	public RealConstant createRealIntervalConstant(double a, double b);

	/**
	 * Makes a constant interval from a double d ([d,d]).
	 * @param d double
	 * @return constant interval
	 */
	public RealConstant cst(double d);

	/**
	 * Makes a constant interval between two doubles [a,b].
	 * @param a lower bound
	 * @param b upper bound
	 * @return constant interval
	 */
	public RealConstant cst(double a, double b);


	public SetVar createSetVar(String name, int a, int b, int domainType);

	public SetVar createBoundSetVar(String name, int a, int b);

	public SetVar createEnumSetVar(String name, int a, int b);

	public TaskVar createTaskVar(String name, IntDomainVar start, IntDomainVar end, IntDomainVar duration);

	public IntDomainVar createIntegerConstant(String name, int val);

	public RealConstant createRealConstant(String name, double val);

	public void setCardReasoning(boolean creas);

	public LargeRelation makeLargeRelation(int[] min, int[] max, List<int[]> tuples, boolean feas);

	public LargeRelation makeLargeRelation(int[] min, int[] max, List<int[]> tuples, boolean feas, int scheme);

	public BinRelation makeBinRelation(int[] min, int[] max, List<int[]> mat, boolean feas, boolean bitset);

	public BinRelation makeBinRelation(int[] min, int[] max, List<int[]> mat, boolean feas);

	public SConstraint relationTupleAC(IntDomainVar[] vs, LargeRelation rela);

	public SConstraint relationTupleAC(IntDomainVar[] vs, LargeRelation rela, int ac);
}
