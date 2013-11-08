package choco.kernel.solver.variables.scheduling;

import gnu.trove.TIntArrayList;

import java.awt.Point;

public final class TimePeriodList implements ITimePeriodList {

	private final TIntArrayList timePeriods = new TIntArrayList(4);

	private int expendedDuration;

	@Override
	public final void reset() {
		timePeriods.resetQuick();
		expendedDuration = 0;
	}


	public void addTimeLength(int start, int length) {
		if( ! timePeriods.isEmpty() && start == timePeriods.getQuick(timePeriods.size()-2)) {
			timePeriods.setQuick(timePeriods.size()-1, start + length);
		} else {
			timePeriods.add(start);
			timePeriods.add( start + length);
		}
		expendedDuration += length;
	}

	public void addTimePeriod(int start, int end) {
		if( ! timePeriods.isEmpty() && start == getPeriodLast()) {
			timePeriods.setQuick(timePeriods.size()-1, end);
		} else {
			timePeriods.add(start);
			timePeriods.add(end);
		}
		expendedDuration += (end - start);
	}

	public final TIntArrayList getTimePeriods() {
		return timePeriods;
	}

	@Override
	public final int getExpendedDuration() {
		return expendedDuration;
	}

	@Override
	public final boolean isEmpty() {
		return timePeriods.isEmpty();
	}
	@Override
	public final int getTimePeriodCount() {
		return timePeriods.size()/2;
	}

	@Override
	public final Point getTimePeriod(int i) {
		final int offset = 2 * i;
		return new Point(timePeriods.get(offset), timePeriods.get(offset + 1));
	}

	public final int getPeriodFirst() {
		return timePeriods.get(0);
	}

	public final int getPeriodLast() {
		return timePeriods.get( timePeriods.size() - 1);
	}

	@Override
	public final int getPeriodStart(int i) {
		return timePeriods.get(2 * i);
	}

	@Override
	public final int getPeriodEnd(int i) {
		return timePeriods.get(2 * i + 1);
	}


	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append('[');
		if(! isEmpty()) {
			for (int i = 0; i < getTimePeriodCount(); i++) {
				b.append(getPeriodStart(i)).append("-").append(getPeriodEnd(i)).append(';');
			}
			b.deleteCharAt(b.length() - 1);
		}
		b.append(']');
		return b.toString();
	}





}