/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package parser.absconparseur.components;

import parser.absconparseur.Toolkit;

public class PTask {
	private final Object origin; // may be null if absent, an Integer or a Variable

	private int originPositionInScope;

	private int originValue;

	private final Object duration;

	private int durationPositionInScope;

	private int durationValue;

	private final Object end;

	private int endPositionInScope;

	private int endValue;

	private final Object height;

	private int heightPositionInScope;

	private int heightValue;

	public Object getOrigin() {
		return origin;
	}

	public int getOriginValue() {
		return originValue;
	}

	public Object getDuration() {
		return duration;
	}

	public int getDurationValue() {
		return durationValue;
	}

	public Object getEnd() {
		return end;
	}

	public int getEndValue() {
		return endValue;
	}

	public Object getHeight() {
		return height;
	}

	public int getHeightValue() {
		return heightValue;
	}

	public PTask(Object origin, Object duration, Object end, Object height) {
		this.origin = origin;
		this.duration = duration;
		this.end = end;
		this.height = height;
	}

	public void setVariablePositions(Object[] scope) {
		this.originPositionInScope = Toolkit.searchFirstObjectOccurrenceIn(origin, scope);
		this.durationPositionInScope = Toolkit.searchFirstObjectOccurrenceIn(duration, scope);
		this.endPositionInScope = Toolkit.searchFirstObjectOccurrenceIn(end, scope);
		this.heightPositionInScope = Toolkit.searchFirstObjectOccurrenceIn(height, scope);
	}

	public int evaluate(int[] tuple) {
		if (origin != null)
			originValue = origin instanceof Integer ? (Integer) origin : tuple[originPositionInScope];
		if (duration != null)
			durationValue = duration instanceof Integer ? (Integer) duration : tuple[durationPositionInScope];
		if (end != null)
			endValue = end instanceof Integer ? (Integer) end : tuple[endPositionInScope];
		if (origin != null && duration != null && end != null && originValue + durationValue != endValue)
			return 1;
		if (origin == null)
			originValue = endValue - durationValue;
		if (end == null)
			endValue = originValue + durationValue;
		heightValue = height instanceof Integer ? (Integer) height : tuple[heightPositionInScope];
		return 0;
	}
	
	
	
	

//	public int getEarliestStartingTime() {
//		if (origin != null) 
//			return origin instanceof Integer ? ((Integer)origin).intValue() : ((Variable)origin).getDomain().getFirstValidValue();
//		int earliestFinishTime = 	end instanceof Integer ? ((Integer)end).intValue() : ((Variable)end).getDomain().getFirstValidValue();
//		int longestDuration = 	duration instanceof Integer ? ((Integer)duration).intValue() : ((Variable)duration).getDomain().getLastValidValue();
//		return 	earliestFinishTime - longestDuration;
//	}
//	
//	
//	public int getEarliestFinishTime() {
//		if (end != null) 
//			return end instanceof Integer ? ((Integer)end).intValue() : ((Variable)end).getDomain().getFirstValidValue();
//		int earliestStartingTime = 	origin instanceof Integer ? ((Integer)origin).intValue() : ((Variable)origin).getDomain().getFirstValidValue();
//		int shortestDuration = 	duration instanceof Integer ? ((Integer)duration).intValue() : ((Variable)duration).getDomain().getFirstValidValue();
//		return 	earliestStartingTime + shortestDuration;
//	}
	
	
	
	
	 public void displayEvaluations() {
	 System.out.println(originValue + " " + durationValue + ' ' + endValue + ' ' + heightValue);
	 }

	public String toString() {
		return " [origin=" + origin + " duration=" + duration + " end=" + end + " height=" + height + "]\n\t";
	}
}