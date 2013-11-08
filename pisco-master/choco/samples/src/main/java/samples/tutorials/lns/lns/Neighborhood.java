package samples.tutorials.lns.lns;

import choco.kernel.solver.branch.AbstractIntBranchingStrategy;

/**
 * Neighborhood defines how to build and to explore the search space of a problem around a solution
 * in hope to improve locally the solution
 * @author Sophie Demassey
 * @see LNSCPSolver
 */
public class Neighborhood implements Comparable {

//private Solution solution;
/** operator defines how to build the search space around a solution */
private NeighborhoodOperator operator;
/**
 * strategy defines how to explore this search space within a backtracking
 * todo: encapsulate the type of the heuristic rather than a heuristic attached to a solver
 */
private AbstractIntBranchingStrategy strategy;
/** impact is a performance indicator */
private int impact;

public Neighborhood(NeighborhoodOperator operator, AbstractIntBranchingStrategy strategy, int impact)
{
//	this.solution = solution;
	this.operator = operator;
	this.strategy = strategy;
	this.impact = impact;
}

public Neighborhood(NeighborhoodOperator operator)
{
	this(operator, null, 0);
}

/*public Solution getSolution()
{
	return solution;
}*/

public NeighborhoodOperator getOperator()
{
	return operator;
}

public AbstractIntBranchingStrategy getStrategy()
{
	return strategy;
}

/*
public void setSolution(Solution solution)
{
	this.solution = solution;
}
*/

@Override
public int compareTo(Object o)
{
	return (this.impact - ((Neighborhood) o).impact);
}

public int decreaseImpact()
{
	return --impact;
}

public int increaseImpact()
{
	return ++impact;
}

}
