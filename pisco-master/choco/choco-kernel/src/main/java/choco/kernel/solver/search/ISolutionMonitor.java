package choco.kernel.solver.search;

import choco.kernel.solver.Solver;

public interface ISolutionMonitor {

	public static ISolutionMonitor NO_MONITORING = NoMonitoring.SINGLOTON; 
	
	void recordSolution(Solver solver);
}

final class NoMonitoring implements ISolutionMonitor{

	public final static NoMonitoring SINGLOTON = new NoMonitoring();
	private NoMonitoring() {
		super();
	}

	@Override
	public void recordSolution(Solver solver) {}
	
}