package samples.tutorials.lns.lns;
/*
 * Created by IntelliJ IDEA.
 * User: sofdem - sophie.demassey{at}mines-nantes.fr
 * Date: 13/01/11 - 14:19
 */

import choco.kernel.solver.Solution;

/**
 * NeighborhoodOperator defines how to restrict the search space of a problem around a given solution
 * @author Sophie Demassey
 * @see LNSCPSolver
 */
public interface NeighborhoodOperator {

/**
 * add restrictions (constraints or variable fixing) to the solver associated to the solution
 * @param solution the solution to build the neighborhood around
 * @return true iff the search space is actually shrunken
 */
public boolean restrictNeighborhood(Solution solution);

}
