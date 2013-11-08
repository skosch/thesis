package choco.kernel.solver.variables.scheduling;

import java.awt.Point;

public interface ITimePeriodList {
	
	void reset();
	
	int getExpendedDuration();
	
	boolean isEmpty();
	
	int getTimePeriodCount(); 

	Point getTimePeriod(int i);

	int getPeriodFirst();
	
	int getPeriodStart(int i);

	int getPeriodEnd(int i);
	
	int getPeriodLast();
}