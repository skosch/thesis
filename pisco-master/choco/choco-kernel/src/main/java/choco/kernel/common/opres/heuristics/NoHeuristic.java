package choco.kernel.common.opres.heuristics;

import choco.Choco;

public final class NoHeuristic implements IHeuristic {

	/** 
	 * The shared instance. 
	 */
	private final static NoHeuristic SINGLOTON = new NoHeuristic();

	/** 
	 * Private constructor. 
	 */
	private NoHeuristic() {
		super();
	}

	/** 
	 * Returns this shared instance. 
	 * 
	 * @returns The shared instance 
	 */
	public final static NoHeuristic getInstance() {
		return SINGLOTON;
	}
	
	@Override
	public void execute() {}
	
	@Override
	public void reset() {}

	@Override
	public Number getObjectiveValue() {
		return Choco.MAX_UPPER_BOUND;
	}

	@Override
	public boolean isObjectiveOptimal() {
		return false;
	}

	@Override
	public int getIterationCount() {
		return 0;
	}

	@Override
	public double getTimeCount() {
		return 0;
	}

	@Override
	public boolean hasSearched() {
		return false;
	}

	@Override
	public boolean existsSolution() {
		return false;
	}

	@Override
	public int getSolutionCount() {
		return 0;
	}
	
	@Override
	public String solutionToString() {
		return null;
	}
	
	
}