package samples.tutorials.lns.lns;

import choco.kernel.solver.Solution;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntVar;
import gnu.trove.TIntHashSet;

import java.util.Random;

/**
 * A neighborhood operator which fix/unfix integer variables randomly
 * @author Sophie Demassey
 */
public class RandomNeighborhoodOperator implements NeighborhoodOperator {

protected Random random;
protected int nbRelaxedVars;
protected TIntHashSet selected;

/**
 * Constructs with a fixed seed.
 * @param nbRelaxedVars number of variables to relax
 */
public RandomNeighborhoodOperator(int nbRelaxedVars)
{
	this(nbRelaxedVars, 0);
}

/**
 * Constructs with a specified seed.
 * @param nbRelaxedVars number of variables to relax
 * @param seed          random seed
 */
public RandomNeighborhoodOperator(int nbRelaxedVars, long seed)
{
	random = new Random(seed);
	this.nbRelaxedVars = nbRelaxedVars;
	selected = new TIntHashSet();
}

/**
 * restrict the search space around the solution by selecting nbRelaxedVars variables randomly to let free
 * and by fixing all other integer variables to their value in solution
 * @param solution the solution to build the neighborhood around
 * @return true iff the search space is actually shrunken
 */
@Override
public boolean restrictNeighborhood(Solution solution)
{

	Solver solver = solution.getSolver();
	int nbTotalVars = solver.getNbIntVars() - 1;

	int n = nbTotalVars - nbRelaxedVars;
	boolean b = false;
	IntVar var;
	selected.clear();
	while (n > 0) {
		int index = random.nextInt(nbTotalVars);
		var = solver.getIntVarQuick(index);
		if (var != solver.getObjective() && !selected.contains(index)) {
			if (solution.getIntValue(index) != Solution.NULL) {
				solver.LOGGER.info("fix " + var);
				solver.post(solver.eq(var, solution.getIntValue(index)));
				b = true;
			}
			selected.add(index);
			n--;
		}
	}
	return b;
}

}
