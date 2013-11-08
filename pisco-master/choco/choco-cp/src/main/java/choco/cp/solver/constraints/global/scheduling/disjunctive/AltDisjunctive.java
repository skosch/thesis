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

package choco.cp.solver.constraints.global.scheduling.disjunctive;
import choco.cp.solver.constraints.global.scheduling.AbstractResourceSConstraint;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.TaskVar;

/**
 * @author Arnaud Malapert</br> 
 * @since 2 mars 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public class AltDisjunctive extends Disjunctive {

	public AltDisjunctive(Solver solver, final String name, final TaskVar[] taskvars, final IntDomainVar[] usages, final IntDomainVar makespan) {
		super(solver, name, taskvars, usages.length, true, ArrayUtils.append(usages, new IntDomainVar[]{makespan}));
		rules = new AltDisjRules(rtasks, this.makespan, solver.getEnvironment());
	}

	@Override
	public void fireTaskRemoval(IRTask rtask) {
		rules.remove(rtask);
	}
	@Override
	public int getFilteredEventMask(int idx) {
		return idx < taskIntVarOffset || idx >= taskIntVarOffset + getNbOptionalTasks() ? 
				AbstractResourceSConstraint.TASK_MASK : IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
	}
	
	@Override
	public void awakeOnRem(int varIdx, int val) throws ContradictionException {
		//applying second, and first rule
		//To enable updating time window over a resource, if value removed from
		//main domain affects hypothetical domain time window (enables handling domains with gaps).
		if(varIdx < taskIntVarOffset){
			if(varIdx < startOffset){
				//start
				if(rtasks[varIdx].isOptional() && val == rtasks[varIdx].getHTask().getEST()){
					//Hypothetical boundary need to be updated
					if(val < vars[varIdx].getSup()){
						//Intersection exists, apply rule two
						final int newEST = vars[varIdx].getNextDomainValue(val);
						assert newEST > val;
						rtasks[varIdx].setEST(newEST);
					}else{
						//applying rule 1 as no intersection exists.
						rtasks[varIdx].remove();
						rtasks[varIdx].fireRemoval();
					}
				}
			}else if(varIdx < endOffset){
				//end
				if(rtasks[varIdx - startOffset].isOptional() && val == rtasks[varIdx - startOffset].getHTask().getLCT()){
					//Hypothetical boundary need to be updated
					if(val > vars[varIdx].getInf()){
						final int newLCT = vars[varIdx].getPrevDomainValue(val);
						assert newLCT < val;
						rtasks[varIdx - startOffset].setLCT(newLCT);
					}else{
						//applying rule 1 as no intersection exists.
						rtasks[varIdx - startOffset].remove();
						rtasks[varIdx - startOffset].fireRemoval();
					}
				}
			}
		}
		constAwake(false);
	}

	@Override
	public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain)
			throws ContradictionException {
		 if (deltaDomain != null) {
	            try {
	                for (; deltaDomain.hasNext();) {
	                    int val = deltaDomain.next();
	                    awakeOnRem(idx, val);
	                }
	            } finally {
	                deltaDomain.dispose();
	            }
	        }
	}

	@Override
	public void propagate() throws ContradictionException {
		//checkHypotheticalDomains();
		super.propagate();
	}
}
