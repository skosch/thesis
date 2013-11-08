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

public class PCumulative extends PGlobalConstraint {
	private PTask[] tasks;

	private final int limit;

	public PCumulative(String name, PVariable[] scope, PTask[] tasks, int limit) {
		super(name, scope);
		this.tasks = tasks;
		for (PTask task : tasks)
			task.setVariablePositions(scope); 
		this.limit = limit;
	}

	public long computeCostOf(int[] tuple) {
		for (PTask task : tasks) {
			if (task.evaluate(tuple) == 1)
				return 1;
			//task.displayEvaluations();
		}
		for (int i = 0; i < tasks.length; i++) {
			for (int period = tasks[i].getOriginValue(); period < tasks[i].getEndValue(); period++) {
				int heightSum = tasks[i].getHeightValue();
				for (int j = i + 1; j < tasks.length; j++) {
					if (period >= tasks[j].getOriginValue() && period < tasks[j].getEndValue())
						heightSum += tasks[j].getHeightValue();
				}
				if (heightSum > limit) {
					//LOGGER.info(" i = " + i + " time = " + period);
					return 1;
				}
			}
		}
		return 0;
	}

	public String toString() {
		String s = super.toString() + " : cumulative\n\t";
        for (PTask task : tasks) {
            s += "  [origin=" + computeStringRepresentationOf(task.getOrigin()) + ' ' + "duration=" + computeStringRepresentationOf(task.getDuration()) + ' ';
            s += "end=" + computeStringRepresentationOf(task.getEnd()) + ' ' + "height=" + computeStringRepresentationOf(task.getHeight()) + "]\n\t";
        }
		s += "nbTasks=" + tasks.length + " limit=" + limit;
		return s;
	}

    public PTask[] getTasks() {
        return tasks;
}

    public void setTasks(PTask[] tasks) {
        this.tasks = tasks;
    }

    public int getLimit() {
        return limit;
    }
}
