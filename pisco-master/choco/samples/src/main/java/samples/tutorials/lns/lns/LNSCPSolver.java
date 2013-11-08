package samples.tutorials.lns.lns;

import choco.cp.solver.CPSolver;
import choco.cp.solver.configure.RestartFactory;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import choco.kernel.model.Model;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ResolutionPolicy;
import choco.kernel.solver.Solution;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.search.limit.Limit;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;

import java.util.*;

/**
 * A Large Neighborhood Search approach encapsulating a CP solver.
 * First, the CP solver computes an initial set of solutions.
 * Then, it is re-used to explore several neighborhoods around these solutions, in hope to locally improve these solutions.
 * The neighborhood of a solution is obtained by restricting the search space around the solution
 * e.g. by fixing some of the variables to their values in the solution and by relaxing the others.
 * todo 1 make LNSCPSolver implement Solver and remove the inheritance from CPSolver: aggregate the statistics of the encapsulated CPSolver
 * OR todo 2 make LNS a BranchingStrategy with restarts
 * @author Sophie Demassey
 */
public class LNSCPSolver extends CPSolver {

CPSolver solver;
int incumbent;
Collection<Neighborhood> neighborhoods;
Collection<Solution> solutions; // todo SolutionPool
boolean maximize;
LNSCPConfiguration lnsConfiguration;


public LNSCPSolver(Configuration configuration)
{
	this(new EnvironmentTrailing(), configuration);
}


public LNSCPSolver(IEnvironment env, Configuration configuration)
{
	super(env, configuration);
	solver = this;  // todo delegate rather than inherits
	//solver = new CPSolver(configuration);
	lnsConfiguration = (LNSCPConfiguration) configuration;
	neighborhoods = new PriorityQueue<Neighborhood>();
	solutions = new ArrayList<Solution>();
}

/**
 * read the model and load it in the delegate solver
 * @param model the model of the problem
 */
@Override
public void read(Model model)
{
	super.read(model); // todo delegate rather than inherits
	//solver.read(model);
}

/**
 * launch the large neighborhood search
 * @param maximize optimization direction: true if maximization , false if minimization
 * @param obj      the objective variable
 * @param restart  unconsidered
 * @return FALSE if infeasibility is proved, TRUE if at least one solution is found, null otherwise
 */
@Override
protected Boolean optimize(boolean maximize, Var obj, boolean restart)
{
	this.maximize = maximize;
	solver.setObjective(obj);

	Boolean first = initialSearch();
	if (Boolean.TRUE != first) {
		return first;
	}
	searchNeighborhoods();
	return Boolean.TRUE;
}

/**
 * add a neighborhood operator to apply to the solutions
 * @param operator the operator type to build the neighborhood
 * @param strategy the branching heuristic to explore the neighborhood // todo branching type rather than branching object
 * @param impact   the number of runs the operator will apply without improvement
 */
public void addNeighborhood(NeighborhoodOperator operator, AbstractIntBranchingStrategy strategy, int impact)
{
	addNeighborhood(new Neighborhood(operator, strategy, impact));
}

/**
 * add a neighborhood operator to apply to the solutions
 * using the default branching strategy and a default run number of 5
 * @param operator the operator type
 */
public void addNeighborhood(NeighborhoodOperator operator)
{
	addNeighborhood(new Neighborhood(operator, null, 5));
}

/**
 * add a neighborhood operator to apply to the solutions
 * @param neighborhood the operator
 */
public void addNeighborhood(Neighborhood neighborhood)
{
	neighborhoods.add(neighborhood);
}

/** define the default list of neighborhood operators. */
private void loadNeighborhoods()
{
	if (neighborhoods.isEmpty()) {
		if (solver.getNbIntVars() > 16) {
			addNeighborhood(new RandomNeighborhoodOperator(solver.getNbIntVars() / 8));
		}
		if (solver.getNbIntVars() > 8) {
			addNeighborhood(new RandomNeighborhoodOperator(solver.getNbIntVars() / 4));
		}
		addNeighborhood(new RandomNeighborhoodOperator(solver.getNbIntVars() / 2));
	}
}


/**
 * Initial step of LNS:
 * compute a first set of feasible solutions by launching a limited branch-and-bound on the solver.
 * First, the root of the search tree is duplicated thanks to a first empty branching (solver.worldPush())
 * and the solution process of the initial step is launched from the duplicated root node.
 * After this step, a backtrack to the initial root node (rootWorldIndex) will then ensure
 * the search tree and the solver to be clean for re-solving in the remaining steps.
 * @return FALSE if infeasibility is proved, TRUE if at least one solution is found, null otherwise
 */
private Boolean initialSearch()
{
	solver.getConfiguration().putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.SATISFACTION);
	Limit limit = lnsConfiguration.readEnum(LNSCPConfiguration.LNS_INIT_SEARCH_LIMIT, Limit.class);
	solver.getConfiguration().putEnum(Configuration.SEARCH_LIMIT, limit);
	solver.getConfiguration().putInt(Configuration.SEARCH_LIMIT_BOUND, lnsConfiguration.readInt(LNSCPConfiguration.LNS_INIT_SEARCH_LIMIT_BOUND));

	int objectiveBound = (maximize) ? ((IntDomainVar) solver.getObjective()).getSup() : ((IntDomainVar) solver.getObjective()).getInf();

	solver.worldPush();
	int rootWorldIndex = solver.getWorldIndex();

	Boolean first = solver.solve(false);
	if (Boolean.TRUE != first) {
		return first;
	}

	SConstraint objectiveCut = null;
	do {
		solutions.add(solver.getSearchStrategy().getSolutionPool().getBestSolution());
		incumbent = solver.getObjectiveValue().intValue();
		if (incumbent != objectiveBound) {
			if (objectiveCut != null) {
				solver.eraseConstraint(objectiveCut);
			}
			objectiveCut = (maximize) ? solver.gt((IntDomainVar) solver.getObjective(), incumbent) : solver.lt((IntDomainVar) solver.getObjective(), incumbent);
			solver.postCut(objectiveCut);
		}
	} while (incumbent != objectiveBound && Boolean.TRUE == solver.nextSolution());
	if (objectiveCut != null) {
		solver.eraseConstraint(objectiveCut);
	}
	solver.worldPopUntil(rootWorldIndex);

	return Boolean.TRUE;
}

/**
 * LNS main loop:
 * the neighborhood operators are applied in turn to the solutions in the pool
 * if incumbent is improved, then the impact of the operator is incremented and the new improving solution is added to the pool
 * otherwise the impact of the operator is decremented.
 * The operator is removed from the list when its impact becomes negative.
 * The loop stops after a given number of runs or when the operator list becomes empty.
 * @return TRUE if the initial incumbent is improved, and FALSE otherwise
 */
private Boolean searchNeighborhoods()
{
	int nbLNSRuns = configuration.readInt(LNSCPConfiguration.LNS_RUN_LIMIT_NUMBER);

	loadNeighborhoods();

	Queue<Solution> newSQueue = new ArrayDeque<Solution>();
	while (!neighborhoods.isEmpty() && nbLNSRuns > 0) {
		Iterator<Neighborhood> it = neighborhoods.iterator();
		while (it.hasNext()) {
			Neighborhood neighborhood = it.next();
			Boolean improve = false;
			for (Solution solution : solutions) {
				if (Boolean.TRUE == searchNeighborhood(neighborhood, solution, incumbent)) {
					improve = true;
					newSQueue.add(solver.getSearchStrategy().getSolutionPool().getBestSolution());
				}
			}
			if (!improve) {
				if (neighborhood.decreaseImpact() < 0) {
					it.remove();
				}
			} else {
				neighborhood.increaseImpact();
			}
		}
		while (!newSQueue.isEmpty()) {
			solutions.add(newSQueue.poll());
		}
		nbLNSRuns--;
	}
	return true;
}

/**
 * Explore one neighborhood defined by an operator applied to a solution:
 * the search space of the solver is restricted around the solution by the neighborhood operator,
 * and the objective is bounded by the value to improve upon.
 * The restricted search space is then explored by limited backtracking.
 * @param neighborhood the neighborhood operator
 * @param solution     the solution around which the neighborhood is explored
 * @param objToImprove the objective value to improve upon (ex: incumbent or solution objective value)
 * @return FALSE if infeasibility is proved, TRUE if one improving solution is found, null otherwise
 */
public Boolean searchNeighborhood(Neighborhood neighborhood, Solution solution, int objToImprove)
{
	Solver.LOGGER.info("\n START Neighborhood : try to improve upon " + objToImprove + "\n");

	int rootWorldIndex = solver.getWorldIndex();
	solver.worldPush();

	RestartFactory.cancelRestarts(solver);
	Limit limit = lnsConfiguration.readEnum(LNSCPConfiguration.LNS_NEIGHBORHOOD_SEARCH_LIMIT, Limit.class);
	solver.getConfiguration().putEnum(Configuration.SEARCH_LIMIT, limit);
	solver.getConfiguration().putInt(Configuration.SEARCH_LIMIT_BOUND, lnsConfiguration.readInt(LNSCPConfiguration.LNS_NEIGHBORHOOD_SEARCH_LIMIT_BOUND));
	solver.resetSearchStrategy();
	solver.clearGoals();
	if (neighborhood.getStrategy() != null) {
		solver.addGoal(neighborhood.getStrategy());
	}

	solver.post((maximize) ? solver.gt((IntVar) solver.getObjective(), objToImprove) : solver.lt((IntVar) solver.getObjective(), objToImprove));

	neighborhood.getOperator().restrictNeighborhood(solution);

	Boolean ok = solver.solve(false);
	if (Boolean.TRUE == ok && incumbent >= solver.getObjectiveValue().intValue()) {
		incumbent = solver.getObjectiveValue().intValue();
	}
	ChocoLogging.flushLogs();
	solver.worldPopUntil(rootWorldIndex);

	return ok;
}

}