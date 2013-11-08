package choco.kernel.common.opres.heuristics;

import gnu.trove.TIntArrayList;

import java.util.Random;
import java.util.logging.Level;

import choco.kernel.common.TimeCacheThread;
import choco.kernel.solver.SolverException;


public abstract class AbstractRandomizedHeuristic implements IHeuristic {

	private int bestsol = Integer.MAX_VALUE;

	private long starth;

	private double timeCount;

	private double timeLimit = Integer.MAX_VALUE;

	private int iterationCount;

	protected int iterationLimit;

	/** solution log : <iteration, objective, seed> */
	private TIntArrayList solutionLogs;

	public AbstractRandomizedHeuristic() {
		super();
		this.solutionLogs = new TIntArrayList();
	}

	private int retrieveIteration() {
		return solutionLogs.getQuick(solutionLogs.size() - 3);
	}

	private int retrieveSeed() {
		return solutionLogs.getQuick(solutionLogs.size() - 1);
	}

	@Override
	public void reset() {
		bestsol = Integer.MAX_VALUE;
		solutionLogs.clear();
		timeCount = 0;
		iterationCount = 0;
		starth =  System.currentTimeMillis();
		TimeCacheThread.currentTimeMillis = starth;
	}

	protected final void forceStoreSolution(final int obj) {
		bestsol = obj;
		iterationCount = 1;
		storeSolution(Integer.MIN_VALUE);
	}
	
	protected final void storeSolution(final int seed) {
		if(LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "heuristics...[obj:{0}][iter:{1}]", new Object[]{Integer.valueOf(bestsol), iterationCount});
		}
		solutionLogs.add(iterationCount);
		solutionLogs.add(bestsol);
		solutionLogs.add(seed);
	}
	public abstract int getLowerBound();

	protected abstract int apply(int iteration, int bestsol,int seed);

	protected final int applySingleIteration(int iteration, int seed) {
		setIterationLimit(1);
		reset();
		bestsol = apply(iteration, Integer.MAX_VALUE, seed);
		iterationCount = 1;
		storeSolution(seed);
		timeCount = System.currentTimeMillis() - starth;
		return bestsol;
	}

	public final int apply(Random random) {
		reset();
		//run randomized heuristics
		while(iterationCount < iterationLimit && timeCount < timeLimit) {
			final int seed = random.nextInt();

			final int obj = apply(iterationCount, bestsol, seed);
			if(obj<bestsol) {
				bestsol = obj;
				storeSolution(seed);
				if(obj==getLowerBound()) {return bestsol;}
			}
			iterationCount++;
			timeCount = TimeCacheThread.currentTimeMillis - starth;
		}
		if(existsSolution()) {
			if( bestsol != apply(retrieveIteration(), Integer.MAX_VALUE, retrieveSeed())) {
				throw new SolverException("heuristics...[restore_solution][FAIL]");
			}	
		}
		timeCount = System.currentTimeMillis() - starth;
		return bestsol;
	}

	@Override
	public boolean isObjectiveOptimal() {
		return bestsol == getLowerBound();
	}
	//*****************************************************************//
	//*******************  Getters/Setters  **************************//
	//***************************************************************//


	/**
	 * in seconds
	 */
	public final void setTimeLimit(int timeLimit) {
		if(timeLimit > 0) this.timeLimit =  timeLimit * 1000;
	}

	public final void setIterationLimit(int iterationLimit) {
		if(iterationLimit > 0) this.iterationLimit = iterationLimit;
	}


	@Override
	public final int getIterationCount() {
		return iterationCount;
	}

	public int getBestIteration() {
		return existsSolution() ? retrieveIteration() : -1;
	}




	//*****************************************************************//
	//*******************  IAlgorithm ********************************//
	//***************************************************************//

	@Override
	public final double getTimeCount() {
		return timeCount /1000D;
	}



	@Override
	public final boolean hasSearched() {
		return iterationCount > 0;
	}



	/**
	 * heuristics always succeeds
	 */
	@Override
	public final boolean existsSolution() {
		return bestsol != Integer.MAX_VALUE;
	}

	@Override
	public final Number getObjectiveValue() {
		return Integer.valueOf(bestsol);
	}

	@Override
	public int getSolutionCount() {
		return solutionLogs.size()/3;
	}

	@Override
	public String solutionToString() {
		return null;
	}
	


}
