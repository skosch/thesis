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

package choco.cp.model.managers.constraints.global;

import static choco.kernel.common.util.tools.VariableUtils.getIntVar;
import static choco.kernel.common.util.tools.VariableUtils.getTaskVar;

import java.util.List;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.cumulative.AltCumulative;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.global.scheduling.ResourceParameters;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;


/**
 * @author Arnaud Malapert</br> 
 * @since 23 janv. 2009 version 2.0.3</br>
 * @version 2.0.1</br>
 */
public final class CumulativeManager extends AbstractResourceManager {

	@Override
	protected SConstraint makeConstraint(CPSolver s,
			Variable[] variables, ResourceParameters rdata, List<String> options) {
		final int n = rdata.getUsagesOffset();
		final TaskVar[] tasks = getTaskVar(s, variables, 0, n);
		final IntDomainVar[] usages = getIntVar(s, variables, n, rdata.getHeightsOffset());
		final IntDomainVar[] heights = getIntVar(s, variables, rdata.getHeightsOffset() ,rdata.getConsOffset());
		final IntDomainVar consumption = s.getVar( (IntegerVariable) variables[rdata.getConsOffset()]);
		final IntDomainVar capacity = s.getVar( (IntegerVariable) variables[rdata.getCapaOffset()]);
		final IntDomainVar horizon = getHorizon(s, variables, rdata);
		
		if(consumption.getSup() > capacity.getInf()) {
			s.post(s.leq(consumption, capacity));
		}

		final Cumulative cstr = (
				rdata.getNbOptionalTasks() > 0 ? 
						new AltCumulative(s, rdata.getRscName(), tasks, heights, usages, consumption, capacity, horizon) :
							new Cumulative(s, rdata.getRscName(), tasks, heights , consumption, capacity, horizon)
		);
		cstr.readOptions(options);
		return cstr;
	}

}
