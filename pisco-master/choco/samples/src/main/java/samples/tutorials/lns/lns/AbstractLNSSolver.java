package samples.tutorials.lns.lns;
/*
 * Created by IntelliJ IDEA.
 * User: sofdem - sophie.demassey{at}mines-nantes.fr
 * Date: 14/01/11 - 15:09
 */

import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.*;
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
import choco.kernel.solver.search.ValIterator;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.search.limit.AbstractGlobalSearchLimit;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;
import choco.kernel.solver.variables.real.RealConstant;
import choco.kernel.solver.variables.real.RealVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.kernel.solver.variables.set.SetVar;

import java.util.Collection;
import java.util.List;

/**
 * @author Sophie Demassey
 * @deprecated
 */
public abstract class AbstractLNSSolver implements Solver {

Solver solver;

@Override
public void clear()
{
	solver.clear();
	clearLNS();
}

protected abstract void clearLNS();

/* MODEL */

@Override
public Configuration getConfiguration() { return solver.getConfiguration(); }

@Override
public Model getModel() { return solver.getModel(); }

@Override
public void setModel(Model model) { solver.setModel(model);}

@Override
public void read(Model model) { solver.read(model); }


/* SOLVING */

@Override
public Boolean solve(boolean all)
{ throw new RuntimeException("LNS is an optimization solver: run optimize instead"); }

@Override
public Boolean solve()
{ throw new RuntimeException("LNS is an optimization solver: run optimize instead"); }

@Override
public Boolean solveAll()
{ throw new RuntimeException("LNS is an optimization solver: run optimize instead"); }

@Override
public void launch()
{ throw new RuntimeException("NO: redundant ? make private ?"); }

@Override
public Boolean nextSolution()
{ throw new RuntimeException("LNS is an optimization solver: run optimize instead"); }


protected abstract Boolean optimize(boolean maximize, Var obj, boolean restart);

@Override
public Boolean maximize(Var obj, boolean restart)
{ return optimize(true, obj, restart); }

@Override
public Boolean minimize(Var obj, boolean restart)
{ return optimize(false, obj, restart); }

@Override
public Boolean maximize(boolean restart)
{
	if (solver.getObjective() == null) {
		throw new SolverException("No objective variable defined");
	}
	return optimize(true, solver.getObjective(), restart);
}

@Override
public Boolean minimize(boolean restart)
{
	if (solver.getObjective() == null) {
		throw new SolverException("No objective variable defined");
	}
	return optimize(false, solver.getObjective(), restart);
}

@Override
public void setObjective(Var objective)
{ solver.setObjective(objective); }

@Override
public Var getObjective()
{ return solver.getObjective(); }

@Override
public boolean isOptimizationSolver()
{ throw new RuntimeException("Bof: no use there"); }

@Override
public void setFeasible(Boolean b)
{ throw new RuntimeException("Bof: no use there"); }


/* SOLUTION */


@Override
public abstract Boolean isFeasible();

@Override
public abstract boolean isObjectiveOptimal();

@Override
public abstract Number getObjectiveValue();

@Override
public Number getOptimumValue()
{ throw new RuntimeException("Deprecated"); }

@Override
public void setSolutionPoolCapacity(int capacity)
{ throw new RuntimeException("Deprecated"); }

@Override
public abstract Boolean checkSolution();

@Override
public abstract boolean existsSolution();

@Override
public abstract int getSolutionCount();

@Override
public abstract Solution recordSolution();

@Override
public abstract void restoreSolution(Solution sol);

@Override
public abstract boolean getFirstSolution();

@Override
public abstract void setFirstSolution(boolean firstSolution);

@Override
public abstract int getNbSolutions();

@Override
public abstract String solutionToString();


/* LIMIT */

// nbSolutionLimit ??
// setLimit(LimitType, int limit)

@Override
public abstract boolean isEncounteredLimit();

@Override
public abstract AbstractGlobalSearchLimit getEncounteredLimit();


@Override
public abstract int getTimeCount();

@Override
public void monitorTimeLimit(boolean b)
{ throw new RuntimeException("Deprecated"); }

@Override
public abstract void setTimeLimit(int timeLimit);


@Override
public abstract int getRestartCount();

@Override
public void monitorNodeLimit(boolean b)
{ throw new RuntimeException("Deprecated"); }

@Override
public abstract void setRestartLimit(int restartLimit);

@Override
public abstract int getNodeCount();

@Override
public abstract void setNodeLimit(int nodeLimit);

@Override
public abstract int getBackTrackCount();

@Override
public void monitorBackTrackLimit(boolean b)
{ throw new RuntimeException("Deprecated"); }

@Override
public abstract void setBackTrackLimit(int backtracklimit);

@Override
public abstract int getFailCount();

@Override
public void monitorFailLimit(boolean b)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public abstract void setFailLimit(int failLimit);


/* LOG */

@Override
public abstract void printRuntimeStatistics();

@Override
public abstract String runtimeStatistics();

@Override
public abstract String pretty();


/* PARAMETERS */

@Override
public void setPrecision(double precision)
{ throw new RuntimeException("Deprecated"); }

@Override
public double getPrecision()
{ throw new RuntimeException("Deprecated"); }

@Override
public void setReduction(double reduction)
{ throw new RuntimeException("Deprecated"); }

@Override
public double getReduction()
{ throw new RuntimeException("Deprecated"); }

@Override
public void setRestart(boolean restart)
{ throw new RuntimeException("Deprecated"); }

@Override
public void setDoMaximize(boolean doMaximize)
{ throw new RuntimeException("Deprecated"); }


/**
 * ******************************************************************************
 * ********************************************************************************
 *
 * SPECIFIC CPSolver
 *
 * ********************************************************************************
 * *******************************************************************************
 */


/* BRANCHING */
@Override
public AbstractGlobalSearchStrategy getSearchStrategy()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void generateSearchStrategy()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void attachGoal(AbstractIntBranchingStrategy branching)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void addGoal(AbstractIntBranchingStrategy branching)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void clearGoals()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void setIlogGoal(Goal ilogGoal)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void setLoggingMaxDepth(int loggingMaxDepth)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public int getLoggingMaxDepth()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void worldPush()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void worldPop()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void worldPopUntil(int n)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void worldPushDuringPropagation()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void worldPopDuringPropagation()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public int getWorldIndex()
{ throw new RuntimeException("NO: CP specific"); }


@Override
public void setVarIntSelector(VarSelector<IntDomainVar> varSelector)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void setVarRealSelector(VarSelector<RealVar> realVarSelector)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void setVarSetSelector(VarSelector<SetVar> setVarIntSelector)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void setValIntIterator(ValIterator<IntDomainVar> valIterator)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void setValRealIterator(ValIterator<RealVar> realValIterator)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void setValSetIterator(ValIterator<SetVar> valIterator)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void setValIntSelector(ValSelector<IntDomainVar> valSelector)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void setValRealSelector(ValSelector<RealVar> valSelector)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void setValSetSelector(ValSelector<SetVar> setValIntSelector)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public DisposableIterator<SConstraint> getIntConstraintIterator()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public DisposableIterator<SConstraint> getConstraintIterator()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public DisposableIterator<IntDomainVar> getIntVarIterator()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public DisposableIterator<SetVar> getSetVarIterator()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public DisposableIterator<RealVar> getRealVarIterator()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public DisposableIterator<TaskVar> getTaskVarIterator()
{ throw new RuntimeException("NO: CP specific"); }


/* PROPAGATION */

@Override
public PropagationEngine getPropagationEngine()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void propagate() throws ContradictionException
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IEnvironment getEnvironment()
{ throw new RuntimeException("NO: CP specific"); }


/* VARIABLES */

@Override
public boolean checkDecisionVariables()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntDomainVar getIntVar(int i)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntDomainVar getIntVarQuick(int i)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public int getIntVarIndex(IntVar c)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public int getNbIntVars()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public int getNbConstants()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public Var getIntConstant(int i)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public Var getRealConstant(double i)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public Collection<Integer> getIntConstantSet()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public Collection<Double> getRealConstantSet()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public RealVar getRealVar(int i)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public RealVar getRealVarQuick(int i)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public int getNbRealVars()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SetVar getSetVar(int i)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SetVar getSetVarQuick(int i)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public int getNbSetVars()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public TaskVar getTaskVar(int i)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public TaskVar getTaskVarQuick(int i)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public int getNbTaskVars()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public int getNbBooleanVars()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntDomainVar[] getIntDecisionVars()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SetVar[] getSetDecisionVars()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public RealVar[] getRealDecisionVars()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public TaskVar[] getTaskDecisionVars()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public <MV extends Variable, SV extends Var> SV _to(MV mv, SV sv)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public <MV extends Variable, SV extends Var> SV[] _to(MV[] mv, SV[] sv)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public <MV extends Variable, SV extends Var> SV getVar(MV v)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public <MV extends Variable, SV extends Var> SV[] getVar(Class<SV> clazz, MV[] mv)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntDomainVar getVar(IntegerVariable v)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntDomainVar[] getVar(IntegerVariable... v)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public RealVar getVar(RealVariable v)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public RealVar[] getVar(RealVariable... v)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SetVar getVar(SetVariable v)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SetVar[] getVar(SetVariable... v)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public TaskVar getVar(TaskVariable v)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public TaskVar[] getVar(TaskVariable... v)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntDomainVar createIntVar(String name, int domainType, int min, int max)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntDomainVar createBooleanVar(String name)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntDomainVar createEnumIntVar(String name, int min, int max)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntDomainVar createBoundIntVar(String name, int min, int max)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntDomainVar createBinTreeIntVar(String name, int min, int max)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntDomainVar createEnumIntVar(String name, int[] sortedValues)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntDomainVar createBinTreeIntVar(String name, int[] sortedValues)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public RealVar createRealVal(String name, double min, double max)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public RealConstant createRealIntervalConstant(double a, double b)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public RealConstant cst(double d)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public RealConstant cst(double a, double b)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SetVar createSetVar(String name, int a, int b, int domainType)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SetVar createBoundSetVar(String name, int a, int b)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SetVar createEnumSetVar(String name, int a, int b)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public TaskVar createTaskVar(String name, IntDomainVar start, IntDomainVar end, IntDomainVar duration)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntDomainVar createIntegerConstant(String name, int val)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public RealConstant createRealConstant(String name, double val)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void setCardReasoning(boolean creas)
{ throw new RuntimeException("NO: CP specific"); }


/* CONSTRAINTS */

@Override
public void post(SConstraint c)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void postCut(SConstraint c)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public void eraseConstraint(SConstraint c)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public int getNbIntConstraints()
{ throw new RuntimeException("NO: CP specific"); }

@Override
public AbstractIntSConstraint getIntConstraint(int i)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntExp plus(IntExp v1, int v2)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntExp plus(int v1, IntExp v2)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntExp plus(IntExp v1, IntExp v2)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint lt(IntExp x, int c)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint lt(int c, IntExp x)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint lt(IntExp x, IntExp y)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint leq(IntExp x, int c)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint leq(int c, IntExp x)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint leq(IntExp x, IntExp y)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint geq(IntExp x, int c)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint geq(int c, IntExp x)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint geq(IntExp x, IntExp y)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint eq(IntExp x, IntExp y)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint eq(IntExp x, int c)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint eq(int c, IntExp x)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint eq(RealVar r, IntDomainVar i)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint gt(IntExp x, IntExp y)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint gt(IntExp x, int c)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint gt(int c, IntExp x)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint neq(IntExp x, int c)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint neq(int c, IntExp x)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint neq(IntExp x, IntExp y)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntExp scalar(int[] lc, IntDomainVar[] lv)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntExp scalar(IntDomainVar[] lv, int[] lc)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public IntExp sum(IntExp... lv)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint getCstr(Constraint ic)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public LargeRelation makeLargeRelation(int[] min, int[] max, List<int[]> tuples, boolean feas)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public LargeRelation makeLargeRelation(int[] min, int[] max, List<int[]> tuples, boolean feas, int scheme)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public BinRelation makeBinRelation(int[] min, int[] max, List<int[]> mat, boolean feas, boolean bitset)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public BinRelation makeBinRelation(int[] min, int[] max, List<int[]> mat, boolean feas)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint relationTupleAC(IntDomainVar[] vs, LargeRelation rela)
{ throw new RuntimeException("NO: CP specific"); }

@Override
public SConstraint relationTupleAC(IntDomainVar[] vs, LargeRelation rela, int ac)
{ throw new RuntimeException("NO: CP specific"); }

/* SCHEDULING */

@Override
public void setHorizon(int horizon)
{ throw new RuntimeException("NO: CP/Scheduling specific"); }

@Override
public int getHorizon()
{ throw new RuntimeException("NO: CP/Scheduling specific"); }

@Override
public IntDomainVar getMakespan()
{ throw new RuntimeException("NO: CP/Scheduling specific"); }

@Override
public int getMakespanValue()
{ throw new RuntimeException("NO: CP/Scheduling specific"); }

}
