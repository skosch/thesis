package choco.kernel.common.opres.pack;

import gnu.trove.TIntProcedure;

public final class ComputeL0 extends AbstractComponentDDFF implements TIntProcedure {

	private int totalSize; 
	
	
	public ComputeL0() {
		super();
	}
	
	public ComputeL0(int capacity) {
		super();
		setCapacity(capacity);
	}
	public void reset() {
		totalSize = 0;
	}
	public int getLowerBound() {
		//Ceil to positive integers
		return totalSize > 0 ? (totalSize - 1)/capacity + 1 : 0;
	}

	@Override
	public boolean execute(int arg0) {
		if(arg0>0) {
			totalSize += arg0;
			return true;
		} else return false;
	}
}